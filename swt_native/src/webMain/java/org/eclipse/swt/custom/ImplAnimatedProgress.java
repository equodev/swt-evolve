package org.eclipse.swt.custom;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public interface ImplAnimatedProgress extends ImplCanvas {

    boolean _active();

    boolean _showStripes();

    int _value();

    int _orientation();

    boolean _showBorder();
}
