package honor.util;

public final class Log {
    public static boolean enabled = true;

    private Log() {
    }

    public static void info(String var0) {
        if (enabled) {
            System.out.println("[I] " + var0);
        }

    }

    public static void warn(String var0) {
        if (enabled) {
            System.out.println("[W] " + var0);
        }

    }

    public static void error(String var0) {
        System.out.println("[E] " + var0);
    }
}