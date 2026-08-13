package honor.util;

import java.io.IOException;
import java.io.InputStream;
import java.awt.image.BufferedImage;
import honor.Img;

public final class Res {
    private Res() {
    }

    public static BufferedImage loadImage(String var0) throws IOException {
        try {
            return Img.load(var0);
        } catch (IOException var2) {
            Log.error("loadImage failed: " + var0);
            throw var2;
        }
    }

    public static BufferedImage tryLoadImage(String var0) {
        return Img.tryLoad(var0);
    }

    public static InputStream open(String var0) {
        return Res.class.getResourceAsStream(var0);
    }
}
