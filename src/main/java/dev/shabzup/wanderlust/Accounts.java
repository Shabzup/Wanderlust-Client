package dev.shabzup.wanderlust;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.yggdrasil.ProfileResult;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.raphimc.minecraftauth.MinecraftAuth;
import net.raphimc.minecraftauth.java.JavaAuthManager;
import net.raphimc.minecraftauth.msa.service.impl.DeviceCodeMsaAuthService;

final class Accounts {
    record Entry(String name, JavaAuthManager auth) {}
    static final List<Entry> entries = new ArrayList<>();
    private static SessionAccess.Session launcher;
    private static Thread worker;
    static String status = "", verificationUrl = "", verificationCode = "";
    static boolean busy;
    private static int generation;
    private static volatile boolean dirty;
    private static final Set<JavaAuthManager> watched = Collections.newSetFromMap(new IdentityHashMap<>());

    static void init() {
        loadSaved();
        ScreenEvents.AFTER_INIT.register((mc, screen, w, h) -> {
            if (screen instanceof TitleScreen) Screens.getWidgets(screen).add(
                Button.builder(Component.literal("Wanderlust"), b -> mc.gui.setScreen(new ControlPanel()))
                    .bounds(6, 6, 104, 20).build());
        });
    }

    static void loadSaved() {
        entries.clear();
        entries.addAll(AccountStore.load());
        for (Entry entry : entries) watch(entry.auth);
        boolean migrated = false;
        for (String name : Settings.text("accounts.offline").split(",")) {
            if (name.matches("[A-Za-z0-9_]{1,16}") && entries.stream().noneMatch(e -> e.auth == null && e.name.equals(name))) {
                entries.add(new Entry(name, null));
                migrated = true;
            }
        }
        if (!AccountStore.error.isBlank()) status = AccountStore.error;
        else if (migrated) saveNames();
        else if (!entries.isEmpty()) status = "Restored " + entries.size() + " saved accounts.";
        dirty = false;
    }

    static boolean canSwitch() {
        var mc = Minecraft.getInstance();
        return mc.level == null && mc.getConnection() == null;
    }

    static void saveNames() {
        Settings.put("accounts.offline", String.join(",", entries.stream().filter(e -> e.auth == null).map(Entry::name).toList()));
        Settings.save();
        if (!AccountStore.save(List.copyOf(entries))) status = AccountStore.error;
        dirty = false;
    }

    private static void watch(JavaAuthManager auth) {
        if (auth != null && watched.add(auth)) auth.getChangeListeners().add(
            (net.raphimc.minecraftauth.util.holder.listener.BasicChangeListener) () -> dirty = true);
    }

    static void remember(String name, JavaAuthManager auth) {
        var profile = auth.getMinecraftProfile().getCached();
        entries.removeIf(entry -> entry.auth == auth || entry.auth != null && (entry.name.equals(name)
            || profile != null && entry.auth.getMinecraftProfile().getCached() != null
                && profile.getId().equals(entry.auth.getMinecraftProfile().getCached().getId())));
        entries.add(new Entry(name, auth));
        watch(auth);
        dirty = true;
    }

    static void tick() {
        if (dirty && !busy) saveNames();
    }

    static void shutdown() {
        cancel();
        if (dirty) saveNames();
    }

    static void addOffline(String name) {
        if (!name.matches("[A-Za-z0-9_]{1,16}")) { status = "Use 1-16 letters, digits or underscores."; return; }
        if (entries.stream().noneMatch(e -> e.auth == null && e.name.equals(name))) entries.add(new Entry(name, null));
        status = "Saved offline username. Select it below.";
        saveNames();
    }

    private static void apply(SessionAccess.Session session) {
        var mc = Minecraft.getInstance();
        if (!canSwitch()) { status = "Disconnect before switching accounts."; return; }
        var access = (SessionAccess)mc;
        if (launcher == null) launcher = access.wanderlust$capture();
        access.wanderlust$apply(session);
        status = "Active account: " + mc.getUser().getName();
    }

    static void restore() {
        if (launcher != null && canSwitch()) apply(launcher);
        else status = canSwitch() ? "Launcher account is already active." : "Disconnect before switching accounts.";
    }

    static void use(Entry entry) {
        if (!canSwitch() || busy) { status = "Disconnect before switching accounts."; return; }
        if (entry.auth != null) {
            int pending = generation + 1;
            start(() -> {
                var session = microsoftSession(entry.auth);
                Minecraft.getInstance().execute(() -> { if (pending == generation) remember(session.user().getName(), entry.auth); });
                return session;
            });
            return;
        }
        UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + entry.name).getBytes(StandardCharsets.UTF_8));
        var offline = YggdrasilAuthenticationService.createOffline(Minecraft.getInstance().getProxy());
        apply(new SessionAccess.Session(new User(entry.name, uuid, "0", Optional.empty(), Optional.empty()),
            UserApiService.OFFLINE, CompletableFuture.completedFuture(new ProfileResult(new GameProfile(uuid, entry.name))),
            CompletableFuture.completedFuture(UserApiService.OFFLINE_PROPERTIES), offline.createFriendsService("0")));
    }

    private static SessionAccess.Session microsoftSession(JavaAuthManager auth) throws Exception {
        var profile = auth.getMinecraftProfile().getUpToDate();
        String token = auth.getMinecraftToken().getUpToDate().getToken();
        var service = new YggdrasilAuthenticationService(Minecraft.getInstance().getProxy());
        var api = service.createUserApiService(token);
        var fullProfile = service.createMinecraftSessionService().fetchProfile(profile.getId(), true);
        if (fullProfile == null) fullProfile = new ProfileResult(new GameProfile(profile.getId(), profile.getName()));
        return new SessionAccess.Session(new User(profile.getName(), profile.getId(), token, Optional.of(auth.getMsaApplicationConfig().getClientId()), Optional.empty()),
            api, CompletableFuture.completedFuture(fullProfile),
            CompletableFuture.completedFuture(api.fetchProperties()), service.createFriendsService(token));
    }

    interface LoginTask { SessionAccess.Session run() throws Exception; }

    private static void start(LoginTask task) {
        if (busy) return;
        busy = true;
        int request = ++generation;
        status = "Contacting Microsoft...";
        worker = Thread.ofVirtual().name("Wanderlust account sign-in").start(() -> {
            try {
                var session = task.run();
                Minecraft.getInstance().execute(() -> { if (request == generation) { busy = false; apply(session); saveNames(); } });
            } catch (Exception e) {
                Minecraft.getInstance().execute(() -> {
                    if (request == generation) { busy = false; status = "Sign-in failed (" + e.getClass().getSimpleName() + "). Try again."; }
                });
            }
        });
    }

    static void login() {
        if (!canSwitch() || busy) { status = "Disconnect before signing in."; return; }
        int pending = generation + 1;
        start(() -> {
            var auth = JavaAuthManager.create(MinecraftAuth.createHttpClient()).<java.util.function.Consumer<net.raphimc.minecraftauth.msa.model.MsaDeviceCode>>login(DeviceCodeMsaAuthService::new,
                (net.raphimc.minecraftauth.msa.model.MsaDeviceCode code) ->
                    Minecraft.getInstance().execute(() -> {
                        if (pending != generation) return;
                        verificationUrl = code.getVerificationUri();
                        verificationCode = code.getUserCode();
                        status = "Open the Microsoft page and enter the code.";
                    }));
            var session = microsoftSession(auth);
            Minecraft.getInstance().execute(() -> {
                if (pending != generation) return;
                remember(session.user().getName(), auth);
            });
            return session;
        });
    }

    static void cancel() {
        ++generation;
        if (worker != null) worker.interrupt();
        busy = false;
        verificationCode = verificationUrl = "";
    }
}
