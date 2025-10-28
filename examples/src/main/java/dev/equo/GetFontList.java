package dev.equo;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import java.util.*;

public class GetFontList {
    public static void main(String[] args) {
        Display display = new Display();
        FontData[] fontList = display.getFontList(null, true);

        Map<String, Set<String>> fonts = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        for (FontData fd : fontList) {
            String name = fd.getName();
            String styleName = getStyleName(fd.getStyle());
            fonts.computeIfAbsent(name, k -> new TreeSet<>()).add(styleName);
        }

        for (Map.Entry<String, Set<String>> entry : fonts.entrySet()) {
            String name = entry.getKey();
            String styles = String.join(", ", entry.getValue());
            System.out.println(name + " [" + styles + "]");
        }

        display.dispose();
    }

    private static String getStyleName(int style) {
        return switch (style) {
            case SWT.BOLD -> "Bold";
            case SWT.ITALIC -> "Italic";
            case SWT.BOLD | SWT.ITALIC -> "Bold Italic";
            default -> "Regular";
        };
    }
}
