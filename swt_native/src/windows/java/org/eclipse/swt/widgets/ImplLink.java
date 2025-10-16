package org.eclipse.swt.widgets;

import java.util.*;
import org.eclipse.swt.*;
import org.eclipse.swt.accessibility.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

public interface ImplLink extends ImplControl {

    String _text();

    int _linkForeground();

    String[] _ids();

    char[] _mnemonics();

    int _nextFocusItem();
}
