package org.eclipse.swt.custom;

import java.util.*;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public interface ImplStyledTextRenderer {

    Device _device();

    StyledText _styledText();

    StyledTextContent _content();

    StyledTextLineSpacingProvider _lineSpacingProvider();

    boolean _lineSpacingComputing();

    Font _regularFont();

    Font _boldFont();

    Font _italicFont();

    Font _boldItalicFont();

    int _tabWidth();

    int _ascent();

    int _descent();

    int _averageCharWidth();

    int _tabLength();

    int _topIndex();

    TextLayout[] _layouts();

    int _lineCount();

    int _maxWidth();

    int _maxWidthLineIndex();

    float _averageLineHeight();

    int _linesInAverageLineHeight();

    boolean _idleRunning();

    Bullet[] _bullets();

    int[] _bulletsIndices();

    int[] _redrawLines();

    int[] _ranges();

    int _styleCount();

    StyleRange[] _styles();

    StyleRange[] _stylesSet();

    int _stylesSetCount();

    boolean _hasLinks();

    boolean _fixedPitch();
}
