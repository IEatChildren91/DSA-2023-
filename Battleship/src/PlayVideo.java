import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javax.swing.*;
import java.io.File;
/**
 * DSA Project - Battleship Game - Panadol Extra.
 * Class: PlayVideo.
 * The PlayVideo class facilitates playing video files using JavaFX in a Swing environment.
 */
public class PlayVideo {
    /**
     * Plays the specified video file using a JavaFX media player embedded within a Swing JFrame.
     * @param videoFileName The file path of the video to be played.
     */
    public static void playVideo(String videoFileName) {
        JFXPanel jfxPanel = new JFXPanel();
        JFrame videoFrame = new JFrame();

        Platform.runLater(() -> {
            try {
                File videoFile = new File(videoFileName);
                MediaPlayer mediaPlayer = new MediaPlayer(new Media(videoFile.toURI().toString()));
                mediaPlayer.setAutoPlay(true);

                MediaView mediaView = new MediaView(mediaPlayer);
                StackPane root = new StackPane(mediaView);
                Scene scene = new Scene(root);
                jfxPanel.setScene(scene);

                mediaPlayer.setOnEndOfMedia(() -> {

                    javax.swing.SwingUtilities.invokeLater(videoFrame::dispose);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        javax.swing.SwingUtilities.invokeLater(() -> {
            videoFrame.add(jfxPanel);
            videoFrame.setSize(550, 450);
            videoFrame.setLocationRelativeTo(null);
            videoFrame.setVisible(true);
            videoFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        });
    }
}
