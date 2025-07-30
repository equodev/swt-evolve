package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;

public class StyledCharacter {
    public char character;
    public Color foreground = null;
    public Color background = null;
    public int fontStyle = SWT.NORMAL;
    public Font font = null;
    public int borderStyle = SWT.NONE;
    public Color borderColor = null;
    public int underlineStyle = SWT.UNDERLINE_SINGLE;
    public boolean underline;
    public boolean strikeout = false;
    public int rise = 0;

    public int lineAlignment = SWT.LEFT;
    public int lineIndent = 0;
    public boolean lineJustify = false;

    public StyledCharacter(char c) {
        this.character = c;
    }

    public void applyStyle(StyleRange style) {
        if (style.foreground != null) this.foreground = style.foreground;
        if (style.background != null) this.background = style.background;

        if (style.fontStyle != SWT.NORMAL) this.fontStyle = style.fontStyle;
        if (style.font != null) this.font = style.font;

        if (style.borderStyle != SWT.NONE) this.borderStyle = style.borderStyle;
        if (style.borderColor != null) this.borderColor = style.borderColor;

        if (style.underline) {
            this.underline = style.underline;
            this.underlineStyle = style.underlineStyle;
        }

        if (style.strikeout) {
            this.strikeout = style.strikeout;
        }

        if (style.rise != 0) {
            this.rise = style.rise;
        }
    }
}