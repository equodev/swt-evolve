package dev.equo.swt;

import org.eclipse.swt.graphics.RGB;

/**
 * The system colours a named theme sets for itself, found through {@link java.util.ServiceLoader}.
 * {@code Display.getSystemColor} answers Evolve's own value for every colour no provider sets.
 */
public interface NamedThemeSystemColors {

    /**
     * The colour {@code id}, an {@code SWT.COLOR_*} constant, takes in {@code themeName}'s light or
     * dark variant; null keeps Evolve's.
     */
    RGB systemColor(String themeName, boolean dark, int id);
}
