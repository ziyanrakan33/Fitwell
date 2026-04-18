package fitwell.ui.pro.consultant;

import fitwell.control.InventoryReportController;
import fitwell.ui.pro.components.ProTextBlocks;
import fitwell.ui.pro.theme.FWTheme;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import fitwell.ui.pro.theme.FWUi;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

public class EquipmentInventoryReportProDialog extends JDialog {

    private final InventoryReportController inventoryReportController;

    private final JSpinner yearSpinner = new JSpinner(
            new SpinnerNumberModel(LocalDate.now().getYear(), 2000, 2100, 1)
    );

    private final JTextArea xmlArea = new JTextArea();

    public EquipmentInventoryReportProDialog(Window owner,
                                             InventoryReportController inventoryReportController) {
        super(owner, "Equipment Inventory Report PRO", ModalityType.APPLICATION_MODAL);
        this.inventoryReportController = inventoryReportController;

        buildUi();

        setSize(920, 650);
        setMinimumSize(new Dimension(760, 520));
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void buildUi() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBackground(FWTheme.DASH_BG);
        root.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        setContentPane(root);

        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildCenter(), BorderLayout.CENTER);
    }

    private JComponent buildHeader() {
        JPanel header = FWUi.cardPanel(FWTheme.CARD_BG, 14);
        header.setLayout(new BorderLayout(12, 8));

        JLabel title = new JLabel("Equipment Inventory XML Report");
        title.setForeground(FWTheme.TEXT_PRIMARY);
        title.setFont(FWTheme.FONT_H2);

        JLabel sub = new JLabel("Generate XML preview for a selected year via InventoryReportController");
        sub.setForeground(FWTheme.TEXT_SECONDARY);
        sub.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JPanel left = new JPanel(new GridLayout(2, 1, 0, 4));
        left.setOpaque(false);
        left.add(title);
        left.add(sub);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        styleSpinner(yearSpinner);

        JButton generateBtn = FWUi.primaryButton("Generate XML");
        generateBtn.addActionListener(e -> onGenerateXml());

        JButton saveFileBtn = FWUi.ghostDarkButton("Save to File");
        saveFileBtn.addActionListener(e -> onSaveToFile());

        JButton exportBtn = FWUi.ghostDarkButton("Send To SwiftFit");
        exportBtn.addActionListener(e -> onExportXml());

        JButton closeBtn = FWUi.ghostDarkButton("Close");
        closeBtn.addActionListener(e -> dispose());

        right.add(label("Year"));
        right.add(yearSpinner);
        right.add(generateBtn);
        right.add(saveFileBtn);
        right.add(exportBtn);
        right.add(closeBtn);

        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);

        return header;
    }

    private JComponent buildCenter() {
        JPanel center = FWUi.cardPanel(FWTheme.CARD_BG, 14);
        center.setLayout(new BorderLayout(8, 8));

        JLabel previewLbl = new JLabel("XML Preview");
        previewLbl.setForeground(FWTheme.TEXT_PRIMARY);
        previewLbl.setFont(FWTheme.FONT_H2);

        xmlArea.setEditable(false);
        xmlArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        xmlArea.setLineWrap(false);
        xmlArea.setWrapStyleWord(false);
        xmlArea.setText("Click 'Generate XML' to preview the equipment inventory report...");

        JScrollPane sp = ProTextBlocks.darkScrollableTextArea(xmlArea);

        center.add(previewLbl, BorderLayout.NORTH);
        center.add(sp, BorderLayout.CENTER);
        return center;
    }

    private void onGenerateXml() {
        try {
            int year = ((Number) yearSpinner.getValue()).intValue();
            String xml = inventoryReportController.generateInventoryReportXML(year);
            xmlArea.setText(xml == null ? "" : xml);
            xmlArea.setCaretPosition(0);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Failed to generate XML:\n" + ex.getMessage(),
                    "Report Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void onSaveToFile() {
        String xml = xmlArea.getText();
        if (xml == null || xml.isBlank() || xml.startsWith("Click")) {
            JOptionPane.showMessageDialog(this, "Generate the XML first.", "No Data", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int year = ((Number) yearSpinner.getValue()).intValue();
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Inventory XML");
        chooser.setSelectedFile(new File("inventory_report_" + year + ".xml"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try (PrintWriter pw = new PrintWriter(new FileWriter(chooser.getSelectedFile()))) {
            pw.print(xml);
            JOptionPane.showMessageDialog(this, "Saved to: " + chooser.getSelectedFile().getAbsolutePath(),
                    "Saved", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Save failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onExportXml() {
        try {
            int year = ((Number) yearSpinner.getValue()).intValue();
            inventoryReportController.exportAnnualReportToSwiftFit(year);
            JOptionPane.showMessageDialog(this, "Annual inventory report sent to SwiftFit.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Failed to export XML:\n" + ex.getMessage(),
                    "Export Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private JLabel label(String txt) {
        JLabel l = new JLabel(txt);
        l.setForeground(FWTheme.TEXT_SECONDARY);
        l.setFont(new Font("SansSerif", Font.BOLD, 12));
        return l;
    }

    private void styleSpinner(JSpinner sp) {
        sp.setPreferredSize(new Dimension(90, 36));
        sp.setBorder(BorderFactory.createLineBorder(FWTheme.BORDER, 1, true));

        JComponent editor = sp.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JFormattedTextField tf = ((JSpinner.DefaultEditor) editor).getTextField();
            tf.setBackground(FWTheme.CARD_BG);
            tf.setForeground(FWTheme.TEXT_PRIMARY);
            tf.setCaretColor(FWTheme.TEXT_PRIMARY);
            tf.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        }
    }
}