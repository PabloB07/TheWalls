package ca.thewalls;

import java.util.ArrayList;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import ca.thewalls.Walls.Team;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;

import java.time.Duration;

public class Utils {

    public Arena arena;
    public Utils(Arena arena) {
        this.arena = arena;
    }

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();
    private static final MiniMessage MINI = MiniMessage.miniMessage();

    public static Component format(String s) {
        return LEGACY.deserialize(s);
    }

    public static Component componentFromString(String s) {
        if (s == null || s.isEmpty()) return Component.empty();
        if (s.indexOf('<') >= 0 && s.indexOf('>') >= 0) {
            return MINI.deserialize(s);
        }
        return LEGACY.deserialize(s);
    }

    public static String toLegacy(Component component) {
        String legacy = LEGACY.serialize(component);
        legacy = legacy.replace("§r", "");
        if (legacy.length() > 0) {
            legacy = "§r" + legacy;
        }
        return legacy;
    }

    public static String toMini(Component component) {
        return MINI.serialize(component);
    }

    // Legacy string format for APIs that only accept String (scoreboards, etc.)
    public static String formatText(String s) {
        return LEGACY.serialize(LEGACY.deserialize(s));
    }

    public static Component adminMessage(String s) {
        return format("&6&l[The Walls]&r " + s);
    }

    public static void sendTitle(Player p, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        Title.Times times = Title.Times.times(
                Duration.ofMillis(fadeIn * 50L),
                Duration.ofMillis(stay * 50L),
                Duration.ofMillis(fadeOut * 50L)
        );
        p.showTitle(Title.title(componentFromString(title), componentFromString(subtitle), times));
    }

    public static String colorToLegacy(String color) {
        if (color == null || color.isEmpty()) return "";
        if (color.indexOf('<') >= 0 && color.indexOf('>') >= 0) {
            return toLegacy(MINI.deserialize(color));
        }
        return color;
    }

    public static String normalizeColor(String color) {
        if (color == null) return "";
        String c = color.trim();
        if (c.isEmpty()) return c;
        if (c.indexOf('<') >= 0 && c.indexOf('>') >= 0) {
            return c;
        }
        if (c.length() >= 2 && (c.charAt(0) == '&' || c.charAt(0) == '§')) {
            char code = Character.toLowerCase(c.charAt(1));
            switch (code) {
                case '0': return "<black>";
                case '1': return "<dark_blue>";
                case '2': return "<dark_green>";
                case '3': return "<dark_aqua>";
                case '4': return "<dark_red>";
                case '5': return "<dark_purple>";
                case '6': return "<gold>";
                case '7': return "<gray>";
                case '8': return "<dark_gray>";
                case '9': return "<blue>";
                case 'a': return "<green>";
                case 'b': return "<aqua>";
                case 'c': return "<red>";
                case 'd': return "<light_purple>";
                case 'e': return "<yellow>";
                case 'f': return "<white>";
                default: return c;
            }
        }
        return c;
    }

    public static String menuTitle(String key, String fallback) {
        String raw = null;
        if (Config.data != null && key != null && !key.isEmpty()) {
            raw = Config.data.getString("gui.titles." + key);
            if ((raw == null || raw.isEmpty()) && !key.endsWith("s")) {
                raw = Config.data.getString("gui.titles." + key + "s");
            }
        }
        if (raw == null || raw.isEmpty()) {
            raw = null;
        }
        if (raw == null || raw.isEmpty()) {
            if (fallback != null && !fallback.isEmpty()) {
                raw = fallback;
            } else {
                String def = null;
                if (Config.data != null) {
                    def = Config.data.getString("gui.titles.default");
                }
                raw = (def == null || def.isEmpty()) ? "Menu" : def;
            }
        }
        String legacy = toLegacy(componentFromString(raw));
        if (legacy.length() <= 32) return legacy;
        // Fallback to plain text truncated to 32
        String plain = legacy.replaceAll("§.", "");
        if (plain.length() > 32) {
            plain = plain.substring(0, 32);
        }
        return plain;
    }

    public static void applyGuiFiller(com.samjakob.spigui.menu.SGMenu menu) {
        if (menu == null || Config.data == null) return;
        if (!Config.data.getBoolean("gui.filler.enabled", true)) return;
        String matName = Config.data.getString("gui.filler.material", "BLACK_STAINED_GLASS_PANE");
        Material mat = Material.matchMaterial(matName);
        if (mat == null) mat = Material.GRAY_STAINED_GLASS_PANE;
        org.bukkit.inventory.ItemStack filler = new org.bukkit.inventory.ItemStack(mat);
        org.bukkit.inventory.meta.ItemMeta meta = filler.getItemMeta();
        if (meta != null) {
            String name = Config.data.getString("gui.filler.name", " ");
            meta.displayName(componentFromString(name));
            java.util.List<String> loreRaw = Config.data.getStringList("gui.filler.lore");
            if (!loreRaw.isEmpty()) {
                java.util.List<net.kyori.adventure.text.Component> lore = new java.util.ArrayList<>();
                for (String line : loreRaw) lore.add(componentFromString(line));
                meta.lore(lore);
            }
            filler.setItemMeta(meta);
        }
        org.bukkit.inventory.Inventory inv = menu.getInventory();
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, filler);
            }
        }
    }

    public static void applyGuiToolbar(com.samjakob.spigui.menu.SGMenu menu, org.bukkit.entity.Player viewer, Runnable backAction) {
        if (menu == null || Config.data == null) return;
        if (!Config.data.getBoolean("gui.toolbar.enabled", true)) return;
        int rows = menu.getInventory().getSize() / 9;
        if (rows < 2) return;
        menu.setRowsPerPage(rows - 1);
        menu.setAutomaticPaginationEnabled(true);

        int prevSlot = Config.data.getInt("gui.toolbar.prev.slot", 45);
        int nextSlot = Config.data.getInt("gui.toolbar.next.slot", 53);
        int currentSlot = Config.data.getInt("gui.toolbar.current.slot", 49);
        int backSlot = Config.data.getInt("gui.toolbar.back.slot", 48);
        int closeSlot = Config.data.getInt("gui.toolbar.close.slot", 50);

        stickIfInRange(menu, prevSlot);
        stickIfInRange(menu, nextSlot);
        stickIfInRange(menu, currentSlot);
        stickIfInRange(menu, backSlot);
        stickIfInRange(menu, closeSlot);

        menu.setToolbarBuilder((slot, page, type, sgMenu) -> {
            if (slot == backSlot && backAction != null) {
                return toolbarButton("gui.toolbar.back", () -> backAction.run());
            }
            if (slot == closeSlot) {
                return toolbarButton("gui.toolbar.close", () -> {
                    if (viewer != null) viewer.closeInventory();
                });
            }
            switch (type) {
                case PREV_BUTTON -> {
                    if (slot == prevSlot) return toolbarButton("gui.toolbar.prev", null);
                }
                case NEXT_BUTTON -> {
                    if (slot == nextSlot) return toolbarButton("gui.toolbar.next", null);
                }
                case CURRENT_BUTTON -> {
                    if (slot == currentSlot) {
                        int max = sgMenu.getMaxPage() + 1;
                        int cur = sgMenu.getCurrentPage() + 1;
                        java.util.Map<String, String> ph = java.util.Map.of("page", String.valueOf(cur), "pages", String.valueOf(max));
                        return toolbarButton("gui.toolbar.current", null, ph);
                    }
                }
                default -> {}
            }
            return null;
        });

        menu.setOnPageChange(m -> applyGuiFiller(m));
    }

    private static void stickIfInRange(com.samjakob.spigui.menu.SGMenu menu, int slot) {
        if (slot < 0 || slot >= menu.getInventory().getSize()) return;
        menu.stickSlot(slot);
    }

    private static com.samjakob.spigui.buttons.SGButton toolbarButton(String key, Runnable onClick) {
        return toolbarButton(key, onClick, null);
    }

    private static com.samjakob.spigui.buttons.SGButton toolbarButton(String key, Runnable onClick, java.util.Map<String, String> placeholders) {
        String base = "gui.toolbar." + key.substring(key.lastIndexOf('.') + 1);
        String matName = Config.data.getString(base + ".material", "GRAY_STAINED_GLASS_PANE");
        String name = Config.data.getString(base + ".name", " ");
        java.util.List<String> loreRaw = Config.data.getStringList(base + ".lore");
        org.bukkit.Material mat = org.bukkit.Material.matchMaterial(matName);
        if (mat == null) mat = org.bukkit.Material.GRAY_STAINED_GLASS_PANE;
        if (placeholders != null) {
            name = applyPlaceholders(name, placeholders);
        }
        com.samjakob.spigui.item.ItemBuilder builder = new com.samjakob.spigui.item.ItemBuilder(mat)
                .name(toLegacy(componentFromString(name)));
        if (loreRaw != null && !loreRaw.isEmpty()) {
            java.util.List<String> lore = new java.util.ArrayList<>();
            for (String line : loreRaw) {
                String resolved = placeholders == null ? line : applyPlaceholders(line, placeholders);
                lore.add(toLegacy(componentFromString(resolved)));
            }
            builder = builder.lore(lore);
        }
        return new com.samjakob.spigui.buttons.SGButton(builder.build()).withListener(event -> {
            event.setCancelled(true);
            if (onClick != null) onClick.run();
        });
    }

    public static int guiRows(String key, int fallback) {
        if (Config.data == null || key == null || key.isEmpty()) return Math.max(1, fallback);
        int rows = Config.data.getInt("gui.rows." + key, fallback);
        if (rows < 1) rows = fallback;
        return Math.max(1, rows);
    }

    public static int guiSlot(String key, int fallback) {
        if (Config.data == null || key == null || key.isEmpty()) return fallback;
        return Config.data.getInt(key, fallback);
    }

    public static com.samjakob.spigui.item.ItemBuilder guiItem(String basePath, org.bukkit.Material fallback, java.util.Map<String, String> placeholders) {
        String matName = Config.data == null ? null : Config.data.getString(basePath + ".material");
        org.bukkit.Material mat = matName == null || matName.isEmpty() ? fallback : org.bukkit.Material.matchMaterial(matName);
        if (mat == null) mat = fallback;
        String name = Config.data == null ? null : Config.data.getString(basePath + ".name", " ");
        if (name == null) name = " ";
        if (placeholders != null) name = applyPlaceholders(name, placeholders);
        com.samjakob.spigui.item.ItemBuilder builder = new com.samjakob.spigui.item.ItemBuilder(mat)
                .name(toLegacy(componentFromString(name)));
        if (Config.data != null) {
            java.util.List<String> loreRaw = Config.data.getStringList(basePath + ".lore");
            if (loreRaw != null && !loreRaw.isEmpty()) {
                java.util.List<String> lore = new java.util.ArrayList<>();
                for (String line : loreRaw) {
                    String resolved = placeholders == null ? line : applyPlaceholders(line, placeholders);
                    lore.add(toLegacy(componentFromString(resolved)));
                }
                builder = builder.lore(lore);
            }
        }
        return builder;
    }

    public static String applyPlaceholders(String input, java.util.Map<String, String> placeholders) {
        if (input == null) return null;
        if (placeholders == null || placeholders.isEmpty()) return input;
        String result = input;
        for (java.util.Map.Entry<String, String> entry : placeholders.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return result;
    }

    public static org.bukkit.Color parseHexColor(String hex, org.bukkit.Color fallback) {
        if (hex == null) return fallback;
        String value = hex.trim();
        if (value.startsWith("#")) value = value.substring(1);
        if (value.length() != 6) return fallback;
        try {
            int rgb = Integer.parseInt(value, 16);
            int r = (rgb >> 16) & 0xFF;
            int g = (rgb >> 8) & 0xFF;
            int b = rgb & 0xFF;
            return org.bukkit.Color.fromRGB(r, g, b);
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    public static org.bukkit.Color colorFromString(String color, org.bukkit.Color fallback) {
        if (color == null) return fallback;
        String c = color.trim();
        if (c.isEmpty()) return fallback;
        int hexIndex = c.indexOf("<#");
        if (hexIndex >= 0) {
            int end = c.indexOf(">", hexIndex);
            if (end > hexIndex + 2) {
                String hex = c.substring(hexIndex + 2, end);
                return parseHexColor(hex, fallback);
            }
        }
        if (c.startsWith("#")) {
            return parseHexColor(c, fallback);
        }
        if (c.length() >= 2 && (c.charAt(0) == '&' || c.charAt(0) == '§')) {
            char code = Character.toLowerCase(c.charAt(1));
            switch (code) {
                case 'c': return org.bukkit.Color.fromRGB(255, 85, 85);
                case '9': return org.bukkit.Color.fromRGB(85, 85, 255);
                case 'a': return org.bukkit.Color.fromRGB(85, 255, 85);
                case 'e': return org.bukkit.Color.fromRGB(255, 255, 85);
                case '6': return org.bukkit.Color.fromRGB(255, 170, 0);
                case 'b': return org.bukkit.Color.fromRGB(85, 255, 255);
                case 'd': return org.bukkit.Color.fromRGB(255, 85, 255);
                case 'f': return org.bukkit.Color.fromRGB(255, 255, 255);
                case '0': return org.bukkit.Color.fromRGB(0, 0, 0);
                case '1': return org.bukkit.Color.fromRGB(0, 0, 170);
                case '2': return org.bukkit.Color.fromRGB(0, 170, 0);
                case '3': return org.bukkit.Color.fromRGB(0, 170, 170);
                case '4': return org.bukkit.Color.fromRGB(170, 0, 0);
                case '5': return org.bukkit.Color.fromRGB(170, 0, 170);
                case '7': return org.bukkit.Color.fromRGB(170, 170, 170);
                case '8': return org.bukkit.Color.fromRGB(85, 85, 85);
                default: return fallback;
            }
        }
        return fallback;
    }

    public static boolean isAlive(Player p) {
        return p.getGameMode() != GameMode.SPECTATOR || p.getStatistic(Statistic.DEATHS) <= 0;
    }

    public static java.util.List<Player> getAlivePlayers(Arena arena) {
        java.util.List<Player> alive = new java.util.ArrayList<>();
        if (arena == null) return alive;
        for (Player p : arena.getPlayers()) {
            if (Utils.isAlive(p)) {
                alive.add(p);
            }
        }
        return alive;
    }

    public static java.util.List<Player> getEventTargets(Arena arena, int maxTargets) {
        java.util.List<Player> alive = getAlivePlayers(arena);
        if (alive.isEmpty()) return alive;
        int safeRadius = Config.data.getInt("events.safeSpawnRadius", 0);
        if (safeRadius > 0) {
            java.util.List<Player> filtered = new java.util.ArrayList<>();
            for (Player p : alive) {
                if (!isNearTeamSpawn(p, arena, safeRadius)) {
                    filtered.add(p);
                }
            }
            if (!filtered.isEmpty()) {
                alive = filtered;
            } else {
                return java.util.Collections.emptyList();
            }
        }
        java.util.Collections.shuffle(alive);
        int limit = Math.max(1, maxTargets);
        if (alive.size() <= limit) return alive;
        return new java.util.ArrayList<>(alive.subList(0, limit));
    }

    private static boolean isNearTeamSpawn(Player p, Arena arena, double radius) {
        if (p == null || arena == null) return false;
        Team t = Team.getPlayerTeam(p, arena.getGame().teams);
        if (t == null || t.teamSpawn == null) return false;
        if (p.getWorld() == null || t.teamSpawn.getWorld() == null) return false;
        if (!p.getWorld().equals(t.teamSpawn.getWorld())) return false;
        return t.teamSpawn.distanceSquared(p.getLocation()) <= (radius * radius);
    }

    public static Plugin getPlugin() {
        return Bukkit.getPluginManager().getPlugin("TheWalls");
    }

    public void checkWinner() {
        ArrayList<Team> teamsToRemove = new ArrayList<>();
        for (Team t : this.arena.getGame().aliveTeams) {
            for (Player p : t.members) {
                t.alive = false;
                if (Utils.isAlive(p)) {
                    t.alive = true;
                    break;
                }
            }
            if (!t.alive) {
                for (Player p : this.arena.getPlayers()) {
                    p.playSound(p.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 255, 1);
                }
                for (Player p : this.arena.getPlayers()) {
                    p.sendMessage(Messages.msg("game.team_eliminated", java.util.Map.of("team", t.teamColor + t.teamName)));
                }
                teamsToRemove.add(t);
            }
        }
        for (Team team : teamsToRemove) {
            Utils.getPlugin().getLogger().info(team.teamName + " has been eliminated!");
            this.arena.getGame().aliveTeams.remove(team);
        }
        if (Config.data.getBoolean("teams.allowTie") || this.arena.getPlayers().size() <= 1) {
            if (this.arena.getGame().aliveTeams.size() == 0) {
                for (Player p : this.arena.getPlayers()) {
                    p.playSound(p.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 255, 1);
                }
                for (Player p : this.arena.getPlayers()) {
                    p.sendMessage(Messages.msg("game.tie"));
                }
                this.arena.getGame().end(false, null);
            }
        }
        if (this.arena.getGame().aliveTeams.size() == 1) {
            Team winningTeam = this.arena.getGame().aliveTeams.get(0);
            for (Player p : this.arena.getPlayers()) {
                p.playSound(p.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 255, 1);
            }
            for (Player p : this.arena.getPlayers()) {
                p.sendMessage(Messages.msg("game.win", java.util.Map.of("team", winningTeam.teamColor + winningTeam.teamName)));
            }
            this.arena.getGame().end(false, null);
        }

    }

}
