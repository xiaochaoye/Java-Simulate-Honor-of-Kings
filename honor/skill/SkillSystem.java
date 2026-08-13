package honor.skill;

import honor.battle.BattleWorld;
import honor.core.FMath;
import honor.entity.Entity;
import honor.entity.Hero;

public final class SkillSystem {
    private static final int DASH_STEP = 8;
    private static final int DASH_HIT_HALF_W = 18;
    private static final int LINE_HIT_HALF_W = 14;
    private static final int SELF_AURA_RADIUS = 28;

    private SkillSystem() {
    }

    public static boolean cast(BattleWorld var0, Hero var1, int var2) {
        if (var0 != null && var1 != null && var1.canCast(var2)) {
            int var3 = var1.skillId(var2);
            boolean var4;
            switch (SkillDef.type(var3)) {
                case 0:
                    var4 = castNuke(var0, var1, var3);
                    break;
                case 1:
                    var4 = castAoe(var0, var1, var3);
                    break;
                case 2:
                    var4 = castDash(var0, var1, var3);
                    break;
                case 3:
                    var4 = castLine(var0, var1, var3);
                    break;
                case 4:
                    var4 = castBuff(var0, var1, var3);
                    break;
                case 5:
                    var4 = castHeal(var0, var1, var3);
                    break;
                default:
                    var4 = false;
            }

            if (var4) {
                applyCastEffects(var0, var1, var3);
                int var5 = SkillDef.shieldPct(var3);
                if (var5 > 0) {
                    var1.addShield(var1.maxHp * var5 / 100);
                }

                var1.onSkillCast(var2);
                var1.paySkill(var2);
                var0.spawnSkillLabel(var1.x, var1.y, SkillDef.name(var3), SkillDef.color(var3));
                if (var2 == 2) {
                    var1.playAnim(4);
                } else if (var2 == 1) {
                    var1.playAnim(3);
                } else {
                    var1.playAnim(2);
                }
            }

            return var4;
        } else {
            return false;
        }
    }

    private static boolean castNuke(BattleWorld var0, Hero var1, int var2) {
        Entity var3 = var1.target;
        if (var3 != null && var3.alive) {
            int var4 = SkillDef.range(var2) + var3.radius;
            if (var1.dist2To(var3) > var4 * var4) {
                return false;
            } else {
                int var5 = SkillDef.color(var2);
                var1.faceTarget(var3);
                var0.spawnSkillSlash(var1, var3, var5);
                var0.spawnSkillRing(var3.x, var3.y, 18, var1.team, var5);
                var0.applySkillHit(var1, var3, power(var1, var2), var5);
                if (var3 instanceof Hero) {
                    applyTargetControl((Hero)var3, var2);
                }

                return true;
            }
        } else {
            return false;
        }
    }

    private static boolean castAoe(BattleWorld var0, Hero var1, int var2) {
        int var3 = var1.x;
        int var4 = var1.y;
        int var5 = SkillDef.range(var2);
        Entity var6 = var1.target;
        if (var5 > 0 && var6 != null && var6.alive && var1.dist2To(var6) <= var5 * var5) {
            var3 = var6.x;
            var4 = var6.y;
            var1.faceTarget(var6);
        }

        int var7 = SkillDef.radius(var2);
        int var8 = SkillDef.color(var2);
        var0.spawnSkillRing(var3, var4, var7, var1.team, var8);
        var0.damageEnemiesInRadius(var1, var3, var4, var7, power(var1, var2), var8);
        var0.affectEnemyHeroesInRadius(var1, var3, var4, var7, SkillDef.effect(var2), controlFrames(var2));
        if ((SkillDef.effect(var2) & 32) != 0) {
            var0.spawnSkillRing(var3, var4, var7 * 2 / 3, var1.team, brighten(var8));
        }

        return true;
    }

    private static boolean castDash(BattleWorld var0, Hero var1, int var2) {
        Entity var5 = var1.target;
        int var6 = SkillDef.range(var2);
        int var3;
        int var4;
        if (var5 != null && var5.alive && var1.dist2To(var5) <= var6 * var6) {
            var3 = var5.x - var1.x;
            var4 = var5.y - var1.y;
            var1.faceTarget(var5);
        } else {
            var3 = var1.facingDx() * 100;
            var4 = var1.facingDy() * 100;
        }

        int var7 = FMath.isqrt(var3 * var3 + var4 * var4);
        if (var7 <= 0) {
            return false;
        } else {
            int var8 = var1.x;
            int var9 = var1.y;
            int var10 = SkillDef.radius(var2);
            int var11 = var10 / 8;
            int var12 = var8;
            int var13 = var9;

            for(int var14 = 1; var14 <= var11; ++var14) {
                int var15 = var14 * 8;
                int var16 = var8 + var3 * var15 / var7;
                int var17 = var9 + var4 * var15 / var7;
                if (!var1.teleportTo(var16, var17)) {
                    break;
                }

                var12 = var16;
                var13 = var17;
            }

            int var18 = SkillDef.color(var2);
            var0.spawnSkillTrail(var8, var9, var12, var13, var1.team, var18);
            var0.damageEnemiesAlongLine(var1, var8, var9, var12, var13, 18, power(var1, var2), var18);
            var0.affectEnemyHeroesAlongLine(var1, var8, var9, var12, var13, 18, SkillDef.effect(var2), controlFrames(var2));
            return true;
        }
    }

    private static boolean castLine(BattleWorld var0, Hero var1, int var2) {
        Entity var5 = var1.target;
        int var3;
        int var4;
        if (var5 != null && var5.alive) {
            var3 = var5.x - var1.x;
            var4 = var5.y - var1.y;
            var1.faceTarget(var5);
        } else {
            var3 = var1.facingDx() * 100;
            var4 = var1.facingDy() * 100;
        }

        int var6 = FMath.isqrt(var3 * var3 + var4 * var4);
        if (var6 <= 0) {
            return false;
        } else {
            int var7 = SkillDef.radius(var2);
            int var8 = var1.x + var3 * var7 / var6;
            int var9 = var1.y + var4 * var7 / var6;
            int var10 = SkillDef.color(var2);
            var0.spawnSkillBeam(var1.x, var1.y, var8, var9, var1.team, var10);
            var0.damageEnemiesAlongLine(var1, var1.x, var1.y, var8, var9, 14, power(var1, var2), var10);
            var0.affectEnemyHeroesAlongLine(var1, var1.x, var1.y, var8, var9, 14, SkillDef.effect(var2), controlFrames(var2));
            return true;
        }
    }

    private static boolean castBuff(BattleWorld var0, Hero var1, int var2) {
        int var3 = SkillDef.color(var2);
        int var4 = SkillDef.duration(var2);
        if (var4 > 0) {
            var1.addSpeedBuff(var4);
        }

        var0.spawnSkillAura(var1, 28, var3);
        var0.spawnSkillLabel(var1.x, var1.y, SkillDef.name(var2), var3);
        return true;
    }

    private static boolean castHeal(BattleWorld var0, Hero var1, int var2) {
        int var3 = SkillDef.radius(var2);
        int var4 = SkillDef.color(var2);
        int var5 = power(var1, var2);
        var0.spawnSkillRing(var1.x, var1.y, var3, var1.team, var4);
        var0.healAlliesInRadius(var1, var1.x, var1.y, var3, var5);
        var0.damageEnemiesInRadius(var1, var1.x, var1.y, var3, var5, var4);
        return true;
    }

    private static void applyCastEffects(BattleWorld var0, Hero var1, int var2) {
        int var3 = SkillDef.effect(var2);
        int var4 = power(var1, var2);
        int var5 = SkillDef.radius(var2);
        if (var5 < 56) {
            var5 = 56;
        }

        if ((var3 & 4) != 0) {
            var1.heal(var4 / 2 + var1.maxHp / 20);
            var0.spawnSkillAura(var1, 30, SkillDef.color(var2));
        }

        if ((var3 & 8) != 0 && SkillDef.type(var2) != 5) {
            var0.healAlliesInRadius(var1, var1.x, var1.y, var5, var4 / 3 + 4);
        }

        if ((var3 & 16) != 0) {
            int var6 = SkillDef.duration(var2);
            if (var6 <= 0) {
                var6 = 36;
            }

            if ((var3 & 8) != 0) {
                var0.speedAlliesInRadius(var1, var5, var6);
            } else {
                var1.addSpeedBuff(var6);
            }
        }

    }

    private static void applyTargetControl(Hero var0, int var1) {
        int var2 = SkillDef.effect(var1);
        int var3 = controlFrames(var1);
        if ((var2 & 1) != 0) {
            var0.addSlow(var3 * 2);
        }

        if ((var2 & 2) != 0) {
            var0.addStun(var3);
        }

    }

    private static int controlFrames(int var0) {
        int var1 = var0 % 3 == 2 ? 12 : 7;
        int var2 = SkillDef.duration(var0);
        if (var2 > 0 && var2 < 30) {
            var1 = var2;
        }

        return var1;
    }

    private static int brighten(int var0) {
        int var1 = (var0 >> 16 & 255) + 36;
        int var2 = (var0 >> 8 & 255) + 36;
        int var3 = (var0 & 255) + 36;
        if (var1 > 255) {
            var1 = 255;
        }

        if (var2 > 255) {
            var2 = 255;
        }

        if (var3 > 255) {
            var3 = 255;
        }

        return var1 << 16 | var2 << 8 | var3;
    }

    private static int power(Hero var0, int var1) {
        int var2 = var1 % 3;
        int var3 = var0.skillLv[var2];
        if (var3 < 1) {
            var3 = 1;
        }

        return var0.atk * SkillDef.power(var1, var3) / 100 + var0.passiveSkillBonus();
    }

    public static int distToSegment2(int var0, int var1, int var2, int var3, int var4, int var5) {
        int var6 = var4 - var2;
        int var7 = var5 - var3;
        int var8 = var6 * var6 + var7 * var7;
        if (var8 == 0) {
            return FMath.dist2(var0, var1, var2, var3);
        } else {
            int var9 = (var0 - var2) * var6 + (var1 - var3) * var7;
            if (var9 < 0) {
                var9 = 0;
            } else if (var9 > var8) {
                var9 = var8;
            }

            int var10 = var2 + (int)((long)var6 * (long)var9 / (long)var8);
            int var11 = var3 + (int)((long)var7 * (long)var9 / (long)var8);
            return FMath.dist2(var0, var1, var10, var11);
        }
    }
}
