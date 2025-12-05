package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

public interface ImplCoolItem extends ImplItem {

    CoolBar _parent();

    Control _control();

    int _id();

    boolean _ideal();

    boolean _minimum();

    Point getSizeInPixels();
}
