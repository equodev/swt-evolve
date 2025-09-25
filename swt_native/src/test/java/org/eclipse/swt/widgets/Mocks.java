package org.eclipse.swt.widgets;

import dev.equo.swt.MockFlutterBridge;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.DartCTabFolder;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.instancio.Instancio;
import org.instancio.InstancioObjectApi;
import org.instancio.Select;
import org.mockito.Mockito;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;

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
        return shell;
    }

    public static Display display() {
        Display display = mock(Display.class);
        SwtDisplay swtDisplay = mock(SwtDisplay.class);
        when(swtDisplay.isValidThread()).thenReturn(true);
        swtDisplay.thread = Thread.currentThread();
        SwtDisplay.Default =  display;
        when(display.getThread()).thenCallRealMethod();
        when(display.getImpl()).thenReturn(swtDisplay);
        try { // mac only
            Method getSystemColor = SwtDisplay.class.getDeclaredMethod("getWidgetColor", int.class);
            when(getSystemColor.invoke(swtDisplay, anyInt())).thenReturn(new Color(10, 10, 10));
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {}
        when(display.getSystemColor(anyInt())).thenReturn(new Color(red(), green(), blue()));
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
        when(impl.checkData(any())).thenReturn(true);
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
        when(impl.checkData(any())).thenReturn(true);
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
