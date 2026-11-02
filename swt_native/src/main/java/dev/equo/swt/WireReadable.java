package dev.equo.swt;

/** A value the client sends in the wire's encoding: its fields as key and value, read by {@link WireReader}. */
public interface WireReadable {

    /** Reads the field numbered {@code key} from {@code r}; false, with nothing read, when there is none. */
    boolean readWire(int key, WireReader r);
}
