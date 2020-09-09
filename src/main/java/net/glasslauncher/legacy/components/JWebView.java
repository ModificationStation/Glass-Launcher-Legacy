package net.glasslauncher.legacy.components;

import com.sun.javafx.application.PlatformImpl;
import com.sun.javafx.webkit.Accessor;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import net.glasslauncher.legacy.Config;
import net.glasslauncher.legacy.util.LinkRedirector;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class JWebView extends JFXPanel {

    private Stage stage;
    private WebView browser;
    private WebEngine webEngine;

    public JWebView(String text) {
        super();

        PlatformImpl.startup(() -> {
            stage = new Stage();
            stage.setWidth(getWidth());
            stage.setHeight(getHeight());
            stage.setResizable(false);

            Group root = new Group();
            Scene scene = new Scene(root, getWidth(), getHeight());
            stage.setScene(scene);

            browser = new WebView();
            browser.setMaxSize(getWidth(), getHeight());
            webEngine = browser.getEngine();
            webEngine.getLoadWorker().stateProperty().addListener(new LinkRedirector(browser));
            webEngine.loadContent("<style>" + Config.CSS + "</style><body>" + text + "</body>");
            Accessor.getPageFor(webEngine).setBackgroundColor(new Color(52, 52, 52).getRGB());
            ObservableList<Node> children = root.getChildren();
            children.add(browser);

            setScene(scene);
        });
    }

    public void setText(String text) {
        PlatformImpl.runLater(() -> {
            webEngine.loadContent("<style>" + Config.CSS + "</style><body>" + text + "</body>");
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        int width = getWidth();
        int height = getHeight();
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.7f));
        g2d.setColor(new Color(0, 0, 0, 178));
        g2d.drawRoundRect(0, 0, width-1, height-1, 4, 4);
        g2d.setColor(new Color(76, 76, 76, 178));
        g2d.fillRoundRect(0, 0, width-1, height-1, 4, 4);
        super.paintComponent(g);
    }
}
