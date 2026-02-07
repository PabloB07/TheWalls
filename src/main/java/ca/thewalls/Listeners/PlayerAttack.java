package ca.thewalls.Listeners;

import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import ca.thewalls.Config;
import ca.thewalls.TheWalls;
import ca.thewalls.Utils;
import ca.thewalls.Walls.Team;

public class PlayerAttack implements Listener {
    public TheWalls walls;

    public PlayerAttack(TheWalls walls) {
        this.walls = walls;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerAttack(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        Player receiver = (Player) e.getEntity();
        Player attacker = null;
        if (e.getDamager() instanceof Player) {
            attacker = (Player) e.getDamager();
        } else if (e.getDamager() instanceof Projectile) {
            Projectile proj = (Projectile) e.getDamager();
            if (proj.getShooter() instanceof Player) {
                attacker = (Player) proj.getShooter();
            }
        }
        if (attacker == null) return;

        ca.thewalls.Arena arena = this.walls.getArenaByPlayer(attacker);
        ca.thewalls.Arena receiverArena = this.walls.getArenaByPlayer((Player) e.getEntity());
        if (arena == null || receiverArena == null) return;
        if (arena != receiverArena) return;
        if (!arena.getGame().started) return;

        if (Team.getPlayerTeam(attacker, arena.getGame().teams) == Team.getPlayerTeam(receiver, arena.getGame().teams)) {
            e.setDamage(0); // Cancel damage, but still allow for knock-back
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerHitBlood(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        Player receiver = (Player) e.getEntity();
        if (receiver.getGameMode() == org.bukkit.GameMode.SPECTATOR) return;
        Player attacker = null;
        if (e.getDamager() instanceof Player) {
            attacker = (Player) e.getDamager();
        } else if (e.getDamager() instanceof Projectile) {
            Projectile proj = (Projectile) e.getDamager();
            if (proj.getShooter() instanceof Player) {
                attacker = (Player) proj.getShooter();
            }
        }
        if (attacker == null) return;
        ca.thewalls.Arena arena = this.walls.getArenaByPlayer(attacker);
        ca.thewalls.Arena receiverArena = this.walls.getArenaByPlayer(receiver);
        if (arena == null || receiverArena == null) return;
        if (arena != receiverArena) return;
        if (!arena.getGame().started) return;
        if (!Config.data.getBoolean("events.blood.enabled", true)) return;
        if (e.getFinalDamage() <= 0.0) return;

        int count = Config.data.getInt("events.blood.amount", 12);
        double spread = Config.data.getDouble("events.blood.spread", 0.3);
        float size = (float) Config.data.getDouble("events.blood.size", 1.0);
        String colorHex = Config.data.getString("events.blood.color", "#8b0000");
        Color color = Utils.parseHexColor(colorHex, Color.fromRGB(139, 0, 0));
        if (Config.data.getBoolean("events.blood.useTeamColor", true)) {
            Team t = Team.getPlayerTeam(receiver, arena.getGame().teams);
            if (t != null) {
                color = Utils.colorFromString(t.teamColor, color);
            }
        }
        Particle.DustOptions dust = new Particle.DustOptions(color, Math.max(0.5f, size));
        receiver.getWorld().spawnParticle(
                Particle.REDSTONE,
                receiver.getLocation().add(0, 1.0, 0),
                Math.max(1, count),
                spread, spread * 0.6, spread,
                0.0,
                dust
        );
    }
}
