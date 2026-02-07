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
        if (!arena.getGame().started) {
            this.walls.arenas.clearPlayer(e.getPlayer());
            this.walls.arenas.onPlayerCountChanged(arena);
            return;
        }
        arena.getGame().removeBoard(e.getPlayer());
        arena.getGame().disableTablistHeartsForPlayer(e.getPlayer());
        Team t = Team.getPlayerTeam(e.getPlayer(), arena.getGame().teams);
        if (!ca.thewalls.Utils.isAlive(e.getPlayer()) || e.getPlayer().getGameMode() == org.bukkit.GameMode.SPECTATOR) {
            this.walls.arenas.clearPlayer(e.getPlayer());
            if (t != null) {
                e.getPlayer().displayName(Component.text(e.getPlayer().getName()));
                e.getPlayer().playerListName(Component.text(e.getPlayer().getName()));
                t.members.remove(e.getPlayer());
                if (t.members.isEmpty()) {
                    arena.getGame().aliveTeams.remove(t);
                    arena.getGame().teams.remove(t);
                }
            }
            this.walls.arenas.onPlayerCountChanged(arena);
            return;
        }
        this.walls.arenas.onPlayerCountChanged(arena);
    }
}
