package com.hbm.render.item;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL20;

import com.hbm.lib.RefStrings;
import com.hbm.render.shader.Shader;
import com.hbm.render.util.RenderItemStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;

public class ItemRenderUltimateUpgrade implements IItemRenderer {

	private static Shader ultimateShader = null;

	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type) {
		return type != ItemRenderType.FIRST_PERSON_MAP;
	}

	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
		return type == ItemRenderType.ENTITY;
	}

	@Override
	public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
		Minecraft mc = Minecraft.getMinecraft();
		boolean hasFbo = OpenGlHelper.isFramebufferEnabled() && mc.getFramebuffer() != null;
		int screenTex = hasFbo ? mc.getFramebuffer().framebufferTexture : 0;
		float screenWidth = hasFbo ? mc.getFramebuffer().framebufferTextureWidth : mc.displayWidth;
		float screenHeight = hasFbo ? mc.getFramebuffer().framebufferTextureHeight : mc.displayHeight;

		GL11.glPushMatrix();

		IIcon iicon = null;
		if (data.length > 1 && data[1] instanceof EntityLivingBase) {
			iicon = ((EntityLivingBase) data[1]).getItemIcon(item, 0);
		}
		if (iicon == null) {
			iicon = item.getItem().getIcon(item, 0);
		}

		if (iicon != null) {
			// ==================== 第一步：绘制物品本体（普通贴图，无Shader） ====================
			mc.getTextureManager().bindTexture(mc.getTextureManager().getResourceLocation(item.getItemSpriteNumber()));
			TextureUtil.func_152777_a(false, false, 1.0F);

			if (type == ItemRenderType.INVENTORY) {
				RenderItem.getInstance().renderIcon(0, 0, iicon, 16, 16);
			} else {
				Tessellator tessellator = Tessellator.instance;
				GL11.glPushMatrix();
				if (type == ItemRenderType.ENTITY) {
					GL11.glTranslated(-0.5, -0.5, 0.0);
				}
				ItemRenderer.renderItemIn2D(tessellator, iicon.getMaxU(), iicon.getMinV(), iicon.getMinU(), iicon.getMaxV(), iicon.getIconWidth(), iicon.getIconHeight(), 0.0625F);
				GL11.glPopMatrix();
			}

			// ==================== 第二步：叠加绘制大黑洞 Quad Canvas（绑定Shader） ====================
			if (hasFbo) {
				OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit + 1);
				GL11.glEnable(GL11.GL_TEXTURE_2D);
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, screenTex);
				OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
			}
			
			if (ultimateShader == null) {
				ultimateShader = new Shader(new ResourceLocation(RefStrings.MODID, "shaders/distortion.frag"));
			}

			ultimateShader.use();
			ultimateShader.setUniform1f("iTime", (System.currentTimeMillis() % 100000) / 1000.0F);
			ultimateShader.setUniform1i("u_screenTexture", 1);
			ultimateShader.setUniform1f("u_screenWidth", screenWidth);
			ultimateShader.setUniform1f("u_screenHeight", screenHeight);
			ultimateShader.setUniform1i("u_hasFbo", hasFbo ? 1 : 0);
			int loc = ultimateShader.getUniformLocation("u_iconUvRange");
			GL20.glUniform4f(loc, iicon.getMinU(), iicon.getMaxU(), iicon.getMinV(), iicon.getMaxV());

			GL11.glDisable(GL11.GL_CULL_FACE);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GL11.glDepthMask(false);

			Tessellator tess = Tessellator.instance;
			if (type == ItemRenderType.INVENTORY) {
				tess.startDrawingQuads();
				tess.addVertexWithUV(-16.0, 32.0, 0.0, -1.0, 2.0);
				tess.addVertexWithUV(32.0, 32.0, 0.0, 2.0, 2.0);
				tess.addVertexWithUV(32.0, -16.0, 0.0, 2.0, -1.0);
				tess.addVertexWithUV(-16.0, -16.0, 0.0, -1.0, -1.0);
				tess.draw();
			} else {
				double offset = (type == ItemRenderType.ENTITY) ? 0.01 : 0.07;
				
				// 正向画布
				GL11.glPushMatrix();
				if (type == ItemRenderType.ENTITY) {
					GL11.glTranslated(0.0, 0.0, offset);
				} else {
					GL11.glTranslated(0.5, 0.5, offset);
				}
				tess.startDrawingQuads();
				tess.addVertexWithUV(-1.5, -1.5, 0.0, -1.0, 2.0);
				tess.addVertexWithUV(1.5, -1.5, 0.0, 2.0, 2.0);
				tess.addVertexWithUV(1.5, 1.5, 0.0, 2.0, -1.0);
				tess.addVertexWithUV(-1.5, 1.5, 0.0, -1.0, -1.0);
				tess.draw();
				GL11.glPopMatrix();

				// 反向画布
				GL11.glPushMatrix();
				if (type == ItemRenderType.ENTITY) {
					GL11.glTranslated(0.0, 0.0, -offset);
				} else {
					GL11.glTranslated(0.5, 0.5, -offset);
				}
				tess.startDrawingQuads();
				tess.addVertexWithUV(-1.5, -1.5, 0.0, -1.0, 2.0);
				tess.addVertexWithUV(1.5, -1.5, 0.0, 2.0, 2.0);
				tess.addVertexWithUV(1.5, 1.5, 0.0, 2.0, -1.0);
				tess.addVertexWithUV(-1.5, 1.5, 0.0, -1.0, -1.0);
				tess.draw();
				GL11.glPopMatrix();
			}

			GL11.glDepthMask(true);
			GL11.glEnable(GL11.GL_CULL_FACE);

			ultimateShader.stop();

			if (hasFbo) {
				OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit + 1);
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
				GL11.glDisable(GL11.GL_TEXTURE_2D);
				OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
			}
		}

		GL11.glPopMatrix();
	}
}
