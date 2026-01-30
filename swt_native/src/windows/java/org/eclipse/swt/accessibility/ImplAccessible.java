package org.eclipse.swt.accessibility;

import java.util.*;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.ole.win32.*;
import org.eclipse.swt.widgets.*;
import java.util.List;

public interface ImplAccessible {

    int _refCount();

    int _enumIndex();

    Runnable _timer();

    List<AccessibleListener> _accessibleListeners();

    List<AccessibleControlListener> _accessibleControlListeners();

    List<AccessibleTextListener> _accessibleTextListeners();

    List<AccessibleActionListener> _accessibleActionListeners();

    List<AccessibleEditableTextListener> _accessibleEditableTextListeners();

    List<AccessibleHyperlinkListener> _accessibleHyperlinkListeners();

    List<AccessibleTableListener> _accessibleTableListeners();

    List<AccessibleTableCellListener> _accessibleTableCellListeners();

    List<AccessibleTextExtendedListener> _accessibleTextExtendedListeners();

    List<AccessibleValueListener> _accessibleValueListeners();

    List<AccessibleAttributeListener> _accessibleAttributeListeners();

    Relation[] _relations();

    Object[] _variants();

    Accessible _parent();

    List<Accessible> _children();

    Control _control();

    int _uniqueID();

    int[] _tableChange();

    Object[] _textDeleted();

    Object[] _textInserted();

    ToolItem _item();
}
