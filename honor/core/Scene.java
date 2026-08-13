package honor.core;

import java.awt.Graphics2D;

public interface Scene {
    int STAY = -1;
    int MENU = 0;
    int SELECT = 1;
    int BATTLE = 2;
    int QUIT = 3;
    int ONLINE = 4;
    int ONLINE_BATTLE = 5;

    void update(KeyInput var1, int var2);

    void render(Graphics2D var1, int var2, int var3, int var4);

    int nextScene();
}