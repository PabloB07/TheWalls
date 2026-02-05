package ca.thewalls;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class ArenaManager {
    private final TheWalls plugin;
    private final Map<String, Arena> arenas = new HashMap<>();
    private final Map<java.util.UUID, String> playerArena = new HashMap<>();
    public ArenaManager(TheWalls plugin) {
        this.plugin = plugin;
        loadArenas();
    }

    public Arena createArena(String name) {
        String key = name.toLowerCase();
        if (arenas.containsKey(key)) {
            return arenas.get(key);
        }
        Arena arena = new Arena(plugin, name);
        arenas.put(key, arena);
        Config.addArenaName(name);
        return arena;
    }

    public Arena getArena(String name) {
        if (name == null) return null;
        return arenas.get(name.toLowerCase());
    }

    public Arena getArenaByPlayer(Player player) {
        if (player == null) return null;
        String arenaName = playerArena.get(player.getUniqueId());
        if (arenaName == null) return null;
        Arena arena = getArena(arenaName);
        return arena;
    }

    public void assignPlayer(Player player, String arenaName) {
        if (player == null || arenaName == null) return;
        playerArena.put(player.getUniqueId(), arenaName.toLowerCase());
        Config.setPlayerArena(player.getUniqueId(), arenaName);
    }

    public void clearPlayer(Player player) {
        if (player == null) return;
        playerArena.remove(player.getUniqueId());
        Config.setPlayerArena(player.getUniqueId(), null);
    }

    public Arena joinPlayer(Player player, String arenaName) {
        if (player == null || arenaName == null) return null;
        Arena previous = getArenaByPlayer(player);
        if (previous != null && previous.getName().equalsIgnoreCase(arenaName)) {
            return previous;
        }
        if (previous != null) {
            clearPlayer(player);
            onPlayerCountChanged(previous);
        }
        Arena arena = createArena(arenaName);
        assignPlayer(player, arena.getName());
        if (arena.getLobby() != null) {
            player.teleport(arena.getLobby());
        }
        ca.thewalls.Listeners.LobbyItems.give(player, arena);
        onPlayerCountChanged(arena);
        return arena;
    }

    public void leavePlayer(Player player) {
        if (player == null) return;
        Arena arena = getArenaByPlayer(player);
        clearPlayer(player);
        if (arena != null) {
            ca.thewalls.Listeners.LobbyItems.clear(player);
            onPlayerCountChanged(arena);
        }
    }

    public boolean deleteArena(String name) {
        if (name == null) return false;
        String key = name.toLowerCase();
        Arena arena = arenas.remove(key);
        if (arena == null) return false;

        if (arena.getGame().started) {
            arena.getGame().end(true, null);
        }
        for (Player p : new java.util.ArrayList<>(getPlayers(arena))) {
            clearPlayer(p);
            ca.thewalls.Listeners.LobbyItems.clear(p);
        }
        Config.clearArenaSigns(key);
        Config.removeArenaLobby(key);
        Config.removeArenaName(key);
        if (plugin.lobbyHolograms != null) {
            plugin.lobbyHolograms.removeArena(arena);
        }
        return true;
    }

    private void loadArenas() {
        for (String name : Config.getArenaNames()) {
            createArena(name);
        }
    }

    public Map<String, Arena> getArenas() {
        return Collections.unmodifiableMap(arenas);
    }

    public java.util.List<Player> getPlayers(Arena arena) {
        if (arena == null) return java.util.Collections.emptyList();
        java.util.List<Player> players = new java.util.ArrayList<>();
        String arenaKey = arena.getName().toLowerCase();
        for (Player p : Bukkit.getOnlinePlayers()) {
            String assigned = playerArena.get(p.getUniqueId());
            if (assigned == null) continue;
            if (assigned.equals(arenaKey)) {
                players.add(p);
            }
        }
        return players;
    }

    public void onPlayerCountChanged(Arena arena) {
        if (arena == null) return;
        if (arena.getGame().started) return;
        int minPlayers = Config.data.getInt("lobby.minPlayers", 2);
        int countdown = Config.data.getInt("lobby.countdownSeconds", 20);
        if (arena.getPlayers().size() >= minPlayers) {
            arena.startLobbyCountdown(countdown);
        } else {
            arena.stopLobbyCountdown();
        }
        if (plugin.lobbyHolograms != null) {
            if (arena.getPlayers().isEmpty()) {
                plugin.lobbyHolograms.removeArena(arena);
            } else {
                plugin.lobbyHolograms.updateArena(arena);
            }
        }
    }
}
