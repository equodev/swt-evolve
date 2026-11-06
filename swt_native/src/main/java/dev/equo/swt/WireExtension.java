package dev.equo.swt;

/**
 * Channels and types an app built on Evolve adds to the wire, numbered after {@link WireTable}'s own
 * by a table generated with it. Found as a {@link java.util.ServiceLoader} service; the client is
 * handed the same table, and {@link WireTable#FINGERPRINT} covers it.
 */
public interface WireExtension {

    /** The number of the first of {@link #channels()}. */
    int firstChannel();

    /** Channel names, numbered from {@link #firstChannel()}. */
    String[] channels();

    /** The number of the first of {@link #types()}. */
    int firstType();

    /** Type names, numbered from {@link #firstType()}. */
    String[] types();
}
