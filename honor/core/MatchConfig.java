package honor.core;

public final class MatchConfig {
    public static final int MODE_1V1 = 0;
    public static final int MODE_3V3 = 1;
    public static final int MODE_5V5 = 2;
    public static final int MODE_COUNT = 3;
    public final int teamSize;
    public final int mode;
    public final int[] blueHeroIds;
    public final int[] redHeroIds;

    private MatchConfig(int var1, int[] var2, int[] var3) {
        this.mode = var1;
        this.teamSize = var2.length;
        this.blueHeroIds = var2;
        this.redHeroIds = var3;
    }

    public static int teamSizeOf(int var0) {
        if (var0 == 1) {
            return 3;
        } else {
            return var0 == 2 ? 5 : 1;
        }
    }

    public static String modeName(int var0) {
        if (var0 == 1) {
            return "3v3 对战";
        } else {
            return var0 == 2 ? "5v5 对战" : "1v1 单挑";
        }
    }

    public int playerHeroId() {
        return this.blueHeroIds[0];
    }

    public static MatchConfig create(int var0, int var1, int var2) {
        int var3 = teamSizeOf(var0);
        int[] var4 = new int[var3];
        int[] var5 = new int[var3];
        var4[0] = var1;
        fillRoster(var4, 1, var2);
        fillRoster(var5, 0, var2 + 977);
        return new MatchConfig(var0, var4, var5);
    }

    public static MatchConfig fromRosters(int var0, int[] var1, int[] var2) {
        int var3 = teamSizeOf(var0);
        if (var1 != null && var2 != null && var1.length == var3 && var2.length == var3) {
            int[] var4 = new int[var3];
            int[] var5 = new int[var3];
            System.arraycopy(var1, 0, var4, 0, var3);
            System.arraycopy(var2, 0, var5, 0, var3);
            return new MatchConfig(var0, var4, var5);
        } else {
            throw new IllegalArgumentException("联机阵容人数与模式不匹配");
        }
    }

    private static void fillRoster(int[] var0, int var1, int var2) {
        int var3 = var2;

        for(int var4 = var1; var4 < var0.length; ++var4) {
            int var5 = -1;

            for(int var6 = 0; var6 < 19; ++var6) {
                var3 = nextRandom(var3);
                int var7 = (var3 >>> 8) % 19;
                if (!contains(var0, var4, var7)) {
                    var5 = var7;
                    break;
                }
            }

            if (var5 < 0) {
                for(int var8 = 0; var8 < 19; ++var8) {
                    if (!contains(var0, var4, var8)) {
                        var5 = var8;
                        break;
                    }
                }
            }

            var0[var4] = var5 < 0 ? 0 : var5;
        }

    }

    private static boolean contains(int[] var0, int var1, int var2) {
        for(int var3 = 0; var3 < var1; ++var3) {
            if (var0[var3] == var2) {
                return true;
            }
        }

        return false;
    }

    private static int nextRandom(int var0) {
        return var0 * 1103515245 + 12345;
    }
}
