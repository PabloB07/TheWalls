package ca.thewalls;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class Kits {
    private Kits() {}

    public static List<String> getKitIds() {
        if (Config.data == null) return java.util.Collections.emptyList();
        ConfigurationSection sec = Config.data.getConfigurationSection("kits.list");
        if (sec == null) return java.util.Collections.emptyList();
        return new ArrayList<>(sec.getKeys(false));
    }

    public static String getDefaultKit() {
        if (Config.data == null) return null;
        return Config.data.getString("kits.default", null);
    }

    public static String getDisplayName(String kitId) {
        if (kitId == null || Config.data == null) return kitId;
        return Config.data.getString("kits.list." + kitId + ".display.name", kitId);
    }

    public static List<String> getLore(String kitId) {
        if (kitId == null || Config.data == null) return java.util.Collections.emptyList();
        return Config.data.getStringList("kits.list." + kitId + ".display.lore");
    }

    public static void applyKit(Player player, String kitId) {
        if (player == null || kitId == null) return;
        ConfigurationSection kit = Config.data.getConfigurationSection("kits.list." + kitId);
        if (kit == null) return;

        // Items
        List<Map<?, ?>> items = kit.getMapList("items");
        for (Map<?, ?> map : items) {
            ItemStack stack = itemFromMap(map);
            if (stack != null) {
                player.getInventory().addItem(stack);
            }
        }

        // Armor
        ConfigurationSection armor = kit.getConfigurationSection("armor");
        if (armor != null) {
            player.getInventory().setHelmet(itemFromMaterial(armor.getString("helmet")));
            player.getInventory().setChestplate(itemFromMaterial(armor.getString("chestplate")));
            player.getInventory().setLeggings(itemFromMaterial(armor.getString("leggings")));
            player.getInventory().setBoots(itemFromMaterial(armor.getString("boots")));
        }

        // Effects
        List<Map<?, ?>> effects = kit.getMapList("effects");
        for (Map<?, ?> map : effects) {
            PotionEffect effect = effectFromMap(map);
            if (effect != null) {
                player.addPotionEffect(effect);
            }
        }
    }

    private static ItemStack itemFromMaterial(String matName) {
        if (matName == null || matName.isEmpty()) return null;
        Material mat = Material.matchMaterial(matName);
        if (mat == null) return null;
        return new ItemStack(mat, 1);
    }

    private static ItemStack itemFromMap(Map<?, ?> map) {
        if (map == null) return null;
        String matName = String.valueOf(map.getOrDefault("material", ""));
        Material mat = Material.matchMaterial(matName);
        if (mat == null) return null;
        int amount = 1;
        Object amtObj = map.get("amount");
        if (amtObj instanceof Number) amount = ((Number) amtObj).intValue();
        ItemStack stack = new ItemStack(mat, Math.max(1, amount));
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            Object name = map.get("name");
            if (name != null) {
                meta.displayName(Utils.componentFromString(String.valueOf(name)));
            }
            Object loreObj = map.get("lore");
            if (loreObj instanceof List) {
                List<?> raw = (List<?>) loreObj;
                List<net.kyori.adventure.text.Component> lore = new ArrayList<>();
                for (Object line : raw) {
                    lore.add(Utils.componentFromString(String.valueOf(line)));
                }
                meta.lore(lore);
            }
            stack.setItemMeta(meta);
        }
        Object enchantsObj = map.get("enchants");
        if (enchantsObj instanceof Map) {
            Map<?, ?> enchants = (Map<?, ?>) enchantsObj;
            for (Map.Entry<?, ?> e : enchants.entrySet()) {
                try {
                    org.bukkit.enchantments.Enchantment ench = org.bukkit.enchantments.Enchantment.getByName(String.valueOf(e.getKey()));
                    if (ench == null) continue;
                    int level = Integer.parseInt(String.valueOf(e.getValue()));
                    stack.addUnsafeEnchantment(ench, level);
                } catch (Exception ignored) {
                }
            }
        }
        return stack;
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
