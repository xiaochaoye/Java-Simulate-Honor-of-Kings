package honor.util;

import java.util.prefs.Preferences;

public final class StatsStore {
    private static final String RS_NAME = "j2mehonor_stats";
    private static final int VERSION = 1;
    private int wins;
    private int losses;
    private int kills;
    private int deaths;
    private int minions;
    private static StatsStore instance;

    private StatsStore() {
    }

    public static StatsStore get() {
        if (instance == null) {
            instance = new StatsStore();
            instance.load();
        }

        return instance;
    }

    public int getWins() {
        return this.wins;
    }

    public int getLosses() {
        return this.losses;
    }

    public int getKills() {
        return this.kills;
    }

    public int getDeaths() {
        return this.deaths;
    }

    public int getMinions() {
        return this.minions;
    }

    public int getGames() {
        return this.wins + this.losses;
    }

    public void recordMatch(boolean var1, int var2, int var3, int var4) {
        if (var1) {
            ++this.wins;
        } else {
            ++this.losses;
        }

        if (var2 > 0) {
            this.kills += var2;
        }

        if (var3 > 0) {
            this.deaths += var3;
        }

        if (var4 > 0) {
            this.minions += var4;
        }

        this.save();
    }

    private void load() {
        try {
            Preferences prefs = Preferences.userNodeForPackage(StatsStore.class);
            String val = prefs.get("wins", null);
            if (val == null) {
                return;
            }
            this.wins = prefs.getInt("wins", 0);
            this.losses = prefs.getInt("losses", 0);
            this.kills = prefs.getInt("kills", 0);
            this.deaths = prefs.getInt("deaths", 0);
            this.minions = prefs.getInt("minions", 0);
        } catch (Exception var8) {
            Log.warn("StatsStore.load: " + var8.getMessage());
        }
    }

    private void save() {
        try {
            Preferences prefs = Preferences.userNodeForPackage(StatsStore.class);
            prefs.putInt("wins", this.wins);
            prefs.putInt("losses", this.losses);
            prefs.putInt("kills", this.kills);
            prefs.putInt("deaths", this.deaths);
            prefs.putInt("minions", this.minions);
            prefs.flush();
        } catch (Exception var8) {
            Log.warn("StatsStore.save: " + var8.getMessage());
        }
    }
}
