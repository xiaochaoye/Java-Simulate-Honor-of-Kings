package honor.core;

import honor.G2D;
import honor.GamePanel;
import honor.Img;
import honor.battle.BattleWorld;
import honor.entity.Hero;
import honor.map.TileMap;
import honor.net.OnlineClient;
import honor.net.OnlineFrame;
import honor.net.OnlineGame;
import honor.ui.MenuScene;
import honor.ui.OnlineScene;
import honor.ui.SelectScene;
import honor.ui.UiKit;
import honor.util.Log;
import honor.util.SettingsStore;
import honor.util.Sfx;
import honor.util.StatsStore;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class GameEngine implements Runnable {
    private static final int LOGIC_MS = 66;   // 游戏逻辑 15 UPS
    private static final int FRAME_MS = 33;   // 渲染 ~30 FPS（每帧 33ms 预算，避免战斗场景掉帧卡顿）
    private final GamePanel panel;
    private final KeyInput input = new KeyInput();
    private final TouchControls touch = new TouchControls();
    private final SettingsStore settings = SettingsStore.get();
    private Thread thread;
    private volatile boolean running;
    private volatile boolean paused;
    private final MenuScene menu = new MenuScene();
    private final SelectScene select = new SelectScene();
    private final OnlineClient onlineClient = new OnlineClient();
    private final OnlineScene online;
    private int state;
    private TileMap map;
    private BattleWorld battle;
    private int camX;
    private int camY;
    private int prevCamX;    // 上一逻辑帧的摄像机位置（用于插值）
    private int prevCamY;
    private long lastLogicTime;  // 上次逻辑更新的时间
    private int tick;
    private int battleTick;
    private boolean statsRecorded;
    private boolean battlePaused;
    private int pauseCursor;
    private boolean battleDetails;
    private OnlineGame onlineGame;
    private KeyInput[] onlineInputs;
    private boolean onlineResultSent;
    private int onlineStall;
    private boolean onlineLeaveConfirm;
    private BufferedImage deathVeil;
    private int veilW;
    private int veilH;
    private String loadError;
    private volatile boolean mapLoadStarted;
    private final java.awt.Font font;
    private final boolean[] keyDown = new boolean[256];

    private static final int UP = 2, DOWN = 64, LEFT = 4, RIGHT = 32, FIRE = 256;

    public GameEngine(GamePanel panel) {
        this.online = new OnlineScene(this.onlineClient);
        this.state = 0;
        this.font = G2D.decodeJ2meFont(64, 1, 8);
        this.panel = panel;
    }

    private int getKeyStates() {
        int states = 0;
        if (keyDown[KeyEvent.VK_UP]    || keyDown['W'] || keyDown['w']) states |= UP;
        if (keyDown[KeyEvent.VK_DOWN]  || keyDown['S'] || keyDown['s']) states |= DOWN;
        if (keyDown[KeyEvent.VK_LEFT]  || keyDown['A'] || keyDown['a']) states |= LEFT;
        if (keyDown[KeyEvent.VK_RIGHT] || keyDown['D'] || keyDown['d']) states |= RIGHT;
        if (keyDown[KeyEvent.VK_SPACE] || keyDown[KeyEvent.VK_ENTER])   states |= FIRE;
        return states;
    }

    private int getGameAction(int keyCode) {
        if (keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_KP_UP)    return 1;
        if (keyCode == KeyEvent.VK_DOWN || keyCode == KeyEvent.VK_KP_DOWN) return 6;
        if (keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_KP_LEFT) return 2;
        if (keyCode == KeyEvent.VK_RIGHT || keyCode == KeyEvent.VK_KP_RIGHT) return 5;
        if (keyCode == KeyEvent.VK_SPACE || keyCode == KeyEvent.VK_ENTER) return 8;
        return 0;
    }

    public void start() {
        if (this.running) {
            this.paused = false;
        } else {
            this.running = true;
            this.paused = false;
            this.ensureMapAsync();
            int var1 = panel.getWidth();
            int var2 = panel.getHeight();
            if (var1 > 0 && var2 > 0) {
                this.settings.configureForDevice(var1, var2, true);
                this.touch.ensureLayout(var1, var2);
            }

            this.thread = new Thread(this);
            this.thread.start();
        }
    }

    private void ensureMapAsync() {
        if (this.map == null && this.loadError == null && !this.mapLoadStarted) {
            this.mapLoadStarted = true;
            (new Thread(new Runnable() {
                public void run() {
                    GameEngine.this.loadMap();
                }
            })).start();
        }
    }

    public void pause() {
        this.paused = true;
    }

    public void stop() {
        this.running = false;
        this.onlineClient.close();
        Thread var1 = this.thread;
        if (var1 != null) {
            try {
                var1.join();
            } catch (InterruptedException var3) {
            }
        }

        this.thread = null;
    }

    public void run() {
        Log.info("GameEngine loop start");
        this.lastLogicTime = System.currentTimeMillis();

        while(this.running) {
            long now = System.currentTimeMillis();

            if (this.paused) {
                // 暂停时降低 CPU 占用
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
                continue;
            }

            // 固定时间步长更新游戏逻辑（15 UPS）
            if (now - this.lastLogicTime >= LOGIC_MS) {
                this.prevCamX = this.camX;
                this.prevCamY = this.camY;
                this.updateFrame();
                this.lastLogicTime += LOGIC_MS;
                // 防止螺旋式追赶（长时间暂停/卡顿后不连续追帧）
                if (now - this.lastLogicTime > LOGIC_MS * 3) {
                    this.lastLogicTime = now;
                }
            }

            // 每帧渲染（~30 FPS）
            this.renderFrame();

            long elapsed = System.currentTimeMillis() - now;
            long sleepTime = FRAME_MS - elapsed;
            if (sleepTime > 0L) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                }
            }
        }

        Log.info("GameEngine loop end");
    }

    public void handleKeyPressed(int var1) {
        this.keyDown[var1] = true;
        try {
            int var2 = this.getGameAction(var1);
            if (var2 != 0) {
                this.input.onGameAction(var2, true);
            }
        } catch (IllegalArgumentException var3) {
        }

        this.input.onKeyCode(var1, true);
        if (this.input.isPressed(256) || this.input.isHeld(256)) {
            System.exit(0);
        }

    }

    public void handleKeyReleased(int var1) {
        this.keyDown[var1] = false;
        try {
            int var2 = this.getGameAction(var1);
            if (var2 != 0) {
                this.input.onGameAction(var2, false);
            }
        } catch (IllegalArgumentException var3) {
        }

        this.input.onKeyCode(var1, false);
    }

    public void handleKeyRepeated(int var1) {
        try {
            int var2 = this.getGameAction(var1);
            if (var2 != 0) {
                this.input.onGameAction(var2, true);
            }
        } catch (IllegalArgumentException var3) {
        }

        this.input.onKeyCode(var1, true);
    }

    public void handlePointerPressed(int var1, int var2) {
        int var3 = PointerIds.current();
        if (this.state == 0) {
            this.menu.onPointer(var1, var2, panel.getWidth(), panel.getHeight());
        } else if (this.state == 1) {
            this.select.onPointer(var1, var2, panel.getWidth(), panel.getHeight(), this.input);
        } else if (this.state == 4) {
            if (!this.online.onPointer(var1, var2, panel.getWidth(), panel.getHeight(), this.input)) {
                this.input.touchAction(16, true);
                this.input.touchAction(16, false);
            }

        } else {
            if (this.state == 5 && this.onlineGame != null && this.onlineGame.localSlot < 0 && this.battle != null) {
                this.touch.ensureLayout(panel.getWidth(), panel.getHeight());
                if (this.battleDetails) {
                    this.battleDetails = false;
                    Sfx.play("ui_click");
                    return;
                }

                if (this.onlineLeaveConfirm) {
                    this.handleOnlineLeaveConfirmPointer(var1, var2);
                    return;
                }

                int var4 = this.touch.pointerSpectator(var3, var1, var2, this.input);
                if (var4 == 1) {
                    this.exitOnlineBattle(false);
                    return;
                }

                if (var4 == 2) {
                    this.battleDetails = true;
                    Sfx.play("ui_click");
                    return;
                }

                int var5 = this.battle.heroAvatarAt(var1, var2);
                if (var5 >= 0) {
                    this.battle.setViewHero(var5);
                    this.updateCamera();
                    Sfx.play("ui_click");
                    return;
                }
            }

            if ((this.state == 2 || this.state == 5) && this.settings.isTouchMode()) {
                this.touch.ensureLayout(panel.getWidth(), panel.getHeight());
                this.touch.setShopMode(this.battle != null && this.battle.isShopOpen(), this.input);
                if (this.battleDetails) {
                    this.battleDetails = false;
                    Sfx.play("ui_click");
                    return;
                }

                if (this.onlineLeaveConfirm) {
                    this.handleOnlineLeaveConfirmPointer(var1, var2);
                    return;
                }

                if (this.battlePaused) {
                    int var13 = panel.getWidth();
                    int var14 = panel.getHeight();
                    int var6 = this.pausePanelWidth(var13);
                    short var7 = 140;
                    int var8 = (var13 - var6) / 2;
                    int var9 = this.pausePanelY(var14, var7);
                    int var10 = var9 + 34;
                    int var11 = (var2 - var10) / 24;
                    if (var1 >= var8 && var1 <= var8 + var6 && var2 >= var10 && var11 >= 0 && var11 < 3) {
                        this.pauseCursor = var11;
                        this.activatePauseItem();
                    }

                    return;
                }

                if (this.battle != null && this.battle.getResult() != 0) {
                    this.input.touchAction(16, true);
                    this.input.touchAction(16, false);
                    return;
                }

                if (this.state == 5 && this.onlineGame != null && this.onlineGame.localSlot < 0) {
                    return;
                }

                if (this.battle != null) {
                    int var12 = this.touch.skillUpgradeAt(var1, var2, this.battle.getLocalHero());
                    if (var12 >= 0) {
                        if (this.state == 5) {
                            this.battle.requestOnlineUpgrade(this.input, var12);
                        } else {
                            this.battle.upgradeLocalSkill(var12);
                        }

                        return;
                    }
                }

                this.touch.pointerPressed(var3, var1, var2, this.input, this.battle == null ? null : this.battle.getLocalHero());
            }

        }
    }

    public void handlePointerDragged(int var1, int var2) {
        if ((this.state == 2 || this.state == 5) && this.settings.isTouchMode()) {
            this.touch.ensureLayout(panel.getWidth(), panel.getHeight());
            this.touch.pointerDragged(PointerIds.current(), var1, var2, this.input);
        }

    }

    public void handlePointerReleased(int var1, int var2) {
        if ((this.state == 2 || this.state == 5) && this.settings.isTouchMode()) {
            this.touch.pointerReleased(PointerIds.current(), var1, var2, this.input);
        } else if (this.state == 0) {
            this.menu.onPointerReleased();
            this.input.releaseTouchActions();
        } else {
            this.input.releaseTouchActions();
        }

    }

    public void handleHideNotify() {
        this.touch.cancelAll(this.input);
        this.input.clearAll();
        this.pause();
    }

    public void handleShowNotify() {
        this.touch.cancelAll(this.input);
        this.input.clearAll();
        this.start();
    }

    private void loadMap() {
        try {
            this.map = new TileMap("/res/map/lane1.bin", "/res/img/tiles.png", "/res/map/spots.bin");
            this.loadError = null;
        } catch (IOException var2) {
            this.loadError = var2.getMessage();
            Log.error("loadMap: " + this.loadError);
        }

    }

    private void updateFrame() {
        this.input.beginFrame(this.getKeyStates());
        if (this.loadError == null) {
            if (this.state == 2) {
                this.updateBattle();
            } else if (this.state == 5) {
                this.updateOnlineBattle();
            } else if (this.state == 4) {
                this.online.update(this.input, this.tick);
                if (this.online.consumeAuthRequest()) {
                    this.showOnlineAuthDialog(this.online.takeAuthHint());
                }

                this.switchScene(this.online.nextScene());
            } else if (this.state == 1) {
                this.select.update(this.input, this.tick);
                this.switchScene(this.select.nextScene());
            } else {
                this.menu.update(this.input, this.tick);
                this.switchScene(this.menu.nextScene());
            }
        }

        ++this.tick;
        this.input.endFrame();
    }

    private void updateBattle() {
        if (this.battle == null) {
            this.switchScene(0);
        } else if (this.battle.getResult() != 0) {
            this.battlePaused = false;
            this.battleDetails = false;
            if (!this.statsRecorded) {
                this.recordBattleStats();
                this.statsRecorded = true;
            }

            if (!this.input.isPressed(16) && !this.input.isPressed(2048)) {
                this.updateCamera();
            } else {
                this.battle = null;
                this.switchScene(0);
            }
        } else if (!this.battleDetails) {
            if (this.input.isPressed(4096)) {
                this.battleDetails = true;
                this.battlePaused = false;
                Sfx.play("ui_click");
                this.touch.cancelAll(this.input);
                this.input.clearAll();
                this.updateCamera();
            } else if (this.input.isPressed(2048)) {
                this.battlePaused = !this.battlePaused;
                this.pauseCursor = 0;
                Sfx.play("ui_click");
                this.touch.cancelAll(this.input);
                this.input.clearAll();
                this.updateCamera();
            } else if (this.battlePaused) {
                if (this.input.isPressed(1)) {
                    this.pauseCursor = (this.pauseCursor + 2) % 3;
                    Sfx.play("ui_click");
                } else if (this.input.isPressed(2)) {
                    this.pauseCursor = (this.pauseCursor + 1) % 3;
                    Sfx.play("ui_click");
                }

                if (this.input.isPressed(16)) {
                    this.activatePauseItem();
                    if (this.battle == null) {
                        return;
                    }
                }

                this.updateCamera();
            } else {
                this.battle.update(this.input, this.battleTick);
                ++this.battleTick;
                this.updateCamera();
            }
        } else {
            if (this.input.isPressed(2048) || this.input.isPressed(16) || this.input.isPressed(4096)) {
                this.battleDetails = false;
                Sfx.play("ui_click");
                this.touch.cancelAll(this.input);
                this.input.clearAll();
            }

            this.updateCamera();
        }
    }

    private void updateOnlineBattle() {
        this.onlineClient.pump();
        if (this.battle != null && this.onlineGame != null) {
            if (this.onlineClient.consumeRoomClosed()) {
                String var12 = this.onlineClient.getError().length() > 0 ? this.onlineClient.getError() : "房主已解散房间";
                this.exitOnlineBattle(true);
                this.online.setStatus(var12);
            } else if (!this.onlineClient.isConnected()) {
                String var11 = this.onlineClient.getError().length() > 0 ? this.onlineClient.getError() : "连接已断开";
                this.exitOnlineBattle(true);
                this.online.setStatus(var11);
            } else {
                int var1 = this.onlineClient.consumeGameOver();
                if (var1 != -2) {
                    this.battle.applyRemoteResult(var1);
                    this.onlineResultSent = true;
                }

                if (!this.onlineClient.hasPendingGame() || !this.startOnlineBattle()) {
                    boolean var2 = this.onlineGame.localSlot >= 0;
                    boolean var3 = this.battle.getResult() != 0;
                    if (var2) {
                        if (!var3 && !this.battleDetails && !this.onlineLeaveConfirm) {
                            this.battle.updateOnlineLocalUi(this.input);
                        }

                        this.onlineClient.sendInput(this.input, this.battle.onlineUiSuppressMask());
                    }

                    int var4 = 1;
                    int var5 = this.onlineClient.queuedFrames();
                    if (this.onlineClient.isReplaying() || var5 > 6) {
                        var4 = var5 > 30 ? 30 : var5;
                    }

                    int var6 = 0;

                    for(int var7 = 0; var7 < var4; ++var7) {
                        OnlineFrame var8 = this.onlineClient.pollFrame();
                        if (var8 == null) {
                            break;
                        }

                        int var9 = this.onlineInputs.length;

                        for(int var10 = 0; var10 < var9; ++var10) {
                            this.onlineInputs[var10].setNetworkFrame(var8.held[var10], var8.pressed[var10], var8.axisX[var10], var8.axisY[var10], var8.cmd[var10]);
                        }

                        this.battle.updateOnline(this.onlineInputs, var8.humanMask, var8.tick);
                        this.battleTick = var8.tick + 1;
                        ++var6;
                    }

                    this.onlineStall = var6 == 0 && !var3 ? this.onlineStall + 1 : 0;
                    this.updateCamera();
                    var3 = this.battle.getResult() != 0;
                    if (var3 && !this.onlineResultSent) {
                        int var14 = this.battle.getResult() == 1 ? 0 : 1;
                        this.onlineClient.send("RESULT|" + var14);
                        this.onlineResultSent = true;
                    }

                    if (var3 && !this.statsRecorded) {
                        this.recordOnlineStats();
                        this.statsRecorded = true;
                    }

                    if (this.battleDetails) {
                        if (this.input.isPressed(2048) || this.input.isPressed(16) || this.input.isPressed(4096)) {
                            this.battleDetails = false;
                            Sfx.play("ui_click");
                            this.touch.cancelAll(this.input);
                            this.input.clearAll();
                        }

                    } else if (!var3 && this.input.isPressed(4096)) {
                        this.battleDetails = true;
                        this.onlineLeaveConfirm = false;
                        Sfx.play("ui_click");
                        this.touch.cancelAll(this.input);
                        this.input.clearAll();
                    } else if (this.onlineLeaveConfirm) {
                        if (this.input.isPressed(2048)) {
                            this.onlineLeaveConfirm = false;
                            Sfx.play("ui_click");
                            this.touch.cancelAll(this.input);
                            this.input.clearAll();
                        } else if (this.input.isPressed(16)) {
                            this.onlineLeaveConfirm = false;
                            if (this.onlineClient.isRoomOwner()) {
                                this.onlineClient.send("DISBAND");
                                this.exitOnlineBattle(true);
                            } else {
                                this.exitOnlineBattle(false);
                            }
                        }

                    } else {
                        if (this.input.isPressed(16) || this.input.isPressed(2048)) {
                            if (var3) {
                                this.exitOnlineBattle(true);
                            } else if (this.input.isPressed(2048)) {
                                if (this.onlineClient.isRoomOwner()) {
                                    this.onlineLeaveConfirm = true;
                                    Sfx.play("ui_click");
                                    this.touch.cancelAll(this.input);
                                    this.input.clearAll();
                                } else {
                                    this.exitOnlineBattle(false);
                                }
                            }
                        }

                    }
                }
            }
        } else {
            this.exitOnlineBattle(false);
        }
    }

    private void exitOnlineBattle(boolean var1) {
        this.battle = null;
        this.onlineGame = null;
        this.onlineStall = 0;
        this.battleDetails = false;
        this.onlineLeaveConfirm = false;
        if (!var1) {
            this.onlineClient.send("LEAVE");
        }

        this.switchScene(4);
    }

    private void recordOnlineStats() {
        Hero var1 = this.battle.getLocalHero();
        if (var1 != null && this.onlineGame != null && this.onlineGame.localSlot >= 0) {
            if (this.battle.getResult() == 3) {
                Sfx.play("ui_click");
            } else {
                boolean var2 = this.battle.getResult() == 1 == (var1.team == 0);
                StatsStore.get().recordMatch(var2, var1.heroKills, var1.deaths, var1.minionKills);
                Sfx.play(var2 ? "win" : "defeat");
            }
        }
    }

    public void onOnlineAuth(String var1, String var2, boolean var3, boolean var4) {
        this.online.submitAuth(var1, var2, var3, var4);
    }

    private void activatePauseItem() {
        if (this.pauseCursor == 0) {
            this.battlePaused = false;
            Sfx.play("ui_ok");
        } else if (this.pauseCursor == 1) {
            this.battlePaused = false;
            this.battleDetails = true;
            Sfx.play("ui_ok");
        } else {
            this.battle = null;
            this.battlePaused = false;
            this.battleDetails = false;
            this.switchScene(0);
        }

    }

    private void recordBattleStats() {
        Hero var1 = this.battle.getLocalHero();
        if (var1 != null) {
            boolean var2 = this.battle.getResult() == 1;
            StatsStore.get().recordMatch(var2, var1.heroKills, var1.deaths, var1.minionKills);
            Sfx.play(var2 ? "win" : "defeat");
        }
    }

    private void switchScene(int var1) {
        if (var1 != -1) {
            if (var1 == 3) {
                System.exit(0);
            } else {
                if (var1 == 1) {
                    this.select.enter(this.menu.getMode(), this.tick);
                } else if (var1 == 2) {
                    if (!this.startBattle()) {
                        return;
                    }
                } else if (var1 == 4) {
                    this.online.enter();
                    if (this.online.needsAuth()) {
                        this.showOnlineAuthDialog(this.online.takeAuthHint());
                    }
                } else if (var1 == 5) {
                    if (!this.startOnlineBattle()) {
                        return;
                    }
                } else if (var1 == 0) {
                    this.menu.reset();
                }

                this.state = var1;
                this.touch.cancelAll(this.input);
                this.input.clearAll();
            }
        }
    }

    private boolean startBattle() {
        MatchConfig var1 = this.select.getConfig();
        if (var1 != null && this.map != null) {
            try {
                this.battle = new BattleWorld(this.map, var1);
                this.battleTick = 0;
                this.statsRecorded = false;
                this.battlePaused = false;
                this.battleDetails = false;
                this.onlineLeaveConfirm = false;
                this.pauseCursor = 0;
                this.updateCamera();
                int var2 = panel.getWidth();
                int var3 = panel.getHeight();
                if (var2 > 0 && var3 > 0) {
                    this.touch.ensureLayout(var2, var3);
                }

                return true;
            } catch (IOException var4) {
                this.loadError = var4.getMessage();
                Log.error("startBattle: " + this.loadError);
                return false;
            }
        } else {
            return false;
        }
    }

    private void drawOnlineLeaveConfirm(Graphics2D var1, int var2, int var3) {
        int var4 = this.pausePanelWidth(var2);
        int var5 = UiKit.confirmDialogHeight();
        int var6 = (var2 - var4) / 2;
        int var7 = this.pausePanelY(var3, var5);
        UiKit.drawConfirmDialog(var1, var2, "主机离开后", "房间对局将会结束", var6, var7, var4, var5);
    }

    private void handleOnlineLeaveConfirmPointer(int var1, int var2) {
        int var3 = panel.getWidth();
        int var4 = panel.getHeight();
        int var5 = this.pausePanelWidth(var3);
        int var6 = UiKit.confirmDialogHeight();
        int var7 = (var3 - var5) / 2;
        int var8 = this.pausePanelY(var4, var6);
        int var9 = UiKit.hitConfirmDialog(var1, var2, var7, var8, var5, var6);
        if (var9 == 1) {
            this.onlineLeaveConfirm = false;
            if (this.onlineClient != null && this.onlineClient.isRoomOwner()) {
                this.onlineClient.send("DISBAND");
                this.exitOnlineBattle(true);
            } else {
                this.exitOnlineBattle(false);
            }

            this.touch.cancelAll(this.input);
            this.input.clearAll();
        } else if (var9 == 2) {
            this.onlineLeaveConfirm = false;
            Sfx.play("ui_click");
            this.touch.cancelAll(this.input);
            this.input.clearAll();
        }

    }

    private void updateCamera() {
        Hero var1 = this.battle.getLocalHero();
        if (var1 != null) {
            int var2 = panel.getWidth();
            int var3 = panel.getHeight();
            int var4 = IsoMath.toScreenX(var1.x, var1.y);
            int var5 = IsoMath.toScreenY(var1.x, var1.y);
            this.camX = var4 - var2 / 2;
            this.camY = var5 - var3 / 2 - 20;
        }
    }

    /** 获取逻辑帧之间的插值摄像机 X（用于平滑滚动） */
    private int interpCamX() {
        long now = System.currentTimeMillis();
        long elapsed = now - this.lastLogicTime;
        if (elapsed <= 0 || elapsed >= LOGIC_MS) return this.camX;
        float t = (float) elapsed / LOGIC_MS;
        return this.prevCamX + Math.round((this.camX - this.prevCamX) * t);
    }

    /** 获取逻辑帧之间的插值摄像机 Y */
    private int interpCamY() {
        long now = System.currentTimeMillis();
        long elapsed = now - this.lastLogicTime;
        if (elapsed <= 0 || elapsed >= LOGIC_MS) return this.camY;
        float t = (float) elapsed / LOGIC_MS;
        return this.prevCamY + Math.round((this.camY - this.prevCamY) * t);
    }

    private void renderFrame() {
        Graphics2D var1 = panel.acquireGraphics();
        int var2 = panel.getWidth();
        int var3 = panel.getHeight();
        if (this.loadError != null) {
            G2D.setColor(var1, 1052696);
            var1.fillRect(0, 0, var2, var3);
            G2D.setColor(var1, 16733525);
            var1.setFont(this.font);
            G2D.drawString(var1, "LOAD ERROR", var2 / 2, var3 / 2 - 10, 17);
            G2D.drawString(var1, this.loadError, var2 / 2, var3 / 2 + 8, 17);
            panel.flushGraphics();
        } else if (this.map == null) {
            G2D.setColor(var1, 1052696);
            var1.fillRect(0, 0, var2, var3);
            G2D.setColor(var1, 13421789);
            var1.setFont(this.font);
            G2D.drawString(var1, "加载中...", var2 / 2, var3 / 2 - 6, 17);
            panel.flushGraphics();
        } else {
            if (var2 > 0 && var3 > 0) {
                this.settings.configureForDevice(var2, var3, true);
                if (this.settings.isTouchMode()) {
                    this.touch.ensureLayout(var2, var3);
                }
            }

            if ((this.state == 2 || this.state == 5) && this.battle != null) {
                int rCamX = this.interpCamX();
                int rCamY = this.interpCamY();
                this.drawBackdrop(var1, var2, var3);
                if (!this.map.renderBackground(var1, rCamX, rCamY)) {
                    this.map.render(var1, rCamX, rCamY, var2, var3);
                }

                this.battle.renderUnits(var1, rCamX, rCamY);
                if (this.battle.isLocalDead()) {
                    this.drawDeathGrayscale(var1, var2, var3);
                }

                this.battle.renderHud(var1, var2, var3, this.battleTick);
                if (this.settings.isTouchMode() && !this.battlePaused && !this.battleDetails && !this.onlineLeaveConfirm) {
                    this.touch.ensureLayout(var2, var3);
                    if (this.state == 5 && this.onlineGame != null && this.onlineGame.localSlot < 0) {
                        this.touch.renderSpectator(var1, this.settings);
                    } else if (this.state != 5 || this.onlineGame != null && this.onlineGame.localSlot >= 0) {
                        this.touch.setShopMode(this.battle.isShopOpen(), this.input);
                        this.touch.render(var1, this.settings, this.battle.getLocalHero());
                    }
                }

                if (this.battleDetails) {
                    this.battle.renderMatchDetails(var1, var2, var3);
                } else if (this.battlePaused) {
                    this.drawPauseOverlay(var1, var2, var3);
                } else if (this.onlineLeaveConfirm) {
                    this.drawOnlineLeaveConfirm(var1, var2, var3);
                }

                if (this.state == 5 && this.onlineGame != null && this.onlineGame.localSlot < 0 && !this.battleDetails) {
                    var1.setFont(UiKit.SMALL);
                    G2D.setColor(var1, 15254634);
                    G2D.drawString(var1, "观战  #" + this.onlineGame.roomId + "  点头像切视角", var2 / 2, 3, 17);
                }

                if (this.state == 5 && this.onlineStall > 7) {
                    var1.setFont(UiKit.SMALL);
                    G2D.setColor(var1, 16763989);
                    G2D.drawString(var1, "等待网络…", var2 / 2, var3 / 2 - 10, 17);
                }
            } else if (this.state == 1) {
                this.select.render(var1, var2, var3, this.tick);
            } else if (this.state == 4) {
                this.online.render(var1, var2, var3, this.tick);
            } else {
                this.menu.render(var1, var2, var3, this.tick);
            }

            panel.flushGraphics();
        }
    }

    private void drawDeathGrayscale(Graphics2D var1, int var2, int var3) {
        this.ensureDeathVeil(var2, var3);
        if (this.deathVeil != null) {
            G2D.drawImage(var1, this.deathVeil, 0, 0, 20);
        }

        G2D.setColor(var1, 3158064);

        for(int var4 = 0; var4 < var3; var4 += 3) {
            var1.drawLine(0, var4, var2 - 1, var4);
        }

    }

    private void ensureDeathVeil(int var1, int var2) {
        if (this.deathVeil == null || this.veilW != var1 || this.veilH != var2) {
            int[] var3 = new int[var1 * var2];

            for(int var4 = 0; var4 < var2; ++var4) {
                for(int var5 = 0; var5 < var1; ++var5) {
                    if ((var5 + var4 & 1) == 0) {
                        var3[var4 * var1 + var5] = -1721342362;
                    } else {
                        var3[var4 * var1 + var5] = -2009910477;
                    }
                }
            }

            try {
                this.deathVeil = Img.createRgb(var3, var1, var2, true);
            } catch (Exception var6) {
                this.deathVeil = null;
            }

            this.veilW = var1;
            this.veilH = var2;
        }
    }

    private void drawPauseOverlay(Graphics2D var1, int var2, int var3) {
        int var4 = this.pausePanelWidth(var2);
        short var5 = 140;
        int var6 = (var2 - var4) / 2;
        int var7 = this.pausePanelY(var3, var5);
        UiKit.panel(var1, var6, var7, var4, var5, 15254634);
        var1.setFont(this.font);
        G2D.setColor(var1, 15254634);
        G2D.drawString(var1, "暂 停", var2 / 2, var7 + 10, 17);
        String[] var8 = new String[]{"继续游戏", "对局详情", "返回主菜单"};

        for(int var9 = 0; var9 < 3; ++var9) {
            boolean var10 = var9 == this.pauseCursor;
            G2D.setColor(var1, var10 ? 16777215 : 8885420);
            String var11 = var10 ? "> " : "  ";
            G2D.drawString(var1, var11 + var8[var9], var2 / 2, var7 + 34 + var9 * 24, 17);
        }

        G2D.setColor(var1, 8885420);
        var1.setFont(UiKit.SMALL);
        G2D.drawString(var1, "#切换  5确认", var2 / 2, var7 + var5 - 18, 17);
        var1.setFont(this.font);
    }

    private boolean startOnlineBattle() {
        OnlineGame var1 = this.onlineClient.takePendingGame();
        if (var1 != null && this.map != null) {
            try {
                MatchConfig var2 = MatchConfig.fromRosters(var1.mode, var1.blue, var1.red);
                int var3 = var1.localSlot < 0 ? 0 : var1.localSlot;
                this.battle = new BattleWorld(this.map, var2, var3, true, var1.humanMask);
                this.onlineGame = var1;
                this.onlineInputs = new KeyInput[var2.teamSize * 2];

                for(int var4 = 0; var4 < this.onlineInputs.length; ++var4) {
                    this.onlineInputs[var4] = new KeyInput();
                    this.onlineInputs[var4].setNetworkFrame(0, 0, 0, 0, 0);
                }

                this.battleTick = 0;
                this.onlineResultSent = false;
                this.onlineStall = 0;
                this.statsRecorded = false;
                this.battlePaused = false;
                this.battleDetails = false;
                this.onlineLeaveConfirm = false;
                this.pauseCursor = 0;
                this.touch.cancelAll(this.input);
                this.input.clearAll();
                this.updateCamera();
                int var7 = panel.getWidth();
                int var5 = panel.getHeight();
                if (var7 > 0 && var5 > 0) {
                    this.touch.ensureLayout(var7, var5);
                }

                return true;
            } catch (Exception var6) {
                this.loadError = var6.getMessage();
                Log.error("startOnlineBattle: " + this.loadError);
                return false;
            }
        } else {
            return false;
        }
    }

    private int pausePanelWidth(int var1) {
        int var2 = var1 - 28;
        if (var2 > 200) {
            var2 = 200;
        }

        if (var2 < 140) {
            var2 = var1 - 16;
        }

        return var2;
    }

    private int pausePanelY(int var1, int var2) {
        int var3 = (var1 - var2) / 2 - 4;
        if (var3 < 4) {
            var3 = 4;
        }

        return var3;
    }

    private void drawBackdrop(Graphics2D var1, int var2, int var3) {
        byte var4 = 8;
        int var5 = var3 / var4;

        for(int var6 = 0; var6 < var4; ++var6) {
            int var7 = var6 * 255 / (var4 - 1);
            int var8 = 18 + var7 * 20 / 255;
            int var9 = 28 + var7 * 70 / 255;
            int var10 = 55 + var7 * 10 / 255;
            G2D.setColor(var1, var8 << 16 | var9 << 8 | var10);
            var1.fillRect(0, var6 * var5, var2, var5 + 1);
        }

    }

    private void showOnlineAuthDialog(String hint) {
        SwingUtilities.invokeLater(() -> {
            SettingsStore store = SettingsStore.get();
            String account = store.getAccount();
            String password = store.getPassword();
            boolean remember = store.isRememberAuth();
            String title = (hint != null && hint.length() > 0) ? hint : "联机账号";
            Object[] options = {"登录", "注册", "取消"};

            while (true) {
                JPanel loginPanel = new JPanel(new java.awt.GridLayout(3, 2, 5, 5));
                PlaceholderTextField accountField = new PlaceholderTextField(account, "英文+数字");
                PlaceholderPasswordField passwordField = new PlaceholderPasswordField(password, "最大长度为20");
                javax.swing.JCheckBox rememberBox = new javax.swing.JCheckBox("记住账号和密码", remember);
                loginPanel.add(new JLabel("账号:"));
                loginPanel.add(accountField);
                loginPanel.add(new JLabel("密码:"));
                loginPanel.add(passwordField);
                loginPanel.add(rememberBox);

                int result = JOptionPane.showOptionDialog(panel, loginPanel, title,
                    JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE,
                    null, options, options[0]);
                if (result != 0 && result != 1) {
                    return; // 取消
                }

                account = accountField.getText() != null ? accountField.getText().trim() : "";
                password = new String(passwordField.getPassword());
                remember = rememberBox.isSelected();
                String error = validateAuth(account, password);
                if (error != null) {
                    JOptionPane.showMessageDialog(panel, error, "输入有误", JOptionPane.WARNING_MESSAGE);
                    continue; // 保留已输入内容重新弹出，要求重输
                }

                boolean isRegister = (result == 1);
                store.setAuth(remember, account, password);
                onOnlineAuth(account, password, isRegister, remember);
                return;
            }
        });
    }

    private static String validateAuth(String account, String password) {
        if (account.length() == 0) {
            return "账号不能为空";
        }
        if (!isValidAccount(account)) {
            return "账号只能包含英文和数字";
        }
        if (password.length() == 0) {
            return "密码不能为空";
        }
        if (password.length() > 20) {
            return "密码最大长度为20";
        }
        return null;
    }

    private static boolean isValidAccount(String s) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (!(c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9' || c == '_')) {
                return false;
            }
        }
        return true;
    }

    // ── 带 placeholder 的输入框：空文本时显示灰色提示文字 ──

    private static final class PlaceholderTextField extends JTextField {
        private final String placeholder;

        PlaceholderTextField(String initial, String placeholder) {
            super(initial, 15);
            this.placeholder = placeholder;
        }

        @Override
        protected void paintComponent(java.awt.Graphics g) {
            super.paintComponent(g);
            if (getText().isEmpty()) {
                paintPlaceholder(g, getFont(), getInsets().left, getWidth(), getHeight(), placeholder);
            }
        }
    }

    private static final class PlaceholderPasswordField extends JPasswordField {
        private final String placeholder;

        PlaceholderPasswordField(String initial, String placeholder) {
            super(initial, 15);
            this.placeholder = placeholder;
        }

        @Override
        protected void paintComponent(java.awt.Graphics g) {
            super.paintComponent(g);
            if (getText().isEmpty()) {
                paintPlaceholder(g, getFont(), getInsets().left, getWidth(), getHeight(), placeholder);
            }
        }
    }

    private static void paintPlaceholder(java.awt.Graphics g, java.awt.Font font,
                                         int insetLeft, int width, int height, String text) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setColor(java.awt.Color.GRAY);
            g2.setFont(font);
            java.awt.FontMetrics fm = g2.getFontMetrics();
            int x = insetLeft + 2;
            int y = (height - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(text, x, y);
        } finally {
            g2.dispose();
        }
    }
}
