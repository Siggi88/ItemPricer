package io.siggi.itempricer.config;

import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class ConfigAmount {
	public ConfigAmount() {
	}
	public ConfigAmount(double base) {
		this.base = base;
	}

	ConfigAmount(Amount amount, ItemPricerSerializedConfiguration config) {
		base = amount.base;
		for (Map.Entry<ItemStack, Double> entry : amount.items.entrySet()) {
			items.put(config.mapItemStack(entry.getKey()), entry.getValue());
		}
		variables.putAll(amount.variables);
	}

	public double base = 0.0;
	public final Map<String, Double> items = new HashMap<>();
	public final Map<String, Double> variables = new HashMap<>();

	Amount toAmount(ItemPricerSerializedConfiguration config) {
		Amount amount = new Amount(base);
		for (Map.Entry<String, Double> entry : items.entrySet()) {
			amount.items.put(config.unmapItemStack(entry.getKey()), entry.getValue());
		}
		amount.variables.putAll(variables);
		return amount;
	}

	public boolean isPure() {
		return items.isEmpty() && variables.isEmpty();
	}
}
