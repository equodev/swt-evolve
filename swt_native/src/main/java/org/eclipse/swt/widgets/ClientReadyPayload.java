package org.eclipse.swt.widgets;

import dev.equo.swt.WireReader;
import dev.equo.swt.WireReadable;

/** What the client reports when it is ready: its fields numbered 1 to 12, in declaration order. */
public class ClientReadyPayload implements WireReadable {
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
    /** Device zoom in percent (100 = no scaling). 0 when the client can't report it yet. */
    public int zoom;
    /** Height of the chrome the client draws inside the window above the app content -- the CSD title strip. */
    public int insetTop;
    /** The fingerprint of the wire table the client was generated with; 0 when it sent none. */
    public int wireFingerprint;
    /**
     * The client's locale as a BCP 47 tag ({@code es-AR}): the browser's on web, the OS's on desktop. Null from a
     * client that does not report it.
     */
    public String locale;
    /**
     * The OS the client runs on, as Flutter's {@code TargetPlatform} name ({@code macOS}, {@code windows},
     * {@code linux}, {@code iOS}, {@code android}). Null from a client that does not report it.
     */
    public String targetPlatform;
    /**
     * The light/dark appearance of the system the client runs on ({@code dark} or {@code light}): the browser's on
     * web, the OS's on desktop. Null from a client that does not report it.
     */
    public String appearance;
    /**
     * Whether the host has a touch-aware pointer device attached and enabled, which is what
     * {@code Display.getTouchEnabled} answers. False from a client that cannot tell.
     */
    public boolean touchEnabled;

    @Override
    public boolean readWire(int key, WireReader r) {
        switch (key) {
            case 1: width = r.int32(); return true;
            case 2: height = r.int32(); return true;
            case 3: isFirst = r.bool(); return true;
            case 4: displayWidth = r.int32(); return true;
            case 5: displayHeight = r.int32(); return true;
            case 6: zoom = r.int32(); return true;
            case 7: insetTop = r.int32(); return true;
            case 8: wireFingerprint = r.int32(); return true;
            case 9: locale = r.string(); return true;
            case 10: targetPlatform = r.string(); return true;
            case 11: appearance = r.string(); return true;
            case 12: touchEnabled = r.bool(); return true;
        }
        return false;
    }

    /**
     * Fails unless the client numbers the wire as this Java does: generated from another table, every
     * number either side sends could mean something else to the other.
     */
    public void requireSameWireTable() {
        if (wireFingerprint != dev.equo.swt.WireTable.FINGERPRINT) {
            throw new IllegalStateException("the client's wire table (" + wireFingerprint + ") is not this Java's ("
                    + dev.equo.swt.WireTable.FINGERPRINT + "): the client and this Java were not generated together");
        }
    }
}
