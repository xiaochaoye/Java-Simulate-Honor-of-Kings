package honor.net;

import java.util.Vector;

public final class Protocol {
    public static final int VERSION = 3;
    public static final int CMD_NONE = 0;
    public static final int CMD_UPGRADE = 1;
    public static final int CMD_BUY = 16;

    private Protocol() {
    }

    public static String[] split(String var0, char var1) {
        Vector var2 = new Vector();
        int var3 = 0;
        int var4 = var0.length();

        for(int var5 = 0; var5 < var4; ++var5) {
            if (var0.charAt(var5) == var1) {
                var2.addElement(var0.substring(var3, var5));
                var3 = var5 + 1;
            }
        }

        var2.addElement(var0.substring(var3));
        String[] var6 = new String[var2.size()];
        var2.copyInto(var6);
        return var6;
    }

    public static int parseInt(String var0, int var1) {
        try {
            return Integer.parseInt(var0);
        } catch (Exception var3) {
            return var1;
        }
    }

    public static int[] parseInts(String var0, char var1) {
        String[] var2 = split(var0, var1);
        int[] var3 = new int[var2.length];

        for(int var4 = 0; var4 < var2.length; ++var4) {
            var3[var4] = parseInt(var2[var4], 0);
        }

        return var3;
    }
}
