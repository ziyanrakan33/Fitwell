package fitwell.ui.pro.trainee;

import fitwell.control.TraineeProfileService;
import fitwell.entity.PreferredUpdateMethod;
import fitwell.entity.Trainee;
import fitwell.ui.pro.theme.FWTheme;
import fitwell.ui.pro.theme.FWUi;

import javax.swing.*;
import java.awt.*;

public class TraineeProfilePanel extends JPanel {
    private final TraineeProfileService traineeProfileService;

    private final JTextField firstNameField = new JTextField();
    private final JTextField lastNameField = new JTextField();
    private final JTextField phoneField = new JTextField();
    private final JTextField emailField = new JTextField();
    private final JPasswordField currentPasswordField = new JPasswordField();
    private final JPasswordField newPasswordField = new JPasswordField();
    private final JPasswordField confirmPasswordField = new JPasswordField();
    private final JComboBox<String> methodCombo = new JComboBox<>(new String[]{"EMAIL", "SMS"});
    private Integer currentTraineeId;

    public TraineeProfilePanel(TraineeProfileService traineeProfileService) {
        this.traineeProfileService = traineeProfileService;
        setLayout(new BorderLayout(12, 12));
        setOpaque(true);
        setBackground(FWTheme.DASH_BG);
        buildUi();
        loadCurrentProfile();
    }

    public void reload() {
        loadCurrentProfile();
    }

    private void buildUi() {
        JPanel card = FWUi.cardPanel(FWTheme.CARD_BG, 18);
        card.setLayout(new GridBagLayout());

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(8, 8, 8, 8);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.WEST;

        style(firstNameField);
        style(lastNameField);
        style(phoneField);
        style(emailField);
        style(currentPasswordField);
        style(newPasswordField);
        style(confirmPasswordField);
        methodCombo.setBackground(FWTheme.CARD_BG);
        methodCombo.setForeground(FWTheme.TEXT_PRIMARY);

        int row = 0;
        addRow(card, gc, row++, "First name", firstNameField);
        addRow(card, gc, row++, "Last name", lastNameField);
        addRow(card, gc, row++, "Phone", phoneField);
        addRow(card, gc, row++, "Email", emailField);
        addRow(card, gc, row++, "Preferred updates", methodCombo);
        addRow(card, gc, row++, "Current Password", currentPasswordField);
        addRow(card, gc, row++, "New Password", newPasswordField);
        addRow(card, gc, row++, "Confirm Password", confirmPasswordField);

        JButton save = FWUi.primaryButton("Save Profile");
        save.addActionListener(e -> saveProfile());
        gc.gridx = 1;
        gc.gridy = row;
        card.add(save, gc);

        add(card, BorderLayout.NORTH);
    }

    private void addRow(JPanel panel, GridBagConstraints gc, int row, String labelText, JComponent field) {
        gc.gridx = 0;
        gc.gridy = row;
        gc.weightx = 0.3;
        JLabel label = new JLabel(labelText);
        label.setForeground(FWTheme.TEXT_SECONDARY);
        panel.add(label, gc);

        gc.gridx = 1;
        gc.weightx = 0.7;
        panel.add(field, gc);
    }

    private void loadCurrentProfile() {
        Trainee trainee = traineeProfileService.getCurrentTrainee();
        currentTraineeId = trainee.getId();
        firstNameField.setText(trainee.getFirstName());
        lastNameField.setText(trainee.getLastName());
        phoneField.setText(trainee.getPhone());
        emailField.setText(trainee.getEmail());
        currentPasswordField.setText(trainee.getPassword());
        methodCombo.setSelectedItem(trainee.getPreferredUpdateMethod());
    }

    private void saveProfile() {
        try {
            Trainee currentTrainee = traineeProfileService.getCurrentTrainee();
            String oldPass = new String(currentPasswordField.getPassword());
            String newPass = new String(newPasswordField.getPassword());
            String confPass = new String(confirmPasswordField.getPassword());

            String targetPassword = currentTrainee.getPassword();

            if (!oldPass.isEmpty() || !newPass.isEmpty() || !confPass.isEmpty()) {
                if (!oldPass.equals(targetPassword)) {
                    JOptionPane.showMessageDialog(this, "Current password incorrect.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (!newPass.equals(confPass)) {
                    JOptionPane.showMessageDialog(this, "New passwords do not match.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (newPass.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "New password cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                targetPassword = newPass;
            }

            Trainee trainee = new Trainee(
                    currentTraineeId,
                    firstNameField.getText().trim(),
                    lastNameField.getText().trim(),
                    phoneField.getText().trim(),
                    emailField.getText().trim(),
                    targetPassword,
                    PreferredUpdateMethod.fromValue(String.valueOf(methodCombo.getSelectedItem()))
            );
            traineeProfileService.saveProfile(trainee);
            currentTraineeId = trainee.getId();

            currentPasswordField.setText("");
            newPasswordField.setText("");
            confirmPasswordField.setText("");

            JOptionPane.showMessageDialog(this, "Profile updated successfully.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Profile", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void style(JTextField field) {
        field.setBackground(FWTheme.CARD_BG);
        field.setForeground(FWTheme.TEXT_PRIMARY);
        field.setCaretColor(FWTheme.TEXT_PRIMARY);
        field.setBorder(BorderFactory.createLineBorder(FWTheme.BORDER, 1, true));
    }
}
