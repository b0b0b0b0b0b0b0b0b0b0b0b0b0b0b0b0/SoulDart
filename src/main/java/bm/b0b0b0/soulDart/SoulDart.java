package bm.b0b0b0.soulDart;

import bm.b0b0b0.soulDart.command.SoulDartCommand;
import bm.b0b0b0.soulDart.config.ConfigurationLoader;
import bm.b0b0b0.soulDart.config.PluginConfig;
import bm.b0b0b0.soulDart.dart.DartManager;
import bm.b0b0b0.soulDart.lang.MessageService;
import bm.b0b0b0.soulDart.service.SoulDartService;
import org.bukkit.plugin.java.JavaPlugin;

public final class SoulDart extends JavaPlugin {

    private DartManager dartManager;

    @Override
    public void onEnable() {
        ConfigurationLoader configurationLoader = new ConfigurationLoader(this);
        PluginConfig pluginConfig = new PluginConfig(configurationLoader);
        MessageService messageService = new MessageService(this, pluginConfig.language());

        dartManager = new DartManager();
        dartManager.start(this);

        SoulDartService soulDartService = new SoulDartService(pluginConfig, dartManager);
        SoulDartCommand command = new SoulDartCommand(pluginConfig, messageService, soulDartService);

        getCommand("souldart").setExecutor(command);
    }

    @Override
    public void onDisable() {
        if (dartManager != null) {
            dartManager.shutdown();
        }
    }

}
