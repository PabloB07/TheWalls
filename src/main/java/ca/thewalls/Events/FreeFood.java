package ca.thewalls.Events;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import ca.thewalls.Arena;
import ca.thewalls.Config;
import ca.thewalls.Messages;
import ca.thewalls.Utils;

class HandleChickens {
    Chicken chicken;
    int runnable;
    int countdown = Config.data.getInt("events.gregs.timer");
    Arena arena;

    public HandleChickens(Chicken _chicken, Player player, Arena arena) {
        this.arena = arena;
        chicken = _chicken;
        chicken.setAdult();
        chicken.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Config.data.getInt("events.gregs.timer") * 20, Config.data.getInt("events.gregs.speed"), true));
        chicken.customName(Utils.format("&2&lGreg"));
        runnable = Bukkit.getScheduler().scheduleSyncRepeatingTask(Utils.getPlugin(), () -> {
            if (!this.arena.getGame().started) {
                chicken.setHealth(0);
                Bukkit.getScheduler().cancelTask(runnable);
                return;
            }

            if (countdown <= 0) {
                chicken.setHealth(0);
                chicken.damage(20);
                Bukkit.getScheduler().cancelTask(runnable);
            } else {
                chicken.customName(Utils.format("&2&lGreg &r-&c " + countdown + "s"));
            }
            countdown--;
        }, 0L, 20L);
    }
}

public class FreeFood extends Event {
    public FreeFood(String eventName, Arena arena) {
        super(eventName, arena);
    }

    @Override
    public void run() {
        for (Player p : this.arena.getPlayers()) {
            p.sendMessage(Messages.msg("events.gregs_start"));
            p.playSound(p.getLocation(), Sound.ENTITY_CHICKEN_AMBIENT, 255, 1);

            if (!Utils.isAlive(p)) continue;

            p.getInventory().addItem(p.getInventory().getItemInOffHand());
            p.getInventory().setItemInOffHand(new ItemStack(Material.WHEAT_SEEDS, 32));

            for (int i = 0; i < Config.data.getInt("events.gregs.amount"); i++) {
                Chicken chicken = (Chicken) this.arena.getWorld().world.spawnEntity(p.getLocation(), EntityType.CHICKEN);
                new HandleChickens(chicken, p, this.arena);
            }
        }
    }
}
