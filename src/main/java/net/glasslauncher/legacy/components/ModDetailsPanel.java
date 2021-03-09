package net.glasslauncher.legacy.components;

import net.glasslauncher.common.CommonConfig;
import net.glasslauncher.legacy.Config;
import net.glasslauncher.legacy.util.HtmlImgHijacker;
import org.commonmark.Extension;
import org.commonmark.ext.autolink.AutolinkExtension;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.*;

public abstract class ModDetailsPanel extends JPanel {

    ArrayList<Component> componentArrayList = new ArrayList<>();

    private final JPanel buttons = new JPanel();

    protected JWebView description;
    protected JTextPane name = new JTextPaneFancy();

    private static final ArrayList<Extension> EXTENSIONS = new ArrayList<Extension>(){{add(AutolinkExtension.create());}};
    protected static final HtmlRenderer RENDERER = HtmlRenderer.builder().extensions(EXTENSIONS).attributeProviderFactory((c) -> new HtmlImgHijacker()).build();
    protected static final Parser PARSER = Parser.builder().extensions(EXTENSIONS).build();

    String instance;

    public ModDetailsPanel(String instance) {
        this.instance = instance;

        setBounds(230, 20, 600, 400);
        setOpaque(false);

        name.setEditable(false);
        name.setFont(UIManager.getFont("Label.font").deriveFont(18f));
        name.setBorder(new EmptyBorder(4, 4, 4, 4));
        name.setContentType("text/html");
        name.setText("<head><base href=\"file://" + CommonConfig.GLASS_PATH + "cache/repo-images\"><style>" + Config.CSS + "</style></head><body><div style=\"font-size: 18px;\">" + "Select a mod to see its details!" + "</div></body>");

        //description.setOpaque(false);
        //description.setBorder(new EmptyBorder(0, 0, 0, 0));
        description = new JWebView("Empty");
        description.getJScrollPane().setBounds(20, 70, getWidth()-40, getHeight()-90);
        description.getJScrollPane().setPreferredSize(new Dimension(getWidth()-40, getHeight()-90));

        setupButtons(buttons);

        buttons.setOpaque(false);
        buttons.setSize(getWidth()-40, 30);
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));

        add(name);
        add(description.getJScrollPane());
        add(buttons);
    }

    abstract void setMod(Object localMod);

    abstract void onModChange();

    abstract void setupButtons(JPanel buttons);

    public void addToOnModChange(Component component) {
        componentArrayList.add(component);
    }
}
