package org.eclipse.swt.custom;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import static org.eclipse.swt.widgets.Mocks.*;
import org.junit.jupiter.api.*;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.*;
import net.javacrumbs.jsonunit.assertj.JsonMapAssert;

class CTabFolderSerializeTest extends SerializeTestBase {

    @Test
    void should_serialize_empty_CTabFolder() {
        CTabFolder w = new CTabFolder(composite(), SWT.NONE);
        String json = serialize(w);
        JsonMapAssert assertJ = assertThatJson(json).isObject();
        assertJ.containsEntry("id", w.hashCode())
               .containsEntry("swt", "CTabFolder");
    }

    @Test
    void should_serialize_filled_CTabFolder() {
        CTabFolder w = new CTabFolder(composite(), SWT.NONE);
        setAll(w);
        String json = serialize(w);
        JsonMapAssert assertJ = assertThatJson(json).isObject();
        assertJ.containsEntry("id", w.hashCode())
               .containsEntry("swt", "CTabFolder")
               .containsEntry("toolTipText", json(w.getToolTipText()))
               .containsEntry("style", w.getStyle());
        assertJ.satisfies(node("borderVisible").equalsTo(w.getBorderVisible(), orAbsentIfFalse));
        assertJ.satisfies(node("fixedTabHeight").equalsTo(value(w).getFixedTabHeight(), orAbsentIf0));
        assertJ.satisfies(node("highlightEnabled").equalsTo(w.getHighlightEnabled(), orAbsentIfFalse));
        assertJ.satisfies(node("maximized").equalsTo(w.getMaximized(), orAbsentIfFalse));
        assertJ.satisfies(node("minChars").equalsTo(w.getMinimumCharacters(), orAbsentIf0));
        assertJ.satisfies(node("minimized").equalsTo(w.getMinimized(), orAbsentIfFalse));
        assertJ.satisfies(node("mru").equalsTo(w.getMRUVisible(), orAbsentIfFalse));
        assertJ.satisfies(node("onBottom").equalsTo(value(w).getOnBottom(), orAbsentIfFalse));
        assertJ.satisfies(node("selectedIndex").equalsTo(w.getSelectionIndex(), orAbsentIf0));
        assertJ.satisfies(node("selectionBackground").equalsTo(w.getSelectionBackground(), orAbsentIfNull));
        assertJ.satisfies(node("selectionForeground").equalsTo(w.getSelectionForeground(), orAbsentIfNull));
        assertJ.satisfies(node("selectionHighlightBarThickness").equalsTo(value(w).getSelectionHighlightBarThickness(), orAbsentIf0));
        assertJ.satisfies(node("showMax").equalsTo(w.getMaximizeVisible(), orAbsentIfFalse));
        assertJ.satisfies(node("showMin").equalsTo(w.getMinimizeVisible(), orAbsentIfFalse));
        assertJ.satisfies(node("showSelectedImage").equalsTo(w.getSelectedImageVisible(), orAbsentIfFalse));
        assertJ.satisfies(node("showUnselectedClose").equalsTo(w.getUnselectedCloseVisible(), orAbsentIfFalse));
        assertJ.satisfies(node("showUnselectedImage").equalsTo(w.getUnselectedImageVisible(), orAbsentIfFalse));
        assertJ.satisfies(node("simple").equalsTo(w.getSimple(), orAbsentIfFalse));
        assertJ.satisfies(node("single").equalsTo(w.getSingle(), orAbsentIfFalse));
        assertJ.satisfies(node("topRight").equalsTo(w.getTopRight(), orAbsentIfNull));
        assertJ.satisfies(node("backgroundMode").equalsTo(w.getBackgroundMode(), orAbsentIf0));
        assertJ.satisfies(node("layoutDeferred").equalsTo(w.getLayoutDeferred(), orAbsentIfFalse));
        assertJ.satisfies(node("scrollbarsMode").equalsTo(w.getScrollbarsMode(), orAbsentIf0));
        assertJ.satisfies(node("background").equalsTo(w.getBackground(), orAbsentIfNull));
        assertJ.satisfies(node("capture").equalsTo(value(w).getCapture(), orAbsentIfFalse));
        assertJ.satisfies(node("dragDetect").equalsTo(w.getDragDetect(), orAbsentIfFalse));
        assertJ.satisfies(node("enabled").equalsTo(w.getEnabled(), orAbsentIfFalse));
        assertJ.satisfies(node("foreground").equalsTo(w.getForeground(), orAbsentIfNull));
        assertJ.satisfies(node("orientation").equalsTo(w.getOrientation(), orAbsentIf0));
        assertJ.satisfies(node("redraw").equalsTo(value(w).getRedraw(), orAbsentIfFalse));
        assertJ.satisfies(node("textDirection").equalsTo(w.getTextDirection(), orAbsentIf0));
        assertJ.satisfies(node("touchEnabled").equalsTo(w.getTouchEnabled(), orAbsentIfFalse));
        assertJ.satisfies(node("visible").equalsTo(w.getVisible(), orAbsentIfFalse));
    }

    VCTabFolder value(CTabFolder w) {
        return ((DartCTabFolder) w.getImpl()).getValue();
    }
}
