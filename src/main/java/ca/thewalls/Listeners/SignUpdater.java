package ca.thewalls.Listeners;

import ca.thewalls.Arena;
import ca.thewalls.Config;
import ca.thewalls.TheWalls;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Sign;
import net.kyori.adventure.text.Component;
import org.bukkit.block.sign.SignSide;
import org.bukkit.block.sign.Side;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.format.NamedTextColor;

public class SignUpdater {
    private SignUpdater() {}

    public static void updateAll(TheWalls plugin) {
        for (String arenaName : plugin.arenas.getArenas().keySet()) {
            for (String entry : Config.getArenaSigns(arenaName)) {
                Location loc = parse(entry);
                if (loc == null) continue;
                updateSign(plugin, loc, arenaName);
            }
        }
    }

    public static void updateSign(TheWalls plugin, Location loc, String arenaName) {
        if (loc == null || loc.getWorld() == null) return;
        if (!(loc.getBlock().getState() instanceof Sign)) return;
        Arena arena = plugin.arenas.getArena(arenaName);
        if (arena == null) return;
        Sign sign = (Sign) loc.getBlock().getState();
        SignSide side = sign.getSide(Side.FRONT);
        int players = arena.getPlayers().size();
        int minPlayers = ca.thewalls.Config.data.getInt("lobby.minPlayers", 2);
        int countdown = arena.getLobbyCountdown();
        String status = arena.getGame().started ? "In-Game" : "Lobby";
        MiniMessage mm = MiniMessage.miniMessage();
        Component line0 = mm.deserialize("<gradient:#ff8a00:#ffd000><bold>[TheWalls]</bold></gradient>");
        Component line1 = mm.deserialize("<yellow>" + arena.getName() + "</yellow>");
        Component line2 = mm.deserialize(status.equals("In-Game") ? "<red>In-Game</red>" : "<green>Lobby</green>");
        String countText = players + "/" + minPlayers + " players";
        if (!arena.getGame().started && countdown >= 0) {
            countText = "Start: " + countdown + "s";
        }
        Component line3 = Component.text(countText, NamedTextColor.GRAY);
        side.line(0, line0);
        side.line(1, line1);
        side.line(2, line2);
        side.line(3, line3);
        sign.setEditable(false);
        sign.setWaxed(true);
        sign.update();
    }

    private static Location parse(String entry) {
        if (entry == null) return null;
        String[] parts = entry.split(",");
        if (parts.length != 4) return null;
        World world = Bukkit.getWorld(parts[0]);
        if (world == null) return null;
        try {
            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);
            int z = Integer.parseInt(parts[3]);
            return new Location(world, x, y, z);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
