package org.eclipse.swt.widgets;

import dev.equo.swt.MockFlutterBridge;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.DartCTabFolder;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.RGB;
import org.instancio.Instancio;
import org.instancio.InstancioObjectApi;
import org.instancio.Select;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

public class Mocks {
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
        Display display = mock(Display.class);
        SwtDisplay swtDisplay = mock(SwtDisplay.class);
        when(swtDisplay.isValidThread()).thenReturn(true);
        swtDisplay.thread = Thread.currentThread();
        SwtDisplay.Default =  display;
        SwtDisplay.register(display);
        when(display.getThread()).thenCallRealMethod();
        when(display.getImpl()).thenReturn(swtDisplay);
        try { // mac only
            Method getSystemColor = SwtDisplay.class.getDeclaredMethod("getWidgetColor", int.class);
            when(getSystemColor.invoke(swtDisplay, anyInt())).thenReturn(new Color(10, 10, 10));
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {}
        when(display.getSystemColor(anyInt())).thenReturn(new Color(red(), green(), blue()));
        when(display.getSystemFont()).thenReturn(mock(org.eclipse.swt.graphics.Font.class));
        when(swtDisplay.getThread()).thenCallRealMethod();
        org.eclipse.swt.graphics.Mocks.device(display, swtDisplay);
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
}
