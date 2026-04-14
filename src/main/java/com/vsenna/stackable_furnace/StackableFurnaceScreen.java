package com.vsenna.stackable_furnace;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

public class StackableFurnaceScreen extends HandledScreen<StackableFurnaceScreenHandler> {

    public StackableFurnaceScreen(StackableFurnaceScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundHeight = 202;
        this.backgroundWidth = 176;
        this.playerInventoryTitleY = 108;
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;

        // Fundo e borda cinza limpo
        context.fill(x, y, x + this.backgroundWidth, y + this.backgroundHeight, 0xFFC6C6C6);
        context.drawBorder(x, y, this.backgroundWidth, this.backgroundHeight, 0xFF000000);

        // Texto e Desenho do Slot de Upgrade
        String tierUpText = "Tier Up";
        context.drawText(this.textRenderer, tierUpText, x + 170 - this.textRenderer.getWidth(tierUpText), y + 6, 0x404040, false);
        drawSlot(context, x + 151, y + 17, true);

        // Texto e Desenho do Slot de Turbo
        String turboText = "Turbo";
        context.drawText(this.textRenderer, turboText, x + 170 - this.textRenderer.getWidth(turboText), y + 42, 0x404040, false);
        drawSlot(context, x + 151, y + 53, true);

        // --- Barra de Duração do Turbo ---
        int turboTime = this.handler.getPropertyDelegate().get(17);
        int turboTimeTotal = this.handler.getPropertyDelegate().get(19);

        if (turboTime > 0 && turboTimeTotal > 0) {
            int barWidth = 4;
            int barHeightMax = 18;
            int currentBarHeight = (int) (((float) turboTime / turboTimeTotal) * barHeightMax);

            // Fundo da barra (escuro)
            context.fill(x + 171, y + 54, x + 171 + barWidth, y + 54 + barHeightMax, 0xFF333333);
            // Barra colorida (preenche de baixo para cima)
            context.fill(x + 171, y + 54 + (barHeightMax - currentBarHeight), x + 171 + barWidth, y + 54 + barHeightMax, 0xFF00FFFF);
            // Borda da barra
            context.drawBorder(x + 171, y + 54, barWidth, barHeightMax, 0xFF000000);
        }

        // Desenha Fornalhas
        int currentTier = this.handler.getTier();
        for (int tier = 0; tier < 4; tier++) {
            int yBase = y + 18 + (tier * 24);
            boolean isActive = tier < currentTier;

            // Slots: Se inativo, desenhamos com um fundo mais escuro (0xFF555555) pra ilustrar bloqueio
            drawSlot(context, x + 39, yBase - 1, isActive);
            drawSlot(context, x + 59, yBase - 1, isActive);

            // Output é um pouco maior
            int outColor = isActive ? 0xFF8B8B8B : 0xFF555555;
            context.fill(x + 112, yBase - 5, x + 112 + 26, yBase - 5 + 26, outColor);
            context.drawBorder(x + 112, yBase - 5, 26, 26, 0xFF000000);

            // Progresso de Fogo e Seta (Apenas nos ativos)
            if (isActive) {
                int burnTime = this.handler.getPropertyDelegate().get(tier * 4);
                int fuelTime = this.handler.getPropertyDelegate().get(tier * 4 + 1);
                int cookTime = this.handler.getPropertyDelegate().get(tier * 4 + 2);
                int cookTimeTotal = this.handler.getPropertyDelegate().get(tier * 4 + 3);

                if (burnTime > 0 && fuelTime > 0) {
                    int fireHeight = (int) (((float) burnTime / fuelTime) * 14);
                    context.fill(x + 22, yBase + 14 - fireHeight, x + 26, yBase + 14, 0xFFFF5500);
                }

                if (cookTimeTotal > 0 && cookTime > 0) {
                    int progressWidth = (int) (((float) cookTime / cookTimeTotal) * 24);
                    context.fill(x + 82, yBase + 4, x + 82 + progressWidth, yBase + 10, 0xFF55FF55);
                    context.drawBorder(x + 82, yBase + 4, 24, 6, 0xFF000000);
                }
            }
        }

        // Inventário Player
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                drawSlot(context, x + 7 + col * 18, y + 119 + row * 18, true);
            }
        }

        // Hotbar
        for (int col = 0; col < 9; col++) {
            drawSlot(context, x + 7 + col * 18, y + 177, true);
        }
    }

    private void drawSlot(DrawContext context, int x, int y, boolean isActive) {
        context.fill(x, y, x + 18, y + 18, isActive ? 0xFF8B8B8B : 0xFF555555);
        context.drawBorder(x, y, 18, 18, 0xFF000000);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }
}