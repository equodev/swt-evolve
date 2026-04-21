package org.eclipse.swt.graphics;

import org.eclipse.swt.*;

public interface ImplTextLayout extends ImplResource {

    Font _font();

    String _text();

    int _stylesCount();

    int _spacing();

    int _ascent();

    int _descent();

    int _indent();

    int _wrapIndent();

    int _verticalIndentInPoints();

    boolean _justify();

    int _alignment();

    int[] _tabs();

    int[] _segments();

    char[] _segmentsChars();

    int _wrapWidth();

    int _orientation();

    int[] _lineOffsets();

    int[] _invalidOffsets();
}
