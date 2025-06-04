package org.eclipse.swt.widgets;

import org.mockito.Mockito;

public class Mocks {
    public static Shell shell() {
        Display display = display();
        return shell(display);
    }

    public static Shell shell(Display display) {
        Shell shell = Mockito.mock(Shell.class);
        SwtShell swtShell = Mockito.mock(SwtShell.class);
        Mockito.when(shell.getImpl()).thenReturn(swtShell);
        swtShell.display = display;
        Mockito.when(shell.getShell()).thenReturn(shell);
        return shell;
    }

    public static Display display() {
        Display display = Mockito.mock(Display.class);
        SwtDisplay swtDisplay = Mockito.mock(SwtDisplay.class);
        swtDisplay.thread = Thread.currentThread();
        Mockito.when(display.getImpl()).thenReturn(swtDisplay);
        return display;
    }
}
