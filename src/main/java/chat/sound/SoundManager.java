package chat.sound;

import javax.sound.sampled.*;

public class SoundManager {
    public static void play(String name) {
        new Thread(() -> {
            try {
                var url = SoundManager.class.getResource("/sounds/" + name + ".wav");
                if (url == null) return;
                var ais = AudioSystem.getAudioInputStream(url);
                var clip = AudioSystem.getClip();
                clip.open(ais);
                clip.start();
            } catch (Exception ignored) {}
        }).start();
    }
    public static void playJoin()    { play("join"); }
    public static void playLeave()   { play("leave"); }
    public static void playReceive() { play("receive"); }
    public static void playSend()    { play("send"); }
    public static void playError()   { play("error"); }
}
