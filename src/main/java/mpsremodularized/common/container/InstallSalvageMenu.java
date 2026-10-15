package mpsremodularized.common.container;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import mpsremodularized.MPSReModularized;
import mpsremodularized.common.base.MPSLogger;
import mpsremodularized.common.capabilities.inventory.modularitem.IModularItem;
import mpsremodularized.common.capabilities.module.powermodule.ModuleCategory;
import mpsremodularized.common.container.slot.CategoryIconSlotItemHandler;
import mpsremodularized.common.container.slot.EquipmentIconSlotItemHandler;
import mpsremodularized.common.container.slot.HideableSlot;
import mpsremodularized.common.container.slot.HideableSlotItemHandler;
import mpsremodularized.common.container.slot.IHideableSlot;
import mpsremodularized.common.container.slot.IIConProvider;
import mpsremodularized.common.container.slot.SlotBackgrounds;
import mpsremodularized.common.math.Color;
import mpsremodularized.common.registration.MPSCapabilities;
import mpsremodularized.common.utils.MathUtils;
import mpsremodularized.common.registration.MPSMenuTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class InstallSalvageMenu extends AbstractContainerMenu {
    EquipmentSlot slotType;
    int mainInventoryStart = 0;
    int hotbarInventoryStart = 0;
    double mouseX;
    double mouseY;
    boolean preserve;
    private final Player owner;
    private final ItemStack targetItem;
    @Nullable private final InstallationAccess.Context access;

    public InstallSalvageMenu(int containerID, Inventory playerInventory, EquipmentSlot slotType) {
        this(containerID, playerInventory, slotType, false, 0, 0);
    }

    public InstallSalvageMenu(int containerID, Inventory playerInventory, EquipmentSlot slotType, boolean preserve, double mouseX, double mouseY) {
        this(containerID, playerInventory, slotType, preserve, mouseX, mouseY, null);
    }

    public InstallSalvageMenu(int containerID, Inventory playerInventory, EquipmentSlot slotType,
                              boolean preserve, double mouseX, double mouseY, @Nullable InstallationAccess.Context access) {
        super(MPSMenuTypes.INSTALL_SALVAGE_MENU_TYPE.get(), containerID);
        this.owner = playerInventory.player;
        this.access = access;
        this.targetItem = owner.getItemBySlot(slotType);
        this.slotType = slotType;
        this.preserve = preserve;
        this.mouseX = mouseX;
        this.mouseY = mouseY;

        int row, col;
        int parentSlot = slotType == EquipmentSlot.MAINHAND ? playerInventory.selected : equipmentSlotToParent(slotType);
        ItemStack modularItemStack = playerInventory.player.getItemBySlot(slotType);

        IModularItem modularItemCapability = MPSCapabilities.getModularItemOrModeChangingCapability(modularItemStack);
        if (modularItemCapability != null) {
            addSlots(modularItemCapability, parentSlot);
        }

        mainInventoryStart = this.slots.size();

        // Main inventory
        for(row = 0; row < 3; ++row) {
            for(col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 178 + col * 18, 133 + row * 18));
            }
        }

        hotbarInventoryStart = this.slots.size();

        // Hotbar with pickup disabled for modular items
        for(col = 0; col < 9; ++col) {
            if (col == playerInventory.selected
                    && MPSCapabilities.getModularItemOrModeChangingCapability(playerInventory.getItem(col)) != null) {
                this.addSlot(new Slot(playerInventory, col, 178 + col * 18, 191) {
                    @Override
                    public boolean mayPickup(Player player) {
                        return false;
                    }
                });
            } else {
                this.addSlot(new Slot(playerInventory, col, 178 + col * 18, 191));
            }
        }

        for (Slot slot : this.slots) {
            if(slot instanceof IHideableSlot) {
                ((IHideableSlot) slot).disable();
            }
        }
    }

    void addSlots(IModularItem cap, int parentSlot) {

        int innerrow = 0;
        int innercol = 0;
        for (int modularItemInvIndex = 0; modularItemInvIndex < cap.getSlots(); modularItemInvIndex ++) {
            innercol = modularItemInvIndex % 16;
            innerrow = (modularItemInvIndex - innercol)/16;
            int finalModularItemInvIndex = modularItemInvIndex;

            if (MathUtils.isIntInRange(cap.getRangeForCategory(ModuleCategory.ARMOR), modularItemInvIndex)) {
                // OFFHAND
                addSlot(new EquipmentIconSlotItemHandler(cap, parentSlot, modularItemInvIndex, 178 + innercol * 18, 14 + innerrow * 18, EquipmentSlot.OFFHAND) {
                    @OnlyIn(Dist.CLIENT)
                    @Override
                    public void drawIconAt(PoseStack matrixStack, double posX, double posY, Color color) {

                    }

                    @Override
                    public boolean mayPlace(ItemStack stack) {
//                        MPSLogger.logDebug("may place <" + stack +"> " + cap.isModuleValidForPlacement(finalModularItemInvIndex, stack));

                        return cap.isModuleValidForPlacement(finalModularItemInvIndex, stack);
                    }
                });

            } else {
                for (ModuleCategory category: ModuleCategory.values()) {
                    // Keep the saved Paxel slot distinct after a tier upgrade without moving existing stacks.
                    if (category == ModuleCategory.NONE && MathUtils.isIntInRange(cap.getRangeForCategory(ModuleCategory.PAXEL), modularItemInvIndex)) continue;
                    if (category == ModuleCategory.NONE && MathUtils.isIntInRange(cap.getRangeForCategory(ModuleCategory.CONSTRUCTION_STICK), modularItemInvIndex)) continue;
                    if((MathUtils.isIntInRange(cap.getRangeForCategory(category), modularItemInvIndex))) {
//                        MPSLogger.logDebug("category " + category +" is in range for slot: " + modularItemInvIndex);
                        if(category.hasContainerIcon()){
                            if(category.usesGuiAtlasIcons()) {
                                addSlot(new CategoryIconSlotItemHandler(cap, parentSlot, finalModularItemInvIndex, 178 + innercol * 18, 14 + innerrow * 18, category) {
                                    @Override
                                    public boolean mayPlace(ItemStack stack) {
//                                        MPSLogger.logDebug("may place <" + stack + "> " + cap.isModuleValidForPlacement(finalModularItemInvIndex, stack));

                                        return cap.isModuleValidForPlacement(finalModularItemInvIndex, stack);
                                    }
                                });
                            } else {
                                addSlot(new HideableSlotItemHandler(cap, parentSlot, modularItemInvIndex, 178 + innercol * 18, 14 + innerrow * 18) {
                                    @Override
                                    @Nullable
                                    public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                                        return SlotBackgrounds.getIconLocationPairForCategory(category);
                                    }

                                    @Override
                                    public boolean mayPlace(ItemStack stack) {
//                                        MPSLogger.logDebug("may place <" + stack +"> " + cap.isModuleValidForPlacement(finalModularItemInvIndex, stack));
                                        return cap.isModuleValidForPlacement(finalModularItemInvIndex, stack);
                                    }
                                });
                            }
                        } else {
                            // Generic slot (Category NONE)
                            addSlot(new HideableSlotItemHandler(cap, parentSlot, modularItemInvIndex, 178 + innercol * 18, 14 + innerrow * 18) {
                                @Override
                                public boolean mayPlace(ItemStack stack) {
//                                    MPSLogger.logDebug("may place <" + stack +"> " + cap.isModuleValidForPlacement(finalModularItemInvIndex, stack));
                                    return cap.isModuleValidForPlacement(finalModularItemInvIndex, stack);
                                }
                            });
                        }
                    }
                }
            }

        }
    }

    public boolean preserveMouse() {
        return preserve;
    }

    public double getMouseX() {
        return mouseX;
    }

    public double getMouseY() {
        return mouseY;
    }

    public EquipmentSlot getEquipmentSlot() {
        return slotType;
    }

    int equipmentSlotToParent(EquipmentSlot slotType) {
        switch (slotType) {
        case HEAD:
            return 39;
        case CHEST:
            return 38;
        case LEGS:
            return 37;
        case FEET:
            return 36;
        case OFFHAND:
            return 40;
        default:
            return  -1;
        }
    }

    public int getMainInventoryStart() {
        return mainInventoryStart;
    }

    /** Display the Paxel beside the reserved tools without changing saved or network slot indices. */
    public List<Integer> getModularInventorySlotIndexes() {
        List<Integer> indexes = new ArrayList<>();
        for (int i = 0; i < mainInventoryStart; i++) indexes.add(i);
        IModularItem cap = MPSCapabilities.getModularItemOrModeChangingCapability(targetItem);
        if (cap != null) {
            var paxelRange = cap.getRangeForCategory(ModuleCategory.PAXEL);
            if (paxelRange != null) {
                for (int i = 0; i < mainInventoryStart; i++) {
                    if (slots.get(i).getContainerSlot() == paxelRange.getFirst()) {
                        indexes.remove(Integer.valueOf(i));
                        indexes.add(Math.min(5, indexes.size()), i);
                        break;
                    }
                }
            }
            var stickRange = cap.getRangeForCategory(ModuleCategory.CONSTRUCTION_STICK);
            if (stickRange != null) {
                for (int i = 0; i < mainInventoryStart; i++) {
                    if (slots.get(i).getContainerSlot() == stickRange.getFirst()) {
                        indexes.remove(Integer.valueOf(i));
                        indexes.add(Math.min(paxelRange == null ? 5 : 6, indexes.size()), i);
                        break;
                    }
                }
            }
        }
        return indexes;
    }

    public int getHotbarInventoryStart() {
        return hotbarInventoryStart;
    }

    // InventoryMenu version
    @Override
    public void removed(Player player) {
        super.removed(player);
    }


    // player container version
    @Override
    public boolean stillValid(Player player) {
        // Client menus are display views; server menus retain the interaction and exact edited stack.
        return player.level().isClientSide() || player == owner && access != null
                && access.isValid(player) && player.getItemBySlot(slotType) == targetItem;
    }

    @Override
    public void clicked(int slot, int button, net.minecraft.world.inventory.ClickType type, Player player) {
        if (!stillValid(player)) return;
        // Native swap actions read the source equipment directly, bypassing its inventory slot pickup check.
        if (type == net.minecraft.world.inventory.ClickType.SWAP) {
            if (button >= 0 && button < 9 && !getSlot(hotbarInventoryStart + button).mayPickup(player)) return;
            if (button == 40 && (slotType == EquipmentSlot.OFFHAND
                    && MPSCapabilities.getModularItemOrModeChangingCapability(targetItem) != null
                    || access != null && access.protectsSlot(EquipmentSlot.OFFHAND))) return;
        }
        super.clicked(slot, button, type, player);
    }


    /**
     * Only handles shift clicking
     * @param player
     * @param index
     * @return
     */
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (!stillValid(player) || index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemStack1 = slot.getItem();
            itemStack = itemStack1.copy();
            // hotbar
            if (index < mainInventoryStart) {
                if (!this.moveItemStackTo(itemStack1, mainInventoryStart, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }

            } else if (!this.moveItemStackTo(itemStack1, 0, mainInventoryStart, false)) {
                return ItemStack.EMPTY;
            }

            if (itemStack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemStack;
    }


    abstract static class IconSlot extends HideableSlot implements IIConProvider {
        public IconSlot(Container container, int parent, int index, int xPosition, int yPosition) {
            super(container, parent, index, xPosition, yPosition);
        }

        public IconSlot(Container container, int parent, int index, int xPosition, int yPosition, boolean isEnabled) {
            super(container, parent, index, xPosition, yPosition, isEnabled);
        }
    }
}

