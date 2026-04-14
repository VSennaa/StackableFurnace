package com.vsenna.stackable_furnace;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.stream.IntStream;

public class StackableFurnaceBlockEntity extends BlockEntity implements ImplementedInventory, NamedScreenHandlerFactory, SidedInventory {

    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(14, ItemStack.EMPTY);

    public final int[] burnTime = new int[4];
    public final int[] fuelTime = new int[4];
    public final int[] cookTime = new int[4];
    public final int[] cookTimeTotal = new int[4];

    public int turboTime = 0;
    public int turboTimeTotal = 0;
    public float currentTurboMultiplier = 1.0f;

    public StackableFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(StackableFurnace.STACKABLE_FURNACE_BLOCK_ENTITY, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, StackableFurnaceBlockEntity entity) {
        int currentTier = state.get(StackableFurnaceBlock.TIER);
        boolean changed = false;

        // SISTEMA DE TURBO (Slot 13)
        ItemStack turboStack = entity.getItems().get(13);
        if (entity.turboTime == 0 && !turboStack.isEmpty()) {
            int fuelValue = net.minecraft.block.entity.AbstractFurnaceBlockEntity.createFuelTimeMap().getOrDefault(turboStack.getItem(), 0);
            if (fuelValue > 0) {
                entity.currentTurboMultiplier = (float) Math.max(1.0, Math.sqrt((double) fuelValue / 1600.0));
                entity.turboTime = fuelValue / 2;
                entity.turboTimeTotal = entity.turboTime;
                net.minecraft.item.Item turboItem = turboStack.getItem();
                if (turboStack.getCount() == 1 && turboItem.hasRecipeRemainder()) {
                    entity.getItems().set(13, new ItemStack(turboItem.getRecipeRemainder()));
                } else {
                    turboStack.decrement(1);
                }
                changed = true;
            }
        }

        if (entity.turboTime > 0) {
            entity.turboTime--;
            changed = true;
        } else {
            entity.currentTurboMultiplier = 1.0f;
        }

        // SISTEMA DE UPGRADE: Lê o slot 12 e sobe de nível
        ItemStack upgradeStack = entity.getItems().get(12);
        int custo = currentTier; // Tier 1->2 custa 1, Tier 2->3 custa 2, Tier 3->4 custa 3
        if (currentTier < 4 && upgradeStack.isOf(StackableFurnace.STACKABLE_FURNACE_BLOCK.asItem()) && upgradeStack.getCount() >= custo) {
            upgradeStack.decrement(custo);
            currentTier++;
            world.setBlockState(pos, state.with(StackableFurnaceBlock.TIER, currentTier));
            changed = true;
        }

        for (int i = 0; i < currentTier; i++) {
            int inputSlot = i * 3;
            int fuelSlot = i * 3 + 1;
            int outputSlot = i * 3 + 2;

            ItemStack inputStack = entity.getItems().get(inputSlot);
            ItemStack fuelStack = entity.getItems().get(fuelSlot);
            ItemStack outputStack = entity.getItems().get(outputSlot);

            if (entity.burnTime[i] > 0) {
                entity.burnTime[i]--;
            }

            SimpleInventory recipeInput = new SimpleInventory(inputStack);
            var recipeOptional = world.getRecipeManager().getFirstMatch(RecipeType.SMELTING, recipeInput, world);

            if (recipeOptional.isPresent()) {
                AbstractCookingRecipe recipe = recipeOptional.get().value();
                entity.cookTimeTotal[i] = recipe.getCookingTime();

                if (entity.burnTime[i] == 0 && !fuelStack.isEmpty()) {
                    int baseBurnTime = net.minecraft.block.entity.AbstractFurnaceBlockEntity.createFuelTimeMap().getOrDefault(fuelStack.getItem(), 0);

                    if (baseBurnTime > 0) {
                        net.minecraft.item.Item fuelItem = fuelStack.getItem();
                        if (fuelStack.getCount() == 1 && fuelItem.hasRecipeRemainder()) {
                            entity.getItems().set(fuelSlot, new ItemStack(fuelItem.getRecipeRemainder()));
                        } else {
                            fuelStack.decrement(1);
                        }
                        int effectiveBurnTime = (int) (baseBurnTime * 0.85); // 15% penalty
                        entity.burnTime[i] = effectiveBurnTime;
                        entity.fuelTime[i] = effectiveBurnTime;
                        changed = true;
                    }
                }

                if (entity.burnTime[i] > 0) {
                    entity.cookTime[i] += (int) Math.max(1, entity.currentTurboMultiplier);
                    if (entity.cookTime[i] >= entity.cookTimeTotal[i]) {
                        ItemStack result = recipe.getResult(world.getRegistryManager());

                        if (outputStack.isEmpty()) {
                            entity.getItems().set(outputSlot, result.copy());
                            inputStack.decrement(1);
                            entity.cookTime[i] = 0;
                            changed = true;
                        } else if (ItemStack.areItemsEqual(outputStack, result) && outputStack.getCount() + result.getCount() <= outputStack.getMaxCount()) {
                            outputStack.increment(result.getCount());
                            inputStack.decrement(1);
                            entity.cookTime[i] = 0;
                            changed = true;
                        }
                    }
                } else {
                    if (entity.cookTime[i] > 0) entity.cookTime[i]--;
                }
            } else {
                entity.cookTime[i] = 0;
            }
        }

        if (changed) {
            entity.markDirty();
        }
    }

    protected final net.minecraft.screen.PropertyDelegate propertyDelegate = new net.minecraft.screen.PropertyDelegate() {
        @Override
        public int get(int index) {
        if (index == 16) {
            return world != null ? world.getBlockState(pos).get(StackableFurnaceBlock.TIER) : 1;
        }
        if (index == 17) return turboTime;
        if (index == 18) return (int) (currentTurboMultiplier * 100);
        if (index == 19) return turboTimeTotal; // Novo: Turbo Time Total
        int tier = index / 4;
        return switch (index % 4) {
            case 0 -> burnTime[tier];
            case 1 -> fuelTime[tier];
            case 2 -> cookTime[tier];
            case 3 -> cookTimeTotal[tier];
            default -> 0;
        };
    }

    @Override
    public void set(int index, int value) {
        if (index == 16) return;
        if (index == 17) { turboTime = value; return; }
        if (index == 18) { currentTurboMultiplier = value / 100.0f; return; }
        if (index == 19) { /* turboTimeTotal não deve ser setado diretamente, apenas lido */ return; }
        int tier = index / 4;
        switch (index % 4) {
            case 0 -> burnTime[tier] = value;
            case 1 -> fuelTime[tier] = value;
            case 2 -> cookTime[tier] = value;
            case 3 -> cookTimeTotal[tier] = value;
        }
    }

    @Override
    public int size() { return 20; } // Agora são 20 propriedades
    };

    @Override
    public Text getDisplayName() {
        return Text.translatable("block.stackable_furnace.stackable_furnace");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new StackableFurnaceScreenHandler(syncId, playerInventory, this, this.propertyDelegate);
    }

    @Override
    public DefaultedList<ItemStack> getItems() { return inventory; }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        Inventories.writeNbt(nbt, inventory, registryLookup);
        nbt.putIntArray("BurnTimes", burnTime);
        nbt.putIntArray("FuelTimes", fuelTime);
        nbt.putIntArray("CookTimes", cookTime);
        nbt.putIntArray("CookTimeTotals", cookTimeTotal);
        nbt.putInt("TurboTime", turboTime);
        nbt.putFloat("TurboMultiplier", currentTurboMultiplier);
        nbt.putInt("TurboTimeTotal", turboTimeTotal);
    }

    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        Inventories.readNbt(nbt, inventory, registryLookup);
        if (nbt.contains("BurnTimes")) System.arraycopy(nbt.getIntArray("BurnTimes"), 0, burnTime, 0, 4);
        if (nbt.contains("FuelTimes")) System.arraycopy(nbt.getIntArray("FuelTimes"), 0, fuelTime, 0, 4);
        if (nbt.contains("CookTimes")) System.arraycopy(nbt.getIntArray("CookTimes"), 0, cookTime, 0, 4);
        if (nbt.contains("CookTimeTotals")) System.arraycopy(nbt.getIntArray("CookTimeTotals"), 0, cookTimeTotal, 0, 4);
        turboTime = nbt.getInt("TurboTime");
        currentTurboMultiplier = nbt.getFloat("TurboMultiplier");
        turboTimeTotal = nbt.getInt("TurboTimeTotal");
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        int currentTier = world != null ? world.getBlockState(pos).get(StackableFurnaceBlock.TIER) : 1;
        if (side == Direction.UP) {
            return IntStream.range(0, currentTier).map(i -> i * 3).toArray();
        } else if (side == Direction.DOWN) {
            return IntStream.range(0, currentTier).map(i -> i * 3 + 2).toArray();
        } else {
            return IntStream.range(0, currentTier).map(i -> i * 3 + 1).toArray();
        }
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        if (slot == 12) return false;
        int currentTier = world != null ? world.getBlockState(pos).get(StackableFurnaceBlock.TIER) : 1;
        if (slot / 3 >= currentTier) return false;

        if (dir == Direction.UP) {
            return slot % 3 == 0 && isSmeltable(stack);
        } else if (dir != Direction.DOWN && dir != null) {
            return slot % 3 == 1 && AbstractFurnaceBlockEntity.canUseAsFuel(stack);
        }
        return false;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        if (slot == 12) return false;
        if (dir == Direction.DOWN && slot % 3 == 2) return true;
        if (slot % 3 == 1 && stack.isOf(Items.BUCKET)) return true;
        return false;
    }

    private boolean isSmeltable(ItemStack stack) {
        return world != null && world.getRecipeManager()
                .getFirstMatch(RecipeType.SMELTING, new SimpleInventory(stack), world).isPresent();
    }
}
