package org.eclipse.swt.printing;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;

public interface ImplPrintDialog extends ImplDialog {

    PrinterData _printerData();

    int _returnCode();
}
