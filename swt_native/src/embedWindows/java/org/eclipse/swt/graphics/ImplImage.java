package org.eclipse.swt.graphics;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.function.*;
import java.util.stream.*;
import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;

public interface ImplImage extends ImplResource {

    GC _memGC();

    String _filename();

    void _filename(String filename);

    void _updateImageData(ImageData updateImageData);

    void _memGC(GC memGC);

    void _pendingRenderFuture(java.util.concurrent.CompletableFuture<Void> pendingRenderFuture);
}
