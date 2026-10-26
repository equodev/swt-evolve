package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** What the implementations of Widget, Control and Scrollable report when they are read directly. */
@Tag("native-unit")
class ControlImplementationNativeTest {

    private Shell shell;

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
        shell = DartMocks.dartShell();
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    @Test
    void theControlImplementationReportsWhatItHolds() {
        DartCanvas impl = new DartCanvas(shell, SWT.NONE, null);
        Canvas canvas = impl.getApi();
        Display display = shell.getDisplay();
        Menu menu = new Menu(shell, SWT.POP_UP);
        Image image = new Image(display, 4, 4);
        Font font = new Font(display, "Helvetica", 11, SWT.NORMAL);
        Cursor cursor = new Cursor(display, SWT.CURSOR_HAND);
        Region region = new Region(display);
        Object data = new Object();
        canvas.setToolTipText("tip");
        canvas.setLayoutData(data);
        canvas.setMenu(menu);
        canvas.setBackgroundImage(image);
        canvas.setFont(font);
        canvas.setCursor(cursor);
        canvas.setRegion(region);
        canvas.setTouchEnabled(true);
        canvas.setBackground(new Color(display, 1, 2, 3));
        canvas.setForeground(new Color(display, 4, 5, 6));
        canvas.setBounds(1, 2, 3, 4);
        canvas.setCapture(true);
        canvas.setDragDetect(false);
        canvas.setRedraw(false);

        assertThat(impl._parent()).isSameAs(shell);
        assertThat(impl._toolTipText()).isEqualTo("tip");
        assertThat(impl._layoutData()).isSameAs(data);
        assertThat(impl._drawCount()).isEqualTo(1);
        assertThat(impl._menu()).isSameAs(menu);
        assertThat(impl._backgroundImage()).isSameAs(image);
        assertThat(impl._font()).isSameAs(font);
        assertThat(impl._cursor()).isSameAs(cursor);
        assertThat(impl._region()).isSameAs(region);
        assertThat(impl._visibleRgn()).isZero();
        assertThat(impl._accessible()).isNull();
        canvas.getAccessible();
        assertThat(impl._accessible()).isNotNull();
        assertThat(impl._inCacheDisplayInRect()).isFalse();
        assertThat(impl._touchEnabled()).isTrue();
        assertThat(impl.__background().getRGB().red).isEqualTo(1);
        assertThat(impl.__foreground().getRGB().blue).isEqualTo(6);
        assertThat(impl._bounds()).isEqualTo(new Rectangle(1, 2, 3, 4));
        assertThat(impl._capture()).isTrue();
        assertThat(impl._dragDetect()).isFalse();
        assertThat(impl._enabled()).isTrue();
        assertThat(impl._redraw()).isFalse();
        assertThat(impl._orientation()).isZero();
        assertThat(impl._textDirection()).isZero();
        assertThat(impl.getExplicitFont()).isSameAs(font);
        canvas.setRedraw(true);
        assertThat(impl._drawCount()).isZero();
    }

    @Test
    void theWidgetImplementationReportsWhatItHolds() {
        DartCanvas impl = new DartCanvas(shell, SWT.NONE, null);
        Canvas canvas = impl.getApi();
        assertThat(impl._display()).isSameAs(shell.getDisplay());
        assertThat(impl._eventTable()).isNull();
        canvas.addListener(SWT.MouseDown, e -> {});
        EventTable table = impl._eventTable();
        assertThat(table).isNotNull();
        impl._eventTable(null);
        assertThat(canvas.isListening(SWT.MouseDown)).isFalse();
        impl._eventTable(table);
        assertThat(canvas.isListening(SWT.MouseDown)).isTrue();
        canvas.setData("data");
        assertThat(impl._data()).isEqualTo("data");
        assertThat(impl._jniRef()).isZero();
    }

    @Test
    void theScrollableImplementationReportsItsBars() {
        DartCanvas impl = new DartCanvas(shell, SWT.H_SCROLL | SWT.V_SCROLL, null);
        Canvas canvas = impl.getApi();
        assertThat(impl._horizontalBar()).isSameAs(canvas.getHorizontalBar());
        assertThat(impl._verticalBar()).isSameAs(canvas.getVerticalBar());
    }
}
