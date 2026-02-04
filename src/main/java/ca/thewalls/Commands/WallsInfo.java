package ca.thewalls.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ca.thewalls.Messages;
import ca.thewalls.TheWalls;
import ca.thewalls.Arena;
import ca.thewalls.ArenaManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class WallsInfo implements CommandExecutor {
    public TheWalls walls;

    public WallsInfo(TheWalls walls) {
        this.walls = walls;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length >= 1) {
            String sub = args[0].toLowerCase();
            if (sub.equals("list")) {
                if (!sender.hasPermission("thewalls.walls.list") && !sender.isOp()) {
                    sender.sendMessage(Messages.msg("admin.no_permission"));
                    return true;
                }
                sender.sendMessage(Messages.msg("walls.list_header"));
                ArenaManager arenas = walls.arenas;
                for (String name : arenas.getArenas().keySet()) {
                    sender.sendMessage(Messages.msg("walls.list_item", java.util.Map.of("arena", name)));
                }
                return true;
            }
            if (sub.equals("join")) {
                if (!sender.hasPermission("thewalls.walls.join") && !sender.isOp()) {
                    sender.sendMessage(Messages.msg("admin.no_permission"));
                    return true;
                }
                if (!(sender instanceof Player)) {
                    sender.sendMessage(Messages.msg("walls.only_player"));
                    return true;
                }
                if (args.length < 2) {
                    ca.thewalls.Listeners.ArenaSelectMenu.open(walls, (Player) sender);
                    return true;
                }
                String arenaName = args[1];
                Arena arena = walls.arenas.joinPlayer((Player) sender, arenaName);
                sender.sendMessage(Messages.msg("walls.joined", java.util.Map.of("arena", arena.getName())));
                if (arena.getLobby() == null) {
                    sender.sendMessage(Messages.msg("walls.no_lobby", java.util.Map.of("arena", arena.getName())));
                }
                return true;
            }
            if (sub.equals("leave")) {
                if (!sender.hasPermission("thewalls.walls.leave") && !sender.isOp()) {
                    sender.sendMessage(Messages.msg("admin.no_permission"));
                    return true;
                }
                if (!(sender instanceof Player)) {
                    sender.sendMessage(Messages.msg("walls.only_player"));
                    return true;
                }
                walls.arenas.leavePlayer((Player) sender);
                sender.sendMessage(Messages.msg("walls.left"));
                return true;
            }
            if (sub.equals("lobby")) {
                if (!sender.hasPermission("thewalls.walls.lobby") && !sender.isOp()) {
                    sender.sendMessage(Messages.msg("admin.no_permission"));
                    return true;
                }
                if (!(sender instanceof Player)) {
                    sender.sendMessage(Messages.msg("walls.only_player"));
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(Messages.msg("walls.lobby_usage"));
                    return true;
                }
                String action = args[1].toLowerCase();
                if (action.equals("set")) {
                    String arenaName = args.length >= 3 ? args[2] : "main";
                    Arena arena = walls.arenas.createArena(arenaName);
                    arena.setLobby(((Player) sender).getLocation());
                    sender.sendMessage(Messages.msg("walls.lobby_set", java.util.Map.of("arena", arena.getName())));
                    return true;
                }
                if (action.equals("tp")) {
                    String arenaName = args.length >= 3 ? args[2] : "main";
                    Arena arena = walls.arenas.getArena(arenaName);
                    if (arena == null || arena.getLobby() == null) {
                        sender.sendMessage(Messages.msg("walls.no_lobby", java.util.Map.of("arena", arenaName)));
                        return true;
                    }
                    ((Player) sender).teleport(arena.getLobby());
                    sender.sendMessage(Messages.msg("walls.lobby_tp", java.util.Map.of("arena", arena.getName())));
                    return true;
                }
                sender.sendMessage(Messages.msg("walls.lobby_usage"));
                return true;
            }
            if (sub.equals("sign")) {
                if (!sender.hasPermission("thewalls.walls.sign") && !sender.isOp()) {
                    sender.sendMessage(Messages.msg("admin.no_permission"));
                    return true;
                }
                if (!(sender instanceof Player)) {
                    sender.sendMessage(Messages.msg("walls.only_player"));
                    return true;
                }
                if (args.length < 3 || !args[1].equalsIgnoreCase("add")) {
                    sender.sendMessage(Messages.msg("walls.sign_usage"));
                    return true;
                }
                String arenaName = args[2];
                org.bukkit.block.Block target = ((Player) sender).getTargetBlockExact(5);
                if (target == null || !(target.getState() instanceof org.bukkit.block.Sign)) {
                    sender.sendMessage(Messages.msg("walls.sign_invalid"));
                    return true;
                }
                ca.thewalls.Config.addArenaSign(arenaName, target.getLocation());
                ca.thewalls.Listeners.SignUpdater.updateSign(walls, target.getLocation(), arenaName);
                sender.sendMessage(Messages.msg("walls.sign_added", java.util.Map.of("arena", arenaName)));
                return true;
            }
            if (sub.equals("arena")) {
                if (!sender.hasPermission("thewalls.walls.arena") && !sender.isOp()) {
                    sender.sendMessage(Messages.msg("admin.no_permission"));
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(Messages.msg("arena.usage"));
                    return true;
                }
                String action = args[1].toLowerCase();
                if (action.equals("create")) {
                    if (args.length < 3) {
                        sender.sendMessage(Messages.msg("arena.usage"));
                        return true;
                    }
                    String arenaName = args[2];
                    Arena existing = walls.arenas.getArena(arenaName);
                    if (existing != null) {
                        sender.sendMessage(Messages.msg("arena.exists", java.util.Map.of("arena", existing.getName())));
                        return true;
                    }
                    Arena created = walls.arenas.createArena(arenaName);
                    sender.sendMessage(Messages.msg("arena.created", java.util.Map.of("arena", created.getName())));
                    return true;
                }
                if (action.equals("delete")) {
                    if (args.length < 3) {
                        sender.sendMessage(Messages.msg("arena.usage"));
                        return true;
                    }
                    String arenaName = args[2];
                    if (walls.mainArena != null && walls.mainArena.getName().equalsIgnoreCase(arenaName)) {
                        sender.sendMessage(Messages.msg("arena.delete_main"));
                        return true;
                    }
                    boolean removed = walls.arenas.deleteArena(arenaName);
                    if (!removed) {
                        sender.sendMessage(Messages.msg("arena.not_found", java.util.Map.of("arena", arenaName)));
                        return true;
                    }
                    sender.sendMessage(Messages.msg("arena.deleted", java.util.Map.of("arena", arenaName)));
                    return true;
                }
                if (action.equals("info")) {
                    if (args.length < 3) {
                        sender.sendMessage(Messages.msg("arena.usage"));
                        return true;
                    }
                    String arenaName = args[2];
                    Arena arena = walls.arenas.getArena(arenaName);
                    if (arena == null) {
                        sender.sendMessage(Messages.msg("arena.not_found", java.util.Map.of("arena", arenaName)));
                        return true;
                    }
                    sender.sendMessage(Messages.msg("arena.info", java.util.Map.of(
                            "arena", arena.getName(),
                            "players", String.valueOf(arena.getPlayers().size()),
                            "status", arena.getGame().started ? "in-game" : "lobby",
                            "lobby", arena.getLobby() == null ? "no" : "yes"
                    )));
                    return true;
                }
                sender.sendMessage(Messages.msg("arena.usage"));
                return true;
            }
            if (sub.equals("team")) {
                if (!sender.hasPermission("thewalls.walls.team") && !sender.isOp()) {
                    sender.sendMessage(Messages.msg("admin.no_permission"));
                    return true;
                }
                if (!(sender instanceof Player)) {
                    sender.sendMessage(Messages.msg("walls.only_player"));
                    return true;
                }
                Arena arena = walls.getArenaByPlayer((Player) sender);
                if (arena == null) {
                    sender.sendMessage(Messages.msg("walls.join_usage"));
                    return true;
                }
                if (arena.getGame().started) {
                    sender.sendMessage(Messages.msg("walls.team_locked"));
                    return true;
                }
                if (args.length < 2) {
                    ca.thewalls.Listeners.TeamSelectMenu.open(walls, (Player) sender, arena);
                    return true;
                }
                String teamName = String.join("", java.util.Arrays.copyOfRange(args, 1, args.length));
                int teamId = teamIdFromName(teamName);
                if (teamId < 0) {
                    sender.sendMessage(Messages.msg("walls.team_invalid"));
                    return true;
                }
                arena.setTeamPreference((Player) sender, teamId);
                sender.sendMessage(Messages.msg("walls.team_set", java.util.Map.of("team", teamName)));
                return true;
            }
        }

        MiniMessage mm = MiniMessage.miniMessage();
        String baseRaw = Messages.raw("info.base").replace("{version}", walls.getPluginMeta().getVersion());
        Component base = mm.deserialize(baseRaw);
        Component hover = mm.deserialize(Messages.raw("info.hover"));

        sender.sendMessage(base.hoverEvent(HoverEvent.showText(hover)));
        return true;
    }

    private int teamIdFromName(String name) {
        if (name == null) return -1;
        String n = name.trim().toLowerCase();
        String t0 = String.valueOf(ca.thewalls.Config.data.getString("teams.zero.name", "red")).toLowerCase();
        String t1 = String.valueOf(ca.thewalls.Config.data.getString("teams.one.name", "blue")).toLowerCase();
        String t2 = String.valueOf(ca.thewalls.Config.data.getString("teams.two.name", "yellow")).toLowerCase();
        String t3 = String.valueOf(ca.thewalls.Config.data.getString("teams.three.name", "green")).toLowerCase();
        if (n.equals(t0) || n.equals("red")) return 0;
        if (n.equals(t1) || n.equals("blue")) return 1;
        if (n.equals(t2) || n.equals("yellow")) return 2;
        if (n.equals(t3) || n.equals("green")) return 3;
        return -1;
    }
}
