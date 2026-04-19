package fitwell.ui.pro;

import fitwell.service.auth.AuthenticationService;
import fitwell.domain.user.Consultant;
import fitwell.domain.user.ConsultantRole;
import fitwell.domain.shared.PreferredUpdateMethod;
import fitwell.domain.user.Trainee;
import fitwell.persistence.api.ConsultantRepository;
import fitwell.persistence.jdbc.JdbcConsultantRepository;
import fitwell.persistence.api.TraineeRepository;
import fitwell.persistence.jdbc.JdbcTraineeRepository;
import fitwell.ui.pro.theme.FWTheme;
import fitwell.ui.pro.theme.FWUi;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class SignUpDialog extends JDialog {

    private final String role;
    private final JTextField firstNameField = new JTextField();
    private final JTextField lastNameField = new JTextField();
    private final JTextField phoneField = new JTextField();
    private final JTextField emailField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final JPasswordField confirmPasswordField = new JPasswordField();
    private final JComboBox<PreferredUpdateMethod> updateMethodCombo =
            new JComboBox<>(PreferredUpdateMethod.values());
    private final JComboBox<ConsultantRole> consultantRoleCombo =
            new JComboBox<>(new ConsultantRole[]{ConsultantRole.TRAINER, ConsultantRole.DIETITIAN});
    private final JLabel errorLabel = new JLabel(" ");
    private boolean signedUp = false;

    private final ConsultantRepository consultantRepo = new JdbcConsultantRepository();
    private final TraineeRepository traineeRepo = new JdbcTraineeRepository();

    public SignUpDialog(Window owner, String role) {
        super(owner, "Sign Up — " + capitalize(role), ModalityType.APPLICATION_MODAL);
        this.role = role;
        setSize(440, 580);
        setResizable(false);
        setLocationRelativeTo(owner);
        buildUi();
    }

    private void buildUi() {
        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setBackground(FWTheme.ENTRY_BG);
        root.setBorder(new EmptyBorder(24, 32, 20, 32));

        JLabel title = new JLabel("Create " + capitalize(role) + " Account");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(FWTheme.ENTRY_TEXT);
        title.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.gridx = 0;

        int row = 0;
        row = addField(form, gbc, row, "First Name", firstNameField);
        row = addField(form, gbc, row, "Last Name", lastNameField);
        row = addField(form, gbc, row, "Phone", phoneField);
        row = addField(form, gbc, row, "Email", emailField);
        row = addField(form, gbc, row, "Password", passwordField);
        row = addField(form, gbc, row, "Confirm Password", confirmPasswordField);

        if ("trainee".equals(role)) {
            row = addComboField(form, gbc, row, "Preferred Update Method", updateMethodCombo);
        } else {
            row = addComboField(form, gbc, row, "Role (Trainer or Dietitian)", consultantRoleCombo);
        }

        errorLabel.setFont(FWTheme.FONT_BODY);
        errorLabel.setForeground(FWTheme.DANGER);
        errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = row;
        gbc.insets = new Insets(8, 0, 0, 0);
        form.add(errorLabel, gbc);

        KeyAdapter enterKey = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) attemptSignUp();
            }
        };
        firstNameField.addKeyListener(enterKey);
        lastNameField.addKeyListener(enterKey);
        phoneField.addKeyListener(enterKey);
        emailField.addKeyListener(enterKey);
        passwordField.addKeyListener(enterKey);
        confirmPasswordField.addKeyListener(enterKey);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        buttons.setOpaque(false);

        JButton signUpBtn = FWUi.primaryButton("Sign Up");
        signUpBtn.setPreferredSize(new Dimension(120, 38));
        signUpBtn.addActionListener(e -> attemptSignUp());

        JButton cancelBtn = FWUi.ghostDarkButton("Cancel");
        cancelBtn.setPreferredSize(new Dimension(120, 38));
        cancelBtn.addActionListener(e -> dispose());

        buttons.add(signUpBtn);
        buttons.add(cancelBtn);

        root.add(title, BorderLayout.NORTH);
        root.add(form, BorderLayout.CENTER);
        root.add(buttons, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private int addField(JPanel form, GridBagConstraints gbc, int row, String label, JTextField field) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(FWTheme.FONT_BODY);
        lbl.setForeground(FWTheme.ENTRY_SUBTEXT);
        gbc.gridy = row;
        gbc.insets = new Insets(row == 0 ? 0 : 6, 0, 2, 0);
        form.add(lbl, gbc);

        styleTextField(field);
        gbc.gridy = row + 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        form.add(field, gbc);

        return row + 2;
    }

    private int addComboField(JPanel form, GridBagConstraints gbc, int row, String label, JComboBox<?> combo) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(FWTheme.FONT_BODY);
        lbl.setForeground(FWTheme.ENTRY_SUBTEXT);
        gbc.gridy = row;
        gbc.insets = new Insets(6, 0, 2, 0);
        form.add(lbl, gbc);

        combo.setFont(new Font("SansSerif", Font.PLAIN, 14));
        combo.setBackground(FWTheme.INPUT_BG);
        combo.setForeground(FWTheme.TEXT_PRIMARY);
        combo.setPreferredSize(new Dimension(0, 32));
        gbc.gridy = row + 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        form.add(combo, gbc);

        return row + 2;
    }

    private void styleTextField(JTextField field) {
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setPreferredSize(new Dimension(0, 32));
        field.setBackground(FWTheme.INPUT_BG);
        field.setForeground(FWTheme.TEXT_PRIMARY);
        field.setCaretColor(FWTheme.TEXT_PRIMARY);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FWTheme.BORDER, 1, true),
                new EmptyBorder(5, 10, 5, 10)
        ));
    }

    private void attemptSignUp() {
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        if (firstName.isEmpty() || lastName.isEmpty() || phone.isEmpty() || email.isEmpty()
                || password.isEmpty() || confirmPassword.isEmpty()) {
            errorLabel.setText("All fields are required.");
            return;
        }

        if (!email.matches("^[\\w.%+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            errorLabel.setText("Please enter a valid email address.");
            return;
        }

        if (password.length() < 4) {
            errorLabel.setText("Password must be at least 4 characters.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            errorLabel.setText("Passwords do not match.");
            return;
        }

        if ("consultant".equals(role)) {
            if (consultantRepo.findByEmail(email) != null) {
                errorLabel.setText("An account with this email already exists.");
                return;
            }
            ConsultantRole selectedRole = (ConsultantRole) consultantRoleCombo.getSelectedItem();
            if (selectedRole == null) selectedRole = ConsultantRole.TRAINER;
            Consultant c = new Consultant(0, firstName, lastName, phone, email, password, false, selectedRole);
            consultantRepo.insert(c);
            JOptionPane.showMessageDialog(this,
                    "Your account has been submitted as " + selectedRole.name() + ".\nA Manager must approve it before you can log in.",
                    "Pending Approval", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            return;
        } else {
            if (traineeRepo.findByEmail(email) != null) {
                errorLabel.setText("An account with this email already exists.");
                return;
            }
            PreferredUpdateMethod method = (PreferredUpdateMethod) updateMethodCombo.getSelectedItem();
            Trainee t = new Trainee(null, firstName, lastName, phone, email, password, method);
            int newId = traineeRepo.insert(t);
            AuthenticationService.getInstance().login(newId, "trainee", firstName + " " + lastName);
        }

        signedUp = true;
        dispose();
    }

    public boolean isSignedUp() {
        return signedUp;
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
