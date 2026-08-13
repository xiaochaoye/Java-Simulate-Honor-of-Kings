package honor.core;

public final class IsoMath {
    public static final int LOGIC = 16;
    public static final int DX_COL = 11;
    public static final int DY_COL = 10;
    public static final int DX_ROW = -22;
    public static final int DY_ROW = 5;
    public static final int TILE_W = 31;
    public static final int TILE_H = 15;
    public static final int TILE_IMG_H = 40;
    public static final int TILE_ORIGIN_X = 21;

    private IsoMath() {
    }

    public static int toScreenX(int var0, int var1) {
        return 11 * var0 + -22 * var1 >> 4;
    }

    public static int toScreenY(int var0, int var1) {
        return 10 * var0 + 5 * var1 >> 4;
    }

    public static int depth(int var0, int var1) {
        return 10 * var0 + 5 * var1;
    }

    public static void screenDirToWorld(int var0, int var1, int[] var2) {
        var2[0] = 5 * var0 + 22 * var1;
        var2[1] = -10 * var0 + 11 * var1;
    }

    public static int minScreenX(int var0, int var1) {
        return toScreenX(0, var1);
    }

    public static int spanScreenX(int var0, int var1) {
        int var2 = toScreenX(var0, 0) - toScreenX(0, var1);
        return var2 > 0 ? var2 : 1;
    }

    public static int spanScreenY(int var0, int var1) {
        int var2 = toScreenY(var0, var1);
        return var2 > 0 ? var2 : 1;
    }

    public static int floorDiv(int var0, int var1) {
        int var2 = var0 / var1;
        if (var0 % var1 != 0 && var0 < 0) {
            --var2;
        }

        return var2;
    }

    public static int cellCenterX(int var0) {
        return var0 * 16 + 8;
    }

    public static int cellCenterY(int var0) {
        return var0 * 16 + 8;
    }
}
