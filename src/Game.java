import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.File;

public class Game {
    private Board board;
    private Piece currentPiece;
    private InputHandler input;
    private GamePanel gamePanel;
    private javax.swing.Timer gameTimer;

    private Runnable onExit;

    // button flags
    private boolean rotateKeyHeld   = false;
    private boolean holdKeyHeld     = false;
    private boolean hardDropKeyHeld = false;
    private boolean rotate180KeyHeld   = false;
    private boolean escKeyHeld      = false;   // NEW

    // spin flags
    private static final int SPIN_NONE = 0;
    private static final int SPIN_MINI = 1;
    private static final int SPIN_FULL = 2;
    private int     lastSpin              = SPIN_NONE;
    private boolean lastActionWasRotation = false;
    private int     pendingSpin           = SPIN_NONE;

    // 7-bag
    private List<Integer> bag;

    // DAS/ARR — read from Options on each resetGame()
    private long dasTimestamp;
    private long arrTimestamp;
    private int  DAS_DELAY = 119;
    private int  ARR_RATE  = 16;

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

    public void setHoldPanel(HoldPanel hp)      { this.holdPanel  = hp; }
    public int  getHoldType()                   { return holdType; }
    public boolean isHoldUsed()                 { return holdUsed; }
    public void setQueuePanel(NextQueuePanel qp) { this.queuePanel = qp; }
    private void loadSettingsFromXml() {
        // Default values
        int loadedDas = 160;
        int loadedArr = 30;
        int loadedVol = 50;

        try {
            File xmlFile = new File("config.xml");
            if (xmlFile.exists()) {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(xmlFile);
                doc.getDocumentElement().normalize();

                // Extract values as Strings, then parse to int
                loadedDas = Integer.parseInt(doc.getElementsByTagName("DAS").item(0).getTextContent());
                loadedArr = Integer.parseInt(doc.getElementsByTagName("ARR").item(0).getTextContent());
                loadedVol = Integer.parseInt(doc.getElementsByTagName("Volume").item(0).getTextContent());

                System.out.println("Configuration loaded successfully.");
            } else {
                System.out.println("config.xml not found, using defaults.");
            }
        } catch (Exception e) {
            System.out.println("Error reading config.xml, using defaults: " + e.getMessage());
        }

        // Apply the loaded (or default) values to the static Options class
        //may need to add setters in Options.java for this
        Options.setDAS(loadedDas);
        Options.setARR(loadedArr);
        Options.setVolume(loadedVol);
    }
    void start(GamePanel gamePanel, Runnable onExit) {
        this.gamePanel = gamePanel;
        this.onExit    = onExit;
        input = new InputHandler();
        input.getinputfromkeyboard(gamePanel);
        resetGame();
        gameTimer = new Timer(16, e -> update());
        gameTimer.start();
    }

    void stop() {
        isRunning = false;
        if (gameTimer != null) gameTimer.stop();
    }

    void resetGame() {
        loadSettingsFromXml();
        DAS_DELAY = Options.getDAS();
        ARR_RATE  = Options.getARR();

        lastFallTime = System.nanoTime() / 1_000_000;
        board    = new Board();
        holdType = -1;
        bag      = new ArrayList<>();
        refillBag();
        spawnPiece();
        isRunning = true;
    }

    void refillBag() {
        while (bag.size() < 14) {
            List<Integer> newBag = new ArrayList<>();
            for (int i = 0; i < 7; i++) newBag.add(i);
            Collections.shuffle(newBag);
            bag.addAll(newBag);
        }
    }

    public List<Integer> getNextQueue(int count) {
        return bag.subList(0, Math.min(count, bag.size()));
    }

    void handleInput() {
        long now = System.nanoTime() / 1_000_000;


        if (input.isKeyPressed(KeyEvent.VK_ESCAPE)) {
            if (!escKeyHeld) {
                escKeyHeld = true;
                stop();
                if (onExit != null) SwingUtilities.invokeLater(onExit);
            }
            return; // consume remaining input this frame
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

            if (dasTimestamp == 0) {
                dasTimestamp = now;
                arrTimestamp = now;
                if (currentPiece.tryMove(dx, 0, board)) {
                    lockTimestamp         = 0;
                    lastActionWasRotation = false;
                    pendingSpin           = SPIN_NONE;
                }
            } else if (now - dasTimestamp >= DAS_DELAY) {
                if (ARR_RATE == 0) {
                    while (currentPiece.tryMove(dx, 0, board)) { lockTimestamp = 0; }
                    lastActionWasRotation = false;
                    pendingSpin           = SPIN_NONE;
                } else {
                    while (now - arrTimestamp >= ARR_RATE) {
                        if (currentPiece.tryMove(dx, 0, board)) {
                            lockTimestamp         = 0;
                            lastActionWasRotation = false;
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

        if (input.isKeyPressed(KeyEvent.VK_DOWN)) {
            if (currentPiece.tryMove(0, 1, board)) {
                lastActionWasRotation = false;
                pendingSpin           = SPIN_NONE;
            }
        }

        if (input.isKeyPressed(KeyEvent.VK_UP) || input.isKeyPressed(KeyEvent.VK_Z)) {
            if (!rotateKeyHeld) {
                boolean rotated = input.isKeyPressed(KeyEvent.VK_UP)
                        ? currentPiece.rotateCW(board)
                        : currentPiece.rotateCCW(board);
                if (rotated) {
                    lastActionWasRotation = true;
                    pendingSpin           = detectSpin(currentPiece);
                }
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
                    pendingSpin           = detectSpin(currentPiece); // Evaluates All-Spins/T-Spins
                }
                rotate180KeyHeld = true; // Prevents repeating every frame when held
            }
        } else {
            rotate180KeyHeld = false;
        }
    }

    // Inside Game.java

    int detectSpin(Piece piece) {
        if (!lastActionWasRotation) return SPIN_NONE;

        int type = piece.getType();
        int px = piece.getX();
        int py = piece.getY();
        int[][] shape = piece.getShape();

        // 1. TETR.IO Immobility Check: Can the piece move left, right, or up?
        boolean canMoveLeft  = !board.isCollision(shape, px - 1, py);
        boolean canMoveRight = !board.isCollision(shape, px + 1, py);
        boolean canMoveUp    = !board.isCollision(shape, px, py - 1);
        boolean isImmobile   = !canMoveLeft && !canMoveRight && !canMoveUp;

        // --- ALL-SPINS FOR NON-T-PIECES ---
        // In TETR.IO, any non-T piece that is completely immobile upon rotation
        // satisfies an All-Spin. These are scored as MINIs.
        if (type != 1) {
            return isImmobile ? SPIN_MINI : SPIN_NONE;
        }

        // --- T-PIECE SPIN DETECTION ---
        // 2. The 3-Corner Rule (T-piece matrix centers around offset [1,1])
        boolean tl = isOccupied(px + 0, py + 0); // Top-Left
        boolean tr = isOccupied(px + 2, py + 0); // Top-Right
        boolean bl = isOccupied(px + 0, py + 2); // Bottom-Left
        boolean br = isOccupied(px + 2, py + 2); // Bottom-Right

        int filledCorners = (tl ? 1 : 0) + (tr ? 1 : 0) + (bl ? 1 : 0) + (br ? 1 : 0);

        // If fewer than 3 corners are filled, it cannot be any type of T-Spin
        if (filledCorners < 3) return SPIN_NONE;

        // 3. Kick Upgrade Exception:
        // If the rotation utilized the 5th SRS kick test (index 4), it's automatically a FULL T-Spin
        if (piece.getLastUsedKickIndex() == 4) {
            return SPIN_FULL;
        }

        // 4. Differentiate Full vs Mini based on "pointing" side corners
        int rot = piece.getRotation();
        boolean front0, front1;

        if (rot == 0) {         // Pointing UP
            front0 = tl; front1 = tr;
        } else if (rot == 1) {  // Pointing RIGHT
            front0 = tr; front1 = br;
        } else if (rot == 2) {  // Pointing DOWN
            front0 = bl; front1 = br;
        } else {                // Pointing LEFT
            front0 = tl; front1 = bl;
        }

        // If both corners on the pointing side are filled -> Full T-Spin
        if (front0 && front1) {
            return SPIN_FULL;
        }

        // Otherwise, if 3 corners are met but pointing side is open -> T-Spin Mini
        return SPIN_MINI;
    }

    // Helper method to see if a cell (or wall/floor boundary) is occupied
    private boolean isOccupied(int x, int y) {
        // Walls and floors count as occupied blocks for corner checks
        if (x < 0 || x >= Board.COLS || y >= Board.ROWS) {
            return true;
        }
        if (y < 0) return false; // Above the screen buffer is free air
        return board.getBoard()[y][x] > 0;
    }



    void hold() {
        if (holdUsed) return;
        holdUsed = true;

        int incoming = currentPiece.getType();
        if (holdType == -1) {
            holdType = incoming;
            spawnPiece();
        } else {
            int swapOut = holdType;
            holdType    = incoming;
            currentPiece = new Piece(swapOut);
        }
        lockTimestamp = 0;
        pendingSpin   = SPIN_NONE;
    }

    void hardDrop() {
        while (currentPiece.tryMove(0, 1, board));
        lockTimestamp  = 0;
        lockResetCount = 0;
        lockPiece();
    }

    void lockPiece() {
        lastSpin = pendingSpin;
        if      (lastSpin == SPIN_MINI) System.out.println("\n[!] MINI SPIN (type " + currentPiece.getType() + ")");
        else if (lastSpin == SPIN_FULL) System.out.println("\n[!] FULL SPIN (type " + currentPiece.getType() + ")");
        else                            System.out.println("\n[!] No spin");

        lastActionWasRotation = false;
        pendingSpin           = SPIN_NONE;
        board.placePiece(currentPiece);
        spawnPiece();
    }

    void spawnPiece() {
        lockResetCount = 0;
        holdUsed       = false;
        currentPiece   = new Piece(bag.remove(0));
        if (bag.size() < 7) refillBag();
        if (board.isCollision(currentPiece)) isRunning = false;
    }

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
            lastActionWasRotation = false;
            if (lockTimestamp != 0 && lockResetCount < MAX_LOCK_RESETS) {
                lockTimestamp = now;
                lockResetCount++;
            }
            if (lockTimestamp == 0) lockResetCount = 0;
        }
    }

    void update() {
        if (!isRunning) return;
        handleInput();
        applyGravity();
        gamePanel.repaint();
        if (queuePanel != null) queuePanel.repaint();
        if (holdPanel  != null) holdPanel.repaint();
    }

    public Board getBoard()        { return board; }
    public Piece getCurrentPiece() { return currentPiece; }
}