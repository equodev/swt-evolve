package org.eclipse.swt.widgets;

import java.util.*;
import java.util.stream.*;
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;

public interface ImplWidget {

    Display _display();

    EventTable _eventTable();

    void _eventTable(EventTable eventTable);

    Object _data();

    long _jniRef();

    void checkOpen();

    boolean isDrawing();

    void release(boolean destroy);

    void sendEvent(Event event);

    void sendEvent(int eventType);

    void sendEvent(int eventType, Event event);

    void sendEvent(int eventType, Event event, boolean send);

    boolean setTabGroupFocus();

    boolean setTabItemFocus();
}
