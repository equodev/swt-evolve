package dev.equo.swt.awt;

import dev.equo.swt.comm.CommService;

/**
 * Carries swing-evolve's engine traffic over a Display's own connection, instead of the loopback
 * socket the engine opens by default, which a client on another machine cannot reach.
 *
 * <p>Whoever installs such a transport as the engine's registers it here too, in
 * {@code META-INF/services/dev.equo.swt.awt.SwingIslandTransport}, because the engine opens its
 * transport before any Display exists: {@link SwingIslandHost} hands it the Display's comm when an
 * island opens. While the engine reports no port of its own, an island with no transport taking
 * its Display's comm fails to open.
 */
public interface SwingIslandTransport {

    /**
     * Offered on every island that opens. Answers whether the engine's traffic rides {@code comm}
     * now; false when this transport is not the engine's, or already carries it over another
     * Display's comm.
     */
    boolean attach(CommService comm);
}
