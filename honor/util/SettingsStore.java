package honor.util;

import java.util.prefs.Preferences;

public final class SettingsStore {
    private static final String RS_NAME = "j2mehonor_cfg";
    private static final int VERSION = 2;
    public static final int CTRL_KEYS = 0;
    public static final int CTRL_TOUCH = 1;
    private int controlMode = 0;
    private boolean autoAttack = true;
    private boolean rememberAuth;
    private String account = "";
    private String password = "";
    private boolean hasStoredSettings;
    private boolean deviceDefaultApplied;
    private static SettingsStore instance;

    private SettingsStore() {
    }

    public static SettingsStore get() {
        if (instance == null) {
            instance = new SettingsStore();
            instance.load();
        }

        return instance;
    }

    public int getControlMode() {
        return this.controlMode;
    }

    public boolean isTouchMode() {
        return this.controlMode == 1;
    }

    public void configureForDevice(int var1, int var2, boolean var3) {
        if (!this.deviceDefaultApplied && var1 > 0 && var2 > 0) {
            this.deviceDefaultApplied = true;
            if (this.hasStoredSettings) {
                if (!var3 && this.controlMode == 1) {
                    this.controlMode = 0;
                    this.save();
                }

            } else {
                this.controlMode = defaultControlMode(var1, var2, var3);
                this.save();
            }
        }
    }

    public static int defaultControlMode(int var0, int var1, boolean var2) {
        if (!var2) {
            return 0;
        } else {
            int var3 = var0 < var1 ? var0 : var1;
            int var4 = var0 > var1 ? var0 : var1;
            return var3 >= 360 && var4 >= 640 ? 1 : 0;
        }
    }

    public void setControlMode(int var1) {
        if (var1 != 0 && var1 != 1) {
            var1 = 0;
        }

        if (this.controlMode != var1 || !this.hasStoredSettings) {
            this.controlMode = var1;
            this.save();
        }
    }

    public void toggleControlMode() {
        this.setControlMode(this.isTouchMode() ? 0 : 1);
    }

    public boolean isAutoAttack() {
        return this.autoAttack;
    }

    public void setAutoAttack(boolean var1) {
        if (this.autoAttack != var1) {
            this.autoAttack = var1;
            this.save();
        }
    }

    public void toggleAutoAttack() {
        this.setAutoAttack(!this.autoAttack);
    }

    public boolean isRememberAuth() {
        return this.rememberAuth;
    }

    public String getAccount() {
        return this.account == null ? "" : this.account;
    }

    public String getPassword() {
        return this.password == null ? "" : this.password;
    }

    public void setAuth(boolean var1, String var2, String var3) {
        this.rememberAuth = var1;
        if (var1) {
            this.account = var2 == null ? "" : var2;
            this.password = var3 == null ? "" : var3;
        } else {
            this.account = "";
            this.password = "";
        }

        this.save();
    }

    private void load() {
        try {
            Preferences prefs = Preferences.userNodeForPackage(SettingsStore.class);
            String val = prefs.get("controlMode", null);
            if (val == null) {
                return;
            }
            this.controlMode = prefs.getInt("controlMode", 0);
            this.autoAttack = prefs.getBoolean("autoAttack", true);
            this.rememberAuth = prefs.getBoolean("rememberAuth", false);
            this.account = prefs.get("account", "");
            this.password = prefs.get("password", "");
            if (!this.rememberAuth) {
                this.account = "";
                this.password = "";
            }
            if (this.controlMode != 0 && this.controlMode != 1) {
                this.controlMode = 0;
                return;
            }
            this.hasStoredSettings = true;
        } catch (Exception var8) {
            Log.warn("SettingsStore.load: " + var8.getMessage());
        }
    }

    private void save() {
        try {
            Preferences prefs = Preferences.userNodeForPackage(SettingsStore.class);
            prefs.putInt("controlMode", this.controlMode);
            prefs.putBoolean("autoAttack", this.autoAttack);
            prefs.putBoolean("rememberAuth", this.rememberAuth);
            prefs.put("account", this.account == null ? "" : this.account);
            prefs.put("password", this.password == null ? "" : this.password);
            prefs.flush();
            this.hasStoredSettings = true;
        } catch (Exception var8) {
            Log.warn("SettingsStore.save: " + var8.getMessage());
        }
    }
}
