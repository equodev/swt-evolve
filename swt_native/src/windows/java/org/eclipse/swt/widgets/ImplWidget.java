package org.eclipse.swt.widgets;

import java.util.*;
import java.util.stream.*;
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

public interface ImplWidget {

    Display _display();

    EventTable _eventTable();

    Object _data();

    void checkOpened();

    void release(boolean destroy);

    void sendEvent(Event event);

    void sendEvent(int eventType);

    void sendEvent(int eventType, Event event);

    void sendEvent(int eventType, Event event, boolean send);

    boolean sendFocusEvent(int type);

    boolean setTabGroupFocus();

    boolean setTabItemFocus();

    int getZoom();
}
