package org.zeith.hmpcompat;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import org.zeith.hammerlib.compat.base.CompatList;
import org.zeith.hammerlib.core.adapter.LanguageAdapter;
import org.zeith.hmpcompat.compat.HMPModule;
import org.zeith.multipart.HammerMultipart;
import org.zeith.multipart.api.PartDefinition;

@Mod(HMPCompat.MOD_ID)
public class HMPCompat
{
	public static final ResourceKey<Registry<PartDefinition>> MULTIPART_DEFINITONS = hmpreg("multipart/definitions");
	
	public static final String MOD_ID = "hmpcompat";
	
	public static final CompatList<HMPModule> COMPATS = CompatList.gather(HMPModule.class);
	
	public HMPCompat()
	{
		LanguageAdapter.registerMod(MOD_ID);
		COMPATS.getActive().forEach(HMPModule::setup);
	}
	
	public static ResourceLocation id(String path)
	{
		return new ResourceLocation(MOD_ID, path);
	}
	
	private static <T> ResourceKey<Registry<T>> hmpreg(String name)
	{
		return ResourceKey.createRegistryKey(HammerMultipart.id(name));
	}
}