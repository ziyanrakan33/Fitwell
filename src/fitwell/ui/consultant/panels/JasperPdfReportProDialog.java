package fitwell.ui.consultant.panels;

import fitwell.controller.JasperReportController;
import fitwell.ui.theme.FWTheme;
import fitwell.ui.theme.FWUi;

import javax.swing.*;
import java.awt.*;
import java.awt.Desktop;
import java.io.File;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneId;

@SuppressWarnings({"serial","this-escape"})
public class JasperPdfReportProDialog extends JDialog {

    private final JasperReportController jasperController = new JasperReportController();

    private final JSpinner fromDateSpinner = new JSpinner(new SpinnerDateModel());
    private final JSpinner toDateSpinner = new JSpinner(new SpinnerDateModel());

    public JasperPdfReportProDialog(Window owner) {
        super(owner, "Generate PDF Report", ModalityType.APPLICATION_MODAL);

        buildUi();

        setSize(520, 280);
        setResizable(false);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void buildUi() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBackground(FWTheme.DASH_BG);
        root.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        setContentPane(root);

        JLabel title = new JLabel("Unregistered Trainees PDF Report");
        title.setForeground(FWTheme.TEXT_PRIMARY);
        title.setFont(FWTheme.FONT_H2);

        JComponent desc = new JLabel("<html><div style='width:420px;color:#A0AEC5;'>" +
                "Generate a PDF report of trainees who attended fewer than 7 classes in the selected period. " +
                "The PDF will be saved and opened with your default PDF viewer.</div></html>");
        desc.setFont(new Font("SansSerif", Font.PLAIN, 12));

        styleDateSpinner(fromDateSpinner);
        styleDateSpinner(toDateSpinner);

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 12));
        form.setOpaque(false);
        form.add(label("From"));
        form.add(fromDateSpinner);
        form.add(label("To"));
        form.add(toDateSpinner);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);

        JButton generateBtn = FWUi.primaryButton("Generate PDF");
        generateBtn.addActionListener(e -> generateAndOpenPdf());

        JButton closeBtn = FWUi.ghostDarkButton("Close");
        closeBtn.addActionListener(e -> dispose());

        buttons.add(generateBtn);
        buttons.add(closeBtn);

        JPanel north = new JPanel(new BorderLayout(0, 14));
        north.setOpaque(false);
        north.add(title, BorderLayout.NORTH);
        north.add(desc, BorderLayout.CENTER);
        north.add(form, BorderLayout.SOUTH);

        root.add(north, BorderLayout.NORTH);
        root.add(buttons, BorderLayout.SOUTH);
    }

    private void generateAndOpenPdf() {
        LocalDate from = toLocalDate((java.util.Date) fromDateSpinner.getValue());
        LocalDate to = toLocalDate((java.util.Date) toDateSpinner.getValue());
        LocalDate toExclusive = to.plusDays(1);

        if (from.isAfter(to)) {
            JOptionPane.showMessageDialog(this, "From date must be before or equal to To date.",
                    "Invalid Dates", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Path pdfPath = jasperController.unregisteredTraineesPdf(from, toExclusive);
            if (pdfPath != null && pdfPath.toFile().exists()) {
                File file = pdfPath.toFile();
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                    Desktop.getDesktop().open(file);
                    JOptionPane.showMessageDialog(this,
                            "PDF generated and opened:\n" + file.getAbsolutePath(),
                            "PDF Created", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "PDF generated at:\n" + file.getAbsolutePath() + "\n\n" +
                                    "Open this file manually with your PDF viewer.",
                            "PDF Created", JOptionPane.INFORMATION_MESSAGE);
                }
                dispose();
            } else if (pdfPath == null) {
                JOptionPane.showMessageDialog(this,
                        "PDF generation failed. Check that the JRXML template exists and JasperReports JARs are in the lib folder.",
                        "Report Error", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "PDF file was not created at:\n" + pdfPath,
                        "Report Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            Throwable t = ex.getCause() != null ? ex.getCause() : ex;
            String msg = t.getMessage();
            if (msg == null || msg.isEmpty()) msg = t.getClass().getSimpleName();
            JOptionPane.showMessageDialog(this,
                    "Failed to generate PDF:\n" + msg,
                    "Report Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private LocalDate toLocalDate(java.util.Date value) {
        return value.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private JLabel label(String txt) {
        JLabel l = new JLabel(txt);
        l.setForeground(FWTheme.TEXT_SECONDARY);
        l.setFont(new Font("SansSerif", Font.BOLD, 12));
        return l;
    }

    private void styleDateSpinner(JSpinner sp) {
        sp.setEditor(new JSpinner.DateEditor(sp, "yyyy-MM-dd"));
        sp.setPreferredSize(new Dimension(130, 36));
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
