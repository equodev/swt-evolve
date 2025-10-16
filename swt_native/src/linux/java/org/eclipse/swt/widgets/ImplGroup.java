package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;

public interface ImplGroup extends ImplComposite {

    long _clientHandle();

    long _labelHandle();

    String _text();

    long parentingHandle();
}
