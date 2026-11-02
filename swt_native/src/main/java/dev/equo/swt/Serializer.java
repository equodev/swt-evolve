package dev.equo.swt;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.custom.*;

/**
 * What the client has been given and what it is owed: the write stamps, deliveries and per-property
 * hashes behind the binary wire ({@link WidgetWire}, {@link GcWire}), and the {@link Tape} that writes
 * a flush's widget records.
 */
public class Serializer {

    /**
     * What a thread's serialization keeps, in one object reached by one lookup: a walk reads it for
     * every record it writes, and what a ThreadLocal lookup costs depends on where its key lands in
     * the thread's table, which shifts whenever a ThreadLocal is created anywhere.
     */
    static final class Local {
        final Thread owner = Thread.currentThread();
        /** How many widget payloads this thread is writing; above zero, getters are being read for the client. */
        int writing;
        /** The value whose partial update is being written, or null while writing whole widgets. */
        Object updating;
        /**
         * The client the frames written on this thread are for. Zero when nothing said, which credits
         * nothing: a walk done to look at a state rather than to send it must not tell the next update
         * it can be relative to something nobody received.
         *
         * <p>Per thread because a serialize can be re-entered on one, and the inner walk may be for a
         * different client than the outer.
         */
        int target;
        /** What the walk in progress asked to be told once its frame has reached its client. */
        final java.util.ArrayList<java.util.function.IntConsumer> onDelivery = new java.util.ArrayList<>();
        /** Resources this walk described, credited once the bytes are on their way. */
        final java.util.ArrayList<Object> writtenResources = new java.util.ArrayList<>();
        /** The {@link #oncePerPayload} cache of the record being written; null outside one. */
        java.util.Map<Object, Object> payloadScope;
    }

    private static final ThreadLocal<Local> locals = ThreadLocal.withInitial(Local::new);

    /** The last thread's state, so the thread that writes nearly every record skips the lookup. */
    private static Local lastLocal;

    static Local local() {
        Local last = lastLocal;
        if (last != null && last.owner == Thread.currentThread()) return last;
        return lastLocal = locals.get();
    }

    /**
     * Whether a widget is being described for the client right now. A getter that has side effects
     * on the application (a virtual item's SetData) checks this to tell a read for the wire apart
     * from one the application made.
     */
    public static boolean isWriting() {
        return local().writing > 0;
    }

    // A widget is serialized both on its own channel and nested inside an ancestor's tree, and
    // the two snapshots can arrive in either order. Bumped at write time, so a lower seq is
    // strictly the older snapshot.
    private static final java.util.concurrent.atomic.AtomicLong writeSeq = new java.util.concurrent.atomic.AtomicLong();

    /**
     * True while {@code value}'s own properties are written as a partial update. False for a widget
     * described whole, even inside another's update: the far side takes that as a replacement.
     */
    public static boolean writingUpdateOf(Object value) {
        return value != null && local().updating == value;
    }

    /** Runs {@code action} with the client's connection once the frame being written is delivered, never if it is dropped. */
    public static void whenDelivered(java.util.function.IntConsumer action) {
        local().onDelivery.add(action);
    }

    /** The client currently being written for. */
    public static int targetConnection() {
        return local().target;
    }

    /**
     * Writes and sends {@code body} as addressed to {@code connection}, so that what it writes is
     * credited to that client and named rather than described only where that client has it.
     */
    public static void targeting(int connection, Runnable body) {
        Local l = local();
        int outer = l.target;
        l.target = connection;
        try {
            body.run();
        } finally {
            l.target = outer;
        }
    }

    /**
     * {@link #targeting} for a caller that cannot hand over a block without allocating one: addresses
     * what is written to {@code connection} and returns who was addressed before, for {@link
     * #leaveTarget}.
     */
    public static int enterTarget(int connection) {
        Local l = local();
        int outer = l.target;
        l.target = connection;
        return outer;
    }

    /** Ends {@link #enterTarget}: addresses {@code outer} again. */
    public static void leaveTarget(int outer) {
        local().target = outer;
    }

    /**
     * Addresses everything written from here on to {@code connection}, until {@link
     * #exitConnection()}. For a caller whose scope is not a block - a test's setup and teardown.
     */
    public static void enterConnection(int connection) {
        local().target = connection;
    }

    /** Stops addressing frames, so a walk that names nobody credits nobody again. */
    public static void exitConnection() {
        local().target = 0;
    }

    /**
     * Marks everything the last walk wrote as delivered: nothing outstanding, and the stamp the next
     * update is measured from. Called by the send path once the bytes have been handed over.
     */
    public static void markDelivered() {
        markDelivered(local());
    }

    private static void markDelivered(Local l) {
        int connection = l.target;
        // Nobody named, so nobody was told: a walk made to read a state rather than to send it.
        if (connection == 0) {
            discardWritten(l);
            return;
        }
        java.util.ArrayList<Object> resources = l.writtenResources;
        if (!resources.isEmpty()) {
            for (int i = 0; i < resources.size(); i++) resourceDelivery.put(resources.get(i), connection);
            resources.clear();
        }
        java.util.ArrayList<java.util.function.IntConsumer> actions = l.onDelivery;
        if (!actions.isEmpty()) {
            java.util.List<java.util.function.IntConsumer> run = new java.util.ArrayList<>(actions);
            actions.clear();
            for (java.util.function.IntConsumer action : run) action.accept(connection);
        }
    }

    /**
     * {@code value}'s record, stamped {@code seq}, has been written for the walk's client: nothing of
     * it is outstanding. Credited as soon as it is written, since nothing can change it in between.
     */
    /** Whether {@code w[from, to)} is where the client starts the property, so a description leaves it out. */
    private static boolean isDefault(VWidget value, int key, WidgetWire w, int from, int to) {
        if (value.wireAlways(key)) return false;
        return w.isZero(from, to) || (w.isEmpty(from, to) && !value.wireKeepsEmpty(key));
    }

    private static boolean isDefault(org.eclipse.swt.graphics.VResource value, int key, WidgetWire w, int from, int to) {
        if (value.wireAlways(key)) return false;
        return w.isZero(from, to) || (w.isEmpty(from, to) && !value.wireKeepsEmpty(key));
    }

    private static void delivered(Local l, Object impl, VWidget value, long seq) {
        int connection = l.target;
        if (connection != 0) {
            value.sent(connection, seq);
            // A delivered widget can be named instead of described from here on, and a name is only
            // any use if the far side can ask for the thing behind it. Being asked for is answered
            // by a lookup the widget only ever entered by being scheduled - which a widget written
            // inside an ancestor need never have been. So delivery is what puts it there.
            FlutterBridge.registerDelivered(impl);
        }
        markDelivered(l);
    }

    /** Takes the resources the last walk wrote without crediting them, for a send buffered before it travels. */
    public static java.util.List<Object> takeWrittenResources() {
        java.util.ArrayList<Object> pending = local().writtenResources;
        if (pending.isEmpty()) return java.util.Collections.emptyList();
        java.util.List<Object> taken = new java.util.ArrayList<>(pending);
        pending.clear();
        return taken;
    }

    /** {@link #takeWrittenResources()} into {@code into}, for a caller that keeps a list of its own. */
    public static void takeWrittenResources(java.util.List<Object> into) {
        java.util.ArrayList<Object> pending = local().writtenResources;
        if (pending.isEmpty()) return;
        into.addAll(pending);
        pending.clear();
    }

    /** Credits resources taken by {@link #takeWrittenResources()} once their buffer has been sent. */
    public static void creditResources(java.util.List<Object> values, int connection) {
        if (connection == 0) return;
        for (int i = 0; i < values.size(); i++) resourceDelivery.put(values.get(i), connection);
    }

    /** Discards what the last walk recorded — for a serialize done only to look at the state. */
    public static void discardWritten() {
        discardWritten(local());
    }

    private static void discardWritten(Local l) {
        l.writtenResources.clear();
        l.onDelivery.clear();
    }

    /** Receives a serialized buffer that is only lent: it goes back to the pool once this returns. */
    @FunctionalInterface
    public interface Lent {
        void accept(byte[] buffer, int length);
    }

    // Null: no payload in flight. EMPTY_SCOPE: one is, and nothing has asked to be cached yet — so a
    // payload with no expensive getter never allocates the map.
    private static final java.util.Map<Object, Object> EMPTY_SCOPE = java.util.Collections.emptyMap();

    /**
     * Derives {@code key}'s value once for the payload being written, or on every call when none is.
     * A generated converter reads each field twice (null check, then write), and one derived value
     * can back several fields, so an uncached getter runs many times per payload.
     *
     * <p>The scope belongs to a single {@link #to} invocation rather than being reference-counted: a
     * getter can pump the event loop and re-enter {@code to}, and that inner payload is a later
     * snapshot which must not reuse this one's values.
     */
    public static <T> T oncePerPayload(Object key, java.util.function.Supplier<T> derive) {
        Local l = local();
        java.util.Map<Object, Object> scope = l.payloadScope;
        if (scope == null) {
            return derive.get();
        }
        Object cached = scope.get(key);
        if (cached == null) {
            T derived = derive.get();
            if (derived == null) {
                return null;
            }
            // Re-read: derive() can pump the event loop and re-enter for this same key.
            scope = l.payloadScope;
            if (scope == EMPTY_SCOPE) {
                scope = new java.util.IdentityHashMap<>();
                l.payloadScope = scope;
            }
            cached = scope.putIfAbsent(key, derived);
            if (cached == null) {
                cached = derived;
            }
        }
        @SuppressWarnings("unchecked")
        T value = (T) cached;
        return value;
    }

    /**
     * {@code bytes[offset, offset + length)} as {@code type}: a payload of tagged values
     * ({@link dev.equo.swt.comm.TaggedValues}) as the maps, lists and scalars it holds, read as
     * {@code Object} or as the one of those it is; null for anything else.
     */
    public <T> T from(Class<T> type, byte[] bytes, int offset, int length) {
        if (length <= 0 || !dev.equo.swt.comm.TaggedValues.isTagged(bytes, offset, length)) return null;
        Object tree = dev.equo.swt.comm.TaggedValues.decode(bytes, offset, length);
        return type == Object.class || type.isInstance(tree) ? type.cast(tree) : null;
    }

    /**
     * On by default. {@code -Dequo.swt.diff=false} goes back to sending every widget whole, which is
     * both the way out if partial updates ever have to be taken out of the picture and the reference
     * the two modes are compared against each other with.
     */
    public static final boolean diffEnabled = !"false".equalsIgnoreCase(System.getProperty("equo.swt.diff"));

    /**
     * How many clients each comm is currently serving.
     *
     * <p>Delivery is recorded against a connection id, and every client on one comm shares that id:
     * a widget described to one is then recorded as delivered, and the next client is handed a name
     * for something it was never given. It recovers by asking for the widget, but only after a round
     * trip, and it does so for every widget it was named rather than described — which, with a window
     * per shell, is most of them. So while any comm serves more than one client, nothing is named.
     *
     * <p>Kept per comm rather than as one flag because a second comm serving one client says nothing
     * about the first, and the direction that is safe to be wrong in is describing too much.
     */
    private static final java.util.concurrent.ConcurrentHashMap<Object, Integer> clientsPerComm =
            new java.util.concurrent.ConcurrentHashMap<>();

    /** Records how many clients {@code comm} is serving; see {@link #clientsPerComm}. */
    public static void clientsConnected(Object comm, int count) {
        if (comm == null) return;
        if (count <= 1) clientsPerComm.remove(comm);
        else clientsPerComm.put(comm, count);
    }

    /** Whether a widget may travel as a name rather than as a description. */
    private static boolean mayReference() {
        return diffEnabled && clientsPerComm.isEmpty();
    }

    /** Whether {@code impl} can be described by what changed rather than in full. */
    public static boolean canDiff(DartWidget impl) {
        return canDiff(impl, local().target);
    }

    /**
     * Forgets that {@code value} was ever delivered, to anyone.
     *
     * <p>For a client that says it does not hold a widget. Until this, asking for one re-sent it as
     * what had changed since a state the asker did not have, with everything under it named rather
     * than described - so the answer to "I do not have this" was a frame that named more things the
     * asker did not have, and it asked again for each.
     */
    public static void forgetDelivery(VWidget value) {
        if (value != null) value.sent(0, 0L);
    }

    /** Whether {@code impl} can be described to {@code connection} by what changed rather than in full. */
    public static boolean canDiff(DartWidget impl, int connection) {
        // Same reason a widget may not be named while several clients share a connection id: a change
        // is relative to a state this client may never have been sent. See mayReference().
        if (!mayReference() || impl == null) return false;
        VWidget value = impl.getValue();
        // Never sent whole, so there is no state on the far side for a change to be relative to.
        return value != null && value.sentSeq(connection) != 0 && value.anyDirty();
    }

    /**
     * The id a client holds {@code api} under: a widget's own, an editor's impl's (it is named by
     * its impl), an image's own.
     */
    static long wireId(Object api) {
        if (api instanceof org.eclipse.swt.custom.ControlEditor) {
            return ((org.eclipse.swt.custom.ControlEditor) api).getImpl().hashCode();
        }
        return api.hashCode();
    }

    /**
     * One flush's binary frame for one client ({@link WidgetWire}), and what that frame still owes
     * the client.
     *
     * <p>Every dirty widget gets a record of its own: the properties changed since the state the
     * client holds, or, when the client holds nothing for it, all of them. A value names the widgets
     * and images it holds by id; any of those the client has not been given is owed, and is described
     * by a record of its own further down the same frame. So a new subtree goes out as one record per
     * widget, parents first, and nothing is ever written inside anything else.
     */
    public static final class Tape implements WidgetWire.Names {

        private final WidgetWire frame = new WidgetWire();
        private int connection;
        /** False when the comm has several clients: then nothing is known delivered, and everything goes whole. */
        private boolean named;
        private final java.util.ArrayDeque<Object> owed = new java.util.ArrayDeque<>();
        /**
         * Widgets owed because they sit under one the client does not hold. The client lets go of a
         * widget together with everything under it, so a name inside one it lacks is a name it cannot
         * resolve, and each costs it a request: these are described, and so is what they name.
         */
        private final java.util.Set<Object> underUnheld =
                java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
        /** Above zero while the record of a widget the client does not hold is being written. */
        private int insideUnheld;
        /** The images and editors this tape has described; a widget is marked on its value instead. */
        private final java.util.Set<Object> described =
                java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
        /**
         * The first image id to carry each distinct pixel content in this frame. A later image with
         * the same content is written as a name of that id rather than a second copy of the pixels,
         * so one icon on a thousand nodes travels once. Reset per frame: the ids it points at
         * are all live and described in the same frame, so a name always resolves.
         */
        private final java.util.Map<Long, Long> canonicalImageId = new java.util.HashMap<>();
        /** This tape, as the widgets it describes are marked with: {@link SentHashes#describedOn}. */
        private long serial;

        public Tape(int connection) {
            start(connection);
        }

        private void start(int connection) {
            this.connection = connection;
            this.named = mayReference();
            serial = tapeSerial.incrementAndGet();
            canonicalImageId.clear();
            frame.reset();
            frame.begin();
            frame.names(this);
        }

        /** Tapes released on this thread, for the next flush: a tape keeps its frame's buffer. */
        private static final ThreadLocal<java.util.ArrayDeque<Tape>> SPARE =
                ThreadLocal.withInitial(java.util.ArrayDeque::new);

        /** A tape for {@code connection}, reused when one was released on this thread. */
        public static Tape borrow(int connection) {
            Tape tape = SPARE.get().pollFirst();
            if (tape == null) return new Tape(connection);
            tape.start(connection);
            return tape;
        }

        /** Hands the tape back for the next {@link #borrow}; it is not used after this. */
        public void release() {
            owed.clear();
            described.clear();
            canonicalImageId.clear();
            underUnheld.clear();
            insideUnheld = 0;
            frame.names(null);
            java.util.ArrayDeque<Tape> spare = SPARE.get();
            // A flush that described a whole window grew its buffer large; that one is not kept.
            if (frame.keepable() && spare.size() < 4) spare.addFirst(this);
        }

        public WidgetWire frame() {
            return frame;
        }

        public boolean isEmpty() {
            return frame.isEmpty();
        }

        public void sendTo(dev.equo.swt.comm.CommService comm) {
            frame.sendTo(comm);
        }

        @Override
        public void widget(Object api) {
            if (api instanceof org.eclipse.swt.widgets.Widget) {
                org.eclipse.swt.widgets.Widget widget = (org.eclipse.swt.widgets.Widget) api;
                if (!(widget.getImpl() instanceof DartWidget) || widget.isDisposed()) return;
                VWidget value = ((DartWidget) widget.getImpl()).getValue();
                if (value == null || (named && insideUnheld == 0 && value.sentSeq(connection) != 0)) return;
                SentHashes seen = sentHashes(value);
                if (seen.describedOn == serial) return;
                seen.describedOn = serial;
                if (insideUnheld > 0) underUnheld.add(api);
            } else if (api instanceof org.eclipse.swt.custom.ControlEditor) {
                if (!described.add(api)) return;
            } else {
                return;
            }
            owed.add(api);
        }

        @Override
        public void image(org.eclipse.swt.graphics.Image image) {
            if (described.contains(image)) return;
            if (!(image.getImpl() instanceof DartImage)) return;
            if (named && canReferenceResource(((DartImage) image.getImpl()).getValue())) return;
            described.add(image);
            owed.add(image);
        }

        @Override
        public long imageRef(org.eclipse.swt.graphics.Image image) {
            // The key is a CRC of the pixels, so two genuinely different icons could in principle
            // collide and the second would render as the first. The window is one frame and the
            // consequence a wrong icon rather than corruption or a crash; the same key already
            // decides the PNG-encode cache, so the risk is one the wire has long accepted.
            long content = wireContentKey(image);
            if (content != 0) {
                Long canonical = canonicalImageId.get(content);
                // A twin's pixels are already in this frame under that id: name it, send nothing.
                if (canonical != null) return canonical;
                canonicalImageId.put(content, (long) image.hashCode());
            }
            image(image);
            return image.hashCode();
        }

        /** A dirty widget's record, and whatever it names that the client lacks. */
        public void describe(DartWidget impl) {
            Local l = local();
            int outerTarget = l.target;
            l.target = connection;
            l.writing++;
            try {
                VWidget value = impl.getValue();
                SentHashes seen = sentHashes(value);
                if (seen.describedOn != serial) {
                    seen.describedOn = serial;
                    java.util.Map<Object, Object> outerScope = l.payloadScope;
                    l.payloadScope = EMPTY_SCOPE;
                    try {
                        // A widget scheduled without naming what changed is described whole.
                        if (named && value.sentSeq(connection) != 0 && value.anyDirty()) writeChange(l, impl, value);
                        else writeWhole(l, impl, impl.getApi(), value);
                    } finally {
                        l.payloadScope = outerScope;
                    }
                }
                settle(l);
            } finally {
                l.writing--;
                l.target = outerTarget;
            }
        }

        /**
         * The record of something that holds widgets without being one, such as the display:
         * {@code properties} writes each named property's value and returns its type, as a widget's
         * keyed writer does.
         */
        public void describeHolder(String type, long id, int[] keys, KeyedWriter properties) {
            targeting(connection, () -> {
                int start = frame.beginRecord(WidgetWire.HOLDER, type, id);
                // Stamped like a widget's record: a description that reaches the client after a
                // newer one must not replace it.
                frame.stamps(writeSeq.incrementAndGet(), 0, false);
                // The record replaces what the client holds, whose defaults are zero, false and null.
                for (int key : keys) {
                    int at = frame.size();
                    int previous = frame.lastKey();
                    frame.key(key);
                    int from = frame.size();
                    if (!properties.write(frame, key) || frame.isZero(from, frame.size())) frame.rewind(at, previous);
                }
                frame.endRecord(start);
                Local l = local();
                l.writing++;
                try {
                    settle(l);
                } finally {
                    l.writing--;
                }
            });
        }

        /** {@code editor}'s record, when the client lacks it. */
        public void describeEditor(org.eclipse.swt.custom.ControlEditor editor) {
            targeting(connection, () -> {
                widget(editor);
                settle(local());
            });
        }

        /** {@code image}'s record, when the client lacks it. */
        public void describeImage(org.eclipse.swt.graphics.Image image) {
            if (image == null || image.isDisposed()) return;
            targeting(connection, () -> {
                image(image);
                settle(local());
            });
        }

        /** Describes everything owed, which can owe more. */
        private void settle(Local l) {
            while (!owed.isEmpty()) {
                Object next = owed.poll();
                java.util.Map<Object, Object> outerScope = l.payloadScope;
                l.payloadScope = EMPTY_SCOPE;
                try {
                    settleOne(l, next);
                } finally {
                    l.payloadScope = outerScope;
                }
            }
        }

        private void settleOne(Local l, Object next) {
            if (next instanceof org.eclipse.swt.graphics.Image) {
                writeImage((org.eclipse.swt.graphics.Image) next);
            } else if (next instanceof org.eclipse.swt.custom.ControlEditor) {
                writeEditor((org.eclipse.swt.custom.ControlEditor) next);
            } else {
                org.eclipse.swt.widgets.Widget widget = (org.eclipse.swt.widgets.Widget) next;
                DartWidget impl = (DartWidget) widget.getImpl();
                writeWhole(l, impl, widget, impl.getValue());
            }
        }

        private void writeChange(Local l, DartWidget impl, VWidget value) {
            l.onDelivery.clear();
            Object outer = l.updating;
            l.updating = value;
            try {
                SentHashes seen = sentHashes(value);
                long seq = writeSeq.incrementAndGet();
                // Written in place, and taken back if every property turns out to be what was sent.
                frame.mark();
                int start = beginRecord(WidgetWire.CHANGE, impl, impl.getApi(), seen);
                frame.stamps(seq, value.sentSeq(connection), true);
                ChangedKeys changed = value.changedKeys();
                boolean any = false;
                for (int i = 0; i < changed.size(); i++) {
                    int key = changed.get(i);
                    int at = frame.size();
                    int previous = frame.lastKey();
                    frame.key(key);
                    int from = frame.size();
                    if (!value.writeWire(frame, key)) {
                        frame.rewind(at, previous);
                        continue;
                    }
                    long hash = frame.hash(from, frame.size());
                    if (seen.same(key, connection, hash)) {
                        frame.rewind(at, previous);
                        continue;
                    }
                    seen.put(key, connection, hash);
                    any = true;
                }
                if (!any) {
                    frame.rollback();
                    value.clearDirty();
                    l.onDelivery.clear();
                    return;
                }
                frame.endRecord(start);
                delivered(l, impl, value, seq);
            } finally {
                l.updating = outer;
            }
        }

        private void writeWhole(Local l, DartWidget impl, Object api, VWidget value) {
            if (value == null) return;
            l.onDelivery.clear();
            SentHashes seen = sentHashes(value);
            long seq = writeSeq.incrementAndGet();
            int start = beginRecord(WidgetWire.WHOLE, impl, api, seen);
            frame.stamps(seq, 0, false);
            int[] keys = value.wireKeys();
            boolean unheld = value.sentSeq(connection) == 0 || underUnheld.contains(api);
            if (unheld) insideUnheld++;
            try {
                writeWholeKeys(value, keys, seen);
            } finally {
                if (unheld) insideUnheld--;
            }
            frame.endRecord(start);
            delivered(l, impl, value, seq);
        }

        private void writeWholeKeys(VWidget value, int[] keys, SentHashes seen) {
            for (int i = 0; i < keys.length; i++) {
                int key = keys[i];
                int at = frame.size();
                int previous = frame.lastKey();
                // What the client starts from is the value class's defaults: zero, false and null,
                // which an empty string or array also stands for. One the value is recognised at is
                // not written, and is recorded as sent with the hash its bytes would have had.
                int written = value.writeWireWhole(frame, key);
                if (written == WidgetWire.UNHANDLED) {
                    frame.key(key);
                    if (!value.writeWire(frame, key)) {
                        frame.rewind(at, previous);
                        continue;
                    }
                    written = WidgetWire.WRITTEN;
                }
                if (written == WidgetWire.AT_ZERO) {
                    seen.put(i, key, connection, WidgetWire.ZERO_HASH);
                    continue;
                }
                if (written == WidgetWire.AT_EMPTY) {
                    seen.put(i, key, connection, WidgetWire.EMPTY_HASH);
                    continue;
                }
                int from = frame.valueStart();
                int to = frame.size();
                seen.put(i, key, connection, frame.hash(from, to));
                if (isDefault(value, key, frame, from, to))
                    frame.rewind(at, previous);
            }
        }

        /**
         * Opens a widget's record under the type its value's memo remembers: worked out once, except
         * for a composite, which can come to be presented under another name as its children arrive.
         */
        private int beginRecord(int kind, DartWidget impl, Object api, SentHashes seen) {
            if (impl instanceof DartComposite) {
                seen.type = swtWidgetName(impl, (org.eclipse.swt.widgets.Widget) api);
                seen.typeNumber = WidgetWire.typeNumber(seen.type);
            } else if (seen.type == null) {
                RecordType type = recordType(impl, api);
                seen.type = type.name;
                seen.typeNumber = type.number;
            }
            return frame.beginRecord(kind, seen.type, seen.typeNumber, FlutterBridge.id(api));
        }

        private void writeEditor(org.eclipse.swt.custom.ControlEditor api) {
            org.eclipse.swt.custom.DartControlEditor impl = (org.eclipse.swt.custom.DartControlEditor) api.getImpl();
            org.eclipse.swt.custom.VControlEditor value = impl.getValue();
            Class<?> apiClass = api.getClass();
            String type = apiClass.isAnonymousClass() ? Config.getSwtBaseClassName(apiClass) : apiClass.getSimpleName();
            long seq = writeSeq.incrementAndGet();
            int start = frame.beginRecord(WidgetWire.WHOLE, type, impl.hashCode());
            frame.stamps(seq, 0, false);
            writeAll(value.wireKeys(), value::writeWire);
            frame.endRecord(start);
        }

        private void writeImage(org.eclipse.swt.graphics.Image image) {
            DartImage impl = (DartImage) image.getImpl();
            VImage value = impl.getValue();
            int start = frame.beginRecord(WidgetWire.IMAGE, null, image.hashCode());
            writeAll(value.wireKeys(), value::writeWire);
            frame.endRecord(start);
            if (worthNaming(value)) {
                local().writtenResources.add(value);
                // The client holds it from here until told the image is gone.
                impl._describedToClient();
            }
            markDelivered();
        }

        /** Every property {@code properties} has a value for, into the open record. */
        private void writeAll(int[] keys, KeyedWriter properties) {
            for (int key : keys) {
                int at = frame.size();
                int previous = frame.lastKey();
                frame.key(key);
                if (!properties.write(frame, key)) frame.rewind(at, previous);
            }
        }
    }

    /** Every property of {@code value} as the widget wire writes an object's fields, closed by a 0. */
    public static byte[] wireFields(VResource value) {
        WidgetWire w = WidgetWire.borrow();
        try {
            w.reset();
            for (int key : value.wireKeys()) {
                int at = w.size();
                int previous = w.lastKey();
                w.key(key);
                if (!value.writeWire(w, key)) w.rewind(at, previous);
            }
            w.varint(0);
            return java.util.Arrays.copyOf(w.buffer(), w.size());
        } finally {
            WidgetWire.giveBack(w);
        }
    }

    /** Writes the property numbered {@code key} of something that is not a widget's value, such as the display. */
    public interface KeyedWriter {
        boolean write(WidgetWire w, int key);
    }

    private static final java.util.concurrent.atomic.AtomicLong tapeSerial = new java.util.concurrent.atomic.AtomicLong();

    /**
     * Has {@code key} written again on {@code value}'s next change even if it encodes as before: a
     * property naming objects that are described alongside it, such as a table's editors, encodes the
     * same when only those objects changed.
     */
    public static void resend(VWidget value, int key) {
        if (value != null) sentHashes(value).forget(key);
    }

    /**
     * The client changed {@code key} itself, so what it was last sent no longer says what it holds:
     * the next change of that property is sent even when it matches that last value.
     */
    public static void clientChanged(VWidget value, int key) {
        resend(value, key);
    }

    /** What the binary wire last sent of {@code value}'s properties, kept on the value itself. */
    static SentHashes sentHashes(VWidget value) {
        SentHashes seen = (SentHashes) value.wireMemo;
        if (seen == null) value.wireMemo = seen = new SentHashes(value.wireKeys().length);
        return seen;
    }

    /**
     * Per property number, the client and the hash of the value last sent: a property re-derived
     * identical is not sent again. A value has a few dozen properties at most, so parallel arrays
     * searched front to back; there is one per widget, so they start small.
     */
    static final class SentHashes {
        /**
         * Property number in the high half, client in the low half. Sized for every property of the
         * value's class, and filled in its {@code wireKeys()} order by a whole record, so that record
         * finds each slot by position instead of searching.
         */
        private long[] sent;
        private long[] hashes;
        private int size;

        SentHashes(int properties) {
            sent = new long[Math.max(1, properties)];
            hashes = new long[sent.length];
        }

        /** Forgets every property, keeping the arrays, for a value that starts over. */
        void clear() {
            size = 0;
            inOrder = true;
        }
        /** The {@link Tape} that last described the widget, so a tape writes it once. */
        long describedOn;
        /** Whether a refresh request can find the widget: {@link FlutterBridge#registerDelivered}. */
        boolean registered;
        /** Whether the widget waits in FlutterBridge's dirty queue; read and written under that queue's lock. */
        boolean queued;
        /** Whether a flush has already considered the widget, so it is no longer new to the client. */
        boolean notNew;
        /** The type the widget's records name, and the client's number for it: {@link Tape#beginRecord}. */
        String type;
        Integer typeNumber;

        boolean same(int key, int connection, long hash) {
            for (int i = 0; i < size; i++) {
                if ((int) (sent[i] >>> 32) == key) return (int) sent[i] == connection && hashes[i] == hash;
            }
            return false;
        }

        /**
         * Whether every slot so far was filled at its property's position, so the next position is
         * the next slot and holds no key yet. A key put any other way can sit anywhere, and a second
         * slot for it would be read in place of the one it updates.
         */
        private boolean inOrder = true;

        /** {@link #put(int, int, long)} for the property at {@code at} in its class's {@code wireKeys()}. */
        void put(int at, int key, int connection, long hash) {
            if (at < size && (int) (sent[at] >>> 32) == key) {
                sent[at] = ((long) key << 32) | (connection & 0xFFFFFFFFL);
                hashes[at] = hash;
                return;
            }
            // A widget's first whole record: each property lands in the next slot, with nothing to search.
            if (at == size && inOrder && size < sent.length) {
                sent[size] = ((long) key << 32) | (connection & 0xFFFFFFFFL);
                hashes[size] = hash;
                size++;
                return;
            }
            put(key, connection, hash);
        }

        /** Drops what was last sent for {@code key}, so its next write goes out whatever it hashes to. */
        void forget(int key) {
            for (int i = 0; i < size; i++) {
                if ((int) (sent[i] >>> 32) != key) continue;
                System.arraycopy(sent, i + 1, sent, i, size - i - 1);
                System.arraycopy(hashes, i + 1, hashes, i, size - i - 1);
                size--;
                inOrder = false;
                return;
            }
        }

        void put(int key, int connection, long hash) {
            long entry = ((long) key << 32) | (connection & 0xFFFFFFFFL);
            for (int i = 0; i < size; i++) {
                if ((int) (sent[i] >>> 32) == key) {
                    sent[i] = entry;
                    hashes[i] = hash;
                    return;
                }
            }
            inOrder = false;
            if (size == sent.length) {
                sent = java.util.Arrays.copyOf(sent, size * 2);
                hashes = java.util.Arrays.copyOf(hashes, size * 2);
            }
            sent[size] = entry;
            hashes[size] = hash;
            size++;
        }
    }

    /** A fresh write stamp, for a state that reached the client without a stamped write. */
    public static long nextWriteStamp() {
        return writeSeq.incrementAndGet();
    }

    /** Whether a resource may be described by what changed, under the same rule as a widget. */
    public static boolean mayDiffResources() {
        return mayReference();
    }

    /**
     * {@code impl}'s whole state written into {@code w}'s scratch buffer, for a {@link GcWire#WHOLE}
     * record. The client starts from the value class's defaults, so a property at its default is
     * left out.
     */
    public WidgetWire wholeState(DartResource impl, GcWire w, SentHashes seen) {
        VResource value = impl.getValue();
        WidgetWire props = w.scratch();
        int connection = local().target;
        seen.clear();
        int[] keys = value.wireKeys();
        for (int i = 0; i < keys.length; i++) {
            int key = keys[i];
            int at = props.size();
            int previous = props.lastKey();
            props.key(key);
            int from = props.size();
            if (!value.writeWire(props, key)) {
                props.rewind(at, previous);
                continue;
            }
            int to = props.size();
            seen.put(i, key, connection, props.hash(from, to));
            if (isDefault(value, key, props, from, to)) props.rewind(at, previous);
        }
        return props;
    }

    /**
     * The properties of {@code impl} that changed since {@code base} as a {@link GcWire#STATE} record
     * of {@code w}, and the new stamp, or {@code base} when none differs from what the client holds.
     */
    public long toDiffBinary(DartResource impl, long base, GcWire w, SentHashes seen) {
        VResource value = impl.getValue();
        WidgetWire props = w.scratch();
        int connection = local().target;
        ChangedKeys changed = value.changedKeys();
        boolean any = false;
        for (int i = 0; i < changed.size(); i++) {
            int key = changed.get(i);
            int at = props.size();
            int previous = props.lastKey();
            props.key(key);
            int from = props.size();
            if (!value.writeWire(props, key)) {
                props.rewind(at, previous);
                continue;
            }
            long hash = props.hash(from, props.size());
            if (seen.same(key, connection, hash)) {
                props.rewind(at, previous);
                continue;
            }
            seen.put(key, connection, hash);
            any = true;
        }
        if (!any) return base;
        w.state(GcWire.STATE, props);
        return writeSeq.incrementAndGet();
    }

    /** The type a record names a widget by, and the client's number for it. */
    private static final class RecordType {
        final Class<?> api;
        final String name;
        final Integer number;

        RecordType(Class<?> api, String name) {
            this.api = api;
            this.name = name;
            this.number = WidgetWire.typeNumber(name);
        }
    }

    /**
     * The record type of each implementation class, with the API class it was worked out for. Only a
     * composite is ever presented under another name, so for any other widget it is a fact about its
     * two classes, worked out once rather than for each widget's first record.
     */
    private static final ClassValue<java.util.concurrent.atomic.AtomicReference<RecordType>> RECORD_TYPES =
            new ClassValue<java.util.concurrent.atomic.AtomicReference<RecordType>>() {
                @Override
                protected java.util.concurrent.atomic.AtomicReference<RecordType> computeValue(Class<?> type) {
                    return new java.util.concurrent.atomic.AtomicReference<>();
                }
            };

    private static RecordType recordType(DartWidget impl, Object api) {
        java.util.concurrent.atomic.AtomicReference<RecordType> held = RECORD_TYPES.get(impl.getClass());
        RecordType type = held.get();
        if (type != null && type.api == api.getClass()) return type;
        type = new RecordType(api.getClass(), swtWidgetName(impl, (Widget) api));
        held.set(type);
        return type;
    }

    private static String swtWidgetName(DartWidget impl, Widget api) {
        String apiName = API_NAMES.get(api.getClass());
        String implName = IMPL_NAMES.get(impl.getClass());
        if (implName != null && !implName.equals(apiName)) {
            return implName;
        }
        // A composite whose layout is the e4 SashLayout and whose direct children are the
        // part-stacks is the main workbench area regardless of how deep the perspective nests
        // it -- serialize it as a MainComposite so the parts get the panel treatment
        // (gap/border/shadow). FlutterBridge.widgetName addresses it by the same rule.
        String presentedAs = Config.presentedName(impl, apiName);
        Config.describedAs(impl, presentedAs);
        return presentedAs;
    }

    /**
     * The SWT class a widget class stands for, worked out once per class: asking a class whether it
     * is anonymous, or for its package, is a call into the VM, and this is asked for every change.
     */
    @SuppressWarnings("unchecked")
    private static final ClassValue<String> API_NAMES = new ClassValue<String>() {
        @Override
        protected String computeValue(Class<?> type) {
            Class<? extends Widget> aClass = (Class<? extends Widget>) type;
            return aClass.isAnonymousClass() || !isOwnPackage(aClass)
                    ? Config.getSwtBaseClassName(aClass)
                    : aClass.getSimpleName();
        }
    };

    /** The widget an implementation class implements, from its {@code Dart} name, or null. */
    private static final ClassValue<String> IMPL_NAMES = new ClassValue<String>() {
        @Override
        protected String computeValue(Class<?> type) {
            String name = type.getSimpleName();
            return name.startsWith("Dart") ? name.substring(4) : null;
        }
    };

    private static boolean isOwnPackage(Class<? extends Widget> aClass) {
        return aClass.getPackage().getName().startsWith("org.eclipse.swt") || aClass.getPackage().getName().startsWith("com.equo.chromium");
    }

    /** Per resource value, the client it has been given to; {@code getValue()} caches, so identity is the resource's. */
    private static final java.util.Map<Object, Integer> resourceDelivery =
            java.util.Collections.synchronizedMap(new java.util.WeakHashMap<>());

    /** Whether {@code value} carries something the far side will keep and can be named by later. */
    private static boolean worthNaming(Object value) {
        if (!(value instanceof VImage)) return false;
        VImage image = (VImage) value;
        return image.getImageData() != null || image.getSvgContent() != null;
    }

    /**
     * A key equal for two images whose wire form is identical, for deduping a repeated icon within a
     * frame ({@link Tape#imageRef}). Zero when the image carries no pixels by value — its content is
     * a remote render ref the client already caches by id, so there is nothing to dedupe.
     */
    static long wireContentKey(org.eclipse.swt.graphics.Image image) {
        if (image == null || !(image.getImpl() instanceof DartImage)) return 0;
        VImage value = ((DartImage) image.getImpl()).getValue();
        if (value == null) return 0;
        String svg = value.getSvgContent();
        if (svg != null) return ImageDataCodec.contentKey(svg);
        org.eclipse.swt.graphics.ImageData data = value.getImageData();
        return data == null || data.data == null ? 0 : ImageDataCodec.contentKey(data);
    }

    /**
     * Whether this client already holds {@code value}. Nesting depth does not apply: a resource is
     * never the subject of its frame, and GC ops do not count depth.
     */
    private static boolean canReferenceResource(Object value) {
        if (!diffEnabled) return false;
        Integer given = resourceDelivery.get(value);
        return given != null && given == local().target;
    }

    /** Forgets that the images {@code value} names were delivered, for a client that says it lacks one. */
    public static void forgetResourcesNamedBy(VWidget value) {
        if (value == null) return;
        WidgetWire w = WidgetWire.borrow();
        try {
            w.reset();
            w.names(new WidgetWire.Names() {
                @Override
                public void widget(Object widget) {
                }

                @Override
                public void image(org.eclipse.swt.graphics.Image image) {
                    if (image.getImpl() instanceof DartImage) forgetResource(((DartImage) image.getImpl()).getValue());
                }
            });
            // Only the image properties: no other getter is read, the widget's state is left as it was.
            for (int key : value.wireKeys()) {
                String kind = key < WireTable.KINDS.length ? WireTable.KINDS[key] : null;
                if (!"image".equals(kind) && !"image[]".equals(kind)) continue;
                int at = w.size();
                int previous = w.lastKey();
                w.key(key);
                if (!value.writeWire(w, key)) w.rewind(at, previous);
            }
        } finally {
            w.names(null);
            WidgetWire.giveBack(w);
        }
    }

    /** Forgets that {@code value} was delivered, for a resource whose content has changed under it. */
    public static void forgetResource(Object value) {
        if (value != null) resourceDelivery.remove(value);
    }


}
