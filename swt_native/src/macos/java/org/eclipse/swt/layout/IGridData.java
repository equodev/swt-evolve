package org.eclipse.swt.layout;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public interface IGridData {

    /**
     * Returns a string containing a concise, human-readable
     * description of the receiver.
     *
     * @return a string representation of the GridData object
     */
    String toString();

    GridData getApi();

    void setApi(GridData api);
}
