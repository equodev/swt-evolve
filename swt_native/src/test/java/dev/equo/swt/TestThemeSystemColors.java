package dev.equo.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;

/** A provider for the tests' own theme name only: it sets the light selection colour and nothing else. */
public final class TestThemeSystemColors implements NamedThemeSystemColors {

    public static final String THEME_NAME = "system-colors-test";
    public static final RGB LIGHT_SELECTION = new RGB(0x12, 0x34, 0x56);

    @Override
    public RGB systemColor(String themeName, boolean dark, int id) {
        return THEME_NAME.equals(themeName) && !dark && id == SWT.COLOR_LIST_SELECTION ? LIGHT_SELECTION : null;
    }
}
