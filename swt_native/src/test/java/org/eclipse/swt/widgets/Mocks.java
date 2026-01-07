package org.eclipse.swt.widgets;

import dev.equo.swt.MockFlutterBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.DartCTabFolder;
import org.eclipse.swt.graphics.*;
import org.instancio.Instancio;
import org.instancio.InstancioObjectApi;
import org.instancio.Select;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

public class Mocks implements AfterEachCallback {

    private static Display display;

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        if (display != null) {
            Mocks.dispose(display);
            display = null;
        }
    }

    public static Shell shell() {
        Display display = display();
        return shell(display);
    }

    public static Shell shell(Display display) {
        Shell shell = mock(Shell.class);
        SwtShell swtShell = mock(SwtShell.class);
        when(shell.getImpl()).thenReturn(swtShell);
        swtShell.display = display;
        when(shell.getShell()).thenReturn(shell);
        when(shell.getBackground()).thenReturn(new Color(red(), green(), blue()));
        when(swtShell._display()).thenCallRealMethod();
        when(swtShell._getChildren()).thenReturn(new Control[0]);
        when(swtShell.menuShell()).thenReturn((Decorations) shell);
        try { // Windows and macOS
            Method getShell = SwtShell.class.getDeclaredMethod("getShell");
            when(getShell.invoke(swtShell)).thenReturn(shell);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {}
        try { // Linux
            Method getShell = SwtShell.class.getDeclaredMethod("_getShell");
            when(getShell.invoke(swtShell)).thenReturn(shell);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {}
        return shell;
    }

    public static Display display() {
        if (display != null)
            return display;
        Display display = mock(Display.class);
        SwtDisplay swtDisplay = mock(SwtDisplay.class);
        when(swtDisplay.isValidThread()).thenReturn(true);
        swtDisplay.thread = Thread.currentThread();
        SwtDisplay.Default = display;
        SwtDisplay.register(display);
        when(display.getThread()).thenCallRealMethod();
        when(display.getImpl()).thenReturn(swtDisplay);
        try { // mac only
            Method getSystemColor = SwtDisplay.class.getDeclaredMethod("getWidgetColor", int.class);
            when(getSystemColor.invoke(swtDisplay, anyInt())).thenReturn(new Color(10, 10, 10));
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {}
        when(display.getSystemColor(anyInt())).thenReturn(new Color(red(), green(), blue()));
        when(display.getSystemCursor(anyInt())).thenReturn(mock(Cursor.class));
        when(swtDisplay.getThread()).thenCallRealMethod();
        org.eclipse.swt.graphics.Mocks.device(display, swtDisplay);

        try {
            Field field = SwtDevice.class.getDeclaredField("dpi");
            field.setAccessible(true);
            field.set(swtDisplay, mock(Point.class));
        } catch (NoSuchFieldException e) {
            // Field doesn't exist on Windows, only on Linux and macOS
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        Mocks.display = display;
        return display;
    }

    public static Composite composite() {
        return shell();
    }

    public static Device device() {
        return display();
    }

    public static CTabFolder cTabFolder() {
        CTabFolder w = mock(CTabFolder.class);
        DartCTabFolder impl = mock(DartCTabFolder.class);
        when(w.getImpl()).thenReturn(impl);
        Display display = display();
        when(impl._display()).thenReturn(display);
        return w;
    }

    public static ToolBar toolBar() {
        ToolBar w = mock(ToolBar.class);
        DartToolBar impl = mock(DartToolBar.class);
        when(w.getImpl()).thenReturn(impl);
        when(impl.getBridge()).thenReturn(new MockFlutterBridge());
        Display display = display();
        //when(w.getDisplay()).thenReturn(display);
        when(impl._display()).thenReturn(display);
        return w;
    }

    public static TabFolder tabFolder() {
        TabFolder w = mock(TabFolder.class);
        DartTabFolder impl = mock(DartTabFolder.class);
        when(w.getImpl()).thenReturn(impl);
        when(impl.getBridge()).thenReturn(new MockFlutterBridge());
        Display display = display();
        when(impl._display()).thenReturn(display);
        when(impl._getChildren()).thenReturn(new Control[0]);
        doNothing().when(impl).createItem(any(TabItem.class), anyInt());
        return w;
    }

    public static CoolBar coolBar() {
        CoolBar w = mock(CoolBar.class);
        DartCoolBar impl = mock(DartCoolBar.class);
        when(w.getImpl()).thenReturn(impl);
        when(impl.getBridge()).thenReturn(new MockFlutterBridge());
        Display display = display();
        //when(w.getDisplay()).thenReturn(display);
        when(impl._display()).thenReturn(display);
        when(impl._getChildren()).thenReturn(new Control[0]);
        doNothing().when(impl).createItem(any(CoolItem.class), anyInt());
        try { // fixPoint method exists in Linux and macOS
            Method fixPoint = DartCoolBar.class.getDeclaredMethod("fixPoint", int.class, int.class);
            fixPoint.setAccessible(true);
            when(fixPoint.invoke(impl, anyInt(), anyInt())).thenAnswer(invocation -> {
                int x = invocation.getArgument(0);
                int y = invocation.getArgument(1);
                return new Point(x, y);
            });
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {}
        return w;
    }

    public static Table table() {
        Table w = mock(Table.class);
        DartTable impl = mock(DartTable.class);
        try { // Windows only
            Method checkData = DartTable.class.getDeclaredMethod("checkData", TableItem.class, boolean.class);
            when(checkData.invoke(impl, any(TableItem.class), anyBoolean())).thenReturn(true);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {}
        try { // Linux and macOS
            Method checkData = DartTable.class.getDeclaredMethod("checkData", TableItem.class);
            when(checkData.invoke(impl, any(TableItem.class))).thenReturn(true);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {}
        when(w.getImpl()).thenReturn(impl);
        when(impl.getBridge()).thenReturn(new MockFlutterBridge());
        Display display = display();
        //when(w.getDisplay()).thenReturn(display);
        when(impl._display()).thenReturn(display);
        return w;
    }

    public static Tree tree() {
        Tree w = mock(Tree.class);
        DartTree impl = mock(DartTree.class);
        try { // Windows only
            Method checkData = DartTree.class.getDeclaredMethod("checkData", TreeItem.class, boolean.class);
            when(checkData.invoke(impl, any(TreeItem.class), anyBoolean())).thenReturn(true);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {}
        try { // Linux and macOS
            Method checkData = DartTree.class.getDeclaredMethod("checkData", TreeItem.class);
            when(checkData.invoke(impl, any(TreeItem.class))).thenReturn(true);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {}
        when(w.getImpl()).thenReturn(impl);
        when(impl.getBridge()).thenReturn(new MockFlutterBridge());
        Display display = display();
        //when(w.getDisplay()).thenReturn(display);
        when(impl._display()).thenReturn(display);
        return w;
    }

    public static Text text() {
        Text w = mock(Text.class);
        DartControl impl = mock(DartControl.class);

        when(w.getImpl()).thenReturn((IText) impl);
        when(impl._display()).thenReturn(display());
        when(impl.getBridge()).thenReturn(new MockFlutterBridge());
        when(impl.getTouchEnabled()).thenReturn(false);

        return w;
    }

    public static ExpandBar expandBar() {
        ExpandBar w = mock(ExpandBar.class);
        DartExpandBar impl = mock(DartExpandBar.class);
        when(w.getImpl()).thenReturn(impl);
        when(impl.getBridge()).thenReturn(new MockFlutterBridge());
        Display display = display();
        //when(w.getDisplay()).thenReturn(display);
        when(impl._display()).thenReturn(display);
        doNothing().when(impl).createItem(any(ExpandItem.class), anyInt(), anyInt());
        return w;
    }

    public static Control control() {
        return shell();
    }

    public static Menu menu() {
        return new Menu(shell());
    }

    public static int index() {
        return 0;
    }

    public static Canvas canvas() {
        Canvas w = mock(Canvas.class);
        InstancioObjectApi<Canvas> inst = Instancio.ofObject(w);
        if ("gtk".equals(org.eclipse.swt.SWT.getPlatform())) {
            inst = inst.set(Select.field(Widget.class, "handle"), Instancio.gen().longs().range(1L, Long.MAX_VALUE).get());
        } else if ("win32".equals(org.eclipse.swt.SWT.getPlatform())) {
            inst = inst.set(Select.field(Control.class, "handle"), Instancio.gen().longs().range(1L, Long.MAX_VALUE).get());
        }
        inst.fill();
        DartCanvas impl = mock(DartCanvas.class);
        when(w.getImpl()).thenReturn(impl);
        Display display = display();
        when(impl._display()).thenReturn(display);
        return w;
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

    public static Canvas drawable() {
        return canvas();
    }

    public static org.eclipse.swt.graphics.FontData fontData() {
        return new org.eclipse.swt.graphics.FontData("Arial", 12, org.eclipse.swt.SWT.NORMAL);
    }

    public static void dispose(Display current) {
        if (SwtDisplay.Default == current)
            SwtDisplay.Default = null;
        SwtDisplay.deregister(current);
    }

}
