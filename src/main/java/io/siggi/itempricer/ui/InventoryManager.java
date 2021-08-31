package io.siggi.itempricer.ui;

import io.siggi.itempricer.ItemPricer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InventoryManager implements Listener {
	private final ItemPricer plugin;

	private final Map<InventoryView, InventoryHandler> handlers = new HashMap<>();

	public InventoryManager(ItemPricer plugin) {
		this.plugin = plugin;
	}

	public void openInventory(Player p, Inventory inventory, InventoryHandler handler) {
		InventoryView view = p.openInventory(inventory);
		handlers.put(view, handler);
	}

	@EventHandler
	public void inventoryClick(InventoryClickEvent event) {
		InventoryView view = event.getView();
		InventoryHandler inventoryHandler = handlers.get(view);
		if (inventoryHandler != null)
			inventoryHandler.click(event);
	}

	@EventHandler
	public void inventoryClose(InventoryCloseEvent event) {
		InventoryView view = event.getView();
		InventoryHandler inventoryHandler = handlers.remove(view);
		if (inventoryHandler != null)
			inventoryHandler.close(event);
	}

	public void closeAll() {
		List<InventoryView> views = new ArrayList<>();
		views.addAll(handlers.keySet());
		for (InventoryView view : views) {
			view.close();
		}
	}
}
