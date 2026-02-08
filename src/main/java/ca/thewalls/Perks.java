package ca.thewalls;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class Perks {
    private Perks() {}
    private static final java.util.Map<java.util.UUID, String> PENDING = new java.util.concurrent.ConcurrentHashMap<>();

    public static List<String> getPerkIds() {
        if (Config.data == null) return java.util.Collections.emptyList();
        ConfigurationSection sec = Config.data.getConfigurationSection("perks.list");
        if (sec == null) return java.util.Collections.emptyList();
        return new ArrayList<>(sec.getKeys(false));
    }

    public static String getName(String perkId) {
        if (perkId == null || Config.data == null) return perkId;
        return Config.data.getString("perks.list." + perkId + ".name", perkId);
    }

    public static String getDescription(String perkId) {
        if (perkId == null || Config.data == null) return "";
        return Config.data.getString("perks.list." + perkId + ".description", "");
    }

    public static int getLevel(String perkId) {
        if (perkId == null || Config.data == null) return 1;
        return Config.data.getInt("perks.list." + perkId + ".level", 1);
    }

    public static void applyPerks(Player player) {
        if (player == null) return;
        List<String> unlocked = Config.getPlayerPerks(player.getUniqueId());
        for (String perkId : unlocked) {
            ConfigurationSection sec = Config.data.getConfigurationSection("perks.list." + perkId);
            if (sec == null) continue;
            List<Map<?, ?>> effects = sec.getMapList("effects");
            for (Map<?, ?> map : effects) {
                PotionEffect effect = effectFromMap(map);
                if (effect != null) {
                    player.addPotionEffect(effect);
                }
            }
        }
    }

    public static String rollRandomPerk() {
        if (Config.data == null) return null;
        List<Map<?, ?>> rewards = Config.data.getMapList("crates.rewards");
        if (rewards == null || rewards.isEmpty()) return null;
        int total = 0;
        for (Map<?, ?> r : rewards) {
            Object chanceObj = r.get("chance");
            int chance = 0;
            if (chanceObj instanceof Number) chance = ((Number) chanceObj).intValue();
            total += Math.max(0, chance);
        }
        if (total <= 0) return null;
        int roll = new java.util.Random().nextInt(total) + 1;
        int current = 0;
        for (Map<?, ?> r : rewards) {
            int chance = 0;
            Object chanceObj = r.get("chance");
            if (chanceObj instanceof Number) chance = ((Number) chanceObj).intValue();
            current += Math.max(0, chance);
            if (roll <= current) {
                Object perkObj = r.get("perk");
                return perkObj == null ? null : String.valueOf(perkObj);
            }
        }
        return null;
    }

    public static String getPendingPerk(java.util.UUID uuid) {
        if (uuid == null) return null;
        return PENDING.get(uuid);
    }

    public static void setPendingPerk(java.util.UUID uuid, String perkId) {
        if (uuid == null) return;
        if (perkId == null || perkId.isEmpty()) {
            PENDING.remove(uuid);
        } else {
            PENDING.put(uuid, perkId);
        }
    }

    public static void clearPendingPerk(java.util.UUID uuid) {
        if (uuid == null) return;
        PENDING.remove(uuid);
    }

    public static int getPerkCost(String perkId) {
        if (perkId == null || Config.data == null) return Config.data.getInt("crates.cost", 0);
        List<Map<?, ?>> rewards = Config.data.getMapList("crates.rewards");
        if (rewards == null) return Config.data.getInt("crates.cost", 0);
        for (Map<?, ?> r : rewards) {
            Object perkObj = r.get("perk");
            if (perkObj == null) continue;
            if (perkId.equalsIgnoreCase(String.valueOf(perkObj))) {
                Object costObj = r.get("cost");
                if (costObj instanceof Number) {
                    return ((Number) costObj).intValue();
                }
            }
        }
        int direct = Config.data.getInt("perks.list." + perkId + ".cost", -1);
        if (direct >= 0) return direct;
        int level = getLevel(perkId);
        String levelKey = "crates.levelCosts." + level;
        if (Config.data.isSet(levelKey)) {
            return Config.data.getInt(levelKey, Config.data.getInt("crates.cost", 0));
        }
        return Config.data.getInt("crates.cost", 0);
    }

    public static int[] getCostRange() {
        int base = Config.data == null ? 0 : Config.data.getInt("crates.cost", 0);
        List<Map<?, ?>> rewards = Config.data == null ? null : Config.data.getMapList("crates.rewards");
        if (rewards == null || rewards.isEmpty()) {
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
            return new int[] { base, base };
        }
        return new int[] { min, max };
    }

    public static String getCurrencySymbol() {
        if (Config.data == null) return "$";
        return Config.data.getString("crates.currencySymbol", "$");
    }

    private static PotionEffect effectFromMap(Map<?, ?> map) {
        if (map == null) return null;
        String typeName = String.valueOf(map.getOrDefault("type", ""));
        PotionEffectType type = PotionEffectType.getByName(typeName);
        if (type == null) return null;
        int duration = 20 * 30;
        int amplifier = 0;
        Object durationObj = map.get("duration");
        if (durationObj instanceof Number) duration = ((Number) durationObj).intValue() * 20;
        Object ampObj = map.get("amplifier");
        if (ampObj instanceof Number) amplifier = ((Number) ampObj).intValue();
        return new PotionEffect(type, duration, amplifier, true, false, true);
    }
}
