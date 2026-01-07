package org.eclipse.swt.graphics;

import org.assertj.core.api.Assertions;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;

import java.util.concurrent.ConcurrentHashMap;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class Mocks {
    public static void device(Display display, SwtDevice swtDisplay) {
        Font defaultFont = new Font(display, "System", 14, SWT.NORMAL);
        Assertions.assertThat(defaultFont.getImpl()).isInstanceOf(DartFont.class);
        when(display.getSystemFont()).thenReturn(defaultFont);
        swtDisplay.systemFont = defaultFont;
        if ("win32".equals(org.eclipse.swt.SWT.getPlatform())) {
            try {
                java.lang.reflect.Field field = SwtDevice.class.getDeclaredField("resourcesWithZoomSupport");
                field.setAccessible(true);
                field.set(swtDisplay, ConcurrentHashMap.newKeySet());
            } catch (NoSuchFieldException e) {
                // Field doesn't exist, ignore
            } catch (Exception e) {
                throw new RuntimeException("Failed to initialize resourcesWithZoomSupport", e);
            }
        }
    }
}
