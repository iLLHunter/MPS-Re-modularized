package mpsremodularized.common.config;

import mpsremodularized.common.constants.MPSConstants;
import net.neoforged.neoforge.common.ModConfigSpec;

public class PowerFistConfig {
    private final ModConfigSpec.IntValue POWER_FIST_1__INVENTORY_SLOTS;
    private final ModConfigSpec.DoubleValue POWER_FIST_1__MAX_HEAT;
    private final ModConfigSpec.IntValue POWER_FIST_2__INVENTORY_SLOTS;
    private final ModConfigSpec.DoubleValue POWER_FIST_2__MAX_HEAT;
    private final ModConfigSpec.IntValue POWER_FIST_3__INVENTORY_SLOTS;
    private final ModConfigSpec.DoubleValue POWER_FIST_3__MAX_HEAT;
    private final ModConfigSpec.IntValue POWER_FIST_4__INVENTORY_SLOTS;
    private final ModConfigSpec.DoubleValue POWER_FIST_4__MAX_HEAT;

    public PowerFistConfig(ModConfigSpec.Builder builder) {
        // Tier 1
        ModConfigSpec.Builder POWER_FIST_1__SETTINGS_BUILDER = builder.push("Power_Fist").push("Tier_1");
        POWER_FIST_1__INVENTORY_SLOTS = POWER_FIST_1__SETTINGS_BUILDER.comment("Minimum 14 slots: five reserved slots and nine general module slots.")
                .defineInRange(MPSConstants.CONFIG_INVENTORY_SLOTS, 14, 14, 30);
        POWER_FIST_1__MAX_HEAT = POWER_FIST_1__SETTINGS_BUILDER.defineInRange(MPSConstants.MAXIMUM_HEAT, 50.0, 0, 1000.0D);

        // Tier 2
        ModConfigSpec.Builder POWER_FIST_2__SETTINGS_BUILDER = POWER_FIST_1__SETTINGS_BUILDER.pop().push("Tier_2");
        POWER_FIST_2__INVENTORY_SLOTS = POWER_FIST_2__SETTINGS_BUILDER.comment("Minimum 22 slots; smaller saved values correct on server restart.")
                .defineInRange(MPSConstants.CONFIG_INVENTORY_SLOTS, 22, 22, 60);
        POWER_FIST_2__MAX_HEAT = POWER_FIST_2__SETTINGS_BUILDER.defineInRange(MPSConstants.MAXIMUM_HEAT, 100.0, 0, 1000.0D);

        // Tier 3
        ModConfigSpec.Builder POWER_FIST_3__SETTINGS_BUILDER = POWER_FIST_2__SETTINGS_BUILDER.pop().push("Tier_3");
        POWER_FIST_3__INVENTORY_SLOTS = POWER_FIST_3__SETTINGS_BUILDER.defineInRange(MPSConstants.CONFIG_INVENTORY_SLOTS, 30, 0, 100);
        POWER_FIST_3__MAX_HEAT = POWER_FIST_3__SETTINGS_BUILDER.defineInRange(MPSConstants.MAXIMUM_HEAT, 200.0, 0, 1000.0D);

        // Tier 4
        ModConfigSpec.Builder POWER_FIST_4__SETTINGS_BUILDER = POWER_FIST_3__SETTINGS_BUILDER.pop().push("Tier_4");
        POWER_FIST_4__INVENTORY_SLOTS = POWER_FIST_4__SETTINGS_BUILDER.comment("71 base slots, including one added for the Powered Diamond Anvil. Paxel providers and Construction Sticks add separate reserved slots.")
                .defineInRange(MPSConstants.CONFIG_INVENTORY_SLOTS, 71, 0, 180);
        POWER_FIST_4__MAX_HEAT = POWER_FIST_4__SETTINGS_BUILDER.defineInRange(MPSConstants.MAXIMUM_HEAT, 400.0, 0, 1000.0D);
        builder.pop().pop();
    }

    public static int powerFistInventorySlots1;
    public static double powerFistMaxHeat1;
    public static int powerFistInventorySlots2;
    public static double powerFistMaxHeat2;
    public static int powerFistInventorySlots3;
    public static double powerFistMaxHeat3;
    public static int powerFistInventorySlots4;
    public static double powerFistMaxHeat4;

    public int inventorySlots(int tier) {
        ModConfigSpec.IntValue value = switch (tier) {
            case 1 -> POWER_FIST_1__INVENTORY_SLOTS;
            case 2 -> POWER_FIST_2__INVENTORY_SLOTS;
            case 3 -> POWER_FIST_3__INVENTORY_SLOTS;
            case 4 -> POWER_FIST_4__INVENTORY_SLOTS;
            default -> throw new IllegalArgumentException("Unknown Power Fist tier: " + tier);
        };
        return MPSCommonConfig.SERVER_SPEC.isLoaded() ? value.getAsInt() : value.getDefault();
    }

    public void load() {

        // Tier 1
        powerFistInventorySlots1 = POWER_FIST_1__INVENTORY_SLOTS.get();
        powerFistMaxHeat1 = POWER_FIST_1__MAX_HEAT.get();
        // Tier 1
        powerFistInventorySlots2 = POWER_FIST_2__INVENTORY_SLOTS.get();
        powerFistMaxHeat2 = POWER_FIST_2__MAX_HEAT.get();
        // Tier 1
        powerFistInventorySlots3 = POWER_FIST_3__INVENTORY_SLOTS.get();
        powerFistMaxHeat3 = POWER_FIST_3__MAX_HEAT.get();
        // Tier 1
        powerFistInventorySlots4 = POWER_FIST_4__INVENTORY_SLOTS.get();
        powerFistMaxHeat4 = POWER_FIST_4__MAX_HEAT.get();
    }
}
