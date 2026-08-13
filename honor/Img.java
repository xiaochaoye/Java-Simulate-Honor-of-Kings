package honor;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;

/**
 * J2ME Image 适配层 —— 替代 javax.microedition.lcdui.Image。
 * 纯静态工具类，底层使用 BufferedImage。
 */
public final class Img {

    private Img() {
    }

    /**
     * 从 classpath 加载图片。
     */
    public static BufferedImage load(String path) throws IOException {
        InputStream in = Img.class.getResourceAsStream(path);
        if (in == null) {
            throw new IOException("Resource not found: " + path);
        }
        try {
            BufferedImage img = ImageIO.read(in);
            if (img == null) {
                throw new IOException("Unsupported image format: " + path);
            }
            return img;
        } finally {
            try {
                in.close();
            } catch (IOException ignored) {
            }
        }
    }

    /**
     * load 的包装，失败返回 null（不抛异常）。
     */
    public static BufferedImage tryLoad(String path) {
        try {
            return load(path);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * 从像素数组创建 BufferedImage。
     * alpha=true → TYPE_INT_ARGB，alpha=false → TYPE_INT_RGB。
     */
    public static BufferedImage createRgb(int[] rgb, int w, int h, boolean alpha) {
        int type = alpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
        BufferedImage img = new BufferedImage(w, h, type);
        img.setRGB(0, 0, w, h, rgb, 0, w);
        return img;
    }

    /**
     * 读取图片像素到数组。
     * ⚠️ 关键：J2ME Image.getRGB(dst, off, scan, x, y, w, h) 与
     * AWT BufferedImage.getRGB(x, y, w, h, dst, off, scan) 参数顺序不同。
     * 此方法采用 J2ME 风格参数，内部完成转换。
     */
    public static void getRGB(BufferedImage img, int[] dst, int off, int scan,
                              int x, int y, int w, int h) {
        img.getRGB(x, y, w, h, dst, off, scan);
    }

    /**
     * 截取子图。本项目 transform 始终为 0，直接调用 getSubimage。
     */
    public static BufferedImage subImage(BufferedImage src, int x, int y,
                                         int w, int h, int transform) {
        return src.getSubimage(x, y, w, h);
    }

    /**
     * 从 classpath 打开 InputStream。
     */
    public static InputStream open(String path) {
        return Img.class.getResourceAsStream(path);
    }
}
