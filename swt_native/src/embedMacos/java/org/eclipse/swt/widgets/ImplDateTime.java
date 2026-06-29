package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

public interface ImplDateTime extends ImplComposite {

    Shell _popupShell();

    DateTime _popupCalendar();

    int _savedYear();

    int _savedMonth();

    int _savedDay();

    Listener _clickListener();
}
