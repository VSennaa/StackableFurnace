package com.vsenna.stackable_furnace;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

public class StackableFurnaceClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Aplica o filtro vermelho no bloco colocado no chão
        ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) -> {
            return 0xFF5555; // Vermelho intenso
        }, StackableFurnace.STACKABLE_FURNACE_BLOCK);

        // Aplica o filtro vermelho no item na mão/inventário
        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> {
            return 0xFF5555;
        }, StackableFurnace.STACKABLE_FURNACE_BLOCK);
        HandledScreens.register(StackableFurnace.STACKABLE_FURNACE_SCREEN_HANDLER, StackableFurnaceScreen::new);
    }
}