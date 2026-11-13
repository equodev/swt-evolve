package org.eclipse.swt.widgets;

import java.util.*;
import java.util.stream.*;
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

public class VWidget {

    protected VWidget() {
    }

    protected VWidget(IWidget impl) {
        this.impl = impl;
    }

    protected IWidget impl;

    public int getStyle() {
        return ((DartWidget) impl).getStyle();
    }

    public void setStyle(int value) {
        ((DartWidget) impl).getApi().style = value;
    }

    /**
     * Numbers of the properties changed since the last send.
     */
    private final dev.equo.swt.ChangedKeys changed = new dev.equo.swt.ChangedKeys();

    /**
     * What the binary wire last sent of this value, kept by the writer; nothing else reads it.
     */
    public Object wireMemo;

    /**
     * Records that {@code key} changed and schedules the widget for the next flush. Both, always: a flag without a flush never ships, and a flush without a flag ships an update that claims nothing changed.
     */
    public void markDirty(int key) {
        assert dev.equo.swt.ChangedKeys.among(wireKeys(), key) : "key " + key + " is not a property of " + getClass().getSimpleName();
        if (!changed.add(key) && dev.equo.swt.Serializer.waitsForFlush(this))
            return;
        if (impl instanceof DartWidget)
            ((DartWidget) impl).dirty();
    }

    /**
     * The properties changed since the last send, in the order they changed.
     */
    public dev.equo.swt.ChangedKeys changedKeys() {
        return changed;
    }

    /**
     * {@link #changedKeys()} by name, in order, for whoever reads them: a log, a test.
     */
    public java.util.Set<String> changedNames() {
        java.util.Set<String> names = new java.util.LinkedHashSet<>();
        for (int i = 0; i < changed.size(); i++) names.add(wireName(changed.get(i)));
        return names;
    }

    public boolean anyDirty() {
        return !changed.isEmpty();
    }

    /**
     * Called once the widget has been sent; what follows is a fresh set of changes.
     */
    public void clearDirty() {
        changed.clear();
    }

    /**
     * Write stamp of the last state sent for this value. A partial update names it, so the far side can tell whether the update fits the state it holds.
     */
    private long sentSeq_ = 0L;

    /**
     * Which connection {@link #sentSeq_} was sent to. Delivery is a fact about one client, not about this object: a frame written for one connection tells a second nothing, and naming a widget it was never given costs it a round trip to ask for the widget again. One id rather than a set because a value is almost always written for one client; a second client re-describes it, which is the safe direction to be wrong in.
     */
    private int sentConn_ = 0;

    /**
     * The stamp this value carries for {@code connection}, or zero when that connection has not been given it.
     */
    public long sentSeq(int connection) {
        return sentConn_ == connection ? sentSeq_ : 0L;
    }

    /**
     * Records that the state stamped {@code seq} has been sent whole to {@code connection}, so nothing is outstanding for it and its next update is measured from here.
     */
    public void sent(int connection, long seq) {
        sentConn_ = connection;
        sentSeq_ = seq;
        changed.clear();
    }

    /**
     * Records that the state stamped {@code seq} has been sent to {@code connection}, forgetting only the properties {@code written} actually carried. A property can be changed after the walk that wrote this frame and before the frame is credited - a layout that adds a child while its parent is being serialized. Forgetting everything at that point drops a change whose new value nothing has sent, and nothing marks it again: the far side keeps the state from before it, and only an unrelated write to the same widget ever repairs that.
     */
    public void sent(int connection, long seq, int[] written) {
        sentConn_ = connection;
        sentSeq_ = seq;
        changed.removeAll(written);
    }

    public static final int STYLE = 676;

    /**
     * A property's name, by its number: for logs and tests.
     */
    public String wireName(int key) {
        switch(key) {
            case STYLE:
                return "style";
        }
        return null;
    }

    /**
     * Writes one property's value into a frame of the binary wire by its number; false for a number no class up to this one writes.
     */
    public boolean writeWire(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 676:
                w.write((int) getStyle());
                return true;
        }
        return false;
    }

    /**
     * Writes one property of a whole description, key and value, unless it is at a default the client starts from, which is recognised without writing it; returns which (dev.equo.swt.WidgetWire.WRITTEN, AT_ZERO, AT_EMPTY or UNHANDLED).
     */
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 676:
                {
                    int v_ = getStyle();
                    w.key(676);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return dev.equo.swt.WidgetWire.UNHANDLED;
    }

    private static final int[] wireKeys_ = new int[] { 676 };

    protected static int[] allWireKeys_() {
        return wireKeys_;
    }

    /**
     * The number of every property a whole description writes.
     */
    public int[] wireKeys() {
        return wireKeys_;
    }

    /**
     * Whether a whole description writes this property even at its default.
     */
    public boolean wireAlways(int key) {
        switch(key) {
            case 676:
                return true;
        }
        return false;
    }

    /**
     * Whether this property is written when empty: empty is not its default, null is.
     */
    public boolean wireKeepsEmpty(int key) {
        return false;
    }
}
