package org.eclipse.swt.widgets;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import static org.eclipse.swt.widgets.Mocks.*;
import org.junit.jupiter.api.*;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.*;
import net.javacrumbs.jsonunit.assertj.JsonMapAssert;

class SpinnerSerializeTest extends SerializeTestBase {

    @Test
    void should_serialize_empty_Spinner() {
        Spinner w = new Spinner(swtShell(), SWT.NONE);
        String json = serialize(w);
        JsonMapAssert assertJ = assertThatJson(json).isObject();
        assertJ.containsEntry("id", w.hashCode())
               .containsEntry("swt", "Spinner");
    }

    @Test
    void should_serialize_filled_Spinner() {
        Spinner w = new Spinner(swtShell(), SWT.NONE);
        setAll(w);
        String json = serialize(w);
        JsonMapAssert assertJ = assertThatJson(json).isObject();
        assertJ.containsEntry("id", w.hashCode())
               .containsEntry("swt", "Spinner")
               .containsEntry("toolTipText", json(w.getToolTipText()))
               .containsEntry("visible", w.getVisible())
               .containsEntry("style", w.getStyle());
        assertJ.satisfies(node("digits").equalsTo(w.getDigits(), orAbsentIf0));
        assertJ.satisfies(node("increment").equalsTo(w.getIncrement(), orAbsentIf0));
        assertJ.satisfies(node("maximum").equalsTo(w.getMaximum(), orAbsentIf0));
        assertJ.satisfies(node("minimum").equalsTo(w.getMinimum(), orAbsentIf0));
        assertJ.satisfies(node("pageIncrement").equalsTo(w.getPageIncrement(), orAbsentIf0));
        assertJ.satisfies(node("selection").equalsTo(w.getSelection(), orAbsentIf0));
        assertJ.satisfies(node("textLimit").equalsTo(w.getTextLimit(), orAbsentIf0));
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
    void should_name_every_change_Spinner() {
        Spinner w = new Spinner(swtShell(), SWT.NONE);
        assertNamesEveryChange(w);
    }

    VSpinner value(Spinner w) {
        return ((DartSpinner) w.getImpl()).getValue();
    }
}
