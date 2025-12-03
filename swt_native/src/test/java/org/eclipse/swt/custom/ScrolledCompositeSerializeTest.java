package org.eclipse.swt.custom;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import static org.eclipse.swt.widgets.Mocks.*;
import org.junit.jupiter.api.*;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.*;
import net.javacrumbs.jsonunit.assertj.JsonMapAssert;

class ScrolledCompositeSerializeTest extends SerializeTestBase {

    @Test
    void should_serialize_empty_ScrolledComposite() {
        ScrolledComposite w = new ScrolledComposite(composite(), SWT.NONE);
        String json = serialize(w);
        JsonMapAssert assertJ = assertThatJson(json).isObject();
        assertJ.containsEntry("id", w.hashCode())
               .containsEntry("swt", "ScrolledComposite");
    }

    @Test
    void should_serialize_filled_ScrolledComposite() {
        ScrolledComposite w = new ScrolledComposite(composite(), SWT.NONE);
        setAll(w);
        String json = serialize(w);
        JsonMapAssert assertJ = assertThatJson(json).isObject();
        assertJ.containsEntry("id", w.hashCode())
               .containsEntry("swt", "ScrolledComposite")
               .containsEntry("toolTipText", json(w.getToolTipText()))
               .containsEntry("style", w.getStyle());
        assertJ.satisfies(node("alwaysShowScrollBars").equalsTo(w.getAlwaysShowScrollBars(), orAbsentIfFalse));
        assertJ.satisfies(node("content").equalsTo(w.getContent(), orAbsentIfNull));
        assertJ.satisfies(node("expandHorizontal").equalsTo(w.getExpandHorizontal(), orAbsentIfFalse));
        assertJ.satisfies(node("expandVertical").equalsTo(w.getExpandVertical(), orAbsentIfFalse));
        assertJ.satisfies(node("minHeight").equalsTo(w.getMinHeight(), orAbsentIf0));
        assertJ.satisfies(node("minSize").equalsTo(value(w).getMinSize(), orAbsentIf0));
        assertJ.satisfies(node("minWidth").equalsTo(w.getMinWidth(), orAbsentIf0));
        assertJ.satisfies(node("origin").equalsTo(w.getOrigin(), orAbsentIfNull));
        assertJ.satisfies(node("showFocusedControl").equalsTo(w.getShowFocusedControl(), orAbsentIfFalse));
        assertJ.satisfies(node("backgroundMode").equalsTo(w.getBackgroundMode(), orAbsentIf0));
        assertJ.satisfies(node("layoutDeferred").equalsTo(w.getLayoutDeferred(), orAbsentIfFalse));
        assertJ.satisfies(node("scrollbarsMode").equalsTo(w.getScrollbarsMode(), orAbsentIf0));
        assertJ.satisfies(node("background").equalsTo(w.getBackground(), orAbsentIfNull));
        assertJ.satisfies(node("backgroundImage").equalsTo(w.getBackgroundImage(), orAbsentIfNull));
        assertJ.satisfies(node("bounds").equalsTo(w.getBounds(), orAbsentIfNull));
        assertJ.satisfies(node("capture").equalsTo(value(w).getCapture(), orAbsentIfFalse));
        assertJ.satisfies(node("dragDetect").equalsTo(w.getDragDetect(), orAbsentIfFalse));
        assertJ.satisfies(node("enabled").equalsTo(w.getEnabled(), orAbsentIfFalse));
        assertJ.satisfies(node("font").equalsTo(w.getFont(), orAbsentIfNull));
        assertJ.satisfies(node("foreground").equalsTo(w.getForeground(), orAbsentIfNull));
        assertJ.satisfies(node("menu").equalsTo(w.getMenu(), orAbsentIfNull));
        assertJ.satisfies(node("orientation").equalsTo(w.getOrientation(), orAbsentIf0));
        assertJ.satisfies(node("redraw").equalsTo(value(w).getRedraw(), orAbsentIfFalse));
        assertJ.satisfies(node("textDirection").equalsTo(w.getTextDirection(), orAbsentIf0));
        assertJ.satisfies(node("touchEnabled").equalsTo(w.getTouchEnabled(), orAbsentIfFalse));
        assertJ.satisfies(node("visible").equalsTo(w.getVisible(), orAbsentIfFalse));
    }

    VScrolledComposite value(ScrolledComposite w) {
        return ((DartScrolledComposite) w.getImpl()).getValue();
    }
}
