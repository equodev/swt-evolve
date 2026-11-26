package dev.equo.swt;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.management.ManagementFactory;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * The bridge's work as trace events on the monotonic clock ({@link System#nanoTime}, which the Dart VM
 * timeline and the Flutter engine's events share on Linux and macOS), one JSON object per line in the
 * file {@code dev.equo.swt.traceTap} names: each flush as a span, each widget frame, GC paint and
 * message sent, and each event that arrived, as an instant. Read next to a timeline taken over the
 * VM service, it shows a client's frame and the Java work that fed it on one time axis.
 * Installed only under that property; otherwise this class is never loaded.
 */
final class TraceTap extends BridgeTap {

    /** Lines not yet written; a line that finds it full is dropped rather than held. */
    private final BlockingQueue<String> pending = new ArrayBlockingQueue<>(1 << 16);
    private final String pid = processId();

    TraceTap(String file) {
        Thread writer = new Thread(() -> {
            try (Writer out = new FileWriter(file, true)) {
                while (true) {
                    out.write(pending.take());
                    String line;
                    while ((line = pending.poll()) != null) out.write(line);
                    out.flush();
                }
            } catch (IOException | InterruptedException e) {
                // The trace stops; the application carries on.
            }
        }, "evolve-trace-tap");
        writer.setDaemon(true);
        writer.start();
    }

    /** The JVM's process id: the part of its runtime name before the {@code @}. */
    private static String processId() {
        String name = ManagementFactory.getRuntimeMXBean().getName();
        int at = name.indexOf('@');
        String pid = at > 0 ? name.substring(0, at) : "";
        return pid.chars().allMatch(Character::isDigit) && !pid.isEmpty() ? pid : "0";
    }

    private static long micros(long nanos) {
        return nanos / 1000;
    }

    private void span(String name, long fromNanos, long toNanos, String args) {
        pending.offer("{\"ph\":\"X\",\"name\":\"" + name + "\",\"ts\":" + micros(fromNanos) + ",\"dur\":" + micros(toNanos - fromNanos)
                + ",\"pid\":" + pid + ",\"tid\":\"java-" + Thread.currentThread().getName() + "\"" + (args == null ? "" : ",\"args\":" + args) + "}\n");
    }

    private void instant(String name, String args) {
        pending.offer("{\"ph\":\"i\",\"name\":\"" + name + "\",\"ts\":" + micros(System.nanoTime())
                + ",\"pid\":" + pid + ",\"tid\":\"java-" + Thread.currentThread().getName() + "\"" + (args == null ? "" : ",\"args\":" + args) + "}\n");
    }

    @Override
    void mark(String name) {
        instant(name, null);
    }

    @Override
    void span(String name, long startNanos) {
        span(name, startNanos, System.nanoTime(), null);
    }

    @Override
    boolean tracing() {
        return true;
    }

    @Override
    long flushStarted() {
        return System.nanoTime();
    }

    @Override
    long flushSending(long started) {
        return System.nanoTime();
    }

    @Override
    void flushEnded(long started, long sending) {
        if (started == 0) return;
        long now = System.nanoTime();
        span("Java.flush.write", started, sending == 0 ? now : sending, null);
        if (sending != 0) span("Java.flush.send", sending, now, null);
    }

    /** {@code dev.equo.swt.traceTap.records}: each widget frame also lists its records, as channel and changed keys. */
    private final boolean records = Boolean.getBoolean("dev.equo.swt.traceTap.records");

    @Override
    void widgetFrame(WidgetWire frame) {
        String detail = "";
        if (records) {
            StringBuilder sb = new StringBuilder();
            try {
                byte[] payload = frame.payload();
                for (WireDump.Record r : WireDump.records(payload, 0, payload.length)) {
                    sb.append(r.channel()).append(':').append(String.join(",", r.properties.keySet())).append(';');
                }
            } catch (RuntimeException e) {
                sb.append("unreadable");
            }
            detail = ",\"list\":\"" + sb.toString().replace("\\", "\\\\").replace("\"", "'") + "\"";
        }
        instant("Java.widgets", "{\"records\":" + frame.records() + ",\"bytes\":" + frame.byteSize() + detail + "}");
    }

    @Override
    void gcFrame(GcWire frame, Object target) {
        String on = target == null ? "image" : target.getClass().getName() + "@" + System.identityHashCode(target);
        if (target instanceof org.eclipse.swt.widgets.Control) {
            org.eclipse.swt.widgets.Control c = (org.eclipse.swt.widgets.Control) target;
            org.eclipse.swt.widgets.Composite p = c.getParent();
            on += " in " + (p == null ? "-" : p.getClass().getName());
            Object data = c.getData();
            if (data != null) on += " data " + data.getClass().getName();
        }
        // A hash of what was sent: the same value twice for one GC is a paint that showed nothing new.
        byte[] buf = frame.buffer();
        int hash = 1, size = frame.byteSize();
        for (int i = frame.headerLength(); i < size && i < buf.length; i++) hash = 31 * hash + buf[i];
        instant("Java.gc", "{\"bytes\":" + size + ",\"ch\":\"" + frame.channel() + "\",\"on\":\"" + on + "\",\"hash\":" + hash + "}");
    }

    @Override
    void sent(byte[] buffer, int payloadStart, int length) {
        instant("Java.send:" + FlutterBridge.channelOf(buffer, payloadStart), "{\"bytes\":" + length + "}");
    }

    /** An event's position, as JSON fields to append; empty for anything but an SWT Event. */
    private static String at(Object ev) {
        if (!(ev instanceof org.eclipse.swt.widgets.Event)) return "";
        org.eclipse.swt.widgets.Event e = (org.eclipse.swt.widgets.Event) ev;
        return ",\"x\":" + e.x + ",\"y\":" + e.y;
    }

    @Override
    void received(Object widget, String channel, Object event) {
        instant("Java.recv:" + channel, "{\"widget\":\"" + (widget == null ? "" : widget.getClass().getSimpleName()) + "\"" + at(event) + "}");
    }

    @Override
    void received(Object widget, String listener, String event, Object ev) {
        instant("Java.recv:" + listener + "/" + event, "{\"widget\":\"" + (widget == null ? "" : widget.getClass().getSimpleName()) + "\"" + at(ev) + "}");
    }

    @Override
    void skipped(Object widget, String listener, String event, String why) {
        instant("Java.skip:" + listener + "/" + event, null);
    }

    @Override
    void skipped(Object widget, String channel, String why) {
        instant("Java.skip:" + channel, null);
    }
}
