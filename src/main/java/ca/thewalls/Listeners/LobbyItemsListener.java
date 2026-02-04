package ca.thewalls.Listeners;

import ca.thewalls.Messages;
import ca.thewalls.TheWalls;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class LobbyItemsListener implements Listener {
    private final TheWalls walls;

    public LobbyItemsListener(TheWalls walls) {
        this.walls = walls;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        if (!e.getAction().isRightClick()) return;
        if (e.getItem() == null) return;
        ca.thewalls.Arena arena = walls.getArenaByPlayer(player);
        if (arena == null || arena.getLobby() == null) return;
        if (arena.getGame().started) return;

        if (LobbyItems.isTeamSelector(e.getItem())) {
            e.setCancelled(true);
            TeamSelectMenu.open(walls, player, arena);
            return;
        }
        if (LobbyItems.isLeaveItem(e.getItem())) {
            e.setCancelled(true);
            walls.arenas.leavePlayer(player);
            LobbyItems.clear(player);
            player.sendMessage(Messages.msg("walls.left"));
            return;
        }
    }
}
