package mpsremodularized.common.jei;

import mpsremodularized.common.constants.MPSConstants;
import mpsremodularized.common.item.Battery;
import mpsremodularized.common.registration.MPSDataComponents;
import mpsremodularized.common.registration.MPSItems;
import mpsremodularized.client.gui.module.install_salvage.InstallSalvageGui;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.List;

@JeiPlugin
public class MPSJeiPlugin implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(MPSConstants.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGuiContainerHandler(InstallSalvageGui.class, new IGuiContainerHandler<InstallSalvageGui>() {
            @Override
            public List<Rect2i> getGuiExtraAreas(InstallSalvageGui screen) {
                return List.of(screen.getJeiSidebarArea());
            }
        });
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        // Keep charge variants distinct in the sidebar, while recipes accept any charge.
        List<DeferredHolder<Item, Battery>> batteries = List.of(
            MPSItems.BATTERY_1,
            MPSItems.BATTERY_2,
            MPSItems.BATTERY_3,
            MPSItems.BATTERY_4
        );

        for (DeferredHolder<Item, Battery> battery : batteries) {
            registration.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, battery.get(), new ISubtypeInterpreter<ItemStack>() {
                @Override
                public Object getSubtypeData(ItemStack stack, UidContext context) {
                    return context == UidContext.Recipe ? 0 : stack.getOrDefault(MPSDataComponents.ENERGY, 0);
                }

                @Override
                @Deprecated
                public String getLegacyStringSubtypeInfo(ItemStack stack, UidContext context) {
                    // Still required by JEI 19's current interpreter interface.
                    return "energy_" + (context == UidContext.Recipe ? 0 : stack.getOrDefault(MPSDataComponents.ENERGY, 0));
                }
            });
        }
    }
}
