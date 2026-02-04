package ca.thewalls.Listeners;

import org.bukkit.Material;
import org.bukkit.entity.Chicken;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import ca.thewalls.Config;
import ca.thewalls.Messages;
import ca.thewalls.TheWalls;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.Random;

public class EntityDeath implements Listener {
    public TheWalls walls;

    public EntityDeath(TheWalls walls) {
        this.walls = walls;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDeath(EntityDeathEvent e) {
        if (e.getEntity().getKiller() == null) return;
        ca.thewalls.Arena arena = this.walls.getArenaByPlayer(e.getEntity().getKiller());
        if (arena == null) return;
        if (!arena.getGame().started) return;

        // Related to FreeFood event
        if (Config.data.getBoolean("events.gregs.enabled")) {
            Component name = e.getEntity().customName();
            if (name == null) return;
            if (!(e.getEntity() instanceof Chicken)) return;
            String plain = PlainTextComponentSerializer.plainText().serialize(name);
            if (plain.toLowerCase().contains("greg")) {
                int rnd = new Random().nextInt(10);
                if (rnd == 9 && e.getEntity().getKiller() != null) {
                    e.getEntity().getKiller().getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE, 2));
                    e.getEntity().getKiller().sendMessage(Messages.msg("events.greg_blessing"));
                } else {
                    arena.getWorld().world.createExplosion(e.getEntity().getLocation(), Config.data.getInt("events.gregs.power"), Config.data.getBoolean("events.gregs.fireExplosion"));
                }
            }
        }
    }
}
