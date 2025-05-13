package org.eclipse.swt.layout;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public interface IRowLayout extends ILayout {

    /**
     * Returns a string containing a concise, human-readable
     * description of the receiver.
     *
     * @return a string representation of the layout
     */
    String toString();

    RowLayout getApi();
}
