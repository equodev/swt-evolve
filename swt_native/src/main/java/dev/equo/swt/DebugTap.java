package dev.equo.swt;

import dev.equo.swt.comm.CommService;

/**
 * The bridge's traffic as {@code send:}, {@code recv:} and {@code chk:} lines ({@link DebugLog}),
 * widget records included, decoded to their properties by name. Installed only under
 * {@code dev.equo.swt.debug}; otherwise this class is never loaded.
 */
final class DebugTap extends BridgeTap {

    @Override
    void wired(CommService comm) {
        // A reflective "run this on the UI thread" primitive (open a dialog by id, and so on) for
        // surfaces the Flutter action layer cannot drive. See TestUiRunner.
        comm.on(TestUiRunner.CHANNEL, Object.class, m -> TestUiRunner.handle(comm, m));
    }

    @Override
    void widgetFrame(WidgetWire frame) {
        DebugLog.logWidgetFrame(frame.payload());
    }

    @Override
    void sent(String eventName, byte[] buffer, int payloadStart, int length) {
        DebugLog.logSend(eventName, buffer, payloadStart, length - payloadStart);
    }

    @Override
    void gcFrame(GcWire frame) {
        DebugLog.logGcFrame(frame);
    }

    @Override
    void received(Object widget, String channel, Object event) {
        if (!printed(channel)) return;
        DebugLog.logRecv(FlutterBridge.event(widget, channel), event);
    }

    @Override
    void received(Object widget, String listener, String event, Object ev) {
        received(widget, listener + "/" + event, ev);
    }

    @Override
    void skipped(Object widget, String listener, String event, String why) {
        skipped(widget, listener + "/" + event, why);
    }

    @Override
    void receivedPayload(String eventName, Object payload) {
        if (!printed(eventName)) return;
        DebugLog.logRecvPayload(eventName, payload instanceof byte[] ? ((byte[]) payload).length + "B" : String.valueOf(payload));
    }

    @Override
    void skipped(Object widget, String channel, String why) {
        DebugLog.checkpoint(FlutterBridge.event(widget, channel), why);
    }

    @Override
    void skipped(String eventName, String why) {
        DebugLog.checkpoint(eventName, why);
    }

    /** A mouse move is printed only when the client's config asks for it: there is one per pixel. */
    private static boolean printed(String channel) {
        return !channel.contains("MouseMove") || Config.getConfigFlags().print_move;
    }
}
