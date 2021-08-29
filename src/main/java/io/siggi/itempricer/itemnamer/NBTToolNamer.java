package io.siggi.itempricer.itemnamer;

import hk.siggi.bukkit.nbt.NBTUtil;
import org.bukkit.inventory.ItemStack;

public class NBTToolNamer extends ItemNamer {
	private final NBTUtil util;

	public NBTToolNamer(NBTUtil util) {
		this.util = util;
	}

	@Override
	public String nameOf(ItemStack stack) {
		return util.getItemName(stack);
	}
}
