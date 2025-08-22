package org.eclipse.swt.graphics;

import org.eclipse.swt.widgets.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class Mocks {
    public static void device(Display display, SwtDevice swtDisplay) {
        Font font = mock(Font.class);
        when(display.getSystemFont()).thenReturn(font);
        swtDisplay.systemFont = font;
    }
}
