package ca.thewalls.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import ca.thewalls.Config;
import ca.thewalls.Messages;
import ca.thewalls.TheWalls;

public class WReload implements CommandExecutor {
    public TheWalls walls;

    public WReload(TheWalls walls) {
        this.walls = walls;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("thewalls.reload") && !sender.isOp()) {
            sender.sendMessage(Messages.msg("admin.no_permission"));
            return false;
        }

        Config.initializeData();
        Messages.reload();
        if (walls.lobbyHolograms != null) {
            walls.lobbyHolograms.refreshAll();
        }
        sender.sendMessage(Messages.msg("admin.reload_ok"));
        return true;
    }
}
