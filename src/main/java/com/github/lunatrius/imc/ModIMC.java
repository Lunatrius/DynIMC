package com.github.lunatrius.imc;

import com.google.gson.annotations.SerializedName;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Map;

public class ModIMC {
	public String modid;

	@SerializedName("string")
	public Map<String, String[]> stringStringMap;

	@SerializedName("itemstack")
	public Map<String, ItemStack[]> stringItemStackMap;

	@SerializedName("nbt")
	public Map<String, NBTTagCompound[]> stringNBTTagCompoundMap;
}
