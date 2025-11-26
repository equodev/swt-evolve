package org.eclipse.swt.graphics;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import static org.eclipse.swt.widgets.Mocks.*;
import org.junit.jupiter.api.*;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.*;
import net.javacrumbs.jsonunit.assertj.JsonMapAssert;

class GCSerializeTest extends SerializeTestBase {

    @Test
    void should_serialize_empty_GC() {
        GC w = new GC(drawable());
        String json = serialize(w);
        JsonMapAssert assertJ = assertThatJson(json).isObject();
        assertJ.isNotEmpty();
    }

    @Test
    void should_serialize_filled_GC() {
        GC w = new GC(drawable());
        setAll(w);
        String json = serialize(w);
        JsonMapAssert assertJ = assertThatJson(json).isObject();
        assertJ.containsEntry("style", w.getStyle());
        assertJ.satisfies(node("XORMode").equalsTo(w.getXORMode(), orAbsentIfFalse));
        assertJ.satisfies(node("advanced").equalsTo(w.getAdvanced(), orAbsentIfFalse));
        assertJ.satisfies(node("alpha").equalsTo(w.getAlpha(), orAbsentIf0));
        assertJ.satisfies(node("antialias").equalsTo(w.getAntialias(), orAbsentIf0));
        assertJ.satisfies(node("background").equalsTo(w.getBackground(), orAbsentIfNull));
        assertJ.satisfies(node("clipping").equalsTo(w.getClipping(), orAbsentIfNull));
        assertJ.satisfies(node("fillRule").equalsTo(w.getFillRule(), orAbsentIf0));
        assertJ.satisfies(node("font").equalsTo(w.getFont(), orAbsentIfNull));
        assertJ.satisfies(node("foreground").equalsTo(w.getForeground(), orAbsentIfNull));
        assertJ.satisfies(node("interpolation").equalsTo(w.getInterpolation(), orAbsentIf0));
        assertJ.satisfies(node("lineCap").equalsTo(w.getLineCap(), orAbsentIf0));
        assertJ.satisfies(node("lineDash").equalsTo(w.getLineDash(), orAbsentIfNull));
        assertJ.satisfies(node("lineJoin").equalsTo(w.getLineJoin(), orAbsentIf0));
        assertJ.satisfies(node("lineStyle").equalsTo(w.getLineStyle(), orAbsentIf0));
        assertJ.satisfies(node("lineWidth").equalsTo(w.getLineWidth(), orAbsentIf0));
        assertJ.satisfies(node("textAntialias").equalsTo(w.getTextAntialias(), orAbsentIf0));
    }

    VGC value(GC w) {
        return ((DartGC) w.getImpl()).getValue();
    }
}
