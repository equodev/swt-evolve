package org.eclipse.swt.graphics;

import java.io.*;
import java.util.*;
import java.util.Map.*;
import java.util.function.*;
import org.eclipse.swt.*;

public interface ImplImage extends ImplResource {

    int _transparentPixel();

    int _transparentColor();

    GC _memGC();
}
