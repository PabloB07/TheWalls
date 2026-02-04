package ca.thewalls.Listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import ca.thewalls.TheWalls;
import ca.thewalls.Walls.Team;
import net.kyori.adventure.text.Component;

public class PlayerLeave implements Listener {
    public TheWalls walls;

    public PlayerLeave(TheWalls walls) {
        this.walls = walls;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLeave(PlayerQuitEvent e) {
        ca.thewalls.Arena arena = this.walls.getArenaByPlayer(e.getPlayer());
        if (arena == null) {
            this.walls.arenas.clearPlayer(e.getPlayer());
            return;
        }
        this.walls.arenas.clearPlayer(e.getPlayer());
        if (!arena.getGame().started) {
            this.walls.arenas.onPlayerCountChanged(arena);
            return;
        }
        arena.getGame().removeBoard(e.getPlayer());
        Team t = Team.getPlayerTeam(e.getPlayer(), arena.getGame().teams);
        if (t == null) return;
        e.getPlayer().displayName(Component.text(e.getPlayer().getName()));
        e.getPlayer().playerListName(Component.text(e.getPlayer().getName()));
        t.members.remove(e.getPlayer());
        if (t.members.size() == 0) {
            arena.getGame().aliveTeams.remove(t);
            arena.getGame().teams.remove(t);
        }
        this.walls.arenas.onPlayerCountChanged(arena);
    }
}
