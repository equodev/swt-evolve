package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A control created with BORDER answers the width of the border it draws, as Win32 and GTK do; without BORDER it
 * answers 0. A Group or a Sash answers 0 either way, as GTK and Cocoa do.
 */
@ExtendWith(Mocks.class)
class BorderWidthTest {

    @BeforeAll
    static void useEquo() {
        Config.forceEquo();
    }

    @AfterAll
    static void reset() {
        Config.defaultToEclipse();
    }

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    @Test
    @DisplayName("a control without BORDER has no border width")
    void aControlWithoutABorderHasNoBorderWidth() {
        Shell shell = Mocks.shell();
        for (Control control : new Control[] {new Canvas(shell, SWT.NONE), new Composite(shell, SWT.NONE),
                new Text(shell, SWT.SINGLE), new Button(shell, SWT.PUSH), new Label(shell, SWT.NONE)})
            assertThat(control.getBorderWidth()).as(control.toString()).isZero();
    }

    @Test
    @DisplayName("a bordered composite or canvas answers the line its trim takes on each side")
    void aBorderedCompositeAnswersTheLineItsTrimTakes() {
        Shell shell = Mocks.shell();
        for (Composite composite : new Composite[] {new Composite(shell, SWT.BORDER), new Canvas(shell, SWT.BORDER)})
            assertThat(composite.getBorderWidth()).as(composite.toString())
                    .isPositive()
                    .isEqualTo(-composite.computeTrim(0, 0, 0, 0).x);
    }

    @Test
    @DisplayName("a bordered text answers a border width")
    void aBorderedTextAnswersABorderWidth() {
        Shell shell = Mocks.shell();
        for (Text text : new Text[] {new Text(shell, SWT.SINGLE | SWT.BORDER), new Text(shell, SWT.MULTI | SWT.BORDER)})
            assertThat(text.getBorderWidth()).as(text.toString()).isPositive();
    }

    @Test
    @DisplayName("a bordered label answers a border width and its size grows by it on each side")
    void aBorderedLabelGrowsByItsBorderWidth() {
        Shell shell = Mocks.shell();
        Label plain = new Label(shell, SWT.NONE);
        Label bordered = new Label(shell, SWT.BORDER);
        plain.setText("Label");
        bordered.setText("Label");
        int border = bordered.getBorderWidth();
        assertThat(border).isPositive();
        for (Point hints : new Point[] {new Point(SWT.DEFAULT, SWT.DEFAULT), new Point(40, 20)}) {
            Point size = plain.computeSize(hints.x, hints.y);
            assertThat(bordered.computeSize(hints.x, hints.y)).as(hints.toString())
                    .isEqualTo(new Point(size.x + 2 * border, size.y + 2 * border));
        }
    }

    @Test
    @DisplayName("a bordered group or sash has no border width")
    void aBorderedGroupOrSashHasNoBorderWidth() {
        Shell shell = Mocks.shell();
        assertThat(new Group(shell, SWT.BORDER).getBorderWidth()).isZero();
        assertThat(new Sash(shell, SWT.BORDER | SWT.VERTICAL).getBorderWidth()).isZero();
    }
}
