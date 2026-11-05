package dev.shabzup.wanderlust;

import java.util.Set;
import net.minecraft.client.Minecraft;

final class BaritoneBridge {
    private static final Set<String> conflicting = new java.util.HashSet<>(Set.of("flight", "boat-fly", "elytra-fly", "elytra-boost",
        "speed", "step", "jesus", "no-fall", "fast-climb", "blink", "free-look", "kill-aura", "criticals",
        "crystal-aura", "anchor-aura", "bed-aura", "bow-aimbot", "bow-spam", "auto-clicker",
        "auto-mend", "auto-breed", "auto-shearer", "auto-fish"));
    static {
        Set<String> compatible=Set.of("freecam","auto-totem","z-auto-totem","auto-armor","offhand","hitboxes",
            "auto-sign","stash-finder","sign-search","ore-sim","ambience","auto-feed","auto-heal","auto-repair","build-pos");
        for(Feature feature:Feature.values())if((feature.nativeGroup==Feature.Group.Movement||feature.nativeGroup==Feature.Group.Combat||feature.nativeGroup==Feature.Group.World)&&!compatible.contains(feature.id))conflicting.add(feature.id);
        conflicting.addAll(Set.of("rotation","reach","no-rotate","attribute-swap","no-interact","air-place"));
    }
    private static Object baritone;
    private static boolean driving, ownsPause;
    private static Object connection;
    private static String status = "Baritone is not installed";
    private static boolean checked;

    private static Object call(Object object, String api, String method, Class<?>[] args, Object... values) throws ReflectiveOperationException {
        return Class.forName("baritone.api." + api).getMethod(method, args).invoke(object, values);
    }

    private static Object call(Object object, String api, String method) throws ReflectiveOperationException {
        return call(object, api, method, new Class<?>[0]);
    }

    static boolean available() { return baritone != null; }
    static void shutdown() {
        if(baritone==null)return;
        try {
            // Meteor's Baritone distribution leaves its cache worker executor alive.
            // Run only after Minecraft has closed, when no more pathing/cache work is submitted.
            for(var method:Class.forName("baritone.Baritone").getMethods()){
                if(java.lang.reflect.Modifier.isStatic(method.getModifiers())&&method.getParameterCount()==0
                    &&java.util.concurrent.Executor.class.isAssignableFrom(method.getReturnType())){
                    Object executor=method.invoke(null);
                    if(executor instanceof java.util.concurrent.ExecutorService service){
                        service.shutdown();
                        try{if(!service.awaitTermination(500,java.util.concurrent.TimeUnit.MILLISECONDS))service.shutdownNow();}
                        catch(InterruptedException interrupted){service.shutdownNow();Thread.currentThread().interrupt();}
                    }
                }
            }
        }catch(ReflectiveOperationException|LinkageError failure){
            org.slf4j.LoggerFactory.getLogger("WanderlustClient").warn("Could not stop optional Baritone workers: {}",failure.getClass().getSimpleName());
        }
    }
    static String status() { return status; }
    static boolean yields(String id) { return driving && conflicting.contains(id); }

    static void tick(Minecraft mc) {
        try {
            if (baritone == null) {
                if (checked) return;
                checked = true;
                var api = Class.forName("baritone.api.BaritoneAPI");
                Object provider = api.getMethod("getProvider").invoke(null);
                baritone = call(provider, "IBaritoneProvider", "getPrimaryBaritone");
            }
            if (connection != mc.getConnection()) { ownsPause = false; connection = mc.getConnection(); }
            boolean needPause = mc.player != null && (Settings.on("freecam") || PlayerTools.holdingFood(mc));
            if (needPause && !ownsPause && driving) ownsPause = command("pause");
            if (!needPause && ownsPause) { command("resume"); ownsPause = false; }
            var behavior = call(baritone, "IBaritone", "getPathingBehavior");
            driving = mc.player != null && !ownsPause && ((boolean)call(behavior, "behavior.IPathingBehavior", "isPathing")
                || ((java.util.Optional<?>)call(behavior, "behavior.IPathingBehavior", "getInProgress")).isPresent());
            status = ownsPause ? "Paused for Freecam / eating" : driving ? "Navigating. Conflicting modules yield." : "Ready";
        } catch (ClassNotFoundException e) {
            driving = false;
        } catch (ReflectiveOperationException | LinkageError e) {
            driving = false;
            status = "Baritone API unavailable for this build";
        }
    }

    static boolean command(String input) {
        if (baritone == null || Minecraft.getInstance().player == null) return false;
        try {
            Object commands = call(baritone, "IBaritone", "getCommandManager");
            return (boolean)call(commands, "command.manager.ICommandManager", "execute", new Class<?>[]{String.class}, input);
        } catch (ReflectiveOperationException | RuntimeException e) { return false; }
    }

    static boolean userCommand(String input) {
        if (Set.of("pause", "resume", "cancel", "stop").contains(input.trim())) ownsPause = false;
        return command(input);
    }
}
