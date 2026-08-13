package honor.core;

public final class FMath {
    public static final int ONE = 65536;
    public static final int HALF = 32768;
    private static final int SIN_TABLE_SIZE = 91;
    private static final int[] SIN_TABLE = new int[91];

    private FMath() {
    }

    public static int fromInt(int var0) {
        return var0 << 16;
    }

    public static int toInt(int var0) {
        return var0 >> 16;
    }

    public static int round(int var0) {
        return var0 + '耀' >> 16;
    }

    public static int mul(int var0, int var1) {
        return (int)((long)var0 * (long)var1 >> 16);
    }

    public static int div(int var0, int var1) {
        return (int)(((long)var0 << 16) / (long)var1);
    }

    public static int abs(int var0) {
        return var0 < 0 ? -var0 : var0;
    }

    public static int min(int var0, int var1) {
        return var0 < var1 ? var0 : var1;
    }

    public static int max(int var0, int var1) {
        return var0 > var1 ? var0 : var1;
    }

    public static int clamp(int var0, int var1, int var2) {
        if (var0 < var1) {
            return var1;
        } else {
            return var0 > var2 ? var2 : var0;
        }
    }

    public static int sinDeg(int var0) {
        var0 %= 360;
        if (var0 < 0) {
            var0 += 360;
        }

        if (var0 <= 90) {
            return SIN_TABLE[var0];
        } else if (var0 <= 180) {
            return SIN_TABLE[180 - var0];
        } else {
            return var0 <= 270 ? -SIN_TABLE[var0 - 180] : -SIN_TABLE[360 - var0];
        }
    }

    public static int cosDeg(int var0) {
        return sinDeg(var0 + 90);
    }

    public static int isqrt(int var0) {
        if (var0 <= 0) {
            return 0;
        } else {
            int var1 = var0;

            for(int var2 = (var0 >> 1) + 1; var2 < var1; var2 = var0 / var2 + var2 >> 1) {
                var1 = var2;
            }

            return var1;
        }
    }

    public static int dist2(int var0, int var1, int var2, int var3) {
        int var4 = var2 - var0;
        int var5 = var3 - var1;
        return var4 * var4 + var5 * var5;
    }

    static {
        for(int var0 = 0; var0 < 91; ++var0) {
            if (var0 == 0) {
                SIN_TABLE[var0] = 0;
            } else if (var0 == 90) {
                SIN_TABLE[var0] = 65536;
            } else {
                int var2 = 4 * var0 * (180 - var0);
                int var3 = '鸴' - var0 * (180 - var0);
                SIN_TABLE[var0] = (int)(((long)var2 << 16) / (long)var3);
            }
        }

    }
}
