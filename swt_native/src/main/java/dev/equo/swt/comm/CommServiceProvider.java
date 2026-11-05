package dev.equo.swt.comm;

/**
 * A transport for the comm other than the built-in one, found through {@link java.util.ServiceLoader}.
 * {@code -Dcomm.impl=<name>} selects the provider whose {@link #name()} matches; with no match the
 * built-in {@link BinaryCommService} is used.
 */
public interface CommServiceProvider {

    /** The value of {@code -Dcomm.impl} that selects this transport. */
    String name();

    /** A new, unwired comm on this transport. */
    CommService create();
}
