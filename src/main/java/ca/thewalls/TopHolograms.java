package ca.thewalls;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class TopHolograms {
    private final TheWalls plugin;
    private Object manager;
    private boolean available;
    private boolean warned;
    private Object hologram;
    private boolean spawned;

    public TopHolograms(TheWalls plugin) {
        this.plugin = plugin;
        setup();
    }

    public boolean isAvailable() {
        return available;
    }

    public void refresh() {
        if (!available) return;
        String arenaName = Config.data.getString("holograms.top.arena", null);
        if (arenaName == null || arenaName.isEmpty()) {
            remove();
            return;
        }
        Arena arena = plugin.arenas.getArena(arenaName);
        if (arena == null || arena.getLobby() == null) {
            remove();
            return;
        }

        if (hologram == null) {
            hologram = createTextHologram(getId());
            if (hologram == null) return;
        }

        Location display = arena.getLobby().clone().add(0.0, 2.6, 0.0);
        String text = buildText();
        setMiniMessageText(hologram, text);
        setLocation(hologram, display);
        if (!spawned) {
            remove();
            spawn(hologram, display);
            spawned = true;
        }
    }

    public void setArena(String arenaName) {
        if (arenaName == null || arenaName.isEmpty()) return;
        Config.data.set("holograms.top.arena", arenaName.toLowerCase());
        try {
            Config.data.save(Config.dataFile);
        } catch (java.io.IOException ex) {
            plugin.getLogger().warning(ex.toString());
        }
        refresh();
    }

    public void remove() {
        if (manager == null) return;
        tryInvoke(manager, "remove", getId(), true);
        tryInvoke(manager, "remove", getId());
        tryInvoke(manager, "delete", getId());
        spawned = false;
    }

    public void shutdown() {
        if (!available) return;
        remove();
    }

    private String getId() {
        return "thewalls_top";
    }

    private void setup() {
        if (Bukkit.getPluginManager().getPlugin("HologramLib") == null) {
            available = false;
            return;
        }
        try {
            Class<?> api = Class.forName("com.maximde.hologramlib.HologramLib");
            Object provider = api.getMethod("getManager").invoke(null);
            if (provider instanceof java.util.Optional<?> opt) {
                manager = opt.orElse(null);
            } else {
                manager = provider;
            }
            available = manager != null;
        } catch (Exception ex) {
            warnOnce("Failed to hook into HologramLib: " + ex.getClass().getSimpleName());
            available = false;
        }
    }

    private Object createTextHologram(String id) {
        try {
            Class<?> text = Class.forName("com.maximde.hologramlib.hologram.TextHologram");
            return text.getConstructor(String.class).newInstance(id);
        } catch (Exception ex) {
            warnOnce("Unable to create HologramLib TextHologram: " + ex.getClass().getSimpleName());
            return null;
        }
    }

    private String buildText() {
        List<String> lines = new ArrayList<>();
        lines.add(Messages.raw("hologram.top.title"));
        lines.add(Messages.raw("hologram.top.arenas_header"));
        List<Map.Entry<String, Integer>> arenas = new ArrayList<>(Config.getArenaPlays().entrySet());
        arenas.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
        if (arenas.isEmpty()) {
            lines.add(Messages.raw("hologram.top.empty"));
        } else {
            int limit = Math.min(3, arenas.size());
            for (int i = 0; i < limit; i++) {
                Map.Entry<String, Integer> entry = arenas.get(i);
                lines.add(Messages.raw("hologram.top.arenas_item", Map.of(
                        "pos", String.valueOf(i + 1),
                        "arena", entry.getKey(),
                        "plays", String.valueOf(entry.getValue())
                )));
            }
        }
        lines.add(Messages.raw("hologram.top.kills_header"));
        List<Map.Entry<String, Integer>> kills = Config.getTopKills(3);
        if (kills.isEmpty()) {
            lines.add(Messages.raw("hologram.top.empty"));
        } else {
            for (int i = 0; i < kills.size(); i++) {
                Map.Entry<String, Integer> entry = kills.get(i);
                lines.add(Messages.raw("hologram.top.kills_item", Map.of(
                        "pos", String.valueOf(i + 1),
                        "player", entry.getKey(),
                        "kills", String.valueOf(entry.getValue())
                )));
            }
        }
        return String.join("\n", lines);
    }

    private void setMiniMessageText(Object hologram, String text) {
        if (hologram == null || text == null) return;
        if (tryInvoke(hologram, "setMiniMessageText", text)) return;
        tryInvoke(hologram, "setText", text);
    }

    private void setLocation(Object hologram, Location location) {
        if (hologram == null || location == null) return;
        if (tryInvoke(hologram, "teleport", location)) return;
        if (tryInvoke(hologram, "setLocation", location)) return;
        tryInvoke(hologram, "setPosition", location);
    }

    private void spawn(Object hologram, Location location) {
        if (manager == null || hologram == null) return;
        if (tryInvoke(manager, "spawn", hologram, location, false, true)) return;
        if (tryInvoke(manager, "spawn", hologram, location, false)) return;
        if (tryInvoke(manager, "spawn", hologram, location)) return;
        tryInvoke(manager, "spawn", hologram);
    }

    private boolean tryInvoke(Object target, String name, Object... args) {
        for (Method method : target.getClass().getMethods()) {
            if (!method.getName().equals(name)) continue;
            Class<?>[] params = method.getParameterTypes();
            if (params.length != args.length) continue;
            if (!areCompatible(params, args)) continue;
            try {
                method.invoke(target, args);
                return true;
            } catch (Exception ignored) {
                return false;
            }
        }
        return false;
    }

    private boolean areCompatible(Class<?>[] params, Object[] args) {
        for (int i = 0; i < params.length; i++) {
            Object arg = args[i];
            if (arg == null) continue;
            Class<?> param = wrapPrimitive(params[i]);
            if (!param.isAssignableFrom(arg.getClass())) return false;
        }
        return true;
    }

    private Class<?> wrapPrimitive(Class<?> type) {
        if (!type.isPrimitive()) return type;
        if (type == boolean.class) return Boolean.class;
        if (type == int.class) return Integer.class;
        if (type == long.class) return Long.class;
        if (type == double.class) return Double.class;
        if (type == float.class) return Float.class;
        if (type == short.class) return Short.class;
        if (type == byte.class) return Byte.class;
        if (type == char.class) return Character.class;
        return type;
    }

    private void warnOnce(String message) {
        if (warned) return;
        warned = true;
        plugin.getLogger().warning(message);
    }
}
