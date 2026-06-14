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
        Vector lateral = DartVisual.lateralFromOrientation(yaw, 0f);

        Location center = base.clone()
                .add(0, 2.0, 0)
                .add(forward.clone().multiply(config.spawnForwardDistance()));

        int count = config.spawnCount();
        double spread = config.spawnSpread();

        for (int index = 0; index < count; index++) {
            Location spawn = spreadSpawnLocation(center, forward, lateral, spread, index, count);
            FlyingDart dart = new FlyingDart(config, player, spawn, yaw);
            dartManager.register(dart);
        }

        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 0.4f, 1.6f);
    }

    private static Location spreadSpawnLocation(
            Location center,
            Vector forward,
            Vector lateral,
            double spread,
            int index,
            int count
    ) {
        double angle = (Math.PI * 2.0 * index) / count;
        double vertical = Math.sin(angle * 2.0) * spread * 0.12;

        Vector offset = lateral.clone().multiply(Math.cos(angle) * spread)
                .add(forward.clone().multiply(Math.sin(angle) * spread * 0.25))
                .add(new Vector(0, vertical, 0));

        return center.clone().add(offset);
    }

}
