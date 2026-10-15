package mpsremodularized.common.compat;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/** Native pneumatic chargers service the sealed Manometer without unpacking it. */
public final class NativeAirCapability {
    private NativeAirCapability() {}
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void register(RegisterCapabilitiesEvent event) {
        if (!ModList.get().isLoaded("pneumaticcraft")) return;
        try {
            var capability = (ItemCapability) Class.forName("me.desht.pneumaticcraft.api.PNCCapabilities").getField("AIR_HANDLER_ITEM").get(null);
            var contract = Class.forName("me.desht.pneumaticcraft.api.tileentity.IAirHandlerItem");
            for (var entry : IntermodModules.ENTRIES) if (entry.donor().equals("pneumaticcraft:manometer"))
                event.registerItem(capability, (module, context) -> {
                    if (!entry.item().get().available() || entry.item().get().source(module).isEmpty()) return null;
                    return Proxy.newProxyInstance(contract.getClassLoader(), new Class<?>[]{contract}, (proxy, method, args) -> {
                        if (method.getName().equals("getContainer")) return module;
                        var source = entry.item().get().source(module);
                        if (source.isEmpty() || !entry.item().get().available()) throw new IllegalStateException("Unavailable sealed native air container");
                        var before = source.copy();
                        var nativeHandler = source.getCapability(capability);
                        if (nativeHandler == null) throw new IllegalStateException("Missing native Manometer air capability");
                        try { return method.invoke(nativeHandler, args); }
                        catch (InvocationTargetException error) { throw error.getCause(); }
                        finally { if (!ItemStack.isSameItemSameComponents(before, source)) entry.item().get().store(module, source); }
                    });
                }, entry.item().get());
        } catch (ReflectiveOperationException error) { throw new IllegalStateException("Unsupported native pneumatic capability", error); }
    }
}
