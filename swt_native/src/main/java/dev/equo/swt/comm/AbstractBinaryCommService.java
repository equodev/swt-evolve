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

    protected final Serializer serializer = new Serializer();
    /**
     * Handlers by owner, the first two segments of a name ({@code Shell/42}), then by the rest of the
     * name ({@code Mouse/MouseDown}); a name with fewer segments is an owner with rest "". Every widget
     * registers the same few dozen rests, so those strings are shared ({@link #REST_NAMES}).
     */
    private final Map<String, Map<String, TypedHandler<?>>> handlers = new ConcurrentHashMap<>();
    private static final Map<String, String> REST_NAMES = new ConcurrentHashMap<>();
    /** One handler for most of an owner's channels; see {@link #onOwner}. */
    private final Map<String, OwnerHandler<?>> ownerHandlers = new ConcurrentHashMap<>();
    /**
     * How many owners each type has in {@link #handlers} and {@link #ownerHandlers} together, so a
     * widget of a type with none, which is most of them, is released without a lookup by its name.
     */
    private final Map<String, java.util.concurrent.atomic.AtomicInteger> ownersByType = new ConcurrentHashMap<>();
    /** Handlers for every widget of a type, by type: {@link #onType}. */
    private final Map<String, TypeHandler<?>> typeHandlers = new ConcurrentHashMap<>();
    private final Map<Object, Object> attachments = new ConcurrentHashMap<>();
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
    public void send(String eventName) {
        send(eventName, (byte[]) null);
    }

    @Override
    public void send(String eventName, byte[] payload) {
        byte[] frame = encodeFrame(eventName, payload);
        bytesSent.add(frame.length);
        if (heldForFirstClient(frame, 0, frame.length, false)) return;
        broadcast(frame, 0, frame.length);
    }

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

    private static byte[] encodeFrame(String eventName, byte[] payload) {
        byte[] header = CommService.frameHeader(eventName);
        int payloadLength = payload != null ? payload.length : 0;
        byte[] frame = Arrays.copyOf(header, header.length + payloadLength);
        if (payloadLength > 0) System.arraycopy(payload, 0, frame, header.length, payloadLength);
        return frame;
    }

    @Override
    public <T> void on(String eventName, Class<T> cls, Consumer<T> callback) {
        int split = ownerEnd(eventName);
        String rest = REST_NAMES.computeIfAbsent(rest(eventName, split), r -> r);
        TypedHandler<T> handler = new TypedHandler<>(cls, callback);
        handlers.compute(eventName.substring(0, split), (owner, named) -> {
            if (named == null) {
                named = new ConcurrentHashMap<>(8);
                countOwner(owner, 1);
            }
            named.put(rest, handler);
            return named;
        });
    }

    private void countOwner(String owner, int delta) {
        int slash = owner.indexOf('/');
        ownersByType.computeIfAbsent(slash < 0 ? owner : owner.substring(0, slash),
                t -> new java.util.concurrent.atomic.AtomicInteger()).addAndGet(delta);
    }

    @Override
    public void remove(String eventName) {
        int split = ownerEnd(eventName);
        String rest = rest(eventName, split);
        handlers.computeIfPresent(eventName.substring(0, split), (owner, named) -> {
            named.remove(rest);
            if (!named.isEmpty()) return named;
            countOwner(owner, -1);
            return null;
        });
    }

    @Override
    public void removeOwner(String owner) {
        if (handlers.remove(owner) != null) countOwner(owner, -1);
        if (ownerHandlers.remove(owner) != null) countOwner(owner, -1);
    }

    @Override
    public void removeOwner(String type, long id) {
        java.util.concurrent.atomic.AtomicInteger owners = ownersByType.get(type);
        if (owners == null || owners.get() <= 0) return;
        removeOwner(type + "/" + id);
    }

    @Override
    public <T> void onOwner(String owner, String[] rests, Class<T> cls,
            java.util.function.BiConsumer<String, T> callback) {
        if (ownerHandlers.put(owner, new OwnerHandler<>(new TypedHandler<>(cls, null), rests, callback)) == null)
            countOwner(owner, 1);
    }

    @Override
    public <T> boolean onType(String type, Class<T> cls, TypeCallback<T> callback) {
        typeHandlers.computeIfAbsent(type, t -> new TypeHandler<>(new TypedHandler<>(cls, null), callback));
        return true;
    }

    private boolean dispatchToType(String eventName, byte[] data, int offset, int length) {
        int first = eventName.indexOf('/');
        if (first < 0) return false;
        String type = eventName.substring(0, first);
        TypeHandler<?> handler = typeHandlers.get(type);
        if (handler == null) return false;
        int split = ownerEnd(eventName);
        long id = ChannelHeader.parseId(eventName, first + 1, split);
        if (id == Long.MIN_VALUE) return false;
        String rest = rest(eventName, split);
        Object target = handler.callback.target(type, id, rest);
        if (target == null) return false;
        handler.handle(target, rest, data, offset, length, serializer, logTag());
        return true;
    }

    private boolean dispatchToOwner(String eventName, byte[] data, int offset, int length) {
        int split = ownerEnd(eventName);
        OwnerHandler<?> owner = ownerHandlers.get(eventName.substring(0, split));
        if (owner == null) return false;
        String rest = rest(eventName, split);
        if (!owner.handles(rest)) return false;
        owner.handle(rest, data, offset, length, serializer, logTag());
        return true;
    }

    private TypedHandler<?> handler(String eventName) {
        int split = ownerEnd(eventName);
        Map<String, TypedHandler<?>> named = handlers.get(eventName.substring(0, split));
        return named == null ? null : named.get(rest(eventName, split));
    }

    /** Where a name's owner ends: at its second '/', or the whole name when it has no second one. */
    private static int ownerEnd(String eventName) {
        int first = eventName.indexOf('/');
        int second = first < 0 ? -1 : eventName.indexOf('/', first + 1);
        return second < 0 ? eventName.length() : second;
    }

    private static String rest(String eventName, int ownerEnd) {
        return ownerEnd >= eventName.length() ? "" : eventName.substring(ownerEnd + 1);
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
        ChannelHeader.Decoded header = ChannelHeader.decode(data, offset, offset + length);
        if (header == null) return;
        dispatch(header.channel(), data, header.payloadStart(), offset + length - header.payloadStart());
    }

    /**
     * The other name a message may be registered under when none is registered under the one it
     * arrived with, or null. A widget's handlers are registered under the name it had when it was
     * built, while the client addresses it by the name its latest description carries.
     */
    private static volatile UnaryOperator<String> alternateName = name -> null;

    public static void setAlternateName(UnaryOperator<String> resolver) {
        alternateName = resolver;
    }

    private void dispatch(String eventName, byte[] data, int offset, int length) {
        TypedHandler<?> typed = handler(eventName);
        if (typed == null) {
            String alternate = alternateName.apply(eventName);
            if (alternate != null) typed = handler(alternate);
        }
        if (typed != null) {
            typed.handle(data, offset, length, serializer, logTag());
            return;
        }
        if (dispatchToOwner(eventName, data, offset, length)) return;
        String alternateOwner = alternateName.apply(eventName);
        if (alternateOwner != null && dispatchToOwner(alternateOwner, data, offset, length)) return;
        if (dispatchToType(eventName, data, offset, length)) return;
        if (alternateOwner != null && dispatchToType(alternateOwner, data, offset, length)) return;
        // The same-type count distinguishes "wrong widget id" (siblings exist) from "this widget
        // type never registered" (none). The list itself runs to hundreds of lines per miss, so it
        // is printed only in debug mode.
        String typePrefix = eventName.substring(0, eventName.indexOf('/') + 1);
        List<String> sameType = java.util.stream.Stream.concat(
                        handlers.entrySet().stream().filter(e -> e.getKey().startsWith(typePrefix))
                                .flatMap(e -> e.getValue().keySet().stream()
                                        .map(rest -> rest.isEmpty() ? e.getKey() : e.getKey() + "/" + rest)),
                        ownerHandlers.entrySet().stream().filter(e -> e.getKey().startsWith(typePrefix))
                                .flatMap(e -> Arrays.stream(e.getValue().rests).map(rest -> e.getKey() + "/" + rest)))
                .sorted().collect(Collectors.toList());
        System.err.println(logTag() + " No handler for: " + eventName + " (" + sameType.size() + " registered for "
                + typePrefix + "*)");
        if (Config.isDebug())
            sameType.forEach(k -> System.err.println(logTag() + "   registered: " + k));
    }

    /**
     * Sends {@code frame[offset, offset + length)} to every currently-open session. Lent, as in
     * {@link #sendFrame}: a transport that finishes the send after returning must copy it first.
     */
    protected abstract void broadcast(byte[] frame, int offset, int length);

    /** Prefix for diagnostic logging; defaults to the concrete class' simple name. */
    protected String logTag() {
        return "[" + getClass().getSimpleName() + "]";
    }

    private static final class TypeHandler<T> {
        final TypedHandler<T> reader;
        final TypeCallback<T> callback;

        TypeHandler(TypedHandler<T> reader, TypeCallback<T> callback) {
            this.reader = reader;
            this.callback = callback;
        }

        void handle(Object target, String rest, byte[] data, int offset, int length, Serializer serializer, String tag) {
            reader.handle(data, offset, length, serializer, tag, value -> callback.accept(target, rest, value));
        }
    }

    private static final class OwnerHandler<T> {
        final TypedHandler<T> reader;
        final String[] rests;
        final java.util.function.BiConsumer<String, T> callback;

        OwnerHandler(TypedHandler<T> reader, String[] rests, java.util.function.BiConsumer<String, T> callback) {
            this.reader = reader;
            this.rests = rests;
            this.callback = callback;
        }

        boolean handles(String rest) {
            for (String r : rests) if (r.equals(rest)) return true;
            return false;
        }

        void handle(String rest, byte[] data, int offset, int length, Serializer serializer, String tag) {
            reader.handle(data, offset, length, serializer, tag, value -> callback.accept(rest, value));
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

        @SuppressWarnings("unchecked")
        void handle(byte[] data, int offset, int length, Serializer serializer, String tag) {
            handle(data, offset, length, serializer, tag, callback);
        }

        @SuppressWarnings("unchecked")
        void handle(byte[] data, int offset, int length, Serializer serializer, String tag, Consumer<T> callback) {
            // ByteBuffer.class is the passthrough that copies nothing: the frame is ours for the
            // length of the dispatch, so a handler that reads it here and keeps nothing takes a view.
            if (cls == java.nio.ByteBuffer.class) {
                callback.accept((T) java.nio.ByteBuffer.wrap(data, offset, length));
                return;
            }
            // byte[].class is a passthrough: deliver the raw frame bytes (a copy, since the handler
            // may outlive this dispatch) with no deserialization — used for already-binary payloads.
            if (cls == byte[].class) {
                callback.accept((T) (length > 0 ? Arrays.copyOfRange(data, offset, offset + length) : null));
                return;
            }
            if (length <= 0 || cls == null || cls == Void.class || cls == void.class) {
                callback.accept(null);
                return;
            }
            // A string is sent as its UTF-8 bytes.
            if (cls == String.class) {
                callback.accept((T) new String(data, offset, length, StandardCharsets.UTF_8));
                return;
            }
            if (wire != null) {
                T value;
                try {
                    value = wire.newInstance();
                } catch (ReflectiveOperationException e) {
                    throw new IllegalStateException(e);
                }
                new dev.equo.swt.WireReader(data, offset, length).fields((dev.equo.swt.WireReadable) value);
                callback.accept(value);
                return;
            }
            // Tagged values: maps, lists and scalars, for a handler taking Object or one of those.
            if (!TaggedValues.isTagged(data, offset, length)) {
                System.err.println(tag + " no binary form of " + cls.getName() + " to read");
                callback.accept(null);
                return;
            }
            callback.accept(serializer.from(cls, data, offset, length));
        }
    }
}
