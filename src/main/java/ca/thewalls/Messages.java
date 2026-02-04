package ca.thewalls;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Map;

public class Messages {
    private static final MiniMessage MINI = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();
    private static File file;
    private static YamlConfiguration data;

    public static void initialize(JavaPlugin plugin) {
        file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        reload();
    }

    public static void reload() {
        if (file == null) return;
        data = YamlConfiguration.loadConfiguration(file);
    }

    public static Component msg(String key) {
        return deserialize(getRaw(key));
    }

    public static Component msg(String key, Map<String, String> placeholders) {
        return deserialize(applyPlaceholders(getRaw(key), placeholders));
    }

    public static String raw(String key) {
        return getRaw(key);
    }

    public static String raw(String key, Map<String, String> placeholders) {
        return applyPlaceholders(getRaw(key), placeholders);
    }

    private static String getRaw(String key) {
        if (data == null) return key;
        String value = data.getString(key);
        return value == null ? key : value;
    }

    private static String applyPlaceholders(String input, Map<String, String> placeholders) {
        String result = input;
        if (placeholders == null) return result;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return result;
    }

    private static Component deserialize(String raw) {
        if (raw == null || raw.isEmpty()) return Component.empty();
        if (raw.indexOf('<') >= 0 && raw.indexOf('>') >= 0) {
            return MINI.deserialize(raw);
        }
        return LEGACY.deserialize(raw);
    }
}
