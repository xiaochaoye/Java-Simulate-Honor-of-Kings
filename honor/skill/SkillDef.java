package honor.skill;

public final class SkillDef {
    public static final int TYPE_NUKE = 0;
    public static final int TYPE_AOE = 1;
    public static final int TYPE_DASH = 2;
    public static final int TYPE_LINE = 3;
    public static final int TYPE_BUFF = 4;
    public static final int TYPE_HEAL = 5;
    public static final int EFFECT_SLOW = 1;
    public static final int EFFECT_STUN = 2;
    public static final int EFFECT_SELF_HEAL = 4;
    public static final int EFFECT_TEAM = 8;
    public static final int EFFECT_SPEED = 16;
    public static final int EFFECT_MULTI = 32;
    public static final int COUNT = 57;
    public static final int SLOT_ULT = 2;
    public static final int ULT_UNLOCK_LEVEL = 4;
    public static final int MAX_RANK_BASIC = 6;
    public static final int MAX_RANK_ULT = 3;
    private static final String[] NAMES = new String[]{"无情冲锋", "背水一战", "国士无双", "将进酒", "神来之笔", "青莲剑歌", "空明斩", "神速", "一决生死", "霸道之刃", "纵横天下", "浴血枭雄", "狂飙突进", "震慑打击", "飓风之锤", "凋零冰晶", "禁锢寒霜", "凛冬已至", "致命灵药", "善恶诊断", "生命主宰", "紧箍之咒", "九环之杖", "轮回之印", "翻滚突袭", "红莲爆弹", "究极弩炮", "多重箭矢", "落日余晖", "灼日之矢", "河豚手雷", "无敌鲨嘴炮", "空中支援", "静谧之眼", "狂风之息", "逃脱", "思无邪", "胡笳乐", "忘忧曲", "化蝶", "蝴蝶效应", "天人合一", "小霸王护盾", "机关魔爪", "暴走熊猫", "霸业之盾", "双重恐吓", "统御战场", "肉弹蹦床", "倒打一耙", "圈养时刻", "爆裂双斧", "激热回旋", "正义潜能", "无畏冲锋", "破釜沉舟", "霸王斩"};
    private static final String[] DESCS = new String[]{"两段冲锋，首段击飞并伤害沿途敌人", "向后跃出并强化下一次横扫", "霸体多段挥枪，末段击飞", "连续突进并眩晕路径敌人，可返回原处", "画出剑阵减速敌人并获得短暂无敌", "化身剑气多次斩击范围敌人", "斩出剑气减速敌人并抵挡飞行物", "向前突进，命中敌人获得护盾", "锁定目标跃斩击飞并限制回复", "连续三段位移，末段击飞", "挥出剑气伤害并减速", "解除控制，强化攻击并持续回复", "突进并强化重锤普攻", "蓄力砸地，中心区域伤害更高", "持续旋转大锤伤害并减速敌人", "召唤冰晶造成伤害与减速", "短暂延迟后冻结范围敌人", "暴风雪持续造成多段伤害和减速", "投掷毒药瓶，持续减速并叠加毒印", "药剂穿透敌人并治疗友军", "引爆全部毒印，同时伤害敌人治疗友军", "紧箍限制目标离开并减速", "法杖召唤九枚法器持续攻击", "开启法阵强化自身并保护友军", "翻滚并强化下一次炮击", "爆弹减速并削弱敌人防御", "发射超远弩炮造成范围爆发", "短时间强化普攻并分裂箭矢", "指定区域落下箭雨并减速", "射出火焰鸟，飞行越远眩晕越久", "投掷手雷造成伤害和减速并触发扫射", "发射鲨嘴炮贯穿目标", "召唤飞艇持续轰炸范围敌人", "放置视野装置并提升伏击能力", "蓄力发射超远狙击弹", "向后跃出并减速近身敌人", "持续治疗附近友军并提高移速", "音波在敌人间弹射并眩晕", "守护残血友军，持续治疗并提高防御", "放出蝴蝶伤害并减速", "蝴蝶环绕自身造成叠加伤害", "解除控制并为队友减伤加速", "获得护盾和加速，强化普攻击飞", "挥爪攻击并眩晕范围敌人", "驾驶机关持续旋转攻击", "展开可爆炸护盾", "蓄力冲锋并眩晕路径敌人", "传送支援友军并提供护盾治疗", "跳跃两次，落地击飞敌人并回复", "筑耙前冲，将敌人拉向障碍", "筑起高墙并困住范围敌人", "跃向目标并减速", "双斧回旋造成两段范围伤害", "持续回复大量生命并提升移速", "向前冲锋并击退敌人", "怒吼降低敌人伤害和移速", "蓄力挥出霸王斩并眩晕残血敌人"};
    private static final byte[] TYPES = new byte[]{2, 2, 1, 2, 1, 1, 3, 2, 2, 2, 3, 4, 2, 1, 1, 1, 1, 1, 1, 3, 5, 0, 1, 4, 2, 1, 3, 4, 1, 3, 1, 3, 1, 4, 3, 2, 5, 1, 5, 3, 1, 5, 4, 1, 1, 4, 1, 5, 2, 3, 1, 2, 1, 4, 2, 1, 3};
    private static final byte[] EFFECTS = new byte[]{2, 1, 34, 2, 1, 32, 1, 0, 3, 32, 1, 20, 1, 2, 33, 1, 2, 33, 1, 8, 40, 1, 32, 8, 16, 1, 0, 32, 1, 2, 1, 0, 32, 16, 0, 1, 24, 2, 8, 1, 33, 24, 16, 2, 32, 0, 2, 8, 6, 2, 2, 1, 32, 20, 2, 1, 2};
    private static final short[] COOLDOWNS = new short[]{45, 40, 150, 35, 38, 160, 40, 70, 150, 38, 75, 150, 40, 70, 150, 38, 65, 150, 40, 45, 150, 38, 68, 150, 35, 60, 150, 35, 70, 150, 45, 40, 150, 35, 75, 150, 35, 60, 150, 38, 65, 150, 40, 55, 150, 42, 65, 150, 42, 65, 150, 45, 60, 150, 42, 65, 150};
    private static final short[] MP_COSTS = new short[]{35, 30, 90, 30, 35, 95, 30, 40, 85, 30, 40, 85, 32, 42, 88, 32, 42, 85, 35, 35, 85, 32, 42, 88, 28, 35, 80, 30, 35, 80, 32, 32, 82, 30, 35, 80, 25, 45, 85, 30, 40, 82, 30, 38, 80, 32, 38, 85, 32, 38, 85, 30, 40, 80, 32, 38, 85};
    private static final short[] POWER = new short[]{160, 170, 260, 150, 170, 230, 180, 0, 230, 175, 0, 225, 190, 210, 235, 150, 160, 230, 160, 140, 240, 165, 170, 245, 150, 170, 220, 170, 0, 250, 140, 170, 225, 165, 0, 270, 130, 95, 160, 140, 160, 90, 80, 0, 70, 140, 0, 200, 130, 0, 195, 120, 0, 190, 135, 200, 245};
    private static final short[] RADII = new short[]{70, 100, 140, 64, 80, 100, 60, 0, 70, 64, 0, 75, 0, 66, 82, 96, 60, 130, 40, 100, 70, 0, 62, 78, 56, 58, 74, 110, 0, 150, 54, 100, 80, 104, 0, 0, 0, 60, 90, 0, 58, 70, 60, 0, 80, 66, 0, 76, 56, 0, 78, 50, 0, 60, 58, 64, 80};
    private static final short[] RANGES = new short[]{90, 0, 160, 90, 100, 0, 85, 0, 0, 88, 0, 0, 76, 0, 0, 0, 80, 0, 80, 0, 80, 78, 0, 0, 90, 0, 86, 0, 0, 0, 80, 0, 90, 0, 0, 170, 80, 0, 0, 80, 0, 0, 90, 0, 0, 86, 0, 0, 80, 0, 0, 62, 0, 0, 82, 0, 0};
    private static final short[] DURATIONS = new short[]{0, 0, 0, 0, 0, 0, 0, 60, 0, 0, 50, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 45, 0, 0, 0, 0, 0, 50, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 40, 30, 0, 0, 30, 0, 60, 30, 0, 0, 0};
    private static final byte[] SHIELD_PCT = new byte[]{0, 0, 0, 0, 0, 0, 0, 16, 0, 0, 22, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 12, 18, 0, 0, 24, 10, 28, 26, 0, 0, 0, 0, 32, 0, 0, 30, 0, 16, 0, 20};
    private static final int[] COLORS = new int[]{11567359, 13672703, 10177535, 14721279, 15778047, 12872703, 16747068, 16756832, 16738858, 14710844, 15767640, 13129784, 12079208, 13660296, 15239328, 7330047, 10283263, 12120319, 16742972, 7330047, 16734762, 9240554, 7334104, 4249792, 16765770, 16769146, 16761930, 16769146, 10289072, 16761930, 10289072, 7332095, 8964351, 13160668, 10531008, 16769146, 12577023, 9240520, 15399935, 10551232, 7397536, 12648416, 10289120, 7395520, 11599848, 11028592, 13133968, 15239344, 13662280, 15243368, 15775880, 11584716, 13624554, 12618314, 5269664, 7375040, 8954080};

    private SkillDef() {
    }

    public static String name(int var0) {
        return NAMES[var0];
    }

    public static String desc(int var0) {
        return DESCS[var0];
    }

    public static int type(int var0) {
        return TYPES[var0];
    }

    public static int cooldown(int var0) {
        return cooldown(var0, 1);
    }

    public static int cooldown(int var0, int var1) {
        short var2 = COOLDOWNS[var0];
        if (var1 <= 1) {
            return var2;
        } else {
            int var3 = var1 > 6 ? 6 : var1;
            int var4 = var2 * (100 - (var3 - 1) * 6) / 100;
            return var4 < 12 ? 12 : var4;
        }
    }

    public static int mpCost(int var0) {
        return mpCost(var0, 1);
    }

    public static int mpCost(int var0, int var1) {
        short var2 = MP_COSTS[var0];
        if (var1 <= 1) {
            return var2;
        } else {
            int var3 = var1 > 6 ? 6 : var1;
            return var2 + (var3 - 1) * 4;
        }
    }

    public static int power(int var0) {
        return power(var0, 1);
    }

    public static int power(int var0, int var1) {
        short var2 = POWER[var0];
        if (var2 > 0 && var1 > 1) {
            int var3 = var1 > 6 ? 6 : var1;
            return var2 + var2 * (var3 - 1) * 12 / 100;
        } else {
            return var2;
        }
    }

    public static int maxRank(int var0) {
        return var0 == 2 ? 3 : 6;
    }

    public static int ultCapAtLevel(int var0) {
        if (var0 < 4) {
            return 0;
        } else if (var0 < 8) {
            return 1;
        } else {
            return var0 < 12 ? 2 : 3;
        }
    }

    public static int radius(int var0) {
        return RADII[var0];
    }

    public static int range(int var0) {
        return RANGES[var0];
    }

    public static int duration(int var0) {
        return DURATIONS[var0];
    }

    public static int shieldPct(int var0) {
        return SHIELD_PCT[var0];
    }

    public static int effect(int var0) {
        return EFFECTS[var0] & 255;
    }

    public static int color(int var0) {
        return COLORS[var0];
    }

    public static int unlockLevel(int var0) {
        return var0 == 2 ? 4 : 1;
    }
}
