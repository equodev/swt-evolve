package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.DartCTabFolder;
import org.eclipse.swt.custom.SwtCTabFolder;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.instancio.Instancio;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
        swtDisplay.thread = Thread.currentThread();
        when(display.getThread()).thenCallRealMethod();
        when(display.getImpl()).thenReturn(swtDisplay);
        when(swtDisplay.getWidgetColor(anyInt())).thenReturn(new Color(10, 10, 10));
        when(display.getSystemColor(anyInt())).thenReturn(new Color(red(), green(), blue()));
        when(swtDisplay.getThread()).thenCallRealMethod();
        Font font = mock(Font.class);
        when(display.getSystemFont()).thenReturn(font);
        return display;
    }

    public static Composite composite() {
        return shell();
    }

    public static Device device() {
        return mock(Device.class);
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
        Display display = display();
        when(impl._display()).thenReturn(display);
        return w;
    }

    public static Canvas canvas() {
        Canvas w = mock(Canvas.class);
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
}
