package io.siggi.itempricer.config;

import io.siggi.itempricer.ItemPricer;
import io.siggi.itempricer.itemdatabase.ItemDatabase;
import io.siggi.itempricer.itemnamer.ItemNamer;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class Amount {
	public Amount() {
	}

	public Amount(double amount) {
		base = amount;
	}

	public double base = 0.0;
	public final Map<ItemStack, Double> items = new HashMap<>();
	public final Map<String, Double> variables = new HashMap<>();

	public Double get() {
		double amount = base;
		if (!items.isEmpty() || !variables.isEmpty()) {
			ItemPricer itemPricer = ItemPricer.getInstance();
			ItemDatabase database = itemPricer.getItemDatabase();
			ItemPricerConfiguration config = itemPricer.getConfiguration();
			for (Map.Entry<ItemStack, Double> itemEntry : items.entrySet()) {
				ItemStack key = itemEntry.getKey();
				double price = database.getPrice(key);
				if (price == 0.0)
					return null;
				double multiplier = itemEntry.getValue();
				amount += price * multiplier;
			}
			for (Map.Entry<String, Double> variableEntry : variables.entrySet()) {
				String key = variableEntry.getKey();
				Double value = config.getVariable(key);
				if (value == null)
					return null;
				double multiplier = variableEntry.getValue();
				amount += value * multiplier;
			}
		}
		return amount;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<ItemStack, Double> itemEntry : items.entrySet()) {
			if (sb.length() > 0)
				sb.append(" + ");
			double multiplier = itemEntry.getValue();
			if (multiplier != 1.0) {
				sb.append(multiplier).append("*");
			}
			sb.append("priceOf(").append(ItemNamer.get().nameOf(itemEntry.getKey())).append(")");
		}
		for (Map.Entry<String, Double> variableEntry : variables.entrySet()) {
			if (sb.length() > 0)
				sb.append(" + ");
			double multiplier = variableEntry.getValue();
			if (multiplier != 1.0) {
				sb.append(multiplier).append("*");
			}
			sb.append(variableEntry.getKey());
		}
		if (this.base != 0.0 || sb.length() == 0) {
			if (sb.length() > 0) {
				sb.append(" + ");
			}
			sb.append(this.base);
		}
		return sb.toString();
	}

	public boolean isPure() {
		return items.isEmpty() && variables.isEmpty();
	}

	public String toColoredString() {
		return ChatColor.AQUA + toString()
				.replace("+", ChatColor.GOLD + "+" + ChatColor.AQUA)
				.replace("priceOf(", ChatColor.GOLD + "priceOf(" + ChatColor.AQUA)
				.replace(")", ChatColor.GOLD + ")" + ChatColor.AQUA);
	}
}
