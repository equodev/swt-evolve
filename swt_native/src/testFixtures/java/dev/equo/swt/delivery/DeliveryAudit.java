package dev.equo.swt.delivery;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.GcFrames;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Watches what the widget tree puts on the wire and works out, independently, what it should have
 * put there.
 *
 * <p>The point is the independence. Once updates carry only what changed, the set of changed
 * properties is decided by dirty bits, and a property whose setter forgets to flag its bit simply
 * never ships — silently, and only for that one property, on whichever widget nobody wrote a test
 * for. An assertion built on the dirty bits cannot see that; it would be asking the accused to
 * testify. So this keeps its own snapshot of each channel's last frame and derives the changed set
 * by <em>comparing states</em>. What the mechanism claims changed can then be checked against what
 * demonstrably did.
 *
 * <p>Because the derivation needs nothing but two payloads, this works on any workload the harness
 * can launch — a widget test, the parity suite, a gallery run — rather than only on cases someone
 * thought to write an assertion for.
 *
 * <p>Test scope on purpose: production carries only the observer hook it installs into, which is
 * inert while unset.
 */
public final class DeliveryAudit implements FlutterBridge.SendObserver, AutoCloseable {

    /**
     * One observed outbound frame: a widget's record as its state, any other frame as its payload
     * decoded; {@code size} is the bytes it took on the wire.
     */
    public record Frame(String channel, Object value, int size) {

        /** The value as a record of properties; fails when it is not one. */
        @SuppressWarnings("unchecked")
        public Map<String, Object> state() {
            if (!(value instanceof Map)) throw new AssertionError(channel + " carries no record: " + value);
            return (Map<String, Object>) value;
        }
    }

    private final List<Frame> frames = new ArrayList<>();

    /** Canonical form of the last frame seen per channel — the state to derive the next diff from. */
    private final Map<String, Map<String, Object>> lastSent = new LinkedHashMap<>();

    /** Changed keys per observed frame, in arrival order, derived by comparison. */
    private final List<Map.Entry<String, Set<String>>> derivedChanges = new ArrayList<>();

    /**
     * What a client would be holding, rebuilt from the frames alone.
     *
     * <p>Comparing consecutive payloads stops working once they are partial: the frame is the
     * sender's claim, so checking it against itself proves nothing. This applies each frame the way
     * a client would - whole replaces, partial merges the names it carries - and the result can then
     * be held against Java's actual state, which the frames had no part in producing.
     *
     * <p>It reconstructs the client rather than being one, so it verifies the <em>sender</em>: that
     * what was named covers what changed. Whether the real client merges correctly is a different
     * question, answered by the merge tests on that side.
     */
    private final Map<String, Map<String, Object>> reconstructed = new LinkedHashMap<>();

    /** Installs itself as the bridge's observer; {@link #close()} removes it. */
    public static DeliveryAudit install() {
        DeliveryAudit audit = new DeliveryAudit();
        FlutterBridge.setSendObserver(audit);
        return audit;
    }

    @Override
    public void onSend(String eventName, byte[] payload) {
        if (dev.equo.swt.WidgetWire.CHANNEL.equals(eventName)) {
            for (dev.equo.swt.harness.WireFrames.Record record : dev.equo.swt.harness.WireFrames.records(payload)) {
                observe(record.channel(), record.state(), record.size());
            }
            return;
        }
        GcFrames.Decoded frame = GcFrames.decode(eventName, payload);
        frames.add(new Frame(frame.event(), frame.value(), payload.length));
    }

    private void observe(String eventName, Map<String, Object> state, int size) {
        frames.add(new Frame(eventName, state, size));
        Map<String, Object> canonical = Canon.canon(state);
        Map<String, Object> previous = lastSent.put(eventName, canonical);
        derivedChanges.add(Map.entry(eventName, previous == null
                ? firstDelivery(canonical)
                : changedKeys(previous, canonical)));
        applyToReconstruction(eventName, state);
    }

    /** Applies one frame to the reconstructed client state, as the client would. */
    private void applyToReconstruction(String channel, Map<String, Object> frame) {
        if (!frame.containsKey("_d")) {
            reconstructed.put(channel, new LinkedHashMap<>(frame));
            return;
        }
        Map<String, Object> held = reconstructed.get(channel);
        if (held == null) return; // nothing to merge into; the gate would have asked for it whole
        for (Object named : (List<?>) frame.get("_d")) {
            String key = String.valueOf(named);
            if (frame.containsKey(key)) held.put(key, frame.get(key));
            else held.remove(key);
        }
    }

    /**
     * Asserts that what the frames would have built matches the state they were describing.
     *
     * <p>This is the check the dirty bits cannot make about themselves: a property that changed but
     * was never named is missing from the reconstruction and present in the truth, and no amount of
     * inspecting the flags would reveal it.
     */
    public void assertConverged(String channel, Map<String, Object> truthState) {
        Map<String, Object> held = reconstructed.get(channel);
        if (held == null) {
            throw new AssertionError("nothing was ever delivered on " + channel);
        }
        Map<String, Object> reconstructedState = Canon.canon(held);
        Map<String, Object> truth = Canon.canon(truthState);
        if (!reconstructedState.equals(truth)) {
            throw new AssertionError("what was sent on " + channel + " does not add up to the state "
                    + "it describes.\n  a client would hold: " + reconstructedState
                    + "\n  Java actually has:   " + truth
                    + "\n  missing from the updates: "
                    + changedKeys(reconstructedState, truth));
        }
    }

    @Override
    public void close() {
        FlutterBridge.setSendObserver(null);
    }

    public List<Frame> frames() {
        return List.copyOf(frames);
    }

    public List<Frame> framesOn(String channelPrefix) {
        return frames.stream().filter(f -> f.channel().startsWith(channelPrefix)).toList();
    }

    /** Changed keys derived for the n-th observed frame. */
    public Set<String> changedKeysOf(int index) {
        return derivedChanges.get(index).getValue();
    }

    /** Changed keys derived for the most recent frame on {@code channel}, or null if none. */
    public Set<String> lastChangedKeysOn(String channel) {
        for (int i = derivedChanges.size() - 1; i >= 0; i--) {
            if (derivedChanges.get(i).getKey().equals(channel)) return derivedChanges.get(i).getValue();
        }
        return null;
    }

    public void reset() {
        frames.clear();
        lastSent.clear();
        derivedChanges.clear();
    }

    /** Every key of a first delivery is a change: the far side held nothing before it. */
    private static Set<String> firstDelivery(Map<String, Object> canonical) {
        return new LinkedHashSet<>(canonical.keySet());
    }

    /**
     * Keys that differ between two canonical payloads — added, removed, or holding a different
     * value. Top level only: that is the granularity a per-property protocol names its changes at,
     * so it is the granularity the claim has to be checked at. A nested difference surfaces as its
     * containing top-level key having changed, which is exactly the claim being made about it.
     *
     * <p>Except where the nested thing is a widget in its own right. It has a channel of its own and
     * records its own changes on it, so a difference confined to the inside of one is not this
     * widget's to report — a scrollbar's increment travels as the scrollbar's, not as the tree's.
     * What still counts is the identity of what is held: a different widget, or a different set of
     * them, is a change to the property naming them.
     */
    public static Set<String> changedKeys(Map<String, Object> before, Map<String, Object> after) {
        Set<String> changed = new LinkedHashSet<>();
        for (Map.Entry<String, Object> entry : after.entrySet()) {
            Object previous = before.get(entry.getKey());
            if (previous == null) {
                changed.add(entry.getKey());
            } else if (!previous.equals(entry.getValue())
                    && !sameWidgets(previous, entry.getValue())) {
                changed.add(entry.getKey());
            }
        }
        for (String key : before.keySet()) {
            if (!after.containsKey(key)) changed.add(key); // cleared back to its default
        }
        return changed;
    }

    /**
     * Whether both sides name the same widget, or the same widgets in the same order.
     *
     * <p>A widget payload is recognised by carrying its own {@code swt} and {@code id}; a value with
     * no identity of its own - a rectangle, a colour - is state, and a difference in one is a real
     * difference in the property holding it.
     */
    private static boolean sameWidgets(Object before, Object after) {
        if (before instanceof Map && after instanceof Map) {
            Map<?, ?> a = (Map<?, ?>) before, b = (Map<?, ?>) after;
            return a.containsKey("swt") && b.containsKey("swt")
                    && a.containsKey("id") && Objects.equals(a.get("id"), b.get("id"));
        }
        if (before instanceof List && after instanceof List) {
            List<?> a = (List<?>) before, b = (List<?>) after;
            if (a.isEmpty() || a.size() != b.size()) return false;
            for (int i = 0; i < a.size(); i++) {
                if (!sameWidgets(a.get(i), b.get(i))) return false;
            }
            return true;
        }
        return false;
    }
}
