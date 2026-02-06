package ca.thewalls.Events;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import ca.thewalls.Arena;
import ca.thewalls.Config;
import ca.thewalls.Messages;
import ca.thewalls.Utils;

class HailStormHandler {
    Player p;
    int taskID = 0;
    int timer = Config.data.getInt("events.hailStorm.delay");
    Arena arena;

    public HailStormHandler(Player p, Arena arena) {
        this.p = p;
        this.arena = arena;

        taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(Utils.getPlugin(), () -> {
            if (timer <= 0) {
                p.sendMessage(Messages.msg("events.hailstorm_now"));

                int amountOfArrows = Config.data.getInt("events.hailStorm.volleySize");
                for (int x = -amountOfArrows; x < amountOfArrows; x++) {
                    for (int z = -amountOfArrows; z < amountOfArrows; z++) {
                        Arrow tempArrow = (Arrow) this.arena.getWorld().world.spawnEntity(p.getLocation().add(x, Config.data.getInt("events.hailStorm.height"), z), EntityType.ARROW);
                        tempArrow.setCritical(true);
                        tempArrow.setDamage(Config.data.getInt("events.hailStorm.arrowDamage"));
                    }
                }

                Bukkit.getScheduler().cancelTask(taskID);
            }
            timer--;
        }, 0L, 20L);
    }
}

public class HailStorm extends Event {
    public HailStorm(String eventName, Arena arena) {
        super(eventName, arena);
    }

    @Override
    public void run() {
        int maxTargets = Config.data.getInt("events.maxTargets", 1);
        for (Player p : Utils.getEventTargets(this.arena, maxTargets)) {
            p.playSound(p.getLocation(), Sound.ENTITY_ARROW_SHOOT, 255, 1);
            p.sendMessage(Messages.msg("events.hailstorm_in", java.util.Map.of("seconds", String.valueOf(Config.data.getInt("events.hailStorm.delay")))));
            new HailStormHandler(p, this.arena);
        }
    }
}
