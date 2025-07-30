package org.eclipse.swt.custom;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import org.eclipse.swt.*;
import org.eclipse.swt.accessibility.*;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.printing.*;
import org.eclipse.swt.widgets.*;

public interface ImplStyledText extends ImplCanvas {

    Color _selectionBackground();

    Color _selectionForeground();

    StyledTextContent _content();

    StyledTextRenderer _renderer();

    Listener _listener();

    TextChangeListener _textChangeListener();

    int _verticalScrollOffset();

    int _horizontalScrollOffset();

    boolean _alwaysShowScroll();

    int _ignoreResize();

    int _topIndex();

    int _topIndexY();

    int _clientAreaHeight();

    int _clientAreaWidth();

    int _tabLength();

    int[] _tabs();

    int _leftMargin();

    int _topMargin();

    int _rightMargin();

    int _bottomMargin();

    Color _marginColor();

    int _columnX();

    Caret[] _carets();

    int[] _caretOffsets();

    int _caretAlignment();

    Point[] _selection();

    int[] _selectionAnchors();

    Point _clipboardSelection();

    Point _doubleClickSelection();

    boolean _editable();

    boolean _wordWrap();

    boolean _visualWrap();

    boolean _hasStyleWithVariableHeight();

    boolean _hasVerticalIndent();

    boolean _doubleClickEnabled();

    boolean _overwrite();

    int _textLimit();

    Map<Integer, Integer> _keyActionMap();

    boolean _customBackground();

    boolean _customForeground();

    boolean _enabled();

    boolean _insideSetEnableCall();

    Clipboard _clipboard();

    int _clickCount();

    int _autoScrollDirection();

    int _autoScrollDistance();

    int _lastTextChangeStart();

    int _lastTextChangeNewLineCount();

    int _lastTextChangeNewCharCount();

    int _lastTextChangeReplaceLineCount();

    int _lastTextChangeReplaceCharCount();

    int _lastCharCount();

    int _lastLineBottom();

    boolean _bidiColoring();

    Image _leftCaretBitmap();

    Image _rightCaretBitmap();

    int _caretDirection();

    int _caretWidth();

    Caret _defaultCaret();

    boolean _updateCaretDirection();

    boolean _dragDetect();

    IME _ime();

    Cursor _cursor();

    int _alignment();

    boolean _justify();

    int _indent();

    int _wrapIndent();

    int _lineSpacing();

    int _alignmentMargin();

    int _newOrientation();

    int _accCaretOffset();

    Accessible _acc();

    AccessibleControlAdapter _accControlAdapter();

    AccessibleAttributeAdapter _accAttributeAdapter();

    AccessibleEditableTextListener _accEditableTextListener();

    AccessibleTextExtendedAdapter _accTextExtendedAdapter();

    AccessibleAdapter _accAdapter();

    MouseNavigator _mouseNavigator();

    boolean _middleClickPressed();

    boolean _blockSelection();

    int _blockXAnchor();

    int _blockYAnchor();

    int _blockXLocation();

    int _blockYLocation();
}
