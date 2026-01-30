package org.eclipse.swt.accessibility;

import java.util.*;
import org.eclipse.swt.*;
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

    Accessible _parent();

    AccessibleObject _accessibleObject();

    Control _control();

    List<Accessible> _children();

    void release();
}
