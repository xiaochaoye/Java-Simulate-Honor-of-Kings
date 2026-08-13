package honor;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * J2ME Graphics 适配层 —— 给 java.awt.Graphics2D 补充 J2ME 独有的方法和锚点常量。
 * 纯静态工具类。
 */
public final class G2D {

    // ── Color 缓存：避免每帧上百次 new Color() 导致 GC 卡顿 ──
    // 顶栏渐变 + 商店面板等高线渐变一帧可插入 100+ 色值，128 会频繁逐出重建，扩到 512
    private static final int COLOR_CACHE_MAX = 512;
    private static final Map<Integer, Color> colorCache =
        new LinkedHashMap<Integer, Color>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Integer, Color> eldest) {
                return size() > COLOR_CACHE_MAX;
            }
        };

    /** 返回缓存的 Color 实例（Color 对象不可变，多线程安全） */
    public static Color color(int rgb) {
        Color c = colorCache.get(rgb);
        if (c == null) {
            c = new Color(rgb);
            colorCache.put(rgb, c);
        }
        return c;
    }

    // ── 锚点常量（J2ME 特有，按位 OR 组合） ──
    public static final int HCENTER  = 1;
    public static final int VCENTER  = 2;
    public static final int LEFT     = 4;
    public static final int RIGHT    = 8;
    public static final int TOP      = 2;   // 注意：与 VCENTER 同值
    public static final int BOTTOM   = 32;
    public static final int BASELINE = 64;

    // 常用组合
    // HCENTER | VCENTER = 3
    // HCENTER | TOP     = 3   （与上一行同值，需由调用方在上下文中区分）
    // LEFT     | TOP     = 6
    // HCENTER | VCENTER = 3
    // RIGHT    | TOP     = 10
    // HCENTER | BOTTOM   = 33
    // LEFT     | VCENTER = 6
    // RIGHT    | VCENTER = 10

    private G2D() {
    }

    // ── 颜色 ──

    public static void setColor(Graphics2D g, int rgb) {
        g.setColor(color(rgb));
    }

    // ── 三角形 ──

    // fillPolygon 同步使用数组且仅渲染线程调用，复用静态数组免去每帧 new int[3]×2
    private static final int[] TRI_X = new int[3];
    private static final int[] TRI_Y = new int[3];

    public static void fillTriangle(Graphics2D g, int x1, int y1,
                                    int x2, int y2, int x3, int y3) {
        TRI_X[0] = x1;
        TRI_X[1] = x2;
        TRI_X[2] = x3;
        TRI_Y[0] = y1;
        TRI_Y[1] = y2;
        TRI_Y[2] = y3;
        g.fillPolygon(TRI_X, TRI_Y, 3);
    }

    // ── 带锚点的 drawString ──

    /**
     * 根据 anchor 计算偏移后调用 AWT drawString。
     *
     * 水平偏移（基于 FontMetrics.stringWidth）：
     *   HCENTER → x -= w/2
     *   RIGHT   → x -= w
     *
     * 垂直偏移（基于 FontMetrics.getAscent/getDescent）：
     *   TOP      → y += ascent       （AWT y 是 baseline）
     *   VCENTER  → y += ascent - h/2
     *   BOTTOM   → y -= descent
     *   BASELINE → y 不变
     */
    public static void drawString(Graphics2D g, String str, int x, int y, int anchor) {
        if (str == null || str.length() == 0) {
            return;
        }

        FontMetrics fm = g.getFontMetrics();
        if (fm == null) {
            g.drawString(str, x, y);
            return;
        }

        int w = fm.stringWidth(str);
        int ascent = fm.getAscent();
        int descent = fm.getDescent();
        int h = ascent + descent;

        // 水平偏移
        if ((anchor & HCENTER) != 0) {
            x -= w / 2;
        } else if ((anchor & RIGHT) != 0) {
            x -= w;
        }
        // LEFT: 不调整

        // 垂直偏移 — AWT drawString 的 y 是 baseline
        if ((anchor & BASELINE) != 0) {
            // 不调整，AWT 默认就是 baseline
        } else if ((anchor & BOTTOM) != 0) {
            y -= descent;
        } else if ((anchor & VCENTER) != 0) {
            y += ascent - h / 2;
        } else {
            // 默认视为 TOP（与 LEFT|TOP=20 兼容）
            y += ascent;
        }

        g.drawString(str, x, y);
    }

    // ── 带锚点的 drawImage ──

    /**
     * 根据 anchor 计算对齐偏移后绘制整张图。
     */
    public static void drawImage(Graphics2D g, BufferedImage img, int x, int y, int anchor) {
        if (img == null) {
            return;
        }

        int w = img.getWidth();
        int h = img.getHeight();

        int dx = x;
        int dy = y;

        // 水平偏移
        if ((anchor & HCENTER) != 0) {
            dx -= w / 2;
        } else if ((anchor & RIGHT) != 0) {
            dx -= w;
        }

        // 垂直偏移
        if ((anchor & BOTTOM) != 0) {
            dy -= h;
        } else if ((anchor & VCENTER) != 0) {
            dy -= h / 2;
        } else if ((anchor & BASELINE) != 0) {
            // baseline: 不调整，y 指向图片顶部
        }

        g.drawImage(img, dx, dy, null);
    }

    // ── drawRegion ──

    /**
     * 绘制图片的指定区域。
     * transform 始终为 0（本项目不使用旋转/镜像）。
     *
     * 使用 Graphics2D.drawImage 的 10 参数版本：
     *   g.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, observer)
     *
     * 目标矩形 (dx1, dy1) → (dx1+sw, dy1+sh)，再根据 anchor 调整。
     * 源矩形 (sx, sy) → (sx+sw, sy+sh)。
     */
    public static void drawRegion(Graphics2D g, BufferedImage img,
                                  int sx, int sy, int sw, int sh,
                                  int transform, int dx, int dy, int anchor) {
        if (img == null || sw <= 0 || sh <= 0) {
            return;
        }

        int dx1 = dx;
        int dy1 = dy;

        // 水平偏移
        if ((anchor & HCENTER) != 0) {
            dx1 -= sw / 2;
        } else if ((anchor & RIGHT) != 0) {
            dx1 -= sw;
        }

        // 垂直偏移
        if ((anchor & BOTTOM) != 0) {
            dy1 -= sh;
        } else if ((anchor & VCENTER) != 0) {
            dy1 -= sh / 2;
        }

        int dx2 = dx1 + sw;
        int dy2 = dy1 + sh;
        int sx2 = sx + sw;
        int sy2 = sy + sh;

        g.drawImage(img, dx1, dy1, dx2, dy2,
                    sx, sy, sx2, sy2, null);
    }

    // ── 字体解码 ──

    /**
     * 将 J2ME 字体参数映射为 java.awt.Font。
     *
     * face:  64 → "SansSerif"（AWT 中没有 proportional face 概念，统用 SansSerif）
     * style: 0=PLAIN, 1=BOLD, 2=ITALIC
     * size:  8=SMALL→14px, 0=MEDIUM→18px, 16=LARGE→24px
     */
    // Font 不可变，全项目组合有限（face∈{64}, style∈{0,1,2}, size∈{8,0,16}）。
    // TouchControls 每帧调用 decodeJ2meFont(64,1,8)，必须缓存避免每帧 new Font()（极贵）。
    private static final Map<Integer, Font> fontCache = new HashMap<Integer, Font>();

    // 中文字体：SansSerif 的中文会回退到宋体点阵，小字号边缘发虚。
    // 优先用微软雅黑（矢量 + ClearType），其次黑体，都比宋体清晰得多。
    private static final String UI_FONT_NAME = pickUiFont();

    private static String pickUiFont() {
        String[] available = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        for (String name : available) {
            if ("Microsoft YaHei".equalsIgnoreCase(name) || "微软雅黑".equals(name)) return name;
        }
        for (String name : available) {
            if ("Microsoft YaHei UI".equalsIgnoreCase(name)) return name;
        }
        for (String name : available) {
            if ("SimHei".equalsIgnoreCase(name) || "黑体".equals(name)) return name;
        }
        return "SansSerif";
    }

    public static Font decodeJ2meFont(int face, int style, int size) {
        int key = ((face & 0xFF) << 16) | ((style & 0xFF) << 8) | (size & 0xFF);
        Font f = fontCache.get(key);
        if (f != null) {
            return f;
        }

        String name = UI_FONT_NAME;

        int awtStyle;
        switch (style) {
            case 1:  awtStyle = Font.BOLD;  break;
            case 2:  awtStyle = Font.ITALIC; break;
            default: awtStyle = Font.PLAIN; break;
        }

        int px;
        switch (size) {
            case 8:  px = 14; break;  // SMALL（原 11px）
            case 16: px = 24; break;  // LARGE（原 18px）
            default: px = 18; break;  // MEDIUM（原 14px，size=0）
        }

        f = new Font(name, awtStyle, px);
        fontCache.put(key, f);
        return f;
    }

    // ── 字体度量辅助方法（替代 J2ME Font.getHeight()/stringWidth()） ──

    private static final java.awt.Canvas fontCanvas = new java.awt.Canvas();

    /** 返回字体的行高（ascent + descent），对应 J2ME Font.getHeight() */
    public static int fontHeight(Font f) {
        return fontCanvas.getFontMetrics(f).getHeight();
    }

    /** 返回指定字体渲染字符串的宽度，对应 J2ME Font.stringWidth() */
    public static int stringWidth(Font f, String s) {
        if (s == null) return 0;
        return fontCanvas.getFontMetrics(f).stringWidth(s);
    }
}
