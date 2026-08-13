package honor.core;

public final class PointerIds {
    private static final String LOADER_PROVIDER = "honor.core.loader.LoaderPointerIdProvider";
    private static PointerIdProvider provider;
    private static boolean checked;

    private PointerIds() {
    }

    private static int forcedId;

    public static void setCurrent(int id) {
        forcedId = id;
    }

    public static int current() {
        if (!checked) {
            checked = true;

            try {
                provider = (PointerIdProvider)Class.forName("honor.core.loader.LoaderPointerIdProvider").newInstance();
            } catch (Throwable var1) {
                provider = null;
            }
        }

        if (provider != null) {
            try {
                return provider.currentPointerId();
            } catch (Throwable var2) {
                provider = null;
            }
        }

        return 0;
    }

    public static boolean isExtensionActive() {
        return provider != null;
    }
}
