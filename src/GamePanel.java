import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class GamePanel extends JPanel {
    private Game game;
    private static final int TILE_SIZE = 36;
    // Only render visible rows (rows 2–21, i.e. skip hidden buffer)
    private static final int VISIBLE_ROWS = 20;
    private static final int BUFFER_ROWS  = Board.ROWS - VISIBLE_ROWS; // = 2

   
    // Sprite images for each piece type (I, T, L, J, Z, S, O)
    private BufferedImage[] pieceSprites;
    
    // Per-type Tetris colors (fallback if sprites fail to load)
    private static final Color[] PIECE_COLORS = {
            new Color(0, 240, 240),   // I - Cyan
            new Color(160, 0, 240),   // T - Purple
            new Color(240, 160, 0),   // L - Orange
            new Color(0, 0, 240),     // J - Blue
            new Color(240, 0, 0),     // Z - Red
            new Color(0, 240, 0),     // S - Green
            new Color(240, 240, 0),   // O - Yellow
    };

    public GamePanel(Game game) {
        this.game = game;
        setFocusable(true);
        // Requesting focus can sometimes fail if the component isn't on screen yet depends on OS, sometimes it doesnt work for mac machines thats all I know.
        // It's best to call requestFocusInWindow() after the frame is visible
        requestFocusInWindow();

        // This explicitly tells layout managers exactly how big the board area needs to be
        setPreferredSize(new Dimension(Board.COLS * TILE_SIZE, VISIBLE_ROWS * TILE_SIZE));
        setBackground(Color.BLACK);
        
        // Load sprite sheet
        loadSprites();
    }
    
    /**
     * Load and crop mino sprites from image
     * Sprite layout: I, T, L, J, Z, S, O (left to right)
     * Each block is 48x48 with 1px padding
     */
    private void loadSprites() {
        try {
            BufferedImage spriteSheet = ImageIO.read(new File("tetris__TEMPLATE.png"));

            pieceSprites = new BufferedImage[7];

            // Z
            pieceSprites[4] = scaleImage(
                    spriteSheet.getSubimage(2, 1, 92, 92),
                    TILE_SIZE,
                    TILE_SIZE
            );

            // J (orange in your sheet)
            pieceSprites[3] = scaleImage(
                    spriteSheet.getSubimage(4, 97, 96, 96),
                    TILE_SIZE+1,
                    TILE_SIZE+1
            );

            // O
            pieceSprites[6] = scaleImage(
                    spriteSheet.getSubimage(193, 1, 96, 96),
                    TILE_SIZE+1,
                    TILE_SIZE+1
            );

            // S
            pieceSprites[5] = scaleImage(
                    spriteSheet.getSubimage(289, 1, 96, 96),
                    TILE_SIZE+1,
                    TILE_SIZE+1
            );

            // I
            pieceSprites[0] = scaleImage(
                    spriteSheet.getSubimage(385, 1, 96, 96),
                    TILE_SIZE+1,
                    TILE_SIZE+1
            );

            // L (blue in your sheet)
            pieceSprites[2] = scaleImage(
                    spriteSheet.getSubimage(97, 1, 96, 96),
                    TILE_SIZE+1,
                    TILE_SIZE+1
            );

            // T
            pieceSprites[1] = scaleImage(
                    spriteSheet.getSubimage(97, 97, 96, 96),
                    TILE_SIZE+1,
                    TILE_SIZE+1
            );

        } catch (IOException e) {
            System.err.println("Failed to load sprite sheet: " + e.getMessage());
            pieceSprites = null;
        }
    }
    
    /**
     * Scale a BufferedImage to the specified dimensions
     */
    private BufferedImage scaleImage(BufferedImage original, int width, int height) {
        BufferedImage scaled = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        //Graphics2D used here to scale grahics, else it will be really messy
        Graphics2D g2d = scaled.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(original, 0, 0, width, height, null);
        //dispose because memory, else it will crash pretty fast
        g2d.dispose();
        return scaled;
    }

    @Override
    protected void paintComponent(Graphics g) {
        // Clear the screen and handle standard Swing background painting automatically
        super.paintComponent(g);

        // without risking flicker, removed the buggy off-screen buffer logic.
        drawGrid(g);
        drawLockedPieces(g);
        drawGhostPiece(g);
        drawActivePiece(g);
    }

    private void drawGrid(Graphics g) {
        g.setColor(new Color(40, 40, 40));
        for (int y = 0; y < VISIBLE_ROWS; y++) {
            for (int x = 0; x < Board.COLS; x++) {
                // Changed TILE_SIZE to TILE_SIZE - 1 for width and height
                g.drawRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE - 1, TILE_SIZE - 1);
            }
        }
    }

    private void drawLockedPieces(Graphics g) {
        int[][] grid = game.getBoard().getBoard();
        for (int y = BUFFER_ROWS; y < grid.length; y++) {   // skip buffer rows
            for (int x = 0; x < grid[y].length; x++) {
                int pieceType = grid[y][x];
                if (pieceType != 0) {
                    // pieceType is 1-7, but our array is 0-6, so subtract 1
                    drawTile(g, x, y, pieceType - 1, true);
                }
            }
        }
    }

    private void drawGhostPiece(Graphics g) {
        Piece piece = game.getCurrentPiece();
        int ghostY = piece.getY();
        while (!game.getBoard().isCollision(piece.getShape(), piece.getX(), ghostY + 1)) {
            ghostY++;
        }
        if (ghostY == piece.getY()) return;

        // Draw ghost piece with transparency
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

    /**
     * Draw a tile using sprite image or fallback to color
     * @param g Graphics context
     * @param x Grid x position
     * @param y Grid y position
     * @param pieceType Piece type (0-6: I, T, L, J, Z, S, O)
     * @param bevel Whether to draw bevel effect (only used for color fallback)
     */
    private void drawTile(Graphics g, int x, int y, int pieceType, boolean bevel) {
        int px = x * TILE_SIZE;
        int py = (y - BUFFER_ROWS) * TILE_SIZE;   // KEY: offset by buffer
        
        // Use sprite if available
        if (pieceSprites != null && pieceType >= 0 && pieceType < pieceSprites.length) {
            g.drawImage(pieceSprites[pieceType], px, py, null);
        } else {
            // Fallback to color rendering
            Color color = PIECE_COLORS[pieceType];
            g.setColor(color);
            g.fillRect(px + 1, py + 1, TILE_SIZE - 2, TILE_SIZE - 2);
            if (bevel) {
                g.setColor(color.brighter());
                g.drawLine(px + 1, py + 1, px + TILE_SIZE - 2, py + 1);
                g.drawLine(px + 1, py + 1, px + 1, py + TILE_SIZE - 2);
                g.setColor(color.darker());
                g.drawLine(px + TILE_SIZE - 2, py + 1, px + TILE_SIZE - 2, py + TILE_SIZE - 2);
                g.drawLine(px + 1, py + TILE_SIZE - 2, px + TILE_SIZE - 2, py + TILE_SIZE - 2);
            }
        }
    }
    
    /**
     * Draw a ghost tile with transparency
     */
    private void drawGhostTile(Graphics g, int x, int y, int pieceType) {
        int px = x * TILE_SIZE;
        int py = (y - BUFFER_ROWS) * TILE_SIZE;
        
        // Use sprite with transparency if available
        if (pieceSprites != null && pieceType >= 0 && pieceType < pieceSprites.length) {
            //Graphics2D is used to control image opacity
            Graphics2D g2d = (Graphics2D) g.create();
            //opacity
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
            g2d.drawImage(pieceSprites[pieceType], px, py, null);
            //dispose becuase memory
            g2d.dispose();
        } else {
            // Fallback to semi-transparent gray
            Color ghost = new Color(100, 100, 100, 120);
            g.setColor(ghost);
            g.fillRect(px + 1, py + 1, TILE_SIZE - 2, TILE_SIZE - 2);
        }
    }
}