package com.hbm.render.tileentity;

import org.lwjgl.opengl.GL11;
import com.hbm.blocks.BlockDummyable;
import com.hbm.main.ResourceManager;
import com.hbm.tileentity.machine.TileEntityAMSBase;
import com.hbm.tileentity.machine.TileEntityAMSLimiter;
import net.minecraft.client.renderer.RenderHelper;
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
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraft.world.World;

public class RenderAMSLimiter extends TileEntitySpecialRenderer implements IItemRendererProvider {

	@Override
	public Item getItemForRenderer() {
		return Item.getItemFromBlock(ModBlocks.ams_limiter);
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
				bindTexture(ResourceManager.ams_limiter_tex);
				ResourceManager.ams_limiter.renderAll();
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

		bindTexture(ResourceManager.ams_limiter_tex);
		ResourceManager.ams_limiter.renderAll();

		GL11.glPopMatrix();

		renderMagneticBeam((TileEntityAMSLimiter)tile, x, y, z);
	}

	private void renderMagneticBeam(TileEntityAMSLimiter limiter, double x, double y, double z) {
		if (limiter.locked) return;

		TileEntityAMSBase base = null;
		ForgeDirection baseDir = ForgeDirection.UNKNOWN;
		int baseDist = -1;
		
		ForgeDirection[] axes = {ForgeDirection.EAST, ForgeDirection.WEST, ForgeDirection.SOUTH, ForgeDirection.NORTH};
		for (ForgeDirection d : axes) {
			for (int r = 1; r <= 8; r++) {
				TileEntity te = limiter.getWorldObj().getTileEntity(limiter.xCoord + d.offsetX * r, limiter.yCoord, limiter.zCoord + d.offsetZ * r);
				if (te instanceof TileEntityAMSBase) {
					base = (TileEntityAMSBase) te;
					baseDir = d;
					baseDist = r;
					break;
				}
			}
			if (base != null) break;
		}

		if (base == null) return;

		Tessellator tessellator = Tessellator.instance;
		GL11.glPushMatrix();
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		GL11.glDepthMask(false);
		GL11.glShadeModel(GL11.GL_SMOOTH);
		RenderHelper.disableStandardItemLighting();

		double sx = x + 0.5D;
		double sy = y + 5.5D;
		double sz = z + 0.5D;

		double ex = x + baseDir.offsetX * baseDist + 0.5D;
		double ey = y - 4.0D + 5.5D;
		double ez = z + baseDir.offsetZ * baseDist + 0.5D;

		double r = 0.08D;
		if (limiter.efficiency > 0) {
			r = 0.16D;
		}

		for (int i = 0; i < 4; i++) {
			double angle = i * Math.PI / 4.0;
			double ox = Math.cos(angle) * r;
			double oz = Math.sin(angle) * r;

			tessellator.startDrawingQuads();
			tessellator.setColorRGBA_F(0.233F, 0.511F, 0.511F, 0.6F);
			tessellator.addVertex(sx - ox, sy, sz - oz);
			tessellator.addVertex(sx + ox, sy, sz + oz);
			
			tessellator.setColorRGBA_F(0.233F, 0.511F, 0.511F, 0.0F);
			tessellator.addVertex(ex + ox, ey, ez + oz);
			tessellator.addVertex(ex - ox, ey, ez - oz);
			tessellator.draw();
		}

		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glDepthMask(true);
		GL11.glShadeModel(GL11.GL_FLAT);
		RenderHelper.enableStandardItemLighting();
		GL11.glPopMatrix();
	}
}
