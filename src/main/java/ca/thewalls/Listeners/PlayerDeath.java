package ca.thewalls.Listeners;

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
        arena.getGame().setLastDeathLocation(ply, ply.getLocation());
        ply.setGameMode(GameMode.SPECTATOR);
        ca.thewalls.Utils.sendTitle(ply, Messages.raw("death.spectator_title"), Messages.raw("death.spectator_subtitle"), 10, 60, 10);
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

        arena.getGame().removeBoard(ply);
        arena.getGame().disableTablistHeartsForPlayer(ply);

        if (e.getEntity().getKiller() != null) {
            Player killer = e.getEntity().getKiller();
            if (ca.thewalls.AntiAbuse.isKillRewardAllowed(killer, ply)) {
                ca.thewalls.Config.incrementKills(killer.getUniqueId());
                if (walls.topHolograms != null) {
                    walls.topHolograms.refresh();
                }
                arena.getGame().getBounty().handleKill(killer, ply);
                ca.thewalls.Cosmetics.playKillEffect(killer, ply.getLocation());
            } else {
                killer.sendMessage(Messages.msg("antiabuse.kill_ignored"));
            }
        }

        arena.getUtils().checkWinner();
    }

}
