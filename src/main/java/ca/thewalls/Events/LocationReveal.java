package ca.thewalls.Events;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import ca.thewalls.Arena;
import ca.thewalls.Config;
import ca.thewalls.Messages;
import ca.thewalls.Utils;
import ca.thewalls.Walls.Team;

import java.util.Random;

public class LocationReveal extends Event {
    public LocationReveal(String eventName, Arena arena) {
        super(eventName, arena);
    }

    @Override
    public void run() {
        for (Team t : this.arena.getGame().aliveTeams) {
            if (!t.alive) continue;
            if (t.members.size() == 0) continue;
            int r = new Random().nextInt(t.members.size());
            Player p = t.members.get(r);
            while (!Utils.isAlive(p) && t.members.size() >= 2) {
                r = new Random().nextInt(t.members.size());
                p = t.members.get(r);
            }
            p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 20 * Config.data.getInt("events.reveal.seconds"), 2));
            for (Player _p : this.arena.getPlayers()) {
                if (Config.data.getBoolean("events.reveal.displayCords")) {
                    _p.sendMessage(Messages.msg("events.location_reveal", java.util.Map.of(
                            "team", t.teamColor,
                            "team_name", t.teamName,
                            "player", p.getName(),
                            "x", String.valueOf(p.getLocation().getBlockX()),
                            "y", String.valueOf(p.getLocation().getBlockY()),
                            "z", String.valueOf(p.getLocation().getBlockZ())
                    )));
                }
                _p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 100, 1);
            }
        }
    }
}
