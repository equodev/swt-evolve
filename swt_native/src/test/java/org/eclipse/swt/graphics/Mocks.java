package org.eclipse.swt.graphics;

import org.eclipse.swt.widgets.*;

import java.util.concurrent.ConcurrentHashMap;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class Mocks {
    public static void device(Display display, SwtDevice swtDisplay) {
        Font font = mock(Font.class);
        when(display.getSystemFont()).thenReturn(font);
        swtDisplay.systemFont = font;
        if ("win32".equals(org.eclipse.swt.SWT.getPlatform())) {
            try {
                java.lang.reflect.Field field = SwtDevice.class.getDeclaredField("resourcesWithZoomSupport");
                field.setAccessible(true);
                field.set(swtDisplay, ConcurrentHashMap.newKeySet());
            } catch (Exception e) {
                throw new RuntimeException("Failed to initialize resourcesWithZoomSupport", e);
            }
        }
    }
}
