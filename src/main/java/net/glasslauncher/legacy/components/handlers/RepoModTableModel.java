package net.glasslauncher.legacy.components.handlers;

import net.glasslauncher.repo.api.mod.jsonobj.ModPreview;

import javax.swing.table.*;
import java.util.*;

public class RepoModTableModel extends AbstractTableModel {
    private List<List<String>> mods = new ArrayList<>();
    public List<String> ids = new ArrayList<>();
    private static final String[] HEADERS = new String[] {
            "ID",
            "Name",
            "Short Description",
            "Latest Version",
            "Minecraft Versions"
    };

    public RepoModTableModel() {
    }

    public void setMods(List<ModPreview> mods) {
        List<List<String>> modList = new ArrayList<>();
        List<String> idList = new ArrayList<>();

        mods.forEach((mod) -> {
            modList.add(new ArrayList<String>() {{
                add(mod.getId());
                add(mod.getName());
                add(mod.getShortDescription());
                add(mod.getLatestVersion());
                add(Arrays.toString(mod.getMinecraftVersions()).replaceAll("[\\[\\]]", ""));
            }});
            idList.add(mod.getId());
        });
        this.ids = idList;
        this.mods = modList;
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return mods.size();
    }

    @Override
    public int getColumnCount() {
        return 5;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return mods.get(rowIndex).get(columnIndex);
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
