package honor.hero;

public final class HeroDef {
    public static final int COUNT = 19;
    public static final int ROLE_ASSASSIN = 0;
    public static final int ROLE_FIGHTER = 1;
    public static final int ROLE_MAGE = 2;
    public static final int ROLE_MARKSMAN = 3;
    public static final int ROLE_SUPPORT = 4;
    public static final int ROLE_TANK = 5;
    public static final int ROLE_COUNT = 6;
    public static final int SKILL_SLOTS = 3;
    public static final int MAX_LEVEL = 15;
    private static final String[] PASSIVE_NAMES = new String[]{"杀意之枪", "侠客行", "二天一流", "争霸", "石之炼金", "冰封之心", "恶德医疗", "锦襕宝衣", "活力迸发", "惩戒射击", "火力压制", "瞄准", "长歌行", "自然意志", "磁力屏障", "君主野望", "毫发无伤", "舍生忘死", "陷阵之志"};
    private static final String[] PASSIVE_DESCS = new String[]{"技能命中强化攻速，第4次普攻额外挑飞伤害", "连续普攻积累剑气，第4次强化并加速", "释放技能后获得一层势，强化下一次普攻", "技能与普攻叠加攻速并缩短技能冷却", "伤害有概率石化英雄", "脱战获得寒冰护盾，护盾破裂减速敌人", "技能与普攻叠加毒药印记并追加伤害", "单次重伤受到锦襕宝衣减免", "普攻缩短翻滚突袭冷却", "普攻叠加惩戒射击并提升攻速", "第5次普攻触发强化扫射", "原地瞄准时普攻伤害提高", "受伤触发回血与加速，附近友军持续恢复", "周期解除减速并获得减伤", "技能可干扰机关并获得护盾", "第4次普攻附带最大生命伤害", "损失生命转为回复并提高承伤能力", "生命越低攻击越高", "低生命时触发陷阵减伤"};
    private static final String[] NAMES = new String[]{"韩信", "李白", "宫本武藏", "曹操", "钟无艳", "王昭君", "扁鹊", "金蝉", "孙尚香", "后羿", "鲁班七号", "百里守约", "蔡文姬", "庄周", "刘禅", "刘邦", "猪八戒", "程咬金", "项羽"};
    private static final String[] ROLES = new String[]{"刺客", "战士", "法师", "射手", "辅助", "坦克"};
    private static final byte[] ROLE_IDS = new byte[]{0, 0, 1, 1, 1, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 5, 5, 5, 5};
    private static final byte[][] HEROES_BY_ROLE = new byte[][]{{0, 1}, {2, 3, 4}, {5, 6, 7}, {8, 9, 10, 11}, {12, 13, 14}, {15, 16, 17, 18}};
    private static final short[] BASE_HP = new short[]{555, 545, 675, 690, 720, 520, 555, 570, 510, 525, 535, 500, 570, 650, 760, 800, 850, 825, 860};
    private static final short[] BASE_MP = new short[]{190, 200, 210, 190, 200, 300, 285, 310, 220, 220, 210, 230, 320, 300, 240, 210, 180, 180, 190};
    private static final short[] BASE_ATK = new short[]{70, 68, 62, 64, 60, 61, 60, 58, 68, 66, 65, 72, 45, 48, 50, 50, 52, 55, 50};
    private static final short[] BASE_DEF = new short[]{8, 8, 12, 13, 14, 6, 7, 8, 5, 5, 6, 5, 8, 11, 16, 18, 19, 18, 20};
    private static final short[] ATK_RANGE = new short[]{30, 30, 32, 32, 32, 68, 64, 64, 74, 76, 72, 82, 68, 62, 32, 30, 30, 30, 30};
    private static final short[] ATK_CD = new short[]{12, 12, 14, 14, 15, 16, 15, 16, 13, 13, 12, 16, 16, 15, 15, 16, 15, 14, 16};
    private static final short[] MOVE_BONUS = new short[]{1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    private static final boolean[] RANGED = new boolean[]{false, false, false, false, false, true, true, true, true, true, true, true, true, true, false, false, false, false, false};
    private static final short[] HP_GROW = new short[]{58, 56, 72, 74, 76, 54, 58, 60, 50, 52, 54, 50, 58, 66, 78, 84, 88, 86, 90};
    private static final short[] MP_GROW = new short[]{12, 12, 12, 12, 12, 20, 20, 22, 14, 14, 14, 14, 22, 20, 14, 12, 10, 10, 10};
    private static final short[] ATK_GROW = new short[]{9, 9, 7, 8, 7, 8, 8, 7, 9, 9, 9, 10, 5, 5, 5, 5, 6, 6, 5};
    private static final short[] DEF_GROW = new short[]{1, 1, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 3, 3, 3, 3};
    private static final byte[] STAT_TOUGH = new byte[]{4, 4, 7, 7, 8, 4, 5, 5, 3, 3, 3, 3, 5, 7, 8, 9, 10, 10, 10};
    private static final byte[] STAT_POWER = new byte[]{9, 9, 7, 8, 7, 9, 8, 8, 10, 9, 10, 10, 4, 5, 5, 5, 6, 6, 5};
    private static final byte[] STAT_MOBILE = new byte[]{10, 9, 7, 6, 5, 5, 5, 5, 7, 6, 5, 6, 6, 6, 5, 5, 5, 5, 4};
    private static final int[] THEME = new int[]{14174280, 5273800, 4217000, 11024440, 12093520, 7915752, 8409256, 14727248, 5810248, 13672512, 13146168, 11055296, 6867160, 5814440, 5810296, 11028592, 13662280, 13127736, 5269664};

    private HeroDef() {
    }

    public static String name(int var0) {
        return NAMES[var0];
    }

    public static String role(int var0) {
        return ROLES[ROLE_IDS[var0]];
    }

    public static int roleId(int var0) {
        return ROLE_IDS[var0];
    }

    public static String roleName(int var0) {
        return ROLES[var0];
    }

    public static int roleHeroCount(int var0) {
        return HEROES_BY_ROLE[var0].length;
    }

    public static int heroAtRole(int var0, int var1) {
        return HEROES_BY_ROLE[var0][var1];
    }

    public static int maxHp(int var0, int var1) {
        return BASE_HP[var0] + HP_GROW[var0] * (var1 - 1);
    }

    public static int maxMp(int var0, int var1) {
        return BASE_MP[var0] + MP_GROW[var0] * (var1 - 1);
    }

    public static int atk(int var0, int var1) {
        return BASE_ATK[var0] + ATK_GROW[var0] * (var1 - 1);
    }

    public static int def(int var0, int var1) {
        return BASE_DEF[var0] + DEF_GROW[var0] * (var1 - 1);
    }

    public static int atkRange(int var0) {
        return ATK_RANGE[var0];
    }

    public static int atkCooldown(int var0) {
        return ATK_CD[var0];
    }

    public static int moveBonus(int var0) {
        return MOVE_BONUS[var0];
    }

    public static boolean ranged(int var0) {
        return RANGED[var0];
    }

    public static int skill(int var0, int var1) {
        return var0 * 3 + var1;
    }

    public static int statTough(int var0) {
        return STAT_TOUGH[var0];
    }

    public static int statPower(int var0) {
        return STAT_POWER[var0];
    }

    public static int statMobile(int var0) {
        return STAT_MOBILE[var0];
    }

    public static int theme(int var0) {
        return THEME[var0];
    }

    public static String spritePath(int var0) {
        return "/res/img/hero_" + var0 + ".png";
    }

    public static String facePath(int var0) {
        return "/res/img/face_" + var0 + ".jpg";
    }

    public static String voiceName(int var0, int var1) {
        return "hero_" + var0 + "_" + var1;
    }

    public static String passiveName(int var0) {
        return PASSIVE_NAMES[var0];
    }

    public static String passiveDesc(int var0) {
        return PASSIVE_DESCS[var0];
    }
}
