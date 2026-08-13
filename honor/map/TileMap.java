package honor.map;

import honor.core.IsoMath;
import honor.util.Log;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import honor.G2D;
import honor.Img;

public class TileMap {
    public static final int TILE_TYPES = 14;
    public static final int TILE_BUSH = 7;
    public static final int TILE_RIVER = 2;
    public static final int TILE_PAD = 3;
    public static final int TILE_ROCK = 6;
    public static final int TILE_CLIFF = 12;
    public static final int TILE_FOUNTAIN = 13;
    public static final int SPOT_TOWER = 0;
    public static final int SPOT_FOUNTAIN = 1;
    public static final int SPOT_BLUE_BUFF = 2;
    public static final int SPOT_RED_BUFF = 3;
    public static final int SPOT_CAMP = 4;
    public static final int SPOT_TYRANT = 5;
    public static final int SPOT_OVERLORD = 6;
    private static final boolean[] WALKABLE = new boolean[]{true, true, true, true, true, true, false, true, true, true, true, true, false, true};
    private static final boolean[] ROTATABLE = new boolean[]{true, true, true, false, false, false, false, true, true, false, true, true, false, false};
    private static final int SPOT_STRIDE = 6;
    private static final int SPOT_STRIDE_LEGACY = 4;
    private final int cols;
    private final int rows;
    private final byte[] cells;
    private final BufferedImage[] tileImages;
    private byte[] spots;
    private int spotCount;
    private BufferedImage mapBg;
    private int bgOriginX;
    private int bgOriginY;
    private static final int MAX_SOLID = 28;
    private int solidCount;
    private final int[] solidX = new int[28];
    private final int[] solidY = new int[28];
    private final int[] solidR = new int[28];
    private final boolean[] solidOn = new boolean[28];

    public TileMap(String var1, String var2, String var3) throws IOException {
        byte[] var4 = readFully(var1);
        if (var4.length < 2) {
            throw new IOException("map too small: " + var1);
        } else {
            this.cols = var4[0] & 255;
            this.rows = var4[1] & 255;
            int var5 = 2 + this.cols * this.rows;
            if (var4.length < var5) {
                throw new IOException("map truncated: " + var1);
            } else {
                this.cells = new byte[this.cols * this.rows];
                System.arraycopy(var4, 2, this.cells, 0, this.cells.length);
                BufferedImage var6 = Img.load(var2);
                this.tileImages = new BufferedImage[14];

                for(int var7 = 0; var7 < 14; ++var7) {
                    this.tileImages[var7] = var6.getSubimage(var7 * 31, 0, 31, 40);
                }

                if (var3 != null) {
                    this.loadSpots(var3);
                }

                this.tryLoadBackground("/res/img/map_bg.jpg", "/res/img/map_bg.origin.txt");
                Log.info("Iso TileMap " + this.cols + "x" + this.rows + " spots=" + this.spotCount + (this.mapBg != null ? " bg=yes" : " bg=no"));
            }
        }
    }

    private void tryLoadBackground(String var1, String var2) {
        this.bgOriginX = 0;
        this.bgOriginY = 0;
        String var3 = var1;
        InputStream var4 = null;

        try {
            var4 = TileMap.class.getResourceAsStream(var2);
            if (var4 != null) {
                ByteArrayOutputStream var5 = new ByteArrayOutputStream();
                byte[] var6 = new byte[64];

                int var7;
                while((var7 = var4.read(var6)) > 0) {
                    var5.write(var6, 0, var7);
                }

                String var8 = new String(var5.toByteArray());
                this.bgOriginX = parseOriginInt(var8, "ox=");
                this.bgOriginY = parseOriginInt(var8, "oy=");
                String var9 = parseOriginStr(var8, "file=");
                if (var9 != null && var9.length() > 0) {
                    int var10 = var1.lastIndexOf(47);
                    String var11 = var10 >= 0 ? var1.substring(0, var10 + 1) : "/res/img/";
                    var3 = var11 + var9;
                }
            } else {
                Log.warn("map_bg.origin.txt missing, origin=0,0");
            }
        } catch (Exception var26) {
            Log.warn("map_bg.origin parse: " + var26.getMessage());
        } finally {
            if (var4 != null) {
                try {
                    var4.close();
                } catch (IOException var22) {
                }
            }

        }

        try {
            this.mapBg = Img.load(var3);
        } catch (IOException var25) {
            if (!var3.endsWith(".jpg") && !var3.endsWith(".jpeg")) {
                if (var3 != var1) {
                    try {
                        this.mapBg = Img.load(var1);
                    } catch (IOException var23) {
                        this.mapBg = null;
                    }
                } else {
                    this.mapBg = null;
                }
            } else {
                String var28 = var3.substring(0, var3.lastIndexOf(46)) + ".png";

                try {
                    this.mapBg = Img.load(var28);
                } catch (IOException var24) {
                    this.mapBg = null;
                }
            }
        }

    }

    private static int parseOriginInt(String var0, String var1) {
        int var2 = var0.indexOf(var1);
        if (var2 < 0) {
            return 0;
        } else {
            var2 += var1.length();

            int var3;
            for(var3 = var2; var3 < var0.length(); ++var3) {
                char var4 = var0.charAt(var3);
                if (var4 != '-' && (var4 < '0' || var4 > '9')) {
                    break;
                }
            }

            if (var3 <= var2) {
                return 0;
            } else {
                try {
                    return Integer.parseInt(var0.substring(var2, var3));
                } catch (NumberFormatException var5) {
                    return 0;
                }
            }
        }
    }

    private static String parseOriginStr(String var0, String var1) {
        int var2 = var0.indexOf(var1);
        if (var2 < 0) {
            return null;
        } else {
            var2 += var1.length();

            int var3;
            for(var3 = var2; var3 < var0.length(); ++var3) {
                char var4 = var0.charAt(var3);
                if (var4 == '\r' || var4 == '\n') {
                    break;
                }
            }

            return var3 <= var2 ? null : var0.substring(var2, var3).trim();
        }
    }

    public boolean renderBackground(Graphics2D var1, int var2, int var3) {
        if (this.mapBg == null) {
            return false;
        } else {
            G2D.drawImage(var1, this.mapBg, -var2 - this.bgOriginX, -var3 - this.bgOriginY, 20);
            return true;
        }
    }

    public boolean hasBackground() {
        return this.mapBg != null;
    }

    private void loadSpots(String var1) throws IOException {
        byte[] var2 = readFully(var1);
        if (var2.length < 1) {
            this.spots = new byte[0];
            this.spotCount = 0;
        } else {
            int var3 = var2[0] & 255;
            if (var2.length >= 1 + var3 * 6) {
                this.spots = new byte[var3 * 6];
                System.arraycopy(var2, 1, this.spots, 0, this.spots.length);
                this.spotCount = var3;
            } else if (var2.length < 1 + var3 * 4) {
                throw new IOException("spots truncated: " + var1);
            } else {
                this.spots = new byte[var3 * 6];

                for(int var4 = 0; var4 < var3; ++var4) {
                    int var5 = 1 + var4 * 4;
                    int var6 = var4 * 6;
                    this.spots[var6] = var2[var5];
                    this.spots[var6 + 1] = var2[var5 + 1];
                    this.spots[var6 + 2] = var2[var5 + 2];
                    this.spots[var6 + 3] = var2[var5 + 3];
                    this.spots[var6 + 4] = 0;
                    this.spots[var6 + 5] = 0;
                }

                this.spotCount = var3;
            }
        }
    }

    public void render(Graphics2D var1, int var2, int var3, int var4, int var5) {
        int var6 = 2 * (this.cols - 1) + (this.rows - 1);
        byte var7 = 25;

        for(int var8 = 0; var8 <= var6; ++var8) {
            int var9 = var8 * 5 - var3 - var7;
            if (var9 + 40 >= 0) {
                if (var9 > var5) {
                    break;
                }

                int var10 = var8 - this.rows + 2 >> 1;
                int var11 = var8 >> 1;
                int var12 = 22 * var8 + var2 + 21;
                int var13 = IsoMath.floorDiv(var12 - 31, 55);
                int var14 = IsoMath.floorDiv(var12 + var4, 55) + 1;
                if (var10 < var13) {
                    var10 = var13;
                }

                if (var11 > var14) {
                    var11 = var14;
                }

                if (var10 < 0) {
                    var10 = 0;
                }

                if (var11 > this.cols - 1) {
                    var11 = this.cols - 1;
                }

                for(int var15 = var10; var15 <= var11; ++var15) {
                    int var16 = var8 - 2 * var15;
                    int var17 = 55 * var15 - 22 * var8 - var2 - 21;
                    int var18 = this.cells[var16 * this.cols + var15] & 255;
                    if (var18 >= 14) {
                        var18 = 0;
                    }

                    if (ROTATABLE[var18] && ((var15 ^ var16) & 1) != 0) {
                        G2D.drawRegion(var1, this.tileImages[var18], 0, var7, 31, 15, 3, var17, var9 + var7, 20);
                    } else {
                        G2D.drawRegion(var1, this.tileImages[var18], 0, 0, 31, 40, 0, var17, var9, 20);
                    }
                }
            }
        }

    }

    public int tileAt(int var1, int var2) {
        if (var1 >= 0 && var2 >= 0) {
            int var3 = var1 / 16;
            int var4 = var2 / 16;
            return var3 < this.cols && var4 < this.rows ? this.cells[var4 * this.cols + var3] & 255 : -1;
        } else {
            return -1;
        }
    }

    public boolean isWalkable(int var1, int var2) {
        int var3 = this.tileAt(var1, var2);
        return var3 >= 0 && var3 < WALKABLE.length ? WALKABLE[var3] : false;
    }

    public void clearSolids() {
        this.solidCount = 0;
    }

    public int addSolid(int var1, int var2, int var3) {
        if (this.solidCount >= 28) {
            return -1;
        } else {
            int var4 = this.solidCount++;
            this.solidX[var4] = var1;
            this.solidY[var4] = var2;
            this.solidR[var4] = var3;
            this.solidOn[var4] = true;
            return var4;
        }
    }

    public void setSolidOn(int var1, boolean var2) {
        if (var1 >= 0 && var1 < this.solidCount) {
            this.solidOn[var1] = var2;
        }

    }

    public boolean hitsSolid(int var1, int var2, int var3) {
        int var4 = var3 > 0 ? var3 : 0;

        for(int var5 = 0; var5 < this.solidCount; ++var5) {
            if (this.solidOn[var5]) {
                int var6 = this.solidR[var5] + var4;
                int var7 = var1 - this.solidX[var5];
                int var8 = var2 - this.solidY[var5];
                if (var7 * var7 + var8 * var8 < var6 * var6) {
                    return true;
                }
            }
        }

        return false;
    }

    public void carveWalkable(int var1, int var2, int var3) {
        int var4 = var3 * var3;
        int var5 = (var1 - var3) / 16;
        int var6 = (var1 + var3) / 16;
        int var7 = (var2 - var3) / 16;
        int var8 = (var2 + var3) / 16;
        if (var5 < 0) {
            var5 = 0;
        }

        if (var7 < 0) {
            var7 = 0;
        }

        if (var6 >= this.cols) {
            var6 = this.cols - 1;
        }

        if (var8 >= this.rows) {
            var8 = this.rows - 1;
        }

        for(int var9 = var7; var9 <= var8; ++var9) {
            for(int var10 = var5; var10 <= var6; ++var10) {
                int var11 = IsoMath.cellCenterX(var10);
                int var12 = IsoMath.cellCenterY(var9);
                int var13 = var11 - var1;
                int var14 = var12 - var2;
                if (var13 * var13 + var14 * var14 <= var4) {
                    int var15 = var9 * this.cols + var10;
                    int var16 = this.cells[var15] & 255;
                    if (var16 == 6 || var16 == 12) {
                        this.cells[var15] = 3;
                    }
                }
            }
        }

    }

    public boolean isBush(int var1, int var2) {
        return this.tileAt(var1, var2) == 7;
    }

    public boolean isRiver(int var1, int var2) {
        return this.tileAt(var1, var2) == 2;
    }

    public int getSpotCount() {
        return this.spotCount;
    }

    public int spotType(int var1) {
        return this.spots[var1 * 6] & 255;
    }

    public int spotX(int var1) {
        int var2 = IsoMath.cellCenterX(this.spots[var1 * 6 + 1] & 255);
        return var2 + this.spots[var1 * 6 + 4];
    }

    public int spotY(int var1) {
        int var2 = IsoMath.cellCenterY(this.spots[var1 * 6 + 2] & 255);
        return var2 + this.spots[var1 * 6 + 5];
    }

    public int spotParam(int var1) {
        return this.spots[var1 * 6 + 3] & 255;
    }

    public int getPixelWidth() {
        return this.cols * 16;
    }

    public int getPixelHeight() {
        return this.rows * 16;
    }

    public int getCols() {
        return this.cols;
    }

    public int getRows() {
        return this.rows;
    }

    private static byte[] readFully(String var0) throws IOException {
        InputStream var1 = TileMap.class.getResourceAsStream(var0);
        if (var1 == null) {
            throw new IOException("resource missing: " + var0);
        } else {
            byte[] var5;
            try {
                ByteArrayOutputStream var2 = new ByteArrayOutputStream();
                byte[] var3 = new byte[256];

                int var4;
                while((var4 = var1.read(var3)) >= 0) {
                    var2.write(var3, 0, var4);
                }

                var5 = var2.toByteArray();
            } finally {
                try {
                    var1.close();
                } catch (IOException var12) {
                }

            }

            return var5;
        }
    }
}
