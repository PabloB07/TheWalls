package ca.thewalls.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import ca.thewalls.Messages;
import ca.thewalls.TheWalls;
import ca.thewalls.Utils;

public class WEnd implements CommandExecutor {
    public TheWalls walls;
    public WEnd(TheWalls walls) {
        this.walls = walls;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("thewalls.end") && !sender.isOp()) {
            sender.sendMessage(Messages.msg("admin.no_permission"));
            return false;
        }
        ca.thewalls.Arena arena = (sender instanceof org.bukkit.entity.Player)
                ? this.walls.getArenaByPlayer((org.bukkit.entity.Player) sender)
                : null;
        if (args.length >= 1) {
            ca.thewalls.Arena target = this.walls.arenas.getArena(args[0]);
            if (target != null) {
                arena = target;
            }
        }
        if (arena == null) {
            sender.sendMessage(Messages.msg("wstart.arena_required"));
            return false;
        }

        if (!arena.getGame().started) {
            sender.sendMessage(Utils.format("&cThere is no game currently going on!"));
            return false;
        }

        arena.getGame().end(true, null);
        return false;
    }
}
