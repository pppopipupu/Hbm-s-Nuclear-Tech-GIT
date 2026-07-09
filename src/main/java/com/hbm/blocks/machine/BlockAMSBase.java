package com.hbm.blocks.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.tileentity.machine.TileEntityAMSBase;
import com.hbm.tileentity.TileEntityProxyCombo;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.entity.player.EntityPlayer;
import cpw.mods.fml.common.network.internal.FMLNetworkHandler;
import com.hbm.main.MainRegistry;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockAMSBase extends BlockDummyable {

	public BlockAMSBase() {
		super(Material.iron);
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
		if (world.isRemote) {
			return true;
		} else if (!player.isSneaking()) {
			int[] pos = this.findCore(world, x, y, z);
			if (pos == null) return false;
			FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, pos[0], pos[1], pos[2]);
			return true;
		}
		return false;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		if (meta >= 12) return new TileEntityAMSBase();
		return new TileEntityProxyCombo().inventory().fluid();
	}

	@Override
	public int[] getDimensions() {
		return new int[] { 2, 0, 1, 1, 1, 1 };
	}

	@Override
	public void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
		super.fillSpace(world, x, y, z, dir, o);
		
		x -= dir.offsetX;
		z -= dir.offsetZ;
		
		for(int i = -1; i <= 1; i++) for(int j = -1; j <= 1; j++) {
			if(i != 0 || j != 0) this.makeExtra(world, x + i, y, z + j);
		}
	}

	@Override
	public int getOffset() {
		return 1;
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, Block block, int i) {
		if (i >= 12) {
			world.spawnEntityInWorld(new EntityItem(world, x + 0.5, y + 0.5, z + 0.5, new ItemStack(ModBlocks.ams_base)));
		}
		super.breakBlock(world, x, y, z, block, i);
	}
}
