public class Piece {
//    KEY METHODS
//    getShape()
//    rotateCW(Board)
//    rotateCCW(Board)
//    Key Data:
//
//      type (I, T, L, etc.)
//      rotation
//      x, y position
    private int type;
    private int rotation;
    private int x,y;
public Piece(int type) {
    if (type < 0 || type > 6) throw new IllegalArgumentException("Invalid piece type: " + type);
    this.type = type;
    this.rotation = 0;
    this.x = 3; // Starting center for a 10-wide board
    this.y = 0;
    // I-piece (type 0) spawns at x=3, others spawn at x=3 or 4 depending on bounding box symmetry.
    // Standard Guideline puts the 4x4 box at x=3, meaning cells occupy columns 3,4,5,6.
    this.y = (type == 0) ? -1 : 0; // Standard Guideline often spawns the I-piece slightly higher

}
public int getType() { return type; }
public int[][] getShape(){
    return PieceData.SHAPES[type][rotation];
}
public int getRotation(){return rotation;}
public int getX(){return this.x;}
public int getY(){return this.y;}

    public void move(int dx, int dy) {
        this.x += dx;
        this.y += dy;
    }
public boolean rotateCW(Board board){
    return tryRotate(board , +1);
}
public boolean rotateCCW(Board board){
    return tryRotate(board,3);
}
public boolean rotate180(Board board){
    return tryRotate(board,2);
}
public boolean tryMove(int dx, int dy, Board board) {
        move(dx, dy);
        if (board.isCollision(this)) {
            move(-dx, -dy); // revert
            return false;
        }
        return true;
    }

    private int lastUsedKickIndex = -1; // Track which kick index succeeded

    public int getLastUsedKickIndex() {
        return lastUsedKickIndex;
    }
    private boolean tryRotate(Board board, int dir) {
        int oldRotation = this.rotation;
        // Calculate new rotation state (dir will be +1 for CW, +2 for 180, +3 for CCW)
        int newRotation = (this.rotation + dir) % 4;

        int[][] kicks = PieceData.getKicks(type, oldRotation, newRotation);

        int baseX = this.x;
        int baseY = this.y;

        for (int i = 0; i < kicks.length; i++) {
            int[] kick = kicks[i];
            this.x = baseX + kick[0];
            this.y = baseY + kick[1];
            this.rotation = newRotation;

            // Validating with your piece object overload method inside Board.java
            if (!board.isCollision(this)) {
                this.lastUsedKickIndex = i; // Crucial for upgrading T-Spins via Kick 4/5
                return true;
            }
        }

        // Rollback state if no kick translations clear a collision
        this.x = baseX;
        this.y = baseY;
        this.rotation = oldRotation;
        this.lastUsedKickIndex = -1;
        return false;
    }

}
