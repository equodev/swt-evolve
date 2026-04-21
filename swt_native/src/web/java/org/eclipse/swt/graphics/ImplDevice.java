package org.eclipse.swt.graphics;

import org.eclipse.swt.*;

public interface ImplDevice {

    boolean _debug();

    boolean _tracking();

    Error[] _errors();

    Object[] _objects();

    Object _trackingLock();

    boolean _disposed();

    boolean _warnings();

    Color _COLOR_BLACK();

    Color _COLOR_DARK_RED();

    Color _COLOR_DARK_GREEN();

    Color _COLOR_DARK_YELLOW();

    Color _COLOR_DARK_BLUE();

    Color _COLOR_DARK_MAGENTA();

    Color _COLOR_DARK_CYAN();

    Color _COLOR_GRAY();

    Color _COLOR_DARK_GRAY();

    Color _COLOR_RED();

    Color _COLOR_TRANSPARENT();

    Color _COLOR_GREEN();

    Color _COLOR_YELLOW();

    Color _COLOR_BLUE();

    Color _COLOR_MAGENTA();

    Color _COLOR_CYAN();

    Color _COLOR_WHITE();

    Font _systemFont();

    Point _dpi();
}
