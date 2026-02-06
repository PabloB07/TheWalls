package ca.thewalls.Events;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;

import ca.thewalls.Arena;
import ca.thewalls.Config;
import ca.thewalls.Messages;
import ca.thewalls.Utils;

class BombingRunHandler {
    public Arena arena;

    public BombingRunHandler(Arena arena) {
        this.arena = arena;
        Random rand = new Random();

        // Alert players
        for (Player p : this.arena.getPlayers()) {
            p.sendMessage(Messages.msg("events.bombing_run"));
            p.playSound(p.getLocation(), Sound.ITEM_GOAT_HORN_SOUND_3, 255f, 0.5f);
        }

        // Get the final z coordinates for the run
        int[] zPoints = {
                this.arena.getWorld().positionTwo[1],
                this.arena.getWorld().positionOne[1]
        };

        int tntSpread = Config.data.getInt("events.bombingRun.tntSpread", 2);
        if (tntSpread <= 0) {
            tntSpread = 1;
        }
        int totalDifference = Math.abs(zPoints[0] - zPoints[1]);
        int totalIterations = Math.max(1, totalDifference / tntSpread);

        Bukkit.getScheduler().scheduleSyncDelayedTask(Utils.getPlugin(), new Runnable() {
            @Override
            public void run() {
                if (!arena.getGame().started) {
                    return;
                }

                // X Pos for TNT
                int furthestX = arena.getWorld().positionOne[0];
                int closestX = arena.getWorld().positionTwo[0];

                for (Player p : arena.getPlayers()) {
                    Location loc = p.getLocation();
                    int x = loc.getBlockX();

                    if (Math.abs(x) > Math.abs(furthestX)) {
                        furthestX = x;
                    }
                    if (Math.abs(x) < Math.abs(closestX)) {
                        closestX = x;
                    }
                }

                int minX = Math.min(closestX, furthestX);
                int maxX = Math.max(closestX, furthestX);
                final int xPos = (minX == maxX) ? minX : rand.nextInt(minX, maxX + 1);

                int timer = Config.data.getInt("events.bombingRun.detonationtime", 10);
                int power = Config.data.getInt("events.bombingRun.tntPower",  16);

                int startZ = Math.min(zPoints[0], zPoints[1]);
                for (int i = 0; i < totalIterations; i++) {
                    Location loc = new Location(arena.getWorld().world, xPos, 325, startZ + ((i + 1) * tntSpread));
                    TNTPrimed tnt = (TNTPrimed) arena.getWorld().world.spawnEntity(loc, EntityType.TNT);
                    tnt.setFuseTicks(20 * (timer + 2));

                    if (!arena.getGame().started) {
                        tnt.teleport(new Location(arena.getWorld().world, 20_000_000, 600, 20_000_000));
                        tnt.remove();
                        return;
                    }

                    final int iter = i;

                    Bukkit.getScheduler().scheduleSyncDelayedTask(arena.getPlugin(), new Runnable() {
                        @Override
                        public void run() {
                            if (!arena.getGame().started) {
                                return;
                            }

                            Block highestBlock = arena.getWorld().world.getHighestBlockAt(xPos, startZ + ((iter + 1) * tntSpread));
                            Location highestLoc = highestBlock.getLocation();
                            arena.getWorld().world.createExplosion(highestLoc, power, true, true);
                            tnt.teleport(new Location(arena.getWorld().world, 20_000_000, 600, 20_000_000));
                            tnt.remove();
                        };
                    }, 20 * timer);
                }
            }
        }, 20 * Config.data.getInt("events.bombingRun.alertTime", 5));
    }
}

public class BombingRun extends Event {
    public BombingRun(String eventName, Arena arena) {
        super(eventName, arena);
    }

    @Override
    public void run() {
        new BombingRunHandler(this.arena);
    }
}
