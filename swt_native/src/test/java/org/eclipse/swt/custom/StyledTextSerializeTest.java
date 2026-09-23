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
        StyledText w = new StyledText(swtShell(), SWT.NONE);
        String json = serialize(w);
        JsonMapAssert assertJ = assertThatJson(json).isObject();
        assertJ.containsEntry("id", w.hashCode())
               .containsEntry("swt", "StyledText");
    }

    @Test
    @Disabled
    void should_serialize_filled_StyledText() {
        StyledText w = new StyledText(swtShell(), SWT.NONE);
        setAll(w);
        String json = serialize(w);
        JsonMapAssert assertJ = assertThatJson(json).isObject();
        assertJ.containsEntry("id", w.hashCode())
               .containsEntry("swt", "StyledText")
               .containsEntry("text", json(w.getText()))
               .containsEntry("toolTipText", json(w.getToolTipText()))
               .containsEntry("visible", w.getVisible())
               .containsEntry("style", w.getStyle());
        assertJ.satisfies(node("alwaysShowScrollBars").equalsTo(w.getAlwaysShowScrollBars(), orAbsentIfFalse));
        assertJ.satisfies(node("bottomMargin").equalsTo(w.getBottomMargin(), orAbsentIf0));
        assertJ.satisfies(node("caretOffset").equalsTo(w.getCaretOffset(), orAbsentIf0));
        assertJ.satisfies(node("doubleClickEnabled").equalsTo(w.getDoubleClickEnabled(), orAbsentIfFalse));
        assertJ.satisfies(node("editable").equalsTo(w.getEditable(), orAbsentIfFalse));
        assertJ.satisfies(node("horizontalPixel").equalsTo(w.getHorizontalPixel(), orAbsentIf0));
        assertJ.satisfies(node("leftMargin").equalsTo(w.getLeftMargin(), orAbsentIf0));
        assertJ.satisfies(node("marginColor").equalsTo(w.getMarginColor(), orAbsentIfNull));
        assertJ.satisfies(node("rightMargin").equalsTo(w.getRightMargin(), orAbsentIf0));
        assertJ.satisfies(node("selectionBackground").equalsTo(w.getSelectionBackground(), orAbsentIfNull));
        assertJ.satisfies(node("selectionForeground").equalsTo(w.getSelectionForeground(), orAbsentIfNull));
        assertJ.satisfies(node("selectionRange").equalsTo(value(w).getSelectionRange(), orAbsentIfNull));
        assertJ.satisfies(node("tabs").equalsTo(w.getTabs(), orAbsentIf0));
        assertJ.satisfies(node("topMargin").equalsTo(w.getTopMargin(), orAbsentIf0));
        assertJ.satisfies(node("topPixel").equalsTo(w.getTopPixel(), orAbsentIf0));
        assertJ.satisfies(node("wordWrap").equalsTo(w.getWordWrap(), orAbsentIfFalse));
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
    @Disabled
    void should_name_every_change_StyledText() {
        StyledText w = new StyledText(swtShell(), SWT.NONE);
        assertNamesEveryChange(w);
    }

    VStyledText value(StyledText w) {
        return ((DartStyledText) w.getImpl()).getValue();
    }
}
