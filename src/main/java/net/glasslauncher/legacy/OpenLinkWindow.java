package net.glasslauncher.legacy;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;

public class OpenLinkWindow extends JOptionPane {

    /**
     * Used to make a pop-up clickable link.
     * @param comp
     * @param url
     */
    OpenLinkWindow(Component comp, String url) {
        JLabel label = new JLabel();
        Font font = label.getFont();

        String style = "font-family:" + font.getFamily() + ";" + "font-weight:" + (font.isBold() ? "bold" : "normal") + ";" +
                "font-size:" + font.getSize() + "pt;";
        JEditorPane link = new JEditorPane("text/html", "<html><body style=\"" + style + "\">" //
                + "<a href=\"" + url + "\">" + url + "</a>" //
                + "</body></html>");
        link.addHyperlinkListener(event -> {
            if (event.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
                try {
                    Desktop.getDesktop().browse(event.getURL().toURI());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        link.setEditable(false);
        link.setBackground(label.getBackground());
        showMessageDialog(comp, link, "Log Uploaded!", JOptionPane.INFORMATION_MESSAGE);
    }
}
