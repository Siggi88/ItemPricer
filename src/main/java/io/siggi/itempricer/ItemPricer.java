package io.siggi.itempricer;

import io.siggi.itempricer.commands.*;
import io.siggi.itempricer.config.ConfigSerialization;
import io.siggi.itempricer.config.ItemPricerConfiguration;
import io.siggi.itempricer.config.ItemPricerSerializedConfiguration;
import io.siggi.itempricer.itemdatabase.ItemDatabase;
import io.siggi.itempricer.itemdatabase.builder.RecipeSimplifier;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class ItemPricer extends JavaPlugin {
	private static ItemPricer instance;
	private ItemDatabase itemDatabase = null;
	private RecipeSimplifier recipeSimplifier = null;
	private ItemPricerConfiguration configuration = null;

	public ItemDatabase getItemDatabase() {
		if (!isEnabled()) {
			throw new IllegalStateException("ItemPricer is not enabled!");
		}
		if (itemDatabase == null) {
			itemDatabase = recipeSimplifier.generateDatabase();
			configuration.sendPricesToDatabase(itemDatabase);
			itemDatabase.recalculatePrices();
		}
		return itemDatabase;
	}

	@Override
	public void onLoad() {
		instance = this;
	}

	@Override
	public void onEnable() {
		getCommand("reloaditempricer").setExecutor(new ReloadItemPricerCommand(this));
		getCommand("dumprecipes").setExecutor(new DumpRecipesCommand(this));
		getCommand("recipes").setExecutor(new RecipesCommand(this));
		getCommand("getprice").setExecutor(new GetPriceCommand(this));
		getCommand("setprice").setExecutor(new SetPriceCommand(this));
		getCommand("dumpprices").setExecutor(new DumpPricesCommand(this));
		recipeSimplifier = new RecipeSimplifier(this);
		configuration = new ItemPricerConfiguration(this);
		loadConfiguration();
	}

	public static ItemPricer getInstance() {
		return instance;
	}

	public ItemPricerConfiguration getConfiguration() {
		return configuration;
	}

	public void loadConfiguration() {
		try {
			File df = getDataFolder();
			File configFile = new File(df, "config.json");
			if (configFile.exists()) {
				try (FileReader reader = new FileReader(configFile)) {
					ItemPricerSerializedConfiguration itemPricerSerializedConfiguration = ConfigSerialization.gson.fromJson(reader, ItemPricerSerializedConfiguration.class);
					itemPricerSerializedConfiguration.applyTo(configuration);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void saveConfiguration() {
		try {
			File df = getDataFolder();
			if (!df.exists()) df.mkdirs();
			File configFile = new File(df, "config.json");
			File tmpSaveFile = new File(df, "config.json.sav");
			ItemPricerSerializedConfiguration serializedConfig = new ItemPricerSerializedConfiguration(configuration);
			try (FileWriter writer = new FileWriter(tmpSaveFile)) {
				ConfigSerialization.gson.toJson(serializedConfig, ItemPricerSerializedConfiguration.class, writer);
			}
			tmpSaveFile.renameTo(configFile);
		} catch (Exception e) {
		}
	}
}
