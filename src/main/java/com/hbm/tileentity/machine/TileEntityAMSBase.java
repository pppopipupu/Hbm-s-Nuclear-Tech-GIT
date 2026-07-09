package com.hbm.tileentity.machine;

import api.hbm.energymk2.IEnergyProviderMK2;
import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import com.hbm.blocks.BlockDummyable;
import com.hbm.explosion.ExplosionLarge;
import com.hbm.inventory.container.ContainerAMSBase;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.gui.GUIAMSBase;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemCatalyst;
import com.hbm.main.MainRegistry;
import com.hbm.items.special.ItemAMSCore;
import com.hbm.lib.Library;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.fauxpointtwelve.DirPos;
import com.hbm.entity.logic.EntityExplosionChunkloading;
import java.util.List;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.packet.PacketDispatcher;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityAMSBase extends TileEntityMachineBase implements IGUIProvider, IEnergyProviderMK2, IEnergyReceiverMK2, IFluidStandardTransceiverMK2 {

	public FluidTank deuterium = new FluidTank(Fluids.DEUTERIUM, 24000);
	public FluidTank tritium = new FluidTank(Fluids.TRITIUM, 24000);
	public FluidTank coolant = new FluidTank(Fluids.COOLANT, 64000);
	public FluidTank amat = new FluidTank(Fluids.QGP, 64000);

	public long power;
	public static final long maxPower = 2000000000L;
	public int heat;
	public static final int maxHeat = 5000;
	public int field = 0;
	public int efficiency = 0;
	public boolean locked = false;
	public int warning = 0;

	public static final long BASE_GEN = 1000000;
	public static final int BASE_AMAT = 50;
	public static final int BASE_FUEL = 5;

	public TileEntityAMSBase() {
		super(16);
	}

	@Override
	public String getName() {
		return "container.amsBase";
	}

	@Override
	public void updateEntity() {
		if(worldObj.isRemote) return;

		// 自动装载/导出流体罐
		deuterium.loadTank(4, 5, slots);
		tritium.loadTank(6, 7, slots);
		coolant.loadTank(0, 1, slots);
		amat.unloadTank(2, 3, slots);

		for(DirPos pos : getPowerPos()) {
			this.tryProvide(worldObj, pos.getX(), pos.getY(), pos.getZ(), pos.getDir());
		}

		for(DirPos pos : getFluidPos()) {
			this.trySubscribe(deuterium.getTankType(), worldObj, pos);
			this.trySubscribe(tritium.getTankType(), worldObj, pos);
			this.trySubscribe(coolant.getTankType(), worldObj, pos);
			this.tryProvide(amat.getTankType(), worldObj, pos);
		}

		boolean isPPPOP = slots[12] != null && slots[12].getItem() == ModItems.gun_pppop;
		if(isPPPOP) {
			warning = 0;
			efficiency = 0;
			if(!worldObj.isRemote) {
				AxisAlignedBB area = AxisAlignedBB.getBoundingBox(
					xCoord - 128, 0, zCoord - 128,
					xCoord + 129, 256, zCoord + 129
				);
				List<EntityExplosionChunkloading> nukes = worldObj.getEntitiesWithinAABB(EntityExplosionChunkloading.class, area);
				for(EntityExplosionChunkloading nuke : nukes) {
					if(nuke.isEntityAlive()) {
						nuke.setDead();
						absorbExplosion(worldObj, nuke.posX, nuke.posY, nuke.posZ);
					}
				}
			}
			networkPackNT(25);
			return;
		}

		if(locked) {
			networkPackNT(25);
			return;
		}

		TileEntityAMSEmitter emitter = findEmitter();
		TileEntityAMSLimiter[] limiters = findLimiters();

		int fieldRaw = 10;
		for(TileEntityAMSLimiter l : limiters) {
			if(l != null) {
				fieldRaw += l.getFieldContribution();
			}
		}
		field = Math.min(100, fieldRaw);

		int eff = 0;
		if(emitter != null) {
			eff = emitter.getEfficiency();
		}
		boolean hasLens = (slots[13] != null && slots[13].getItem() == ModItems.ams_lens)
				|| (slots[14] != null && slots[14].getItem() == ModItems.ams_lens)
				|| (slots[15] != null && slots[15].getItem() == ModItems.ams_lens);
		if(hasLens) {
			eff = Math.min(100, eff + 15);
		}
		efficiency = eff;

		boolean hasCatalysts = slots[8] != null && slots[8].getItem() instanceof ItemCatalyst
				&& slots[9] != null && slots[9].getItem() instanceof ItemCatalyst
				&& slots[10] != null && slots[10].getItem() instanceof ItemCatalyst
				&& slots[11] != null && slots[11].getItem() instanceof ItemCatalyst;

		boolean hasCore = slots[12] != null && slots[12].getItem() instanceof ItemAMSCore;

		if(field > 0 && efficiency > 0 && hasCatalysts && hasCore 
				&& deuterium.getFill() > 0 && tritium.getFill() > 0) {

			float powMod = 0;
			float heatMod = 0;
			float fuelMod = 0;
			long powAbs = 0;

			for(int i = 8; i <= 11; i++) {
				powMod += ItemCatalyst.getPowerMod(slots[i]);
				heatMod += ItemCatalyst.getHeatMod(slots[i]);
				fuelMod += ItemCatalyst.getFuelMod(slots[i]);
				powAbs += ItemCatalyst.getPowerAbs(slots[i]);
			}

			powMod /= 4.0F;
			heatMod /= 4.0F;
			fuelMod /= 4.0F;

			double factor = (field * efficiency) / 10000.0;

			long gen = (long)(BASE_GEN * factor * powMod) + (long)((powAbs / 20.0) * factor);
			power = Math.min(maxPower, power + gen);

			int amatGen = (int)(BASE_AMAT * factor * powMod);
			amat.setFill(Math.min(amat.getMaxFill(), amat.getFill() + amatGen));

			int fuel = (int)(BASE_FUEL * fuelMod * factor) + 1;
			deuterium.setFill(Math.max(0, deuterium.getFill() - fuel));
			tritium.setFill(Math.max(0, tritium.getFill() - fuel));

			int heatBase = ItemAMSCore.getHeatBase(slots[12]);
			heat += (int)(heatBase * heatMod * (1.0 - field / 100.0));

		} else {
			if(heat > 0 && worldObj.getTotalWorldTime() % 10 == 0) {
				heat = Math.max(0, heat - 2);
			}
		}

		if(coolant.getTankType() == Fluids.CRYOGEL && coolant.getFill() > 0 && heat > 0) {
			heat = Math.max(0, heat - 150);
			coolant.setFill(Math.max(0, coolant.getFill() - 5));
		} else if(coolant.getTankType() == Fluids.COOLANT && coolant.getFill() > 0 && heat > 0) {
			heat = Math.max(0, heat - 50);
			coolant.setFill(Math.max(0, coolant.getFill() - 5));
		}

		if(heat >= maxHeat) {
			locked = true;
			warning = 3;
			ExplosionLarge.spawnShock(worldObj, xCoord + 0.5, yCoord + 0.5, zCoord + 0.5, 20, 20);
			ExplosionLarge.spawnBurst(worldObj, xCoord + 0.5, yCoord + 0.5, zCoord + 0.5, 20, 20);
		}

		networkPackNT(25);
	}

	private TileEntityAMSEmitter findEmitter() {
		for(int y = yCoord + 1; y <= yCoord + 16; y++) {
			TileEntity te = worldObj.getTileEntity(xCoord, y, zCoord);
			if(te instanceof TileEntityAMSEmitter) {
				TileEntityAMSEmitter emitter = (TileEntityAMSEmitter) te;
				if(emitter.getBlockMetadata() >= 12) {
					return emitter;
				}
			}
		}
		return null;
	}

	private TileEntityAMSLimiter[] findLimiters() {
		TileEntityAMSLimiter[] out = new TileEntityAMSLimiter[4];
		ForgeDirection[] axes = {ForgeDirection.EAST, ForgeDirection.WEST, ForgeDirection.SOUTH, ForgeDirection.NORTH};
		for(int i = 0; i < 4; i++) {
			ForgeDirection d = axes[i];
			for(int r = 1; r <= 8; r++) {
				TileEntity te = worldObj.getTileEntity(xCoord + d.offsetX * r, yCoord, zCoord + d.offsetZ * r);
				if(te instanceof TileEntityAMSLimiter) {
					TileEntityAMSLimiter l = (TileEntityAMSLimiter) te;
					if(l.getBlockMetadata() >= 12) {
						out[i] = l;
						break;
					}
				}
			}
		}
		return out;
	}

	public boolean isLocked() {
		return locked;
	}

	public DirPos[] getPowerPos() {
		return new DirPos[] {
			new DirPos(xCoord + 2, yCoord, zCoord - 1, Library.POS_X),
			new DirPos(xCoord + 2, yCoord, zCoord, Library.POS_X),
			new DirPos(xCoord + 2, yCoord, zCoord + 1, Library.POS_X),
			new DirPos(xCoord - 2, yCoord, zCoord - 1, Library.NEG_X),
			new DirPos(xCoord - 2, yCoord, zCoord, Library.NEG_X),
			new DirPos(xCoord - 2, yCoord, zCoord + 1, Library.NEG_X),
			new DirPos(xCoord - 1, yCoord, zCoord + 2, Library.POS_Z),
			new DirPos(xCoord, yCoord, zCoord + 2, Library.POS_Z),
			new DirPos(xCoord + 1, yCoord, zCoord + 2, Library.POS_Z),
			new DirPos(xCoord - 1, yCoord, zCoord - 2, Library.NEG_Z),
			new DirPos(xCoord, yCoord, zCoord - 2, Library.NEG_Z),
			new DirPos(xCoord + 1, yCoord, zCoord - 2, Library.NEG_Z),
			
			new DirPos(xCoord + 2, yCoord + 1, zCoord - 1, Library.POS_X),
			new DirPos(xCoord + 2, yCoord + 1, zCoord, Library.POS_X),
			new DirPos(xCoord + 2, yCoord + 1, zCoord + 1, Library.POS_X),
			new DirPos(xCoord - 2, yCoord + 1, zCoord - 1, Library.NEG_X),
			new DirPos(xCoord - 2, yCoord + 1, zCoord, Library.NEG_X),
			new DirPos(xCoord - 2, yCoord + 1, zCoord + 1, Library.NEG_X),
			new DirPos(xCoord - 1, yCoord + 1, zCoord + 2, Library.POS_Z),
			new DirPos(xCoord, yCoord + 1, zCoord + 2, Library.POS_Z),
			new DirPos(xCoord + 1, yCoord + 1, zCoord + 2, Library.POS_Z),
			new DirPos(xCoord - 1, yCoord + 1, zCoord - 2, Library.NEG_Z),
			new DirPos(xCoord, yCoord + 1, zCoord - 2, Library.NEG_Z),
			new DirPos(xCoord + 1, yCoord + 1, zCoord - 2, Library.NEG_Z),
		};
	}

	public DirPos[] getFluidPos() {
		return new DirPos[] {
			new DirPos(xCoord + 2, yCoord, zCoord - 1, Library.POS_X),
			new DirPos(xCoord + 2, yCoord, zCoord, Library.POS_X),
			new DirPos(xCoord + 2, yCoord, zCoord + 1, Library.POS_X),
			new DirPos(xCoord - 2, yCoord, zCoord - 1, Library.NEG_X),
			new DirPos(xCoord - 2, yCoord, zCoord, Library.NEG_X),
			new DirPos(xCoord - 2, yCoord, zCoord + 1, Library.NEG_X),
			new DirPos(xCoord - 1, yCoord, zCoord + 2, Library.POS_Z),
			new DirPos(xCoord, yCoord, zCoord + 2, Library.POS_Z),
			new DirPos(xCoord + 1, yCoord, zCoord + 2, Library.POS_Z),
			new DirPos(xCoord - 1, yCoord, zCoord - 2, Library.NEG_Z),
			new DirPos(xCoord, yCoord, zCoord - 2, Library.NEG_Z),
			new DirPos(xCoord + 1, yCoord, zCoord - 2, Library.NEG_Z),
			
			new DirPos(xCoord + 2, yCoord + 1, zCoord - 1, Library.POS_X),
			new DirPos(xCoord + 2, yCoord + 1, zCoord, Library.POS_X),
			new DirPos(xCoord + 2, yCoord + 1, zCoord + 1, Library.POS_X),
			new DirPos(xCoord - 2, yCoord + 1, zCoord - 1, Library.NEG_X),
			new DirPos(xCoord - 2, yCoord + 1, zCoord, Library.NEG_X),
			new DirPos(xCoord - 2, yCoord + 1, zCoord + 1, Library.NEG_X),
			new DirPos(xCoord - 1, yCoord + 1, zCoord + 2, Library.POS_Z),
			new DirPos(xCoord, yCoord + 1, zCoord + 2, Library.POS_Z),
			new DirPos(xCoord + 1, yCoord + 1, zCoord + 2, Library.POS_Z),
			new DirPos(xCoord - 1, yCoord + 1, zCoord - 2, Library.NEG_Z),
			new DirPos(xCoord, yCoord + 1, zCoord - 2, Library.NEG_Z),
			new DirPos(xCoord + 1, yCoord + 1, zCoord - 2, Library.NEG_Z),
		};
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		power = nbt.getLong("power");
		heat = nbt.getInteger("heat");
		field = nbt.getInteger("field");
		efficiency = nbt.getInteger("efficiency");
		locked = nbt.getBoolean("locked");
		warning = nbt.getInteger("warning");
		deuterium.readFromNBT(nbt, "deuterium");
		tritium.readFromNBT(nbt, "tritium");
		coolant.readFromNBT(nbt, "coolant");
		amat.readFromNBT(nbt, "amat");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setLong("power", power);
		nbt.setInteger("heat", heat);
		nbt.setInteger("field", field);
		nbt.setInteger("efficiency", efficiency);
		nbt.setBoolean("locked", locked);
		nbt.setInteger("warning", warning);
		deuterium.writeToNBT(nbt, "deuterium");
		tritium.writeToNBT(nbt, "tritium");
		coolant.writeToNBT(nbt, "coolant");
		amat.writeToNBT(nbt, "amat");
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeLong(power);
		buf.writeInt(heat);
		buf.writeInt(field);
		buf.writeInt(efficiency);
		buf.writeBoolean(locked);
		buf.writeInt(warning);
		deuterium.serialize(buf);
		tritium.serialize(buf);
		coolant.serialize(buf);
		amat.serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		power = buf.readLong();
		heat = buf.readInt();
		field = buf.readInt();
		efficiency = buf.readInt();
		locked = buf.readBoolean();
		warning = buf.readInt();
		deuterium.deserialize(buf);
		tritium.deserialize(buf);
		coolant.deserialize(buf);
		amat.deserialize(buf);
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerAMSBase(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIAMSBase(player.inventory, this);
	}

	@Override
	public long getPower() {
		return power;
	}

	@Override
	public void setPower(long p) {
		this.power = p;
	}

	@Override
	public long getMaxPower() {
		return maxPower;
	}

	@Override
	public FluidTank[] getReceivingTanks() {
		return new FluidTank[] { deuterium, tritium, coolant };
	}

	@Override
	public FluidTank[] getSendingTanks() {
		return new FluidTank[] { amat };
	}

	@Override
	public FluidTank[] getAllTanks() {
		return new FluidTank[] { deuterium, tritium, coolant, amat };
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack) {
		if (itemstack == null) return false;
		if (i >= 8 && i <= 11) return itemstack.getItem() instanceof ItemCatalyst;
		if (i == 12) return itemstack.getItem() instanceof ItemAMSCore || itemstack.getItem() == ModItems.gun_pppop;
		if (i >= 13 && i <= 15) return itemstack.getItem() == ModItems.sat_chip || itemstack.getItem() == ModItems.ams_lens;
		return true;
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack stack, int side) {
		return false;
	}

	public static TileEntityAMSBase findActivePPPOPBase(World world, double x, double y, double z) {
		if(world == null || world.loadedTileEntityList == null) return null;
		for(Object obj : world.loadedTileEntityList) {
			if(obj instanceof TileEntityAMSBase) {
				TileEntityAMSBase ams = (TileEntityAMSBase) obj;
				if(ams.slots[12] != null && ams.slots[12].getItem() == ModItems.gun_pppop) {
					double dx = ams.xCoord + 0.5D - x;
					double dy = ams.yCoord + 0.5D - y;
					double dz = ams.zCoord + 0.5D - z;
					if(dx * dx + dy * dy + dz * dz <= 128.0D * 128.0D) {
						return ams;
					}
				}
			}
		}
		return null;
	}

	public void absorbExplosion(World world, double x, double y, double z) {
		if(world.isRemote) return;
		this.amat.setFill(Math.min(this.amat.getMaxFill(), this.amat.getFill() + 2000));
		world.playSoundEffect(x, y, z, "hbm:alarm.nuclear", 1.5F, 1.2F);
		
		NBTTagCompound data = new NBTTagCompound();
		data.setString("type", "pppopShield");
		data.setDouble("posX", x);
		data.setDouble("posY", y);
		data.setDouble("posZ", z);
		data.setDouble("targetX", xCoord + 0.5D);
		data.setDouble("targetY", yCoord + 2.5D);
		data.setDouble("targetZ", zCoord + 0.5D);
		PacketDispatcher.wrapper.sendToAllAround(new AuxParticlePacketNT(data, x, y, z), new TargetPoint(world.provider.dimensionId, x, y, z, 150));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox() {
		return AxisAlignedBB.getBoundingBox(xCoord - 4, yCoord - 2, zCoord - 4, xCoord + 5, yCoord + 6, zCoord + 5);
	}
}
