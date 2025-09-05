package org.eclipse.swt.graphics;

import java.io.*;
import java.util.*;
import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;

public interface ImplImage extends ImplResource {

    int _transparentPixel();

    int _transparentColor();

    GC _memGC();

    int _width();

    int _height();
}
