package net.glasslauncher.legacy.components;

import lombok.Getter;
import net.glasslauncher.common.CommonConfig;
import net.glasslauncher.legacy.Config;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.io.*;

public class JWebView {

    @Getter
    private final JEditorPane webview;

    @Getter
    private final JScrollPane jScrollPane;

    public JWebView(String text) {
        webview = new JEditorPane();
        webview.setContentType("text/html");
        setText(text);
        webview.setBorder(BorderFactory.createEmptyBorder());
        webview.setEditable(false);
        webview.addHyperlinkListener(event -> {
            if (event.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
                try {
                    Desktop.getDesktop().browse(event.getURL().toURI());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        jScrollPane = new JScrollPane(webview);
        jScrollPane.setBorder(BorderFactory.createEmptyBorder());
        jScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        jScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        jScrollPane.setAutoscrolls(false);
    }

    public void setText(String text) {
        SwingUtilities.invokeLater(() -> {
            try {
                webview.setText("<head><base href=\"" + (new File(CommonConfig.getGlassPath() + "cache/repo-images")).toURI().toURL() + "\"><style>" + Config.getCSS() + "</style></head><body>" + text + "</body>");
                webview.select(0, 0);
                webview.revalidate();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
