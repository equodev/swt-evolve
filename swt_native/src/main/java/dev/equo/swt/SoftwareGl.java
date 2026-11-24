package dev.equo.swt;

import java.util.Locale;

/**
 * Whether this Linux session's GL stack is a software rasterizer (llvmpipe/swrast), probed once per
 * JVM. Keyed on the live {@code glxinfo -B} renderer string, not on GPU hardware presence: a box can
 * own a GPU its session can't reach (a remote desktop whose discrete card the session never sees), so
 * a render node exists yet every frame is still rasterized on the CPU.
 */
final class SoftwareGl {
    private SoftwareGl() {
    }

    private static final class Holder {
        static final boolean VALUE = detect(System.getenv("LIBGL_ALWAYS_SOFTWARE"), query());
    }

    /** True when the session renders through a software rasterizer. Computed once, then cached. */
    static boolean present() {
        return Holder.VALUE;
    }

    /** Pure policy over the two inputs; package-private for the test. */
    static boolean detect(String libglAlwaysSoftware, String glxinfoOutput) {
        if ("1".equals(libglAlwaysSoftware)) {
            return true;
        }
        return classify(glxinfoOutput);
    }

    /**
     * Pure classifier over {@code glxinfo -B} text; package-private for the test. Anchored to the
     * {@code OpenGL renderer string:} and {@code Accelerated:} lines so loader noise elsewhere (e.g.
     * a stderr {@code libGL error: failed to load driver: swrast} on a box with a real GPU) can't
     * misclassify an accelerated session as software.
     */
    static boolean classify(String glxinfoOutput) {
        if (glxinfoOutput == null) {
            return false;
        }
        for (String raw : glxinfoOutput.split("\n")) {
            String line = raw.trim().toLowerCase(Locale.ROOT);
            if (line.startsWith("accelerated:")) {
                if (line.contains("no")) {
                    return true;
                }
            } else if (line.startsWith("opengl renderer string:")) {
                if (line.contains("llvmpipe") || line.contains("softpipe")
                        || line.contains("swrast") || line.contains("software rasterizer")) {
                    return true;
                }
            }
        }
        return false;
    }

    /** {@code glxinfo -B} output, or null when it can't be run — then the GPU default stands. */
    private static String query() {
        String out = Commands.run("glxinfo", "-B");
        if (out == null || out.isEmpty()) {
            // glxinfo is absent (not installed by default on RHEL) or there is no display: leave the
            // GPU default (Impeller). Set LIBGL_ALWAYS_SOFTWARE=1 or -Ddev.equo.swt.impeller=false
            // to force Skia in that case.
            return null;
        }
        return out;
    }
}
