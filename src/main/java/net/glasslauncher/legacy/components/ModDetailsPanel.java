package net.glasslauncher.legacy.components;

import net.glasslauncher.repo.api.mod.jsonobj.Author;
import net.glasslauncher.repo.api.mod.jsonobj.Mod;
import org.commonmark.Extension;
import org.commonmark.ext.autolink.AutolinkExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;

public abstract class ModDetailsPanel extends JPanel {

    ArrayList<Component> componentArrayList = new ArrayList<>();

    private JWebView description = new JWebView("Empty");
    private JTextArea name = new JTextAreaFancy("Select a mod to see its details!");
    private JTextArea authors = new JTextAreaFancy("None");

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

        authors.setEditable(false);
        authors.setFont(UIManager.getFont("Label.font").deriveFont(18f));
        authors.setForeground(new Color(128, 128, 128));
        authors.setBorder(new EmptyBorder(4, 4, 4, 4));

        description.setOpaque(false);
        description.setBorder(new EmptyBorder(0, 0, 0, 0));
        description.setBounds(20, 70, getWidth()-40, getHeight()-90);
        description.setPreferredSize(new Dimension(getWidth()-40, getHeight()-90));

        add(name);
        add(description);
    }

    public void setRepoMod(Mod repoMod) {
        name.setText(repoMod.getName());
        Node document = parser.parse(repoMod.getDescription());

        description.setText(renderer.render(document));
        Author[] authorsArr = repoMod.getAuthors();
        StringBuilder authorNames = new StringBuilder();
        for (Author author : Arrays.copyOfRange(authorsArr, 0, authorsArr.length - 1)) {
            authorNames.append(author.getUsername()).append(", ");
        }
        authorNames.append(authorsArr[authorsArr.length-1]);
        authors.setText(authorNames.toString());

        this.repoMod = repoMod;

        onModChange();
    }

    public void setLocalMod(net.glasslauncher.legacy.jsontemplate.Mod mod) {
        name.setText(mod.getName());
        description.setText(mod.getDescription());
        authors.setText(String.join(", ", mod.getAuthors()));

        this.localMod = mod;

        onModChange();
    }

    abstract void onModChange();

    public void addToOnModChange(Component component) {
        componentArrayList.add(component);
    }
}
