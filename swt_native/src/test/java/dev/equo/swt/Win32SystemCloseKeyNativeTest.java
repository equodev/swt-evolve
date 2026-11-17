package dev.equo.swt;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.internal.Callback;
import org.eclipse.swt.internal.win32.OS;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;

/**
 * Alt+F4 is the system close. Win32 turns its press into SC_CLOSE and no control sees the key, so
 * native SWT fires no KeyDown for it on the focus control. The Flutter view must not get the press
 * or its release either, while every other key, F4 alone and Alt with another key, still reaches it.
 *
 * <p>The keys are posted to the Flutter view, which the test subclasses to see what reaches it.
 */
@Tag("native-unit")
@EnabledOnOs(org.junit.jupiter.api.condition.OS.WINDOWS)
public class Win32SystemCloseKeyNativeTest {

    private static final long F4_SCAN = 0x3EL << 16;
    private static final long F5_SCAN = 0x3FL << 16;
    private static final long ALT_DOWN = 1L << 29;
    private static final long RELEASE = (1L << 30) | (1L << 31);

    private final List<String> keysAtView = new ArrayList<>();
    private long viewProc;

    @Test
    void altF4ClosesTheWindowAndNeitherItsPressNorItsReleaseReachesTheView() throws Exception {
        withView((context, view) -> {
            OS.PostMessage(view, OS.WM_SYSKEYDOWN, OS.VK_F4, 1 | F4_SCAN | ALT_DOWN);
            OS.PostMessage(view, OS.WM_SYSKEYUP, OS.VK_F4, 1 | F4_SCAN | ALT_DOWN | RELEASE);

            assertThat(FlutterNative.pump(context)).as("pump result").isEqualTo(FlutterNative.PUMP_CLOSE_REQUESTED);
            assertThat(keysAtView).as("keys the view got").isEmpty();
        });
    }

    @Test
    void f4AloneAndAltWithAnotherKeyStillReachTheView() throws Exception {
        withView((context, view) -> {
            OS.PostMessage(view, OS.WM_SYSKEYDOWN, OS.VK_F5, 1 | F5_SCAN | ALT_DOWN);
            OS.PostMessage(view, OS.WM_SYSKEYUP, OS.VK_F5, 1 | F5_SCAN | ALT_DOWN | RELEASE);
            OS.PostMessage(view, OS.WM_KEYDOWN, OS.VK_F4, 1 | F4_SCAN);
            OS.PostMessage(view, OS.WM_KEYUP, OS.VK_F4, 1 | F4_SCAN | RELEASE);

            assertThat(FlutterNative.pump(context)).as("pump result").isNotEqualTo(FlutterNative.PUMP_CLOSE_REQUESTED);
            assertThat(keysAtView).as("keys the view got").startsWith(
                    key(OS.WM_SYSKEYDOWN, OS.VK_F5), key(OS.WM_SYSKEYUP, OS.VK_F5),
                    key(OS.WM_KEYDOWN, OS.VK_F4), key(OS.WM_KEYUP, OS.VK_F4));
        });
    }

    long viewProc(long hwnd, long msg, long wParam, long lParam) {
        if (msg == OS.WM_KEYDOWN || msg == OS.WM_KEYUP || msg == OS.WM_SYSKEYDOWN || msg == OS.WM_SYSKEYUP) {
            keysAtView.add(key(msg, wParam));
        }
        return OS.CallWindowProc(viewProc, hwnd, (int) msg, wParam, lParam);
    }

    private static String key(long msg, long vk) {
        String kind = msg == OS.WM_KEYDOWN ? "KEYDOWN" : msg == OS.WM_KEYUP ? "KEYUP"
                : msg == OS.WM_SYSKEYDOWN ? "SYSKEYDOWN" : "SYSKEYUP";
        return kind + "(0x" + Long.toHexString(vk) + ")";
    }

    private interface ViewCheck {
        void run(long context, long view) throws Exception;
    }

    private void withView(ViewCheck check) throws Exception {
        try {
            FlutterLibraryLoader.initialize();
        } catch (FlutterLibraryLoader.LibraryLoaderException e) {
            Assumptions.abort("no Windows runner built here: " + e.getMessage());
        }
        try (ServerSocket nobody = new ServerSocket(0)) {
            long context = FlutterNative.initialize(nobody.getLocalPort(), 0, 1, "Shell", "light", 0, 0, 400, 300);
            assertThat(context).as("window created").isNotZero();
            Callback callback = new Callback(this, "viewProc", 4);
            long view = OS.GetWindow(FlutterNative.getView(context), OS.GW_CHILD);
            try {
                assertThat(view).as("Flutter view").isNotZero();
                viewProc = OS.SetWindowLongPtr(view, OS.GWLP_WNDPROC, callback.getAddress());
                check.run(context, view);
            } finally {
                if (viewProc != 0) {
                    OS.SetWindowLongPtr(view, OS.GWLP_WNDPROC, viewProc);
                }
                FlutterNative.dispose(context);
                FlutterNative.pumpMessages(Integer.MAX_VALUE);
                callback.dispose();
            }
        }
    }
}
