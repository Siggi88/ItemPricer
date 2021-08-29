package io.siggi.itempricer.itemdatabase;

import io.siggi.itempricer.ItemPricer;
import io.siggi.itempricer.Util;
import io.siggi.itempricer.itemnamer.ItemNamer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import static io.siggi.itempricer.Util.choiceToString;

public class SimplifiedRecipe {
	public enum RecipeType {
		CRAFTING(
				"Craft",
				"Crafting",
				3,
				() -> ItemPricer.getInstance().getConfiguration().craftingAdd,
				() -> ItemPricer.getInstance().getConfiguration().craftingMultiply
		),
		COOKING(
				"Cook",
				"Cooking",
				4,
				() -> ItemPricer.getInstance().getConfiguration().cookingAdd + (ItemPricer.getInstance().getItemDatabase().getUnitCookingCost()),
				() -> ItemPricer.getInstance().getItemDatabase().getUnitCookingCost() == 0.0 ? 0.0 : ItemPricer.getInstance().getConfiguration().cookingMultiply
		),
		SMITHING(
				"Smith",
				"Smithing",
				2,
				() -> ItemPricer.getInstance().getConfiguration().smithingAdd,
				() -> ItemPricer.getInstance().getConfiguration().smithingMultiply
		),
		STONECUTTING(
				"Stonecut",
				"Stonecutting",
				1,
				() -> ItemPricer.getInstance().getConfiguration().stonecuttingAdd,
				() -> ItemPricer.getInstance().getConfiguration().stonecuttingMultiply
		),
		BREWING(
				"Brew",
				"Brewing",
				5,
				() -> ItemPricer.getInstance().getConfiguration().brewingAdd + (ItemPricer.getInstance().getItemDatabase().getPrice(Material.BLAZE_POWDER) / 20.0),
				() -> ItemPricer.getInstance().getItemDatabase().getPrice(Material.BLAZE_POWDER) == 0.0 ? 0.0 : ItemPricer.getInstance().getConfiguration().brewingMultiply
		),
		MINING(
				"Mine",
				"Mining",
				6,
				() -> ItemPricer.getInstance().getConfiguration().miningAdd,
				() -> ItemPricer.getInstance().getConfiguration().miningMultiply
		);

		public final String verb;
		public final String verbPresentTense;
		public final int orderNumber;
		public final Supplier<Double> additionalCost;
		public final Supplier<Double> multiplicativeCost;

		private RecipeType(String verb, String verbPresentTense, int orderNumber, Supplier<Double> additionalCost, Supplier<Double> multiplicativeCost) {
			this.verb = verb;
			this.verbPresentTense = verbPresentTense;
			this.orderNumber = orderNumber;
			this.additionalCost = additionalCost;
			this.multiplicativeCost = multiplicativeCost;
		}
	}

	private final Map<RecipeChoice, Integer> inputs;
	private final ItemStack output;
	private final RecipeType type;

	public SimplifiedRecipe(List<RecipeChoice> inputs, ItemStack output, RecipeType type) {
		if (inputs == null || output == null || type == null) {
			throw new NullPointerException("no arguments allowed to be null when constructing SimplifiedRecipe");
		}
		this.inputs = new HashMap<>();
		for (RecipeChoice input : inputs) {
			if (this.inputs.containsKey(input)) {
				this.inputs.put(input, this.inputs.get(input) + 1);
			} else {
				this.inputs.put(input, 1);
			}
		}
		this.output = output;
		this.type = type;
	}

	public Map<RecipeChoice, Integer> getInputs() {
		return inputs;
	}

	public ItemStack getOutput() {
		return output;
	}

	public RecipeType getType() {
		return type;
	}

	private String recipeString = null;

	public String getRecipeString() {
		if (recipeString == null) toString();
		return recipeString;
	}

	private String toStringCache = null;

	@Override
	public String toString() {
		if (toStringCache == null) {
			Map<RecipeChoice, Integer> inputs = getInputs();

			StringBuilder recipeStringBuilder = new StringBuilder();
			for (Map.Entry<RecipeChoice, Integer> choiceEntry : inputs.entrySet()) {
				if (!recipeStringBuilder.isEmpty())
					recipeStringBuilder.append(", ");
				String choiceString = choiceToString(choiceEntry.getKey());
				int count = choiceEntry.getValue();
				if (count > 1) {
					recipeStringBuilder.append(count).append("x ");
				}
				recipeStringBuilder.append(choiceString);
			}
			ItemStack output = getOutput();
			int makes = this.output.getAmount();
			StringBuilder sb = new StringBuilder(getType().verbPresentTense).append(": ").append(recipeStringBuilder);
			int endOfRecipe = sb.length();
			sb.append(" makes ");
			if (makes != 1) {
				sb.append(makes);
				endOfRecipe = sb.length();
				sb.append("x ");
			}
			sb.append(ItemNamer.get().nameOf(output));
			toStringCache = sb.toString();
			recipeString = toStringCache.substring(0, endOfRecipe);
		}
		return toStringCache;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof SimplifiedRecipe)) {
			return false;
		}
		SimplifiedRecipe o = (SimplifiedRecipe) other;
		return inputs.equals(o.inputs) && output.equals(o.output) && type == o.type;
	}

	@Override
	public int hashCode() {
		return Objects.hash(inputs, output, type);
	}
}
