package fitwell.ui.consultant.panels;

import fitwell.service.training.TrainingClassService;
import fitwell.domain.user.Consultant;
import fitwell.domain.training.TrainingClass;
import fitwell.persistence.api.ConsultantRepository;
import fitwell.persistence.jdbc.JdbcConsultantRepository;
import fitwell.persistence.api.TrainingClassRepository;
import fitwell.ui.theme.FWTheme;
import fitwell.ui.theme.FWUi;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.text.DefaultFormatter;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"serial","this-escape"})
public class ClassFormProDialog extends JDialog {

    private final ConsultantRepository consultantRepo = new JdbcConsultantRepository();
    private final TrainingClass editingClass;
    private final TrainingClassService trainingClassService;
    private boolean saved = false;

    private JTextField nameField;
    private JComboBox<String> typeCombo;
    private JTextField startField;
    private JTextField endField;

    // Max spinner (fixed + readable)
    private JSpinner maxSpinner;
    private SpinnerNumberModel maxSpinnerModel;

    // Consultant selection
    private JComboBox<ConsultantItem> consultantCombo;

    // Tips (up to 5, may include links to online resources)
    private final JTextField[] tipFields = new JTextField[5];

    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final List<ConsultantItem> consultantItems = new ArrayList<>();

    public ClassFormProDialog(Window owner,
                              TrainingClassRepository trainingClassRepo,
                              TrainingClass editingClass,
                              TrainingClassService trainingClassService) {
        super(owner, editingClass == null ? "Add Class" : "Edit Class", ModalityType.APPLICATION_MODAL);
        this.editingClass = editingClass;
        this.trainingClassService = trainingClassService;

        buildUi();
        loadConsultants();

        if (editingClass != null) {
            loadExistingData();
        } else {
            loadDefaultData();
        }

        pack();
        setMinimumSize(new Dimension(720, 780));
        setSize(new Dimension(800, 860));
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    public boolean isSaved() {
        return saved;
    }

    private void buildUi() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBackground(FWTheme.DASH_BG);
        root.setBorder(new EmptyBorder(14, 14, 14, 14));
        setContentPane(root);

        // ===== Header =====
        JPanel header = FWUi.cardPanel(FWTheme.CARD_BG, 14);
        header.setLayout(new BorderLayout(0, 8));

        JLabel title = new JLabel(editingClass == null ? "Create Training Class" : "Edit Training Class");
        title.setForeground(FWTheme.TEXT_PRIMARY);
        title.setFont(FWTheme.FONT_H2);

        JLabel sub = new JLabel("Date format: yyyy-MM-dd HH:mm  (example: 2026-03-01 18:30)");
        sub.setForeground(FWTheme.TEXT_SECONDARY);
        sub.setFont(new Font("SansSerif", Font.PLAIN, 12));

        header.add(title, BorderLayout.NORTH);
        header.add(sub, BorderLayout.SOUTH);

        // ===== Form Card =====
        JPanel formCard = FWUi.cardPanel(FWTheme.CARD_BG, 14);
        formCard.setLayout(new GridBagLayout());

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(8, 8, 8, 8);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;

        nameField = proTextField();

        typeCombo = new JComboBox<>(new String[]{
                "Yoga", "Pilates", "TRX", "Strength", "Cardio", "Functional", "HIIT", "Mobility"
        });
        styleCombo(typeCombo);

        startField = proTextField();
        endField = proTextField();

        // ===== FIXED Max Participants spinner =====
        maxSpinnerModel = new SpinnerNumberModel(10, 1, 500, 1);
        maxSpinner = new JSpinner(maxSpinnerModel);
        styleSpinner(maxSpinner); // important fix

        // ===== Consultant combo =====
        consultantCombo = new JComboBox<>();
        styleCombo(consultantCombo);
        consultantCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                lbl.setText(value == null ? "Select Consultant..." : value.toString());
                lbl.setBackground(isSelected ? new Color(34, 50, 83) : FWTheme.CARD_BG);
                lbl.setForeground(FWTheme.TEXT_PRIMARY);
                lbl.setBorder(new EmptyBorder(4, 8, 4, 8));
                return lbl;
            }
        });

        int row = 0;
        addRow(formCard, gc, row++, "Class Name", nameField);
        addRow(formCard, gc, row++, "Type", typeCombo);
        addRow(formCard, gc, row++, "Start Date & Time", startField);
        addRow(formCard, gc, row++, "End Date & Time", endField);
        addRow(formCard, gc, row++, "Max Participants", maxSpinner);
        addRow(formCard, gc, row++, "Consultant", consultantCombo);

        // Tips fields (up to 5, may include links to online resources)
        for (int i = 0; i < tipFields.length; i++) {
            tipFields[i] = proTextField();
            tipFields[i].setToolTipText("Tip or link to online resource (optional)");
            addRow(formCard, gc, row++, "Tip " + (i + 1), tipFields[i]);
        }

        JPanel notePanel = new JPanel(new BorderLayout());
        notePanel.setOpaque(true);
        notePanel.setBackground(new Color(14, 22, 40));
        notePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FWTheme.BORDER, 1, true),
                new EmptyBorder(10, 12, 10, 12)
        ));

        JLabel note = new JLabel("<html><b>Note:</b> End must be after Start. Max participants &gt; 0. Tips may include links to online resources.</html>");
        note.setForeground(FWTheme.TEXT_SECONDARY);
        note.setFont(new Font("SansSerif", Font.PLAIN, 12));
        notePanel.add(note, BorderLayout.CENTER);

        gc.gridx = 0;
        gc.gridy = row++;
        gc.gridwidth = 2;
        gc.weightx = 1;
        gc.weighty = 0;
        gc.fill = GridBagConstraints.HORIZONTAL;
        formCard.add(notePanel, gc);

        gc.gridx = 0;
        gc.gridy = row;
        gc.gridwidth = 2;
        gc.weightx = 1;
        gc.weighty = 1;
        gc.fill = GridBagConstraints.BOTH;
        formCard.add(Box.createVerticalGlue(), gc);

        JScrollPane formScroll = new JScrollPane(formCard);
        styleProScrollPane(formScroll);

        // ===== Footer / Actions =====
        JPanel footer = FWUi.cardPanel(FWTheme.CARD_BG, 12);
        footer.setLayout(new BorderLayout());

        JPanel footerLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        footerLeft.setOpaque(false);

        JButton fillNextHourBtn = FWUi.ghostDarkButton("Fill Next Hour");
        fillNextHourBtn.addActionListener(e -> fillNextHour());

        JButton minusBtn = FWUi.ghostDarkButton("-1");
        minusBtn.addActionListener(e -> adjustMax(-1));

        JButton plusBtn = FWUi.ghostDarkButton("+1");
        plusBtn.addActionListener(e -> adjustMax(1));

        footerLeft.add(fillNextHourBtn);
        footerLeft.add(minusBtn);
        footerLeft.add(plusBtn);

        JPanel footerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footerRight.setOpaque(false);

        JButton cancelBtn = FWUi.ghostDarkButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        JButton saveBtn = FWUi.primaryButton(editingClass == null ? "Create" : "Save");
        saveBtn.addActionListener(e -> onSave());

        footerRight.add(cancelBtn);
        footerRight.add(saveBtn);

        footer.add(footerLeft, BorderLayout.WEST);
        footer.add(footerRight, BorderLayout.EAST);

        root.add(header, BorderLayout.NORTH);
        root.add(formScroll, BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(saveBtn);
    }

    private void addRow(JPanel parent, GridBagConstraints gc, int row, String labelText, JComponent field) {
        gc.gridwidth = 1;
        gc.weighty = 0;

        gc.gridx = 0;
        gc.gridy = row;
        gc.weightx = 0.33;
        gc.fill = GridBagConstraints.HORIZONTAL;
        parent.add(label(labelText), gc);

        gc.gridx = 1;
        gc.gridy = row;
        gc.weightx = 0.67;
        gc.fill = GridBagConstraints.HORIZONTAL;
        parent.add(field, gc);
    }

    private void loadConsultants() {
        consultantItems.clear();
        consultantCombo.removeAllItems();

        // Placeholder item
        consultantCombo.addItem(null);

        try {
            List<Consultant> consultants = consultantRepo.findAll();
            if (consultants != null) {
                for (Consultant c : consultants) {
                    if (c == null) continue;

                    int id = c.getId();
                    if (id <= 0) continue;

                    String name = buildConsultantDisplayName(c);

                    ConsultantItem item = new ConsultantItem(id, name);
                    consultantItems.add(item);
                    consultantCombo.addItem(item);
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Warning: Could not load consultants from DB.\nYou may not be able to create classes until this is fixed.\n\n"
                            + buildErrorMessage(ex),
                    "Consultants Load Warning",
                    JOptionPane.WARNING_MESSAGE
            );
        }
    }

    private String buildConsultantDisplayName(Consultant c) {
        if (c == null) return "Consultant";

        String first = "";
        String last = "";

        try { first = c.getFirstName(); } catch (Exception ignored) {}
        try { last = c.getLastName(); } catch (Exception ignored) {}

        String full = (safe(first) + " " + safe(last)).trim();
        if (!full.isEmpty()) return full;

        return "Consultant #" + c.getId();
    }

    private void loadDefaultData() {
        LocalDateTime base = LocalDateTime.now()
                .withSecond(0)
                .withNano(0)
                .plusHours(1);

        startField.setText(base.format(dtf));
        endField.setText(base.plusHours(1).format(dtf));

        maxSpinnerModel.setValue(10);

        // auto-select first real consultant if exists
        if (consultantCombo.getItemCount() > 1) {
            consultantCombo.setSelectedIndex(1);
        } else {
            consultantCombo.setSelectedIndex(0);
        }
    }

    private void fillNextHour() {
        LocalDateTime base = LocalDateTime.now()
                .withSecond(0)
                .withNano(0)
                .plusHours(1);

        startField.setText(base.format(dtf));
        endField.setText(base.plusHours(1).format(dtf));
    }

    private void adjustMax(int delta) {
        try {
            maxSpinner.commitEdit();
        } catch (Exception ignored) {}

        int current = ((Number) maxSpinner.getValue()).intValue();
        int next = current + delta;
        if (next < 1) next = 1;
        if (next > 500) next = 500;
        maxSpinner.setValue(next);
    }

    private void loadExistingData() {
        nameField.setText(safe(editingClass.getName()));
        typeCombo.setSelectedItem(safe(editingClass.getType()));

        if (editingClass.getStartTime() != null) {
            startField.setText(editingClass.getStartTime().format(dtf));
        }
        if (editingClass.getEndTime() != null) {
            endField.setText(editingClass.getEndTime().format(dtf));
        }

        maxSpinnerModel.setValue(Math.max(1, editingClass.getMaxParticipants()));
        selectConsultantById(editingClass.getConsultantId());

        List<String> tips = editingClass.getTips();
        for (int i = 0; i < tipFields.length; i++) {
            tipFields[i].setText(i < tips.size() ? safe(tips.get(i)) : "");
        }
    }

    private void selectConsultantById(int consultantId) {
        ComboBoxModel<ConsultantItem> model = consultantCombo.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            ConsultantItem item = model.getElementAt(i);
            if (item != null && item.id == consultantId) {
                consultantCombo.setSelectedIndex(i);
                return;
            }
        }
        consultantCombo.setSelectedIndex(0);
    }

    private void onSave() {
        try {
            String name = safe(nameField.getText()).trim();
            String type = typeCombo.getSelectedItem() == null ? "" : typeCombo.getSelectedItem().toString().trim();
            String startText = safe(startField.getText()).trim();
            String endText = safe(endField.getText()).trim();

            try {
                maxSpinner.commitEdit();
            } catch (Exception ex) {
                warn("Max Participants must be a valid number.");
                return;
            }
            int max = ((Number) maxSpinner.getValue()).intValue();

            ConsultantItem selectedConsultant = (ConsultantItem) consultantCombo.getSelectedItem();

            // ===== Validation =====
            if (name.isEmpty()) {
                warn("Class name is required.");
                nameField.requestFocus();
                return;
            }
            if (type.isEmpty()) {
                warn("Type is required.");
                typeCombo.requestFocus();
                return;
            }
            if (selectedConsultant == null) {
                warn("Please select a consultant.");
                consultantCombo.requestFocus();
                return;
            }

            LocalDateTime start;
            LocalDateTime end;

            try {
                start = LocalDateTime.parse(startText, dtf);
            } catch (DateTimeParseException ex) {
                warn("Start date/time is invalid.\nUse format: yyyy-MM-dd HH:mm");
                startField.requestFocus();
                startField.selectAll();
                return;
            }

            try {
                end = LocalDateTime.parse(endText, dtf);
            } catch (DateTimeParseException ex) {
                warn("End date/time is invalid.\nUse format: yyyy-MM-dd HH:mm");
                endField.requestFocus();
                endField.selectAll();
                return;
            }

            if (!end.isAfter(start)) {
                warn("End time must be after Start time.");
                endField.requestFocus();
                endField.selectAll();
                return;
            }

            if (max <= 0) {
                warn("Max participants must be greater than 0.");
                return;
            }

            int consultantId = selectedConsultant.id;

            List<String> tips = new ArrayList<>();
            for (JTextField tipField : tipFields) {
                String tipText = safe(tipField.getText()).trim();
                if (!tipText.isEmpty()) {
                    tips.add(tipText);
                }
            }

            TrainingClassService classService = trainingClassService;
            if (editingClass == null) {
                TrainingClass newClass = new TrainingClass(
                        null, name, start, end, type, max, consultantId
                );
                newClass.setTips(tips);
                classService.saveClass(newClass);
            } else {
                TrainingClass updated = new TrainingClass(
                        editingClass.getClassId(), name, start, end, type, max, consultantId
                );
                updated.setTips(tips);
                classService.saveClass(updated);
            }

            saved = true;
            dispose();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    buildErrorMessage(ex),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // ===== UI helpers =====

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(FWTheme.TEXT_SECONDARY);
        l.setFont(new Font("SansSerif", Font.BOLD, 13));
        return l;
    }

    private JTextField proTextField() {
        JTextField tf = new JTextField();
        tf.setBackground(FWTheme.CARD_BG);
        tf.setForeground(FWTheme.TEXT_PRIMARY);
        tf.setCaretColor(FWTheme.TEXT_PRIMARY);
        tf.setFont(new Font("SansSerif", Font.PLAIN, 13));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FWTheme.BORDER, 1, true),
                new EmptyBorder(10, 12, 10, 12)
        ));
        tf.setPreferredSize(new Dimension(320, 42));
        return tf;
    }

    private void styleCombo(JComboBox<?> cb) {
        cb.setBackground(FWTheme.CARD_BG);
        cb.setForeground(FWTheme.TEXT_PRIMARY);
        cb.setFont(new Font("SansSerif", Font.PLAIN, 13));
        cb.setBorder(BorderFactory.createLineBorder(FWTheme.BORDER, 1, true));
        cb.setPreferredSize(new Dimension(320, 42));
        cb.setFocusable(true);
    }

    private void styleSpinner(JSpinner sp) {
        sp.setFont(new Font("SansSerif", Font.PLAIN, 13));
        sp.setPreferredSize(new Dimension(320, 42));
        sp.setBorder(BorderFactory.createLineBorder(FWTheme.BORDER, 1, true));
        sp.setFocusable(true);
        sp.setBackground(FWTheme.CARD_BG);

        JComponent editor = sp.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JFormattedTextField tf = ((JSpinner.DefaultEditor) editor).getTextField();
            tf.setEditable(true);
            tf.setEnabled(true);

            tf.setBackground(FWTheme.CARD_BG);
            tf.setForeground(FWTheme.TEXT_PRIMARY);
            tf.setCaretColor(FWTheme.TEXT_PRIMARY);

            tf.setFont(new Font("SansSerif", Font.BOLD, 14));
            tf.setHorizontalAlignment(SwingConstants.LEFT);
            tf.setBorder(new EmptyBorder(8, 10, 8, 10));

            try {
                JFormattedTextField.AbstractFormatter f = tf.getFormatter();
                if (f instanceof DefaultFormatter) {
                    ((DefaultFormatter) f).setCommitsOnValidEdit(true);
                }
            } catch (Exception ignored) {}
        }
    }

    private void styleProScrollPane(JScrollPane sp) {
        sp.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FWTheme.BORDER, 1, true),
                new EmptyBorder(2, 2, 2, 2)
        ));
        sp.getViewport().setBackground(FWTheme.DASH_BG);
        sp.getVerticalScrollBar().setUnitIncrement(14);
        sp.getHorizontalScrollBar().setUnitIncrement(14);

        JScrollBar vsb = sp.getVerticalScrollBar();
        JScrollBar hsb = sp.getHorizontalScrollBar();

        vsb.setBackground(FWTheme.SIDEBAR_BG);
        hsb.setBackground(FWTheme.SIDEBAR_BG);

        vsb.setUI(new BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                thumbColor = new Color(63, 78, 110);
                trackColor = FWTheme.SIDEBAR_BG;
            }
            @Override protected JButton createDecreaseButton(int orientation) { return zeroButton(); }
            @Override protected JButton createIncreaseButton(int orientation) { return zeroButton(); }
        });

        hsb.setUI(new BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                thumbColor = new Color(63, 78, 110);
                trackColor = FWTheme.SIDEBAR_BG;
            }
            @Override protected JButton createDecreaseButton(int orientation) { return zeroButton(); }
            @Override protected JButton createIncreaseButton(int orientation) { return zeroButton(); }
        });
    }

    private JButton zeroButton() {
        JButton b = new JButton();
        b.setPreferredSize(new Dimension(0, 0));
        b.setMinimumSize(new Dimension(0, 0));
        b.setMaximumSize(new Dimension(0, 0));
        return b;
    }

    private void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Validation", JOptionPane.WARNING_MESSAGE);
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private String buildErrorMessage(Throwable ex) {
        StringBuilder sb = new StringBuilder("Save failed:\n");
        Throwable cur = ex;
        int depth = 0;
        while (cur != null && depth < 6) {
            String m = cur.getMessage();
            if (m != null && !m.isBlank()) {
                sb.append("- ").append(cur.getClass().getSimpleName()).append(": ").append(m).append("\n");
            }
            cur = cur.getCause();
            depth++;
        }
        return sb.toString();
    }

    // ===== inner model =====
    @SuppressWarnings({"serial","this-escape"})
    private static class ConsultantItem {
        final int id;
        final String displayName;

        ConsultantItem(int id, String displayName) {
            this.id = id;
            this.displayName = (displayName == null || displayName.isBlank())
                    ? ("Consultant #" + id)
                    : displayName;
        }

        @Override
        public String toString() {
            return "#" + id + " - " + displayName;
        }
    }
}