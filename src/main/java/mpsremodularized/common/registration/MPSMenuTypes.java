package mpsremodularized.common.registration;

import java.util.function.Supplier;
import mpsremodularized.common.constants.MPSConstants;
import mpsremodularized.common.container.ArmorStandMenu;
import mpsremodularized.common.container.ChargingBaseMenu;
import mpsremodularized.common.container.InstallSalvageMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MPSMenuTypes {
    /**
     * AbstractContainerMenu Types ----------------------------------------------------------------------------
     */
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(BuiltInRegistries.MENU, MPSConstants.MOD_ID);

    public static final Supplier<MenuType<InstallSalvageMenu>> INSTALL_SALVAGE_MENU_TYPE =
    MENU_TYPES.register(MPSConstants.INSTALL_SALVAGE_CONTAINER_TYPE,
        () -> IMenuTypeExtension.create((windowId, inv, data) -> {
            EquipmentSlot slot = data.readEnum(EquipmentSlot.class);
            boolean preserveMouse = data.readBoolean();
            double mouseX = data.readDouble();
            double mouseY = data.readDouble();
            return new InstallSalvageMenu(windowId, inv, slot,preserveMouse, mouseX, mouseY);
    }));

    public static final Supplier<MenuType<ArmorStandMenu>> ARMOR_STAND_CONTAINER_TYPE = MENU_TYPES.register("armorstand_modding_container",
        () -> IMenuTypeExtension.create((windowId, inv, data) -> {
            int entityID = data.readInt();
            Entity armorStand = inv.player.level().getEntity(entityID);
            if (armorStand instanceof ArmorStand) {
                return new ArmorStandMenu(windowId, inv, (ArmorStand) armorStand);
        }
            return null;
    }));

    public static final Supplier<MenuType<ChargingBaseMenu>> CHARGING_BASE_CONTAINER_TYPE = MENU_TYPES.register(MPSConstants.CHARGING_BASE_REGNAME,
        () -> IMenuTypeExtension.create((windowId, inv, data) -> {
            BlockPos pos = data.readBlockPos();
            return new ChargingBaseMenu(windowId, inv.player, pos);
    }));
}
