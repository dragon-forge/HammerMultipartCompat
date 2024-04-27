package org.zeith.hmpcompat.mixins.prettypipes;

import de.ellpeck.prettypipes.pipe.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.zeith.hmpcompat.compat.prettypipes.ents.PartEntityPrettyPipe;
import org.zeith.multipart.api.PartContainer;
import org.zeith.multipart.api.WorldPartComponents;
import org.zeith.multipart.init.PartPlacementsHM;

@Mixin(value = PipeBlockEntity.class, remap = false)
public abstract class PipeBlockEntityMixin
		extends BlockEntity
{
	public PipeBlockEntityMixin(BlockEntityType<?> p_155228_, BlockPos p_155229_, BlockState p_155230_)
	{
		super(p_155228_, p_155229_, p_155230_);
	}
	
	@Inject(
			method = "getConnectionType",
			at = @At("HEAD"),
			cancellable = true
	)
	private void HMPCompat_getConnectionType(BlockPos pipePos, Direction direction, CallbackInfoReturnable<ConnectionType> cir)
	{
		PartContainer pc = WorldPartComponents.getContainer(getLevel(), pipePos.relative(direction));
		if(pc != null && pc.getPartAt(PartPlacementsHM.CENTER) instanceof PartEntityPrettyPipe pepp)
		{
			var state = pepp.getState();
			cir.setReturnValue(
					state.getValue(PipeBlock.DIRECTIONS.get(direction.getOpposite())) == ConnectionType.BLOCKED
					? ConnectionType.BLOCKED
					: ConnectionType.CONNECTED
			);
		}
	}
}