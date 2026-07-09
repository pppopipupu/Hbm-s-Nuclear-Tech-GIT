package com.hbm.inventory.gui;

import org.lwjgl.opengl.GL11;
import com.hbm.inventory.container.ContainerAMSBase;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityAMSBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

public class GUIAMSBase extends GuiInfoContainer {

	private static final ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":textures/gui/gui_ams_base.png");
	private final TileEntityAMSBase ams;

	public GUIAMSBase(InventoryPlayer invPlayer, TileEntityAMSBase ams) {
		super(new ContainerAMSBase(invPlayer, ams));
		this.ams = ams;
		xSize = 176;
		ySize = 222;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float f) {
		super.drawScreen(mouseX, mouseY, f);

		ams.deuterium.renderTankInfo(this, mouseX, mouseY, guiLeft + 26, guiTop + 70 - 52, 16, 52);
		ams.tritium.renderTankInfo(this, mouseX, mouseY, guiLeft + 134, guiTop + 70 - 52, 16, 52);
		ams.coolant.renderTankInfo(this, mouseX, mouseY, guiLeft + 26, guiTop + 124 - 52, 16, 52);
		ams.amat.renderTankInfo(this, mouseX, mouseY, guiLeft + 134, guiTop + 124 - 52, 16, 52);
		
		this.drawElectricityInfo(this, mouseX, mouseY, guiLeft + 116, guiTop + 124 - 106, 7, 106, ams.power, TileEntityAMSBase.maxPower);
		this.drawCustomInfo(this, mouseX, mouseY, guiLeft + 44, guiTop + 124 - 106, 7, 106, new String[] { "Restriction Field:", ams.field + "%" });
		this.drawCustomInfo(this, mouseX, mouseY, guiLeft + 53, guiTop + 124 - 106, 7, 106, new String[] { "Efficiency:", ams.efficiency + "%" });
		this.drawCustomInfo(this, mouseX, mouseY, guiLeft + 125, guiTop + 124 - 106, 7, 106, new String[] { "Heat:", ams.heat + "/" + TileEntityAMSBase.maxHeat + "C" });
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int i, int j) {
		String name = ams.hasCustomInventoryName() ? ams.getInventoryName() : I18n.format(ams.getInventoryName());
		this.fontRendererObj.drawString(name, 97 - this.fontRendererObj.getStringWidth(name) / 2, 5, 4210752);
		this.fontRendererObj.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float interp, int x, int y) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		// Render fluid tanks
		ams.deuterium.renderTank(guiLeft + 26, guiTop + 70, this.zLevel, 16, 52);
		ams.tritium.renderTank(guiLeft + 134, guiTop + 70, this.zLevel, 16, 52);
		ams.coolant.renderTank(guiLeft + 26, guiTop + 124, this.zLevel, 16, 52);
		ams.amat.renderTank(guiLeft + 134, guiTop + 124, this.zLevel, 16, 52);

		// Power bar
		int powerHeight = (int) (ams.power * 106 / TileEntityAMSBase.maxPower);
		if (powerHeight > 0) {
			drawTexturedModalRect(guiLeft + 116, guiTop + 124 - powerHeight, 206, 106 - powerHeight, 7, powerHeight);
		}

		// Field bar
		int fieldHeight = ams.field * 106 / 100;
		if (fieldHeight > 0) {
			drawTexturedModalRect(guiLeft + 44, guiTop + 124 - fieldHeight, 192, 106 - fieldHeight, 7, fieldHeight);
		}

		// Efficiency bar
		int effHeight = ams.efficiency * 106 / 100;
		if (effHeight > 0) {
			drawTexturedModalRect(guiLeft + 53, guiTop + 124 - effHeight, 199, 106 - effHeight, 7, effHeight);
		}

		// Heat bar
		int heatHeight = ams.heat * 106 / TileEntityAMSBase.maxHeat;
		if (heatHeight > 0) {
			drawTexturedModalRect(guiLeft + 125, guiTop + 124 - heatHeight, 213, 106 - heatHeight, 7, heatHeight);
		}

		// Warning indicator
		int w = ams.warning;
		if (w > 0) {
			drawTexturedModalRect(guiLeft + 80, guiTop + 18, 176, 32 + 16 * w, 16, 16);
		}
	}
}
