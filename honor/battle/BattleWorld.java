package honor.battle;

import honor.core.FMath;
import honor.core.KeyInput;
import honor.core.MatchConfig;
import honor.entity.Crystal;
import honor.entity.Entity;
import honor.entity.Hero;
import honor.entity.Jungle;
import honor.entity.Minion;
import honor.entity.Tower;
import honor.hero.HeroDef;
import honor.map.TileMap;
import honor.skill.SkillSystem;
import honor.ui.Hud;
import honor.util.Log;
import honor.util.Res;
import honor.util.SettingsStore;
import honor.util.Sfx;
import java.io.IOException;
import java.io.InputStream;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import honor.G2D;
import honor.Img;

public class BattleWorld {
    public static final int RESULT_NONE = 0;
    public static final int RESULT_BLUE_WIN = 1;
    public static final int RESULT_RED_WIN = 2;
    public static final int RESULT_DRAW = 3;
    private static final int MINION_POOL = 48;
    private static final int FLOAT_POOL = 24;
    private static final int FX_POOL = 40;
    private static final int TOWER_MAX = 24;
    private static final int JUNGLE_MAX = 16;
    private static final int SPAWN_INTERVAL = 100;
    private static final int FIRST_SPAWN_DELAY = 75;
    private static final int SIEGE_FROM_WAVE = 8;
    private static final int LANE_COUNT = 3;
    private static final int MID_LANE = 1;
    private static final int FOUNTAIN_R2 = 12100;
    private static final int FOUNTAIN_HEAL_EVERY = 10;
    private static final int FOUNTAIN_HEAL_PCT = 4;
    private static final int REVIVE_BASE = 45;
    private static final int REVIVE_PER_LEVEL = 8;
    private static final int PASSIVE_GOLD_EVERY = 30;
    private static final int PASSIVE_EXP = 3;
    private static final int TOWER_WAVE_R2 = 10404;
    private static final int AI_AGGRO_R2 = 9216;
    private static final int AI_LANE_CORRIDOR = 72;
    private static final int AI_FIGHT_R2 = 7744;
    private static final int TOWER_RANGE_PREVIEW = 154;
    private static final int BUSH_REVEAL_R2 = 1600;
    private static final int REVEAL_FRAMES = 15;
    private static final int OVERLORD_WAVE_BONUS = 10;
    private static final int OVERLORD_BUFF_FRAMES = 900;
    private static final int CRYSTAL_SFX_CD = 90;
    private final TileMap map;
    private final MatchConfig config;
    private final int teamSize;
    private final int heroCount;
    private final Hero[] heroes;
    private Hero local;
    private final int localIndex;
    private int viewIndex;
    private final boolean onlineMode;
    private int onlineHumanMask;
    private KeyInput[] onlineInputs;
    private final Tower[] towers;
    private final Crystal[] crystals;
    private int[] towerSolidIdx;
    private final int[] crystalSolidIdx;
    private static final int TOWER_SOLID_R = 2;
    private static final int CRYSTAL_SOLID_R = 8;
    private static final int PATH_SOLID_CLEAR = 16;
    private final int[] fountainX;
    private final int[] fountainY;
    private final Minion[] minions;
    private final Jungle[] jungles;
    private final DamageText[] floaters;
    private final AttackFx[] attackFx;
    private final int[][] pathBlue;
    private final int[][] pathRed;
    private int spawnTimer;
    private int wave;
    private int result;
    private final int[] reviveTimer;
    private final int[] teamKills;
    private final int perLane;
    private final int structureHpPct;
    private final int passiveGold;
    private final int reviveBase;
    private final int bountyPct;
    private boolean shopOpen;
    private int shopPage;
    private boolean skillUpgradeMode;
    private int onlineSkillSuppress;
    private boolean localAsAi;
    private int overlordTeam;
    private int overlordLeft;
    private boolean firstBloodDone;
    private int crystalSfxCd;
    private boolean announcePlayed;
    private String deathKillerName;
    private int deathBlowDamage;
    private int deathDamageTaken;
    private String lastHitName;
    private int lastHitDamage;
    private final Hud hud;

    public BattleWorld(TileMap var1, MatchConfig var2) throws IOException {
        this(var1, var2, 0, false, 0);
    }

    public BattleWorld(TileMap var1, MatchConfig var2, int var3, boolean var4, int var5) throws IOException {
        this.crystals = new Crystal[2];
        this.crystalSolidIdx = new int[2];
        this.fountainX = new int[2];
        this.fountainY = new int[2];
        this.minions = new Minion[48];
        this.floaters = new DamageText[24];
        this.attackFx = new AttackFx[40];
        this.pathBlue = new int[3][];
        this.pathRed = new int[3][];
        this.result = 0;
        this.teamKills = new int[2];
        this.overlordTeam = -1;
        this.deathKillerName = "";
        this.lastHitName = "";
        this.hud = new Hud();
        this.map = var1;
        this.config = var2;
        this.teamSize = var2.teamSize;
        this.heroCount = this.teamSize * 2;
        if (var3 < 0 || var3 >= this.heroCount) {
            var3 = 0;
        }

        this.localIndex = var3;
        this.viewIndex = var3;
        this.onlineMode = var4;
        this.onlineHumanMask = var5;
        this.heroes = new Hero[this.heroCount];
        this.reviveTimer = new int[this.heroCount];
        this.perLane = this.teamSize >= 5 ? 1 : 2;
        if (this.teamSize >= 5) {
            this.structureHpPct = 100;
            this.passiveGold = 2;
            this.reviveBase = 50;
            this.bountyPct = 90;
        } else if (this.teamSize >= 3) {
            this.structureHpPct = 82;
            this.passiveGold = 3;
            this.reviveBase = 36;
            this.bountyPct = 110;
        } else {
            this.structureHpPct = 65;
            this.passiveGold = 4;
            this.reviveBase = 28;
            this.bountyPct = 130;
        }

        BufferedImage var6 = Res.loadImage("/res/img/towers.png");
        BufferedImage var7 = Img.subImage(var6, 0, 0, 28, 48, 0);
        BufferedImage var8 = Img.subImage(var6, 28, 0, 28, 48, 0);
        BufferedImage var9 = Res.loadImage("/res/img/minion_blue.png");
        BufferedImage var10 = Res.loadImage("/res/img/minion_red.png");
        BufferedImage var11 = Res.loadImage("/res/img/jungle.png");
        BufferedImage var12 = Res.loadImage("/res/img/crystal.png");
        BufferedImage var13 = Img.subImage(var12, 0, 0, 36, 44, 0);
        BufferedImage var14 = Img.subImage(var12, 36, 0, 36, 44, 0);
        this.loadPaths();
        this.readFountains();
        this.openBaseExits();
        this.towers = this.buildTowers(var7, var8);
        this.jungles = this.buildJungles(var11);
        this.crystals[0] = new Crystal(this.fountainX[0], this.fountainY[0], 0, var13);
        this.crystals[1] = new Crystal(this.fountainX[1], this.fountainY[1], 1, var14);
        this.scaleStructure(this.crystals[0]);
        this.scaleStructure(this.crystals[1]);
        this.registerStructureSolids();
        this.softPathsAroundSolids();
        this.buildHeroes();
        this.local = this.heroes[this.localIndex];

        for(int var15 = 0; var15 < 48; ++var15) {
            this.minions[var15] = new Minion();
            this.minions[var15].setSheets(var9, var10);
            this.minions[var15].terrain = var1;
        }

        for(int var16 = 0; var16 < 24; ++var16) {
            this.floaters[var16] = new DamageText();
        }

        for(int var17 = 0; var17 < 40; ++var17) {
            this.attackFx[var17] = new AttackFx();
        }

        this.spawnTimer = 75;
        this.wave = 0;
        this.shopOpen = false;
        this.shopPage = 0;
        this.skillUpgradeMode = false;
        this.announcePlayed = false;

        for(int var18 = 0; var18 < this.heroCount; ++var18) {
            if (var4 && (this.onlineHumanMask & 1 << var18) == 0 || !var4 && (this.heroes[var18] != this.local || this.localAsAi)) {
                this.heroes[var18].autoSpendSkillPoints();
            }
        }

        Log.info("BattleWorld " + this.teamSize + "v" + this.teamSize + " towers=" + this.towers.length + " jungles=" + this.jungles.length);
    }

    private void openBaseExits() {
        for(int var1 = 0; var1 < 2; ++var1) {
            this.map.carveWalkable(this.fountainX[var1], this.fountainY[var1], 48);
            int var2 = var1 == 0 ? -1 : 1;

            for(int var3 = 1; var3 <= 5; ++var3) {
                int var4 = this.fountainX[var1] + var2 * 16 * var3;
                int var5 = this.fountainY[var1] + var2 * 16 * var3;
                this.map.carveWalkable(var4, var5, 20);
            }
        }

        for(int var6 = 0; var6 < 3; ++var6) {
            if (this.pathBlue[var6] != null && this.pathBlue[var6].length >= 2) {
                this.map.carveWalkable(this.pathBlue[var6][0], this.pathBlue[var6][1], 22);
            }

            if (this.pathRed[var6] != null && this.pathRed[var6].length >= 2) {
                this.map.carveWalkable(this.pathRed[var6][0], this.pathRed[var6][1], 22);
            }
        }

        for(int var7 = 0; var7 < this.map.getSpotCount(); ++var7) {
            if (this.map.spotType(var7) == 0) {
                this.map.carveWalkable(this.map.spotX(var7), this.map.spotY(var7), 26);
            }
        }

    }

    private void readFountains() {
        this.fountainX[0] = this.map.getPixelWidth() - 64;
        this.fountainY[0] = this.map.getPixelHeight() - 64;
        this.fountainX[1] = 64;
        this.fountainY[1] = 64;

        for(int var1 = 0; var1 < this.map.getSpotCount(); ++var1) {
            if (this.map.spotType(var1) == 1) {
                int var2 = this.map.spotParam(var1) & 1;
                this.fountainX[var2] = this.map.spotX(var1);
                this.fountainY[var2] = this.map.spotY(var1);
            }
        }

    }

    private Tower[] buildTowers(BufferedImage var1, BufferedImage var2) {
        Tower[] var3 = new Tower[24];
        int var4 = 0;

        for(int var5 = 0; var5 < this.map.getSpotCount() && var4 < 24; ++var5) {
            if (this.map.spotType(var5) == 0) {
                int var6 = this.map.spotParam(var5);
                int var7 = var6 >> 4 & 15;
                int var8 = var6 & 15;
                if (var7 <= 1 && var8 <= 2) {
                    BufferedImage var9 = var7 == 0 ? var1 : var2;
                    Tower var10 = new Tower(this.map.spotX(var5), this.map.spotY(var5), var7, var9, var8);
                    this.scaleStructure(var10);
                    var3[var4++] = var10;
                }
            }
        }

        Tower[] var11 = new Tower[var4];
        System.arraycopy(var3, 0, var11, 0, var4);
        return var11;
    }

    private int[] snapWalkableXY(int var1, int var2) {
        if (this.map.isWalkable(var1, var2) && !this.map.hitsSolid(var1, var2, 8)) {
            return new int[]{var1, var2};
        } else {
            byte var3 = 16;

            for(int var4 = var3; var4 <= var3 * 3; var4 += var3) {
                for(int var5 = -var4; var5 <= var4; var5 += var3) {
                    for(int var6 = -var4; var6 <= var4; var6 += var3) {
                        int var7 = var1 + var6;
                        int var8 = var2 + var5;
                        if (this.map.isWalkable(var7, var8) && !this.map.hitsSolid(var7, var8, 8)) {
                            return new int[]{var7, var8};
                        }
                    }
                }
            }

            return new int[]{var1, var2};
        }
    }

    private void scaleStructure(Entity var1) {
        var1.maxHp = var1.maxHp * this.structureHpPct / 100;
        var1.hp = var1.maxHp;
    }

    private void registerStructureSolids() {
        this.map.clearSolids();
        this.towerSolidIdx = new int[this.towers.length];

        for(int var1 = 0; var1 < this.towers.length; ++var1) {
            Tower var2 = this.towers[var1];
            this.towerSolidIdx[var1] = this.map.addSolid(var2.x, var2.y, 2);
        }

        this.crystalSolidIdx[0] = this.map.addSolid(this.crystals[0].x, this.crystals[0].y, 8);
        this.crystalSolidIdx[1] = this.map.addSolid(this.crystals[1].x, this.crystals[1].y, 8);
    }

    private void syncStructureSolids() {
        if (this.towerSolidIdx != null) {
            for(int var1 = 0; var1 < this.towers.length; ++var1) {
                this.map.setSolidOn(this.towerSolidIdx[var1], this.towers[var1].alive);
            }
        }

        this.map.setSolidOn(this.crystalSolidIdx[0], this.crystals[0].alive);
        this.map.setSolidOn(this.crystalSolidIdx[1], this.crystals[1].alive);
    }

    private void softPathsAroundSolids() {
        for(int var1 = 0; var1 < 3; ++var1) {
            this.softOnePath(this.pathBlue[var1]);
            this.softOnePath(this.pathRed[var1]);
        }

    }

    private void softOnePath(int[] var1) {
        if (var1 != null) {
            int var2 = var1.length / 2;

            for(int var3 = 0; var3 < var2; ++var3) {
                int var4 = var1[var3 * 2];
                int var5 = var1[var3 * 2 + 1];
                if (!this.map.isWalkable(var4, var5) || this.map.hitsSolid(var4, var5, 16)) {
                    int var6 = 0;
                    int var7 = 0;
                    if (var3 + 1 < var2) {
                        var6 = var1[(var3 + 1) * 2] - var4;
                        var7 = var1[(var3 + 1) * 2 + 1] - var5;
                    } else if (var3 > 0) {
                        var6 = var4 - var1[(var3 - 1) * 2];
                        var7 = var5 - var1[(var3 - 1) * 2 + 1];
                    }

                    int var8 = FMath.isqrt(var6 * var6 + var7 * var7);
                    if (var8 < 1) {
                        var8 = 1;
                    }

                    int var9 = var6 * 16 / var8;
                    int var10 = var7 * 16 / var8;
                    int var11 = -var10;
                    int var12 = var9;
                    boolean var13 = false;

                    for(int var14 = 12; var14 <= 48 && !var13; var14 += 4) {
                        int[][] var15 = new int[][]{{var4 + var11 * var14 / 16, var5 + var12 * var14 / 16}, {var4 - var11 * var14 / 16, var5 - var12 * var14 / 16}, {var4 + var9 * var14 / 16, var5 + var10 * var14 / 16}, {var4 - var9 * var14 / 16, var5 - var10 * var14 / 16}, {var4 + var14, var5}, {var4 - var14, var5}, {var4, var5 + var14}, {var4, var5 - var14}, {var4 + var14, var5 + var14}, {var4 - var14, var5 + var14}, {var4 + var14, var5 - var14}, {var4 - var14, var5 - var14}};

                        for(int var16 = 0; var16 < var15.length; ++var16) {
                            int var17 = var15[var16][0];
                            int var18 = var15[var16][1];
                            if (this.map.isWalkable(var17, var18) && !this.map.hitsSolid(var17, var18, 16)) {
                                var1[var3 * 2] = var17;
                                var1[var3 * 2 + 1] = var18;
                                var13 = true;
                                break;
                            }
                        }
                    }
                }
            }

        }
    }

    private Jungle[] buildJungles(BufferedImage var1) {
        Jungle[] var2 = new Jungle[16];
        int var3 = 0;

        for(int var4 = 0; var4 < this.map.getSpotCount() && var3 < 16; ++var4) {
            byte var5;
            switch (this.map.spotType(var4)) {
                case 2:
                    var5 = 1;
                    break;
                case 3:
                    var5 = 2;
                    break;
                case 4:
                    var5 = 0;
                    break;
                case 5:
                    var5 = 3;
                    break;
                case 6:
                    var5 = 4;
                    break;
                default:
                    continue;
            }

            int[] var6 = this.snapWalkableXY(this.map.spotX(var4), this.map.spotY(var4));
            Jungle var7 = new Jungle(var5, var6[0], var6[1], var1);
            var7.terrain = this.map;
            var2[var3++] = var7;
        }

        Jungle[] var8 = new Jungle[var3];
        System.arraycopy(var2, 0, var8, 0, var3);
        return var8;
    }

    private void buildHeroes() throws IOException {
        BufferedImage[] var1 = new BufferedImage[19];

        for(int var2 = 0; var2 <= 1; ++var2) {
            int[] var3 = var2 == 0 ? this.config.blueHeroIds : this.config.redHeroIds;
            int[][] var4 = var2 == 0 ? this.pathBlue : this.pathRed;
            Crystal var5 = this.crystals[1 - var2];

            for(int var6 = 0; var6 < this.teamSize; ++var6) {
                int var7 = var3[var6];
                if (var1[var7] == null) {
                    var1[var7] = Res.loadImage(HeroDef.spritePath(var7));
                }

                int var8 = this.laneOf(var6);
                int[] var9 = this.spawnPointFor(var2, var6);
                int var10 = var2 * this.teamSize + var6;
                boolean var11 = this.onlineMode ? (this.onlineHumanMask & 1 << var10) != 0 : var10 == this.localIndex;
                Hero var12 = new Hero(var7, var9[0], var9[1], var2, var1[var7], var11);
                var12.terrain = this.map;
                var12.homeX = var9[0];
                var12.homeY = var9[1];
                var12.pushX = var5.x;
                var12.pushY = var5.y;
                if (var4[var8] != null) {
                    var12.aiPath = var4[var8];
                    var12.aiPathIndex = 0;
                }

                this.heroes[var2 * this.teamSize + var6] = var12;
            }
        }

    }

    private int laneOf(int var1) {
        if (this.teamSize > 1 && var1 != 0) {
            return var1 % 2 == 1 ? 0 : 2;
        } else {
            return 1;
        }
    }

    private int[] spawnPointFor(int var1, int var2) {
        int var3 = var1 == 0 ? -1 : 1;
        int var4 = (var2 - this.teamSize / 2) * 14;
        int[] var5 = new int[]{40, 48, 56, 32, 64, 72};

        for(int var6 = 0; var6 < var5.length; ++var6) {
            int var7 = var5[var6];
            int var8 = this.fountainX[var1] + var3 * var7 + var4 / 2;
            int var9 = this.fountainY[var1] + var3 * var7 - var4;
            if (this.canSpawnAt(var8, var9)) {
                return new int[]{var8, var9};
            }

            var8 = this.fountainX[var1] + var3 * var7 + var4;
            var9 = this.fountainY[var1] + var3 * (var7 - 10);
            if (this.canSpawnAt(var8, var9)) {
                return new int[]{var8, var9};
            }
        }

        for(int var10 = 20; var10 <= 96; var10 += 8) {
            for(int var11 = -2; var11 <= 2; ++var11) {
                int var13 = this.fountainX[var1] + var3 * var10 + var11 * 12;
                int var15 = this.fountainY[var1] + var3 * var10 - var11 * 8;
                if (this.canSpawnAt(var13, var15)) {
                    return new int[]{var13, var15};
                }
            }
        }

        return new int[]{this.fountainX[var1] + var3 * 48, this.fountainY[var1] + var3 * 48};
    }

    private boolean canSpawnAt(int var1, int var2) {
        if (!this.map.isWalkable(var1, var2)) {
            return false;
        } else {
            return !this.map.hitsSolid(var1, var2, 8);
        }
    }

    public void update(KeyInput var1, int var2) {
        if (this.result == 0) {
            this.syncStructureSolids();
            if (!this.announcePlayed) {
                this.announcePlayed = true;
                Sfx.play("enemy5");
            }

            if (this.crystalSfxCd > 0) {
                --this.crystalSfxCd;
            }

            if (!this.onlineMode) {
                this.handlePlayerCommands(var1);
            }

            for(int var3 = 0; var3 < this.heroCount; ++var3) {
                Hero var4 = this.heroes[var3];
                var4.inBush = var4.alive && this.map.isBush(var4.x, var4.y);
            }

            if (this.onlineMode) {
                this.updateOnlineHumans();
            } else if (this.localAsAi) {
                if (this.local.alive) {
                    this.acquireHeroTarget(this.local);
                    if (this.shouldRetreatFromFight(this.local)) {
                        this.local.retreating = true;
                        this.local.target = null;
                    }

                    this.local.holdBack = this.shouldHoldBack(this.local);
                    this.local.updateAi(this.map.getPixelWidth(), this.map.getPixelHeight());
                    ShopCatalog.autoBuyCheapest(this.local);
                }
            } else {
                this.local.updatePlayer(var1, this.map.getPixelWidth(), this.map.getPixelHeight());
                if (this.local.recallLeft > 0) {
                    this.local.tickRecall();
                }

                this.acquireHeroTarget(this.local);
            }

            if (this.local.alive) {
                if (this.local.consumeTauntVoice()) {
                    Sfx.play(HeroDef.voiceName(this.local.getHeroId(), 5));
                } else if (this.local.consumeMoveVoice()) {
                    Sfx.play(HeroDef.voiceName(this.local.getHeroId(), 4));
                }
            }

            int var7 = this.teamSize >= 5 ? 3 : (this.teamSize >= 3 ? 2 : 1);

            for(int var8 = 0; var8 < this.heroCount; ++var8) {
                Hero var5 = this.heroes[var8];
                if ((this.onlineMode || var5 != this.local) && var5.alive && (!this.onlineMode || (this.onlineHumanMask & 1 << var8) == 0)) {
                    boolean var6 = var2 % var7 == var8 % var7;
                    if (var6) {
                        this.acquireHeroTarget(var5);
                        if (this.shouldRetreatFromFight(var5)) {
                            var5.retreating = true;
                            var5.target = null;
                        }

                        var5.holdBack = this.shouldHoldBack(var5);
                    } else if (var5.target != null && (!var5.target.alive || !var5.isEnemy(var5.target))) {
                        var5.target = null;
                    }

                    var5.updateAi(this.map.getPixelWidth(), this.map.getPixelHeight());
                    if (var6) {
                        ShopCatalog.autoBuyCheapest(var5);
                    }
                }
            }

            --this.spawnTimer;
            if (this.spawnTimer <= 0) {
                this.spawnWave();
                this.spawnTimer = 100;
            }

            for(int var9 = 0; var9 < 48; ++var9) {
                Minion var16 = this.minions[var9];
                if (var16.alive) {
                    this.acquireMinionTarget(var16);
                    var16.update(var2);
                }
            }

            for(int var10 = 0; var10 < this.towers.length; ++var10) {
                Tower var17 = this.towers[var10];
                if (var17.alive) {
                    this.acquireTowerTarget(var17);
                    var17.update(var2);
                }
            }

            for(int var11 = 0; var11 < this.jungles.length; ++var11) {
                Jungle var18 = this.jungles[var11];
                if (var18.alive && var18.target == null) {
                    for(int var21 = 0; var21 < this.heroCount; ++var21) {
                        var18.considerAggro(this.heroes[var21]);
                    }
                }

                var18.update(var2);
            }

            for(int var12 = 0; var12 < 2; ++var12) {
                Crystal var19 = this.crystals[var12];
                if (var19.alive) {
                    this.acquireCrystalTarget(var19);
                    var19.update(var2);
                }
            }

            this.castSkills(var1);
            this.resolveAttacks(var1);

            for(int var13 = 0; var13 < 40; ++var13) {
                AttackFx var20 = this.attackFx[var13];
                if (var20.active) {
                    var20.update();
                    if (var20.shouldApplyDamage()) {
                        this.applyPendingArrow(var20);
                    }
                }
            }

            for(int var14 = 0; var14 < 24; ++var14) {
                this.floaters[var14].update();
            }

            this.tickFountain(var2);
            this.tickPassiveIncome(var2);
            this.tickStructurePressure(var2);
            this.tickRevives();

            for(int var15 = 0; var15 < this.heroCount; ++var15) {
                this.heroes[var15].tickMpRegen(var2);
            }

            if (this.overlordLeft > 0) {
                --this.overlordLeft;
                if (this.overlordLeft <= 0) {
                    this.overlordTeam = -1;
                }
            }

            if (!this.crystals[1].alive) {
                this.result = 1;
                this.shopOpen = false;
            } else if (!this.crystals[0].alive) {
                this.result = 2;
                this.shopOpen = false;
            }

        }
    }

    private void castSkills(KeyInput var1) {
        if (this.onlineMode && this.onlineInputs != null) {
            for(int var2 = 0; var2 < this.heroCount; ++var2) {
                if ((this.onlineHumanMask & 1 << var2) != 0 && this.heroes[var2].alive) {
                    KeyInput var3 = this.onlineInputs[var2];
                    if (var3 != null) {
                        if (var3.isPressed(32)) {
                            this.castAndReveal(this.heroes[var2], 0);
                        }

                        if (var3.isPressed(64)) {
                            this.castAndReveal(this.heroes[var2], 1);
                        }

                        if (var3.isPressed(128)) {
                            this.castAndReveal(this.heroes[var2], 2);
                        }
                    }
                }
            }
        } else if (!this.shopOpen && !this.skillUpgradeMode) {
            if (var1.isPressed(32) && !this.tryPreferUpgrade(this.local, 0)) {
                this.castAndReveal(this.local, 0);
            }

            if (var1.isPressed(64) && !this.tryPreferUpgrade(this.local, 1)) {
                this.castAndReveal(this.local, 1);
            }

            if (var1.isPressed(128) && !this.tryPreferUpgrade(this.local, 2)) {
                this.castAndReveal(this.local, 2);
            }
        }

        for(int var5 = 0; var5 < this.heroCount; ++var5) {
            Hero var6 = this.heroes[var5];
            if ((this.localAsAi || var6 != this.local) && var6.alive && (!this.onlineMode || (this.onlineHumanMask & 1 << var5) == 0)) {
                if (var6.skillPoints > 0) {
                    var6.autoSpendSkillPoints();
                }

                int var4 = var6.pickAiSkill();
                if (var4 >= 0) {
                    this.castAndReveal(var6, var4);
                }
            }
        }

    }

    private void castAndReveal(Hero var1, int var2) {
        if (SkillSystem.cast(this, var1, var2)) {
            var1.reveal(15);
            if (var1 == this.local) {
                Sfx.play(HeroDef.voiceName(var1.getHeroId(), 1));
            }
        }

    }

    private void handlePlayerCommands(KeyInput var1) {
        if (!this.local.alive) {
            this.shopOpen = false;
            this.skillUpgradeMode = false;
        } else if (var1.isPressed(1024)) {
            this.shopOpen = !this.shopOpen;
            if (this.shopOpen) {
                this.shopPage = 0;
                this.skillUpgradeMode = false;
            }

        } else {
            if (this.skillUpgradeMode && var1.isPressed(16)) {
                this.skillUpgradeMode = false;
            }

            if (this.shopOpen) {
                if (var1.isPressed(16)) {
                    this.shopPage = (this.shopPage + 1) % 4;
                }

                if (var1.isPressed(32)) {
                    this.buyShopSlot(0);
                }

                if (var1.isPressed(64)) {
                    this.buyShopSlot(1);
                }

                if (var1.isPressed(128)) {
                    this.buyShopSlot(2);
                }

            } else if (this.skillUpgradeMode) {
                if (var1.isPressed(32) && this.local.upgradeSkill(0)) {
                    Sfx.play("ui_ok");
                }

                if (var1.isPressed(64) && this.local.upgradeSkill(1)) {
                    Sfx.play("ui_ok");
                }

                if (var1.isPressed(128) && this.local.upgradeSkill(2)) {
                    Sfx.play("ui_ok");
                }

                if (this.local.skillPoints <= 0) {
                    this.skillUpgradeMode = false;
                }

            } else {
                if (var1.isPressed(512) && this.local.recallLeft <= 0) {
                    this.local.startRecall();
                    if (this.local.recallLeft > 0) {
                        Sfx.play("recall");
                    }
                }

            }
        }
    }

    public void updateOnline(KeyInput[] var1, int var2, int var3) {
        this.onlineInputs = var1;
        this.onlineHumanMask = var2;
        KeyInput var4 = var1[this.localIndex];
        if (var4 == null && var1.length > 0) {
            var4 = var1[0];
        }

        this.update(var4, var3);
    }

    public void setViewHero(int var1) {
        if (var1 >= 0 && var1 < this.heroCount && this.heroes[var1] != null) {
            this.viewIndex = var1;
            this.local = this.heroes[var1];
        }
    }

    public int getViewHeroSlot() {
        return this.viewIndex;
    }

    private void updateOnlineHumans() {
        if (this.onlineInputs != null) {
            for(int var1 = 0; var1 < this.heroCount; ++var1) {
                if ((this.onlineHumanMask & 1 << var1) != 0) {
                    Hero var2 = this.heroes[var1];
                    KeyInput var3 = this.onlineInputs[var1];
                    if (var3 != null && var2.alive) {
                        this.handleOnlineCommand(var2, var3);
                        var2.updatePlayer(var3, this.map.getPixelWidth(), this.map.getPixelHeight());
                        if (var2.recallLeft > 0) {
                            var2.tickRecall();
                        }

                        this.acquireHeroTarget(var2);
                    }
                }
            }

        }
    }

    private void handleOnlineCommand(Hero var1, KeyInput var2) {
        if (var2.isPressed(512) && var1.recallLeft <= 0) {
            var1.startRecall();
        }

        int var3 = var2.networkCmd();
        if (var3 >= 1 && var3 < 4) {
            var1.upgradeSkill(var3 - 1);
        } else if (var3 >= 16 && var3 < 28) {
            ShopCatalog.tryBuy(var1, var3 - 16);
        }

    }

    private boolean tryPreferUpgrade(Hero var1, int var2) {
        if (var1 != null && var1.skillPoints > 0 && var1.canUpgradeSkill(var2)) {
            if (var1.upgradeSkill(var2)) {
                Sfx.play("ui_ok");
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public void updateOnlineLocalUi(KeyInput var1) {
        if (this.onlineMode && this.local != null) {
            if (this.local.alive && this.result == 0) {
                if (var1.isPressed(1024)) {
                    this.shopOpen = !this.shopOpen;
                    if (this.shopOpen) {
                        this.shopPage = 0;
                        this.skillUpgradeMode = false;
                    }

                } else if (this.shopOpen) {
                    if (var1.isPressed(16)) {
                        this.shopPage = (this.shopPage + 1) % 4;
                    }

                    if (var1.isPressed(32)) {
                        this.requestBuy(var1, 0);
                    }

                    if (var1.isPressed(64)) {
                        this.requestBuy(var1, 1);
                    }

                    if (var1.isPressed(128)) {
                        this.requestBuy(var1, 2);
                    }

                } else if (this.skillUpgradeMode) {
                    if (var1.isPressed(16)) {
                        this.skillUpgradeMode = false;
                    } else {
                        if (var1.isPressed(32)) {
                            this.requestUpgrade(var1, 0);
                        }

                        if (var1.isPressed(64)) {
                            this.requestUpgrade(var1, 1);
                        }

                        if (var1.isPressed(128)) {
                            this.requestUpgrade(var1, 2);
                        }

                        if (this.local.skillPoints <= 0) {
                            this.skillUpgradeMode = false;
                        }

                    }
                } else {
                    if (this.local.skillPoints > 0) {
                        if (var1.isPressed(32) && this.local.canUpgradeSkill(0)) {
                            this.requestUpgrade(var1, 0);
                            this.onlineSkillSuppress |= 32;
                        } else if (var1.isPressed(64) && this.local.canUpgradeSkill(1)) {
                            this.requestUpgrade(var1, 1);
                            this.onlineSkillSuppress |= 64;
                        } else if (var1.isPressed(128) && this.local.canUpgradeSkill(2)) {
                            this.requestUpgrade(var1, 2);
                            this.onlineSkillSuppress |= 128;
                        }
                    }

                }
            } else {
                this.shopOpen = false;
                this.skillUpgradeMode = false;
            }
        }
    }

    public int onlineUiSuppressMask() {
        int var1 = 7168 | this.onlineSkillSuppress;
        this.onlineSkillSuppress = 0;
        if (this.shopOpen || this.skillUpgradeMode) {
            var1 |= 240;
        }

        return var1;
    }

    private void requestBuy(KeyInput var1, int var2) {
        int var3 = ShopCatalog.itemAt(this.shopPage, var2);
        if (var3 >= 0 && ShopCatalog.canBuy(this.local, var3)) {
            var1.queueCommand(16 + var3);
            Sfx.play("ui_ok");
        }

    }

    private void requestUpgrade(KeyInput var1, int var2) {
        if (this.local.canUpgradeSkill(var2)) {
            var1.queueCommand(1 + var2);
            Sfx.play("ui_ok");
        }

    }

    public boolean requestOnlineUpgrade(KeyInput var1, int var2) {
        if (this.local != null && this.local.alive && this.local.canUpgradeSkill(var2)) {
            var1.queueCommand(1 + var2);
            Sfx.play("ui_ok");
            return true;
        } else {
            return false;
        }
    }

    public void applyRemoteResult(int var1) {
        if (this.result == 0) {
            if (var1 == 0) {
                this.result = 1;
            } else if (var1 == 1) {
                this.result = 2;
            } else {
                this.result = 3;
            }

            this.shopOpen = false;
            this.skillUpgradeMode = false;
        }
    }

    private void buyShopSlot(int var1) {
        int var2 = ShopCatalog.itemAt(this.shopPage, var1);
        if (var2 >= 0 && ShopCatalog.tryBuy(this.local, var2)) {
            Sfx.play("ui_ok");
        }

    }

    public boolean isSkillUpgradeMode() {
        return this.skillUpgradeMode;
    }

    public boolean upgradeLocalSkill(int var1) {
        if (this.local != null && this.local.alive && this.local.upgradeSkill(var1)) {
            Sfx.play("ui_ok");
            if (this.local.skillPoints <= 0) {
                this.skillUpgradeMode = false;
            }

            return true;
        } else {
            return false;
        }
    }

    private boolean isVisible(Entity var1, Entity var2) {
        if (var2.unitType != 1) {
            return true;
        } else {
            Hero var3 = (Hero)var2;
            if (!var3.isHidden()) {
                return true;
            } else {
                return var1.team == var3.team ? true : this.teamHasBushVision(var1.team, var3);
            }
        }
    }

    private boolean teamHasBushVision(int var1, Hero var2) {
        for(int var3 = 0; var3 < this.heroCount; ++var3) {
            Hero var4 = this.heroes[var3];
            if (var4.alive && var4.team == var1 && var4.dist2To(var2) <= 1600) {
                return true;
            }
        }

        for(int var5 = 0; var5 < 48; ++var5) {
            Minion var7 = this.minions[var5];
            if (var7.alive && var7.team == var1 && var7.dist2To(var2) <= 1600) {
                return true;
            }
        }

        for(int var6 = 0; var6 < this.towers.length; ++var6) {
            Tower var8 = this.towers[var6];
            if (var8.alive && var8.team == var1 && var8.dist2To(var2) <= 1600) {
                return true;
            }
        }

        return false;
    }

    private void acquireMinionTarget(Minion var1) {
        Object var2 = null;
        int var3 = Integer.MAX_VALUE;
        short var4 = 3136;
        if (var1.target == null || var1.target.unitType != 2 || !var1.isEnemy(var1.target) || !var1.isLaneTarget(var1.target) || var1.dist2To(var1.target) > var4) {
            for(int var5 = 0; var5 < 48; ++var5) {
                Minion var6 = this.minions[var5];
                if (var1.isEnemy(var6) && var1.isLaneTarget(var6)) {
                    int var7 = var1.dist2To(var6);
                    int var8 = var6.maxHp <= 0 ? 100 : var6.hp * 100 / var6.maxHp;
                    int var9 = var7 + var8 * 3;
                    if (var7 <= var4 && var9 < var3) {
                        var3 = var9;
                        var2 = var6;
                    }
                }
            }

            if (var2 != null) {
                var1.target = (Entity)var2;
            } else {
                for(int var10 = 0; var10 < this.towers.length; ++var10) {
                    Tower var13 = this.towers[var10];
                    if (var1.isEnemy(var13) && var13.alive && var1.isLaneTarget(var13)) {
                        int var16 = var1.dist2To(var13);
                        if (var16 <= 4096 && var16 < var3) {
                            var3 = var16;
                            var2 = var13;
                        }
                    }
                }

                if (var2 != null) {
                    var1.target = (Entity)var2;
                } else {
                    for(int var11 = 0; var11 < this.heroCount; ++var11) {
                        Hero var14 = this.heroes[var11];
                        if (var1.isEnemy(var14) && var14.alive && var1.isLaneTarget(var14) && this.isVisible(var1, var14)) {
                            int var17 = var1.dist2To(var14);
                            if (var17 <= var4 && var17 < var3) {
                                var3 = var17;
                                var2 = var14;
                            }
                        }
                    }

                    Crystal var12 = this.enemyCrystalOf(var1.team);
                    if (var12.alive && !this.isCrystalShielded(var12) && var1.isLaneTarget(var12)) {
                        int var15 = var1.dist2To(var12);
                        if (var15 <= 1600 && var15 < var3) {
                            var2 = var12;
                        }
                    }

                    var1.target = (Entity)var2;
                }
            }
        }
    }

    private void acquireTowerTarget(Tower var1) {
        Minion var2 = null;
        int var3 = Integer.MAX_VALUE;

        for(int var4 = 0; var4 < 48; ++var4) {
            Minion var5 = this.minions[var4];
            if (var1.isEnemy(var5) && var1.inRange(var5)) {
                int var6 = var1.dist2To(var5);
                if (var6 < var3) {
                    var3 = var6;
                    var2 = var5;
                }
            }
        }

        if (var2 != null) {
            var1.target = var2;
        } else {
            for(int var7 = 0; var7 < this.heroCount; ++var7) {
                Hero var8 = this.heroes[var7];
                if (var1.isEnemy(var8) && var8.alive && var1.inRange(var8) && this.isVisible(var1, var8)) {
                    var1.target = var8;
                    return;
                }
            }

            var1.target = null;
        }
    }

    private void acquireCrystalTarget(Crystal var1) {
        Minion var2 = null;
        int var3 = Integer.MAX_VALUE;

        for(int var4 = 0; var4 < 48; ++var4) {
            Minion var5 = this.minions[var4];
            if (var1.isEnemy(var5) && var1.inRange(var5)) {
                int var6 = var1.dist2To(var5);
                if (var6 < var3) {
                    var3 = var6;
                    var2 = var5;
                }
            }
        }

        if (var2 != null) {
            var1.target = var2;
        } else {
            for(int var7 = 0; var7 < this.heroCount; ++var7) {
                Hero var8 = this.heroes[var7];
                if (var1.isEnemy(var8) && var8.alive && var1.inRange(var8) && this.isVisible(var1, var8)) {
                    var1.target = var8;
                    return;
                }
            }

            var1.target = null;
        }
    }

    private void acquireHeroTarget(Hero var1) {
        if (!var1.alive) {
            var1.target = null;
        } else {
            Object var2 = null;
            int var3 = Integer.MAX_VALUE;
            boolean var4 = this.onlineMode ? !var1.isPlayerControlled() : var1 != this.local || this.localAsAi;
            if (var4 && !var1.canSearchAiTarget()) {
                var1.target = null;
            } else {
                for(int var5 = 0; var5 < this.heroCount; ++var5) {
                    Hero var6 = this.heroes[var5];
                    if (var1.isEnemy(var6) && var6.alive && this.isVisible(var1, var6)) {
                        int var7 = var1.dist2To(var6);
                        if ((var7 <= 9216 || var1.inRange(var6)) && (!var4 || var1.inRange(var6) || var1.isNearAiPath(var6.x, var6.y, 72)) && (!var4 || var1.hasDirectWalkingLineTo(var6.x, var6.y)) && (!var4 || !this.isUnsafeTowerDive(var1, var6))) {
                            int var8 = var6.maxHp <= 0 ? 100 : var6.hp * 100 / var6.maxHp;
                            int var9;
                            if (var8 <= 35) {
                                var9 = var7 / 5;
                            } else if (var8 <= 55) {
                                var9 = var7 / 2;
                            } else {
                                var9 = var7 + 200;
                            }

                            if (var1.target == var6) {
                                var9 -= 360;
                            }

                            for(int var10 = 0; var10 < this.heroCount; ++var10) {
                                Hero var11 = this.heroes[var10];
                                if (var11 != var1 && var11.alive && var11.team == var1.team && var11.target == var6 && FMath.dist2(var1.x, var1.y, var11.x, var11.y) <= 7744) {
                                    var9 -= 520;
                                    break;
                                }
                            }

                            if (var6.target == var1) {
                                var9 -= 180;
                            }

                            if (var9 < var3) {
                                var3 = var9;
                                var2 = var6;
                            }
                        }
                    }
                }

                for(int var12 = 0; var12 < 48; ++var12) {
                    Minion var14 = this.minions[var12];
                    if (var1.isEnemy(var14) && var14.alive) {
                        int var18 = var1.dist2To(var14);
                        if ((var18 <= 9216 || var1.inRange(var14)) && (!var4 || var1.inRange(var14) || var1.isNearAiPath(var14.x, var14.y, 72)) && (!var4 || var1.hasDirectWalkingLineTo(var14.x, var14.y)) && (!var4 || !this.isUnsafeTowerDive(var1, var14))) {
                            int var22 = var14.maxHp <= 0 ? 100 : var14.hp * 100 / var14.maxHp;
                            int var26 = var18 + var22 * 2 + 60;
                            if (var14.hp <= var1.atk) {
                                var26 -= 640;
                            }

                            if (var1.target == var14) {
                                var26 -= 240;
                            }

                            if (var26 < var3) {
                                var3 = var26;
                                var2 = var14;
                            }
                        }
                    }
                }

                int var13 = var1.maxHp <= 0 ? 100 : var1.hp * 100 / var1.maxHp;
                if (var2 == null && var13 >= 55) {
                    for(int var15 = 0; var15 < this.jungles.length; ++var15) {
                        Jungle var19 = this.jungles[var15];
                        if (var19.alive) {
                            int var23 = var1.dist2To(var19);
                            if (var23 <= 3136 || var1.inRange(var19)) {
                                int var27 = var23 + 900;
                                if (var27 < var3) {
                                    var3 = var27;
                                    var2 = var19;
                                }
                            }
                        }
                    }
                }

                for(int var16 = 0; var16 < this.towers.length; ++var16) {
                    Tower var20 = this.towers[var16];
                    if (var1.isEnemy(var20) && var20.alive && this.hasAlliedMinionNear(var1.team, var20.x, var20.y, 10404)) {
                        int var24 = var1.dist2To(var20);
                        if (var24 <= 9216 || var1.inRange(var20)) {
                            int var28 = var24 + 700;
                            if (var1.target == var20) {
                                var28 -= 180;
                            }

                            if (var28 < var3) {
                                var3 = var28;
                                var2 = var20;
                            }
                        }
                    }
                }

                Crystal var17 = this.enemyCrystalOf(var1.team);
                if (var17.alive && !this.isCrystalShielded(var17) && this.hasAlliedMinionNear(var1.team, var17.x, var17.y, 10404)) {
                    int var21 = var1.dist2To(var17);
                    if (var21 <= 9216 || var1.inRange(var17)) {
                        int var25 = var21 + 500;
                        if (var25 < var3) {
                            var2 = var17;
                        }
                    }
                }

                var1.target = (Entity)var2;
            }
        }
    }

    private boolean shouldRetreatFromFight(Hero var1) {
        int var2 = var1.maxHp <= 0 ? 100 : var1.hp * 100 / var1.maxHp;
        if (var2 >= 55) {
            return false;
        } else {
            int var3 = 1;
            int var4 = 0;

            for(int var5 = 0; var5 < this.heroCount; ++var5) {
                Hero var6 = this.heroes[var5];
                if (var6 != var1 && var6.alive && FMath.dist2(var1.x, var1.y, var6.x, var6.y) <= 7744) {
                    if (var6.team == var1.team) {
                        ++var3;
                    } else if (this.isVisible(var1, var6)) {
                        ++var4;
                    }
                }
            }

            return var4 > 0 && (var2 <= 35 || var4 > var3);
        }
    }

    private boolean hasAlliedMinionNear(int var1, int var2, int var3, int var4) {
        for(int var5 = 0; var5 < 48; ++var5) {
            Minion var6 = this.minions[var5];
            if (var6.alive && var6.team == var1 && FMath.dist2(var2, var3, var6.x, var6.y) <= var4) {
                return true;
            }
        }

        return false;
    }

    private boolean isUnsafeTowerDive(Hero var1, Entity var2) {
        for(int var3 = 0; var3 < this.towers.length; ++var3) {
            Tower var4 = this.towers[var3];
            if (var4.alive && var4.isEnemy(var1)) {
                int var5 = var4.atkRange + var2.radius + 6;
                if (var4.dist2To(var2) <= var5 * var5 && !this.hasAlliedMinionNear(var1.team, var4.x, var4.y, 10404)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean shouldHoldBack(Hero var1) {
        Tower var2 = null;

        for(int var3 = 0; var3 < this.towers.length; ++var3) {
            Tower var4 = this.towers[var3];
            int var5 = var4.atkRange + var1.radius + 8;
            if (var4.alive && var4.isEnemy(var1) && var4.dist2To(var1) <= var5 * var5) {
                var2 = var4;
                break;
            }
        }

        if (var2 == null) {
            return false;
        } else {
            return !this.hasAlliedMinionNear(var1.team, var2.x, var2.y, 10404);
        }
    }

    private void resolveAttacks(KeyInput var1) {
        for(int var2 = 0; var2 < this.heroCount; ++var2) {
            Hero var3 = this.heroes[var2];
            if (var3.wantsAttack()) {
                if (var3.target.unitType == 3 && !this.hasAlliedMinionNear(var3.team, var3.target.x, var3.target.y, 10404)) {
                    var3.target = null;
                } else if (this.onlineMode || var3 != this.local || SettingsStore.get().isAutoAttack() || var1.isHeld(16) || var1.isPressed(16)) {
                    var3.faceTarget(var3.target);
                    var3.reveal(15);
                    this.fireAttack(var3, var3.target);
                    var3.playAnim(1);
                    if (var3 == this.local) {
                        Sfx.play("atk");
                    }

                    var3.resetAttackCd();
                }
            }
        }

        for(int var5 = 0; var5 < 48; ++var5) {
            Minion var9 = this.minions[var5];
            if (var9.wantsAttack()) {
                this.fireAttack(var9, var9.target);
                var9.resetAttackCd();
            }
        }

        for(int var6 = 0; var6 < this.towers.length; ++var6) {
            Tower var10 = this.towers[var6];
            if (var10.wantsAttack()) {
                Entity var4 = var10.target;
                var10.markAttacking(var4);
                this.fireAttack(var10, var4);
                var10.resetAttackCd();
            }
        }

        for(int var7 = 0; var7 < 2; ++var7) {
            Crystal var11 = this.crystals[var7];
            if (var11.wantsAttack()) {
                this.fireAttack(var11, var11.target);
                var11.resetAttackCd();
            }
        }

        for(int var8 = 0; var8 < this.jungles.length; ++var8) {
            Jungle var12 = this.jungles[var8];
            if (var12.wantsAttack()) {
                this.fireAttack(var12, var12.target);
                var12.resetAttackCd();
            }
        }

    }

    private void fireAttack(Entity var1, Entity var2) {
        if (var1 != null && var2 != null && var2.alive) {
            if (var2.unitType == 5) {
                ((Jungle)var2).onAttackedBy(var1);
            }

            int var3 = var1.team == 0 ? 16769152 : 16748688;
            int var4 = this.rawAttackDamage(var1, var2);
            if (var1.ranged) {
                this.spawnArrowFx(var1, var2, var4, var3);
            } else {
                this.spawnSlashFx(var1, var2, var3);
                this.applyDamageAmount(var1, var2, var4, var3);
            }

        }
    }

    private int rawAttackDamage(Entity var1, Entity var2) {
        int var3 = var1.atk;
        if (var1.unitType == 3 && var2.unitType == 1) {
            var3 += ((Tower)var1).heroDamageBonus(var2);
        }

        if (var1.unitType == 1) {
            Hero var4 = (Hero)var1;
            if (var4.redBuffLeft > 0) {
                var3 += 12;
            }

            var3 += var4.rollPassiveAttackBonus();
        }

        return var3;
    }

    private void applyPendingArrow(AttackFx var1) {
        Entity var2 = var1.victim;
        Entity var3 = var1.attacker;
        int var4 = var1.pendingDamage;
        int var5 = var1.floaterColor;
        var1.markDamageApplied();
        if (var2 != null && var2.alive && var4 > 0) {
            this.applyDamageAmount(var3, var2, var4, var5);
        }

    }

    private void applyDamageAmount(Entity var1, Entity var2, int var3, int var4) {
        if (var2 != null && var2.alive && var3 > 0) {
            if (var2.unitType == 4 && this.isCrystalShielded((Crystal)var2)) {
                this.spawnLabel(var2.x, var2.y, "护盾", 8965375);
            } else {
                if (var1 != null && var1.unitType == 2) {
                    if (var2.unitType == 1) {
                        var3 = var3 * 55 / 100;
                    } else if (var2.unitType == 3 || var2.unitType == 4) {
                        var3 += var3 >> 1;
                        int var5 = ((Minion)var1).kind;
                        if (var5 == 2) {
                            var3 += var3 >> 1;
                        } else if (var5 == 3) {
                            var3 += var3 >> 2;
                        }
                    }
                }

                if (this.wave >= 18 && (var2.unitType == 3 || var2.unitType == 4)) {
                    var3 += var3 >> 2;
                }

                int var7 = var3 * 100 / (100 + var2.def);
                if (var2.unitType == 1) {
                    var7 = var7 * ((Hero)var2).passiveDamageTakenPct() / 100;
                }

                if (var7 < 1) {
                    var7 = 1;
                }

                int var6 = var2.takeDamage(var7);
                if (var6 > 0) {
                    this.spawnFloater(var2.x, var2.y, var6, var4);
                    if (var2 == this.local) {
                        this.deathDamageTaken += var6;
                        this.lastHitDamage = var6;
                        this.lastHitName = this.attackerLabel(var1);
                    }

                    if (var2.unitType == 4 && var2.alive && var2.team == this.local.team && this.crystalSfxCd <= 0) {
                        Sfx.play("crystal");
                        this.crystalSfxCd = 90;
                    }
                }

                if (!var2.alive) {
                    if (var2 == this.local) {
                        this.deathKillerName = this.lastHitName.length() > 0 ? this.lastHitName : this.attackerLabel(var1);
                        this.deathBlowDamage = this.lastHitDamage > 0 ? this.lastHitDamage : var6;
                        Sfx.play(HeroDef.voiceName(this.local.getHeroId(), 2));
                    }

                    this.announceKill(var1, var2);
                    this.grantKillReward(var1, var2);
                }

            }
        }
    }

    private String attackerLabel(Entity var1) {
        if (var1 == null) {
            return "未知";
        } else if (var1.unitType == 1) {
            return ((Hero)var1).getName();
        } else if (var1.unitType == 3) {
            return "防御塔";
        } else if (var1.unitType == 2) {
            int var2 = ((Minion)var1).kind;
            if (var2 == 2) {
                return "炮车";
            } else if (var2 == 3) {
                return "超级兵";
            } else {
                return var2 == 1 ? "远程兵" : "近战兵";
            }
        } else if (var1.unitType == 5) {
            return "野怪";
        } else {
            return var1.unitType == 4 ? "水晶" : "敌人";
        }
    }

    private void announceKill(Entity var1, Entity var2) {
        if (var2 == this.local) {
            this.spawnLabel(var2.x, var2.y, "阵亡", 16736352);
        } else if (var2.unitType == 1) {
            this.spawnLabel(var2.x, var2.y, "击杀!", 16764992);
            this.playHeroKillSfx(var1);
        } else if (var2.unitType == 3) {
            Tower var3 = (Tower)var2;
            this.spawnLabel(var2.x, var2.y, var3.tier == 2 ? "高地塔已破" : "塔已破", 16760928);
            Sfx.play(var3.team == this.local.team ? "tower_ally" : "tower");
        } else if (var2.unitType == 5) {
            Jungle var4 = (Jungle)var2;
            if (var4.getKind() == 3) {
                this.spawnLabel(var2.x, var2.y, "暴君已被击败", 16751178);
                Sfx.play("tyrant");
            } else if (var4.getKind() == 4) {
                this.spawnLabel(var2.x, var2.y, "主宰已被击败", 12618495);
                Sfx.play("overlord");
            }
        }

    }

    private void playHeroKillSfx(Entity var1) {
        int var2 = 1;
        if (var1 instanceof Hero) {
            var2 = ((Hero)var1).killStreak + 1;
        }

        if (!this.firstBloodDone) {
            this.firstBloodDone = true;
            Sfx.play("kill");
        } else {
            if (var2 >= 6) {
                if (var1 == this.local) {
                    Sfx.play(HeroDef.voiceName(this.local.getHeroId(), 5));
                } else {
                    Sfx.play("legend");
                }
            } else if (var2 == 5) {
                Sfx.play("penta");
            } else if (var2 == 4) {
                Sfx.play("quad");
            } else if (var2 == 3) {
                Sfx.play("triple");
            } else if (var2 == 2) {
                Sfx.play("double");
            } else if (var1 == this.local) {
                Sfx.play(HeroDef.voiceName(this.local.getHeroId(), 3));
            } else {
                Sfx.play("slay");
            }

        }
    }

    private void grantKillReward(Entity var1, Entity var2) {
        if (var2.unitType == 1) {
            Hero var3 = (Hero)var2;
            ++var3.deaths;
            var3.killStreak = 0;
            if (var1 != null && var1.team != var3.team && var1.team <= 1) {
                int var10002 = this.teamKills[var1.team]++;
            }
        }

        if (var1 instanceof Hero) {
            Hero var4 = (Hero)var1;
            if (var2.unitType == 2) {
                var4.grantReward(this.bounty(18), 25);
                ++var4.minionKills;
            } else if (var2.unitType == 1) {
                var4.grantReward(this.bounty(120), 100);
                ++var4.heroKills;
                ++var4.killStreak;
                var4.onPassiveKill();
            } else if (var2.unitType == 3) {
                var4.grantReward(this.bounty(80), 40);
            } else if (var2.unitType == 4) {
                var4.grantReward(this.bounty(200), 80);
            } else if (var2.unitType == 5) {
                this.grantJungleReward(var4, (Jungle)var2);
            }

        }
    }

    private int bounty(int var1) {
        return var1 * this.bountyPct / 100;
    }

    private void grantJungleReward(Hero var1, Jungle var2) {
        int var3 = var2.getKind();
        if (var2.isBoss()) {
            for(int var4 = 0; var4 < this.heroCount; ++var4) {
                Hero var5 = this.heroes[var4];
                if (var5.team == var1.team) {
                    var5.grantReward(this.bounty(var2.getGoldReward()), var2.getExpReward());
                }
            }

            if (var3 == 4) {
                this.overlordTeam = var1.team;
                this.overlordLeft = 900;
            }

        } else {
            var1.grantReward(this.bounty(var2.getGoldReward()), var2.getExpReward());
            if (var3 == 1) {
                var1.blueBuffLeft = 600;
                this.spawnLabel(var1.x, var1.y, "蓝BUFF", 7317759);
            } else if (var3 == 2) {
                var1.redBuffLeft = 600;
                this.spawnLabel(var1.x, var1.y, "红BUFF", 16742972);
            }

        }
    }

    public void applySkillHit(Hero var1, Entity var2, int var3, int var4) {
        if (var2 != null && var2.unitType == 5) {
            ((Jungle)var2).onAttackedBy(var1);
        }

        this.applyDamageAmount(var1, var2, var3, var4);
    }

    public int damageEnemiesInRadius(Hero var1, int var2, int var3, int var4, int var5, int var6) {
        int var7 = var4 * var4;
        int var8 = 0;

        for(int var9 = 0; var9 < this.heroCount; ++var9) {
            Hero var10 = this.heroes[var9];
            if (var1.isEnemy(var10) && var10.alive && FMath.dist2(var2, var3, var10.x, var10.y) <= var7) {
                this.applySkillHit(var1, var10, var5, var6);
                ++var8;
            }
        }

        for(int var11 = 0; var11 < 48; ++var11) {
            Minion var14 = this.minions[var11];
            if (var1.isEnemy(var14) && FMath.dist2(var2, var3, var14.x, var14.y) <= var7) {
                this.applySkillHit(var1, var14, var5, var6);
                ++var8;
            }
        }

        for(int var12 = 0; var12 < this.jungles.length; ++var12) {
            Jungle var15 = this.jungles[var12];
            if (var15.alive && FMath.dist2(var2, var3, var15.x, var15.y) <= var7) {
                this.applySkillHit(var1, var15, var5, var6);
                ++var8;
            }
        }

        for(int var13 = 0; var13 < this.towers.length; ++var13) {
            Tower var16 = this.towers[var13];
            if (var1.isEnemy(var16) && var16.alive && FMath.dist2(var2, var3, var16.x, var16.y) <= var7) {
                this.applySkillHit(var1, var16, var5, var6);
                ++var8;
            }
        }

        return var8;
    }

    public int damageEnemiesAlongLine(Hero var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8) {
        int var9 = var6 * var6;
        int var10 = 0;

        for(int var11 = 0; var11 < this.heroCount; ++var11) {
            Hero var12 = this.heroes[var11];
            if (var1.isEnemy(var12) && var12.alive && SkillSystem.distToSegment2(var12.x, var12.y, var2, var3, var4, var5) <= var9) {
                this.applySkillHit(var1, var12, var7, var8);
                ++var10;
            }
        }

        for(int var13 = 0; var13 < 48; ++var13) {
            Minion var15 = this.minions[var13];
            if (var1.isEnemy(var15) && SkillSystem.distToSegment2(var15.x, var15.y, var2, var3, var4, var5) <= var9) {
                this.applySkillHit(var1, var15, var7, var8);
                ++var10;
            }
        }

        for(int var14 = 0; var14 < this.jungles.length; ++var14) {
            Jungle var16 = this.jungles[var14];
            if (var16.alive && SkillSystem.distToSegment2(var16.x, var16.y, var2, var3, var4, var5) <= var9) {
                this.applySkillHit(var1, var16, var7, var8);
                ++var10;
            }
        }

        return var10;
    }

    public int healAlliesInRadius(Hero var1, int var2, int var3, int var4, int var5) {
        int var6 = var4 * var4;
        int var7 = 0;

        for(int var8 = 0; var8 < this.heroCount; ++var8) {
            Hero var9 = this.heroes[var8];
            if (var9.team == var1.team && var9.alive && FMath.dist2(var2, var3, var9.x, var9.y) <= var6) {
                int var10 = var9.heal(var5);
                if (var10 > 0) {
                    this.spawnFloater(var9.x, var9.y, var10, 8454048);
                    ++var7;
                }
            }
        }

        return var7;
    }

    public void affectEnemyHeroesInRadius(Hero var1, int var2, int var3, int var4, int var5, int var6) {
        int var7 = var4 * var4;

        for(int var8 = 0; var8 < this.heroCount; ++var8) {
            Hero var9 = this.heroes[var8];
            if (var1.isEnemy(var9) && var9.alive && FMath.dist2(var2, var3, var9.x, var9.y) <= var7) {
                this.applyHeroControl(var9, var5, var6);
            }
        }

    }

    public void affectEnemyHeroesAlongLine(Hero var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8) {
        int var9 = var6 * var6;

        for(int var10 = 0; var10 < this.heroCount; ++var10) {
            Hero var11 = this.heroes[var10];
            if (var1.isEnemy(var11) && var11.alive && SkillSystem.distToSegment2(var11.x, var11.y, var2, var3, var4, var5) <= var9) {
                this.applyHeroControl(var11, var7, var8);
            }
        }

    }

    private void applyHeroControl(Hero var1, int var2, int var3) {
        if ((var2 & 1) != 0) {
            var1.addSlow(var3 * 2);
        }

        if ((var2 & 2) != 0) {
            var1.addStun(var3);
        }

    }

    public void speedAlliesInRadius(Hero var1, int var2, int var3) {
        int var4 = var2 * var2;

        for(int var5 = 0; var5 < this.heroCount; ++var5) {
            Hero var6 = this.heroes[var5];
            if (var6.alive && var6.team == var1.team && FMath.dist2(var1.x, var1.y, var6.x, var6.y) <= var4) {
                var6.addSpeedBuff(var3);
            }
        }

    }

    public void spawnSkillSlash(Hero var1, Entity var2, int var3) {
        this.spawnSlashFx(var1, var2, var3);
    }

    public void spawnSkillRing(int var1, int var2, int var3, int var4, int var5) {
        AttackFx var6 = this.freeFx();
        if (var6 != null) {
            var6.spawnRing(var1, var2, var3, var4, var5);
        }

    }

    public void spawnSkillTrail(int var1, int var2, int var3, int var4, int var5, int var6) {
        AttackFx var7 = this.freeFx();
        if (var7 != null) {
            var7.spawnTrail(var1, var2, var3, var4, var5, var6);
        }

    }

    public void spawnSkillBeam(int var1, int var2, int var3, int var4, int var5, int var6) {
        AttackFx var7 = this.freeFx();
        if (var7 != null) {
            var7.spawnBeam(var1, var2, var3, var4, var5, var6);
        }

    }

    public void spawnSkillAura(Hero var1, int var2, int var3) {
        AttackFx var4 = this.freeFx();
        if (var4 != null) {
            var4.spawnAura(var1, var2, var3);
        }

    }

    public void spawnSkillLabel(int var1, int var2, String var3, int var4) {
        this.spawnLabel(var1, var2, var3, var4);
    }

    private boolean inFountain(Hero var1) {
        return FMath.dist2(var1.x, var1.y, this.fountainX[var1.team], this.fountainY[var1.team]) <= 12100;
    }

    private void tickFountain(int var1) {
        if (var1 % 10 == 0) {
            for(int var2 = 0; var2 < this.heroCount; ++var2) {
                Hero var3 = this.heroes[var2];
                if (var3.alive && this.inFountain(var3)) {
                    var3.heal(var3.maxHp * 4 / 100);
                    var3.mp += var3.maxMp * 4 / 100;
                    if (var3.mp > var3.maxMp) {
                        var3.mp = var3.maxMp;
                    }
                }
            }

        }
    }

    private void tickPassiveIncome(int var1) {
        if (var1 % 30 == 0) {
            for(int var2 = 0; var2 < this.heroCount; ++var2) {
                if (this.heroes[var2].alive) {
                    this.heroes[var2].grantReward(this.passiveGold, 3);
                }
            }

            for(int var7 = 0; var7 < this.heroCount; ++var7) {
                Hero var3 = this.heroes[var7];
                if (var3.alive) {
                    int var4 = var3.passiveAuraHeal();
                    if (var4 > 0) {
                        for(int var5 = 0; var5 < this.heroCount; ++var5) {
                            Hero var6 = this.heroes[var5];
                            if (var6.alive && var6.team == var3.team && var3.dist2To(var6) <= 4900) {
                                var6.heal(var4);
                            }
                        }
                    }
                }
            }

        }
    }

    private void tickStructurePressure(int var1) {
        if (this.wave >= 36 && var1 % 30 == 0) {
            int var2 = 10 + (this.wave - 36) * 2;
            if (var2 > 80) {
                var2 = 80;
            }

            for(int var3 = 0; var3 < this.towers.length; ++var3) {
                Tower var4 = this.towers[var3];
                if (var4.alive) {
                    var4.takeDamage(var2);
                    if (!var4.alive) {
                        this.announceKill((Entity)null, var4);
                    }
                }
            }

            for(int var5 = 0; var5 < 2; ++var5) {
                Crystal var6 = this.crystals[var5];
                if (var6.alive && !this.isCrystalShielded(var6)) {
                    var6.takeDamage(var2);
                    if (!var6.alive) {
                        this.announceKill((Entity)null, var6);
                    }
                }
            }

        }
    }

    private int countAliveTowers(int var1) {
        int var2 = 0;

        for(int var3 = 0; var3 < this.towers.length; ++var3) {
            if (this.towers[var3].alive && this.towers[var3].team == var1) {
                ++var2;
            }
        }

        return var2;
    }

    private void tickRevives() {
        for(int var1 = 0; var1 < this.heroCount; ++var1) {
            Hero var2 = this.heroes[var1];
            if (var2.alive) {
                this.reviveTimer[var1] = 0;
            } else {
                int var10002 = this.reviveTimer[var1]++;
                if (this.reviveTimer[var1] >= this.reviveFrames(var2)) {
                    var2.respawn();
                    this.reviveTimer[var1] = 0;
                    if (var2 == this.local) {
                        this.deathDamageTaken = 0;
                        this.deathBlowDamage = 0;
                        this.deathKillerName = "";
                        this.lastHitName = "";
                        this.lastHitDamage = 0;
                    }
                }
            }
        }

    }

    private int reviveFrames(Hero var1) {
        return this.reviveBase + var1.level * 8;
    }

    private boolean isCrystalShielded(Crystal var1) {
        for(int var2 = 0; var2 < this.towers.length; ++var2) {
            Tower var3 = this.towers[var2];
            if (var3.alive && var3.team == var1.team && var3.tier == 2) {
                return true;
            }
        }

        return false;
    }

    private Crystal enemyCrystalOf(int var1) {
        return this.crystals[var1 == 0 ? 1 : 0];
    }

    private void spawnWave() {
        ++this.wave;
        this.updateNonHeroDamageScaling();

        for(int var1 = 0; var1 < 3; ++var1) {
            if (this.pathBlue[var1] != null && this.pathBlue[var1].length >= 2) {
                int var2 = this.pathBlue[var1][0];
                int var3 = this.pathBlue[var1][1];
                int var4 = this.pathRed[var1][0];
                int var5 = this.pathRed[var1][1];
                this.spawnLaneComposition(0, this.pathBlue[var1], var2, var3);
                this.spawnLaneComposition(1, this.pathRed[var1], var4, var5);
            }
        }

    }

    private void updateNonHeroDamageScaling() {
        for(int var1 = 0; var1 < this.towers.length; ++var1) {
            this.towers[var1].applyBattleProgress(this.wave);
        }

        for(int var2 = 0; var2 < this.jungles.length; ++var2) {
            this.jungles[var2].applyBattleProgress(this.wave);
        }

    }

    private void spawnLaneComposition(int var1, int[] var2, int var3, int var4) {
        boolean var5 = !this.hasAliveHighTower(1 - var1);
        int var6 = 0;
        if (var5) {
            this.spawnMinion(var1, var2, var3 + this.offsetX(var6++), var4, 3);
            this.spawnMinion(var1, var2, var3 + this.offsetX(var6++), var4, 1);
            if (this.wave >= 8) {
                this.spawnMinion(var1, var2, var3 + this.offsetX(var6++), var4, 2);
            }

        } else {
            int var7 = this.perLane >= 2 ? 2 : 1;

            for(int var8 = 0; var8 < var7; ++var8) {
                this.spawnMinion(var1, var2, var3 + this.offsetX(var6++), var4, 0);
            }

            this.spawnMinion(var1, var2, var3 + this.offsetX(var6++), var4, 1);
            if (this.wave >= 8) {
                this.spawnMinion(var1, var2, var3 + this.offsetX(var6), var4, 2);
            }

        }
    }

    private int offsetX(int var1) {
        return (var1 - 1) * 7;
    }

    private boolean hasAliveHighTower(int var1) {
        for(int var2 = 0; var2 < this.towers.length; ++var2) {
            Tower var3 = this.towers[var2];
            if (var3.alive && var3.team == var1 && var3.tier == 2) {
                return true;
            }
        }

        return false;
    }

    private void spawnMinion(int var1, int[] var2, int var3, int var4, int var5) {
        int var6 = this.wave;
        if (this.overlordLeft > 0 && this.overlordTeam == var1) {
            var6 += 10;
        }

        int var7 = 9 - this.countAliveTowers(1 - var1);
        if (var7 > 0) {
            var6 += var7 * 2;
        }

        for(int var8 = 0; var8 < 48; ++var8) {
            if (!this.minions[var8].alive) {
                this.minions[var8].spawn(var1, var2, var3, var4, var6, var5);
                return;
            }
        }

    }

    private void spawnMinion(int var1, int[] var2, int var3, int var4) {
        this.spawnMinion(var1, var2, var3, var4, 0);
    }

    private AttackFx freeFx() {
        for(int var1 = 0; var1 < 40; ++var1) {
            if (!this.attackFx[var1].active) {
                return this.attackFx[var1];
            }
        }

        return null;
    }

    private void spawnSlashFx(Entity var1, Entity var2, int var3) {
        AttackFx var4 = this.freeFx();
        if (var4 != null) {
            var4.spawnSlash(var1, var2, var3);
        }

    }

    private void spawnArrowFx(Entity var1, Entity var2, int var3, int var4) {
        AttackFx var5 = this.freeFx();
        if (var5 != null) {
            var5.spawnArrow(var1, var2, var3, var4);
        } else {
            this.applyDamageAmount(var1, var2, var3, var4);
        }
    }

    private void spawnFloater(int var1, int var2, int var3, int var4) {
        for(int var5 = 0; var5 < 24; ++var5) {
            if (!this.floaters[var5].active) {
                this.floaters[var5].spawn(var1, var2, var3, var4);
                return;
            }
        }

    }

    private void spawnLabel(int var1, int var2, String var3, int var4) {
        for(int var5 = 0; var5 < 24; ++var5) {
            if (!this.floaters[var5].active) {
                this.floaters[var5].spawnLabel(var1, var2, var3, var4);
                return;
            }
        }

    }

    public void renderUnits(Graphics2D var1, int var2, int var3) {
        Tower var4 = null;
        int var5 = Integer.MAX_VALUE;
        if (this.local != null && this.local.alive) {
            short var6 = 23716;

            for(int var7 = 0; var7 < this.towers.length; ++var7) {
                Tower var8 = this.towers[var7];
                if (var8.alive && var8.isEnemy(this.local)) {
                    int var9 = var8.dist2To(this.local);
                    if (var9 <= var6 && var9 < var5) {
                        var5 = var9;
                        var4 = var8;
                    }
                }
            }
        }

        if (var4 != null) {
            byte var10 = 0;
            if (var4.isThreatening(this.local)) {
                var10 = 2;
            } else {
                int var17 = var4.atkRange + this.local.radius;
                if (var4.dist2To(this.local) <= var17 * var17) {
                    var10 = 1;
                }
            }

            var4.renderAttackRange(var1, var2, var3, var10);
        }

        this.crystals[0].render(var1, var2, var3);
        this.crystals[1].render(var1, var2, var3);

        for(int var11 = 0; var11 < this.towers.length; ++var11) {
            this.towers[var11].render(var1, var2, var3);
        }

        for(int var12 = 0; var12 < this.jungles.length; ++var12) {
            this.jungles[var12].render(var1, var2, var3);
        }

        for(int var13 = 0; var13 < 48; ++var13) {
            if (this.minions[var13].alive) {
                this.minions[var13].render(var1, var2, var3);
            }
        }

        for(int var14 = 0; var14 < this.heroCount; ++var14) {
            Hero var18 = this.heroes[var14];
            if (var18.team == this.local.team || !var18.isHidden() || this.teamHasBushVision(this.local.team, var18)) {
                var18.render(var1, var2, var3);
            }
        }

        for(int var15 = 0; var15 < 40; ++var15) {
            this.attackFx[var15].render(var1, var2, var3);
        }

        for(int var16 = 0; var16 < 24; ++var16) {
            this.floaters[var16].render(var1, var2, var3);
        }

    }

    public void renderHud(Graphics2D var1, int var2, int var3, int var4) {
        this.hud.render(var1, var2, var3, this, var4);
    }

    public void renderMatchDetails(Graphics2D var1, int var2, int var3) {
        this.hud.drawMatchDetails(var1, var2, var3, this);
    }

    public int heroAvatarAt(int var1, int var2) {
        return this.hud.heroAvatarAt(var1, var2);
    }

    public void drawMinimap(Graphics2D var1, int var2, int var3, int var4) {
        int var5 = this.map.getPixelWidth();
        int var6 = this.map.getPixelHeight();
        if (var5 > 0 && var6 > 0 && var4 >= 8) {
            var1.setColor(G2D.color(660512));
            var1.fillRect(var2, var3, var4, var4);
            var1.setColor(G2D.color(2767434));
            var1.drawRect(var2, var3, var4 - 1, var4 - 1);
            var1.setColor(G2D.color(3820090));
            int var7 = var4 / 8;
            var1.drawLine(var2 + var7, var3 + var4 - 1 - var7, var2 + var4 - 1 - var7, var3 + var7);
            var1.drawLine(var2 + var7, var3 + var4 - 1 - var7, var2 + var7, var3 + var7);
            var1.drawLine(var2 + var7, var3 + var7, var2 + var4 - 1 - var7, var3 + var7);
            var1.drawLine(var2 + var7, var3 + var4 - 1 - var7, var2 + var4 - 1 - var7, var3 + var4 - 1 - var7);
            var1.drawLine(var2 + var4 - 1 - var7, var3 + var4 - 1 - var7, var2 + var4 - 1 - var7, var3 + var7);
            var1.setColor(G2D.color(2773120));
            var1.drawLine(var2 + var7, var3 + var7, var2 + var4 - 1 - var7, var3 + var4 - 1 - var7);

            for(int var8 = 0; var8 < this.jungles.length; ++var8) {
                Jungle var9 = this.jungles[var8];
                if (var9.alive) {
                    int var10 = miniX(var9.x, var5, var2, var4);
                    int var11 = miniY(var9.y, var6, var3, var4);
                    if (var9.isBoss()) {
                        var1.setColor(G2D.color(var9.getKind() == 3 ? 16751178 : 12618495));
                        var1.fillRect(var10 - 1, var11 - 1, 3, 3);
                    } else {
                        var1.setColor(G2D.color(11573336));
                        var1.fillRect(var10, var11, 1, 1);
                    }
                }
            }

            for(int var12 = 0; var12 < 48; ++var12) {
                Minion var15 = this.minions[var12];
                if (var15.alive) {
                    var1.setColor(G2D.color(var15.team == 0 ? 7385343 : 16744576));
                    var1.fillRect(miniX(var15.x, var5, var2, var4), miniY(var15.y, var6, var3, var4), 1, 1);
                }
            }

            for(int var13 = 0; var13 < this.towers.length; ++var13) {
                Tower var16 = this.towers[var13];
                if (var16.alive) {
                    var1.setColor(G2D.color(var16.team == 0 ? 4029408 : 14174280));
                    var1.fillRect(miniX(var16.x, var5, var2, var4) - 1, miniY(var16.y, var6, var3, var4) - 1, 3, 3);
                }
            }

            this.drawMiniCrystal(var1, this.crystals[0], var5, var6, var2, var3, var4);
            this.drawMiniCrystal(var1, this.crystals[1], var5, var6, var2, var3, var4);

            for(int var14 = 0; var14 < this.heroCount; ++var14) {
                Hero var17 = this.heroes[var14];
                if (var17.alive && (var17.team == this.local.team || !var17.isHidden() || this.teamHasBushVision(this.local.team, var17))) {
                    int var18 = miniX(var17.x, var5, var2, var4);
                    int var19 = miniY(var17.y, var6, var3, var4);
                    var1.setColor(G2D.color(var17 == this.local ? 16769120 : 16777215));
                    var1.fillArc(var18 - 2, var19 - 2, 5, 5, 0, 360);
                    var1.setColor(G2D.color(var17.team == 0 ? 4243711 : 16728128));
                    var1.fillArc(var18 - 1, var19 - 1, 3, 3, 0, 360);
                }
            }

        }
    }

    private void drawMiniCrystal(Graphics2D var1, Crystal var2, int var3, int var4, int var5, int var6, int var7) {
        int var8 = miniX(var2.x, var3, var5, var7);
        int var9 = miniY(var2.y, var4, var6, var7);
        var1.setColor(G2D.color(var2.alive ? (var2.team == 0 ? 8442111 : 16752768) : 5592405));
        G2D.fillTriangle(var1, var8, var9 - 3, var8 - 3, var9 + 2, var8 + 3, var9 + 2);
    }

    private static int miniX(int var0, int var1, int var2, int var3) {
        if (var1 <= 0) {
            return var2;
        } else {
            int var4 = (var1 - var0) * (var3 - 1) / var1;
            if (var4 < 0) {
                var4 = 0;
            } else if (var4 >= var3) {
                var4 = var3 - 1;
            }

            return var2 + var4;
        }
    }

    private static int miniY(int var0, int var1, int var2, int var3) {
        if (var1 <= 0) {
            return var2;
        } else {
            int var4 = var0 * (var3 - 1) / var1;
            if (var4 < 0) {
                var4 = 0;
            } else if (var4 >= var3) {
                var4 = var3 - 1;
            }

            return var2 + var4;
        }
    }

    public Hero getLocalHero() {
        return this.local;
    }

    public int getHeroCount() {
        return this.heroCount;
    }

    public Hero getHeroAt(int var1) {
        return this.heroes[var1];
    }

    public Hero getTeamHero(int var1, int var2) {
        return this.heroes[var1 * this.teamSize + var2];
    }

    public int getTeamSize() {
        return this.teamSize;
    }

    public MatchConfig getConfig() {
        return this.config;
    }

    public int getTeamKills(int var1) {
        return this.teamKills[var1];
    }

    public int getResult() {
        return this.result;
    }

    public void setLocalAsAi(boolean var1) {
        this.localAsAi = var1;
    }

    public TileMap getMap() {
        return this.map;
    }

    public int getWave() {
        return this.wave;
    }

    public boolean isShopOpen() {
        return this.shopOpen;
    }

    public int getShopPage() {
        return this.shopPage;
    }

    public boolean isLocalInFountain() {
        return this.inFountain(this.local);
    }

    public boolean isEnemyCrystalShielded() {
        return this.isCrystalShielded(this.enemyCrystalOf(this.local.team));
    }

    public int getLocalReviveSeconds() {
        int var1 = this.reviveFrames(this.local) - this.reviveTimer[0];
        if (var1 < 0) {
            var1 = 0;
        }

        return (var1 + 14) / 15;
    }

    public boolean isLocalDead() {
        return this.local != null && !this.local.alive;
    }

    public String getDeathKillerName() {
        return this.deathKillerName;
    }

    public int getDeathBlowDamage() {
        return this.deathBlowDamage;
    }

    public int getDeathDamageTaken() {
        return this.deathDamageTaken;
    }

    public int getJungleCount() {
        return this.jungles.length;
    }

    public Jungle getJungleAt(int var1) {
        return this.jungles[var1];
    }

    public Jungle getBoss(int var1) {
        for(int var2 = 0; var2 < this.jungles.length; ++var2) {
            if (this.jungles[var2].getKind() == var1) {
                return this.jungles[var2];
            }
        }

        return null;
    }

    public int getOverlordTeam() {
        return this.overlordLeft > 0 ? this.overlordTeam : -1;
    }

    private void loadPaths() throws IOException {
        InputStream var1 = BattleWorld.class.getResourceAsStream("/res/map/paths.bin");
        if (var1 == null) {
            throw new IOException("missing /res/map/paths.bin");
        } else {
            try {
                int var2 = var1.read() & 255;
                if (var2 < 3) {
                    throw new IOException("paths laneCount=" + var2);
                }

                for(int var3 = 0; var3 < 3; ++var3) {
                    int var4 = var1.read() & 255;
                    int[] var5 = new int[var4 * 2];

                    for(int var6 = 0; var6 < var4; ++var6) {
                        int var7 = (var1.read() & 255) << 8 | var1.read() & 255;
                        int var8 = (var1.read() & 255) << 8 | var1.read() & 255;
                        var5[var6 * 2] = var7;
                        var5[var6 * 2 + 1] = var8;
                    }

                    this.pathBlue[var3] = var5;
                    int[] var18 = new int[var4 * 2];

                    for(int var19 = 0; var19 < var4; ++var19) {
                        var18[var19 * 2] = var5[(var4 - 1 - var19) * 2];
                        var18[var19 * 2 + 1] = var5[(var4 - 1 - var19) * 2 + 1];
                    }

                    this.pathRed[var3] = var18;
                }

                for(int var16 = 3; var16 < var2; ++var16) {
                    int var17 = var1.read() & 255;
                    var1.skip((long)var17 * 4L);
                }
            } finally {
                try {
                    var1.close();
                } catch (IOException var14) {
                }

            }

        }
    }
}
