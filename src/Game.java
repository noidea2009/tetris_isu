import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import org.w3c.dom.*;

import javax.swing.Timer;
import javax.xml.parsers.*;
import java.io.File;
import java.util.*;
import javax.sound.sampled.*;

public class Game {
    private Board board;
    private Piece currentPiece;
    private InputHandler input;
    private GamePanel gamePanel;
    private javax.swing.Timer gameTimer;

    private Runnable onExit;
    //music variables
    private MusicPlayer bGMusic = new MusicPlayer();
    Clip place;

    // button flags
    private boolean rotateKeyHeld   = false;
    private boolean holdKeyHeld     = false;
    private boolean hardDropKeyHeld = false;
    private boolean rotate180KeyHeld   = false;
    private boolean escKeyHeld      = false;
    private int lastRotationType = -1; // -1: none, 0: CW, 1: CCW, 2: 180
    // spin flags
    private static final int SPIN_NONE = 0;
    private static final int SPIN_MINI = 1;
    private static final int SPIN_FULL = 2;
    private int     lastSpin              = SPIN_NONE;
    private boolean lastActionWasRotation = false;
    private int     pendingSpin           = SPIN_NONE;

    // 7-bag
    private List<Integer> bag;

    // DAS/ARR/SDF— read from Options on each resetGame()
    private long dasTimestamp;
    private long arrTimestamp;
    private int  DAS_DELAY = 119;
    private int  ARR_RATE  = 16;
    private long softDropTimestamp = 0;
    private int  SOFT_DROP_INTERVAL = 50;

    // Gravity / lock
    private long lockTimestamp              = 0;
    private static final int LOCK_DELAY     = 500;
    private long lastFallTime;
    private static final int GRAVITY_INTERVAL = 500;
    private static final int MAX_LOCK_RESETS  = 15;
    private int lockResetCount = 0;

    // Game state
    private boolean isRunning;

    private NextQueuePanel queuePanel;
    private HoldPanel      holdPanel;
    private int     holdType = -1;
    private boolean holdUsed = false;

    // score flags
    private long    score             = 0;
    private int     level             = 1;
    private int     totalLinesCleared = 0;
    private int     combo             = -1;
    private boolean backToBack        = false;

    /**
     * @param hp the hold piece preview panel
     */
    public void setHoldPanel(HoldPanel hp)      { this.holdPanel  = hp; }

    public int  getHoldType()                   { return holdType; }
    public boolean isHoldUsed()                 { return holdUsed; }

    /**
     * @param qp the next queue preview panel
     */
    public void setQueuePanel(NextQueuePanel qp) { this.queuePanel = qp; }

    private Map<String, List<Clip>> audioPool = new HashMap<>();
    private static final int CLIPS_PER_SOUND = 8;

    /**
     * Preloads all sound effects into the audio pool
     */
    public void loadAudio() {
        try {
            for (String sound : new String[]{"spin_fixed.wav", "hard_drop_fixed.wav","allclear_fixed.wav","clearline_fixed.wav","clearquad_fixed.wav","clearspin_fixed.wav"}) {
                List<Clip> clips = new ArrayList<>();
                for (int i = 0; i < CLIPS_PER_SOUND; i++) {
                    Clip clip = AudioSystem.getClip();
                    clip.open(AudioSystem.getAudioInputStream(new File("tetris_sfx/" + sound)));
                    clips.add(clip);
                }
                audioPool.put(sound, clips);
            }
        } catch (Exception e) {
            System.err.println("Audio load error: " + e.getMessage());
        }
    }

    /**
     * Plays a sound from the audio pool, reusing available clips
     * @param filename the sound file name to play
     */
    private void playSound(String filename) {
        List<Clip> clips = audioPool.get(filename);
        if (clips != null) {
            for (Clip clip : clips) {
                if (!clip.isRunning()) {
                    clip.setFramePosition(0);
                    clip.start();
                    return;
                }
            }
            // Fallback: if all clips busy, restart oldest (shouldn't happen often)
            System.err.println("[Audio] No free clips for " + filename);
        } else {
            System.err.println("[Audio] Sound not loaded: " + filename);
        }
    }

    /**
     * Closes all audio clips and clears the audio pool
     */
    public void cleanup() {
        for (List<Clip> clips : audioPool.values()) {
            for (Clip clip : clips) {
                if (clip.isOpen()) clip.close();
            }
        }
        audioPool.clear();
    }

    /**
     * Initializes and starts the game loop
     * @param gamePanel the main game rendering panel
     * @param onExit callback invoked when game exits
     */
    void start(GamePanel gamePanel, Runnable onExit) {
        this.gamePanel = gamePanel;
        loadAudio();
        bGMusic.start();
        this.onExit    = onExit;
        input = new InputHandler();
        input.getinputfromkeyboard(gamePanel);
        resetGame();
        gameTimer = new Timer(16, e -> update());
        gameTimer.start();
    }

    /**
     * Stops the game loop and cleans up resources
     */
    void stop() {
        isRunning = false;
        if (gameTimer != null) gameTimer.stop();
        cleanup();
    }

    /**
     * Resets all game state to initial values
     */
    void resetGame() {
        // reloads the DAS and ARR settings
        DAS_DELAY = Options.getDAS();
        ARR_RATE  = Options.getARR();
        SOFT_DROP_INTERVAL = Options.getSDF();

        lastFallTime = System.nanoTime() / 1_000_000;
        board    = new Board();
        holdType = -1;
        bag      = new ArrayList<>();
        refillBag();
        spawnPiece();
        isRunning = true;

        score             = 0;
        level             = 1;
        totalLinesCleared = 0;
        combo             = -1;
        backToBack        = false;
    }

    /**
     * Refills the piece bag with shuffled piece types
     */
    void refillBag() {
        //add element once it has less than 14 elements, standard TETR.IO rules
        while (bag.size() < 14) {
            List<Integer> newBag = new ArrayList<>();
            for (int i = 0; i < 7; i++) newBag.add(i);
            //I am cutting slack here because its more convenient
            Collections.shuffle(newBag);
            bag.addAll(newBag);
        }
    }

    /**
     * @param count maximum number of upcoming pieces to return
     * @return list of upcoming piece types
     */
    public List<Integer> getNextQueue(int count) {
        return bag.subList(0, Math.min(count, bag.size()));
    }

    /**
     * Processes all keyboard input for movement, rotation, and actions
     */
    void handleInput() {
        long now = System.nanoTime() / 1_000_000;

        if (input.isKeyPressed(KeyEvent.VK_ESCAPE)) {
            if (!escKeyHeld) {
                escKeyHeld = true;
                stop();
                if (onExit != null) SwingUtilities.invokeLater(onExit);
            }
            return;
        } else {
            escKeyHeld = false;
        }

        if (input.isKeyPressed(KeyEvent.VK_R)) resetGame();

        if (input.isKeyPressed(KeyEvent.VK_SPACE)) {
            if (!hardDropKeyHeld) {
                hardDrop();
                hardDropKeyHeld = true;
            }
        } else {
            hardDropKeyHeld = false;
        }
        if (input.isKeyPressed(KeyEvent.VK_LEFT) || input.isKeyPressed(KeyEvent.VK_RIGHT)) {
            int dx = input.isKeyPressed(KeyEvent.VK_LEFT) ? -1 : 1;
            // flags are modified here
            if (dasTimestamp == 0) {
                dasTimestamp = now;
                arrTimestamp = now;
                if (currentPiece.tryMove(dx, 0, board)) {
                    lockTimestamp         = 0;
                    pendingSpin           = SPIN_NONE;
                }
            } else if (now - dasTimestamp >= DAS_DELAY) {
                if (ARR_RATE == 0) {
                    while (currentPiece.tryMove(dx, 0, board)) { lockTimestamp = 0; }
                    pendingSpin           = SPIN_NONE;
                } else {
                    while (now - arrTimestamp >= ARR_RATE) {
                        if (currentPiece.tryMove(dx, 0, board)) {
                            lockTimestamp         = 0;
                            pendingSpin           = SPIN_NONE;
                        }
                        arrTimestamp += ARR_RATE;
                    }
                }
            }
        } else {
            dasTimestamp = 0;
            arrTimestamp = 0;
        }

        if (input.isKeyPressed(KeyEvent.VK_C)) {
            if (!holdKeyHeld) { hold(); holdKeyHeld = true; }
        } else {
            holdKeyHeld = false;
        }

        // UPDATED SOFT DROP (TETR.IO INSTANT WITH LOCK DELAY MATCH)
        if (input.isKeyPressed(KeyEvent.VK_DOWN)) {
            if (SOFT_DROP_INTERVAL == 0) {
                boolean shiftedDown = false;
                while (currentPiece.tryMove(0, 1, board)) {
                    shiftedDown = true;
                }
                if (shiftedDown) {
                    lastFallTime          = now;
                    pendingSpin           = SPIN_NONE;
                    if (lockTimestamp == 0) {
                        lockTimestamp = now; // Starts lock delay countdown instantly on hit
                    }
                }
            } else if (softDropTimestamp == 0) {
                softDropTimestamp = now;
                dropOneCell();
            } else {
                while (now - softDropTimestamp >= SOFT_DROP_INTERVAL) {
                    softDropTimestamp += SOFT_DROP_INTERVAL;
                    dropOneCell();
                }
            }
        } else {
            softDropTimestamp = 0;
        }

        if (input.isKeyPressed(KeyEvent.VK_UP) || input.isKeyPressed(KeyEvent.VK_Z)) {
            if (!rotateKeyHeld) {
                boolean isCW = input.isKeyPressed(KeyEvent.VK_UP);
                boolean rotated = isCW ? currentPiece.rotateCW(board) : currentPiece.rotateCCW(board);
                // detecting spin
                if (rotated) {
                    lastActionWasRotation = true;
                    lastRotationType = isCW? 1 :2;
                    pendingSpin           = detectSpin(currentPiece);
                }
                // avoiding burst input
                rotateKeyHeld = true;
            }
        } else {
            rotateKeyHeld = false;
        }
        if (input.isKeyPressed(KeyEvent.VK_A)) {
            if (!rotate180KeyHeld) {
                boolean rotated = currentPiece.rotate180(board);
                if (rotated) {
                    lastActionWasRotation = true;
                    pendingSpin           = detectSpin(currentPiece);
                }
                rotate180KeyHeld = true;
            }
        } else {
            rotate180KeyHeld = false;
        }
    }

    /**
     * Moves the current piece down one cell if possible
     */
    private void dropOneCell() {
        if (currentPiece.tryMove(0, 1, board)) {
            lastFallTime          = System.nanoTime() / 1_000_000;
            pendingSpin           = SPIN_NONE;
            lockTimestamp         = 0;
        }
    }

    /**
     * Detects the spin type (none, mini, or full) for the current piece
     * @param piece the piece to check for spin
     * @return spin type: SPIN_NONE, SPIN_MINI, or SPIN_FULL
     */
    int detectSpin(Piece piece) {
        if (!lastActionWasRotation) {
            return SPIN_NONE;
        }

        // T piece
        if (piece.getType() == 1) {

            int px = piece.getX();
            int py = piece.getY();

            boolean tl = isCornerFilled(px + 0, py + 0);
            boolean tr = isCornerFilled(px + 2, py + 0);
            boolean bl = isCornerFilled(px + 0, py + 2);
            boolean br = isCornerFilled(px + 2, py + 2);

            int filledCorners =
                    (tl ? 1 : 0) +
                            (tr ? 1 : 0) +
                            (bl ? 1 : 0) +
                            (br ? 1 : 0);


            int rot = piece.getRotation();

            boolean front0, front1;

            switch (rot) {
                case 0: // Up
                    front0 = tl;
                    front1 = tr;
                    break;

                case 1: // Right
                    front0 = tr;
                    front1 = br;
                    break;

                case 2: // Down
                    front0 = bl;
                    front1 = br;
                    break;

                default: // Left
                    front0 = tl;
                    front1 = bl;
                    break;
            }
            //debug statments, do not remove them because it's really useful
            System.out.printf(
                    "rot=%d TL=%b TR=%b BL=%b BR=%b kick=%d%n",
                    piece.getRotation(),
                    tl, tr, bl, br,
                    piece.getLastUsedKickIndex()
            );
            System.out.printf(
                    "TL(%d,%d) TR(%d,%d) BL(%d,%d) BR(%d,%d)%n",
                    px, py,
                    px+2, py,
                    px, py+2,
                    px+2, py+2
            );

            System.out.printf(
                    "T x=%d y=%d rot=%d%n",
                    px, py, piece.getRotation()
            );
            for (int[] b : piece.getShape()) {
                System.out.printf(
                        "(%d,%d) ",
                        b[0] + px,
                        b[1] + py
                );
            }

            System.out.println();
            if (filledCorners >= 3) {

                if (front0 && front1)
                    return SPIN_FULL;

                if (piece.getLastUsedKickIndex() == 4)
                    return SPIN_FULL;

                if (piece.getLastUsedKickIndex() > 0)
                    return SPIN_MINI;

                return SPIN_NONE;
            }

// Special kicked mini case
            if (filledCorners == 2 && piece.getLastUsedKickIndex() > 0) {
                return SPIN_MINI;
            }

            return SPIN_NONE;}

        // All-Mini for non-T pieces
        int px = piece.getX();
        int py = piece.getY();

        boolean canMoveLeft =
                !board.isCollision(piece.getShape(), px - 1, py);

        boolean canMoveRight =
                !board.isCollision(piece.getShape(), px + 1, py);

        boolean canMoveUp =
                !board.isCollision(piece.getShape(), px, py - 1);

        if (!canMoveLeft && !canMoveRight && !canMoveUp) {
            playSound("spin_fixed.wav");
            return SPIN_MINI;
        }
        return SPIN_NONE;
    }

    /**
     * Checks if a corner position is filled by a block or boundary
     * @param x the x coordinate
     * @param y the y coordinate
     * @return true if boundary or occupied cell
     */
    private boolean isCornerFilled(int x, int y) {
        // Treat boundaries as FILLED (this is the key to T-Spin wall-tucks)
        if (x < 0 || x >= Board.COLS || y < 0 || y >= Board.ROWS) {
            return true;
        }
        // Only return true if there is an actual block on the board
        return board.getBoard()[y][x] != 0;
    }

    /**
     * Swaps current piece with held piece
     */
    void hold() {

        if (holdUsed) return;
        holdUsed = true;

        int incoming = currentPiece.getType();
        if (holdType == -1) {
        //save piece type
            holdType = incoming;
            spawnPiece();
        } else {
            //swap piece type
            int swapOut = holdType;
            holdType    = incoming;
            currentPiece = new Piece(swapOut);
        }
        lockTimestamp = 0;
        pendingSpin   = SPIN_NONE;
    }

    /**
     * Instantly drops piece to bottom and locks it
     */
    void hardDrop() {
        while (currentPiece.tryMove(0, 1, board));
        playSound("hard_drop_fixed.wav");
        lockTimestamp  = 0;
        lockResetCount = 0;

        lockPiece();
    }

    /**
     * Locks the current piece onto the board and spawns next piece
     */
    void lockPiece() {
        lastSpin = detectSpin(currentPiece);
        //debug satements
        if      (lastSpin == SPIN_MINI) {System.out.println("\n[!] MINI SPIN (type " + currentPiece.getType() + ")") ;
            board.printBoardDebug();}
        else if (lastSpin == SPIN_FULL) System.out.println("\n[!] FULL SPIN (type " + currentPiece.getType() + ")");
        else                            System.out.println("\n[!] No spin");
        //reset flags don't reset lastSpin
        lastActionWasRotation = false;
        lastRotationType = -1;
        pendingSpin = SPIN_NONE;
        //calls to calculate score
        int linesCleared = board.placePiece(currentPiece);
        calculateScore(linesCleared, lastSpin);
        spawnPiece();
    }

    /**
     * Calculates and adds score for line clears and spins
     * @param lines number of lines cleared
     * @param spin spin type applied (none, mini, or full)
     */
    private void calculateScore(int lines, int spin) {
        boolean isSpecialClear = (lines == 4) || (spin != SPIN_NONE && lines > 0);

        if (lines == 0) {
            if      (spin == SPIN_FULL) score += 400L * level;
            else if (spin == SPIN_MINI) score += 100L * level;
            combo = -1;
            return;
        }
        if (spin != SPIN_NONE) {
            playSound("clearspin_fixed.wav");  // spin clear
        } else if (lines == 4) {
            playSound("clearquad_fixed.wav");  // Tetris (4-line clear)
        } else {
            playSound("clearline_fixed.wav");  // 1-3 line clear
        }
        combo++;
        score += 50L * combo * level;

        long base;
        if (spin == SPIN_FULL) {
            long[] tSpinScores = {800, 1200, 1600, 1600};
            base = tSpinScores[Math.min(lines, 4) - 1];
        } else if (spin == SPIN_MINI) {
            long[] miniScores = {200, 400, 400, 400};
            base = miniScores[Math.min(lines, 4) - 1];
        } else {
            long[] normalScores = {100, 300, 500, 800};
            base = normalScores[Math.min(lines, 4) - 1];
        }

        boolean wasBackToBack = backToBack;
        if (isSpecialClear) {
            if (backToBack) base = base + base / 2;
            backToBack = true;
        } else {
            backToBack = false;
        }
        score += base * level;

        if (board.isBoardEmpty()) {
            playSound("allclear_fixed.wav");
            long pcBonus;
            if (wasBackToBack && lines == 4) {
                pcBonus = 3200L;
            } else {
                long[] pcScores = {800, 1200, 1800, 2000};
                pcBonus = pcScores[Math.min(lines, 4) - 1];
            }
            score += pcBonus * level;
            System.out.println("[!] PERFECT CLEAR  +" + (pcBonus * level));
        }

        totalLinesCleared += lines;
        level = totalLinesCleared / 10 + 1;

        System.out.printf("[score] lines=%d spin=%d b2b=%b combo=%d  →  total=%d  lvl=%d%n",
                lines, spin, wasBackToBack, combo, score, level);
    }

    /**
     * Spawns a new piece at the top of the board
     */
    void spawnPiece() {
        lockResetCount = 0;
        holdUsed       = false;
        currentPiece   = new Piece(bag.remove(0));
        if (bag.size() < 7) refillBag();
        if (board.isCollision(currentPiece)) isRunning = false;
    }

    /**
     * Applies gravity to current piece and manages lock delay
     */
    void applyGravity() {
        long now = System.nanoTime() / 1_000_000;
        if (now - lastFallTime < GRAVITY_INTERVAL) return;
        lastFallTime = now;

        boolean moved = currentPiece.tryMove(0, 1, board);
        if (!moved) {
            if (lockTimestamp == 0) {
                lockTimestamp = now;
            } else if (now - lockTimestamp >= LOCK_DELAY || lockResetCount >= MAX_LOCK_RESETS) {
                lockPiece();
                lockTimestamp  = 0;
                lockResetCount = 0;
            }
        } else {
            if (lockTimestamp != 0 && lockResetCount < MAX_LOCK_RESETS) {
                lockTimestamp = now;
                lockResetCount++;
            }
            if (lockTimestamp == 0) lockResetCount = 0;
        }
    }

    /**
     * Main game update loop called every frame
     */
    void update() {
        if (!isRunning) resetGame();
        handleInput();
        applyGravity();
        gamePanel.repaint();
        if (queuePanel != null) queuePanel.repaint();
        if (holdPanel  != null) holdPanel.repaint();

    }

    public Board getBoard()        { return board; }
    public Piece getCurrentPiece() { return currentPiece; }
    public long getScore()         { return score; }
    public int getLevel()          { return level; }
    public int getLines()          { return totalLinesCleared; }
}