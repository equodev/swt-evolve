package org.eclipse.swt.widgets;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.swt.internal.Callback;
import org.eclipse.swt.internal.cocoa.OS;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;

/**
 * An application may register an {@code NSAppleEventManager} handler for the launch-time
 * open-application event and spin {@code readAndDispatch()}/{@code sleep()} until it runs. On the
 * browser surface nothing else launches NSApp or drains its queue, so that loop never ends — hence
 * {@code -Ddev.equo.swt.mac.appleEventPump=true}, which pumps the queue of the NSApp the Display launches.
 *
 * <p>The pump is opt-in because it costs the Display thread: on an NSApp that was finish-launched
 * but never run, {@code nextEventMatchingMask} does not return while the queue stays empty, which
 * is the ordinary browser-surface run. Both halves of that contract are pinned here.
 *
 * <p>Apple Events are only delivered to the main thread, which the test worker is not, so each
 * scenario runs in a {@link MacChildJvm}.
 */
@Tag("native-unit")
@EnabledOnOs(org.junit.jupiter.api.condition.OS.MAC)
public class MacLaunchAppleEventNativeTest {

    private static final String DELIVERED = "open-application event delivered";

    private static final String DISPATCHED = "off-thread work dispatched";

    @Test
    void theWebDisplayDeliversTheOpenApplicationEventWhenThePumpIsAskedFor() throws Exception {
        assertThat(MacChildJvm.run(LaunchEventProbe.class, "-Ddev.equo.swt.mac.appleEventPump=true"))
                .as("the child's output")
                .contains(DELIVERED);
    }

    /**
     * Without the pump the Display thread has to come back from {@code readAndDispatch} and run what
     * another thread posted to it — every Flutter event arrives that way, so a thread that stays
     * inside AppKit leaves the client with a tree nothing ever resizes, repaints or answers.
     */
    @Test
    void theWebDisplayThreadRunsOffThreadWorkWhenThePumpIsNotAskedFor() throws Exception {
        assertThat(MacChildJvm.run(AsyncExecProbe.class))
                .as("the child's output")
                .contains(DISPATCHED);
    }

    /** What an application blocking its startup on the launch event does, on a web Display. */
    public static final class LaunchEventProbe {

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
            Callback callback = new Callback(LaunchEventProbe.class, "handleAppleEvent", 4);
            long handlerClass = OS.objc_allocateClassPair(OS.class_NSObject, "EvolveLaunchEventProbe", 0);
            OS.class_addMethod(handlerClass, selector, callback.getAddress(), "i@:@@");
            OS.objc_registerClassPair(handlerClass);
            long handler = OS.class_createInstance(handlerClass, 0);
            long manager = OS.objc_msgSend(OS.objc_getClass("NSAppleEventManager"),
                    OS.sel_registerName("sharedAppleEventManager"));
            OS.objc_msgSend(manager, OS.sel_registerName("setEventHandler:andSelector:forEventClass:andEventID:"),
                    handler, selector, kCoreEventClass, kAEOpenApplication);

            MacChildJvm.exitWith(MacChildJvm.spinUntil(display, () -> delivered)
                    ? DELIVERED : "open-application event never delivered");
        }
    }

    /** What the comm thread does on every client message: post to the Display thread and expect it to run. */
    public static final class AsyncExecProbe {

        public static void main(String[] args) {
            MacChildJvm.exitWith(MacChildJvm.dispatchesOffThreadWork(new Display())
                    ? DISPATCHED : "off-thread work never dispatched");
        }
    }
}
