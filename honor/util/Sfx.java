package honor.util;

import java.io.BufferedInputStream;
import java.io.InputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.Clip;

public final class Sfx {
    public static boolean enabled = true;
    private static Clip clip;
    private static boolean playingVoice;

    private Sfx() {
    }

    public static void play(String var0) {
        if (enabled && var0 != null) {
            boolean var1 = isVoiceClip(var0);
            if (var1 || !playingVoice || !isStillPlaying()) {
                InputStream var2 = null;

                try {
                    stop();
                    var2 = Res.open("/res/snd/" + var0 + ".wav");
                    if (var2 != null) {
                        AudioInputStream ais = AudioSystem.getAudioInputStream(
                            new BufferedInputStream(var2));
                        clip = AudioSystem.getClip();
                        clip.open(ais);
                        clip.start();
                        playingVoice = var1;
                        return;
                    }
                } catch (Throwable var13) {
                    enabled = false;
                    Log.warn("Sfx.play(" + var0 + "): " + var13.getMessage());
                    closeQuietly(clip);
                    clip = null;
                    playingVoice = false;
                    return;
                } finally {
                    if (clip == null && var2 != null) {
                        try {
                            var2.close();
                        } catch (Exception var12) {
                        }
                    }

                }

            }
        }
    }

    private static boolean isVoiceClip(String var0) {
        if (var0.startsWith("hero_")) {
            return true;
        } else {
            return var0.equals("kill") || var0.equals("slay") || var0.equals("double") || var0.equals("triple") || var0.equals("quad") || var0.equals("penta") || var0.equals("legend") || var0.equals("win") || var0.equals("defeat") || var0.equals("enemy5") || var0.equals("crystal") || var0.equals("tower") || var0.equals("tower_ally") || var0.equals("tower_enemy") || var0.equals("tyrant") || var0.equals("overlord") || var0.equals("recall");
        }
    }

    private static boolean isStillPlaying() {
        if (clip == null) {
            return false;
        } else {
            try {
                return clip.isRunning();
            } catch (Exception var1) {
                return false;
            }
        }
    }

    public static void stop() {
        if (clip == null) {
            playingVoice = false;
        } else {
            try {
                clip.stop();
                clip.flush();
            } catch (Exception var1) {
            }

            closeQuietly(clip);
            clip = null;
            playingVoice = false;
        }
    }

    private static void closeQuietly(Clip var0) {
        if (var0 != null) {
            try {
                var0.close();
            } catch (Exception var2) {
            }

        }
    }
}
