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
        int size = Math.max(1, ((arenas.size() - 1) / 9) + 1);
        SGMenu menu = plugin.spigui.create("thewalls-arenas", size, Utils.menuTitle("menu.arena_title", null));

        int slot = 0;
        for (Arena arena : arenas) {
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

        player.openInventory(menu.getInventory());
    }
}
