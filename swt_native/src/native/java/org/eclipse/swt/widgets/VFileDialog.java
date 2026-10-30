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
        ArrayList<String> result = new ArrayList<>(values.length);
        for (String v : values) if (v != null)
            result.add(v);
        return result.toArray(new String[0]);
    }

    public void setFileNames(String[] value) {
        ((DartFileDialog) impl).fileNames = value;
    }

    public String[] getFilterExtensions() {
        String[] values = ((DartFileDialog) impl).filterExtensions;
        if (values == null)
            return null;
        ArrayList<String> result = new ArrayList<>(values.length);
        for (String v : values) if (v != null)
            result.add(v);
        return result.toArray(new String[0]);
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
        ArrayList<String> result = new ArrayList<>(values.length);
        for (String v : values) if (v != null)
            result.add(v);
        return result.toArray(new String[0]);
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
        return 613;
    }

    @Override
    public void writeWireFields(dev.equo.swt.WidgetWire w) {
        super.writeWireFields(w);
        w.key(606);
        w.write(getFileName());
        w.key(607);
        w.write(getFileNames());
        w.key(608);
        w.write(getFilterExtensions());
        w.key(609);
        w.write((int) getFilterIndex());
        w.key(610);
        w.write(getFilterNames());
        w.key(611);
        w.write(getFilterPath());
        w.key(612);
        w.write(getOverwrite());
    }
}
