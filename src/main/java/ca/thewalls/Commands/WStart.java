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

        if (!(sender instanceof Player) && !Config.data.isSet("theWalls.autoExecute.center")) {
            sender.sendMessage(Utils.format("&cYou need to be a player in the world to run this command or you can setup the default match config in config.yml!"));
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
                : this.walls.mainArena;
        if (arena == null) {
            arena = this.walls.mainArena;
        }
        if (arenaName != null) {
            arena = this.walls.arenas.createArena(arenaName);
        }

        if (arena.getGame().started) {
            sender.sendMessage(Utils.format("&cThere is already an ongoing game of The Walls!"));
            return false;
        }

        try {
            
            // In case the admin doesn't properly setup game values
            arena.getGame().size = Config.data.getInt("theWalls.autoExecute.size");
            if (args.length >= index + 1) {
                arena.getGame().size = Integer.parseInt(args[index]);
            }
            arena.getGame().prepTime = Config.data.getInt("theWalls.autoExecute.prepTime");
            if (args.length >= index + 2) {
                arena.getGame().prepTime = Integer.parseInt(args[index + 1]);
            }
            arena.getGame().borderCloseTime = Config.data.getInt("theWalls.autoExecute.timeUntilBorderClose");
            if (args.length >= index + 3) {
                arena.getGame().borderCloseTime = Integer.parseInt(args[index + 2]);
            }
            arena.getGame().borderCloseSpeed = Config.data.getInt("theWalls.autoExecute.speedOfBorderClose");
            if (args.length >= index + 4) {
                arena.getGame().borderCloseSpeed = Integer.parseInt(args[index + 3]);
            }
            arena.getGame().eventCooldown = Config.data.getInt("theWalls.autoExecute.eventCooldown");
            if (args.length >= index + 5) {
                arena.getGame().eventCooldown = Integer.parseInt(args[index + 4]);
            }
            if (sender instanceof Player) {
                arena.getGame().start((Player) sender);
            } else {
                arena.getGame().start(null);
            }
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
}
