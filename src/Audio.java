import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;


// Made with help from AI

public final class Audio {
    private static Clip musicClip;

    private Audio() {}

    private static Clip loadClip(String absoluteResourcePath) {
        try (InputStream raw = Audio.class.getResourceAsStream(absoluteResourcePath)) {
            if (raw == null) throw new IllegalArgumentException("Missing resource: " + absoluteResourcePath);

            try (BufferedInputStream bis = new BufferedInputStream(raw);
                 AudioInputStream originalAis = AudioSystem.getAudioInputStream(bis)) {

                AudioFormat baseFormat = originalAis.getFormat();

                AudioFormat decodedFormat = new AudioFormat(
                        AudioFormat.Encoding.PCM_SIGNED,
                        baseFormat.getSampleRate(),
                        16,
                        baseFormat.getChannels(),
                        baseFormat.getChannels() * 2,
                        baseFormat.getSampleRate(),
                        false
                );

                AudioInputStream playableAis = AudioSystem.getAudioInputStream(decodedFormat, originalAis);

                Clip clip = AudioSystem.getClip();
                clip.open(playableAis);
                return clip;
            }
        } catch (UnsupportedAudioFileException e) {
            throw new IllegalArgumentException("Unsupported audio file: " + absoluteResourcePath, e);
        } catch (LineUnavailableException | IOException e) {
            throw new RuntimeException("Failed to load audio: " + absoluteResourcePath, e);
        }
    }

    private static Clip loadClipFile(String fullPath) {
        try (AudioInputStream ais = AudioSystem.getAudioInputStream(new File(fullPath))) {
            Clip clip = AudioSystem.getClip();
            clip.open(ais);
            return clip;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load audio file: " + fullPath, e);
        }
    }

    private static String wavName(String fileName) {
        return fileName.endsWith(".wav") ? fileName : (fileName + ".wav");
    }

    // -------- Music --------
    public static void playMusicLoop(String fileName, float volumeDb) {
        stopMusic();

        String f = wavName(fileName);

        // Build: src/resources/audio/music/<name>.wav
        Path p = Paths.get( "src",  "resources", "audio", "music", f);
        String fullPath = p.toString();

        System.out.println("Loading music from: " + fullPath);

        musicClip = loadClipFile(fullPath);
        setVolume(musicClip, volumeDb);
        musicClip.loop(Clip.LOOP_CONTINUOUSLY);
        musicClip.start();
    }


    public static void stopMusic() {
        if (musicClip != null) {
            musicClip.stop();
            musicClip.close();
            musicClip = null;
        }
    }

    public static void setMusicVolume(float volumeDb) {
        if (musicClip != null) setVolume(musicClip, volumeDb);
    }

    // -------- SFX --------
    public static void playSfx(String fileName, float volumeDb) {
        String path = "src/resources/audio/sfx/" + fileName;

        final Clip sfxClip = loadClip(path);
        setVolume(sfxClip, volumeDb);

        sfxClip.addLineListener(ev -> {
            if (ev.getType() == LineEvent.Type.STOP) {
                ev.getLine().close();
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
