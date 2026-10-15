package mpsremodularized.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import mpsremodularized.client.control.KeyMappingReaderWriter;
import mpsremodularized.client.control.KeymappingKeyHandler;
import mpsremodularized.client.gui.MPSIcons;
import mpsremodularized.client.gui.module.install_salvage.InstallSalvageGui;
import mpsremodularized.client.model.helper.ModelLayerHelper;
import mpsremodularized.client.model.obj.MPSObjLoader;
import mpsremodularized.client.overlay.MPSOverlay;
import mpsremodularized.client.overlay.ModeChangingIconOverlay;
import mpsremodularized.client.overlay.VisionHudMath;
import mpsremodularized.client.particle.RailgunEnergyParticle;
import mpsremodularized.client.render.block.TinkerTableBER;
import mpsremodularized.client.render.entity.LuxCapacitorEntityRenderer;
import mpsremodularized.client.render.entity.MPSArmorStandRenderer;
import mpsremodularized.client.render.entity.PlasmaBoltEntityRenderer;
import mpsremodularized.client.render.entity.SpinningBladeEntityRenderer;
import mpsremodularized.client.render.item.MPSArmorLayer;
import mpsremodularized.client.render.item.MPSArmorStandItemRenderer;
import mpsremodularized.client.render.item.MPSBEWLR;
import mpsremodularized.client.screen.ArmorStandScreen;
import mpsremodularized.client.screen.ChargingBaseScreen;
import mpsremodularized.common.base.MPSLogger;
import mpsremodularized.common.capabilities.inventory.modechanging.IModeChangingItem;
import mpsremodularized.common.capabilities.inventory.modularitem.IModularItem;
import mpsremodularized.common.capabilities.module.blockbreaking.IBlockBreakingModule;
import mpsremodularized.common.capabilities.module.powermodule.IPowerModule;
import mpsremodularized.common.capabilities.render.highlight.IHighlight;
import mpsremodularized.common.constants.MPSConstants;
import mpsremodularized.common.math.Color;
import mpsremodularized.common.registration.MPSBlockEntities;
import mpsremodularized.common.registration.MPSBlocks;
import mpsremodularized.common.registration.MPSCapabilities;
import mpsremodularized.common.registration.MPSEntities;
import mpsremodularized.common.registration.MPSItems;
import mpsremodularized.common.registration.MPSMenuTypes;
import mpsremodularized.common.registration.MPSParticles;
import mpsremodularized.common.utils.ElectricItemUtils;
import mpsremodularized.common.utils.IconUtils;
import mpsremodularized.common.utils.ItemUtils;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ArmorStandRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.common.NeoForge;

@EventBusSubscriber(modid = MPSConstants.MOD_ID, value = Dist.CLIENT)
public class MPSClientEvents {
    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(MPSParticles.RAILGUN_FLASH.get(), sprites -> new RailgunEnergyParticle.Provider(sprites, true));
        event.registerSpriteSet(MPSParticles.RAILGUN_SPARK.get(), sprites -> new RailgunEnergyParticle.Provider(sprites, false));
    }

    @SubscribeEvent
    public static void onRegisterReloadListenerEvent(RegisterClientReloadListenersEvent event) {
        MPSIcons icon = IconUtils.getIcon();
        event.registerReloadListener(icon.getSpriteUploader());
    }

    @SubscribeEvent
    public static void modelRegistry(ModelEvent.RegisterGeometryLoaders event) {
        event.register(ResourceLocation.fromNamespaceAndPath(MPSConstants.MOD_ID, "obj"), MPSObjLoader.INSTANCE);
    }

    @SubscribeEvent
    public static void addLayers(EntityRenderersEvent.AddLayers event) {
        // 1. Get the ModelManager needed for the super constructor
        var modelManager = event.getContext().getModelManager();

        // 2. Fix for Armor Stands
        addLayerToArmorStandRenderer(event.getRenderer(EntityType.ARMOR_STAND), modelManager);
        addLayerToArmorStandRenderer(event.getRenderer(MPSEntities.ARMOR_STAND__ENTITY_TYPE.get()), modelManager);

        //        MPSLogger.logDebug("adding layers");

        // Add our own custom armor and elytra (TODO? jetpack maybe?) layer to the various player renderers
        for (PlayerSkin.Model skin : event.getSkins()) {
            //Note: We expect this to always be an instanceof PlayerRenderer, but we just bother checking if it is a LivingEntityRenderer
            // additional note: this should also apply to most humanoid mobs
            if (event.getSkin(skin) instanceof LivingEntityRenderer<?, ?> renderer) {
                Set<EntityType<?>> x = event.getEntityTypes();
                //                for (EntityType<?> e : x) {
                //                    ModelLayerHelper.addCustomLayers(e, renderer, event.getContext());
                ////                    MPSLogger.logDebug("render layer for type: " + e);
                //                }
                ModelLayerHelper.addCustomLayers(EntityType.PLAYER, renderer, event.getContext());
            }
        }
    }

    static void addLayerToArmorStandRenderer(ArmorStandRenderer armorStandRenderer, ModelManager modelManager) {
        if (armorStandRenderer != null) {
            // Find the vanilla armor layer to pass into your constructor
            HumanoidArmorLayer<?, ?, ?> vanillaArmorLayer = null;
            for (var layer : armorStandRenderer.layers) {
                if (layer instanceof HumanoidArmorLayer) {
                    vanillaArmorLayer = (HumanoidArmorLayer<?, ?, ?>) layer;
                    break;
                }
            }

            if (vanillaArmorLayer != null) {
                // Construct and add your custom layer to the armor stand renderer
                MPSArmorLayer newLayer = new MPSArmorLayer(
                    armorStandRenderer,
                    vanillaArmorLayer,
                    modelManager
                );

                armorStandRenderer.layers.removeIf(layer -> layer.getClass() == HumanoidArmorLayer.class);
                armorStandRenderer.addLayer(newLayer);
            }
        }
    }

    @SubscribeEvent
    public static void onClientSetup(final FMLClientSetupEvent event) {
        NeoForge.EVENT_BUS.register(MPSFOVUpdateEventHandler.class);
        NeoForge.EVENT_BUS.addListener(MPSClientEvents::onFOVUpdate);
        NeoForge.EVENT_BUS.addListener(MPSClientEvents::renderBlockHighlight);
        NeoForge.EVENT_BUS.register(new KeymappingKeyHandler());
    }

    @SubscribeEvent
    public static void registerKeyBinding(RegisterKeyMappingsEvent event) {
        event.register(MPSFOVUpdateEventHandler.fovToggleKey.get());
        KeymappingKeyHandler.initializeModuleKeyBindings();
        KeyMappingReaderWriter.INSTANCE.readInKeybinds();
        KeymappingKeyHandler.getMPSKeyMappings().forEach(event::register);
        Arrays.stream(KeymappingKeyHandler.keybindArray).forEach(event::register);
        KeyMapping.resetMapping();
        KeyMappingReaderWriter.INSTANCE.writeOutKeybindSetings();
        MPSOverlay.makeKBDisplayList();
    }

    @SubscribeEvent
    public static void onRegisterRenderers(final EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(MPSEntities.ARMOR_STAND__ENTITY_TYPE.get(), MPSArmorStandRenderer::new);
        //        event.registerEntityRenderer(MPSEntities.RAILGUN_BOLT_ENTITY_TYPE.get(), RailGunBoltRenderer::new);
        event.registerEntityRenderer(MPSEntities.LUX_CAPACITOR_ENTITY_TYPE.get(), LuxCapacitorEntityRenderer::new);
        event.registerEntityRenderer(MPSEntities.PLASMA_BALL_ENTITY_TYPE.get(), PlasmaBoltEntityRenderer::new);
        //        event.registerEntityRenderer(MPSEntities.PLASMA_BALL_ENTITY_TYPE.get(), PlasmaBallEntityRenderer2::new);
        event.registerEntityRenderer(MPSEntities.SPINNING_BLADE_ENTITY_TYPE.get(), SpinningBladeEntityRenderer::new);
        event.registerBlockEntityRenderer(MPSBlockEntities.TINKER_TABLE_BLOCKENTITY_TYPE.get(), TinkerTableBER::new);
    }

    @SubscribeEvent
    public static void registerScreen(RegisterMenuScreensEvent event) {
        event.register(MPSMenuTypes.CHARGING_BASE_CONTAINER_TYPE.get(), ChargingBaseScreen::new);
        event.register(MPSMenuTypes.ARMOR_STAND_CONTAINER_TYPE.get(), ArmorStandScreen::new);
        event.register(MPSMenuTypes.INSTALL_SALVAGE_MENU_TYPE.get(), InstallSalvageGui::new);
    }

    //
    //    @SubscribeEvent
    //    public static void renderLayer(RenderGuiLayerEvent event) {
    //        LayeredDraw.Layer layer = event.getLayer();
    //        GuiGraphics gfx = event.getGuiGraphics();
    //        ResourceLocation name = event.getName();
    //        float partialTick = event.getPartialTick();
    //
    //
    //    }

    @SubscribeEvent
    public static void registerOverlay(RegisterGuiLayersEvent event) {
        event.registerAboveAll(MPSConstants.MODE_CHANGING_ICON_RENDERER, ModeChangingIconOverlay.MODE_CHANGING_ICON_OVERLAY);
        event.registerAboveAll(ResourceLocation.fromNamespaceAndPath(MPSConstants.MOD_ID, "keybinds"), MPSOverlay.MPS_KEYBIND_OVERLAY);
        //        event.registerAboveAll("mps_hud", MPSOverlay.MPS_HUD);
    }

    @SubscribeEvent
    public static void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {
        ModelBakeEventHandler.INSTANCE.onModifyBakingResult(event);
    }

    @SubscribeEvent
    public static void onAddAdditional(ModelEvent.RegisterAdditional e) {
        ModelSpecBakeHandler.onAddAdditional(e);
        ModelBakeEventHandler.onAddAdditional(e);
    }

    @OnlyIn(Dist.CLIENT)
    //    @SubscribeEvent
    public static void onFOVUpdate(ComputeFovModifierEvent e) {
        IModularItem cap = ItemUtils.getItemFromEntitySlot(e.getPlayer(), EquipmentSlot.HEAD).getCapability(MPSCapabilities.Inventory.MODULAR_ITEM);
        if(cap != null) {
            ItemStack binoculars = cap.getOnlineModuleOrEmpty(MPSConstants.BINOCULARS_MODULE);
            if (!binoculars.isEmpty()) {
                IPowerModule pm = cap.getModuleCapability(binoculars);
                if (pm != null) {
                    e.setNewFovModifier(VisionHudMath.zoom(e.getNewFovModifier(), pm.applyPropertyModifiers(MPSConstants.FOV)));
                }
            }
        }
    }

    /**
     * Just for a couple modules that can break multiple blocks at once
     * @param event
     */
    @OnlyIn(Dist.CLIENT)
    //    @SubscribeEvent
    public static void renderBlockHighlight(RenderHighlightEvent.Block event) {
        Color.WHITE.setShaderColor();
        Level level = Minecraft.getInstance().level;

        if (event.getTarget().getType() != HitResult.Type.BLOCK || !(event.getCamera().getEntity() instanceof Player player) || level == null) {
            return;
        }

        BlockHitResult result = event.getTarget();
        double playerEnergy = ElectricItemUtils.getPlayerEnergy(player);
        ItemStack heldItem = player.getMainHandItem();
        IModeChangingItem mci = MPSCapabilities.getModeChangingModularItem(heldItem);
        if (mci != null) {
            ItemStack module = mci.getActiveModule();
            IPowerModule pm = mci.getModuleCapability(module);

            if(pm instanceof IHighlight highlight && pm.isModuleOnline()) {
                playerEnergy = playerEnergy - pm.getEnergyUsage();
                NonNullList<IBlockBreakingModule> modules = NonNullList.create();
                if (mpsremodularized.common.compat.PaxelIntegration.find(heldItem) != null) {
                    modules.addAll(mpsremodularized.common.compat.PaxelIntegration.miningTools(heldItem));
                } else for (int i=0; i< mci.getSlots(); i++) {
                    IPowerModule pm1 = mci.getModuleCapability(mci.getStackInSlot(i));
                    if(pm1 instanceof IBlockBreakingModule bbm && pm1 != pm) {
                        modules.add(bbm);
                    }
                }

                HashMap<IHighlight.BlockPostionData, Integer> blockPositions = highlight
                    .getBlockPositions(
                    heldItem,
                    result,
                    player,
                    level,
                    modules,
                    playerEnergy
                );

                if(blockPositions.isEmpty()) {
                    return;
                }

                PoseStack matrixStack = event.getPoseStack();
                MultiBufferSource buffer = event.getMultiBufferSource();
                VertexConsumer lineBuilder = buffer.getBuffer(RenderType.LINES);

                double partialTicks = event.getDeltaTracker().getGameTimeDeltaPartialTick(true); //  FIXME: IS this correct

                double x = player.xOld + (player.getX() - player.xOld) * partialTicks;
                double y = player.yOld + player.getEyeHeight() + (player.getY() - player.yOld) * partialTicks;
                double z = player.zOld + (player.getZ() - player.zOld) * partialTicks;
                matrixStack.pushPose();

                blockPositions.forEach((blockPostionData, miningLevel) -> {
                        BlockPos blockPos = blockPostionData.pos();
                        AABB aabb = new AABB(blockPos).move(-x, -y, -z);

                        float r=0F;
                        float g=0F;
                        float b=0;
                        float a=0.2F;

                        // Render the targeted block at full brightness to distinguish it from others
                        if (blockPos.compareTo(result.getBlockPos()) == 0) {
                            a=1;
                    }

                        if (blockPostionData.canHarvest()) {
                            if(miningLevel == 1) {
                                g = 0.8F;
                        } else {
                                g = 1F;
                        }
                    } else {
                            r = 1;
                    }

                        LevelRenderer.renderLineBox(matrixStack, lineBuilder, aabb, r, g, b, a);
                });
                matrixStack.popPose();
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onScreenInitPost(ScreenEvent.Init.Post event) {
        if(event.getScreen() instanceof InstallSalvageGui) {
            ((InstallSalvageGui) event.getScreen()).restoreMouse();
        }
    }

    //
    ////        NeoForge.EVENT_BUS.register(new ToolTipEvent());
    ////        event.enqueueWork(() -> {
    ////            MenuScreens.register(NuminaObjects.CHARGING_BASE_CONTAINER_TYPE.get(), ChargingBaseScreen::new);
    ////            MenuScreens.register(NuminaObjects.ARMOR_STAND_CONTAINER_TYPE.get(), ArmorStandScreen::new);
    ////            //        ScreenManager.func_216911_a(NuminaObjects.SCANNER_CONTAINER.get(), MPSGuiScanner::new);
    ////        });
    //
    ////        MinecraftForge.EVENT_BUS.addListener((InputEvent.Key e) -> {
    //////            ModelTransformCalibration.CALIBRATION.transformCalibration(e);
    ////        });
    //    }

    @SubscribeEvent
    public static void clientExtensionEvent(RegisterClientExtensionsEvent event) {
        var nativeRenderer = new mpsremodularized.client.render.item.NativeModuleRenderer();
        event.registerItem(new IClientItemExtensions() {
            @Override public BlockEntityWithoutLevelRenderer getCustomRenderer() { return nativeRenderer; }
        }, mpsremodularized.common.compat.IntermodModules.ENTRIES.stream().map(entry -> entry.item().get()).toArray(net.minecraft.world.item.Item[]::new));
        event.registerItem(new IClientItemExtensions() {
                private final BlockEntityWithoutLevelRenderer renderer = new MPSArmorStandItemRenderer();
                @Override
                public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                    return renderer;
            }
        }, MPSItems.ARMOR_STAND_ITEM.get()
        );
        final BlockEntityWithoutLevelRenderer renderer = ModelBakeEventHandler.INSTANCE.MPSBERINSTANCE;

        event.registerItem(new IClientItemExtensions() {
                @Override
                public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                    return renderer;
            }

                @Override
                public boolean applyForgeHandTransform(PoseStack poseStack, LocalPlayer player, HumanoidArm arm, ItemStack itemInHand, float partialTick, float equipProcess, float swingProcess) {
                    ((MPSBEWLR)renderer).setFiringData(new MPSBEWLR.FiringData(player, arm, itemInHand));
                    return IClientItemExtensions.super.applyForgeHandTransform(poseStack, player, arm, itemInHand, partialTick, equipProcess, swingProcess);
            }
        }, MPSItems.POWER_FIST_1.get(), MPSItems.POWER_FIST_2.get(), MPSItems.POWER_FIST_3.get(), MPSItems.POWER_FIST_4.get());

        event.registerItem(new IClientItemExtensions() {
                @Override
                public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                    return renderer;
            }
        }, MPSItems.TINKER_TABLE_ITEM.get());

        //        event.registerItem(new IClientItemExtensions() {
        //            @Override
        //            public HumanoidModel<?> getHumanoidArmorModel(
        //                LivingEntity livingEntity,
        //                ItemStack itemStack,
        //                EquipmentSlot equipmentSlot,
        //                HumanoidModel<?> originalModel
        //            ) {
        //                // Fetch your custom model layer instance
        ////                MyCustomArmorModel<?> model = ClientCache.getCustomModel(equipmentSlot);
        //                HighPolyArmor model = ArmorModelInstance.getInstance();
        //
        //                IModelSpec renderCapability = itemStack.getCapability(MPSCapabilities.RENDER);
        //                if (renderCapability != null) {
        //                    model.renderSpec = renderCapability.getRenderTagOrDefault();
        //                }
        //
        //
        //                // Sync slot visibility rules (toggles helmet/chest/boots parts)
        //                model.setAllVisible(false);
        //                switch (equipmentSlot) {
        //                case HEAD -> {
        //                    model.head.visible = true;
        //                    model.hat.visible = true;
        //                }
        //                case CHEST -> {
        //                    model.body.visible = true;
        //                    model.rightArm.visible = true;
        //                    model.leftArm.visible = true;
        //                }
        //                case LEGS -> {
        //                    model.body.visible = true;
        //                    model.rightLeg.visible = true;
        //                    model.leftLeg.visible = true;
        //                }
        //                case FEET -> {
        //                    model.rightLeg.visible = true;
        //                    model.leftLeg.visible = true;
        //                }
        //                }
        //
        //
        ////                new MPSArmorLayer(renderer, (HumanoidArmorLayer<T, ?, ?>) layerRenderer, context.getModelManager()));
        //
        //                // IMPORTANT FOR ARMOR STANDS:
        //                // Copy properties from the original model so it adopts the exact body pose
        //                // (e.g., armor stand arms, head tilt, crouching state)
        //                originalModel.copyPropertiesTo((HumanoidModel) model);
        //
        //                return model;
        //            }
        //        }, MPSItems.POWER_ARMOR_HELMET_1.get(),
        //                MPSItems.POWER_ARMOR_HELMET_2.get(),
        //                MPSItems.POWER_ARMOR_HELMET_3.get(),
        //                MPSItems.POWER_ARMOR_HELMET_4.get(),
        //
        //                MPSItems.POWER_ARMOR_CHESTPLATE_1.get(),
        //                MPSItems.POWER_ARMOR_CHESTPLATE_2.get(),
        //                MPSItems.POWER_ARMOR_CHESTPLATE_3.get(),
        //                MPSItems.POWER_ARMOR_CHESTPLATE_4.get(),
        //
        //                MPSItems.POWER_ARMOR_LEGGINGS_1.get(),
        //                MPSItems.POWER_ARMOR_LEGGINGS_2.get(),
        //                MPSItems.POWER_ARMOR_LEGGINGS_3.get(),
        //                MPSItems.POWER_ARMOR_LEGGINGS_4.get(),
        //
        //                MPSItems.POWER_ARMOR_BOOTS_1.get(),
        //                MPSItems.POWER_ARMOR_BOOTS_2.get(),
        //                MPSItems.POWER_ARMOR_BOOTS_3.get(),
        //                MPSItems.POWER_ARMOR_BOOTS_4.get()); // Add all your armor items here
    }
}
