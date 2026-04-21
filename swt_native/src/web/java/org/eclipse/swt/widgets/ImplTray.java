package org.eclipse.swt.widgets;

import org.eclipse.swt.*;

public interface ImplTray extends ImplWidget {

    int _itemCount();

    TrayItem[] _items();
}
