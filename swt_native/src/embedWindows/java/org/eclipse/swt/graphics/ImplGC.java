package org.eclipse.swt.graphics;

import java.util.*;
import java.util.function.*;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.Image.*;
import org.eclipse.swt.graphics.Region.*;

public interface ImplGC extends ImplResource {

    Drawable _drawable();

    GCData _data();

    int getZoom();
}
