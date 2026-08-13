package honor.ui;

import honor.core.KeyInput;
import honor.core.MatchConfig;
import honor.core.Scene;
import honor.util.SettingsStore;
import honor.util.Sfx;
import honor.util.StatsStore;
import java.awt.Graphics2D;
import java.awt.Color;
import honor.G2D;

public final class MenuScene implements Scene {
    private static final int ITEM_START = 0;
    private static final int ITEM_ONLINE = 1;
    private static final int ITEM_MODE = 2;
    private static final int ITEM_CTRL = 3;
    private static final int ITEM_AUTO = 4;
    private static final int ITEM_HELP = 5;
    private static final int ITEM_QUIT = 6;
    private static final int ITEM_COUNT = 7;
    private static final String[] LABELS = new String[]{"单机对战", "联机对战", "对战模式", "操控方式", "自动攻击", "操作说明", "退出游戏"};
    private static final String[] HELP_LINES = new String[]{"【按键】2 4 6 8 / WASD 移动", "1/3 技能   7 大招   9 回城", "0 商店   5 确认   ESC 退出", "【触摸】左半屏滑动移动", "右侧：攻 / 技能 / 回城 / 商店", "关闭自动攻击时需点普攻键"};
    private int cursor;
    private int mode = 1;
    private boolean helpOpen;
    private int next = -1;
    private int listTop;
    private int lineH;
    private int screenW;
    private int screenH;
    private int pressItem = -1;
    private int pressFlash;
    private boolean helpBackPressed;

    public MenuScene() {
    }

    public int getMode() {
        return this.mode;
    }

    public void reset() {
        this.next = -1;
        this.helpOpen = false;
        this.pressItem = -1;
        this.pressFlash = 0;
        this.helpBackPressed = false;
    }

    public void onPointer(int var1, int var2, int var3, int var4) {
        this.ensureHitMetrics(var3, var4);
        if (this.helpOpen) {
            this.helpOpen = false;
            this.helpBackPressed = true;
            this.pressFlash = 6;
            Sfx.play("ui_click");
        } else {
            int var5 = (var2 - this.listTop) / this.lineH;
            if (var5 >= 0 && var5 < 7) {
                byte var6 = 24;
                int var7 = this.screenW > 48 ? this.screenW - 48 : this.screenW;
                if (var1 >= var6 - 4 && var1 <= var6 + var7 + 4) {
                    this.cursor = var5;
                    this.pressItem = var5;
                    this.pressFlash = 8;
                    this.activateItem();
                }
            }
        }
    }

    public void onPointerReleased() {
        this.helpBackPressed = false;
    }

    public void update(KeyInput var1, int var2) {
        if (this.next != -1) {
            if (this.pressFlash > 0) {
                --this.pressFlash;
            }

        } else {
            if (this.pressFlash > 0) {
                --this.pressFlash;
                if (this.pressFlash == 0) {
                    this.pressItem = -1;
                }
            }

            if (this.helpOpen) {
                if (var1.isPressed(16) || var1.isPressed(2048)) {
                    this.helpOpen = false;
                    Sfx.play("ui_click");
                }

            } else {
                if (var1.isPressed(1)) {
                    this.cursor = (this.cursor + 7 - 1) % 7;
                    Sfx.play("ui_click");
                }

                if (var1.isPressed(2)) {
                    this.cursor = (this.cursor + 1) % 7;
                    Sfx.play("ui_click");
                }

                if (this.cursor == 2) {
                    if (var1.isPressed(4)) {
                        this.mode = (this.mode + 3 - 1) % 3;
                        Sfx.play("ui_click");
                    }

                    if (var1.isPressed(8)) {
                        this.mode = (this.mode + 1) % 3;
                        Sfx.play("ui_click");
                    }
                }

                if (this.cursor == 3 && (var1.isPressed(4) || var1.isPressed(8))) {
                    SettingsStore.get().toggleControlMode();
                    Sfx.play("ui_ok");
                }

                if (this.cursor == 4 && (var1.isPressed(4) || var1.isPressed(8))) {
                    SettingsStore.get().toggleAutoAttack();
                    Sfx.play("ui_ok");
                }

                if (var1.isPressed(16)) {
                    this.pressItem = this.cursor;
                    this.pressFlash = 6;
                    this.activateItem();
                }

            }
        }
    }

    private void activateItem() {
        if (this.cursor == 0) {
            this.next = 1;
            Sfx.play("ui_ok");
        } else if (this.cursor == 1) {
            this.next = 4;
            Sfx.play("ui_ok");
        } else if (this.cursor == 2) {
            this.mode = (this.mode + 1) % 3;
            Sfx.play("ui_click");
        } else if (this.cursor == 3) {
            SettingsStore.get().toggleControlMode();
            Sfx.play("ui_ok");
        } else if (this.cursor == 4) {
            SettingsStore.get().toggleAutoAttack();
            Sfx.play("ui_ok");
        } else if (this.cursor == 5) {
            this.helpOpen = true;
            Sfx.play("ui_click");
        } else {
            this.next = 3;
        }

    }

    public void render(Graphics2D var1, int var2, int var3, int var4) {
        this.ensureHitMetrics(var2, var3);
        UiKit.backdrop(var1, var2, var3);
        this.drawTitle(var1, var2, var4);
        this.lineH = G2D.fontHeight(UiKit.MEDIUM) + 8;
        int var5 = this.lineH * 7;
        this.listTop = (var3 - var5) / 2 + 8;

        for(int var6 = 0; var6 < 7; ++var6) {
            this.drawItem(var1, var2, this.listTop + var6 * this.lineH, var6, var4);
        }

        var1.setFont(UiKit.SMALL);
        var1.setColor(G2D.color(8885420));
        StatsStore var8 = StatsStore.get();
        String var7 = "战绩 " + var8.getWins() + "胜" + var8.getLosses() + "负  K/D " + var8.getKills() + "/" + var8.getDeaths();
        G2D.drawString(var1, var7, var2 / 2, var3 - G2D.fontHeight(UiKit.SMALL) * 2 - 6, 17);
        G2D.drawString(var1, "上下选择  左右改设置  5确认  可触摸", var2 / 2, var3 - G2D.fontHeight(UiKit.SMALL) - 4, 17);
        if (this.helpOpen) {
            this.drawHelp(var1, var2, var3);
        }

    }

    public int nextScene() {
        return this.next;
    }

    private void ensureHitMetrics(int var1, int var2) {
        if (var1 > 0) {
            this.screenW = var1;
        }

        if (var2 > 0) {
            this.screenH = var2;
        }

        if (this.lineH <= 0 || this.screenW <= 0 || this.screenH <= 0) {
            if (this.screenW <= 0) {
                this.screenW = 240;
            }

            if (this.screenH <= 0) {
                this.screenH = 320;
            }

            this.lineH = G2D.fontHeight(UiKit.MEDIUM) + 8;
            int var3 = this.lineH * 7;
            this.listTop = (this.screenH - var3) / 2 + 8;
        }
    }

    private void drawTitle(Graphics2D var1, int var2, int var3) {
        var1.setFont(UiKit.LARGE);
        byte var4 = 18;
        var1.setColor(G2D.color(3811848));
        G2D.drawString(var1, "王者峡谷", var2 / 2 + 1, var4 + 1, 17);
        var1.setColor(G2D.color(15254634));
        G2D.drawString(var1, "王者峡谷", var2 / 2, var4, 17);
        var1.setFont(UiKit.SMALL);
        var1.setColor(G2D.color(8885420));
        G2D.drawString(var1, "HONOR CANYON", var2 / 2, var4 + G2D.fontHeight(UiKit.LARGE) + 1, 17);
    }

    private void drawItem(Graphics2D var1, int var2, int var3, int var4, int var5) {
        boolean var6 = var4 == this.cursor;
        boolean var7 = this.pressItem == var4 && this.pressFlash > 0;
        int var8 = var2 - 48;
        int var9 = 24;
        int var10 = G2D.fontHeight(UiKit.MEDIUM) + 4;
        if (var7) {
            ++var9;
            ++var3;
            var8 -= 2;
            var10 -= 2;
        }

        if (!var6 && !var7) {
            var1.setColor(G2D.color(1054762));
            var1.fillRoundRect(var9, var3, var8, var10, 6, 6);
            var1.setColor(G2D.color(2766160));
        } else {
            UiKit.gradientV(var1, var9, var3, var8, var10, var7 ? 3820158 : 2767454, var7 ? 2371674 : 1450042);
            var1.setColor(G2D.color(var7 ? 16777215 : 15254634));
        }

        var1.drawRoundRect(var9, var3, var8, var10, 6, 6);
        var1.setFont(UiKit.MEDIUM);
        var1.setColor(G2D.color(!var6 && !var7 ? 8885420 : 14476530));
        G2D.drawString(var1, LABELS[var4], var9 + 10, var3 + 2, 20);
        var1.setColor(G2D.color(15254634));
        String var11 = null;
        if (var4 == 2) {
            var11 = MatchConfig.modeName(this.mode);
        } else if (var4 == 3) {
            var11 = SettingsStore.get().isTouchMode() ? "触摸" : "按键";
        } else if (var4 == 4) {
            var11 = SettingsStore.get().isAutoAttack() ? "开" : "关";
        }

        if (var11 != null) {
            G2D.drawString(var1, var11, var9 + var8 - 10, var3 + 2, 24);
        }

        if (var6 && !var7 && (var5 >> 2 & 1) == 0) {
            G2D.fillTriangle(var1, var9 - 8, var3 + var10 / 2 - 4, var9 - 8, var3 + var10 / 2 + 4, var9 - 2, var3 + var10 / 2);
        }

    }

    private void drawHelp(Graphics2D var1, int var2, int var3) {
        var1.setFont(UiKit.SMALL);
        int var4 = G2D.fontHeight(UiKit.SMALL) + 3;
        int var5 = var2 - 20;
        int var6 = var4 * (HELP_LINES.length + 3) + 16;
        byte var7 = 10;
        int var8 = (var3 - var6) / 2;
        UiKit.panel(var1, var7, var8, var5, var6, 15254634);
        var1.setColor(G2D.color(15254634));
        G2D.drawString(var1, "操 作 说 明", var7 + var5 / 2, var8 + 6, 17);
        var1.setColor(G2D.color(14476530));

        for(int var9 = 0; var9 < HELP_LINES.length; ++var9) {
            G2D.drawString(var1, HELP_LINES[var9], var7 + 8, var8 + 8 + var4 * (var9 + 1), 20);
        }

        int var11 = var7 + (var5 - 54) / 2;
        int var10 = var8 + var6 - 24 - 8;
        UiKit.drawBackButton(var1, var11, var10, this.helpBackPressed || this.pressFlash > 4);
    }
}
