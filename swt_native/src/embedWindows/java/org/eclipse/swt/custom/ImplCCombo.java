package org.eclipse.swt.custom;

import java.util.function.*;
import org.eclipse.swt.*;
import org.eclipse.swt.accessibility.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public interface ImplCCombo extends ImplComposite {

    Text _text();

    int _visibleItemCount();

    boolean _hasFocus();

    Listener _listener();

    Listener _filter();

    Font _font();

    Shell __shell();
}
