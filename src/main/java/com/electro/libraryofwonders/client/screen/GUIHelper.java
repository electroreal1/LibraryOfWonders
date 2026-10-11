package com.electro.libraryofwonders.client.screen;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

public class GUIHelper {

    public static class Drawing {

        /**
         * Draws an animated progress bar with an outer glow.
         * @param progress A float between 0.0F and 1.0F
         */
        public static void drawProgressBar(GuiGraphics graphics, int x, int y, int width, int height, float progress, int bgColor, int fillColor, int glowColor) {
            progress = Mth.clamp(progress, 0.0F, 1.0F);
            int fillWidth = (int) (width * progress);

            graphics.fill(x, y, x + width, y + height, bgColor);

            if (fillWidth > 0) {
                drawGlowingPanel(graphics, x, y, fillWidth, height, fillColor, glowColor, 4);
            }
        }

        /**
         * Draws a panel with a solid core and a fading, glowing border.
         */
        public static void drawGlowingPanel(GuiGraphics graphics, int x, int y, int width, int height, int coreColor, int glowColor, int radius) {
            if (radius > 0) {
                int glowAlpha = (glowColor >> 24) & 0xFF;
                int rgb = glowColor & 0xFFFFFF;
                float step = glowAlpha / (float) radius;

                for (int i = radius; i > 0; i--) {
                    int currentAlpha = (int) (glowAlpha - (i * step));
                    int layerColor = (currentAlpha << 24) | rgb;
                    graphics.fill(x - i, y - i, x + width + i, y + height + i, layerColor);
                }
            }
            graphics.fill(x, y, x + width, y + height, coreColor);
        }

        /**
         * Draws a vertical gradient box (good for scroll bars or deep backgrounds).
         */
        public static void drawGradientBox(GuiGraphics graphics, int x, int y, int width, int height, int topColor, int bottomColor) {
            graphics.fillGradient(x, y, x + width, y + height, topColor, bottomColor);
        }

        /**
         * Draws a grid inside a specific bounding box.
         */
        public static void drawHoloGrid(GuiGraphics graphics, int x, int y, int width, int height, int cellSize, int gridColor) {
            for (int i = 0; i <= width; i += cellSize) {
                graphics.fill(x + i, y, x + i + 1, y + height, gridColor);
            }
            for (int j = 0; j <= height; j += cellSize) {
                graphics.fill(x, y + j, x + width, y + j + 1, gridColor);
            }
        }

        /**
         * Draws text perfectly centered on the given X and Y coordinates.
         */
        public static void drawCenteredText(GuiGraphics graphics, Font font, String text, int x, int y, int color, boolean dropShadow) {
            int width = font.width(text);
            int drawX = x - (width / 2);
            int drawY = y - (font.lineHeight / 2);

            graphics.drawString(font, text, drawX, drawY, color, dropShadow);
        }

        /**
         * Draws text perfectly centered on the X coordinate, with optional pulsing alpha.
         */
        public static void drawCenteredPulsingText(GuiGraphics graphics, Font font, String text, int centerX, int y, int rgbColor, float time, float pulseSpeed) {
            int textWidth = font.width(text);
            int drawX = centerX - (textWidth / 2);

            float alphaMult = 0.65F + (Mth.sin(time * pulseSpeed) * 0.35F);
            int alpha = (int) (255 * alphaMult);
            int finalColor = (alpha << 24) | (rgbColor & 0xFFFFFF);

            graphics.drawString(font, text, drawX, y, finalColor, true);
        }

        /**
         * Draws text with a blurred bloom effect behind it.
         */
        public static void drawBloomingText(GuiGraphics graphics, Font font, String text, int x, int y, int coreColor, int bloomColor) {
            graphics.drawString(font, text, x - 1, y, bloomColor, false);
            graphics.drawString(font, text, x + 1, y, bloomColor, false);
            graphics.drawString(font, text, x, y - 1, bloomColor, false);
            graphics.drawString(font, text, x, y + 1, bloomColor, false);

            graphics.drawString(font, text, x, y, coreColor, false);
        }

        /**
         * Draws smooth text that dynamically changes color (e.g., breathing or rainbow).
         */
        public static void drawAnimatedText(GuiGraphics graphics, Font font, String text, int x, int y, float time, boolean dropShadow) {
            float hue = (time * 0.05F + (x * 0.01F)) % 1.0F;
            int color = Mth.hsvToRgb(hue, 0.8F, 1.0F);

            graphics.drawString(font, text, x, y, color, dropShadow);
        }
    }

    public static class Placement {

        /**
         * Calculates the centered coordinate for an element. Works universally for X (width) and Y (height).
         */
        public static int center(int screenDimen, int elementDimen) {
            return (screenDimen - elementDimen) / 2;
        }

        /**
         * Anchors an element to a specific percentage across the screen.
         */
        public static int percent(int screenDimen, float percentage, int elementDimen) {
            return (int) (screenDimen * percentage) - (elementDimen / 2);
        }

        /**
         * Anchors an element to the right side of the screen.
         */
        public static int right(int screenWidth, int elementWidth, int padding) {
            return screenWidth - elementWidth - padding;
        }

        /**
         * Anchors an element to the bottom of the screen.
         */
        public static int bottom(int screenHeight, int elementHeight, int padding) {
            return screenHeight - elementHeight - padding;
        }

        /**
         * Calculates the X coordinate for an item in a grid layout.
         */
        public static int gridX(int startX, int column, int elementWidth, int spacing) {
            return startX + (column * (elementWidth + spacing));
        }

        /**
         * Calculates the Y coordinate for an item in a grid layout.
         */
        public static int gridY(int startY, int row, int elementHeight, int spacing) {
            return startY + (row * (elementHeight + spacing));
        }
    }
}