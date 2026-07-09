package com.hbm.tileentity.machine;

import api.hbm.energymk2.IBatteryItem;
import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import com.hbm.blocks.BlockDummyable;
import com.hbm.explosion.ExplosionLarge;
import com.hbm.inventory.container.ContainerAMSLimiter;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.gui.GUIAMSLimiter;
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

public class TileEntityAMSLimiter extends TileEntityMachineBase implements IGUIProvider, IEnergyReceiverMK2, IFluidStandardTransceiverMK2 {

	public FluidTank coolant = new FluidTank(Fluids.COOLANT, 64000);
	public long power;
	public static final long maxPower = 50000000;
	public int heat;
	public static final int maxHeat = 2500;
	public int mode = 0; // 0 none, 1 limiter, 2 booster
	public boolean locked = false;
	public int warning = 0;
	public int efficiency = 0;

	public TileEntityAMSLimiter() {
		super(4);
	}

	@Override
	public String getName() {
		return "container.amsLimiter";
	}

	@Override
	public void updateEntity() {
		if(worldObj.isRemote) return;

		coolant.loadTank(0, 1, slots);

		if(locked) {
			networkPackNT(25);
			return;
		}

		if(slots[2] != null) {
			if(slots[2].getItem() == ModItems.ams_focus_limiter) mode = 1;
			else if(slots[2].getItem() == ModItems.ams_focus_booster) mode = 2;
			else mode = 0;
		} else {
			mode = 0;
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

		if(mode > 0 && power > 0) {
			long drain = Math.min(power, 15000);
			power -= drain;
			heat += (mode == 2 ? 3 : 1);
			efficiency = mode == 2 ? 35 : 18;
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

	public int getFieldContribution() {
		if(locked || power <= 0 || mode == 0) return 0;
		return mode == 2 ? 35 : 18;
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
		mode = nbt.getInteger("mode");
		locked = nbt.getBoolean("locked");
		warning = nbt.getInteger("warning");
		coolant.readFromNBT(nbt, "coolant");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setLong("power", power);
		nbt.setInteger("heat", heat);
		nbt.setInteger("mode", mode);
		nbt.setBoolean("locked", locked);
		nbt.setInteger("warning", warning);
		coolant.writeToNBT(nbt, "coolant");
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeLong(power);
		buf.writeInt(heat);
		buf.writeInt(mode);
		buf.writeBoolean(locked);
		buf.writeInt(warning);
		coolant.serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		power = buf.readLong();
		heat = buf.readInt();
		mode = buf.readInt();
		locked = buf.readBoolean();
		warning = buf.readInt();
		coolant.deserialize(buf);
		
		this.efficiency = (mode > 0 && power > 0) ? (mode == 2 ? 35 : 18) : 0;
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerAMSLimiter(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIAMSLimiter(player.inventory, this);
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
		if (i == 2) return itemstack.getItem() == ModItems.ams_focus_limiter || itemstack.getItem() == ModItems.ams_focus_booster;
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
