package org.eclipse.swt.widgets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import dev.equo.swt.outside.DisplaySubclasses;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** What the Display answers through its public API, as SWT answers it. */
@Tag("flutter-it")
class DisplayApiFlutterTest {

    private Display display;

    @BeforeEach
    void setUp() {
        display = newDisplay();
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed())
            display.dispose();
        FlutterBridge.set(null);
    }

    private static Display newDisplay() {
        FlutterBridge.set(new RecordingBridge());
        try {
            return new Display();
        } finally {
            FlutterBridge.set(null);
        }
    }

    private boolean spinUntil(BooleanSupplier done) {
        long deadline = System.currentTimeMillis() + 2000;
        while (!done.getAsBoolean() && System.currentTimeMillis() < deadline) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        return done.getAsBoolean();
    }

    @Test
    void theBoundsAndTheCursorLocationAreCopies() {
        Rectangle bounds = display.getBounds();
        bounds.width = -1;
        assertThat(display.getBounds().width).isPositive();
        Point location = display.getCursorLocation();
        location.x = 12345;
        assertThat(display.getCursorLocation().x).isNotEqualTo(12345);
    }

    @Test
    void theBoundsTheShellsAndTheColorsAreRefusedOnceDisposed() {
        display.dispose();
        assertThatThrownBy(display::getBounds).isInstanceOf(SWTException.class)
                .extracting(e -> ((SWTException) e).code).isEqualTo(SWT.ERROR_DEVICE_DISPOSED);
        assertThatThrownBy(display::getShells).isInstanceOf(SWTException.class)
                .extracting(e -> ((SWTException) e).code).isEqualTo(SWT.ERROR_DEVICE_DISPOSED);
        assertThatThrownBy(() -> display.getSystemColor(SWT.COLOR_RED)).isInstanceOf(SWTException.class)
                .extracting(e -> ((SWTException) e).code).isEqualTo(SWT.ERROR_DEVICE_DISPOSED);
    }

    @Test
    void theBasicColorsAreSharedAndTransparentIsWhite() {
        for (int id = SWT.COLOR_WHITE; id <= SWT.COLOR_DARK_GRAY; id++)
            assertThat(display.getSystemColor(id)).as("color %d", id).isSameAs(display.getSystemColor(id));
        assertThat(display.getSystemColor(SWT.COLOR_TRANSPARENT)).isSameAs(display.getSystemColor(SWT.COLOR_TRANSPARENT));
        assertThat(display.getSystemColor(SWT.COLOR_TRANSPARENT).getRGB()).isEqualTo(new RGB(255, 255, 255));
        assertThat(display.getSystemColor(SWT.COLOR_TRANSPARENT).getAlpha()).isZero();
        assertThat(display.getSystemColor(-1)).isSameAs(display.getSystemColor(SWT.COLOR_BLACK));
    }

    @Test
    void theSystemIconsAreSharedAndGoWithTheDisplay() {
        int[] ids = {SWT.ICON_ERROR, SWT.ICON_INFORMATION, SWT.ICON_QUESTION, SWT.ICON_WARNING, SWT.ICON_WORKING};
        List<Image> icons = new ArrayList<>();
        for (int id : ids) {
            Image icon = display.getSystemImage(id);
            assertThat(icon).as("icon %d", id).isNotNull().isSameAs(display.getSystemImage(id));
            assertThat(icon.getBounds()).isEqualTo(new Rectangle(0, 0, DisplayHelper.ICON_SIZE, DisplayHelper.ICON_SIZE));
            int canonical = DisplayHelper.systemIconOf(id);
            assertThat(icon).as("icon %d", id).isSameAs(display.getSystemImage(canonical));
            icons.add(icon);
        }
        assertThat(display.getSystemImage(SWT.ICON_CANCEL)).isNull();
        display.dispose();
        assertThat(icons).allMatch(Image::isDisposed);
    }

    @Test
    void aSubclassIsRefusedUnlessItAllowsItself() {
        display.dispose();
        FlutterBridge.set(new RecordingBridge());
        try {
            assertThatThrownBy(DisplaySubclasses::plain).isInstanceOf(SWTException.class)
                    .extracting(e -> ((SWTException) e).code).isEqualTo(SWT.ERROR_INVALID_SUBCLASS);
            assertThat(Display.getCurrent()).isNull();
            display = DisplaySubclasses.allowingItself();
        } finally {
            FlutterBridge.set(null);
        }
        assertThat(Display.getCurrent()).isSameAs(display);
    }

    @Test
    void thePlatformAnswersAreTheSameOnEveryPlatform() {
        assertThat(display.getDismissalAlignment()).isEqualTo(SWT.RIGHT);
        assertThat(display.getIconDepth()).isEqualTo(display.getDepth());
        assertThat(display.getIconSizes()).isNotEmpty().allMatch(size -> size.x > 0 && size.x == size.y);
        assertThat(display.getCursorSizes()).isNotEmpty().allMatch(size -> size.x > 0 && size.x == size.y);
    }
}
