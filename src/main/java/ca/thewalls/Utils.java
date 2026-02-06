package ca.thewalls;

import java.util.ArrayList;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import ca.thewalls.Walls.Team;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;

import java.time.Duration;

public class Utils {

    public Arena arena;
    public Utils(Arena arena) {
        this.arena = arena;
    }

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();

    public static Component format(String s) {
        return LEGACY.deserialize(s);
    }

    public static String toLegacy(Component component) {
        String legacy = LEGACY.serialize(component);
        legacy = legacy.replace("§r", "");
        if (legacy.length() > 0) {
            legacy = "§r" + legacy;
        }
        return legacy;
    }

    // Legacy string format for APIs that only accept String (scoreboards, etc.)
    public static String formatText(String s) {
        return LEGACY.serialize(LEGACY.deserialize(s));
    }

    public static Component adminMessage(String s) {
        return format("&6&l[The Walls]&r " + s);
    }

    public static void sendTitle(Player p, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        Title.Times times = Title.Times.times(
                Duration.ofMillis(fadeIn * 50L),
                Duration.ofMillis(stay * 50L),
                Duration.ofMillis(fadeOut * 50L)
        );
        p.showTitle(Title.title(format(title), format(subtitle), times));
    }

    public static boolean isAlive(Player p) {
        return p.getGameMode() != GameMode.SPECTATOR || p.getStatistic(Statistic.DEATHS) <= 0;
    }

    public static Plugin getPlugin() {
        return Bukkit.getPluginManager().getPlugin("TheWalls");
    }

    public void checkWinner() {
        int i = 0;
        ArrayList<Integer> teamsToRemove = new ArrayList<>();
        for (Team t : this.arena.getGame().aliveTeams) {
            for (Player p : t.members) {
                t.alive = false;
                if (Utils.isAlive(p)) {
                    t.alive = true;
                    break;
                }
            }
            if (!t.alive) {
                for (Player p : this.arena.getPlayers()) {
                    p.playSound(p.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 255, 1);
                }
                for (Player p : this.arena.getPlayers()) {
                    p.sendMessage(Messages.msg("game.team_eliminated", java.util.Map.of("team", t.teamColor + t.teamName)));
                }
                teamsToRemove.add(i);
            }
            i++;
        }
        for (int j = 0; j < teamsToRemove.size(); j++) {
            Utils.getPlugin().getLogger().info(this.arena.getGame().aliveTeams.get(j).teamName + " has been eliminated!");
            this.arena.getGame().aliveTeams.remove(j);
        }
        if (Config.data.getBoolean("teams.allowTie") || this.arena.getPlayers().size() <= 1) {
            if (this.arena.getGame().aliveTeams.size() == 0) {
                for (Player p : this.arena.getPlayers()) {
                    p.playSound(p.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 255, 1);
                }
                for (Player p : this.arena.getPlayers()) {
                    p.sendMessage(Messages.msg("game.tie"));
                }
                this.arena.getGame().end(false, null);
            }
        }
        if (this.arena.getGame().aliveTeams.size() == 1) {
            Team winningTeam = this.arena.getGame().aliveTeams.get(0);
            for (Player p : this.arena.getPlayers()) {
                p.playSound(p.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 255, 1);
            }
            for (Player p : this.arena.getPlayers()) {
                p.sendMessage(Messages.msg("game.win", java.util.Map.of("team", winningTeam.teamColor + winningTeam.teamName)));
            }
            this.arena.getGame().end(false, null);
        }

    }

}
