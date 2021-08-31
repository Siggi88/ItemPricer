package io.siggi.itempricer.ui;

import io.siggi.itempricer.ItemPricer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.function.Consumer;

public class PaginatedInventory {
	private final Player p;
	private final String title;
	private final List<ItemStack> items;
	private final Consumer<InventoryClickEvent> clickHandler;
	private final Consumer<InventoryCloseEvent> closeHandler;
	private Inventory inventory;
	private boolean shown = false;
	private int currentPage = 0;

	public PaginatedInventory(Player p, String title, List<ItemStack> items, Consumer<InventoryClickEvent> clickHandler, Consumer<InventoryCloseEvent> closeHandler) {
		this.p = p;
		this.title = title;
		this.items = items;
		this.clickHandler = clickHandler;
		this.closeHandler = closeHandler;
	}

	public int pageCount() {
		return (items.size() + 44) / 45;
	}

	public void show() {
		if (shown) return;
		shown = true;
		inventory = Bukkit.createInventory(p, 54, title);
		updateInventory();
		ItemPricer.getInstance().getInventoryManager().openInventory(p, inventory, this::click, this::close);
	}

	private void updateInventory() {
		inventory.clear();
		int startAt = currentPage * 45;
		int count = Math.min(45, items.size() - startAt);
		for (int i = 0; i < count; i++) {
			inventory.setItem(i, items.get(startAt + i));
		}
		if (currentPage > 0) {
			ItemStack pageButton = new ItemStack(Material.PAPER);
			ItemMeta pageButtonItemMeta = pageButton.getItemMeta();
			pageButtonItemMeta.setDisplayName("" + ChatColor.RESET + ChatColor.YELLOW + "Previous Page");
			pageButton.setItemMeta(pageButtonItemMeta);
			inventory.setItem(45, pageButton);
		}
		if (currentPage < pageCount() - 1) {
			ItemStack pageButton = new ItemStack(Material.PAPER);
			ItemMeta pageButtonItemMeta = pageButton.getItemMeta();
			pageButtonItemMeta.setDisplayName("" + ChatColor.RESET + ChatColor.YELLOW + "Next Page");
			pageButton.setItemMeta(pageButtonItemMeta);
			inventory.setItem(53, pageButton);
		}
	}

	private void click(InventoryClickEvent event) {
		switch (event.getRawSlot()) {
			case 45:
				if (currentPage > 0) {
					currentPage -= 1;
					updateInventory();
				}
				event.setCancelled(true);
				return;
			case 53:
				if (currentPage < pageCount() - 1) {
					currentPage += 1;
					updateInventory();
				}
				event.setCancelled(true);
				return;
		}
		if (clickHandler != null) {
			clickHandler.accept(event);
		}
	}

	private void close(InventoryCloseEvent event) {
		if (closeHandler != null) {
			closeHandler.accept(event);
		}
	}
}
