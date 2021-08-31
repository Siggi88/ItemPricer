package io.siggi.itempricer;

import hk.siggi.bukkit.nbt.NBTCompound;
import hk.siggi.bukkit.nbt.NBTTool;
import io.siggi.itempricer.itemnamer.ItemNamer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;

import java.util.*;
import java.util.function.Predicate;

public class Util {
	private static String minecraftVersion = null;
	public static String getMinecraftVersion() {
		if (minecraftVersion == null) {
			String bukkitVer = Bukkit.getServer().getVersion();
			int index = bukkitVer.indexOf("(MC: ");
			if (index > 0) {
				minecraftVersion = bukkitVer.substring(index + 5);
				index = minecraftVersion.indexOf(")");
				if (index > 0) minecraftVersion = minecraftVersion.substring(0, index);
			} else {
				minecraftVersion = null;
			}
		}
		return minecraftVersion;
	}

	public static <T> Iterable<T> iterable(Iterator<T> iterator) {
		return new Iterable<T>() {
			public Iterator<T> iterator() {
				return iterator;
			}
		};
	}

	public static String getPotionType(ItemStack item) {
		NBTCompound tag = NBTTool.getUtil().getTag(item);
		if (tag == null) return null;
		return tag.getString("Potion");
	}

	public static ItemStack getItemWithPotion(String itemType, String potionType) {
		NBTCompound itemData = NBTTool.getUtil().newCompound();
		NBTCompound tag = NBTTool.getUtil().newCompound();
		itemData.setByte("Count", (byte) 1);
		itemData.setInt("DataVersion", 2730);
		itemData.setString("id", "minecraft:" + itemType);
		itemData.setCompound("tag", tag);
		tag.setString("Potion", "minecraft:" + potionType);
		return NBTTool.getUtil().itemFromNBT(itemData);
	}

	public static List<ItemStack> choiceToItemStacks(RecipeChoice choice) {
		if (choice instanceof RecipeChoice.ExactChoice) {
			RecipeChoice.ExactChoice exactChoice = (RecipeChoice.ExactChoice) choice;
			List<ItemStack> choices = exactChoice.getChoices();
			return choices;
		} else if (choice instanceof RecipeChoice.MaterialChoice) {
			RecipeChoice.MaterialChoice materialChoice = (RecipeChoice.MaterialChoice) choice;
			List<Material> choices = materialChoice.getChoices();
			List<ItemStack> items = new ArrayList<>();
			for (Material material : choices) {
				items.add(new ItemStack(material));
			}
			return items;
		}
		return null;
	}

	public static String choiceToString(RecipeChoice choice) {
		if (choice instanceof RecipeChoice.ExactChoice) {
			RecipeChoice.ExactChoice exactChoice = (RecipeChoice.ExactChoice) choice;
			List<ItemStack> choices = exactChoice.getChoices();
			if (choices.size() == 1) {
				return ItemNamer.get().nameOf(choices.get(0));
			}
			StringBuilder sb = new StringBuilder();
			sb.append("{");
			for (ItemStack item : choices) {
				if (sb.length() > 1) {
					sb.append(",");
				}
				sb.append(ItemNamer.get().nameOf(item));
			}
			sb.append("}");
			return sb.toString();
		} else if (choice instanceof RecipeChoice.MaterialChoice) {
			RecipeChoice.MaterialChoice materialChoice = (RecipeChoice.MaterialChoice) choice;
			List<Material> choices = materialChoice.getChoices();
			if (choices.size() == 1) {
				return ItemNamer.get().nameOf(new ItemStack(choices.get(0)));
			}
			StringBuilder sb = new StringBuilder();
			sb.append("{");
			for (Material item : choices) {
				if (sb.length() > 1) {
					sb.append(",");
				}
				sb.append(ItemNamer.get().nameOf(new ItemStack(item)));
			}
			sb.append("}");
			return sb.toString();
		}
		return "(non-standard recipe choice)";
	}

	public static List<String> tabComplete(String currentValue, String... possibleValues) {
		return tabComplete(currentValue, Arrays.asList(possibleValues));
	}

	public static List<String> tabComplete(String currentValue, Collection<String> possibleValues) {
		String currentValueL = currentValue.toLowerCase();
		ArrayList<String> values = new ArrayList<>();
		for (String possibleValue : possibleValues) {
			if (possibleValue.toLowerCase().startsWith(currentValueL)) {
				values.add(possibleValue);
			}
		}
		return values;
	}

	private static List<String> materialList = null;

	public static List<String> materialList() {
		if (materialList == null) {
			materialList = new ArrayList<>();
			for (Material material : Material.values()) {
				String name = material.name();
				if (name.startsWith("LEGACY_")) continue;
				materialList.add(name.toLowerCase());
			}
		}
		return materialList;
	}

	public static Collection<Material> allMaterials(Predicate<Material> predicate) {
		Set<Material> materials = new HashSet<>();
		for (Material material : Material.values()) {
			if (predicate.test(material)) {
				materials.add(material);
			}
		}
		return materials;
	}
}
