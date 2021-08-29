package io.siggi.itempricer.itemnamer;

import hk.siggi.bukkit.nbt.NBTTool;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public abstract class ItemNamer {
	private static ItemNamer namer;
	public static ItemNamer get() {
		if (namer == null) {
			try {
				Plugin nbtTool = Bukkit.getPluginManager().getPlugin("NBTTool");
				if (nbtTool != null) {
					namer = new NBTToolNamer(NBTTool.getUtil());
				}
			} catch (Exception e) {
			}
			if (namer == null) {
				namer = new NullNamer();
			}
		}
		return namer;
	}
	public abstract String nameOf(ItemStack stack);
}
