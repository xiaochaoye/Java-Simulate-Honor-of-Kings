package honor.ui;

import honor.G2D;
import honor.Img;
import honor.hero.HeroDef;
import honor.util.Res;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import java.util.Map;

public final class UiKit {
    public static final int C_PANEL = 725024;
    public static final int C_PANEL_HI = 1845570;
    public static final int C_GOLD = 15254634;
    public static final int C_GOLD_DK = 8018974;
    public static final int C_TEXT = 14476530;
    public static final int C_DIM = 8885420;
    public static final int C_BLUE = 4029408;
    public static final int C_RED = 14174280;
    public static final int C_HP = 4177759;
    public static final int C_HP_HI = 8842148;
    public static final int C_MP = 4161496;
    public static final int C_MP_HI = 9224447;
    public static final int C_EXP = 12623936;
    private static final int PORTRAIT_BASE = 32;
    private static BufferedImage[] portraits;
    private static BufferedImage[][] portraitSized;
    private static BufferedImage[][] portraitDead;
    public static final Font SMALL = G2D.decodeJ2meFont(64, 1, 8);
    public static final Font MEDIUM = G2D.decodeJ2meFont(64, 1, 0);
    public static final Font LARGE = G2D.decodeJ2meFont(64, 1, 16);
    public static final int BACK_BTN_W = 54;
    public static final int BACK_BTN_H = 24;
    public static final int CONFIRM_BTN_H = 28;
    public static final int CONFIRM_BTN_GAP = 6;
    public static final int CONFIRM_HIT_OK = 1;
    public static final int CONFIRM_HIT_CANCEL = 2;

    private UiKit() {
    }

    public static int confirmDialogHeight() {
        return 134;
    }

    public static void drawConfirmDialog(Graphics2D var0, int var1, String var2, String var3, int var4, int var5, int var6, int var7) {
        panel(var0, var4, var5, var6, var7, 15254634);
        var0.setFont(SMALL);
        G2D.setColor(var0,14476530);
        G2D.drawString(var0,var2, var1 / 2, var5 + 12, 17);
        G2D.drawString(var0,var3, var1 / 2, var5 + 30, 17);
        int var8 = var6 - 24;
        int var9 = var4 + 12;
        int var10 = confirmOkY(var5, var7);
        int var11 = confirmCancelY(var5, var7);
        drawTextButton(var0, var9, var10, var8, 28, "确认退出", 15254634, false);
        drawTextButton(var0, var9, var11, var8, 28, "取消", 8885420, false);
    }

    public static int hitConfirmDialog(int var0, int var1, int var2, int var3, int var4, int var5) {
        int var6 = var4 - 24;
        int var7 = var2 + 12;
        if (hitRect(var0, var1, var7, confirmOkY(var3, var5), var6, 28, 4)) {
            return 1;
        } else {
            return hitRect(var0, var1, var7, confirmCancelY(var3, var5), var6, 28, 4) ? 2 : 0;
        }
    }

    private static int confirmOkY(int var0, int var1) {
        return var0 + var1 - 12 - 28 - 6 - 28;
    }

    private static int confirmCancelY(int var0, int var1) {
        return var0 + var1 - 12 - 28;
    }

    public static void drawBackButton(Graphics2D var0, int var1, int var2, boolean var3) {
        drawTextButton(var0, var1, var2, 54, 24, "返回", 15254634, var3);
    }

    public static boolean hitBackButton(int var0, int var1, int var2, int var3) {
        return var0 >= var2 - 4 && var0 < var2 + 54 + 4 && var1 >= var3 - 4 && var1 < var3 + 24 + 4;
    }

    public static void drawTextButton(Graphics2D var0, int var1, int var2, int var3, int var4, String var5, int var6, boolean var7) {
        int var8 = var7 ? 1 : 0;
        int var9 = var7 ? 1 : 0;
        int var10 = var7 ? var3 - 2 : var3;
        int var11 = var7 ? var4 - 2 : var4;
        int var12 = var1 + var8;
        int var13 = var2 + var9;
        gradientV(var0, var12, var13, var10, var11, var7 ? shade(1845570, 30) : 1845570, var7 ? shade(725024, 20) : 725024);
        G2D.setColor(var0,var7 ? shade(var6, 40) : var6);
        var0.drawRoundRect(var12, var13, var10, var11, 6, 6);
        if (var7) {
            G2D.setColor(var0,shade(var6, -40));
            var0.drawRoundRect(var12 + 1, var13 + 1, var10 - 2, var11 - 2, 5, 5);
        }

        var0.setFont(SMALL);
        G2D.setColor(var0,var7 ? 16777215 : 14476530);
        G2D.drawString(var0,var5, var12 + var10 / 2, var13 + (var11 - var0.getFontMetrics(SMALL).getHeight()) / 2, 17);
    }

    public static boolean hitRect(int var0, int var1, int var2, int var3, int var4, int var5, int var6) {
        return var0 >= var2 - var6 && var0 < var2 + var4 + var6 && var1 >= var3 - var6 && var1 < var3 + var5 + var6;
    }

    // 渐变图缓存：backdrop/顶栏/商店面板每帧逐行 fillRect + 逐行 mix 色值插缓存，
    // 是每帧最大的一块绘制开销。改为预烘焙成 1×h 单列图，横向拉伸绘制，
    // 每帧只 1 次 drawImage，且不再向 Color 缓存灌入几十上百个色值。
    // 用 mix 同款算法生成，视觉完全一致。key=(c1,c2,h)，LRU 限 8 张。
    private static final int GRADIENT_CACHE_MAX = 8;
    private static final Map<Long, BufferedImage> gradientCache =
        new LinkedHashMap<Long, BufferedImage>(8, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Long, BufferedImage> eldest) {
                return size() > GRADIENT_CACHE_MAX;
            }
        };

    public static void gradientV(Graphics2D var0, int var1, int var2, int var3, int var4, int var5, int var6) {
        if (var3 <= 0 || var4 <= 0) {
            return;
        }

        long key = ((long)(var5 & 0xFFFFFF) << 36) | ((long)(var6 & 0xFFFFFF) << 12) | (var4 & 0xFFF);
        BufferedImage col = gradientCache.get(key);
        if (col == null) {
            col = new BufferedImage(1, var4, BufferedImage.TYPE_INT_RGB);
            Graphics2D ig = col.createGraphics();
            for (int var7 = 0; var7 < var4; ++var7) {
                G2D.setColor(ig, mix(var5, var6, var7, var4));
                ig.fillRect(0, var7, 1, 1);
            }
            ig.dispose();
            gradientCache.put(key, col);
        }

        var0.drawImage(col, var1, var2, var3, var4, null);
    }

    public static void backdrop(Graphics2D var0, int var1, int var2) {
        gradientV(var0, 0, 0, var1, var2, 1252400, 329743);
        G2D.setColor(var0,1977416);
        var0.drawLine(0, var2 / 3, var1, var2 / 3);
        G2D.setColor(var0,1450554);
        var0.drawLine(0, var2 / 3 + 2, var1, var2 / 3 + 2);
    }

    public static void panel(Graphics2D var0, int var1, int var2, int var3, int var4, int var5) {
        gradientV(var0, var1, var2, var3, var4, 1845570, 725024);
        G2D.setColor(var0,var5);
        var0.drawRoundRect(var1, var2, var3, var4, 8, 8);
        var0.drawRoundRect(var1 + 2, var2 + 2, var3 - 4, var4 - 4, 6, 6);
    }

    public static void bar(Graphics2D var0, int var1, int var2, int var3, int var4, int var5, int var6, int var7) {
        G2D.setColor(var0,329486);
        var0.fillRoundRect(var1 - 1, var2 - 1, var3 + 2, var4 + 2, 4, 4);
        G2D.setColor(var0,2238778);
        var0.fillRoundRect(var1, var2, var3, var4, 3, 3);
        int var8 = var3 * clamp(var5, 0, 100) / 100;
        if (var8 > 0) {
            G2D.setColor(var0,var6);
            var0.fillRoundRect(var1, var2, var8, var4, 3, 3);
            G2D.setColor(var0,var7);
            var0.fillRect(var1 + 1, var2 + 1, var8 > 2 ? var8 - 2 : 1, 1);
        }

        G2D.setColor(var0,8018974);
        var0.drawRoundRect(var1 - 1, var2 - 1, var3 + 2, var4 + 2, 4, 4);
    }

    public static void pips(Graphics2D var0, int var1, int var2, int var3, int var4, int var5, int var6, int var7) {
        for(int var8 = 0; var8 < var6; ++var8) {
            G2D.setColor(var0,var8 < var5 ? var7 : 2304570);
            var0.fillRect(var1 + var8 * (var3 + 1), var2, var3, var4);
        }

    }

    public static void coin(Graphics2D var0, int var1, int var2) {
        G2D.setColor(var0,8018974);
        var0.fillArc(var1 - 4, var2 - 4, 8, 8, 0, 360);
        G2D.setColor(var0,15254634);
        var0.fillArc(var1 - 3, var2 - 3, 6, 6, 0, 360);
    }

    public static void avatar(Graphics2D var0, int var1, int var2, int var3, int var4, int var5, boolean var6, boolean var7) {
        if (var3 < 4) {
            var3 = 4;
        }

        int var8 = HeroDef.theme(var4);
        G2D.setColor(var0,329486);
        var0.fillArc(var1 - var3 - 1, var2 - var3 - 1, (var3 + 1) * 2, (var3 + 1) * 2, 0, 360);
        BufferedImage var9 = portrait(var4, var3 * 2, var6);
        if (var9 != null) {
            G2D.drawImage(var0,var9, var1, var2, 3);
        } else {
            G2D.setColor(var0,var6 ? var8 : 4868693);
            var0.fillArc(var1 - var3, var2 - var3, var3 * 2, var3 * 2, 0, 360);
            G2D.setColor(var0,shade(var6 ? var8 : 4868693, 60));
            int var10 = var3 / 3;
            var0.fillArc(var1 - var10, var2 - var3 / 2 - var10, var10 * 2 + 1, var10 * 2 + 1, 0, 360);
            var0.fillArc(var1 - var3 / 2 - 1, var2, var3 + 2, var3, 0, 180);
        }

        G2D.setColor(var0,var6 ? 15254634 : 8018974);
        var0.drawArc(var1 - var3, var2 - var3, var3 * 2, var3 * 2, 0, 360);
        if (var5 >= 1) {
            int var12 = var7 ? var1 - var3 - 7 : var1 + var3 - 6;
            int var11 = var2 + var3 - 6;
            G2D.setColor(var0,658968);
            var0.fillArc(var12, var11, 13, 13, 0, 360);
            G2D.setColor(var0,15254634);
            var0.drawArc(var12, var11, 13, 13, 0, 360);
            var0.setFont(SMALL);
            G2D.drawString(var0,String.valueOf(var5), var12 + 7, var11 - 1, 17);
        }
    }

    public static BufferedImage portrait(int var0, int var1, boolean var2) {
        if (var0 >= 0 && var0 < 19 && var1 >= 8) {
            ensurePortraitBase();
            if (portraits != null && portraits[var0] != null) {
                if (portraitSized == null) {
                    portraitSized = new BufferedImage[19][];
                    portraitDead = new BufferedImage[19][];
                }

                BufferedImage[] var3 = var2 ? portraitSized[var0] : portraitDead[var0];
                if (var3 == null) {
                    var3 = new BufferedImage[65];
                    if (var2) {
                        portraitSized[var0] = var3;
                    } else {
                        portraitDead[var0] = var3;
                    }
                }

                if (var1 >= var3.length) {
                    var1 = var3.length - 1;
                }

                if (var3[var1] == null) {
                    var3[var1] = scalePortrait(portraits[var0], var1, !var2);
                }

                return var3[var1];
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    private static void ensurePortraitBase() {
        if (portraits == null) {
            portraits = new BufferedImage[19];

            for(int var0 = 0; var0 < 19; ++var0) {
                try {
                    portraits[var0] = buildPortraitBase(var0);
                } catch (Throwable var2) {
                    portraits[var0] = null;
                }
            }

        }
    }

    private static BufferedImage buildPortraitBase(int var0) {
        BufferedImage var1 = Res.tryLoadImage(HeroDef.facePath(var0));
        if (var1 != null) {
            return circleMaskImage(var1, 32, -16777216 | HeroDef.theme(var0));
        } else {
            BufferedImage var2 = Res.tryLoadImage(HeroDef.spritePath(var0));
            if (var2 == null) {
                return null;
            } else {
                byte var3 = 32;
                byte var4 = 40;
                if (var2.getWidth() >= var3 && var2.getHeight() >= var4) {
                    int[] var5 = new int[var3 * var4];
                    Img.getRGB(var2, var5, 0, var3, 0, 0, var3, var4);
                    byte var6 = 32;
                    int[] var7 = new int[var6 * var6];
                    int var8 = -16777216 | HeroDef.theme(var0);
                    byte var9 = 28;
                    int var10 = var6 / 2;
                    int var11 = var10 * var10;

                    for(int var12 = 0; var12 < var6; ++var12) {
                        for(int var13 = 0; var13 < var6; ++var13) {
                            int var14 = var13 - var10;
                            int var15 = var12 - var10;
                            if (var14 * var14 + var15 * var15 > var11) {
                                var7[var12 * var6 + var13] = 0;
                            } else {
                                int var16 = var13 * var3 / var6;
                                int var17 = var12 * var9 / var6;
                                if (var17 >= var4) {
                                    var17 = var4 - 1;
                                }

                                int var18 = var5[var17 * var3 + var16];
                                if ((var18 >>> 24 & 255) < 16) {
                                    var18 = var8;
                                }

                                var7[var12 * var6 + var13] = var18;
                            }
                        }
                    }

                    return Img.createRgb(var7, var6, var6, true);
                } else {
                    return null;
                }
            }
        }
    }

    private static BufferedImage circleMaskImage(BufferedImage var0, int var1, int var2) {
        int var3 = var0.getWidth();
        int var4 = var0.getHeight();
        int[] var5 = new int[var3 * var4];
        Img.getRGB(var0, var5, 0, var3, 0, 0, var3, var4);
        int[] var6 = new int[var1 * var1];
        int var7 = var1 / 2;
        int var8 = var7 * var7;

        for(int var9 = 0; var9 < var1; ++var9) {
            for(int var10 = 0; var10 < var1; ++var10) {
                int var11 = var10 - var7;
                int var12 = var9 - var7;
                if (var11 * var11 + var12 * var12 > var8) {
                    var6[var9 * var1 + var10] = 0;
                } else {
                    int var13 = var10 * var3 / var1;
                    int var14 = var9 * var4 / var1;
                    int var15 = var5[var14 * var3 + var13];
                    if ((var15 >>> 24 & 255) < 8) {
                        var15 = var2;
                    } else {
                        var15 |= -16777216;
                    }

                    var6[var9 * var1 + var10] = var15;
                }
            }
        }

        return Img.createRgb(var6, var1, var1, true);
    }

    private static BufferedImage scalePortrait(BufferedImage var0, int var1, boolean var2) {
        int var3 = var0.getWidth();
        int var4 = var0.getHeight();
        int[] var5 = new int[var3 * var4];
        Img.getRGB(var0, var5, 0, var3, 0, 0, var3, var4);
        int[] var6 = new int[var1 * var1];

        for(int var7 = 0; var7 < var1; ++var7) {
            int var8 = var7 * var4 / var1;

            for(int var9 = 0; var9 < var1; ++var9) {
                int var10 = var9 * var3 / var1;
                int var11 = var5[var8 * var3 + var10];
                if (var2 && (var11 >>> 24 & 255) > 0) {
                    int var12 = var11 >> 16 & 255;
                    int var13 = var11 >> 8 & 255;
                    int var14 = var11 & 255;
                    int var15 = (var12 * 30 + var13 * 59 + var14 * 11) / 100;
                    var11 = var11 & -16777216 | var15 << 16 | var15 << 8 | var15;
                }

                var6[var7 * var1 + var9] = var11;
            }
        }

        return Img.createRgb(var6, var1, var1, true);
    }

    public static int mix(int var0, int var1, int var2, int var3) {
        if (var3 <= 1) {
            return var0;
        } else {
            int var4 = ((var0 >> 16 & 255) * (var3 - var2) + (var1 >> 16 & 255) * var2) / var3;
            int var5 = ((var0 >> 8 & 255) * (var3 - var2) + (var1 >> 8 & 255) * var2) / var3;
            int var6 = ((var0 & 255) * (var3 - var2) + (var1 & 255) * var2) / var3;
            return var4 << 16 | var5 << 8 | var6;
        }
    }

    public static int shade(int var0, int var1) {
        int var2 = clamp((var0 >> 16 & 255) + var1, 0, 255);
        int var3 = clamp((var0 >> 8 & 255) + var1, 0, 255);
        int var4 = clamp((var0 & 255) + var1, 0, 255);
        return var2 << 16 | var3 << 8 | var4;
    }

    public static String pad2(int var0) {
        return var0 < 10 ? "0" + var0 : String.valueOf(var0);
    }

    public static int clamp(int var0, int var1, int var2) {
        if (var0 < var1) {
            return var1;
        } else {
            return var0 > var2 ? var2 : var0;
        }
    }
}
