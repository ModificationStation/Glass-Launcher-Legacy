package net.glasslauncher.legacy.util;

import net.glasslauncher.legacy.Main;
import org.commonmark.node.Image;
import org.commonmark.node.Node;
import org.commonmark.renderer.html.AttributeProvider;

import java.util.*;

public class HtmlImgHijacker implements AttributeProvider {
    @Override
    public void setAttributes(Node node, String tagName, Map<String, String> attributes) {
        if (node instanceof Image && attributes.get("src") != null) {
            String url = attributes.get("src");
            try {
                attributes.put("src", TempImageHandler.getImage(url));
                Main.LOGGER.info(TempImageHandler.getImage(url));
            } catch (Exception e) {
                Main.LOGGER.severe("Unable to hijack image in markdown.");
                e.printStackTrace();
            }
        }
    }
}
