package ca.thewalls;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class Cosmetics {
    private static final Map<UUID, org.bukkit.boss.BossBar> PREVIEW_BARS = new ConcurrentHashMap<>();

    private Cosmetics() {}

    public static boolean isTrailEnabled() {
        return Config.data != null && Config.data.getBoolean("cosmetics.trails.enabled", true);
    }

    public static boolean isKillEffectEnabled() {
        return Config.data != null && Config.data.getBoolean("cosmetics.killEffects.enabled", true);
    }

    public static String getTrailId(Player player) {
        if (player == null) return "";
        String id = Config.getPlayerTrail(player.getUniqueId());
        if (id == null || id.isEmpty()) {
            id = Config.data.getString("cosmetics.trails.default", "");
        }
        return id == null ? "" : id;
    }

    public static String getKillEffectId(Player player) {
        if (player == null) return "";
        String id = Config.getPlayerKillEffect(player.getUniqueId());
        if (id == null || id.isEmpty()) {
            id = Config.data.getString("cosmetics.killEffects.default", "");
        }
        return id == null ? "" : id;
    }

    public static boolean isValidTrail(String id) {
        if (id == null || id.isEmpty()) return false;
        return Config.data != null && Config.data.isConfigurationSection("cosmetics.trails.list." + id);
    }

    public static boolean isValidKillEffect(String id) {
        if (id == null || id.isEmpty()) return false;
        return Config.data != null && Config.data.isConfigurationSection("cosmetics.killEffects.list." + id);
    }

    public static int getTrailCost(String id) {
        if (Config.data == null || id == null) return 0;
        return Math.max(0, Config.data.getInt("cosmetics.trails.list." + id + ".cost", 0));
    }

    public static int getKillEffectCost(String id) {
        if (Config.data == null || id == null) return 0;
        return Math.max(0, Config.data.getInt("cosmetics.killEffects.list." + id + ".cost", 0));
    }

    public static boolean isTrailUnlocked(Player player, String id) {
        return player != null && Config.isCosmeticUnlocked(player.getUniqueId(), "trails", id);
    }

    public static boolean isKillEffectUnlocked(Player player, String id) {
        return player != null && Config.isCosmeticUnlocked(player.getUniqueId(), "killeffects", id);
    }

    public static void unlockTrail(Player player, String id) {
        if (player == null) return;
        Config.unlockCosmetic(player.getUniqueId(), "trails", id);
    }

    public static void unlockKillEffect(Player player, String id) {
        if (player == null) return;
        Config.unlockCosmetic(player.getUniqueId(), "killeffects", id);
    }

    public static boolean hasTrailPermission(Player player, String id) {
        if (player == null) return false;
        String perm = Config.data.getString("cosmetics.trails.list." + id + ".permission", "");
        return perm == null || perm.isEmpty() || player.hasPermission(perm) || player.isOp();
    }

    public static boolean hasKillEffectPermission(Player player, String id) {
        if (player == null) return false;
        String perm = Config.data.getString("cosmetics.killEffects.list." + id + ".permission", "");
        return perm == null || perm.isEmpty() || player.hasPermission(perm) || player.isOp();
    }

    public static void playTrail(Player player) {
        if (player == null || !isTrailEnabled()) return;
        String id = getTrailId(player);
        if (id == null || id.isEmpty()) return;
        ConfigurationSection sec = Config.data.getConfigurationSection("cosmetics.trails.list." + id);
        if (sec == null) return;
        displayShape(sec, player.getLocation());
    }

    public static void playKillEffect(Player killer, Location location) {
        if (location == null || !isKillEffectEnabled()) return;
        String id = killer == null ? "" : getKillEffectId(killer);
        if (id == null || id.isEmpty()) return;
        ConfigurationSection sec = Config.data.getConfigurationSection("cosmetics.killEffects.list." + id);
        if (sec == null) return;
        displayShape(sec, location);
        String soundName = sec.getString("sound", "");
        if (soundName != null && !soundName.isEmpty()) {
            try {
                Sound sound = Sound.valueOf(soundName.toUpperCase());
                location.getWorld().playSound(location, sound, 1.0f, 1.0f);
            } catch (Exception ignored) {
            }
        }
    }

    private static void displayShape(ConfigurationSection sec, Location loc) {
        if (sec == null || loc == null || loc.getWorld() == null) return;
        String shape = sec.getString("shape", "circle");
        int points = Math.max(8, sec.getInt("points", 32));
        double radius = sec.getDouble("radius", 0.6);
        double height = sec.getDouble("height", 1.6);
        double spin = sec.getDouble("spin", 0.2);
        int petals = Math.max(3, sec.getInt("petals", 6));
        hm.zelha.particlesfx.particles.parents.Particle particle = createParticle(sec);
        hm.zelha.particlesfx.util.LocationSafe center = new hm.zelha.particlesfx.util.LocationSafe(loc);

        switch (shape.toLowerCase()) {
            case "circle_filled":
                new hm.zelha.particlesfx.shapers.ParticleCircleFilled(particle, center, radius, points).display();
                break;
            case "sphere":
                new hm.zelha.particlesfx.shapers.ParticleSphere(particle, center, radius, points).display();
                break;
            case "line":
                hm.zelha.particlesfx.util.LocationSafe top = new hm.zelha.particlesfx.util.LocationSafe(loc.clone().add(0, height, 0));
                new hm.zelha.particlesfx.shapers.ParticleLine(particle, points, center, top).display();
                break;
            case "spiral":
                hm.zelha.particlesfx.util.CircleInfo circle = new hm.zelha.particlesfx.util.CircleInfo(center, radius);
                hm.zelha.particlesfx.shapers.ParticleSpiral spiral = new hm.zelha.particlesfx.shapers.ParticleSpiral(particle, spin, points, circle);
                spiral.setRotateCircles(true);
                spiral.display();
                break;
            case "donut":
                hm.zelha.particlesfx.util.ParticleSFX.donut(particle, center, radius, points).display();
                break;
            case "flower":
                hm.zelha.particlesfx.util.ParticleSFX.flower(particle, center, radius, petals, points).display();
                break;
            case "heart":
                hm.zelha.particlesfx.util.ParticleSFX.heart2D(particle, center, radius, points).display();
                break;
            case "star":
                hm.zelha.particlesfx.util.ParticleSFX.star(particle, center, radius, points).display();
                break;
            default:
                new hm.zelha.particlesfx.shapers.ParticleCircle(particle, center, radius, points).display();
                break;
        }
    }

    private static hm.zelha.particlesfx.particles.parents.Particle createParticle(ConfigurationSection sec) {
        String name = sec.getString("particle", "dust");
        String colorStr = sec.getString("color", "#ff3b3b");
        double size = sec.getDouble("size", 1.0);
        hm.zelha.particlesfx.util.Color color = toSfxColor(Utils.parseHexColor(colorStr, org.bukkit.Color.fromRGB(255, 59, 59)));
        switch (name.toLowerCase()) {
            case "flame":
                return new hm.zelha.particlesfx.particles.ParticleFlame();
            case "glow":
                return new hm.zelha.particlesfx.particles.ParticleGlow();
            case "crit":
                return new hm.zelha.particlesfx.particles.ParticleCrit();
            case "cloud":
                return new hm.zelha.particlesfx.particles.ParticleCloud();
            case "smoke":
                return new hm.zelha.particlesfx.particles.ParticleSmokeWhite();
            case "dust":
            default:
                return new hm.zelha.particlesfx.particles.ParticleDustColored(color, size);
        }
    }

    private static hm.zelha.particlesfx.util.Color toSfxColor(org.bukkit.Color color) {
        if (color == null) return new hm.zelha.particlesfx.util.Color(255, 59, 59);
        return new hm.zelha.particlesfx.util.Color(color.getRed(), color.getGreen(), color.getBlue());
    }

    public static void previewTrail(Player player, String id) {
        if (player == null || Config.data == null || id == null) return;
        ConfigurationSection sec = Config.data.getConfigurationSection("cosmetics.trails.list." + id);
        if (sec == null) return;
        displayShape(sec, player.getLocation());
        showPreviewBar(player, sec.getString("display.name", id));
    }

    public static void previewKillEffect(Player player, String id) {
        if (player == null || Config.data == null || id == null) return;
        ConfigurationSection sec = Config.data.getConfigurationSection("cosmetics.killEffects.list." + id);
        if (sec == null) return;
        displayShape(sec, player.getLocation());
        showPreviewBar(player, sec.getString("display.name", id));
    }

    private static void showPreviewBar(Player player, String name) {
        if (player == null) return;
        int seconds = Math.max(1, Config.data.getInt("cosmetics.previewSeconds", 3));
        org.bukkit.boss.BossBar bar = PREVIEW_BARS.remove(player.getUniqueId());
        if (bar != null) {
            bar.removeAll();
        }
        String title = Utils.toLegacy(Utils.componentFromString(Messages.raw("menu.cosmetics_preview_bar", java.util.Map.of("name", name))));
        bar = org.bukkit.Bukkit.createBossBar(
                title,
                org.bukkit.boss.BarColor.PURPLE,
                org.bukkit.boss.BarStyle.SOLID
        );
        bar.addPlayer(player);
        PREVIEW_BARS.put(player.getUniqueId(), bar);
        final org.bukkit.boss.BossBar finalBar = bar;
        for (int i = 0; i <= seconds; i++) {
            int tick = i;
            org.bukkit.Bukkit.getScheduler().runTaskLater(Utils.getPlugin(), () -> {
                if (!PREVIEW_BARS.containsKey(player.getUniqueId())) return;
                double progress = Math.max(0.0, 1.0 - (tick / (double) seconds));
                finalBar.setProgress(progress);
                if (tick >= seconds) {
                    finalBar.removeAll();
                    PREVIEW_BARS.remove(player.getUniqueId());
                }
            }, i * 20L);
        }
    }
}
