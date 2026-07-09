package com.hbm.render.tileentity;

import org.lwjgl.opengl.GL11;
import com.hbm.blocks.BlockDummyable;
import com.hbm.main.ResourceManager;
import com.hbm.tileentity.machine.TileEntityAMSBase;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import net.minecraftforge.client.IItemRenderer;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.blocks.ModBlocks;
import com.hbm.render.tileentity.IItemRendererProvider;
import com.hbm.items.machine.ItemCatalyst;
import com.hbm.items.special.ItemAMSCore;
import com.hbm.items.ModItems;

public class RenderAMSBase extends TileEntitySpecialRenderer implements IItemRendererProvider {

	@Override
	public Item getItemForRenderer() {
		return Item.getItemFromBlock(ModBlocks.ams_base);
	}

	@Override
	public IItemRenderer getRenderer() {
		return new ItemRenderBase() {
			@Override
			public void renderInventory() {
				GL11.glTranslated(0, -1.2, 0);
				GL11.glScaled(1.6, 1.6, 1.6);
			}
			@Override
			public void renderCommonWithStack(ItemStack item) {
				GL11.glShadeModel(GL11.GL_SMOOTH);
				bindTexture(ResourceManager.ams_base_tex);
				ResourceManager.ams_base.renderAll();
				GL11.glShadeModel(GL11.GL_FLAT);
			}
		};
	}

	@Override
	public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float interp) {
		int meta = tile.getBlockMetadata();
		if (meta < 12) return; // only render on core block

		GL11.glPushMatrix();
		GL11.glTranslated(x + 0.5, y, z + 0.5);
		GL11.glEnable(GL11.GL_LIGHTING);

		int dir = meta - 10;
		int rotation = 0;
		if (dir == 2) rotation = 180;
		if (dir == 4) rotation = 90;
		if (dir == 5) rotation = 270;

		GL11.glRotatef(rotation, 0.0F, 1.0F, 0.0F);

		bindTexture(ResourceManager.ams_base_tex);
		ResourceManager.ams_base.renderAll();

		GL11.glPopMatrix();

		TileEntityAMSBase ams = (TileEntityAMSBase) tile;

		boolean isPPPOP = ams.slots[12] != null && ams.slots[12].getItem() == ModItems.gun_pppop;

		if(isPPPOP) {
			renderGreenOrb(ams, x, y, z);
		} else {
			boolean hasCatalysts = ams.slots[8] != null && ams.slots[8].getItem() instanceof ItemCatalyst
					&& ams.slots[9] != null && ams.slots[9].getItem() instanceof ItemCatalyst
					&& ams.slots[10] != null && ams.slots[10].getItem() instanceof ItemCatalyst
					&& ams.slots[11] != null && ams.slots[11].getItem() instanceof ItemCatalyst;

			boolean hasCore = ams.slots[12] != null && ams.slots[12].getItem() instanceof ItemAMSCore;

			boolean isRunning = ams.field > 0 && ams.efficiency > 0 && hasCatalysts && hasCore 
					&& ams.deuterium.getFill() > 0 && ams.tritium.getFill() > 0;

			if(isRunning) {
				renderOrb(ams, x, y, z);
			}
		}
	}

	private void renderGreenOrb(TileEntityAMSBase tile, double x, double y, double z) {
		GL11.glPushMatrix();
		GL11.glTranslated(x + 0.5, y + 2.5, z + 0.5);

		float r = 0.1F, g = 1.0F, b = 0.1F;

		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		GL11.glDepthMask(false);

		GL11.glColor4f(r, g, b, 1.0F);
		bindTexture(ResourceManager.fusion_plasma_tex);
		
		long time = System.currentTimeMillis();
		double mainOsc = (time / 1000D) % 1.0D;
		
		GL11.glMatrixMode(GL11.GL_TEXTURE);
		GL11.glPushMatrix();
		GL11.glLoadIdentity();
		GL11.glTranslated(0, mainOsc, 0);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		
		float baseScale = 2.5F;
		GL11.glScalef(baseScale, baseScale, baseScale);
		ResourceManager.sphere_ruv.renderAll();
		
		GL11.glMatrixMode(GL11.GL_TEXTURE);
		GL11.glPopMatrix();
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		
		bindTexture(ResourceManager.fusion_plasma_sparkle_tex);
		double ix = (tile.getWorldObj().getTotalWorldTime() * 0.15D) % (Math.PI * 2D);
		double t = 0.8F;
		float pulse = (float) ((1D / t) * Math.atan((t * Math.sin(ix)) / (1 - t * Math.cos(ix))));
		pulse = (pulse + 1F) / 2F;
		
		for(int i = 0; i < 4; i++) {
			GL11.glPushMatrix();
			float s = 1.0F + 0.3F * i + pulse * 0.25F;
			GL11.glScalef(s, s, s);
			GL11.glColor4f(r * 2.0F, g * 2.0F, b * 2.0F, 0.5F - (i * 0.08F));
			ResourceManager.sphere_ruv.renderAll();
			GL11.glPopMatrix();
		}

		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glDepthMask(true);
		
		GL11.glPopMatrix();
	}

	private void renderOrb(TileEntityAMSBase tile, double x, double y, double z) {
		GL11.glPushMatrix();
		GL11.glTranslated(x + 0.5, y + 1.5, z + 0.5);

		int rSum = 0, gSum = 0, bSum = 0;
		int count = 0;
		for(int i = 8; i <= 11; i++) {
			ItemStack cat = tile.slots[i];
			if(cat != null && cat.getItem() instanceof ItemCatalyst) {
				int color = ((ItemCatalyst)cat.getItem()).getColor();
				rSum += (color & 0xFF0000) >> 16;
				gSum += (color & 0x00FF00) >> 8;
				bSum += (color & 0x0000FF);
				count++;
			}
		}
		
		float r = 1.0F, g = 1.0F, b = 1.0F;
		if(count > 0) {
			r = (rSum / (float)count) / 255.0F;
			g = (gSum / (float)count) / 255.0F;
			b = (bSum / (float)count) / 255.0F;
		}

		ItemStack coreStack = tile.slots[12];
		float scaleMod = 1.0F;
		if(coreStack != null && coreStack.getItem() instanceof ItemAMSCore) {
			if(coreStack.getItem() == ModItems.ams_core_sing) {
				scaleMod = 1.5F;
			} else if(coreStack.getItem() == ModItems.ams_core_wormhole) {
				scaleMod = 0.8F;
			} else if(coreStack.getItem() == ModItems.ams_core_eyeofharmony) {
				scaleMod = 2.0F;
			} else if(coreStack.getItem() == ModItems.ams_core_thingy) {
				scaleMod = 1.0F;
			}
		}

		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		GL11.glDepthMask(false);

		GL11.glColor4f(r, g, b, 0.8F);
		bindTexture(ResourceManager.fusion_plasma_tex);
		
		long time = System.currentTimeMillis();
		double mainOsc = (time / 1000D) % 1.0D;
		
		GL11.glMatrixMode(GL11.GL_TEXTURE);
		GL11.glPushMatrix();
		GL11.glLoadIdentity();
		GL11.glTranslated(0, mainOsc, 0);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		
		float baseScale = 0.5F * scaleMod;
		GL11.glScalef(baseScale, baseScale, baseScale);
		ResourceManager.sphere_ruv.renderAll();
		
		GL11.glMatrixMode(GL11.GL_TEXTURE);
		GL11.glPopMatrix();
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		
		bindTexture(ResourceManager.fusion_plasma_sparkle_tex);
		double ix = (tile.getWorldObj().getTotalWorldTime() * 0.1D) % (Math.PI * 2D);
		double t = 0.8F;
		float pulse = (float) ((1D / t) * Math.atan((t * Math.sin(ix)) / (1 - t * Math.cos(ix))));
		pulse = (pulse + 1F) / 2F;
		
		for(int i = 0; i < 4; i++) {
			GL11.glPushMatrix();
			float s = 1.0F + 0.3F * i + pulse * 0.2F;
			GL11.glScalef(s, s, s);
			GL11.glColor4f(r * 1.5F, g * 1.5F, b * 1.5F, 0.3F - (i * 0.05F));
			ResourceManager.sphere_ruv.renderAll();
			GL11.glPopMatrix();
		}

		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glDepthMask(true);
		
		GL11.glPopMatrix();
	}
}
