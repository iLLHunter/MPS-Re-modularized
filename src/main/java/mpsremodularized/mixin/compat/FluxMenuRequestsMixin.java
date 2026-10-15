package mpsremodularized.mixin.compat;

import java.util.function.Supplier;
import mpsremodularized.imixin.common.item.INativeToolMenu;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "sonar.fluxnetworks.register.Messages", remap = false)
public abstract class FluxMenuRequestsMixin {
    // These are the native server-work callbacks, after packet decoding/enqueueing.
    // Returning no player uses their existing early return, before any native operation.
    @Redirect(method = {"lambda$onDeviceBuffer$0", "lambda$onSuperAdmin$1", "lambda$onEditTile$2",
            "lambda$onCreateNetwork$3", "lambda$onDeleteNetwork$4", "lambda$onTileNetwork$5",
            "lambda$onEditNetwork$6", "lambda$onUpdateNetwork$7", "lambda$onEditMember$8",
            "lambda$onEditConnection$9", "lambda$onWirelessMode$10", "lambda$onDisconnect$11",
            "lambda$onUpdateConnections$12"},
            at = @At(value = "INVOKE", target = "Ljava/util/function/Supplier;get()Ljava/lang/Object;"))
    private static Object mps$validate(Supplier<?> supplier) {
        Object player = supplier.get();
        return player instanceof ServerPlayer serverPlayer && serverPlayer.containerMenu instanceof INativeToolMenu menu
                && !menu.mpsremodularized$nativeOwnerValid() ? null : player;
    }
}
