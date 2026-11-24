package dev.equo.swt;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Impeller stays the renderer unless shells can get windows, and engines, of their own — except on a
 * Linux session whose GL stack is a software rasterizer, where Impeller would shade on the CPU and the
 * UI crawls, so it falls back to Skia. Software GL is detected from the live glxinfo renderer string,
 * not GPU hardware presence.
 */
class FlutterNativeImpellerTest {

    private String savedMode;
    private String savedImpeller;

    @BeforeEach
    void save() {
        savedMode = System.getProperty(WindowPolicy.MODE_PROPERTY);
        savedImpeller = System.getProperty(FlutterNative.IMPELLER_PROPERTY);
        System.clearProperty(WindowPolicy.MODE_PROPERTY);
        System.clearProperty(FlutterNative.IMPELLER_PROPERTY);
    }

    @AfterEach
    void restore() {
        WindowPolicy.setResolver(null);
        restore(WindowPolicy.MODE_PROPERTY, savedMode);
        restore(FlutterNative.IMPELLER_PROPERTY, savedImpeller);
    }

    private static void restore(String key, String value) {
        if (value == null) System.clearProperty(key);
        else System.setProperty(key, value);
    }

    // --- renderer policy (pure) -------------------------------------------------------------------

    @Test
    void offWindowsKeepsTheSingleWindowDefault() {
        assertThat(FlutterNative.decideImpeller(false, false, true)).isTrue();
        assertThat(FlutterNative.decideImpeller(false, false, false)).isFalse();
    }

    @Test
    void linuxWithAGpuStaysOnImpellerEvenInMultiWindowMode() {
        // The pre-fix behaviour: a GPU Linux box keeps Impeller regardless of window mode.
        assertThat(FlutterNative.decideImpeller(true, false, true)).isTrue();
        assertThat(FlutterNative.decideImpeller(true, false, false)).isTrue();
    }

    @Test
    void linuxOnSoftwareGlFallsBackToSkia() {
        assertThat(FlutterNative.decideImpeller(true, true, true)).isFalse();
        assertThat(FlutterNative.decideImpeller(true, true, false)).isFalse();
    }

    // --- the -D override, end to end --------------------------------------------------------------

    @Test
    void thePropertyForcesEitherWay() {
        System.setProperty(FlutterNative.IMPELLER_PROPERTY, "true");
        assertThat(FlutterNative.impellerEnabled()).isTrue();

        System.setProperty(FlutterNative.IMPELLER_PROPERTY, "false");
        assertThat(FlutterNative.impellerEnabled()).isFalse();
    }

    // --- software-GL detection (pure) -------------------------------------------------------------

    @Test
    void libglAlwaysSoftwareForcesSoftware() {
        assertThat(SoftwareGl.detect("1", null)).isTrue();
        assertThat(SoftwareGl.detect("0", null)).isFalse();
        assertThat(SoftwareGl.detect(null, null)).isFalse();
    }

    @Test
    void llvmpipeRendererIsSoftware() {
        String llvmpipe = String.join("\n",
                "    Device: llvmpipe (LLVM 21.1.8, 256 bits) (0xffffffff)",
                "    Accelerated: no",
                "OpenGL renderer string: llvmpipe (LLVM 21.1.8, 256 bits)");
        assertThat(SoftwareGl.classify(llvmpipe)).isTrue();
    }

    @Test
    void acceleratedHardwareIsNotSoftware() {
        assertThat(SoftwareGl.classify(String.join("\n",
                "    Accelerated: yes",
                "OpenGL renderer string: Mesa Intel(R) UHD Graphics (CML GT2)"))).isFalse();
        assertThat(SoftwareGl.classify(
                "OpenGL renderer string: NVIDIA GeForce RTX 3080/PCIe/SSE2")).isFalse();
    }

    @Test
    void loaderNoiseMentioningSwrastDoesNotMisclassifyARealGpu() {
        // A real GPU box can emit a swrast loader error on stderr/stdout; only the renderer and
        // Accelerated lines decide, so this must stay Impeller.
        String noisy = String.join("\n",
                "libGL error: failed to load driver: swrast",
                "    Accelerated: yes",
                "OpenGL renderer string: NVIDIA GeForce RTX 3080/PCIe/SSE2");
        assertThat(SoftwareGl.classify(noisy)).isFalse();
    }

    @Test
    void softpipeAndSwrastRenderersAreSoftware() {
        assertThat(SoftwareGl.classify("OpenGL renderer string: softpipe")).isTrue();
        assertThat(SoftwareGl.classify(
                "OpenGL renderer string: llvmpipe (LLVM 15, 128 bits)")).isTrue();
    }

    @Test
    void unknownOutputFallsBackToTheHardwareDefault() {
        assertThat(SoftwareGl.classify(null)).isFalse();
        assertThat(SoftwareGl.classify("")).isFalse();
    }
}
