package org.eclipse.swt.widgets;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import static org.eclipse.swt.widgets.Mocks.*;
import org.junit.jupiter.api.*;
import static dev.equo.swt.harness.WireAssert.assertThatWire;
import dev.equo.swt.harness.WireAssert;

class TabFolderSerializeTest extends SerializeTestBase {

    @Test
    void should_serialize_empty_TabFolder() {
        TabFolder w = new TabFolder(shell(), SWT.NONE);
        WireAssert assertJ = assertThatWire(serialize(w));
        assertJ.containsEntry("id", w.hashCode())
               .containsEntry("swt", "TabFolder");
    }

    @Test
    void should_serialize_filled_TabFolder() {
        TabFolder w = new TabFolder(shell(), SWT.NONE);
        setAll(w);
        WireAssert assertJ = assertThatWire(serialize(w));
        assertJ.containsEntry("id", w.hashCode())
               .containsEntry("swt", "TabFolder")
               .containsEntry("enabled", w.getEnabled())
               .containsEntry("toolTipText", w.getToolTipText())
               .containsEntry("visible", w.getVisible())
               .containsEntry("style", w.getStyle());
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
    void should_write_every_property_TabFolder() throws Exception {
        assertWritesEveryProperty(style -> {
            TabFolder w = new TabFolder(shell(), style);
            setAll(w);
            fillUnsettable(w);
            return w;
        }, unwritable("horizontalBar", "checkStyle drops H_SCROLL and V_SCROLL, so the widget never has scroll bars", "verticalBar", "checkStyle drops H_SCROLL and V_SCROLL, so the widget never has scroll bars", "hasOwnBackground", "the client never reads it off a TabFolder"), SWT.NONE, SWT.TOP, SWT.BOTTOM, SWT.NO_BACKGROUND, SWT.NO_FOCUS, SWT.NO_MERGE_PAINTS, SWT.NO_REDRAW_RESIZE, SWT.NO_RADIO_GROUP, SWT.EMBEDDED, SWT.DOUBLE_BUFFERED, SWT.H_SCROLL, SWT.V_SCROLL, SWT.BORDER, SWT.LEFT_TO_RIGHT, SWT.RIGHT_TO_LEFT, SWT.FLIP_TEXT_DIRECTION);
    }

    @Test
    void should_name_every_change_TabFolder() {
        TabFolder w = new TabFolder(shell(), SWT.NONE);
        assertNamesEveryChange(w);
    }

    VTabFolder value(TabFolder w) {
        return ((DartTabFolder) w.getImpl()).getValue();
    }
}
