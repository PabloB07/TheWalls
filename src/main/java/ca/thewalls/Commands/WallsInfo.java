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
            if (sub.equals("start")) {
                return new WStart(walls).onCommand(sender, command, label, slice(args, 1));
            }
            if (sub.equals("end")) {
                return new WEnd(walls).onCommand(sender, command, label, slice(args, 1));
            }
            if (sub.equals("forceteam")) {
                return new WForceTeam(walls).onCommand(sender, command, label, slice(args, 1));
            }
            if (sub.equals("leaderboard")) {
                return new WLeaderboard(walls).onCommand(sender, command, label, slice(args, 1));
            }
            if (sub.equals("events")) {
                return new WEvents(walls).onCommand(sender, command, label, slice(args, 1));
            }
            if (sub.equals("kits")) {
                if (!sender.hasPermission("thewalls.walls.kits") && !sender.isOp()) {
                    sender.sendMessage(Messages.msg("admin.no_permission"));
                    return true;
                }
                if (!(sender instanceof Player)) {
                    sender.sendMessage(Messages.msg("walls.only_player"));
                    return true;
                }
                ca.thewalls.Listeners.KitSelectMenu.open(walls, (Player) sender);
                return true;
            }
            if (sub.equals("perks")) {
                if (!sender.hasPermission("thewalls.walls.perks") && !sender.isOp()) {
                    sender.sendMessage(Messages.msg("admin.no_permission"));
                    return true;
                }
                if (!(sender instanceof Player)) {
                    sender.sendMessage(Messages.msg("walls.only_player"));
                    return true;
                }
                if (!ca.thewalls.Crates.isEnabled()) {
                    sender.sendMessage(Messages.msg("walls.crate_disabled"));
                    return true;
                }
                ca.thewalls.Listeners.PerkMenu.open(walls, (Player) sender);
                return true;
            }
            if (sub.equals("cosmetics")) {
                return new WCosmetics(walls).onCommand(sender, command, label, slice(args, 1));
            }
            if (sub.equals("trail")) {
                if (!sender.hasPermission("thewalls.walls.trail") && !sender.isOp()) {
                    sender.sendMessage(Messages.msg("admin.no_permission"));
                    return true;
                }
                if (!(sender instanceof Player)) {
                    sender.sendMessage(Messages.msg("walls.only_player"));
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(Messages.msg("walls.trail_usage"));
                    return true;
                }
                String id = args[1].toLowerCase();
                if (id.equals("off") || id.equals("none")) {
                    ca.thewalls.Config.setPlayerTrail(((Player) sender).getUniqueId(), "");
                    sender.sendMessage(Messages.msg("walls.trail_disabled"));
                    return true;
                }
                if (!ca.thewalls.Cosmetics.isValidTrail(id)) {
                    sender.sendMessage(Messages.msg("walls.trail_invalid"));
                    return true;
                }
                if (!ca.thewalls.Cosmetics.hasTrailPermission((Player) sender, id) && !ca.thewalls.Cosmetics.isTrailUnlocked((Player) sender, id)) {
                    int cost = ca.thewalls.Cosmetics.getTrailCost(id);
                    if (cost > 0) {
                        if (!ca.thewalls.EconomyService.isAvailable()) {
                            sender.sendMessage(Messages.msg("walls.economy_missing"));
                            return true;
                        }
                        double bal = ca.thewalls.EconomyService.getBalance((Player) sender);
                        if (bal < cost) {
                            sender.sendMessage(Messages.msg("walls.cosmetic_not_enough_money", java.util.Map.of("amount", String.valueOf(cost))));
                            return true;
                        }
                        if (ca.thewalls.EconomyService.withdraw((Player) sender, cost)) {
                            ca.thewalls.Cosmetics.unlockTrail((Player) sender, id);
                            sender.sendMessage(Messages.msg("walls.cosmetic_unlocked", java.util.Map.of("item", id)));
                        } else {
                            sender.sendMessage(Messages.msg("walls.economy_missing"));
                            return true;
                        }
                    } else {
                        ca.thewalls.Cosmetics.unlockTrail((Player) sender, id);
                    }
                }
                ca.thewalls.Config.setPlayerTrail(((Player) sender).getUniqueId(), id);
                sender.sendMessage(Messages.msg("walls.trail_set", java.util.Map.of("trail", id)));
                return true;
            }
            if (sub.equals("killeffect")) {
                if (!sender.hasPermission("thewalls.walls.killeffect") && !sender.isOp()) {
                    sender.sendMessage(Messages.msg("admin.no_permission"));
                    return true;
                }
                if (!(sender instanceof Player)) {
                    sender.sendMessage(Messages.msg("walls.only_player"));
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(Messages.msg("walls.killeffect_usage"));
                    return true;
                }
                String id = args[1].toLowerCase();
                if (id.equals("off") || id.equals("none")) {
                    ca.thewalls.Config.setPlayerKillEffect(((Player) sender).getUniqueId(), "");
                    sender.sendMessage(Messages.msg("walls.killeffect_disabled"));
                    return true;
                }
                if (!ca.thewalls.Cosmetics.isValidKillEffect(id)) {
                    sender.sendMessage(Messages.msg("walls.killeffect_invalid"));
                    return true;
                }
                if (!ca.thewalls.Cosmetics.hasKillEffectPermission((Player) sender, id) && !ca.thewalls.Cosmetics.isKillEffectUnlocked((Player) sender, id)) {
                    int cost = ca.thewalls.Cosmetics.getKillEffectCost(id);
                    if (cost > 0) {
                        if (!ca.thewalls.EconomyService.isAvailable()) {
                            sender.sendMessage(Messages.msg("walls.economy_missing"));
                            return true;
                        }
                        double bal = ca.thewalls.EconomyService.getBalance((Player) sender);
                        if (bal < cost) {
                            sender.sendMessage(Messages.msg("walls.cosmetic_not_enough_money", java.util.Map.of("amount", String.valueOf(cost))));
                            return true;
                        }
                        if (ca.thewalls.EconomyService.withdraw((Player) sender, cost)) {
                            ca.thewalls.Cosmetics.unlockKillEffect((Player) sender, id);
                            sender.sendMessage(Messages.msg("walls.cosmetic_unlocked", java.util.Map.of("item", id)));
                        } else {
                            sender.sendMessage(Messages.msg("walls.economy_missing"));
                            return true;
                        }
                    } else {
                        ca.thewalls.Cosmetics.unlockKillEffect((Player) sender, id);
                    }
                }
                ca.thewalls.Config.setPlayerKillEffect(((Player) sender).getUniqueId(), id);
                sender.sendMessage(Messages.msg("walls.killeffect_set", java.util.Map.of("effect", id)));
                return true;
            }
            if (sub.equals("reload")) {
                return new WReload(walls).onCommand(sender, command, label, slice(args, 1));
            }
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
                    if (args.length < 3) {
                        sender.sendMessage(Messages.msg("walls.lobby_usage"));
                        return true;
                    }
                    String arenaName = args[2];
                    Arena arena = walls.arenas.createArena(arenaName);
                    arena.setLobby(((Player) sender).getLocation());
                    sender.sendMessage(Messages.msg("walls.lobby_set", java.util.Map.of("arena", arena.getName())));
                    return true;
                }
                if (action.equals("tp")) {
                    if (args.length < 3) {
                        sender.sendMessage(Messages.msg("walls.lobby_usage"));
                        return true;
                    }
                    String arenaName = args[2];
                    Arena arena = walls.arenas.getArena(arenaName);
                    if (arena == null || arena.getLobby() == null) {
                        sender.sendMessage(Messages.msg("walls.no_lobby", java.util.Map.of("arena", arenaName)));
                        return true;
                    }
                    ((Player) sender).teleport(arena.getLobby());
                    sender.sendMessage(Messages.msg("walls.lobby_tp", java.util.Map.of("arena", arena.getName())));
                    return true;
                }
                if (action.equals("remove")) {
                    if (args.length < 3) {
                        sender.sendMessage(Messages.msg("walls.lobby_usage"));
                        return true;
                    }
                    String arenaName = args[2];
                    Arena arena = walls.arenas.getArena(arenaName);
                    if (arena == null || arena.getLobby() == null) {
                        sender.sendMessage(Messages.msg("walls.no_lobby", java.util.Map.of("arena", arenaName)));
                        return true;
                    }
                    arena.clearLobby();
                    sender.sendMessage(Messages.msg("walls.lobby_removed", java.util.Map.of("arena", arena.getName())));
                    return true;
                }
                sender.sendMessage(Messages.msg("walls.lobby_usage"));
                return true;
            }
            if (sub.equals("hub")) {
                if (!sender.hasPermission("thewalls.walls.hub") && !sender.isOp()) {
                    sender.sendMessage(Messages.msg("admin.no_permission"));
                    return true;
                }
                if (!(sender instanceof Player)) {
                    sender.sendMessage(Messages.msg("walls.only_player"));
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(Messages.msg("hub.usage"));
                    return true;
                }
                String action = args[1].toLowerCase();
                if (action.equals("set")) {
                    ca.thewalls.Config.setHub(((Player) sender).getLocation());
                    sender.sendMessage(Messages.msg("hub.set"));
                    return true;
                }
                if (action.equals("tp")) {
                    org.bukkit.Location hub = ca.thewalls.Config.getHub();
                    if (hub == null) {
                        sender.sendMessage(Messages.msg("hub.no_hub"));
                        return true;
                    }
                    ((Player) sender).teleport(hub);
                    sender.sendMessage(Messages.msg("hub.tp"));
                    return true;
                }
                if (action.equals("off")) {
                    ca.thewalls.Config.data.set("hub.enabled", false);
                    try {
                        ca.thewalls.Config.data.save(ca.thewalls.Config.dataFile);
                    } catch (java.io.IOException ex) {
                        ca.thewalls.Utils.getPlugin().getLogger().warning(ex.toString());
                    }
                    sender.sendMessage(Messages.msg("hub.disabled"));
                    return true;
                }
                sender.sendMessage(Messages.msg("hub.usage"));
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
            if (sub.equals("hologram")) {
                if (!sender.hasPermission("thewalls.walls.hologram") && !sender.isOp()) {
                    sender.sendMessage(Messages.msg("admin.no_permission"));
                    return true;
                }
                if (args.length < 3 || !args[1].equalsIgnoreCase("top")) {
                    sender.sendMessage(Messages.msg("hologram.usage"));
                    return true;
                }
                String action = args[2].toLowerCase();
                if (action.equals("set")) {
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(Messages.msg("walls.only_player"));
                        return true;
                    }
                    if (walls.topHolograms != null) {
                        walls.topHolograms.setLocation(((Player) sender).getLocation());
                    }
                    sender.sendMessage(Messages.msg("hologram.set"));
                    return true;
                }
                if (action.equals("remove")) {
                    ca.thewalls.Config.data.set("holograms.top.location.world", "");
                    try {
                        ca.thewalls.Config.data.save(ca.thewalls.Config.dataFile);
                    } catch (java.io.IOException ex) {
                        ca.thewalls.Utils.getPlugin().getLogger().warning(ex.toString());
                    }
                    if (walls.topHolograms != null) {
                        walls.topHolograms.remove();
                    }
                    sender.sendMessage(Messages.msg("hologram.removed"));
                    return true;
                }
                if (action.equals("refresh")) {
                    if (walls.topHolograms != null) {
                        walls.topHolograms.refresh();
                    }
                    sender.sendMessage(Messages.msg("hologram.refreshed"));
                    return true;
                }
                sender.sendMessage(Messages.msg("hologram.usage"));
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

    private String[] slice(String[] args, int from) {
        if (args == null || args.length <= from) return new String[0];
        return java.util.Arrays.copyOfRange(args, from, args.length);
    }
}
