package org.eclipse.swt.widgets;

import org.eclipse.swt.*;

public class VTaskBar extends VWidget {

    protected VTaskBar() {
    }

    protected VTaskBar(DartTaskBar impl) {
        super(impl);
    }

    public TaskItem[] getItems() {
        TaskItem[] values = ((DartTaskBar) impl).items;
        if (values == null)
            return null;
        int kept = 0;
        for (TaskItem v : values) if (v != null)
            kept++;
        if (kept == values.length)
            return values;
        TaskItem[] result = new TaskItem[kept];
        kept = 0;
        for (TaskItem v : values) if (v != null)
            result[kept++] = v;
        return result;
    }

    public void setItems(TaskItem[] value) {
        ((DartTaskBar) impl).items = value;
    }

    @Override
    public String wireName(int key) {
        switch(key) {
        }
        return super.wireName(key);
    }

    @Override
    public boolean writeWire(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 671:
                return false;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 671:
                return dev.equo.swt.WidgetWire.UNHANDLED;
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VWidget.allWireKeys_(), new int[] {});

    protected static int[] allWireKeys_() {
        return wireKeys_;
    }

    @Override
    public int[] wireKeys() {
        return wireKeys_;
    }

    @Override
    public boolean wireAlways(int key) {
        return super.wireAlways(key);
    }

    @Override
    public boolean wireKeepsEmpty(int key) {
        return super.wireKeepsEmpty(key);
    }
}
