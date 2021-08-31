package io.siggi.itempricer.ui;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

@FunctionalInterface
public interface InventoryHandler {
	public void click(InventoryClickEvent event);

	public default void close(InventoryCloseEvent event) {
	}
}
