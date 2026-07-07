package org.eclipse.swt.widgets;

import dev.equo.swt.MockFlutterBridge;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.DartFont;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import java.lang.reflect.Field;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Pure-Flutter counterpart of {@code org.eclipse.swt.widgets.Mocks.swtShell()}/{@code swtDisplay()}
 * for the native/web backend, where there is no {@code Swt*} implementation at all — every widget,
 * including Display and Shell themselves, is Dart-backed. Mocks {@link DartShell}/{@link DartDisplay}
 * the same way the embedded suite mocks {@code SwtShell}/{@code SwtDisplay}, so a size test needs no
 * real renderer just to host a widget under measurement.
 *
 * <p>Unlike {@code org.eclipse.swt.widgets.Mocks} (which compiles per-OS against the embedded
 * backend's {@code Swt*} classes and therefore needs reflection to paper over per-OS method-name
 * differences), {@code DartShell}/{@code DartDisplay} are a single shared implementation
 * ({@code src/native/java}) across every native/web flavor, so the stubs below are plain Mockito.
 */
public class DartMocks {

    private static Display display;

    public static Shell dartShell() {
        return dartShell(dartDisplay());
    }

    public static Shell dartShell(Display display) {
        Shell shell = mock(Shell.class);
        DartShell dartShell = mock(DartShell.class);
        when(shell.getImpl()).thenReturn(dartShell);
        dartShell.display = display;
        when(shell.getDisplay()).thenReturn(display);
        when(shell.getShell()).thenReturn(shell);
        Color bg = new Color(red(), green(), blue());
        when(shell.getBackground()).thenReturn(bg);
        when(dartShell.getBackgroundColor()).thenReturn(bg);
        when(dartShell._display()).thenCallRealMethod();
        when(dartShell._getChildren()).thenReturn(new Control[0]);
        return shell;
    }

    public static Display dartDisplay() {
        if (display != null) return display;
        Display d = mock(Display.class);
        DartDisplay dartDisplay = mock(DartDisplay.class);
        dartDisplay.thread = Thread.currentThread();
        DartDisplay.Default = d;
        DartDisplay.register(d);
        when(d.getThread()).thenCallRealMethod();
        when(d.getImpl()).thenReturn(dartDisplay);
        when(d.getDPI()).thenReturn(new Point(96, 96));
        when(d.getSystemColor(anyInt())).thenReturn(new Color(red(), green(), blue()));
        when(d.getSystemCursor(anyInt())).thenReturn(mock(Cursor.class));
        when(dartDisplay.getThread()).thenCallRealMethod();
        Monitor monitor = mock(Monitor.class);
        when(monitor.getClientArea()).thenReturn(new Rectangle(0, 0, 1280, 720));
        when(d.getMonitors()).thenReturn(new Monitor[] {monitor});

        Font defaultFont = new DartFont(d, "System", 14, org.eclipse.swt.SWT.NORMAL, null).getApi();
        when(d.getSystemFont()).thenReturn(defaultFont);
        setDeviceField(dartDisplay, "systemFont", defaultFont);
        setDeviceField(dartDisplay, "dpi", mock(Point.class));

        display = d;
        return d;
    }

    /** {@code Font systemFont}/{@code Point dpi} live on {@code DartDevice} (package
     *  {@code org.eclipse.swt.graphics}), package-private and thus inaccessible from here. */
    private static void setDeviceField(DartDisplay dartDisplay, String name, Object value) {
        try {
            Field field = org.eclipse.swt.graphics.DartDevice.class.getDeclaredField(name);
            field.setAccessible(true);
            field.set(dartDisplay, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /** Pure-Flutter counterpart of {@code Mocks.table()}, for {@code TableItemSizeTest} (a
     *  TableItem needs a Table to live in, not just a Shell). */
    public static Table dartTable() {
        Table w = mock(Table.class);
        DartTable impl = mock(DartTable.class);
        when(w.getImpl()).thenReturn(impl);
        when(impl.getBridge()).thenReturn(new MockFlutterBridge());
        Display display = dartDisplay();
        impl.display = display; // DartWidget's constructor reads parent.getImpl().display directly
        when(w.getDisplay()).thenReturn(display);
        when(impl._display()).thenReturn(display);
        // getText()/getImage() etc. treat a false checkData() as "disposed" (see DartTableItem);
        // real checkData() lazily fires SWT.SetData for VIRTUAL tables, irrelevant to a plain mock.
        when(impl.checkData(org.mockito.ArgumentMatchers.any())).thenReturn(true);
        return w;
    }

    public static int red() {
        return 10;
    }

    public static int green() {
        return 10;
    }

    public static int blue() {
        return 10;
    }
}
