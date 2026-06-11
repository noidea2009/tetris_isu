import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Main {

    private static final String BG_FILE = "tetris miku.jpg";

    private static JFrame          frame;
    private static CardLayout      cardLayout;
    private static BackgroundPanel root;

    private static Game   game;
    private static JPanel gameWrapper;

    public static void main(String[] args) {
        frame      = new JFrame("Tetris");
        cardLayout = new CardLayout();
        root       = new BackgroundPanel(BG_FILE, cardLayout);

        root.add(buildMenu(), "MENU");
        root.add(new Options(() -> showCard("MENU")), "OPTIONS");

        frame.add(root);
        frame.setSize(600, 700);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        cardLayout.show(root, "MENU");
    }

    // subclass here becuase it is eaiser to manage
    // Child panels must call setOpaque(false) to let the image show through.
    static class BackgroundPanel extends JPanel {
        private final BufferedImage bg;

        BackgroundPanel(String filename, LayoutManager layout) {
            super(layout);
            setBackground(Color.BLACK); // fallback if image missing or window larger than image
            BufferedImage loaded = null;
            try {
                loaded = ImageIO.read(new File(filename));
            } catch (IOException e) {
                System.err.println("[BackgroundPanel] Could not load '" + filename + "': " + e.getMessage());
            }
            bg = loaded;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g); // fills with background color first
            if (bg == null) return;
            // Center image at its native size — no resize, no distortion
            int x = (getWidth()  - bg.getWidth())  / 2;
            int y = (getHeight() - bg.getHeight()) / 2;
            g.drawImage(bg, x, y, null);
        }
    }


    private static JPanel buildMenu() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false); // lets BackgroundPanel show through
        // panel.setBackground(Color.BLACK); // comment out

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(14, 0, 14, 0);
        gbc.gridx  = 0;
        gbc.fill   = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("TETRIS", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 52));
        title.setForeground(new Color(0, 240, 240));
        gbc.gridy = 0;
        panel.add(title, gbc);

        JButton play = menuButton("PLAY");
        play.addActionListener(e -> startGame());
        gbc.gridy = 1;
        panel.add(play, gbc);

        JButton options = menuButton("OPTIONS");
        options.addActionListener(e -> showCard("OPTIONS"));
        gbc.gridy = 2;
        panel.add(options, gbc);

        JButton quit = menuButton("QUIT");
        quit.addActionListener(e -> System.exit(0));
        gbc.gridy = 3;
        panel.add(quit, gbc);

        return panel;
    }

    private static void startGame() {
        if (game != null) game.stop();
        if (gameWrapper != null) {
            root.remove(gameWrapper);
            gameWrapper = null;
        }

        game = new Game();
        GamePanel      gamePanel  = new GamePanel(game);
        HoldPanel      holdPanel  = new HoldPanel(game);
        NextQueuePanel queuePanel = new NextQueuePanel(game);

        game.setHoldPanel(holdPanel);
        game.setQueuePanel(queuePanel);

        gameWrapper = new JPanel(new GridBagLayout());
        gameWrapper.setOpaque(false); // lets BackgroundPanel show through

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy  = 0;
        gbc.anchor = GridBagConstraints.NORTH;

        gbc.gridx  = 0; gbc.insets = new Insets(0, 0, 0, 5);
        gameWrapper.add(holdPanel, gbc);
        gbc.gridx  = 1; gbc.insets = new Insets(0, 0, 0, 0);
        gameWrapper.add(gamePanel, gbc);
        gbc.gridx  = 2; gbc.insets = new Insets(0, 10, 0, 0);
        gameWrapper.add(queuePanel, gbc);

        root.add(gameWrapper, "GAME");
        root.revalidate();

        showCard("GAME");
        gamePanel.requestFocusInWindow();

        game.start(gamePanel, () -> SwingUtilities.invokeLater(() -> showCard("MENU")));
    }

    private static void showCard(String name) {
        if ("MENU".equals(name) && game != null) game.stop();
        cardLayout.show(root, name);
        root.repaint();
    }

    private static JButton menuButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 20));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(40, 40, 40));
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(200, 50));
        return btn;
    }
}