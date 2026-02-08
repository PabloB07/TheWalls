package ca.thewalls.Listeners;

import ca.thewalls.Config;
import ca.thewalls.Messages;
import ca.thewalls.Perks;
import ca.thewalls.TheWalls;
import ca.thewalls.Utils;
import com.samjakob.spigui.buttons.SGButton;
import com.samjakob.spigui.item.ItemBuilder;
import com.samjakob.spigui.menu.SGMenu;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PerkMenu {
    private PerkMenu() {}

    public static void open(TheWalls plugin, Player player) {
        List<String> perks = Perks.getPerkIds();
        int size = Math.max(1, ((perks.size() - 1) / 9) + 1);
        String balanceText = "";
        if (ca.thewalls.EconomyService.isAvailable()) {
            double balance = ca.thewalls.EconomyService.getBalance(player);
            balanceText = Perks.getCurrencySymbol() + ca.thewalls.EconomyService.format(balance);
        }
        String title = Utils.menuTitle("menu.perk_title", "Perks");
        SGMenu menu = plugin.spigui.create("thewalls-perks", size, title);

        int slot = 0;
        for (String perkId : perks) {
            String name = Perks.getName(perkId);
            String desc = Perks.getDescription(perkId);
            boolean unlocked = Config.hasPerk(player.getUniqueId(), perkId);
            ItemBuilder builder = new ItemBuilder(unlocked ? Material.EMERALD : Material.REDSTONE)
                    .name(Utils.toLegacy(Utils.componentFromString(name)));
            List<String> lore = new ArrayList<>();
            if (desc != null && !desc.isEmpty()) {
                lore.add(Utils.toLegacy(Utils.componentFromString(desc)));
            }
            int cost = Perks.getPerkCost(perkId);
            if (cost > 0) {
                lore.add(Utils.toLegacy(Messages.msg("menu.crate_cost", java.util.Map.of("amount", Perks.getCurrencySymbol() + cost))));
            }
            lore.add(Utils.toLegacy(Messages.msg(unlocked ? "menu.perk_unlocked" : "menu.perk_locked")));
            builder = builder.lore(lore);
            SGButton button = new SGButton(builder.build()).withListener(event -> event.setCancelled(true));
            menu.setButton(slot, button);
            slot++;
        }

        // Crate button
        if (Config.crates != null && Config.crates.getBoolean("crates.enabled", true)) {
            ItemBuilder crate = new ItemBuilder(Material.ENDER_CHEST)
                    .name(Utils.toLegacy(Utils.componentFromString(Config.crates.getString("crates.display.name", "Crate"))));
            List<String> lore = Config.crates.getStringList("crates.display.lore");
            if (!lore.isEmpty()) {
                List<String> formatted = new ArrayList<>();
                for (String line : lore) {
                    formatted.add(Utils.toLegacy(Utils.componentFromString(line)));
                }
                crate = crate.lore(formatted);
            }
            if (!balanceText.isEmpty()) {
                List<String> currentLore = new ArrayList<>(crate.build().getLore() == null ? java.util.Collections.emptyList() : crate.build().getLore());
                currentLore.add(Utils.toLegacy(Messages.msg("menu.balance", java.util.Map.of("amount", balanceText))));
                crate = crate.lore(currentLore);
            }
            // show base cost in lore
            int[] range = Perks.getCostRange();
            if (range[0] > 0 || range[1] > 0) {
                String amount = (range[0] == range[1])
                        ? (Perks.getCurrencySymbol() + range[0])
                        : (Perks.getCurrencySymbol() + range[0] + "-" + Perks.getCurrencySymbol() + range[1]);
                List<String> currentLore = new ArrayList<>(crate.build().getLore() == null ? java.util.Collections.emptyList() : crate.build().getLore());
                currentLore.add(Utils.toLegacy(Messages.msg("menu.crate_cost", java.util.Map.of("amount", amount))));
                crate = crate.lore(currentLore);
            }
            SGButton crateButton = new SGButton(crate.build()).withListener(event -> {
                event.setCancelled(true);
                if (!ca.thewalls.EconomyService.isAvailable()) {
                    player.sendMessage(Messages.msg("walls.economy_missing"));
                    return;
                }
                PerkConfirmMenu.open(plugin, player, null, 0.0);
            });
            menu.setButton(Math.min(size * 9 - 1, 8), crateButton);
        }

        player.openInventory(menu.getInventory());
    }
}
