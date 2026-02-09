package ca.thewalls.Listeners;

import ca.thewalls.Kits;
import ca.thewalls.Messages;
import ca.thewalls.TheWalls;
import ca.thewalls.Utils;
import com.samjakob.spigui.buttons.SGButton;
import com.samjakob.spigui.item.ItemBuilder;
import com.samjakob.spigui.menu.SGMenu;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

public class KitSelectMenu {
    private KitSelectMenu() {}

    public static void open(TheWalls plugin, Player player) {
        List<String> kits = Kits.getKitIds();
        int fallbackRows = Math.max(2, ((kits.size() - 1) / 9) + 1);
        int rows = Math.max(2, Utils.guiRows("kits", fallbackRows));
        SGMenu menu = plugin.spigui.create("thewalls-kits", rows, Utils.menuTitle("kit", null));

        int maxSlots = rows * 9;
        if (ca.thewalls.Config.data.getBoolean("gui.toolbar.enabled", true) && rows > 1) {
            maxSlots = (rows - 1) * 9;
        }
        java.util.Set<Integer> reserved = new java.util.HashSet<>();
        for (int i = 0; i < 9 && i < maxSlots; i++) reserved.add(i);
        int infoSlot = Utils.guiSlot("gui.slots.kits.info", 4);
        if (infoSlot >= 0 && infoSlot < maxSlots) {
            menu.setButton(infoSlot, new SGButton(
                    Utils.guiItem("gui.items.kit_info", Material.BOOK, null).build()
            ).withListener(event -> event.setCancelled(true)));
            reserved.add(infoSlot);
        }
        Utils.applyGuiToolbar(menu, player, null);

        int slot = 0;
        for (String kitId : kits) {
            while (reserved.contains(slot) && slot < maxSlots) slot++;
            if (slot >= maxSlots) break;
            String name = Kits.getDisplayName(kitId);
            ItemBuilder builder = new ItemBuilder(Material.CHEST)
                    .name(Utils.toLegacy(Utils.componentFromString(name)));
            List<String> lore = Kits.getLore(kitId);
            if (!lore.isEmpty()) {
                java.util.List<String> formatted = new java.util.ArrayList<>();
                for (String line : lore) {
                    formatted.add(Utils.toLegacy(Utils.componentFromString(line)));
                }
                builder = builder.lore(formatted);
            }
            SGButton button = new SGButton(builder.build())
                .withListener(event -> {
                    event.setCancelled(true);
                    ca.thewalls.Config.setPlayerKit(player.getUniqueId(), kitId);
                    player.sendMessage(Messages.msg("walls.kit_selected", java.util.Map.of("kit", name)));
                    ca.thewalls.Arena arena = plugin.getArenaByPlayer(player);
                    if (arena != null && !arena.getGame().started) {
                        ca.thewalls.Kits.applyKitInLobby(player, kitId);
                        ca.thewalls.Listeners.LobbyItems.give(player, arena);
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
