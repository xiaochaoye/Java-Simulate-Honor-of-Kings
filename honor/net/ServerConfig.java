package honor.net;

public final class ServerConfig {
    public static final String HOST = "nas.zixing.fun";
    public static final int PORT = 9527;

    private ServerConfig() {
    }

    public static String socketUrl() {
        return "socket://nas.zixing.fun:9527";
    }
}