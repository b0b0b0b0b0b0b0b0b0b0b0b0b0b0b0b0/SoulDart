package bm.b0b0b0.soulDart.dart;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.util.Vector;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class DartVisual {

    static final SegmentSpec[] SEGMENTS = {
            new SegmentSpec(1, -1, Material.PINK_WOOL),
            new SegmentSpec(-1, -1, Material.PINK_WOOL),
            new SegmentSpec(0, -1, Material.PINK_WOOL),
            new SegmentSpec(0, 0, Material.PINK_WOOL),
            new SegmentSpec(0, 1, Material.RED_WOOL),
    };

    private static final Vector MODEL_FORWARD = modelForwardAtRest();

    private final List<BlockDisplay> displays = new ArrayList<>(SEGMENTS.length);
    private final Matrix4f[] segmentMatrices = new Matrix4f[SEGMENTS.length];
    private final SegmentSpec tipSegment = SEGMENTS[SEGMENTS.length - 1];

    public DartVisual(World world, Location pivot) {
        for (int index = 0; index < SEGMENTS.length; index++) {
            SegmentSpec segment = SEGMENTS[index];
            segmentMatrices[index] = localMatrix(segment.lateral, segment.forward);
            BlockDisplay display = world.spawn(pivot, BlockDisplay.class, entity -> {
                entity.setBlock(segment.material.createBlockData());
                entity.setBillboard(Display.Billboard.FIXED);
                entity.setPersistent(false);
                entity.setGravity(false);
                entity.setInterpolationDuration(0);
                entity.setTeleportDuration(0);
            });
            displays.add(display);
        }
    }

    public Matrix4f[] createAssemblyStartMatrices() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        Matrix4f[] matrices = new Matrix4f[SEGMENTS.length];
        for (int index = 0; index < SEGMENTS.length; index++) {
            float scatterX = random.nextFloat(-5f, 5f);
            float scatterY = random.nextFloat(-2.5f, 3.5f);
            float scatterZ = random.nextFloat(-5f, 5f);
            matrices[index] = new Matrix4f()
                    .rotateXYZ(
                            (float) Math.toRadians(random.nextFloat(-180f, 180f)),
                            (float) Math.toRadians(random.nextFloat(-180f, 180f)),
                            (float) Math.toRadians(random.nextFloat(-180f, 180f))
                    )
                    .translate(scatterX, scatterY, scatterZ)
                    .translate(-0.5f, -0.5f, -0.5f);
        }
        return matrices;
    }

    public void updateAssembly(Location pivot, float yaw, float progress, int spinTick, Matrix4f[] startMatrices) {
        Matrix4f rotation = groupRotation(yaw, 0f);
        float spinStrength = (1f - progress) * (float) Math.toRadians(22f);

        for (int index = 0; index < displays.size(); index++) {
            Matrix4f end = new Matrix4f(rotation).mul(segmentMatrices[index]);
            Matrix4f current = new Matrix4f(startMatrices[index]).lerp(end, progress);
            if (spinStrength > 0.001f) {
                current.rotateY(spinStrength * spinTick);
            }
            BlockDisplay display = displays.get(index);
            display.setInterpolationDuration(2);
            display.setInterpolationDelay(0);
            display.teleport(pivot);
            display.setTransformationMatrix(current);
        }
    }

    public void update(Location pivot, Quaternionf orientation) {
        Matrix4f rotation = matrixFromOrientation(orientation);

        for (int index = 0; index < displays.size(); index++) {
            BlockDisplay display = displays.get(index);
            Matrix4f matrix = new Matrix4f(rotation).mul(segmentMatrices[index]);
            display.setInterpolationDuration(1);
            display.setInterpolationDelay(0);
            display.teleport(pivot);
            display.setTransformationMatrix(matrix);
        }
    }

    public void update(Location pivot, float yaw, float pitch) {
        update(pivot, orientationFromYawPitch(yaw, pitch));
    }

    public Location tipLocation(Location pivot, Quaternionf orientation) {
        Vector3f localNose = new Vector3f(tipSegment.lateral + 0.5f, 0f, tipSegment.forward);
        orientation.transform(localNose);
        return pivot.clone().add(localNose.x, localNose.y, localNose.z);
    }

    public Location tipLocation(Location pivot, float yaw, float pitch) {
        return tipLocation(pivot, orientationFromYawPitch(yaw, pitch));
    }

    public void remove() {
        for (BlockDisplay display : displays) {
            display.remove();
        }
        displays.clear();
    }

    public static Vector directionFromOrientation(float yaw, float pitch) {
        return directionFromOrientation(orientationFromYawPitch(yaw, pitch));
    }

    public static Vector directionFromOrientation(Quaternionf orientation) {
        Vector3f localNose = new Vector3f(
                tipSegmentStatic().lateral + 0.5f,
                0f,
                tipSegmentStatic().forward
        );
        orientation.transform(localNose);
        return new Vector(localNose.x, localNose.y, localNose.z).normalize();
    }

    public static Vector lateralFromOrientation(Quaternionf orientation) {
        Vector forward = directionFromOrientation(orientation);
        Vector lateral = forward.clone().crossProduct(new Vector(0, 1, 0));
        if (lateral.lengthSquared() < 0.0001) {
            return new Vector(1, 0, 0);
        }
        return lateral.normalize();
    }

    public static Vector lateralFromOrientation(float yaw, float pitch) {
        return lateralFromOrientation(orientationFromYawPitch(yaw, pitch));
    }

    public static Quaternionf orientationFromDirection(Vector direction) {
        Vector normalized = direction.clone();
        if (normalized.lengthSquared() < 0.0001) {
            return new Quaternionf();
        }
        normalized.normalize();
        return new Quaternionf().rotationTo(
                (float) MODEL_FORWARD.getX(),
                (float) MODEL_FORWARD.getY(),
                (float) MODEL_FORWARD.getZ(),
                (float) normalized.getX(),
                (float) normalized.getY(),
                (float) normalized.getZ()
        );
    }

    public static Quaternionf rotateTowards(Quaternionf current, Vector desiredDirection, float maxStepDegrees) {
        Vector desired = desiredDirection.clone();
        if (desired.lengthSquared() < 0.0001) {
            return new Quaternionf(current);
        }
        desired.normalize();

        Vector currentForward = directionFromOrientation(current);
        double dot = clamp(currentForward.dot(desired), -1.0, 1.0);
        if (dot > 0.9995) {
            return orientationFromDirection(desired);
        }

        double angle = Math.acos(dot);
        Quaternionf target = orientationFromDirection(desired);
        float blend = (float) Math.min(1.0, Math.toRadians(maxStepDegrees) / angle);
        return new Quaternionf(current).slerp(target, blend);
    }

    public static Quaternionf orientationFromYawPitch(float yaw, float pitch) {
        double yawRad = Math.toRadians(yaw);
        double pitchRad = Math.toRadians(pitch);
        double cosPitch = Math.cos(pitchRad);
        Vector forward = new Vector(
                -Math.sin(yawRad) * cosPitch,
                -Math.sin(pitchRad),
                Math.cos(yawRad) * cosPitch
        ).normalize();
        return orientationFromDirection(forward);
    }

    public static Matrix4f matrixFromOrientation(Quaternionf orientation) {
        return new Matrix4f().rotate(orientation);
    }

    public static Matrix4f groupRotation(float yaw, float pitch) {
        return matrixFromOrientation(orientationFromYawPitch(yaw, pitch));
    }

    private static Vector modelForwardAtRest() {
        SegmentSpec tip = SEGMENTS[SEGMENTS.length - 1];
        return new Vector(tip.lateral + 0.5, 0, tip.forward).normalize();
    }

    private static SegmentSpec tipSegmentStatic() {
        return SEGMENTS[SEGMENTS.length - 1];
    }

    private static Matrix4f localMatrix(int lateral, int forward) {
        return new Matrix4f()
                .translate(lateral, 0f, forward)
                .translate(-0.5f, -0.5f, -0.5f);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    record SegmentSpec(int forward, int lateral, Material material) {
    }

}
