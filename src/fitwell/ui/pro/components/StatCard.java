package fitwell.ui.pro.components;

import fitwell.ui.pro.theme.FWTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class StatCard extends JPanel {

    private final JLabel valueLabel = new JLabel("0");
    private final JLabel titleLabel = new JLabel("Metric");
    private final JLabel subLabel = new JLabel("-");

    public StatCard(String title, String value, String subText) {
        setLayout(new BorderLayout(8, 6));
        setOpaque(true);
        setBackground(FWTheme.CARD_BG);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FWTheme.BORDER, 1, true),
                new EmptyBorder(16, 16, 16, 16)
        ));

        titleLabel.setText(title);
        titleLabel.setForeground(FWTheme.TEXT_SECONDARY);
        titleLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));

        valueLabel.setText(value);
        valueLabel.setForeground(FWTheme.TEXT_PRIMARY);
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 28));

        subLabel.setText(subText);
        subLabel.setForeground(FWTheme.ACCENT);
        subLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

        add(titleLabel, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridLayout(2, 1, 0, 4));
        center.setOpaque(false);
        center.add(valueLabel);
        center.add(subLabel);

        add(center, BorderLayout.CENTER);
    }

    public void setValue(String value) {
        valueLabel.setText(value);
    }

    public void setSubText(String text) {
        subLabel.setText(text);
    }
}