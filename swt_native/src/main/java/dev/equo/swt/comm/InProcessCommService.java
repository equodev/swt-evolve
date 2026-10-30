package dev.equo.swt.comm;

import dev.equo.swt.FlutterNative;
import dev.equo.swt.Serializer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * {@link CommService} for a Flutter engine running inside this JVM: frames are handed to the Dart
 * isolate's port and back through the native bridge, with no socket in between.
 *
 * <p>Its {@link #getPort()} is negative, which is how the client knows to attach here instead of
 * opening a WebSocket. What arrives from the client is handled on a thread of its own, as a
 * WebSocket's worker thread would handle it: the client hands a frame over from its own thread
 * (the UI thread, on platforms that merge it with the platform thread), and handlers must not run
 * inside that call.
 */
public class InProcessCommService extends AbstractBinaryCommService {

    private static final AtomicInteger IDS = new AtomicInteger();
    private static final Map<Integer, InProcessCommService> BY_ID = new ConcurrentHashMap<>();
    private static final LinkedBlockingQueue<Runnable> RECEIVED = new LinkedBlockingQueue<>();
    private static volatile Boolean available;

    private final int id = IDS.incrementAndGet();
    private int clients;

    /** Whether the native bridge in this process carries the channel. */
    public static boolean isAvailable() {
        Boolean known = available;
        if (known == null) {
            synchronized (InProcessCommService.class) {
                known = available;
                if (known == null) {
                    try {
                        known = FlutterNative.openInProcessChannel(InProcessCommService.class);
                    } catch (LinkageError e) {
                        known = false;
                    }
                    available = known;
                }
            }
        }
        return known;
    }

    public InProcessCommService() {
        BY_ID.put(id, this);
        Receiver.start();
    }

    @Override
    public int getPort() {
        return -id;
    }

    @Override
    protected void broadcast(byte[] frame, int offset, int length) {
        FlutterNative.postInProcess(id, frame, offset, length);
    }

    @Override
    public void stop() {
        BY_ID.remove(id);
    }

    /** The thread that handles what clients send, started with the first channel. */
    private static final class Receiver {
        private static final Thread THREAD = new Thread(() -> {
            while (true) {
                try {
                    RECEIVED.take().run();
                } catch (InterruptedException e) {
                    return;
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
            }
        }, "comm-in-process");

        static {
            THREAD.setDaemon(true);
            THREAD.start();
        }

        static void start() {
        }
    }

    /** Called by the native bridge, from the client's thread, with a frame the client sent. */
    static void fromClient(int id, byte[] frame) {
        InProcessCommService comm = BY_ID.get(id);
        if (comm != null) RECEIVED.add(() -> comm.onBinaryMessage(frame, 0, frame.length));
    }

    /** Called by the native bridge, from the client's thread, when a client attaches or goes away. */
    static void clientsChanged(int id, int count) {
        InProcessCommService comm = BY_ID.get(id);
        if (comm != null) RECEIVED.add(() -> comm.setClients(count));
    }

    private void setClients(int count) {
        boolean attached = count > clients;
        clients = count;
        Serializer.clientsConnected(this, count);
        if (attached) onClientConnected(frame -> broadcast(frame, 0, frame.length));
    }
}
