import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class GamePanel extends JPanel {
    private Game game;
    private static final int TILE_SIZE     = 40;
    private static final int VISIBLE_ROWS  = 20;
    private static final int BUFFER_ROWS   = Board.ROWS - VISIBLE_ROWS;
    private static final int SCORE_PANEL_H = 64;

    private BufferedImage[] pieceSprites;
    private static final Color[] PIECE_COLORS = {
            new Color(0,   240, 240), new Color(160, 0,   240),
            new Color(240, 160, 0),   new Color(0,   0,   240),
            new Color(240, 0,   0),   new Color(0,   240, 0),
            new Color(240, 240, 0),
    };

    public GamePanel(Game game) {
        this.game = game;
        setFocusable(true);
        requestFocusInWindow();
        setPreferredSize(new Dimension(
                Board.COLS * TILE_SIZE,
                VISIBLE_ROWS * TILE_SIZE + SCORE_PANEL_H));
        setBackground(Color.BLACK);
        loadSprites();
    }

    private void loadSprites() {
        try {
            // Sprite loading with dynamic scaling for flexibility
            BufferedImage sheet = ImageIO.read(new File("tetris__TEMPLATE.png"));
            pieceSprites = new BufferedImage[7];

            pieceSprites[4] = scaleImage(sheet.getSubimage(2,   1,  92, 92), TILE_SIZE,     TILE_SIZE);
            pieceSprites[3] = scaleImage(sheet.getSubimage(4,   97, 96, 96), TILE_SIZE + 1, TILE_SIZE + 1);
            pieceSprites[6] = scaleImage(sheet.getSubimage(193, 1,  96, 96), TILE_SIZE + 1, TILE_SIZE + 1);
            pieceSprites[5] = scaleImage(sheet.getSubimage(289, 1,  96, 96), TILE_SIZE + 1, TILE_SIZE + 1);
            pieceSprites[0] = scaleImage(sheet.getSubimage(385, 1,  96, 96), TILE_SIZE + 1, TILE_SIZE + 1);
            pieceSprites[2] = scaleImage(sheet.getSubimage(97,  1,  96, 96), TILE_SIZE + 1, TILE_SIZE + 1);
            pieceSprites[1] = scaleImage(sheet.getSubimage(97,  97, 96, 96), TILE_SIZE + 1, TILE_SIZE + 1);
        } catch (IOException e) {
            // Fallback mechanism: for missing assets
            System.err.println("Failed to load sprite sheet: " + e.getMessage());
            pieceSprites = null;
        }
    }

    private BufferedImage scaleImage(BufferedImage src, int w, int h) {
        //  scale the image better so I DONT need to deal with blurry sprites
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = out.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(src, 0, 0, w, h, null);
        g2.dispose(); // Important: resource management to prevent leaks
        return out;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // rendering order dictates depth layering
        drawGrid(g);
        drawLockedPieces(g);
        drawGhostPiece(g); // Ghost rendered before active to maintain layering
        drawActivePiece(g);
        drawScorePanel(g);
    }

    private void drawScorePanel(Graphics g) {
        int boardBottom = VISIBLE_ROWS * TILE_SIZE;
        int w = Board.COLS * TILE_SIZE;

        // Background dark container
        g.setColor(new Color(18, 18, 18));
        g.fillRect(0, boardBottom, w, SCORE_PANEL_H);

        // Grid separator line
        g.setColor(new Color(55, 55, 55));
        g.drawLine(0, boardBottom, w, boardBottom);

        // SCORE rendering
        String scoreStr = String.format("%,d", game.getScore());
        g.setFont(new Font("Arial", Font.BOLD, 22));
        g.setColor(Color.WHITE);
        g.drawString(scoreStr, 10, boardBottom + 30);

        g.setFont(new Font("Arial", Font.PLAIN, 10));
        g.setColor(new Color(130, 130, 130));
        g.drawString("SCORE", 10, boardBottom + 46);

        // LEVEL rendering
        int midX = 180;
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.setColor(new Color(80, 200, 255));
        g.drawString(String.valueOf(game.getLevel()), midX, boardBottom + 28);

        g.setFont(new Font("Arial", Font.PLAIN, 10));
        g.setColor(new Color(130, 130, 130));
        g.drawString("LEVEL", midX, boardBottom + 43);

        // LINES rendering
        int rightX = 240;
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.setColor(new Color(255, 210, 60));
        g.drawString(String.valueOf(game.getLines()), rightX, boardBottom + 28);

        g.setFont(new Font("Arial", Font.PLAIN, 10));
        g.setColor(new Color(130, 130, 130));
        g.drawString("LINES", rightX, boardBottom + 43);
    }

    private void drawGrid(Graphics g) {
        g.setColor(new Color(40, 40, 40));
        for (int y = 0; y < VISIBLE_ROWS; y++)
            for (int x = 0; x < Board.COLS; x++)
                g.drawRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE - 1, TILE_SIZE - 1);
    }

    private void drawLockedPieces(Graphics g) {
        int[][] grid = game.getBoard().getBoard();
        // Buffer offset: maps logical game state to visible screen coordinates
        for (int y = BUFFER_ROWS; y < grid.length; y++)
            for (int x = 0; x < grid[y].length; x++)
                if (grid[y][x] != 0)
                    drawTile(g, x, y, grid[y][x] - 1, true);
    }

    private void drawGhostPiece(Graphics g) {
        // Decoupled logic: calculates preview without modifying actual game state
        Piece piece = game.getCurrentPiece();
        int ghostY = piece.getY();
        while (!game.getBoard().isCollision(piece.getShape(), piece.getX(), ghostY + 1))
            ghostY++;
        if (ghostY == piece.getY()) return;

        for (int[] block : piece.getShape()) {
            int px = block[0] + piece.getX();
            int py = block[1] + ghostY;
            if (py >= BUFFER_ROWS) drawGhostTile(g, px, py, piece.getType());
        }
    }

    private void drawActivePiece(Graphics g) {
        Piece piece = game.getCurrentPiece();
        for (int[] block : piece.getShape()) {
            int px = block[0] + piece.getX();
            int py = block[1] + piece.getY();
            if (py >= BUFFER_ROWS) drawTile(g, px, py, piece.getType(), true);
        }
    }

    private void drawTile(Graphics g, int x, int y, int pieceType, boolean bevel) {
        int px = x * TILE_SIZE;
        int py = (y - BUFFER_ROWS) * TILE_SIZE;
        if (pieceSprites != null && pieceType >= 0 && pieceType < pieceSprites.length) {
            g.drawImage(pieceSprites[pieceType], px, py, null);
        } else {
            // Procedural fallback: creates bevel effect for UI depth
            Color c = PIECE_COLORS[pieceType];
            g.setColor(c);
            g.fillRect(px + 1, py + 1, TILE_SIZE - 2, TILE_SIZE - 2);
            if (bevel) {
                g.setColor(c.brighter());
                g.drawLine(px + 1, py + 1, px + TILE_SIZE - 2, py + 1);
                g.drawLine(px + 1, py + 1, px + 1, py + TILE_SIZE - 2);
                g.setColor(c.darker());
                g.drawLine(px + TILE_SIZE - 2, py + 1, px + TILE_SIZE - 2, py + TILE_SIZE - 2);
                g.drawLine(px + 1, py + TILE_SIZE - 2, px + TILE_SIZE - 2, py + TILE_SIZE - 2);
            }
        }
    }

    private void drawGhostTile(Graphics g, int x, int y, int pieceType) {
        int px = x * TILE_SIZE;
        int py = (y - BUFFER_ROWS) * TILE_SIZE;
        if (pieceSprites != null && pieceType >= 0 && pieceType < pieceSprites.length) {
            // Alpha composition: creates visual transparency for the ghost piece
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
            g2d.drawImage(pieceSprites[pieceType], px, py, null);
            g2d.dispose();
        } else {
            g.setColor(new Color(100, 100, 100, 120));
            g.fillRect(px + 1, py + 1, TILE_SIZE - 2, TILE_SIZE - 2);
        }
    }
}