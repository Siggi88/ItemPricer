package io.siggi.itempricer.itemdatabase.builder;

import io.siggi.itempricer.itemdatabase.ItemDatabase;
import org.bukkit.Material;

import java.util.function.Consumer;

public class BrewingRecipes {
	public static void addTo(ItemDatabase itemDatabase) {
		Consumer<BrewingRecipe> slowness = parent -> parent
				.then(Material.FERMENTED_SPIDER_EYE, "slowness", s -> s
						.thenStrong()
						.thenLong()
				);

		Consumer<BrewingRecipe> strongSlowness = parent -> parent
				.then(Material.FERMENTED_SPIDER_EYE, "strong_slowness");

		Consumer<BrewingRecipe> harming = parent -> parent
				.then(Material.FERMENTED_SPIDER_EYE, "harming", s -> s
						.thenStrong()
				);

		Consumer<BrewingRecipe> strongHarming = parent -> parent
				.then(Material.FERMENTED_SPIDER_EYE, "strong_harming");

		Consumer<BrewingRecipe> invisibility = parent -> parent
				.then(Material.FERMENTED_SPIDER_EYE, "invisibility", s -> s
						.thenLong()
				);

		Consumer<BrewingRecipe> longInvisibility = parent -> parent
				.then(Material.FERMENTED_SPIDER_EYE, "long_invisibility");

		new BrewingRecipe(itemDatabase, null, null, "water")
				.then(Material.NETHER_WART, "awkward", awkward -> awkward
						.then(Material.SUGAR, "swiftness", swiftness -> swiftness
								.thenStrong(strongSwiftness -> strongSwiftness
										.then(strongSlowness)
								)
								.thenLong()
								.then(slowness)
						)
						.then(Material.RABBIT_FOOT, "leaping", leaping -> leaping
								.thenStrong(strongLeaping -> strongLeaping
										.then(strongSlowness)
								)
								.thenLong()
								.then(slowness)
						)
						.then(Material.BLAZE_POWDER, "strength", strength -> strength
								.thenStrong()
								.thenLong()
						)
						.then(Material.GLISTERING_MELON_SLICE, "healing", healing -> healing
								.thenStrong(strongHealing -> strongHealing
										.then(strongHarming)
								)
								.then(harming)
						)
						.then(Material.SPIDER_EYE, "poison", poison -> poison
								.thenStrong(strongPoison -> strongPoison
										.then(strongHarming)
								)
								.thenLong()
								.then(harming)
						)
						.then(Material.GHAST_TEAR, "regeneration", regeneration -> regeneration
								.thenStrong()
								.thenLong()
						)
						.then(Material.MAGMA_CREAM, "fire_resistance", fireResistance -> fireResistance
								.thenStrong()
								.thenLong()
						)
						.then(Material.PUFFERFISH, "water_breathing", waterBreathing -> waterBreathing
								.thenStrong()
								.thenLong()
						)
						.then(Material.GOLDEN_CARROT, "night_vision", nightVision -> nightVision
								.thenLong(longNightVision -> longNightVision
										.then(longInvisibility)
								)
								.then(invisibility)
						)
						.then(Material.TURTLE_HELMET, "turtle_master", turtleMaster -> turtleMaster
								.thenStrong()
								.thenLong()
						)
						.then(Material.PHANTOM_MEMBRANE, "slow_falling", slowFalling -> slowFalling
								.thenLong()
						)
				)
				.then(Material.FERMENTED_SPIDER_EYE, "weakness", weakness -> weakness
						.thenLong()
				);
	}
}
