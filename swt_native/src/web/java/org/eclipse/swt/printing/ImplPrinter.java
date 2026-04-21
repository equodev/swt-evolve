package org.eclipse.swt.printing;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;

public interface ImplPrinter extends ImplDevice {

    PrinterData _data();

    boolean _isGCCreated();
}
