package dev.shabzup.wanderlust;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.*;
import java.util.*;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.LoggerFactory;

final class Settings {
    enum Kind { NUMBER, TEXT, TOGGLE, CHOICE }
    record Option(String owner, String key, String label, String initial, double min, double max, double step,
                  Kind kind, List<String> choices) {
        boolean numeric() { return kind == Kind.NUMBER; }
    }

    static final List<Option> options = new ArrayList<>();
    private static final Properties values = new Properties();
    private static final Map<String, Double> numbers = new HashMap<>();
    private static final Set<String> active = java.util.concurrent.ConcurrentHashMap.newKeySet();
    static long moduleRevision;
    private static final Set<String> transientKeys = new HashSet<>();
    private static final Path path = FabricLoader.getInstance().getConfigDir().resolve("wanderlust.properties");

    static void numeric(String module, String key, String label, double initial, double min, double max, double step) {
        register(new Option(module, key, label, format(initial), min, max, step, Kind.NUMBER, List.of()));
    }

    static void string(String module, String key, String label, String initial) {
        register(new Option(module, key, label, initial, 0, 0, 0, Kind.TEXT, List.of()));
    }
    static void secret(String module,String key,String label,String initial){string(module,key,label,initial);transientKeys.add(key);}

    static void toggle(String module, String key, String label, boolean initial) {
        register(new Option(module, key, label, Boolean.toString(initial), 0, 0, 0, Kind.TOGGLE, List.of()));
    }

    static void choice(String module, String key, String label, String initial, String... choices) {
        register(new Option(module, key, label, initial, 0, 0, 0, Kind.CHOICE, List.of(choices)));
    }

    private static void register(Option option) {
        if (options.stream().anyMatch(existing -> existing.key.equals(option.key))) return;
        options.add(option);
    }

    static String format(double number) {
        return java.math.BigDecimal.valueOf(number).stripTrailingZeros().toPlainString();
    }

    static void load() {
        values.clear();
        numbers.clear();
        active.clear();
        if (Files.isRegularFile(path)) {
            try (Reader reader = Files.newBufferedReader(path)) {
                values.load(reader);
            } catch (IOException | IllegalArgumentException e) {
                LoggerFactory.getLogger("WanderlustClient").warn("Could not read {}", path, e);
            }
        }
        for (Option option : options) {
            String value = values.getProperty(option.key, option.initial);
            if(transientKeys.contains(option.key))value=option.initial;
            if (option.numeric()) {
                double parsed;
                try { parsed = Double.parseDouble(value); }
                catch (NumberFormatException ignored) { parsed = Double.NaN; }
                if (!Double.isFinite(parsed)) parsed = Double.parseDouble(option.initial);
                setNumber(option, parsed);
            } else if (option.kind == Kind.CHOICE && !option.choices.contains(value)) {
                values.setProperty(option.key, option.initial);
            } else if (option.kind == Kind.TOGGLE && !value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) {
                values.setProperty(option.key, option.initial);
            } else values.setProperty(option.key, value);
        }
        for (Feature feature : Feature.values()) {
            if (Boolean.parseBoolean(values.getProperty("enabled." + feature.id))) active.add(feature.id);
        }
        ClientFeedback.invalidate();
        moduleRevision++;
    }

    static boolean on(String id) { return active.contains(id); }
    static double number(String key) { return numbers.get(key); }
    static String text(String key) { return values.getProperty(key, ""); }
    static boolean toggle(String key) { return Boolean.parseBoolean(values.getProperty(key, "false")); }
    static String choice(String key) { return values.getProperty(key, ""); }
    static void put(String key, String value) { values.setProperty(key, value); }

    static void setEnabled(Feature feature, boolean enabled) {
        boolean changed = enabled ? active.add(feature.id) : active.remove(feature.id);
        if (!changed) return;
        moduleRevision++;
        values.setProperty("enabled." + feature.id, Boolean.toString(enabled));
        save();
        ClientFeedback.changed(feature, enabled);
    }

    static void setNumber(Option option, double next) {
        double clamped = Math.clamp(next, option.min, option.max);
        clamped=Math.rint(clamped*1_000_000)/1_000_000;
        numbers.put(option.key, clamped);
        values.setProperty(option.key, format(clamped));
    }

    static void setText(Option option, String text) { values.setProperty(option.key, text); }
    static void setToggle(Option option, boolean enabled) { values.setProperty(option.key, Boolean.toString(enabled)); }
    static void setChoice(Option option, String choice) {
        if (option.choices.contains(choice)) values.setProperty(option.key, choice);
    }

    static synchronized void save() {
        try {
            Files.createDirectories(path.getParent());
            Path temp = path.resolveSibling(path.getFileName() + ".tmp");
            Properties saved=new Properties();saved.putAll(values);for(String key:transientKeys)saved.remove(key);
            try (Writer writer = Files.newBufferedWriter(temp)) { saved.store(writer, "WanderlustClient"); }
            try { Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE); }
            catch (AtomicMoveNotSupportedException e) { Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING); }
        } catch (IOException e) {
            LoggerFactory.getLogger("WanderlustClient").warn("Could not save {}", path, e);
        }
    }
}
