package com.github.lunatrius.imc.deserializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import cpw.mods.fml.common.registry.GameData;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.lang.reflect.Type;

public class ItemStackDeserializer implements JsonDeserializer<ItemStack> {
    public static final String STACK_SIZE = "stackSize";
    public static final String ITEM_DAMAGE = "itemDamage";
    public static final String BLOCK = "block";
    public static final String ITEM = "item";

    public static final int DEFAULT_STACK_SIZE = 1;
    public static final int DEFAULT_ITEM_DAMAGE = 0;

    @Override
    public ItemStack deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (!json.isJsonObject()) {
            return null;
        }

        JsonObject jsonObject = json.getAsJsonObject();

        int stackSize = jsonObject.has(STACK_SIZE) ? jsonObject.get(STACK_SIZE).getAsInt() : DEFAULT_STACK_SIZE;
        int itemDamage = jsonObject.has(ITEM_DAMAGE) ? jsonObject.get(ITEM_DAMAGE).getAsInt() : DEFAULT_ITEM_DAMAGE;

        if (jsonObject.has(BLOCK)) {
            Block block = GameData.getBlockRegistry().getObject(jsonObject.get(BLOCK).getAsString());
            return new ItemStack(block, stackSize, itemDamage);
        } else if (jsonObject.has(ITEM)) {
            Item item = GameData.getItemRegistry().getObject(jsonObject.get(ITEM).getAsString());
            return new ItemStack(item, stackSize, itemDamage);
        }

        return null;
    }
}
