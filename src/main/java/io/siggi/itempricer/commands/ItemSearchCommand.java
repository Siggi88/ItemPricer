package io.siggi.itempricer.commands;

import io.siggi.itempricer.ItemPricer;
import io.siggi.itempricer.Util;
import io.siggi.itempricer.config.Amount;
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

import static io.siggi.itempricer.Util.doubleToPriceString;
import static io.siggi.itempricer.Util.encapsulateTrueItem;

public class ItemSearchCommand implements CommandExecutor, TabExecutor {
	private final ItemPricer plugin;

	public ItemSearchCommand(ItemPricer plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] params) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "This command can only be used in-game.");
			return true;
		}
		Player p = (Player) sender;
		String searchString = params.length == 0 ? "" : params[0];
		for (int i = 1; i < params.length; i++) {
			searchString += " " + params[i];
		}
		String title = searchString.isEmpty() ? "All Items" : searchString;
		searchString = searchString.toLowerCase();
		ItemNamer itemNamer = ItemNamer.get();
		List<ItemStack> items = new ArrayList<>();
		for (ItemInfo info : plugin.getItemDatabase().getItemInfos()) {
			ItemStack item = info.getItemStack();
			String name = itemNamer.nameOf(item);
			if (name.toLowerCase().contains(searchString)) {
				items.add(annotate(info, item));
			}
		}
		items.sort(Comparator.comparing(itemNamer::nameOf));
		PaginatedInventory pi = new PaginatedInventory(p, title, items, (event) -> {
			if (event.getRawSlot() < 54) {
				ItemStack item = Util.extractTrueItem(event.getCurrentItem());
				switch (event.getClick()) {
					case RIGHT: {
						event.setCancelled(true);
						RecipeBook.open(p, item);
					}
					return;
				}
			}
			if (p.getGameMode() != GameMode.CREATIVE) {
				event.setCancelled(true);
			}
		}, null);
		pi.show();
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] params) {
		return Arrays.asList(new String[0]);
	}

	private ItemStack annotate(ItemInfo info, ItemStack item) {
		item = encapsulateTrueItem(item);
		Set<SimplifiedRecipe> recipes = info.getRecipes();
		ItemMeta itemMeta = item.getItemMeta();
		List<String> lore = new ArrayList<>();
		if (info.hasCalculatedPrice()) {
			lore.add("" + ChatColor.RESET + ChatColor.GOLD + "Price: " + ChatColor.AQUA + doubleToPriceString(info.getPrice()));
		} else {
			lore.add("" + ChatColor.RESET + ChatColor.GOLD + "Price: " + ChatColor.RED + "Not set");
		}
		Amount adminPrice = info.getAdminPrice();
		if (adminPrice != null) {
			if (adminPrice.isPure()) {
				lore.add("" + ChatColor.RESET + ChatColor.GOLD + "Admin Price: " + ChatColor.AQUA + doubleToPriceString(adminPrice.get()));
			} else {
				lore.add("" + ChatColor.RESET + ChatColor.GOLD + "Admin Price: " + ChatColor.AQUA + adminPrice.toColoredString());
				Double evaluatedAdminPrice = adminPrice.get();
				if (evaluatedAdminPrice!=null) {
					lore.add("" + ChatColor.RESET + ChatColor.GOLD + "Evaluated Admin Price: " + ChatColor.AQUA + doubleToPriceString(evaluatedAdminPrice));
				}
			}
		}
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
