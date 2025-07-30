package org.eclipse.swt.layout;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;

public interface IRowData {

    /**
     * Returns a string containing a concise, human-readable
     * description of the receiver.
     *
     * @return a string representation of the RowData object
     */
    String toString();

    RowData getApi();

    void setApi(RowData api);
}
