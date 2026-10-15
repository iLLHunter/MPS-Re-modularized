package mpsremodularized.common.compat;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import mpsremodularized.common.item.electric.armor.AbstractElectricArmor;
import mpsremodularized.common.registration.MPSCapabilities;

/** A native module context used only by Mekanism's HUD traversal. */
public final class MekanismHelmetIntegration {
    private MekanismHelmetIntegration() {}
    @SuppressWarnings("unchecked") public static Object hudContainer(ItemStack helmet) {
        if (!(helmet.getItem() instanceof AbstractElectricArmor armor) || armor.getEquipmentSlot() != EquipmentSlot.HEAD) return null;
        var host = MPSCapabilities.getModularItem(helmet);
        if (host == null) return null;
        ItemStack context = null;
        try {
            for (int index=0; index<host.getSlots(); index++) {
                var module=host.getStackInSlot(index);
                if (!(module.getItem() instanceof NativeModuleItem item) || item.kind != NativeModuleItem.Kind.HELMET_UNIT
                        || !host.isModuleOperational(host.getModuleCapability(module))) continue;
                var donor=item.source(module);
                if (donor.isEmpty()) continue;
                if (context == null) context=donor.copy();
                var component=(DataComponentType<Object>) ((java.util.function.Supplier<?>)Class.forName("mekanism.common.registries.MekanismDataComponents").getField("MODULE_CONTAINER").get(null)).get();
                var type=Class.forName("mekanism.common.content.gear.ModuleContainer");
                var container=context.getOrDefault(component,type.getField("EMPTY").get(null));
                var nativeType=Class.forName("mekanism.common.registries.MekanismModules").getField(item.donor.getPath().equals("module_geiger_unit") ? "GEIGER_UNIT" : "DOSIMETER_UNIT").get(null);
                type.getMethod("addModule",net.minecraft.core.HolderLookup.Provider.class,ItemStack.class,Holder.class,int.class)
                        .invoke(container,net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer()!=null
                                ? net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer().registryAccess()
                                : mpsremodularized.client.compat.NativeModuleRegistries.current(),context,nativeType,1);
            }
            if (context == null) return null;
            var component=(DataComponentType<Object>) ((java.util.function.Supplier<?>)Class.forName("mekanism.common.registries.MekanismDataComponents").getField("MODULE_CONTAINER").get(null)).get();
            return context.get(component);
        } catch (ReflectiveOperationException error) { throw new IllegalStateException("Unsupported Mekanism HUD module API",error); }
    }
}
