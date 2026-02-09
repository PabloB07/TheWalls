package ca.thewalls;

import me.arcaniax.hdb.api.DatabaseLoadEvent;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class HeadDatabaseHook implements Listener {
    private static HeadDatabaseAPI api;
    private static boolean ready = false;
    private static final java.util.Map<String, ItemStack> CACHE = new java.util.concurrent.ConcurrentHashMap<>();
    private final TheWalls plugin;

    public HeadDatabaseHook(TheWalls plugin) {
        this.plugin = plugin;
        if (plugin.getServer().getPluginManager().getPlugin("HeadDatabase") != null) {
            tryInit();
        }
    }

    @EventHandler
    public void onDatabaseLoad(DatabaseLoadEvent e) {
        tryInit();
    }

    private void tryInit() {
        try {
            api = new HeadDatabaseAPI();
            ready = true;
            plugin.getLogger().info("[Cosmetics] HeadDatabase detected and hooked.");
        } catch (Exception ex) {
            ready = false;
            api = null;
        }
    }

    public static boolean isReady() {
        return ready && api != null;
    }

    public static ItemStack getHead(String id) {
        if (!isReady() || id == null || id.isEmpty()) return null;
        try {
            return api.getItemHead(id);
        } catch (Exception ex) {
            return null;
        }
    }

    public static ItemStack getHeadCached(String id) {
        if (id == null || id.isEmpty()) return null;
        ItemStack cached = CACHE.get(id);
        if (cached != null) return cached.clone();
        ItemStack head = getHead(id);
        if (head != null) {
            CACHE.put(id, head);
            return head.clone();
        }
        return null;
    }

    public static void clearCache() {
        CACHE.clear();
    }
}
