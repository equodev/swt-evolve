package org.eclipse.swt.browser;

import java.util.*;
import org.eclipse.swt.*;
import org.eclipse.swt.program.*;
import org.eclipse.swt.widgets.*;

public interface ImplBrowser extends ImplComposite {

    WebBrowser _webBrowser();

    int _userStyle();

    boolean _isClosing();
}
