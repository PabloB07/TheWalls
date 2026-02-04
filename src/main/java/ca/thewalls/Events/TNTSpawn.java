package ca.thewalls.Events;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;

import ca.thewalls.Arena;
import ca.thewalls.Config;
import ca.thewalls.Messages;
import ca.thewalls.Utils;

public class TNTSpawn extends Event {
    public TNTSpawn(String eventName, Arena arena) {
        super(eventName, arena);
    }

    @Override
    public void run() {
        for (Player p : this.arena.getPlayers()) {
            p.sendMessage(Messages.msg("events.tnt_warning"));
            p.playSound(p.getLocation(), Sound.ENTITY_TNT_PRIMED, 255, 1);
            if (!Utils.isAlive(p)) continue;
            for (int i = 0; i < Config.data.getInt("events.tnt.amount"); i++) {
                if (!arena.getGame().started) {
                    return;
                }

                if (Config.data.getBoolean("events.tnt.followPlayer", true)) {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(arena.getPlugin(), new Runnable() {
                        @Override
                        public void run() {
                            TNTPrimed primed = (TNTPrimed)arena.getWorld().world.spawnEntity(p.getLocation(), EntityType.TNT);
                            if (!arena.getGame().started) {
                                primed.teleport(new Location(arena.getWorld().world, 20_000_000, 600, 20_000_000));
                                primed.remove();
                                return;
                            }
                        }
                    }, (10 * i));
                } else {
                    TNTPrimed primed = (TNTPrimed)arena.getWorld().world.spawnEntity(p.getLocation(), EntityType.TNT);
                    if (!arena.getGame().started) {
                        primed.teleport(new Location(arena.getWorld().world, 20_000_000, 600, 20_000_000));
                        primed.remove();
                        return;
                    }
                }
            }
        }
    }
}
