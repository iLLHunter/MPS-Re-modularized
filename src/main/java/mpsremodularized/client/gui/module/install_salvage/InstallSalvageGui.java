package mpsremodularized.client.gui.module.install_salvage;

import com.mojang.blaze3d.systems.RenderSystem;
import mpsremodularized.client.gui.ExtendedContainerScreen;
import mpsremodularized.client.gui.frame.ModularItemSelectionFrameContainered;
import mpsremodularized.client.gui.geometry.MusePoint2D;
import mpsremodularized.client.gui.geometry.Rect;
import mpsremodularized.common.base.MPSLogger;
import mpsremodularized.common.capabilities.inventory.modularitem.IModularItem;
import mpsremodularized.common.capabilities.module.powermodule.IPowerModule;
import mpsremodularized.common.utils.ItemUtils;
import mpsremodularized.client.gui.common.ScrollableInventoryFrame2;
import mpsremodularized.client.gui.common.TabSelectFrame;
import mpsremodularized.common.constants.MPSConstants;
import mpsremodularized.common.container.InstallSalvageMenu;
import mpsremodularized.common.network.MPSPackets;
import mpsremodularized.common.network.packets.serverbound.CreativeInstallPacketServerBound;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Comparator;

@OnlyIn(Dist.CLIENT)
public class InstallSalvageGui extends ExtendedContainerScreen<InstallSalvageMenu> {
    Component modularItemInventoryLabel = Component.translatable("gui." + MPSConstants.MOD_ID + ".modularitem.inventory");
    Component moduleSelectionFrameLabel = Component.translatable(MPSConstants.GUI_COMPATIBLE_MODULES);
    public static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(MPSConstants.MOD_ID, "textures/gui/background/install_salvage.png");
    protected TabSelectFrame tabSelectFrame;
    ModularItemSelectionFrameContainered<InstallSalvageMenu> itemSelectFrame;

    ScrollableInventoryFrame2<InstallSalvageMenu> modularItemInventory;
    protected CompatibleModuleDisplayFrame moduleSelectFrame;

    Inventory playerInventory;

    public InstallSalvageGui(InstallSalvageMenu container, Inventory playerInventory, Component title) {
        super(container, playerInventory, title, 352, 217);
        this.playerInventory = playerInventory;
    }

    public void restoreMouse() {
        if(menu.preserveMouse()) {
            Minecraft mc = Minecraft.getInstance();
            long windowHandle = mc.getWindow().getWindow();
            GLFW.glfwSetCursorPos(windowHandle, menu.getMouseX(), menu.getMouseY());
        }
    }


    @Override
    public void init() {
        super.init();
        this.topPos = Math.max(TabSelectFrame.HEIGHT, this.topPos);
        this.inventoryLabelX = this.imageWidth - 170;
        this.inventoryLabelY = this.imageHeight - 94;
        this.titleLabelX = 12;
        this.titleLabelY = 5;

        // clickable buttons on the top for selecting GUI -------------------------------------------------------------
        tabSelectFrame = new TabSelectFrame(this.leftPos, this.topPos, this.imageWidth, playerInventory.player, 0);
        tabSelectFrame.setPosition(center());
        tabSelectFrame.setBottom(topPos);
        addFrame(tabSelectFrame);

        // the buttons that select the equipped modular item if any ---------------------------------------------------
        itemSelectFrame = new ModularItemSelectionFrameContainered<>(menu, new MusePoint2D(leftPos - 30, topPos), menu.getEquipmentSlot());
        itemSelectFrame.fitCreativeButtons(this.height);
        addFrame(itemSelectFrame);

        /// frame to display and allow selecting of installed modules --------------------------------------------------
        CompatibleModuleDisplayFrame previousModules = moduleSelectFrame;
        moduleSelectFrame = new CompatibleModuleDisplayFrame(itemSelectFrame, new Rect(leftPos + 8, topPos + 13, leftPos + 172, topPos + 208));
        moduleSelectFrame.restoreSelection(previousModules);
        itemSelectFrame.getCreativeInstallButton().setOnPressed(pressed -> {
            itemSelectFrame.getCreativeInstallButton().playDownSound(Minecraft.getInstance().getSoundManager());
            if (moduleSelectFrame.getSelectedModule() != null && itemSelectFrame.selectedType().isPresent()) {
                MPSPackets.sendToServer(new CreativeInstallPacketServerBound(itemSelectFrame.selectedType().get(), ItemUtils.getRegistryName(moduleSelectFrame.getSelectedModule().getModule())));
            }
        });

        // not perfect but better than nothing, I think
        itemSelectFrame.getCreativeInstallAllButton().setOnPressed(pressed->{
            IModularItem modularItemCap = itemSelectFrame.getModularItemCapability();
            if (modularItemCap != null) {
                itemSelectFrame.getCreativeInstallButton().playDownSound(Minecraft.getInstance().getSoundManager());
                NonNullList<ItemStack> compatibleModules = getCompatibleModuleList();
                compatibleModules.sort(Comparator.comparingInt((ItemStack module) ->
                        modularItemCap.getModuleCapability(module).getTier()).reversed());
                for(ItemStack module : compatibleModules) {
                    IPowerModule powerModule = modularItemCap.getModuleCapability(module);
                    if (powerModule != null) {
                        MPSPackets.sendToServer(new CreativeInstallPacketServerBound(itemSelectFrame.selectedType().get(), ItemUtils.getRegistryName(module)));
                    }
                }
            }
        });

        addFrame(moduleSelectFrame);

        // inventory frame for the modular item inventory. Changes with different selected modular items --------------
        modularItemInventory = new ScrollableInventoryFrame2<>(menu, new Rect(178,14,353,118), itemSelectFrame, this.ulGetter());
        addFrame(modularItemInventory);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        // FIXME: replace button with something more native
        if(Objects.requireNonNull(getMinecraft().player).isCreative()) {
            itemSelectFrame.getCreativeInstallButton().enableAndShow();
            itemSelectFrame.getCreativeInstallAllButton().enableAndShow();
        } else {
            itemSelectFrame.getCreativeInstallButton().disableAndHide();
            itemSelectFrame.getCreativeInstallAllButton().disableAndHide();
        }
    }

    NonNullList<ItemStack> getCompatibleModuleList() {
        return moduleSelectFrame.getPossibleItems();
    }

    public Rect2i getJeiSidebarArea() {
        boolean creative = playerInventory.player.isCreative();
        int left = (int) Math.floor(creative
                ? Math.min(itemSelectFrame.left(), Math.min(itemSelectFrame.creativeInstallButton.left(), itemSelectFrame.creativeInstallAllButton.left()))
                : itemSelectFrame.left());
        int right = (int) Math.ceil(creative
                ? Math.max(itemSelectFrame.right(), Math.max(itemSelectFrame.creativeInstallButton.right(), itemSelectFrame.creativeInstallAllButton.right()))
                : itemSelectFrame.right());
        int top = (int) Math.floor(itemSelectFrame.top());
        int bottom = (int) Math.ceil(creative ? itemSelectFrame.creativeInstallAllButton.bottom() : itemSelectFrame.creativeInstallButton.top());
        return new Rect2i(left, top, right - left, bottom - top);
    }

    @Override
    public void render(@Nonnull GuiGraphics gfx, int mouseX, int mouseY, float frameTime) {
        //        this.renderBackground(gfx, mouseX, mouseY, frameTime);
        super.render(gfx, mouseX, mouseY, frameTime);
        this.renderTooltip(gfx, mouseX, mouseY);
    }

    @Override
    public void renderTooltip(GuiGraphics gfx, int mouseX, int mouseY) {
        ItemStack module = moduleSelectFrame.getHoveredModule(mouseX, mouseY);
        if (!module.isEmpty()) {
            gfx.renderTooltip(font, moduleSelectFrame.getToolTip(mouseX, mouseY), module.getTooltipImage(), module, mouseX, mouseY);
            return;
        }
        super.renderTooltip(gfx, mouseX, mouseY);
    }

    @Override
    public void renderLabels(GuiGraphics gfx, int mouseX, int mouseY) {
        gfx.drawString(this.font,
            this.moduleSelectionFrameLabel,
            this.titleLabelX,
            this.titleLabelY,
            4210752, false);

        gfx.drawString(this.font,
            this.modularItemInventoryLabel,
            this.titleLabelX + 173,
            this.titleLabelY, 4210752, false);

        gfx.drawString(this.font,
            this.playerInventory.getDisplayName(),
            this.inventoryLabelX,
            inventoryLabelY,
            4210752,
            false);
    }

    @Override
    protected void renderBg(GuiGraphics gfx, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int i = this.leftPos;
        int j = this.topPos;
        gfx.blit(BACKGROUND, i, j, 0, 0, imageWidth, imageHeight, 512, 512);
        super.renderBg(gfx, partialTick, mouseX, mouseY);
    }
}
