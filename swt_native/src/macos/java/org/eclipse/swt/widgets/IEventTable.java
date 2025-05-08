package org.eclipse.swt.widgets;

import java.util.*;
import org.eclipse.swt.*;

public interface IEventTable {

    Listener[] getListeners(int eventType);

    void hook(int eventType, Listener listener);

    boolean hooks(int eventType);

    void sendEvent(Event event);

    int size();

    void unhook(int eventType, Listener listener);

    void unhook(int eventType, EventListener listener);

    EventTable getApi();

    void setApi(EventTable api);
}
