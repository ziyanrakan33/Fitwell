package fitwell.ui.pro.consultant;

import fitwell.service.equipment.InspectionWorkflowService;
import fitwell.domain.equipment.Equipment;
import fitwell.domain.training.TrainingClass;
import fitwell.ui.pro.theme.FWTheme;
import fitwell.ui.pro.theme.FWUi;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ClassDetailsProDialog extends JDialog {

    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public ClassDetailsProDialog(Window owner, TrainingClass trainingClass, InspectionWorkflowService inspectionWorkflowService) {
        super(owner, "Class Details — " + trainingClass.getName(), ModalityType.APPLICATION_MODAL);

        setSize(820, 580);
        setMinimumSize(new Dimension(680, 480));
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBackground(FWTheme.DASH_BG);
        root.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        setContentPane(root);

        root.add(buildInfoCard(trainingClass), BorderLayout.NORTH);
        root.add(buildEquipmentCard(trainingClass.getClassId(), inspectionWorkflowService), BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setOpaque(false);
        JButton closeBtn = FWUi.primaryButton("Close");
        closeBtn.addActionListener(e -> dispose());
        footer.add(closeBtn);
        root.add(footer, BorderLayout.SOUTH);
    }

    private JComponent buildInfoCard(TrainingClass tc) {
        JPanel card = FWUi.cardPanel(FWTheme.CARD_BG, 16);
        card.setLayout(new GridLayout(0, 2, 10, 8));

        addRow(card, "Class ID", String.valueOf(tc.getClassId()));
        addRow(card, "Name", tc.getName());
        addRow(card, "Type", tc.getType() != null ? tc.getType() : "—");
        addRow(card, "Status", tc.getStatus() != null ? tc.getStatus().name() : "—");
        addRow(card, "Start Time", tc.getStartTime() != null ? tc.getStartTime().format(fmt) : "—");
        addRow(card, "End Time", tc.getEndTime() != null ? tc.getEndTime().format(fmt) : "—");
        addRow(card, "Max Participants", String.valueOf(tc.getMaxParticipants()));
        if (tc.getTips() != null && !tc.getTips().isEmpty()) {
            addRow(card, "Tips", String.join(" | ", tc.getTips()));
        }

        JPanel wrapper = FWUi.cardPanel(FWTheme.DASH_BG, 0);
        wrapper.setLayout(new BorderLayout(8, 8));
        JLabel title = new JLabel("Class Information");
        title.setForeground(FWTheme.TEXT_PRIMARY);
        title.setFont(FWTheme.FONT_H2);
        wrapper.add(title, BorderLayout.NORTH);
        wrapper.add(card, BorderLayout.CENTER);
        return wrapper;
    }

    private JComponent buildEquipmentCard(int classId, InspectionWorkflowService service) {
        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"Serial", "Name", "Category", "Qty", "Status", "Location", "Flagged"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        List<Equipment> equipmentList = service.findAssignedEquipment(classId);
        for (Equipment eq : equipmentList) {
            String loc = "—";
            if (eq.getLocation() != null) {
                loc = "X: " + eq.getLocation().getX() + ", Y: " + eq.getLocation().getY()
                        + ", Shelf: " + eq.getLocation().getShelfNumber();
            }
            model.addRow(new Object[]{
                    eq.getSerialNumber(),
                    eq.getName(),
                    eq.getCategory() != null ? eq.getCategory().name() : "—",
                    eq.getQuantity(),
                    eq.getStatus() != null ? eq.getStatus().name() : "—",
                    loc,
                    eq.isFlagged() ? "⚠ Yes" : "No"
            });
        }

        JTable table = new JTable(model);
        table.setBackground(FWTheme.CARD_BG);
        table.setForeground(FWTheme.TEXT_PRIMARY);
        table.setSelectionBackground(new Color(34, 50, 83));
        table.setSelectionForeground(FWTheme.TEXT_PRIMARY);
        table.setRowHeight(28);
        table.setGridColor(FWTheme.BORDER);

        JPanel wrapper = FWUi.cardPanel(FWTheme.DASH_BG, 0);
        wrapper.setLayout(new BorderLayout(8, 8));

        JLabel title = new JLabel("Assigned Equipment (" + equipmentList.size() + " items)");
        title.setForeground(FWTheme.TEXT_PRIMARY);
        title.setFont(FWTheme.FONT_H2);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.add(title, BorderLayout.WEST);

        if (equipmentList.isEmpty()) {
            JLabel empty = new JLabel("No equipment assigned to this class.");
            empty.setForeground(FWTheme.TEXT_SECONDARY);
            empty.setHorizontalAlignment(SwingConstants.CENTER);
            wrapper.add(topBar, BorderLayout.NORTH);
            wrapper.add(empty, BorderLayout.CENTER);
        } else {
            JScrollPane sp = new JScrollPane(table);
            sp.getViewport().setBackground(FWTheme.CARD_BG);
            wrapper.add(topBar, BorderLayout.NORTH);
            wrapper.add(sp, BorderLayout.CENTER);
        }
        return wrapper;
    }

    private void addRow(JPanel panel, String labelText, String value) {
        JLabel lbl = new JLabel("<html><b>" + labelText + ":</b></html>");
        lbl.setForeground(FWTheme.TEXT_SECONDARY);

        JLabel val = new JLabel(value);
        val.setForeground(FWTheme.TEXT_PRIMARY);

        panel.add(lbl);
        panel.add(val);
    }
}
