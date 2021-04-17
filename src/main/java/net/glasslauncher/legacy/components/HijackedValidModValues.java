package net.glasslauncher.legacy.components;

import com.google.gson.annotations.SerializedName;
import net.glasslauncher.legacy.util.ArrayUtils;
import net.glasslauncher.repo.api.mod.jsonobj.ModValues;

import java.util.Arrays;

public class HijackedValidModValues extends ModValues {

    private final String[] types;
    private final String[] categories;
    @SerializedName("minecraft_versions")
    private final String[] minecraftVersions;

    /**
     * Sorts the arrays alphabetically and prepends a None value. For use in sorters, etc.
     * @param validModValues The original mod values.
     */
    public HijackedValidModValues(ModValues validModValues) {
        String[] categories = validModValues.getCategories();
        Arrays.sort(categories);
        this.categories = ArrayUtils.addToBeginning(categories, "None");

        String[] types = validModValues.getTypes();
        Arrays.sort(types);
        this.types = ArrayUtils.addToBeginning(types, "None");

        this.minecraftVersions = validModValues.getMinecraftVersions();
    }

    public String[] getTypes() {
        return types;
    }

    public String[] getCategories() {
        return categories;
    }

    public String[] getMinecraftVersions() {
        return minecraftVersions;
    }
}
