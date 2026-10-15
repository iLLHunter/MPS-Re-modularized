package mpsremodularized.client.overlay;

import mpsremodularized.common.capabilities.inventory.modularitem.IModularItem;
import mpsremodularized.common.capabilities.module.powermodule.IPowerModule;
import mpsremodularized.common.capabilities.render.hud.IHudModule;
import mpsremodularized.common.math.Color;
import mpsremodularized.common.registration.MPSCapabilities;
import mpsremodularized.common.utils.IconUtils;
import mpsremodularized.common.utils.ItemUtils;
import mpsremodularized.common.utils.StringUtils;
import mpsremodularized.client.config.MPSClientConfig;
import mpsremodularized.common.constants.MPSConstants;
import mpsremodularized.common.item.module.environmental.AutoFeederModule;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class MPSHUD {
    static final ItemStack food = new ItemStack(Items.COOKED_BEEF);

    public static void render(Player player, GuiGraphics gfx, float top , float left) {
        Level level = player.level();
        int yOffsetString = 18;
        float yOffsetIcon = 16.0F;
        // Misc Overlay Items ---------------------------------------------------------------------------------
        AtomicInteger index = new AtomicInteger(0);
        // Helmet modules with overlay
        IModularItem iModularItem = ItemUtils.getItemFromEntitySlot(player, EquipmentSlot.HEAD).getCapability(MPSCapabilities.Inventory.MODULAR_ITEM);
        if(iModularItem != null) {
            for (int i = 0; i < iModularItem.getSlots(); i++) {
                ItemStack module = iModularItem.getStackInSlot(i);
                IPowerModule pm = iModularItem.getModuleCapability(module);
                if (pm != null) {
                    if (iModularItem.isModuleOperational(pm)) {
                        // AutoFeeder
                        if (ItemUtils.getRegistryName(module).equals(MPSConstants.AUTO_FEEDER_MODULE)) {
                            // FIXME!!!
                            int foodLevel =  (int) AutoFeederModule.getFoodLevel(module);

                            String num = StringUtils.formatNumberShort(foodLevel);
                            StringUtils.drawShadowedString(gfx, num, left + 17, 4 + top + (yOffsetString * index.get()));
                            // FIXME
                            IconUtils.drawItemAt(gfx, left - 1.0, top + (yOffsetIcon * index.get()), food, Color.WHITE);
                            index.addAndGet(1);

                            // Clock
                        } else if (Objects.equals(ItemUtils.getRegistryName(module), ItemUtils.getRegistryName(Items.CLOCK))) {
                            if (iModularItem.isModuleOperational(pm)) {
                                StringUtils.drawShadowedString(gfx, VisionHudMath.clock(level.getDayTime(), MPSClientConfig.hud_use_24_hour_clock), left + 17, 4 + top + (yOffsetString * index.get()));
                                IconUtils.drawItemAt(gfx, left - 1.0, top + (yOffsetIcon * index.get()), module, Color.WHITE);
                                index.addAndGet(1);
                            }
                            // Generic modules
                        } else if (pm instanceof IHudModule) {
                            IconUtils.drawItemAt(gfx, left - 1.0, top + (yOffsetIcon * index.get()), module, Color.WHITE);
                            index.addAndGet(1);
                        }
                    }
                }
            }
        }
    }
}
