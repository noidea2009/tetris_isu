import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class NextQueuePanel extends JPanel {

    private static final int TILE_SIZE = 30;
    private static final int PREVIEW_TILE = 24;
    private static final int CELL_W = 4 * PREVIEW_TILE;
    private static final int CELL_H = 4 * PREVIEW_TILE;
    private static final int QUEUE_SIZE = 5;
    private static final Color[] PIECE_COLORS = {
            new Color(0, 240, 240),
            new Color(160, 0, 240),
            new Color(240, 160, 0),
            new Color(0, 0, 240),
            new Color(240, 0, 0),
            new Color(0, 240, 0),
            new Color(240, 240, 0),
    };

    private final Game game;
    // Sprite images for each piece type (I, T, L, J, Z, S, O)
    private BufferedImage[] pieceSprites;

    /**
     * Load mino sprites at original size.
     */
    private void loadSprites() {
        try {
            BufferedImage spriteSheet = ImageIO.read(new File("tetris__TEMPLATE.png"));
            pieceSprites = new BufferedImage[7];

            // Load original sizes directly (assuming the subimages define the full size)
            pieceSprites[4] = spriteSheet.getSubimage(2, 1, 92, 92);   // Z
            pieceSprites[3] = spriteSheet.getSubimage(4, 97, 96, 96);  // J
            pieceSprites[6] = spriteSheet.getSubimage(193, 1, 96, 96); // O
            pieceSprites[5] = spriteSheet.getSubimage(289, 1, 96, 96); // S
            pieceSprites[0] = spriteSheet.getSubimage(385, 1, 96, 96); // I
            pieceSprites[2] = spriteSheet.getSubimage(97, 1, 96, 96);  // L
            pieceSprites[1] = spriteSheet.getSubimage(97, 97, 96, 96); // T

        } catch (IOException e) {
            System.err.println("Failed to load sprite sheet: " + e.getMessage());
            pieceSprites = null;
        }
    }

    /**
     * Scale a BufferedImage to the specified dimensions
     * @param original the original photo of mino blocks itself
     * @param width width to scale to
     * @param height height to scale to
     */
    private BufferedImage scaleImage(BufferedImage original, int width, int height) {
        BufferedImage scaled = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = scaled.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(original, 0, 0, width, height, null);
        g2d.dispose();
        return scaled;
    }
    public NextQueuePanel(Game game) {
        this.game = game;
        loadSprites();
        int panelH = 20 + QUEUE_SIZE * (CELL_H + 10);
        setPreferredSize(new Dimension(CELL_W + 20, panelH));
        setBackground(Color.BLACK);
    }

    /**
     *paintComponent of NextQueuePanel, called every 16ms
     **/
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(new Color(180, 180, 180));
        g.setFont(new Font("Arial", Font.BOLD, 13));
        g.drawString("NEXT", 10, 16);

        List<Integer> queue = game.getNextQueue(QUEUE_SIZE);

        for (int i = 0; i < queue.size(); i++) {
            int type = queue.get(i);
            int[][] shape = PieceData.SHAPES[type][0];

            // 1. Calculate bounding box for centering
            int minX = 99, minY = 99, maxX = -1, maxY = -1;
            for (int[] b : shape) {
                minX = Math.min(minX, b[0]); maxX = Math.max(maxX, b[0]);
                minY = Math.min(minY, b[1]); maxY = Math.max(maxY, b[1]);
            }

            int shapeW = (maxX - minX + 1) * PREVIEW_TILE;
            int shapeH = (maxY - minY + 1) * PREVIEW_TILE;

            // Calculate offsets
            int cellTop = 24 + i * (CELL_H + 10);
            int offsetX = 10 + (CELL_W - shapeW) / 2 - minX * PREVIEW_TILE;
            int offsetY = cellTop + (CELL_H - shapeH) / 2 - minY * PREVIEW_TILE;

            //  Draw using Sprite OR Fallback
            for (int[] block : shape) {
                int px = offsetX + block[0] * PREVIEW_TILE;
                int py = offsetY + block[1] * PREVIEW_TILE;

                if (pieceSprites != null && pieceSprites[type] != null) {
                    g.drawImage(pieceSprites[type], px, py, PREVIEW_TILE, PREVIEW_TILE, null);
                } else {
                    // Fallback drawing logic
                    Color color = PIECE_COLORS[type];
                    g.setColor(color);
                    g.fillRect(px + 1, py + 1, PREVIEW_TILE - 2, PREVIEW_TILE - 2);
                    g.setColor(color.brighter());
                    g.drawLine(px + 1, py + 1, px + PREVIEW_TILE - 2, py + 1);
                    g.drawLine(px + 1, py + 1, px + 1, py + PREVIEW_TILE - 2);
                    g.setColor(color.darker());
                    g.drawLine(px + PREVIEW_TILE - 2, py + 1, px + PREVIEW_TILE - 2, py + PREVIEW_TILE - 2);
                    g.drawLine(px + 1, py + PREVIEW_TILE - 2, px + PREVIEW_TILE - 2, py + PREVIEW_TILE - 2);
                }
            }
        }
    }
}