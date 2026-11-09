package dev.equo.swt.comm;

import dev.equo.swt.Config;
import dev.equo.swt.Serializer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

/**
 * Transport-agnostic core of the binary comm protocol shared by every {@link CommService}
 * implementation. Subclasses provide only the WebSocket transport (server lifecycle, session
 * tracking, per-session send and {@link #getPort()}); everything wire-format related lives here.
 *
 * <p>Wire format: the channel as {@link ChannelHeader} writes it, then the payload. Frames sent
 * before the first client connects are buffered and flushed on connect (see
 * {@link #onClientConnected}).
 */
public abstract class AbstractBinaryCommService implements CommService {

    /**
     * The address the server binds, which the desktop client connects to as a literal: a name would
     * cost it a lookup, answered on a turn of the window's main thread, before it could connect.
     */
    public static final String LOOPBACK = "127.0.0.1";

    protected final Serializer serializer = new Serializer();
    private final Map<Object, Object> attachments = new ConcurrentHashMap<>();

    /*
     * Handlers by the numbers a message's header carries: a widget's own and its owner handler by its
     * type and id (see ownerKey), a global channel's by its number, and one for every widget of a
     * type by the type's number.
     */
    private final LongTable<Numbered> numbered = new LongTable<>();
    private volatile TypedHandler<?>[] globals = new TypedHandler<?>[0];
    private volatile TypeHandler<?>[] typesByNumber = new TypeHandler<?>[0];
    /** The other type number a widget may be addressed under, by type number; see {@link #setAlternateTypes}. */
    private static volatile int[] alternateTypes = new int[0];
    private final String logTag = "[" + getClass().getSimpleName() + "]";
    private final List<byte[]> pendingFrames = new ArrayList<>();
    private volatile boolean firstClientConnected = false;
    /** Set while the first client is handed the held frames; guarded by {@link #pendingFrames}. */
    private boolean handingOver;

    /**
     * The client frames are being written for. Starts at 1, not 0: frames sent before anyone
     * connects are buffered and handed to the first client on connect, so they genuinely reach it
     * and are rightly credited to it. Each session after that gets a fresh id, which is what makes
     * a reconnecting client be described in full rather than referred to by name.
     *
     * <p>Drawn from one count for every comm, so an id names one client in the whole JVM: what is
     * recorded as delivered (an image shared by two Displays, say) is recorded against the client
     * it reached and no other.
     */
    private final java.util.concurrent.atomic.AtomicInteger connection =
            new java.util.concurrent.atomic.AtomicInteger(NEXT_CONNECTION.getAndIncrement());

    private static final java.util.concurrent.atomic.AtomicInteger NEXT_CONNECTION =
            new java.util.concurrent.atomic.AtomicInteger(1);

    /** Wire bytes each way, for measuring what a paint or an event costs in traffic. */
    private final java.util.concurrent.atomic.LongAdder bytesSent = new java.util.concurrent.atomic.LongAdder();
    private final java.util.concurrent.atomic.LongAdder bytesReceived = new java.util.concurrent.atomic.LongAdder();

    public long bytesSent() {
        return bytesSent.sum();
    }

    public long bytesReceived() {
        return bytesReceived.sum();
    }

    /** Whether a client has connected; until one does, every frame sent is buffered for it. */
    public boolean hasClient() {
        return firstClientConnected;
    }

    @Override
    public int connectionId() {
        return connection.get();
    }

    @Override
    public void send(int type, long id, int channel, byte[] payload) {
        int payloadLength = payload != null ? payload.length : 0;
        byte[] frame = SCRATCH.get();
        if (frame.length < ChannelHeader.MAX_NUMBERED + payloadLength) {
            frame = new byte[ChannelHeader.MAX_NUMBERED + payloadLength];
            if (frame.length <= MAX_SCRATCH) SCRATCH.set(frame);
        }
        int headerLength = ChannelHeader.write(frame, 0, type, id, channel);
        if (payloadLength > 0) System.arraycopy(payload, 0, frame, headerLength, payloadLength);
        sendFrame(frame, 0, headerLength + payloadLength);
    }

    /** Where {@link #send} composes a frame on each thread, lent to the transport. */
    private static final ThreadLocal<byte[]> SCRATCH = ThreadLocal.withInitial(() -> new byte[256]);

    /** A frame larger than this is composed in a buffer of its own, so no thread keeps one that size. */
    private static final int MAX_SCRATCH = 64 * 1024;

    @Override
    public void sendFrame(byte[] frame, int offset, int length) {
        bytesSent.add(length);
        if (heldForFirstClient(frame, offset, length, true)) return;
        broadcast(frame, offset, length);
    }

    /** Keeps a frame until the first client connects; a lent one is copied, since it outlives the call. */
    private boolean heldForFirstClient(byte[] frame, int offset, int length, boolean lent) {
        if (firstClientConnected) return false;
        synchronized (pendingFrames) {
            if (firstClientConnected) return false;
            pendingFrames.add(lent ? Arrays.copyOfRange(frame, offset, offset + length) : frame);
            return true;
        }
    }

    @Override
    public <T> void on(int type, long id, int channel, Class<T> cls, Consumer<T> callback) {
        set(type, id, channel, new TypedHandler<>(cls, callback));
    }

    @Override
    public void onBytes(int type, long id, int channel, FrameBytes handler) {
        set(type, id, channel, new TypedHandler<>(handler));
    }

    @Override
    public void remove(int type, long id, int channel) {
        set(type, id, channel, null);
    }

    /** Puts {@code handler} (null: none) on the channel these numbers name. */
    private void set(int type, long id, int channel, TypedHandler<?> handler) {
        if (ChannelHeader.name(channel) == null) throw new IllegalArgumentException("no channel numbered " + channel);
        if (type == 0) {
            setGlobal(channel, handler);
            return;
        }
        if (ChannelHeader.typeName(type) == null) throw new IllegalArgumentException("no type numbered " + type);
        numbered.compute(ownerKey(type, id), o -> {
            if (handler == null) return o == null ? null : o.without(channel);
            return (o == null ? Numbered.EMPTY : o).with(channel, handler);
        });
    }

    private synchronized void setGlobal(int channel, TypedHandler<?> handler) {
        TypedHandler<?>[] next = Arrays.copyOf(globals, Math.max(globals.length, channel + 1));
        next[channel] = handler;
        globals = next;
    }

    /** The key a widget's handlers are found by in {@link #numbered}: its type's number and its id. */
    private static long ownerKey(int type, long id) {
        return ((long) type << 32) | (id & 0xFFFFFFFFL);
    }

    @Override
    public void removeOwner(int type, long id) {
        long key = ownerKey(type, id);
        // Most widgets register nothing of their own: a lookup takes no lock, a removal does.
        if (numbered.get(key) != null) numbered.remove(key);
    }

    @Override
    public <T> void onOwner(int type, long id, int[] channels, Class<T> cls,
            java.util.function.BiConsumer<String, T> callback) {
        OwnerHandler<T> handler = new OwnerHandler<>(new TypedHandler<>(cls, null), channels, callback);
        numbered.compute(ownerKey(type, id), o -> (o == null ? Numbered.EMPTY : o).withOwner(handler));
    }

    @Override
    public <T> boolean onType(int type, Class<T> cls, TypeCallback<T> callback) {
        setType(type, new TypeHandler<>(ChannelHeader.typeName(type), new TypedHandler<>(cls, null), callback));
        return true;
    }

    /** Registering a type a second time keeps the first callback. */
    private synchronized void setType(int number, TypeHandler<?> handler) {
        if (number < typesByNumber.length && typesByNumber[number] != null) return;
        TypeHandler<?>[] next = Arrays.copyOf(typesByNumber, Math.max(typesByNumber.length, number + 1));
        next[number] = handler;
        typesByNumber = next;
    }

    /** State kept for as long as this connection lives, under [key]; [create] makes it on first use. */
    @SuppressWarnings("unchecked")
    public <T> T attachment(Object key, java.util.function.Supplier<T> create) {
        return (T) attachments.computeIfAbsent(key, k -> create.get());
    }

    /**
     * Subclasses call this once per established session. The first call drains any frames buffered
     * before a client was connected, delivering them to the just-connected session.
     */
    protected void onClientConnected(Consumer<byte[]> sendToSession) {
        dev.equo.swt.BootTrace.markOnce("comm.clientConnected");
        if (firstClientConnected) {
            // A different client from the one everything so far was written for. It holds none of
            // it, so it is given a new id and every widget is described to it in full again.
            connection.set(NEXT_CONNECTION.getAndIncrement());
            return;
        }
        synchronized (pendingFrames) {
            if (firstClientConnected || handingOver) return;
            handingOver = true;
        }
        // What is sent while the held frames go out is held behind them, and the comm counts as
        // connected only once none is left: a frame sent now can be relative to one still held.
        while (true) {
            List<byte[]> drain;
            synchronized (pendingFrames) {
                if (pendingFrames.isEmpty()) {
                    firstClientConnected = true;
                    handingOver = false;
                    return;
                }
                drain = new ArrayList<>(pendingFrames);
                pendingFrames.clear();
            }
            for (byte[] frame : drain) {
                sendToSession.accept(frame);
            }
        }
    }

    /**
     * Subclasses call this with each received binary frame. {@code data[0, offset + length)} is
     * handed over: decoding may overwrite it, so it must not be read again or shared.
     */
    protected void onBinaryMessage(byte[] data, int offset, int length) {
        bytesReceived.add(length);
        int end = offset + length;
        long first = varint(data, offset, end);
        if (first <= 0xFF) {
            System.err.println(logTag + " dropped a message with no channel number");
            return;
        }
        int at = offset + (int) (first & 0xFF);
        long head = first >>> 8;
        int channel = (int) (head >>> 1);
        if (ChannelHeader.name(channel) == null) throw unknown("channel", channel);
        if ((head & 1) == 0) {
            if (!dispatchGlobal(channel, data, at, end - at)) unhandled(0, 0, channel);
            return;
        }
        long type = varint(data, at, end);
        int idAt = at + (int) (type & 0xFF);
        long zigzag = type < 0 ? -1 : varint(data, idAt, end);
        if (zigzag < 0) {
            System.err.println(logTag + " dropped a message with a malformed header");
            return;
        }
        if (ChannelHeader.typeName((int) (type >>> 8)) == null) throw unknown("type", (int) (type >>> 8));
        int payload = idAt + (int) (zigzag & 0xFF);
        long z = zigzag >>> 8;
        long id = (z >>> 1) ^ -(z & 1);
        if (!dispatchNumbered((int) (type >>> 8), id, channel, data, payload, end - payload))
            unhandled((int) (type >>> 8), id, channel);
    }

    /**
     * A number the generated wire table does not have: the client was built from another table than
     * this Java, so nothing it sends can be trusted to mean what it says.
     */
    private IllegalStateException unknown(String what, int number) {
        return new IllegalStateException(logTag + " the client sent " + what + " " + number
                + ", which the wire table has no number for: the client and this Java were not generated together");
    }

    private void unhandled(int type, long id, int channel) {
        String name = ChannelHeader.name(channel);
        System.err.println(logTag + " No handler for: "
                + (type == 0 ? name : ChannelHeader.typeName(type) + "/" + id + (name == null || name.isEmpty() ? "" : "/" + name)));
    }

    /**
     * The varint at {@code data[at]} shifted left 8, its length in the low byte; -1 when it does not
     * end before {@code end} or takes more than 5 bytes, which no channel, type or widget id does.
     */
    private static long varint(byte[] data, int at, int end) {
        long v = 0;
        for (int i = 0; i < 5 && at + i < end; i++) {
            int b = data[at + i] & 0xFF;
            v |= (long) (b & 0x7F) << (7 * i);
            if ((b & 0x80) == 0) return (v << 8) | (i + 1);
        }
        return -1;
    }

    private boolean dispatchGlobal(int channel, byte[] data, int offset, int length) {
        TypedHandler<?>[] g = globals;
        TypedHandler<?> handler = channel >= 0 && channel < g.length ? g[channel] : null;
        if (handler == null) return false;
        handler.handle(data, offset, length, serializer, logTag);
        return true;
    }

    /**
     * Finds a widget channel's handler: the channel's own, under the widget's type and then its
     * alternate; then the owner's; then the type's.
     */
    private boolean dispatchNumbered(int type, long id, int channel, byte[] data, int offset, int length) {
        if (type <= 0 || channel < 0) return false;
        int[] alternates = alternateTypes;
        int alternate = type < alternates.length ? alternates[type] : 0;
        Numbered own = numbered.get(ownerKey(type, id));
        Numbered other = alternate == 0 ? null : numbered.get(ownerKey(alternate, id));
        TypedHandler<?> handler = own == null ? null : own.handler(channel);
        if (handler == null && other != null) handler = other.handler(channel);
        if (handler != null) {
            handler.handle(data, offset, length, serializer, logTag);
            return true;
        }
        String rest = ChannelHeader.name(channel);
        if (rest == null) return false;
        if (own != null && own.owner != null && own.owner.handles(channel)) {
            own.owner.handle(rest, data, offset, length, serializer, logTag);
            return true;
        }
        if (other != null && other.owner != null && other.owner.handles(channel)) {
            other.owner.handle(rest, data, offset, length, serializer, logTag);
            return true;
        }
        TypeHandler<?>[] types = typesByNumber;
        TypeHandler<?> byType = type < types.length ? types[type] : null;
        Object target = byType == null ? null : byType.callback.target(byType.type, id, channel, rest);
        if (target == null) {
            byType = alternate != 0 && alternate < types.length ? types[alternate] : null;
            target = byType == null ? null : byType.callback.target(byType.type, id, channel, rest);
        }
        if (target == null) return false;
        byType.handle(target, rest, data, offset, length, serializer, logTag);
        return true;
    }

    /**
     * Types {@code a} and {@code b} are one widget's two names: a message carrying either type's
     * number is also looked up under the other's.
     */
    public static synchronized void setAlternateTypes(String a, String b) {
        int na = ChannelHeader.requireType(a);
        int nb = ChannelHeader.requireType(b);
        int[] next = Arrays.copyOf(alternateTypes, Math.max(alternateTypes.length, Math.max(na, nb) + 1));
        next[na] = nb;
        next[nb] = na;
        alternateTypes = next;
    }

    /**
     * Sends {@code frame[offset, offset + length)} to every currently-open session. Lent, as in
     * {@link #sendFrame}: a transport that finishes the send after returning must copy it first.
     */
    protected abstract void broadcast(byte[] frame, int offset, int length);

    /** Prefix for diagnostic logging; defaults to the concrete class' simple name. */
    protected String logTag() {
        return logTag;
    }

    /** One owner's handlers by channel number, replaced whole by every change. */
    private static final class Numbered {
        static final Numbered EMPTY = new Numbered(new int[0], new TypedHandler<?>[0], null);

        final int[] channels;
        final TypedHandler<?>[] handlers;
        final OwnerHandler<?> owner;

        Numbered(int[] channels, TypedHandler<?>[] handlers, OwnerHandler<?> owner) {
            this.channels = channels;
            this.handlers = handlers;
            this.owner = owner;
        }

        TypedHandler<?> handler(int channel) {
            int[] c = channels;
            for (int i = 0; i < c.length; i++) if (c[i] == channel) return handlers[i];
            return null;
        }

        Numbered with(int channel, TypedHandler<?> handler) {
            for (int i = 0; i < channels.length; i++) {
                if (channels[i] != channel) continue;
                TypedHandler<?>[] h = handlers.clone();
                h[i] = handler;
                return new Numbered(channels, h, owner);
            }
            int n = channels.length;
            int[] c = Arrays.copyOf(channels, n + 1);
            TypedHandler<?>[] h = Arrays.copyOf(handlers, n + 1);
            c[n] = channel;
            h[n] = handler;
            return new Numbered(c, h, owner);
        }

        Numbered without(int channel) {
            for (int i = 0; i < channels.length; i++) {
                if (channels[i] != channel) continue;
                int n = channels.length - 1;
                if (n == 0 && owner == null) return null;
                int[] c = new int[n];
                TypedHandler<?>[] h = new TypedHandler<?>[n];
                System.arraycopy(channels, 0, c, 0, i);
                System.arraycopy(channels, i + 1, c, i, n - i);
                System.arraycopy(handlers, 0, h, 0, i);
                System.arraycopy(handlers, i + 1, h, i, n - i);
                return new Numbered(c, h, owner);
            }
            return this;
        }

        Numbered withOwner(OwnerHandler<?> handler) {
            return new Numbered(channels, handlers, handler);
        }
    }

    private static final class TypeHandler<T> {
        final String type;
        final TypedHandler<T> reader;
        final TypeCallback<T> callback;

        TypeHandler(String type, TypedHandler<T> reader, TypeCallback<T> callback) {
            this.type = type;
            this.reader = reader;
            this.callback = callback;
        }

        void handle(Object target, String rest, byte[] data, int offset, int length, Serializer serializer, String tag) {
            callback.accept(target, rest, reader.read(data, offset, length, serializer, tag));
        }
    }

    private static final class OwnerHandler<T> {
        final TypedHandler<T> reader;
        /** The channels' numbers. */
        final int[] channels;
        final java.util.function.BiConsumer<String, T> callback;

        OwnerHandler(TypedHandler<T> reader, int[] channels, java.util.function.BiConsumer<String, T> callback) {
            this.reader = reader;
            this.channels = channels;
            this.callback = callback;
        }

        boolean handles(int channel) {
            for (int n : channels) if (n == channel) return true;
            return false;
        }

        void handle(String rest, byte[] data, int offset, int length, Serializer serializer, String tag) {
            callback.accept(rest, reader.read(data, offset, length, serializer, tag));
        }
    }

    private static final class TypedHandler<T> {
        final Class<T> cls;
        final Consumer<T> callback;
        /** Makes an empty value to read into, for a class the client sends in the wire's encoding. */
        final java.lang.reflect.Constructor<T> wire;

        @SuppressWarnings("unchecked")
        TypedHandler(Class<T> cls, Consumer<T> callback) {
            this.cls = cls;
            this.callback = callback;
            this.wire = cls == null ? null : (java.lang.reflect.Constructor<T>) WIRE_CONSTRUCTORS.get(cls);
            this.bytes = null;
        }

        /** One per class, shared by every handler of it: there is one handler per widget and event. */
        private static final ClassValue<java.lang.reflect.Constructor<?>> WIRE_CONSTRUCTORS =
                new ClassValue<java.lang.reflect.Constructor<?>>() {
            @Override
            protected java.lang.reflect.Constructor<?> computeValue(Class<?> cls) {
                if (!dev.equo.swt.WireReadable.class.isAssignableFrom(cls)) return null;
                try {
                    return cls.getConstructor();
                } catch (NoSuchMethodException e) {
                    return null;
                }
            }
        };

        /** A handler of the payload where it lies: {@link #onBytes}. */
        TypedHandler(FrameBytes bytes) {
            this.cls = null;
            this.callback = null;
            this.wire = null;
            this.bytes = bytes;
        }

        private final FrameBytes bytes;

        void handle(byte[] data, int offset, int length, Serializer serializer, String tag) {
            if (bytes != null) {
                bytes.accept(data, offset, length);
                return;
            }
            callback.accept(read(data, offset, length, serializer, tag));
        }

        private static final Object[] NO_ARGUMENTS = new Object[0];

        /** The payload {@code data[offset, offset + length)} as the handler takes it. */
        @SuppressWarnings("unchecked")
        T read(byte[] data, int offset, int length, Serializer serializer, String tag) {
            // ByteBuffer.class is the passthrough that copies nothing: the frame is ours for the
            // length of the dispatch, so a handler that reads it here and keeps nothing takes a view.
            if (cls == java.nio.ByteBuffer.class) return (T) java.nio.ByteBuffer.wrap(data, offset, length);
            // byte[].class is a passthrough: deliver the raw frame bytes (a copy, since the handler
            // may outlive this dispatch) with no deserialization — used for already-binary payloads.
            if (cls == byte[].class) return (T) (length > 0 ? Arrays.copyOfRange(data, offset, offset + length) : null);
            if (length <= 0 || cls == null || cls == Void.class || cls == void.class) return null;
            // A string is sent as the widget wire writes one, so it is copied, not decoded.
            if (cls == String.class) return (T) dev.equo.swt.WireReader.over(data, offset, length).string();
            if (wire != null) {
                T value;
                try {
                    value = wire.newInstance(NO_ARGUMENTS);
                } catch (ReflectiveOperationException e) {
                    throw new IllegalStateException(e);
                }
                dev.equo.swt.WireReader.over(data, offset, length).fields((dev.equo.swt.WireReadable) value);
                return value;
            }
            // Tagged values: maps, lists and scalars, for a handler taking Object or one of those.
            if (!TaggedValues.isTagged(data, offset, length)) {
                System.err.println(tag + " no binary form of " + cls.getName() + " to read");
                return null;
            }
            return serializer.from(cls, data, offset, length);
        }
    }
}
