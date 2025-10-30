package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;

public interface ImplProgressBar extends ImplControl {

    int _timerId();

    int _minimum();

    int _maximum();

    int _selection();
}
