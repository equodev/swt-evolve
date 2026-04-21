package org.eclipse.swt.custom;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public interface ImplCTabFolderRenderer {

    int[] _curve();

    int[] _topCurveHighlightStart();

    int[] _topCurveHighlightEnd();

    int _curveWidth();

    int _curveIndent();

    int _lastTabHeight();

    Color _fillColor();

    Color _selectionHighlightGradientBegin();

    Color[] _selectionHighlightGradientColorsCache();

    Color _selectedOuterColor();

    Color _selectedInnerColor();

    Color _tabAreaColor();

    Color _lastBorderColor();
}
