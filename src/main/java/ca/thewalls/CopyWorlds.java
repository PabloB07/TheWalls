package ca.thewalls;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.Consumer;

public final class CopyWorlds {
    private CopyWorlds() {}

    public static void loadFromTemplateAsync(String templateName, String instanceName, Consumer<World> callback) {
        if (callback == null) return;
        if (templateName == null || templateName.isEmpty()) {
            callback.accept(null);
            return;
        }
        if (instanceName == null || instanceName.isEmpty()) {
            callback.accept(null);
            return;
        }
        Path templatePath = resolveTemplatePath(templateName);
        if (templatePath == null || !Files.exists(templatePath)) {
            callback.accept(null);
            return;
        }
        Path instancePath = new java.io.File(Bukkit.getWorldContainer(), instanceName).toPath();
        Bukkit.getScheduler().runTaskAsynchronously(Utils.getPlugin(), () -> {
            try {
                if (Files.exists(instancePath)) {
                    deleteDirectory(instancePath);
                }
                copyDirectory(templatePath, instancePath);
            } catch (IOException ex) {
                Utils.getPlugin().getLogger().warning("World copy failed: " + ex.getMessage());
                Bukkit.getScheduler().runTask(Utils.getPlugin(), () -> callback.accept(null));
                return;
            }
            Bukkit.getScheduler().runTask(Utils.getPlugin(), () -> {
                World world = Bukkit.createWorld(new WorldCreator(instanceName));
                callback.accept(world);
            });
        });
    }

    public static void unloadInstance(World world, String instanceName, boolean deleteFolder) {
        if (world == null) return;
        try {
            Bukkit.unloadWorld(world, false);
        } catch (Exception ignored) {
        }
        if (!deleteFolder || instanceName == null || instanceName.isEmpty()) return;
        try {
            Path instancePath = new java.io.File(Bukkit.getWorldContainer(), instanceName).toPath();
            deleteDirectory(instancePath);
        } catch (IOException ignored) {
        }
    }

    private static Path resolveTemplatePath(String templateName) {
        Path templatePath = Paths.get(templateName);
        if (templatePath.isAbsolute()) {
            return templatePath;
        }
        String baseDir = Config.getCopyTemplateDir();
        if (baseDir == null || baseDir.isEmpty()) {
            baseDir = "worlds/templates";
        }
        Path base = Paths.get(baseDir);
        if (!base.isAbsolute()) {
            base = new java.io.File(Bukkit.getWorldContainer(), baseDir).toPath();
        }
        return base.resolve(templateName);
    }

    private static void copyDirectory(Path source, Path target) throws IOException {
        Files.walkFileTree(source, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path rel = source.relativize(dir);
                Path dest = target.resolve(rel);
                Files.createDirectories(dest);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                String name = file.getFileName().toString();
                if (name.equalsIgnoreCase("uid.dat") || name.equalsIgnoreCase("session.lock")) {
                    return FileVisitResult.CONTINUE;
                }
                Path rel = source.relativize(file);
                Path dest = target.resolve(rel);
                Files.copy(file, dest, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private static void deleteDirectory(Path dir) throws IOException {
        if (dir == null || !Files.exists(dir)) return;
        Files.walkFileTree(dir, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.deleteIfExists(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.deleteIfExists(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
