import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import javax.sound.sampled.*;
// plays the background music
//made by Junjit Chang
// https://www.geeksforgeeks.org/java/play-audio-file-using-java/
/**
 * Manages background music playback, including playlist discovery,
 * sequential looping, and volume control using the Java Sound API.
 */
public class MusicPlayer {

    private int songnum = 0;
    private Clip currentClip;
    private List<String> playlist;

    /**
     * Initializes the MusicPlayer and automatically populates the playlist
     * from the "songs" directory.
     */
    public MusicPlayer() {
        this.playlist = getList();
    }

    /**
     * Begins playback of the current playlist.
     */
    public void start() {
        if (playlist != null && !playlist.isEmpty()) {
            playPlaylist(playlist);
        } else {
            System.err.println("No .wav files found in /songs folder.");
        }
    }

    /**
     * Scans the local "songs" directory for .wav files.
     * @return A List of filenames found in the directory.
     */
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

    /**
     * Plays the specified list of songs, handling playback state and recursion.
     * @param songs The list of filenames to be played.
     */
    public synchronized void playPlaylist(List<String> songs) {
        if (songs == null || songs.isEmpty()) return;

        stopAndCloseCurrentClip();

        String fileName = songs.get(songnum);
        File musicFile = new File("songs/" + fileName);

        try {
            AudioInputStream audioInput = AudioSystem.getAudioInputStream(musicFile);
            currentClip = AudioSystem.getClip();
            currentClip.open(audioInput);

            updateVolume(Options.getVolume());

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

    /**
     * Safely halts and releases resources for the currently active clip.
     */
    public void stopAndCloseCurrentClip() {
        if (currentClip != null) {
            currentClip.stop();
            currentClip.flush();
            currentClip.close();
        }
    }

    /**
     * Manually advances playback to the next song in the playlist.
     */
    public void next() {
        if (playlist != null && !playlist.isEmpty()) {
            songnum = (songnum + 1) % playlist.size();
            playPlaylist(playlist);
        }
    }

    /**
     * Adjusts the gain of the current clip.
     * @param volumePercent Integer from 0 to 100 representing desired volume.
     */
    public void updateVolume(int volumePercent) {
        if (currentClip != null && currentClip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl gainControl = (FloatControl) currentClip.getControl(FloatControl.Type.MASTER_GAIN);
            float min = gainControl.getMinimum();
            float max = gainControl.getMaximum();
            float volume = min + (max - min) * (volumePercent / 100.0f);
            gainControl.setValue(volume);
        }
    }
}