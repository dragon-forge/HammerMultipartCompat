package org.zeith.hmpcompat.mixins.prettypipes;

import de.ellpeck.prettypipes.Utility;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.zeith.hmpcompat.compat.prettypipes.ents.PartEntityPrettyPipe;
import org.zeith.multipart.api.*;
import org.zeith.multipart.init.PartPlacementsHM;

@Mixin(value = Utility.class, remap = false)
public class UtilityMixin
{
	@Inject(
			method = "getBlockEntity",
			at = @At("HEAD"),
			cancellable = true
	)
	private static <T extends BlockEntity> void HMPCompat_getBlockEntity(Class<T> type, BlockGetter world, BlockPos pos, CallbackInfoReturnable<T> cir)
	{
		PartContainer pc = WorldPartComponents.getContainer(world, pos);
		if(pc != null)
		{
			PartEntity center = pc.getPartAt(PartPlacementsHM.CENTER);
			if(center instanceof PartEntityPrettyPipe pepp)
			{
				var w = pepp.getWrapped();
				if(type.isInstance(w))
					cir.setReturnValue(type.cast(w));
			}
		}
	}
}