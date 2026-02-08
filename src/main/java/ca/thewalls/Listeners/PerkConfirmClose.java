package ca.thewalls.Listeners;

import ca.thewalls.Messages;
import ca.thewalls.Perks;
import ca.thewalls.Utils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class PerkConfirmClose implements Listener {
    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        String title = e.getView().title() == null ? "" : Utils.toLegacy(Messages.msg("menu.confirm_title"));
        String viewTitle = e.getView().title() == null ? "" : net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().serialize(e.getView().title());
        if (viewTitle.equals(title)) {
            Perks.clearPendingPerk(e.getPlayer().getUniqueId());
        }
    }
}
