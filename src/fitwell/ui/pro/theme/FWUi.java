package fitwell.ui.pro.theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public final class FWUi {
    private FWUi() {}

    public static JPanel panel(Color bg) {
        JPanel p = new JPanel();
        p.setOpaque(true);
        p.setBackground(bg);
        return p;
    }

    public static JPanel cardPanel(Color bg, int radiusPadding) {
        JPanel p = new JPanel();
        p.setOpaque(true);
        p.setBackground(bg);
        p.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(FWTheme.BORDER, 1, true),
                new EmptyBorder(radiusPadding, radiusPadding, radiusPadding, radiusPadding)
        ));
        return p;
    }

    public static JButton primaryButton(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setFont(FWTheme.FONT_BUTTON);
        b.setForeground(Color.WHITE);
        b.setBackground(FWTheme.ACCENT);
        b.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addHoverColor(b, FWTheme.ACCENT, FWTheme.ACCENT_DARK);
        return b;
    }

    public static JButton ghostDarkButton(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setFont(FWTheme.FONT_BUTTON);
        b.setForeground(FWTheme.TEXT_PRIMARY);
        b.setBackground(FWTheme.CARD_BG);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FWTheme.BORDER, 1, true),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addHoverColor(b, FWTheme.CARD_BG, FWTheme.CARD_BG_HOVER);
        return b;
    }

    public static JButton ghostLightButton(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setFont(FWTheme.FONT_BUTTON);
        b.setForeground(FWTheme.ENTRY_TEXT);
        b.setBackground(Color.WHITE);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 226, 234), 1, true),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addHoverColor(b, Color.WHITE, new Color(245, 248, 252));
        return b;
    }

    public static void addHoverColor(AbstractButton b, Color normal, Color hover) {
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { b.setBackground(hover); }
            @Override public void mouseExited(MouseEvent e) { b.setBackground(normal); }
        });
    }

    public static JLabel title(String txt, Color color) {
        JLabel l = new JLabel(txt);
        l.setFont(FWTheme.FONT_TITLE);
        l.setForeground(color);
        return l;
    }

    public static JLabel subtitle(String txt, Color color) {
        JLabel l = new JLabel(txt);
        l.setFont(FWTheme.FONT_SUBTITLE);
        l.setForeground(color);
        return l;
    }
}