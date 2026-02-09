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
        int size = Math.max(1, ((kits.size() - 1) / 9) + 1);
        SGMenu menu = plugin.spigui.create("thewalls-kits", size, Utils.menuTitle("menu.kit_title", null));

        int slot = 0;
        for (String kitId : kits) {
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

        player.openInventory(menu.getInventory());
    }
}
