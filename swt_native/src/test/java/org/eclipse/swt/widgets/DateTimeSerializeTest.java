package org.eclipse.swt.widgets;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import static org.eclipse.swt.widgets.Mocks.*;
import org.junit.jupiter.api.*;
import static dev.equo.swt.harness.WireAssert.assertThatWire;
import dev.equo.swt.harness.WireAssert;

class DateTimeSerializeTest extends SerializeTestBase {

    @Test
    void should_serialize_empty_DateTime() {
        DateTime w = new DateTime(shell(), SWT.NONE);
        WireAssert assertJ = assertThatWire(serialize(w));
        assertJ.containsEntry("id", w.hashCode())
               .containsEntry("swt", "DateTime");
    }

    @Test
    void should_serialize_filled_DateTime() {
        DateTime w = new DateTime(shell(), SWT.NONE);
        setAll(w);
        WireAssert assertJ = assertThatWire(serialize(w));
        assertJ.containsEntry("id", w.hashCode())
               .containsEntry("swt", "DateTime")
               .containsEntry("hours", w.getHours())
               .containsEntry("minutes", w.getMinutes())
               .containsEntry("month", w.getMonth())
               .containsEntry("seconds", w.getSeconds())
               .containsEntry("enabled", w.getEnabled())
               .containsEntry("toolTipText", w.getToolTipText())
               .containsEntry("visible", w.getVisible())
               .containsEntry("style", w.getStyle());
        assertJ.satisfies(node("day").equalsTo(w.getDay(), orAbsentIf0));
        assertJ.satisfies(node("year").equalsTo(w.getYear(), orAbsentIf0));
        assertJ.satisfies(node("backgroundMode").equalsTo(w.getBackgroundMode(), orAbsentIf0));
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
    void should_write_every_property_DateTime() throws Exception {
        assertWritesEveryProperty(style -> {
            DateTime w = new DateTime(shell(), style);
            setAll(w);
            fillUnsettable(w);
            return w;
        }, unwritable("horizontalBar", "checkStyle drops H_SCROLL and V_SCROLL, so the widget never has scroll bars", "verticalBar", "checkStyle drops H_SCROLL and V_SCROLL, so the widget never has scroll bars"), SWT.NONE, SWT.DATE, SWT.TIME, SWT.CALENDAR, SWT.SHORT, SWT.MEDIUM, SWT.LONG, SWT.DROP_DOWN, SWT.CALENDAR_WEEKNUMBERS, SWT.NO_BACKGROUND, SWT.NO_FOCUS, SWT.NO_MERGE_PAINTS, SWT.NO_REDRAW_RESIZE, SWT.NO_RADIO_GROUP, SWT.EMBEDDED, SWT.DOUBLE_BUFFERED, SWT.H_SCROLL, SWT.V_SCROLL, SWT.BORDER, SWT.LEFT_TO_RIGHT, SWT.RIGHT_TO_LEFT, SWT.FLIP_TEXT_DIRECTION);
    }

    @Test
    void should_name_every_change_DateTime() {
        DateTime w = new DateTime(shell(), SWT.NONE);
        assertNamesEveryChange(w);
    }

    VDateTime value(DateTime w) {
        return ((DartDateTime) w.getImpl()).getValue();
    }
}
