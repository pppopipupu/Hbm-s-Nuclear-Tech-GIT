package com.hbm.items.gear;

import com.hbm.items.ModItems;
import com.hbm.main.MainRegistry;

import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSpade;

public class SpadeSchrabidium extends ItemSpade {
	public SpadeSchrabidium(ToolMaterial t, String s){
		super(t);
		this.setUnlocalizedName(s);
		this.setRegistryName(s);
		this.setCreativeTab(MainRegistry.tabTest);
		ModItems.ALL_ITEMS.add(this);
	}

	@Override
	public EnumRarity getRarity(ItemStack stack) {
		return EnumRarity.RARE;
	}
}