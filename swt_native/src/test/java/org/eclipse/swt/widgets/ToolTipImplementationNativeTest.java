package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/** Where {@link DartToolTip} tells Flutter to draw the tip. */
@Tag("native-unit")
class ToolTipImplementationNativeTest {

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

    private void pointerAt(int x, int y) {
        when(shell.getDisplay().getCursorLocation()).thenReturn(new Point(x, y));
    }

    private static VToolTip value(ToolTip tip) {
        return ((DartToolTip) tip.getImpl()).getValue();
    }

    @Test
    @DisplayName("a tip shown without a location is drawn where the pointer is")
    void withoutALocationTheTipGoesToThePointer() {
        pointerAt(120, 80);
        ToolTip tip = new ToolTip(shell, SWT.BALLOON);
        tip.setText("Title");
        value(tip).clearDirty();

        tip.setVisible(true);

        assertThat(value(tip).getLocation()).isEqualTo(new Point(120, 80));
        assertThat(value(tip).changedKeys()).contains(VToolTip.LOCATION);
    }

    @Test
    @DisplayName("a location with either coordinate at -1 counts as none")
    void aMinusOneCoordinateMeansNoLocation() {
        pointerAt(7, 9);
        ToolTip tip = new ToolTip(shell, SWT.NONE);
        tip.setLocation(30, -1);
        tip.setVisible(true);
        assertThat(value(tip).getLocation()).isEqualTo(new Point(7, 9));

        tip.setLocation(-1, 40);
        assertThat(value(tip).getLocation()).isEqualTo(new Point(7, 9));
    }

    @Test
    @DisplayName("a location given is where the tip is drawn, shown or not")
    void aGivenLocationIsKept() {
        pointerAt(7, 9);
        ToolTip tip = new ToolTip(shell, SWT.NONE);
        tip.setLocation(30, 40);
        assertThat(value(tip).getLocation()).isEqualTo(new Point(30, 40));
        tip.setVisible(true);
        assertThat(value(tip).getLocation()).isEqualTo(new Point(30, 40));
        tip.setLocation(new Point(50, 60));
        assertThat(value(tip).getLocation()).isEqualTo(new Point(50, 60));
    }

    @Test
    @DisplayName("a shown tip without a location follows the pointer when its text changes, as upstream reconfigures it")
    void aShownTipIsPlacedAgainWhenItsTextChanges() {
        pointerAt(5, 5);
        ToolTip tip = new ToolTip(shell, SWT.NONE);
        tip.setVisible(true);
        pointerAt(50, 60);
        tip.setText("Title");
        assertThat(value(tip).getLocation()).isEqualTo(new Point(50, 60));
        pointerAt(70, 80);
        tip.setMessage("Message");
        assertThat(value(tip).getLocation()).isEqualTo(new Point(70, 80));
        value(tip).clearDirty();
        tip.setMessage("Message");
        assertThat(value(tip).changedKeys()).doesNotContain(VToolTip.LOCATION);
    }
}
