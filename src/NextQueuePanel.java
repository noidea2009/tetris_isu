import javax.swing.*;
import java.awt.*;
import java.util.List;

public class NextQueuePanel extends JPanel {
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

    public NextQueuePanel(Game game) {
        this.game = game;
        int panelH = 20 + QUEUE_SIZE * (CELL_H + 10);
        setPreferredSize(new Dimension(CELL_W + 20, panelH));
        setBackground(Color.BLACK);
    }

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
            Color color = PIECE_COLORS[type];

            // bounding box of this shape
            int minX = 99, minY = 99, maxX = -1, maxY = -1;
            for (int[] b : shape) {
                minX = Math.min(minX, b[0]); maxX = Math.max(maxX, b[0]);
                minY = Math.min(minY, b[1]); maxY = Math.max(maxY, b[1]);
            }
            int shapeW = (maxX - minX + 1) * PREVIEW_TILE;
            int shapeH = (maxY - minY + 1) * PREVIEW_TILE;

            // center within cell
            int cellTop = 24 + i * (CELL_H + 10);
            int offsetX = 10 + (CELL_W - shapeW) / 2 - minX * PREVIEW_TILE;
            int offsetY = cellTop + (CELL_H - shapeH) / 2 - minY * PREVIEW_TILE;

            for (int[] block : shape) {
                int px = offsetX + block[0] * PREVIEW_TILE;
                int py = offsetY + block[1] * PREVIEW_TILE;
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