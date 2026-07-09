package com.hbm.util;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.items.ModItems;
import com.hbm.main.MainRegistry;
import com.hbm.explosion.ExplosionNukeSmall;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.stats.Achievement;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.init.Blocks;
import java.util.List;

import java.util.HashMap;

public class AchievementHandler {

	public static HashMap<ComparableStack, Achievement> craftingAchievements = new HashMap();

	public static void register() {
		craftingAchievements.put(new ComparableStack(ModItems.piston_selenium), MainRegistry.achSelenium);
		craftingAchievements.put(new ComparableStack(ModItems.gun_b92), MainRegistry.achSelenium);
		craftingAchievements.put(new ComparableStack(ModItems.battery_potatos), MainRegistry.achPotato);
		craftingAchievements.put(new ComparableStack(ModBlocks.machine_press), MainRegistry.achBurnerPress);
		craftingAchievements.put(new ComparableStack(ModItems.rbmk_fuel_empty), MainRegistry.achRBMK);
		craftingAchievements.put(new ComparableStack(ModBlocks.machine_chemical_plant), MainRegistry.achChemplant);
		craftingAchievements.put(new ComparableStack(ModBlocks.concrete_smooth), MainRegistry.achConcrete);
		craftingAchievements.put(new ComparableStack(ModBlocks.concrete_asbestos), MainRegistry.achConcrete);
		craftingAchievements.put(new ComparableStack(ModItems.ingot_polymer), MainRegistry.achPolymer);
		craftingAchievements.put(new ComparableStack(ModItems.ingot_desh), MainRegistry.achDesh);
		craftingAchievements.put(new ComparableStack(ModItems.gem_tantalium), MainRegistry.achTantalum);
		craftingAchievements.put(new ComparableStack(ModBlocks.machine_gascent), MainRegistry.achGasCent);
		craftingAchievements.put(new ComparableStack(ModBlocks.machine_centrifuge), MainRegistry.achCentrifuge);
		craftingAchievements.put(new ComparableStack(ModItems.ingot_schrabidium), MainRegistry.achSchrab);
		craftingAchievements.put(new ComparableStack(ModItems.nugget_schrabidium), MainRegistry.achSchrab);
		craftingAchievements.put(new ComparableStack(ModBlocks.machine_crystallizer), MainRegistry.achAcidizer);
		craftingAchievements.put(new ComparableStack(ModBlocks.machine_silex), MainRegistry.achSILEX);
		craftingAchievements.put(new ComparableStack(ModItems.nugget_technetium), MainRegistry.achTechnetium);
		craftingAchievements.put(new ComparableStack(ModBlocks.struct_watz_core), MainRegistry.achWatz);
		craftingAchievements.put(new ComparableStack(ModItems.nugget_bismuth), MainRegistry.achBismuth);
		craftingAchievements.put(new ComparableStack(ModItems.nugget_am241), MainRegistry.achBreeding);
		craftingAchievements.put(new ComparableStack(ModItems.nugget_am242), MainRegistry.achBreeding);
		craftingAchievements.put(new ComparableStack(ModItems.missile_nuclear), MainRegistry.achRedBalloons);
		craftingAchievements.put(new ComparableStack(ModItems.missile_nuclear_cluster), MainRegistry.achRedBalloons);
		craftingAchievements.put(new ComparableStack(ModItems.missile_doomsday), MainRegistry.achRedBalloons);
		craftingAchievements.put(new ComparableStack(ModItems.mp_warhead_10_nuclear), MainRegistry.achRedBalloons);
		craftingAchievements.put(new ComparableStack(ModItems.mp_warhead_10_nuclear_large), MainRegistry.achRedBalloons);
		craftingAchievements.put(new ComparableStack(ModItems.mp_warhead_15_nuclear), MainRegistry.achRedBalloons);
		craftingAchievements.put(new ComparableStack(ModItems.mp_warhead_15_nuclear_shark), MainRegistry.achRedBalloons);
		craftingAchievements.put(new ComparableStack(ModItems.mp_warhead_15_boxcar), MainRegistry.achRedBalloons);
		craftingAchievements.put(new ComparableStack(ModBlocks.struct_torus_core), MainRegistry.achFusion);
		craftingAchievements.put(new ComparableStack(ModBlocks.machine_blast_furnace), MainRegistry.achBlastFurnace);
		craftingAchievements.put(new ComparableStack(ModBlocks.machine_assembly_machine), MainRegistry.achAssembly);
		craftingAchievements.put(new ComparableStack(ModItems.billet_pu_mix), MainRegistry.achChicagoPile);
		craftingAchievements.put(new ComparableStack(ModItems.particle_digamma), MainRegistry.achOmega12);
		craftingAchievements.put(new ComparableStack(ModItems.gun_pppop), MainRegistry.achPPPOP);
		craftingAchievements.put(new ComparableStack(ModBlocks.ams_base), MainRegistry.achAMSBase);
		craftingAchievements.put(new ComparableStack(ModItems.bucket_qgp), MainRegistry.achQGP);
	}

	public static void fire(EntityPlayer player, ItemStack stack) {
		if(player == null || player.worldObj.isRemote) return;
		ComparableStack comp = new ComparableStack(stack).makeSingular();
		Achievement achievement = craftingAchievements.get(comp);
		if(achievement != null) {
			boolean unlockedNow = grantAchievement(player, achievement);
			if(achievement == MainRegistry.achPPPOP && !unlockedNow) {
				ExplosionNukeSmall.explode(player.worldObj, player.posX, player.posY, player.posZ, ExplosionNukeSmall.PARAMS_MEDIUM);
			}
		}
	}

	public static boolean grantAchievement(EntityPlayer player, Achievement achievement) {
		if(player == null || player.worldObj.isRemote) return false;
		boolean unlockedNow = false;
		if(player instanceof EntityPlayerMP) {
			EntityPlayerMP playerMP = (EntityPlayerMP) player;
			if(playerMP.func_147099_x() != null && !playerMP.func_147099_x().hasAchievementUnlocked(achievement)) {
				unlockedNow = true;
			}
		}
		player.triggerAchievement(achievement);
		if(unlockedNow && achievement == MainRegistry.achPPPOP) {
			ExplosionNukeSmall.explode(player.worldObj, player.posX, player.posY, player.posZ, ExplosionNukeSmall.PARAMS_MEDIUM);
		}
		if(unlockedNow && achievement == MainRegistry.achAMSBase) {
			player.addPotionEffect(new PotionEffect(12, 100, 0));
			player.worldObj.spawnEntityInWorld(new EntityLightningBolt(player.worldObj, player.posX, player.posY, player.posZ));
			player.worldObj.spawnEntityInWorld(new EntityLightningBolt(player.worldObj, player.posX + 1, player.posY, player.posZ + 1));
			player.worldObj.spawnEntityInWorld(new EntityLightningBolt(player.worldObj, player.posX - 1, player.posY, player.posZ - 1));
			
			List<Entity> list = player.worldObj.getEntitiesWithinAABBExcludingEntity(player, player.boundingBox.expand(8.0D, 8.0D, 8.0D));
			for(Entity ent : list) {
				if(ent instanceof EntityLivingBase) {
					double dx = player.posX - ent.posX;
					double dy = player.posY - ent.posY;
					double dz = player.posZ - ent.posZ;
					ent.motionX += dx * 0.35D;
					ent.motionY += dy * 0.35D + 0.2D;
					ent.motionZ += dz * 0.35D;
				}
			}
			
			player.worldObj.playAuxSFX(2002, (int)Math.round(player.posX), (int)Math.round(player.posY), (int)Math.round(player.posZ), 0);
			if(!player.inventory.addItemStackToInventory(new ItemStack(ModItems.ams_lens))) {
				player.dropPlayerItemWithRandomChoice(new ItemStack(ModItems.ams_lens), false);
			}
		}
		if(unlockedNow && achievement == MainRegistry.achQGP) {
			int px = (int)Math.floor(player.posX);
			int py = (int)Math.floor(player.posY);
			int pz = (int)Math.floor(player.posZ);
			for(int x = px - 2; x <= px + 2; x++) {
				for(int z = pz - 2; z <= pz + 2; z++) {
					for(int y = py - 1; y <= py + 2; y++) {
						if(player.worldObj.getBlock(x, y, z) == Blocks.air && player.worldObj.rand.nextFloat() < 0.3F) {
							player.worldObj.setBlock(x, y, z, Blocks.fire);
						}
					}
				}
			}
			
			player.addPotionEffect(new PotionEffect(12, 1200, 1));
			player.addPotionEffect(new PotionEffect(5, 300, 1));
			player.addPotionEffect(new PotionEffect(1, 300, 4));
			player.addPotionEffect(new PotionEffect(8, 300, 4));
			player.addPotionEffect(new PotionEffect(3, 300, 4));
			player.addPotionEffect(new PotionEffect(9, 600, 0));
			
			player.worldObj.playAuxSFX(2004, (int)Math.round(player.posX), (int)Math.round(player.posY), (int)Math.round(player.posZ), 0);
		}
		return unlockedNow;
	}
}
