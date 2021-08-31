package io.siggi.itempricer.commands;

import io.siggi.itempricer.ItemPricer;
import io.siggi.itempricer.itemdatabase.ItemInfo;
import io.siggi.itempricer.itemdatabase.SimplifiedRecipe;
import io.siggi.itempricer.itemnamer.ItemNamer;
import io.siggi.itempricer.ui.RecipeBook;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

import static io.siggi.itempricer.Util.materialList;
import static io.siggi.itempricer.Util.tabComplete;

public class RecipesCommand implements CommandExecutor, TabExecutor {
	private ItemPricer plugin;

	public RecipesCommand(ItemPricer plugin) {
		this.plugin = plugin;
	}

	private void printUsage(CommandSender sender, String label) {
		sender.sendMessage("Usage: /" + label + " - Print recipes for item held main hand (in-game only!)");
		sender.sendMessage("Usage: /" + label + " item_type - Print recipes for the specified item_type");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] params) {
		ItemStack item;
		if (params.length == 0) {
			if (!(sender instanceof Player)) {
				printUsage(sender, label);
				return true;
			}
			Player p = (Player) sender;
			item = p.getInventory().getItemInMainHand();
		} else if (params.length == 1) {
			item = new ItemStack(Material.valueOf(params[0].toUpperCase()));
		} else {
			printUsage(sender, label);
			return true;
		}
		RecipeBook.open(sender, item);
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] params) {
		switch (params.length) {
			case 1:
				return tabComplete(params[0], materialList());
		}
		return Arrays.asList(new String[0]);
	}
}
