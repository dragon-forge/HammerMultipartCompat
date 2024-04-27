package org.zeith.hmpcompat.compat.prettypipes.defs;

import de.ellpeck.prettypipes.Registry;
import de.ellpeck.prettypipes.pipe.IPipeItem;
import de.ellpeck.prettypipes.pipe.PipeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.zeith.hmpcompat.compat.prettypipes.client.PrettyPipePartRenderer;
import org.zeith.hmpcompat.compat.prettypipes.ents.PartEntityPrettyPipe;
import org.zeith.multipart.api.*;
import org.zeith.multipart.api.placement.PartPlacement;
import org.zeith.multipart.api.placement.PlacedPartConfiguration;
import org.zeith.multipart.client.IClientPartDefinitionExtensions;
import org.zeith.multipart.client.rendering.IPartRenderer;
import org.zeith.multipart.init.PartPlacementsHM;

import java.util.Optional;
import java.util.function.Consumer;

public class PartDefinitionPrettyPipe
		extends PartDefinition
{
	public PartDefinitionPrettyPipe()
	{
		model.addParticleIcon(new ResourceLocation("prettypipes:block/pipe"));
	}
	
	@Override
	public PartEntity createEntity(PartContainer container, PartPlacement placement)
	{
		return new PartEntityPrettyPipe(this, container, placement);
	}
	
	@Override
	public Optional<PlacedPartConfiguration> convertBlockToPart(Level level, BlockPos pos, BlockState state)
	{
		if(!state.is(Registry.pipeBlock) || !(level.getBlockEntity(pos) instanceof PipeBlockEntity pipe))
			return Optional.empty();
		
		var nbt = pipe.serializeNBT();
		
		for(IPipeItem item : pipe.getItems())
			item.drop(level, item.getContent());
		
		return Optional.of(new PlacedPartConfiguration(
				this,
				(container, placement) ->
				{
					var e = new PartEntityPrettyPipe(this, container, placement, nbt);
					e.state = state;
					return e;
				},
				PartPlacementsHM.CENTER
		));
	}
	
	@Override
	public void initializeClient(Consumer<IClientPartDefinitionExtensions> consumer)
	{
		consumer.accept(new IClientPartDefinitionExtensions()
		{
			@Override
			public IPartRenderer createRenderer(PartEntity part)
			{
				if(part instanceof PartEntityPrettyPipe pepp)
					return new PrettyPipePartRenderer(pepp);
				return null;
			}
		});
	}
}