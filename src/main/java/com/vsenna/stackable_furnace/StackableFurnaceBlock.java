package com.vsenna.stackable_furnace;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.item.ItemPlacementContext;
import org.jetbrains.annotations.Nullable;

public class StackableFurnaceBlock extends BlockWithEntity {

    // A mágica da compactação: Propriedade que guarda o nível (1 a 4) no próprio bloco
    public static final IntProperty TIER = IntProperty.of("tier", 1, 4);
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
    public static final MapCodec<StackableFurnaceBlock> CODEC = createCodec(StackableFurnaceBlock::new);

    public StackableFurnaceBlock(Settings settings) {
        super(settings);
        // O bloco sempre nasce no Nível 1 e virado para o Norte
        this.setDefaultState(this.stateManager.getDefaultState().with(TIER, 1).with(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(TIER, FACING);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, net.minecraft.block.entity.BlockEntityType<T> type) {
        // Não roda a lógica de queima no cliente (lado visual), apenas no servidor (lado lógico)
        if (world.isClient) {
            return null;
        }

        // Faz a checagem manualmente sem depender do checkType do Fabric
        if (type == StackableFurnace.STACKABLE_FURNACE_BLOCK_ENTITY) {
            return (BlockEntityTicker<T>) (world1, pos1, state1, entity) -> {
                StackableFurnaceBlockEntity.tick(world1, pos1, state1, (StackableFurnaceBlockEntity) entity);
            };
        }

        return null;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new StackableFurnaceBlockEntity(pos, state);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    // Repassa o clique com item para o clique normal
    @Override
    protected net.minecraft.util.ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, net.minecraft.util.Hand hand, BlockHitResult hit) {
        return net.minecraft.util.ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    // Abre a interface independente se tem item na mão ou não
    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof StackableFurnaceBlockEntity) {
                player.openHandledScreen((StackableFurnaceBlockEntity) blockEntity);
            }
        }
        return ActionResult.SUCCESS;
    }




}