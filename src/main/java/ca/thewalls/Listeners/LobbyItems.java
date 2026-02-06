package ca.thewalls.Listeners;

import ca.thewalls.Arena;
import ca.thewalls.Config;
import ca.thewalls.Messages;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class LobbyItems {
    private LobbyItems() {}

    public static void give(Player player, Arena arena) {
        if (arena.getGame().started) return;
        if (!Config.data.getBoolean("lobby.items.enabled", true)) return;
        int teamSlot = Config.data.getInt("lobby.items.teamSelectorSlot", 0);
        int leaveSlot = Config.data.getInt("lobby.items.leaveSlot", 8);

        ItemStack team = new ItemStack(Material.NETHER_STAR);
        ItemMeta teamMeta = team.getItemMeta();
        teamMeta.displayName(Messages.msg("lobby.items.team_selector").decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false));
        team.setItemMeta(teamMeta);

        ItemStack leave = new ItemStack(Material.BARRIER);
        ItemMeta leaveMeta = leave.getItemMeta();
        leaveMeta.displayName(Messages.msg("lobby.items.leave").decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false));
        leave.setItemMeta(leaveMeta);

        player.getInventory().setItem(teamSlot, team);
        player.getInventory().setItem(leaveSlot, leave);
    }

    public static boolean isTeamSelector(ItemStack item) {
        if (item == null) return false;
        if (item.getType() != Material.NETHER_STAR) return false;
        return item.hasItemMeta();
    }

    public static boolean isLeaveItem(ItemStack item) {
        if (item == null) return false;
        if (item.getType() != Material.BARRIER) return false;
        return item.hasItemMeta();
    }

    public static void clear(Player player) {
        if (player == null) return;
        if (!Config.data.getBoolean("lobby.items.enabled", true)) return;
        int teamSlot = Config.data.getInt("lobby.items.teamSelectorSlot", 0);
        int leaveSlot = Config.data.getInt("lobby.items.leaveSlot", 8);
        player.getInventory().setItem(teamSlot, null);
        player.getInventory().setItem(leaveSlot, null);
    }
}
