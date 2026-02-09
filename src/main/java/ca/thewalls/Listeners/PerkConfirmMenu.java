package ca.thewalls.Listeners;

import ca.thewalls.Config;
import ca.thewalls.EconomyHook;
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

public class PerkConfirmMenu {
    private PerkConfirmMenu() {}

    public static void open(TheWalls plugin, Player player, String perkId, double cost) {
        SGMenu menu = plugin.spigui.create("thewalls-perk-confirm", 1, Utils.menuTitle("menu.confirm_title", null));

        String resolvedPerk = perkId;
        if (resolvedPerk == null || resolvedPerk.isEmpty()) {
            String pending = Perks.getPendingPerk(player.getUniqueId());
            resolvedPerk = (pending == null || pending.isEmpty()) ? Perks.rollRandomPerk() : pending;
            Perks.setPendingPerk(player.getUniqueId(), resolvedPerk);
        }
        if (resolvedPerk == null) {
            player.sendMessage(Messages.msg("walls.no_perks"));
            player.closeInventory();
            return;
        }
        int perkCost = Perks.getPerkCost(resolvedPerk);
        double finalCost = perkCost > 0 ? perkCost : cost;
        final String perkFinal = resolvedPerk;
        final double costFinal = finalCost;

        String name = Perks.getName(resolvedPerk);
        String desc = Perks.getDescription(resolvedPerk);
        ItemBuilder perkItem = new ItemBuilder(Material.NETHER_STAR)
                .name(Utils.toLegacy(Utils.componentFromString(name)));
        List<String> lore = new ArrayList<>();
        if (desc != null && !desc.isEmpty()) {
            lore.add(Utils.toLegacy(Utils.componentFromString(desc)));
        }
        lore.add(Utils.toLegacy(Messages.msg("menu.crate_cost", java.util.Map.of("amount", Perks.getCurrencySymbol() + costFinal))));
        perkItem = perkItem.lore(lore);
        menu.setButton(4, new SGButton(perkItem.build()).withListener(event -> event.setCancelled(true)));

        ItemBuilder accept = new ItemBuilder(Material.LIME_CONCRETE)
                .name(Utils.toLegacy(Messages.msg("menu.confirm_accept")));
        menu.setButton(2, new SGButton(accept.build()).withListener(event -> {
            event.setCancelled(true);
            if (!ca.thewalls.EconomyService.isAvailable()) {
                player.sendMessage(Messages.msg("walls.economy_missing"));
                player.closeInventory();
                return;
            }
            if (Config.hasPerk(player.getUniqueId(), perkFinal)) {
                player.sendMessage(Messages.msg("walls.perk_already"));
                player.closeInventory();
                Perks.clearPendingPerk(player.getUniqueId());
                return;
            }
            if (costFinal > 0.0) {
                double balance = ca.thewalls.EconomyService.getBalance(player);
                if (balance < costFinal) {
                    player.sendMessage(Messages.msg("walls.not_enough_money"));
                    player.closeInventory();
                    Perks.clearPendingPerk(player.getUniqueId());
                    return;
                }
                ca.thewalls.EconomyService.withdraw(player, costFinal);
                String amount = Perks.getCurrencySymbol() + ca.thewalls.EconomyService.format(costFinal);
                player.sendMessage(Messages.msg("walls.money_taken", java.util.Map.of("amount", amount)));
            }
            Config.unlockPerk(player.getUniqueId(), perkFinal);
            player.sendMessage(Messages.msg("walls.perk_unlocked", java.util.Map.of("perk", Perks.getName(perkFinal))));
            Perks.clearPendingPerk(player.getUniqueId());
            player.closeInventory();
        }));

        ItemBuilder cancel = new ItemBuilder(Material.RED_CONCRETE)
                .name(Utils.toLegacy(Messages.msg("menu.confirm_cancel")));
        menu.setButton(6, new SGButton(cancel.build()).withListener(event -> {
            event.setCancelled(true);
            Perks.clearPendingPerk(player.getUniqueId());
            player.closeInventory();
        }));

        player.openInventory(menu.getInventory());
    }
}
