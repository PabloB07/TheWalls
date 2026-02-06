package ca.thewalls;

import ca.thewalls.Walls.Game;
import ca.thewalls.Walls.World;

public class Arena {
    private final TheWalls plugin;
    private final String name;
    private final Game game;
    private final World world;
    private final Utils utils;
    private org.bukkit.Location lobby;
    private final java.util.Map<java.util.UUID, Integer> teamPrefs = new java.util.HashMap<>();
    private int lobbyCountdownTask = -1;
    private int lobbyCountdown = -1;

    public Arena(TheWalls plugin, String name) {
        this.plugin = plugin;
        this.name = name;
        this.game = new Game(this);
        this.world = new World(this);
        this.utils = new Utils(this);
        this.lobby = Config.getArenaLobby(name);
    }

    public TheWalls getPlugin() {
        return plugin;
    }

    public String getName() {
        return name;
    }

    public Game getGame() {
        return game;
    }

    public World getWorld() {
        return world;
    }

    public Utils getUtils() {
        return utils;
    }

    public java.util.List<org.bukkit.entity.Player> getPlayers() {
        return plugin.arenas.getPlayers(this);
    }

    public org.bukkit.Location getLobby() {
        return lobby;
    }

    public void setLobby(org.bukkit.Location lobby) {
        this.lobby = lobby;
        Config.setArenaLobby(name, lobby);
    }

    public void setTeamPreference(org.bukkit.entity.Player player, int teamId) {
        if (player == null) return;
        teamPrefs.put(player.getUniqueId(), teamId);
        Config.setPlayerTeamPref(player.getUniqueId(), name, teamId);
    }

    public Integer getTeamPreference(org.bukkit.entity.Player player) {
        if (player == null) return null;
        Integer pref = teamPrefs.get(player.getUniqueId());
        if (pref != null) return pref;
        Integer stored = Config.getPlayerTeamPref(player.getUniqueId(), name);
        if (stored != null) {
            teamPrefs.put(player.getUniqueId(), stored);
        }
        return stored;
    }

    public void clearTeamPreference(org.bukkit.entity.Player player) {
        if (player == null) return;
        teamPrefs.remove(player.getUniqueId());
        Config.setPlayerTeamPref(player.getUniqueId(), name, null);
    }

    public int getLobbyCountdown() {
        return lobbyCountdown;
    }

    public void startLobbyCountdown(int seconds) {
        if (lobbyCountdownTask != -1) return;
        lobbyCountdown = seconds;
        lobbyCountdownTask = org.bukkit.Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            if (game.started) {
                stopLobbyCountdown();
                return;
            }
            int minPlayers = Config.data.getInt("lobby.minPlayers", 2);
            if (getPlayers().size() < minPlayers) {
                stopLobbyCountdown();
                return;
            }
            if (lobbyCountdown <= 0) {
                stopLobbyCountdown();
                java.util.List<org.bukkit.entity.Player> players = getPlayers();
                if (players.isEmpty()) return;
                game.start(players.get(0));
                return;
            }
            lobbyCountdown--;
        }, 20L, 20L);
    }

    public void stopLobbyCountdown() {
        if (lobbyCountdownTask != -1) {
            org.bukkit.Bukkit.getScheduler().cancelTask(lobbyCountdownTask);
        }
        lobbyCountdownTask = -1;
        lobbyCountdown = -1;
    }
}
