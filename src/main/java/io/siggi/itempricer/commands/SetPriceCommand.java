package io.siggi.itempricer.commands;

import io.siggi.itempricer.ItemPricer;
import io.siggi.itempricer.config.Amount;
import io.siggi.itempricer.itemnamer.ItemNamer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.siggi.itempricer.Util.materialList;
import static io.siggi.itempricer.Util.tabComplete;

public class SetPriceCommand implements CommandExecutor, TabExecutor {
	private final ItemPricer plugin;

	public SetPriceCommand(ItemPricer plugin) {
		this.plugin = plugin;
	}

	private void printUsage(CommandSender sender, String label) {
		sender.sendMessage("Usage: /" + label + " [price] - Set the price of the item in your hand (in-game only)");
		sender.sendMessage("Usage: /" + label + " [item_type] [price] - Set the price of the specified item");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] params) {
		Amount price;
		String variable = null;
		ItemStack item;
		if (params.length == 0) {
			printUsage(sender, label);
			return true;
		} else if (params.length == 1) {
			if (!(sender instanceof Player)) {
				printUsage(sender, label);
				return true;
			}
			item = ((Player) sender).getInventory().getItemInMainHand();
			try {
				price = parseAmount(sender, params, 0);
			} catch (NumberFormatException nfe) {
				printUsage(sender, label);
				return true;
			}
		} else {
			try {
				if (params[0].startsWith("$")) {
					variable = params[0].substring(1);
					item = null;
				} else if (params[0].equalsIgnoreCase("hand")) {
					if (!(sender instanceof Player)) {
						printUsage(sender, label);
						return true;
					}
					item = ((Player) sender).getInventory().getItemInMainHand();
				} else
					item = new ItemStack(Material.valueOf(params[0].toUpperCase()));
				price = parseAmount(sender, params, 1);
			} catch (IllegalArgumentException e) {
				printUsage(sender, label);
				return true;
			}
		}

		if (variable != null) {
			if (variable.length() < 2) {
				sender.sendMessage(ChatColor.RED + "Variable names must be at least 2 characters!");
			} else if (!isValidVariableName(variable)) {
				sender.sendMessage(ChatColor.RED + "Variable names can only contain A-Z, a-z, 0-9, -, _, and non-ASCII characters.");
			} else if (isNumericChar(variable.charAt(0))) {
				sender.sendMessage(ChatColor.RED + "Variable names cannot start with a number!");
			} else {
				plugin.getConfiguration().variables.put(variable, price);
				sender.sendMessage(ChatColor.GOLD + "Set variable " + ChatColor.AQUA + variable + ChatColor.GOLD + " to " + ChatColor.AQUA + price.toString() + ChatColor.GOLD + ".");
			}
		} else {
			plugin.getConfiguration().setItemPrice(item, price);
			sender.sendMessage(ChatColor.GOLD + "Set price of " + ChatColor.AQUA + ItemNamer.get().nameOf(item) + ChatColor.GOLD + " to " + price.toColoredString() + ChatColor.GOLD + ".");
		}
		plugin.getConfiguration().sendPricesToDatabase(plugin.getItemDatabase());
		plugin.saveConfiguration();
		plugin.getItemDatabase().recalculatePrices();
		return true;
	}

	private boolean isNumericChar(char c) {
		return ((c >= '0' && c <= '9') || c == '.');
	}

	public boolean isValidVariableName(String variable) {
		for (char c : variable.toCharArray()) {
			if (!isValidVariableCharacter(c)) return false;
		}
		return true;
	}

	private boolean isValidVariableCharacter(char c) {
		return (
				(c >= 'a' && c <= 'z')
						|| (c >= 'A' && c <= 'Z')
						|| (c >= '0' && c <= '9')
						|| c == '-'
						|| c == '_'
						|| c > ((char) 127)
		);
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] params) {
		switch (params.length) {
			case 1: {
				List<String> possibleValues = new ArrayList<>();
				if (params[0].startsWith("$")) {
					for (String variable : plugin.getConfiguration().variables.keySet()) {
						possibleValues.add("$" + variable);
					}
					return tabComplete(params[0], possibleValues);
				} else {
					if (sender instanceof Player) possibleValues.add("hand");
					possibleValues.addAll(materialList());
				}
				return tabComplete(params[0], possibleValues);
			}
			default: {
				List<String> possibleValues = new ArrayList<>();
				String soFar = params[params.length - 1];
				int numberPrefixLength = getNumberPrefixLength(soFar);
				String numberPrefix = soFar.substring(0, numberPrefixLength);
				if (sender instanceof Player) {
					PlayerInventory inventory = ((Player) sender).getInventory();
					for (int i = 0; i < 9; i++) {
						String suffix = new String(new char[]{(char) ('a' + i)});
						ItemStack item = inventory.getItem(i);
						if (item != null && item.getType() != Material.AIR) {
							possibleValues.add(numberPrefix + suffix + "/" + ItemNamer.get().nameOf(item).replace(" ",""));
						}
					}
				}
				for (String variable : plugin.getConfiguration().variables.keySet())
					possibleValues.add(numberPrefix + variable);
				return tabComplete(soFar, possibleValues);
			}
		}
	}

	private Amount parseAmount(CommandSender sender, String[] params, int startAt) {
		Amount amount = new Amount();
		for (int i = startAt; i < params.length; i++) {
			String param = params[i];
			int numberPrefixLength = getNumberPrefixLength(param);
			String numberPrefix = param.substring(0, numberPrefixLength);
			String variable = param.substring(numberPrefixLength);
			if (variable.contains("/")) variable = variable.substring(0, variable.indexOf("/"));
			double multiplier = numberPrefix.isEmpty() ? 1.0 : Double.parseDouble(numberPrefix);
			switch (variable.length()) {
				case 0: {
					// it's not a multiplier anymore, it's a base now
					amount.base = multiplier;
				}
				break;
				case 1: {
					int index = (int) (variable.toLowerCase().charAt(0) - 'a');
					if (index < 0 || index > 9) {
						break;
					}
					ItemStack item = ((Player) sender).getInventory().getItem(index);
					amount.items.put(item, multiplier);
				}
				break;
				default: {
					amount.variables.put(variable, multiplier);
				}
				break;
			}
		}
		return amount;
	}

	public int getNumberPrefixLength(String str) {
		for (int i = 0; i < str.length(); i++) {
			if (!isNumericChar(str.charAt(i))) {
				return i;
			}
		}
		return str.length();
	}
}
