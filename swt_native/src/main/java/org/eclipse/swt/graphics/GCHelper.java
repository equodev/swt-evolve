package org.eclipse.swt.graphics;

import dev.equo.swt.FontMetricsUtil;
import dev.equo.swt.size.PointD;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;

public class GCHelper {

    /**
     * Computes the extent of a single line of text (no line breaks).
     */
    public static PointD computeLineExtent(String line, Font font) {
        return FontMetricsUtil.getFontSize(line, font);
    }

    /**
     * Computes the extent of the given string, processing flags for tab expansion,
     * line delimiters, and mnemonic characters.
     */
    public static Point textExtent(String string, int flags, Font font) {
        if (font == null) {
            font = Display.getCurrent().getSystemFont();
        }

        String text = string;

        // Process DRAW_MNEMONIC: remove '&' characters (mnemonic markers)
        if ((flags & SWT.DRAW_MNEMONIC) != 0) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if (c == '&' && i + 1 < text.length()) {
                    char next = text.charAt(i + 1);
                    if (next == '&') {
                        sb.append('&');
                        i++;
                    }
                    continue;
                }
                sb.append(c);
            }
            text = sb.toString();
        }

        // Process DRAW_TAB: replace tabs with spaces (8 spaces per tab)
        if ((flags & SWT.DRAW_TAB) != 0) {
            text = text.replace("\t", "        ");
        }

        // Process DRAW_DELIMITER: handle multiple lines
        if ((flags & SWT.DRAW_DELIMITER) != 0) {
            String[] lines = text.split("\r\n|\r|\n", -1);
            double maxWidth = 0;
            double totalHeight = 0;
            for (String line : lines) {
                PointD size = computeLineExtent(line, font);
                maxWidth = Math.max(maxWidth, size.x());
                totalHeight += size.y();
            }
            if (string.isEmpty()) {
                PointD size = computeLineExtent(" ", font);
                return new Point(0, (int) Math.round(size.y()));
            }
            return new Point((int) Math.round(maxWidth), (int) Math.round(totalHeight));
        }

        // No DRAW_DELIMITER: single line
        if (string.isEmpty()) {
            PointD size = computeLineExtent(" ", font);
            return new Point(0, (int) Math.round(size.y()));
        }
        return computeLineExtent(text, font).toPoint();
    }
}