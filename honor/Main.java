package honor;

import honor.core.GameEngine;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("王者荣耀 MOBA");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);

            GamePanel panel = new GamePanel();
            GameEngine engine = new GameEngine(panel);
            panel.setEngine(engine);

            frame.add(panel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            // 启动游戏循环
            new Thread(engine, "GameLoop").start();
        });
    }
}
