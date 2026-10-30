package dev.equo.swt;

import dev.equo.swt.comm.CommService;

import java.util.ArrayList;
import java.util.List;

/**
 * What watches the bridge's traffic: the debug log, the bench's wire counters, a test's send
 * observer. Chosen once, from system properties, before the bridge first runs (see {@link #select}),
 * and held in a {@code static final} field: with nothing watching, every call is to {@link #NONE},
 * whose methods do nothing, and is compiled away, field read and all.
 */
abstract class BridgeTap {

    static final BridgeTap NONE = new BridgeTap() {
    };

    /** The tap the properties ask for: the debug log, the bench's counters, a test's observer. */
    static BridgeTap select() {
        List<BridgeTap> taps = new ArrayList<>();
        if (Config.isDebug()) taps.add(new DebugTap());
        if (Boolean.getBoolean("dev.equo.swt.wireStats")) taps.add(WireStats.TAP);
        if (Boolean.getBoolean("dev.equo.swt.observeSends")) taps.add(ObservedSends.TAP);
        if (taps.isEmpty()) return NONE;
        if (taps.size() == 1) return taps.get(0);
        return new Chain(taps.toArray(new BridgeTap[0]));
    }

    /** {@code tap} is installed: the property that selects it was set. */
    static boolean installed(BridgeTap tap) {
        BridgeTap current = FlutterBridge.TAP;
        if (current == tap) return true;
        if (current instanceof Chain) {
            for (BridgeTap t : ((Chain) current).taps) if (t == tap) return true;
        }
        return false;
    }

    /** A comm has just been given the bridge's own channels. */
    void wired(CommService comm) {
    }

    /** A flush starts; what it returns is handed back to {@link #flushSending} and {@link #flushEnded}. */
    long flushStarted() {
        return 0;
    }

    /** The flush has written everything and starts handing it to the transport. */
    long flushSending(long started) {
        return 0;
    }

    void flushEnded(long started, long sending) {
    }

    /** A frame of widget records goes out. */
    void widgetFrame(WidgetWire frame) {
    }

    /** A message goes out: {@code buffer[0, length)} is the frame, its payload from {@code payloadStart}. */
    void sent(byte[] buffer, int payloadStart, int length) {
    }

    /** A GC's paint goes out. */
    void gcFrame(GcWire frame) {
    }

    /** An event arrived for {@code widget} on {@code channel}, the part of its name after the widget. */
    void received(Object widget, String channel, Object event) {
    }

    /** {@link #received(Object, String, Object)} on the channel {@code listener + "/" + event}, not joined unless read. */
    void received(Object widget, String listener, String event, Object ev) {
    }

    /** {@link #skipped(Object, String, String)} on the channel {@code listener + "/" + event}. */
    void skipped(Object widget, String listener, String event, String why) {
    }

    /** A payload arrived on {@code eventName}, described by {@code payload}. */
    void receivedPayload(String eventName, Object payload) {
    }

    /** What arrived on {@code channel} for {@code widget} was not handed on as it came, and why. */
    void skipped(Object widget, String channel, String why) {
    }

    /** {@link #skipped} for a widget that could not be found, named by what addressed it. */
    void skipped(String eventName, String why) {
    }

    private static final class Chain extends BridgeTap {
        final BridgeTap[] taps;

        Chain(BridgeTap[] taps) {
            this.taps = taps;
        }

        @Override
        void wired(CommService comm) {
            for (BridgeTap t : taps) t.wired(comm);
        }

        @Override
        long flushStarted() {
            long started = 0;
            for (BridgeTap t : taps) started |= t.flushStarted();
            return started;
        }

        @Override
        long flushSending(long started) {
            long sending = 0;
            for (BridgeTap t : taps) sending |= t.flushSending(started);
            return sending;
        }

        @Override
        void flushEnded(long started, long sending) {
            for (BridgeTap t : taps) t.flushEnded(started, sending);
        }

        @Override
        void widgetFrame(WidgetWire frame) {
            for (BridgeTap t : taps) t.widgetFrame(frame);
        }

        @Override
        void sent(byte[] buffer, int payloadStart, int length) {
            for (BridgeTap t : taps) t.sent(buffer, payloadStart, length);
        }

        @Override
        void gcFrame(GcWire frame) {
            for (BridgeTap t : taps) t.gcFrame(frame);
        }

        @Override
        void received(Object widget, String channel, Object event) {
            for (BridgeTap t : taps) t.received(widget, channel, event);
        }

        @Override
        void received(Object widget, String listener, String event, Object ev) {
            for (BridgeTap t : taps) t.received(widget, listener, event, ev);
        }

        @Override
        void skipped(Object widget, String listener, String event, String why) {
            for (BridgeTap t : taps) t.skipped(widget, listener, event, why);
        }

        @Override
        void receivedPayload(String eventName, Object payload) {
            for (BridgeTap t : taps) t.receivedPayload(eventName, payload);
        }

        @Override
        void skipped(Object widget, String channel, String why) {
            for (BridgeTap t : taps) t.skipped(widget, channel, why);
        }

        @Override
        void skipped(String eventName, String why) {
            for (BridgeTap t : taps) t.skipped(eventName, why);
        }
    }
}
