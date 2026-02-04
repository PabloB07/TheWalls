package ca.thewalls.Listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import ca.thewalls.TheWalls;
import ca.thewalls.Walls.Team;

public class PlayerAttack implements Listener {
    public TheWalls walls;

    public PlayerAttack(TheWalls walls) {
        this.walls = walls;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerAttack(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player)) return;
        if (!(e.getEntity() instanceof Player)) return;
        ca.thewalls.Arena arena = this.walls.getArenaByPlayer((Player) e.getDamager());
        ca.thewalls.Arena receiverArena = this.walls.getArenaByPlayer((Player) e.getEntity());
        if (arena == null || receiverArena == null) return;
        if (arena != receiverArena) return;
        if (!arena.getGame().started) return;

        Player attacker = (Player) e.getDamager();
        Player receiver = (Player) e.getEntity();

        if (Team.getPlayerTeam(attacker, arena.getGame().teams) == Team.getPlayerTeam(receiver, arena.getGame().teams)) {
            e.setDamage(0); // Cancel damage, but still allow for knock-back
        }
    }
}
