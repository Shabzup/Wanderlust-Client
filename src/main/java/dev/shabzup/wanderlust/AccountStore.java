package dev.shabzup.wanderlust;

import com.google.gson.*;
import com.sun.jna.Platform;
import com.sun.jna.platform.win32.Crypt32Util;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import net.fabricmc.loader.api.FabricLoader;
import net.raphimc.minecraftauth.MinecraftAuth;
import net.raphimc.minecraftauth.java.JavaAuthManager;

/** Account credentials are encrypted with Windows DPAPI for the current OS user. */
final class AccountStore {
    private static final byte[] MAGIC = "WLAC1".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] ENTROPY = "WanderlustClient/accounts/v1".getBytes(StandardCharsets.UTF_8);
    private static final Path path = FabricLoader.getInstance().getConfigDir().resolve("wanderlust-accounts.dat");
    private static boolean readFailed;
    static String error = "";

    static List<Accounts.Entry> load() {
        readFailed = false;
        error = "";
        if (!Files.isRegularFile(path)) return List.of();
        byte[] plain = null;
        try {
            requireWindows();
            if (Files.size(path) > 8 * 1024 * 1024) throw new IllegalArgumentException("Vault size");
            byte[] file = Files.readAllBytes(path);
            if (file.length <= MAGIC.length || !Arrays.equals(Arrays.copyOf(file, MAGIC.length), MAGIC))
                throw new IllegalArgumentException("Vault format");
            plain = Crypt32Util.cryptUnprotectData(Arrays.copyOfRange(file, MAGIC.length, file.length), ENTROPY, 1, null);
            JsonObject root = JsonParser.parseString(new String(plain, StandardCharsets.UTF_8)).getAsJsonObject();
            if (root.get("version").getAsInt() != 1) throw new IllegalArgumentException("Vault version");
            List<Accounts.Entry> result = new ArrayList<>();
            for (JsonElement element : root.getAsJsonArray("accounts")) {
                JsonObject account = element.getAsJsonObject();
                String name = account.get("name").getAsString();
                if (name.isBlank() || name.length() > 64) throw new IllegalArgumentException("Account name");
                String type = account.get("type").getAsString();
                JavaAuthManager auth;
                if (type.equals("offline") && name.matches("[A-Za-z0-9_]{1,16}")) auth = null;
                else if (type.equals("microsoft")) auth = JavaAuthManager.fromJson(MinecraftAuth.createHttpClient(), account.getAsJsonObject("auth"));
                else throw new IllegalArgumentException("Account type");
                result.add(new Accounts.Entry(name, auth));
            }
            return List.copyOf(result);
        } catch (Exception | LinkageError failure) {
            readFailed = true;
            error = "Cannot read saved accounts (" + failure.getClass().getSimpleName() + "). Existing vault preserved.";
            return List.of();
        } finally {
            if (plain != null) Arrays.fill(plain, (byte)0);
        }
    }

    static boolean save(List<Accounts.Entry> entries) {
        if (readFailed) return false;
        byte[] plain = null;
        try {
            requireWindows();
            JsonObject root = new JsonObject();
            root.addProperty("version", 1);
            JsonArray accounts = new JsonArray();
            for (Accounts.Entry entry : entries) {
                JsonObject account = new JsonObject();
                account.addProperty("name", entry.name());
                account.addProperty("type", entry.auth() == null ? "offline" : "microsoft");
                if (entry.auth() != null) account.add("auth", JavaAuthManager.toJson(entry.auth()));
                accounts.add(account);
            }
            root.add("accounts", accounts);
            plain = root.toString().getBytes(StandardCharsets.UTF_8);
            byte[] encrypted = Crypt32Util.cryptProtectData(plain, ENTROPY, 1, "Wanderlust saved accounts", null);
            byte[] file = Arrays.copyOf(MAGIC, MAGIC.length + encrypted.length);
            System.arraycopy(encrypted, 0, file, MAGIC.length, encrypted.length);
            Files.createDirectories(path.getParent());
            Path temp = path.resolveSibling(path.getFileName() + ".tmp");
            Files.write(temp, file);
            try { Files.move(temp, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
            catch (AtomicMoveNotSupportedException unsupported) { Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING); }
            error = "";
            return true;
        } catch (Exception | LinkageError failure) {
            error = "Could not save encrypted accounts (" + failure.getClass().getSimpleName() + ").";
            return false;
        } finally {
            if (plain != null) Arrays.fill(plain, (byte)0);
        }
    }

    private static void requireWindows() {
        if (!Platform.isWindows()) throw new UnsupportedOperationException("Windows DPAPI required");
    }
}
