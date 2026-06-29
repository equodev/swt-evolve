package org.eclipse.swt.widgets;

import java.util.*;
import java.util.concurrent.*;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;

public interface ImplSynchronizer {

    Display _display();

    ConcurrentLinkedQueue<RunnableLock> _messages();

    Thread _syncThread();
}
