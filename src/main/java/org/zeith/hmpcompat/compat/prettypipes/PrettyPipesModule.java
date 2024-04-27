package org.zeith.hmpcompat.compat.prettypipes;

import de.ellpeck.prettypipes.Registry;
import de.ellpeck.prettypipes.pipe.PipeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegisterEvent;
import org.zeith.hammerlib.compat.base.BaseCompat;
import org.zeith.hmpcompat.HMPCompat;
import org.zeith.hmpcompat.compat.HMPModule;
import org.zeith.hmpcompat.compat.prettypipes.defs.PartDefinitionPrettyPipe;
import org.zeith.hmpcompat.compat.prettypipes.ents.PartEntityPrettyPipe;
import org.zeith.multipart.HammerMultipart;
import org.zeith.multipart.api.PartDefinition;
import org.zeith.multipart.api.item.IMultipartPlacerItem;
import org.zeith.multipart.api.placement.PlacedPartConfiguration;
import org.zeith.multipart.init.PartPlacementsHM;
import org.zeith.multipart.init.PartRegistries;

import java.util.Optional;

@BaseCompat.LoadCompat(
		compatType = HMPModule.class,
		modid = "prettypipes"
)
public class PrettyPipesModule
		extends HMPModule
{
	public static final PartDefinitionPrettyPipe PIPE_DEF = new PartDefinitionPrettyPipe();
	
	@Override
	public void setup()
	{
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		
		bus.addListener(this::registerContent);
		bus.addListener(this::gameLoaded);
	}
	
	private void registerContent(RegisterEvent e)
	{
		e.register(HMPCompat.MULTIPART_DEFINITONS, this::registerParts);
	}
	
	private void gameLoaded(FMLLoadCompleteEvent loadCompleteEvent)
	{
		PartRegistries.registerFallbackPartPlacer(Registry.pipeBlock.asItem(), (level, pos, player, stack, hit) ->
		{
			return Optional.of(new PlacedPartConfiguration(
					PIPE_DEF,
					(container, placement) -> new PartEntityPrettyPipe(PIPE_DEF, container, placement),
					PartPlacementsHM.CENTER
			));
		});
	}
	
	private void registerParts(RegisterEvent.RegisterHelper<PartDefinition> reg)
	{
		reg.register("prettypipes/pipe", PIPE_DEF);
	}
}