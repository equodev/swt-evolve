package org.eclipse.swt.widgets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** What {@link DartMenuItem} does when it is driven directly rather than through {@link MenuItem}. */
@Tag("native-unit")
class MenuItemImplementationNativeTest {

    private Menu menu;

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
        menu = new Menu(DartMocks.dartShell(), SWT.POP_UP);
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    @Test
    @DisplayName("a negative id is refused and leaves the id as it was")
    void negativeIdIsRefused() {
        MenuItem item = new MenuItem(menu, SWT.PUSH);
        item.setID(5);

        assertThatThrownBy(() -> item.setID(-1)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ((DartMenuItem) item.getImpl()).setID(Integer.MIN_VALUE))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(item.getID()).isEqualTo(5);
    }

    @Test
    @DisplayName("changing the selection of a disposed item reports it disposed rather than failing on its missing menu")
    void disposedItemRefusesASelectionChange() {
        MenuItem item = new MenuItem(menu, SWT.CHECK);
        DartMenuItem impl = (DartMenuItem) item.getImpl();
        item.dispose();
        impl.bridge = null;

        assertThatThrownBy(() -> item.setSelection(true))
                .isInstanceOf(SWTException.class)
                .satisfies(e -> assertThat(((SWTException) e).code).isEqualTo(SWT.ERROR_WIDGET_DISPOSED));
    }
}
