package honor;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * J2ME Sprite 适配层 —— 替代 javax.microedition.lcdui.game.Sprite。
 * 基于 spritesheet 的帧动画。
 */
public class Sprite {

    private BufferedImage sheet;
    private int frameW, frameH;
    private int cols;          // sheetWidth / frameW
    private int currentFrame;
    private int refX, refY;    // reference pixel 在屏幕上的位置
    private int refPx, refPy;  // reference pixel 在帧内的偏移

    /**
     * @param sheet  spritesheet 图片
     * @param frameW 每帧宽度
     * @param frameH 每帧高度
     */
    public Sprite(BufferedImage sheet, int frameW, int frameH) {
        this.sheet = sheet;
        this.frameW = frameW;
        this.frameH = frameH;
        this.cols = sheet.getWidth() / frameW;
        this.currentFrame = 0;
    }

    /**
     * 设置 reference pixel 在帧内的偏移。
     */
    public void defineReferencePixel(int x, int y) {
        this.refPx = x;
        this.refPy = y;
    }

    /**
     * 设置 reference pixel 在屏幕上的位置（即绘制位置）。
     */
    public void setRefPixelPosition(int x, int y) {
        this.refX = x;
        this.refY = y;
    }

    /**
     * 切换到第 n 帧。
     */
    public void setFrame(int n) {
        this.currentFrame = n;
    }

    /**
     * 在 refPixel 位置绘制当前帧。
     * 源矩形: (col×frameW, row×frameH) → (col×frameW+frameW, row×frameH+frameH)
     * 目标矩形: (refX-refPx, refY-refPy) → (refX-refPx+frameW, refY-refPy+frameH)
     */
    public void paint(Graphics2D g) {
        int col = currentFrame % cols;
        int row = currentFrame / cols;

        int sx = col * frameW;
        int sy = row * frameH;

        int dx = refX - refPx;
        int dy = refY - refPy;

        g.drawImage(sheet,
                    dx, dy, dx + frameW, dy + frameH,
                    sx, sy, sx + frameW, sy + frameH,
                    null);
    }

    /**
     * 空实现 —— 本项目不使用旋转/镜像。
     */
    public void setTransform(int t) {
        // 空实现
    }

    // ── getters ──

    public int getFrameW() {
        return frameW;
    }

    public int getFrameH() {
        return frameH;
    }

    public int getCurrentFrame() {
        return currentFrame;
    }

    /**
     * 返回 spritesheet 中帧的总数。
     */
    public int getRawFrameCount() {
        int rows = sheet.getHeight() / frameH;
        return cols * rows;
    }
}
