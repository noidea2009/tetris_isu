import javax.swing.*;
import java.awt.*;
import java.util.function.IntConsumer;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.*;
import java.io.File;

public class Options extends JPanel {

    // Static settings — Game.java reads these on resetGame()
    private static int das    ;
    private static int arr    ;
    private static int sdf    ;  // 0 ms = infinite/instant SDF down drop in Game
    private static int volume ;

    public static int getDAS()    { return das; }
    public static int getARR()    { return arr; }
    public static int getSDF()    { return sdf; }
    public static int getVolume() { return volume; }

    public static void setDAS(int val)    { das = val; }
    public static void setARR(int val)    { arr = val; }
    public static void setSDF(int val)    { sdf = val; }
    public static void setVolume(int val) { volume = val; }

    public Options(Runnable onBack) {
        setOpaque(false);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets  = new Insets(14, 24, 14, 24);
        gbc.gridx   = 0;
        gbc.fill    = GridBagConstraints.HORIZONTAL;

        // Title
        gbc.gridy = 0; gbc.gridwidth = 2;
        add(styledLabel("OPTIONS", 30), gbc);
        gbc.gridwidth = 1;

        // Sliders (0 ms on SDF will trigger the instant-drop logic)
        addSliderRow("DAS (ms)",  das,    0, 300, gbc, 1, v -> das    = v);
        addSliderRow("ARR (ms)",  arr,    0, 100, gbc, 2, v -> arr    = v);
        addSliderRow("SDF (ms)",  sdf,    0, 200, gbc, 3, v -> sdf    = v);
        addSliderRow("Volume",    volume, 0, 100, gbc, 4, v -> volume = v);

        // Buttons
        JButton back = styledButton("BACK");
        back.addActionListener(e -> onBack.run());

        JButton saveButton = styledButton("SAVE");
        saveButton.addActionListener(e -> saveSettingsToXml());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setBackground(Color.BLACK);
        buttonPanel.add(saveButton);
        buttonPanel.add(back);

        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        add(buttonPanel, gbc);
    }

    private void addSliderRow(String label, int initial, int min, int max,
                              GridBagConstraints gbc, int row, IntConsumer onChange) {
        // Label column
        gbc.gridy  = row;
        gbc.gridx  = 0;
        gbc.fill   = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        add(styledLabel(label, 14), gbc);

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

    private void saveSettingsToXml() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();

            // Root element matched to clean flat file configurations
            Element root = doc.createElement("DataConfiguration");
            doc.appendChild(root);

            // Directly append targets to maintain tag lookups seamlessly
            createElement(doc, root, "DAS", String.valueOf(das));
            createElement(doc, root, "ARR", String.valueOf(arr));
            createElement(doc, root, "SDF", String.valueOf(sdf));
            createElement(doc, root, "Volume", String.valueOf(volume));

            // Write XML file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File("config.xml"));
            transformer.transform(source, result);

            JOptionPane.showMessageDialog(this, "Settings Saved Successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving settings: " + e.getMessage());
        }
    }

    private void createElement(Document doc, Element parent, String tag, String value) {
        Element e = doc.createElement(tag);
        e.appendChild(doc.createTextNode(value));
        parent.appendChild(e);
    }
    public static void loadSettingsFromXml() {
        File configFile = new File("config.xml");

        // 1. If no file exists, just exit. The class fields will
        // hold their default values (e.g., 0 or whatever you set them to).
        if (!configFile.exists()) {
            System.out.println("No config file found; using program defaults.");
            return;
        }

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(configFile);
            doc.getDocumentElement().normalize();

            // 2. Use a helper to parse, providing the target field and a fallback default
            setDAS(parseSetting(doc, "DAS", 160));
            setARR(parseSetting(doc, "ARR", 30));
            setSDF(parseSetting(doc, "SDF", 50));
            setVolume(parseSetting(doc, "Volume", 50));

            System.out.println("Configuration successfully loaded.");
        } catch (Exception e) {
            System.err.println("Error parsing XML: " + e.getMessage());
            // Optionally: reset file if corrupted
        }
    }

    /**
     * Helper to extract an integer from a tag safely.
     */
    private static int parseSetting(Document doc, String tagName, int defaultValue) {
        NodeList nodes = doc.getElementsByTagName(tagName);
        if (nodes != null && nodes.getLength() > 0) {
            try {
                String val = nodes.item(0).getTextContent();
                return Integer.parseInt(val);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
}