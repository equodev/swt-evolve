package org.eclipse.swt.widgets;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import static org.eclipse.swt.widgets.Mocks.*;
import org.junit.jupiter.api.*;
import static dev.equo.swt.harness.WireAssert.assertThatWire;
import dev.equo.swt.harness.WireAssert;

class ProgressBarSerializeTest extends SerializeTestBase {

    @Test
    void should_serialize_empty_ProgressBar() {
        ProgressBar w = new ProgressBar(shell(), SWT.NONE);
        WireAssert assertJ = assertThatWire(serialize(w));
        assertJ.containsEntry("id", w.hashCode())
               .containsEntry("swt", "ProgressBar");
    }

    @Test
    void should_serialize_filled_ProgressBar() {
        ProgressBar w = new ProgressBar(shell(), SWT.NONE);
        setAll(w);
        WireAssert assertJ = assertThatWire(serialize(w));
        assertJ.containsEntry("id", w.hashCode())
               .containsEntry("swt", "ProgressBar")
               .containsEntry("enabled", w.getEnabled())
               .containsEntry("toolTipText", w.getToolTipText())
               .containsEntry("visible", w.getVisible())
               .containsEntry("style", w.getStyle());
        assertJ.satisfies(node("maximum").equalsTo(w.getMaximum(), orAbsentIf0));
        assertJ.satisfies(node("minimum").equalsTo(w.getMinimum(), orAbsentIf0));
        assertJ.satisfies(node("selection").equalsTo(w.getSelection(), orAbsentIf0));
        assertJ.satisfies(node("background").equalsTo(w.getBackground(), orAbsentIfNull));
        assertJ.satisfies(node("backgroundImage").equalsTo(w.getBackgroundImage(), orAbsentIfNull));
        assertJ.satisfies(node("bounds").equalsTo(value(w).getBounds(), orAbsentIfNull));
        assertJ.satisfies(node("cursor").equalsTo(w.getCursor(), orAbsentIfNull));
        assertJ.satisfies(node("dragDetect").equalsTo(w.getDragDetect(), orAbsentIfFalse));
        assertJ.satisfies(node("font").equalsTo(w.getFont(), orAbsentIfNull));
        assertJ.satisfies(node("foreground").equalsTo(w.getForeground(), orAbsentIfNull));
        assertJ.satisfies(node("menu").equalsTo(w.getMenu(), orAbsentIfNull));
    }

    @Test
    void should_write_every_property_ProgressBar() throws Exception {
        assertWritesEveryProperty(style -> {
            ProgressBar w = new ProgressBar(shell(), style);
            setAll(w);
            fillUnsettable(w);
            return w;
        }, unwritable("tabStop", "ControlHelper.isTabStop never makes this widget a Tab stop"), SWT.NONE, SWT.SMOOTH, SWT.HORIZONTAL, SWT.VERTICAL, SWT.INDETERMINATE, SWT.BORDER, SWT.LEFT_TO_RIGHT, SWT.RIGHT_TO_LEFT, SWT.FLIP_TEXT_DIRECTION);
    }

    @Test
    void should_name_every_change_ProgressBar() {
        ProgressBar w = new ProgressBar(shell(), SWT.NONE);
        assertNamesEveryChange(w);
    }

    VProgressBar value(ProgressBar w) {
        return ((DartProgressBar) w.getImpl()).getValue();
    }
}
