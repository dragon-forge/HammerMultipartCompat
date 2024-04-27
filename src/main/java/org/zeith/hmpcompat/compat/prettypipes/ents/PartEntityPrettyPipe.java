package org.zeith.hmpcompat.compat.prettypipes.ents;

import de.ellpeck.prettypipes.Registry;
import de.ellpeck.prettypipes.network.PipeNetwork;
import de.ellpeck.prettypipes.pipe.*;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.zeith.hammerlib.api.io.NBTSerializable;
import org.zeith.hammerlib.util.java.tuples.Tuple2;
import org.zeith.hammerlib.util.java.tuples.Tuples;
import org.zeith.hmpcompat.mixins.prettypipes.PipeBlockAccessor;
import org.zeith.multipart.api.*;
import org.zeith.multipart.api.placement.PartPlacement;
import org.zeith.multipart.init.PartPlacementsHM;

import java.util.Iterator;
import java.util.Optional;
import java.util.function.Function;

@Getter
public class PartEntityPrettyPipe
		extends PartEntity
		implements IPipeConnectable
{
	protected PipeBlockEntity wrapped;
	
	@NBTSerializable
	public BlockState state;
	
	private final LazyOptional<PartEntityPrettyPipe> lazyThis = LazyOptional.of(() -> this);
	
	public PartEntityPrettyPipe(PartDefinition definition, PartContainer container, PartPlacement placement)
	{
		super(definition, container, placement);
		wrapped = new PipeBlockEntity(container.pos(), state = Registry.pipeBlock.defaultBlockState());
	}
	
	public PartEntityPrettyPipe(PartDefinition definition, PartContainer container, PartPlacement placement, CompoundTag entity)
	{
		super(definition, container, placement);
		wrapped = new PipeBlockEntity(container.pos(), state = Registry.pipeBlock.defaultBlockState());
		wrapped.deserializeNBT(entity);
	}
	
	@Override
	protected VoxelShape updateShape()
	{
		PartContainer ctr = container();
		var level = ctr.level();
		var pos = ctr.pos();
		return state.getShape(level, pos);
	}
	
	@Override
	protected VoxelShape updateCollisionShape()
	{
		PartContainer ctr = container();
		var level = ctr.level();
		var pos = ctr.pos();
		return state.getCollisionShape(level, pos);
	}
	
	@Override
	public Optional<Tuple2<BlockState, Function<BlockPos, BlockEntity>>> disassemblePart()
	{
		var worldIn = container().level();
		if(!wrapped.hasLevel())
			wrapped.setLevel(worldIn);
		if(wrapped.hasLevel())
			for(IPipeItem item : wrapped.getItems())
				item.drop(worldIn, item.getContent());
		
		PipeNetwork net = PipeNetwork.get(worldIn);
		net.removeNode(container().pos());
		
		var state = this.state;
		var nbt = wrapped.serializeNBT();
		return Optional.of(Tuples.immutable(state, pos ->
		{
			PipeBlockEntity pbe = new PipeBlockEntity(pos, state);
			pbe.deserializeNBT(nbt);
			net.addNode(pos, state);
			return pbe;
		}));
	}
	
	@Override
	public void onChunkUnloaded()
	{
		wrapped.onChunkUnloaded();
	}
	
	@Override
	public void onRemove()
	{
		wrapped.setRemoved();
	}
	
	@Override
	public void onRemovedBy(Player player, boolean willHarvest)
	{
		var level = container().level();
		var pos = container().pos();
		PipeBlock.dropItems(level, pos, player);
		super.onRemovedBy(player, willHarvest);
	}
	
	@Override
	public void neighborChanged(@Nullable Direction from, BlockPos neigborPos, BlockState neigborState, boolean waterlogged)
	{
		super.neighborChanged(from, neigborPos, neigborState, waterlogged);
		
		PartContainer ctr = container();
		var level = ctr.level();
		var pos = ctr.pos();
		
		if(!wrapped.hasLevel())
			wrapped.setLevel(level);
		
		PartContainer c = WorldPartComponents.getContainer(level, pos);
		if(c != ctr || !(level instanceof ServerLevel))
		{
			return;
		}
		
		var state = this.state;
		BlockState newState = ((PipeBlockAccessor) Registry.pipeBlock).callCreateState(level, pos, state);
		if(newState != state)
		{
			this.state = newState;
			wrapped.setBlockState(newState);
			PipeBlock.onStateChanged(level, pos, newState);
			ctr.causeBlockUpdate = true;
			syncDirty = true;
		}
		
		PipeNetwork pn = PipeNetwork.get(level);
		pn.removeNode(pos);
		pn.addNode(pos, newState);
		pn.onPipeChanged(pos, newState);
	}
	
	@Override
	protected void tickShared()
	{
		PartContainer ctr = container();
		var level = ctr.level();
		var pos = ctr.pos();
		
		if(level instanceof ServerLevel && level.getGameTime() % 5L == 0L)
		{
			PipeNetwork pn = PipeNetwork.get(level);
			if(!pn.isNode(pos))
			{
				pn.addNode(pos, state);
				pn.onPipeChanged(pos, state);
			}
		}
		
		wrapped.setLevel(level);
		wrapped.setBlockState(state);
		PipeBlockEntity.tick(level, pos, state, wrapped);
	}
	
	@Nullable
	@Override
	public BlockState getRenderState()
	{
		return state;
	}
	
	@Override
	public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side)
	{
		return cap == Registry.pipeConnectableCapability ? this.lazyThis.cast() : LazyOptional.empty();
	}
	
	@Override
	public ConnectionType getConnectionType(BlockPos pipePos, Direction direction)
	{
		Level level = container().level();
		if(level == null) return ConnectionType.DISCONNECTED;
		
		BlockState state;
		
		PartContainer pc = WorldPartComponents.getContainer(level, pipePos.relative(direction));
		if(pc != null && pc.getPartAt(PartPlacementsHM.CENTER) instanceof PartEntityPrettyPipe pepp)
			state = pepp.getState();
		else
			state = level.getBlockState(pipePos.relative(direction));
		
		return state.getValue(PipeBlock.DIRECTIONS.get(direction.getOpposite())) == ConnectionType.BLOCKED
			   ? ConnectionType.BLOCKED
			   : ConnectionType.CONNECTED;
	}
	
	@Override
	public CompoundTag serialize()
	{
		var tag = super.serialize();
		tag.put("TileData", wrapped.serializeNBT());
		return tag;
	}
	
	@Override
	public void deserialize(CompoundTag tag)
	{
		super.deserialize(tag);
		wrapped.deserializeNBT(tag.getCompound("TileData"));
	}
	
	@Override
	public InteractionResult use(Player player, InteractionHand hand, BlockHitResult hit, IndexedVoxelShape selection)
	{
		return state.use(container().level(), player, hand, hit);
	}
}