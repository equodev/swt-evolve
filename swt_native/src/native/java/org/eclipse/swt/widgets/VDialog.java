package org.eclipse.swt.widgets;

import org.eclipse.swt.*;

public class VDialog implements dev.equo.swt.WireObject {

    protected VDialog() {
    }

    protected VDialog(IDialog impl) {
        this.impl = impl;
    }

    protected IDialog impl;

    public int getStyle() {
        return ((DartDialog) impl).getStyle();
    }

    public void setStyle(int value) {
        ((DartDialog) impl).style = value;
    }

    public String getText() {
        return ((DartDialog) impl).getText();
    }

    public void setText(String value) {
        ((DartDialog) impl).title = value;
    }

    protected String swt = "";

    protected long id = 0L;

    public String getSwt() {
        return swt;
    }

    public void setSwt(String v) {
        this.swt = v;
    }

    public long getId() {
        return id;
    }

    public void setId(long v) {
        this.id = v;
    }

    @Override
    public int wireTypeId() {
        return 29;
    }

    @Override
    public void writeWireFields(dev.equo.swt.WidgetWire w) {
        w.key(172);
        w.write((int) getStyle());
        w.key(174);
        w.write(getText());
        w.key(173);
        w.write(getSwt());
        w.key(171);
        w.write(getId());
    }
}
