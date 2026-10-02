package org.eclipse.swt.widgets;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.swt.internal.Callback;
import org.eclipse.swt.internal.cocoa.NSApplication;
import org.eclipse.swt.internal.cocoa.OS;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;

/**
 * An application may register an {@code NSAppleEventManager} handler for the launch-time
 * open-application event and spin {@code readAndDispatch()}/{@code sleep()} until it runs. On the
 * browser surface nothing else launches NSApp or drains its queue, so that loop never ends.
 *
 * <p>Apple Events are only delivered to the main thread, which the test worker is not, so the
 * scenario runs in a child JVM started with {@code -XstartOnFirstThread}.
 */
@Tag("native-unit")
@EnabledOnOs(org.junit.jupiter.api.condition.OS.MAC)
public class MacLaunchAppleEventNativeTest {

    private static final long TIMEOUT_MS = 20_000;

    private static final String DELIVERED = "open-application event delivered";

    @Test
    void theWebDisplayDeliversTheOpenApplicationEvent() throws Exception {
        try {
            NSApplication.sharedApplication();
        } catch (Throwable notAvailable) {
            Assumptions.abort("no native SWT library on this runner: " + notAvailable);
            return;
        }

        Path log = Files.createTempFile("launch-apple-event", ".log");
        try {
            List<String> command = new ArrayList<>();
            command.add(System.getProperty("java.home") + File.separator + "bin" + File.separator + "java");
            command.add("-XstartOnFirstThread");
            command.add("-Ddev.equo.swt.mode=web");
            command.add("-Dequo.swt.browser=none");
            command.add("-Ddev.equo.swt.crashReport.disabled=true");
            command.add("-cp");
            command.add(System.getProperty("java.class.path"));
            command.add(Probe.class.getName());
            Process child = new ProcessBuilder(command).redirectErrorStream(true).redirectOutput(log.toFile()).start();
            boolean exited = child.waitFor(TIMEOUT_MS + 30_000, TimeUnit.MILLISECONDS);
            if (!exited)
                child.destroyForcibly();

            String output = new String(Files.readAllBytes(log), StandardCharsets.UTF_8);
            assertThat(output).as("the child's output").contains(DELIVERED);
        } finally {
            Files.deleteIfExists(log);
        }
    }

    /** What an application blocking its startup on the launch event does, on a web Display. */
    public static final class Probe {

        private static final long kCoreEventClass = 0x61657674L; // 'aevt'

        private static final long kAEOpenApplication = 0x6f617070L; // 'oapp'

        private static volatile boolean delivered;

        static long handleAppleEvent(long id, long sel, long event, long reply) {
            delivered = true;
            return 0;
        }

        public static void main(String[] args) {
            Display display = new Display();
            long selector = OS.sel_registerName("handleAppleEvent:withReplyEvent:");
            Callback callback = new Callback(Probe.class, "handleAppleEvent", 4);
            long handlerClass = OS.objc_allocateClassPair(OS.class_NSObject, "EvolveLaunchEventProbe", 0);
            OS.class_addMethod(handlerClass, selector, callback.getAddress(), "i@:@@");
            OS.objc_registerClassPair(handlerClass);
            long handler = OS.class_createInstance(handlerClass, 0);
            long manager = OS.objc_msgSend(OS.objc_getClass("NSAppleEventManager"),
                    OS.sel_registerName("sharedAppleEventManager"));
            OS.objc_msgSend(manager, OS.sel_registerName("setEventHandler:andSelector:forEventClass:andEventID:"),
                    handler, selector, kCoreEventClass, kAEOpenApplication);

            long deadline = System.currentTimeMillis() + TIMEOUT_MS;
            while (!delivered && System.currentTimeMillis() < deadline) {
                if (!display.readAndDispatch())
                    display.sleep();
            }
            System.out.println(delivered ? DELIVERED : "open-application event never delivered");
            System.out.flush();
            Runtime.getRuntime().halt(0);
        }
    }
}
