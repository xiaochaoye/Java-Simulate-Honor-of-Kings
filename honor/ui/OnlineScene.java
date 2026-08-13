package honor.ui;

import honor.core.KeyInput;
import honor.core.MatchConfig;
import honor.core.Scene;
import honor.hero.HeroDef;
import honor.net.OnlineClient;
import honor.net.Protocol;
import honor.net.RoomInfo;
import honor.util.Sfx;
import java.awt.Graphics2D;
import java.awt.Color;
import honor.G2D;

public final class OnlineScene implements Scene {
    private static final int LOBBY_FIXED_ROWS = 4;
    private final OnlineClient client;
    private final SelectScene heroPick = new SelectScene();
    private boolean pickingHero;
    private int next = -1;
    private int cursor;
    private int mode = 0;
    private boolean bots = true;
    private int hero;
    private boolean watchChoice;
    private String status = "请登录账号";
    private String authHint = "";
    private int refreshLeft;
    private boolean roomSynced;
    private String lastError = "";
    private int errorLeft;
    private boolean statusIsError;
    private int screenW;
    private int screenH;
    private int listTop;
    private int rowH;
    private int lobbyScroll;
    private int backFlash;
    private boolean ownerLeaveConfirm;

    public OnlineScene(OnlineClient var1) {
        this.client = var1;
    }

    public void enter() {
        this.next = -1;
        this.cursor = 0;
        this.watchChoice = false;
        this.roomSynced = false;
        this.authHint = "";
        this.pickingHero = false;
        this.backFlash = 0;
        this.ownerLeaveConfirm = false;
        if (this.client.isConnected()) {
            this.status = "在线：" + this.client.getPlayerName();
        } else {
            this.status = "请登录账号";
        }

    }

    public boolean needsAuth() {
        return !this.client.isConnected();
    }

    public boolean consumeAuthRequest() {
        return this.client.consumeAuthRequest();
    }

    public String takeAuthHint() {
        String var1 = this.authHint;
        this.authHint = "";
        if (var1.length() == 0) {
            var1 = this.client.getError();
        }

        return var1;
    }

    public void setStatus(String var1) {
        this.status = var1;
    }

    public void submitAuth(String var1, String var2, boolean var3, boolean var4) {
        if (var1 == null) {
            this.next = 0;
        } else {
            this.status = var3 ? "正在注册..." : "正在登录...";
            this.status = this.status + " " + "nas.zixing.fun";
            this.client.connect(var1, var2, var3);
        }
    }

    public void update(KeyInput var1, int var2) {
        if (this.backFlash > 0) {
            --this.backFlash;
        }

        this.client.pump();
        if (this.client.hasPendingGame()) {
            this.pickingHero = false;
            this.next = 5;
        } else {
            String var3 = this.client.getError();
            if (var3.length() > 0 && !var3.equals(this.lastError)) {
                this.lastError = var3;
                this.authHint = var3;
                this.errorLeft = 90;
            }

            if (var3.length() > 0 && (this.errorLeft > 0 || !this.client.isConnected())) {
                if (this.errorLeft > 0) {
                    --this.errorLeft;
                }

                this.status = var3;
                this.statusIsError = true;
            } else if (this.client.isConnected()) {
                this.status = "在线：" + this.client.getPlayerName() + "  " + this.client.getWins() + "胜" + this.client.getLosses() + "负";
                this.statusIsError = false;
            } else if (this.client.getState() == 1) {
                this.status = "正在连接 nas.zixing.fun...";
                this.statusIsError = false;
            }

            if (this.pickingHero) {
                this.updateHeroPick(var1, var2);
            } else if (this.ownerLeaveConfirm) {
                if (var1.isPressed(2048)) {
                    this.ownerLeaveConfirm = false;
                    Sfx.play("ui_click");
                } else if (var1.isPressed(16)) {
                    this.ownerLeaveConfirm = false;
                    this.client.send("DISBAND");
                    this.cursor = 0;
                    Sfx.play("ui_ok");
                }

            } else if (var1.isPressed(2048)) {
                this.handleBack();
            } else if (this.client.isConnected()) {
                if (this.client.consumeLobbyChanged()) {
                    this.client.send("LIST");
                }

                if (this.refreshLeft-- <= 0 && this.client.getRoomState().length() == 0) {
                    this.client.send("LIST");
                    this.refreshLeft = 75;
                }

                if (this.client.getRoomState().length() > 0) {
                    this.syncRoomSelection();
                    this.updateRoom(var1);
                } else {
                    this.roomSynced = false;
                    this.updateLobby(var1);
                }

            }
        }
    }

    private void updateHeroPick(KeyInput var1, int var2) {
        this.heroPick.update(var1, var2);
        if (this.heroPick.consumePickConfirmed()) {
            this.hero = this.heroPick.getSelectedHero();
            this.pickingHero = false;
            if (this.client.getRoomState().length() > 0) {
                this.client.send("SETHERO|" + this.hero);
            }

            Sfx.play("ui_ok");
        } else {
            if (this.heroPick.consumePickCancelled()) {
                this.pickingHero = false;
                Sfx.play("ui_click");
            }

        }
    }

    private void openHeroPick() {
        this.pickingHero = true;
        this.heroPick.enterForPick(this.hero);
    }

    private void handleBack() {
        this.backFlash = 8;
        if (this.pickingHero) {
            this.pickingHero = false;
            Sfx.play("ui_click");
        } else {
            if (this.client.getRoomState().length() > 0) {
                this.requestLeaveRoom();
            } else {
                this.client.close();
                this.next = 0;
                Sfx.play("ui_click");
            }

        }
    }

    private void requestLeaveRoom() {
        if (this.isOwner()) {
            this.ownerLeaveConfirm = true;
            Sfx.play("ui_click");
        } else {
            this.client.send("LEAVE");
            this.cursor = 0;
            Sfx.play("ui_click");
        }
    }

    private void syncRoomSelection() {
        String[] var1 = Protocol.split(this.client.getRoomState(), '|');
        if (var1.length >= 8) {
            if (!this.client.getPlayerName().equals(var1[5]) || !this.roomSynced) {
                this.mode = Protocol.parseInt(var1[2], this.mode);
                this.bots = Protocol.parseInt(var1[3], this.bots ? 1 : 0) != 0;
            }

            if (!this.roomSynced) {
                this.roomSynced = true;
                if (var1[7].length() != 0) {
                    String[] var2 = Protocol.split(var1[7], ';');

                    for(int var3 = 0; var3 < var2.length; ++var3) {
                        String[] var4 = Protocol.split(var2[var3], ',');
                        if (var4.length >= 3 && var4[1].equals(this.client.getPlayerName())) {
                            this.hero = Protocol.parseInt(var4[2], this.hero);
                            return;
                        }
                    }

                }
            }
        }
    }

    private void updateLobby(KeyInput var1) {
        RoomInfo[] var2 = this.client.getRooms();
        boolean var3 = this.client.getResumeRoomId() > 0;
        int var4 = 4 + (var3 ? 1 : 0);
        int var5 = var4 + var2.length;
        if (this.cursor >= var5) {
            this.cursor = var5 - 1;
        }

        if (this.cursor < 0) {
            this.cursor = 0;
        }

        if (var1.isPressed(1)) {
            this.cursor = (this.cursor + var5 - 1) % var5;
            Sfx.play("ui_click");
        }

        if (var1.isPressed(2)) {
            this.cursor = (this.cursor + 1) % var5;
            Sfx.play("ui_click");
        }

        int var6 = var3 ? 4 : 3;
        int var7 = var1.isPressed(4) ? -1 : (var1.isPressed(8) ? 1 : 0);
        if (var7 != 0) {
            if (this.cursor == 1) {
                this.mode = (this.mode + var7 + 3) % 3;
            } else if (this.cursor == 2) {
                this.bots = !this.bots;
            } else {
                if (this.cursor == var6) {
                    this.openHeroPick();
                    return;
                }

                if (this.cursor >= var4) {
                    RoomInfo var8 = var2[this.cursor - var4];
                    this.watchChoice = var8.state != 0 || !this.watchChoice;
                }
            }

            Sfx.play("ui_click");
        }

        if (var1.isPressed(16)) {
            if (var3 && this.cursor == 0) {
                this.client.send("REJOIN");
                Sfx.play("ui_ok");
            } else {
                int var12 = var3 ? 1 : 0;
                int var9 = var3 ? 2 : 1;
                int var10 = var3 ? 3 : 2;
                if (this.cursor == var12) {
                    this.client.send("CREATE|" + this.mode + "|" + (this.bots ? 1 : 0) + "|" + this.hero);
                } else if (this.cursor == var9) {
                    this.mode = (this.mode + 1) % 3;
                } else if (this.cursor == var10) {
                    this.bots = !this.bots;
                } else {
                    if (this.cursor == var6) {
                        this.openHeroPick();
                        return;
                    }

                    RoomInfo var11 = var2[this.cursor - var4];
                    if (var11.state == 0 && !this.watchChoice) {
                        this.client.send("JOIN|" + var11.id + "|" + this.hero);
                    } else {
                        this.client.send("WATCH|" + var11.id);
                    }
                }

                Sfx.play("ui_ok");
            }
        }
    }

    private void updateRoom(KeyInput var1) {
        boolean var2 = this.isOwner();
        int var3 = var2 ? 5 : 2;
        if (this.cursor >= var3) {
            this.cursor = var3 - 1;
        }

        if (var1.isPressed(1)) {
            this.cursor = (this.cursor + var3 - 1) % var3;
            Sfx.play("ui_click");
        }

        if (var1.isPressed(2)) {
            this.cursor = (this.cursor + 1) % var3;
            Sfx.play("ui_click");
        }

        int var4 = var1.isPressed(4) ? -1 : (var1.isPressed(8) ? 1 : 0);
        if (var4 != 0 && this.cursor == 0) {
            this.openHeroPick();
        } else {
            if (var4 != 0 && var2 && this.cursor == 1) {
                this.mode = (this.mode + var4 + 3) % 3;
                this.client.send("SETMODE|" + this.mode);
                Sfx.play("ui_click");
            } else if (var4 != 0 && var2 && this.cursor == 2) {
                this.bots = !this.bots;
                this.client.send("SETBOTS|" + (this.bots ? 1 : 0));
                Sfx.play("ui_click");
            }

            if (var1.isPressed(16)) {
                if (this.cursor == 0) {
                    this.openHeroPick();
                } else {
                    if (var2 && this.cursor == 1) {
                        this.mode = (this.mode + 1) % 3;
                        this.client.send("SETMODE|" + this.mode);
                        Sfx.play("ui_ok");
                    } else if (var2 && this.cursor == 2) {
                        this.bots = !this.bots;
                        this.client.send("SETBOTS|" + (this.bots ? 1 : 0));
                        Sfx.play("ui_ok");
                    } else if (var2 && this.cursor == 3) {
                        this.client.send("START");
                        Sfx.play("ui_ok");
                    } else {
                        this.requestLeaveRoom();
                    }

                }
            }
        }
    }

    public void render(Graphics2D var1, int var2, int var3, int var4) {
        this.screenW = var2;
        this.screenH = var3;
        if (this.pickingHero) {
            this.heroPick.render(var1, var2, var3, var4);
        } else if (this.ownerLeaveConfirm) {
            UiKit.backdrop(var1, var2, var3);
            int var5 = var2 - 24;
            if (var5 > 200) {
                var5 = 200;
            }

            int var6 = UiKit.confirmDialogHeight();
            int var7 = (var2 - var5) / 2;
            int var8 = (var3 - var6) / 2;
            UiKit.drawConfirmDialog(var1, var2, "主机离开后", "房间对局将会结束", var7, var8, var5, var6);
            UiKit.drawBackButton(var1, 4, 2, this.backFlash > 0);
        } else {
            UiKit.backdrop(var1, var2, var3);
            var1.setFont(UiKit.LARGE);
            var1.setColor(G2D.color(15254634));
            G2D.drawString(var1, "联机对战", var2 / 2, 8, 17);
            var1.setFont(UiKit.SMALL);
            var1.setColor(G2D.color(this.statusIsError ? 16740464 : 8885420));
            G2D.drawString(var1, this.status, var2 / 2, 8 + G2D.fontHeight(UiKit.LARGE), 17);
            if (!this.client.isConnected()) {
                var1.setColor(G2D.color(14476530));
                G2D.drawString(var1, this.client.getState() == 0 ? "请重新登录连接" : "正在握手...", var2 / 2, var3 / 2, 17);
                UiKit.drawBackButton(var1, 4, 2, this.backFlash > 0);
            } else {
                if (this.client.getRoomState().length() > 0) {
                    this.renderRoom(var1, var2, var3);
                } else {
                    this.renderLobby(var1, var2, var3);
                }

                UiKit.drawBackButton(var1, 4, 2, this.backFlash > 0);
            }
        }
    }

    private void renderLobby(Graphics2D var1, int var2, int var3) {
        RoomInfo[] var4 = this.client.getRooms();
        boolean var5 = this.client.getResumeRoomId() > 0;
        int var6 = 4 + (var5 ? 1 : 0);
        this.rowH = G2D.fontHeight(UiKit.SMALL) + 8;
        this.listTop = 48;
        int var7 = 0;
        if (var5) {
            this.drawRow(var1, var7++, "返回对局 #" + this.client.getResumeRoomId(), "确认");
        }

        this.drawRow(var1, var7++, "创建房间", "确认");
        this.drawRow(var1, var7++, "模式", MatchConfig.modeName(this.mode));
        this.drawRow(var1, var7++, "补充机器人", this.bots ? "开" : "关");
        this.drawRow(var1, var7++, "我的英雄", HeroDef.name(this.hero));
        int var8 = (var3 - this.listTop - this.rowH * var6 - 18) / this.rowH;
        int var9 = this.cursor - var6;
        int var10 = 0;
        if (var9 >= var8 && var8 > 0) {
            var10 = var9 - var8 + 1;
        }

        this.lobbyScroll = var10;

        for(int var11 = 0; var11 < var8 && var11 + var10 < var4.length; ++var11) {
            RoomInfo var12 = var4[var11 + var10];
            boolean var13 = this.cursor == var6 + var11 + var10;
            String var14 = var12.state == 0 ? var12.players + "/" + var12.capacity : (var12.state == 1 ? "战斗中" : "已结束");
            String var15 = "#" + var12.id + " " + var12.owner + " " + modeShort(var12.mode);
            String var16 = var13 ? (var12.state == 0 && !this.watchChoice ? "加入" : "观战") : var14;
            this.drawVisualRow(var1, var6 + var11, var15, var16, var13);
        }

        if (var4.length == 0) {
            var1.setColor(G2D.color(8885420));
            G2D.drawString(var1, "大厅还没有房间", var2 / 2, this.listTop + this.rowH * var6 + 8, 17);
        } else if (this.client.getRoomCapacity() > 0) {
            var1.setColor(G2D.color(8885420));
            G2D.drawString(var1, "房间 " + this.client.getRoomCount() + "/" + this.client.getRoomCapacity(), var2 - 8, 42, 24);
        }

        var1.setColor(G2D.color(8885420));
        G2D.drawString(var1, "选英雄打开列表  5确认  #返回", var2 / 2, var3 - 14, 17);
    }

    private void renderRoom(Graphics2D var1, int var2, int var3) {
        String[] var4 = Protocol.split(this.client.getRoomState(), '|');
        if (var4.length >= 8) {
            boolean var5 = this.client.getPlayerName().equals(var4[5]);
            boolean var6 = Protocol.parseInt(var4[4], 0) != 0;
            var1.setFont(UiKit.SMALL);
            var1.setColor(G2D.color(14476530));
            G2D.drawString(var1, "房间 #" + var4[1] + "  房主 " + var4[5] + (var6 ? "  对局中" : ""), var2 / 2, 42, 17);
            int var7 = 58;
            if (var4[7].length() > 0) {
                String[] var8 = Protocol.split(var4[7], ';');

                for(int var9 = 0; var9 < var8.length && var9 < 10; ++var9) {
                    String[] var10 = Protocol.split(var8[var9], ',');
                    if (var10.length >= 4) {
                        int var11 = Protocol.parseInt(var10[2], 0);
                        boolean var12 = var10.length >= 5 && Protocol.parseInt(var10[4], 0) != 0;
                        var1.setColor(G2D.color(var10[1].equals(this.client.getPlayerName()) ? 15254634 : 14476530));
                        G2D.drawString(var1, (Protocol.parseInt(var10[0], 0) < MatchConfig.teamSizeOf(this.mode) ? "蓝 " : "红 ") + var10[1] + "·" + HeroDef.name(var11) + (var12 ? "(托管)" : ""), 18, var7, 20);
                        var7 += G2D.fontHeight(UiKit.SMALL) + 2;
                    }
                }
            }

            this.rowH = G2D.fontHeight(UiKit.SMALL) + 8;
            this.listTop = var3 - this.rowH * (var5 ? 5 : 2) - 18;
            this.drawRow(var1, 0, "英雄", HeroDef.name(this.hero));
            if (var5) {
                this.drawRow(var1, 1, "模式", MatchConfig.modeName(this.mode));
                this.drawRow(var1, 2, "补充机器人", this.bots ? "开" : "关");
                this.drawRow(var1, 3, "开始游戏", "确认");
                this.drawRow(var1, 4, "离开房间", "");
            } else {
                this.drawRow(var1, 1, "离开房间", "");
            }

            var1.setColor(G2D.color(8885420));
            G2D.drawString(var1, "观众 " + var4[6] + " 人", var2 - 8, 42, 24);
        }
    }

    private void drawRow(Graphics2D var1, int var2, String var3, String var4) {
        this.drawVisualRow(var1, var2, var3, var4, this.cursor == var2);
    }

    private void drawVisualRow(Graphics2D var1, int var2, String var3, String var4, boolean var5) {
        byte var6 = 12;
        int var7 = this.listTop + var2 * this.rowH;
        int var8 = this.screenW - 24;
        if (var5) {
            UiKit.gradientV(var1, var6, var7, var8, this.rowH - 2, 2767454, 1450042);
            var1.setColor(G2D.color(15254634));
            var1.drawRoundRect(var6, var7, var8, this.rowH - 2, 5, 5);
        }

        var1.setFont(UiKit.SMALL);
        var1.setColor(G2D.color(var5 ? 14476530 : 8885420));
        G2D.drawString(var1, var3, var6 + 7, var7 + 3, 20);
        if (var4 != null && var4.length() > 0) {
            var1.setColor(G2D.color(15254634));
            G2D.drawString(var1, var4, var6 + var8 - 7, var7 + 3, 24);
        }

    }

    public boolean onPointer(int var1, int var2, int var3, int var4, KeyInput var5) {
        if (this.pickingHero) {
            this.heroPick.onPointer(var1, var2, var3, var4, var5);
            return true;
        } else if (this.ownerLeaveConfirm) {
            if (UiKit.hitBackButton(var1, var2, 4, 2)) {
                this.ownerLeaveConfirm = false;
                Sfx.play("ui_click");
                return true;
            } else {
                int var11 = var3 - 24;
                if (var11 > 200) {
                    var11 = 200;
                }

                int var13 = UiKit.confirmDialogHeight();
                int var14 = (var3 - var11) / 2;
                int var15 = (var4 - var13) / 2;
                int var16 = UiKit.hitConfirmDialog(var1, var2, var14, var15, var11, var13);
                if (var16 == 1) {
                    this.ownerLeaveConfirm = false;
                    this.client.send("DISBAND");
                    this.cursor = 0;
                    Sfx.play("ui_ok");
                } else if (var16 == 2) {
                    this.ownerLeaveConfirm = false;
                    Sfx.play("ui_click");
                }

                return true;
            }
        } else if (UiKit.hitBackButton(var1, var2, 4, 2)) {
            this.handleBack();
            return true;
        } else if (this.rowH > 0 && var2 >= this.listTop) {
            int var6 = (var2 - this.listTop) / this.rowH;
            if (this.client.getRoomState().length() == 0) {
                boolean var7 = this.client.getResumeRoomId() > 0;
                int var8 = 4 + (var7 ? 1 : 0);
                RoomInfo[] var9 = this.client.getRooms();
                if (var6 >= 0 && var6 < var8) {
                    this.cursor = var6;
                } else if (var6 >= var8) {
                    int var10 = var6 - var8 + this.lobbyScroll;
                    if (var10 >= 0 && var10 < var9.length) {
                        this.cursor = var8 + var10;
                    }
                }
            } else {
                int var12 = this.isOwner() ? 5 : 2;
                if (var6 >= 0 && var6 < var12) {
                    this.cursor = var6;
                }
            }

            return false;
        } else {
            return false;
        }
    }

    private boolean isOwner() {
        String[] var1 = Protocol.split(this.client.getRoomState(), '|');
        return var1.length >= 6 && this.client.getPlayerName().equals(var1[5]);
    }

    private static String modeShort(int var0) {
        if (var0 == 1) {
            return "3v3";
        } else {
            return var0 == 2 ? "5v5" : "1v1";
        }
    }

    public int nextScene() {
        return this.next;
    }
}
