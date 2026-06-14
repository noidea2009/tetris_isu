import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;

/**
 * Made by Junjit Chang
 * Manages keyboard input state using Java's Key Bindings API.
 * This class tracks which keys are currently held down, preventing
 * issues common with standard KeyListeners like ghosting or stuck keys.
 */
public class InputHandler {

    /** Tracks the set of currently depressed keys. */
    // Q: why hashets? A: in my previous project making tetris in python I used a hashset to store inputs so it should also work here
    private Set<Integer> keyStates = new HashSet<>();

    /**
     * Binds common game keys to the provided component.
     * @param component The JComponent (usually a JPanel) to bind keys to.
     */
    public void getinputfromkeyboard(JComponent component) {
        bindKey(component, "LEFT", KeyEvent.VK_LEFT);
        bindKey(component, "RIGHT", KeyEvent.VK_RIGHT);
        bindKey(component, "UP", KeyEvent.VK_UP);
        bindKey(component, "DOWN", KeyEvent.VK_DOWN);
        bindKey(component, "Z", KeyEvent.VK_Z);
        bindKey(component, "C", KeyEvent.VK_C);
        bindKey(component, "SPACE", KeyEvent.VK_SPACE);
        bindKey(component, "R", KeyEvent.VK_R);
        bindKey(component, "A", KeyEvent.VK_A);
        bindKey(component, "ESCAPE", KeyEvent.VK_ESCAPE);
    }

    /**
     * Maps a specific key code to pressed/released actions within the component's ActionMap.
     * @param component The target component.
     * @param key The string representation of the key.
     * @param keyCode The KeyEvent constant (e.g., KeyEvent.VK_LEFT).
     */
    private void bindKey(JComponent component, String key, int keyCode) {
        //This is important, else its a makeshift keylogger, and weird stuff happens to the memory
        InputMap inputMap = component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = component.getActionMap();

        // Register the press action
        inputMap.put(KeyStroke.getKeyStroke("pressed " + key), key + "_pressed");
        actionMap.put(key + "_pressed", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                keyStates.add(keyCode);
            }
        });

        // Register the release action to remove the key from the set
        inputMap.put(KeyStroke.getKeyStroke("released " + key), key + "_released");
        actionMap.put(key + "_released", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                keyStates.remove(keyCode);
            }
        });
    }

    /**
     * Checks if a key is currently being held down.
     * @param keyCode The KeyEvent constant to check.
     * @return true if the key is in the set of pressed keys, false otherwise.
     */
    public boolean isKeyPressed(int keyCode) {
        return keyStates.contains(keyCode);
    }
}