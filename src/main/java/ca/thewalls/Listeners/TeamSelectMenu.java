package ca.thewalls.Listeners;

import ca.thewalls.Arena;
import ca.thewalls.Config;
import ca.thewalls.Messages;
import ca.thewalls.TheWalls;
import ca.thewalls.Utils;
import com.samjakob.spigui.menu.SGMenu;
import com.samjakob.spigui.buttons.SGButton;
import com.samjakob.spigui.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class TeamSelectMenu {
    private TeamSelectMenu() {}

    public static void open(TheWalls plugin, Player player, Arena arena) {
        int rows = Utils.guiRows("team", 3);
        SGMenu menu = plugin.spigui.create("thewalls-team", rows, Utils.menuTitle("team", null));

        menu.setButton(Utils.guiSlot("gui.slots.team.red", 10), teamButton(plugin, player, arena, 0, "gui.items.team_red", "menu.team_red"));
        menu.setButton(Utils.guiSlot("gui.slots.team.blue", 12), teamButton(plugin, player, arena, 1, "gui.items.team_blue", "menu.team_blue"));
        menu.setButton(Utils.guiSlot("gui.slots.team.yellow", 14), teamButton(plugin, player, arena, 2, "gui.items.team_yellow", "menu.team_yellow"));
        menu.setButton(Utils.guiSlot("gui.slots.team.green", 16), teamButton(plugin, player, arena, 3, "gui.items.team_green", "menu.team_green"));

        Utils.applyGuiFiller(menu);
        player.openInventory(menu.getInventory());
    }

    private static SGButton teamButton(TheWalls plugin, Player player, Arena arena, int teamId, String itemPath, String nameKey) {
        java.util.List<String> lore = Messages.list("menu.team_lore");
        ItemBuilder builder = Utils.guiItem(itemPath, Material.WHITE_WOOL, null)
                .name(Utils.toLegacy(Messages.msg(nameKey)));
        if (!lore.isEmpty()) {
            java.util.List<String> formatted = new java.util.ArrayList<>();
            for (String line : lore) {
                formatted.add(Utils.toLegacy(Messages.msg(line)));
            }
            builder = builder.lore(formatted);
        }
        return new SGButton(builder.build())
                .withListener(event -> {
                    event.setCancelled(true);
                    if (arena.getGame().started) {
                        player.sendMessage(Messages.msg("walls.team_locked"));
                        return;
                    }
                    arena.setTeamPreference(player, teamId);
                    String teamName = switch (teamId) {
                        case 0 -> Config.data.getString("teams.zero.name", "Red");
                        case 1 -> Config.data.getString("teams.one.name", "Blue");
                        case 2 -> Config.data.getString("teams.two.name", "Yellow");
                        case 3 -> Config.data.getString("teams.three.name", "Green");
                        default -> "Team";
                    };
                    player.sendMessage(Messages.msg("walls.team_set", java.util.Map.of("team", teamName)));
                    player.closeInventory();
                });
    }
}
