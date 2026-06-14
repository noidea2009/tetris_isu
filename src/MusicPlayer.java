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
            AudioInputStream audioInput = AudioSystem.getAudioInputStream(musicFile);
            currentClip = AudioSystem.getClip();
            currentClip.open(audioInput);

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

    // Optional: add a method to skip to the next song manually
    public void next() {
        if (playlist != null && !playlist.isEmpty()) {
            songnum = (songnum + 1) % playlist.size();
            playPlaylist(playlist);
        }
    }
}