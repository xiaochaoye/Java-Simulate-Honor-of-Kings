# 王者峡谷（HONOR CANYON）

一个用 Java Swing 复刻《王者荣耀》核心玩法的 2D MOBA 游戏。项目最初是 J2ME（Java ME MIDlet）实现，现已移植为纯 Java SE 桌面应用，无任何第三方依赖。

这是从[小众网码农掌叔](https://iniche.cn/search?q=%E7%8E%8B%E8%80%85%E8%8D%A3%E8%80%80)那拿到的jar包解包并改造的，所以虽然能玩，但是很多变量意义不明，所幸不影响改造。**联机对战仍由[J2ME 在线模拟器](https://java.zixing.fun/java/)支持，也请多多[支持站长](https://java.zixing.fun/java/about.html)** 


## 玩法

- **三种对战模式**：1v1 单挑 / 3v3 对战 / 5v5 对战（在主菜单「对战模式」切换）
- **19 名英雄**，覆盖 6 大职业：刺客、战士、法师、射手、辅助、坦克
- 每条路有兵线、防御塔、野怪，**推掉敌方水晶**即获胜
- **单机对战**（人机）与**联机对战**（真实玩家匹配）

## 操作

**键盘**（可用数字小键盘或 WASD）：

| 按键 | 作用 |
| --- | --- |
| 2 / 4 / 6 / 8 或 WASD | 移动 |
| 1、3 | 技能 |
| 7 | 大招（Lv4 解锁） |
| 9 | 回城 |
| 0 | 商店 |
| 5 | 确认 |
| ESC | 返回 / 退出 |

**触摸**（主菜单「操控方式」切换）：左半屏滑动移动，右侧为攻击 / 技能 / 回城 / 商店按钮。（不建议用这个）

## 启动

要求 JDK 8 及以上（建议 JDK 11+），纯标准库，无需额外依赖。

**方式一：IDE（推荐）**
打开项目，直接运行 `honor/Main.java` 的 `main` 方法。

**方式二：命令行**
在项目根目录执行（资源通过 classpath 的 `/res` 加载，运行目录必须是项目根目录）：

```bash
# 编译
javac -encoding UTF-8 -d out $(find honor -name '*.java')

# 运行
java -cp 'out;.' honor.Main
```

> Windows 下若用 CMD 而非 Git Bash，请将 `$(find ...)` 换成显式文件列表，并将 classpath 分隔符保持为 `;`。

## 注意事项

- 资源目录 `res/` 必须位于 classpath 根目录，否则加载失败。
- 联机对战需连接服务器（见 `honor/net/ServerConfig.java`），首次进入需注册 / 登录账号：账号仅限**英文 + 数字**，密码**最大长度 20**。
- 单机战绩（胜/负、K/D）保存在本地。

## 项目结构

```
honor/
  Main.java        入口（Swing 窗口 + 游戏循环线程）
  GamePanel.java   画布适配层（双缓冲、输入转发）
  G2D.java         Graphics 适配层（字体 / 颜色缓存、锚点绘制）
  core/            游戏引擎、输入、匹配配置
  battle/          战斗世界、伤害、商店
  entity/          英雄、小兵、防御塔、水晶、野怪
  hero/ skill/     英雄与技能数据
  map/             地图加载与渲染
  net/             联机协议与客户端
  ui/              主菜单、选人、HUD 等界面
res/               图片、地图、音效、数据
```
