package org.eclipse.swt.widgets;

import java.util.*;
import org.eclipse.swt.*;

public interface ImplFileDialog extends ImplDialog {

    String[] _filterNames();

    String[] _filterExtensions();

    String[] _fileNames();

    String _filterPath();

    String _fileName();

    int _filterIndex();

    boolean _overwrite();
}
