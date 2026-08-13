package honor.core.loader;

import honor.core.PointerIdProvider;

public final class LoaderPointerIdProvider implements PointerIdProvider {
    public LoaderPointerIdProvider() {
    }

    public int currentPointerId() {
        // J2ME Display.getPointerNumber() 在 Java SE 不可用，桌面端默认单指针
        return 0;
    }
}
