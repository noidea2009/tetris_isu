import javax.swing.*;
import java.awt.*;
import java.util.function.IntConsumer;

public class Options extends JPanel {

    // Static settings — Game.java reads these on resetGame()
    private static int das    = 119;
    private static int arr    = 16;
    private static int volume = 50;  // placeholder; wire to audio system when added

    public static int getDAS()    { return das; }
    public static int getARR()    { return arr; }
    public static int getVolume() { return volume; }

    public Options(Runnable onBack) {
        setBackground(Color.BLACK);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets  = new Insets(14, 24, 14, 24);
        gbc.gridx   = 0;
        gbc.fill    = GridBagConstraints.HORIZONTAL;

        // Title
        gbc.gridy = 0; gbc.gridwidth = 2;
        add(styledLabel("OPTIONS", 30), gbc);
        gbc.gridwidth = 1;

        // Sliders
        addSliderRow("DAS (ms)",  das,    0, 300, gbc, 1, v -> das    = v);
        addSliderRow("ARR (ms)",  arr,    0, 100, gbc, 2, v -> arr    = v);
        addSliderRow("Volume",    volume, 0, 100, gbc, 3, v -> volume = v);

        // Back button
        JButton back = styledButton("BACK");
        back.addActionListener(e -> onBack.run());
        gbc.gridy = 4; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        add(back, gbc);
    }

    private void addSliderRow(String label, int initial, int min, int max,
                              GridBagConstraints gbc, int row, IntConsumer onChange) {
        // Label column
        gbc.gridy  = row;
        gbc.gridx  = 0;
        gbc.fill   = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        add(styledLabel(label, 14), gbc);

        // Value readout
        JLabel valueLabel = new JLabel(String.valueOf(initial));
        valueLabel.setForeground(Color.LIGHT_GRAY);
        valueLabel.setFont(new Font("Monospaced", Font.PLAIN, 14));
        valueLabel.setPreferredSize(new Dimension(36, 20));
        valueLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        // Slider
        JSlider slider = new JSlider(min, max, initial);
        slider.setBackground(Color.BLACK);
        slider.setForeground(Color.WHITE);
        slider.setPreferredSize(new Dimension(210, 30));
        slider.addChangeListener(e -> {
            int val = slider.getValue();
            onChange.accept(val);
            valueLabel.setText(String.valueOf(val));
        });

        JPanel row_panel = new JPanel(new BorderLayout(8, 0));
        row_panel.setBackground(Color.BLACK);
        row_panel.add(slider, BorderLayout.CENTER);
        row_panel.add(valueLabel, BorderLayout.EAST);

        gbc.gridx  = 1;
        gbc.fill   = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        add(row_panel, gbc);
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private static JLabel styledLabel(String text, int size) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setForeground(Color.WHITE);
        lbl.setFont(new Font("Arial", Font.BOLD, size));
        return lbl;
    }

    private static JButton styledButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 18));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(40, 40, 40));
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(160, 44));
        return btn;
    }
}