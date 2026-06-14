package bm.b0b0b0.soulDart.service;

import bm.b0b0b0.soulDart.config.PluginConfig;
import bm.b0b0b0.soulDart.dart.DartManager;
import bm.b0b0b0.soulDart.dart.DartVisual;
import bm.b0b0b0.soulDart.dart.FlyingDart;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public final class SoulDartService {

    private final PluginConfig config;
    private final DartManager dartManager;

    public SoulDartService(PluginConfig config, DartManager dartManager) {
        this.config = config;
        this.dartManager = dartManager;
    }

    public void launch(Player player) {
        Location base = player.getLocation();
        float yaw = base.getYaw();
        Vector forward = DartVisual.directionFromOrientation(yaw, 0f);

        Location spawn = base.clone().add(0, 2.0, 0).add(forward.clone().multiply(3.0));
        snapToBlockCenter(spawn);

        FlyingDart dart = new FlyingDart(config, player, spawn, yaw);
        dartManager.register(dart);

        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 0.4f, 1.6f);
    }

    private static void snapToBlockCenter(Location location) {
        location.setX(Math.floor(location.getX()) + 0.5);
        location.setY(Math.floor(location.getY()) + 0.5);
        location.setZ(Math.floor(location.getZ()) + 0.5);
    }

}
