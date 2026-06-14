package bm.b0b0b0.soulDart.config;

import bm.b0b0b0.soulDart.config.settings.SoulDartSettings;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;

public final class ConfigurationLoader {

    private final SoulDartSettings settings;

    public ConfigurationLoader(JavaPlugin plugin) {
        Path configPath = plugin.getDataFolder().toPath().resolve("config.yml");
        plugin.getDataFolder().mkdirs();
        settings = new SoulDartSettings();
        if (configPath.toFile().exists()) {
            settings.reload(configPath);
        }
        settings.save(configPath);
    }

    public SoulDartSettings settings() {
        return settings;
    }

}
