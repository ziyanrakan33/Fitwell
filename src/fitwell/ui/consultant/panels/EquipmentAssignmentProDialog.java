package fitwell.ui.consultant.panels;

import fitwell.controller.EquipmentAssignmentController;
import fitwell.ui.theme.FWTheme;
import fitwell.ui.theme.FWUi;

import javax.swing.*;
import java.awt.*;

public class EquipmentAssignmentProDialog extends JDialog {

    private final EquipmentAssignmentController controller;
    private final int classId;
    private final Integer consultantId;

    private final JTextField serialField = new JTextField();
    private final JSpinner qtySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 9999, 1));
    private final JTextArea notesArea = new JTextArea(4, 30);
    private final JTextArea resultArea = new JTextArea(8, 30);

    public EquipmentAssignmentProDialog(Window owner,
                                        EquipmentAssignmentController controller,
                                        int classId,
                                        Integer consultantId) {
        super(owner, "Assign Equipment to Class", ModalityType.APPLICATION_MODAL);
        this.controller = controller;
        this.classId = classId;
        this.consultantId = consultantId;

        buildUi();

        setSize(760, 560);
        setMinimumSize(new Dimension(680, 500));
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void buildUi() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBackground(FWTheme.DASH_BG);
        root.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        setContentPane(root);

        root.add(buildFormCard(), BorderLayout.NORTH);
        root.add(buildResultCard(), BorderLayout.CENTER);
    }

    private JComponent buildFormCard() {
        JPanel card = FWUi.cardPanel(FWTheme.CARD_BG, 16);
        card.setLayout(new BorderLayout(12, 12));

        JLabel title = new JLabel("Equipment Assignment");
        title.setForeground(FWTheme.TEXT_PRIMARY);
        title.setFont(new Font("SansSerif", Font.BOLD, 22));

        JLabel sub = new JLabel("Class ID: " + classId + "  |  Includes overlap availability check");
        sub.setForeground(FWTheme.TEXT_SECONDARY);

        JPanel top = new JPanel(new GridLayout(2, 1, 0, 4));
        top.setOpaque(false);
        top.add(title);
        top.add(sub);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;

        styleTextField(serialField);
        styleSpinner(qtySpinner);
        styleTextArea(notesArea);

        int y = 0;

        gc.gridx = 0; gc.gridy = y; gc.weightx = 0;
        form.add(label("Equipment Serial"), gc);
        gc.gridx = 1; gc.gridy = y; gc.weightx = 1;
        form.add(serialField, gc);

        y++;
        gc.gridx = 0; gc.gridy = y; gc.weightx = 0;
        form.add(label("Quantity"), gc);
        gc.gridx = 1; gc.gridy = y; gc.weightx = 1;
        form.add(qtySpinner, gc);

        y++;
        gc.gridx = 0; gc.gridy = y; gc.weightx = 0; gc.anchor = GridBagConstraints.NORTHWEST;
        form.add(label("Notes"), gc);
        gc.gridx = 1; gc.gridy = y; gc.weightx = 1;
        form.add(new JScrollPane(notesArea), gc);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actions.setOpaque(false);

        JButton assignBtn = FWUi.primaryButton("Assign");
        JButton closeBtn = FWUi.ghostDarkButton("Close");

        assignBtn.addActionListener(e -> onAssign());
        closeBtn.addActionListener(e -> dispose());

        actions.add(assignBtn);
        actions.add(closeBtn);

        card.add(top, BorderLayout.NORTH);
        card.add(form, BorderLayout.CENTER);
        card.add(actions, BorderLayout.SOUTH);

        return card;
    }

    private JComponent buildResultCard() {
        JPanel card = FWUi.cardPanel(FWTheme.CARD_BG, 16);
        card.setLayout(new BorderLayout(8, 8));

        JLabel title = new JLabel("Validation / Result");
        title.setForeground(FWTheme.TEXT_PRIMARY);
        title.setFont(new Font("SansSerif", Font.BOLD, 18));

        resultArea.setEditable(false);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        styleTextArea(resultArea);
        resultArea.setText("Ready.\nEnter serial + quantity, then click Assign.");

        card.add(title, BorderLayout.NORTH);
        card.add(new JScrollPane(resultArea), BorderLayout.CENTER);
        return card;
    }

    private void onAssign() {
        String serial = serialField.getText() == null ? "" : serialField.getText().trim();
        int qty = ((Number) qtySpinner.getValue()).intValue();
        String notes = notesArea.getText();

        EquipmentAssignmentController.AssignmentResponse r =
                controller.assignEquipmentToClass(classId, serial, qty, consultantId, notes);

        StringBuilder sb = new StringBuilder();
        sb.append("Success: ").append(r.success).append('\n');
        sb.append("Message: ").append(r.message == null ? "-" : r.message).append('\n');

        if (r.availability != null) {
            sb.append("\nAvailability Check:\n");
            sb.append(" - Requested: ").append(r.availability.requestedQty).append('\n');
            sb.append(" - Total inventory qty: ").append(r.availability.totalInventoryQty).append('\n');
            sb.append(" - Available at class time: ").append(r.availability.availableQtyAtTime).append('\n');
            sb.append(" - Overlapping classes: ").append(r.availability.overlappingClassIds).append('\n');
        }

        if (r.assignmentId != null) {
            sb.append("\nAssignment ID: ").append(r.assignmentId);
        }

        resultArea.setText(sb.toString());
        resultArea.setCaretPosition(0);

        if (r.success) {
            JOptionPane.showMessageDialog(this, "Equipment assigned successfully.", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, r.message, "Assignment Failed",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private JLabel label(String txt) {
        JLabel l = new JLabel(txt);
        l.setForeground(FWTheme.TEXT_SECONDARY);
        l.setFont(new Font("SansSerif", Font.BOLD, 13));
        return l;
    }

    private void styleTextField(JTextField tf) {
        tf.setPreferredSize(new Dimension(220, 36));
        tf.setBackground(FWTheme.CARD_BG);
        tf.setForeground(FWTheme.TEXT_PRIMARY);
        tf.setCaretColor(FWTheme.TEXT_PRIMARY);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FWTheme.BORDER, 1, true),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
    }

    private void styleSpinner(JSpinner sp) {
        sp.setPreferredSize(new Dimension(100, 36));
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

    private void styleTextArea(JTextArea ta) {
        ta.setBackground(FWTheme.CARD_BG);
        ta.setForeground(FWTheme.TEXT_PRIMARY);
        ta.setCaretColor(FWTheme.TEXT_PRIMARY);
        ta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FWTheme.BORDER, 1, true),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
    }
}