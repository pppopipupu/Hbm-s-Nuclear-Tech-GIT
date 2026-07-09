package com.hbm.render.item.weapon.sedna;

import org.lwjgl.opengl.GL11;
import com.hbm.lib.RefStrings;
import com.hbm.render.shader.Shader;
import com.hbm.inventory.fluid.Fluids;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;

public class ItemRenderQGPDisperser implements IItemRenderer {

	private static Shader qgpShader = null;

	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type) {
		if (item != null && item.getItemDamage() == Fluids.QGP.getID()) {
			return type != ItemRenderType.FIRST_PERSON_MAP;
		}
		return false;
	}

	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
		return type == ItemRenderType.ENTITY;
	}

	@Override
	public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
		Minecraft mc = Minecraft.getMinecraft();
		
		if (qgpShader == null) {
			qgpShader = new Shader(new ResourceLocation(RefStrings.MODID, "shaders/qgp.vert"), new ResourceLocation(RefStrings.MODID, "shaders/qgp.frag"));
		}

		GL11.glPushMatrix();

		if (type == ItemRenderType.ENTITY) {
			GL11.glTranslated(-0.5, -0.5, 0);
		} else if (type == ItemRenderType.INVENTORY) {
			GL11.glScalef(16.0F, 16.0F, 1.0F);
		}

		mc.getTextureManager().bindTexture(mc.getTextureManager().getResourceLocation(item.getItemSpriteNumber()));
		TextureUtil.func_152777_a(false, false, 1.0F);

		// Render Pass 0: The canister body
		IIcon bodyIcon = item.getItem().getIcon(item, 0);
		if (bodyIcon != null) {
			Tessellator tessellator = Tessellator.instance;
			ItemRenderer.renderItemIn2D(tessellator, bodyIcon.getMaxU(), bodyIcon.getMinV(), bodyIcon.getMinU(), bodyIcon.getMaxV(), bodyIcon.getIconWidth(), bodyIcon.getIconHeight(), 0.0625F);
		}

		// Render Pass 1: The liquid overlay
		IIcon liquidIcon = item.getItem().getIcon(item, 1);
		if (liquidIcon != null) {
			boolean isQGP = item.getItemDamage() == Fluids.QGP.getID();
			
			if (isQGP) {
				qgpShader.use();
				qgpShader.setUniform1f("iTime", (System.currentTimeMillis() % 100000) / 1000.0F);
			} else {
				int color = item.getItem().getColorFromItemStack(item, 1);
				float r = ((color & 0xff0000) >> 16) / 255F;
				float g = ((color & 0x00ff00) >> 8) / 255F;
				float b = ((color & 0x0000ff) >> 0) / 255F;
				GL11.glColor3f(r, g, b);
			}

			Tessellator tessellator = Tessellator.instance;
			ItemRenderer.renderItemIn2D(tessellator, liquidIcon.getMaxU(), liquidIcon.getMinV(), liquidIcon.getMinU(), liquidIcon.getMaxV(), liquidIcon.getIconWidth(), liquidIcon.getIconHeight(), 0.0625F);

			if (isQGP) {
				qgpShader.stop();
			} else {
				GL11.glColor3f(1.0F, 1.0F, 1.0F);
			}
		}

		GL11.glPopMatrix();
	}
}
