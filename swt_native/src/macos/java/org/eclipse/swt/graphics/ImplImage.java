package org.eclipse.swt.graphics;

import java.io.*;
import org.eclipse.swt.*;

public interface ImplImage extends ImplResource {

    GC _memGC();

    int _width();

    int _height();

    void createAlpha();
}
