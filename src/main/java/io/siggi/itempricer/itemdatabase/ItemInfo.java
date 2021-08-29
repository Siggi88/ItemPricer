package io.siggi.itempricer.itemdatabase;

import io.siggi.itempricer.config.Amount;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;

import java.util.*;

public class ItemInfo {
	private final ItemDatabase database;
	private final ItemStack stack;
	private final Set<SimplifiedRecipe> recipes = new HashSet<>();
	private SimplifiedRecipe cheapestRecipe = null;
	private Amount adminPrice = null;
	private boolean hasCalculatedPrice = false;
	private double price = 0.0;

	ItemInfo(ItemDatabase database, ItemStack stack) {
		this.database = database;
		this.stack = stack;
	}

	public ItemStack getItemStack() {
		return stack;
	}

	public Set<SimplifiedRecipe> getRecipes() {
		return recipes;
	}

	public SimplifiedRecipe getTopRecipe() {
		if (recipes.isEmpty()) return null;
		List<SimplifiedRecipe> topRecipes = new ArrayList<>();
		topRecipes.addAll(recipes);
		topRecipes.sort((r1, r2) -> r2.getType().orderNumber - r1.getType().orderNumber);
		return topRecipes.get(0);
	}

	public SimplifiedRecipe getCheapestRecipe() {
		return cheapestRecipe;
	}

	public boolean hasAdminPrice() {
		return adminPrice != null && adminPrice.get() != null;
	}

	public Amount getAdminPrice() {
		return adminPrice;
	}

	public void setAdminPrice(Amount amount) {
		this.adminPrice = amount;
	}

	public boolean hasCalculatedPrice() {
		return hasCalculatedPrice;
	}

	public double getPrice() {
		return price;
	}

	private boolean addLowerPrice(SimplifiedRecipe recipe, double price) {
		if (hasCalculatedPrice && price >= this.price) {
			return false;
		}
		hasCalculatedPrice = true;
		this.cheapestRecipe = recipe;
		this.price = price;
		return true;
	}

	public boolean isAdminPriceTooHigh() {
		return hasAdminPrice() && adminPrice.get() > price;
	}

	void resetPrice() {
		price = 0.0;
		hasCalculatedPrice = false;
	}

	boolean calculateStep() {
		boolean changed = false;
		if (adminPrice != null) {
			Double admPrice = adminPrice.get();
			if (admPrice != null && admPrice != 0) {
				addLowerPrice(null, admPrice);
			}
		}
		for (SimplifiedRecipe recipe : recipes) {
			double price = calculateRecipePrice(recipe);
			if (price != 0.0)
				if (addLowerPrice(recipe, price))
					changed = true;
		}
		return changed;
	}

	double calculateRecipePrice(SimplifiedRecipe recipe) {
		double price = 0.0;
		Map<RecipeChoice, Integer> inputs = recipe.getInputs();
		for (Map.Entry<RecipeChoice, Integer> choiceEntry : inputs.entrySet()) {
			RecipeChoice input = choiceEntry.getKey();
			Integer count = choiceEntry.getValue();
			double ingredientPrice = getIngredientPrice(input) * ((double) count);
			if (ingredientPrice == 0.0) return 0.0;
			price += ingredientPrice;
		}
		double unitCost = price / ((double) recipe.getOutput().getAmount());
		double additionalCost = recipe.getType().additionalCost.get();
		double multiplicativeCost = recipe.getType().multiplicativeCost.get();
		if (multiplicativeCost == 0.0) return 0.0;
		unitCost *= multiplicativeCost;
		unitCost += additionalCost;
		return unitCost;
	}

	double getIngredientPrice(RecipeChoice choice) {
		double cheapestPrice;
		if (choice instanceof RecipeChoice.ExactChoice) {
			RecipeChoice.ExactChoice exactChoice = (RecipeChoice.ExactChoice) choice;
			List<ItemStack> choices = exactChoice.getChoices();
			cheapestPrice = getCheapestIngredientPrice(choices);
		} else if (choice instanceof RecipeChoice.MaterialChoice) {
			RecipeChoice.MaterialChoice materialChoice = (RecipeChoice.MaterialChoice) choice;
			List<Material> choices = materialChoice.getChoices();
			cheapestPrice = getCheapestIngredientPriceFromMaterials(choices);
		} else {
			return 0.0;
		}
		return cheapestPrice;
	}

	private double getCheapestIngredientPriceFromMaterials(List<Material> items) {
		List<ItemStack> itemStackList = new ArrayList<>(items.size());
		for (Material material : items) {
			itemStackList.add(new ItemStack(material));
		}
		return getCheapestIngredientPrice(itemStackList);
	}

	private double getCheapestIngredientPrice(List<ItemStack> items) {
//		ItemStack cheapestItem = null;
		double lowestPrice = 0.0;
		for (ItemStack item : items) {
			ItemInfo info = database.getItemInfo(item);
			if (info == null || !info.hasCalculatedPrice()) continue;
			double price = info.getPrice();
			if (lowestPrice == 0.0 || price < lowestPrice) {
//				cheapestItem = item;
				lowestPrice = price;
			}
		}
		return lowestPrice;
	}
}
