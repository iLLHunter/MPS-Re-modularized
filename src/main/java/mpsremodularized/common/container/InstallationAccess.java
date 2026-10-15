package mpsremodularized.common.container;

import mpsremodularized.common.capabilities.inventory.modechanging.IModeChangingItem;
import mpsremodularized.common.constants.MPSConstants;
import mpsremodularized.common.registration.MPSBlocks;
import mpsremodularized.common.registration.MPSCapabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.WeakHashMap;

/** Access is granted by server interactions; client tab requests cannot create it. */
public final class InstallationAccess {
    // Server game thread only. The context survives switching to the containerless MPS tabs.
    private static final Map<ServerPlayer, Context> ACCESS = new WeakHashMap<>();

    public sealed interface Context permits PlacedTable, PortableTable {
        boolean isValid(Player player);
        default boolean protectsSlot(EquipmentSlot slot) { return false; }
    }

    private record PlacedTable(Level level, BlockPos pos) implements Context {
        @Override public boolean isValid(Player player) {
            return validPlayer(player, level) && level.getChunkSource().hasChunk(pos.getX() >> 4, pos.getZ() >> 4)
                    && level.getBlockState(pos).is(MPSBlocks.TINKER_TABLE_BLOCK.get())
                    && player.canInteractWithBlock(pos, 4.0);
        }
    }

    private record PortableTable(Level level, EquipmentSlot sourceSlot, ItemStack source) implements Context {
        @Override public boolean protectsSlot(EquipmentSlot slot) { return slot == sourceSlot; }
        @Override public boolean isValid(Player player) {
            return validPlayer(player, level) && player.getItemBySlot(sourceSlot) == source
                    && isPortableSourceValid(source);
        }
    }

    private static boolean validPlayer(Player player, Level level) {
        return player instanceof ServerPlayer && player.level() == level && player.isAlive() && !player.isSpectator();
    }

    @Nullable
    public static Context grantPlacedTable(Player player, Level level, BlockPos pos) {
        Context context = new PlacedTable(level, pos.immutable());
        if (!(player instanceof ServerPlayer serverPlayer) || !context.isValid(player)) return null;
        ACCESS.put(serverPlayer, context);
        return context;
    }

    /** The portable module must remain valid and online in the original held Power Fist. */
    public static boolean isPortableSourceValid(ItemStack source) {
        var host = MPSCapabilities.getModularItemOrModeChangingCapability(source);
        if (!(host instanceof IModeChangingItem)) return false;
        var module = host.getModuleCapability(host.getOnlineModuleOrEmpty(MPSConstants.TINKER_TABLE));
        return host.isModuleOperational(module);
    }

    public static boolean openPortableTable(Player player, InteractionHand hand) {
        if (!(player instanceof ServerPlayer serverPlayer)) return false;
        ItemStack source = player.getItemInHand(hand);
        var host = MPSCapabilities.getModularItemOrModeChangingCapability(source);
        if (!(host instanceof IModeChangingItem modes) || !modes.hasActiveModule(MPSConstants.TINKER_TABLE)) return false;
        EquipmentSlot sourceSlot = hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
        Context context = new PortableTable(player.level(), sourceSlot, source);
        if (!context.isValid(player)) return false;
        ACCESS.put(serverPlayer, context);
        return open(serverPlayer, context, sourceSlot, false, -1, -1);
    }

    @Nullable
    private static Context current(ServerPlayer player) {
        Context context = ACCESS.get(player);
        if (context != null && !context.isValid(player)) {
            ACCESS.remove(player);
            return null;
        }
        return context;
    }

    public static void clearInvalid(ServerPlayer player) {
        current(player);
    }

    public static boolean isSupportedSlot(EquipmentSlot slot) {
        return slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND
                || slot == EquipmentSlot.HEAD || slot == EquipmentSlot.CHEST
                || slot == EquipmentSlot.LEGS || slot == EquipmentSlot.FEET;
    }

    public static boolean reopen(ServerPlayer player, EquipmentSlot slot, boolean preserve, double mouseX, double mouseY) {
        if (!isSupportedSlot(slot) || !Double.isFinite(mouseX) || !Double.isFinite(mouseY)) return false;
        if (player.containerMenu != player.inventoryMenu
                && (!(player.containerMenu instanceof InstallSalvageMenu menu) || !menu.stillValid(player))) return false;
        Context context = current(player);
        return context != null && open(player, context, slot, preserve, mouseX, mouseY);
    }

    private static boolean open(ServerPlayer player, Context context, EquipmentSlot slot,
                                boolean preserve, double mouseX, double mouseY) {
        return player.openMenu(new SimpleMenuProvider((id, inventory, owner) ->
                new InstallSalvageMenu(id, inventory, slot, preserve, mouseX, mouseY, context),
                Component.translatable(MPSConstants.GUI_INSTALL_SALVAGE)), buf -> {
            buf.writeEnum(slot);
            buf.writeBoolean(preserve);
            buf.writeDouble(mouseX);
            buf.writeDouble(mouseY);
        }).isPresent();
    }

    private InstallationAccess() {}
}
