package com.hbm.render.tileentity;

import java.util.Random;
import org.lwjgl.opengl.GL11;
import com.hbm.blocks.BlockDummyable;
import com.hbm.main.ResourceManager;
import com.hbm.tileentity.machine.TileEntityAMSBase;
import com.hbm.tileentity.machine.TileEntityAMSEmitter;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import net.minecraftforge.client.IItemRenderer;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.blocks.ModBlocks;
import com.hbm.render.tileentity.IItemRendererProvider;
import net.minecraft.util.AxisAlignedBB;

public class RenderAMSEmitter extends TileEntitySpecialRenderer implements IItemRendererProvider {

	@Override
	public Item getItemForRenderer() {
		return Item.getItemFromBlock(ModBlocks.ams_emitter);
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
				bindTexture(ResourceManager.ams_emitter_tex);
				ResourceManager.ams_emitter.renderAll();
				GL11.glShadeModel(GL11.GL_FLAT);
			}
		};
	}

	@Override
	public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float interp) {
		int meta = tile.getBlockMetadata();
		if (meta < 12) return;

		GL11.glPushMatrix();
		GL11.glTranslated(x + 0.5, y, z + 0.5);
		GL11.glEnable(GL11.GL_LIGHTING);

		int dir = meta - 10;
		int rotation = 0;
		if (dir == 2) rotation = 180;
		if (dir == 4) rotation = 90;
		if (dir == 5) rotation = 270;

		GL11.glRotatef(rotation, 0.0F, 1.0F, 0.0F);

		bindTexture(ResourceManager.ams_emitter_tex);
		ResourceManager.ams_emitter.renderAll();

		GL11.glPopMatrix();
		
		renderBeam((TileEntityAMSEmitter)tile, x, y, z);
	}

	private void renderBeam(TileEntityAMSEmitter emitter, double x, double y, double z) {
		if (emitter.locked || emitter.efficiency <= 0) return;

		int baseIndex = -1;
		for (int dy = 1; dy <= 16; dy++) {
			TileEntity te = emitter.getWorldObj().getTileEntity(emitter.xCoord, emitter.yCoord - dy, emitter.zCoord);
			if (te instanceof TileEntityAMSBase) {
				baseIndex = dy;
				break;
			}
		}

		if (baseIndex == -1) return;

		float radius = 0.04F;
		int layers = 3;
		Tessellator tessellator = Tessellator.instance;
		Random rand = new Random(emitter.xCoord ^ emitter.yCoord ^ emitter.zCoord ^ emitter.getWorldObj().getTotalWorldTime());

		GL11.glPushMatrix();
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		
		GL11.glTranslated(x + 0.5D, y + 0.1D, z + 0.5D);

		double lastPosX = 0;
		double lastPosZ = 0;
		double step = 0.5D;
		double totalDist = baseIndex - 1.5D;

		for (double d = 0; d < totalDist; d += step) {
			double posX = (rand.nextDouble() - 0.5D) * 0.15D;
			double posZ = (rand.nextDouble() - 0.5D) * 0.15D;
			
			if (d + step >= totalDist) {
				posX = 0;
				posZ = 0;
			}

			for (int j = 1; j <= layers; j++) {
				tessellator.startDrawingQuads();
				tessellator.setColorRGBA_F(1.0F, 0.5F, 0.0F, 0.8F);
				
				tessellator.addVertex(lastPosX + (radius * j), -d, lastPosZ + (radius * j));
				tessellator.addVertex(lastPosX + (radius * j), -d, lastPosZ - (radius * j));
				tessellator.addVertex(posX + (radius * j), -(d + step), posZ - (radius * j));
				tessellator.addVertex(posX + (radius * j), -(d + step), posZ + (radius * j));
				
				tessellator.addVertex(lastPosX - (radius * j), -d, lastPosZ + (radius * j));
				tessellator.addVertex(lastPosX - (radius * j), -d, lastPosZ - (radius * j));
				tessellator.addVertex(posX - (radius * j), -(d + step), posZ - (radius * j));
				tessellator.addVertex(posX - (radius * j), -(d + step), posZ + (radius * j));
				
				tessellator.addVertex(lastPosX + (radius * j), -d, lastPosZ + (radius * j));
				tessellator.addVertex(lastPosX - (radius * j), -d, lastPosZ + (radius * j));
				tessellator.addVertex(posX - (radius * j), -(d + step), posZ + (radius * j));
				tessellator.addVertex(posX + (radius * j), -(d + step), posZ + (radius * j));
				
				tessellator.addVertex(lastPosX + (radius * j), -d, lastPosZ - (radius * j));
				tessellator.addVertex(lastPosX - (radius * j), -d, lastPosZ - (radius * j));
				tessellator.addVertex(posX - (radius * j), -(d + step), posZ - (radius * j));
				tessellator.addVertex(posX + (radius * j), -(d + step), posZ - (radius * j));
				
				tessellator.draw();
			}

			lastPosX = posX;
			lastPosZ = posZ;
		}

		for (int j = 1; j <= 2; j++) {
			tessellator.startDrawingQuads();
			tessellator.setColorRGBA_F(1.0F, 1.0F, 0.0F, 0.5F);
			
			tessellator.addVertex(0 + (radius * j), 0, 0 + (radius * j));
			tessellator.addVertex(0 + (radius * j), 0, 0 - (radius * j));
			tessellator.addVertex(0 + (radius * j), -totalDist, 0 - (radius * j));
			tessellator.addVertex(0 + (radius * j), -totalDist, 0 + (radius * j));
			
			tessellator.addVertex(0 - (radius * j), 0, 0 + (radius * j));
			tessellator.addVertex(0 - (radius * j), 0, 0 - (radius * j));
			tessellator.addVertex(0 - (radius * j), -totalDist, 0 - (radius * j));
			tessellator.addVertex(0 - (radius * j), -totalDist, 0 + (radius * j));
			
			tessellator.addVertex(0 + (radius * j), 0, 0 + (radius * j));
			tessellator.addVertex(0 - (radius * j), 0, 0 + (radius * j));
			tessellator.addVertex(0 - (radius * j), -totalDist, 0 + (radius * j));
			tessellator.addVertex(0 + (radius * j), -totalDist, 0 + (radius * j));
			
			tessellator.addVertex(0 + (radius * j), 0, 0 - (radius * j));
			tessellator.addVertex(0 - (radius * j), 0, 0 - (radius * j));
			tessellator.addVertex(0 - (radius * j), -totalDist, 0 - (radius * j));
			tessellator.addVertex(0 + (radius * j), -totalDist, 0 - (radius * j));
			
			tessellator.draw();
		}

		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glPopMatrix();
	}
}
