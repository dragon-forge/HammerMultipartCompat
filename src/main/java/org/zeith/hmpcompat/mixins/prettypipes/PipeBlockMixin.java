package org.zeith.hmpcompat.mixins.prettypipes;

import de.ellpeck.prettypipes.pipe.PipeBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.zeith.hmpcompat.compat.prettypipes.ents.PartEntityPrettyPipe;
import org.zeith.multipart.api.WorldPartComponents;
import org.zeith.multipart.init.PartPlacementsHM;

@Mixin(PipeBlock.class)
public class PipeBlockMixin
{
	@Redirect(
			method = "onStateChanged",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"
			)
	)
	private static BlockState HMPCompat_onStateChanged(Level level, BlockPos pos)
	{
		var pc = WorldPartComponents.getContainer(level, pos);
		if(pc != null && pc.getPartAt(PartPlacementsHM.CENTER) instanceof PartEntityPrettyPipe pepp)
			return pepp.getState();
		return level.getBlockState(pos);
	}
}