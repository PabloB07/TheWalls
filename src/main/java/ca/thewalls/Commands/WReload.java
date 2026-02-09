package ca.thewalls.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import ca.thewalls.Config;
import ca.thewalls.Messages;
import ca.thewalls.TheWalls;
import ca.thewalls.Listeners.SignUpdater;

public class WReload implements CommandExecutor {
    public TheWalls walls;

    public WReload(TheWalls walls) {
        this.walls = walls;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("thewalls.reload") && !sender.hasPermission("thewalls.walls.reload") && !sender.isOp()) {
            sender.sendMessage(Messages.msg("admin.no_permission"));
            return false;
        }

        Config.initializeData();
        Messages.reload();
        Messages.applyDefaults(walls);
        Config.reloadPerksAndCrates();
        ca.thewalls.EconomyService.setup();
        ca.thewalls.Kits.validateAll();
        if (walls.arenas != null) {
            walls.arenas.reloadFromConfig();
            for (ca.thewalls.Arena arena : walls.arenas.getArenas().values()) {
                if (arena.getGame().started) continue;
                for (org.bukkit.entity.Player p : arena.getPlayers()) {
                    String kitId = ca.thewalls.Config.getPlayerKit(p.getUniqueId());
                    if (kitId == null || kitId.isEmpty() || !ca.thewalls.Kits.isValidKit(kitId)) {
                        kitId = ca.thewalls.Kits.getDefaultKit();
                        if (kitId != null && !kitId.isEmpty()) {
                            ca.thewalls.Config.setPlayerKit(p.getUniqueId(), kitId);
                        }
                    }
                    if (kitId != null && !kitId.isEmpty() && ca.thewalls.Kits.isValidKit(kitId)) {
                        ca.thewalls.Kits.applyKitInLobby(p, kitId);
                    }
                    ca.thewalls.Listeners.LobbyItems.give(p, arena);
                }
            }
        }
        SignUpdater.updateAll(walls);
        if (walls.topHolograms != null) {
            walls.topHolograms.refresh();
        }
        sender.sendMessage(Messages.msg("admin.reload_ok"));
        return true;
    }
}
