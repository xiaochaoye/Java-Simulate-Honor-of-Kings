package honor.net;

import honor.core.KeyInput;
import honor.util.Log;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Vector;

public final class OnlineClient {
    public static final int DISCONNECTED = 0;
    public static final int CONNECTING = 1;
    public static final int CONNECTED = 2;
    private volatile int state = 0;
    private volatile boolean running;
    private Socket socket;
    private InputStream in;
    private OutputStream out;
    private final Object writeLock = new Object();
    private final Vector lines = new Vector();
    private final Vector frames = new Vector();
    private Thread reader;
    public static final int WINNER_NONE = -2;
    public static final int WINNER_DRAW = -1;
    private static final long PING_MS = 4000L;
    private static final long DEAD_MS = 30000L;
    private String playerName = "";
    private String error = "";
    private boolean welcomed;
    private boolean lobbyChanged;
    private boolean authRejected;
    private RoomInfo[] rooms = new RoomInfo[0];
    private final Vector roomBuffer = new Vector();
    private String roomState = "";
    private OnlineGame pendingGame;
    private boolean replaying;
    private int gameOverWinner = -2;
    private int wins;
    private int losses;
    private int roomCount;
    private int roomCapacity;
    private int resumeRoomId;
    private String roomOwner = "";
    private boolean roomClosed;
    private long lastRecvAt;
    private long lastPingAt;

    public OnlineClient() {
    }

    public int getState() {
        return this.state;
    }

    public boolean isConnected() {
        return this.state == 2 && this.welcomed;
    }

    public String getPlayerName() {
        return this.playerName;
    }

    public String getError() {
        return this.error;
    }

    public RoomInfo[] getRooms() {
        return this.rooms;
    }

    public String getRoomState() {
        return this.roomState;
    }

    public boolean consumeLobbyChanged() {
        boolean var1 = this.lobbyChanged;
        this.lobbyChanged = false;
        return var1;
    }

    public boolean consumeAuthRequest() {
        boolean var1 = this.authRejected;
        this.authRejected = false;
        return var1;
    }

    public boolean isReplaying() {
        return this.replaying;
    }

    public int consumeGameOver() {
        int var1 = this.gameOverWinner;
        this.gameOverWinner = -2;
        return var1;
    }

    public int getWins() {
        return this.wins;
    }

    public int getLosses() {
        return this.losses;
    }

    public int getRoomCount() {
        return this.roomCount;
    }

    public int getRoomCapacity() {
        return this.roomCapacity;
    }

    public int getResumeRoomId() {
        return this.resumeRoomId;
    }

    public boolean isRoomOwner() {
        return this.playerName != null && this.playerName.length() > 0 && this.playerName.equals(this.roomOwner);
    }

    public boolean consumeRoomClosed() {
        boolean var1 = this.roomClosed;
        this.roomClosed = false;
        return var1;
    }

    public void connect(final String var1, final String var2, final boolean var3) {
        this.close();
        this.playerName = var1;
        this.error = "";
        this.welcomed = false;
        this.authRejected = false;
        this.lastRecvAt = System.currentTimeMillis();
        this.lastPingAt = this.lastRecvAt;
        this.state = 1;
        this.running = true;
        this.reader = new Thread(new Runnable() {
            public void run() {
                OnlineClient.this.runConnection(var1, var2, var3);
            }
        });
        this.reader.start();
    }

    private void runConnection(String var1, String var2, boolean var3) {
        try {
            this.socket = new Socket(ServerConfig.HOST, ServerConfig.PORT);
            this.socket.setTcpNoDelay(true);
            this.in = this.socket.getInputStream();
            this.out = this.socket.getOutputStream();
            this.state = 2;
            if (var3) {
                this.send("REGISTER|" + var1 + "|" + var2);
            } else {
                this.send("LOGIN|" + var1 + "|" + var2);
            }

            while(this.running) {
                String var4 = readUtf8Line(this.in);
                if (var4 == null) {
                    throw new IOException("连接已关闭");
                }

                synchronized(this.lines) {
                    this.lines.addElement(var4);
                }
            }
        } catch (Exception var14) {
            if (this.running) {
                this.error = messageOf(var14);
                this.enqueue("ERROR|NETWORK|" + this.error);
            }
        } finally {
            this.closeTransport();
            this.state = 0;
        }

    }

    public void pump() {
        boolean var1 = false;

        while(true) {
            String var2;
            synchronized(this.lines) {
                if (this.lines.size() == 0) {
                    break;
                }

                var2 = (String)this.lines.elementAt(0);
                this.lines.removeElementAt(0);
            }

            var1 = true;
            this.parse(var2);
        }

        long var6 = System.currentTimeMillis();
        if (var1) {
            this.lastRecvAt = var6;
        } else if (this.state != 0) {
            if (var6 - this.lastRecvAt > 30000L) {
                this.error = "服务器无响应";
                Log.error("online: heartbeat timeout");
                this.close();
                this.authRejected = true;
            } else if (var6 - this.lastPingAt > 4000L) {
                this.lastPingAt = var6;
                this.send("PING");
            }

        }
    }

    private void parse(String var1) {
        String[] var2 = Protocol.split(var1, '|');
        if (var2.length != 0) {
            String var3 = var2[0];
            if ("HELLO".equals(var3)) {
                int var4 = var2.length >= 2 ? Protocol.parseInt(var2[1], 0) : 0;
                if (var4 != 3) {
                    this.error = "服务端协议 v" + var4 + "，客户端 v" + 3;
                    this.close();
                    this.authRejected = true;
                }
            } else if ("WELCOME".equals(var3)) {
                this.welcomed = true;
                this.error = "";
                if (var2.length >= 3 && var2[2].length() > 0) {
                    this.playerName = var2[2];
                }

                this.send("REJOIN");
                this.send("LIST");
                this.send("RECORD");
            } else if (!"PONG".equals(var3)) {
                if ("ERROR".equals(var3)) {
                    this.error = var2.length >= 3 ? var2[2] : "服务器错误";
                    String var10 = var2.length >= 2 ? var2[1] : "";
                    if (isAuthError(var10)) {
                        this.close();
                        this.authRejected = true;
                    }
                } else if ("RECORD".equals(var3) && var2.length >= 3) {
                    this.wins = Protocol.parseInt(var2[1], 0);
                    this.losses = Protocol.parseInt(var2[2], 0);
                } else if ("GAMEOVER".equals(var3) && var2.length >= 2) {
                    this.gameOverWinner = Protocol.parseInt(var2[1], -1);
                    this.replaying = false;
                    this.send("RECORD");
                } else if ("ROOMS".equals(var3)) {
                    this.roomBuffer.removeAllElements();
                    if (var2.length >= 3) {
                        this.roomCount = Protocol.parseInt(var2[1], 0);
                        this.roomCapacity = Protocol.parseInt(var2[2], 0);
                    }
                } else if ("ROOM".equals(var3) && var2.length >= 9) {
                    RoomInfo var14 = new RoomInfo();
                    var14.id = Protocol.parseInt(var2[1], 0);
                    var14.owner = var2[2];
                    var14.mode = Protocol.parseInt(var2[3], 0);
                    var14.bots = Protocol.parseInt(var2[4], 0) != 0;
                    var14.players = Protocol.parseInt(var2[5], 0);
                    var14.capacity = Protocol.parseInt(var2[6], 0);
                    var14.spectators = Protocol.parseInt(var2[7], 0);
                    var14.state = Protocol.parseInt(var2[8], 0);
                    this.roomBuffer.addElement(var14);
                } else if ("ENDROOMS".equals(var3)) {
                    RoomInfo[] var11 = new RoomInfo[this.roomBuffer.size()];
                    this.roomBuffer.copyInto(var11);
                    this.rooms = var11;
                } else if ("LOBBYCHANGED".equals(var3)) {
                    this.lobbyChanged = true;
                } else if ("ROOMSTATE".equals(var3)) {
                    this.roomState = var1;
                    this.resumeRoomId = 0;
                    if (var2.length >= 6) {
                        this.roomOwner = var2[5];
                    }
                } else if ("LEFT".equals(var3)) {
                    this.roomState = "";
                    this.roomOwner = "";
                    this.send("LIST");
                } else if ("ROOMCLOSED".equals(var3)) {
                    this.roomState = "";
                    this.roomOwner = "";
                    this.resumeRoomId = 0;
                    this.roomClosed = true;
                    this.error = var2.length >= 2 && var2[1].length() > 0 ? var2[1] : "房主已解散房间";
                    this.send("LIST");
                } else if ("CANRESUME".equals(var3) && var2.length >= 2) {
                    this.resumeRoomId = Protocol.parseInt(var2[1], 0);
                } else if ("GAME".equals(var3) && var2.length >= 8) {
                    this.resumeRoomId = 0;
                    OnlineGame var13 = new OnlineGame();
                    var13.roomId = Protocol.parseInt(var2[1], 0);
                    var13.mode = Protocol.parseInt(var2[2], 0);
                    var13.seed = Protocol.parseInt(var2[3], 0);
                    var13.localSlot = Protocol.parseInt(var2[4], -1);
                    var13.humanMask = Protocol.parseInt(var2[5], 0);
                    var13.blue = Protocol.parseInts(var2[6], ',');
                    var13.red = Protocol.parseInts(var2[7], ',');
                    this.pendingGame = var13;
                    this.gameOverWinner = -2;
                    this.replaying = false;
                    synchronized(this.frames) {
                        this.frames.removeAllElements();
                    }
                } else if ("REPLAY".equals(var3)) {
                    this.replaying = true;
                } else if ("REPLAYEND".equals(var3)) {
                    this.replaying = false;
                } else if ("FRAME".equals(var3) && var2.length >= 4) {
                    OnlineFrame var12 = this.parseFrame(var2);
                    synchronized(this.frames) {
                        this.frames.addElement(var12);
                        if (this.frames.size() > 12000) {
                            this.frames.removeElementAt(0);
                        }
                    }
                }
            }

        }
    }

    private OnlineFrame parseFrame(String[] var1) {
        OnlineFrame var2 = new OnlineFrame();
        var2.tick = Protocol.parseInt(var1[1], 0);
        var2.humanMask = Protocol.parseInt(var1[2], 0);
        if (var1[3].length() == 0) {
            return var2;
        } else {
            String[] var3 = Protocol.split(var1[3], ';');

            for(int var4 = 0; var4 < var3.length; ++var4) {
                int[] var5 = Protocol.parseInts(var3[var4], ',');
                if (var5.length >= 5 && var5[0] >= 0 && var5[0] < var2.held.length) {
                    int var6 = var5[0];
                    var2.held[var6] = var5[1];
                    var2.pressed[var6] = var5[2];
                    var2.axisX[var6] = var5[3];
                    var2.axisY[var6] = var5[4];
                    var2.cmd[var6] = var5.length >= 6 ? var5[5] : 0;
                }
            }

            return var2;
        }
    }

    public OnlineGame takePendingGame() {
        OnlineGame var1 = this.pendingGame;
        this.pendingGame = null;
        return var1;
    }

    public boolean hasPendingGame() {
        return this.pendingGame != null;
    }

    public OnlineFrame pollFrame() {
        synchronized(this.frames) {
            if (this.frames.size() == 0) {
                return null;
            } else {
                OnlineFrame var2 = (OnlineFrame)this.frames.elementAt(0);
                this.frames.removeElementAt(0);
                return var2;
            }
        }
    }

    public int queuedFrames() {
        synchronized(this.frames) {
            return this.frames.size();
        }
    }

    public void sendInput(KeyInput var1, int var2) {
        if (this.isConnected()) {
            int var3 = var1.takePendingCommand();
            this.send("INPUT|" + (var1.heldMask() & ~var2) + "|" + (var1.pressedMask() & ~var2) + "|" + var1.axisX100() + "|" + var1.axisY100() + "|" + var3);
        }
    }

    public void send(String var1) {
        OutputStream var2 = this.out;
        if (var2 != null) {
            synchronized(this.writeLock) {
                try {
                    byte[] var4 = (var1 + "\n").getBytes("UTF-8");
                    var2.write(var4);
                    var2.flush();
                } catch (IOException var6) {
                    this.error = messageOf(var6);
                    this.closeTransport();
                }

            }
        }
    }

    public void close() {
        this.running = false;
        this.closeTransport();
        this.welcomed = false;
        this.state = 0;
        this.roomState = "";
        this.roomOwner = "";
        this.pendingGame = null;
        this.replaying = false;
        this.gameOverWinner = -2;
        this.rooms = new RoomInfo[0];
        this.roomBuffer.removeAllElements();
        synchronized(this.lines) {
            this.lines.removeAllElements();
        }

        synchronized(this.frames) {
            this.frames.removeAllElements();
        }
    }

    private void closeTransport() {
        try {
            if (this.in != null) {
                this.in.close();
            }
        } catch (IOException var4) {
        }

        try {
            if (this.out != null) {
                this.out.close();
            }
        } catch (IOException var3) {
        }

        try {
            if (this.socket != null) {
                this.socket.close();
            }
        } catch (IOException var2) {
        }

        this.in = null;
        this.out = null;
        this.socket = null;
    }

    private void enqueue(String var1) {
        synchronized(this.lines) {
            this.lines.addElement(var1);
        }
    }

    private static String readUtf8Line(InputStream var0) throws IOException {
        ByteArrayOutputStream var1 = new ByteArrayOutputStream(128);

        while(true) {
            int var2 = var0.read();
            if (var2 < 0) {
                return var1.size() == 0 ? null : new String(var1.toByteArray(), "UTF-8");
            }

            if (var2 == 10) {
                return new String(var1.toByteArray(), "UTF-8");
            }

            if (var2 != 13) {
                if (var1.size() >= 65535) {
                    throw new IOException("服务器消息过长");
                }

                var1.write(var2);
            }
        }
    }

    private static String messageOf(Exception var0) {
        String var1 = var0.getMessage();
        if (var1 == null || var1.length() == 0) {
            var1 = var0.toString();
        }

        Log.error("online: " + var1);
        return var1;
    }

    private static boolean isAuthError(String var0) {
        return "AUTH_FAIL".equals(var0) || "ACCOUNT_EXISTS".equals(var0) || "ACCOUNT_ONLINE".equals(var0) || "BAD_ACCOUNT".equals(var0) || "BAD_PASSWORD".equals(var0) || "NETWORK".equals(var0);
    }
}
