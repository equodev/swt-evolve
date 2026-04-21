package org.eclipse.swt.widgets;

import java.util.*;
import org.eclipse.swt.*;

public interface ImplFileDialog extends ImplDialog {

    String[] _filterNames();

    String[] _filterExtensions();

    String[] _fileNames();

    String _filterPath();

    String _fileName();

    String _fullPath();

    int _filterIndex();

    long _jniRef();

    long _method_overwriteExistingFileCheck();

    long _methodImpl_overwriteExistingFileCheck();

    long _method_performKeyEquivalent();

    long _methodImpl_performKeyEquivalent();

    boolean _overwrite();
}
