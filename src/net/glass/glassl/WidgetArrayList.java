package net.glass.glassl;

import java.awt.*;
import java.util.ArrayList;

public class WidgetArrayList extends ArrayList<Component> {
    public void setStateAll(boolean state) {
        for (int i = 0; i == this.toArray().length -1; i++) {
            this.get(i).setEnabled(state);
        }
    }
}
