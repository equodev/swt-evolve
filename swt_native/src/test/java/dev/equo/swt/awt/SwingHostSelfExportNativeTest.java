package dev.equo.swt.awt;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import dev.equo.swt.Config;

/**
 * Regression guard for the Swing bridge's runtime module access (the {@code SWT_AWT.new_Frame}
 * path).
 *
 * <p>{@code EvolveContent implements sun.swing.LightweightContent}, so from Java 17 on the bridge
 * only works when the JVM can reach {@code java.desktop/sun.swing} (and {@code sun.awt}). Without
 * the {@code --add-exports} for those packages, defining that class throws {@code IllegalAccessError}
 * straight into the host application. The fix lets {@link EvolveSwingHost} open the packages itself
 * from {@code --add-opens=java.base/java.lang=ALL-UNNAMED} alone (what the affected app's {@code .ini}
 * already carries). When it cannot, it prints a single diagnostic and {@code SWT_AWT.new_Frame}
 * fails with {@code ERROR_NOT_IMPLEMENTED}.</p>
 *
 * <p>The production test tasks pass the {@code --add-exports} themselves, so the scenarios can only
 * be told apart in a <em>child</em> JVM whose flags we control. Each case forks one and reads back
 * whether {@code java.desktop} exported {@code sun.swing} to our module before and after
 * {@link EvolveSwingHost#canHost()}, and what {@code canHost()} returned.</p>
 */
@Tag("native-unit")
public class SwingHostSelfExportNativeTest {

    private static final long TIMEOUT_MS = 30_000;

    private static final String OPEN_ONLY = "--add-opens=java.base/java.lang=ALL-UNNAMED";

    /**
     * The field case: only {@code --add-opens=java.base/java.lang} is present (no {@code --add-exports}).
     * The host must open {@code sun.swing} to itself and host the bridge — the editor renders.
     */
    @Test
    void openingJavaLangIsEnoughForTheBridgeToExportSunSwingToItself() throws Exception {
        String out = runProbe(OPEN_ONLY);
        assertThat(out).as("child output with --add-opens only").contains("BEFORE_EXPORT=false");
        assertThat(out).as("the host exported sun.swing to itself").contains("AFTER_EXPORT=true");
        assertThat(out).as("the bridge can host").contains("CANHOST=true");
        assertThat(out).as("no access error escaped").doesNotContain("IllegalAccessError");
    }

    /**
     * No flags at all: the host cannot open the packages (no {@code --add-opens} for the reflection
     * it needs), so it must fail closed — {@code canHost()} false, no error escaping — and name the
     * missing flags once.
     */
    @Test
    void withNeitherFlagTheBridgeFailsClosedAndNamesTheMissingFlags() throws Exception {
        String out = runProbe();
        assertThat(out).as("child output with no flags").contains("CANHOST=false");
        assertThat(out).as("the diagnostic names --add-exports").contains("--add-exports=java.desktop/sun.swing");
        assertThat(out).as("the diagnostic names the --add-opens alternative").contains(OPEN_ONLY);
        assertThat(out).as("the diagnostic says what happens instead").contains("SWT_AWT.new_Frame will fail")
                .doesNotContain("falls back");
        assertThat(out).as("no access error escaped").doesNotContain("Exception in thread");
    }

    /**
     * The stock case the production tasks run under: the {@code --add-exports} are already there, so
     * the host finds {@code sun.swing} exported and hosts without opening anything itself.
     */
    @Test
    void withTheAddExportsFlagsTheBridgeHostsDirectly() throws Exception {
        String out = runProbe(
                "--add-exports=java.desktop/sun.swing=ALL-UNNAMED",
                "--add-exports=java.desktop/sun.awt=ALL-UNNAMED");
        assertThat(out).as("child output with --add-exports").contains("BEFORE_EXPORT=true");
        assertThat(out).as("the bridge can host").contains("CANHOST=true");
    }

    /**
     * A runtime image made without {@code jdk.unsupported.desktop}, as a product that ships its own
     * trimmed runtime has: {@code --limit-modules} gives the child the same boot layer. The bridge
     * must load and host there, its dispatcher included.
     */
    @Test
    void theBridgeHostsOnARuntimeWithoutJdkUnsupportedDesktop() throws Exception {
        String out = runProbe(Probe.class, "--limit-modules=java.se,jdk.unsupported", OPEN_ONLY);
        assertThat(out).as("the child runtime really lacks the module").contains("UNSUPPORTED_DESKTOP=false");
        assertThat(out).as("the bridge can host").contains("CANHOST=true");
        assertThat(out).as("the dispatcher can be defined").contains("DISPATCHER=true");
        assertThat(out).as("no linkage error escaped").doesNotContain("NoClassDefFoundError");
    }

    /**
     * A runtime the bridge cannot even be loaded on. {@code SWT_AWT.new_Frame} has no other way to
     * host Swing in a Dart-backed composite, so the entry point it calls must say why and fail,
     * rather than return nothing and send the caller into the native embedding path.
     */
    @Test
    void aBridgeThatCannotBeLoadedSaysWhyAndRefusesTheFrame() throws Exception {
        String out = runProbe(NewFrameProbe.class, "--limit-modules=java.base", OPEN_ONLY);
        assertThat(out).as("the host class cannot be loaded at all").contains("HOST_LOADS=false");
        assertThat(out).as("new_Frame fails as not implemented").contains("NEW_FRAME=org.eclipse.swt.SWTError: Not implemented");
        assertThat(out).as("the error carries the linkage failure").contains("CAUSE=java.lang.NoClassDefFoundError");
        assertThat(out).as("the diagnostic names the missing module").contains("needs the java.desktop module");
    }

    /** The internals are unreachable (no flags at all): the same refusal, after the existing diagnostic. */
    @Test
    void withNeitherFlagNewFrameIsRefusedInsteadOfFallingBack() throws Exception {
        String out = runProbe(NewFrameProbe.class);
        assertThat(out).as("the host class loads; only its JDK-internal classes cannot be defined").contains("HOST_LOADS=true");
        assertThat(out).as("new_Frame fails as not implemented").contains("NEW_FRAME=org.eclipse.swt.SWTError: Not implemented");
        assertThat(out).as("the error carries the access failure").contains("CAUSE=java.lang.IllegalAccessError");
    }

    private static String runProbe(String... vmArgs) throws Exception {
        return runProbe(Probe.class, vmArgs);
    }

    /** Runs {@code main} in a child JVM with exactly {@code vmArgs} (and nothing else), returns its output. */
    private static String runProbe(Class<?> main, String... vmArgs) throws Exception {
        Path log = Files.createTempFile("swing-host-self-export", ".log");
        try {
            List<String> command = new ArrayList<>();
            command.add(System.getProperty("java.home") + File.separator + "bin" + File.separator + "java");
            for (String vmArg : vmArgs) command.add(vmArg);
            command.add("-cp");
            command.add(System.getProperty("java.class.path"));
            command.add(main.getName());
            Process child = new ProcessBuilder(command).redirectErrorStream(true).redirectOutput(log.toFile()).start();
            if (!child.waitFor(TIMEOUT_MS, TimeUnit.MILLISECONDS)) child.destroyForcibly();
            return new String(Files.readAllBytes(log), StandardCharsets.UTF_8);
        } finally {
            Files.deleteIfExists(log);
        }
    }

    /** Reports whether the Swing bridge can reach its JDK internals on the flags this JVM was given. */
    public static final class Probe {
        public static void main(String[] args) {
            System.out.println("UNSUPPORTED_DESKTOP=" + ModuleLayer.boot().findModule("jdk.unsupported.desktop").isPresent());
            Module desktop = ModuleLayer.boot().findModule("java.desktop").orElseThrow();
            Module self = EvolveSwingHost.class.getModule();
            System.out.println("BEFORE_EXPORT=" + desktop.isExported("sun.swing", self));
            boolean canHost = EvolveSwingHost.canHost();
            System.out.println("CANHOST=" + canHost);
            System.out.println("AFTER_EXPORT=" + desktop.isExported("sun.swing", self));
            try {
                Class.forName("dev.equo.swt.awt.EvolveSwingHost$EvolveDispatcherWrapper");
                System.out.println("DISPATCHER=true");
            } catch (ClassNotFoundException | LinkageError cannotDefine) {
                System.out.println("DISPATCHER=" + cannotDefine);
            }
            System.out.flush();
        }
    }

    /**
     * Calls what {@code SWT_AWT.new_Frame} calls for a Dart-backed composite, and reports the outcome:
     * whether the host class loads at all, the error, and its cause.
     */
    public static final class NewFrameProbe {
        public static void main(String[] args) {
            try {
                Class.forName("dev.equo.swt.awt.EvolveSwingHost");
                System.out.println("HOST_LOADS=true");
            } catch (ClassNotFoundException | LinkageError cannotLoad) {
                System.out.println("HOST_LOADS=false");
            }
            try {
                System.out.println("NEW_FRAME=" + Config.newSwingFrame(null));
            } catch (Throwable t) {
                System.out.println("NEW_FRAME=" + t);
                System.out.println("CAUSE=" + t.getCause());
            }
            System.out.flush();
        }
    }
}
