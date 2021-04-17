package net.glasslauncher.legacy.util;

import java.util.Arrays;

public class ArrayUtils {

    public static <T> T[] addToBeginning(T[] elements, T element) {
        T[] newArray = Arrays.copyOf(elements, elements.length + 1);
        newArray[0] = element;
        System.arraycopy(elements, 0, newArray, 1, elements.length);

        return newArray;
    }
}
