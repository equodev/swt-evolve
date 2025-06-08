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
        JsonMapAssert assertJson = assertThatJson(json).isObject();
        assertJson.containsEntry("id", w.hashCode())
                  .containsEntry("swt", "CTabFolder")
                  .containsEntry("style", w.getStyle());
    }

    @Test
    void should_serialize_filled_CTabFolder() {
        CTabFolder w = new CTabFolder(composite(), SWT.NONE);
        setAll(w);
        String json = serialize(w);
        JsonMapAssert assertJson = assertThatJson(json).isObject();
        assertJson.containsEntry("id", w.hashCode())
                  .containsEntry("swt", "CTabFolder")
                  .containsEntry("style", w.getStyle());
        assertJson.node("borderVisible")
                  .satisfies(isEqualTo(w.getBorderVisible(), orAbsentIfFalse()));
        assertJson.node("minimized")
                  .satisfies(isEqualTo(w.getMinimized(), orAbsentIfFalse()));
        assertJson.node("showMin")
                  .satisfies(isEqualTo(w.getMinimizeVisible(), orAbsentIfFalse()));
        assertJson.node("minChars")
                  .satisfies(isEqualTo(w.getMinimumCharacters(), orAbsentIf0()));
        assertJson.node("maximized")
                  .satisfies(isEqualTo(w.getMaximized(), orAbsentIfFalse()));
        assertJson.node("showMax")
                  .satisfies(isEqualTo(w.getMaximizeVisible(), orAbsentIfFalse()));
        assertJson.node("mru")
                  .satisfies(isEqualTo(w.getMRUVisible(), orAbsentIfFalse()));
        assertJson.node("selectedIndex")
                  .satisfies(isEqualTo(w.getSelectionIndex(), orAbsentIf0()));
        assertJson.node("selectionBackground")
                  .satisfies(isEqualTo(w.getSelectionBackground(), ifNotNull()));
        assertJson.node("selectionForeground")
                  .satisfies(isEqualTo(w.getSelectionForeground(), ifNotNull()));
        assertJson.node("simple")
                  .satisfies(isEqualTo(w.getSimple(), orAbsentIfFalse()));
        assertJson.node("single")
                  .satisfies(isEqualTo(w.getSingle(), orAbsentIfFalse()));
        assertJson.node("showUnselectedClose")
                  .satisfies(isEqualTo(w.getUnselectedCloseVisible(), orAbsentIfFalse()));
        assertJson.node("showUnselectedImage")
                  .satisfies(isEqualTo(w.getUnselectedImageVisible(), orAbsentIfFalse()));
        assertJson.node("showSelectedImage")
                  .satisfies(isEqualTo(w.getSelectedImageVisible(), orAbsentIfFalse()));
        assertJson.node("background")
                  .satisfies(isEqualTo(value(w).getBackground(), ifNotNull()));
        assertJson.node("foreground")
                  .satisfies(isEqualTo(value(w).getForeground(), ifNotNull()));
        assertJson.node("selectionHighlightBarThickness")
                  .satisfies(isEqualTo(value(w).getSelectionHighlightBarThickness(), orAbsentIf0()));
        assertJson.node("fixedTabHeight")
                  .satisfies(isEqualTo(value(w).getFixedTabHeight(), orAbsentIf0()));
        assertJson.node("onBottom")
                  .satisfies(isEqualTo(value(w).getOnBottom(), orAbsentIfFalse()));
        assertJson.node("highlightEnabled")
                  .satisfies(isEqualTo(w.getHighlightEnabled(), orAbsentIfFalse()));
        assertJson.node("backgroundMode")
                  .satisfies(isEqualTo(w.getBackgroundMode(), orAbsentIf0()));
        assertJson.node("layoutDeferred")
                  .satisfies(isEqualTo(w.getLayoutDeferred(), orAbsentIfFalse()));
        assertJson.node("scrollbarsMode")
                  .satisfies(isEqualTo(w.getScrollbarsMode(), orAbsentIf0()));
        assertJson.node("background")
                  .satisfies(isEqualTo(value(w).getBackground(), ifNotNull()));
        assertJson.node("dragDetect")
                  .satisfies(isEqualTo(w.getDragDetect(), orAbsentIfFalse()));
        assertJson.node("enabled")
                  .satisfies(isEqualTo(w.getEnabled(), orAbsentIfFalse()));
        assertJson.node("foreground")
                  .satisfies(isEqualTo(value(w).getForeground(), ifNotNull()));
        assertJson.node("orientation")
                  .satisfies(isEqualTo(w.getOrientation(), orAbsentIf0()));
        assertJson.node("textDirection")
                  .satisfies(isEqualTo(w.getTextDirection(), orAbsentIf0()));
        assertJson.node("toolTipText")
                  .satisfies(isEqualTo(w.getToolTipText(), ifNotNull()));
        assertJson.node("touchEnabled")
                  .satisfies(isEqualTo(w.getTouchEnabled(), orAbsentIfFalse()));
        assertJson.node("visible")
                  .satisfies(isEqualTo(w.getVisible(), orAbsentIfFalse()));
        assertJson.node("capture")
                  .satisfies(isEqualTo(value(w).getCapture(), orAbsentIfFalse()));
        assertJson.node("redraw")
                  .satisfies(isEqualTo(value(w).getRedraw(), orAbsentIfFalse()));
    }

    VCTabFolder value(CTabFolder w) {
        return ((DartCTabFolder) w.getImpl()).getValue();
    }
}
