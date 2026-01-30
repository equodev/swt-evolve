package org.eclipse.swt.custom;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import static org.eclipse.swt.widgets.Mocks.*;
import org.junit.jupiter.api.*;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.*;
import net.javacrumbs.jsonunit.assertj.JsonMapAssert;

class StyledTextSerializeTest extends SerializeTestBase {

    @Test
    void should_serialize_empty_StyledText() {
        StyledText w = new StyledText(composite(), SWT.NONE);
        String json = serialize(w);
        JsonMapAssert assertJ = assertThatJson(json).isObject();
        assertJ.containsEntry("id", w.hashCode())
               .containsEntry("swt", "StyledText");
    }

    @Test
    @Disabled
    void should_serialize_filled_StyledText() {
        StyledText w = new StyledText(composite(), SWT.NONE);
        setAll(w);
        String json = serialize(w);
        JsonMapAssert assertJ = assertThatJson(json).isObject();
        assertJ.containsEntry("id", w.hashCode())
               .containsEntry("swt", "StyledText")
               .hasEntrySatisfying("styleRanges", p -> assertThatJson(p).isArray()
                                                                        .hasSameSizeAs(w.getStyleRanges()))
               .containsEntry("text", json(w.getText()))
               .containsEntry("toolTipText", json(w.getToolTipText()))
               .containsEntry("style", w.getStyle());
        assertJ.satisfies(node("alignment").equalsTo(w.getAlignment(), orAbsentIf0));
        assertJ.satisfies(node("alwaysShowScrollBars").equalsTo(w.getAlwaysShowScrollBars(), orAbsentIfFalse));
        assertJ.satisfies(node("bidiColoring").equalsTo(w.getBidiColoring(), orAbsentIfFalse));
        assertJ.satisfies(node("blockSelection").equalsTo(w.getBlockSelection(), orAbsentIfFalse));
        assertJ.satisfies(node("blockSelectionBounds").equalsTo(w.getBlockSelectionBounds(), orAbsentIfNull));
        assertJ.satisfies(node("bottomMargin").equalsTo(w.getBottomMargin(), orAbsentIf0));
        assertJ.satisfies(node("caretOffset").equalsTo(w.getCaretOffset(), orAbsentIf0));
        assertJ.satisfies(node("doubleClickEnabled").equalsTo(w.getDoubleClickEnabled(), orAbsentIfFalse));
        assertJ.satisfies(node("editable").equalsTo(w.getEditable(), orAbsentIfFalse));
        assertJ.satisfies(node("fixedLineMetrics").equalsTo(value(w).getFixedLineMetrics(), orAbsentIfNull));
        assertJ.satisfies(node("horizontalIndex").equalsTo(w.getHorizontalIndex(), orAbsentIf0));
        assertJ.satisfies(node("horizontalPixel").equalsTo(w.getHorizontalPixel(), orAbsentIf0));
        assertJ.satisfies(node("indent").equalsTo(w.getIndent(), orAbsentIf0));
        assertJ.satisfies(node("justify").equalsTo(w.getJustify(), orAbsentIfFalse));
        assertJ.satisfies(node("leftMargin").equalsTo(w.getLeftMargin(), orAbsentIf0));
        assertJ.satisfies(node("lineSpacing").equalsTo(w.getLineSpacing(), orAbsentIf0));
        assertJ.satisfies(node("marginColor").equalsTo(w.getMarginColor(), orAbsentIfNull));
        assertJ.satisfies(node("mouseNavigatorEnabled").equalsTo(w.getMouseNavigatorEnabled(), orAbsentIfFalse));
        assertJ.satisfies(node("rightMargin").equalsTo(w.getRightMargin(), orAbsentIf0));
        assertJ.satisfies(node("selection").equalsTo(w.getSelection(), orAbsentIfNull));
        assertJ.satisfies(node("selectionBackground").equalsTo(w.getSelectionBackground(), orAbsentIfNull));
        assertJ.satisfies(node("selectionForeground").equalsTo(w.getSelectionForeground(), orAbsentIfNull));
        assertJ.satisfies(node("selectionRange").equalsTo(w.getSelectionRange(), orAbsentIfNull));
        assertJ.satisfies(node("selectionRanges").equalsTo(w.getSelectionRanges(), orAbsentIfNull));
        assertJ.satisfies(node("styleRange").equalsTo(value(w).getStyleRange(), orAbsentIf0));
        assertJ.satisfies(node("tabStops").equalsTo(w.getTabStops(), orAbsentIfNull));
        assertJ.satisfies(node("tabs").equalsTo(w.getTabs(), orAbsentIf0));
        assertJ.satisfies(node("textLimit").equalsTo(w.getTextLimit(), orAbsentIf0));
        assertJ.satisfies(node("topIndex").equalsTo(w.getTopIndex(), orAbsentIf0));
        assertJ.satisfies(node("topMargin").equalsTo(w.getTopMargin(), orAbsentIf0));
        assertJ.satisfies(node("topPixel").equalsTo(w.getTopPixel(), orAbsentIf0));
        assertJ.satisfies(node("wordWrap").equalsTo(w.getWordWrap(), orAbsentIfFalse));
        assertJ.satisfies(node("wrapIndent").equalsTo(w.getWrapIndent(), orAbsentIf0));
        assertJ.satisfies(node("caret").equalsTo(w.getCaret(), orAbsentIfNull));
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

    VStyledText value(StyledText w) {
        return ((DartStyledText) w.getImpl()).getValue();
    }
}
