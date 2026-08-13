package honor.core;

import honor.G2D;
import honor.Img;
import honor.entity.Hero;
import honor.util.SettingsStore;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public final class TouchControls {
    private static final int MAX_POINTERS = 10;
    private static final int STICK_R = 34;
    private static final int ATK_R = 22;
    private static final int SK_R = 16;
    private static final int BTN_ALPHA = 128;
    private static final int STICK_ALPHA = 104;
    private int sw;
    private int sh;
    private int stickR = 34;
    private int atkR = 22;
    private int skillR = 16;
    private int stickHomeX;
    private int stickHomeY;
    private int stickCx;
    private int stickCy;
    private boolean stickRemembered;
    private int zoneX0;
    private int zoneY0;
    private int zoneX1;
    private int zoneY1;
    private int atkCx;
    private int atkCy;
    private int s1Cx;
    private int s1Cy;
    private int s2Cx;
    private int s2Cy;
    private int ultCx;
    private int ultCy;
    private int recallCx;
    private int recallCy;
    private int shopCx;
    private int shopCy;
    private int pauseCx;
    private int pauseCy;
    private int detailsCx;
    private int detailsCy;
    private boolean shopMode;
    private int shopPanelX;
    private int shopPanelW;
    private int shopHeaderY;
    private int shopRowY;
    private int shopRowH;
    private boolean stickDown;
    private int stickPx;
    private int stickPy;
    private int axisX100;
    private int axisY100;
    private int pressedBits;
    private final boolean[] pointerActive = new boolean[10];
    private final int[] pointerIds = new int[10];
    private final int[] pointerBits = new int[10];
    private int stickPointerId = Integer.MIN_VALUE;
    private int lastPressedBit;
    private int pressFlash;
    private BufferedImage[] circleCache = new BufferedImage[12];
    private int[] circleKey = new int[12];
    private int circleCacheN;

    public TouchControls() {
    }

    public void ensureLayout(int var1, int var2) {
        if (var1 != this.sw || var2 != this.sh) {
            this.layout(var1, var2);
        }
    }

    public void layout(int var1, int var2) {
        this.sw = var1;
        this.sh = var2;
        boolean var3 = var1 > var2;
        boolean var4 = var1 <= 200 || var2 <= 260;
        this.stickR = var4 ? 28 : 34;
        this.atkR = var4 ? 19 : 22;
        this.skillR = var4 ? 13 : 16;
        this.zoneX0 = 0;
        this.zoneY0 = this.sh * 45 / 100;
        this.zoneX1 = this.sw * 48 / 100;
        this.zoneY1 = this.sh;
        if (var3) {
            this.stickHomeX = this.stickR + 14;
            this.stickHomeY = var2 - this.stickR - 10;
            this.atkCx = var1 - this.atkR - 14;
            this.atkCy = var2 - this.atkR - 10;
            this.s1Cx = this.atkCx - this.atkR - this.skillR - 10;
            this.s1Cy = this.atkCy + 2;
            this.s2Cx = this.atkCx - this.atkR - 4;
            this.s2Cy = this.atkCy - this.atkR - this.skillR - 12;
            this.ultCx = this.atkCx + 5;
            this.ultCy = this.s2Cy - 5;
            this.recallCx = this.s2Cx - this.skillR * 2 - 9;
            this.recallCy = this.s2Cy + 8;
            this.shopCx = var1 - this.skillR - 8;
            this.shopCy = var2 / 2 - 10;
            this.pauseCx = var1 - this.skillR - 7;
            this.pauseCy = this.skillR + 32;
            this.detailsCx = this.pauseCx - this.skillR * 2 - 8;
            this.detailsCy = this.pauseCy;
        } else {
            this.stickHomeX = this.stickR + 8;
            this.stickHomeY = var2 - this.stickR - 8;
            this.atkCx = var1 - this.atkR - 8;
            this.atkCy = var2 - this.atkR - 8;
            this.s1Cx = this.atkCx - this.atkR - this.skillR - 8;
            this.s1Cy = this.atkCy - 2;
            this.s2Cx = this.atkCx - this.atkR - 6;
            this.s2Cy = this.atkCy - this.atkR - this.skillR - 13;
            this.ultCx = this.atkCx + 4;
            this.ultCy = this.s2Cy - 4;
            this.recallCx = this.s2Cx - this.skillR * 2 - 8;
            this.recallCy = this.s2Cy + 10;
            this.shopCx = var1 - this.skillR - 5;
            this.shopCy = var2 / 2 + 20;
            this.pauseCx = var1 - this.skillR - 5;
            this.pauseCy = this.skillR + 32;
            this.detailsCx = this.pauseCx - this.skillR * 2 - 6;
            this.detailsCy = this.pauseCy;
        }

        this.stickHomeX = clamp(this.stickHomeX, this.zoneX0 + this.stickR, this.zoneX1 - this.stickR);
        this.stickHomeY = clamp(this.stickHomeY, this.zoneY0 + this.stickR, this.zoneY1 - this.stickR);
        if (!this.stickRemembered) {
            this.stickCx = this.stickHomeX;
            this.stickCy = this.stickHomeY;
        } else if (!this.stickDown) {
            this.stickCx = clamp(this.stickCx, this.zoneX0 + this.stickR, this.zoneX1 - this.stickR);
            this.stickCy = clamp(this.stickCy, this.zoneY0 + this.stickR, this.zoneY1 - this.stickR);
        }

        this.circleCacheN = 0;
        Font layFont = G2D.decodeJ2meFont(64, 1, 8);
        BufferedImage layImg = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D layG = layImg.createGraphics();
        int var5 = layG.getFontMetrics(layFont).getHeight() + 1;
        layG.dispose();
        this.shopRowH = var5 * 3;
        int var6 = var5 * 2 + 8;
        this.shopPanelW = this.sw - (this.sw <= 200 ? 12 : 24);
        this.shopPanelX = (this.sw - this.shopPanelW) / 2;
        int var7 = var6 + this.shopRowH * 3 + 7;
        int var8 = (this.sh - var7) / 2;
        this.shopHeaderY = var8 + var6 / 2;
        this.shopRowY = var8 + var6;
    }

    public void setShopMode(boolean var1, KeyInput var2) {
        if (this.shopMode != var1) {
            this.cancelAll(var2);
        }

        this.shopMode = var1;
    }

    public boolean pointerPressed(int var1, int var2, int var3, KeyInput var4, Hero var5) {
        int var6 = this.findPointer(var1);
        if (var6 >= 0) {
            this.releasePointer(var6, var4);
        }

        int var7 = this.allocatePointer(var1);
        if (var7 < 0) {
            return false;
        } else {
            boolean var8 = this.stickDown;
            this.lastPressedBit = 0;
            boolean var9 = this.pointerPressedInternal(var2, var3, var4, var5);
            if (!var9) {
                this.clearPointer(var7);
                return false;
            } else {
                if (!var8 && this.stickDown) {
                    this.stickPointerId = var1;
                } else {
                    this.pointerBits[var7] = this.lastPressedBit;
                }

                return true;
            }
        }
    }

    private boolean pointerPressedInternal(int var1, int var2, KeyInput var3, Hero var4) {
        if (this.shopMode) {
            if (var2 < this.shopRowY) {
                int var7 = var1 >= this.sw / 2 ? 1024 : 16;
                this.markPressed(var7);
                var3.touchAction(var7, true);
                return true;
            } else {
                int var5 = (var2 - this.shopRowY) / this.shopRowH;
                if (var5 >= 0 && var5 < 3 && var1 >= this.shopPanelX && var1 <= this.shopPanelX + this.shopPanelW) {
                    int var6 = var5 == 0 ? 32 : (var5 == 1 ? 64 : 128);
                    this.markPressed(var6);
                    var3.touchAction(var6, true);
                    return true;
                } else {
                    return false;
                }
            }
        } else if (this.tapButton(var1, var2, this.pauseCx, this.pauseCy, this.skillR, 2048, var3)) {
            return true;
        } else if (this.tapButton(var1, var2, this.detailsCx, this.detailsCy, this.skillR, 4096, var3)) {
            return true;
        } else if (this.tapButton(var1, var2, this.atkCx, this.atkCy, this.atkR, 16, var3)) {
            return true;
        } else if (this.tapSkillButton(var1, var2, this.s1Cx, this.s1Cy, this.skillR, 32, 0, var4, var3)) {
            return true;
        } else if (this.tapSkillButton(var1, var2, this.s2Cx, this.s2Cy, this.skillR, 64, 1, var4, var3)) {
            return true;
        } else if (this.tapSkillButton(var1, var2, this.ultCx, this.ultCy, this.skillR, 128, 2, var4, var3)) {
            return true;
        } else if (this.tapButton(var1, var2, this.recallCx, this.recallCy, this.skillR, 512, var3)) {
            return true;
        } else if (this.tapButton(var1, var2, this.shopCx, this.shopCy, this.skillR, 1024, var3)) {
            return true;
        } else if (!this.inStickZone(var1, var2)) {
            if (var1 >= this.sw / 2 && !SettingsStore.get().isAutoAttack()) {
                this.markPressed(16);
                this.pressFlash = 0;
                var3.touchAction(16, true);
                return true;
            } else {
                return false;
            }
        } else {
            this.stickDown = true;
            if (!this.stickRemembered || !inCircle(var1, var2, this.stickCx, this.stickCy, this.stickR + 18)) {
                this.stickCx = clamp(var1, this.zoneX0 + this.stickR, this.zoneX1 - this.stickR);
                this.stickCy = clamp(var2, this.zoneY0 + this.stickR, this.zoneY1 - this.stickR);
            }

            this.stickRemembered = true;
            this.updateStick(var1, var2, var3);
            return true;
        }
    }

    public void pointerDragged(int var1, int var2, int var3, KeyInput var4) {
        if (this.stickDown && var1 == this.stickPointerId) {
            this.updateStick(var2, var3, var4);
        }

    }

    public void pointerReleased(int var1, int var2, int var3, KeyInput var4) {
        int var5 = this.findPointer(var1);
        if (var5 < 0) {
            var5 = this.findPointerAt(var2, var3);
        }

        if (var5 >= 0) {
            this.releasePointer(var5, var4);
        }
    }

    public void cancelAll(KeyInput var1) {
        for(int var2 = 0; var2 < 10; ++var2) {
            if (this.pointerActive[var2]) {
                this.clearPointer(var2);
            }
        }

        this.stickDown = false;
        this.stickPointerId = Integer.MIN_VALUE;
        this.axisX100 = 0;
        this.axisY100 = 0;
        var1.cancelTouchActions();
        this.pressedBits = 0;
        this.pressFlash = 0;
    }

    private void releasePointer(int var1, KeyInput var2) {
        int var3 = this.pointerIds[var1];
        if (this.stickDown && var3 == this.stickPointerId) {
            this.stickDown = false;
            this.stickPointerId = Integer.MIN_VALUE;
            this.axisX100 = 0;
            this.axisY100 = 0;
            this.stickCx = clamp(this.stickCx, this.zoneX0 + this.stickR, this.zoneX1 - this.stickR);
            this.stickCy = clamp(this.stickCy, this.zoneY0 + this.stickR, this.zoneY1 - this.stickR);
            this.stickRemembered = true;
            var2.setTouchAxis(0, 0);
        }

        int var4 = this.pointerBits[var1];
        if (var4 != 0) {
            var2.touchAction(var4, false);
            this.pressFlash = 6;
        }

        this.clearPointer(var1);
    }

    public void renderSpectator(Graphics2D var1, SettingsStore var2) {
        if (var2 != null && var2.isTouchMode()) {
            if (this.pressFlash > 0) {
                --this.pressFlash;
                if (this.pressFlash == 0) {
                    this.pressedBits = 0;
                }
            }

            int var3 = this.spectatorButtonY();
            this.drawBtn(var1, this.pauseCx, var3, this.skillR, 5592422, "退", this.isPressedVis(2048));
            this.drawBtn(var1, this.detailsCx, var3, this.skillR, 4874888, "详", this.isPressedVis(4096));
        }
    }

    private int spectatorButtonY() {
        return this.pauseCy + this.skillR + 10;
    }

    public boolean hitSpectatorBack(int var1, int var2) {
        return inCircle(var1, var2, this.pauseCx, this.spectatorButtonY(), this.skillR + 8);
    }

    public boolean hitSpectatorDetails(int var1, int var2) {
        return inCircle(var1, var2, this.detailsCx, this.spectatorButtonY(), this.skillR + 8);
    }

    public int pointerSpectator(int var1, int var2, int var3, KeyInput var4) {
        byte var5 = 0;
        if (this.hitSpectatorBack(var2, var3)) {
            var5 = 1;
            this.markPressed(2048);
        } else {
            if (!this.hitSpectatorDetails(var2, var3)) {
                return 0;
            }

            var5 = 2;
            this.markPressed(4096);
        }

        int var6 = this.findPointer(var1);
        if (var6 >= 0) {
            this.releasePointer(var6, var4);
        }

        int var7 = this.allocatePointer(var1);
        if (var7 >= 0) {
            this.pointerBits[var7] = this.lastPressedBit;
        }

        this.pressFlash = 6;
        return var5;
    }

    public void render(Graphics2D var1, SettingsStore var2, Hero var3) {
        if (var2 != null && var2.isTouchMode()) {
            if (this.pressFlash > 0) {
                --this.pressFlash;
                if (this.pressFlash == 0) {
                    this.pressedBits = 0;
                }
            }

            if (this.shopMode) {
                int var12 = this.skillR > 12 ? 12 : this.skillR;
                this.drawBtn(var1, this.shopPanelX + 15, this.shopHeaderY, var12, 4029408, ">", this.isPressedVis(16));
                this.drawBtn(var1, this.shopPanelX + this.shopPanelW - 15, this.shopHeaderY, var12, 9072704, "$", this.isPressedVis(1024));
                String[] var13 = new String[]{"1", "3", "7"};

                for(int var14 = 0; var14 < 3; ++var14) {
                    int var15 = var14 == 0 ? 32 : (var14 == 1 ? 64 : 128);
                    this.drawBtn(var1, this.shopPanelX + 15, this.shopRowY + var14 * this.shopRowH + 9, var12, var14 == 2 ? 12618288 : 4889450, var13[var14], this.isPressedVis(var15));
                }

            } else {
                this.drawStick(var1);
                boolean var4 = var3 == null || var3.isSkillUnlocked(0);
                boolean var5 = var3 == null || var3.isSkillUnlocked(1);
                boolean var6 = var3 == null || var3.isSkillUnlocked(2);
                String var7 = var3 != null && var3.atkCdLeft > 0 ? "" : "攻";
                String var8 = var3 != null && var3.skillCd[0] > 0 ? "" : "1";
                String var9 = var3 != null && var3.skillCd[1] > 0 ? "" : "2";
                String var10 = var3 != null && var3.skillCd[2] > 0 ? "" : "大";
                this.drawBtn(var1, this.pauseCx, this.pauseCy, this.skillR, 5592422, "II", this.isPressedVis(2048));
                this.drawBtn(var1, this.detailsCx, this.detailsCy, this.skillR, 4874888, "详", this.isPressedVis(4096));
                this.drawBtn(var1, this.atkCx, this.atkCy, this.atkR, 4029408, var7, this.isPressedVis(16));
                this.drawBtn(var1, this.s1Cx, this.s1Cy, this.skillR, var4 ? 4889450 : 5593183, var8, this.isPressedVis(32));
                this.drawBtn(var1, this.s2Cx, this.s2Cy, this.skillR, var5 ? 4889450 : 5593183, var9, this.isPressedVis(64));
                this.drawBtn(var1, this.ultCx, this.ultCy, this.skillR, var6 ? 12618288 : 5593183, var10, this.isPressedVis(128));
                this.drawBtn(var1, this.recallCx, this.recallCy, this.skillR, 6974090, "回", this.isPressedVis(512));
                this.drawBtn(var1, this.shopCx, this.shopCy, this.skillR, 9072704, "$", this.isPressedVis(1024));
                if (var3 != null) {
                    this.drawCooldown(var1, this.atkCx, this.atkCy, var3.atkCdLeft);
                    this.drawCooldown(var1, this.s1Cx, this.s1Cy, var3.skillCd[0]);
                    this.drawCooldown(var1, this.s2Cx, this.s2Cy, var3.skillCd[1]);
                    this.drawCooldown(var1, this.ultCx, this.ultCy, var3.skillCd[2]);
                    int var11 = var3.attackSpeed10();
                    var1.setFont(G2D.decodeJ2meFont(64, 1, 8));
                    G2D.setColor(var1, 14215423);
                    G2D.drawString(var1,var11 / 10 + "." + var11 % 10 + "/秒", this.atkCx, this.atkCy - this.atkR - 12, 17);
                }

                this.drawSkillUpgrade(var1, var3, 0, this.s1Cx, this.s1Cy);
                this.drawSkillUpgrade(var1, var3, 1, this.s2Cx, this.s2Cy);
                this.drawSkillUpgrade(var1, var3, 2, this.ultCx, this.ultCy);
            }
        }
    }

    public int skillUpgradeAt(int var1, int var2, Hero var3) {
        if (!this.shopMode && var3 != null && var3.alive && var3.skillPoints > 0) {
            int var4 = this.skillR <= 13 ? 8 : 9;
            if (!var3.canUpgradeSkill(0) || !inCircle(var1, var2, this.upgradeX(this.s1Cx), this.upgradeY(this.s1Cy), var4) && (var3.skillLv[0] != 0 || !inCircle(var1, var2, this.s1Cx, this.s1Cy, this.skillR + 4))) {
                if (!var3.canUpgradeSkill(1) || !inCircle(var1, var2, this.upgradeX(this.s2Cx), this.upgradeY(this.s2Cy), var4) && (var3.skillLv[1] != 0 || !inCircle(var1, var2, this.s2Cx, this.s2Cy, this.skillR + 4))) {
                    return !var3.canUpgradeSkill(2) || !inCircle(var1, var2, this.upgradeX(this.ultCx), this.upgradeY(this.ultCy), var4) && (var3.skillLv[2] != 0 || !inCircle(var1, var2, this.ultCx, this.ultCy, this.skillR + 4)) ? -1 : 2;
                } else {
                    return 1;
                }
            } else {
                return 0;
            }
        } else {
            return -1;
        }
    }

    private void drawSkillUpgrade(Graphics2D var1, Hero var2, int var3, int var4, int var5) {
        if (var2 != null && var2.alive && var2.canUpgradeSkill(var3)) {
            int var6 = this.skillR <= 13 ? 7 : 8;
            int var7 = this.upgradeX(var4);
            int var8 = this.upgradeY(var5);
            this.drawAlphaCircle(var1, var7, var8, var6, 2668632, 210, true);
            var1.setFont(G2D.decodeJ2meFont(64, 1, 8));
            G2D.setColor(var1, 16777215);
            G2D.drawString(var1,"+", var7, var8 - var1.getFontMetrics(var1.getFont()).getHeight() / 2, 17);
        }
    }

    private int upgradeX(int var1) {
        return var1 - this.skillR + 2;
    }

    private int upgradeY(int var1) {
        return var1 - this.skillR + 2;
    }

    private boolean isPressedVis(int var1) {
        return (this.pressedBits & var1) != 0;
    }

    private void updateStick(int var1, int var2, KeyInput var3) {
        this.stickPx = var1;
        this.stickPy = var2;
        int var4 = var1 - this.stickCx;
        int var5 = var2 - this.stickCy;
        int var6 = this.stickR;
        int var7 = var4 < 0 ? -var4 : var4;
        int var8 = var5 < 0 ? -var5 : var5;
        int var9 = var7 + var8;
        if (var9 > var6 && var9 > 0) {
            var4 = var4 * var6 / var9;
            var5 = var5 * var6 / var9;
        }

        this.axisX100 = var4 * 100 / var6;
        this.axisY100 = var5 * 100 / var6;
        if (this.axisX100 > 100) {
            this.axisX100 = 100;
        }

        if (this.axisX100 < -100) {
            this.axisX100 = -100;
        }

        if (this.axisY100 > 100) {
            this.axisY100 = 100;
        }

        if (this.axisY100 < -100) {
            this.axisY100 = -100;
        }

        var3.setTouchAxis(this.axisX100, this.axisY100);
    }

    private boolean tapButton(int var1, int var2, int var3, int var4, int var5, int var6, KeyInput var7) {
        if (!inCircle(var1, var2, var3, var4, var5 + 4)) {
            return false;
        } else {
            this.markPressed(var6);
            this.pressFlash = 0;
            var7.touchAction(var6, true);
            return true;
        }
    }

    private boolean tapSkillButton(int var1, int var2, int var3, int var4, int var5, int var6, int var7, Hero var8, KeyInput var9) {
        if (!inCircle(var1, var2, var3, var4, var5 + 4)) {
            return false;
        } else if (var8 != null && var8.canCast(var7)) {
            this.markPressed(var6);
            this.pressFlash = 0;
            var9.touchAction(var6, true);
            return true;
        } else {
            return true;
        }
    }

    private void markPressed(int var1) {
        this.lastPressedBit = var1;
        this.pressedBits |= var1;
    }

    private int findPointer(int var1) {
        for(int var2 = 0; var2 < 10; ++var2) {
            if (this.pointerActive[var2] && this.pointerIds[var2] == var1) {
                return var2;
            }
        }

        return -1;
    }

    private int allocatePointer(int var1) {
        for(int var2 = 0; var2 < 10; ++var2) {
            if (!this.pointerActive[var2]) {
                this.pointerActive[var2] = true;
                this.pointerIds[var2] = var1;
                this.pointerBits[var2] = 0;
                return var2;
            }
        }

        return -1;
    }

    private int findPointerAt(int var1, int var2) {
        if (this.stickDown && this.inStickZone(var1, var2)) {
            return this.findPointer(this.stickPointerId);
        } else {
            for(int var3 = 0; var3 < 10; ++var3) {
                if (this.pointerActive[var3] && this.pointerBits[var3] != 0 && this.isActionAt(this.pointerBits[var3], var1, var2)) {
                    return var3;
                }
            }

            return -1;
        }
    }

    private boolean isActionAt(int var1, int var2, int var3) {
        if (var1 == 16) {
            return inCircle(var2, var3, this.atkCx, this.atkCy, this.atkR + 8);
        } else if (var1 == 32) {
            return inCircle(var2, var3, this.s1Cx, this.s1Cy, this.skillR + 8);
        } else if (var1 == 64) {
            return inCircle(var2, var3, this.s2Cx, this.s2Cy, this.skillR + 8);
        } else if (var1 == 128) {
            return inCircle(var2, var3, this.ultCx, this.ultCy, this.skillR + 8);
        } else if (var1 == 512) {
            return inCircle(var2, var3, this.recallCx, this.recallCy, this.skillR + 8);
        } else if (var1 == 1024) {
            return inCircle(var2, var3, this.shopCx, this.shopCy, this.skillR + 8);
        } else if (var1 == 2048) {
            return inCircle(var2, var3, this.pauseCx, this.pauseCy, this.skillR + 8);
        } else {
            return var1 == 4096 ? inCircle(var2, var3, this.detailsCx, this.detailsCy, this.skillR + 8) : false;
        }
    }

    private void clearPointer(int var1) {
        this.pointerActive[var1] = false;
        this.pointerIds[var1] = 0;
        this.pointerBits[var1] = 0;
    }

    private boolean inStickZone(int var1, int var2) {
        return var1 >= this.zoneX0 && var1 < this.zoneX1 && var2 >= this.zoneY0 && var2 < this.zoneY1;
    }

    private static boolean inCircle(int var0, int var1, int var2, int var3, int var4) {
        int var5 = var0 - var2;
        int var6 = var1 - var3;
        return var5 * var5 + var6 * var6 <= var4 * var4;
    }

    private static int clamp(int var0, int var1, int var2) {
        if (var1 > var2) {
            return (var1 + var2) / 2;
        } else if (var0 < var1) {
            return var1;
        } else {
            return var0 > var2 ? var2 : var0;
        }
    }

    private void drawStick(Graphics2D var1) {
        int var2 = this.stickDown ? this.stickR - 2 : this.stickR;
        int var3 = this.stickDown ? 7829384 : 5592422;
        this.drawAlphaCircle(var1, this.stickCx, this.stickCy, var2, var3, 104, true);
        int var4 = this.stickCx;
        int var5 = this.stickCy;
        if (this.stickDown) {
            var4 = this.stickCx + this.axisX100 * this.stickR / 100;
            var5 = this.stickCy + this.axisY100 * this.stickR / 100;
        }

        int var6 = this.stickDown ? 14 : 12;
        int var7 = this.stickDown ? 16777215 : 14739711;
        this.drawAlphaCircle(var1, var4, var5, var6, var7, 128, true);
    }

    private void drawBtn(Graphics2D var1, int var2, int var3, int var4, int var5, String var6, boolean var7) {
        int var8 = var7 ? var4 - 3 : var4;
        int var9 = var7 ? 1 : 0;
        int var10 = var7 ? 1 : 0;
        int var11 = var2 + var9;
        int var12 = var3 + var10;
        int var13 = var7 ? brighten(var5, 50) : var5;
        this.drawAlphaCircle(var1, var11, var12, var8, 2236962, 128, false);
        this.drawAlphaCircle(var1, var11, var12, var8 - 2, var13, 128, true);
        G2D.setColor(var1,16777215);
        G2D.drawString(var1,var6, var11, var12 - 6, 17);
    }

    private void drawCooldown(Graphics2D var1, int var2, int var3, int var4) {
        if (var4 > 0) {
            String var5;
            if (var4 >= 15) {
                var5 = String.valueOf((var4 + 14) / 15);
            } else {
                int var6 = (var4 * 10 + 14) / 15;
                var5 = var6 >= 10 ? "1.0" : "0." + var6;
            }

            Font var9 = G2D.decodeJ2meFont(64, 1, 8);
            var1.setFont(var9);
            int var7 = var1.getFontMetrics(var9).getHeight();
            int var8 = var3 - var7 / 2;
            G2D.setColor(var1,2106412);
            G2D.drawString(var1,var5, var2 + 1, var8 + 1, 17);
            G2D.setColor(var1,16777215);
            G2D.drawString(var1,var5, var2, var8, 17);
        }
    }

    private void drawAlphaCircle(Graphics2D var1, int var2, int var3, int var4, int var5, int var6, boolean var7) {
        if (var4 >= 2) {
            int var8 = var4 << 24 ^ var5 << 8 ^ var6 << 1 ^ (var7 ? 1 : 0);
            BufferedImage var9 = null;

            for(int var10 = 0; var10 < this.circleCacheN; ++var10) {
                if (this.circleKey[var10] == var8) {
                    var9 = this.circleCache[var10];
                    break;
                }
            }

            if (var9 == null) {
                int var23 = var4 * 2 + 1;
                int[] var11 = new int[var23 * var23];
                int var12 = var4 * var4;
                int var13 = (var4 - 1) * (var4 - 1);
                int var14 = (var6 & 255) << 24;
                int var15 = var14 | var5 & 16777215;
                int var16 = var6 + 40;
                if (var16 > 255) {
                    var16 = 255;
                }

                int var17 = var16 << 24 | 16777215;

                for(int var18 = 0; var18 < var23; ++var18) {
                    int var19 = var18 - var4;

                    for(int var20 = 0; var20 < var23; ++var20) {
                        int var21 = var20 - var4;
                        int var22 = var21 * var21 + var19 * var19;
                        if (var22 <= var12) {
                            if (var7 && var22 > var13) {
                                var11[var18 * var23 + var20] = var17;
                            } else {
                                var11[var18 * var23 + var20] = var15;
                            }
                        }
                    }
                }

                var9 = Img.createRgb(var11, var23, var23, true);
                if (this.circleCacheN < this.circleCache.length) {
                    this.circleKey[this.circleCacheN] = var8;
                    this.circleCache[this.circleCacheN] = var9;
                    ++this.circleCacheN;
                }
            }

            G2D.drawImage(var1,var9, var2 - var4, var3 - var4, 20);
        }
    }

    private static int brighten(int var0, int var1) {
        int var2 = (var0 >> 16 & 255) + var1;
        int var3 = (var0 >> 8 & 255) + var1;
        int var4 = (var0 & 255) + var1;
        if (var2 > 255) {
            var2 = 255;
        }

        if (var3 > 255) {
            var3 = 255;
        }

        if (var4 > 255) {
            var4 = 255;
        }

        return var2 << 16 | var3 << 8 | var4;
    }
}
