package com.github.lunatrius.imc.deserializer;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import cpw.mods.fml.common.registry.GameData;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.nbt.NBTTagString;

import java.lang.reflect.Type;

public class NBTTagCompoundDeserializer implements JsonDeserializer<NBTTagCompound> {
	public static final String MEMBER_NAME = "name";
	public static final String MEMBER_VALUE = "value";
	public static final String DELIMITER = ":";

	public static final String ID_TYPE_BLOCK = "block";
	public static final String ID_TYPE_ITEM = "item";

	public static final String TYPE_BOOLEAN = "boolean";
	public static final String TYPE_BYTE = "byte";
	public static final String TYPE_SHORT = "short";
	public static final String TYPE_INT = "int";
	public static final String TYPE_LONG = "long";
	public static final String TYPE_FLOAT = "float";
	public static final String TYPE_DOUBLE = "double";
	public static final String TYPE_BYTE_ARRAY = "byte[]";
	public static final String TYPE_STRING = "string";
	public static final String TYPE_LIST = "list";
	public static final String TYPE_COMPOUND = "compound";
	public static final String TYPE_INT_ARRAY = "int[]";

	@Override
	public NBTTagCompound deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		if (!json.isJsonObject()) {
			return null;
		}

		JsonObject jsonObject = json.getAsJsonObject();
		JsonElement value = jsonObject.get(MEMBER_VALUE);
		return getCompound(value.getAsJsonArray());
	}

	private NBTBase getNBTTag(JsonObject jsonObject, String type, String name) {
		JsonElement value = jsonObject.get(MEMBER_VALUE);

		if (type.equalsIgnoreCase(TYPE_BOOLEAN)) {
			return new NBTTagByte((byte) (value.getAsBoolean() ? 1 : 0));
		} else if (type.equalsIgnoreCase(TYPE_BYTE)) {
			return new NBTTagByte(value.getAsByte());
		} else if (type.equalsIgnoreCase(TYPE_SHORT)) {
			NBTBase.NBTPrimitive primitive = getIdFromString(value, type);

			if (primitive != null) {
				return primitive;
			}

			return new NBTTagShort(value.getAsShort());
		} else if (type.equalsIgnoreCase(TYPE_INT)) {
			NBTBase.NBTPrimitive primitive = getIdFromString(value, type);

			if (primitive != null) {
				return primitive;
			}

			return new NBTTagInt(value.getAsInt());
		} else if (type.equalsIgnoreCase(TYPE_LONG)) {
			return new NBTTagLong(value.getAsLong());
		} else if (type.equalsIgnoreCase(TYPE_FLOAT)) {
			return new NBTTagFloat(value.getAsFloat());
		} else if (type.equalsIgnoreCase(TYPE_DOUBLE)) {
			return new NBTTagDouble(value.getAsDouble());
		} else if (type.equalsIgnoreCase(TYPE_BYTE_ARRAY)) {
			return getByteArray(value.getAsJsonArray());
		} else if (type.equalsIgnoreCase(TYPE_STRING)) {
			return new NBTTagString(value.getAsString());
		} else if (type.equalsIgnoreCase(TYPE_LIST)) {
			return getList(value.getAsJsonArray());
		} else if (type.equalsIgnoreCase(TYPE_COMPOUND)) {
			return getCompound(value.getAsJsonArray());
		} else if (type.equalsIgnoreCase(TYPE_INT_ARRAY)) {
			return getIntArray(value.getAsJsonArray());
		}

		return null;
	}

	private NBTBase.NBTPrimitive getIdFromString(JsonElement jsonElement, String type) {
		if (jsonElement.isJsonPrimitive()) {
			JsonPrimitive jsonPrimitive = jsonElement.getAsJsonPrimitive();

			if (jsonPrimitive.isString()) {
				String[] split = jsonPrimitive.getAsString().split(DELIMITER, 2);
				int id = -1;

				if (split.length == 2) {
					if (split[0].equalsIgnoreCase(ID_TYPE_BLOCK)) {
						id = GameData.getBlockRegistry().getId(split[1]);
					} else if (split[0].equalsIgnoreCase(ID_TYPE_ITEM)) {
						id = GameData.getItemRegistry().getId(split[1]);
					}
				} else {
					id = GameData.getBlockRegistry().getId(split[1]);

					if (id == -1) {
						id = GameData.getItemRegistry().getId(split[1]);
					}
				}

				if (id >= 0) {
					if (type.equalsIgnoreCase(TYPE_SHORT)) {
						return new NBTTagShort((short) id);
					} else if (type.equalsIgnoreCase(TYPE_INT)) {
						return new NBTTagInt(id);
					}
				}
			}
		}

		return null;
	}

	private NBTTagCompound getCompound(JsonArray jsonArray) {
		NBTTagCompound compound = new NBTTagCompound();

		for (JsonElement jsonElement : jsonArray) {
			JsonObject jsonObject = jsonElement.getAsJsonObject();

			String[] split = getSplit(jsonObject);
			if (split.length != 2) {
				continue;
			}

			NBTBase tag = getNBTTag(jsonObject, split[0], split[1]);
			if (tag != null) {
				compound.setTag(split[1], tag);
			}
		}

		return compound;
	}

	private NBTTagList getList(JsonArray jsonArray) {
		NBTTagList tagList = new NBTTagList();

		for (JsonElement jsonElement : jsonArray) {
			JsonObject jsonObject = jsonElement.getAsJsonObject();

			String[] split = getSplit(jsonObject);
			if (split.length != 2) {
				continue;
			}

			NBTBase tag = getNBTTag(jsonObject, split[0], split[1]);
			if (tag != null) {
				tagList.appendTag(tag);
			}
		}

		return tagList;
	}

	private String[] getSplit(JsonObject jsonObject) {
		return jsonObject.get(MEMBER_NAME).getAsString().split(DELIMITER, 2);
	}

	private NBTTagByteArray getByteArray(JsonArray jsonArray) {
		byte[] byteArray = new byte[jsonArray.size()];

		for (int i = 0; i < jsonArray.size(); i++) {
			byteArray[i] = jsonArray.get(i).getAsByte();
		}

		return new NBTTagByteArray(byteArray);
	}

	private NBTTagIntArray getIntArray(JsonArray jsonArray) {
		int[] intArray = new int[jsonArray.size()];

		for (int i = 0; i < jsonArray.size(); i++) {
			intArray[i] = jsonArray.get(i).getAsInt();
		}

		return new NBTTagIntArray(intArray);
	}
}
