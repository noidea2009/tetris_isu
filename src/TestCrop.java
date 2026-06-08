import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;

public class TestCrop {
    public static void main(String[] args) throws IOException {
        // 1. Load the source image
        BufferedImage source = ImageIO.read(new File("tetris__TEMPLATE.png"));

        // 2. Define the crop region (x, y, width, height)
        int x = 1;
        int y = 1;
        int width = 92;
        int height = 92;

        // 3. Crop the image
        BufferedImage cropped = source.getSubimage(x, y, width, height);

        // 4. Display the cropped image
        JFrame frame = new JFrame("Cropped Image");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new JLabel(new ImageIcon(cropped)));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}   