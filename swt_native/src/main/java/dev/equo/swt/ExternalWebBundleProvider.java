package dev.equo.swt;

/**
 * Web analogue of {@link ExternalBundleProvider}. Implemented by the owner of an external combined
 * WEB bundle (EWT). When Evolve creates a web Display and no {@code dev.equo.swt.web.dir} override is
 * set, {@code WebDisplayBridge} discovers the provider via ServiceLoader and asks it for the web
 * directory, so the WebFlutterServer serves that bundle (with EWT's web decoders) instead of Evolve's
 * own. Standalone Evolve (no provider on the classpath) is unaffected.
 */
public interface ExternalWebBundleProvider {

    /**
     * Extracts (once, cached) the external combined web bundle and returns the directory that
     * CONTAINS {@code index.html} (the value for {@code dev.equo.swt.web.dir}). Returns {@code null}
     * when no web bundle is available.
     */
    String extractAndGetWebDir();
}
