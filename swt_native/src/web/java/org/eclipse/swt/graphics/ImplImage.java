package org.eclipse.swt.graphics;

import java.io.*;
import java.util.*;
import java.util.function.*;
import org.eclipse.swt.*;

public interface ImplImage extends ImplResource {

    GC _memGC();

    int _width();

    int _height();

    void createAlpha();
}
