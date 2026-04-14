package com.vsenna.stackable_furnace;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.resource.featuretoggle.FeatureFlags;
public class StackableFurnace implements ModInitializer {
    public static final String MOD_ID = "stackable_furnace";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // 1. O Bloco
    public static final Block STACKABLE_FURNACE_BLOCK = new StackableFurnaceBlock(AbstractBlock.Settings.create().strength(3.5f).requiresTool());

    // 2. A Block Entity (O motor da fornalha)
    public static final BlockEntityType<StackableFurnaceBlockEntity> STACKABLE_FURNACE_BLOCK_ENTITY = Registry.register(
            Registries.BLOCK_ENTITY_TYPE,
            new Identifier(MOD_ID, "stackable_furnace"),
            BlockEntityType.Builder.create(StackableFurnaceBlockEntity::new, STACKABLE_FURNACE_BLOCK).build()
    );

    // 3. A aba criativa
    public static final ItemGroup MOD_TAB = Registry.register(Registries.ITEM_GROUP,
            new Identifier(MOD_ID, "aba_fornalhas"),
            FabricItemGroup.builder()
                    .displayName(Text.translatable("itemGroup.stackable_furnace.aba_fornalhas"))
                    .icon(() -> new ItemStack(STACKABLE_FURNACE_BLOCK))
                    .entries((context, entries) -> {
                        entries.add(STACKABLE_FURNACE_BLOCK);
                    })
                    .build());
    // 4. A Interface Gráfica
    public static final ScreenHandlerType<StackableFurnaceScreenHandler> STACKABLE_FURNACE_SCREEN_HANDLER = Registry.register(
            Registries.SCREEN_HANDLER,
            new Identifier(MOD_ID, "stackable_furnace"),
            new ScreenHandlerType<>(StackableFurnaceScreenHandler::new, FeatureFlags.VANILLA_FEATURES)
    );
    @Override
    public void onInitialize() {
        LOGGER.info("Inicializando o Stackable Furnace!");

        Identifier idDoBloco = new Identifier(MOD_ID, "stackable_furnace");
        Registry.register(Registries.BLOCK, idDoBloco, STACKABLE_FURNACE_BLOCK);
        Registry.register(Registries.ITEM, idDoBloco, new BlockItem(STACKABLE_FURNACE_BLOCK, new Item.Settings()));


    }

}