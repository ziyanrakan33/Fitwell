package fitwell.ui.pro.consultant;

import fitwell.control.AuthenticationService;
import fitwell.domain.user.Consultant;
import fitwell.persistence.api.ConsultantRepository;
import fitwell.persistence.jdbc.JdbcConsultantRepository;
import fitwell.ui.pro.theme.FWTheme;
import fitwell.ui.pro.theme.FWUi;

import javax.swing.*;
import java.awt.*;

public class ConsultantProfilePanel extends JPanel {
    private final ConsultantRepository consultantRepo = new JdbcConsultantRepository();

    private final JTextField firstNameField = new JTextField();
    private final JTextField lastNameField = new JTextField();
    private final JTextField phoneField = new JTextField();
    private final JTextField emailField = new JTextField();
    private final JPasswordField currentPasswordField = new JPasswordField();
    private final JPasswordField newPasswordField = new JPasswordField();
    private final JPasswordField confirmPasswordField = new JPasswordField();

    public ConsultantProfilePanel() {
        setLayout(new BorderLayout());
        setBackground(FWTheme.DASH_BG);

        JPanel main = new JPanel(new BorderLayout());
        main.setOpaque(false);
        main.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel formCard = FWUi.cardPanel(FWTheme.CARD_BG, 18);
        formCard.setLayout(new BorderLayout(10, 10));

        JLabel title = new JLabel("My Profile");
        title.setFont(FWTheme.FONT_H2);
        title.setForeground(FWTheme.TEXT_PRIMARY);
        formCard.add(title, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 15));
        formPanel.setOpaque(false);
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        formPanel.add(createLabel("First Name:")); formPanel.add(firstNameField);
        formPanel.add(createLabel("Last Name:"));  formPanel.add(lastNameField);
        formPanel.add(createLabel("Phone:"));      formPanel.add(phoneField);
        formPanel.add(createLabel("Email:"));      formPanel.add(emailField);
        formPanel.add(createLabel("Current Password:")); formPanel.add(currentPasswordField);
        formPanel.add(createLabel("New Password:")); formPanel.add(newPasswordField);
        formPanel.add(createLabel("Confirm New Password:")); formPanel.add(confirmPasswordField);
        emailField.setEditable(false);

        formCard.add(formPanel, BorderLayout.CENTER);

        JButton saveBtn = FWUi.primaryButton("Save Profile");
        saveBtn.addActionListener(e -> saveProfile());
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.setOpaque(false);
        actionPanel.add(saveBtn);

        formCard.add(actionPanel, BorderLayout.SOUTH);
        main.add(formCard, BorderLayout.NORTH);
        add(main, BorderLayout.CENTER);

        styleField(firstNameField);
        styleField(lastNameField);
        styleField(phoneField);
        styleField(emailField);
        styleField(currentPasswordField);
        styleField(newPasswordField);
        styleField(confirmPasswordField);

        loadProfile();
    }

    public void reload() {
        loadProfile();
    }

    private void styleField(JTextField field) {
        field.setBackground(FWTheme.INPUT_BG);
        field.setForeground(FWTheme.TEXT_PRIMARY);
        field.setCaretColor(FWTheme.TEXT_PRIMARY);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FWTheme.BORDER, 1, true),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
    }

    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(FWTheme.TEXT_PRIMARY);
        return l;
    }

    private void loadProfile() {
        int currentId = AuthenticationService.getInstance().getCurrentUserId();
        if (currentId != -1) {
            Consultant fromDb = consultantRepo.findById(currentId);
            if (fromDb != null) {
                firstNameField.setText(fromDb.getFirstName());
                lastNameField.setText(fromDb.getLastName());
                phoneField.setText(fromDb.getPhone());
                emailField.setText(fromDb.getEmail());
                currentPasswordField.setText(fromDb.getPassword());
            }
        }
    }

    private void saveProfile() {
        int currentId = AuthenticationService.getInstance().getCurrentUserId();
        if (currentId == -1) return;
        
        Consultant c = consultantRepo.findById(currentId);
        if (c == null) return;

        String oldPass = new String(currentPasswordField.getPassword());
        String newPass = new String(newPasswordField.getPassword());
        String confPass = new String(confirmPasswordField.getPassword());

        if (!oldPass.isEmpty() || !newPass.isEmpty() || !confPass.isEmpty()) {
            if (!oldPass.equals(c.getPassword())) {
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
            c.setPassword(newPass);
        }

        c.setFirstName(firstNameField.getText().trim());
        c.setLastName(lastNameField.getText().trim());
        c.setPhone(phoneField.getText().trim());

        consultantRepo.update(c);
        // Assuming user name updates automatically when user logs in next time

        currentPasswordField.setText("");
        newPasswordField.setText("");
        confirmPasswordField.setText("");

        JOptionPane.showMessageDialog(this, "Profile saved successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
    }
}
