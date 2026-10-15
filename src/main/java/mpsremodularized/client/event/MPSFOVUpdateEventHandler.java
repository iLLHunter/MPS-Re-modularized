package mpsremodularized.client.event;

import mpsremodularized.client.config.MPSClientConfig;
import mpsremodularized.common.constants.MPSConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;
import net.neoforged.neoforge.common.util.Lazy;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
public class MPSFOVUpdateEventHandler {
    public static final Lazy<KeyMapping> fovToggleKey = Lazy.of(() -> new KeyMapping("key." + MPSConstants.MOD_ID + ".fovfixtoggle", GLFW.GLFW_KEY_UNKNOWN, "key.categories." + MPSConstants.MOD_ID));
    public static boolean fovIsActive = MPSClientConfig.fovFixDefaultState;

    @SubscribeEvent
    public static void onFOVUpdate(ComputeFovModifierEvent e) {
        if (MPSClientConfig.useFovFix) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (fovToggleKey.get().consumeClick()) {
                fovIsActive = !fovIsActive;
                if (player == null) {
                    return;
                }

                if (fovIsActive) {
                    player.sendSystemMessage(Component.translatable("message." + MPSConstants.MOD_ID + ".fovfixtoggle.enabled"));
                } else {
                    player.sendSystemMessage(Component.translatable("message." + MPSConstants.MOD_ID + ".fovfixtoggle.disabled"));
                }
            }

            if (fovIsActive && player != null) {
                AttributeInstance attributeinstance = player.getAttribute(Attributes.MOVEMENT_SPEED);
                if (attributeinstance == null || player.getAbilities().getWalkingSpeed() <= 0) return;
                double movementFov = (attributeinstance.getValue() / player.getAbilities().getWalkingSpeed() + 1.0) / 2.0;
                e.setNewFovModifier(mpsremodularized.client.overlay.VisionHudMath.zoom(e.getNewFovModifier(), movementFov));
                if (MPSClientConfig.useFovNormalize) {
                    if (player.isSprinting() && !player.isInWaterOrBubble()) {
                        e.setNewFovModifier(e.getNewFovModifier() + 0.15F);
                    }
                }
            }
        }
    }
}



