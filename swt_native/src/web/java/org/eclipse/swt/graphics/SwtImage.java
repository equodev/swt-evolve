package org.eclipse.swt.graphics;

import java.util.concurrent.CompletableFuture;

public class SwtImage {
    public String filename;
    public GC memGC;
    public CompletableFuture<Void> pendingRenderFuture;

    public void updateImageData(ImageData newData) {
    }

    public Image getApi() { return null; }
}