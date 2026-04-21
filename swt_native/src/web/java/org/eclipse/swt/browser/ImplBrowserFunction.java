package org.eclipse.swt.browser;

import java.util.*;
import org.eclipse.swt.*;

public interface ImplBrowserFunction {

    Browser _browser();

    String _name();

    String _functionString();

    int _index();

    boolean _isEvaluate();

    boolean _top();

    String _token();

    String[] _frameNames();
}
