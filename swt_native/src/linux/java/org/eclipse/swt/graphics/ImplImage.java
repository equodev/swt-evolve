package org.eclipse.swt.graphics;

import java.io.*;
import org.eclipse.swt.*;

public interface ImplImage extends ImplResource {

    int _transparentPixel();

    GC _memGC();

    int _width();

    int _height();
}
