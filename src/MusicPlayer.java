import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import javax.sound.sampled.*;
//made by Junjit Chang
// plays the background music
// https://www.geeksforgeeks.org/java/play-audio-file-using-java/
public class MusicPlayer {

    private int songnum = 0;
    private Clip currentClip;
    private List<String> playlist;

    public MusicPlayer() {
        // Initialize the playlist immediately when the object is created
        this.playlist = getList();
    }


    public void start() {
        if (playlist != null && !playlist.isEmpty()) {
            playPlaylist(playlist);
        } else {
            System.err.println("No .wav files found in /songs folder.");
        }
    }

    public List<String> getList() {
        Path folderPath = Paths.get("songs");
        if (!Files.exists(folderPath)) return List.of();

        try (var stream = Files.list(folderPath)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().toLowerCase().endsWith(".wav"))
                    .map(path -> path.getFileName().toString())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return List.of();
        }
    }

    public synchronized void playPlaylist(List<String> songs) {
        if (songs == null || songs.isEmpty()) return;

        stopAndCloseCurrentClip();

        String fileName = songs.get(songnum);
        File musicFile = new File("songs/" + fileName);

        // We don't use try-with-resources on AudioInputStream here because
        // the Clip needs the stream to remain open until it's done playing.
        try {
            //Get the stream
            AudioInputStream audioInput = AudioSystem.getAudioInputStream(musicFile);

            // Initialize the clip FIRST
            currentClip = AudioSystem.getClip();

            // Open the clip with the stream
            currentClip.open(audioInput);

            // Apply volume (Now that the clip is open, controls are available)
            updateVolume(Options.getVolume());

            // Setup listener and start
            currentClip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    if (currentClip.getMicrosecondPosition() >= currentClip.getMicrosecondLength()) {
                        songnum = (songnum + 1) % songs.size();
                        playPlaylist(songs);
                    }
                }
            });

            currentClip.start();
            System.out.println("Started: " + fileName);

        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("Resource Error: " + e.getMessage());
        }
    }

    public void stopAndCloseCurrentClip() {
        if (currentClip != null) {
            currentClip.stop();
            currentClip.flush();
            currentClip.close();
        }
    }

    // method to skip to the next song manually
    public void next() {
        if (playlist != null && !playlist.isEmpty()) {
            songnum = (songnum + 1) % playlist.size();
            playPlaylist(playlist);
        }
    }
    public void updateVolume(int volumePercent) {
        if (currentClip != null && currentClip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl gainControl = (FloatControl) currentClip.getControl(FloatControl.Type.MASTER_GAIN);

            // Convert 0-100 to Decibels (-80.0 to 6.0206)
            // Note: 0% volume is usually silent, -80dB is the standard floor for Java Clips
            float min = gainControl.getMinimum();
            float max = gainControl.getMaximum();
            float volume = min + (max - min) * (volumePercent / 100.0f);

            gainControl.setValue(volume);
        }
    }
}