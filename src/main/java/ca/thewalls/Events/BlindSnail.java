package ca.thewalls.Events;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import ca.thewalls.Arena;
import ca.thewalls.Config;
import ca.thewalls.Messages;
import ca.thewalls.Utils;

public class BlindSnail extends Event {
    public BlindSnail(String eventName, Arena arena) {
        super(eventName, arena);
    }

    @Override
    public void run() {
        int maxTargets = Config.data.getInt("events.maxTargets", 1);
        for (Player p : Utils.getEventTargets(this.arena, maxTargets)) {
            p.sendMessage(Messages.msg("events.blind_snail"));
            p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_LAND, 255, 1);

            p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Config.data.getInt("events.blindSnail.seconds") * 20, Config.data.getInt("events.blindSnail.blindStrength"), true));
            p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, Config.data.getInt("events.blindSnail.seconds") * 20, Config.data.getInt("events.blindSnail.slowStrength"), true));
        }
    }
}
