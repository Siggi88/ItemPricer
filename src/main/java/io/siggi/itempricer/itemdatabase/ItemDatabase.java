package io.siggi.itempricer.itemdatabase;

import io.siggi.itempricer.config.Amount;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.*;

import static io.siggi.itempricer.Util.allMaterials;

public class ItemDatabase {
	private final Map<ItemStack, ItemInfo> itemInfos = new HashMap<>();
	private double unitCookingCost = 0.0;

	public ItemDatabase() {
	}

	public ItemInfo getOrCreateItemInfo(ItemStack item) {
		item = item.clone();
		item.setAmount(1);
		ItemInfo itemInfo = itemInfos.get(item);
		if (itemInfo == null) itemInfos.put(item, itemInfo = new ItemInfo(this, item));
		return itemInfo;
	}

	public ItemInfo getItemInfo(ItemStack item) {
		item = item.clone();
		item.setAmount(1);
		return itemInfos.get(item);
	}

	public Collection<ItemInfo> getItemInfos() {
		return itemInfos.values();
	}

	public void setAdminPrice(ItemStack item, Amount price) {
		getOrCreateItemInfo(item).setAdminPrice(price);
	}

	public void addRecipe(SimplifiedRecipe recipe) {
		getOrCreateItemInfo(recipe.getOutput()).getRecipes().add(recipe);
	}

	public void recalculatePrices() {
		for (ItemInfo info : itemInfos.values()) {
			info.resetPrice();
		}
		for (int i = 0; i < 16; i++) {
			if (!doRecalculatePrices()) break;
		}
	}

	private boolean doRecalculatePrices() {
		boolean changed = false;
		for (ItemInfo info : itemInfos.values()) {
			if (info.calculateStep()) {
				changed = true;
			}
		}
		if (calculateFuelCost()) changed = true;
		return changed;
	}

	private boolean calculateFuelCost() {
		boolean haveFuelCost = false;
		double lowestUnitCost = 0.0;
		for (Map.Entry<Material,Double> fuelInfo : getFuelOperations().entrySet()) {
			double fuelItemCost = getPrice(fuelInfo.getKey());
			if (fuelItemCost == 0.0) continue;
			double unitCost = fuelItemCost / fuelInfo.getValue();
			if (!haveFuelCost) {
				lowestUnitCost = unitCost;
				haveFuelCost = true;
			} else {
				lowestUnitCost = Math.min(lowestUnitCost, unitCost);
			}
		}
		if (unitCookingCost != lowestUnitCost) {
			unitCookingCost = lowestUnitCost;
			return true;
		} else {return false;}
	}

	public double getPrice(ItemStack item) {
		ItemInfo info = getItemInfo(item);
		if (info == null) return 0.0;
		return info.getPrice() * item.getAmount();
	}

	public double getPrice(Material item) {
		return getPrice(new ItemStack(item));
	}

	public double getUnitCookingCost() {
		return unitCookingCost;
	}

	private Map<Material, Double> fuelOperations = null;

	public Map<Material, Double> getFuelOperations() {
		if (fuelOperations == null) {
			Map<Material, Double> newMap = new HashMap<>();
			newMap.put(Material.LAVA_BUCKET, 100.0);
			newMap.put(Material.COAL_BLOCK, 80.0);
			newMap.put(Material.DRIED_KELP_BLOCK, 20.0);
			newMap.put(Material.BLAZE_ROD, 12.0);
			newMap.put(Material.COAL, 8.0);
			newMap.put(Material.CHARCOAL, 8.0);
			fuelOperations = Collections.unmodifiableMap(newMap);
		}
		return fuelOperations;
	}
}
