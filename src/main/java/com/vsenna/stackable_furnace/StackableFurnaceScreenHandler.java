package com.vsenna.stackable_furnace;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.world.World;
import net.minecraft.recipe.RecipeType;

public class StackableFurnaceScreenHandler extends ScreenHandler {
    private final Inventory inventory;
    private final PropertyDelegate propertyDelegate;
    private final World world;

    public StackableFurnaceScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(14), new ArrayPropertyDelegate(20)); 
    }

    public StackableFurnaceScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory, PropertyDelegate propertyDelegate) {
        super(StackableFurnace.STACKABLE_FURNACE_SCREEN_HANDLER, syncId);
        this.inventory = inventory;
        this.propertyDelegate = propertyDelegate;
        this.world = playerInventory.player.getWorld();

        this.addProperties(propertyDelegate);

        // --- SLOT DE UPGRADE (Slot 12) ---
        this.addSlot(new Slot(inventory, 12, 152, 18));

        // Slots da Fornalha Multi-Tier (4 fornalhas x 3 slots = 12 slots [0 ao 11])
        for (int tier = 0; tier < 4; tier++) {
            int yBase = 18 + (tier * 24);
            int slotOffset = tier * 3;

            this.addSlot(new FurnaceSlot(inventory, slotOffset, 40, yBase, tier));       // Input
            this.addSlot(new FurnaceSlot(inventory, slotOffset + 1, 60, yBase, tier));   // Fuel
            this.addSlot(new FurnaceSlot(inventory, slotOffset + 2, 116, yBase, tier) {  // Output
                @Override
                public boolean canInsert(ItemStack stack) { return false; } 
            });
        }

        // --- SLOT DE TURBO (Slot 13) ---
        this.addSlot(new Slot(inventory, 13, 152, 54));

        // Inventário Principal do Jogador (Slots 14 ao 40)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 120 + row * 18));
            }
        }

        // Hotbar do Jogador (Slots 41 ao 49)
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 178));
        }
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slotId) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotId);

        if (slot != null && slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();
            int currentTier = this.getTier();

            // CLIQUE DENTRO DA FORNALHA (Slots 0 ao 13) -> Vai para o Inventário do Jogador
            if (slotId < 14) {
                if (!this.insertItem(originalStack, 14, 50, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickTransfer(originalStack, newStack);
            }
            // CLIQUE NO INVENTÁRIO DO JOGADOR -> Vai para a Fornalha
            else {
                // 1. Tenta colocar Upgrade (Fornalha no slot 12)
                if (originalStack.isOf(StackableFurnace.STACKABLE_FURNACE_BLOCK.asItem()) && currentTier < 4) {
                    if (!this.insertItem(originalStack, 0, 1, false)) { 
                        return ItemStack.EMPTY;
                    }
                }
                // 2. Tenta inserir Combustível
                else if (isFuel(originalStack)) {
                    boolean inserted = false;
                    // Tenta slots de combustível das fornalhas
                    for (int t = 0; t < currentTier; t++) {
                        int visualFuelSlotIndex = 1 + (t * 3) + 1;
                        if (this.insertItem(originalStack, visualFuelSlotIndex, visualFuelSlotIndex + 1, false)) {
                            inserted = true;
                            break;
                        }
                    }
                    // Se não inseriu, tenta o Slot de Turbo (índice 13)
                    if (!inserted) {
                        if (this.insertItem(originalStack, 13, 14, false)) {
                            inserted = true;
                        }
                    }
                    if (!inserted) return ItemStack.EMPTY;
                }
                // 3. Tenta inserir Minérios / Comida (Smeltables)
                else if (isSmeltable(originalStack)) {
                    boolean inserted = false;
                    for (int t = 0; t < currentTier; t++) {
                        int visualInputSlotIndex = 1 + (t * 3);
                        if (this.insertItem(originalStack, visualInputSlotIndex, visualInputSlotIndex + 1, false)) {
                            inserted = true;
                            break;
                        }
                    }
                    if (!inserted) return ItemStack.EMPTY;
                }
                // 4. Move do Inventário Principal para Hotbar
                else if (slotId >= 14 && slotId < 41) {
                    if (!this.insertItem(originalStack, 41, 50, false)) return ItemStack.EMPTY;
                }
                // 5. Move da Hotbar para o Inventário Principal
                else if (slotId >= 41 && slotId < 50) {
                    if (!this.insertItem(originalStack, 14, 41, false)) return ItemStack.EMPTY;
                }
            }

            if (originalStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }

            if (originalStack.getCount() == newStack.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTakeItem(player, originalStack);
        }
        return newStack;
    }

    private boolean isFuel(ItemStack stack) {
        // Usa o mapeamento oficial de fornalhas do Minecraft
        return net.minecraft.block.entity.AbstractFurnaceBlockEntity.createFuelTimeMap().containsKey(stack.getItem());
    }

    private boolean isSmeltable(ItemStack stack) {
        if (this.world == null) return false;
        return this.world.getRecipeManager().getFirstMatch(RecipeType.SMELTING, new SimpleInventory(stack), this.world).isPresent();
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    public PropertyDelegate getPropertyDelegate() {
        return this.propertyDelegate;
    }

    public int getTier() {
        return this.propertyDelegate.get(16);
    }

    // --- CLASSE INTERNA: Protege slots que ainda não foram destrancados ---
    private class FurnaceSlot extends Slot {
        private final int tier;

        public FurnaceSlot(Inventory inventory, int index, int x, int y, int tier) {
            super(inventory, index, x, y);
            this.tier = tier;
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return this.tier < getTier(); // Só deixa colocar item se a fornalha estiver num tier ativo
        }

        @Override
        public boolean canTakeItems(PlayerEntity playerEntity) {
            return this.tier < getTier();
        }
    }
}