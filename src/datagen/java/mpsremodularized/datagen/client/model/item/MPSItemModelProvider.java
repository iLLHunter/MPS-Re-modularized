package mpsremodularized.datagen.client.model.item;

import java.util.Objects;
import mpsremodularized.common.constants.MPSConstants;
import mpsremodularized.common.registration.MPSEntities;
import mpsremodularized.common.registration.MPSItems;
import mpsremodularized.datagen.client.model.item.AbstractItemModelProvider;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.neoforge.client.model.generators.CustomLoaderBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;


public class MPSItemModelProvider extends AbstractItemModelProvider {
    public MPSItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, MPSConstants.MOD_ID, existingFileHelper);
    }

    ItemModelBuilder LUX_CAP_MODEL_ITEM;

    @Override
    protected void registerModels() {
        registerComponentModels();
        registerBlockItemModels();
        registerModularItemModels();
        registerModuleModels();
    }

    protected void registerBlockItemModels() {
        // Block Items ============================================================
        itemEntity(MPSItems.TINKER_TABLE_ITEM.get())
            .guiLight(BlockModel.GuiLight.FRONT)
            .transforms()
            .transform(ItemDisplayContext.THIRD_PERSON_LEFT_HAND)
            .rotation( 75, 45, 0 )
            .translation( 0, 2.5F, 0)
            .scale(0.38F).end()

            .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)
            .rotation( 75, 45, 0 )
            .translation( 0, 2.5F, 0)
            .scale(0.38F).end()

            .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND)
            .rotation( 0, 225, 0 )
            .translation( 0, 0, 0)
            .scale(0.4F).end()

            .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND)
            .rotation( 0, 45, 0 )
            .translation( 0, 0, 0)
            .scale(0.4F).end()

            .transform(ItemDisplayContext.GUI)
            .rotation( 30, 225, 0 )
            .translation( 0, 0, 0)
            .scale(0.63F).end()

            .transform(ItemDisplayContext.FIXED)
            .rotation(0, 0, 0)
            .translation( 0, 0, 0)
            .scale(0.5F).end()

            .transform(ItemDisplayContext.GROUND)
            .rotation(0, 0, 0)
            .translation( 0, 3, 0)
            .scale(0.25F).end();

        LUX_CAP_MODEL_ITEM = blockItemEntity(MPSItems.LUX_CAPACITOR_ITEM.get(), modLoc("block/lux_capacitor"))
            .transforms()
            .transform(ItemDisplayContext.THIRD_PERSON_LEFT_HAND)
            .rotation(0, 0, 45)
            .translation(0, 2, 3)
            .scale(0.5F).end()

            .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)
            .rotation(0, 0, 45)
            .translation(0, 2, 3)
            .scale(0.5F).end()

            .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND)
            .rotation(-25, -90, 0)
            .translation(1.13F, 3.2F, 1.13F)
            .scale(0.68F).end()

            .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND)
            .rotation(0, -90, 25)
            .translation(1.13F, 3.2F, 1.13F)
            .scale(0.68F).end()

            .transform(ItemDisplayContext.GUI)
            .rotation(-45, 0, 45)
            .translation(0, 2.75F, 0)
            .scale(0.625F).end()

            .transform(ItemDisplayContext.FIXED)
            .rotation(0, 180, 0)
            .scale(1).end()

            .transform(ItemDisplayContext.GROUND)
            .rotation(-90, 0, 0)
            .translation(0, 2, 0)
            .scale(0.5F).end().end();


        // Append this inside your registerModels() method
        getBuilder(MPSEntities.PLASMA_BALL_ENTITY_TYPE.get())
            .parent(new ModelFile.UncheckedModelFile("builtin/entity"))
            .texture("particle", ResourceLocation.fromNamespaceAndPath(MPSConstants.MOD_ID, "block/white"))
            .customLoader((parent, existingFileHelper) ->
                new CustomLoaderBuilder<>(ResourceLocation.fromNamespaceAndPath(MPSConstants.MOD_ID, "obj"), parent, existingFileHelper, false) {
                    @Override
                    public com.google.gson.JsonObject toJson(com.google.gson.JsonObject json) {
                        super.toJson(json);
                        json.addProperty("model", MPSConstants.MOD_ID + ":models/entity/obj/sphere.obj");
                        json.addProperty("flip_v", true);
                        return json;
                    }
                }
            );
    }


    void applyColor(ItemModelBuilder builder, int tier) {
        String color = switch (tier) {
            case 2 -> "red";
            case 3 -> "blue";
            case 4 -> "purple";
            default -> "green";
        };

        builder
            .customLoader((parent, existingFileHelper) ->
                new net.neoforged.neoforge.client.model.generators.CustomLoaderBuilder<>(
                    ResourceLocation.fromNamespaceAndPath("neoforge", "elements"), parent, existingFileHelper, false) {
                    @Override
                    public com.google.gson.JsonObject toJson(com.google.gson.JsonObject json) {
                        super.toJson(json);

                        //                        // Set your standard generated layers
                        //                        com.google.gson.JsonObject textures = new com.google.gson.JsonObject();
                        //                        textures.addProperty("layer0", "minecraft:item/water_bucket");
                        //                        json.add("textures", textures);

                        // Force a static hardcoded hex tint onto the model natively
                        com.google.gson.JsonObject extraData = new com.google.gson.JsonObject();
                        extraData.addProperty("color", "0xFF9C27B0"); // Plasma Purple
                        json.add("neoforge_data", extraData);

                        return json;
                    }
                }
            );
    }


    protected void registerModularItemModels() {
        // Power Fist =============================================================
        powerFist(MPSItems.POWER_FIST_1.get(), "item/tool/handitem");
        powerFist(MPSItems.POWER_FIST_2.get(), "item/tool/handitem");
        powerFist(MPSItems.POWER_FIST_3.get(), "item/tool/handitem");
        powerFist(MPSItems.POWER_FIST_4.get(), "item/tool/handitem");
        // TODO: check for:
        //            "parent": "item/generated",
        //            "textures": {
        //            "layer0": "mpsremodularized:item/tool/handitem"

        // Armor ==================================================================
        this.basicItem(MPSItems.POWER_ARMOR_HELMET_1.get(), "item/armor/powerarmor.head");
        this.basicItem(MPSItems.POWER_ARMOR_HELMET_2.get(), "item/armor/powerarmor.head");
        this.basicItem(MPSItems.POWER_ARMOR_HELMET_3.get(), "item/armor/powerarmor.head");
        this.basicItem(MPSItems.POWER_ARMOR_HELMET_4.get(), "item/armor/powerarmor.head");

        this.basicItem(MPSItems.POWER_ARMOR_CHESTPLATE_1.get(), "item/armor/powerarmor.torso");
        this.basicItem(MPSItems.POWER_ARMOR_CHESTPLATE_2.get(), "item/armor/powerarmor.torso");
        this.basicItem(MPSItems.POWER_ARMOR_CHESTPLATE_3.get(), "item/armor/powerarmor.torso");
        this.basicItem(MPSItems.POWER_ARMOR_CHESTPLATE_4.get(), "item/armor/powerarmor.torso");

        this.basicItem(MPSItems.POWER_ARMOR_LEGGINGS_1.get(), "item/armor/powerarmor.legs");
        this.basicItem(MPSItems.POWER_ARMOR_LEGGINGS_2.get(), "item/armor/powerarmor.legs");
        this.basicItem(MPSItems.POWER_ARMOR_LEGGINGS_3.get(), "item/armor/powerarmor.legs");
        this.basicItem(MPSItems.POWER_ARMOR_LEGGINGS_4.get(), "item/armor/powerarmor.legs");

        this.basicItem(MPSItems.POWER_ARMOR_BOOTS_1.get(), "item/armor/powerarmor.feet");
        this.basicItem(MPSItems.POWER_ARMOR_BOOTS_2.get(), "item/armor/powerarmor.feet");
        this.basicItem(MPSItems.POWER_ARMOR_BOOTS_3.get(), "item/armor/powerarmor.feet");
        this.basicItem(MPSItems.POWER_ARMOR_BOOTS_4.get(), "item/armor/powerarmor.feet");
    }

    protected void registerModuleModels() {
        this.basicItem(MPSItems.SHOCK_MODULE.get(), "item/module/environmental/shock_module");
        this.basicItem(MPSItems.BLOCK_REACH_MODULE.get(), "item/module/environmental/block_reach");
        this.basicItem(MPSItems.SHOCK_COIL.get(), "item/component/shock_coil");
        for (var entry : mpsremodularized.common.compat.IntermodModules.ENTRIES) {
            getBuilder(entry.id()).parent(new ModelFile.UncheckedModelFile("builtin/entity"));
        }
        // Modules ================================================================
        // Armor ----------------------------------------------
        this.basicItem(MPSItems.IRON_PLATING_MODULE.get(), "item/module/armor/plating.iron");
        this.basicItem(MPSItems.DIAMOND_PLATING_MODULE.get(), "item/module/armor/plating.diamond");
        this.basicItem(MPSItems.NETHERITE_PLATING_MODULE.get(), "item/module/armor/plating.netherite");
        this.basicItem(MPSItems.ENERGY_SHIELD_MODULE.get(), "item/module/armor/energy_shield");

        //Cosmetic --------------------------------------------
        this.basicItem(MPSItems.TRANSPARENT_ARMOR_MODULE.get(), "item/module/cosmetic/transparent_armor");

        // Energy Generation --------------------------------------------------------------------------
        // Thermal
        this.basicItem(MPSItems.THERMAL_GENERATOR_MODULE_1.get(), "item/module/energy/generation/thermalgenerator1");
        this.basicItem(MPSItems.THERMAL_GENERATOR_MODULE_2.get(), "item/module/energy/generation/thermalgenerator2");
        this.basicItem(MPSItems.THERMAL_GENERATOR_MODULE_3.get(), "item/module/energy/generation/thermalgenerator3");
        this.basicItem(MPSItems.THERMAL_GENERATOR_MODULE_4.get(), "item/module/energy/generation/thermalgenerator4");

        // Solar
        this.basicItem(MPSItems.SOLAR_GENERATOR_MODULE_1.get(), "item/module/energy/generation/generator.solar1");
        this.basicItem(MPSItems.SOLAR_GENERATOR_MODULE_2.get(), "item/module/energy/generation/generator.solar2");
        this.basicItem(MPSItems.SOLAR_GENERATOR_MODULE_3.get(), "item/module/energy/generation/generator.solar3");
        this.basicItem(MPSItems.SOLAR_GENERATOR_MODULE_4.get(), "item/module/energy/generation/generator.solar4");

        // Environmental --------------------------------------
        this.basicItem(MPSItems.ACTIVE_CAMOUFLAGE_MODULE.get(), "item/module/environmental/active_camouflage");
        this.basicItem(MPSItems.AUTO_FEEDER_MODULE.get(), "item/module/environmental/auto_feeder");

        this.basicItem(MPSItems.COOLANT_TANK_MODULE_1.get(), "item/module/environmental/primitive_coolant_tank");
        this.basicItem(MPSItems.COOLANT_TANK_MODULE_2.get(), "item/module/environmental/prototype_coolant_tank");
        this.basicItem(MPSItems.COOLANT_TANK_MODULE_3.get(), "item/module/environmental/prototype_coolant_tank_mkii");
        this.basicItem(MPSItems.COOLANT_TANK_MODULE_4.get(), "item/module/environmental/coolant_tank");

        this.basicItem(MPSItems.MAGNET_MODULE.get(), "item/module/environmental/magnet");
        this.basicItem(MPSItems.MOB_REPULSOR_MODULE.get(), "item/module/environmental/mob_repulsor");
        this.basicItemMC(MPSItems.PIGLIN_PACIFICATION_MODULE.get(), "item/gold_nugget");
        this.basicItem(MPSItems.WATER_ELECTROLYZER_MODULE.get(), "item/module/environmental/water_electrolyzer");

        // Mining Enchantment ---------------------------------
        this.basicItem(MPSItems.AQUA_AFFINITY_MODULE.get(), "item/module/mining_enchantment/aqua_affinity");
        this.basicItem(MPSItems.FORTUNE_MODULE.get(), "item/module/mining_enchantment/fortune");
        this.basicItem(MPSItems.SILK_TOUCH_MODULE.get(), "item/module/mining_enchantment/silk");

        // Mining Enhancement ---------------------------------
        this.basicItem(MPSItems.TUNNEL_BORE_MODULE.get(), "item/module/mining_enhancement/tunnel_bore");
        this.basicItemMC(MPSItems.VEIN_MINER_MODULE.get(), "item/golden_pickaxe");
        this.basicItem(MPSItems.SELECTIVE_MINER_MODULE.get(), "item/module/mining_enhancement/selective_miner");

        // Movement -------------------------------------------
        this.basicItem(MPSItems.BLINK_DRIVE_MODULE.get(), "item/module/movement/blink_drive");
        this.basicItem(MPSItems.UPHILL_STEP_ASSIST_MODULE.get(), "item/module/movement/uphill_step_assist");
        this.basicItem(MPSItems.DIMENSIONAL_TEAR_MODULE.get(), "item/module/movement/dim_tear_gen");
        this.basicItem(MPSItems.FLIGHT_CONTROL_MODULE.get(),  "item/module/movement/flight_control");
        this.basicItem(MPSItems.GLIDER_MODULE.get(), "item/module/movement/glider");
        this.basicItem(MPSItems.JET_BOOTS_MODULE.get(), "item/module/movement/jet_boots");
        this.basicItem(MPSItems.JETPACK_MODULE.get(), "item/module/movement/jetpack");
        this.basicItem(MPSItems.JUMP_ASSIST_MODULE.get(), "item/module/movement/jump_assist");
        this.basicItem(MPSItems.SHOCK_ABSORBER_MODULE.get(), "item/module/movement/shock_absorber");
        this.basicItem(MPSItems.SPRINT_ASSIST_MODULE.get(), "item/module/movement/sprint_assist");
        this.basicItem(MPSItems.SWIM_BOOST_MODULE.get(), "item/module/movement/swim_boost");

        // Tools ----------------------------------------------
        // Axe
        this.basicItemMC(MPSItems.STONE_AXE_MODULE.get(), "item/stone_axe");
        this.basicItemMC(MPSItems.IRON_AXE_MODULE.get(), "item/iron_axe");
        this.basicItemMC(MPSItems.DIAMOND_AXE_MODULE.get(), "item/diamond_axe");
        this.basicItemMC(MPSItems.NETHERITE_AXE_MODULE.get(), "item/netherite_axe");

        // Shovel
        this.basicItemMC(MPSItems.STONE_SHOVEL_MODULE.get(), "item/stone_shovel");
        this.basicItemMC(MPSItems.IRON_SHOVEL_MODULE.get(), "item/iron_shovel");
        this.basicItemMC(MPSItems.DIAMOND_SHOVEL_MODULE.get(), "item/diamond_shovel");
        this.basicItemMC(MPSItems.NETHERITE_SHOVEL_MODULE.get(), "item/netherite_shovel");

        // Farming --------------------------------------------
        this.basicItemMC(MPSItems.STONE_ROTOTILLER_MODULE.get(), "item/stone_hoe");
        this.basicItemMC(MPSItems.IRON_ROTOTILLER_MODULE.get(), "item/iron_hoe");
        this.basicItemMC(MPSItems.DIAMOND_ROTOTILLER_MODULE.get(), "item/diamond_hoe");
        this.basicItemMC(MPSItems.NETHERITE_ROTOTILLER_MODULE.get(), "item/netherite_hoe");

        // Shears
        this.basicItem(MPSItems.SHEARS_MODULE.get(), "item/module/tool/shears");

        // PickAxe --------------------------------------------
        this.basicItemMC(MPSItems.STONE_PICKAXE_MODULE.get(), "item/stone_pickaxe");
        this.basicItemMC(MPSItems.IRON_PICKAXE_MODULE.get(), "item/iron_pickaxe");
        this.basicItemMC(MPSItems.DIAMOND_PICKAXE_MODULE.get(), "item/diamond_pickaxe");
        this.basicItemMC(MPSItems.NETHERITE_PICKAXE_MODULE.get(), "item/netherite_pickaxe");

        // Misc -----------------------------------------------
        this.basicItemMC(MPSItems.FLINT_AND_STEEL_MODULE.get(), "item/flint_and_steel");
        this.basicItem(MPSItems.UNIVERSAL_WRENCH_MODULE.get(), "item/component/wrench_module_frame");
        this.basicItem(MPSItems.LEAF_BLOWER_MODULE.get(),  "item/module/tool/leaf_blower");
        this.basicItem(MPSItems.LUX_CAPACITOR_MODULE.get(),  "block/luxlight").parent(LUX_CAP_MODEL_ITEM);

        // Vision ---------------------------------------------
        this.basicItem(MPSItems.BINOCULARS_MODULE.get(), "item/module/vision/binoculars");
        this.basicItem(MPSItems.NIGHTVISION_MODULE.get(), "item/module/vision/night_vision");

        // Weapon ---------------------------------------------
        this.basicItem(MPSItems.BLADE_LAUNCHER_MODULE.get(),  "item/module/weapon/spinningblade");
        this.basicItem(MPSItems.LIGHTNING_SUMMONER_MODULE.get(), "item/module/weapon/lightning");
        this.basicItem(MPSItems.MELEE_ASSIST_MODULE.get(), "item/module/weapon/melee_assist");
        this.basicItem(MPSItems.PLASMA_CANNON_MODULE.get(), "item/module/weapon/plasma_cannon");
        this.basicItem(MPSItems.RAILGUN_MODULE.get(), "item/module/weapon/railgun");
    }



    protected void powerFist(Item item, String texture) {
        blockItemEntity(item,  Objects.requireNonNull(mcLoc("item/generated")))
            .texture("layer0", modLoc(texture))
            .transforms()

            .transform(ItemDisplayContext.THIRD_PERSON_LEFT_HAND)
            .rotation(165, 0, 0)
            .translation(-5.36F, -6.56F, -2.46F)
            .scale(0.67F).end()

            .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)
            .rotation(165, 0, 0)
            .translation(5.36F, -6.56F, -2.46F)
            .scale(0.67F).end()

            .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND)
            .rotation(176, 353, 0)
            .translation(-4.31F, -0.25F, -0.08F)
            .scale(0.4F).end()

            .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND)
            .rotation(176, 353, 0)
            .translation(1.75F, -0.25F, -0.75F)
            .scale(0.4F).end()

            .transform(ItemDisplayContext.GUI)
            .rotation(0, 0, 0)
            .translation(0, 0, 0)
            .scale(1).end()

            .transform(ItemDisplayContext.FIXED)
            .rotation(105, 180, 0)
            .translation(-5.25F, 0.75F, 3.50F)
            .scale(0.6F).end()

            .transform(ItemDisplayContext.GROUND)
            .rotation(160, 0, 0)
            .translation(2, 1, -1.75F)
            .scale(0.25F).end();
    }

    ItemModelBuilder getBuilder(EntityType entity) {
        return getBuilder(BuiltInRegistries.ENTITY_TYPE.getKey(entity).getPath());
    }

    private void registerComponentModels() {
        itemEntity(MPSItems.ARMOR_STAND_ITEM.get())
            .transforms()
            .transform(ItemDisplayContext.THIRD_PERSON_LEFT_HAND)
            .rotation(70, 0, 0)
            .translation(-50.5f, 1, 18.25f)
            .scale(0.62f, 0.62f, 0.62f)
            .end()

            .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)
            .rotation(70, 0, 0)
            .translation(4.25F, 1, 18.25F)
            .scale(0.62f, 0.62f, 0.62f)
            .end()

            .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND)
            .rotation(0, 0, 352)
            .translation(3.75F, 11.5F, 0)
            .scale(0.42f, 0.4f, 0.4f)
            .end()

            .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND)
            .rotation(0, 0, 352)
            .translation(11.5F, 11.5F, 0)
            .scale(0.42f, 0.4f, 0.4f)
            .end()

            .transform(ItemDisplayContext.GUI)
            .rotation(0, 0, 316)
            .translation(22.5F, 12, 0)
            .scale(1, 1, 1)
            .end()

            .transform(ItemDisplayContext.GROUND)
            .rotation(0, 0, 0)
            .translation( 0, 18.75F, 0)
            .scale(0.62F, 0.62F, 0.62F)
            .end()

            .transform(ItemDisplayContext.FIXED)
            .rotation(0, 180, 0)
            .translation(-6.25F, 17.75F, -6.50F)
            .scale( 0.75F, 0.75F, 0.75F)
            .end();

        // Batteries ----------------------------------------------------------------------------------------
        batteryItem(MPSItems.BATTERY_1.get(), "basic");
        batteryItem(MPSItems.BATTERY_2.get(), "advanced");
        batteryItem(MPSItems.BATTERY_3.get(), "elite");
        batteryItem(MPSItems.BATTERY_4.get(), "ultimate");

        // BlockItem ----------------------------------------------------------------------------------------
        blockItemEntity(MPSItems.CHARGING_BASE_ITEM.get(),
            Objects.requireNonNull(modLoc("block/base_unpowered")))// texture location
            .transforms()
            .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND)
            .rotation(75, 315, 0)
            .translation(0, 2.5F, 0)
            .scale( 0.375F, 0.375F, 0.375F)
            .end()

            .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND)
            .rotation( 0, 315, 0 )
            .translation(0, 2.5F, 0)
            .scale(0.4F, 0.4F, 0.4F)
            .end()

            .transform(ItemDisplayContext.GUI)
            .rotation(30, 45, 0)
            .translation(0, 0, 0)
            .scale(0.625F, 0.625F, 0.625F)
            .end()

            .transform(ItemDisplayContext.GROUND)
            .rotation(0, 0, 0)
            .translation( 0, 3, 0)
            .scale(0.25F,  0.25F, 0.25F)
            .end()

            .transform(ItemDisplayContext.FIXED)
            .rotation(0, 180, 0)
            .translation(0, 0, 0)
            .scale( 1, 1, 1)
            .end();

        // Components ---------------------------------------------------------------------------------------
        componentItem(MPSItems.ARTIFICIAL_MUSCLE.get(), "artificialmuscle");
        componentItem(MPSItems.CAPACITOR_1.get(), "capacitor_1");
        componentItem(MPSItems.CAPACITOR_2.get(), "capacitor_2");
        componentItem(MPSItems.CAPACITOR_3.get(), "capacitor_3");
        componentItem(MPSItems.CAPACITOR_4.get(), "capacitor_4");
        componentItem(MPSItems.CARBON_MYOFIBER.get(), "myofiber");
        componentItem(MPSItems.COMPUTER_CHIP.get(), "computerchip");
        componentItem(MPSItems.CONTROL_CIRCUIT_1.get(), "controlcircuit_1");
        componentItem(MPSItems.CONTROL_CIRCUIT_2.get(), "controlcircuit_2");
        componentItem(MPSItems.CONTROL_CIRCUIT_3.get(), "controlcircuit_3");
        componentItem(MPSItems.CONTROL_CIRCUIT_4.get(), "controlcircuit_4");
        componentItem(MPSItems.FORCE_FIELD_EMITTER.get(), "fieldemitter");
        componentItem(MPSItems.GLIDER_WING.get(), "gliderwing");
        componentItem(MPSItems.ION_THRUSTER.get(), "ionthruster");
        componentItem(MPSItems.HOLOGRAM_EMITTER.get(), "hologramemitter");
        componentItem(MPSItems.MAGNET.get(), "magnetb");
        componentItem(MPSItems.MYOFIBER_GEL.get(), "paste");
        componentItem(MPSItems.PLATING_DIAMOND.get(), "diamond_plate");
        componentItem(MPSItems.PLATING_IRON.get(), "iron_plate");
        componentItem(MPSItems.PLATING_NETHERITE.get(), "netherite_plate");
        componentItem(MPSItems.RUBBER_HOSE.get(), "rubberhose");
        componentItem(MPSItems.SERVO.get(), "servo");
        componentItem(MPSItems.WRENCH_MODULE_FRAME.get(), "wrench_module_frame");
        componentItem(MPSItems.SOLAR_PANEL.get(), "solarpanel");
        componentItem(MPSItems.SOLENOID.get(), "solenoid");
        componentItem(MPSItems.WIRING_STANDARD.get(), "wiring_copper");
        componentItem(MPSItems.WIRING_HIGH_CONDUCTIVITY.get(), "wiring_gold");
        basicItemMC(MPSItems.COMPONENT_MOLD.get(), "block/iron_trapdoor");
        basicItemMC(MPSItems.WIRING_TEMPLATE.get(), "item/paper").texture("layer1", modLoc("item/component/wiring_copper"));
        basicItemMC(MPSItems.HIGH_CONDUCTIVITY_WIRING_TEMPLATE.get(), "item/paper").texture("layer1", modLoc("item/component/wiring_gold"));
        basicItemMC(MPSItems.SOLENOID_TEMPLATE.get(), "item/paper").texture("layer1", modLoc("item/component/solenoid"));
        componentItem(MPSItems.UNFINISHED_HOLOGRAM_EMITTER.get(), "hologramemitter").texture("layer1", mcLoc("item/iron_nugget"));
        basicItem(MPSItems.UNFINISHED_PLASMA_CANNON.get(), "item/module/weapon/plasma_cannon").texture("layer1", mcLoc("item/iron_nugget"));
        basicItem(MPSItems.UNFINISHED_RAILGUN.get(), "item/module/weapon/railgun").texture("layer1", mcLoc("item/iron_nugget"));
        basicItemMC(MPSItems.PRIMITIVE_HELMET_FRAME.get(), "item/leather_helmet");
        basicItemMC(MPSItems.PRIMITIVE_CHESTPLATE_FRAME.get(), "item/leather_chestplate");
        basicItemMC(MPSItems.PRIMITIVE_LEGGINGS_FRAME.get(), "item/leather_leggings");
        basicItemMC(MPSItems.PRIMITIVE_BOOTS_FRAME.get(), "item/leather_boots");
        componentItem(MPSItems.PRIMITIVE_POWERFIST_FRAME.get(), "servo");

        // Smithing Templates -------------------------------------------------------------------------------
        upgrade(MPSItems.TIER_2_SMITHING_UPGRADE.get(), "tier_2_smithing_upgrade");
        upgrade(MPSItems.TIER_3_SMITHING_UPGRADE.get(), "tier_3_smithing_upgrade");
        upgrade(MPSItems.TIER_4_SMITHING_UPGRADE.get(), "tier_4_smithing_upgrade");
        }

    public ItemModelBuilder batteryItem(Item item, String texturePath) {
        return this.basicItem(Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(item)), "item/battery/" + texturePath);
    }

    public ItemModelBuilder componentItem(Item item, String texturePath) {
        return this.basicItem(Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(item)),"item/component/" + texturePath);
    }

    public ItemModelBuilder upgrade(Item item, String texturePath) {
        return this.basicItem(Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(item)),"item/upgrade/" + texturePath);
    }

}
