import javax.swing.*;
import java.awt.*;

public class HoldPanel extends JPanel {
    private static final int PREVIEW_TILE = 24;
    private static final int CELL_W = 4 * PREVIEW_TILE;
    private static final int CELL_H = 4 * PREVIEW_TILE;
    private static final Color[] PIECE_COLORS = {
            new Color(0, 240, 240), new Color(160, 0, 240), new Color(240, 160, 0),
            new Color(0, 0, 240),   new Color(240, 0, 0),   new Color(0, 240, 0),
            new Color(240, 240, 0),
    };

    private final Game game;

    public HoldPanel(Game game) {
        this.game = game;
        setPreferredSize(new Dimension(CELL_W + 20, CELL_H + 30));
        setBackground(Color.BLACK);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(new Color(180, 180, 180));
        g.setFont(new Font("Arial", Font.BOLD, 13));
        g.drawString("HOLD", 10, 16);

        int holdType = game.getHoldType(); // -1 if empty
        if (holdType == -1) return;

        int[][] shape = PieceData.SHAPES[holdType][0];
        Color color = game.isHoldUsed()
                ? PIECE_COLORS[holdType].darker()   // dim when unavailable
                : PIECE_COLORS[holdType];

        int minX = 99, minY = 99, maxX = -1, maxY = -1;
        for (int[] b : shape) {
            minX = Math.min(minX, b[0]); maxX = Math.max(maxX, b[0]);
            minY = Math.min(minY, b[1]); maxY = Math.max(maxY, b[1]);
        }
        int shapeW = (maxX - minX + 1) * PREVIEW_TILE;
        int shapeH = (maxY - minY + 1) * PREVIEW_TILE;

        int offsetX = 10 + (CELL_W - shapeW) / 2 - minX * PREVIEW_TILE;
        int offsetY = 24  + (CELL_H - shapeH) / 2 - minY * PREVIEW_TILE;

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