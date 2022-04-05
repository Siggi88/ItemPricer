package io.siggi.itempricer.config;

import hk.siggi.bukkit.nbt.NBTCompound;
import hk.siggi.bukkit.nbt.NBTTool;
import org.bukkit.inventory.ItemStack;

import java.util.*;

import static io.siggi.itempricer.config.ConfigSerialization.itemStackGson;

public class ItemPricerSerializedConfiguration {
	public ItemPricerSerializedConfiguration() {
	}

	public ItemPricerSerializedConfiguration(ItemPricerConfiguration configuration) {
		copyFrom(configuration);
	}

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
	public double waterAdd = 1;
	public double waterMultiply = 1;
	public double boneMealAdd = 0.0;
	public double boneMealMultiply = 0.8;
	public double boneMealDuplicateAdd = 0.0;
	public double boneMealDuplicateMultiply = 0.5;
	public final Map<String, ConfigAmount> prices = new LinkedHashMap<>();
	public final Map<String, ConfigAmount> variables = new LinkedHashMap<>();
	public final List<String> ignoredItems = new ArrayList<>();
	public final Map<String, String> itemStackMapping = new LinkedHashMap<>();
	public final transient Map<ItemStack, String> itemStackSerializationMapping = new HashMap<>();

	public void applyTo(ItemPricerConfiguration configuration) {
		configuration.craftingAdd = craftingAdd;
		configuration.craftingMultiply = craftingMultiply;
		configuration.cookingAdd = cookingAdd;
		configuration.cookingMultiply = cookingMultiply;
		configuration.smithingAdd = smithingAdd;
		configuration.smithingMultiply = smithingMultiply;
		configuration.stonecuttingAdd = stonecuttingAdd;
		configuration.stonecuttingMultiply = stonecuttingMultiply;
		configuration.brewingAdd = brewingAdd;
		configuration.brewingMultiply = brewingMultiply;
		configuration.miningAdd = miningAdd;
		configuration.miningMultiply = miningMultiply;
		configuration.waterAdd = waterAdd;
		configuration.waterMultiply = waterMultiply;
		configuration.boneMealAdd = boneMealAdd;
		configuration.boneMealMultiply = boneMealMultiply;
		configuration.boneMealDuplicateAdd = boneMealDuplicateAdd;
		configuration.boneMealDuplicateMultiply = boneMealDuplicateMultiply;

		configuration.prices.clear();
		for (Map.Entry<String, ConfigAmount> entry : prices.entrySet()) {
			configuration.prices.put(unmapItemStack(entry.getKey()), entry.getValue().toAmount(this));
		}
		configuration.variables.clear();
		for (Map.Entry<String, ConfigAmount> entry : variables.entrySet()) {
			configuration.variables.put(entry.getKey(), entry.getValue().toAmount(this));
		}
		configuration.ignoredItems.clear();
		for (String item : ignoredItems) {
			configuration.ignoredItems.add(unmapItemStack(item));
		}
	}

	public void copyFrom(ItemPricerConfiguration configuration) {
		craftingAdd = configuration.craftingAdd;
		craftingMultiply = configuration.craftingMultiply;
		cookingAdd = configuration.cookingAdd;
		cookingMultiply = configuration.cookingMultiply;
		smithingAdd = configuration.smithingAdd;
		smithingMultiply = configuration.smithingMultiply;
		stonecuttingAdd = configuration.stonecuttingAdd;
		stonecuttingMultiply = configuration.stonecuttingMultiply;
		brewingAdd = configuration.brewingAdd;
		brewingMultiply = configuration.brewingMultiply;
		miningAdd = configuration.miningAdd;
		miningMultiply = configuration.miningMultiply;
		waterAdd =configuration. waterAdd;
		waterMultiply = configuration.waterMultiply;
		boneMealAdd = configuration.boneMealAdd;
		boneMealMultiply = configuration.boneMealMultiply;
		boneMealDuplicateAdd = configuration.boneMealDuplicateAdd;
		boneMealDuplicateMultiply = configuration.boneMealDuplicateMultiply;

		itemStackMapping.clear();

		prices.clear();
		for (Map.Entry<ItemStack, Amount> entry : configuration.prices.entrySet()) {
			prices.put(mapItemStack(entry.getKey()), new ConfigAmount(entry.getValue(), this));
		}
		variables.clear();
		for (Map.Entry<String, Amount> entry : configuration.variables.entrySet()) {
			variables.put(entry.getKey(), new ConfigAmount(entry.getValue(), this));
		}
		ignoredItems.clear();
		for (ItemStack item : configuration.ignoredItems) {
			ignoredItems.add(mapItemStack(item));
		}

		sort(prices);
		sort(variables);
		sort(ignoredItems);
		sort(itemStackMapping);
	}

	String mapItemStack(ItemStack key) {
		if (key.getAmount() != 1) {
			key = key.clone();
			key.setAmount(1);
		}
		String s = itemStackSerializationMapping.get(key);
		if (s == null) {
			NBTCompound nbt = NBTTool.getUtil().itemToNBT(key);
			String id = nbt.getString("id");
			if (id.startsWith("minecraft:"))
				id = id.substring(10);
			if (id.equals("potion")) {
				NBTCompound tag = nbt.getCompound("tag");
				if (tag != null) {
					String potionType = tag.getString("Potion");
					if (potionType != null) {
						if (potionType.startsWith("minecraft:"))
							potionType = potionType.substring(10);
						id += "/" + potionType;
					}
				}
			}
			nbt.remove("Count");
			String json = itemStackGson.toJson(nbt, NBTCompound.class);
			String newKey;
			int subId = 0;
			do {
				subId += 1;
				newKey = id + (subId == 1 ? "" : ("_" + subId));
			} while (itemStackMapping.containsKey(newKey));
			itemStackMapping.put(newKey, json);
			itemStackSerializationMapping.put(key, s = newKey);
		}
		return s;
	}

	ItemStack unmapItemStack(String key) {
		String json = key.startsWith("{") ? key : itemStackMapping.get(key);
		if (json == null) return null;
		NBTCompound itemData = itemStackGson.fromJson(json, NBTCompound.class);
		itemData.setByte("Count", (byte) 1);
		return NBTTool.getUtil().itemFromNBT(itemData);
	}

	private <V> void sort(Map<String, V> map) {
		Map<String, V> tempSpace = new HashMap<>();
		tempSpace.putAll(map);
		map.clear();
		SortedSet<String> sortedSet = new TreeSet<>((s1, s2) -> s1.compareTo(s2));
		sortedSet.addAll(tempSpace.keySet());
		for (String key : sortedSet) {
			map.put(key, tempSpace.get(key));
		}
	}

	private void sort(List<String> list) {
		list.sort((s1, s2) -> s1.compareTo(s2));
	}
}
