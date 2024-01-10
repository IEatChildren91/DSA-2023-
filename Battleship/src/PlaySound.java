import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
/**
 * DSA Project - Battleship Game - Panadol Extra.
 * Class: PlaySound.
 * The PlaySound class provides a utility method to play audio files using Java's Sound API.
 */
public class PlaySound {
    /**
     * Plays the audio file specified by the provided file path.
     * @param soundFileName The file path of the sound file to be played.
     */
    public static void playSound(String soundFileName) {
        try {
            // Load the sound file into an AudioInputStream.
            File soundFile = new File(soundFileName);
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);

            // Retrieve the format of the loaded audio file.
            AudioFormat baseFormat = audioIn.getFormat();

            // Define a new audio format with a lower bit depth (16-bit).
            // This is done to ensure compatibility with a wider range of audio systems.
            AudioFormat decodedFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED, // Encoding type
                    baseFormat.getSampleRate(),      // Preserve original sample rate
                    16,                              // Change bit depth to 16-bit
                    baseFormat.getChannels(),        // Preserve number of audio channels
                    baseFormat.getChannels() * 2,    // Calculate frame size (16-bit stereo = 4 bytes/frame)
                    baseFormat.getSampleRate(),      // Preserve sample rate for frame rate
                    false                            // Use little-endian byte order
            );

            // Convert the original audio input stream to the new format.
            AudioInputStream decodedAudioIn = AudioSystem.getAudioInputStream(decodedFormat, audioIn);

            // Prepare an audio clip for playback.
            Clip clip = AudioSystem.getClip();
            clip.open(decodedAudioIn);
            clip.start(); // Start playing the audio.
        } catch (UnsupportedAudioFileException | IOException e) {
            // Handle exceptions related to file format and IO issues.
            e.printStackTrace();
        } catch (LineUnavailableException e) {
            // Handle exception where the audio line is not available for playback.
            System.err.println("Audio line for playback is not available.");
            e.printStackTrace();
        }
    }
}
