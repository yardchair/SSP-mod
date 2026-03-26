package com.bodyhealthmod.client.gui;

import com.bodyhealthmod.common.capability.BodyHealthCapability;
import com.bodyhealthmod.common.capability.IBodyHealth;
import com.bodyhealthmod.common.damage.BodyPart;
import com.bodyhealthmod.common.damage.BodyPartHealth;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Renders the body-part HUD panel.
 *
 * Shows a simple body silhouette in the bottom-right of the screen.
 * Each body part box highlights red briefly when hit, then fades back to neutral.
 * The actual vanilla damage number floats next to the hit part and fades over 3 seconds.
 *
 * Vanilla hearts are completely unaffected — they display normally in their usual position.
 * This HUD is read-only and has zero gameplay effect.
 *
 * Layout (3 columns, 3 rows):
 *
 *   col:  0        1        2
 *  row 0: [  ]    [HD]    [  ]
 *  row 1: [LA]    [CH]    [RA]
 *  row 2: [LL]    [RL]    [  ]
 */
@SideOnly(Side.CLIENT)
public class BodyHealthHUD extends Gui {

    // Box dimensions
    private static final int BOX_W       = 30;
    private static final int BOX_H       = 16;
    private static final int GAP         = 4;
    private static final int PANEL_PAD   = 6;
    private static final int PANEL_RIGHT = 8;
    private static final int PANEL_BOTTOM= 48;   // sits just above the hotbar

    // How long the hit flash lasts before starting to fade (ticks)
    private static final int FADE_START  = 40;

    // Grid: null = empty cell
    private static final BodyPart[][] GRID = {
        { null,            BodyPart.HEAD,      null            },
        { BodyPart.LEFT_ARM, BodyPart.CHEST,  BodyPart.RIGHT_ARM },
        { BodyPart.LEFT_LEG, BodyPart.RIGHT_LEG, null          }
    };

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.HEALTH) return;

        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.player;
        if (player == null) return;

        IBodyHealth bh = player.getCapability(BodyHealthCapability.BODY_HEALTH, null);
        if (bh == null) return;

        // Tick display timers
        for (BodyPartHealth bph : bh.getParts().values()) bph.clientTick();

        int sw = mc.displayWidth  / mc.gameSettings.guiScale;
        int sh = mc.displayHeight / mc.gameSettings.guiScale;

        int cols   = 3;
        int rows   = GRID.length;
        int panelW = PANEL_PAD * 2 + cols * BOX_W + (cols - 1) * GAP;
        int panelH = PANEL_PAD * 2 + rows * BOX_H + (rows - 1) * GAP;

        int panelX = sw - panelW - PANEL_RIGHT;
        int panelY = sh - panelH - PANEL_BOTTOM;

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();

        // Panel background
        drawRect(panelX, panelY, panelX + panelW, panelY + panelH, 0x88000000);

        for (int row = 0; row < GRID.length; row++) {
            for (int col = 0; col < GRID[row].length; col++) {
                BodyPart bp = GRID[row][col];
                if (bp == null) continue;

                BodyPartHealth bph = bh.getPart(bp);
                int bx = panelX + PANEL_PAD + col * (BOX_W + GAP);
                int by = panelY + PANEL_PAD + row * (BOX_H + GAP);

                drawPartBox(bx, by, bph, mc);
            }
        }

        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private void drawPartBox(int x, int y, BodyPartHealth bph, Minecraft mc) {
        int ticks = bph.getHitDisplayTicks();

        // Box fill: red flash when recently hit, dark grey otherwise
        int fillColor;
        if (ticks > 0) {
            int alpha = ticks < FADE_START
                    ? (int)(0x88 * (ticks / (float) FADE_START))
                    : 0x88;
            fillColor = (alpha << 24) | 0xAA2222;
        } else {
            fillColor = 0x55333333;
        }

        // Draw fill
        drawRect(x, y, x + BOX_W, y + BOX_H, fillColor);

        // Draw border (always neutral grey)
        int border = 0xAAAAAAFF;
        drawHorizontalLine(x,          x + BOX_W - 1, y,            border);
        drawHorizontalLine(x,          x + BOX_W - 1, y + BOX_H - 1, border);
        drawVerticalLine  (x,          y, y + BOX_H - 1,             border);
        drawVerticalLine  (x + BOX_W - 1, y, y + BOX_H - 1,         border);

        // Label (always white)
        String label = abbrev(bph.getPart());
        int labelX = x + (BOX_W - mc.fontRenderer.getStringWidth(label)) / 2;
        int labelY = y + (BOX_H - mc.fontRenderer.FONT_HEIGHT) / 2;
        mc.fontRenderer.drawString(label, labelX, labelY, 0xFFFFFFFF, false);

        // Damage counter — shown to the right, fades out
        if (ticks > 0 && bph.getLastHitDamage() > 0f) {
            int alpha = ticks < FADE_START
                    ? (int)(255 * (ticks / (float) FADE_START))
                    : 255;
            String dmgStr = String.format("-%.1f", bph.getLastHitDamage());
            int color = (alpha << 24) | 0xFF5555;
            mc.fontRenderer.drawString(dmgStr, x + BOX_W + 3, y + (BOX_H - mc.fontRenderer.FONT_HEIGHT) / 2, color, true);
        }
    }

    private static String abbrev(BodyPart bp) {
        switch (bp) {
            case HEAD:      return "Head";
            case CHEST:     return "Chest";
            case LEFT_ARM:  return "L.Arm";
            case RIGHT_ARM: return "R.Arm";
            case LEFT_LEG:  return "L.Leg";
            case RIGHT_LEG: return "R.Leg";
            default:        return "?";
        }
    }
}
