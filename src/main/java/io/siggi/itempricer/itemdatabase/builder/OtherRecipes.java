package io.siggi.itempricer.itemdatabase.builder;

import io.siggi.itempricer.itemdatabase.ItemDatabase;
import io.siggi.itempricer.itemdatabase.SimplifiedRecipe;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import static org.bukkit.Material.*;

public class OtherRecipes {

	public static void addTo(ItemDatabase itemDatabase) {
		addConcreteWatering(itemDatabase);
		addBoneMealing(itemDatabase);
		addPlantDuplication(itemDatabase);
		addShulkerBoxes(itemDatabase);
	}

	private static void addConcreteWatering(ItemDatabase itemDatabase) {
		BiConsumer<Material,Material> consumer = (input, output) -> {
			List<RecipeChoice> inputChoice = Arrays.asList(new RecipeChoice.MaterialChoice(input));
			itemDatabase.addRecipe(new SimplifiedRecipe(inputChoice, new ItemStack(output, 1), SimplifiedRecipe.RecipeType.WATERING));
		};
		consumer.accept(WHITE_CONCRETE_POWDER, WHITE_CONCRETE);
		consumer.accept(ORANGE_CONCRETE_POWDER, ORANGE_CONCRETE);
		consumer.accept(MAGENTA_CONCRETE_POWDER, MAGENTA_CONCRETE);
		consumer.accept(LIGHT_BLUE_CONCRETE_POWDER, LIGHT_BLUE_CONCRETE);
		consumer.accept(YELLOW_CONCRETE_POWDER, YELLOW_CONCRETE);
		consumer.accept(LIME_CONCRETE_POWDER, LIME_CONCRETE);
		consumer.accept(PINK_CONCRETE_POWDER, PINK_CONCRETE);
		consumer.accept(GRAY_CONCRETE_POWDER, GRAY_CONCRETE);
		consumer.accept(LIGHT_GRAY_CONCRETE_POWDER, LIGHT_GRAY_CONCRETE);
		consumer.accept(CYAN_CONCRETE_POWDER, CYAN_CONCRETE);
		consumer.accept(PURPLE_CONCRETE_POWDER, PURPLE_CONCRETE);
		consumer.accept(BLUE_CONCRETE_POWDER, BLUE_CONCRETE);
		consumer.accept(BROWN_CONCRETE_POWDER, BROWN_CONCRETE);
		consumer.accept(GREEN_CONCRETE_POWDER, GREEN_CONCRETE);
		consumer.accept(RED_CONCRETE_POWDER, RED_CONCRETE);
		consumer.accept(BLACK_CONCRETE_POWDER, BLACK_CONCRETE);
	}

	private static void addBoneMealing(ItemDatabase itemDatabase) {
		List<RecipeChoice> inputChoice = Arrays.asList(new RecipeChoice.MaterialChoice(Material.BONE_MEAL));
		BiConsumer<Material,Integer> consumer = (item, count) -> {
			itemDatabase.addRecipe(new SimplifiedRecipe(inputChoice, new ItemStack(item, count), SimplifiedRecipe.RecipeType.BONE_MEAL));
		};
		consumer.accept(GRASS, 40);
		consumer.accept(DANDELION, 8);
		consumer.accept(POPPY, 8);
		consumer.accept(BLUE_ORCHID, 8);
		consumer.accept(ALLIUM, 8);
		consumer.accept(AZURE_BLUET, 8);
		consumer.accept(ORANGE_TULIP, 8);
		consumer.accept(PINK_TULIP, 8);
		consumer.accept(RED_TULIP, 8);
		consumer.accept(WHITE_TULIP, 8);
		consumer.accept(OXEYE_DAISY, 8);
		consumer.accept(CORNFLOWER, 8);
		consumer.accept(LILY_OF_THE_VALLEY, 8);
	}

	private static void addPlantDuplication(ItemDatabase itemDatabase) {
		List<RecipeChoice> inputChoice = Arrays.asList(new RecipeChoice.MaterialChoice(Material.BONE_MEAL));
		Material[] types = new Material[]{
				SUNFLOWER,
				LILAC,
				ROSE_BUSH,
				PEONY
		};
		for (Material type : types) {
			itemDatabase.addRecipe(new SimplifiedRecipe(inputChoice, new ItemStack(type, 1), SimplifiedRecipe.RecipeType.BONE_MEAL_DUPLICATING));
		}
	}

	private static void addShulkerBoxes(ItemDatabase itemDatabase) {
		Map<Material,Material> mappings = new HashMap<>();
		mappings.put(WHITE_SHULKER_BOX, WHITE_DYE);
		mappings.put(ORANGE_SHULKER_BOX, ORANGE_DYE);
		mappings.put(MAGENTA_SHULKER_BOX, MAGENTA_DYE);
		mappings.put(LIGHT_BLUE_SHULKER_BOX, LIGHT_BLUE_DYE);
		mappings.put(YELLOW_SHULKER_BOX, YELLOW_DYE);
		mappings.put(LIME_SHULKER_BOX, LIME_DYE);
		mappings.put(PINK_SHULKER_BOX, PINK_DYE);
		mappings.put(GRAY_SHULKER_BOX, GRAY_DYE);
		mappings.put(LIGHT_GRAY_SHULKER_BOX, LIGHT_GRAY_DYE);
		mappings.put(CYAN_SHULKER_BOX, CYAN_DYE);
		mappings.put(PURPLE_SHULKER_BOX, PURPLE_DYE);
		mappings.put(BLUE_SHULKER_BOX, BLUE_DYE);
		mappings.put(BROWN_SHULKER_BOX, BROWN_DYE);
		mappings.put(GREEN_SHULKER_BOX, GREEN_DYE);
		mappings.put(RED_SHULKER_BOX, RED_DYE);
		mappings.put(BLACK_SHULKER_BOX, BLACK_DYE);

		List<Material> inputs = new ArrayList<>();
		inputs.add(SHULKER_BOX);
		inputs.addAll(mappings.keySet());

		RecipeChoice inputShulkerChoice = new RecipeChoice.MaterialChoice(inputs.toArray(new Material[inputs.size()]));

		for (Map.Entry<Material,Material> entry : mappings.entrySet()) {
			Material shulkerType = entry.getKey();
			Material dyeType = entry.getValue();
			RecipeChoice dyeChoice = new RecipeChoice.MaterialChoice(new Material[]{dyeType});
			itemDatabase.addRecipe(new SimplifiedRecipe(Arrays.asList(inputShulkerChoice, dyeChoice), new ItemStack(shulkerType, 1), SimplifiedRecipe.RecipeType.CRAFTING));
		}
	}
}
