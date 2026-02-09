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

        if (arena.getGame().started && walls.reconnects != null) {
            ca.thewalls.ReconnectManager.Info info = walls.reconnects.consume(e.getPlayer());
            if (info != null) {
                ca.thewalls.Walls.Team team = ca.thewalls.Walls.Team.getPlayerTeam(e.getPlayer(), arena.getGame().teams);
                if (team != null) {
                    team.removeMemberByUUID(e.getPlayer().getUniqueId());
                    team.members.add(e.getPlayer());
                }
                if (info.getLocation() != null && info.getLocation().getWorld() == arena.getWorld().world) {
                    e.getPlayer().teleport(info.getLocation());
                } else if (team != null && team.teamSpawn != null) {
                    team.teamSpawn.setY(arena.getWorld().world.getHighestBlockYAt(team.teamSpawn.getBlockX(), team.teamSpawn.getBlockZ()));
                    e.getPlayer().teleport(team.teamSpawn);
                } else {
                    e.getPlayer().teleport(arena.getWorld().world.getSpawnLocation());
                }
                e.getPlayer().setGameMode(GameMode.SURVIVAL);
                if (team != null) {
                    e.getPlayer().displayName(ca.thewalls.Utils.componentFromString(team.teamColor + "[" + team.teamName + "] " + e.getPlayer().getName()));
                    e.getPlayer().playerListName(ca.thewalls.Utils.componentFromString(team.teamColor + "[" + team.teamName + "] " + e.getPlayer().getName()));
                }
                arena.getGame().ensureBoard(e.getPlayer());
                arena.getGame().enableTablistHeartsForPlayer(e.getPlayer());
                return;
            }
        }
        if (!arena.getGame().started) {
            if (arena.getLobby() != null) {
                e.getPlayer().teleport(arena.getLobby());
            }
            this.walls.arenas.onPlayerCountChanged(arena);
            LobbyItems.give(e.getPlayer(), arena);
            arena.getGame().ensureBoard(e.getPlayer());
            arena.getGame().updateLobbyBoards();
            return;
        }

        // Game already running: rejoin only if player is still alive
        ca.thewalls.Walls.Team team = ca.thewalls.Walls.Team.getPlayerTeam(e.getPlayer(), arena.getGame().teams);
        boolean alive = team != null && e.getPlayer().getStatistic(org.bukkit.Statistic.DEATHS) <= 0;
        if (!alive) {
            this.walls.arenas.clearPlayer(e.getPlayer());
            org.bukkit.Location hub = Config.getHub();
            if (hub != null) {
                e.getPlayer().teleport(hub);
            } else if (arena.getLobby() != null) {
                e.getPlayer().teleport(arena.getLobby());
            }
            return;
        }
        if (team != null && team.teamSpawn != null) {
            team.teamSpawn.setY(arena.getWorld().world.getHighestBlockYAt(team.teamSpawn.getBlockX(), team.teamSpawn.getBlockZ()));
            e.getPlayer().teleport(team.teamSpawn);
        } else if (arena.getWorld().world != null) {
            e.getPlayer().teleport(arena.getWorld().world.getSpawnLocation());
        }
        e.getPlayer().setGameMode(GameMode.SURVIVAL);
        e.getPlayer().displayName(ca.thewalls.Utils.componentFromString(team.teamColor + "[" + team.teamName + "] " + e.getPlayer().getName()));
        e.getPlayer().playerListName(ca.thewalls.Utils.componentFromString(team.teamColor + "[" + team.teamName + "] " + e.getPlayer().getName()));
        arena.getGame().ensureBoard(e.getPlayer());
        arena.getGame().enableTablistHeartsForPlayer(e.getPlayer());
    }
}
