package org.eclipse.swt.accessibility;

import java.net.*;
import java.util.*;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import java.util.List;

public interface ImplAccessible {

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

    Accessible _parent();

    Control _control();

    int _currentRole();

    Map<Integer, SWTAccessibleDelegate> _childToIdMap();

    SWTAccessibleDelegate _delegate();

    int _index();

    TableAccessibleDelegate _tableDelegate();

    void release(boolean destroy);
}
