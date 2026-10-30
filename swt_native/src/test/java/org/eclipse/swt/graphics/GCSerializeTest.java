package org.eclipse.swt.graphics;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import static org.eclipse.swt.widgets.Mocks.*;
import org.junit.jupiter.api.*;
import static dev.equo.swt.harness.WireAssert.assertThatWire;
import dev.equo.swt.harness.WireAssert;
import org.eclipse.swt.widgets.*;

class GCSerializeTest extends SerializeTestBase {

    @Test
    void should_serialize_empty_GC() {
        GC w = new GC(drawable());
        WireAssert assertJ = assertThatWire(serialize(w));
        assertJ.isNotEmpty();
    }

    @Test
    void should_serialize_filled_GC() {
        GC w = new GC(drawable());
        setAll(w);
        WireAssert assertJ = assertThatWire(serialize(w));
        assertJ.containsEntry("style", w.getStyle());
        assertJ.satisfies(node("XORMode").equalsTo(w.getXORMode(), orAbsentIfFalse));
        assertJ.satisfies(node("alpha").equalsTo(w.getAlpha(), 255));
        assertJ.satisfies(node("antialias").equalsTo(w.getAntialias(), orAbsentIf0));
        assertJ.satisfies(node("background").equalsTo(w.getBackground(), orAbsentIfNull));
        assertJ.satisfies(node("clipping").equalsTo(value(w).getClipping(), orAbsentIfNull));
        assertJ.satisfies(node("fillRule").equalsTo(w.getFillRule(), 1));
        assertJ.satisfies(node("font").equalsTo(w.getFont(), orAbsentIfNull));
        assertJ.satisfies(node("foreground").equalsTo(w.getForeground(), orAbsentIfNull));
        assertJ.satisfies(node("interpolation").equalsTo(w.getInterpolation(), orAbsentIf0));
        assertJ.satisfies(node("lineCap").equalsTo(w.getLineCap(), 1));
        assertJ.satisfies(node("lineDash").equalsTo(w.getLineDash(), orAbsentIfNull));
        assertJ.satisfies(node("lineJoin").equalsTo(w.getLineJoin(), 1));
        assertJ.satisfies(node("lineStyle").equalsTo(w.getLineStyle(), 1));
        assertJ.satisfies(node("lineWidth").equalsTo(w.getLineWidth(), orAbsentIf0));
    }

    @Test
    void should_write_every_property_GC() throws Exception {
        assertWritesEveryProperty(unwritable(), () -> {
            GC w = new GC(drawable());
            setAll(w);
            fillUnsettable(w);
            return w;
        }, () -> {
            GC w = new GC(drawable(), SWT.LEFT_TO_RIGHT);
            setAll(w);
            fillUnsettable(w);
            return w;
        }, () -> {
            GC w = new GC(drawable(), SWT.RIGHT_TO_LEFT);
            setAll(w);
            fillUnsettable(w);
            return w;
        });
    }

    VGC value(GC w) {
        return ((DartGC) w.getImpl()).getValue();
    }
}
