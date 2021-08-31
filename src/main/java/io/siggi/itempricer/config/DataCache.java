package io.siggi.itempricer.config;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

import static io.siggi.itempricer.Util.getMinecraftVersion;
import static io.siggi.itempricer.config.ConfigSerialization.itemStackGson;

public class DataCache {
	public DataCache() {
		this.serverVersion = getMinecraftVersion();
	}

	public DataCache(String serverVersion) {
		this.serverVersion = serverVersion;
	}

	public boolean isValid() {
		return getMinecraftVersion().equals(serverVersion);
	}

	public String serverVersion = null;
	public final Map<String, String> blockDrops = new HashMap<>();

	public void cacheBlockDrops(Map<Material, ItemStack> drops) {
		blockDrops.clear();
		for (Map.Entry<Material, ItemStack> entry : drops.entrySet()) {
			blockDrops.put(entry.getKey().name(), itemStackGson.toJson(entry.getValue(), ItemStack.class));
		}
	}

	public boolean restoreBlockDrops(Map<Material, ItemStack> drops) {
		if (!isValid() || blockDrops.isEmpty()) return false;
		try {
			Map<Material,ItemStack> tmpSpace = new HashMap<>();
			for (Map.Entry<String, String> entry : blockDrops.entrySet()) {
				tmpSpace.put(Material.valueOf(entry.getKey()), itemStackGson.fromJson(entry.getValue(), ItemStack.class));
			}
			drops.clear();
			drops.putAll(tmpSpace);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
