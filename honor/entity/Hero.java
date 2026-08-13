package honor.entity;

import honor.core.FMath;
import honor.core.IsoMath;
import honor.core.KeyInput;
import honor.hero.HeroDef;
import honor.skill.SkillDef;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import honor.G2D;
import honor.Img;
import honor.Sprite;

public class Hero extends Entity {
    public static final int FRAME_W = 32;
    public static final int FRAME_H = 40;
    public static final int ANIM_WALK = 0;
    public static final int ANIM_ATK = 1;
    public static final int ANIM_SK1 = 2;
    public static final int ANIM_SK2 = 3;
    public static final int ANIM_ULT = 4;
    private static final int ATK_ANIM_LEN = 6;
    private static final int SKILL_ANIM_LEN = 8;
    private static final int ULT_ANIM_LEN = 12;
    private static final int MOVE_VOICE_CD = 180;
    public static final int RECALL_FRAMES = 45;
    public static final int EXP_PER_LEVEL = 100;
    public static final int MP_REGEN_EVERY = 15;
    public static final int MP_REGEN = 6;
    public static final int BLUE_BUFF_CDR = 20;
    public static final int RED_BUFF_BURN = 12;
    private static final int BASE_MOVE = FMath.fromInt(3);
    private static final int SPEED_BUFF_BONUS = 2;
    private static final int NAV_STALL_FRAMES = 14;
    private static final int DETOUR_FRAMES = 28;
    private static final int[] UNSTICK_X = new int[]{1, -1, 0, 0, 1, -1, 1, -1, 2, -2, 1, -1, 2, -2, 1, -1};
    private static final int[] UNSTICK_Y = new int[]{0, 0, 1, -1, 1, 1, -1, -1, 1, 1, 2, 2, -1, -1, -2, -2};
    private final Sprite sprite;
    private int facing = 0;
    private int animTick = 0;
    private boolean moving;
    private final boolean playerControlled;
    private final int heroId;
    public int gold;
    public int exp;
    public int level = 1;
    public int mp;
    public int maxMp;
    public int equipMask;
    public int bonusAtk;
    public int bonusHp;
    public int bonusDef;
    public int heroKills;
    public int deaths;
    public int minionKills;
    public int killStreak;
    public int moveBonus;
    public int recallLeft;
    public final int[] skillCd = new int[3];
    public final int[] skillLv = new int[3];
    public int skillPoints = 1;
    public int passiveHits;
    private int passiveCharge;
    private int passiveGuardCd;
    public int speedBuffLeft;
    public int slowLeft;
    public int stunLeft;
    public int attackSpeedBuffLeft;
    public int redBuffLeft;
    public int blueBuffLeft;
    public boolean inBush;
    public int revealLeft;
    public int homeX;
    public int homeY;
    public int[] aiPath;
    public int aiPathIndex;
    public boolean retreating;
    public int pushX;
    public int pushY;
    public boolean holdBack;
    private int stuckFrames;
    private int unstickDir;
    private int navGoalX;
    private int navGoalY;
    private int navBestD2 = Integer.MAX_VALUE;
    private int navStallFrames;
    private int navRecoveryCount;
    private int targetSearchLockLeft;
    private int detourX;
    private int detourY;
    private int detourLeft;
    private final int[] dirBuf = new int[2];
    private int animMode = 0;
    private int animLeft;
    private int animPhase;
    private int moveVoiceCd;
    private boolean moveVoicePending;
    private boolean tauntVoicePending;

    public Hero(int var1, int var2, int var3, int var4, BufferedImage var5, boolean var6) {
        super(var2, var3, var4, 1);
        this.heroId = var1;
        this.playerControlled = var6;
        this.sprite = new Sprite(var5, 32, 40);
        this.sprite.defineReferencePixel(16, 38);
        this.sprite.setFrame(0);
        this.radius = 6;
        this.level = 1;
        this.atkRange = HeroDef.atkRange(var1);
        if (this.atkRange > 82) {
            this.atkRange = 82;
        }

        this.atkCooldown = HeroDef.atkCooldown(var1);
        this.atkCdLeft = 0;
        this.ranged = HeroDef.ranged(var1);
        this.moveBonus = HeroDef.moveBonus(var1);
        this.homeX = var2;
        this.homeY = var3;
        this.applyStats();
        this.hp = this.maxHp;
        this.mp = this.maxMp;
    }

    public void applyStats() {
        int var1 = HeroDef.maxHp(this.heroId, this.level) + this.bonusHp;
        int var2 = HeroDef.maxMp(this.heroId, this.level);
        this.maxHp = var1;
        this.maxMp = var2;
        this.atk = HeroDef.atk(this.heroId, this.level) + this.bonusAtk;
        this.def = HeroDef.def(this.heroId, this.level) + this.bonusDef;
        if (this.hp > this.maxHp) {
            this.hp = this.maxHp;
        }

        if (this.mp > this.maxMp) {
            this.mp = this.maxMp;
        }

    }

    public int getHeroId() {
        return this.heroId;
    }

    public String getName() {
        return HeroDef.name(this.heroId);
    }

    public int getMoveSpeed() {
        int var1 = this.moveBonus;
        if (this.speedBuffLeft > 0) {
            var1 += 2;
        }

        int var2 = BASE_MOVE + FMath.fromInt(var1);
        if (this.slowLeft > 0) {
            var2 = var2 * 6 / 10;
        }

        return var2;
    }

    public void updatePlayer(KeyInput var1, int var2, int var3) {
        if (this.alive) {
            this.tickCooldowns();
            if (this.stunLeft > 0) {
                this.vx = 0;
                this.vy = 0;
                this.moving = false;
                this.tickAnim();
                this.updateSprite();
            } else {
                int var4 = var1.axisX();
                int var5 = var1.axisY();
                int var6 = var1.axisX100();
                int var7 = var1.axisY100();
                if (this.recallLeft > 0) {
                    if (var4 == 0 && var5 == 0 && var6 == 0 && var7 == 0) {
                        this.vx = 0;
                        this.vy = 0;
                        this.moving = false;
                        this.updateSprite();
                        return;
                    }

                    this.cancelRecall();
                }

                int var8 = this.getMoveSpeed();
                IsoMath.screenDirToWorld(var6, var7, this.dirBuf);
                int var9 = this.dirBuf[0];
                int var10 = this.dirBuf[1];
                int var11 = var9 * var9 + var10 * var10;
                if (var11 == 0) {
                    this.vx = 0;
                    this.vy = 0;
                } else {
                    int var12 = FMath.isqrt(var11);
                    if (var12 < 1) {
                        var12 = 1;
                    }

                    this.vx = var9 * var8 / var12;
                    this.vy = var10 * var8 / var12;
                }

                int var15 = this.x;
                int var13 = this.y;
                this.moveWithCollision(var2, var3);
                boolean var14 = this.moving;
                this.moving = this.x != var15 || this.y != var13;
                if (this.moving) {
                    if (this.animMode == 0) {
                        if (Math.abs(var6) >= Math.abs(var7)) {
                            this.facing = var6 < 0 ? 1 : 2;
                        } else {
                            this.facing = var7 < 0 ? 3 : 0;
                        }
                    }

                    ++this.animTick;
                    if (!var14) {
                        this.maybeQueueMoveVoice();
                    }
                }

                this.tickAnim();
                this.updateSprite();
                if (this.target != null && (!this.target.alive || !this.isEnemy(this.target))) {
                    this.target = null;
                }

            }
        }
    }

    public void updateAi(int var1, int var2) {
        if (this.alive) {
            this.tickCooldowns();
            if (this.stunLeft > 0) {
                this.vx = 0;
                this.vy = 0;
                this.moving = false;
                this.tickAnim();
                this.updateSprite();
            } else {
                if (this.targetSearchLockLeft > 0) {
                    --this.targetSearchLockLeft;
                }

                this.moving = false;
                int var3 = this.maxHp <= 0 ? 100 : this.hp * 100 / this.maxHp;
                if (var3 < 28) {
                    this.retreating = true;
                } else if (var3 > 65) {
                    this.retreating = false;
                }

                boolean var4 = false;
                int var5 = this.x;
                int var6 = this.y;
                if (this.retreating) {
                    var5 = this.homeX;
                    var6 = this.homeY;
                    if (FMath.dist2(this.x, this.y, this.homeX, this.homeY) <= 64) {
                        this.vx = 0;
                        this.vy = 0;
                    } else {
                        this.moveToward(this.homeX, this.homeY);
                        var4 = true;
                    }
                } else if (this.holdBack) {
                    var5 = this.safeLaneX();
                    var6 = this.safeLaneY();
                    this.target = null;
                    if (FMath.dist2(this.x, this.y, var5, var6) <= 144) {
                        this.vx = 0;
                        this.vy = 0;
                    } else {
                        this.moveToward(var5, var6);
                        var4 = true;
                    }
                } else if (this.target != null && this.target.alive && this.inRange(this.target)) {
                    var5 = this.target.x;
                    var6 = this.target.y;
                    int var7 = this.ranged ? this.atkRange / 2 : 0;
                    if (var7 > 12 && this.target.unitType != 3 && this.target.unitType != 4 && this.dist2To(this.target) < var7 * var7) {
                        int var8 = this.x - this.target.x;
                        int var9 = this.y - this.target.y;
                        var5 = this.x + (var8 < 0 ? -32 : (var8 > 0 ? 32 : 0));
                        var6 = this.y + (var9 < 0 ? -32 : (var9 > 0 ? 32 : 0));
                        this.moveToward(var5, var6);
                        var4 = true;
                    } else {
                        this.vx = 0;
                        this.vy = 0;
                        this.faceToward(this.target.x, this.target.y);
                    }
                } else if (this.target != null && this.target.alive) {
                    var5 = this.target.x;
                    var6 = this.target.y;
                    this.moveToward(this.target.x, this.target.y);
                    var4 = true;
                } else {
                    if (this.aiPath != null && this.aiPathIndex * 2 + 1 < this.aiPath.length) {
                        var5 = this.aiPath[this.aiPathIndex * 2];
                        var6 = this.aiPath[this.aiPathIndex * 2 + 1];
                    } else {
                        var5 = this.pushX;
                        var6 = this.pushY;
                    }

                    this.followAiPath();
                    var4 = this.vx != 0 || this.vy != 0;
                }

                int var12 = this.x;
                int var13 = this.y;
                this.moveWithCollision(var1, var2);
                this.moving = this.x != var12 || this.y != var13;
                if (var4 && !this.moving) {
                    ++this.stuckFrames;
                    if (this.stuckFrames >= 2) {
                        this.tryUnstick(var1, var2);
                        this.stuckFrames = 0;
                    }
                } else if (var4 && this.moving) {
                    int var14 = (this.x - var12) * (this.x - var12) + (this.y - var13) * (this.y - var13);
                    if (var14 <= 1) {
                        ++this.stuckFrames;
                        if (this.stuckFrames >= 5) {
                            this.tryUnstick(var1, var2);
                            this.stuckFrames = 0;
                        }
                    } else {
                        this.stuckFrames = 0;
                    }
                } else {
                    this.stuckFrames = 0;
                }

                this.updateNavigationProgress(var4, var5, var6);
                if (this.moving) {
                    ++this.animTick;
                    this.maybeQueueMoveVoice();
                }

                this.tickAnim();
                this.updateSprite();
            }
        }
    }

    private void tryUnstick(int var1, int var2) {
        int var3 = this.pushX;
        int var4 = this.pushY;
        if (this.target != null && this.target.alive) {
            var3 = this.target.x;
            var4 = this.target.y;
        } else if (this.aiPath != null && this.aiPathIndex * 2 + 1 < this.aiPath.length) {
            var3 = this.aiPath[this.aiPathIndex * 2];
            var4 = this.aiPath[this.aiPathIndex * 2 + 1];
        } else if (this.retreating) {
            var3 = this.homeX;
            var4 = this.homeY;
        }

        int var5 = var3 - this.x;
        int var6 = var4 - this.y;
        int var7 = this.getMoveSpeed();
        if (this.startDetour(var3, var4)) {
            int var18 = this.x;
            int var20 = this.y;
            this.moveToward(var3, var4);
            this.moveWithCollision(var1, var2);
            this.moving = this.x != var18 || this.y != var20;
        } else {
            if (var5 != 0 || var6 != 0) {
                int var8 = this.x;
                int var9 = this.y;
                if (FMath.abs(var5) >= FMath.abs(var6)) {
                    this.vx = var5 > 0 ? var7 : -var7;
                    this.vy = 0;
                    this.moveWithCollision(var1, var2);
                    if (this.x == var8 && var6 != 0) {
                        this.vx = 0;
                        this.vy = var6 > 0 ? var7 : -var7;
                        this.moveWithCollision(var1, var2);
                    }
                } else {
                    this.vx = 0;
                    this.vy = var6 > 0 ? var7 : -var7;
                    this.moveWithCollision(var1, var2);
                    if (this.y == var9 && var5 != 0) {
                        this.vy = 0;
                        this.vx = var5 > 0 ? var7 : -var7;
                        this.moveWithCollision(var1, var2);
                    }
                }

                if (this.x != var8 || this.y != var9) {
                    this.faceToward(var3, var4);
                    this.moving = true;
                    return;
                }
            }

            int var17 = this.unstickDir % UNSTICK_X.length;

            for(int var19 = 0; var19 < 2; ++var19) {
                for(int var10 = 0; var10 < UNSTICK_X.length; ++var10) {
                    int var11 = (var17 + var10) % UNSTICK_X.length;
                    int var12 = UNSTICK_X[var11];
                    int var13 = UNSTICK_Y[var11];
                    int var14 = var12 * var5 + var13 * var6;
                    if (var19 != 0 || var14 > 0) {
                        int var15 = this.x;
                        int var16 = this.y;
                        this.vx = var12 * var7;
                        this.vy = var13 * var7;
                        this.moveWithCollision(var1, var2);
                        if (this.x != var15 || this.y != var16) {
                            this.unstickDir = var17 + var10 + 1;
                            this.faceToward(this.x + var12, this.y + var13);
                            this.moving = true;
                            return;
                        }
                    }
                }
            }

            if (this.snapToNearbyWalkable(48, var3, var4)) {
                this.moving = true;
                ++this.unstickDir;
                this.vx = 0;
                this.vy = 0;
            } else {
                if (this.aiPath != null && this.aiPathIndex * 2 + 3 < this.aiPath.length) {
                    ++this.aiPathIndex;
                    this.moving = true;
                }

                ++this.unstickDir;
                this.vx = 0;
                this.vy = 0;
            }
        }
    }

    private void updateNavigationProgress(boolean var1, int var2, int var3) {
        if (!var1) {
            this.navStallFrames = 0;
            this.navBestD2 = Integer.MAX_VALUE;
            this.navRecoveryCount = 0;
            this.detourLeft = 0;
        } else {
            int var4 = FMath.dist2(this.navGoalX, this.navGoalY, var2, var3);
            if (this.navBestD2 != Integer.MAX_VALUE && var4 <= 256) {
                if (this.detourLeft > 0) {
                    this.navStallFrames = 0;
                } else {
                    int var5 = FMath.dist2(this.x, this.y, var2, var3);
                    if (var5 + 16 < this.navBestD2) {
                        this.navBestD2 = var5;
                        this.navStallFrames = 0;
                        this.navRecoveryCount = 0;
                    } else {
                        ++this.navStallFrames;
                        if (this.navStallFrames >= 14) {
                            ++this.navRecoveryCount;
                            if (this.target != null && this.navRecoveryCount >= 3) {
                                this.target = null;
                                this.targetSearchLockLeft = 30;
                                this.detourLeft = 0;
                                this.navRecoveryCount = 0;
                            } else {
                                this.startDetour(var2, var3);
                            }

                            this.navBestD2 = var5;
                            this.navStallFrames = 0;
                        }
                    }

                }
            } else {
                this.navGoalX = var2;
                this.navGoalY = var3;
                this.navBestD2 = FMath.dist2(this.x, this.y, var2, var3);
                this.navStallFrames = 0;
                this.navRecoveryCount = 0;
                this.detourLeft = 0;
            }
        }
    }

    private boolean startDetour(int var1, int var2) {
        if (this.terrain == null) {
            return false;
        } else {
            int var3 = var1 - this.x;
            int var4 = var2 - this.y;
            int var5 = (this.unstickDir & 1) == 0 ? 1 : -1;
            int var6 = Integer.MAX_VALUE;
            int var7 = this.x;
            int var8 = this.y;

            for(int var9 = 20; var9 <= 68; var9 += 8) {
                for(int var10 = 0; var10 < 16; ++var10) {
                    int var11 = var10 * 22;
                    int var12 = FMath.cosDeg(var11) * var9 >> 16;
                    int var13 = FMath.sinDeg(var11) * var9 >> 16;
                    int var14 = this.x + var12;
                    int var15 = this.y + var13;
                    if (this.canNavigateTo(var14, var15) && this.canNavigateSegment(this.x, this.y, var14, var15)) {
                        int var16 = var3 * var13 - var4 * var12;
                        int var17 = var16 < 0 ? -1 : (var16 > 0 ? 1 : 0);
                        int var18 = var3 * var12 + var4 * var13;
                        int var19 = FMath.dist2(var14, var15, var1, var2) + var9 * var9 / 2;
                        if (!this.canNavigateSegment(var14, var15, var1, var2)) {
                            var19 += 2400;
                        }

                        if (var17 == 0) {
                            var19 += 3000;
                        } else if (var17 != var5) {
                            var19 += 1400;
                        }

                        if (var18 < 0) {
                            var19 += 1800;
                        }

                        if (var19 < var6) {
                            var6 = var19;
                            var7 = var14;
                            var8 = var15;
                        }
                    }
                }
            }

            if (var6 == Integer.MAX_VALUE) {
                return false;
            } else {
                this.detourX = var7;
                this.detourY = var8;
                this.detourLeft = 28;
                ++this.unstickDir;
                return true;
            }
        }
    }

    private boolean snapToNearbyWalkable(int var1, int var2, int var3) {
        if (this.terrain == null) {
            return false;
        } else {
            int var4 = this.x;
            int var5 = this.y;
            int var6 = Integer.MAX_VALUE;

            for(int var7 = 4; var7 <= var1; var7 += 4) {
                for(int var8 = 0; var8 < 16; ++var8) {
                    int var9 = var8 * 22;
                    int var10 = FMath.cosDeg(var9) * var7 >> 16;
                    int var11 = FMath.sinDeg(var9) * var7 >> 16;
                    int var12 = this.x + var10;
                    int var13 = this.y + var11;
                    if (this.canStand(var12, var13)) {
                        int var14 = var2 - var12;
                        int var15 = var3 - var13;
                        int var16 = var10 * var10 + var11 * var11 + (var14 * var14 + var15 * var15 >> 4);
                        if (var16 < var6) {
                            var6 = var16;
                            var4 = var12;
                            var5 = var13;
                        }
                    }
                }

                if (var6 != Integer.MAX_VALUE) {
                    this.x = var4;
                    this.y = var5;
                    return true;
                }
            }

            return false;
        }
    }

    public void update(int var1) {
        this.tickCooldowns();
    }

    public void tickMpRegen(int var1) {
        if (this.alive && var1 % 15 == 0) {
            this.mp += 6;
            if (this.mp > this.maxMp) {
                this.mp = this.maxMp;
            }

        }
    }

    public void startRecall() {
        if (this.alive && this.recallLeft <= 0) {
            this.recallLeft = 45;
            this.vx = 0;
            this.vy = 0;
        }
    }

    public void cancelRecall() {
        this.recallLeft = 0;
    }

    public boolean tickRecall() {
        if (this.recallLeft <= 0) {
            return false;
        } else {
            --this.recallLeft;
            if (this.recallLeft <= 0) {
                this.recallLeft = 0;
                this.x = this.homeX;
                this.y = this.homeY;
                this.vx = 0;
                this.vy = 0;
                return true;
            } else {
                return false;
            }
        }
    }

    public boolean isSkillUnlocked(int var1) {
        return var1 >= 0 && var1 < 3 && this.skillLv[var1] > 0;
    }

    public boolean canUpgradeSkill(int var1) {
        if (this.skillPoints > 0 && var1 >= 0 && var1 < 3) {
            int var2 = SkillDef.maxRank(var1);
            if (var1 == 2) {
                var2 = SkillDef.ultCapAtLevel(this.level);
            }

            return this.skillLv[var1] < var2;
        } else {
            return false;
        }
    }

    public boolean upgradeSkill(int var1) {
        if (!this.canUpgradeSkill(var1)) {
            return false;
        } else {
            int var10002 = this.skillLv[var1]++;
            --this.skillPoints;
            return true;
        }
    }

    public void autoSpendSkillPoints() {
        while(true) {
            if (this.skillPoints > 0) {
                boolean var1 = false;
                if (this.canUpgradeSkill(2)) {
                    this.upgradeSkill(2);
                    var1 = true;
                } else if (this.canUpgradeSkill(0)) {
                    this.upgradeSkill(0);
                    var1 = true;
                } else if (this.canUpgradeSkill(1)) {
                    this.upgradeSkill(1);
                    var1 = true;
                }

                if (var1) {
                    continue;
                }
            }

            return;
        }
    }

    public boolean canCast(int var1) {
        if (this.alive && this.recallLeft <= 0 && this.stunLeft <= 0 && var1 >= 0 && var1 < 3) {
            if (this.isSkillUnlocked(var1) && this.skillCd[var1] <= 0) {
                return this.mp >= SkillDef.mpCost(this.skillId(var1), this.skillLv[var1]);
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public void paySkill(int var1) {
        int var2 = this.skillId(var1);
        int var3 = this.skillLv[var1];
        this.mp -= SkillDef.mpCost(var2, var3);
        if (this.mp < 0) {
            this.mp = 0;
        }

        int var4 = SkillDef.cooldown(var2, var3);
        if (this.blueBuffLeft > 0) {
            var4 = var4 * 80 / 100;
        }

        this.skillCd[var1] = var4;
    }

    public int skillId(int var1) {
        return HeroDef.skill(this.heroId, var1);
    }

    public int pickAiSkill() {
        if (this.target != null && this.target.alive && this.recallLeft <= 0) {
            for(int var1 = 2; var1 >= 0; --var1) {
                if (this.canCast(var1)) {
                    int var2 = this.skillId(var1);
                    int var3 = SkillDef.type(var2);
                    if (this.target.unitType != 2 || this.target.hp > this.atk || var3 == 5 || var3 == 4) {
                        if (var1 == 2 && this.target.unitType != 1) {
                            int var4 = this.maxHp <= 0 ? 100 : this.hp * 100 / this.maxHp;
                            if (var4 > 45 || var3 != 5 && var3 != 4) {
                                continue;
                            }
                        }

                        int var6 = SkillDef.range(var2);
                        if (var6 <= 0) {
                            var6 = SkillDef.radius(var2);
                        }

                        if (var6 <= 0) {
                            var6 = this.atkRange;
                        }

                        int var5 = var6 + this.target.radius;
                        if (this.dist2To(this.target) <= var5 * var5) {
                            return var1;
                        }
                    }
                }
            }

            return -1;
        } else {
            return -1;
        }
    }

    public boolean isNearAiPath(int var1, int var2, int var3) {
        if (this.aiPath != null && this.aiPath.length >= 4) {
            int var4 = var3 * var3;

            for(int var5 = 0; var5 + 3 < this.aiPath.length; var5 += 2) {
                int var6 = this.aiPath[var5];
                int var7 = this.aiPath[var5 + 1];
                int var8 = this.aiPath[var5 + 2];
                int var9 = this.aiPath[var5 + 3];
                int var10 = var8 - var6;
                int var11 = var9 - var7;
                int var12 = var1 - var6;
                int var13 = var2 - var7;
                int var14 = var10 * var10 + var11 * var11;
                int var15 = var6;
                int var16 = var7;
                if (var14 > 0) {
                    int var17 = var12 * var10 + var13 * var11;
                    if (var17 >= var14) {
                        var15 = var8;
                        var16 = var9;
                    } else if (var17 > 0) {
                        var15 = var6 + var10 * var17 / var14;
                        var16 = var7 + var11 * var17 / var14;
                    }
                }

                int var19 = var1 - var15;
                int var18 = var2 - var16;
                if (var19 * var19 + var18 * var18 <= var4) {
                    return true;
                }
            }

            return false;
        } else {
            return true;
        }
    }

    public boolean hasDirectWalkingLineTo(int var1, int var2) {
        return this.canNavigateSegment(this.x, this.y, var1, var2);
    }

    public boolean canSearchAiTarget() {
        return this.targetSearchLockLeft <= 0;
    }

    public void addSpeedBuff(int var1) {
        if (var1 > this.speedBuffLeft) {
            this.speedBuffLeft = var1;
        }

    }

    public void addAttackSpeedBuff(int var1) {
        if (var1 > this.attackSpeedBuffLeft) {
            this.attackSpeedBuffLeft = var1;
        }

    }

    public void addSlow(int var1) {
        if (var1 > this.slowLeft) {
            this.slowLeft = var1;
        }

    }

    public void addStun(int var1) {
        if (var1 > this.stunLeft) {
            this.stunLeft = var1;
            this.vx = 0;
            this.vy = 0;
        }

    }

    public void addShield(int var1) {
        if (var1 > this.shield) {
            this.shield = var1;
        }

    }

    public int heal(int var1) {
        if (this.alive && var1 > 0 && this.hp < this.maxHp) {
            int var2 = this.hp;
            this.hp += var1;
            if (this.hp > this.maxHp) {
                this.hp = this.maxHp;
            }

            return this.hp - var2;
        } else {
            return 0;
        }
    }

    public void grantReward(int var1, int var2) {
        this.gold += var1;
        this.exp += var2;

        while(this.exp >= 100 && this.level < 15) {
            this.exp -= 100;
            this.levelUp();
        }

    }

    private void levelUp() {
        int var1 = this.maxHp;
        int var2 = this.maxMp;
        ++this.level;
        ++this.skillPoints;
        this.applyStats();
        this.hp += this.maxHp - var1;
        this.mp += this.maxMp - var2;
        if (this.hp > this.maxHp) {
            this.hp = this.maxHp;
        }

        if (this.mp > this.maxMp) {
            this.mp = this.maxMp;
        }

        if (this.playerControlled) {
            this.moveVoicePending = false;
            this.tauntVoicePending = true;
        }

        if (!this.playerControlled) {
            this.autoSpendSkillPoints();
        }

    }

    public int rollPassiveAttackBonus() {
        ++this.passiveHits;
        switch (this.heroId) {
            case 0:
                if (this.passiveHits >= 4) {
                    this.passiveHits = 0;
                    this.addAttackSpeedBuff(36);
                    if (this.target instanceof Hero) {
                        ((Hero)this.target).addStun(5);
                    }

                    return 14 + this.level * 3;
                }
                break;
            case 1:
                if (this.passiveHits >= 4) {
                    this.passiveHits = 0;
                    this.addAttackSpeedBuff(40);
                    return this.atk / 2 + this.level * 2;
                }
                break;
            case 2:
                if (this.passiveCharge > 0) {
                    --this.passiveCharge;
                    return 18 + this.level * 3;
                }
                break;
            case 3:
                this.reduceSkillCooldowns(1);
                if (this.passiveHits >= 3) {
                    this.passiveHits = 0;
                    return 10 + this.level * 2;
                }
                break;
            case 4:
                if (this.passiveHits >= 4) {
                    this.passiveHits = 0;
                    if (this.target instanceof Hero) {
                        ((Hero)this.target).addStun(8);
                    }

                    return 12 + this.level * 2;
                }
            case 5:
            case 7:
            case 12:
            case 13:
            default:
                break;
            case 6:
                if (this.passiveHits >= 5) {
                    this.passiveHits = 0;
                    return 16 + this.level * 3;
                }
                break;
            case 8:
                if (this.skillCd[0] > 0) {
                    int var10002 = this.skillCd[0]--;
                }
                break;
            case 9:
                if (this.passiveHits >= 3) {
                    this.passiveHits = 0;
                    this.addAttackSpeedBuff(45);
                    return this.atk / 5;
                }
                break;
            case 10:
                if (this.passiveHits >= 5) {
                    this.passiveHits = 0;
                    return this.atk * 3 / 4 + this.level * 2;
                }
                break;
            case 11:
                if (!this.moving) {
                    return this.atk / 3;
                }
                break;
            case 14:
                if (this.target != null && (this.target.unitType == 3 || this.target.unitType == 4)) {
                    return 12 + this.level * 2;
                }
                break;
            case 15:
                if (this.passiveHits >= 4) {
                    this.passiveHits = 0;
                    return this.maxHp / 25;
                }
                break;
            case 16:
                if (this.passiveHits >= 3) {
                    this.passiveHits = 0;
                    this.heal((this.maxHp - this.hp) / 18 + 4);
                    return 8 + this.level * 2;
                }
                break;
            case 17:
                return (this.maxHp - this.hp) * (12 + this.level) / this.maxHp;
            case 18:
                if (this.hp * 100 / this.maxHp <= 35) {
                    return 10 + this.level * 2;
                }
        }

        return 0;
    }

    public void onSkillCast(int var1) {
        switch (this.heroId) {
            case 0:
                this.addAttackSpeedBuff(24);
            case 1:
            case 4:
            case 6:
            case 7:
            case 8:
            case 10:
            case 11:
            case 12:
            case 13:
            default:
                break;
            case 2:
                this.passiveCharge = 1;
                break;
            case 3:
                this.addAttackSpeedBuff(30);
                break;
            case 5:
                this.addShield(this.maxHp / 12);
                break;
            case 9:
                if (var1 == 0) {
                    this.addAttackSpeedBuff(60);
                }
                break;
            case 14:
                this.addShield(this.maxHp / 16);
        }

    }

    private void reduceSkillCooldowns(int var1) {
        for(int var2 = 0; var2 < this.skillCd.length; ++var2) {
            if (this.skillCd[var2] > 0) {
                int[] var10000 = this.skillCd;
                var10000[var2] -= var1;
                if (this.skillCd[var2] < 0) {
                    this.skillCd[var2] = 0;
                }
            }
        }

    }

    public int passiveSkillBonus() {
        switch (this.heroId) {
            case 5:
                return 8 + this.level * 2;
            case 6:
                return 6 + this.level * 2 + this.passiveHits;
            case 7:
                return 7 + this.level * 2;
            case 14:
                if (this.target != null && this.target.unitType == 3) {
                    return 12 + this.level * 2;
                }
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            default:
                return 0;
        }
    }

    public int passiveDamageTakenPct() {
        switch (this.heroId) {
            case 7:
                return 82;
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 14:
            case 15:
            default:
                return 100;
            case 13:
                return this.slowLeft > 0 ? 82 : 92;
            case 16:
                return 86;
            case 17:
                return this.hp * 100 / this.maxHp < 35 ? 82 : 92;
            case 18:
                return this.hp * 100 / this.maxHp < 35 ? 68 : 90;
        }
    }

    public void onPassiveKill() {
        if (this.heroId == 0) {
            this.heal(24 + this.level * 4);
        } else if (this.heroId == 1) {
            this.skillCd[0] = 0;
            this.addSpeedBuff(30);
        }

    }

    public int passiveAuraHeal() {
        if (this.heroId == 12) {
            return 3 + this.level / 2;
        } else {
            return this.heroId == 13 ? 1 + this.level / 3 : 0;
        }
    }

    public int takeDamage(int var1) {
        if (var1 > 0) {
            if (this.recallLeft > 0) {
                this.cancelRecall();
            }

            if (this.inBush) {
                this.reveal(15);
            }

            if (this.heroId == 12 && this.passiveGuardCd <= 0) {
                this.heal(18 + this.level * 3);
                this.addSpeedBuff(24);
                this.passiveGuardCd = 120;
            }

            if (this.heroId == 5 && this.shield <= 0 && this.passiveGuardCd <= 0) {
                this.addShield(this.maxHp / 10);
                this.passiveGuardCd = 150;
            }
        }

        return super.takeDamage(var1);
    }

    public boolean wantsAttack() {
        return this.alive && this.recallLeft <= 0 && this.stunLeft <= 0 && this.target != null && this.target.alive && this.inRange(this.target) && this.atkCdLeft <= 0;
    }

    public void resetAttackCd() {
        int var1 = this.atkCooldown;
        if (this.attackSpeedBuffLeft > 0) {
            var1 = var1 * 70 / 100;
        }

        if (var1 < 4) {
            var1 = 4;
        }

        this.atkCdLeft = var1;
    }

    public int attackSpeed10() {
        int var1 = this.atkCooldown;
        if (this.attackSpeedBuffLeft > 0) {
            var1 = var1 * 70 / 100;
        }

        if (var1 < 4) {
            var1 = 4;
        }

        return 150 / var1;
    }

    public void faceTarget(Entity var1) {
        if (var1 != null && !this.moving) {
            this.faceToward(var1.x, var1.y);
        }
    }

    public int facingDx() {
        if (this.facing == 1) {
            return -1;
        } else {
            return this.facing == 2 ? 1 : 0;
        }
    }

    public int facingDy() {
        if (this.facing == 3) {
            return -1;
        } else {
            return this.facing == 0 ? 1 : 0;
        }
    }

    public boolean isMoving() {
        return this.moving;
    }

    public boolean teleportTo(int var1, int var2) {
        if (!this.canStand(var1, var2)) {
            return false;
        } else {
            this.x = var1;
            this.y = var2;
            return true;
        }
    }

    public void render(Graphics2D var1, int var2, int var3) {
        if (this.alive) {
            int var4 = IsoMath.toScreenX(this.x, this.y) - var2;
            int var5 = IsoMath.toScreenY(this.x, this.y) - var3;
            var1.setColor(G2D.color(0));
            var1.fillArc(var4 - 9, var5 - 4, 18, 8, 0, 360);
            var1.setColor(G2D.color(this.team == 0 ? 3968255 : 16731212));
            var1.drawArc(var4 - 9, var5 - 4, 18, 8, 0, 360);
            if (this.playerControlled) {
                var1.setColor(G2D.color(16765514));
                var1.drawArc(var4 - 11, var5 - 5, 22, 10, 0, 360);
            }

            this.sprite.setRefPixelPosition(var4, var5);
            this.sprite.paint(var1);
            this.drawHpBar(var1, var4, var5, 20, 40);
            this.drawStatusMarks(var1, var4, var5);
            if (this.recallLeft > 0) {
                byte var6 = 22;
                int var7 = var6 * (45 - this.recallLeft) / 45;
                var1.setColor(G2D.color(2105376));
                var1.fillRect(var4 - 11, var5 - 40 - 6, var6 + 2, 4);
                var1.setColor(G2D.color(8442111));
                if (var7 > 0) {
                    var1.fillRect(var4 - 10, var5 - 40 - 5, var7, 2);
                }
            }

        }
    }

    private void drawStatusMarks(Graphics2D var1, int var2, int var3) {
        int var4 = var2 - 9;
        int var5 = var3 - 40 + 1;
        if (this.redBuffLeft > 0) {
            var1.setColor(G2D.color(16742972));
            var1.fillRect(var4, var5, 3, 3);
            var4 += 4;
        }

        if (this.blueBuffLeft > 0) {
            var1.setColor(G2D.color(7317759));
            var1.fillRect(var4, var5, 3, 3);
            var4 += 4;
        }

        if (this.speedBuffLeft > 0) {
            var1.setColor(G2D.color(9240480));
            var1.fillRect(var4, var5, 3, 3);
            var4 += 4;
        }

        if (this.slowLeft > 0) {
            var1.setColor(G2D.color(11575488));
            var1.fillRect(var4, var5, 3, 3);
            var4 += 4;
        }

        if (this.stunLeft > 0) {
            var1.setColor(G2D.color(16767040));
            var1.fillRect(var4, var5, 3, 3);
            var4 += 4;
        }

        if (this.attackSpeedBuffLeft > 0) {
            var1.setColor(G2D.color(16758861));
            var1.fillRect(var4, var5, 3, 3);
        }

    }

    public boolean isPlayerControlled() {
        return this.playerControlled;
    }

    public void respawn() {
        this.alive = true;
        this.hp = this.maxHp;
        this.mp = this.maxMp;
        this.shield = 0;
        this.x = this.homeX;
        this.y = this.homeY;
        this.recallLeft = 0;
        this.killStreak = 0;
        this.target = null;
        this.retreating = false;
        this.holdBack = false;
        this.aiPathIndex = 0;
        this.navBestD2 = Integer.MAX_VALUE;
        this.navStallFrames = 0;
        this.navRecoveryCount = 0;
        this.targetSearchLockLeft = 0;
        this.detourLeft = 0;
        this.speedBuffLeft = 0;
        this.slowLeft = 0;
        this.stunLeft = 0;
        this.attackSpeedBuffLeft = 0;
        this.passiveCharge = 0;
        this.passiveGuardCd = 0;
    }

    private void tickCooldowns() {
        if (this.atkCdLeft > 0) {
            --this.atkCdLeft;
        }

        for(int var1 = 0; var1 < this.skillCd.length; ++var1) {
            if (this.skillCd[var1] > 0) {
                int var10002 = this.skillCd[var1]--;
            }
        }

        if (this.speedBuffLeft > 0) {
            --this.speedBuffLeft;
        }

        if (this.slowLeft > 0) {
            --this.slowLeft;
        }

        if (this.stunLeft > 0) {
            --this.stunLeft;
        }

        if (this.passiveGuardCd > 0) {
            --this.passiveGuardCd;
        }

        if (this.attackSpeedBuffLeft > 0) {
            --this.attackSpeedBuffLeft;
        }

        if (this.redBuffLeft > 0) {
            --this.redBuffLeft;
        }

        if (this.blueBuffLeft > 0) {
            --this.blueBuffLeft;
        }

        if (this.revealLeft > 0) {
            --this.revealLeft;
        }

    }

    public boolean isHidden() {
        return this.alive && this.inBush && this.revealLeft <= 0;
    }

    public void reveal(int var1) {
        if (var1 > this.revealLeft) {
            this.revealLeft = var1;
        }

    }

    private void moveToward(int var1, int var2) {
        int var3 = var1;
        int var4 = var2;
        if (this.detourLeft > 0) {
            if (FMath.dist2(this.x, this.y, this.detourX, this.detourY) <= 64) {
                this.detourLeft = 0;
                this.navBestD2 = Integer.MAX_VALUE;
            } else {
                var3 = this.detourX;
                var4 = this.detourY;
                --this.detourLeft;
            }
        }

        this.steerToward(var3, var4, this.getMoveSpeed());
        if (this.vx != 0 || this.vy != 0) {
            this.faceToward(var3, var4);
        }

    }

    private void followAiPath() {
        if (this.aiPath != null && this.aiPathIndex * 2 + 1 < this.aiPath.length) {
            int var1 = this.aiPath[this.aiPathIndex * 2];
            int var2 = this.aiPath[this.aiPathIndex * 2 + 1];

            for(int var3 = 0; this.terrain != null && !this.canNavigateTo(var1, var2) && this.aiPathIndex * 2 + 3 < this.aiPath.length && var3 < 3; var2 = this.aiPath[this.aiPathIndex * 2 + 1]) {
                ++this.aiPathIndex;
                ++var3;
                var1 = this.aiPath[this.aiPathIndex * 2];
            }

            short var4 = 900;
            if (this.terrain != null && this.terrain.hitsSolid(var1, var2, 12)) {
                var4 = 2200;
            }

            if (FMath.dist2(this.x, this.y, var1, var2) <= var4) {
                ++this.aiPathIndex;
                this.vx = 0;
                this.vy = 0;
            } else {
                this.moveToward(var1, var2);
            }
        } else {
            if (this.pushX == 0 && this.pushY == 0) {
                this.vx = 0;
                this.vy = 0;
            } else {
                this.moveToward(this.pushX, this.pushY);
            }

        }
    }

    private int safeLaneX() {
        if (this.aiPath != null && this.aiPath.length >= 2) {
            int var1 = this.aiPathIndex - 1;
            if (var1 < 0) {
                var1 = 0;
            }

            if (var1 * 2 + 1 >= this.aiPath.length) {
                var1 = this.aiPath.length / 2 - 1;
            }

            return this.aiPath[var1 * 2];
        } else {
            return this.homeX;
        }
    }

    private int safeLaneY() {
        if (this.aiPath != null && this.aiPath.length >= 2) {
            int var1 = this.aiPathIndex - 1;
            if (var1 < 0) {
                var1 = 0;
            }

            if (var1 * 2 + 1 >= this.aiPath.length) {
                var1 = this.aiPath.length / 2 - 1;
            }

            return this.aiPath[var1 * 2 + 1];
        } else {
            return this.homeY;
        }
    }

    private void faceToward(int var1, int var2) {
        int var3 = var1 - this.x;
        int var4 = var2 - this.y;
        if (var3 != 0 || var4 != 0) {
            if (FMath.abs(var3) >= FMath.abs(var4)) {
                this.facing = var3 < 0 ? 1 : 2;
            } else {
                this.facing = var4 < 0 ? 3 : 0;
            }

        }
    }

    public void playAnim(int var1) {
        if (var1 >= 1 && var1 <= 4) {
            this.animMode = var1;
            this.animPhase = 0;
            if (var1 == 1) {
                this.animLeft = 6;
            } else if (var1 == 4) {
                this.animLeft = 12;
            } else {
                this.animLeft = 8;
            }

            this.updateSprite();
        }
    }

    public boolean consumeMoveVoice() {
        if (!this.moveVoicePending) {
            return false;
        } else {
            this.moveVoicePending = false;
            return true;
        }
    }

    public boolean consumeTauntVoice() {
        if (!this.tauntVoicePending) {
            return false;
        } else {
            this.tauntVoicePending = false;
            return true;
        }
    }

    private void maybeQueueMoveVoice() {
        if (this.moveVoiceCd <= 0) {
            if ((this.animTick + this.heroId * 17 & 3) == 0) {
                this.moveVoicePending = true;
                this.moveVoiceCd = 180;
            }

        }
    }

    private void tickAnim() {
        if (this.moveVoiceCd > 0) {
            --this.moveVoiceCd;
        }

        if (this.animMode != 0) {
            ++this.animPhase;
            if (this.animLeft > 0) {
                --this.animLeft;
            }

            if (this.animLeft <= 0) {
                this.animMode = 0;
                this.animPhase = 0;
            }

        }
    }

    private void updateSprite() {
        int var1;
        if (this.animMode == 1) {
            var1 = 8 + this.facing;
        } else if (this.animMode == 2) {
            var1 = 12 + (this.animPhase / 4 & 1);
        } else if (this.animMode == 3) {
            var1 = 14 + (this.animPhase / 4 & 1);
        } else if (this.animMode == 4) {
            var1 = 16 + (this.animPhase / 3 & 3);
        } else {
            int var2 = 0;
            if (this.moving) {
                var2 = this.animTick / 3 & 1;
            }

            var1 = this.facing * 2 + var2;
        }

        int var3 = this.sprite.getRawFrameCount();
        if (var1 >= var3) {
            var1 = this.facing * 2 + (this.moving ? this.animTick / 3 & 1 : 0);
            if (var1 >= var3) {
                var1 = 0;
            }
        }

        this.sprite.setFrame(var1);
    }
}
