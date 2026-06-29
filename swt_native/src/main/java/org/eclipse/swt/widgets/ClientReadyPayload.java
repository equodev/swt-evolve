package org.eclipse.swt.widgets;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.CompiledJson.ObjectFormatPolicy;

@CompiledJson(objectFormatPolicy = ObjectFormatPolicy.FULL)
public class ClientReadyPayload {
    /** The client viewport (window content) logical size — drives the main shell on desk, the Display on web. */
    public int width;
    public int height;
    public boolean isFirst;
    /**
     * The logical size of the monitor the window is on (the window's {@code FlutterView.display}).
     * Drives {@code Display.bounds} on desktop, where the Display is the screen rather than the window.
     * {@code 0} when the client can't report it (older clients, or no plausible display size); the
     * desk bridge then keeps its current/default Display bounds, and the web bridge ignores these.
     */
    public int displayWidth;
    public int displayHeight;
}
