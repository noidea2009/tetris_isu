import javax.swing.*;
import java.awt.*;

public class Main {

    private static JFrame     frame;
    private static CardLayout cardLayout;
    private static JPanel     root;

    // Game state — recreated each Play press
    private static Game    game;
    private static JPanel  gameWrapper; // tracked so we can remove it before re-adding

    public static void main(String[] args) {
        frame     = new JFrame("Tetris");
        cardLayout = new CardLayout();
        root       = new JPanel(cardLayout);
        root.setBackground(Color.BLACK);

        root.add(buildMenu(), "MENU");
        root.add(new Options(() -> showCard("MENU")), "OPTIONS");
        // "GAME" card is built dynamically in startGame()

        frame.add(root);
        frame.setSize(600, 700);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        cardLayout.show(root, "MENU");
    }

    // ── Menu ──────────────────────────────────────────────────────────────────

    private static JPanel buildMenu() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.BLACK);

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
        // Stop previous session cleanly
        if (game != null) game.stop();

        // Remove stale GAME card to avoid duplicates in CardLayout
        // this happened last week so I needed to add this
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
        gameWrapper.setBackground(Color.BLACK);

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

        // ESC in-game calls this lambda → returns to menu on the EDT
        game.start(gamePanel, () -> SwingUtilities.invokeLater(() -> showCard("MENU")));
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    private static void showCard(String name) {
        if ("MENU".equals(name) && game != null) game.stop();
        cardLayout.show(root, name);
        root.repaint();
    }

    // ── Shared style ──────────────────────────────────────────────────────────

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