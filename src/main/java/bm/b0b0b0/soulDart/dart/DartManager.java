package bm.b0b0b0.soulDart.dart;

import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class DartManager {

    private final List<FlyingDart> activeDarts = new ArrayList<>();
    private BukkitRunnable tickTask;

    public void start(JavaPlugin plugin) {
        if (tickTask != null) {
            return;
        }
        tickTask = new BukkitRunnable() {
            @Override
            public void run() {
                tickAll();
            }
        };
        tickTask.runTaskTimer(plugin, 0L, 1L);
    }

    public void register(FlyingDart dart) {
        activeDarts.add(dart);
    }

    public void shutdown() {
        if (tickTask != null) {
            tickTask.cancel();
            tickTask = null;
        }
        for (FlyingDart dart : activeDarts) {
            dart.forceRemove();
        }
        activeDarts.clear();
    }

    private void tickAll() {
        Iterator<FlyingDart> iterator = activeDarts.iterator();
        while (iterator.hasNext()) {
            FlyingDart dart = iterator.next();
            if (!dart.tick()) {
                iterator.remove();
            }
        }
    }

}
