package org.eclipse.swt.graphics;

import java.util.*;
import java.util.stream.*;
import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;

public interface ImplGC extends ImplResource {

    Drawable _drawable();

    GCData _data();

    int getZoom();
}
