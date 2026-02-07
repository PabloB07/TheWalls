package ca.thewalls.Listeners;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

import ca.thewalls.TheWalls;

public class PlayerRespawn implements Listener {
    private final TheWalls walls;

    public PlayerRespawn(TheWalls walls) {
        this.walls = walls;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent e) {
        ca.thewalls.Arena arena = walls.getArenaByPlayer(e.getPlayer());
        if (arena == null) return;
        if (!arena.getGame().started) return;

        Location deathLoc = arena.getGame().getLastDeathLocation(e.getPlayer());
        if (deathLoc != null) {
            e.setRespawnLocation(deathLoc);
        }
        e.getPlayer().setGameMode(GameMode.SPECTATOR);
        e.getPlayer().getInventory().clear();
    }
}
