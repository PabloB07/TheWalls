package ca.thewalls.Listeners;

import ca.thewalls.Config;
import ca.thewalls.Cosmetics;
import ca.thewalls.TheWalls;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerTrail implements Listener {
    private final TheWalls walls;
    private final Map<UUID, Location> last = new ConcurrentHashMap<>();

    public PlayerTrail(TheWalls walls) {
        this.walls = walls;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (!Cosmetics.isTrailEnabled()) return;
        ca.thewalls.Arena arena = walls.getArenaByPlayer(p);
        if (arena == null) return;
        if (!arena.getGame().started && !Config.data.getBoolean("cosmetics.trails.showInLobby", false)) return;
        if (p.getGameMode() == org.bukkit.GameMode.SPECTATOR) return;
        Location from = e.getFrom();
        Location to = e.getTo();
        if (to == null) return;
        double step = Config.data.getDouble("cosmetics.trails.stepDistance", 0.6);
        Location prev = last.get(p.getUniqueId());
        if (prev != null && prev.getWorld() == to.getWorld() && prev.distanceSquared(to) < (step * step)) {
            return;
        }
        if (from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY() && from.getBlockZ() == to.getBlockZ()) {
            return;
        }
        last.put(p.getUniqueId(), to.clone());
        Cosmetics.playTrail(p);
    }
}
