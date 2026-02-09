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
        int fallbackRows = Math.max(2, ((perks.size() - 1) / 9) + 1);
        int rows = Math.max(2, Utils.guiRows("perks", fallbackRows));
        String balanceText = "";
        if (ca.thewalls.EconomyService.isAvailable()) {
            double balance = ca.thewalls.EconomyService.getBalance(player);
            balanceText = Perks.getCurrencySymbol() + ca.thewalls.EconomyService.format(balance);
        }
        String title = Utils.menuTitle("perk", null);
        SGMenu menu = plugin.spigui.create("thewalls-perks", rows, title);

        int maxSlots = rows * 9;
        if (ca.thewalls.Config.data.getBoolean("gui.toolbar.enabled", true) && rows > 1) {
            maxSlots = (rows - 1) * 9;
        }
        java.util.Set<Integer> reserved = new java.util.HashSet<>();
        for (int i = 0; i < 9 && i < maxSlots; i++) reserved.add(i);
        int infoSlot = Utils.guiSlot("gui.slots.perks.info", 4);
        if (infoSlot >= 0 && infoSlot < maxSlots) {
            menu.setButton(infoSlot, new SGButton(
                    Utils.guiItem("gui.items.perk_info", Material.ENCHANTED_BOOK, null).build()
            ).withListener(event -> event.setCancelled(true)));
            reserved.add(infoSlot);
        }
        int balanceSlot = Utils.guiSlot("gui.slots.perks.balance", 0);
        if (!balanceText.isEmpty() && balanceSlot >= 0 && balanceSlot < maxSlots) {
            menu.setButton(balanceSlot, new SGButton(
                    Utils.guiItem("gui.items.balance", Material.GOLD_NUGGET, java.util.Map.of("amount", balanceText)).build()
            ).withListener(event -> event.setCancelled(true)));
            reserved.add(balanceSlot);
        }
        Utils.applyGuiToolbar(menu, player, null);

        int slot = 0;
        for (String perkId : perks) {
            while (reserved.contains(slot) && slot < maxSlots) slot++;
            if (slot >= maxSlots) break;
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
        if (ca.thewalls.Crates.isEnabled()) {
            ItemBuilder crate = Utils.guiItem("gui.items.perks_crate", Material.ENDER_CHEST, null);
            // show base cost in lore
            int[] range = ca.thewalls.Crates.getCostRange();
            if (range[0] > 0 || range[1] > 0) {
                String amount = (range[0] == range[1])
                        ? (ca.thewalls.Crates.getCurrencySymbol() + range[0])
                        : (ca.thewalls.Crates.getCurrencySymbol() + range[0] + "-" + ca.thewalls.Crates.getCurrencySymbol() + range[1]);
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
                CrateConfirmMenu.open(plugin, player);
            });
            int crateSlot = Utils.guiSlot("gui.slots.perks.crate", 8);
            if (crateSlot >= 0 && crateSlot < maxSlots) {
                menu.setButton(crateSlot, crateButton);
                reserved.add(crateSlot);
            }
        }

        Utils.applyGuiFiller(menu);
        player.openInventory(menu.getInventory());
    }
}
