package fitwell.ui.pro;

import fitwell.service.auth.AuthenticationService;
import fitwell.domain.user.Consultant;
import fitwell.domain.user.ConsultantRole;
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class LoginDialog extends JDialog {

    private final String role;
    private final JTextField emailField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final JLabel errorLabel = new JLabel(" ");
    private boolean authenticated = false;

    private final ConsultantRepository consultantRepo = new JdbcConsultantRepository();
    private final TraineeRepository traineeRepo = new JdbcTraineeRepository();

    private final AuthenticationService authenticationService;

    public LoginDialog(Window owner, String role, AuthenticationService authenticationService) {
        super(owner, "Login — " + capitalize(role), ModalityType.APPLICATION_MODAL);
        this.role = role;
        this.authenticationService = authenticationService;
        setSize(420, 370);
        setResizable(false);
        setLocationRelativeTo(owner);
        buildUi();
    }

    private void buildUi() {
        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(FWTheme.ENTRY_BG);
        root.setBorder(new EmptyBorder(28, 32, 24, 32));

        JLabel title = new JLabel("Sign in as " + capitalize(role));
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(FWTheme.ENTRY_TEXT);
        title.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 0, 3, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.gridx = 0;

        JLabel emailLabel = new JLabel("Email");
        emailLabel.setFont(FWTheme.FONT_BODY);
        emailLabel.setForeground(FWTheme.ENTRY_SUBTEXT);
        gbc.gridy = 0;
        form.add(emailLabel, gbc);

        styleTextField(emailField);
        gbc.gridy = 1;
        form.add(emailField, gbc);

        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(FWTheme.FONT_BODY);
        passwordLabel.setForeground(FWTheme.ENTRY_SUBTEXT);
        gbc.gridy = 2;
        gbc.insets = new Insets(10, 0, 3, 0);
        form.add(passwordLabel, gbc);

        styleTextField(passwordField);
        gbc.gridy = 3;
        gbc.insets = new Insets(3, 0, 3, 0);
        form.add(passwordField, gbc);

        errorLabel.setFont(FWTheme.FONT_BODY);
        errorLabel.setForeground(FWTheme.DANGER);
        errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 4;
        gbc.insets = new Insets(8, 0, 0, 0);
        form.add(errorLabel, gbc);

        KeyAdapter enterKey = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) attemptLogin();
            }
        };
        emailField.addKeyListener(enterKey);
        passwordField.addKeyListener(enterKey);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        buttons.setOpaque(false);

        JButton loginBtn = FWUi.primaryButton("Login");
        loginBtn.setPreferredSize(new Dimension(120, 38));
        loginBtn.addActionListener(e -> attemptLogin());

        JButton cancelBtn = FWUi.ghostDarkButton("Cancel");
        cancelBtn.setPreferredSize(new Dimension(120, 38));
        cancelBtn.addActionListener(e -> dispose());

        buttons.add(loginBtn);
        buttons.add(cancelBtn);

        JPanel bottomPanel = new JPanel(new BorderLayout(0, 10));
        bottomPanel.setOpaque(false);
        bottomPanel.add(buttons, BorderLayout.NORTH);

        JLabel signUpLink = new JLabel("Don't have an account? Sign up");
        signUpLink.setFont(FWTheme.FONT_BODY);
        signUpLink.setForeground(FWTheme.ACCENT);
        signUpLink.setHorizontalAlignment(SwingConstants.CENTER);
        signUpLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        signUpLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openSignUp();
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                signUpLink.setForeground(FWTheme.ACCENT_DARK);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                signUpLink.setForeground(FWTheme.ACCENT);
            }
        });
        bottomPanel.add(signUpLink, BorderLayout.SOUTH);

        root.add(title, BorderLayout.NORTH);
        root.add(form, BorderLayout.CENTER);
        root.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void styleTextField(JTextField field) {
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setPreferredSize(new Dimension(0, 36));
        field.setBackground(FWTheme.INPUT_BG);
        field.setForeground(FWTheme.TEXT_PRIMARY);
        field.setCaretColor(FWTheme.TEXT_PRIMARY);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FWTheme.BORDER, 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));
    }

    private void attemptLogin() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (email.isEmpty()) {
            errorLabel.setText("Please enter your email.");
            return;
        }
        if (password.isEmpty()) {
            errorLabel.setText("Please enter your password.");
            return;
        }

        AuthenticationService auth = authenticationService;

        if ("consultant".equals(role)) {
            Consultant c = consultantRepo.authenticate(email, password);
            if (c == null) {
                errorLabel.setText("Invalid email or password.");
                return;
            }
            if (!c.isApproved()) {
                errorLabel.setText("Your account is pending approval.");
                return;
            }
            ConsultantRole consultantRole = c.getRole() != null ? c.getRole() : ConsultantRole.TRAINER;
            auth.loginConsultant(c.getId(), consultantRole, c.getFirstName() + " " + c.getLastName());
        } else {
            Trainee t = traineeRepo.authenticate(email, password);
            if (t == null) {
                errorLabel.setText("Invalid email or password.");
                return;
            }
            auth.login(t.getId(), "trainee", t.fullName());
        }

        authenticated = true;
        dispose();
    }

    private void openSignUp() {
        dispose();
        SignUpDialog signUpDialog = new SignUpDialog(getOwner(), role, authenticationService);
        signUpDialog.setVisible(true);
        if (signUpDialog.isSignedUp()) {
            authenticated = true;
        }
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
