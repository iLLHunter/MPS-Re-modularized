package mpsremodularized.client.compat;

import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;

/** Physical-side isolation for decoding a sealed native stack on a connected client. */
public final class NativeModuleRegistries {
    private NativeModuleRegistries() {}
    public static HolderLookup.Provider current() {
        var client = Minecraft.getInstance();
        return client.getConnection() == null ? null : client.getConnection().registryAccess();
    }
}
