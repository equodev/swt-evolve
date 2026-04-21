package org.eclipse.swt.widgets;

import java.util.*;
import org.eclipse.swt.*;

public interface ImplMessageBox extends ImplDialog {

    String _message();

    int _userResponse();
}
