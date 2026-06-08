public class Board {
    private int[][] grid;
    public static final int COLS = 10;
    public static final int ROWS = 22;
    public void reset(){
        grid = new int[22][10];

    }
    public Board(){
        reset();
    }
    //TO DO: Maintain the grid (10×20)
    //Handle collision detection
    //Handle piece placement (locking)
    //Handle line clearing
    //
    //Key Data:
    //
    //int[][] grid
    //
    //Key Methods:
    //
    //isCollision(piece, x, y) done
    //placePiece(piece, x, y) done
    //clearLines() done
    //removeLine(int y) y is the index of the row, use .remove and then clone the index -1 .clone becuase you want it to be the one that is above
    
    public int[][] getBoard(){
        return grid;
    }
    public boolean isCollision(int[][] piece, int x, int y) {
        for (int[] block : piece) {

            int targetX = block[0] + x;
            int targetY = block[1] + y;

            if (targetX < 0 || targetX >= grid[0].length) {
                return true;
            }

            if (targetY >= grid.length) {
                return true;
            }

            // Any non-zero cell is occupied
            if (targetY >= 0 && grid[targetY][targetX] != 0) {
                return true;
            }
        }

        return false;
    }
    //overloaded for type: Piece
    public boolean isCollision(Piece piece) {
        return isCollision(piece.getShape(), piece.getX(), piece.getY());
    }


    public void placePiece(int[][] piece, int x, int y){
        for (int[] block : piece){
            int boardx = block[0] + x;
            int boardy = block[1] +y;
            if (boardy >= 0){
                grid[boardy][boardx] = 1;
            }

        }
        clearLines();
    }
    public void placePiece(Piece piece) {
        int[][] shape = piece.getShape();
        int x = piece.getX();
        int y = piece.getY();

        for (int[] block : shape) {
            int boardx = block[0] + x;
            int boardy = block[1] + y;

            if (boardy >= 0) {
                // Store unique piece type (1-7)
                grid[boardy][boardx] = piece.getType() + 1;
            }
        }

        clearLines();
    }
    public void clearLines() {

        // Start from bottom row
        for (int row = grid.length - 1; row >= 0; row--) {

            boolean full = true;

            // Check if every column is occupied
            for (int col = 0; col < grid[row].length; col++) {
                if (grid[row][col] == 0) {
                    full = false;
                    break;
                }
            }

            // Remove full row
            if (full) {
                removeLine(row);

                // Recheck same row after shifting
                row++;
            }
        }
    }

    private void removeLine(int removeindex){
        for(int i = removeindex; i> 0; i--){
            grid[i]=grid[i-1].clone();
        }
        grid[0]=new int[grid[0].length];//new int list with 0
    }
}
