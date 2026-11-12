package org.eclipse.swt.custom;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import static org.eclipse.swt.widgets.Mocks.*;
import org.junit.jupiter.api.*;
import static dev.equo.swt.harness.WireAssert.assertThatWire;
import dev.equo.swt.harness.WireAssert;
import org.eclipse.swt.widgets.*;

class ScrolledCompositeSerializeTest extends SerializeTestBase {

    @Test
    void should_serialize_empty_ScrolledComposite() {
        ScrolledComposite w = new ScrolledComposite(shell(), SWT.NONE);
        WireAssert assertJ = assertThatWire(serialize(w));
        assertJ.containsEntry("id", w.hashCode())
               .containsEntry("swt", "ScrolledComposite");
    }

    @Test
    void should_serialize_filled_ScrolledComposite() {
        ScrolledComposite w = new ScrolledComposite(shell(), SWT.NONE);
        setAll(w);
        WireAssert assertJ = assertThatWire(serialize(w));
        assertJ.containsEntry("id", w.hashCode())
               .containsEntry("swt", "ScrolledComposite")
               .containsEntry("enabled", w.getEnabled())
               .containsEntry("toolTipText", w.getToolTipText())
               .containsEntry("visible", w.getVisible())
               .containsEntry("style", w.getStyle());
        assertJ.satisfies(node("alwaysShowScrollBars").equalsTo(w.getAlwaysShowScrollBars(), orAbsentIfFalse));
        assertJ.satisfies(node("content").equalsTo(w.getContent(), orAbsentIfNull));
        assertJ.satisfies(node("expandHorizontal").equalsTo(w.getExpandHorizontal(), orAbsentIfFalse));
        assertJ.satisfies(node("expandVertical").equalsTo(w.getExpandVertical(), orAbsentIfFalse));
        assertJ.satisfies(node("minHeight").equalsTo(w.getMinHeight(), orAbsentIf0));
        assertJ.satisfies(node("minWidth").equalsTo(w.getMinWidth(), orAbsentIf0));
        assertJ.satisfies(node("origin").equalsTo(value(w).getOrigin(), orAbsentIfNull));
        assertJ.satisfies(node("backgroundMode").equalsTo(w.getBackgroundMode(), orAbsentIf0));
        assertJ.satisfies(node("background").equalsTo(w.getBackground(), orAbsentIfNull));
        assertJ.satisfies(node("backgroundImage").equalsTo(w.getBackgroundImage(), orAbsentIfNull));
        assertJ.satisfies(node("bounds").equalsTo(value(w).getBounds(), orAbsentIfNull));
        assertJ.satisfies(node("cursor").equalsTo(w.getCursor(), orAbsentIfNull));
        assertJ.satisfies(node("dragDetect").equalsTo(w.getDragDetect(), orAbsentIfFalse));
        assertJ.satisfies(node("foreground").equalsTo(w.getForeground(), orAbsentIfNull));
        assertJ.satisfies(node("menu").equalsTo(w.getMenu(), orAbsentIfNull));
    }

    @Test
    void should_write_every_property_ScrolledComposite() throws Exception {
        assertWritesEveryProperty(style -> {
            ScrolledComposite w = new ScrolledComposite(shell(), style);
            setAll(w);
            fillUnsettable(w);
            return w;
        }, unwritable("font", "the client never reads it off a ScrolledComposite", "hasOwnBackground", "the client never reads it off a ScrolledComposite"), SWT.NONE, SWT.H_SCROLL, SWT.V_SCROLL, SWT.NO_BACKGROUND, SWT.NO_FOCUS, SWT.NO_MERGE_PAINTS, SWT.NO_REDRAW_RESIZE, SWT.NO_RADIO_GROUP, SWT.EMBEDDED, SWT.DOUBLE_BUFFERED, SWT.BORDER, SWT.LEFT_TO_RIGHT, SWT.RIGHT_TO_LEFT, SWT.FLIP_TEXT_DIRECTION);
    }

    @Test
    void should_name_every_change_ScrolledComposite() {
        ScrolledComposite w = new ScrolledComposite(shell(), SWT.NONE);
        assertNamesEveryChange(w);
    }

    VScrolledComposite value(ScrolledComposite w) {
        return ((DartScrolledComposite) w.getImpl()).getValue();
    }
}
