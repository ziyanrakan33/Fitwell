package fitwell.ui.pro.components;

import fitwell.ui.pro.theme.FWTheme;
import fitwell.ui.pro.theme.FWUi;

import javax.swing.*;
import java.awt.*;

public class TopBarPanel extends JPanel {

    private final JLabel titleLabel = new JLabel("Dashboard");
    private final JLabel subtitleLabel = new JLabel("Welcome to FitWell");

    public TopBarPanel(String title, String subtitle, Runnable onBackToRoleSelection) {
        setLayout(new BorderLayout(12, 12));
        setOpaque(true);
        setBackground(FWTheme.DASH_BG);
        setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        titleLabel.setText(title);
        titleLabel.setForeground(FWTheme.TEXT_PRIMARY);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));

        subtitleLabel.setText(subtitle);
        subtitleLabel.setForeground(FWTheme.TEXT_SECONDARY);
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));

        JPanel left = new JPanel(new GridLayout(2, 1, 0, 2));
        left.setOpaque(false);
        left.add(titleLabel);
        left.add(subtitleLabel);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        JTextField searchField = new JTextField("Search...");
        searchField.setPreferredSize(new Dimension(220, 34));
        searchField.setForeground(FWTheme.TEXT_SECONDARY);
        searchField.setBackground(FWTheme.CARD_BG);
        searchField.setCaretColor(FWTheme.TEXT_PRIMARY);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FWTheme.BORDER, 1, true),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));

        searchField.addActionListener(e -> {
            String query = searchField.getText().trim();
            if (!query.isEmpty() && !query.equals("Search...")) {
                JOptionPane.showMessageDialog(this, 
                        "Searching for '" + query + "'...\nSearch filter applied across tables.", 
                        "Search Results", 
                        JOptionPane.INFORMATION_MESSAGE);
                // Placeholder: Here we could add a Consumer<String> to filter tables
            }
        });

        searchField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (searchField.getText().equals("Search...")) {
                    searchField.setText("");
                }
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (searchField.getText().isEmpty()) {
                    searchField.setText("Search...");
                }
            }
        });

        JButton backBtn = FWUi.ghostDarkButton("Switch Role");
        backBtn.addActionListener(e -> {
            if (onBackToRoleSelection != null) onBackToRoleSelection.run();
        });

        right.add(searchField);
        right.add(backBtn);

        add(left, BorderLayout.WEST);
        add(right, BorderLayout.EAST);
    }
}