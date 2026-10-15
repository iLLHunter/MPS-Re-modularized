package mpsremodularized.common.config;

import mpsremodularized.common.constants.MPSConstants;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

public class MPSCommonConfig {
    /**
     * Charging Base ------------------------------------------------------------------------------
     */
    private static final ModConfigSpec.Builder CHARGING_BASE_BUILDER = new ModConfigSpec.Builder().push("ChargingBase settings");
    private static final ModConfigSpec.IntValue CHARGING_BASE_MAX_ENERGY = CHARGING_BASE_BUILDER
        .comment("Max base power for charging base")
        .defineInRange("chargingBaseMaxEnergy", 1000000, 100, 10000000);
    private static final ModConfigSpec.IntValue CHARGING_BASE_MAX_TRANSFER = CHARGING_BASE_BUILDER
        .comment("Max power transfer for charging base")
        .defineInRange("chargingBaseMaxTransfer", 10000, 10, 1000000);

    /**
     * Batteries ----------------------------------------------------------------------------------
     */

    // Basic
    private static final ModConfigSpec.Builder BATTERIES_BUILDER1 = CHARGING_BASE_BUILDER.pop().push("Batteries").push(MPSConstants.MODULE_BATTERY_1__REGNAME);
    private static final ModConfigSpec.IntValue BATTERY_1__MAX_ENERGY = BATTERIES_BUILDER1.defineInRange(MPSConstants.CONFIG_MAX_ENERGY, 1000000, 0, Integer.MAX_VALUE);
    private static final ModConfigSpec.IntValue BATTERY_1__MAX_TRANSFER = BATTERIES_BUILDER1.defineInRange(MPSConstants.CONFIG_MAX_TRANSFER, 1250000, 0, Integer.MAX_VALUE);
    private static final ModConfigSpec.BooleanValue BATTERY_1__IS_ALLOWED = BATTERIES_BUILDER1.define(MPSConstants.CONFIG_IS_ALLOWED, true);

    // Advanced
    private static final ModConfigSpec.Builder BATTERIES_BUILDER2 = BATTERIES_BUILDER1.pop().push(MPSConstants.MODULE_BATTERY_2__REGNAME);
    private static final ModConfigSpec.IntValue BATTERY_2__MAX_ENERGY = BATTERIES_BUILDER2.defineInRange(MPSConstants.CONFIG_MAX_ENERGY, 10000000, 0, Integer.MAX_VALUE);
    private static final ModConfigSpec.IntValue BATTERY_2__MAX_TRANSFER = BATTERIES_BUILDER2.defineInRange(MPSConstants.CONFIG_MAX_TRANSFER, 1250000, 0, Integer.MAX_VALUE);
    private static final ModConfigSpec.BooleanValue BATTERY_2__IS_ALLOWED = BATTERIES_BUILDER2.define(MPSConstants.CONFIG_IS_ALLOWED, true);

    // Elite
    private static final ModConfigSpec.Builder BATTERIES_BUILDER3 = BATTERIES_BUILDER2.pop().push(MPSConstants.MODULE_BATTERY_3__REGNAME);
    private static final ModConfigSpec.IntValue BATTERY_ELITE__MAX_ENERGY = BATTERIES_BUILDER3.defineInRange(MPSConstants.CONFIG_MAX_ENERGY, 100000000, 0, Integer.MAX_VALUE);
    private static final ModConfigSpec.IntValue BATTERY_ELITE__MAX_TRANSFER = BATTERIES_BUILDER3.defineInRange(MPSConstants.CONFIG_MAX_TRANSFER, 1250000, 0, Integer.MAX_VALUE);
    private static final ModConfigSpec.BooleanValue BATTERY_ELITE__IS_ALLOWED = BATTERIES_BUILDER3.define(MPSConstants.CONFIG_IS_ALLOWED, true);

    // Ultimate
    private static final ModConfigSpec.Builder BATTERIES_BUILDER4 = BATTERIES_BUILDER3.pop().push(MPSConstants.MODULE_BATTERY_4__REGNAME);
    private static final ModConfigSpec.IntValue BATTERY_4__MAX_ENERGY = BATTERIES_BUILDER4.defineInRange(MPSConstants.CONFIG_MAX_ENERGY, 1000000000, 0, Integer.MAX_VALUE);
    private static final ModConfigSpec.IntValue BATTERY_4__MAX_TRANSFER = BATTERIES_BUILDER4.defineInRange(MPSConstants.CONFIG_MAX_TRANSFER, 1250000, 0, Integer.MAX_VALUE);
    private static final ModConfigSpec.BooleanValue BATTERY_4__IS_ALLOWED = BATTERIES_BUILDER4.define(MPSConstants.CONFIG_IS_ALLOWED, true);
    private static final ModConfigSpec.Builder BUILDER = BATTERIES_BUILDER4.pop().pop();
    public static int chargingBaseMaxEnergy;
    public static int chargingBaseMaxTransfer;
    public static int batteryMaxEnergy1;
    public static int batteryMaxTransfer1;
    public static boolean batteryIsAllowed1;
    public static int batteryMaxEnergy2;
    public static int batteryMaxTransfer2;
    public static boolean batteryIsAllowed2;
    public static int batteryMaxEnergy3;
    public static int batteryMaxTransfer3;
    public static boolean batteryIsAllowed3;
    public static int batteryMaxEnergy4;
    public static int batteryMaxTransfer4;
    public static boolean batteryIsAllowed4;
    private static final ModConfigSpec.DoubleValue GENERAL_MAX_FLYING_SPEED;

    private static final ArmorConfig ARMOR_SETTINGS;

    private static final PowerFistConfig POWER_FIST_SETTINGS;

    public static final CompatibilityConfig COMPATIBILITY_SETTINGS;

    public static final ModConfigSpec SERVER_SPEC;

    static {
        ModConfigSpec.Builder builder = BUILDER;
        builder.comment("General settings").push("General");
        GENERAL_MAX_FLYING_SPEED = builder.comment("Maximum flight speed (in m/s)")
            .translation(MPSConstants.CONFIG_GENERAL_MAX_FLYING_SPEED)
            .defineInRange("maximumFlyingSpeedmps", 25.0, 0, Float.MAX_VALUE);
        builder.pop();
        ARMOR_SETTINGS = new ArmorConfig(builder);
        POWER_FIST_SETTINGS = new PowerFistConfig(builder);
        COMPATIBILITY_SETTINGS = new CompatibilityConfig(builder);
        SERVER_SPEC = builder.build();
    }

    public static double maxFlyingSpeed;

    public static int getPowerFistInventorySlots(int tier) {
        return POWER_FIST_SETTINGS.inventorySlots(tier);
    }

    public static void onLoad(final ModConfigEvent event) {
        if (event.getConfig().getSpec() == SERVER_SPEC) {
            loadSettings();
        }
    }

    static void loadSettings() {
        chargingBaseMaxEnergy = CHARGING_BASE_MAX_ENERGY.getAsInt();
        chargingBaseMaxTransfer = CHARGING_BASE_MAX_TRANSFER.getAsInt();

        batteryMaxEnergy1 = BATTERY_1__MAX_ENERGY.getAsInt();
        batteryMaxTransfer1 = BATTERY_1__MAX_TRANSFER.getAsInt();
        batteryIsAllowed1 = BATTERY_1__IS_ALLOWED.getAsBoolean();

        batteryMaxEnergy2 = BATTERY_2__MAX_ENERGY.get();
        batteryMaxTransfer2 = BATTERY_2__MAX_TRANSFER.getAsInt();
        batteryIsAllowed2 = BATTERY_2__IS_ALLOWED.getAsBoolean();

        batteryMaxEnergy3 = BATTERY_ELITE__MAX_ENERGY.getAsInt();
        batteryMaxTransfer3 = BATTERY_ELITE__MAX_TRANSFER.getAsInt();
        batteryIsAllowed3 = BATTERY_ELITE__IS_ALLOWED.getAsBoolean();

        batteryMaxEnergy4 = BATTERY_4__MAX_ENERGY.getAsInt();
        batteryMaxTransfer4 = BATTERY_4__MAX_TRANSFER.getAsInt();
        batteryIsAllowed4 = BATTERY_4__IS_ALLOWED.getAsBoolean();

        maxFlyingSpeed = GENERAL_MAX_FLYING_SPEED.get();
        ARMOR_SETTINGS.load();
        POWER_FIST_SETTINGS.load();
    }
}
