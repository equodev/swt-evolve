package org.eclipse.swt.widgets;

import dev.equo.swt.MockFlutterBridge;
import org.eclipse.swt.accessibility.Accessible;
import org.eclipse.swt.accessibility.DartAccessible;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.DartCTabFolder;
import org.eclipse.swt.graphics.*;
import org.instancio.Instancio;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * The whole-tree-Flutter twin of the embedded {@code Mocks} in {@code src/test/java}: same surface,
 * every widget backed by {@code Dart*} instead of {@code Swt*}, because this backend has no
 * {@code Swt*} implementation at all — Display and Shell included.
 *
 * <p>Two source sets, one class name. A test migrates from the embedded suite to this one by being
 * moved; which fixture it binds to is decided by which source set compiles it.
 *
 * <p>It is the simpler of the two. The embedded fixture reaches its host through reflection because
 * {@code SwtShell}/{@code SwtDisplay} are generated per OS and their method names differ;
 * {@code DartShell}/{@code DartDisplay} are one shared implementation in {@code src/native/java},
 * so plain Mockito is enough.
 *
 * <p><b>The display comes from {@link DartMocks} rather than from here.</b> {@code DartDisplay}
 * keeps a static registry, and {@code checkDisplay} reads {@code getImpl().thread} off every entry
 * in it before letting a real {@code Display} be built. A second mock display, or one taken out of
 * the registry between tests, is enough to stop every later renderer test from opening a display at
 * all. One mock, registered once, is what the suite already runs with.
 */
public class Mocks implements BeforeEachCallback, AfterEachCallback {

    private Mocks() {
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        System.setProperty("dev.equo.swt.loadLibrary", "false");
        DartMocks.resetDisplayState();
    }

    /**
     * Deliberately does not touch the display registry — see the class note. The mock display is
     * shared and outlives every test, exactly as {@code DartMocks} has always left it.
     *
     */
    @Override
    public void afterEach(ExtensionContext context) {
        // Left set, not cleared: the suite runs with -Ddev.equo.swt.loadLibrary=false on the task,
        // and clearing it here wiped that for every later test in the same JVM. The ones that build
        // a Display bridge then reached the platform init, which on a runner without the native SWT
        // library dies in GTK's class initializer.
        System.setProperty("dev.equo.swt.loadLibrary", "false");
    }

    // --- host ----------------------------------------------------------------------------------

    public static Shell shell() {
        return DartMocks.dartShell();
    }

    public static Shell shell(Display display) {
        return DartMocks.dartShell(display);
    }

    public static Display display() {
        return DartMocks.dartDisplay();
    }

    public static Device device() {
        return display();
    }

    public static Control control() {
        return shell();
    }

    // --- containers ----------------------------------------------------------------------------

    public static Composite composite() {
        Composite w = mock(Composite.class);
        DartComposite impl = mock(DartComposite.class);
        ((DartWidget) impl).display = display();
        when(w.getImpl()).thenReturn(impl);
        when(impl._display()).thenReturn(display());
        when(impl._getChildren()).thenReturn(new Control[0]);
        return w;
    }

    public static CTabFolder cTabFolder() {
        CTabFolder w = mock(CTabFolder.class);
        DartCTabFolder impl = mock(DartCTabFolder.class);
        ((DartWidget) impl).display = display();
        when(w.getImpl()).thenReturn(impl);
        when(impl._display()).thenReturn(display());
        when(impl._getChildren()).thenReturn(new Control[0]);
        return w;
    }

    public static ToolBar toolBar() {
        ToolBar w = mock(ToolBar.class);
        DartToolBar impl = mock(DartToolBar.class);
        ((DartWidget) impl).display = display();
        when(w.getImpl()).thenReturn(impl);
        when(w.isEnabled()).thenReturn(true);
        when(impl.getBridge()).thenReturn(new MockFlutterBridge());
        when(impl._display()).thenReturn(display());
        return w;
    }

    public static TabFolder tabFolder() {
        TabFolder w = mock(TabFolder.class);
        DartTabFolder impl = mock(DartTabFolder.class);
        ((DartWidget) impl).display = display();
        when(w.getImpl()).thenReturn(impl);
        when(impl.getBridge()).thenReturn(new MockFlutterBridge());
        when(impl._display()).thenReturn(display());
        when(impl._getChildren()).thenReturn(new Control[0]);
        doNothing().when(impl).createItem(any(TabItem.class), anyInt());
        return w;
    }

    public static CoolBar coolBar() {
        CoolBar w = mock(CoolBar.class);
        DartCoolBar impl = mock(DartCoolBar.class);
        ((DartWidget) impl).display = display();
        when(w.getImpl()).thenReturn(impl);
        when(impl.getBridge()).thenReturn(new MockFlutterBridge());
        Display display = display();
        when(w.getDisplay()).thenReturn(display);
        when(impl._display()).thenReturn(display);
        when(impl._getChildren()).thenReturn(new Control[0]);
        doNothing().when(impl).createItem(any(CoolItem.class), anyInt());
        return w;
    }

    public static ExpandBar expandBar() {
        ExpandBar w = mock(ExpandBar.class);
        DartExpandBar impl = mock(DartExpandBar.class);
        ((DartWidget) impl).display = display();
        when(w.getImpl()).thenReturn(impl);
        when(impl.getBridge()).thenReturn(new MockFlutterBridge());
        Display display = display();
        when(w.getDisplay()).thenReturn(display);
        when(impl._display()).thenReturn(display);
        doNothing().when(impl).createItem(any(ExpandItem.class), anyInt(), anyInt());
        return w;
    }

    public static Table table() {
        return DartMocks.dartTable();
    }

    public static Tree tree() {
        return DartMocks.dartTree();
    }

    public static Text text() {
        Text w = mock(Text.class);
        DartControl impl = mock(DartControl.class);
        ((DartWidget) impl).display = display();
        when(w.getImpl()).thenReturn((IText) impl);
        when(impl._display()).thenReturn(display());
        when(impl.getBridge()).thenReturn(new MockFlutterBridge());
        when(impl.getTouchEnabled()).thenReturn(false);
        return w;
    }

    public static Canvas canvas() {
        Canvas w = mock(Canvas.class);
        DartCanvas impl = mock(DartCanvas.class);
        ((DartWidget) impl).display = display();
        when(w.getImpl()).thenReturn(impl);
        when(impl._display()).thenReturn(display());
        return w;
    }

    public static Canvas drawable() {
        return canvas();
    }

    public static Accessible accessible() {
        Control ctrl = control();
        Accessible w = mock(Accessible.class);
        DartAccessible impl = mock(DartAccessible.class);
        when(w.getImpl()).thenReturn(impl);
        when(impl._control()).thenReturn(ctrl);
        return w;
    }

    public static Menu menu() {
        return new Menu(shell());
    }

    // --- values --------------------------------------------------------------------------------

    public static int index() {
        return 0;
    }

    public static int aInt() {
        return Instancio.gen().ints().get();
    }

    public static RGB rGB() {
        return new RGB(red(), green(), blue());
    }

    public static int red() {
        return Instancio.gen().ints().range(1, 255).get();
    }

    public static int green() {
        return Instancio.gen().ints().range(1, 255).get();
    }

    public static int blue() {
        return Instancio.gen().ints().range(1, 255).get();
    }

    public static int width() {
        return Instancio.gen().ints().range(10, 100).get();
    }

    public static int height() {
        return Instancio.gen().ints().range(10, 100).get();
    }

    public static FontData fontData() {
        return new FontData("Arial", 12, org.eclipse.swt.SWT.NORMAL);
    }
}
