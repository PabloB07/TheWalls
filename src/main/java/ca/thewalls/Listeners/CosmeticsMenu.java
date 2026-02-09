package ca.thewalls.Listeners;

import ca.thewalls.Config;
import ca.thewalls.Cosmetics;
import ca.thewalls.Messages;
import ca.thewalls.TheWalls;
import ca.thewalls.Utils;
import com.samjakob.spigui.buttons.SGButton;
import com.samjakob.spigui.item.ItemBuilder;
import com.samjakob.spigui.menu.SGMenu;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CosmeticsMenu {
    private CosmeticsMenu() {}
    private static final java.util.Map<java.util.UUID, String> FILTERS = new java.util.concurrent.ConcurrentHashMap<>();

    private static String getFilter(Player player) {
        if (player == null) return "all";
        return FILTERS.getOrDefault(player.getUniqueId(), "all");
    }

    private static void setFilter(Player player, String value) {
        if (player == null) return;
        FILTERS.put(player.getUniqueId(), value == null ? "all" : value.toLowerCase());
    }

    public static void openMain(TheWalls plugin, Player player) {
        int rows = Utils.guiRows("cosmetics", 3);
        SGMenu menu = plugin.spigui.create("thewalls-cosmetics", rows, Utils.menuTitle("cosmetics", null));

        if (ca.thewalls.EconomyService.isAvailable()) {
            double balance = ca.thewalls.EconomyService.getBalance(player);
            String balanceText = ca.thewalls.EconomyService.format(balance);
            int balanceSlot = Utils.guiSlot("gui.slots.cosmetics.balance", 0);
            if (balanceSlot >= 0 && balanceSlot < rows * 9) {
                SGButton balanceBtn = new SGButton(
                        Utils.guiItem("gui.items.balance", Material.GOLD_NUGGET, java.util.Map.of("amount", balanceText)).build()
                ).withListener(event -> event.setCancelled(true));
                menu.setButton(balanceSlot, balanceBtn);
            }
        }

        SGButton trails = new SGButton(
                Utils.guiItem("gui.items.cosmetics_trails", Material.FIREWORK_STAR, null).build()
        ).withListener(event -> {
            event.setCancelled(true);
            openTrails(plugin, player);
        });
        SGButton kills = new SGButton(
                Utils.guiItem("gui.items.cosmetics_killeffects", Material.NETHERITE_SWORD, null).build()
        ).withListener(event -> {
            event.setCancelled(true);
            openKillEffects(plugin, player);
        });

        menu.setButton(Utils.guiSlot("gui.slots.cosmetics.trails", 2), trails);
        menu.setButton(Utils.guiSlot("gui.slots.cosmetics.killeffects", 6), kills);
        int infoSlot = Utils.guiSlot("gui.slots.cosmetics.info", 4);
        if (infoSlot >= 0 && infoSlot < rows * 9) {
            menu.setButton(infoSlot, new SGButton(
                    Utils.guiItem("gui.items.cosmetics_info", Material.NAME_TAG, null).build()
            ).withListener(event -> event.setCancelled(true)));
        }
        Utils.applyGuiFiller(menu);
        player.openInventory(menu.getInventory());
    }

    public static void openTrails(TheWalls plugin, Player player) {
        ConfigurationSection sec = Config.data.getConfigurationSection("cosmetics.trails.list");
        List<String> ids = sec == null ? java.util.Collections.emptyList() : new ArrayList<>(sec.getKeys(false));
        boolean filters = Config.data.getBoolean("cosmetics.filters.enabled", true);
        int baseSize = Math.max(1, ((ids.size() - 1) / 9) + 1);
        int size = filters ? Math.max(2, baseSize + 1) : baseSize;
        int rows = Math.max(2, Utils.guiRows("trails", size));
        SGMenu menu = plugin.spigui.create("thewalls-trails", rows, Utils.menuTitle("trails", null));
        int maxSlots = rows * 9;
        if (ca.thewalls.Config.data.getBoolean("gui.toolbar.enabled", true) && rows > 1) {
            maxSlots = (rows - 1) * 9;
        }
        java.util.Set<Integer> reserved = new java.util.HashSet<>();
        for (int i = 0; i < 9 && i < maxSlots; i++) reserved.add(i);
        if (filters) {
            renderFilterRow(menu, player, () -> openTrails(plugin, player));
        }
        int infoSlot = Utils.guiSlot("gui.slots.trails.info", 4);
        if (infoSlot >= 0 && infoSlot < maxSlots) {
            menu.setButton(infoSlot, new SGButton(
                    Utils.guiItem("gui.items.trails_info", Material.END_ROD, null).build()
            ).withListener(event -> event.setCancelled(true)));
            reserved.add(infoSlot);
        }
        Utils.applyGuiToolbar(menu, player, () -> openMain(plugin, player));

        boolean sortEnabled = Config.data.getBoolean("cosmetics.sort.enabled", true);
        List<String> filtered = new ArrayList<>();
        for (String id : ids) {
            ConfigurationSection entry = sec.getConfigurationSection(id);
            String name = entry == null ? id : entry.getString("display.name", id);
            List<String> lore = entry == null ? java.util.Collections.emptyList() : entry.getStringList("display.lore");
            if (!matchesFilter(entry, getFilter(player))) {
                continue;
            }
            filtered.add(id);
        }
        if (sortEnabled) {
            filtered.sort((a, b) -> {
                ConfigurationSection ea = sec.getConfigurationSection(a);
                ConfigurationSection eb = sec.getConfigurationSection(b);
                int ra = rarityWeight(ea);
                int rb = rarityWeight(eb);
                if (ra != rb) return Integer.compare(ra, rb);
                int ca = Cosmetics.getTrailCost(a);
                int cb = Cosmetics.getTrailCost(b);
                if (ca != cb) return Integer.compare(ca, cb);
                String na = ea == null ? a : ea.getString("display.name", a);
                String nb = eb == null ? b : eb.getString("display.name", b);
                return na.compareToIgnoreCase(nb);
            });
        }

        int slot = 0;
        for (String id : filtered) {
            while (reserved.contains(slot) && slot < maxSlots) slot++;
            if (slot >= maxSlots) break;
            ConfigurationSection entry = sec.getConfigurationSection(id);
            String name = entry == null ? id : entry.getString("display.name", id);
            List<String> lore = entry == null ? java.util.Collections.emptyList() : entry.getStringList("display.lore");
            ItemBuilder builder = new ItemBuilder(buildDisplayItem(entry, Material.FIREWORK_STAR))
                    .name(Utils.toLegacy(Utils.componentFromString(name)));
            if (!lore.isEmpty()) {
                List<String> formatted = new ArrayList<>();
                for (String line : lore) formatted.add(Utils.toLegacy(Utils.componentFromString(line)));
                builder = builder.lore(formatted);
                String hdbId = entry.getString("display.headDatabaseId", "");
                if (hdbId != null && !hdbId.isEmpty()) {
                    List<String> merged = new ArrayList<>(builder.build().getLore() == null ? java.util.Collections.emptyList() : builder.build().getLore());
                    appendHeadDbLore(merged, hdbId);
                    builder = builder.lore(merged);
                }
            }
            String rarity = entry == null ? "" : entry.getString("display.rarity", "");
            String rarityColor = entry == null ? "" : entry.getString("display.rarityColor", "");
            int cost = Cosmetics.getTrailCost(id);
            boolean unlocked = Cosmetics.isTrailUnlocked(player, id);
            boolean allowed = Cosmetics.hasTrailPermission(player, id);
            List<String> extra = new ArrayList<>();
            if (cost > 0) {
                extra.add(Utils.toLegacy(Messages.msg("menu.cosmetics_cost", java.util.Map.of("amount", String.valueOf(cost)))));
            }
            if (rarity != null && !rarity.isEmpty()) {
                extra.add(Utils.toLegacy(Messages.msg("menu.cosmetics_rarity", java.util.Map.of(
                        "rarity", Utils.toMini(Utils.componentFromString((rarityColor == null ? "" : rarityColor) + rarity))
                ))));
            }
            extra.add(Utils.toLegacy(Messages.msg(unlocked || allowed || cost == 0 ? "menu.cosmetics_owned" : "menu.cosmetics_locked")));
            if (!extra.isEmpty()) {
                List<String> merged = new ArrayList<>(builder.build().getLore() == null ? java.util.Collections.emptyList() : builder.build().getLore());
                merged.addAll(extra);
                builder = builder.lore(merged);
            }
            SGButton button = new SGButton(builder.build()).withListener(event -> {
                event.setCancelled(true);
                if (event.getClick() != null && event.getClick().isRightClick()) {
                    Cosmetics.previewTrail(player, id);
                    player.sendMessage(Messages.msg("menu.cosmetics_preview"));
                    return;
                }
                if (!allowed && !unlocked && cost > 0) {
                    if (!ca.thewalls.EconomyService.isAvailable()) {
                        player.sendMessage(Messages.msg("walls.economy_missing"));
                        return;
                    }
                    double bal = ca.thewalls.EconomyService.getBalance(player);
                    if (bal < cost) {
                        player.sendMessage(Messages.msg("walls.cosmetic_not_enough_money", java.util.Map.of("amount", String.valueOf(cost))));
                        return;
                    }
                    if (ca.thewalls.EconomyService.withdraw(player, cost)) {
                        Cosmetics.unlockTrail(player, id);
                        player.sendMessage(Messages.msg("walls.cosmetic_unlocked", java.util.Map.of("item", id)));
                    } else {
                        player.sendMessage(Messages.msg("walls.economy_missing"));
                        return;
                    }
                } else if (!allowed && !unlocked && cost <= 0) {
                    Cosmetics.unlockTrail(player, id);
                }
                if (!allowed && !Cosmetics.isTrailUnlocked(player, id) && cost > 0) {
                    return;
                }
                Config.setPlayerTrail(player.getUniqueId(), id);
                player.sendMessage(Messages.msg("walls.trail_set", java.util.Map.of("trail", id)));
            });
            menu.setButton(slot++, button);
        }
        Utils.applyGuiFiller(menu);
        player.openInventory(menu.getInventory());
    }

    public static void openKillEffects(TheWalls plugin, Player player) {
        ConfigurationSection sec = Config.data.getConfigurationSection("cosmetics.killEffects.list");
        List<String> ids = sec == null ? java.util.Collections.emptyList() : new ArrayList<>(sec.getKeys(false));
        boolean filters = Config.data.getBoolean("cosmetics.filters.enabled", true);
        int baseSize = Math.max(1, ((ids.size() - 1) / 9) + 1);
        int size = filters ? Math.max(2, baseSize + 1) : baseSize;
        int rows = Math.max(2, Utils.guiRows("killeffects", size));
        SGMenu menu = plugin.spigui.create("thewalls-killeffects", rows, Utils.menuTitle("killeffects", null));
        int maxSlots = rows * 9;
        if (ca.thewalls.Config.data.getBoolean("gui.toolbar.enabled", true) && rows > 1) {
            maxSlots = (rows - 1) * 9;
        }
        java.util.Set<Integer> reserved = new java.util.HashSet<>();
        for (int i = 0; i < 9 && i < maxSlots; i++) reserved.add(i);
        if (filters) {
            renderFilterRow(menu, player, () -> openKillEffects(plugin, player));
        }
        int infoSlot = Utils.guiSlot("gui.slots.killeffects.info", 4);
        if (infoSlot >= 0 && infoSlot < maxSlots) {
            menu.setButton(infoSlot, new SGButton(
                    Utils.guiItem("gui.items.killeffects_info", Material.NETHERITE_SWORD, null).build()
            ).withListener(event -> event.setCancelled(true)));
            reserved.add(infoSlot);
        }
        Utils.applyGuiToolbar(menu, player, () -> openMain(plugin, player));

        boolean sortEnabled = Config.data.getBoolean("cosmetics.sort.enabled", true);
        List<String> filtered = new ArrayList<>();
        for (String id : ids) {
            ConfigurationSection entry = sec.getConfigurationSection(id);
            String name = entry == null ? id : entry.getString("display.name", id);
            List<String> lore = entry == null ? java.util.Collections.emptyList() : entry.getStringList("display.lore");
            if (!matchesFilter(entry, getFilter(player))) {
                continue;
            }
            filtered.add(id);
        }
        if (sortEnabled) {
            filtered.sort((a, b) -> {
                ConfigurationSection ea = sec.getConfigurationSection(a);
                ConfigurationSection eb = sec.getConfigurationSection(b);
                int ra = rarityWeight(ea);
                int rb = rarityWeight(eb);
                if (ra != rb) return Integer.compare(ra, rb);
                int ca = Cosmetics.getKillEffectCost(a);
                int cb = Cosmetics.getKillEffectCost(b);
                if (ca != cb) return Integer.compare(ca, cb);
                String na = ea == null ? a : ea.getString("display.name", a);
                String nb = eb == null ? b : eb.getString("display.name", b);
                return na.compareToIgnoreCase(nb);
            });
        }

        int slot = 0;
        for (String id : filtered) {
            while (reserved.contains(slot) && slot < maxSlots) slot++;
            if (slot >= maxSlots) break;
            ConfigurationSection entry = sec.getConfigurationSection(id);
            String name = entry == null ? id : entry.getString("display.name", id);
            List<String> lore = entry == null ? java.util.Collections.emptyList() : entry.getStringList("display.lore");
            ItemBuilder builder = new ItemBuilder(buildDisplayItem(entry, Material.NETHERITE_SWORD))
                    .name(Utils.toLegacy(Utils.componentFromString(name)));
            if (!lore.isEmpty()) {
                List<String> formatted = new ArrayList<>();
                for (String line : lore) formatted.add(Utils.toLegacy(Utils.componentFromString(line)));
                builder = builder.lore(formatted);
                String hdbId = entry.getString("display.headDatabaseId", "");
                if (hdbId != null && !hdbId.isEmpty()) {
                    List<String> merged = new ArrayList<>(builder.build().getLore() == null ? java.util.Collections.emptyList() : builder.build().getLore());
                    appendHeadDbLore(merged, hdbId);
                    builder = builder.lore(merged);
                }
            }
            String rarity = entry == null ? "" : entry.getString("display.rarity", "");
            String rarityColor = entry == null ? "" : entry.getString("display.rarityColor", "");
            int cost = Cosmetics.getKillEffectCost(id);
            boolean unlocked = Cosmetics.isKillEffectUnlocked(player, id);
            boolean allowed = Cosmetics.hasKillEffectPermission(player, id);
            List<String> extra = new ArrayList<>();
            if (cost > 0) {
                extra.add(Utils.toLegacy(Messages.msg("menu.cosmetics_cost", java.util.Map.of("amount", String.valueOf(cost)))));
            }
            if (rarity != null && !rarity.isEmpty()) {
                extra.add(Utils.toLegacy(Messages.msg("menu.cosmetics_rarity", java.util.Map.of(
                        "rarity", Utils.toMini(Utils.componentFromString((rarityColor == null ? "" : rarityColor) + rarity))
                ))));
            }
            extra.add(Utils.toLegacy(Messages.msg(unlocked || allowed || cost == 0 ? "menu.cosmetics_owned" : "menu.cosmetics_locked")));
            if (!extra.isEmpty()) {
                List<String> merged = new ArrayList<>(builder.build().getLore() == null ? java.util.Collections.emptyList() : builder.build().getLore());
                merged.addAll(extra);
                builder = builder.lore(merged);
            }
            SGButton button = new SGButton(builder.build()).withListener(event -> {
                event.setCancelled(true);
                if (event.getClick() != null && event.getClick().isRightClick()) {
                    Cosmetics.previewKillEffect(player, id);
                    player.sendMessage(Messages.msg("menu.cosmetics_preview"));
                    return;
                }
                if (!allowed && !unlocked && cost > 0) {
                    if (!ca.thewalls.EconomyService.isAvailable()) {
                        player.sendMessage(Messages.msg("walls.economy_missing"));
                        return;
                    }
                    double bal = ca.thewalls.EconomyService.getBalance(player);
                    if (bal < cost) {
                        player.sendMessage(Messages.msg("walls.cosmetic_not_enough_money", java.util.Map.of("amount", String.valueOf(cost))));
                        return;
                    }
                    if (ca.thewalls.EconomyService.withdraw(player, cost)) {
                        Cosmetics.unlockKillEffect(player, id);
                        player.sendMessage(Messages.msg("walls.cosmetic_unlocked", java.util.Map.of("item", id)));
                    } else {
                        player.sendMessage(Messages.msg("walls.economy_missing"));
                        return;
                    }
                } else if (!allowed && !unlocked && cost <= 0) {
                    Cosmetics.unlockKillEffect(player, id);
                }
                if (!allowed && !Cosmetics.isKillEffectUnlocked(player, id) && cost > 0) {
                    return;
                }
                Config.setPlayerKillEffect(player.getUniqueId(), id);
                player.sendMessage(Messages.msg("walls.killeffect_set", java.util.Map.of("effect", id)));
            });
            menu.setButton(slot++, button);
        }
        Utils.applyGuiFiller(menu);
        player.openInventory(menu.getInventory());
    }

    private static boolean matchesFilter(ConfigurationSection entry, String filter) {
        if (filter == null || filter.equalsIgnoreCase("all")) return true;
        if (entry == null) return filter.equalsIgnoreCase("common");
        String rarity = entry.getString("display.rarity", "Common");
        return rarity != null && rarity.equalsIgnoreCase(filter);
    }

    private static int rarityWeight(ConfigurationSection entry) {
        if (entry == null) return 0;
        String rarity = entry.getString("display.rarity", "Common");
        if (rarity == null) return 0;
        switch (rarity.toLowerCase()) {
            case "uncommon": return 1;
            case "rare": return 2;
            case "epic": return 3;
            case "legendary": return 4;
            default: return 0;
        }
    }

    private static void renderFilterRow(SGMenu menu, Player player, Runnable reopen) {
        String current = getFilter(player);
        menu.setButton(0, filterButton(player, "all", current, Material.WHITE_STAINED_GLASS_PANE, reopen));
        menu.setButton(1, filterButton(player, "common", current, Material.GRAY_STAINED_GLASS_PANE, reopen));
        menu.setButton(2, filterButton(player, "uncommon", current, Material.GREEN_STAINED_GLASS_PANE, reopen));
        menu.setButton(3, filterButton(player, "rare", current, Material.LIGHT_BLUE_STAINED_GLASS_PANE, reopen));
        menu.setButton(4, filterButton(player, "epic", current, Material.PURPLE_STAINED_GLASS_PANE, reopen));
        menu.setButton(5, filterButton(player, "legendary", current, Material.ORANGE_STAINED_GLASS_PANE, reopen));
    }

    private static SGButton filterButton(Player player, String value, String current, Material mat, Runnable reopen) {
        String key = "menu.cosmetics_filter_" + value;
        String name = Messages.raw(key);
        ItemBuilder builder = new ItemBuilder(mat).name(Utils.toLegacy(Utils.componentFromString(name)));
        org.bukkit.inventory.ItemStack item = builder.build();
        if (value.equalsIgnoreCase(current)) {
            try {
                org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.addEnchant(org.bukkit.enchantments.Enchantment.LUCK_OF_THE_SEA, 1, true);
                    meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
                    item.setItemMeta(meta);
                }
            } catch (Exception ignored) {
            }
        }
        return new SGButton(item).withListener(event -> {
            event.setCancelled(true);
            setFilter(player, value);
            reopen.run();
        });
    }

    private static org.bukkit.inventory.ItemStack buildDisplayItem(ConfigurationSection entry, Material fallback) {
        if (entry == null) return new org.bukkit.inventory.ItemStack(fallback);
        String matName = entry.getString("display.material", "");
        Material mat = matName == null || matName.isEmpty() ? fallback : Material.matchMaterial(matName);
        if (mat == null) mat = fallback;
        String hdbId = entry.getString("display.headDatabaseId", "");
        if (hdbId != null && !hdbId.isEmpty() && ca.thewalls.HeadDatabaseHook.isReady()) {
            org.bukkit.inventory.ItemStack hdb = ca.thewalls.HeadDatabaseHook.getHeadCached(hdbId);
            if (hdb != null) {
                return hdb;
            }
        }
        org.bukkit.inventory.ItemStack item = new org.bukkit.inventory.ItemStack(mat);
        String skullOwner = entry.getString("display.headOwner", "");
        String skullUrl = entry.getString("display.headTextureUrl", "");
        if (mat == Material.PLAYER_HEAD && (skullOwner != null && !skullOwner.isEmpty() || skullUrl != null && !skullUrl.isEmpty())) {
            try {
                org.bukkit.inventory.meta.SkullMeta meta = (org.bukkit.inventory.meta.SkullMeta) item.getItemMeta();
                if (skullOwner != null && !skullOwner.isEmpty()) {
                    meta.setOwningPlayer(org.bukkit.Bukkit.getOfflinePlayer(skullOwner));
                } else if (skullUrl != null && !skullUrl.isEmpty()) {
                    org.bukkit.profile.PlayerProfile profile = org.bukkit.Bukkit.createPlayerProfile(java.util.UUID.randomUUID());
                    profile.getTextures().setSkin(java.net.URI.create(skullUrl).toURL());
                    meta.setOwnerProfile(profile);
                }
                item.setItemMeta(meta);
            } catch (Exception ignored) {
            }
        }
        return item;
    }

    private static void appendHeadDbLore(List<String> lore, String id) {
        if (id == null || id.isEmpty()) return;
        String line;
        if (ca.thewalls.HeadDatabaseHook.isReady()) {
            line = Messages.raw("menu.cosmetics_headdb", java.util.Map.of("id", id));
        } else {
            line = Messages.raw("menu.cosmetics_headdb_missing", java.util.Map.of("id", id));
        }
        if (line != null && !line.isEmpty()) {
            lore.add(Utils.toLegacy(Utils.componentFromString(line)));
        }
    }
}
