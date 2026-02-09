package ca.thewalls.Listeners;

import ca.thewalls.Arena;
import ca.thewalls.Messages;
import ca.thewalls.TheWalls;
import ca.thewalls.Utils;
import com.samjakob.spigui.menu.SGMenu;
import com.samjakob.spigui.buttons.SGButton;
import com.samjakob.spigui.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ArenaSelectMenu {
    private ArenaSelectMenu() {}

    public static void open(TheWalls plugin, Player player) {
        List<Arena> arenas = new ArrayList<>(plugin.arenas.getArenas().values());
        int fallbackRows = Math.max(2, ((arenas.size() - 1) / 9) + 1);
        int rows = Math.max(2, Utils.guiRows("arenas", fallbackRows));
        SGMenu menu = plugin.spigui.create("thewalls-arenas", rows, Utils.menuTitle("arena", null));

        int maxSlots = rows * 9;
        if (ca.thewalls.Config.data.getBoolean("gui.toolbar.enabled", true) && rows > 1) {
            maxSlots = (rows - 1) * 9;
        }
        java.util.Set<Integer> reserved = new java.util.HashSet<>();
        for (int i = 0; i < 9 && i < maxSlots; i++) reserved.add(i);
        int infoSlot = Utils.guiSlot("gui.slots.arenas.info", 4);
        if (infoSlot >= 0 && infoSlot < maxSlots) {
            menu.setButton(infoSlot, new SGButton(
                    Utils.guiItem("gui.items.arena_info", Material.COMPASS, null).build()
            ).withListener(event -> event.setCancelled(true)));
            reserved.add(infoSlot);
        }
        int refreshSlot = Utils.guiSlot("gui.slots.arenas.refresh", 6);
        if (refreshSlot >= 0 && refreshSlot < maxSlots) {
            menu.setButton(refreshSlot, new SGButton(
                    Utils.guiItem("gui.items.arena_refresh", Material.SPYGLASS, null).build()
            ).withListener(event -> {
                event.setCancelled(true);
                open(plugin, player);
            }));
            reserved.add(refreshSlot);
        }
        Utils.applyGuiToolbar(menu, player, null);

        int slot = 0;
        for (Arena arena : arenas) {
            while (reserved.contains(slot) && slot < maxSlots) slot++;
            if (slot >= maxSlots) break;
            ItemBuilder builder = new ItemBuilder(Material.ENDER_EYE)
                    .name(Utils.toLegacy(Messages.msg("menu.arena_item", java.util.Map.of("arena", arena.getName()))));
            java.util.List<String> lore = Messages.list("menu.arena_lore");
            if (!lore.isEmpty()) {
                java.util.List<String> formatted = new java.util.ArrayList<>();
                for (String line : lore) {
                    formatted.add(Utils.toLegacy(Messages.msg(line)));
                }
                builder = builder.lore(formatted);
            }
            SGButton button = new SGButton(builder.build())
                .withListener(event -> {
                    event.setCancelled(true);
                    plugin.arenas.joinPlayer(player, arena.getName());
                    player.sendMessage(Messages.msg("walls.joined", java.util.Map.of("arena", arena.getName())));
                    if (arena.getLobby() == null) {
                        player.sendMessage(Messages.msg("walls.no_lobby", java.util.Map.of("arena", arena.getName())));
                    }
                    player.closeInventory();
                });
            menu.setButton(slot, button);
            slot++;
        }

        Utils.applyGuiFiller(menu);
        player.openInventory(menu.getInventory());
    }
}
