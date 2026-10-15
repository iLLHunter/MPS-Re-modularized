package mpsremodularized.client.screen;

import mpsremodularized.client.config.MPSClientConfig;
import mpsremodularized.client.gui.meter.EnergyMeter;
import mpsremodularized.common.constants.MPSConstants;
import mpsremodularized.common.container.ChargingBaseMenu;
import mpsremodularized.common.utils.StringUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ChargingBaseScreen extends AbstractContainerScreen<ChargingBaseMenu> {
    public static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(MPSConstants.MOD_ID, "textures/gui/container/charging_base.png");
    static final Component ENERGYSTRING = Component.translatable(MPSConstants.MOD_ID + ".energy").append(": ");
    EnergyMeter energyMeter;

    public ChargingBaseScreen(ChargingBaseMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        energyMeter = new EnergyMeter(MPSClientConfig::getEnergyMeterConfig);
    }

    @Override
    public void render(GuiGraphics gfx, int pMouseX, int pMouseY, float pPartialTick) {
        renderBackground(gfx, pMouseX, pMouseY, pPartialTick);
        super.render(gfx, pMouseX, pMouseY, pPartialTick);
        this.renderTooltip(gfx, pMouseX, pMouseY);
        energyMeter.draw(gfx, 71 + leftPos, 58 + topPos, menu.getEnergyForMeter());
    }

    @Override
    protected void renderBg(GuiGraphics gfx, float pPartialTick, int pMouseX, int pMouseY) {
        int i = this.leftPos;
        int j = this.topPos;
        gfx.blit(BACKGROUND, i, j, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public void renderLabels(GuiGraphics gfx, int mouseX, int mouseY) {
        super.renderLabels(gfx, mouseX, mouseY);
        gfx.drawString(this.font,
                ENERGYSTRING,
                (imageWidth - 102 - font.width(ENERGYSTRING)),
                this.imageHeight - 108,
                4210752, false);

        String energyString = new StringBuilder()
                .append(StringUtils.formatNumberShort(menu.getEnergy()))
                .append(" FE").toString();

        gfx.drawString(this.font,
                Component.literal(energyString),
                imageWidth -71,
                this.imageHeight - 108,
                4210752, false);
    }
}
