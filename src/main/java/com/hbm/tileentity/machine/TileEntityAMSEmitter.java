package com.hbm.tileentity.machine;

import api.hbm.energymk2.IBatteryItem;
import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import com.hbm.blocks.BlockDummyable;
import com.hbm.explosion.ExplosionLarge;
import com.hbm.inventory.container.ContainerAMSEmitter;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.gui.GUIAMSEmitter;
import com.hbm.items.ModItems;
import com.hbm.lib.Library;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.fauxpointtwelve.DirPos;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityAMSEmitter extends TileEntityMachineBase implements IGUIProvider, IEnergyReceiverMK2, IFluidStandardTransceiverMK2 {

	public FluidTank coolant = new FluidTank(Fluids.COOLANT, 64000);
	public long power;
	public static final long maxPower = 100000000;
	public int heat;
	public static final int maxHeat = 2500;
	public int efficiency = 0;
	public boolean locked = false;
	public int warning = 0;
	
	public static final long EFF_POWER_REF = 10000000;

	public TileEntityAMSEmitter() {
		super(4);
	}

	@Override
	public String getName() {
		return "container.amsEmitter";
	}

	@Override
	public void updateEntity() {
		if(worldObj.isRemote) return;

		coolant.loadTank(0, 1, slots);

		if(locked) {
			networkPackNT(25);
			return;
		}

		this.power = Library.chargeTEFromItems(slots, 3, this.power, maxPower);

		for(DirPos pos : getPowerPos()) {
			this.trySubscribe(worldObj, pos);
		}

		for(DirPos pos : getFluidPos()) {
			this.trySubscribe(coolant.getTankType(), worldObj, pos);
		}

		if(coolant.getTankType() == Fluids.CRYOGEL && coolant.getFill() > 0 && heat > 0) {
			heat = Math.max(0, heat - 40);
			coolant.setFill(Math.max(0, coolant.getFill() - 2));
		} else if(coolant.getTankType() == Fluids.COOLANT && coolant.getFill() > 0 && heat > 0) {
			heat = Math.max(0, heat - 15);
			coolant.setFill(Math.max(0, coolant.getFill() - 2));
		}

		boolean hasMuzzle = hasMuzzle();

		if(hasMuzzle && power > 0) {
			efficiency = (int)Math.min(100, (power * 100.0) / EFF_POWER_REF);
			long drain = Math.min(power, 50000);
			power -= drain;
			heat += 2;
		} else {
			efficiency = 0;
			if(heat > 0 && worldObj.getTotalWorldTime() % 10 == 0) {
				heat = Math.max(0, heat - 1);
			}
		}

		if(heat >= maxHeat) {
			locked = true;
			warning = 3;
			ExplosionLarge.spawnShock(worldObj, xCoord + 0.5, yCoord + 0.5, zCoord + 0.5, 5, 5);
			ExplosionLarge.spawnBurst(worldObj, xCoord + 0.5, yCoord + 0.5, zCoord + 0.5, 5, 5);
		}

		networkPackNT(25);
	}

	public boolean hasMuzzle() {
		return slots[2] != null && slots[2].getItem() == ModItems.ams_muzzle;
	}

	public int getEfficiency() {
		if(locked || power <= 0 || !hasMuzzle()) return 0;
		return efficiency;
	}

	public boolean isLocked() {
		return locked;
	}

	public DirPos[] getPowerPos() {
		ForgeDirection dir = ForgeDirection.getOrientation(getBlockMetadata() - BlockDummyable.offset);
		return new DirPos[] {
			new DirPos(xCoord, yCoord, zCoord, dir.getOpposite())
		};
	}

	public DirPos[] getFluidPos() {
		ForgeDirection dir = ForgeDirection.getOrientation(getBlockMetadata() - BlockDummyable.offset);
		return new DirPos[] {
			new DirPos(xCoord, yCoord, zCoord, dir.getOpposite())
		};
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		power = nbt.getLong("power");
		heat = nbt.getInteger("heat");
		efficiency = nbt.getInteger("efficiency");
		locked = nbt.getBoolean("locked");
		warning = nbt.getInteger("warning");
		coolant.readFromNBT(nbt, "coolant");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setLong("power", power);
		nbt.setInteger("heat", heat);
		nbt.setInteger("efficiency", efficiency);
		nbt.setBoolean("locked", locked);
		nbt.setInteger("warning", warning);
		coolant.writeToNBT(nbt, "coolant");
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeLong(power);
		buf.writeInt(heat);
		buf.writeInt(efficiency);
		buf.writeBoolean(locked);
		buf.writeInt(warning);
		coolant.serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		power = buf.readLong();
		heat = buf.readInt();
		efficiency = buf.readInt();
		locked = buf.readBoolean();
		warning = buf.readInt();
		coolant.deserialize(buf);
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerAMSEmitter(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIAMSEmitter(player.inventory, this);
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
		return new FluidTank[] { coolant };
	}

	@Override
	public FluidTank[] getSendingTanks() {
		return new FluidTank[0];
	}

	@Override
	public FluidTank[] getAllTanks() {
		return new FluidTank[] { coolant };
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack) {
		if (itemstack == null) return false;
		if (i == 2) return itemstack.getItem() == ModItems.ams_muzzle;
		if (i == 3) return itemstack.getItem() instanceof IBatteryItem;
		return true;
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack stack, int side) {
		return false;
	}

	public long getPowerScaled(long i) {
		return (power * i) / maxPower;
	}

	public int getEfficiencyScaled(int i) {
		return (efficiency * i) / 100;
	}

	public int getHeatScaled(int i) {
		return (heat * i) / maxHeat;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox() {
		return AxisAlignedBB.getBoundingBox(xCoord - 2, yCoord, zCoord - 2, xCoord + 3, yCoord + 6, zCoord + 3);
	}
}
