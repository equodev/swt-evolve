package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;

public interface ImplTaskItem extends ImplItem {

    TaskBar _parent();

    Shell _shell();

    int _progress();

    int _iProgress();

    int _progressState();

    Image _overlayImage();

    String _overlayText();

    Menu _menu();
}
