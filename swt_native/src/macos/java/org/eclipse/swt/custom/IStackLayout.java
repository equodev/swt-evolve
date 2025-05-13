package org.eclipse.swt.custom;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public interface IStackLayout extends ILayout {

    /**
     * Returns a string containing a concise, human-readable
     * description of the receiver.
     *
     * @return a string representation of the layout
     */
    String toString();

    StackLayout getApi();
}
