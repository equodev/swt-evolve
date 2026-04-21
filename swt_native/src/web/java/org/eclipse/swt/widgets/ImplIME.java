package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;

public interface ImplIME extends ImplWidget {

    Canvas _parent();

    int _caretOffset();

    int _startOffset();

    int _commitCount();

    String _text();

    int[] _ranges();

    TextStyle[] _styles();
}
