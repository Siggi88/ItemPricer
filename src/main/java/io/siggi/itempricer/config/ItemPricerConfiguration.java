package io.siggi.itempricer.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import hk.siggi.bukkit.nbt.NBTCompound;
import hk.siggi.bukkit.nbt.NBTTool;
import hk.siggi.bukkit.nbt.NBTUtil;
import io.siggi.itempricer.ItemPricer;
import io.siggi.itempricer.itemdatabase.ItemDatabase;
import io.siggi.itempricer.itemdatabase.ItemInfo;
import org.bukkit.inventory.ItemStack;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;

public class ItemPricerConfiguration {

	private final ItemPricer plugin;
	private final Gson gson;

	public ItemPricerConfiguration(ItemPricer plugin) {
		this.plugin = plugin;
		GsonBuilder builder = new GsonBuilder();
		NBTTool.getSerializer().registerTo(builder);
		gson = builder.create();
	}

	public final Map<ItemStack, Amount> prices = new HashMap<>();
	public final Map<String, Amount> variables = new HashMap<>();
	public final Set<ItemStack> ignoredItems = new HashSet<>();

	public double craftingAdd = 0.75;
	public double craftingMultiply = 1;

	public double cookingAdd = 1;
	public double cookingMultiply = 1;

	public double smithingAdd = 0.6;
	public double smithingMultiply = 1;

	public double stonecuttingAdd = 0.6;
	public double stonecuttingMultiply = 1;

	public double brewingAdd = 1;
	public double brewingMultiply = 1;

	public double miningAdd = 0.0;
	public double miningMultiply = 0.95;

	public void setItemPrice(ItemStack item, Amount price) {
		if (price == null) {
			prices.remove(item);
		} else {
			prices.put(item, price);
		}
	}

	public Double getVariable(String key) {
		return variables.get(key).get();
	}

	public void sendPricesToDatabase(ItemDatabase itemDatabase) {
		// delete all admin prices first
		for (ItemInfo info : itemDatabase.getItemInfos()) {
			info.setAdminPrice(null);
		}
		// then send new prices
		for (Map.Entry<ItemStack, Amount> priceEntry : prices.entrySet()) {
			itemDatabase.setAdminPrice(priceEntry.getKey(), priceEntry.getValue());
		}
	}
}
