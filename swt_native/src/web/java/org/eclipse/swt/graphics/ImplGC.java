package org.eclipse.swt.graphics;

import java.util.*;
import org.eclipse.swt.*;

public interface ImplGC extends ImplResource {

    Drawable _drawable();

    GCData _data();

    int _count();

    int _typeCount();

    byte[] _types();

    double[] _points();

    double[] _point();
}
