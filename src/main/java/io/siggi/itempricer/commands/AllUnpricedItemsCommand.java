package io.siggi.itempricer.commands;

import io.siggi.itempricer.ItemPricer;
import io.siggi.itempricer.Util;
import io.siggi.itempricer.itemdatabase.ItemInfo;
import io.siggi.itempricer.itemdatabase.SimplifiedRecipe;
import io.siggi.itempricer.itemnamer.ItemNamer;
import io.siggi.itempricer.ui.PaginatedInventory;
import io.siggi.itempricer.ui.RecipeBook;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

import static io.siggi.itempricer.Util.encapsulateTrueItem;

public class AllUnpricedItemsCommand implements CommandExecutor, TabExecutor {

	private final ItemPricer plugin;

	public AllUnpricedItemsCommand(ItemPricer plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] params) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "This command can only be used in-game.");
			return true;
		}
		open((Player) sender);
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] params) {
		return Arrays.asList(new String[0]);
	}

	public void open(Player p) {
		List<ItemStack> items = new ArrayList<>();
		for (ItemInfo info : plugin.getItemDatabase().getItemInfos()) {
			if (!info.hasCalculatedPrice()) {
				ItemStack item = info.getItemStack();
				if (!plugin.getConfiguration().ignoredItems.contains(item)) {
					items.add(annotate(info, item));
				}
			}
		}
		ItemNamer itemNamer = ItemNamer.get();
		items.sort(Comparator.comparing(itemNamer::nameOf));
		PaginatedInventory pi = new PaginatedInventory(p, "Items with no price", items, (event) -> {
			if (event.getRawSlot() < 54) {
				ItemStack item = Util.extractTrueItem(event.getCurrentItem());
				switch (event.getClick()) {
					case RIGHT: {
						event.setCancelled(true);
						RecipeBook.open(p, item);
					}
					return;
					case DROP: {
						items.remove(item);
						event.setCancelled(true);
						event.getInventory().setItem(event.getRawSlot(), null);
						plugin.getConfiguration().ignoredItems.add(item);
						p.sendMessage(ChatColor.GOLD + "Added " + ChatColor.AQUA + itemNamer.nameOf(item) + ChatColor.GOLD + " to the ignore list.");
					}
					return;
				}
			}
			if (p.getGameMode() != GameMode.CREATIVE) {
				event.setCancelled(true);
			}
		}, null);
		pi.show();
	}

	private ItemStack annotate(ItemInfo info, ItemStack item) {
		item = encapsulateTrueItem(item);
		Set<SimplifiedRecipe> recipes = info.getRecipes();
		ItemMeta itemMeta = item.getItemMeta();
		List<String> lore = new ArrayList<>();
		lore.add("" + ChatColor.RESET + ChatColor.GOLD + "To set a price, place in your");
		lore.add("" + ChatColor.RESET + ChatColor.GOLD + "hotbar, then select this item");
		lore.add("" + ChatColor.RESET + ChatColor.GOLD + "then type " + ChatColor.YELLOW + "/setprice hand [price]");
		lore.add("");
		lore.add("" + ChatColor.RESET + ChatColor.GOLD + "To ignore this item, press Q");
		if (recipes.isEmpty()) {
			lore.add("" + ChatColor.RESET + ChatColor.GOLD + "Recipes: " + ChatColor.AQUA + "0");
		} else {
			lore.add("" + ChatColor.RESET + ChatColor.GOLD + "Recipes: " + ChatColor.AQUA + recipes.size() + " (right-click to see)");
		}
		itemMeta.setLore(lore);
		item.setItemMeta(itemMeta);
		return item;
	}
}
