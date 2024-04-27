package org.zeith.hmpcompat.mixins.prettypipes;

import de.ellpeck.prettypipes.Registry;
import de.ellpeck.prettypipes.network.NetworkEdge;
import de.ellpeck.prettypipes.network.PipeNetwork;
import de.ellpeck.prettypipes.pipe.PipeBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.zeith.hmpcompat.compat.prettypipes.ents.PartEntityPrettyPipe;
import org.zeith.multipart.api.WorldPartComponents;
import org.zeith.multipart.init.PartPlacementsHM;

@Mixin(PipeNetwork.class)
public class PipeNetworkMixin
{
	@Inject(
			method = "createEdge",
			at = @At("HEAD"),
			cancellable = true,
			remap = false
	)
	private void HMPCompat_createEdgeFix(BlockPos pos, BlockState state, Direction dir, boolean ignoreCurrBlocked, CallbackInfoReturnable<NetworkEdge> cir)
	{
		if(!state.hasProperty(PipeBlock.DIRECTIONS.get(dir)))
			cir.setReturnValue(null);
	}
	
	@Redirect(
			method = "createEdge",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"
			)
	)
	private BlockState HMPCompat_createEdge(Level level, BlockPos pos)
	{
		var pc = WorldPartComponents.getContainer(level, pos);
		if(pc != null)
		{
			if(pc.getPartAt(PartPlacementsHM.CENTER) instanceof PartEntityPrettyPipe pepp)
				return pepp.getState();
			else
				return Registry.pipeBlock.defaultBlockState();
		}
		return level.getBlockState(pos);
	}
	
	@Redirect(
			method = "getNodeFromPipe",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"
			)
	)
	private BlockState HMPCompat_getNodeFromPipe(Level level, BlockPos pos)
	{
		var pc = WorldPartComponents.getContainer(level, pos);
		if(pc != null && pc.getPartAt(PartPlacementsHM.CENTER) instanceof PartEntityPrettyPipe pepp)
			return pepp.getState();
		return level.getBlockState(pos);
	}
}