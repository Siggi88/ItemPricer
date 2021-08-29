package io.siggi.itempricer.commands;

import io.siggi.itempricer.ItemPricer;
import io.siggi.itempricer.itemdatabase.ItemDatabase;
import io.siggi.itempricer.itemdatabase.ItemInfo;
import io.siggi.itempricer.itemdatabase.SimplifiedRecipe;
import io.siggi.itempricer.itemnamer.ItemNamer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class DumpRecipesCommand implements CommandExecutor {

	private final ItemPricer plugin;

	public DumpRecipesCommand(ItemPricer plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] params) {
		ItemDatabase database = plugin.getItemDatabase();
		ItemNamer namer = ItemNamer.get();
		for (ItemInfo info : database.getItemInfos()) {
			sender.sendMessage(namer.nameOf(info.getItemStack()) + ":");
			for (SimplifiedRecipe recipe : info.getRecipes()) {
				sender.sendMessage(recipe.toString());
			}
		}
		return true;
	}


}
