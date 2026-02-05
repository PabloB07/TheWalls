package ca.thewalls;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public final class LobbyHolograms {
    private final TheWalls plugin;
    private Object manager;
    private boolean available;
    private boolean warned;
    private final Map<String, Object> holograms = new HashMap<>();
    private final java.util.Set<String> spawned = new java.util.HashSet<>();

    public LobbyHolograms(TheWalls plugin) {
        this.plugin = plugin;
        setup();
    }

    public boolean isAvailable() {
        return available;
    }

    public void refreshAll() {
        if (!available || plugin.arenas == null) return;
        for (Arena arena : plugin.arenas.getArenas().values()) {
            updateArena(arena);
        }
    }

    public void updateArena(Arena arena) {
        if (!available || arena == null) return;
        Location lobby = arena.getLobby();
        String id = getId(arena);
        if (lobby == null) {
            remove(id);
            return;
        }

        Object hologram = holograms.get(id);
        if (hologram == null) {
            hologram = createTextHologram(id);
            if (hologram == null) return;
            holograms.put(id, hologram);
        }

        int minPlayers = Config.data.getInt("lobby.minPlayers", 2);
        int countdown = arena.getLobbyCountdown();
        String status = countdown >= 0
                ? Messages.raw("hologram.starting", java.util.Map.of("seconds", String.valueOf(countdown)))
                : Messages.raw("hologram.waiting");
        String text = Messages.raw("hologram.lobby", java.util.Map.of(
                "arena", arena.getName(),
                "current", String.valueOf(arena.getPlayers().size()),
                "min", String.valueOf(minPlayers),
                "status", status
        ));

        Location display = lobby.clone().add(0.0, 2.2, 0.0);
        setMiniMessageText(hologram, text);
        setLocation(hologram, display);
        if (!spawned.contains(id)) {
            remove(id);
            spawn(hologram, display);
            spawned.add(id);
        }
    }

    public void shutdown() {
        if (!available) return;
        for (String id : new java.util.ArrayList<>(holograms.keySet())) {
            remove(id);
        }
        holograms.clear();
        spawned.clear();
    }

    public void removeArena(Arena arena) {
        if (!available || arena == null) return;
        remove(getId(arena));
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

    private String getId(Arena arena) {
        return "thewalls_lobby_" + arena.getName().toLowerCase();
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

    private void remove(String id) {
        if (manager == null || id == null) return;
        tryInvoke(manager, "remove", id, true);
        tryInvoke(manager, "remove", id);
        tryInvoke(manager, "delete", id);
        holograms.remove(id);
        spawned.remove(id);
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
