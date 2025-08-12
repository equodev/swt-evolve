package org.eclipse.swt.layout;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;

public interface IFormAttachment {

    /**
     * Returns a string containing a concise, human-readable
     * description of the receiver.
     *
     * @return a string representation of the FormAttachment
     */
    String toString();

    FormAttachment getApi();

    void setApi(FormAttachment api);
}
