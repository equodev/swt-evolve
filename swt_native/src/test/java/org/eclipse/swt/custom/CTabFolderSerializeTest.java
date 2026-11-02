package org.eclipse.swt.custom;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import static org.eclipse.swt.widgets.Mocks.*;
import org.junit.jupiter.api.*;
import static dev.equo.swt.harness.WireAssert.assertThatWire;
import dev.equo.swt.harness.WireAssert;
import org.eclipse.swt.widgets.*;

class CTabFolderSerializeTest extends SerializeTestBase {

    @Test
    void should_serialize_empty_CTabFolder() {
        CTabFolder w = new CTabFolder(shell(), SWT.NONE);
        WireAssert assertJ = assertThatWire(serialize(w));
        assertJ.containsEntry("id", w.hashCode())
               .containsEntry("swt", "CTabFolder");
    }

    @Test
    void should_serialize_filled_CTabFolder() {
        CTabFolder w = new CTabFolder(shell(), SWT.NONE);
        setAll(w);
        WireAssert assertJ = assertThatWire(serialize(w));
        assertJ.containsEntry("id", w.hashCode())
               .containsEntry("swt", "CTabFolder")
               .containsEntry("toolTipText", w.getToolTipText())
               .containsEntry("visible", w.getVisible())
               .containsEntry("style", w.getStyle());
        assertJ.satisfies(node("borderVisible").equalsTo(w.getBorderVisible(), orAbsentIfFalse));
        assertJ.satisfies(node("highlightEnabled").equalsTo(w.getHighlightEnabled(), orAbsentIfFalse));
        assertJ.satisfies(node("maximizeVisible").equalsTo(w.getMaximizeVisible(), orAbsentIfFalse));
        assertJ.satisfies(node("maximized").equalsTo(w.getMaximized(), orAbsentIfFalse));
        assertJ.satisfies(node("minimizeVisible").equalsTo(w.getMinimizeVisible(), orAbsentIfFalse));
        assertJ.satisfies(node("minimized").equalsTo(w.getMinimized(), orAbsentIfFalse));
        assertJ.satisfies(node("minimumCharacters").equalsTo(w.getMinimumCharacters(), orAbsentIf0));
        assertJ.satisfies(node("selectedImageVisible").equalsTo(w.getSelectedImageVisible(), orAbsentIfFalse));
        assertJ.satisfies(node("selection").equalsTo(w.getSelectionIndex(), orAbsentIf0));
        assertJ.satisfies(node("selectionBackground").equalsTo(w.getSelectionBackground(), orAbsentIfNull));
        assertJ.satisfies(node("selectionBarThickness").equalsTo(value(w).getSelectionBarThickness(), orAbsentIf0));
        assertJ.satisfies(node("selectionBgImage").equalsTo(value(w).getSelectionBgImage(), orAbsentIfNull));
        assertJ.satisfies(node("selectionForeground").equalsTo(w.getSelectionForeground(), orAbsentIfNull));
        assertJ.satisfies(node("single").equalsTo(w.getSingle(), orAbsentIfFalse));
        assertJ.satisfies(node("tabHeight").equalsTo(value(w).getTabHeight(), null));
        assertJ.satisfies(node("tabPosition").equalsTo(w.getTabPosition(), orAbsentIf0));
        assertJ.satisfies(node("topRight").equalsTo(w.getTopRight(), orAbsentIfNull));
        assertJ.satisfies(node("unselectedCloseVisible").equalsTo(w.getUnselectedCloseVisible(), orAbsentIfFalse));
        assertJ.satisfies(node("unselectedImageVisible").equalsTo(w.getUnselectedImageVisible(), orAbsentIfFalse));
        assertJ.satisfies(node("backgroundMode").equalsTo(w.getBackgroundMode(), orAbsentIf0));
        assertJ.satisfies(node("background").equalsTo(w.getBackground(), orAbsentIfNull));
        assertJ.satisfies(node("backgroundImage").equalsTo(w.getBackgroundImage(), orAbsentIfNull));
        assertJ.satisfies(node("bounds").equalsTo(value(w).getBounds(), orAbsentIfNull));
        assertJ.satisfies(node("cursor").equalsTo(w.getCursor(), orAbsentIfNull));
        assertJ.satisfies(node("dragDetect").equalsTo(w.getDragDetect(), orAbsentIfFalse));
        assertJ.satisfies(node("enabled").equalsTo(w.getEnabled(), orAbsentIfFalse));
        assertJ.satisfies(node("font").equalsTo(w.getFont(), orAbsentIfNull));
        assertJ.satisfies(node("foreground").equalsTo(w.getForeground(), orAbsentIfNull));
        assertJ.satisfies(node("menu").equalsTo(w.getMenu(), orAbsentIfNull));
    }

    @Test
    void should_write_every_property_CTabFolder() throws Exception {
        assertWritesEveryProperty(style -> {
            CTabFolder w = new CTabFolder(shell(), style);
            setAll(w);
            fillUnsettable(w);
            return w;
        }, unwritable("horizontalBar", "checkStyle drops H_SCROLL and V_SCROLL, so the widget never has scroll bars", "verticalBar", "checkStyle drops H_SCROLL and V_SCROLL, so the widget never has scroll bars"), SWT.NONE, SWT.CLOSE, SWT.TOP, SWT.BOTTOM, SWT.FLAT, SWT.BORDER, SWT.SINGLE, SWT.MULTI, SWT.NO_BACKGROUND, SWT.NO_FOCUS, SWT.NO_MERGE_PAINTS, SWT.NO_REDRAW_RESIZE, SWT.NO_RADIO_GROUP, SWT.EMBEDDED, SWT.DOUBLE_BUFFERED, SWT.H_SCROLL, SWT.V_SCROLL, SWT.LEFT_TO_RIGHT, SWT.RIGHT_TO_LEFT, SWT.FLIP_TEXT_DIRECTION);
    }

    @Test
    void should_name_every_change_CTabFolder() {
        CTabFolder w = new CTabFolder(shell(), SWT.NONE);
        assertNamesEveryChange(w);
    }

    VCTabFolder value(CTabFolder w) {
        return ((DartCTabFolder) w.getImpl()).getValue();
    }
}
