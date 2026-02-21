package ca.thewalls.Listeners;

import ca.thewalls.Config;
import ca.thewalls.Cosmetics;
import ca.thewalls.Kits;
import ca.thewalls.Messages;
import ca.thewalls.Perks;
import ca.thewalls.TheWalls;
import ca.thewalls.Utils;
import com.samjakob.spigui.buttons.SGButton;
import com.samjakob.spigui.item.ItemBuilder;
import com.samjakob.spigui.menu.SGMenu;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class LoadoutMenu {
    private static final Map<UUID, String> FILTERS = new ConcurrentHashMap<>();

    private LoadoutMenu() {}

    public static void openHub(TheWalls plugin, Player player) {
        int rows = Math.max(3, Utils.guiRows("cosmetics", 6));
        SGMenu menu = plugin.spigui.create("thewalls-loadout-hub", rows, Utils.menuTitle("cosmetics", null));

        renderCategoryRow(menu, player, plugin, "hub");
        setInfoCard(menu, "gui.items.cosmetics_info", Material.NAME_TAG, "<gold><bold>Loadout Hub</bold></gold>",
                List.of(
                        "<gray>Administra todo tu loadout.</gray>",
                        "<dark_gray>Kits • Perks • Trails • Kill Effects</dark_gray>"
                ),
                Utils.guiSlot("gui.slots.cosmetics.info", 22));

        int trailsSlot = Utils.guiSlot("gui.slots.cosmetics.trails", 20);
        int killsSlot = Utils.guiSlot("gui.slots.cosmetics.killeffects", 24);
        menu.setButton(trailsSlot, categoryCard(Material.FIREWORK_STAR,
                "<aqua><bold>Trails</bold></aqua>",
                List.of("<gray>Efecto activo:</gray> <white>" + currentTrailName(player), "<dark_gray>Click para abrir</dark_gray>"),
                () -> openTrails(plugin, player)));
        menu.setButton(killsSlot, categoryCard(Material.NETHERITE_SWORD,
                "<red><bold>Kill Effects</bold></red>",
                List.of("<gray>Efecto activo:</gray> <white>" + currentKillEffectName(player), "<dark_gray>Click para abrir</dark_gray>"),
                () -> openKillEffects(plugin, player)));

        if (ca.thewalls.EconomyService.isAvailable()) {
            String amount = ca.thewalls.EconomyService.format(ca.thewalls.EconomyService.getBalance(player));
            int balanceSlot = Utils.guiSlot("gui.slots.cosmetics.balance", 0);
            if (isInRange(balanceSlot, menu)) {
                ItemStack balance = Utils.guiItem("gui.items.balance", Material.SUNFLOWER, Map.of("amount", amount)).build();
                menu.setButton(balanceSlot, new SGButton(balance).withListener(e -> e.setCancelled(true)));
            }
        }

        Utils.applyGuiToolbar(menu, player, null);
        Utils.applyGuiFiller(menu);
        player.openInventory(menu.getInventory());
    }

    public static void openKits(TheWalls plugin, Player player) {
        List<String> kits = Kits.getKitIds();
        int fallbackRows = Math.max(3, ((kits.size() - 1) / 9) + 2);
        int rows = Math.max(3, Utils.guiRows("kits", fallbackRows));
        SGMenu menu = plugin.spigui.create("thewalls-loadout-kits", rows, Utils.menuTitle("kits", null));

        Set<Integer> reserved = reserveTopRow(menu);
        renderCategoryRow(menu, player, plugin, "kits");
        int infoSlot = Utils.guiSlot("gui.slots.kits.info", 2);
        if (isInContentRange(infoSlot, menu)) {
            String selected = selectedKitName(player);
            setInfoCard(menu, "gui.items.kit_info", Material.WRITABLE_BOOK,
                    "<aqua><bold>Kits</bold></aqua>",
                    List.of("<gray>Kit actual:</gray> <white>" + selected, "<dark_gray>Click para equipar</dark_gray>"),
                    infoSlot);
            reserved.add(infoSlot);
        }

        Utils.applyGuiToolbar(menu, player, () -> openHub(plugin, player));

        int slot = 0;
        for (String kitId : kits) {
            while (reserved.contains(slot) && isInContentRange(slot, menu)) {
                slot++;
            }
            if (!isInContentRange(slot, menu)) {
                break;
            }

            String display = Kits.getDisplayName(kitId);
            boolean selected = kitId.equalsIgnoreCase(getSelectedKitId(player));
            ItemBuilder builder = new ItemBuilder(selected ? Material.CHEST_MINECART : Material.CHEST)
                    .name(Utils.toLegacy(Utils.componentFromString(display)));

            List<String> lore = new ArrayList<>();
            for (String line : Kits.getLore(kitId)) {
                lore.add(Utils.toLegacy(Utils.componentFromString(line)));
            }
            lore.add(" ");
            lore.add(Utils.toLegacy(Utils.componentFromString(selected ? "<green><bold>Equipado</bold></green>" : "<gray>Click para equipar</gray>")));
            builder = builder.lore(lore);

            ItemStack icon = builder.build();
            if (selected) {
                applyGlint(icon);
            }

            menu.setButton(slot, new SGButton(icon).withListener(event -> {
                event.setCancelled(true);
                Config.setPlayerKit(player.getUniqueId(), kitId);
                player.sendMessage(Messages.msg("walls.kit_selected", Map.of("kit", display)));

                ca.thewalls.Arena arena = plugin.getArenaByPlayer(player);
                if (arena != null && !arena.getGame().started) {
                    Kits.applyKitInLobby(player, kitId);
                    LobbyItems.give(player, arena);
                }
                openKits(plugin, player);
            }));
            slot++;
        }

        Utils.applyGuiFiller(menu);
        player.openInventory(menu.getInventory());
    }

    public static void openPerks(TheWalls plugin, Player player) {
        List<String> perks = Perks.getPerkIds();
        int fallbackRows = Math.max(3, ((perks.size() - 1) / 9) + 2);
        int rows = Math.max(3, Utils.guiRows("perks", fallbackRows));
        SGMenu menu = plugin.spigui.create("thewalls-loadout-perks", rows, Utils.menuTitle("perks", null));

        Set<Integer> reserved = reserveTopRow(menu);
        renderCategoryRow(menu, player, plugin, "perks");

        int infoSlot = Utils.guiSlot("gui.slots.perks.info", 2);
        if (isInContentRange(infoSlot, menu)) {
            setInfoCard(menu, "gui.items.perk_info", Material.ENCHANTED_BOOK,
                    "<light_purple><bold>Perks</bold></light_purple>",
                    List.of("<gray>Desbloquea perks permanentes.</gray>", "<dark_gray>Click para comprar/desbloquear</dark_gray>"),
                    infoSlot);
            reserved.add(infoSlot);
        }

        if (ca.thewalls.EconomyService.isAvailable()) {
            String balanceText = Perks.getCurrencySymbol() + ca.thewalls.EconomyService.format(ca.thewalls.EconomyService.getBalance(player));
            int balanceSlot = Utils.guiSlot("gui.slots.perks.balance", 0);
            if (isInContentRange(balanceSlot, menu)) {
                menu.setButton(balanceSlot, new SGButton(
                        Utils.guiItem("gui.items.balance", Material.SUNFLOWER, Map.of("amount", balanceText)).build()
                ).withListener(e -> e.setCancelled(true)));
                reserved.add(balanceSlot);
            }
        }

        if (ca.thewalls.Crates.isEnabled()) {
            int crateSlot = Utils.guiSlot("gui.slots.perks.crate", 6);
            if (isInContentRange(crateSlot, menu)) {
                ItemBuilder crate = Utils.guiItem("gui.items.perks_crate", Material.ENDER_CHEST, null);
                int[] range = ca.thewalls.Crates.getCostRange();
                if (range[0] > 0 || range[1] > 0) {
                    String amount = (range[0] == range[1])
                            ? (ca.thewalls.Crates.getCurrencySymbol() + range[0])
                            : (ca.thewalls.Crates.getCurrencySymbol() + range[0] + "-" + ca.thewalls.Crates.getCurrencySymbol() + range[1]);
                    List<String> lore = new ArrayList<>(crate.build().getLore() == null ? Collections.emptyList() : crate.build().getLore());
                    lore.add(Utils.toLegacy(Messages.msg("menu.crate_cost", Map.of("amount", amount))));
                    crate = crate.lore(lore);
                }
                menu.setButton(crateSlot, new SGButton(crate.build()).withListener(event -> {
                    event.setCancelled(true);
                    CrateConfirmMenu.open(plugin, player);
                }));
                reserved.add(crateSlot);
            }
        }

        Utils.applyGuiToolbar(menu, player, () -> openHub(plugin, player));

        int slot = 0;
        for (String perkId : perks) {
            while (reserved.contains(slot) && isInContentRange(slot, menu)) {
                slot++;
            }
            if (!isInContentRange(slot, menu)) {
                break;
            }

            String name = Perks.getName(perkId);
            String desc = Perks.getDescription(perkId);
            int level = Perks.getLevel(perkId);
            int cost = Perks.getPerkCost(perkId);
            boolean unlocked = Config.hasPerk(player.getUniqueId(), perkId);

            ItemBuilder builder = new ItemBuilder(unlocked ? Material.ENCHANTED_BOOK : Material.BOOK)
                    .name(Utils.toLegacy(Utils.componentFromString(name)));
            List<String> lore = new ArrayList<>();
            if (desc != null && !desc.isEmpty()) {
                lore.add(Utils.toLegacy(Utils.componentFromString(desc)));
            }
            lore.add(Utils.toLegacy(Utils.componentFromString("<gray>Nivel:</gray> <white>" + level)));
            if (cost > 0) {
                lore.add(Utils.toLegacy(Messages.msg("menu.crate_cost", Map.of("amount", Perks.getCurrencySymbol() + cost))));
            }
            lore.add(" ");
            lore.add(Utils.toLegacy(Messages.msg(unlocked ? "menu.perk_unlocked" : "menu.perk_locked")));
            lore.add(Utils.toLegacy(Utils.componentFromString(unlocked ? "<dark_gray>Ya disponible en partida</dark_gray>" : "<gray>Click para comprar</gray>")));
            builder = builder.lore(lore);

            ItemStack icon = builder.build();
            if (unlocked) {
                applyGlint(icon);
            }

            menu.setButton(slot, new SGButton(icon).withListener(event -> {
                event.setCancelled(true);
                if (Config.hasPerk(player.getUniqueId(), perkId)) {
                    player.sendMessage(Messages.msg("walls.perk_already"));
                    return;
                }
                int price = Perks.getPerkCost(perkId);
                if (price <= 0) {
                    Config.unlockPerk(player.getUniqueId(), perkId);
                    player.sendMessage(Messages.msg("walls.perk_unlocked", Map.of("perk", Perks.getName(perkId))));
                    openPerks(plugin, player);
                    return;
                }
                PerkConfirmMenu.open(plugin, player, perkId, price);
            }));
            slot++;
        }

        Utils.applyGuiFiller(menu);
        player.openInventory(menu.getInventory());
    }

    public static void openCosmetics(TheWalls plugin, Player player) {
        openHub(plugin, player);
    }

    public static void openTrails(TheWalls plugin, Player player) {
        ConfigurationSection sec = Config.data.getConfigurationSection("cosmetics.trails.list");
        List<String> ids = sec == null ? Collections.emptyList() : new ArrayList<>(sec.getKeys(false));
        int fallbackRows = Math.max(3, ((ids.size() - 1) / 9) + 3);
        int rows = Math.max(3, Utils.guiRows("trails", fallbackRows));
        SGMenu menu = plugin.spigui.create("thewalls-loadout-trails", rows, Utils.menuTitle("trails", null));

        Set<Integer> reserved = reserveTopRow(menu);
        renderCategoryRow(menu, player, plugin, "trails");

        boolean filters = Config.data.getBoolean("cosmetics.filters.enabled", true);
        if (filters && rows >= 4) {
            renderFilterRow(menu, player, () -> openTrails(plugin, player), 9);
            reserveRow(reserved, 9, 9, menu);
        }

        int infoSlot = Utils.guiSlot("gui.slots.trails.info", 2);
        if (isInContentRange(infoSlot, menu)) {
            setInfoCard(menu, "gui.items.trails_info", Material.END_ROD,
                    "<aqua><bold>Trails</bold></aqua>",
                    List.of("<gray>Trail actual:</gray> <white>" + currentTrailName(player), "<dark_gray>Click derecho: preview</dark_gray>"),
                    infoSlot);
            reserved.add(infoSlot);
        }

        Utils.applyGuiToolbar(menu, player, () -> openHub(plugin, player));

        List<String> filtered = filterAndSortCosmetics(ids, sec, player, true);

        int slot = 0;
        for (String id : filtered) {
            while (reserved.contains(slot) && isInContentRange(slot, menu)) {
                slot++;
            }
            if (!isInContentRange(slot, menu)) {
                break;
            }

            ConfigurationSection entry = sec == null ? null : sec.getConfigurationSection(id);
            ItemStack base = buildDisplayItem(entry, Material.FIREWORK_STAR);
            ItemBuilder builder = new ItemBuilder(base)
                    .name(Utils.toLegacy(Utils.componentFromString(resolveDisplayName(entry, id))));

            List<String> lore = buildCosmeticLore(entry, id, player, true);
            builder = builder.lore(lore);
            ItemStack icon = builder.build();
            if (isEquippedTrail(player, id)) {
                applyGlint(icon);
            }

            menu.setButton(slot, new SGButton(icon).withListener(event -> {
                event.setCancelled(true);
                if (event.getClick() != null && event.getClick().isRightClick()) {
                    Cosmetics.previewTrail(player, id);
                    player.sendMessage(Messages.msg("menu.cosmetics_preview"));
                    return;
                }
                if (!ensureCosmeticUnlocked(player, id, true)) {
                    return;
                }
                Config.setPlayerTrail(player.getUniqueId(), id);
                player.sendMessage(Messages.msg("walls.trail_set", Map.of("trail", id)));
                openTrails(plugin, player);
            }));
            slot++;
        }

        Utils.applyGuiFiller(menu);
        player.openInventory(menu.getInventory());
    }

    public static void openKillEffects(TheWalls plugin, Player player) {
        ConfigurationSection sec = Config.data.getConfigurationSection("cosmetics.killEffects.list");
        List<String> ids = sec == null ? Collections.emptyList() : new ArrayList<>(sec.getKeys(false));
        int fallbackRows = Math.max(3, ((ids.size() - 1) / 9) + 3);
        int rows = Math.max(3, Utils.guiRows("killeffects", fallbackRows));
        SGMenu menu = plugin.spigui.create("thewalls-loadout-killeffects", rows, Utils.menuTitle("killeffects", null));

        Set<Integer> reserved = reserveTopRow(menu);
        renderCategoryRow(menu, player, plugin, "killeffects");

        boolean filters = Config.data.getBoolean("cosmetics.filters.enabled", true);
        if (filters && rows >= 4) {
            renderFilterRow(menu, player, () -> openKillEffects(plugin, player), 9);
            reserveRow(reserved, 9, 9, menu);
        }

        int infoSlot = Utils.guiSlot("gui.slots.killeffects.info", 2);
        if (isInContentRange(infoSlot, menu)) {
            setInfoCard(menu, "gui.items.killeffects_info", Material.NETHERITE_SWORD,
                    "<red><bold>Kill Effects</bold></red>",
                    List.of("<gray>Efecto actual:</gray> <white>" + currentKillEffectName(player), "<dark_gray>Click derecho: preview</dark_gray>"),
                    infoSlot);
            reserved.add(infoSlot);
        }

        Utils.applyGuiToolbar(menu, player, () -> openHub(plugin, player));

        List<String> filtered = filterAndSortCosmetics(ids, sec, player, false);

        int slot = 0;
        for (String id : filtered) {
            while (reserved.contains(slot) && isInContentRange(slot, menu)) {
                slot++;
            }
            if (!isInContentRange(slot, menu)) {
                break;
            }

            ConfigurationSection entry = sec == null ? null : sec.getConfigurationSection(id);
            ItemStack base = buildDisplayItem(entry, Material.NETHERITE_SWORD);
            ItemBuilder builder = new ItemBuilder(base)
                    .name(Utils.toLegacy(Utils.componentFromString(resolveDisplayName(entry, id))));

            List<String> lore = buildCosmeticLore(entry, id, player, false);
            builder = builder.lore(lore);
            ItemStack icon = builder.build();
            if (isEquippedKillEffect(player, id)) {
                applyGlint(icon);
            }

            menu.setButton(slot, new SGButton(icon).withListener(event -> {
                event.setCancelled(true);
                if (event.getClick() != null && event.getClick().isRightClick()) {
                    Cosmetics.previewKillEffect(player, id);
                    player.sendMessage(Messages.msg("menu.cosmetics_preview"));
                    return;
                }
                if (!ensureCosmeticUnlocked(player, id, false)) {
                    return;
                }
                Config.setPlayerKillEffect(player.getUniqueId(), id);
                player.sendMessage(Messages.msg("walls.killeffect_set", Map.of("effect", id)));
                openKillEffects(plugin, player);
            }));
            slot++;
        }

        Utils.applyGuiFiller(menu);
        player.openInventory(menu.getInventory());
    }

    private static void renderCategoryRow(SGMenu menu, Player player, TheWalls plugin, String selected) {
        reserveRow(new HashSet<>(), 0, 9, menu);
        setCategoryButton(menu, 0, Material.CHEST, "<aqua>Kits</aqua>", "kits", selected, "thewalls.walls.kits", () -> openKits(plugin, player));
        setCategoryButton(menu, 1, Material.ENCHANTED_BOOK, "<light_purple>Perks</light_purple>", "perks", selected, "thewalls.walls.perks", () -> openPerks(plugin, player));
        setCategoryButton(menu, 2, Material.NAME_TAG, "<gold>Hub</gold>", "hub", selected, "thewalls.walls.cosmetics", () -> openHub(plugin, player));
        setCategoryButton(menu, 3, Material.FIREWORK_STAR, "<aqua>Trails</aqua>", "trails", selected, "thewalls.walls.cosmetics", () -> openTrails(plugin, player));
        setCategoryButton(menu, 4, Material.NETHERITE_SWORD, "<red>KillFX</red>", "killeffects", selected, "thewalls.walls.cosmetics", () -> openKillEffects(plugin, player));

        for (int i = 0; i < 9; i++) {
            menu.stickSlot(i);
        }
    }

    private static void setCategoryButton(SGMenu menu, int slot, Material mat, String label, String key, String selected, String permission, Runnable action) {
        ItemBuilder builder = new ItemBuilder(mat)
                .name(Utils.toLegacy(Utils.componentFromString(label)));
        List<String> lore = new ArrayList<>();
        boolean active = key.equalsIgnoreCase(selected);
        lore.add(Utils.toLegacy(Utils.componentFromString(active ? "<green><bold>Seleccionado</bold></green>" : "<gray>Click para abrir</gray>")));
        ItemStack icon = builder.lore(lore).build();
        if (active) {
            applyGlint(icon);
        }
        menu.setButton(slot, new SGButton(icon).withListener(event -> {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            if (permission != null && !permission.isEmpty() && !player.hasPermission(permission) && !player.isOp()) {
                player.sendMessage(Messages.msg("admin.no_permission"));
                return;
            }
            action.run();
        }));
    }

    private static SGButton categoryCard(Material mat, String name, List<String> loreMini, Runnable action) {
        ItemBuilder builder = new ItemBuilder(mat)
                .name(Utils.toLegacy(Utils.componentFromString(name)));
        List<String> lore = new ArrayList<>();
        for (String line : loreMini) {
            lore.add(Utils.toLegacy(Utils.componentFromString(line)));
        }
        return new SGButton(builder.lore(lore).build()).withListener(event -> {
            event.setCancelled(true);
            action.run();
        });
    }

    private static void setInfoCard(SGMenu menu, String basePath, Material fallback, String fallbackTitle, List<String> fallbackLore, int slot) {
        if (!isInRange(slot, menu)) {
            return;
        }
        ItemBuilder conf = Utils.guiItem(basePath, fallback, null);
        ItemStack built = conf.build();
        boolean empty = built.getType() == fallback && (built.getItemMeta() == null || !built.getItemMeta().hasDisplayName());
        if (empty) {
            List<String> lore = new ArrayList<>();
            for (String line : fallbackLore) {
                lore.add(Utils.toLegacy(Utils.componentFromString(line)));
            }
            built = new ItemBuilder(fallback)
                    .name(Utils.toLegacy(Utils.componentFromString(fallbackTitle)))
                    .lore(lore)
                    .build();
        }
        menu.setButton(slot, new SGButton(built).withListener(e -> e.setCancelled(true)));
    }

    private static Set<Integer> reserveTopRow(SGMenu menu) {
        Set<Integer> reserved = new HashSet<>();
        reserveRow(reserved, 0, 9, menu);
        return reserved;
    }

    private static void reserveRow(Set<Integer> reserved, int startSlot, int width, SGMenu menu) {
        int max = menu.getInventory().getSize();
        for (int i = 0; i < width; i++) {
            int slot = startSlot + i;
            if (slot >= 0 && slot < max) {
                reserved.add(slot);
            }
        }
    }

    private static boolean isInRange(int slot, SGMenu menu) {
        return slot >= 0 && slot < menu.getInventory().getSize();
    }

    private static boolean isInContentRange(int slot, SGMenu menu) {
        int rows = menu.getInventory().getSize() / 9;
        int max = rows * 9;
        if (Config.data.getBoolean("gui.toolbar.enabled", true) && rows > 1) {
            max = (rows - 1) * 9;
        }
        return slot >= 0 && slot < max;
    }

    private static String getFilter(Player player) {
        if (player == null) {
            return "all";
        }
        return FILTERS.getOrDefault(player.getUniqueId(), "all");
    }

    private static void setFilter(Player player, String value) {
        if (player == null) {
            return;
        }
        FILTERS.put(player.getUniqueId(), value == null ? "all" : value.toLowerCase());
    }

    private static void renderFilterRow(SGMenu menu, Player player, Runnable reopen, int rowStart) {
        int[] slots = {rowStart, rowStart + 1, rowStart + 2, rowStart + 3, rowStart + 4, rowStart + 5};
        String[] values = {"all", "common", "uncommon", "rare", "epic", "legendary"};
        Material[] mats = {
                Material.WHITE_STAINED_GLASS_PANE,
                Material.GRAY_STAINED_GLASS_PANE,
                Material.GREEN_STAINED_GLASS_PANE,
                Material.LIGHT_BLUE_STAINED_GLASS_PANE,
                Material.PURPLE_STAINED_GLASS_PANE,
                Material.ORANGE_STAINED_GLASS_PANE
        };

        String current = getFilter(player);
        for (int i = 0; i < values.length; i++) {
            int slot = slots[i];
            if (!isInRange(slot, menu)) {
                continue;
            }
            String value = values[i];
            String key = "menu.cosmetics_filter_" + value;
            String name = Messages.raw(key);
            ItemStack icon = new ItemBuilder(mats[i])
                    .name(Utils.toLegacy(Utils.componentFromString(name)))
                    .build();
            if (value.equalsIgnoreCase(current)) {
                applyGlint(icon);
            }
            menu.setButton(slot, new SGButton(icon).withListener(event -> {
                event.setCancelled(true);
                setFilter(player, value);
                reopen.run();
            }));
            menu.stickSlot(slot);
        }
    }

    private static List<String> filterAndSortCosmetics(List<String> ids, ConfigurationSection root, Player player, boolean trails) {
        List<String> filtered = new ArrayList<>();
        String filter = getFilter(player);
        for (String id : ids) {
            ConfigurationSection entry = root == null ? null : root.getConfigurationSection(id);
            if (matchesFilter(entry, filter)) {
                filtered.add(id);
            }
        }

        if (!Config.data.getBoolean("cosmetics.sort.enabled", true)) {
            return filtered;
        }

        filtered.sort(Comparator
                .comparingInt((String id) -> rarityWeight(root == null ? null : root.getConfigurationSection(id)))
                .thenComparingInt(id -> trails ? Cosmetics.getTrailCost(id) : Cosmetics.getKillEffectCost(id))
                .thenComparing(id -> resolveDisplayName(root == null ? null : root.getConfigurationSection(id), id), String.CASE_INSENSITIVE_ORDER));
        return filtered;
    }

    private static List<String> buildCosmeticLore(ConfigurationSection entry, String id, Player player, boolean trail) {
        List<String> lore = new ArrayList<>();

        List<String> baseLore = entry == null ? Collections.emptyList() : entry.getStringList("display.lore");
        for (String line : baseLore) {
            lore.add(Utils.toLegacy(Utils.componentFromString(line)));
        }

        String hdbId = entry == null ? "" : entry.getString("display.headDatabaseId", "");
        if (hdbId != null && !hdbId.isEmpty()) {
            appendHeadDbLore(lore, hdbId);
        }

        int cost = trail ? Cosmetics.getTrailCost(id) : Cosmetics.getKillEffectCost(id);
        boolean unlocked = trail ? Cosmetics.isTrailUnlocked(player, id) : Cosmetics.isKillEffectUnlocked(player, id);
        boolean allowed = trail ? Cosmetics.hasTrailPermission(player, id) : Cosmetics.hasKillEffectPermission(player, id);
        boolean equipped = trail ? isEquippedTrail(player, id) : isEquippedKillEffect(player, id);
        String rarity = entry == null ? "" : entry.getString("display.rarity", "");
        String rarityColor = entry == null ? "" : entry.getString("display.rarityColor", "");

        lore.add(" ");
        if (cost > 0) {
            lore.add(Utils.toLegacy(Messages.msg("menu.cosmetics_cost", Map.of("amount", String.valueOf(cost)))));
        }
        if (rarity != null && !rarity.isEmpty()) {
            lore.add(Utils.toLegacy(Messages.msg("menu.cosmetics_rarity", Map.of(
                    "rarity", Utils.toMini(Utils.componentFromString((rarityColor == null ? "" : rarityColor) + rarity))
            ))));
        }

        if (equipped) {
            lore.add(Utils.toLegacy(Utils.componentFromString("<green><bold>Equipado</bold></green>")));
        } else if (unlocked || allowed || cost == 0) {
            lore.add(Utils.toLegacy(Messages.msg("menu.cosmetics_owned")));
            lore.add(Utils.toLegacy(Utils.componentFromString("<gray>Click para equipar</gray>")));
        } else {
            lore.add(Utils.toLegacy(Messages.msg("menu.cosmetics_locked")));
            lore.add(Utils.toLegacy(Utils.componentFromString("<gray>Click para comprar</gray>")));
        }
        lore.add(Utils.toLegacy(Utils.componentFromString("<dark_gray>Click derecho para preview</dark_gray>")));

        return lore;
    }

    private static boolean ensureCosmeticUnlocked(Player player, String id, boolean trail) {
        boolean unlocked = trail ? Cosmetics.isTrailUnlocked(player, id) : Cosmetics.isKillEffectUnlocked(player, id);
        boolean allowed = trail ? Cosmetics.hasTrailPermission(player, id) : Cosmetics.hasKillEffectPermission(player, id);
        int cost = trail ? Cosmetics.getTrailCost(id) : Cosmetics.getKillEffectCost(id);

        if (allowed || unlocked) {
            return true;
        }
        if (cost <= 0) {
            if (trail) {
                Cosmetics.unlockTrail(player, id);
            } else {
                Cosmetics.unlockKillEffect(player, id);
            }
            return true;
        }
        if (!ca.thewalls.EconomyService.isAvailable()) {
            player.sendMessage(Messages.msg("walls.economy_missing"));
            return false;
        }
        double balance = ca.thewalls.EconomyService.getBalance(player);
        if (balance < cost) {
            player.sendMessage(Messages.msg("walls.cosmetic_not_enough_money", Map.of("amount", String.valueOf(cost))));
            return false;
        }
        if (!ca.thewalls.EconomyService.withdraw(player, cost)) {
            player.sendMessage(Messages.msg("walls.economy_missing"));
            return false;
        }
        if (trail) {
            Cosmetics.unlockTrail(player, id);
        } else {
            Cosmetics.unlockKillEffect(player, id);
        }
        player.sendMessage(Messages.msg("walls.cosmetic_unlocked", Map.of("item", id)));
        return true;
    }

    private static String getSelectedKitId(Player player) {
        String kit = Config.getPlayerKit(player.getUniqueId());
        if (kit != null && !kit.isEmpty()) {
            return kit;
        }
        String def = Kits.getDefaultKit();
        return def == null ? "" : def;
    }

    private static String selectedKitName(Player player) {
        String id = getSelectedKitId(player);
        return id.isEmpty() ? "none" : stripMini(Kits.getDisplayName(id));
    }

    private static String currentTrailName(Player player) {
        String id = Cosmetics.getTrailId(player);
        ConfigurationSection sec = Config.data.getConfigurationSection("cosmetics.trails.list." + id);
        return stripMini(resolveDisplayName(sec, id));
    }

    private static String currentKillEffectName(Player player) {
        String id = Cosmetics.getKillEffectId(player);
        ConfigurationSection sec = Config.data.getConfigurationSection("cosmetics.killEffects.list." + id);
        return stripMini(resolveDisplayName(sec, id));
    }

    private static boolean isEquippedTrail(Player player, String id) {
        String selected = Cosmetics.getTrailId(player);
        return selected != null && selected.equalsIgnoreCase(id);
    }

    private static boolean isEquippedKillEffect(Player player, String id) {
        String selected = Cosmetics.getKillEffectId(player);
        return selected != null && selected.equalsIgnoreCase(id);
    }

    private static boolean matchesFilter(ConfigurationSection entry, String filter) {
        if (filter == null || filter.equalsIgnoreCase("all")) {
            return true;
        }
        if (entry == null) {
            return filter.equalsIgnoreCase("common");
        }
        String rarity = entry.getString("display.rarity", "Common");
        return rarity != null && rarity.equalsIgnoreCase(filter);
    }

    private static int rarityWeight(ConfigurationSection entry) {
        if (entry == null) {
            return 0;
        }
        String rarity = entry.getString("display.rarity", "Common");
        if (rarity == null) {
            return 0;
        }
        return switch (rarity.toLowerCase()) {
            case "uncommon" -> 1;
            case "rare" -> 2;
            case "epic" -> 3;
            case "legendary" -> 4;
            default -> 0;
        };
    }

    private static String resolveDisplayName(ConfigurationSection entry, String id) {
        return entry == null ? id : entry.getString("display.name", id);
    }

    private static void appendHeadDbLore(List<String> lore, String id) {
        String line;
        if (ca.thewalls.HeadDatabaseHook.isReady()) {
            line = Messages.raw("menu.cosmetics_headdb", Map.of("id", id));
        } else {
            line = Messages.raw("menu.cosmetics_headdb_missing", Map.of("id", id));
        }
        lore.add(Utils.toLegacy(Utils.componentFromString(line)));
    }

    private static ItemStack buildDisplayItem(ConfigurationSection entry, Material fallback) {
        if (entry == null) {
            return new ItemStack(fallback);
        }

        String matName = entry.getString("display.material", "");
        Material mat = matName == null || matName.isEmpty() ? fallback : Material.matchMaterial(matName);
        if (mat == null) {
            mat = fallback;
        }

        String hdbId = entry.getString("display.headDatabaseId", "");
        if (hdbId != null && !hdbId.isEmpty() && ca.thewalls.HeadDatabaseHook.isReady()) {
            ItemStack hdb = ca.thewalls.HeadDatabaseHook.getHeadCached(hdbId);
            if (hdb != null) {
                return hdb;
            }
        }

        ItemStack item = new ItemStack(mat);
        String skullOwner = entry.getString("display.headOwner", "");
        String skullUrl = entry.getString("display.headTextureUrl", "");

        if (mat == Material.PLAYER_HEAD && ((skullOwner != null && !skullOwner.isEmpty()) || (skullUrl != null && !skullUrl.isEmpty()))) {
            try {
                SkullMeta meta = (SkullMeta) item.getItemMeta();
                if (meta != null) {
                    if (skullOwner != null && !skullOwner.isEmpty()) {
                        meta.setOwningPlayer(org.bukkit.Bukkit.getOfflinePlayer(skullOwner));
                    } else if (skullUrl != null && !skullUrl.isEmpty()) {
                        org.bukkit.profile.PlayerProfile profile = org.bukkit.Bukkit.createPlayerProfile(UUID.randomUUID());
                        profile.getTextures().setSkin(URI.create(skullUrl).toURL());
                        meta.setOwnerProfile(profile);
                    }
                    item.setItemMeta(meta);
                }
            } catch (Exception ignored) {
            }
        }
        return item;
    }

    private static void applyGlint(ItemStack item) {
        if (item == null) {
            return;
        }
        try {
            ItemMeta meta = item.getItemMeta();
            if (meta == null) {
                return;
            }
            meta.addEnchant(org.bukkit.enchantments.Enchantment.LUCK_OF_THE_SEA, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        } catch (Exception ignored) {
        }
    }

    private static String stripMini(String value) {
        if (value == null || value.isEmpty()) {
            return "none";
        }
        String legacy = Utils.toLegacy(Utils.componentFromString(value));
        return legacy.replaceAll("§.", "").trim();
    }
}
