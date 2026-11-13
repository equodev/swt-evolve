package org.eclipse.swt.widgets;

import java.util.*;
import org.eclipse.swt.*;

public class VFileDialog extends VDialog {

    protected VFileDialog() {
    }

    protected VFileDialog(DartFileDialog impl) {
        super(impl);
    }

    public String getFileName() {
        return ((DartFileDialog) impl).getFileName();
    }

    public void setFileName(String value) {
        ((DartFileDialog) impl).fileName = value;
    }

    public String[] getFileNames() {
        String[] values = ((DartFileDialog) impl).fileNames;
        if (values == null)
            return null;
        int kept = 0;
        for (String v : values) if (v != null)
            kept++;
        if (kept == values.length)
            return values;
        String[] result = new String[kept];
        kept = 0;
        for (String v : values) if (v != null)
            result[kept++] = v;
        return result;
    }

    public void setFileNames(String[] value) {
        ((DartFileDialog) impl).fileNames = value;
    }

    public String[] getFilterExtensions() {
        String[] values = ((DartFileDialog) impl).filterExtensions;
        if (values == null)
            return null;
        int kept = 0;
        for (String v : values) if (v != null)
            kept++;
        if (kept == values.length)
            return values;
        String[] result = new String[kept];
        kept = 0;
        for (String v : values) if (v != null)
            result[kept++] = v;
        return result;
    }

    public void setFilterExtensions(String[] value) {
        ((DartFileDialog) impl).filterExtensions = value;
    }

    public int getFilterIndex() {
        return ((DartFileDialog) impl).getFilterIndex();
    }

    public void setFilterIndex(int value) {
        ((DartFileDialog) impl).filterIndex = value;
    }

    public String[] getFilterNames() {
        String[] values = ((DartFileDialog) impl).filterNames;
        if (values == null)
            return null;
        int kept = 0;
        for (String v : values) if (v != null)
            kept++;
        if (kept == values.length)
            return values;
        String[] result = new String[kept];
        kept = 0;
        for (String v : values) if (v != null)
            result[kept++] = v;
        return result;
    }

    public void setFilterNames(String[] value) {
        ((DartFileDialog) impl).filterNames = value;
    }

    public String getFilterPath() {
        return ((DartFileDialog) impl).getFilterPath();
    }

    public void setFilterPath(String value) {
        ((DartFileDialog) impl).filterPath = value;
    }

    public boolean getOverwrite() {
        return ((DartFileDialog) impl).getOverwrite();
    }

    public void setOverwrite(boolean value) {
        ((DartFileDialog) impl).overwrite = value;
    }

    @Override
    public int wireTypeId() {
        return 38;
    }

    @Override
    public void writeWireFields(dev.equo.swt.WidgetWire w) {
        super.writeWireFields(w);
        w.key(234);
        w.write(getFileName());
        w.key(235);
        w.write(getFileNames());
        w.key(236);
        w.write(getFilterExtensions());
        w.key(237);
        w.write((int) getFilterIndex());
        w.key(238);
        w.write(getFilterNames());
        w.key(239);
        w.write(getFilterPath());
        w.key(240);
        w.write(getOverwrite());
    }
}
