package honor;

import honor.core.GameEngine;
import honor.core.PointerIds;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

/**
 * J2ME GameCanvas 适配层 —— 替代 javax.microedition.lcdui.game.GameCanvas。
 * 继承 JPanel，提供双缓冲和输入事件转发。
 */
public class GamePanel extends JPanel {

    private static final int PANEL_W = 480;
    private static final int PANEL_H = 800;

    private BufferedImage backbuffer;
    private Graphics2D currentGraphics;
    private GameEngine engine;

    public GamePanel() {
        setPreferredSize(new Dimension(PANEL_W, PANEL_H));
        setFocusable(true);

        // 鼠标事件
        MouseHandler mouseHandler = new MouseHandler();
        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);

        // 键盘事件
        KeyHandler keyHandler = new KeyHandler();
        addKeyListener(keyHandler);

        // 焦点事件
        addFocusListener(new FocusHandler());
    }

    /**
     * 注入 GameEngine。
     */
    public void setEngine(GameEngine engine) {
        this.engine = engine;
    }

    /**
     * 获取后备缓冲的 Graphics2D。
     * 缓冲为 null 或尺寸变化时重建。
     */
    public Graphics2D acquireGraphics() {
        int w = getWidth();
        int h = getHeight();
        if (w <= 0) w = PANEL_W;
        if (h <= 0) h = PANEL_H;

        if (backbuffer == null || backbuffer.getWidth() != w || backbuffer.getHeight() != h) {
            if (currentGraphics != null) {
                currentGraphics.dispose();
                currentGraphics = null;
            }
            // 不透明缓冲：让 LCD 亚像素文本抗锯齿生效（ARGB 透明表面会退化为灰度）
            backbuffer = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        }

        // 释放上一帧的 Graphics2D，防止资源泄漏导致 GC 卡顿
        if (currentGraphics != null) {
            currentGraphics.dispose();
        }

        currentGraphics = backbuffer.createGraphics();
        // LCD 亚像素渲染（Windows ClearType），中文/小字号最清晰
        currentGraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                          RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        currentGraphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                          RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        currentGraphics.setRenderingHint(RenderingHints.KEY_TEXT_LCD_CONTRAST, 160);
        return currentGraphics;
    }

    /**
     * 触发重绘。
     */
    public void flushGraphics() {
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backbuffer != null) {
            g.drawImage(backbuffer, 0, 0, null);
        }
    }

    // ── 事件处理器（内部类） ──

    private class MouseHandler implements MouseListener, MouseMotionListener {

        @Override
        public void mousePressed(MouseEvent e) {
            if (engine == null) return;
            PointerIds.setCurrent(0);
            engine.handlePointerPressed(e.getX(), e.getY());
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (engine == null) return;
            engine.handlePointerReleased(e.getX(), e.getY());
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (engine == null) return;
            engine.handlePointerDragged(e.getX(), e.getY());
        }

        @Override public void mouseClicked(MouseEvent e) {}
        @Override public void mouseEntered(MouseEvent e) {}
        @Override public void mouseExited(MouseEvent e) {}
        @Override public void mouseMoved(MouseEvent e) {}
    }

    private class KeyHandler implements KeyListener {

        @Override
        public void keyPressed(KeyEvent e) {
            if (engine == null) return;
            engine.handleKeyPressed(e.getKeyCode());
        }

        @Override
        public void keyReleased(KeyEvent e) {
            if (engine == null) return;
            engine.handleKeyReleased(e.getKeyCode());
        }

        @Override
        public void keyTyped(KeyEvent e) {
        }
    }

    private class FocusHandler implements FocusListener {

        @Override
        public void focusGained(FocusEvent e) {
            if (engine == null) return;
            engine.handleShowNotify();
        }

        @Override
        public void focusLost(FocusEvent e) {
            if (engine == null) return;
            engine.handleHideNotify();
        }
    }
}
