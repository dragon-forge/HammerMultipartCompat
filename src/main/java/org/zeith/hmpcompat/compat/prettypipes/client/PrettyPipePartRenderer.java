package org.zeith.hmpcompat.compat.prettypipes.client;

import com.mojang.blaze3d.vertex.PoseStack;
import de.ellpeck.prettypipes.pipe.PipeBlockEntity;
import de.ellpeck.prettypipes.pipe.PipeRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import org.zeith.hmpcompat.compat.prettypipes.ents.PartEntityPrettyPipe;
import org.zeith.multipart.client.rendering.IPartRenderer;

public class PrettyPipePartRenderer
		implements IPartRenderer
{
	public static final PipeRenderer PIPE_RENDERER = new PipeRenderer(null);
	protected final PartEntityPrettyPipe part;
	
	public PrettyPipePartRenderer(PartEntityPrettyPipe part)
	{
		this.part = part;
	}
	
	@Override
	public void renderPart(float partial, PoseStack matrix, MultiBufferSource buf, int lighting, int overlay)
	{
		PipeBlockEntity be = part.getWrapped();
		
		if(!be.hasLevel())
		{
			var lvl = part.container().level();
			if(lvl == null)
				return;
			be.setLevel(lvl);
		}
		PIPE_RENDERER.render(be, partial, matrix, buf, lighting, overlay);
	}
}