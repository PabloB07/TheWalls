package ca.thewalls;

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class AntiAbuse {
    private static final Map<UUID, Map<UUID, Long>> LAST_KILL = new ConcurrentHashMap<>();

    private AntiAbuse() {}

    public static boolean isKillRewardAllowed(Player killer, Player victim) {
        if (killer == null || victim == null) return true;
        if (Config.data == null) return true;
        if (!Config.data.getBoolean("antiabuse.enabled", true)) return true;
        long cooldownSeconds = Config.data.getLong("antiabuse.kills.cooldownSeconds", 60);
        if (cooldownSeconds <= 0) return true;
        long now = System.currentTimeMillis();
        Map<UUID, Long> map = LAST_KILL.computeIfAbsent(killer.getUniqueId(), k -> new ConcurrentHashMap<>());
        Long last = map.get(victim.getUniqueId());
        map.put(victim.getUniqueId(), now);
        return last == null || (now - last) >= cooldownSeconds * 1000L;
    }
}
