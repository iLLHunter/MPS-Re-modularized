package mpsremodularized.imixin.common.item;

public interface INativeToolMenu {
    default void mpsremodularized$bindNativeOwner(mpsremodularized.common.compat.NativeToolIntegration.Installed owner, Runnable save) {
        throw new IllegalStateException("Native menu binding is unavailable");
    }
    boolean mpsremodularized$nativeOwnerValid();
    default mpsremodularized.common.compat.NativeToolIntegration.Installed mpsremodularized$nativeOwner() { return null; }
    default void mpsremodularized$saveNativeSource() {
        var owner = mpsremodularized$nativeOwner();
        if (owner != null && mpsremodularized$nativeOwnerValid()) owner.save();
    }
}
