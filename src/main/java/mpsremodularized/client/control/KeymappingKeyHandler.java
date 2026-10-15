package mpsremodularized.client.control;

import mpsremodularized.common.capabilities.inventory.modechanging.IModeChangingItem;
import mpsremodularized.common.capabilities.player.keystates.IPlayerKeyStates;
import mpsremodularized.common.network.MPSPackets;
import mpsremodularized.common.network.serverbound.PlayerUpdatePacketServerBound;
import mpsremodularized.common.registration.MPSCapabilities;
import mpsremodularized.common.registration.ModuleRegistry;
import mpsremodularized.common.utils.MathUtils;
import mpsremodularized.client.gui.module.select.GuiModeSelector;
import mpsremodularized.common.constants.MPSConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

@OnlyIn(Dist.CLIENT)
public class KeymappingKeyHandler {
    Minecraft minecraft;
    public static final KeyMapping goDownKey = new KeyMapping(Component.translatable("keybinding." + MPSConstants.MOD_ID + ".goDownKey").getString(), GLFW.GLFW_KEY_Z, MPSConstants.MPS_ITEM_GROUP);
    public static final KeyMapping cycleToolBackward = new KeyMapping(Component.translatable("keybinding." + MPSConstants.MOD_ID + ".cycleToolBackward").getString(), GLFW.GLFW_KEY_UNKNOWN, MPSConstants.MPS_ITEM_GROUP);
    public static final KeyMapping cycleToolForward = new KeyMapping(Component.translatable("keybinding." + MPSConstants.MOD_ID + ".cycleToolForward").getString(), GLFW.GLFW_KEY_UNKNOWN, MPSConstants.MPS_ITEM_GROUP);
    public static final KeyMapping openKeybindGUI = new KeyMapping(Component.translatable("keybinding." + MPSConstants.MOD_ID + ".openKeybindGui").getString()/*"Open MPS Keybind GUI"*/, GLFW.GLFW_KEY_UNKNOWN, MPSConstants.MPS_ITEM_GROUP);
    public static final KeyMapping openCosmeticGUI = new KeyMapping(Component.translatable("keybinding." + MPSConstants.MOD_ID + ".openCosmeticGUI").getString() /*Cosmetic GUI (MPS)"*/, GLFW.GLFW_KEY_UNKNOWN, MPSConstants.MPS_ITEM_GROUP);
    public static final KeyMapping openModuleTweakGUI = new KeyMapping(Component.translatable("keybinding." + MPSConstants.MOD_ID + ".openModuleTweakGUI").getString() /*Open MPS Keybind GUI"*/, GLFW.GLFW_KEY_UNKNOWN, MPSConstants.MPS_ITEM_GROUP);
    public static final KeyMapping openInstallSalvageGUI = new KeyMapping(Component.translatable("keybinding." + MPSConstants.MOD_ID + ".openInstallSalvageGUI").getString() /*Cosmetic GUI (MPS)"*/, GLFW.GLFW_KEY_UNKNOWN, MPSConstants.MPS_ITEM_GROUP);
    public static final KeyMapping[] keybindArray = new KeyMapping[]{goDownKey, cycleToolBackward, cycleToolForward, openKeybindGUI, openCosmeticGUI, openModuleTweakGUI, openInstallSalvageGUI};
    public static boolean isKeyPressed(int key) {
        return GLFW.glfwGetKey(Minecraft.getInstance().getWindow().getWindow(), key) == GLFW.GLFW_PRESS;
    }

    public KeymappingKeyHandler() {
        minecraft = Minecraft.getInstance();
    }

    void updatePlayerValues(LocalPlayer clientPlayer) {
        if (clientPlayer == null) return;
        IPlayerKeyStates keyStates = clientPlayer.getCapability(MPSCapabilities.PLAYER_KEYSTATES);
        if (keyStates == null) return;
        boolean allow = clientPlayer.isAlive() && minecraft.screen == null && minecraft.isWindowActive();
        Byte changed = pollMovementKeys(keyStates, allow, minecraft.options.keyUp, minecraft.options.keyDown,
                minecraft.options.keyLeft, minecraft.options.keyRight, goDownKey, minecraft.options.keyJump);
        if (changed != null) MPSPackets.sendToServer(new PlayerUpdatePacketServerBound(changed));
    }

    /** Poll Minecraft mappings, including mouse bindings and releases without keyboard events. */
    static Byte pollMovementKeys(IPlayerKeyStates state, boolean allow, KeyMapping... mappings) {
        boolean[] values = new boolean[8];
        for (int i = 0; i < 6; i++) values[i] = allow && mappings[i].isDown();
        boolean changed = state.getForwardKeyState() != values[0] || state.getReverseKeyState() != values[1]
                || state.getLeftStrafeKeyState() != values[2] || state.getRightStrafeKeyState() != values[3]
                || state.getDownKeyState() != values[4] || state.getJumpKeyState() != values[5];
        state.setForwardKeyState(values[0]); state.setReverseKeyState(values[1]);
        state.setLeftStrafeKeyState(values[2]); state.setRightStrafeKeyState(values[3]);
        state.setDownKeyState(values[4]); state.setJumpKeyState(values[5]);
        return changed ? MathUtils.boolArrayToByte(values) : null;
    }

    @SubscribeEvent
    public void beforeClientTick(ClientTickEvent.Pre event) {
        updatePlayerValues(minecraft.player);
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.Key e) {
        LocalPlayer player = minecraft.player;
        if (player == null) {
            return;
        }

        KeyMapping[] hotbarKeys = minecraft.options.keyHotbarSlots;
        Inventory inventory = player.getInventory();

        IModeChangingItem mci = getModeChangingItem(player);






        // Mode changinging GUI
        if (e.getAction() == GLFW.GLFW_PRESS && hotbarKeys[inventory.selected].matches(e.getKey(), e.getScanCode())
                && minecraft.screen == null && minecraft.isWindowActive()) {
            if(mci != null) {
                if (!(Minecraft.getInstance().screen instanceof GuiModeSelector)) {
                    Minecraft.getInstance().tell(() -> Minecraft.getInstance().setScreen(new GuiModeSelector(player, Component.literal("modeChanging"))));
                }
            }
        }

        /**  TODO: server config option to disable these and enforce tinker table requirement */
        /*
        if (openKeybindGUI.isDown()) {
            Musique.playClientSound(MPSSoundDictionary.SOUND_EVENT_GUI_SELECT.get(), 1);
            Minecraft.getInstance().tell(() -> Minecraft.getInstance().setScreen(new TinkerKeymapGui(player, Component.translatable("gui." + MPSConstants.MOD_ID + ".tab.keybinds.toggle"))));
        }

        if (openCosmeticGUI.isDown()) {
            Musique.playClientSound(MPSSoundDictionary.SOUND_EVENT_GUI_SELECT.get(), 1);
            Minecraft.getInstance().tell(() -> Minecraft.getInstance().setScreen(new CosmeticGui(player.getInventory(), Component.translatable("gui.tinkertable"))));
        }

        if (openModuleTweakGUI.isDown()) {
            Musique.playClientSound(MPSSoundDictionary.SOUND_EVENT_GUI_SELECT.get(), 1);
            Minecraft.getInstance().tell(() -> Minecraft.getInstance().setScreen(new ModuleTweakGui(Component.translatable("gui.tinkertable"))));
        }

        if (openInstallSalvageGUI.isDown()) {
            Musique.playClientSound(MPSSoundDictionary.SOUND_EVENT_GUI_SELECT.get(), 1);
            MPSPackets.CHANNEL_INSTANCE.sendToServer(new ContainerGuiOpenPacket(EquipmentSlot.MAINHAND));
        }
        */
    }

    private static final Map<String, MPSKeyMapping> keyMappings = new LinkedHashMap<>();

    @SubscribeEvent
    public void onClientTick(ClientTickEvent.Post event) {
        boolean allowToggles = minecraft.player != null && minecraft.screen == null && minecraft.isWindowActive();
        consumeModuleClicks(allowToggles, MPSKeyMapping::toggleModules);
        consumeModeClicks(allowToggles, direction -> {
            IModeChangingItem item = getModeChangingItem(minecraft.player);
            if (item != null) item.cycleMode(minecraft.player, direction);
        });
    }

    /** Keyboard and mouse clicks share Minecraft's queue; clicks in menus are discarded. */
    static void consumeModuleClicks(boolean allowToggles, Consumer<MPSKeyMapping> toggle) {
        for (MPSKeyMapping mapping : getMPSKeyMappings()) {
            while (mapping.consumeClick()) {
                if (allowToggles) toggle.accept(mapping);
            }
        }
    }

    /** Mode bindings use the same queue for keyboard and mouse, including menu suppression. */
    static void consumeModeClicks(boolean allow, IntConsumer cycle) {
        while (cycleToolBackward.consumeClick()) if (allow) cycle.accept(1);
        while (cycleToolForward.consumeClick()) if (allow) cycle.accept(-1);
    }

    public static void initializeModuleKeyBindings() {
        ModuleRegistry.INSTANCE.freeze().stream().filter(definition -> definition.isToggleable()).forEach(definition -> {
            String name = "keybinding." + definition.id().getNamespace() + "." + definition.id().getPath();
            keyMappings.computeIfAbsent(name, ignored -> new MPSKeyMapping(
                    definition.id(), name, GLFW.GLFW_KEY_UNKNOWN, MPSConstants.MPS_ITEM_GROUP, true));
        });
    }

    public static List<MPSKeyMapping> getMPSKeyMappings() {
        return List.copyOf(keyMappings.values());
    }

    @Nullable
    static MPSKeyMapping getModuleKeyMapping(String name) {
        return keyMappings.get(name);
    }

    @Nullable
    static IModeChangingItem getModeChangingItem(Player player) {
        return MPSCapabilities.getModeChangingModularItem(player.getMainHandItem());
    }

}
