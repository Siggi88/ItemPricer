package io.siggi.itempricer.itemdatabase.builder;

import io.siggi.itempricer.ItemPricer;
import io.siggi.itempricer.itemdatabase.ItemDatabase;
import io.siggi.itempricer.itemdatabase.ItemInfo;
import io.siggi.itempricer.itemdatabase.SimplifiedRecipe;
import io.siggi.itempricer.itemnamer.ItemNamer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.*;

import java.util.*;

import static io.siggi.itempricer.Util.iterable;
import static io.siggi.itempricer.Util.choiceToItemStacks;

public class RecipeSimplifier {

	private final ItemPricer plugin;

	public RecipeSimplifier(ItemPricer plugin) {
		this.plugin = plugin;
	}

	private String name(ItemStack item) {
		return ItemNamer.get().nameOf(item);
	}

	private String name(Material item) {
		return name(new ItemStack(item));
	}

	public ItemDatabase generateDatabase() {
		ItemDatabase itemDb = new ItemDatabase();
		for (Recipe recipe : iterable(plugin.getServer().recipeIterator())) {
			if (recipe instanceof FurnaceRecipe) {
				furnaceRecipe((FurnaceRecipe) recipe, itemDb);
			} else if (recipe instanceof ShapedRecipe) {
				shapedRecipe((ShapedRecipe) recipe, itemDb);
			} else if (recipe instanceof ShapelessRecipe) {
				shapelessRecipe((ShapelessRecipe) recipe, itemDb);
			} else if (recipe instanceof SmithingRecipe) {
				smithingRecipe((SmithingRecipe) recipe, itemDb);
			} else if (recipe instanceof StonecuttingRecipe) {
				stonecuttingRecipe((StonecuttingRecipe) recipe, itemDb);
			}
		}
		for (Map.Entry<Material,ItemStack> dropMapping : getBlockDrops().entrySet()) {
			itemDb.addRecipe(
					new SimplifiedRecipe(
							Arrays.asList(new RecipeChoice[]{new RecipeChoice.MaterialChoice(dropMapping.getKey())}),
							dropMapping.getValue(),
							SimplifiedRecipe.RecipeType.MINING
					)
			);
		}
		BrewingRecipes.addTo(itemDb);
		List<ItemStack> toCreate = new LinkedList<>();
		for (ItemInfo info : itemDb.getItemInfos()) {
			for (SimplifiedRecipe recipe : info.getRecipes()) {
				for (RecipeChoice choice : recipe.getInputs().keySet()) {
					for (ItemStack item : choiceToItemStacks(choice)) {
						toCreate.add(item);
					}
				}
			}
		}
		for (ItemStack item : toCreate) {
			itemDb.getOrCreateItemInfo(item);
		}
		return itemDb;
	}

	private Map<Material,ItemStack> blockDrops = null;
	private Map<Material,ItemStack> getBlockDrops() {
		if (blockDrops == null) {
			Map<Material,ItemStack> bd = new HashMap<>();
			Block testBlock = findTestBlock();
			try {
				for (Material material : Material.values()) {
					if (!material.isItem()) continue;
					try {
						testBlock.setType(material, false);
					} catch (IllegalArgumentException iae) {
						// it's not a block
						continue;
					}
					Collection<ItemStack> drops = testBlock.getDrops();
					if (drops.size() != 1) continue;
					ItemStack drop = drops.iterator().next();
					Material dropType = drop.getType();
					if (dropType == material || dropType == Material.AIR) continue;
					bd.put(material, drop);
				}
			} finally {
				testBlock.setType(Material.AIR);
			}
			blockDrops = Collections.unmodifiableMap(bd);
		}
		return blockDrops;
	}

	private void furnaceRecipe(FurnaceRecipe recipe, ItemDatabase itemDb) {
		itemDb.addRecipe(
				new SimplifiedRecipe(
						Arrays.asList(new RecipeChoice[]{recipe.getInputChoice()}),
						recipe.getResult(),
						SimplifiedRecipe.RecipeType.COOKING
				)
		);
	}

	private static final ItemStack air = new ItemStack(Material.AIR);

	private void shapedRecipe(ShapedRecipe recipe, ItemDatabase itemDb) {
		Map<Character, RecipeChoice> choiceMap = recipe.getChoiceMap();
		String[] shape = recipe.getShape();
		List<RecipeChoice> choices = new ArrayList<>();
		for (String string : shape) {
			for (char c : string.toCharArray()) {
				RecipeChoice recipeChoice = choiceMap.get(c);
				if (recipeChoice == null || recipeChoice.test(air)) continue;
				choices.add(recipeChoice);
			}
		}
		itemDb.addRecipe(
				new SimplifiedRecipe(
						choices,
						recipe.getResult(),
						SimplifiedRecipe.RecipeType.CRAFTING
				)
		);
	}

	private void shapelessRecipe(ShapelessRecipe recipe, ItemDatabase itemDb) {
		itemDb.addRecipe(
				new SimplifiedRecipe(
						recipe.getChoiceList(),
						recipe.getResult(),
						SimplifiedRecipe.RecipeType.CRAFTING
				)
		);
	}

	private void smithingRecipe(SmithingRecipe recipe, ItemDatabase itemDb) {
		itemDb.addRecipe(
				new SimplifiedRecipe(
						Arrays.asList(new RecipeChoice[]{recipe.getBase(), recipe.getAddition()}),
						recipe.getResult(),
						SimplifiedRecipe.RecipeType.SMITHING
				)
		);
	}

	private void stonecuttingRecipe(StonecuttingRecipe recipe, ItemDatabase itemDb) {
		itemDb.addRecipe(
				new SimplifiedRecipe(
						Arrays.asList(new RecipeChoice[]{recipe.getInputChoice()}),
						recipe.getResult(),
						SimplifiedRecipe.RecipeType.STONECUTTING
				)
		);
	}

	private Block findTestBlock() {
		World world = Bukkit.getWorlds().get(0);
		Block testBlock = world.getSpawnLocation().getBlock();
		testBlock = world.getBlockAt(testBlock.getX(), world.getMaxHeight(), testBlock.getZ());
		while (testBlock.getY() > world.getMinHeight()) {
			Block below = testBlock.getRelative(BlockFace.DOWN);
			if (below.getType() == Material.AIR)
				testBlock = below;
			else
				break;
		}
		return testBlock;
	}
}
