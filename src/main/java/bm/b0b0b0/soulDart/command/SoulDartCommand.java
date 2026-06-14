package bm.b0b0b0.soulDart.command;

import bm.b0b0b0.soulDart.config.PluginConfig;
import bm.b0b0b0.soulDart.lang.MessageService;
import bm.b0b0b0.soulDart.service.SoulDartService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class SoulDartCommand implements CommandExecutor {

    private final PluginConfig config;
    private final MessageService messages;
    private final SoulDartService soulDartService;

    public SoulDartCommand(PluginConfig config, MessageService messages, SoulDartService soulDartService) {
        this.config = config;
        this.messages = messages;
        this.soulDartService = soulDartService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.component("players-only"));
            return true;
        }

        if (!player.hasPermission(config.permission())) {
            player.sendMessage(messages.component("no-permission"));
            return true;
        }

        soulDartService.launch(player);
        player.sendMessage(messages.component("launch-success"));
        return true;
    }

}
