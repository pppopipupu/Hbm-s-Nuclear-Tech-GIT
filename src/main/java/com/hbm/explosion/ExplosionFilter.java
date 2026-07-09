package com.hbm.explosion;

import com.hbm.items.ModItems;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.tileentity.machine.TileEntityAMSBase;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class ExplosionFilter {

	public static boolean shouldBlock(World world, double x, double y, double z) {
		if (world == null || world.loadedTileEntityList == null) return false;

		for (Object obj : world.loadedTileEntityList) {
			if (obj instanceof TileEntityAMSBase) {
				TileEntityAMSBase ams = (TileEntityAMSBase) obj;
				if (ams.slots[12] != null && ams.slots[12].getItem() == ModItems.gun_pppop) {
					double dx = ams.xCoord + 0.5D - x;
					double dy = ams.yCoord + 0.5D - y;
					double dz = ams.zCoord + 0.5D - z;
					if (dx * dx + dy * dy + dz * dz <= 128.0D * 128.0D) {
						ams.absorbExplosion(world, x, y, z);
						return true;
					}
				}
			}
		}

		return false;
	}
}
