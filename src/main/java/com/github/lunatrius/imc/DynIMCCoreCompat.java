package com.github.lunatrius.imc;

import com.github.lunatrius.core.version.VersionChecker;
import com.github.lunatrius.imc.lib.Reference;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = Reference.MODID + "|CoreCompat", name = Reference.NAME + "|CoreCompat", dependencies = "after:" + Reference.MODID)
public class DynIMCCoreCompat {
	@Optional.Method(modid = "LunatriusCore")
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		VersionChecker.registerMod(DynIMC.instance.modMetadata);
	}
}
