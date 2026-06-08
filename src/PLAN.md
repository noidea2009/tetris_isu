# Tetris Java (Swing) – Program Structure Overview

## 1. Project Objective

This project is a **modular, maintainable Tetris implementation in Java using Swing/AWT**, designed with:

- Clear separation of concerns
- Expandability (SRS, T-spins, UI features)
- Deterministic input handling (DAS/ARR)
- Event-driven architecture (Swing-compliant)

---

## 2. High-Level Architecture

The system follows a **layered architecture**:

Each layer is **independent but coordinated through `Game.java`**, which acts as the central controller.

---

## 3. Core Components

### 3.1 Main.java (Application Entry Point)

**Responsibility:**
- Initialize the application window (`JFrame`)
- Create and attach the rendering panel
- Initialize input handling
- Start the game loop

**Key Tasks:**
- Configure window size and properties
- Instantiate core systems
- Wire dependencies together

---

### 3.2 Game.java (Core Game Controller)

**Responsibility:**
- Central coordination of the game
- Game loop execution using `javax.swing.Timer`
- Timing logic (gravity, DAS, ARR)
- Piece spawning and progression
- Game state management (running, game over)

**Key Systems Managed:**
- Current active piece
- Next piece (7-bag system)
- Game timing
- Input interpretation

**Key Methods:**
- `start()` → starts the game loop
- `update()` → executed every frame (~60 FPS)
- `spawnPiece()` → handles new piece creation
- `resetGame()` → resets full game state

---

### 3.3 Board.java (Game State & Rules)

**Responsibility:**
- Maintain the grid (10×22)
- Handle collision detection
- Handle piece placement (locking)
- Handle line clearing

**Key Data:**
- `int[][] grid`

**Key Methods:**
- `isCollision(piece, x, y)`
- `placePiece(piece, x, y)`
- `clearLines()`

This class contains **all rule-based logic related to the board**.

---

### 3.4 Piece.java (Active Piece State)

**Responsibility:**
- Represent the currently active tetromino
- Track position and rotation
- Handle rotation logic (with collision validation)

**Key Data:**
- `type` (I, T, L, etc.)
- `rotation`
- `x, y` position

**Key Methods:**
- `getShape()`
- `rotateCW(Board)`
- `rotateCCW(Board)`

---

### 3.5 PieceData.java (Static Definitions)

**Responsibility:**
- Store all tetromino shapes and rotations

**Characteristics:**
- Static, immutable data
- No logic

**Example:**
- 4 rotation states per piece
- Coordinate definitions for each mino

---

### 3.6 InputHandler.java (Input Layer)

**Responsibility:**
- Capture keyboard input using Swing Key Bindings
- Maintain real-time key state

**Key Features:**
- Tracks pressed keys using a `Set<Integer>`
- Supports continuous input (required for DAS/ARR)

**Key Method:**
- `isKeyPressed(int keyCode)`

---

### 3.7 GamePanel.java (Rendering Layer)

**Responsibility:**
- Render the board and active piece
- Handle all drawing operations

**Implementation:**
- Extends `JPanel`
- Overrides `paintComponent(Graphics g)`

**Rendering Tasks:**
- Draw grid
- Draw placed blocks
- Draw active piece

---

## 4. Game Loop Architecture

The system uses an **event-driven loop**:

---

## 5. Input Handling (DAS / ARR)

**Approach:**
- Input state tracked continuously via `InputHandler`
- Timing handled in `Game.java` using `System.nanoTime()`

**Key Concepts:**
- **DAS (Delayed Auto Shift):** delay before horizontal repeat
- **ARR (Auto Repeat Rate):** speed of repeated movement

**Design Decision:**
- No external libraries used
- Fully integrated into the game loop for consistency

---

## 6. Data Flow

- `InputHandler` provides key states
- `Game` interprets input and updates logic
- `Board` and `Piece` store state
- `GamePanel` renders final result

---

## 7. 7-Bag Randomization System

**Responsibility:** `Game.java`

**Behavior:**
- Maintain a shuffled list of all 7 pieces
- Consume pieces one-by-one
- Refill and reshuffle when empty

**Purpose:**
- Ensures fair and consistent piece distribution

---

## 8. Responsibilities Summary

| Class         | Responsibility                    |
|--------------|----------------------------------|
| Main         | Application setup                |
| Game         | Game loop + coordination         |
| Board        | Grid + collision + line clearing |
| Piece        | Active piece logic               |
| PieceData    | Static shape definitions         |
| InputHandler | Keyboard input                   |
| GamePanel    | Rendering                        |

---

## 9. Development Guidelines

### Separation of Concerns
- Do not mix rendering with game logic
- Do not handle input inside `GamePanel`

### Threading
- All logic runs on Swing EDT via `Timer`
- Avoid additional threads

### Extensibility

Design should support future features:
- SRS (wall kicks)
- T-spin detection
- Hold piece
- Next queue display
- Scoring system

---

## 10. Implementation Roadmap (Team Execution)

### Phase 1 – Core Setup
- Window + panel + input
- Basic game loop

### Phase 2 – Gameplay Core
- Board + piece system
- Collision detection
- Gravity

### Phase 3 – Controls
- DAS / ARR
- Rotation

### Phase 4 – Gameplay Features
- Line clearing
- 7-bag system

### Phase 5 – Enhancements
- SRS
- UI improvements
- Scoring

---

## 11. Final Notes

This architecture is designed to be:

- Maintainable
- Testable
- Expandable
- Consistent with Swing best practices
