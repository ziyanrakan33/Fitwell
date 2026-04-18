package fitwell.ui.pro.consultant;

import fitwell.control.FitWellServiceRegistry;
import fitwell.control.TraineeProfileService;
import fitwell.entity.PreferredUpdateMethod;
import fitwell.entity.Trainee;
import fitwell.ui.pro.theme.FWTheme;
import fitwell.ui.pro.theme.FWUi;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ConsultantTraineesPanel extends JPanel {
    private final TraineeProfileService traineeProfileService;

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"ID", "First Name", "Last Name", "Phone", "Email", "Update Method"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);
    private List<Trainee> visibleTrainees = new ArrayList<>();

    public ConsultantTraineesPanel() {
        this.traineeProfileService = FitWellServiceRegistry.getInstance().traineeProfileService();
        setLayout(new BorderLayout(12, 12));
        setOpaque(true);
        setBackground(FWTheme.DASH_BG);
        buildUi();
        reload();
    }

    public void reload() {
        visibleTrainees = new ArrayList<>(traineeProfileService.getAllTrainees());
        tableModel.setRowCount(0);
        for (Trainee t : visibleTrainees) {
            tableModel.addRow(new Object[]{
                    t.getId(),
                    t.getFirstName(),
                    t.getLastName(),
                    t.getPhone(),
                    t.getEmail(),
                    t.getPreferredUpdateMethod()
            });
        }
    }

    private void buildUi() {
        JPanel toolbar = FWUi.cardPanel(FWTheme.CARD_BG, 12);
        toolbar.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));

        JButton addBtn = FWUi.primaryButton("Add Trainee");
        addBtn.addActionListener(e -> addTrainee());
        JButton editBtn = FWUi.ghostDarkButton("Edit");
        editBtn.addActionListener(e -> editSelectedTrainee());
        JButton deleteBtn = FWUi.ghostDarkButton("Delete");
        deleteBtn.addActionListener(e -> deleteSelectedTrainee());
        JButton refreshBtn = FWUi.ghostDarkButton("Refresh");
        refreshBtn.addActionListener(e -> reload());

        toolbar.add(addBtn);
        toolbar.add(editBtn);
        toolbar.add(deleteBtn);
        toolbar.add(refreshBtn);

        JLabel title = new JLabel("  Trainee Profiles Management");
        title.setForeground(FWTheme.TEXT_PRIMARY);
        title.setFont(FWTheme.FONT_H2);
        toolbar.add(title);

        table.setBackground(FWTheme.CARD_BG);
        table.setForeground(FWTheme.TEXT_PRIMARY);
        table.setSelectionBackground(new Color(34, 50, 83));
        table.setSelectionForeground(FWTheme.TEXT_PRIMARY);
        table.setRowHeight(28);

        add(toolbar, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private void addTrainee() {
        TraineeFormDialog dialog = new TraineeFormDialog(
                SwingUtilities.getWindowAncestor(this), null);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            reload();
        }
    }

    private void editSelectedTrainee() {
        Trainee selected = selectedTrainee();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Select a trainee first.");
            return;
        }
        TraineeFormDialog dialog = new TraineeFormDialog(
                SwingUtilities.getWindowAncestor(this), selected);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            reload();
        }
    }

    private void deleteSelectedTrainee() {
        Trainee selected = selectedTrainee();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Select a trainee first.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete trainee " + selected.fullName() + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            traineeProfileService.deleteProfile(selected.getId());
            reload();
        }
    }

    private Trainee selectedTrainee() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= visibleTrainees.size()) {
            return null;
        }
        return visibleTrainees.get(row);
    }

    static class TraineeFormDialog extends JDialog {
        private final TraineeProfileService service =
                FitWellServiceRegistry.getInstance().traineeProfileService();
        private final Trainee editing;
        private boolean saved = false;

        private final JTextField firstNameField = new JTextField(20);
        private final JTextField lastNameField = new JTextField(20);
        private final JTextField phoneField = new JTextField(20);
        private final JTextField emailField = new JTextField(20);
        private final JComboBox<String> updateMethodCombo = new JComboBox<>(new String[]{"EMAIL", "SMS"});

        TraineeFormDialog(Window owner, Trainee editing) {
            super(owner, editing == null ? "Add Trainee" : "Edit Trainee", ModalityType.APPLICATION_MODAL);
            this.editing = editing;
            buildUi();
            if (editing != null) {
                loadData();
            }
            pack();
            setMinimumSize(new Dimension(450, 380));
            setLocationRelativeTo(owner);
        }

        boolean isSaved() { return saved; }

        private void buildUi() {
            JPanel root = new JPanel(new BorderLayout(12, 12));
            root.setBackground(FWTheme.DASH_BG);
            root.setBorder(new EmptyBorder(14, 14, 14, 14));
            setContentPane(root);

            JPanel form = FWUi.cardPanel(FWTheme.CARD_BG, 14);
            form.setLayout(new GridBagLayout());
            GridBagConstraints gc = new GridBagConstraints();
            gc.insets = new Insets(6, 8, 6, 8);
            gc.anchor = GridBagConstraints.WEST;
            gc.fill = GridBagConstraints.HORIZONTAL;

            styleField(firstNameField);
            styleField(lastNameField);
            styleField(phoneField);
            styleField(emailField);
            updateMethodCombo.setBackground(FWTheme.CARD_BG);
            updateMethodCombo.setForeground(FWTheme.TEXT_PRIMARY);

            int row = 0;
            addFormRow(form, gc, row++, "First Name", firstNameField);
            addFormRow(form, gc, row++, "Last Name", lastNameField);
            addFormRow(form, gc, row++, "Phone", phoneField);
            addFormRow(form, gc, row++, "Email", emailField);
            addFormRow(form, gc, row++, "Update Method", updateMethodCombo);

            JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            footer.setOpaque(false);
            JButton cancelBtn = FWUi.ghostDarkButton("Cancel");
            cancelBtn.addActionListener(e -> dispose());
            JButton saveBtn = FWUi.primaryButton(editing == null ? "Create" : "Save");
            saveBtn.addActionListener(e -> onSave());
            footer.add(cancelBtn);
            footer.add(saveBtn);

            root.add(form, BorderLayout.CENTER);
            root.add(footer, BorderLayout.SOUTH);
            getRootPane().setDefaultButton(saveBtn);
        }

        private void loadData() {
            firstNameField.setText(editing.getFirstName());
            lastNameField.setText(editing.getLastName());
            phoneField.setText(editing.getPhone());
            emailField.setText(editing.getEmail());
            updateMethodCombo.setSelectedItem(editing.getPreferredUpdateMethod());
        }

        private void onSave() {
            try {
                String firstName = firstNameField.getText().trim();
                String lastName = lastNameField.getText().trim();
                String phone = phoneField.getText().trim();
                String email = emailField.getText().trim();
                PreferredUpdateMethod method = PreferredUpdateMethod.fromValue(
                        (String) updateMethodCombo.getSelectedItem());

                Trainee trainee;
                if (editing == null) {
                    trainee = new Trainee(null, firstName, lastName, phone, email, method);
                } else {
                    trainee = editing;
                    trainee.updateProfile(firstName, lastName, phone, email, method);
                }
                service.saveProfile(trainee);
                saved = true;
                dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation", JOptionPane.WARNING_MESSAGE);
            }
        }

        private void addFormRow(JPanel parent, GridBagConstraints gc, int row, String label, JComponent field) {
            gc.gridx = 0; gc.gridy = row; gc.weightx = 0.3;
            JLabel lbl = new JLabel(label);
            lbl.setForeground(FWTheme.TEXT_SECONDARY);
            lbl.setFont(new Font("SansSerif", Font.BOLD, 13));
            parent.add(lbl, gc);
            gc.gridx = 1; gc.weightx = 0.7;
            parent.add(field, gc);
        }

        private void styleField(JTextField tf) {
            tf.setBackground(FWTheme.CARD_BG);
            tf.setForeground(FWTheme.TEXT_PRIMARY);
            tf.setCaretColor(FWTheme.TEXT_PRIMARY);
            tf.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(FWTheme.BORDER, 1, true),
                    new EmptyBorder(8, 10, 8, 10)));
        }
    }
}
