package com.hbm.render.block;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import com.hbm.lib.RefStrings;
import com.hbm.render.shader.Shader;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;

public class RenderQGPBlock implements ISimpleBlockRenderingHandler {

	public static int renderId = -1;
	private static Shader qgpShader = null;

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
		Minecraft mc = Minecraft.getMinecraft();
		boolean hasFbo = OpenGlHelper.isFramebufferEnabled() && mc.getFramebuffer() != null;
		int screenTex = hasFbo ? mc.getFramebuffer().framebufferTexture : 0;
		float screenWidth = hasFbo ? mc.getFramebuffer().framebufferTextureWidth : mc.displayWidth;
		float screenHeight = hasFbo ? mc.getFramebuffer().framebufferTextureHeight : mc.displayHeight;

		if (hasFbo) {
			OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit + 1);
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, screenTex);
			OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
		}

		if (qgpShader == null) {
			qgpShader = new Shader(new ResourceLocation(RefStrings.MODID, "shaders/default.vert"), new ResourceLocation(RefStrings.MODID, "shaders/distortion.frag"));
		}

		qgpShader.use();
		qgpShader.setUniform1f("iTime", (System.currentTimeMillis() % 100000) / 1000.0F);
		qgpShader.setUniform1i("u_screenTexture", 1);
		qgpShader.setUniform1f("u_screenWidth", screenWidth);
		qgpShader.setUniform1f("u_screenHeight", screenHeight);
		qgpShader.setUniform1i("u_hasFbo", hasFbo ? 1 : 0);
		
		int loc = qgpShader.getUniformLocation("u_iconUvRange");
		GL20.glUniform4f(loc, 0.0F, 1.0F, 0.0F, 1.0F);

		boolean result = renderer.renderBlockLiquid(block, x, y, z);

		qgpShader.stop();

		if (hasFbo) {
			OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit + 1);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
		}
		
		return result;
	}

	@Override
	public boolean shouldRender3DInInventory(int modelId) {
		return false;
	}

	@Override
	public int getRenderId() {
		return renderId;
	}
}
