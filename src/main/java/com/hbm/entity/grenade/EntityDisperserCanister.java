package com.hbm.entity.grenade;

import com.hbm.entity.effect.EntityMist;
import com.hbm.entity.logic.EntityNukeExplosionMK3;
import com.hbm.entity.effect.EntityCloudFleija;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class EntityDisperserCanister extends EntityGrenadeBase {
	
	public EntityDisperserCanister(World world) {
		super(world);
	}

	public EntityDisperserCanister(World world, EntityLivingBase living) {
		super(world, living);
	}

	public EntityDisperserCanister(World world, double x, double y, double z) {
		super(world, x, y, z);
	}

	public EntityDisperserCanister setFluid(int id) {
		this.dataWatcher.updateObject(12, id);
		return this;
	}

	@Override
	protected void entityInit() {
		this.dataWatcher.addObject(12, 0);
		this.dataWatcher.addObject(13, 0);
	}

	public EntityDisperserCanister setType(int id) {
		this.dataWatcher.updateObject(13, id);
		return this;
	}

	public FluidType getFluid() {
		return Fluids.fromID(this.dataWatcher.getWatchableObjectInt(12));
	}

	public Item getType() {
		return Item.getItemById(this.dataWatcher.getWatchableObjectInt(13));
	}

	@Override
	public void explode() {
		if(!worldObj.isRemote) {
			EntityMist mist = new EntityMist(worldObj);
			mist.setType(getFluid());
			mist.setPosition(posX, posY, posZ);
			mist.setArea(10, 5);
			mist.setDuration(80);
			worldObj.spawnEntityInWorld(mist);
			
			if (getFluid() == Fluids.QGP) {
				EntityNukeExplosionMK3 ex = EntityNukeExplosionMK3.statFacFleija(worldObj, posX, posY, posZ, 20);
				if(!ex.isDead) {
					worldObj.playSoundEffect(posX, posY, posZ, "random.explode", 100.0F, worldObj.rand.nextFloat() * 0.1F + 0.9F);
					worldObj.spawnEntityInWorld(ex);
					EntityCloudFleija cloud = new EntityCloudFleija(worldObj, 20);
					cloud.setPosition(posX, posY, posZ);
					worldObj.spawnEntityInWorld(cloud);
				}
			}
			
			this.setDead();
		}
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbt) {
		super.writeEntityToNBT(nbt);
		nbt.setInteger("fluid", this.dataWatcher.getWatchableObjectInt(12));
		nbt.setInteger("item", this.dataWatcher.getWatchableObjectInt(13));
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbt) {
		super.readEntityFromNBT(nbt);
		this.dataWatcher.updateObject(12, nbt.getInteger("fluid"));
		this.dataWatcher.updateObject(13, nbt.getInteger("item"));

	}
}
