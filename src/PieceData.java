public final class PieceData {
    public static final int[][][][] SHAPES = {

            // I PIECE
            {
                    {{0,1}, {1,1}, {2,1}, {3,1}}, // 0
                    {{2,0}, {2,1}, {2,2}, {2,3}}, // R
                    {{0,2}, {1,2}, {2,2}, {3,2}}, // 2
                    {{1,0}, {1,1}, {1,2}, {1,3}}  // L
            },

            // T PIECE
            {
                    {{1,0}, {0,1}, {1,1}, {2,1}}, // 0
                    {{1,0}, {1,1}, {2,1}, {1,2}}, // R
                    {{0,1}, {1,1}, {2,1}, {1,2}}, // 2
                    {{1,0}, {0,1}, {1,1}, {1,2}}  // L
            },

            // L PIECE
            {
                    {{2,0}, {0,1}, {1,1}, {2,1}}, // 0: foot top-right
                    {{1,0}, {1,1}, {1,2}, {2,2}}, // R: foot bottom-right  ← swapped from yours
                    {{0,2}, {0,1}, {1,1}, {2,1}}, // 2: foot bottom-left
                    {{1,0}, {0,0}, {1,1}, {1,2}}  // L: foot top-left
            },

            // J PIECE
            {
                    {{0,0}, {0,1}, {1,1}, {2,1}}, // 0: foot top-left
                    {{1,0}, {2,0}, {1,1}, {1,2}}, // R: foot top-right
                    {{0,1}, {1,1}, {2,1}, {2,2}}, // 2: foot bottom-right
                    {{1,0}, {1,1}, {0,2}, {1,2}}  // L: foot bottom-left
            },

            // Z PIECE  (SRS: top-left pair, bottom-right pair)
            {
                    {{0,0}, {1,0}, {1,1}, {2,1}}, // 0  ← ✓
                    {{2,0}, {1,1}, {2,1}, {1,2}}, // R  ← was {{1,0},{1,1},{0,1},{0,2}}
                    {{0,1}, {1,1}, {1,2}, {2,2}}, // 2  ←  ✓
                    {{1,0}, {0,1}, {1,1}, {0,2}}  // L  ← was {{2,0},{2,1},{1,1},{1,2}}
            },

// S PIECE  (SRS: top-right pair, bottom-left pair)
            {
                    {{1,0}, {2,0}, {0,1}, {1,1}}, // 0  ← was {{2,0},{1,0},{1,1},{0,1}}
                    {{1,0}, {1,1}, {2,1}, {2,2}}, // R  ← was {{0,0},{0,1},{1,1},{1,2}}
                    {{1,1}, {2,1}, {0,2}, {1,2}}, // 2  ← was {{2,1},{1,1},{1,2},{0,2}}
                    {{0,0}, {0,1}, {1,1}, {1,2}}  // L  ← was {{1,0},{1,1},{2,1},{2,2}}
            },
            // O PIECE
            {
                    {{1,0}, {2,0}, {1,1}, {2,1}},
                    {{1,0}, {2,0}, {1,1}, {2,1}},
                    {{1,0}, {2,0}, {1,1}, {2,1}},
                    {{1,0}, {2,0}, {1,1}, {2,1}}
            }
    };
    private PieceData() {}
    // JLSTZ offset data [rotation][offsetIndex][x,y]
    private static final int[][][] OFFSETS_JLSTZ = {
            { {0,0}, {0,0},  {0,0},  {0,0},  {0,0}  }, // 0
            { {0,0}, {1,0},  {1,-1}, {0,2},  {1,2}  }, // R
            { {0,0}, {0,0},  {0,0},  {0,0},  {0,0}  }, // 2
            { {0,0}, {-1,0}, {-1,-1},{0,2},  {-1,2} }  // L
    };

    private static final int[][][] OFFSETS_I = {
            { {0,0}, {-1,0}, {2,0},  {-1,0}, {2,0}  }, // 0
            { {0,0}, {0,0},  {0,0},  {0,1},  {0,-2} }, // R   Changed first element from {-1,0} to {0,0}
            { {-1,1}, {1,1},  {-2,1}, {1,0},  {-2,0} }, // 2
            { {0,1},  {0,1},  {0,1},  {0,-1}, {0,2}  }  // L
    };

    // O offset data [rotation][offsetIndex][x,y]
    private static final int[][][] OFFSETS_O = {
            { {0,0}  }, // 0
            { {0,-1} }, // R
            { {-1,-1}}, // 2
            { {-1,0} }  // L
    };


    // Define standard 180-degree kicks for JLSTZ pieces
    private static final int[][][] KICKS_180_JLSTZ = {
            { {0,0}, {0,1}, {1,1}, {-1,1}, {1,0}, {-1,0} },   // 0 -> 2
            { {0,0}, {1,0}, {1,2}, {1,1}, {0,2}, {0,1} },     // R -> L
            { {0,0}, {0,-1}, {-1,-1}, {1,-1}, {-1,0}, {1,0} }, // 2 -> 0
            { {0,0}, {-1,0}, {-1,2}, {-1,1}, {0,2}, {0,1} }    // L -> R
    };

    // Define standard 180-degree kicks for I pieces
    private static final int[][][] KICKS_180_I = {
            { {0,0}, {-1,0}, {-2,0}, {1,0}, {2,0}, {0,1} },   // 0 -> 2
            { {0,0}, {0,1}, {0,2}, {0,-1}, {0,-2}, {-1,0} },  // R -> L
            { {0,0}, {1,0}, {2,0}, {-1,0}, {-2,0}, {0,-1} },  // 2 -> 0
            { {0,0}, {0,1}, {0,2}, {0,-1}, {0,-2}, {1,0} }    // L -> R
    };

    public static int[][] getKicks(int type, int fromRot, int toRot) {
        if (type < 0 || type > 6) throw new IllegalArgumentException("Invalid piece type: " + type);
        if (fromRot < 0 || fromRot > 3 || toRot < 0 || toRot > 3)
            throw new IllegalArgumentException("Invalid rotation state");

        if (type == 6) {
            return new int[][] { {0, 0} };
        }

        // 180 spin
        int diff = Math.abs(fromRot - toRot);
        if (diff == 2) {
            int[][][] table180 = (type == 0) ? KICKS_180_I : KICKS_180_JLSTZ;
            int[][] baselineKicks = table180[fromRot];

            // Copy directly without inverting the Y axis, as the vectors are already
            // mapped in screen-space coordinates.
            int[][] adjustedKicks = new int[baselineKicks.length][2];
            for (int i = 0; i < baselineKicks.length; i++) {
                adjustedKicks[i][0] = baselineKicks[i][0];
                adjustedKicks[i][1] = baselineKicks[i][1];
            }
            return adjustedKicks;
        }

        // Fallback to standard 90-degree offset calculation
        int[][][] table = (type == 0) ? OFFSETS_I : OFFSETS_JLSTZ;
        int count = table[0].length;
        int[][] kicks = new int[count][2];
        for (int i = 0; i < count; i++) {
            kicks[i][0] = table[fromRot][i][0] - table[toRot][i][0];
            kicks[i][1] = -(table[fromRot][i][1] - table[toRot][i][1]);
        }
        return kicks;
    }
}