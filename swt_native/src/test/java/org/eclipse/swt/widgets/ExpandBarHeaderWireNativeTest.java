package org.eclipse.swt.widgets;

import static dev.equo.swt.harness.WireAssert.assertThatWire;
import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.swt.widgets.Mocks.shell;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.junit.jupiter.api.Test;

/**
 * The client draws a header as tall as Java's band, so Java sends it with the item and says again when it changes: a
 * font moves every header, an image taller than the band moves its own.
 */
class ExpandBarHeaderWireNativeTest extends SerializeTestBase {

    private static java.util.Set<Integer> changed(ExpandItem item) {
        return ((DartWidget) item.getImpl()).getValue().changedKeys();
    }

    @Test
    void an_item_sends_the_height_of_its_header() {
        ExpandBar bar = new ExpandBar(shell(), SWT.NONE);
        ExpandItem item = new ExpandItem(bar, SWT.NONE);

        assertThatWire(serialize(item)).node("headerHeight").isEqualTo(item.getHeaderHeight());
        assertThat(item.getHeaderHeight()).isEqualTo(24);
    }

    @Test
    void a_font_taller_than_the_band_moves_every_header_and_says_so() {
        ExpandBar bar = new ExpandBar(shell(), SWT.NONE);
        ExpandItem first = new ExpandItem(bar, SWT.NONE);
        ExpandItem second = new ExpandItem(bar, SWT.NONE);
        serialize(first);
        serialize(second);
        assertThat(changed(first)).isEmpty();

        Font big = new Font(bar.getDisplay(), "Helvetica", 40, SWT.NORMAL);
        bar.setFont(big);
        GC gc = new GC(bar);
        int line = gc.getFontMetrics().getHeight();
        gc.dispose();

        assertThat(line).isGreaterThan(24);
        assertThat(changed(first)).contains(VExpandItem.HEADER_HEIGHT);
        assertThat(changed(second)).contains(VExpandItem.HEADER_HEIGHT);
        assertThatWire(serialize(first)).node("headerHeight").isEqualTo(line);
        big.dispose();
    }

    @Test
    void an_image_taller_than_the_band_makes_the_header_as_tall_and_says_so() {
        ExpandBar bar = new ExpandBar(shell(), SWT.NONE);
        ExpandItem item = new ExpandItem(bar, SWT.NONE);
        serialize(item);

        Image tall = new Image(Mocks.device(), new ImageData(30, 40, 24, new PaletteData(0xFF0000, 0xFF00, 0xFF)));
        item.setImage(tall);

        assertThat(changed(item)).contains(VExpandItem.HEADER_HEIGHT);
        assertThatWire(serialize(item)).node("headerHeight").isEqualTo(40);
    }
}
