package fitwell.ui.consultant.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import fitwell.domain.equipment.Equipment;
import fitwell.domain.equipment.EquipmentCategory;
import fitwell.domain.equipment.EquipmentLocation;
import fitwell.domain.equipment.EquipmentStatus;
import fitwell.ui.theme.FWTheme;

@SuppressWarnings({"serial","this-escape"})
public class EquipmentFormDialog extends JDialog {

    public enum Mode { ADD, UPDATE }

    private final Mode mode;

    private final JTextField serialField = new JTextField();
    private final JTextField nameField = new JTextField();
    private final JTextArea descArea = new JTextArea(4, 22);

    private final JComboBox<EquipmentCategory> categoryBox = new JComboBox<>(EquipmentCategory.values());
    private final JComboBox<EquipmentStatus> statusBox = new JComboBox<>(EquipmentStatus.values());

    private final JSpinner qtySpinner = new JSpinner(new SpinnerNumberModel(0, 0, 999999, 1));
    private final JSpinner xSpinner = new JSpinner(new SpinnerNumberModel(0, -999999, 999999, 1));
    private final JSpinner ySpinner = new JSpinner(new SpinnerNumberModel(0, -999999, 999999, 1));
    private final JSpinner shelfSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 999999, 1));

    private Equipment result = null;

    public EquipmentFormDialog(JFrame owner, Mode mode) {
        super(owner, mode == Mode.ADD ? "Add Equipment" : "Update Equipment", true);
        this.mode = mode;

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(520, 420);
        setLocationRelativeTo(owner);

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBackground(FWTheme.DASH_BG);
        root.add(buildForm(), BorderLayout.CENTER);
        root.add(buildButtons(), BorderLayout.SOUTH);
        setContentPane(root);

        styleAllFields();
    }

    private JPanel buildForm() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel grid = new JPanel(new GridLayout(6, 2, 10, 10));

        grid.add(new JLabel("Serial Number:"));
        grid.add(serialField);

        grid.add(new JLabel("Name:"));
        grid.add(nameField);

        grid.add(new JLabel("Category:"));
        grid.add(categoryBox);

        grid.add(new JLabel("Status:"));
        grid.add(statusBox);

        grid.add(new JLabel("Quantity:"));
        grid.add(qtySpinner);

        grid.add(new JLabel("Location (X / Y / Shelf):"));
        JPanel loc = new JPanel(new GridLayout(1, 3, 8, 8));
        loc.add(xSpinner);
        loc.add(ySpinner);
        loc.add(shelfSpinner);
        grid.add(loc);

        root.add(grid, BorderLayout.NORTH);

        JPanel descPanel = new JPanel(new BorderLayout(6, 6));
        descPanel.add(new JLabel("Description:"), BorderLayout.NORTH);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descPanel.add(new JScrollPane(descArea), BorderLayout.CENTER);

        root.add(descPanel, BorderLayout.CENTER);

        return root;
    }

    private JPanel buildButtons() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));

        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(e -> {
            result = null;
            dispose();
        });

        JButton ok = new JButton(mode == Mode.ADD ? "Add" : "Update");
        ok.addActionListener(e -> onOk());

        getRootPane().setDefaultButton(ok);

        p.add(cancel);
        p.add(ok);
        return p;
    }

    private void onOk() {
        String serial = serialField.getText() == null ? "" : serialField.getText().trim();
        String name = nameField.getText() == null ? "" : nameField.getText().trim();
        String desc = descArea.getText() == null ? "" : descArea.getText().trim();

        if (serial.isBlank()) {
            showError("Serial Number is required.");
            return;
        }


        if (serial.length() < 3) {
            showError("Serial Number is too short.");
            return;
        }
        if (name.isBlank()) {
            showError("Name is required.");
            return;
        }

        int qty = (Integer) qtySpinner.getValue();
        if (qty < 0) {
            showError("Quantity cannot be negative.");
            return;
        }

        EquipmentCategory cat = (EquipmentCategory) categoryBox.getSelectedItem();
        EquipmentStatus st = (EquipmentStatus) statusBox.getSelectedItem();

        int x = (Integer) xSpinner.getValue();
        int y = (Integer) ySpinner.getValue();
        int shelf = (Integer) shelfSpinner.getValue();

        result = new Equipment(
                serial,
                name,
                desc,
                cat == null ? EquipmentCategory.other : cat,
                qty,
                st == null ? EquipmentStatus.IN_SERVICE : st,
                new EquipmentLocation(x, y, shelf)
        );

        dispose();
    }

    private void styleAllFields() {
        Color inputBg = FWTheme.INPUT_BG;
        Color textColor = FWTheme.TEXT_PRIMARY;

        for (JTextField tf : new JTextField[]{serialField, nameField}) {
            tf.setBackground(inputBg);
            tf.setForeground(textColor);
            tf.setCaretColor(textColor);
        }

        descArea.setBackground(inputBg);
        descArea.setForeground(textColor);
        descArea.setCaretColor(textColor);

        categoryBox.setBackground(inputBg);
        categoryBox.setForeground(textColor);
        statusBox.setBackground(inputBg);
        statusBox.setForeground(textColor);

        for (JSpinner sp : new JSpinner[]{qtySpinner, xSpinner, ySpinner, shelfSpinner}) {
            sp.setBackground(inputBg);
            if (sp.getEditor() instanceof JSpinner.DefaultEditor de) {
                de.getTextField().setBackground(inputBg);
                de.getTextField().setForeground(textColor);
                de.getTextField().setCaretColor(textColor);
            }
        }
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Validation Error", JOptionPane.ERROR_MESSAGE);
    }

    public void setEquipment(Equipment e) {
        if (e == null) return;
        serialField.setText(e.getSerialNumber());
        serialField.setEditable(mode == Mode.ADD);
        nameField.setText(e.getName());
        descArea.setText(e.getDescription());
        categoryBox.setSelectedItem(e.getCategory());
        statusBox.setSelectedItem(e.getStatus());
        qtySpinner.setValue(e.getQuantity());

        if (e.getLocation() != null) {
            xSpinner.setValue(e.getLocation().getX());
            ySpinner.setValue(e.getLocation().getY());
            shelfSpinner.setValue(e.getLocation().getShelfNumber());
        }
    }

    public void setSerialEditable(boolean editable) {
        serialField.setEditable(editable);
    }

    public Equipment showDialogAndGetResult() {
        setVisible(true);
        return result;
    }
}
