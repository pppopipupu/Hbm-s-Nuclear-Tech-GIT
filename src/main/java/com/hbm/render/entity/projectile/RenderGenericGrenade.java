package com.hbm.render.entity.projectile;

import com.hbm.entity.grenade.EntityDisperserCanister;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.hbm.entity.grenade.IGenericGrenade;
import com.hbm.inventory.fluid.FluidType;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

import com.hbm.render.shader.Shader;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.lib.RefStrings;
import com.hbm.entity.grenade.EntityGrenadeBouncyGeneric;
import com.hbm.items.special.ItemQGPMiningBomb;
import net.minecraft.init.Blocks;

public class RenderGenericGrenade extends Render {

	private static Shader qgpShader;
	private static Shader rainbowTntShader;

	@Override
	public void doRender(Entity entity, double x, double y, double z, float f0, float f1) {
		
		boolean disperser = entity instanceof EntityDisperserCanister;
		
		for(int i = 0; i < (disperser ? 2 : 1); i++) {
			
			IIcon iicon;
			if(disperser){
				EntityDisperserCanister canister = (EntityDisperserCanister) entity;
				FluidType fluid = canister.getFluid();
				iicon = canister.getType().getIconFromDamageForRenderPass(fluid.getID(), i);
				
				if(i == 1) {
					int hex = fluid.getColor();
					int r = (hex & 0xFF0000) >> 16;
					int g = (hex & 0xFF00) >> 8;
					int b = (hex & 0xFF);
					GL11.glColor3b((byte) (r / 2), (byte) (g / 2), (byte) (b / 2));
				}
				
			} else {
				IGenericGrenade grenade = (IGenericGrenade) entity;
				if(grenade.getGrenade() instanceof ItemQGPMiningBomb) {
					iicon = Blocks.tnt.getIcon(2, 0);
				} else {
					iicon = grenade.getGrenade().getIconFromDamage(i);
				}
			}
	
			if(iicon != null) {
				boolean isQGP = false;
				boolean isRainbowTNT = false;
				if(disperser && i == 1 && entity instanceof EntityDisperserCanister) {
					EntityDisperserCanister canister = (EntityDisperserCanister) entity;
					if(canister.getFluid() == Fluids.QGP) {
						isQGP = true;
					}
				}
				if(entity instanceof EntityGrenadeBouncyGeneric) {
					EntityGrenadeBouncyGeneric bouncy = (EntityGrenadeBouncyGeneric) entity;
					if(bouncy.getGrenade() instanceof ItemQGPMiningBomb) {
						isRainbowTNT = true;
					}
				}
				if(isQGP) {
					if(qgpShader == null) {
						qgpShader = new Shader(new ResourceLocation(RefStrings.MODID, "shaders/qgp.vert"), new ResourceLocation(RefStrings.MODID, "shaders/qgp.frag"));
					}
					qgpShader.use();
					qgpShader.setUniform1f("iTime", (System.currentTimeMillis() % 100000) / 1000.0F);
				}
				if(isRainbowTNT) {
					if(rainbowTntShader == null) {
						rainbowTntShader = new Shader(new ResourceLocation(RefStrings.MODID, "shaders/qgp.vert"), new ResourceLocation(RefStrings.MODID, "shaders/rainbow_tnt.frag"));
					}
					rainbowTntShader.use();
					rainbowTntShader.setUniform1f("iTime", (System.currentTimeMillis() % 100000) / 1000.0F);
				}

				GL11.glPushMatrix();
				GL11.glTranslatef((float) x, (float) y, (float) z);
				GL11.glEnable(GL12.GL_RESCALE_NORMAL);
				GL11.glScalef(0.5F, 0.5F, 0.5F);
				this.bindEntityTexture(entity);
				Tessellator tessellator = Tessellator.instance;
	
				this.renderItem(tessellator, iicon);
				GL11.glDisable(GL12.GL_RESCALE_NORMAL);
				GL11.glPopMatrix();

				if(isQGP) {
					qgpShader.stop();
				}
				if(isRainbowTNT) {
					rainbowTntShader.stop();
				}
			}
			
			GL11.glColor3f(1F, 1F, 1F);
		}
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity entity) {
		if(entity instanceof EntityGrenadeBouncyGeneric) {
			EntityGrenadeBouncyGeneric bouncy = (EntityGrenadeBouncyGeneric) entity;
			if(bouncy.getGrenade() instanceof ItemQGPMiningBomb) {
				return TextureMap.locationBlocksTexture;
			}
		}
		return TextureMap.locationItemsTexture;
	}

	private void renderItem(Tessellator tess, IIcon icon) {
		float minU = icon.getMinU();
		float maxU = icon.getMaxU();
		float minV = icon.getMinV();
		float maxV = icon.getMaxV();
		float max = 1.0F;
		float offX = 0.5F;
		float offY = 0.25F;
		
		GL11.glRotatef(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
		GL11.glRotatef(-this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
		
		tess.startDrawingQuads();
		tess.setNormal(0.0F, 1.0F, 0.0F);
		tess.addVertexWithUV((double) (0.0F - offX), (double) (0.0F - offY), 0.0D, (double) minU, (double) maxV);
		tess.addVertexWithUV((double) (max - offX), (double) (0.0F - offY), 0.0D, (double) maxU, (double) maxV);
		tess.addVertexWithUV((double) (max - offX), (double) (max - offY), 0.0D, (double) maxU, (double) minV);
		tess.addVertexWithUV((double) (0.0F - offX), (double) (max - offY), 0.0D, (double) minU, (double) minV);
		tess.draw();
	}
}
