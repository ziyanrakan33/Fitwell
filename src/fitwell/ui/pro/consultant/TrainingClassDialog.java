package fitwell.ui.pro.consultant;

import fitwell.control.TrainingClassService;
import fitwell.domain.user.Consultant;
import fitwell.domain.training.TrainingClass;
import fitwell.repo.ConsultantRepository;
import fitwell.ui.pro.theme.FWTheme;
import fitwell.ui.pro.theme.FWUi;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class TrainingClassDialog extends JDialog {
    private final TrainingClassService trainingClassService;
    private final ConsultantRepository consultantRepository = new ConsultantRepository();
    private final TrainingClass editingClass;
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final JTextField nameField = createTextField();
    private final JComboBox<String> typeCombo = new JComboBox<>(new String[]{
            "Yoga", "Pilates", "TRX", "Strength", "Cardio", "Functional", "HIIT", "Mobility"
    });
    private final JTextField startField = createTextField();
    private final JTextField endField = createTextField();
    private final JSpinner maxSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 500, 1));
    private final JComboBox<ConsultantItem> consultantCombo = new JComboBox<>();
    private final List<JTextField> tipFields = new ArrayList<>();

    private boolean saved;

    public TrainingClassDialog(Window owner, TrainingClassService trainingClassService, TrainingClass editingClass) {
        super(owner, editingClass == null ? "Create Training Class" : "Edit Training Class", ModalityType.APPLICATION_MODAL);
        this.trainingClassService = trainingClassService;
        this.editingClass = editingClass;
        buildUi();
        loadConsultants();
        loadInitialState();
        setSize(820, 760);
        setMinimumSize(new Dimension(760, 700));
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    public boolean isSaved() {
        return saved;
    }

    private void buildUi() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(new EmptyBorder(14, 14, 14, 14));
        root.setBackground(FWTheme.DASH_BG);
        setContentPane(root);

        JPanel header = FWUi.cardPanel(FWTheme.CARD_BG, 14);
        header.setLayout(new GridLayout(2, 1, 0, 6));
        JLabel title = new JLabel(editingClass == null ? "Training Class Details" : "Update Training Class");
        title.setForeground(FWTheme.TEXT_PRIMARY);
        title.setFont(FWTheme.FONT_H2);
        JLabel subtitle = new JLabel("Use up to 5 class tips. Links can be included directly in a tip.");
        subtitle.setForeground(FWTheme.TEXT_SECONDARY);
        header.add(title);
        header.add(subtitle);

        JPanel formCard = FWUi.cardPanel(FWTheme.CARD_BG, 14);
        formCard.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(8, 8, 8, 8);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.WEST;

        styleCombo(typeCombo);
        styleSpinner(maxSpinner);
        styleCombo(consultantCombo);

        int row = 0;
        addRow(formCard, gc, row++, "Class name", nameField);
        addRow(formCard, gc, row++, "Type", typeCombo);
        addRow(formCard, gc, row++, "Start", startField);
        addRow(formCard, gc, row++, "End", endField);
        addRow(formCard, gc, row++, "Max participants", maxSpinner);
        addRow(formCard, gc, row++, "Consultant", consultantCombo);

        for (int i = 0; i < 5; i++) {
            JTextField tipField = createTextField();
            tipField.setToolTipText("Optional training tip or useful link");
            tipFields.add(tipField);
            addRow(formCard, gc, row++, "Tip " + (i + 1), tipField);
        }

        JPanel footer = FWUi.cardPanel(FWTheme.CARD_BG, 12);
        footer.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton cancel = FWUi.ghostDarkButton("Cancel");
        cancel.addActionListener(e -> dispose());
        JButton save = FWUi.primaryButton(editingClass == null ? "Create" : "Save");
        save.addActionListener(e -> onSave());
        footer.add(cancel);
        footer.add(save);

        root.add(header, BorderLayout.NORTH);
        root.add(new JScrollPane(formCard), BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(save);
    }

    private void addRow(JPanel panel, GridBagConstraints gc, int row, String labelText, JComponent field) {
        gc.gridx = 0;
        gc.gridy = row;
        gc.weightx = 0.25;
        panel.add(label(labelText), gc);

        gc.gridx = 1;
        gc.weightx = 0.75;
        panel.add(field, gc);
    }

    private void loadConsultants() {
        consultantCombo.removeAllItems();
        consultantCombo.addItem(null);
        for (Consultant consultant : consultantRepository.findAll()) {
            if (consultant != null && consultant.getId() > 0) {
                consultantCombo.addItem(new ConsultantItem(consultant.getId(),
                        consultant.getFirstName() + " " + consultant.getLastName()));
            }
        }
    }

    private void loadInitialState() {
        if (editingClass == null) {
            LocalDateTime start = LocalDateTime.now().withSecond(0).withNano(0).plusDays(2).withHour(18).withMinute(0);
            startField.setText(start.format(dateTimeFormatter));
            endField.setText(start.plusHours(1).format(dateTimeFormatter));
            if (consultantCombo.getItemCount() > 1) {
                consultantCombo.setSelectedIndex(1);
            }
            return;
        }

        nameField.setText(editingClass.getName());
        typeCombo.setSelectedItem(editingClass.getType());
        startField.setText(editingClass.getStartTime() == null ? "" : editingClass.getStartTime().format(dateTimeFormatter));
        endField.setText(editingClass.getEndTime() == null ? "" : editingClass.getEndTime().format(dateTimeFormatter));
        maxSpinner.setValue(editingClass.getMaxParticipants());
        for (int i = 0; i < consultantCombo.getItemCount(); i++) {
            ConsultantItem item = consultantCombo.getItemAt(i);
            if (item != null && item.id == editingClass.getConsultantId()) {
                consultantCombo.setSelectedIndex(i);
                break;
            }
        }
        List<String> tips = editingClass.getTips();
        for (int i = 0; i < tips.size() && i < tipFields.size(); i++) {
            tipFields.get(i).setText(tips.get(i));
        }
    }

    private void onSave() {
        try {
            ConsultantItem consultant = (ConsultantItem) consultantCombo.getSelectedItem();
            if (consultant == null) {
                throw new IllegalArgumentException("Select a consultant.");
            }

            TrainingClass trainingClass = new TrainingClass(
                    editingClass == null ? null : editingClass.getClassId(),
                    nameField.getText().trim(),
                    parseDateTime(startField.getText()),
                    parseDateTime(endField.getText()),
                    String.valueOf(typeCombo.getSelectedItem()),
                    ((Number) maxSpinner.getValue()).intValue(),
                    consultant.id
            );
            trainingClass.setTips(readTips());
            if (editingClass != null) {
                trainingClass.setStatus(editingClass.getStatus());
            }

            trainingClassService.saveClass(trainingClass);
            saved = true;
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation", JOptionPane.WARNING_MESSAGE);
        }
    }

    private LocalDateTime parseDateTime(String value) {
        try {
            return LocalDateTime.parse(value.trim(), dateTimeFormatter);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Use date format yyyy-MM-dd HH:mm");
        }
    }

    private List<String> readTips() {
        List<String> tips = new ArrayList<>();
        for (JTextField tipField : tipFields) {
            String value = tipField.getText().trim();
            if (!value.isEmpty()) {
                tips.add(value);
            }
        }
        return tips;
    }

    private JLabel label(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(FWTheme.TEXT_SECONDARY);
        label.setFont(new Font("SansSerif", Font.BOLD, 12));
        return label;
    }

    private JTextField createTextField() {
        JTextField textField = new JTextField();
        textField.setBackground(FWTheme.CARD_BG);
        textField.setForeground(FWTheme.TEXT_PRIMARY);
        textField.setCaretColor(FWTheme.TEXT_PRIMARY);
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FWTheme.BORDER, 1, true),
                new EmptyBorder(9, 10, 9, 10)
        ));
        return textField;
    }

    private void styleCombo(JComboBox<?> comboBox) {
        comboBox.setBackground(FWTheme.CARD_BG);
        comboBox.setForeground(FWTheme.TEXT_PRIMARY);
        comboBox.setBorder(BorderFactory.createLineBorder(FWTheme.BORDER, 1, true));
    }

    private void styleSpinner(JSpinner spinner) {
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor defaultEditor) {
            defaultEditor.getTextField().setBackground(FWTheme.CARD_BG);
            defaultEditor.getTextField().setForeground(FWTheme.TEXT_PRIMARY);
            defaultEditor.getTextField().setCaretColor(FWTheme.TEXT_PRIMARY);
        }
    }

    private static class ConsultantItem {
        private final int id;
        private final String label;

        private ConsultantItem(int id, String label) {
            this.id = id;
            this.label = label == null ? ("Consultant #" + id) : label.trim();
        }

        @Override
        public String toString() {
            return "#" + id + " - " + label;
        }
    }
}
