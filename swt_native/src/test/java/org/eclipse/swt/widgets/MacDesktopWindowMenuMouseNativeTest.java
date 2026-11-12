package org.eclipse.swt.widgets;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.internal.cocoa.CGPoint;
import org.eclipse.swt.internal.cocoa.NSApplication;
import org.eclipse.swt.internal.cocoa.NSMenu;
import org.eclipse.swt.internal.cocoa.NSRect;
import org.eclipse.swt.internal.cocoa.NSScreen;
import org.eclipse.swt.internal.cocoa.NSString;
import org.eclipse.swt.internal.cocoa.NSWindow;
import org.eclipse.swt.internal.cocoa.OS;
import org.eclipse.swt.layout.FillLayout;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;

/**
 * A desktop window can carry an NSMenu of its own: an application may set one through the native
 * window it reaches. Under a host linked against an old enough SDK, AppKit's
 * default right-press handling on a view then takes the press as a contextual-menu request, so the
 * Flutter view's controller never sees a right press or release, and a right drag reaches the
 * application as plain moves. Context menus are SWT's own (MenuDetect), so these presses must reach
 * the engine whatever menu the window has.
 *
 * The engine reads the button from the OS's own events, so the probe posts real pointer input. A
 * session that may not post input sees not even the left drag, and the test is skipped there.
 */
@Tag("native-unit")
@EnabledOnOs(org.junit.jupiter.api.condition.OS.MAC)
public class MacDesktopWindowMenuMouseNativeTest {

    private static final String RESULT = "result:";

    private static final String NO_INPUT = "posted input does not reach this session";

    private static final String NO_WINDOW = "no painted window";

    @Test
    void aRightDragOnACanvasReachesTheApplicationWhenTheWindowHasAMenu() throws Exception {
        assertDragsArrive(MacChildJvm.run(DragProbe.class, "-Ddev.equo.swt.mode=desktop"));
    }

    /**
     * An RCP product's launcher can carry no SDK version at all. A host stamped 10.9 still gets its
     * right presses through; one stamped 10.5 loses them.
     */
    @Test
    void aRightDragReachesTheApplicationUnderAnOldSdkHostWhenTheWindowHasAMenu() throws Exception {
        assertDragsArrive(MacOldSdkHostNativeTest.runUnderOldSdkHost(DragProbe.class, "10.5"));
    }

    private static void assertDragsArrive(String output) {
        Assumptions.assumeFalse(output.contains(NO_INPUT), NO_INPUT);
        // What this test checks needs a window the engine paints; MacOldSdkHostNativeTest covers that.
        Assumptions.assumeFalse(output.contains(NO_WINDOW), NO_WINDOW);
        assertThat(output).as("the child's output").contains(RESULT);
        String result = output.substring(output.indexOf(RESULT));
        // "release N" is logged just before the probe posts the release: the drag must arrive while
        // the button is still down, not only together with the release.
        assertThat(result).as("a left drag").containsSubsequence("down 1", "drag 1", "release 1", "up 1");
        assertThat(result).as("a right drag").containsSubsequence("down 3", "drag 3", "release 3", "up 3");
        assertThat(result).as("a right press").contains("menu");
    }

    /**
     * Opens a desktop shell filled by a canvas, gives its window a menu, then drags across the canvas
     * with the left button and with the right one, and prints what the canvas's listeners saw.
     */
    public static final class DragProbe {

        private static final int LEFT = 0, RIGHT = 1;

        private static final int LEFT_DRAGGED = 6, RIGHT_DRAGGED = 7;

        public static void main(String[] args) {
            Display display = new Display();
            Shell shell = new Shell(display);
            shell.setLayout(new FillLayout());
            Canvas canvas = new Canvas(shell, SWT.NONE);
            List<String> seen = new ArrayList<>();
            boolean[] painted = new boolean[1];
            canvas.addListener(SWT.Paint, e -> painted[0] = true);
            canvas.addListener(SWT.MouseDown, e -> seen.add("down " + e.button));
            canvas.addListener(SWT.MouseUp, e -> seen.add("up " + e.button));
            canvas.addListener(SWT.MenuDetect, e -> seen.add("menu"));
            canvas.addListener(SWT.MouseMove, e -> {
                if ((e.stateMask & SWT.BUTTON3) != 0)
                    seen.add("drag 3");
                else if ((e.stateMask & SWT.BUTTON1) != 0)
                    seen.add("drag 1");
            });
            shell.setSize(400, 300);
            shell.open();
            if (!MacChildJvm.spinUntil(display, () -> MacOldSdkHostNativeTest.window() != null && painted[0]))
                MacChildJvm.exitWith(NO_WINDOW);
            NSWindow window = MacOldSdkHostNativeTest.window();
            NSMenu menu = (NSMenu) new NSMenu().alloc();
            menu = menu.initWithTitle(NSString.stringWith("Window"));
            menu.addItemWithTitle(NSString.stringWith("Item"), 0, NSString.stringWith(""));
            OS.objc_msgSend(window.id, OS.sel_registerName("setMenu:"), menu.id);
            NSApplication.sharedApplication().activateIgnoringOtherApps(true);
            window.makeKeyAndOrderFront(null);
            spinFor(display, 500);

            NSRect frame = window.frame();
            NSRect content = window.contentView().frame();
            double primaryHeight = new NSScreen(NSScreen.screens().objectAtIndex(0)).frame().height;
            CGPoint centre = new CGPoint();
            centre.x = frame.x + content.width / 2;
            centre.y = primaryHeight - (frame.y + content.height / 2);

            post(OS.kCGEventMouseMoved, centre, LEFT);
            spinFor(display, 300);
            drag(display, seen, centre, OS.kCGEventLeftMouseDown, LEFT_DRAGGED, OS.kCGEventLeftMouseUp, LEFT);
            if (!MacChildJvm.spinUntil(display, () -> seen.contains("up 1")) && !seen.contains("down 1"))
                MacChildJvm.exitWith(NO_INPUT);
            drag(display, seen, centre, OS.kCGEventRightMouseDown, RIGHT_DRAGGED, OS.kCGEventRightMouseUp, RIGHT);
            MacChildJvm.spinUntil(display, () -> seen.contains("up 3"));
            MacChildJvm.exitWith(RESULT + " " + String.join(", ", seen));
        }

        private static void drag(Display display, List<String> seen, CGPoint start, int down, int dragged, int up,
                int button) {
            post(down, start, button);
            spinFor(display, 100);
            CGPoint at = new CGPoint();
            for (int step = 1; step <= 5; step++) {
                at.x = start.x + step * 12;
                at.y = start.y + step * 6;
                post(dragged, at, button);
                spinFor(display, 60);
            }
            spinFor(display, 200);
            seen.add("release " + (button == LEFT ? 1 : 3));
            post(up, at, button);
            spinFor(display, 200);
        }

        private static void post(int type, CGPoint at, int button) {
            long source = OS.CGEventSourceCreate(OS.kCGEventSourceStateHIDSystemState);
            long event = OS.CGEventCreateMouseEvent(source, type, at, button);
            OS.CGEventPost(OS.kCGHIDEventTap, event);
            OS.CFRelease(event);
            if (source != 0)
                OS.CFRelease(source);
        }

        private static void spinFor(Display display, long millis) {
            long until = System.currentTimeMillis() + millis;
            while (System.currentTimeMillis() < until)
                if (!display.readAndDispatch())
                    Thread.yield();
        }
    }
}
