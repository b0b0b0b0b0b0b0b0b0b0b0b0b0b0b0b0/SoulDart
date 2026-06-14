package bm.b0b0b0.soulDart.config;

import bm.b0b0b0.soulDart.config.settings.SoulDartSettings;

public final class PluginConfig {

    private final SoulDartSettings settings;

    public PluginConfig(ConfigurationLoader loader) {
        this.settings = loader.settings();
    }

    public int lifetimeTicks() {
        return Math.max(1, (int) Math.round(settings.lifetimeSeconds * 20.0));
    }

    public int spawnCount() {
        return Math.max(1, settings.spawnCount);
    }

    public double spawnSpread() {
        return Math.max(0.0, settings.spawnSpread);
    }

    public double spawnForwardDistance() {
        return Math.max(1.0, settings.spawnForwardDistance);
    }

    public double targetRadius() {
        return settings.targetRadius;
    }

    public double flightSpeed() {
        return settings.flightSpeed;
    }

    public double chaseSpeedMultiplier() {
        return settings.chaseSpeedMultiplier;
    }

    public double wanderTurnSpeed() {
        return settings.wanderTurnSpeed;
    }

    public double targetTurnSpeed() {
        return settings.targetTurnSpeed;
    }

    public double targetPitchSpeed() {
        return settings.targetPitchSpeed;
    }

    public double velocityInertia() {
        return settings.velocityInertia;
    }

    public double chaseVelocityInertia() {
        return settings.chaseVelocityInertia;
    }

    public double combatDistance() {
        return settings.combatDistance;
    }

    public double combatDistanceMargin() {
        return settings.combatDistanceMargin;
    }

    public int assemblyDurationTicks() {
        return settings.assemblyDurationTicks;
    }

    public double bobAmplitude() {
        return settings.bobAmplitude;
    }

    public double swayAmplitude() {
        return settings.swayAmplitude;
    }

    public int burstSize() {
        return settings.burstSize;
    }

    public int burstShotDelay() {
        return settings.burstShotDelay;
    }

    public int burstCooldownMin() {
        return settings.burstCooldownMin;
    }

    public int burstCooldownMax() {
        return settings.burstCooldownMax;
    }

    public double burstAimAngle() {
        return settings.burstAimAngle;
    }

    public double spitSpeed() {
        return settings.spitSpeed;
    }

    public String permission() {
        return settings.permission;
    }

    public String language() {
        return settings.language;
    }

}
