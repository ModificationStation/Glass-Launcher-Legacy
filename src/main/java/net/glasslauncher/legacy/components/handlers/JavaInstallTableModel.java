package net.glasslauncher.legacy.components.handlers;

import net.glasslauncher.legacy.util.JavaFinder;

import javax.swing.table.*;
import java.util.*;

public class JavaInstallTableModel extends AbstractTableModel {
    private List<List<String>> installList = new ArrayList<>();
    private static final String[] HEADERS = new String[] {
            "Path",
            "Architecture"
    };

    public JavaInstallTableModel() {
    }

    public void setData(List<JavaFinder.JavaPath> installs) {
        List<List<String>> installList = new ArrayList<>();

        installs.forEach((java) -> {
            installList.add(new ArrayList<String>() {{
                add(java.getJavaPath());
                add(java.getArch().value);
            }});
        });
        this.installList = installList;
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return installList.size();
    }

    @Override
    public int getColumnCount() {
        return HEADERS.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return installList.get(rowIndex).get(columnIndex);
    }

    @Override
    public String getColumnName(int column) {
        return HEADERS[column];
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }


}
