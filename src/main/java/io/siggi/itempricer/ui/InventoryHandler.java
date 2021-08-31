package io.siggi.itempricer.ui;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.function.Consumer;

final class InventoryHandler {
	InventoryHandler(Consumer<InventoryClickEvent> clickHandler, Consumer<InventoryCloseEvent> closeHandler) {
		this.clickHandler = clickHandler;
		this.closeHandler = closeHandler;
	}

	private final Consumer<InventoryClickEvent> clickHandler;
	private final Consumer<InventoryCloseEvent> closeHandler;

	public void click(InventoryClickEvent event) {
		if (clickHandler != null)
			clickHandler.accept(event);
	}

	public void close(InventoryCloseEvent event) {
		if (closeHandler != null)
			closeHandler.accept(event);
	}
}
