package ca.thewalls.Events;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import ca.thewalls.Arena;
import ca.thewalls.Messages;
import ca.thewalls.Utils;

import java.util.ArrayList;
import java.util.Collections;

public class LocationSwap extends Event {
    public LocationSwap(String eventName, Arena arena) {
        super(eventName, arena);
    }

    @Override
    public void run() {
        ArrayList<Location> locations = new ArrayList<>();
        // fill locations of players
        for (Player p : this.arena.getPlayers()) {
            if (!Utils.isAlive(p)) continue;
            locations.add(p.getLocation());
        }
        
        Collections.shuffle(locations);

        int i = 0;
        for (Player p : this.arena.getPlayers()) {
            if (!Utils.isAlive(p)) continue;
            p.teleport(locations.get(i));
            p.sendMessage(Messages.msg("events.location_swap"));
            p.playSound(p.getLocation(), Sound.ENTITY_ENDER_EYE_LAUNCH, 255, 1);
            i++;
        }
    }
}
