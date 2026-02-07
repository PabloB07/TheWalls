package ca.thewalls;

import com.infernalsuite.asp.api.AdvancedSlimePaperAPI;
import com.infernalsuite.asp.api.loaders.SlimeLoader;
import com.infernalsuite.asp.api.world.SlimeWorld;
import com.infernalsuite.asp.api.world.SlimeWorldInstance;
import com.infernalsuite.asp.api.world.properties.SlimePropertyMap;
import com.infernalsuite.asp.loaders.file.FileLoader;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.io.File;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

public final class AspWorlds {
    private static AdvancedSlimePaperAPI asp;
    private static SlimeLoader loader;
    private static final Map<String, Deque<String>> POOL = new ConcurrentHashMap<>();

    private AspWorlds() {}

    public static void init() {
        if (asp != null && loader != null) return;
        asp = AdvancedSlimePaperAPI.instance();
        String dir = Config.getAspLoaderDir();
        if (dir == null || dir.isEmpty()) {
            dir = "slime_worlds";
        }
        loader = new FileLoader(dir);
    }

    public static boolean isAvailable() {
        return asp != null && loader != null;
    }

    public static World loadFromTemplate(String templateWorld, String instanceName) {
        if (templateWorld == null || templateWorld.isEmpty()) return null;
        if (instanceName == null || instanceName.isEmpty()) return null;
        init();
        if (!isAvailable()) return null;
        try {
            SlimeWorld template;
            if (loader.worldExists(templateWorld)) {
                template = asp.readWorld(loader, templateWorld, false, new SlimePropertyMap());
            } else {
                File dir = new File(Bukkit.getWorldContainer(), templateWorld);
                template = asp.readVanillaWorld(dir, templateWorld, loader);
                asp.saveWorld(template);
            }

            if (loader.worldExists(instanceName)) {
                loader.deleteWorld(instanceName);
            }

            SlimeWorld clone = template.clone(instanceName, loader);
            SlimeWorldInstance instance = asp.loadWorld(clone, true);
            return instance.getBukkitWorld();
        } catch (Exception ex) {
            Utils.getPlugin().getLogger().warning("ASP load failed: " + ex.getMessage());
            return null;
        }
    }

    public static void loadFromTemplateAsync(String templateWorld, String instancePrefix, BiConsumer<World, String> callback) {
        if (callback == null) return;
        if (templateWorld == null || templateWorld.isEmpty()) {
            callback.accept(null, null);
            return;
        }
        init();
        if (!isAvailable()) {
            callback.accept(null, null);
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(Utils.getPlugin(), () -> {
            try {
                String pooledName = takeFromPool(templateWorld);
                final String instanceName;
                final SlimeWorld source;
                if (pooledName != null) {
                    instanceName = pooledName;
                    source = asp.readWorld(loader, instanceName, false, new SlimePropertyMap());
                } else {
                    instanceName = buildInstanceName(instancePrefix);
                    SlimeWorld template;
                    if (loader.worldExists(templateWorld)) {
                        template = asp.readWorld(loader, templateWorld, false, new SlimePropertyMap());
                    } else {
                        File dir = new File(Bukkit.getWorldContainer(), templateWorld);
                        template = asp.readVanillaWorld(dir, templateWorld, loader);
                        asp.saveWorld(template);
                    }
                    if (loader.worldExists(instanceName)) {
                        loader.deleteWorld(instanceName);
                    }
                    source = template.clone(instanceName, loader);
                }
                Bukkit.getScheduler().runTask(Utils.getPlugin(), () -> {
                    try {
                        SlimeWorldInstance instance = asp.loadWorld(source, true);
                        callback.accept(instance.getBukkitWorld(), instanceName);
                        ensurePoolAsync(templateWorld, instancePrefix, 1);
                    } catch (Exception ex) {
                        Utils.getPlugin().getLogger().warning("ASP load (sync) failed: " + ex.getMessage());
                        callback.accept(null, null);
                    }
                });
            } catch (Exception ex) {
                Utils.getPlugin().getLogger().warning("ASP load (async) failed: " + ex.getMessage());
                Bukkit.getScheduler().runTask(Utils.getPlugin(), () -> callback.accept(null, null));
            }
        });
    }

    public static void ensurePoolAsync(String templateWorld, String instancePrefix, int count) {
        if (count <= 0) return;
        init();
        if (!isAvailable()) return;
        Bukkit.getScheduler().runTaskAsynchronously(Utils.getPlugin(), () -> {
            try {
                SlimeWorld template;
                if (loader.worldExists(templateWorld)) {
                    template = asp.readWorld(loader, templateWorld, false, new SlimePropertyMap());
                } else {
                    File dir = new File(Bukkit.getWorldContainer(), templateWorld);
                    template = asp.readVanillaWorld(dir, templateWorld, loader);
                    asp.saveWorld(template);
                }
                for (int i = 0; i < count; i++) {
                    String instanceName = buildInstanceName(instancePrefix);
                    if (loader.worldExists(instanceName)) {
                        loader.deleteWorld(instanceName);
                    }
                    SlimeWorld clone = template.clone(instanceName, loader);
                    asp.saveWorld(clone);
                    addToPool(templateWorld, instanceName);
                }
            } catch (Exception ex) {
                Utils.getPlugin().getLogger().warning("ASP pool prewarm failed: " + ex.getMessage());
            }
        });
    }

    public static void unloadInstance(World world, String instanceName, boolean deleteWorld) {
        if (world == null) return;
        try {
            Bukkit.unloadWorld(world, false);
        } catch (Exception ignored) {
        }
        if (!deleteWorld || instanceName == null || instanceName.isEmpty()) return;
        try {
            if (loader != null) {
                loader.deleteWorld(instanceName);
            }
        } catch (Exception ignored) {
        }
    }

    private static String buildInstanceName(String prefix) {
        String safePrefix = (prefix == null || prefix.isEmpty()) ? "tw_" : prefix;
        return safePrefix + UUID.randomUUID();
    }

    private static void addToPool(String templateWorld, String instanceName) {
        POOL.computeIfAbsent(templateWorld, k -> new ArrayDeque<>()).add(instanceName);
    }

    private static String takeFromPool(String templateWorld) {
        Deque<String> queue = POOL.get(templateWorld);
        if (queue == null || queue.isEmpty()) return null;
        return queue.pollFirst();
    }
}
