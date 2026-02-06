package ca.thewalls.Listeners;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import ca.thewalls.TheWalls;
import ca.thewalls.Messages;
import ca.thewalls.Walls.Team;
import net.kyori.adventure.text.Component;

public class PlayerDeath implements Listener {
    public TheWalls walls;

    public PlayerDeath(TheWalls walls) {
        this.walls = walls;
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDeath(PlayerDeathEvent e) {
        ca.thewalls.Arena arena = this.walls.getArenaByPlayer(e.getEntity());
        if (arena == null) return;
        if (!arena.getGame().started) return;
        e.deathMessage(Component.empty());

        Player ply = e.getEntity();
        ply.setGameMode(GameMode.SPECTATOR);
        Team temp = Team.getPlayerTeam(ply, arena.getGame().teams);
        if (temp == null) {
            for (Player p : arena.getPlayers()) {
                p.sendMessage(Messages.msg("death.eliminated", java.util.Map.of("player", ply.getName())));
            }
        } else {
            for (Player p : arena.getPlayers()) {
                p.sendMessage(Messages.msg("death.eliminated_team", java.util.Map.of("team", temp.teamColor, "player", ply.getName())));
            }
        }

        for (Player p : arena.getPlayers()) {
            p.playSound(ply.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 100, 1);
            LightningStrike strike = arena.getWorld().world.strikeLightning(ply.getLocation().add(0, 15, 0));
            strike.setVisualFire(false);
            strike.setFireTicks(0);
        }

        if (e.getEntity().getKiller() != null) {
            ca.thewalls.Config.incrementKills(e.getEntity().getKiller().getUniqueId());
            if (walls.topHolograms != null) {
                walls.topHolograms.refresh();
            }
        }

        arena.getUtils().checkWinner();
    }

}
