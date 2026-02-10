import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class Audio {

    private static Clip musicClip;
    private static boolean isMuted = false;

    private Audio() {}

    public static void setMuted(boolean muted) {
        isMuted = muted;
        if (isMuted) stopMusic();
    }

    public static boolean isMuted() {
        return isMuted;
    }

    private static Clip loadClipFile(String fullPath) {
        try (AudioInputStream ais = AudioSystem.getAudioInputStream(new File(fullPath))) {
            Clip clip = AudioSystem.getClip();
            clip.open(ais);
            return clip;
        } catch (UnsupportedAudioFileException e) {
            throw new IllegalArgumentException("Unsupported audio file: " + fullPath, e);
        } catch (LineUnavailableException | IOException e) {
            throw new RuntimeException("Failed to load audio file: " + fullPath, e);
        }
    }

    private static String wavName(String fileName) {
        return fileName.endsWith(".wav") ? fileName : (fileName + ".wav");
    }

    // -------- Music --------
    public static void playMusicLoop(String fileName, float volumeDb) {
        if (isMuted) return;

        stopMusic();

        String f = wavName(fileName);
        Path p = Paths.get("src", "resources", "audio", "music", f);

        try {
            musicClip = loadClipFile(p.toString());
        } catch (Exception e) {
            musicClip = null;
            System.out.println("Failed to load music: " + f);
            return;
        }

        setVolume(musicClip, volumeDb);
        musicClip.loop(Clip.LOOP_CONTINUOUSLY);
        musicClip.start();
    }

    public static void stopMusic() {
        if (musicClip != null) {
            try { musicClip.stop(); } catch (Exception ignored) {}
            try { musicClip.close(); } catch (Exception ignored) {}
            musicClip = null;
        }
    }

    public static void setMusicVolume(float volumeDb) {
        if (musicClip != null) setVolume(musicClip, volumeDb);
    }

    // -------- SFX --------
    public static void playSfx(String fileName, float volumeDb) {
        if (isMuted) return;

        String f = wavName(fileName);
        Path p = Paths.get("src", "resources", "audio", "sfx", f);

        final Clip sfxClip;
        try {
            sfxClip = loadClipFile(p.toString());
        } catch (Exception e) {
            System.out.println("Failed to load sfx: " + f);
            return;
        }

        setVolume(sfxClip, volumeDb);

        sfxClip.addLineListener(ev -> {
            if (ev.getType() == LineEvent.Type.STOP) {
                try { ev.getLine().close(); } catch (Exception ignored) {}
            }
        });

        sfxClip.start();
    
    }

    

    // -------- Volume --------
    private static void setVolume(Clip clip, float volumeDb) {
        try {
            FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float clamped = Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), volumeDb));
            gain.setValue(clamped);
        } catch (IllegalArgumentException ignored) {
        }
    }
}
