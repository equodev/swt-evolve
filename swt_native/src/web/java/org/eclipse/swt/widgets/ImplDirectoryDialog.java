package org.eclipse.swt.widgets;

import java.util.*;
import org.eclipse.swt.*;

public interface ImplDirectoryDialog extends ImplDialog {

    String _directoryPath();

    String _message();

    String _filterPath();

    long _method_performKeyEquivalent();

    long _methodImpl_performKeyEquivalent();
}
