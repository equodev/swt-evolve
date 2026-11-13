package org.eclipse.swt.graphics;

import org.eclipse.swt.*;

public class VFontData implements dev.equo.swt.WireObject {

    protected VFontData() {
    }

    protected VFontData(DartFontData impl) {
        this.impl = impl;
    }

    protected DartFontData impl;

    public int getHeight() {
        return ((DartFontData) impl).getHeight();
    }

    public void setHeight(int value) {
        ((DartFontData) impl).getApi().height = value;
    }

    public String getLocale() {
        return ((DartFontData) impl).getLocale();
    }

    public void setLocale(String value) {
        ((DartFontData) impl).lang = value;
    }

    public String getName() {
        return dev.equo.swt.FontMetricsUtil.substituteFontName(((DartFontData) impl).getName());
    }

    public void setName(String value) {
        ((DartFontData) impl).name = value;
    }

    public int getStyle() {
        return ((DartFontData) impl).getStyle();
    }

    public void setStyle(int value) {
        ((DartFontData) impl).style = value;
    }

    @Override
    public int wireTypeId() {
        return 41;
    }

    @Override
    public void writeWireFields(dev.equo.swt.WidgetWire w) {
        w.key(242);
        w.write((int) getHeight());
        w.key(243);
        w.write(getName());
        w.key(244);
        w.write((int) getStyle());
    }
}
