package fitwell.ui.pro.consultant;

import fitwell.control.LowAttendanceReportService;
import fitwell.entity.LowAttendanceRecord;
import fitwell.ui.pro.components.ProTextBlocks;
import fitwell.ui.pro.theme.FWTheme;
import fitwell.ui.pro.theme.FWUi;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

public class UnregisteredClassReportProDialog extends JDialog {
    private final LowAttendanceReportService lowAttendanceReportService;

    private final JSpinner fromDateSpinner = new JSpinner(new SpinnerDateModel());
    private final JSpinner toDateSpinner = new JSpinner(new SpinnerDateModel());
    private final JSpinner minClassesSpinner = new JSpinner(new SpinnerNumberModel(7, 0, 999, 1));

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"#", "Name", "Phone", "Email", "Attended Classes", "Contact Method"}, 0) {
        @Override public boolean isCellEditable(int row, int col) { return false; }
    };
    private final JTable resultTable = new JTable(tableModel);
    private final JLabel summaryLabel = new JLabel("Generate a report to see results.");
    private List<LowAttendanceRecord> lastRecords = List.of();

    public UnregisteredClassReportProDialog(Window owner, LowAttendanceReportService lowAttendanceReportService) {
        super(owner, "Low Attendance Report", ModalityType.APPLICATION_MODAL);
        this.lowAttendanceReportService = lowAttendanceReportService;
        buildUi();
        setSize(960, 680);
        setMinimumSize(new Dimension(780, 520));
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void buildUi() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBackground(FWTheme.DASH_BG);
        root.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        setContentPane(root);
        root.add(buildTopCard(), BorderLayout.NORTH);
        root.add(buildResultCard(), BorderLayout.CENTER);
    }

    private JComponent buildTopCard() {
        JPanel card = FWUi.cardPanel(FWTheme.CARD_BG, 14);
        card.setLayout(new BorderLayout(12, 10));

        JLabel title = new JLabel("Low Attendance Report");
        title.setForeground(FWTheme.TEXT_PRIMARY);
        title.setFont(FWTheme.FONT_H2);

        JComponent desc = ProTextBlocks.description(
                "Lists trainees who attended fewer than N classes in a date range.\n" +
                "Use 'Export CSV' to download the results for further analysis."
        );

        JPanel left = new JPanel(new BorderLayout(0, 6));
        left.setOpaque(false);
        left.add(title, BorderLayout.NORTH);
        left.add(desc, BorderLayout.CENTER);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        styleDateSpinner(fromDateSpinner);
        styleDateSpinner(toDateSpinner);
        styleNumberSpinner(minClassesSpinner, 70);

        JButton generateBtn = FWUi.primaryButton("Generate");
        generateBtn.addActionListener(e -> generateReport());
        JButton exportBtn = FWUi.ghostDarkButton("Export CSV");
        exportBtn.addActionListener(e -> exportCsv());
        JButton closeBtn = FWUi.ghostDarkButton("Close");
        closeBtn.addActionListener(e -> dispose());

        right.add(label("From")); right.add(fromDateSpinner);
        right.add(label("To")); right.add(toDateSpinner);
        right.add(label("Min Classes")); right.add(minClassesSpinner);
        right.add(generateBtn); right.add(exportBtn); right.add(closeBtn);

        card.add(left, BorderLayout.CENTER);
        card.add(right, BorderLayout.EAST);
        return card;
    }

    private JComponent buildResultCard() {
        JPanel card = FWUi.cardPanel(FWTheme.CARD_BG, 14);
        card.setLayout(new BorderLayout(8, 8));

        JLabel title = new JLabel("Report Results");
        title.setForeground(FWTheme.TEXT_PRIMARY);
        title.setFont(FWTheme.FONT_H2);

        configureTable();
        JScrollPane sp = new JScrollPane(resultTable);
        sp.setBackground(FWTheme.CARD_BG);
        sp.getViewport().setBackground(FWTheme.CARD_BG);
        sp.setBorder(BorderFactory.createLineBorder(FWTheme.BORDER, 1));

        summaryLabel.setForeground(FWTheme.TEXT_SECONDARY);
        summaryLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        summaryLabel.setBorder(new EmptyBorder(4, 0, 0, 0));

        card.add(title, BorderLayout.NORTH);
        card.add(sp, BorderLayout.CENTER);
        card.add(summaryLabel, BorderLayout.SOUTH);
        return card;
    }

    private void configureTable() {
        resultTable.setRowHeight(30);
        resultTable.setBackground(FWTheme.CARD_BG);
        resultTable.setForeground(FWTheme.TEXT_PRIMARY);
        resultTable.setSelectionBackground(new Color(34, 50, 83));
        resultTable.setSelectionForeground(FWTheme.TEXT_PRIMARY);
        resultTable.setGridColor(FWTheme.BORDER);
        resultTable.getTableHeader().setBackground(FWTheme.SIDEBAR_BG);
        resultTable.getTableHeader().setForeground(FWTheme.TEXT_SECONDARY);
        resultTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        resultTable.setFont(new Font("SansSerif", Font.PLAIN, 13));
        resultTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        resultTable.getColumnModel().getColumn(1).setPreferredWidth(160);
        resultTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        resultTable.getColumnModel().getColumn(3).setPreferredWidth(200);
        resultTable.getColumnModel().getColumn(4).setPreferredWidth(130);
        resultTable.getColumnModel().getColumn(5).setPreferredWidth(130);
    }

    private void generateReport() {
        LocalDate from = toLocalDate((java.util.Date) fromDateSpinner.getValue());
        LocalDate to = toLocalDate((java.util.Date) toDateSpinner.getValue());
        int threshold = ((Number) minClassesSpinner.getValue()).intValue();

        lastRecords = lowAttendanceReportService.generate(from, to, threshold);
        tableModel.setRowCount(0);
        int row = 1;
        for (LowAttendanceRecord rec : lastRecords) {
            tableModel.addRow(new Object[]{
                    row++,
                    rec.getTrainee().fullName(),
                    rec.getTrainee().getPhone(),
                    rec.getTrainee().getEmail(),
                    rec.getAttendanceCount(),
                    rec.getPreferredMethod()
            });
        }
        summaryLabel.setText(lastRecords.isEmpty()
                ? "No trainees matched the criteria for the selected period."
                : "Found " + lastRecords.size() + " trainee(s) with fewer than "
                        + threshold + " attended classes between " + from + " and " + to + ".");
    }

    private void exportCsv() {
        if (lastRecords.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Generate a report first.", "No Data", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Report as CSV");
        chooser.setSelectedFile(new File("low_attendance_report.csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File file = chooser.getSelectedFile();
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            pw.println("#,Name,Phone,Email,Attended Classes,Contact Method");
            int i = 1;
            for (LowAttendanceRecord rec : lastRecords) {
                pw.printf("%d,\"%s\",\"%s\",\"%s\",%d,\"%s\"%n",
                        i++,
                        rec.getTrainee().fullName(),
                        rec.getTrainee().getPhone(),
                        rec.getTrainee().getEmail(),
                        rec.getAttendanceCount(),
                        rec.getPreferredMethod());
            }
            JOptionPane.showMessageDialog(this, "Saved to: " + file.getAbsolutePath(), "Export Complete", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
        sp.setPreferredSize(new Dimension(120, 36));
        sp.setBorder(BorderFactory.createLineBorder(FWTheme.BORDER, 1, true));
        JComponent editor = sp.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JFormattedTextField tf = ((JSpinner.DefaultEditor) editor).getTextField();
            tf.setBackground(FWTheme.CARD_BG); tf.setForeground(FWTheme.TEXT_PRIMARY);
            tf.setCaretColor(FWTheme.TEXT_PRIMARY); tf.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        }
    }

    private void styleNumberSpinner(JSpinner sp, int width) {
        sp.setPreferredSize(new Dimension(width, 36));
        sp.setBorder(BorderFactory.createLineBorder(FWTheme.BORDER, 1, true));
        JComponent editor = sp.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JFormattedTextField tf = ((JSpinner.DefaultEditor) editor).getTextField();
            tf.setBackground(FWTheme.CARD_BG); tf.setForeground(FWTheme.TEXT_PRIMARY);
            tf.setCaretColor(FWTheme.TEXT_PRIMARY); tf.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        }
    }
}
