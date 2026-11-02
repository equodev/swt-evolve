package dev.equo.swt;

import dev.equo.swt.harness.WireValues;
import org.eclipse.swt.widgets.VWidget;

import java.util.Map;

/**
 * A value's state written out in full, past everything that decides what a frame leaves out: the
 * memory of what was sent, the client a frame is for, the change set. What the client should hold.
 */
public final class WireOracle {

    private WireOracle() {
    }

    /** Every property {@code value} writes, read back as the client reads them, defaults included. */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> fields(VWidget value) {
        WidgetWire wire = WidgetWire.borrow();
        try {
            wire.reset();
            for (int key : value.wireKeys()) {
                int at = wire.size();
                int previous = wire.lastKey();
                wire.key(key);
                if (!value.writeWire(wire, key)) wire.rewind(at, previous);
            }
            wire.varint(0);
            return (Map<String, Object>) WireValues.plain(WireDump.fields(wire.buffer(), 0, wire.size()));
        } finally {
            WidgetWire.giveBack(wire);
        }
    }
}
