package dev.equo.swt;

/**
 * A value written into the binary widget wire field by field, each field as its number and its value:
 * the data classes, resources and other values a widget's properties hold. The ones the client sends
 * back (an event) are read the same way.
 */
public interface WireObject extends WireReadable {

    void writeWireFields(WidgetWire w);

    /** This object's class's number: what a value whose class is only known when written carries. */
    int wireTypeId();

    @Override
    default boolean readWire(int key, WireReader r) {
        return false;
    }
}
