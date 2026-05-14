package org.eclipse.swt.widgets;

import java.text.*;
import java.text.AttributedCharacterIterator.*;
import java.text.DateFormat.*;
import java.util.*;
import org.eclipse.swt.*;
import org.eclipse.swt.accessibility.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

public interface ImplDateTime extends ImplComposite {

    int _day();

    int _month();

    int _year();

    int _hours();

    int _minutes();

    int _seconds();

    long _textEntryHandle();

    long _spinButtonHandle();

    long _containerHandle();

    long _calendarHandle();

    long _editableHandle();

    Calendar _calendar();

    Button _down();

    FieldPosition _currentField();

    StringBuilder _typeBuffer();

    int _typeBufferPos();

    boolean _firstTime();

    Color _fg();

    Color _bg();

    boolean _hasFocus();

    int _savedYear();

    int _savedMonth();

    int _savedDay();

    Shell _popupShell();

    DateTime _popupCalendar();

    Listener _popupListener();

    Listener _popupFilter();

    Point _prefferedSize();

    Locale _locale();

    Listener _mouseEventListener();
}
