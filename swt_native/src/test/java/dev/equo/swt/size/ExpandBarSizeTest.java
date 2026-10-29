package dev.equo.swt.size;

import dev.equo.swt.ConfigFlags;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.DartExpandBar;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.within;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.eclipse.swt.widgets.DartMocks.dartShell;

@Tag("flutter-it")
@Tag("metal")
class ExpandBarSizeTest extends SizeTestBase {

    @RegisterExtension
    static final WidgetSizeBridge flutter = new WidgetSizeBridge();

    @ParameterizedTest
    @MethodSource("headerCases")
    void java_size_should_equals_flutter(String image, String[] headers) {
        ExpandBar w = new ExpandBar(dartShell(), SWT.NONE);
        for (String header : headers) {
            ExpandItem item = new ExpandItem(w, SWT.NONE);
            item.setText(header);
            if (!NoImg.equals(image))
                item.setImage(createImage(image));
        }
        DartExpandBar impl = (DartExpandBar) w.getImpl();
        Point javaSize = ExpandBarSizes.computeSize(w, SWT.DEFAULT, SWT.DEFAULT);
        Measure measure = assertCompletes(flutter, flutter.measure(impl, ConfigFlags.use_swt_fonts(false)));
        assertSoftly(soft -> {
            soft.assertThat(measure.widget.x)
                .as("width")
                .isCloseTo(javaSize.x, within(1));
            soft.assertThat(measure.widget.y)
                .as("height")
                .isEqualTo(javaSize.y);
            // The label's line height enters no size of the bar.
            soft.assertThat(measure.textStyle)
                .as("header label style")
                .usingRecursiveComparison()
                .ignoringFields("height")
                .isEqualTo(ExpandBarSizes.headerLabelStyle());
        });
    }

    static Stream<Arguments> headerCases() {
        return Stream.of(Arguments.of(NoImg, Named.of("empty", new String[] { "" })), Arguments.of(NoImg, Named.of("one", new String[] { "Sample" })), Arguments.of("16x16", Named.of("one", new String[] { "Sample" })), Arguments.of("48x48", Named.of("one", new String[] { "Sample" })), Arguments.of(NoImg, Named.of("items", Items)), Arguments.of("16x16", Named.of("items", Items)));
    }
}
