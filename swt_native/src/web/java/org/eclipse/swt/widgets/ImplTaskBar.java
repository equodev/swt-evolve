package org.eclipse.swt.widgets;

import org.eclipse.swt.*;

public interface ImplTaskBar extends ImplWidget {

    int _itemCount();

    TaskItem[] _items();
}
