package io.siggi.itempricer.commands;

import io.siggi.itempricer.ItemPricer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.Arrays;
import java.util.List;

public class ReloadItemPricerCommand implements CommandExecutor, TabExecutor {
	private final ItemPricer plugin;
	public ReloadItemPricerCommand(ItemPricer plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] strings) {
		plugin.loadConfiguration();
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] params) {
		return Arrays.asList(new String[0]);
	}
}
