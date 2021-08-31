package io.siggi.itempricer.ui;

import io.siggi.itempricer.ItemPricer;
import io.siggi.itempricer.Util;
import io.siggi.itempricer.config.Amount;
import io.siggi.itempricer.itemdatabase.ItemInfo;
import io.siggi.itempricer.itemdatabase.SimplifiedRecipe;
import io.siggi.itempricer.itemnamer.ItemNamer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

import static io.siggi.itempricer.Util.*;

public class RecipeBook {
	private final String title;
	private final List<SimplifiedRecipe> recipes;
	private final Player p;
	private int currentPage = 0;
	private final int pageCount;
	private Inventory inventory;
	private int cycleIndex = 0;
	private BukkitRunnable runnable;

	public static void open(CommandSender sender, ItemStack item) {
		ItemInfo itemInfo = ItemPricer.getInstance().getItemDatabase().getItemInfo(item);
		Set<SimplifiedRecipe> recipes;
		if (itemInfo == null || (recipes = itemInfo.getRecipes()).isEmpty()) {
			sender.sendMessage(ChatColor.RED + "No recipes found for this item.");
			return;
		}
		for (SimplifiedRecipe recipe : recipes) {
			if (sender instanceof Player) {
				new RecipeBook(ItemNamer.get().nameOf(item), recipes, (Player) sender).show();
			} else {
				String recipeString = recipe.toString();
				int colonPos = recipeString.indexOf(":");
				recipeString = ChatColor.GOLD + recipeString.substring(0, colonPos + 1)
						+ ChatColor.AQUA + recipeString.substring(colonPos + 1)
						.replace(",", ChatColor.GOLD + "," + ChatColor.AQUA)
						.replace(" makes ", ChatColor.GOLD + " makes " + ChatColor.AQUA)
						.replace("{", ChatColor.GOLD + "{" + ChatColor.AQUA)
						.replace("}", ChatColor.GOLD + "}" + ChatColor.AQUA);
				sender.sendMessage(recipeString);
			}
		}
	}

	private RecipeBook(String title, Collection<SimplifiedRecipe> recipes, Player p) {
		this.title = title;
		if (recipes instanceof List) {
			this.recipes = (List<SimplifiedRecipe>) recipes;
		} else {
			this.recipes = new ArrayList<>();
			this.recipes.addAll(recipes);
		}
		this.p = p;
		if (recipes.size() > 6) {
			pageCount = (recipes.size() + 4) / 5;
		} else {
			pageCount = 1;
		}
	}

	public void show() {
		if (runnable != null) return;
		inventory = Bukkit.createInventory(p, pageCount == 1 ? (9 * recipes.size()) : 54, title);
		ItemPricer.getInstance().getInventoryManager().openInventory(p, inventory, this::click, this::close);
		updateInventory(true);
		(runnable = new BukkitRunnable() {
			@Override
			public void run() {
				updateInventory(false);
			}
		}).runTaskTimer(ItemPricer.getInstance(), 20L, 20L);
	}

	private void updateInventory(boolean mustClear) {
		cycleIndex++;
		if (mustClear) {
			cycleIndex = 0;
			inventory.clear();
		}
		int startAt = currentPage * 5;
		int count;
		if (pageCount == 1) {
			count = recipes.size();
		} else {
			count = Math.min(5, recipes.size() - startAt);
			ItemStack prevPage = new ItemStack(Material.PAPER);
			ItemMeta prevPageItemMeta = prevPage.getItemMeta();
			prevPageItemMeta.setDisplayName("Previous Page");
			prevPage.setItemMeta(prevPageItemMeta);
			ItemStack nextPage = new ItemStack(Material.PAPER);
			ItemMeta nextPageItemMeta = nextPage.getItemMeta();
			nextPageItemMeta.setDisplayName("Next Page");
			nextPage.setItemMeta(nextPageItemMeta);
			if (currentPage != 0)
				inventory.setItem(45, prevPage);
			if (currentPage < pageCount - 1)
				inventory.setItem(53, nextPage);
		}
		for (int i = 0; i < count; i++) {
			SimplifiedRecipe recipe = recipes.get(startAt + i);
			int slot = i * 9;
			for (Map.Entry<RecipeChoice, Integer> entry : recipe.getInputs().entrySet()) {
				RecipeChoice choice = entry.getKey();
				int itemCount = entry.getValue();
				List<ItemStack> itemStacks = choiceToItemStacks(choice);
				ItemStack item = itemStacks.get(cycleIndex % itemStacks.size());
				item.setAmount(itemCount);
				inventory.setItem(slot++, annotate(item, recipe));
			}
		}
	}

	private void click(InventoryClickEvent event) {
		if (pageCount != 1) {
			switch (event.getRawSlot()) {
				case 45:
					if (currentPage > 0) {
						currentPage -= 1;
						updateInventory(true);
						event.setCancelled(true);
						return;
					}
				case 54:
					if (currentPage < pageCount - 1) {
						currentPage += 1;
						updateInventory(true);
						event.setCancelled(true);
						return;
					}
			}
		}
		if (event.getRawSlot() < inventory.getSize()) {
			ItemStack currentItem = event.getCurrentItem();
			if (currentItem == null || currentItem.getType() == Material.AIR) return;
			ItemStack trueItem = Util.extractTrueItem(currentItem);
			switch (event.getClick()) {
				case RIGHT: {
					open(p, trueItem);
					event.setCancelled(true);
				}
				break;
				case DROP: {
					int recipeIndex = (event.getSlot() / 9) - (currentPage * 5);
					if (recipeIndex < recipes.size()) {
						SimplifiedRecipe recipe = recipes.get(recipeIndex);
						ShapedRecipe shapedRecipe = recipe.getShapedRecipe();
						if (shapedRecipe != null) {
							Inventory craftingTable = Bukkit.createInventory(p, 27, "Crafting Recipe: " + title);
							Map<Character, RecipeChoice> choiceMap = shapedRecipe.getChoiceMap();
							String[] shape = shapedRecipe.getShape();
							for (int y = 0; y < shape.length; y++) {
								String shapeLine = shape[y];
								for (int x = 0; x < shapeLine.length(); x++) {
									int inventorySlot = y * 9 + x + 3;
									char choiceId = shapeLine.charAt(x);
									RecipeChoice recipeChoice = choiceMap.get(choiceId);
									if (recipeChoice == null) continue;
									List<ItemStack> possibleItems = choiceToItemStacks(recipeChoice);
									if (possibleItems == null || possibleItems.isEmpty()) continue;
									craftingTable.setItem(inventorySlot, possibleItems.get(0));
								}
							}
							ItemPricer.getInstance().getInventoryManager().openInventory(p, craftingTable, e -> e.setCancelled(true), null);
						}
					}
					event.setCancelled(true);
				}
				default: {
					if (p.getGameMode() != GameMode.CREATIVE) {
						event.setCancelled(true);
					} else {
						event.setCurrentItem(trueItem);
					}
				}
			}

		} else if (p.getGameMode() != GameMode.CREATIVE) {
			event.setCancelled(true);
		}
	}

	private void close(InventoryCloseEvent event) {
		runnable.cancel();
	}

	private ItemStack annotate(ItemStack item, SimplifiedRecipe recipe) {
		ItemInfo itemInfo = ItemPricer.getInstance().getItemDatabase().getItemInfo(item);
		item = encapsulateTrueItem(item);
		ItemMeta itemMeta = item.getItemMeta();
		List<String> lore = new ArrayList<>();
		if (itemInfo != null) {
			if (itemInfo.hasCalculatedPrice()) {
				lore.add("" + ChatColor.RESET + ChatColor.GOLD + "Item Value: " + ChatColor.AQUA + doubleToPriceString(itemInfo.getPrice()));
			} else {
				lore.add("" + ChatColor.RESET + ChatColor.GOLD + "Item Value: " + ChatColor.RED + "Not set");
			}
			Amount adminPrice = itemInfo.getAdminPrice();
			if (adminPrice != null) {
				if (adminPrice.isPure()) {
					lore.add("" + ChatColor.RESET + ChatColor.GOLD + "Admin Value: " + ChatColor.AQUA + doubleToPriceString(adminPrice.get()));
				} else {
					lore.add("" + ChatColor.RESET + ChatColor.GOLD + "Admin Value: " + ChatColor.AQUA + adminPrice.toColoredString());
					Double evaluatedPrice = adminPrice.get();
					if (evaluatedPrice != null)
						lore.add("" + ChatColor.RESET + ChatColor.GOLD + "Evaluated Admin Value: " + ChatColor.AQUA + doubleToPriceString(evaluatedPrice));
				}
			}
		} else {
			lore.add("" + ChatColor.RESET + ChatColor.GOLD + "Item Value: " + ChatColor.RED + "Not set");
		}
		lore.add("" + ChatColor.RESET + ChatColor.GOLD + "Right click to see this item's recipes");
		lore.add("");
		lore.add("" + ChatColor.RESET + ChatColor.GOLD + "Recipe Type: " + ChatColor.AQUA + recipe.getType().verbPresentTense);
		int amount = recipe.getOutput().getAmount();
		if (amount != 1) {
			lore.add("" + ChatColor.RESET + ChatColor.GOLD + "Makes " + ChatColor.AQUA + amount);
		}
		if (recipe.getType() == SimplifiedRecipe.RecipeType.CRAFTING) {
			if (recipe.getShapedRecipe() != null) {
				lore.add("" + ChatColor.RESET + ChatColor.GOLD + "Shaped Recipe: " + ChatColor.AQUA + "Yes" + ChatColor.GOLD + " (press Q to see)");
			} else {
				lore.add("" + ChatColor.RESET + ChatColor.GOLD + "Shaped Recipe: " + ChatColor.AQUA + "No");
			}
		}
		itemMeta.setLore(lore);
		item.setItemMeta(itemMeta);
		return item;
	}
}
