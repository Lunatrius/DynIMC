package com.github.lunatrius.imc;

import com.github.lunatrius.core.version.VersionChecker;
import com.github.lunatrius.imc.deserializer.ItemStackDeserializer;
import com.github.lunatrius.imc.deserializer.NBTTagCompoundDeserializer;
import com.github.lunatrius.imc.reference.Reference;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mod(modid = Reference.MODID, name = Reference.NAME, version = Reference.VERSION)
public class DynIMC {
    public static final FilenameFilter IMC_JSON_FILTER = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
            return name.endsWith(".json");
        }
    };

    @Instance(Reference.MODID)
    public static DynIMC instance;

    private Gson gson;
    private File directory;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        Reference.logger = event.getModLog();
        this.directory = event.getModConfigurationDirectory();

        if (Loader.isModLoaded("LunatriusCore")) {
            registerVersionChecker(event.getModMetadata());
        }
        
        if (this.directory != null) {
            readConfiguration(this.directory);
        }
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
    	
    }

    private void readConfiguration(File configurationDirectory) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(ItemStack.class, new ItemStackDeserializer());
        gsonBuilder.registerTypeAdapter(NBTTagCompound.class, new NBTTagCompoundDeserializer());
        this.gson = gsonBuilder.create();

        File cfgDirectory = new File(configurationDirectory, Reference.MODID.toLowerCase());
        if (!cfgDirectory.exists()) {
            if (!cfgDirectory.mkdirs()) {
                Reference.logger.error("Could not create directory!");
            }
        }

        File[] files = cfgDirectory.listFiles(IMC_JSON_FILTER);
        for (File file : files) {
            List<ModIMC> modIMCs = readFile(file);

            if (modIMCs == null) {
                continue;
            }

            Reference.logger.trace("Reading IMC messages from " + file.getName());
            for (ModIMC modIMC : modIMCs) {
                if (modIMC != null) {
                    if (modIMC.stringItemStackMap != null) {
                        for (Map.Entry<String, ItemStack[]> entry : modIMC.stringItemStackMap.entrySet()) {
                            for (ItemStack itemStack : entry.getValue()) {
                                if (itemStack != null && itemStack.getItem() != null) {
                                    Reference.logger.trace("Sending IMC message " + entry.getKey() + ": " + itemStack);
                                    FMLInterModComms.sendMessage(modIMC.modid, entry.getKey(), itemStack);
                                } else {
                                    Reference.logger.trace("Tried to send null IMC message " + entry.getKey());
                                }
                            }
                        }
                    }

                    if (modIMC.stringStringMap != null) {
                        for (Map.Entry<String, String[]> entry : modIMC.stringStringMap.entrySet()) {
                            for (String string : entry.getValue()) {
                                if (string != null) {
                                    Reference.logger.trace("Sending IMC message " + entry.getKey() + ": " + string);
                                    FMLInterModComms.sendMessage(modIMC.modid, entry.getKey(), string);
                                } else {
                                    Reference.logger.trace("Tried to send null IMC message " + entry.getKey());
                                }
                            }
                        }
                    }

                    if (modIMC.stringNBTTagCompoundMap != null) {
                        for (Map.Entry<String, NBTTagCompound[]> entry : modIMC.stringNBTTagCompoundMap.entrySet()) {
                            for (NBTTagCompound nbt : entry.getValue()) {
                                if (nbt != null) {
                                    Reference.logger.trace("Sending IMC message " + entry.getKey() + ": " + String.valueOf(nbt));
                                    FMLInterModComms.sendMessage(modIMC.modid, entry.getKey(), nbt);
                                } else {
                                    Reference.logger.trace("Tried to send a null IMC message " + entry.getKey());
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void registerVersionChecker(ModMetadata modMetadata) {
    	DummyModContainer container = new DummyModContainer(modMetadata);
    	VersionChecker.registerMod(container, Reference.FORGE);
    }

    private List<ModIMC> readFile(File file) {
        BufferedReader buffer = null;
        try {
            if (file.getParentFile() != null) {
                if (!file.getParentFile().mkdirs()) {
                    Reference.logger.debug("Could not create directory!");
                }
            }

            if (!file.exists() && !file.createNewFile()) {
                return null;
            }

            if (file.canRead()) {
                FileReader fileInputStream = new FileReader(file);
                buffer = new BufferedReader(fileInputStream);

                String str = "";

                String line;
                while ((line = buffer.readLine()) != null) {
                    str += line + "\n";
                }

                return this.gson.fromJson(str, new TypeToken<ArrayList<ModIMC>>() {}.getType());
            }
        } catch (IOException e) {
            Reference.logger.error("IO failure!", e);
        } catch (JsonSyntaxException e) {
            Reference.logger.error(String.format("Malformed JSON in %s!", file.getName()), e);
        } finally {
            if (buffer != null) {
                try {
                    buffer.close();
                } catch (IOException e) {
                    Reference.logger.error("IO failure!", e);
                }
            }
        }

        return null;
    }
}
