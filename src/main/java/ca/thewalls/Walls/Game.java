package ca.thewalls.Walls;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import fr.mrmicky.fastboard.adventure.FastBoard;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import ca.thewalls.Arena;
import ca.thewalls.Config;
import ca.thewalls.Messages;
import ca.thewalls.Utils;
import ca.thewalls.Events.*;

import javax.annotation.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

class Loop implements Runnable {
    Arena arena;
    Game game;
    public Loop(Arena arena) {
        this.arena = arena;
        this.game = this.arena.getGame();
    }

    @Override
    public void run() {
        if (!game.started) return;

        if ((game.time >= game.prepTime) && !game.wallsFallen) {
            this.arena.getWorld().dropWalls();
            for (Player p : this.arena.getPlayers()) {
                p.playSound(p.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, 255, 1);
                p.playSound(p.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 255, 1);
                p.sendMessage(Messages.msg("game.walls_fallen"));
                Utils.sendTitle(p, Messages.raw("title.walls_fallen"), "", 10, 80, 10);
            }
            game.wallsFallen = true;
        }

        if (game.wallsFallen && game.eventTimer <= 0 && game.events.size() >= 1) {
            int rnd = new Random().nextInt(game.events.size());
            game.events.get(rnd).run();
            game.eventTimer = game.eventCooldown;
        }

        if (game.wallsFallen && game.borderCloseTimer <= 0 && !game.borderClosing) {
            for (Player p : this.arena.getPlayers()) {
                p.playSound(p.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 255, 1);
                p.sendMessage(Messages.msg("game.borders_closing"));
            }
            this.arena.getWorld().world.getWorldBorder().setSize((double) game.size * Config.data.getDouble("world.borderShrinkPercentageOfSize"), game.borderCloseSpeed);
            game.borderClosing = true;
        }

        for (Player p : this.arena.getPlayers()) {
            if (!Utils.isAlive(p)) {
                Team tempTeam = Team.getPlayerTeam(p, game.teams);
                if ((Config.data.getBoolean("theWalls.respawnDuringPrepTime") && !game.wallsFallen) || (Config.data.getBoolean("theWalls.respawnDuringInitialFighting") && !game.borderClosing)) {
                    if (tempTeam != null) {
                        tempTeam.readyPlayer(p);
                    } else {
                        game.aliveTeams.get(0).members.add(p);
                        game.aliveTeams.get(0).readyPlayer(p);
                    }
                }
            }

            game.ensureBoard(p);
            FastBoard board = game.getBoard(p);
            if (board == null) continue;
            List<Component> lines = game.buildScoreboardLines(p);
            board.updateLines(lines);
        }

        game.time++;
        if (game.wallsFallen) {
            if (game.events.size() >= 1) {
                game.eventTimer--;
            }
            game.borderCloseTimer--;
        }
        if (Config.data.getBoolean("teams.checkWinEverySecond")) {
            this.arena.getUtils().checkWinner();
        }
    }
}

public class Game {

    Arena arena;

    public Game(Arena arena) {
        this.arena = arena;
    }

    public boolean started = false;
    public boolean wallsFallen = false;
    public boolean borderClosing = false;
    public int prepTime = 0;
    public int borderCloseTime = 0;
    public int borderCloseTimer = 0;
    public int borderCloseSpeed = 0;
    public int size = 0;
    public int eventTimer = 0;
    public int eventCooldown = 0;
    // All teams in the game
    public ArrayList<Team> teams = new ArrayList<>();
    // Used to determine the winning team of the walls
    public ArrayList<Team> aliveTeams = new ArrayList<>();
    public ArrayList<ca.thewalls.Events.Event> events = new ArrayList<>();
    public int gameLoopID = 0;

    public int time = 0;
    private final Map<UUID, FastBoard> boards = new HashMap<>();
    private final Map<UUID, org.bukkit.scoreboard.Scoreboard> tablistRestore = new HashMap<>();

    public void ensureBoard(Player player) {
        if (boards.containsKey(player.getUniqueId())) return;
        FastBoard board = new FastBoard(player);
        board.updateTitle(Utils.format("&6&lThe Walls"));
        boards.put(player.getUniqueId(), board);
    }

    public void removeBoard(Player player) {
        FastBoard board = boards.remove(player.getUniqueId());
        if (board != null) {
            board.delete();
        }
    }

    public FastBoard getBoard(Player player) {
        return boards.get(player.getUniqueId());
    }

    private void clearBoards() {
        for (FastBoard board : boards.values()) {
            board.delete();
        }
        boards.clear();
    }

    List<Component> buildScoreboardLines(Player viewer) {
        List<Component> lines = new ArrayList<>();
        Team myTeam = viewer == null ? null : Team.getPlayerTeam(viewer, teams);
        String myTeamName = myTeam == null ? "None" : myTeam.teamColor + myTeam.teamName;
        int myKills = 0;
        if (viewer != null) {
            java.util.UUID uid = viewer.getUniqueId();
            myKills = Config.leaderboard.getInt(uid.toString() + ".kills", 0);
        }
        lines.add(Messages.msg("scoreboard.game_title", java.util.Map.of(
                "team", myTeamName
        )));

        if (wallsFallen) {
            lines.add(Messages.msg("scoreboard.game_phase_fight"));
            if (events.size() >= 1) {
                lines.add(Messages.msg("scoreboard.game_event_in", java.util.Map.of("seconds", String.valueOf(eventTimer))));
            }
            if (borderCloseTimer <= 0) {
                lines.add(Messages.msg("scoreboard.game_border_now"));
            } else {
                lines.add(Messages.msg("scoreboard.game_border_in", java.util.Map.of("seconds", String.valueOf(borderCloseTimer))));
            }
        } else {
            lines.add(Messages.msg("scoreboard.game_phase_prep", java.util.Map.of("seconds", String.valueOf(prepTime - time))));
        }

        lines.add(Messages.msg("scoreboard.game_players", java.util.Map.of(
                "current", String.valueOf(this.arena.getPlayers().size())
        )));
        lines.add(Messages.msg("scoreboard.game_you", java.util.Map.of(
                "team", myTeamName,
                "kills", String.valueOf(myKills)
        )));

        lines.add(Messages.msg("scoreboard.game_teams"));
        for (Team t : teams) {
            if (t.alive) {
                lines.add(Messages.msg("scoreboard.game_team_alive", java.util.Map.of(
                        "team", t.teamColor + t.teamName,
                        "alive", String.valueOf(t.getAliveMembers())
                )));
            } else {
                lines.add(Messages.msg("scoreboard.game_team_dead", java.util.Map.of(
                        "team", t.teamColor + t.teamName
                )));
            }
        }

        return lines;
    }

    public void updateLobbyBoards() {
        if (started) return;
        int minPlayers = Config.data.getInt("lobby.minPlayers", 2);
        int countdown = this.arena.getLobbyCountdown();
        List<Component> lines = new ArrayList<>();
        lines.add(Messages.msg("scoreboard.lobby_title"));
        lines.add(Messages.msg("scoreboard.lobby_arena", java.util.Map.of(
                "arena", this.arena.getName()
        )));
        lines.add(Messages.msg("scoreboard.lobby_players", java.util.Map.of(
                "current", String.valueOf(this.arena.getPlayers().size()),
                "min", String.valueOf(minPlayers)
        )));
        if (countdown >= 0) {
            lines.add(Messages.msg("scoreboard.lobby_starting", java.util.Map.of("seconds", String.valueOf(countdown))));
        } else {
            lines.add(Messages.msg("scoreboard.lobby_waiting"));
        }

        for (Player p : this.arena.getPlayers()) {
            ensureBoard(p);
            FastBoard board = getBoard(p);
            if (board == null) continue;
            board.updateLines(lines);
        }
    }

    public void start(@Nullable Player starter) {

        if (this.arena.getPlayers().size() == 0) {
            if (starter != null) {
                starter.sendMessage(Messages.msg("game.no_players"));
            }
            return;
        }

        String configuredWorld = ca.thewalls.Config.getArenaGameWorld(this.arena.getName());
        if (configuredWorld != null) {
            this.arena.getWorld().world = Bukkit.getWorld(configuredWorld);
            if (this.arena.getWorld().world == null) {
                for (Player p : this.arena.getPlayers()) {
                    p.sendMessage(Messages.msg("wstart.world_missing", java.util.Map.of("world", configuredWorld)));
                }
                return;
            }
            Location loc = new Location(this.arena.getWorld().world, Config.data.getDouble("theWalls.autoExecute.center.x"), 0, Config.data.getDouble("theWalls.autoExecute.center.z"));
            this.arena.getWorld().world.getWorldBorder().setCenter(loc.getX(), loc.getZ());
            this.arena.getWorld().positionOne[0] = loc.getBlockX() + size;
            this.arena.getWorld().positionOne[1] = loc.getBlockZ() + size;
            this.arena.getWorld().positionTwo[0] = loc.getBlockX() - size;
            this.arena.getWorld().positionTwo[1] = loc.getBlockZ() - size;
        } else if (starter == null) {
            this.arena.getWorld().world = Bukkit.getWorld(Objects.requireNonNull(Config.data.getString("theWalls.autoExecute.worldName")));
            assert this.arena.getWorld().world != null;
            Location loc = new Location(this.arena.getWorld().world, Config.data.getDouble("theWalls.autoExecute.center.x"), 0, Config.data.getDouble("theWalls.autoExecute.center.z"));
            this.arena.getWorld().world.getWorldBorder().setCenter(loc.getX(), loc.getZ());
            this.arena.getWorld().positionOne[0] = loc.getBlockX() + size;
            this.arena.getWorld().positionOne[1] = loc.getBlockZ() + size;
            this.arena.getWorld().positionTwo[0] = loc.getBlockX() - size;
            this.arena.getWorld().positionTwo[1] = loc.getBlockZ() - size;
        } else {
            this.arena.getWorld().world = starter.getWorld();
            this.arena.getWorld().world.getWorldBorder().setCenter(starter.getLocation());
            this.arena.getWorld().positionOne[0] = starter.getLocation().getBlockX() + size;
            this.arena.getWorld().positionOne[1] = starter.getLocation().getBlockZ() + size;
            this.arena.getWorld().positionTwo[0] = starter.getLocation().getBlockX() - size;
            this.arena.getWorld().positionTwo[1] = starter.getLocation().getBlockZ() - size;
        }

        int safeSize = Math.max(1, size);
        double borderSize = (safeSize * 2.0) - 2.0;
        if (borderSize < 1.0) borderSize = 1.0;
        if (borderSize > 5.9999968E7) borderSize = 5.9999968E7;
        this.arena.getWorld().world.getWorldBorder().setSize(borderSize);
        this.arena.getWorld().world.setTime(1000);

        // Handle teams
        java.util.List<Player> arenaPlayers = this.arena.getPlayers();
        boolean[] needs = new boolean[] { false, false, false, false };
        if (arenaPlayers.size() >= 1) needs[0] = true;
        if (arenaPlayers.size() >= 2) needs[1] = true;
        if (arenaPlayers.size() >= 3) needs[2] = true;
        if (arenaPlayers.size() >= 4) needs[3] = true;
        for (Player p : arenaPlayers) {
            Integer pref = this.arena.getTeamPreference(p);
            if (pref != null && pref >= 0 && pref < 4) {
                needs[pref] = true;
            }
        }
        int enabledTeams = 0;
        for (boolean need : needs) {
            if (need) enabledTeams++;
        }
        if (arenaPlayers.size() >= 2 && enabledTeams < 2) {
            needs[0] = true;
            needs[1] = true;
        }
        for (int id = 0; id < 4; id++) {
            if (needs[id]) {
                new Team(id, false, this.arena);
            }
        }

        this.arena.getWorld().save();
        this.arena.getWorld().wallBlocks();
        
        java.util.Map<Integer, java.util.List<Player>> preferred = new java.util.HashMap<>();
        for (Player p : arenaPlayers) {
            Integer pref = this.arena.getTeamPreference(p);
            if (pref != null && pref >= 0 && pref < 4) {
                preferred.computeIfAbsent(pref, k -> new java.util.ArrayList<>()).add(p);
            }
        }

        for (Team t : teams) {
            java.util.List<Player> prefList = preferred.get(t.team);
            if (prefList == null) continue;
            for (Player p : prefList) {
                t.members.add(p);
                t.readyPlayer(p);
            }
        }

        int i = 0;
        java.util.List<Team> teamList = new java.util.ArrayList<>(teams);
        for (Player p : arenaPlayers) {
            Integer pref = this.arena.getTeamPreference(p);
            if (pref != null && pref >= 0 && pref < 4) continue;
            if (teamList.isEmpty()) break;
            Team t = teamList.get(i % teamList.size());
            t.members.add(p);
            t.readyPlayer(p);
            i++;
        }

        // Register events
        if (Config.data.getBoolean("events.tnt.enabled"))
            new TNTSpawn("TNT Spawn", this.arena);
        if (Config.data.getBoolean("events.blindSnail.enabled"))
            new BlindSnail("Blind Snail", this.arena);
        if (Config.data.getBoolean("events.locationSwap.enabled"))
            new LocationSwap("Player Location Swap", this.arena);
        if (Config.data.getBoolean("events.supplyChest.enabled"))
            new SupplyChest("Supply Chest", this.arena);
        if (Config.data.getBoolean("events.gregs.enabled"))
            new FreeFood("Free Food / Chicken Explosion", this.arena);
        if (Config.data.getBoolean("events.reveal.enabled"))
            new LocationReveal("Location Reveal", this.arena);
        if (Config.data.getBoolean("events.sinkHole.enabled"))
            new SinkHole("Sink Hole", this.arena);
        if (Config.data.getBoolean("events.hailStorm.enabled"))
            new HailStorm("Hail of Arrows", this.arena);
        if (Config.data.getBoolean("events.bossMan.enabled"))
            new BossMan("Boss Man", this.arena);
        if (Config.data.getBoolean("events.itemCheck.enabled"))
            new ItemCheck("Item Check", this.arena);
        if (Config.data.getBoolean("events.bombingRun.enabled"))
            new BombingRun("Bombing Run", this.arena);

        started = true;
        time = 0;
        eventTimer = eventCooldown;
        borderCloseTimer = borderCloseTime;
        enableTablistHeartsForPlayers(this.arena.getPlayers());
        gameLoopID = Bukkit.getScheduler().scheduleSyncRepeatingTask(Utils.getPlugin(), new Loop(arena), 20L, 20L);

        Utils.getPlugin().getLogger().info("Started game with teams: ");
        for (Team t : aliveTeams) {
            Utils.getPlugin().getLogger().info(" - " + t.teamName);
        }
    }

    // End the game and clean up anything related to it
    public void end(boolean forced, @Nullable Team winningTeam) {
        borderClosing = false;
        wallsFallen = false;
        started = false;

        Bukkit.getScheduler().cancelTask(gameLoopID);
        disableTablistHeartsForPlayers(this.arena.getPlayers());
        if (Utils.getPlugin().isEnabled()) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(Utils.getPlugin(), this.arena.getWorld()::reset, 20*7);
        } else {
            this.arena.getWorld().reset();
        }

        if (!forced) {
            for (Team t : teams) {
                for (Player p : t.members) {
                    if (t.alive) {
                        int wins = Config.leaderboard.getInt(p.getUniqueId().toString() + ".wins") + 1;
                        Config.leaderboard.set(p.getUniqueId().toString() + ".wins", wins);
                    } else {
                        int losses = Config.leaderboard.getInt(p.getUniqueId().toString() + ".losses") + 1;
                        Config.leaderboard.set(p.getUniqueId().toString() + ".losses", losses);
                    }
                }
            }

            try {
                Config.leaderboard.save(Config.leaderboardFile);
            } catch (IOException ex) {
                Utils.getPlugin().getLogger().warning(ex.toString());
            }
            Config.incrementArenaPlays(this.arena.getName());
            if (this.arena.getPlugin().topHolograms != null) {
                this.arena.getPlugin().topHolograms.refresh();
            }
        }
        for (Player p : this.arena.getPlayers()) {
            if (!forced && winningTeam != null) {
                Utils.sendTitle(
                        p,
                        Messages.raw("title.win_title", java.util.Map.of("team", winningTeam.teamColor + "&l" + winningTeam.teamName)),
                        Messages.raw("title.win_subtitle", java.util.Map.of("team", winningTeam.teamColor)),
                        10, 80, 20
                );
            }
            p.sendMessage(Messages.msg("game.end"));
            p.displayName(Component.text(p.getName()));
            p.getInventory().clear();
            p.playerListName(Component.text(p.getName()));
            p.setGameMode(GameMode.SURVIVAL);
            int wins = Config.leaderboard.getInt(p.getUniqueId().toString() + ".wins");
            int losses = Config.leaderboard.getInt(p.getUniqueId().toString() + ".losses");
            p.sendMessage(Messages.msg("game.stats", java.util.Map.of(
                    "wins", String.valueOf(wins),
                    "losses", String.valueOf(losses)
            )));
        }
        clearBoards();
        events.clear();
        teams.clear();
        aliveTeams.clear();
        this.arena.stopLobbyCountdown();
        int endCooldown = Config.data.getInt("lobby.endCooldownSeconds", 10);
        this.arena.startLobbyEndCooldown(endCooldown);
        if (this.arena.getLobby() != null) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(Utils.getPlugin(), () -> {
                for (Player p : this.arena.getPlayers()) {
                    p.teleport(this.arena.getLobby());
                    ca.thewalls.Listeners.LobbyItems.give(p, this.arena);
                }
                this.arena.stopLobbyEndCooldown();
            }, endCooldown * 20L);
        } else {
            this.arena.stopLobbyEndCooldown();
        }
    }

    public void enableTablistHeartsForPlayer(Player player) {
        if (player == null) return;
        if (tablistRestore.containsKey(player.getUniqueId())) return;
        org.bukkit.scoreboard.Scoreboard current = player.getScoreboard();
        tablistRestore.put(player.getUniqueId(), current);
        org.bukkit.scoreboard.Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        org.bukkit.scoreboard.Objective health = board.registerNewObjective("health", org.bukkit.scoreboard.Criteria.HEALTH, net.kyori.adventure.text.Component.text("❤"));
        health.setRenderType(org.bukkit.scoreboard.RenderType.HEARTS);
        health.setDisplaySlot(org.bukkit.scoreboard.DisplaySlot.PLAYER_LIST);
        player.setScoreboard(board);
    }

    public void disableTablistHeartsForPlayer(Player player) {
        if (player == null) return;
        org.bukkit.scoreboard.Scoreboard prev = tablistRestore.remove(player.getUniqueId());
        if (prev != null) {
            player.setScoreboard(prev);
        }
    }

    private void enableTablistHeartsForPlayers(java.util.List<Player> players) {
        for (Player p : players) {
            enableTablistHeartsForPlayer(p);
        }
    }

    private void disableTablistHeartsForPlayers(java.util.List<Player> players) {
        for (Player p : players) {
            disableTablistHeartsForPlayer(p);
        }
    }
}
