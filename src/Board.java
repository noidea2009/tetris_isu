/**
 * Made by Junjit Chang
 * Represents the game board state. Manages the 22x10 grid, collision detection,
 * piece locking, and line clearing logic.
 */
public class Board {
    private int[][] grid;
    public static final int COLS = 10;
    public static final int ROWS = 22;

    /**
     * Initializes the board and resets the grid to empty.
     */
    public void reset() {
        grid = new int[ROWS][COLS];
    }

    /**
     * Constructs a new Board instance and initializes the grid.
     */
    public Board() {
        reset();
    }

    /**
     * Returns the raw 2D array representation of the board.
     * @return The 22x10 grid.
     */
    public int[][] getBoard() {
        return grid;
    }

    /**
     * Checks if a piece would be blocked by existing pieces on the board.
     * Used primarily for spin detection (ignoring wall/floor collisions).
     * * @param piece The shape of the piece to check.
     * @param x The target column.
     * @param y The target row.
     * @return true if blocked by a piece, false otherwise.
     */
    public boolean isBlockedByBlocks(int[][] piece, int x, int y) {
        for (int[] block : piece) {
            int targetX = block[0] + x;
            int targetY = block[1] + y;

            if (targetY >= 0 && targetY < grid.length && targetX >= 0 && targetX < grid[0].length) {
                if (grid[targetY][targetX] != 0) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Prints a debug representation of the board to the console.
     */
    public void printBoardDebug() {
        System.out.println("--- Current Board State ---");
        for (int[] row : grid) {
            for (int cell : row) {
                System.out.print((cell != 0 ? "1" : "0") + " ");
            }
            System.out.println();
        }
        System.out.println("---------------------------");
    }

    /**
     * Checks if a piece collides with board boundaries or existing blocks.
     * @param piece The shape coordinates of the piece.
     * @param x The board x-coordinate to test.
     * @param y The board y-coordinate to test.
     * @return true if collision occurs.
     */
    public boolean isCollision(int[][] piece, int x, int y) {
        for (int[] block : piece) {
            int targetX = block[0] + x;
            int targetY = block[1] + y;

            if (targetX < 0 || targetX >= grid[0].length || targetY >= grid.length) {
                return true;
            }

            if (targetY >= 0 && grid[targetY][targetX] != 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Overloaded collision check using a Piece object.
     * @param piece The piece to check.
     * @return true if collision occurs.
     */
    public boolean isCollision(Piece piece) {
        return isCollision(piece.getShape(), piece.getX(), piece.getY());
    }

    /**
     * Locks a piece into the board and triggers line clearing.
     * @param piece The piece to place.
     * @return The number of lines cleared.
     */
    public int placePiece(Piece piece) {
        int[][] shape = piece.getShape();
        int x = piece.getX(), y = piece.getY();
        for (int[] block : shape) {
            int bx = block[0] + x, by = block[1] + y;
            if (by >= 0) grid[by][bx] = piece.getType() + 1;
        }
        return clearLines();
    }

    /**
     * Scans the grid for full rows, removes them, and shifts higher blocks down.
     * @return The total number of lines cleared.
     */
    public int clearLines() {
        int cleared = 0;
        for (int row = grid.length - 1; row >= 0; row--) {
            boolean full = true;
            for (int col = 0; col < grid[row].length; col++) {
                if (grid[row][col] == 0) { full = false; break; }
            }
            if (full) {
                removeLine(row);
                row++;
                cleared++;
            }
        }
        return cleared;
    }

    /**
     * Shifts all rows above the given index down by one.
     * @param removeindex The row index to clear.
     */
    private void removeLine(int removeindex) {
        for(int i = removeindex; i > 0; i--) {
            grid[i] = grid[i-1].clone();
        }
        grid[0] = new int[grid[0].length];
    }

    /**
     * Checks if the board contains no blocks.
     * @return true if the board is empty.
     */
    public boolean isBoardEmpty() {
        for (int[] row : grid)
            for (int cell : row)
                if (cell != 0) return false;
        return true;
    }
}