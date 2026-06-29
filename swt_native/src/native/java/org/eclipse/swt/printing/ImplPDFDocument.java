package org.eclipse.swt.printing;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;

public interface ImplPDFDocument extends ImplDevice {

    long _pdfContext();

    boolean _isGCCreated();

    boolean _pageStarted();

    String _filename();

    double _widthInPoints();

    double _heightInPoints();
}
