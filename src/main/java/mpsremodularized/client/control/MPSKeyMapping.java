package mpsremodularized.client.control;

import com.mojang.blaze3d.platform.InputConstants;
import mpsremodularized.common.capabilities.inventory.modularitem.IModularItem;
import mpsremodularized.common.network.MPSPackets;
import mpsremodularized.common.network.serverbound.ToggleRequestPacketServerBound;
import mpsremodularized.common.registration.MPSCapabilities;
import mpsremodularized.client.overlay.MPSOverlay;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.settings.KeyModifier;

public class MPSKeyMapping extends KeyMapping {
    public final ResourceLocation registryName;
    public boolean showOnHud = true;
    public boolean toggleVal = false; // fixme: get value on load?

    public MPSKeyMapping(ResourceLocation registryName, String name, int key, String category, boolean showOnHud) {
        super(name, key, category);
        this.registryName = registryName;
        this.showOnHud = showOnHud;
        initToggleVal();
    }

    @Override
    public void setKey(InputConstants.Key key) {
        super.setKey(key);
        bindingChanged();
    }

    @Override
    public void setKeyModifierAndCode(KeyModifier modifier, InputConstants.Key key) {
        super.setKeyModifierAndCode(modifier, key);
        bindingChanged();
    }

    private void bindingChanged() {
        // Vanilla setKey only changes the field. NeoForge's modifier setter can retain the old entry.
        KeyMapping.resetMapping();
        KeyMappingReaderWriter.INSTANCE.writeOutKeybindSetings();
        MPSOverlay.makeKBDisplayList();
    }

    /**
     * Use this one to set the key from inside MPS
     * @param key
     */
    public void setKeyInternal(InputConstants.Key key) {
        setKeyInternal(key, KeyModifier.NONE);
    }

    public void setKeyInternal(InputConstants.Key key, KeyModifier modifier) {
        super.setKeyModifierAndCode(modifier, key);
    }

    void initToggleVal() {
        if (Minecraft.getInstance() != null && Minecraft.getInstance().player != null) {
            toggleVal = isModuleOnline(Minecraft.getInstance().player);
        }
    }

    private boolean isModuleOnline(Player player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            IModularItem modularItem = MPSCapabilities.getModularItemOrModeChangingCapability(player.getInventory().getItem(i));
            if (modularItem != null && modularItem.isModuleOnline(registryName)) {
                return true;
            }
        }
        return false;
    }

    public void toggleModules() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        toggleVal = !isModuleOnline(player);
        MPSPackets.sendToServer(new ToggleRequestPacketServerBound(registryName, toggleVal));
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            IModularItem modularItem = MPSCapabilities.getModularItemOrModeChangingCapability(player.getInventory().getItem(i));
            if (modularItem != null) {
                modularItem.toggleModule(registryName, toggleVal);
            }
        }
    }
}
