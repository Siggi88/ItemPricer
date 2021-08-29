package io.siggi.itempricer.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import hk.siggi.bukkit.nbt.NBTTool;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.Map;

public class ConfigSerialization {
	private ConfigSerialization() {
	}
	public static final Gson itemStackGson;
	public static final Gson gson;
	static {
		GsonBuilder builder = new GsonBuilder();
		Gson defaultGson = builder.create();
		NBTTool.getSerializer().registerTo(builder);
		itemStackGson = builder.create();
		builder = new GsonBuilder();
		builder.registerTypeAdapter(ItemStack.class, new TypeAdapter<ItemStack>() {
			@Override
			public ItemStack read(JsonReader jsonReader) throws IOException {
				return itemStackGson.fromJson(jsonReader, ItemStack.class);
			}
			@Override
			public void write(JsonWriter jsonWriter, ItemStack itemStack) throws IOException {
				itemStackGson.toJson(itemStack, ItemStack.class, jsonWriter);
			}
		});
		builder.registerTypeAdapter(ConfigAmount.class, new TypeAdapter<ConfigAmount>() {
			@Override
			public ConfigAmount read(JsonReader jsonReader) throws IOException {
				if (jsonReader.peek() == JsonToken.NUMBER) {
					return new ConfigAmount(jsonReader.nextDouble());
				} else
				return defaultGson.fromJson(jsonReader, ConfigAmount.class);
			}
			@Override
			public void write(JsonWriter jsonWriter, ConfigAmount configAmount) throws IOException {
				if (configAmount.isPure()) {
					jsonWriter.value(configAmount.base);
				} else {
					jsonWriter.beginObject();
					if (configAmount.base != 0.0) {
						jsonWriter.name("base").value(configAmount.base);
					}
					if (!configAmount.items.isEmpty()) {
						jsonWriter.name("items").beginObject();
						for (Map.Entry<String,Double> entry : configAmount.items.entrySet()) {
							jsonWriter.name(entry.getKey()).value(entry.getValue());
						}
						jsonWriter.endObject();
					}
					if (!configAmount.variables.isEmpty()) {
						jsonWriter.name("variables").beginObject();
						for (Map.Entry<String,Double> entry : configAmount.variables.entrySet()) {
							jsonWriter.name(entry.getKey()).value(entry.getValue());
						}
						jsonWriter.endObject();
					}
					jsonWriter.endObject();
				}
			}
		});
		builder.setPrettyPrinting();
		gson = builder.create();
	}
}
