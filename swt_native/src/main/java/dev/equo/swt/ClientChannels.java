package dev.equo.swt;

import dev.equo.swt.comm.CommService;

/**
 * Channels a build listens to on every connection to a Flutter client, besides the bridge's own,
 * found through {@link java.util.ServiceLoader}.
 */
public interface ClientChannels {

    /** Registers this build's handlers on {@code comm}, a connection being set up. */
    void wire(CommService comm);
}
