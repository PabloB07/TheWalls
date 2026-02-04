package ca.thewalls.Events;

import ca.thewalls.Arena;
import ca.thewalls.Utils;

public abstract class Event {
    public abstract void run();

    public Arena arena;

    public Event(String eventName, Arena arena) {
        Utils.getPlugin().getLogger().info("Registered event: " + eventName);
        arena.getGame().events.add(this);

        this.arena = arena;
    }
}
