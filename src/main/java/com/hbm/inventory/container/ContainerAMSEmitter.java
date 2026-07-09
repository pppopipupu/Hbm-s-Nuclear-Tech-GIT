package com.hbm.inventory.container;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

public class ContainerAMSEmitter extends ContainerBase {
	public ContainerAMSEmitter(InventoryPlayer invPlayer, IInventory inv) {
		super(invPlayer, inv);
		//Fluid In
		this.addSlotToContainer(new Slot(inv, 0, 44, 17));
		//Fluid Out
		this.addSlotToContainer(new Slot(inv, 1, 44, 53));
		//Focus
		this.addSlotToContainer(new Slot(inv, 2, 80, 53));
		//Battery
		this.addSlotToContainer(new Slot(inv, 3, 116, 53));
		
		playerInv(invPlayer, 8, 84);
	}
}
