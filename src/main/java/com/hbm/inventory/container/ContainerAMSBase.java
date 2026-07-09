package com.hbm.inventory.container;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

public class ContainerAMSBase extends ContainerBase {
	public ContainerAMSBase(InventoryPlayer invPlayer, IInventory inv) {
		super(invPlayer, inv);
		//Cool 1 In
		this.addSlotToContainer(new Slot(inv, 0, 8, 18));
		//Cool 1 Out
		this.addSlotToContainer(new Slot(inv, 1, 8, 54));
		//Cool 2 In
		this.addSlotToContainer(new Slot(inv, 2, 152, 18));
		//Cool 2 Out
		this.addSlotToContainer(new Slot(inv, 3, 152, 54));
		//Fuel 1 In
		this.addSlotToContainer(new Slot(inv, 4, 8, 72));
		//Fuel 1 Out
		this.addSlotToContainer(new Slot(inv, 5, 8, 108));
		//Fuel 2 In
		this.addSlotToContainer(new Slot(inv, 6, 152, 72));
		//Fuel 2 Out
		this.addSlotToContainer(new Slot(inv, 7, 152, 108));
		//Moderator
		this.addSlotToContainer(new Slot(inv, 8, 80, 45));
		this.addSlotToContainer(new Slot(inv, 9, 62, 63));
		this.addSlotToContainer(new Slot(inv, 10, 98, 63));
		this.addSlotToContainer(new Slot(inv, 11, 80, 81));
		//Core
		this.addSlotToContainer(new Slot(inv, 12, 80, 63));
		//Sat Chips
		this.addSlotToContainer(new Slot(inv, 13, 62, 108));
		this.addSlotToContainer(new Slot(inv, 14, 62 + 18, 108));
		this.addSlotToContainer(new Slot(inv, 15, 62 + 36, 108));
		
		playerInv(invPlayer, 8, 140);
	}
}
