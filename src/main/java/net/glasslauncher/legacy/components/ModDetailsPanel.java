package net.glasslauncher.legacy.components;

import net.glasslauncher.repo.api.mod.jsonobj.Author;
import net.glasslauncher.repo.api.mod.jsonobj.Mod;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
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

    private static HtmlRenderer renderer = HtmlRenderer.builder().build();
    private static Parser parser = Parser.builder().build();

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

        description.setBorder(new EmptyBorder(6, 6, 6, 6));

        JScrollPane descriptionScroll = new JScrollPane();
        descriptionScroll.setViewportView(description);
        descriptionScroll.setOpaque(false);
        descriptionScroll.getViewport().setOpaque(false);
        descriptionScroll.setBorder(new EmptyBorder(0, 0, 0, 0));
        descriptionScroll.setBounds(20, 70, getWidth()-40, getHeight()-90);
        descriptionScroll.setPreferredSize(new Dimension(getWidth()-40, getHeight()-90));

        add(name);
        add(descriptionScroll);
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
