package org.zeith.hmpcompat.mixins.prettypipes;

import de.ellpeck.prettypipes.pipe.PipeBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(PipeBlock.class)
public interface PipeBlockAccessor
{
	@Invoker
	BlockState callCreateState(Level world, BlockPos pos, BlockState curr);
}
