package mpsremodularized;

import mpsremodularized.client.sound.MPSSoundDictionary;
import mpsremodularized.common.constants.MPSConstants;
import mpsremodularized.common.entity.MPSArmorStand;
import mpsremodularized.common.event.HarvestEventHandler;
import mpsremodularized.common.event.MovementManager;
import mpsremodularized.common.event.PlayerUpdateHandler;
import mpsremodularized.common.network.MPSPackets;
import mpsremodularized.common.registration.MPSArmorMaterial;
import mpsremodularized.common.registration.MPSBlockEntities;
import mpsremodularized.common.registration.MPSBlocks;
import mpsremodularized.common.registration.MPSCapabilities;
import mpsremodularized.common.registration.MPSConfigurations;
import mpsremodularized.common.registration.MPSDataComponents;
import mpsremodularized.common.registration.MPSEntities;
import mpsremodularized.common.registration.MPSIngredientTypes;
import mpsremodularized.common.registration.MPSItems;
import mpsremodularized.common.registration.MPSMenuTypes;
import mpsremodularized.common.registration.MPSParticles;
import mpsremodularized.common.registration.RecipeSerializersRegistry;
import mpsremodularized.common.utils.HeatUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

/** Owns all runtime registrations for MPS Re-modularized. */
@Mod(MPSConstants.MOD_ID)
public final class MPSReModularized {
    public MPSReModularized(IEventBus modEventBus, ModContainer modContainer) {
        // Finish declaring every module before any capability or client consumer freezes the catalog.
        MPSItems.MPS_ITEMS.register(modEventBus);
        MPSItems.MPS_CREATIVE_MODE_TAB.register(modEventBus);
        MPSBlocks.MPS_BLOCKS.register(modEventBus);
        MPSBlockEntities.BLOCKENTITY_TYPES.register(modEventBus);
        MPSEntities.MPS_ENTITIES.register(modEventBus);
        MPSParticles.PARTICLE_TYPES.register(modEventBus);
        MPSMenuTypes.MENU_TYPES.register(modEventBus);
        MPSArmorMaterial.ARMOR_MATERIALS.register(modEventBus);
        MPSDataComponents.DATA_COMPONENT_TYPES.register(modEventBus);
        MPSDataComponents.ATTACHMENT_TYPES.register(modEventBus);
        MPSIngredientTypes.INGREDIENT_TYPES.register(modEventBus);
        RecipeSerializersRegistry.RECIPE_SERIALIZERS.register(modEventBus);
        MPSSoundDictionary.MPS_SOUND_EVENTS.register(modEventBus);

        modEventBus.addListener(MPSReModularized::addEntityAttributes);
        modEventBus.addListener(MPSPackets::register);
        modEventBus.addListener(MPSCapabilities::registerCapabilities);
        modEventBus.addListener(mpsremodularized.common.compat.NativeArmorIntegration::registerCapabilities);
        modEventBus.addListener((net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent event) ->
                event.enqueueWork(mpsremodularized.common.compat.GascraftIntegration::register));

        NeoForge.EVENT_BUS.register(HarvestEventHandler.class);
        NeoForge.EVENT_BUS.register(HeatUtils.class);
        NeoForge.EVENT_BUS.register(PlayerUpdateHandler.class);
        NeoForge.EVENT_BUS.register(MovementManager.INSTANCE);
        MPSConfigurations.setup(modContainer);
    }

    private static void addEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(MPSEntities.ARMOR_STAND__ENTITY_TYPE.get(), MPSArmorStand.createAttributes().build());
    }
}
