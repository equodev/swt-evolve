package org.eclipse.swt.widgets;

/**
 * Per-OS native init shared by the two Display surfaces on macOS — the web one ({@link WebDisplayBridge}) and the desktop-native one ({@link DeskDisplayBridge}). On macOS
 * nothing has to be initialized from Java: the desktop-native window sets up {@code NSApp} natively
 * in {@code bridge.swift}, and the web surface drives a browser/Chromium.
 */
final class DisplayBridgePlatform {
    private DisplayBridgePlatform() {
    }

    static void init() {
        // macOS: no Java-side native init required.
    }
}
