package net.glasslauncher.legacy.components;

import net.glasslauncher.legacy.Config;
import net.glasslauncher.repo.api.mod.jsonobj.Mod;
import org.commonmark.Extension;
import org.commonmark.ext.autolink.AutolinkExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;

public abstract class ModDetailsPanel extends JPanel {

    ArrayList<Component> componentArrayList = new ArrayList<>();

    private JWebView description = new JWebView("Empty");
    private JTextPane name = new JTextPaneFancy();

    private static ArrayList<Extension> extensions = new ArrayList<Extension>(){{add(AutolinkExtension.create());}};
    private static HtmlRenderer renderer = HtmlRenderer.builder().extensions(extensions).build();
    private static Parser parser = Parser.builder().extensions(extensions).build();

    Mod repoMod = null;
    net.glasslauncher.legacy.jsontemplate.Mod localMod = null;

    public ModDetailsPanel() {
        setBounds(230, 20, 600, 400);
        setOpaque(false);

        name.setEditable(false);
        name.setFont(UIManager.getFont("Label.font").deriveFont(18f));
        name.setBorder(new EmptyBorder(4, 4, 4, 4));
        name.setContentType("text/html");
        name.setText("<style>" + Config.CSS + "</style><body><div style=\"font-size: 18px;\">" + "Select a mod to see its details!" + "</div></body>");

        description.setOpaque(false);
        description.setBorder(new EmptyBorder(0, 0, 0, 0));
        description.setBounds(20, 70, getWidth()-40, getHeight()-90);
        description.setPreferredSize(new Dimension(getWidth()-40, getHeight()-90));

        add(name);
        add(description);
    }

    public void setRepoMod(Mod repoMod) {
        name.setText("<style>" + Config.CSS + "</style><body><div style=\"font-size: 18px;\">" + repoMod.getName() + " <sup style=\"font-size: 10px;\">by " + repoMod.getAuthors()[0].getUsername() + "</sup></div></body>");
        Node document = parser.parse(repoMod.getDescription());

        description.setText(renderer.render(document));

        this.repoMod = repoMod;

        onModChange();
    }

    public void setLocalMod(net.glasslauncher.legacy.jsontemplate.Mod mod) {
        name.setText(mod.getName());
        description.setText(mod.getDescription());

        this.localMod = mod;

        onModChange();
    }

    abstract void onModChange();

    public void addToOnModChange(Component component) {
        componentArrayList.add(component);
    }
}
