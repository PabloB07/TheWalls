package ca.thewalls.Commands;

import ca.thewalls.Config;
import ca.thewalls.TheWalls;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WallsCompleter implements TabCompleter {
    private final TheWalls walls;

    public WallsCompleter(TheWalls walls) {
        this.walls = walls;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            addIfPerm(sender, completions, "list", "thewalls.walls.list");
            addIfPerm(sender, completions, "join", "thewalls.walls.join");
            addIfPerm(sender, completions, "leave", "thewalls.walls.leave");
            addIfPerm(sender, completions, "team", "thewalls.walls.team");
            addIfPerm(sender, completions, "lobby", "thewalls.walls.lobby");
            addIfPerm(sender, completions, "sign", "thewalls.walls.sign");
            addIfPerm(sender, completions, "arena", "thewalls.walls.arena");
            return StringUtil.copyPartialMatches(args[0], completions, new ArrayList<>());
        }

        String sub = args[0].toLowerCase();
        if (args.length == 2) {
            if (sub.equals("join") || sub.equals("lobby") || sub.equals("sign") || sub.equals("arena")) {
                if (sub.equals("lobby")) {
                    addIfPerm(sender, completions, "set", "thewalls.walls.lobby");
                    addIfPerm(sender, completions, "tp", "thewalls.walls.lobby");
                } else if (sub.equals("sign")) {
                    addIfPerm(sender, completions, "add", "thewalls.walls.sign");
                } else if (sub.equals("arena")) {
                    addIfPerm(sender, completions, "create", "thewalls.walls.arena");
                    addIfPerm(sender, completions, "delete", "thewalls.walls.arena");
                    addIfPerm(sender, completions, "info", "thewalls.walls.arena");
                } else if (sub.equals("join")) {
                    completions.addAll(walls.arenas.getArenas().keySet());
                }
                return StringUtil.copyPartialMatches(args[1], completions, new ArrayList<>());
            }
            if (sub.equals("team")) {
                completions.addAll(teamNames());
                return StringUtil.copyPartialMatches(args[1], completions, new ArrayList<>());
            }
        }

        if (args.length == 3) {
            if (sub.equals("lobby")) {
                completions.addAll(walls.arenas.getArenas().keySet());
                return StringUtil.copyPartialMatches(args[2], completions, new ArrayList<>());
            }
            if (sub.equals("sign") && args[1].equalsIgnoreCase("add")) {
                completions.addAll(walls.arenas.getArenas().keySet());
                return StringUtil.copyPartialMatches(args[2], completions, new ArrayList<>());
            }
            if (sub.equals("arena")) {
                if (args[1].equalsIgnoreCase("delete") || args[1].equalsIgnoreCase("info")) {
                    completions.addAll(walls.arenas.getArenas().keySet());
                }
                return StringUtil.copyPartialMatches(args[2], completions, new ArrayList<>());
            }
        }

        return Collections.emptyList();
    }

    private void addIfPerm(CommandSender sender, List<String> completions, String value, String perm) {
        if (sender.hasPermission(perm) || sender.isOp()) {
            completions.add(value);
        }
    }

    private List<String> teamNames() {
        List<String> names = new ArrayList<>();
        names.add("red");
        names.add("blue");
        names.add("yellow");
        names.add("green");
        String t0 = Config.data.getString("teams.zero.name");
        String t1 = Config.data.getString("teams.one.name");
        String t2 = Config.data.getString("teams.two.name");
        String t3 = Config.data.getString("teams.three.name");
        if (t0 != null) names.add(t0.toLowerCase());
        if (t1 != null) names.add(t1.toLowerCase());
        if (t2 != null) names.add(t2.toLowerCase());
        if (t3 != null) names.add(t3.toLowerCase());
        return names;
    }
}
