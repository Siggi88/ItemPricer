package io.siggi.itempricer.itemnamer;

import org.bukkit.inventory.ItemStack;

public class NullNamer extends ItemNamer {

	@Override
	public String nameOf(ItemStack stack) {
		return stack.getType().name();
	}
}
