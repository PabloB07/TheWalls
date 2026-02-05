package ca.thewalls.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ca.thewalls.Config;
import ca.thewalls.Messages;
import ca.thewalls.TheWalls;
import ca.thewalls.Utils;

// Start command for The Walls
public class WStart implements CommandExecutor {
    public TheWalls walls;
    public WStart(TheWalls walls) {
        this.walls = walls;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("thewalls.start") && !sender.isOp()) {
            sender.sendMessage(Messages.msg("admin.no_permission"));
            return false;
        }

        String arenaName = null;
        int index = 0;
        if (args.length >= 1 && !isInt(args[0])) {
            arenaName = args[0];
            index = 1;
        }
        ca.thewalls.Arena arena = (sender instanceof Player)
                ? this.walls.getArenaByPlayer((Player) sender)
                : null;
        if (arenaName != null) {
            arena = this.walls.arenas.createArena(arenaName);
        }
        if (arena == null) {
            sender.sendMessage(Messages.msg("wstart.arena_required"));
            return false;
        }

        if (arena.getGame().started) {
            sender.sendMessage(Utils.format("&cThere is already an ongoing game of The Walls!"));
            return false;
        }

        if (arena.getPlayers().isEmpty()) {
            sender.sendMessage(Messages.msg("wstart.no_players", java.util.Map.of("arena", arena.getName())));
            return false;
        }

        try {
            
            // In case the admin doesn't properly setup game values
            arena.getGame().size = Config.data.getInt("theWalls.autoExecute.size");
            if (args.length >= index + 1) {
                arena.getGame().size = parseInt(args[index], "size");
            }
            arena.getGame().prepTime = Config.data.getInt("theWalls.autoExecute.prepTime");
            if (args.length >= index + 2) {
                arena.getGame().prepTime = parseInt(args[index + 1], "prepTime");
            }
            arena.getGame().borderCloseTime = Config.data.getInt("theWalls.autoExecute.timeUntilBorderClose");
            if (args.length >= index + 3) {
                arena.getGame().borderCloseTime = parseInt(args[index + 2], "borderCloseTime");
            }
            arena.getGame().borderCloseSpeed = Config.data.getInt("theWalls.autoExecute.speedOfBorderClose");
            if (args.length >= index + 4) {
                arena.getGame().borderCloseSpeed = parseInt(args[index + 3], "borderCloseSpeed");
            }
            arena.getGame().eventCooldown = Config.data.getInt("theWalls.autoExecute.eventCooldown");
            if (args.length >= index + 5) {
                arena.getGame().eventCooldown = parseInt(args[index + 4], "eventCooldown");
            }
            if (sender instanceof Player) {
                arena.getGame().start((Player) sender);
            } else {
                arena.getGame().start(null);
            }
        } catch (IllegalArgumentException ex) {
            sender.sendMessage(Messages.msg("wstart.invalid_number"));
            return false;
        } catch (Exception ex) {
            sender.sendMessage(Utils.format("&cAn error occured while starting The Walls!"));
            Utils.getPlugin().getLogger().warning(ex.toString());
        }


        return false;
    }

    private boolean isInt(String input) {
        if (input == null) return false;
        try {
            Integer.parseInt(input);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private int parseInt(String input, String name) {
        if (input == null) throw new IllegalArgumentException(name);
        return Integer.parseInt(input);
    }
}
