package io.siggi.itempricer.commands;

import io.siggi.itempricer.ItemPricer;
import io.siggi.itempricer.Util;
import io.siggi.itempricer.config.Amount;
import io.siggi.itempricer.itemdatabase.ItemDatabase;
import io.siggi.itempricer.itemdatabase.ItemInfo;
import io.siggi.itempricer.itemdatabase.SimplifiedRecipe;
import io.siggi.itempricer.itemnamer.ItemNamer;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class DumpPricesCommand implements CommandExecutor {
	private final ItemPricer plugin;

	public DumpPricesCommand(ItemPricer plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] params) {
		ItemNamer itemNamer = ItemNamer.get();
		ItemDatabase itemDatabase = plugin.getItemDatabase();
		try {
			File df = plugin.getDataFolder();
			if (!df.exists()) {
				df.mkdirs();
			}
			File outputFile = new File(df, "dump.csv");
			try (FileWriter writer = new FileWriter(outputFile)) {
				writer.write("Item Name,Potion,Price,\"What Determines Price?\"\n");
				List<String> allPricedItems = new ArrayList<>();
				List<String> allMissingItemsNoRecipe = new ArrayList<>();
				List<String> allMissingItems = new ArrayList<>();
				for (ItemInfo info : itemDatabase.getItemInfos()) {
					String itemName = itemNamer.nameOf(info.getItemStack());
					String potionType = Util.getPotionType(info.getItemStack());
					if (potionType == null) potionType = "";
					double price = info.getPrice();
					Amount adminPrice = info.getAdminPrice();
					SimplifiedRecipe cheapestRecipe = info.getCheapestRecipe();
					String cheapestRecipeStr = cheapestRecipe == null ? (adminPrice == null ? "" : ("Admin Price: " + adminPrice.toString())) : ("\"" + cheapestRecipe.getRecipeString() + "\"");
					SimplifiedRecipe topRecipe = info.getTopRecipe();
					String recipeStr = topRecipe == null ? "" : ("\"" + topRecipe.getRecipeString() + "\"");
					if (info.hasCalculatedPrice())
						allPricedItems.add(itemName + "," + potionType + "," + price + "," + cheapestRecipeStr);
					else if (topRecipe == null)
						allMissingItemsNoRecipe.add(itemName + (potionType.isEmpty() ? "" : ("," + potionType)));
					else
						allMissingItems.add(itemName + "," + potionType + ",," + recipeStr);
				}
				allPricedItems.sort((s1, s2) -> s1.compareTo(s2));
				for (String line : allPricedItems) {
					writer.write(line + "\n");
				}
				allMissingItemsNoRecipe.sort((s1, s2) -> s1.compareTo(s2));
				for (String line : allMissingItemsNoRecipe) {
					writer.write(line + "\n");
				}
				allMissingItems.sort((s1, s2) -> s1.compareTo(s2));
				for (String line : allMissingItems) {
					writer.write(line + "\n");
				}
			}
			sender.sendMessage(ChatColor.GOLD + "Prices dumped to: " + ChatColor.AQUA + outputFile.getAbsolutePath());
		} catch (Exception e) {
			e.printStackTrace();
			sender.sendMessage(ChatColor.RED + "Unable to dump prices -- see console for stack trace.");
		}
		return true;
	}
}
