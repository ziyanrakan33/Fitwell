package fitwell.ui.components;

import fitwell.ui.theme.FWTheme;

import javax.swing.*;
import java.awt.*;

@SuppressWarnings({"serial","this-escape"})
public class SidebarButton extends JButton {
    private boolean selected = false;

    public SidebarButton(String text) {
        super(text);
        setHorizontalAlignment(SwingConstants.LEFT);
        setFocusPainted(false);
        setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setFont(new Font("SansSerif", Font.BOLD, 13));
        setForeground(FWTheme.TEXT_PRIMARY);
        setBackground(FWTheme.SIDEBAR_BG);
        setOpaque(true);
        refreshStyle();

        addChangeListener(e -> {
            if (!getModel().isPressed() && !selected) refreshStyle();
        });

        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                if (!selected) setBackground(FWTheme.CARD_BG);
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                if (!selected) refreshStyle();
            }
        });
    }

    public void setSelectedState(boolean selected) {
        this.selected = selected;
        refreshStyle();
    }

    private void refreshStyle() {
        if (selected) {
            setBackground(FWTheme.CARD_BG);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 3, 0, 0, FWTheme.ACCENT),
                    BorderFactory.createEmptyBorder(12, 11, 12, 14)
            ));
        } else {
            setBackground(FWTheme.SIDEBAR_BG);
            setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        }
    }
}