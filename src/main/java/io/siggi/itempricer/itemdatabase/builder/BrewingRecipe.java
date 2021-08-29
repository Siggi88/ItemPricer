package io.siggi.itempricer.itemdatabase.builder;

import io.siggi.itempricer.Util;
import io.siggi.itempricer.itemdatabase.ItemDatabase;
import io.siggi.itempricer.itemdatabase.SimplifiedRecipe;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;

import java.util.Arrays;
import java.util.function.Consumer;

public class BrewingRecipe {
	private final ItemDatabase itemDatabase;
	private final ItemStack from;
	private final Material mixIn;
	private final String outputPotionName;
	private final ItemStack outputPotion;
	private final ItemStack outputSplashPotion;
	private final ItemStack outputLingeringPotion;

	BrewingRecipe(ItemDatabase itemDatabase, ItemStack from, Material mixIn, String outputPotion) {
		if (itemDatabase == null) {
			throw new NullPointerException("itemDatabase cannot be null");
		}
		if ((from == null && mixIn != null) || (from != null && mixIn == null)) {
			throw new NullPointerException("from and mixIn must be both null or both not null");
		}
		if (outputPotion == null) {
			throw new NullPointerException("outputPotion cannot be null");
		}
		this.itemDatabase = itemDatabase;
		this.from = from;
		this.mixIn = mixIn;
		this.outputPotionName = outputPotion;
		this.outputPotion = Util.getItemWithPotion("potion", outputPotion);
		this.outputSplashPotion = Util.getItemWithPotion("splash_potion", outputPotion);
		this.outputLingeringPotion = Util.getItemWithPotion("lingering_potion", outputPotion);
		if (from != null) {
			itemDatabase.addRecipe(
					new SimplifiedRecipe(
							Arrays.asList(new RecipeChoice[]{
									new RecipeChoice.ExactChoice(from),
									new RecipeChoice.MaterialChoice(mixIn)
							}),
							this.outputPotion,
							SimplifiedRecipe.RecipeType.BREWING
					)
			);
		}
		itemDatabase.addRecipe(
				new SimplifiedRecipe(
						Arrays.asList(new RecipeChoice[]{
								new RecipeChoice.ExactChoice(this.outputPotion),
								new RecipeChoice.MaterialChoice(Material.GUNPOWDER)
						}),
						this.outputSplashPotion,
						SimplifiedRecipe.RecipeType.BREWING
				)
		);
		itemDatabase.addRecipe(
				new SimplifiedRecipe(
						Arrays.asList(new RecipeChoice[]{
								new RecipeChoice.ExactChoice(this.outputSplashPotion),
								new RecipeChoice.MaterialChoice(Material.DRAGON_BREATH)
						}),
						this.outputLingeringPotion,
						SimplifiedRecipe.RecipeType.BREWING
				)
		);
	}

	public BrewingRecipe then(Material mixIn, String outputPotion) {
		then(mixIn, outputPotion, null);
		return this;
	}

	public BrewingRecipe then(Material mixIn, String outputPotion, Consumer<BrewingRecipe> then) {
		BrewingRecipe brewingRecipe = new BrewingRecipe(itemDatabase, this.outputPotion, mixIn, outputPotion);
		if (then != null)
			then.accept(brewingRecipe);
		return this;
	}

	public BrewingRecipe thenStrong() {
		return thenStrong(null);
	}

	public BrewingRecipe thenStrong(Consumer<BrewingRecipe> then) {
		return then(Material.GLOWSTONE_DUST, "strong_" + outputPotionName, then);
	}

	public BrewingRecipe thenLong() {
		return thenLong(null);
	}

	public BrewingRecipe thenLong(Consumer<BrewingRecipe> then) {
		return then(Material.REDSTONE, "long_" + outputPotionName, then);
	}

	public BrewingRecipe then(Consumer<BrewingRecipe> applyTo) {
		applyTo.accept(this);
		return this;
	}
}
