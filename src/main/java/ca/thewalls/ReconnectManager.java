package ca.thewalls;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ReconnectManager {
    private final TheWalls plugin;
    private final Map<UUID, Info> pending = new ConcurrentHashMap<>();

    public ReconnectManager(TheWalls plugin) {
        this.plugin = plugin;
    }

    public void recordDisconnect(Arena arena, Player player) {
        if (arena == null || player == null) return;
        if (!Config.data.getBoolean("reconnect.enabled", true)) return;
        if (!arena.getGame().started) return;
        if (!Utils.isAlive(player) || player.getGameMode() == org.bukkit.GameMode.SPECTATOR) return;
        long grace = Config.data.getLong("reconnect.graceSeconds", 45);
        if (grace <= 0) return;
        Info info = new Info();
        info.arenaName = arena.getName();
        info.location = player.getLocation();
        info.expiresAt = System.currentTimeMillis() + grace * 1000L;
        pending.put(player.getUniqueId(), info);
        Bukkit.getScheduler().runTaskLater(plugin, () -> expire(player.getUniqueId()), grace * 20L);
    }

    public Info consume(Player player) {
        if (player == null) return null;
        Info info = pending.remove(player.getUniqueId());
        if (info == null) return null;
        if (System.currentTimeMillis() > info.expiresAt) return null;
        return info;
    }

    private void expire(UUID uuid) {
        Info info = pending.remove(uuid);
        if (info == null) return;
        Arena arena = plugin.arenas == null ? null : plugin.arenas.getArena(info.arenaName);
        if (arena == null || !arena.getGame().started) return;
        ca.thewalls.Walls.Team team = ca.thewalls.Walls.Team.getPlayerTeamByUUID(uuid, arena.getGame().teams);
        if (team != null) {
            team.removeMemberByUUID(uuid);
            if (team.members.isEmpty()) {
                arena.getGame().aliveTeams.remove(team);
                arena.getGame().teams.remove(team);
            }
        }
        arena.getUtils().checkWinner();
    }

    public static class Info {
        String arenaName;
        Location location;
        long expiresAt;

        public String getArenaName() {
            return arenaName;
        }

        public Location getLocation() {
            return location;
        }
    }
}
