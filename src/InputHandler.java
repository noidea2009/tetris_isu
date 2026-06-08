import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;

public class InputHandler {

    private Set<Integer> keyStates = new HashSet<>(); //hashset avoids multiple inputs

    public void getinputfromkeyboard(JComponent component) {
        bindKey(component, "LEFT", KeyEvent.VK_LEFT);
        bindKey(component, "RIGHT", KeyEvent.VK_RIGHT);
        bindKey(component, "UP", KeyEvent.VK_UP);
        bindKey(component, "DOWN", KeyEvent.VK_DOWN);
        bindKey(component, "Z", KeyEvent.VK_Z);
        bindKey(component, "C", KeyEvent.VK_C);
        bindKey(component,"SPACE", KeyEvent.VK_SPACE);
        bindKey(component,"R",KeyEvent.VK_R);
        bindKey(component,"A",KeyEvent.VK_A);
        bindKey(component,"ESCAPE",KeyEvent.VK_ESCAPE);
    }

    private void bindKey(JComponent component, String key, int keyCode) {

        InputMap inputMap = component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = component.getActionMap();

        inputMap.put(KeyStroke.getKeyStroke("pressed " + key), key + "_pressed");// example console message: "LEFT pressed"

        actionMap.put(key + "_pressed", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                keyStates.add(keyCode);
            }
        });

        inputMap.put(KeyStroke.getKeyStroke("released " + key), key + "_released");
        actionMap.put(key + "_released", new AbstractAction() {
            @Override// WHAT DOES THIS DO????
            public void actionPerformed(ActionEvent e) {
                keyStates.remove(keyCode);
            }
        });
    }

    public boolean isKeyPressed(int keyCode) {
        return keyStates.contains(keyCode);
    }

    static void main(String[] args) {//test & example usage

        JFrame frame = new JFrame("Keyboard Test");
        JPanel panel = new JPanel();

        frame.add(panel);
        frame.setSize(400,300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        InputHandler handler = new InputHandler();
        handler.getinputfromkeyboard(panel);

        new Timer(100, e -> {

            if (handler.isKeyPressed(KeyEvent.VK_LEFT))
                System.out.println("LEFT pressed");

            if (handler.isKeyPressed(KeyEvent.VK_Z))
                System.out.println("Z pressed");

        }).start();
    }
}