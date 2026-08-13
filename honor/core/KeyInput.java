package honor.core;

public final class KeyInput {
    public static final int UP = 1;
    public static final int DOWN = 2;
    public static final int LEFT = 4;
    public static final int RIGHT = 8;
    public static final int FIRE = 16;
    public static final int SKILL1 = 32;
    public static final int SKILL2 = 64;
    public static final int ULT = 128;
    public static final int EXIT = 256;
    public static final int RECALL = 512;
    public static final int SHOP = 1024;
    public static final int BACK = 2048;
    public static final int DETAILS = 4096;
    private static final int POLL_MASK = 31;
    private static final int PRIME_FRAMES = 3;
    private int pollHeld;
    private int pollArmed;
    private int eventHeld;
    private int prevRaw;
    private int primeLeft = 3;
    private int pressed;
    private int lastRaw;
    private int touchAx;
    private int touchAy;
    private boolean touchMove;
    private int touchHeld;
    private final byte[] touchOwners = new byte[13];
    private boolean networkMode;
    private int networkHeld;
    private int networkPressed;
    private int networkAx;
    private int networkAy;
    private int networkCmd;
    private int pendingCmd;

    public KeyInput() {
    }

    public void beginFrame(int var1) {
        int var2 = 0;
        if ((var1 & 2) != 0) {
            var2 |= 1;
        }

        if ((var1 & 64) != 0) {
            var2 |= 2;
        }

        if ((var1 & 4) != 0) {
            var2 |= 4;
        }

        if ((var1 & 32) != 0) {
            var2 |= 8;
        }

        if ((var1 & 256) != 0) {
            var2 |= 16;
        }

        var2 &= 31;
        this.lastRaw = var2;
        if (this.primeLeft > 0) {
            --this.primeLeft;
            this.prevRaw = var2;
            this.pollArmed = 0;
            this.pollHeld = 0;
        } else {
            this.pollArmed = this.pollArmed & var2 | var2 & ~this.prevRaw;
            int var3 = this.pollArmed;
            this.pressed |= var3 & ~this.pollHeld;
            this.pollHeld = var3;
            this.prevRaw = var2;
            this.eventHeld = this.eventHeld & -32 | this.eventHeld & var2;
        }
    }

    public void onGameAction(int var1, boolean var2) {
        byte var3 = 0;
        if (var1 == 1) {
            var3 = 1;
        } else if (var1 == 6) {
            var3 = 2;
        } else if (var1 == 2) {
            var3 = 4;
        } else if (var1 == 5) {
            var3 = 8;
        } else if (var1 == 8) {
            var3 = 16;
        }

        this.applyEvent(var3, var2);
    }

    public void onKeyCode(int var1, boolean var2) {
        this.applyEvent(mapKeyCode(var1), var2);
    }

    public void clearAll() {
        this.pollHeld = 0;
        this.pollArmed = 0;
        this.eventHeld = 0;
        this.pressed = 0;
        this.prevRaw = 0;
        this.lastRaw = 0;
        this.primeLeft = 3;
        this.touchAx = 0;
        this.touchAy = 0;
        this.touchMove = false;
        this.touchHeld = 0;
        this.networkMode = false;
        this.networkHeld = 0;
        this.networkPressed = 0;
        this.networkAx = 0;
        this.networkAy = 0;
        this.networkCmd = 0;
        this.pendingCmd = 0;

        for(int var1 = 0; var1 < this.touchOwners.length; ++var1) {
            this.touchOwners[var1] = 0;
        }

    }

    public void endFrame() {
        this.pressed = 0;
    }

    public void setTouchAxis(int var1, int var2) {
        if (var1 < -100) {
            var1 = -100;
        }

        if (var1 > 100) {
            var1 = 100;
        }

        if (var2 < -100) {
            var2 = -100;
        }

        if (var2 > 100) {
            var2 = 100;
        }

        this.touchAx = var1;
        this.touchAy = var2;
        this.touchMove = var1 * var1 + var2 * var2 > 100;
        if (!this.touchMove) {
            this.touchAx = 0;
            this.touchAy = 0;
        }

    }

    public void touchAction(int var1, boolean var2) {
        if (var1 != 0) {
            int var3 = touchIndex(var1);
            if (var3 >= 0) {
                if (var2) {
                    if (this.touchOwners[var3] < 127) {
                        ++this.touchOwners[var3];
                    }

                    this.touchHeld |= var1;
                } else if (this.touchOwners[var3] > 0) {
                    --this.touchOwners[var3];
                    if (this.touchOwners[var3] == 0) {
                        this.pressed |= var1;
                        this.touchHeld &= ~var1;
                    }
                } else {
                    this.touchHeld &= ~var1;
                }

            }
        }
    }

    public void releaseTouchActions() {
        this.pressed |= this.touchHeld;
        this.touchHeld = 0;

        for(int var1 = 0; var1 < this.touchOwners.length; ++var1) {
            this.touchOwners[var1] = 0;
        }

        this.touchAx = 0;
        this.touchAy = 0;
        this.touchMove = false;
    }

    public void cancelTouchActions() {
        this.touchHeld = 0;

        for(int var1 = 0; var1 < this.touchOwners.length; ++var1) {
            this.touchOwners[var1] = 0;
        }

        this.touchAx = 0;
        this.touchAy = 0;
        this.touchMove = false;
    }

    public boolean isHeld(int var1) {
        if (this.networkMode) {
            return (this.networkHeld & var1) != 0;
        } else {
            return ((this.pollHeld | this.eventHeld | this.touchHeld) & var1) != 0;
        }
    }

    public boolean isPressed(int var1) {
        if (this.networkMode) {
            return (this.networkPressed & var1) != 0;
        } else {
            return (this.pressed & var1) != 0;
        }
    }

    public int axisX() {
        int var1 = 0;
        if (this.isHeld(4)) {
            --var1;
        }

        if (this.isHeld(8)) {
            ++var1;
        }

        if (var1 == 0 && this.touchMove) {
            if (this.touchAx <= -25) {
                var1 = -1;
            } else if (this.touchAx >= 25) {
                var1 = 1;
            }
        }

        return var1;
    }

    public int axisY() {
        int var1 = 0;
        if (this.isHeld(1)) {
            --var1;
        }

        if (this.isHeld(2)) {
            ++var1;
        }

        if (var1 == 0 && this.touchMove) {
            if (this.touchAy <= -25) {
                var1 = -1;
            } else if (this.touchAy >= 25) {
                var1 = 1;
            }
        }

        return var1;
    }

    public int axisX100() {
        if (this.networkMode) {
            return this.networkAx;
        } else {
            return this.touchMove ? this.touchAx : this.axisX() * 100;
        }
    }

    public int axisY100() {
        if (this.networkMode) {
            return this.networkAy;
        } else {
            return this.touchMove ? this.touchAy : this.axisY() * 100;
        }
    }

    public int debugHeld() {
        return this.pollHeld | this.eventHeld | this.touchHeld;
    }

    public int heldMask() {
        return this.networkMode ? this.networkHeld : this.pollHeld | this.eventHeld | this.touchHeld;
    }

    public int pressedMask() {
        return this.networkMode ? this.networkPressed : this.pressed;
    }

    public void setNetworkFrame(int var1, int var2, int var3, int var4, int var5) {
        this.networkMode = true;
        this.networkHeld = var1;
        this.networkPressed = var2;
        this.networkAx = clampAxis(var3);
        this.networkAy = clampAxis(var4);
        this.networkCmd = var5;
    }

    public int networkCmd() {
        return this.networkCmd;
    }

    public void queueCommand(int var1) {
        if (var1 > 0) {
            this.pendingCmd = var1;
        }

    }

    public int takePendingCommand() {
        int var1 = this.pendingCmd;
        this.pendingCmd = 0;
        return var1;
    }

    private static int clampAxis(int var0) {
        if (var0 < -100) {
            return -100;
        } else {
            return var0 > 100 ? 100 : var0;
        }
    }

    public int debugRaw() {
        return this.lastRaw;
    }

    private static int touchIndex(int var0) {
        int var1;
        for(var1 = 0; var1 < 13 && 1 << var1 != var0; ++var1) {
        }

        return var1 < 13 ? var1 : -1;
    }

    private void applyEvent(int var1, boolean var2) {
        if (var1 != 0) {
            if (var2) {
                if ((this.eventHeld & var1) == 0) {
                    this.pressed |= var1;
                }

                this.eventHeld |= var1;
                this.pollArmed |= var1;
            } else {
                this.eventHeld &= ~var1;
                this.pollArmed &= ~var1;
                this.pollHeld &= ~var1;
            }

        }
    }

    private static int mapKeyCode(int var0) {
        switch (var0) {
            case -21:
            case -6:
                return 2048;
            case 27:   // ESC → BACK（菜单返回/对战暂停）
                return 2048;
            case 32:
            case 53:
                return 16;
            case 35:
                return 2048;
            case 42:   // 小键盘 *
                return 256;
            case 48:
                return 1024;
            case 49:
                return 32;
            case 50:
            case 87:
            case 119:
                return 1;
            case 51:
                return 64;
            case 52:
            case 65:
            case 97:
                return 4;
            case 54:
            case 68:
            case 100:
                return 8;
            case 55:
                return 128;
            case 56:
            case 83:
            case 115:
                return 2;
            case 57:
                return 512;
            case 73:
            case 105:
                return 4096;
            default:
                return 0;
        }
    }
}
