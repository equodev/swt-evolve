package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;

public interface ImplTreeItem extends ImplItem {

    Tree _parent();

    Font _font();

    Font[] _cellFont();

    String[] _strings();

    boolean _cached();

    boolean _grayed();

    boolean _isExpanded();

    boolean _updated();

    boolean _settingData();
}
