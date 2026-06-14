package bm.b0b0b0.soulDart.config.settings;

import net.elytrium.serializer.SerializerConfig;
import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;
import net.elytrium.serializer.annotations.NewLine;
import net.elytrium.serializer.language.object.YamlSerializable;

public class SoulDartSettings extends YamlSerializable {

    private static final SerializerConfig CONFIG = new SerializerConfig.Builder().build();

    @Comment(@CommentValue("Время жизни объекта (тики, 20 = 1 сек)"))
    public int lifetimeTicks = 200;

    @Comment(@CommentValue("Радиус поиска целей (блоки)"))
    public double targetRadius = 25.0;

    @Comment(@CommentValue("Скорость полёта (блоков за тик)"))
    public double flightSpeed = 0.22;

    @Comment(@CommentValue("Множитель скорости при преследовании цели"))
    public double chaseSpeedMultiplier = 1.35;

    @Comment(@CommentValue("Скорость поворота при патруле (градусы/тик)"))
    public double wanderTurnSpeed = 1.6;

    @Comment(@CommentValue("Скорость поворота при захвате цели (градусы/тик)"))
    public double targetTurnSpeed = 14.0;

    @Comment(@CommentValue("Скорость поворота носа по pitch при захвате цели (градусы/тик)"))
    public double targetPitchSpeed = 14.0;

    @Comment(@CommentValue("Инерция хвоста: меньше = зад тяжелее, траектория плавнее"))
    public double velocityInertia = 0.055;

    @Comment(@CommentValue("Инерция при преследовании (выше = быстрее догоняет цель)"))
    public double chaseVelocityInertia = 0.11;

    @Comment(@CommentValue("Желаемая дистанция до цели (блоки)"))
    public double combatDistance = 8.0;

    @Comment(@CommentValue("Зона удержания дистанции ± блоки"))
    public double combatDistanceMargin = 2.5;

    @Comment(@CommentValue("Длительность сборки из блоков (тики)"))
    public int assemblyDurationTicks = 40;

    @Comment(@CommentValue("Амплитуда вертикального покачивания"))
    public double bobAmplitude = 0.12;

    @Comment(@CommentValue("Амплитуда бокового покачивания"))
    public double swayAmplitude = 0.08;

    @Comment(@CommentValue("Плевков в одном залпе"))
    public int burstSize = 4;

    @Comment(@CommentValue("Пауза между плевками в залпе (тики)"))
    public int burstShotDelay = 2;

    @Comment(@CommentValue("Мин. перезарядка залпа (тики)"))
    public int burstCooldownMin = 30;

    @Comment(@CommentValue("Макс. перезарядка залпа (тики)"))
    public int burstCooldownMax = 50;

    @Comment(@CommentValue("Угол наведения для начала залпа (градусы)"))
    public double burstAimAngle = 22.0;

    @Comment(@CommentValue("Скорость плевка"))
    public double spitSpeed = 1.6;

    @NewLine
    @Comment(@CommentValue("Permission для команды /souldart"))
    public String permission = "souldart.use";

    @Comment(@CommentValue("Локаль сообщений: ru или en"))
    public String language = "ru";

    public SoulDartSettings() {
        super(CONFIG);
    }

}
