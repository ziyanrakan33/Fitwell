package fitwell.ui.pro.components;

import fitwell.ui.pro.theme.FWTheme;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;

/**
 * Dark-card-safe text blocks (avoids white JTextArea background issues on some LAFs).
 */
public final class ProTextBlocks {

    private ProTextBlocks() {}

    public static JComponent description(String text) {
        JLabel lbl = new JLabel(toHtml(text));
        lbl.setForeground(FWTheme.TEXT_SECONDARY);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lbl.setVerticalAlignment(SwingConstants.TOP);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(lbl, BorderLayout.NORTH);
        return wrap;
    }

    public static JComponent monoBlock(String text) {
        JTextPane pane = new JTextPane();
        pane.setEditable(false);
        pane.setFocusable(false);
        pane.setOpaque(false);
        pane.setBorder(null);
        pane.setBackground(new Color(0, 0, 0, 0));

        StyledDocument doc = pane.getStyledDocument();
        SimpleAttributeSet attrs = new SimpleAttributeSet();
        StyleConstants.setForeground(attrs, FWTheme.TEXT_SECONDARY);
        StyleConstants.setFontFamily(attrs, "Monospaced");
        StyleConstants.setFontSize(attrs, 13);

        try {
            doc.insertString(doc.getLength(), text == null ? "" : text, attrs);
        } catch (BadLocationException ignored) {}

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(pane, BorderLayout.CENTER);
        return wrap;
    }

    public static JScrollPane darkScrollableTextArea(JTextArea area) {
        area.setBackground(FWTheme.CARD_BG);
        area.setForeground(FWTheme.TEXT_PRIMARY);
        area.setCaretColor(FWTheme.TEXT_PRIMARY);
        area.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane sp = new JScrollPane(area);
        sp.getViewport().setBackground(FWTheme.CARD_BG);
        sp.setBorder(BorderFactory.createLineBorder(FWTheme.BORDER, 1, true));
        sp.getVerticalScrollBar().setUnitIncrement(14);
        sp.getHorizontalScrollBar().setUnitIncrement(14);
        return sp;
    }

    private static String toHtml(String text) {
        if (text == null) text = "";
        String escaped = text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
        escaped = escaped.replace("\n", "<br>");
        return "<html><div style='color:#A0AEC5; line-height:1.35;'>" + escaped + "</div></html>";
    }
}