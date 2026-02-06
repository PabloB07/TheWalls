package ca.thewalls.Listeners;

import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import ca.thewalls.Config;
import ca.thewalls.Messages;
import ca.thewalls.TheWalls;

public class PlayerJoin implements Listener {
    public TheWalls walls;

    public PlayerJoin(TheWalls walls) {
        this.walls = walls;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent e) {
        Config.createLeaderboardPlayer(e.getPlayer());

        String savedArena = Config.getPlayerArena(e.getPlayer().getUniqueId());
        if (savedArena != null) {
            this.walls.arenas.createArena(savedArena);
            this.walls.arenas.assignPlayer(e.getPlayer(), savedArena);
        }
        if (savedArena == null) {
            org.bukkit.Location hub = Config.getHub();
            if (hub != null) {
                e.getPlayer().teleport(hub);
                return;
            }
        }
        ca.thewalls.Arena arena = this.walls.getArenaByPlayer(e.getPlayer());
        if (arena == null) return;
        if (arena.getLobby() != null) {
            e.getPlayer().teleport(arena.getLobby());
        }
        this.walls.arenas.onPlayerCountChanged(arena);
        LobbyItems.give(e.getPlayer(), arena);
        arena.getGame().ensureBoard(e.getPlayer());
        arena.getGame().updateLobbyBoards();
        if (!arena.getGame().started) return;
        arena.getGame().ensureBoard(e.getPlayer());
        arena.getGame().enableTablistHeartsForPlayer(e.getPlayer());
        e.getPlayer().setGameMode(GameMode.SPECTATOR);
        e.getPlayer().sendMessage(Messages.msg("join.spectator"));
    }
}
