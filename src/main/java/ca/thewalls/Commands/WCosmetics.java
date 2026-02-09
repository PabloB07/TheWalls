package ca.thewalls.Commands;

import ca.thewalls.Messages;
import ca.thewalls.TheWalls;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WCosmetics implements CommandExecutor {
    private final TheWalls walls;

    public WCosmetics(TheWalls walls) {
        this.walls = walls;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("thewalls.walls.cosmetics") && !sender.isOp()) {
            sender.sendMessage(Messages.msg("admin.no_permission"));
            return true;
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage(Messages.msg("walls.only_player"));
            return true;
        }
        ca.thewalls.Listeners.CosmeticsMenu.openMain(walls, (Player) sender);
        return true;
    }
}
