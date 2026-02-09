package ca.thewalls.Listeners;

import ca.thewalls.Config;
import ca.thewalls.TheWalls;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.InventoryHolder;

public class ArenaProtection implements Listener {
    private final TheWalls walls;

    public ArenaProtection(TheWalls walls) {
        this.walls = walls;
    }

    private boolean shouldProtect(Player p) {
        if (p == null) return false;
        if (!Config.data.getBoolean("protections.enabled", true)) return false;
        ca.thewalls.Arena arena = walls.getArenaByPlayer(p);
        return arena != null;
    }

    private boolean allowBuild(Player p) {
        ca.thewalls.Arena arena = walls.getArenaByPlayer(p);
        if (arena == null) return true;
        if (arena.getGame().started) {
            return Config.data.getBoolean("protections.allowBuildInGame", true);
        }
        return Config.data.getBoolean("protections.allowBuildInLobby", false);
    }

    private boolean allowInteract(Player p) {
        ca.thewalls.Arena arena = walls.getArenaByPlayer(p);
        if (arena == null) return true;
        if (arena.getGame().started) {
            return Config.data.getBoolean("protections.allowInteractInGame", true);
        }
        return Config.data.getBoolean("protections.allowInteractInLobby", false);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBreak(BlockBreakEvent e) {
        if (!shouldProtect(e.getPlayer())) return;
        if (!allowBuild(e.getPlayer())) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlace(BlockPlaceEvent e) {
        if (!shouldProtect(e.getPlayer())) return;
        if (!allowBuild(e.getPlayer())) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onIgnite(BlockIgniteEvent e) {
        if (!Config.data.getBoolean("protections.enabled", true)) return;
        if (Config.data.getBoolean("protections.preventFireIgnite", true)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onExplode(EntityExplodeEvent e) {
        if (!Config.data.getBoolean("protections.enabled", true)) return;
        if (Config.data.getBoolean("protections.preventBlockExplosions", true)) {
            e.blockList().clear();
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (!shouldProtect(p)) return;
        if (allowInteract(p)) return;
        Block block = e.getClickedBlock();
        if (block != null && block.getState() instanceof InventoryHolder) {
            e.setCancelled(true);
        }
    }
}
