package ca.thewalls.Listeners;

import ca.thewalls.Messages;
import ca.thewalls.TheWalls;
import ca.thewalls.Config;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.block.sign.SignSide;
import org.bukkit.block.sign.Side;
import org.bukkit.event.player.PlayerInteractEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class JoinSign implements Listener {
    private final TheWalls walls;

    public JoinSign(TheWalls walls) {
        this.walls = walls;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onSignChange(SignChangeEvent e) {
        if (!e.getPlayer().isOp()) return;
        String line0 = PlainTextComponentSerializer.plainText().serialize(e.line(0));
        String line1 = PlainTextComponentSerializer.plainText().serialize(e.line(1));
        if (line0 == null || line1 == null) return;
        if (!line0.equalsIgnoreCase("[TheWalls]")) return;
        if (!line1.equalsIgnoreCase("join")) return;

        String arenaName = PlainTextComponentSerializer.plainText().serialize(e.line(2));
        if (arenaName == null || arenaName.trim().isEmpty()) {
            e.getPlayer().sendMessage(Messages.msg("walls.join_usage"));
            return;
        }
        e.line(0, Component.text("[TheWalls]"));
        e.line(1, Component.text("Join"));
        e.line(2, Component.text(arenaName));
        Config.addArenaSign(arenaName, e.getBlock().getLocation());
        SignUpdater.updateSign(walls, e.getBlock().getLocation(), arenaName);
        if (e.getBlock().getState() instanceof Sign sign) {
            sign.setEditable(false);
            sign.setWaxed(true);
            sign.update();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSignUse(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK && e.getAction() != Action.LEFT_CLICK_BLOCK) return;
        Block block = e.getClickedBlock();
        if (block == null) return;
        if (!(block.getState() instanceof Sign)) return;
        Sign sign = (Sign) block.getState();
        Player player = e.getPlayer();
        String arenaName = getArenaForSign(sign);
        if (arenaName == null || arenaName.isEmpty()) {
            player.sendMessage(Messages.msg("walls.join_usage"));
            return;
        }
        e.setCancelled(true);
        e.setUseInteractedBlock(org.bukkit.event.Event.Result.DENY);
        e.setUseItemInHand(org.bukkit.event.Event.Result.DENY);
        sign.setEditable(false);
        sign.setWaxed(true);
        sign.update();
        walls.arenas.createArena(arenaName);
        walls.arenas.joinPlayer(player, arenaName);
        player.sendMessage(Messages.msg("walls.joined", java.util.Map.of("arena", arenaName)));
        if (walls.arenas.getArena(arenaName) != null && walls.arenas.getArena(arenaName).getLobby() == null) {
            player.sendMessage(Messages.msg("walls.no_lobby", java.util.Map.of("arena", arenaName)));
        }
        LobbyItems.give(player, walls.arenas.getArena(arenaName));
    }

    private String getArenaForSign(Sign sign) {
        if (sign == null || sign.getWorld() == null) return null;
        String world = sign.getWorld().getName();
        int x = sign.getX();
        int y = sign.getY();
        int z = sign.getZ();
        for (String arenaName : ca.thewalls.Config.getArenaNames()) {
            for (String entry : ca.thewalls.Config.getArenaSigns(arenaName)) {
                String[] parts = entry.split(",");
                if (parts.length != 4) continue;
                if (!parts[0].equalsIgnoreCase(world)) continue;
                try {
                    int sx = Integer.parseInt(parts[1]);
                    int sy = Integer.parseInt(parts[2]);
                    int sz = Integer.parseInt(parts[3]);
                    if (sx == x && sy == y && sz == z) {
                        return arenaName;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return null;
    }
}
