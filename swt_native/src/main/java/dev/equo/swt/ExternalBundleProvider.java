package dev.equo.swt;

/**
 * SPI implemented by the owner of an external combined app bundle (EWT). When Evolve runs
 * with no dev.equo.ewt.bundleDir set (a packaged product), FlutterLibraryLoader
 * discovers the provider via ServiceLoader and asks it for the bundle base, so the engine
 * boots that external libapp.so / flutter_assets instead of Evolve's own. Standalone Evolve
 * (no provider on the classpath) is unaffected.
 */
public interface ExternalBundleProvider {

    /**
     * Extracts (once, cached) the external combined bundle and returns the base directory
     * that CONTAINS the per-OS "release" layout — the value for dev.equo.ewt.bundleDir,
     * such that {@code <base>/linux/x64/release/bundle/lib/libapp.so} exists. Returns
     * {@code null} when no bundle is available.
     */
    String extractAndGetBundleBaseDir();
}
