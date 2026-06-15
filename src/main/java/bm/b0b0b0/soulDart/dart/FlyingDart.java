package bm.b0b0b0.soulDart.dart;

import bm.b0b0b0.soulDart.config.PluginConfig;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.LlamaSpit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.util.concurrent.ThreadLocalRandom;

public final class FlyingDart {

    private final PluginConfig config;
    private final Player owner;
    private final DartVisual visual;
    private final World world;
    private final Matrix4f[] assemblyStartMatrices;

    private Location anchor;
    private float spawnYaw;
    private float wanderYaw;
    private Quaternionf orientation;
    private Vector velocity;

    private LivingEntity target;
    private int age;
    private int assemblyTick;
    private boolean assembled;
    private int wanderTimer;
    private int burstReloadTicks;
    private int burstShotsLeft;
    private int burstShotTimer;

    public FlyingDart(PluginConfig config, Player owner, Location spawnLocation, float initialYaw) {
        this.config = config;
        this.owner = owner;
        this.world = spawnLocation.getWorld();
        this.anchor = spawnLocation.clone();

        this.spawnYaw = initialYaw;
        this.wanderYaw = initialYaw;
        this.orientation = DartVisual.orientationFromYawPitch(initialYaw, 0f);
        this.velocity = new Vector(0, 0, 0);

        visual = new DartVisual(world, anchor);
        assemblyStartMatrices = visual.createAssemblyStartMatrices();
        assembled = false;
        assemblyTick = 0;

        age = 0;
        wanderTimer = 0;
        burstReloadTicks = 0;
        burstShotsLeft = 0;
        burstShotTimer = 0;
    }

    public boolean tick() {
        if (!assembled) {
            return tickAssembly();
        }

        age++;
        if (age >= config.lifetimeTicks()) {
            despawn(true);
            return false;
        }

        target = findNearestVisibleTarget();

        Vector desiredMovement;

        if (target != null) {
            Vector desiredAim = desiredAimDirection();
            float turnSpeed = (float) (burstShotsLeft > 0
                    ? config.targetTurnSpeed() * 1.5
                    : config.targetTurnSpeed() * 1.15);
            approachOrientation(desiredAim, turnSpeed);
            desiredMovement = combatMovement();
        } else {
            if (burstShotsLeft > 0) {
                burstShotsLeft = 0;
            }
            wanderTimer++;
            if (wanderTimer >= 40) {
                wanderTimer = 0;
                wanderYaw += ThreadLocalRandom.current().nextFloat() * 28f - 14f;
            }
            Vector wanderDirection = DartVisual.directionFromOrientation(wanderYaw, 0f);
            approachOrientation(wanderDirection, (float) config.wanderTurnSpeed());
            desiredMovement = noseDirection().multiply(config.flightSpeed() * 0.5);
        }

        if (target != null) {
            if (desiredMovement.lengthSquared() > 0.0001) {
                velocity = desiredMovement;
            } else {
                Vector nose = noseDirection();
                velocity = nose.multiply(velocity.dot(nose) * 0.65);
            }
        } else {
            double inertia = config.velocityInertia();
            velocity.multiply(1.0 - inertia).add(desiredMovement.multiply(inertia));
            Vector nose = noseDirection();
            velocity = nose.multiply(velocity.dot(nose));
        }

        Vector lateral = DartVisual.lateralFromOrientation(orientation);
        double bob = target == null ? Math.sin(age * 0.11) * config.bobAmplitude() : 0;
        double sway = target == null ? Math.sin(age * 0.075) * config.swayAmplitude() : 0;

        anchor.add(velocity);
        anchor.setY(anchor.getY() + bob);
        anchor.add(lateral.clone().multiply(sway));

        visual.update(anchor, orientation);
        tickBurst();

        return true;
    }

    public void forceRemove() {
        despawn(false);
    }

    private boolean tickAssembly() {
        assemblyTick++;
        int duration = Math.max(1, config.assemblyDurationTicks());
        float progress = Math.min(1f, assemblyTick / (float) duration);
        float eased = 1f - (1f - progress) * (1f - progress) * (1f - progress);

        visual.updateAssembly(anchor, spawnYaw, eased, assemblyTick, assemblyStartMatrices);

        if (progress >= 1f) {
            assembled = true;
            orientation = DartVisual.orientationFromYawPitch(spawnYaw, 0f);
            velocity = DartVisual.directionFromOrientation(orientation).multiply(config.flightSpeed() * 0.35);
            visual.update(anchor, orientation);
            world.playSound(anchor, Sound.BLOCK_WOOL_PLACE, 0.8f, 0.7f);
            world.playSound(anchor, Sound.ENTITY_IRON_GOLEM_REPAIR, 0.5f, 1.4f);
            world.spawnParticle(Particle.CLOUD, anchor, 8, 0.4, 0.4, 0.4, 0.02);
        }

        return true;
    }

    private Vector desiredAimDirection() {
        Location tip = visual.tipLocation(anchor, orientation);
        Vector toTarget = aimPoint(target).subtract(tip.toVector());
        if (toTarget.lengthSquared() < 0.01) {
            return noseDirection();
        }
        return toTarget.normalize();
    }

    private void approachOrientation(Vector desiredDirection, float maxStepDegrees) {
        orientation = DartVisual.rotateTowards(orientation, desiredDirection, maxStepDegrees);
    }

    private Vector combatMovement() {
        Vector nose = noseDirection();
        double distance = target.getLocation().distance(anchor);
        double preferred = config.combatDistance();
        double margin = config.combatDistanceMargin();
        double speed = config.flightSpeed() * config.chaseSpeedMultiplier();

        if (distance < preferred - margin) {
            return nose.clone().multiply(-speed);
        }
        if (distance > preferred + margin) {
            return nose.clone().multiply(speed);
        }

        double distanceError = distance - preferred;
        if (Math.abs(distanceError) > 0.2) {
            return nose.clone().multiply(Math.signum(distanceError) * speed * 0.3);
        }

        return new Vector(0, 0, 0);
    }

    private Vector noseDirection() {
        return DartVisual.directionFromOrientation(orientation);
    }

    private void tickBurst() {
        if (burstShotsLeft > 0) {
            burstShotTimer--;
            if (burstShotTimer <= 0) {
                if (target == null || !target.isValid() || target.isDead() || !hasLineOfSight(target)) {
                    burstShotsLeft = 0;
                    return;
                }
                fireSpitAt(target);
                burstShotsLeft--;
                if (burstShotsLeft > 0) {
                    burstShotTimer = config.burstShotDelay();
                } else {
                    int min = config.burstCooldownMin();
                    int max = config.burstCooldownMax();
                    if (max < min) {
                        max = min;
                    }
                    burstReloadTicks = ThreadLocalRandom.current().nextInt(min, max + 1);
                }
            }
            return;
        }

        if (burstReloadTicks > 0) {
            burstReloadTicks--;
            return;
        }

        if (target == null || !target.isValid() || target.isDead()) {
            return;
        }

        if (!isAimedAt(target, config.burstAimAngle()) || !hasLineOfSight(target)) {
            return;
        }

        burstShotsLeft = config.burstSize();
        burstShotTimer = 0;
    }

    private void fireSpitAt(LivingEntity aimTarget) {
        Location tip = visual.tipLocation(anchor, orientation);
        Vector shotDirection = noseDirection();

        if (aimTarget != null && aimTarget.isValid() && !aimTarget.isDead()) {
            Vector toTarget = aimPoint(aimTarget).subtract(tip.toVector());
            if (toTarget.lengthSquared() >= 0.01) {
                toTarget.normalize();
                if (shotDirection.dot(toTarget) < 0.85) {
                    return;
                }
                shotDirection = toTarget;
            }
        }

        shotDirection.normalize().multiply(config.spitSpeed());
        Vector spitVelocity = shotDirection.clone();

        world.spawn(tip, LlamaSpit.class, spit -> {
            spit.setShooter(owner);
            spit.setVelocity(spitVelocity);
        });

        world.spawnParticle(Particle.ITEM_SLIME, tip, 2, 0.06, 0.06, 0.06, 0.01);
        world.spawnParticle(Particle.BLOCK, tip, 3, 0.08, 0.08, 0.08, 0.02, Material.RED_WOOL.createBlockData());
        world.playSound(tip, Sound.ENTITY_LLAMA_SPIT, 0.65f, 1.1f + ThreadLocalRandom.current().nextFloat() * 0.2f);
    }

    private Vector aimPoint(LivingEntity entity) {
        return entity.getEyeLocation().toVector();
    }

    private boolean hasLineOfSight(LivingEntity entity) {
        return hasLineOfSightFrom(visual.tipLocation(anchor, orientation), entity);
    }

    private boolean hasLineOfSightFrom(Location origin, LivingEntity entity) {
        Vector direction = aimPoint(entity).subtract(origin.toVector());
        double distance = direction.length();
        if (distance < 0.25) {
            return true;
        }
        direction.normalize();
        RayTraceResult trace = world.rayTraceBlocks(
                origin,
                direction,
                distance,
                FluidCollisionMode.NEVER,
                true
        );
        return trace == null;
    }

    private boolean isAimedAt(LivingEntity entity, double maxAngleDegrees) {
        Location tip = visual.tipLocation(anchor, orientation);
        Vector toTarget = aimPoint(entity).subtract(tip.toVector());
        if (toTarget.lengthSquared() < 0.01) {
            return true;
        }
        toTarget.normalize();
        double dot = noseDirection().dot(toTarget);
        return dot >= Math.cos(Math.toRadians(maxAngleDegrees));
    }

    private void despawn(boolean withEffect) {
        if (withEffect) {
            world.spawnParticle(Particle.CLOUD, anchor, 10, 0.5, 0.5, 0.5, 0.02);
            world.spawnParticle(Particle.BLOCK, anchor, 14, 0.6, 0.6, 0.6, 0.05, Material.PINK_WOOL.createBlockData());
            world.spawnParticle(Particle.BLOCK, anchor, 8, 0.4, 0.4, 0.4, 0.04, Material.RED_WOOL.createBlockData());
            world.playSound(anchor, Sound.ENTITY_LLAMA_AMBIENT, 0.4f, 0.8f);
        }
        visual.remove();
    }

    private LivingEntity findNearestVisibleTarget() {
        double radius = config.targetRadius();
        LivingEntity nearest = null;
        double nearestDistanceSquared = radius * radius;

        for (LivingEntity entity : anchor.getNearbyLivingEntities(radius)) {
            if (!isValidTarget(entity)) {
                continue;
            }
            if (!hasLineOfSightFrom(anchor, entity)) {
                continue;
            }
            double distanceSquared = entity.getLocation().distanceSquared(anchor);
            if (distanceSquared <= nearestDistanceSquared) {
                nearestDistanceSquared = distanceSquared;
                nearest = entity;
            }
        }
        return nearest;
    }

    private boolean isValidTarget(LivingEntity entity) {
        if (entity.equals(owner) || entity.isDead() || !entity.isValid()) {
            return false;
        }
        if (entity instanceof Player player) {
            return player.isOnline() && !player.isDead();
        }
        return entity instanceof Villager || entity instanceof Monster;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

}
