package org.eclipse.swt.widgets;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.swt.internal.cocoa.NSApplication;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;

/**
 * AWT loaded on the main thread decides from {@code [NSApp isRunning]} whether it embeds in a host
 * application or runs its own event loop there. Native SWT's Display runs NSApp, so an SWT
 * application that loads AWT gets the first; the web Display has to answer the same, or AWT keeps
 * the main thread and the Display thread never gets it back.
 *
 * <p>Each scenario runs in a {@link MacChildJvm}, so the Display and AWT share the main thread the
 * way an application's do.
 */
@Tag("native-unit")
@EnabledOnOs(org.junit.jupiter.api.condition.OS.MAC)
public class MacAwtOnWebDisplayNativeTest {

    private static final String RUNNING = "NSApp is running";

    private static final String STOPPED_ON_DISPOSE = "NSApp stops running with the Display";

    private static final String RUNNING_AGAIN = "NSApp runs again with a new Display";

    private static final String AWT_LOADED = "AWT loaded on the main thread";

    private static final String DISPATCHED = "Display thread dispatched after AWT";

    @Test
    void aWebDisplayLeavesNSAppRunning() throws Exception {
        assertThat(MacChildJvm.run(IsRunningProbe.class))
                .as("the child's output")
                .contains(RUNNING);
    }

    /** As native SWT's: NSApp reports running only while a Display is alive. */
    @Test
    void nsAppStopsRunningWithTheLastWebDisplay() throws Exception {
        assertThat(MacChildJvm.run(DisposeProbe.class))
                .as("the child's output")
                .contains(STOPPED_ON_DISPOSE)
                .contains(RUNNING_AGAIN);
    }

    /**
     * Whether a non-running NSApp costs the main thread depends on the JDK's AWT: Java 8's runs its
     * own loop there, Java 21's does not. Making NSApp report running must not cost it either.
     */
    @Test
    void aWebDisplayKeepsTheMainThreadAfterAwtLoadsOnIt() throws Exception {
        assertThat(MacChildJvm.run(AwtProbe.class, "-Djava.awt.headless=false"))
                .as("the child's output")
                .contains(AWT_LOADED)
                .contains(DISPATCHED);
    }

    /** Ends the child after twice the timeout with {@code message}, whatever the main thread is doing. */
    private static void haltAfterTimeout(String message) {
        Thread watchdog = new Thread(() -> {
            try {
                Thread.sleep(2 * MacChildJvm.TIMEOUT_MS);
            } catch (InterruptedException e) {
                return;
            }
            MacChildJvm.exitWith(message);
        });
        watchdog.setDaemon(true);
        watchdog.start();
    }

    private static boolean running() {
        return NSApplication.sharedApplication().isRunning();
    }

    public static final class IsRunningProbe {

        public static void main(String[] args) {
            new Display();
            MacChildJvm.exitWith(running() ? RUNNING : "NSApp is not running");
        }
    }

    public static final class DisposeProbe {

        public static void main(String[] args) {
            new Display().dispose();
            System.out.println(running() ? "NSApp still runs after the Display is disposed" : STOPPED_ON_DISPOSE);
            new Display();
            MacChildJvm.exitWith(running() ? RUNNING_AGAIN : "NSApp does not run with a new Display");
        }
    }

    /** What an application that loads AWT on the Display thread does, then its event loop. */
    public static final class AwtProbe {

        public static void main(String[] args) throws Exception {
            haltAfterTimeout("the main thread never came back from AWT");
            Display display = new Display();
            java.awt.Toolkit.getDefaultToolkit();
            // AWT's own startup is asynchronous: a font metric needs its toolkit thread to answer.
            new java.awt.Frame().getFontMetrics(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
            System.out.println(AWT_LOADED);
            System.out.flush();

            MacChildJvm.exitWith(MacChildJvm.dispatchesOffThreadWork(display)
                    ? DISPATCHED : "the Display thread never dispatched after AWT");
        }
    }
}
