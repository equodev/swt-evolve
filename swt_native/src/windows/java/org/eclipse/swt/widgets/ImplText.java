package org.eclipse.swt.widgets;

import java.util.*;
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

public interface ImplText extends ImplScrollable {

    int _tabs();

    int _oldStart();

    int _oldEnd();

    boolean _doubleClick();

    boolean _ignoreModify();

    boolean _ignoreVerify();

    boolean _ignoreCharacter();

    boolean _allowPasswordChar();

    String _message();

    int[] _segments();

    int _clearSegmentsCount();

    long _hwndActiveIcon();
}
