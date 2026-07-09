package com.hbm.items.special;

import com.hbm.items.weapon.ItemGenericGrenade;
import com.hbm.blocks.ModBlocks;
import com.hbm.packet.toclient.QGPDistortionPacket;
import com.hbm.packet.PacketDispatcher;
import com.hbm.tileentity.machine.storage.TileEntityCrateDesh;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import java.util.ArrayList;
import java.util.List;

public class ItemQGPMiningBomb extends ItemGenericGrenade {

	public ItemQGPMiningBomb(int fuse) {
		super(fuse);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamage(int meta) {
		return Blocks.tnt.getIcon(2, 0);
	}

	@Override
	public void explode(Entity grenade, EntityLivingBase thrower, World world, double x, double y, double z) {
		if (world.isRemote) {
			return;
		}

		int blockX = (int) Math.floor(x);
		int blockY = (int) Math.floor(y);
		int blockZ = (int) Math.floor(z);
		int chunkX = blockX >> 4;
		int chunkZ = blockZ >> 4;

		int minX = chunkX * 16;
		int maxX = minX + 15;
		int minZ = chunkZ * 16;
		int maxZ = minZ + 15;

		List<ItemStack> collectedOres = new ArrayList<ItemStack>();

		for (int ix = minX; ix <= maxX; ix++) {
			for (int iz = minZ; iz <= maxZ; iz++) {
				for (int iy = 0; iy < 256; iy++) {
					Block block = world.getBlock(ix, iy, iz);
					if (block == null || block == Blocks.air) {
						continue;
					}
					int meta = world.getBlockMetadata(ix, iy, iz);
					Item item = Item.getItemFromBlock(block);
					if (item != null) {
						ItemStack checkStack = new ItemStack(item, 1, meta);
						int[] ids = OreDictionary.getOreIDs(checkStack);
						boolean isOre = false;
						for (int id : ids) {
							String name = OreDictionary.getOreName(id);
							if (name != null && name.toLowerCase().contains("ore")) {
								isOre = true;
								break;
							}
						}
						if (isOre) {
							collectedOres.add(new ItemStack(item, 2, meta));
						}
					}
				}
			}
		}

		List<ItemStack> mergedOres = new ArrayList<ItemStack>();
		for (ItemStack stack : collectedOres) {
			boolean found = false;
			for (ItemStack merged : mergedOres) {
				if (merged.getItem() == stack.getItem() && merged.getItemDamage() == stack.getItemDamage()) {
					merged.stackSize += stack.stackSize;
					found = true;
					break;
				}
			}
			if (!found) {
				mergedOres.add(stack);
			}
		}

		List<ItemStack> finalSlots = new ArrayList<ItemStack>();
		for (ItemStack stack : mergedOres) {
			int total = stack.stackSize;
			while (total > 0) {
				int count = Math.min(total, stack.getMaxStackSize());
				ItemStack split = new ItemStack(stack.getItem(), count, stack.getItemDamage());
				finalSlots.add(split);
				total -= count;
			}
		}

		for (int ix = minX; ix <= maxX; ix++) {
			for (int iz = minZ; iz <= maxZ; iz++) {
				for (int iy = 0; iy < 256; iy++) {
					Block b = world.getBlock(ix, iy, iz);
					if (b != Blocks.air) {
						world.setBlock(ix, iy, iz, Blocks.air, 0, 2);
					}
				}
			}
		}

		for (int i = 0; i < 5; i++) {
			int rx = minX + world.rand.nextInt(16);
			int rz = minZ + world.rand.nextInt(16);
			int ry = 1 + world.rand.nextInt(250);
			world.setBlock(rx, ry, rz, ModBlocks.gas_radon, 0, 3);
		}

		int itemsPerCrate = 104;
		int totalCrates = (finalSlots.size() + itemsPerCrate - 1) / itemsPerCrate;

		int targetY = Math.max(1, Math.min(254, blockY));
		if (totalCrates == 0) {
			int targetX = minX + 8;
			int targetZ = minZ + 8;
			world.setBlock(targetX, targetY, targetZ, ModBlocks.crate_desh, 0, 3);
		} else {
			for (int crateIdx = 0; crateIdx < totalCrates; crateIdx++) {
				int targetX = minX + 8 + (crateIdx - totalCrates / 2);
				int targetZ = minZ + 8;
				world.setBlock(targetX, targetY, targetZ, ModBlocks.crate_desh, 0, 3);
				TileEntityCrateDesh tile = (TileEntityCrateDesh) world.getTileEntity(targetX, targetY, targetZ);
				if (tile != null) {
					int startIdx = crateIdx * itemsPerCrate;
					int endIdx = Math.min(startIdx + itemsPerCrate, finalSlots.size());
					for (int i = startIdx; i < endIdx; i++) {
						ItemStack stack = finalSlots.get(i);
						int slotIdx = i - startIdx;
						tile.setInventorySlotContents(slotIdx, stack);
					}
				}
			}
		}

		for (Object pObj : world.playerEntities) {
			if (pObj instanceof EntityPlayer) {
				EntityPlayer player = (EntityPlayer) pObj;
				int pChunkX = ((int) Math.floor(player.posX)) >> 4;
				int pChunkZ = ((int) Math.floor(player.posZ)) >> 4;
				if (Math.abs(pChunkX - chunkX) <= 4 && Math.abs(pChunkZ - chunkZ) <= 4) {
					if (player instanceof EntityPlayerMP) {
						PacketDispatcher.wrapper.sendTo(new QGPDistortionPacket(60), (EntityPlayerMP) player);
					}
				}
			}
		}

		world.playSoundEffect(x, y, z, "random.explode", 4.0F, (1.0F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.2F) * 0.7F);
	}
}
