package ca.thewalls.Listeners;

import ca.thewalls.Config;
import ca.thewalls.Crates;
import ca.thewalls.Messages;
import ca.thewalls.TheWalls;
import ca.thewalls.Utils;
import com.samjakob.spigui.buttons.SGButton;
import com.samjakob.spigui.item.ItemBuilder;
import com.samjakob.spigui.menu.SGMenu;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CrateConfirmMenu {
    private static final Map<java.util.UUID, Crates.Reward> PENDING = new ConcurrentHashMap<>();

    private CrateConfirmMenu() {}

    public static void open(TheWalls plugin, Player player) {
        int rows = Utils.guiRows("crate_confirm", 3);
        SGMenu menu = plugin.spigui.create("thewalls-crate-confirm", rows, Utils.menuTitle("crate_confirm", null));

        Crates.Reward reward = PENDING.get(player.getUniqueId());
        if (reward == null) {
            reward = Crates.rollReward(player);
            if (reward != null) {
                PENDING.put(player.getUniqueId(), reward);
            }
        }
        if (reward == null) {
            player.sendMessage(Messages.msg("crate.no_rewards"));
            player.closeInventory();
            return;
        }

        String name = reward.name == null || reward.name.isEmpty() ? reward.id : reward.name;
        ItemBuilder item = new ItemBuilder(Material.NETHER_STAR)
                .name(Utils.toLegacy(Utils.componentFromString(name)));
        List<String> lore = new ArrayList<>();
        if (reward.description != null && !reward.description.isEmpty()) {
            for (String line : reward.description.split("\\n")) {
                lore.add(Utils.toLegacy(Utils.componentFromString(line)));
            }
        }
        lore.add(Utils.toLegacy(Messages.msg("menu.crate_cost", Map.of("amount", Crates.getCurrencySymbol() + reward.cost))));
        item = item.lore(lore);
        int previewSlot = Utils.guiSlot("gui.slots.crate_confirm.preview", 13);
        if (previewSlot >= 0 && previewSlot < rows * 9) {
            menu.setButton(previewSlot, new SGButton(item.build()).withListener(event -> event.setCancelled(true)));
        }

        ItemBuilder accept = Utils.guiItem("gui.items.confirm_accept", Material.LIME_CONCRETE, null)
                .name(Utils.toLegacy(Messages.msg("menu.confirm_accept")));
        int acceptSlot = Utils.guiSlot("gui.slots.crate_confirm.accept", 11);
        menu.setButton(acceptSlot, new SGButton(accept.build()).withListener(event -> {
            event.setCancelled(true);
            Crates.Reward pending = PENDING.get(player.getUniqueId());
            if (pending == null) {
                player.closeInventory();
                return;
            }
            if (!ca.thewalls.EconomyService.isAvailable()) {
                player.sendMessage(Messages.msg("walls.economy_missing"));
                player.closeInventory();
                PENDING.remove(player.getUniqueId());
                return;
            }
            if (Crates.isOwned(player, pending)) {
                player.sendMessage(Messages.msg("crate.already_owned"));
                player.closeInventory();
                PENDING.remove(player.getUniqueId());
                return;
            }
            if (pending.cost > 0) {
                double bal = ca.thewalls.EconomyService.getBalance(player);
                if (bal < pending.cost) {
                    player.sendMessage(Messages.msg("walls.not_enough_money"));
                    player.closeInventory();
                    PENDING.remove(player.getUniqueId());
                    return;
                }
                ca.thewalls.EconomyService.withdraw(player, pending.cost);
                String amount = Crates.getCurrencySymbol() + ca.thewalls.EconomyService.format(pending.cost);
                player.sendMessage(Messages.msg("walls.money_taken", Map.of("amount", amount)));
            }
            Crates.grant(player, pending);
            player.sendMessage(Messages.msg("crate.reward_unlocked", Map.of(
                    "name", name
            )));
            PENDING.remove(player.getUniqueId());
            player.closeInventory();
        }));

        ItemBuilder cancel = Utils.guiItem("gui.items.confirm_cancel", Material.RED_CONCRETE, null)
                .name(Utils.toLegacy(Messages.msg("menu.confirm_cancel")));
        int cancelSlot = Utils.guiSlot("gui.slots.crate_confirm.cancel", 15);
        menu.setButton(cancelSlot, new SGButton(cancel.build()).withListener(event -> {
            event.setCancelled(true);
            PENDING.remove(player.getUniqueId());
            player.closeInventory();
        }));

        Utils.applyGuiFiller(menu);
        player.openInventory(menu.getInventory());
    }
}
