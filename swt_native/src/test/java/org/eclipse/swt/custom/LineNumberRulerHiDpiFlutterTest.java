package org.eclipse.swt.custom;

import org.junit.jupiter.api.Tag;

/**
 * {@link LineNumberRulerFlutterTest} on a 2x display, where the column's double buffer is denser
 * than its size and the scroll it copies has to be taken in the buffer's pixels.
 */
@Tag("flutter-it")
class LineNumberRulerHiDpiFlutterTest extends LineNumberRulerFlutterTest {

    @Override
    double deviceScale() {
        return 2;
    }
}
