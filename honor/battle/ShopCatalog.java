package honor.battle;

import honor.entity.Hero;

public final class ShopCatalog {
    public static final int ITEM_SWORD = 0;
    public static final int ITEM_RUBY = 1;
    public static final int ITEM_ARMOR = 2;
    public static final int ITEM_BOOTS = 3;
    public static final int ITEM_GREATSWORD = 4;
    public static final int ITEM_BELT = 5;
    public static final int ITEM_AXE = 6;
    public static final int ITEM_RUIN = 7;
    public static final int ITEM_OMEN = 8;
    public static final int ITEM_OVERLORD = 9;
    public static final int ITEM_RESIST_BOOTS = 10;
    public static final int ITEM_GUARDIAN = 11;
    public static final int COUNT = 12;
    public static final int PAGE_SIZE = 3;
    public static final int PAGE_COUNT = 4;
    private static final String[] NAMES = new String[]{"铁剑", "红玛瑙", "布甲", "神速之靴", "风暴巨剑", "力量腰带", "暗影战斧", "破军", "不祥征兆", "霸者重装", "抵抗之靴", "贤者庇护"};
    private static final short[] PRICES = new short[]{250, 260, 220, 250, 520, 520, 620, 980, 700, 850, 480, 900};
    private static final short[] BONUS_ATK = new short[]{10, 0, 0, 0, 22, 0, 48, 80, 0, 0, 0, 0};
    private static final short[] BONUS_HP = new short[]{0, 120, 0, 0, 0, 260, 220, 0, 720, 900, 0, 480};
    private static final byte[] BONUS_DEF = new byte[]{0, 0, 8, 0, 0, 0, 0, 0, 28, 10, 18, 42};
    private static final byte[] BONUS_MOVE = new byte[]{0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 2, 0};
    private static final int[] RECIPES = new int[]{0, 0, 0, 0, 0, 0, 17, 17, 38, 34, 12, 36};

    private ShopCatalog() {
    }

    public static int price(int var0) {
        return valid(var0) ? PRICES[var0] : 9999;
    }

    public static int fullPrice(int var0) {
        if (!valid(var0)) {
            return 0;
        } else {
            int var1 = PRICES[var0];
            int var2 = RECIPES[var0];

            for(int var3 = 0; var3 < 12; ++var3) {
                if ((var2 & 1 << var3) != 0) {
                    var1 += fullPrice(var3);
                }
            }

            return var1;
        }
    }

    public static int economy(Hero var0) {
        if (var0 == null) {
            return 0;
        } else {
            int var1 = var0.gold;

            for(int var2 = 0; var2 < 12; ++var2) {
                if ((var0.equipMask & 1 << var2) != 0) {
                    var1 += fullPrice(var2);
                }
            }

            return var1;
        }
    }

    public static String name(int var0) {
        return valid(var0) ? NAMES[var0] : "?";
    }

    public static String statText(int var0) {
        if (!valid(var0)) {
            return "";
        } else {
            String var1 = "";
            if (BONUS_ATK[var0] > 0) {
                var1 = var1 + "+" + BONUS_ATK[var0] + "攻";
            }

            if (BONUS_HP[var0] > 0) {
                var1 = var1 + (var1.length() > 0 ? " " : "") + "+" + BONUS_HP[var0] + "血";
            }

            if (BONUS_DEF[var0] > 0) {
                var1 = var1 + (var1.length() > 0 ? " " : "") + "+" + BONUS_DEF[var0] + "甲";
            }

            if (BONUS_MOVE[var0] > 0) {
                var1 = var1 + (var1.length() > 0 ? " " : "") + "+" + BONUS_MOVE[var0] + "速";
            }

            return var1;
        }
    }

    public static String recipeText(int var0) {
        if (valid(var0) && RECIPES[var0] != 0) {
            String var1 = "";
            int var2 = RECIPES[var0];

            for(int var3 = 0; var3 < 12; ++var3) {
                if ((var2 & 1 << var3) != 0) {
                    var1 = var1 + (var1.length() > 0 ? "+" : "") + NAMES[var3];
                }
            }

            return var1;
        } else {
            return "可直接购买";
        }
    }

    public static int itemAt(int var0, int var1) {
        int var2 = var0 * 3 + var1;
        return valid(var2) ? var2 : -1;
    }

    public static boolean isAdvanced(int var0) {
        return valid(var0) && RECIPES[var0] != 0;
    }

    public static boolean owned(Hero var0, int var1) {
        return var0 != null && valid(var1) && (var0.equipMask & 1 << var1) != 0;
    }

    public static boolean hasRecipe(Hero var0, int var1) {
        if (var0 != null && valid(var1)) {
            int var2 = RECIPES[var1];
            return var2 == 0 || (var0.equipMask & var2) == var2;
        } else {
            return false;
        }
    }

    public static boolean canBuy(Hero var0, int var1) {
        return var0 != null && valid(var1) && !owned(var0, var1) && hasRecipe(var0, var1) && var0.gold >= price(var1);
    }

    public static boolean tryBuy(Hero var0, int var1) {
        if (!canBuy(var0, var1)) {
            return false;
        } else {
            int var2 = var0.maxHp;
            int var3 = RECIPES[var1];
            if (var3 != 0) {
                for(int var4 = 0; var4 < 12; ++var4) {
                    if ((var3 & 1 << var4) != 0) {
                        var0.equipMask &= ~(1 << var4);
                        applyBonus(var0, var4, -1);
                    }
                }
            }

            var0.gold -= price(var1);
            var0.equipMask |= 1 << var1;
            applyBonus(var0, var1, 1);
            var0.applyStats();
            int var5 = var0.maxHp - var2;
            if (var5 > 0) {
                var0.hp += var5;
                if (var0.hp > var0.maxHp) {
                    var0.hp = var0.maxHp;
                }
            }

            return true;
        }
    }

    public static void autoBuyCheapest(Hero var0) {
        int var1 = -1;
        int var2 = Integer.MAX_VALUE;

        for(int var3 = 0; var3 < 12; ++var3) {
            if (isAdvanced(var3) && canBuy(var0, var3) && price(var3) < var2) {
                var1 = var3;
                var2 = price(var3);
            }
        }

        if (var1 < 0) {
            for(int var4 = 0; var4 < 12; ++var4) {
                if (!isAdvanced(var4) && canBuy(var0, var4) && price(var4) < var2) {
                    var1 = var4;
                    var2 = price(var4);
                }
            }
        }

        if (var1 >= 0) {
            tryBuy(var0, var1);
        }

    }

    private static void applyBonus(Hero var0, int var1, int var2) {
        var0.bonusAtk += BONUS_ATK[var1] * var2;
        var0.bonusHp += BONUS_HP[var1] * var2;
        var0.bonusDef += BONUS_DEF[var1] * var2;
        var0.moveBonus += BONUS_MOVE[var1] * var2;
    }

    private static boolean valid(int var0) {
        return var0 >= 0 && var0 < 12;
    }
}
