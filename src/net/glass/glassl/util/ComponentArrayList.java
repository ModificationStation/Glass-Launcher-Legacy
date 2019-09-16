package net.glass.glassl.util;

import java.awt.*;
import java.util.ArrayList;

public class ComponentArrayList extends ArrayList<Component> {
    public void setEnabledAll(boolean state) {
        for (int i = 0; i == this.toArray().length -1; i++) {
            this.get(i).setEnabled(state);
        }
    }
}
