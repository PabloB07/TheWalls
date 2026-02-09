package ca.thewalls;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public final class Crates {
    private Crates() {}

    public static boolean isEnabled() {
        return Config.crates != null && Config.crates.getBoolean("crates.enabled", true);
    }

    public static String getCurrencySymbol() {
        if (Config.crates == null) return "$";
        return Config.crates.getString("crates.currencySymbol", "$");
    }

    public static int[] getCostRange() {
        if (Config.crates == null) return new int[] {0, 0};
        List<Map<?, ?>> rewards = Config.crates.getMapList("crates.rewards");
        if (rewards == null || rewards.isEmpty()) {
            int base = Config.crates.getInt("crates.cost", 0);
            return new int[] { base, base };
        }
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        boolean found = false;
        for (Map<?, ?> r : rewards) {
            Object costObj = r.get("cost");
            if (!(costObj instanceof Number)) continue;
            int cost = ((Number) costObj).intValue();
            min = Math.min(min, cost);
            max = Math.max(max, cost);
            found = true;
        }
        if (!found) {
            int base = Config.crates.getInt("crates.cost", 0);
            return new int[] { base, base };
        }
        return new int[] { min, max };
    }

    public static Reward rollReward(Player player) {
        if (Config.crates == null) return null;
        List<Reward> pool = buildRewards(player, true);
        if (pool.isEmpty()) {
            pool = buildRewards(player, false);
        }
        if (pool.isEmpty()) return null;
        int total = 0;
        for (Reward r : pool) {
            total += Math.max(0, r.chance);
        }
        if (total <= 0) return null;
        int roll = new Random().nextInt(total) + 1;
        int current = 0;
        for (Reward r : pool) {
            current += Math.max(0, r.chance);
            if (roll <= current) return r;
        }
        return null;
    }

    private static List<Reward> buildRewards(Player player, boolean skipOwned) {
        List<Reward> rewards = new ArrayList<>();
        List<Map<?, ?>> list = Config.crates.getMapList("crates.rewards");
        if (list == null) return rewards;
        for (Map<?, ?> map : list) {
            Reward r = fromMap(map);
            if (r == null) continue;
            if (skipOwned && player != null && isOwned(player, r)) {
                continue;
            }
            rewards.add(r);
        }
        return rewards;
    }

    private static Reward fromMap(Map<?, ?> map) {
        if (map == null) return null;
        String type = map.containsKey("type") ? String.valueOf(map.get("type")).toLowerCase() : "";
        String id = map.containsKey("id") ? String.valueOf(map.get("id")) : "";
        if (type.isEmpty()) {
            if (map.containsKey("perk")) {
                type = "perk";
                id = String.valueOf(map.get("perk"));
            } else if (map.containsKey("trail")) {
                type = "trail";
                id = String.valueOf(map.get("trail"));
            } else if (map.containsKey("killeffect")) {
                type = "killeffect";
                id = String.valueOf(map.get("killeffect"));
            }
        }
        if (type.isEmpty() || id == null || id.isEmpty()) return null;
        int chance = map.get("chance") instanceof Number ? ((Number) map.get("chance")).intValue() : 0;
        int cost = map.get("cost") instanceof Number ? ((Number) map.get("cost")).intValue() : -1;
        String name = map.containsKey("name") ? String.valueOf(map.get("name")) : "";
        String desc = map.containsKey("description") ? String.valueOf(map.get("description")) : "";

        if (type.equals("perk")) {
            if (Config.perks == null || !Config.perks.isConfigurationSection("perks.list." + id)) return null;
            if (name.isEmpty()) name = Perks.getName(id);
            if (desc.isEmpty()) desc = Perks.getDescription(id);
            if (cost < 0) cost = Perks.getPerkCost(id);
        } else if (type.equals("trail")) {
            ConfigurationSection sec = Config.data.getConfigurationSection("cosmetics.trails.list." + id);
            if (sec == null) return null;
            if (name.isEmpty()) name = sec.getString("display.name", id);
            if (desc.isEmpty()) desc = String.join("\n", sec.getStringList("display.lore"));
            if (cost < 0) cost = Math.max(0, sec.getInt("cost", 0));
        } else if (type.equals("killeffect")) {
            ConfigurationSection sec = Config.data.getConfigurationSection("cosmetics.killEffects.list." + id);
            if (sec == null) return null;
            if (name.isEmpty()) name = sec.getString("display.name", id);
            if (desc.isEmpty()) desc = String.join("\n", sec.getStringList("display.lore"));
            if (cost < 0) cost = Math.max(0, sec.getInt("cost", 0));
        } else {
            return null;
        }
        return new Reward(type, id, name, desc, Math.max(0, cost), chance);
    }

    public static boolean isOwned(Player player, Reward r) {
        if (player == null || r == null) return false;
        switch (r.type) {
            case "perk":
                return Config.hasPerk(player.getUniqueId(), r.id);
            case "trail":
                return Cosmetics.isTrailUnlocked(player, r.id);
            case "killeffect":
                return Cosmetics.isKillEffectUnlocked(player, r.id);
            default:
                return false;
        }
    }

    public static void grant(Player player, Reward r) {
        if (player == null || r == null) return;
        switch (r.type) {
            case "perk":
                Config.unlockPerk(player.getUniqueId(), r.id);
                break;
            case "trail":
                Cosmetics.unlockTrail(player, r.id);
                break;
            case "killeffect":
                Cosmetics.unlockKillEffect(player, r.id);
                break;
        }
    }

    public static class Reward {
        public final String type;
        public final String id;
        public final String name;
        public final String description;
        public final int cost;
        public final int chance;

        public Reward(String type, String id, String name, String description, int cost, int chance) {
            this.type = type;
            this.id = id;
            this.name = name;
            this.description = description;
            this.cost = cost;
            this.chance = chance;
        }
    }
}
