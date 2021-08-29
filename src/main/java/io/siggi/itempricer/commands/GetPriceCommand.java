package io.siggi.itempricer.commands;

import io.siggi.itempricer.ItemPricer;
import io.siggi.itempricer.config.Amount;
import io.siggi.itempricer.itemdatabase.ItemInfo;
import io.siggi.itempricer.itemnamer.ItemNamer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.siggi.itempricer.Util.materialList;
import static io.siggi.itempricer.Util.tabComplete;

public class GetPriceCommand implements CommandExecutor, TabExecutor {
	private final ItemPricer plugin;

	public GetPriceCommand(ItemPricer plugin) {
		this.plugin = plugin;
	}

	private void printUsage(CommandSender sender, String label) {
		sender.sendMessage("Usage: /" + label + " - Get the price of the item in your hand (in-game only)");
		sender.sendMessage("Usage: /" + label + " [item_type] - Get the price of the specified item");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] params) {
		String variable = null;
		ItemStack item;
		switch (params.length) {
			case 0:
				if (!(sender instanceof Player)) {
					printUsage(sender, label);
				}
				item = ((Player) sender).getInventory().getItemInMainHand();
				break;
			case 1:
				try {
					if (params[0].startsWith("$")) {
						variable = params[0].substring(1);
						item = null;
					} else {
						item = new ItemStack(Material.valueOf(params[0].toUpperCase()));
					}
				} catch (IllegalArgumentException e) {
					printUsage(sender, label);
					return true;
				}
				break;
			default:
				printUsage(sender, label);
				return true;
		}
		if (variable != null) {
			Amount amount = plugin.getConfiguration().variables.get(variable);
			if (amount == null) {
				sender.sendMessage(ChatColor.RED + "Variable " + ChatColor.GOLD + variable + ChatColor.RED + " not found.");
			} else {
				sender.sendMessage(ChatColor.AQUA + variable + ChatColor.GOLD + " = " + ChatColor.AQUA + amount.toString());
				if (!amount.isPure()) {
					sender.sendMessage(ChatColor.GOLD + "Evaluates to: " + ChatColor.AQUA + amount.get());
				}
			}
			return true;
		}
		ItemInfo itemInfo = plugin.getItemDatabase().getItemInfo(item);
		if (itemInfo == null) {
			sender.sendMessage(ChatColor.RED + "No info found for " + ChatColor.AQUA + ItemNamer.get().nameOf(item));
			return true;
		}
		sender.sendMessage(ChatColor.GOLD + "Item: " + ChatColor.AQUA + ItemNamer.get().nameOf(item));
		Amount adminPrice = itemInfo.getAdminPrice();
		if (adminPrice != null) {
			String adminPriceString = adminPrice.toColoredString();
			sender.sendMessage(ChatColor.GOLD + "Admin Price: " + adminPriceString);
			if (!adminPrice.isPure()) {
				sender.sendMessage(ChatColor.GOLD + "Evaluated Admin Price: " + ChatColor.AQUA + adminPrice.get());
			}
		}
		if (itemInfo.hasCalculatedPrice()) {
			sender.sendMessage(ChatColor.GOLD + "Price: " + ChatColor.AQUA + itemInfo.getPrice());
		} else {
			sender.sendMessage(ChatColor.GOLD + "Price: " + ChatColor.AQUA + "not set");
		}
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] params) {
		switch (params.length) {
			case 1:
				if (params[0].startsWith("$")) {
					List<String> variables = new ArrayList<>();
					for (String variable : plugin.getConfiguration().variables.keySet()) {
						variables.add("$" + variable);
					}
					return tabComplete(params[0], variables);
				}
				return tabComplete(params[0], materialList());
		}
		return Arrays.asList(new String[0]);
	}
}
