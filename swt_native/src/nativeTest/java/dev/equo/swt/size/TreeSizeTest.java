package dev.equo.swt.size;

import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.DartTree;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.swt.widgets.DartMocks.dartShell;

@Tag("flutter-it")
@Tag("metal")
class TreeSizeTest extends SizeTestBase {

    @RegisterExtension
    static final WidgetSizeBridge flutter = new WidgetSizeBridge();

    @ParameterizedTest
    @MethodSource("cases")
    void header_height_matches_flutter(boolean headerVisible, int columnCount) {
        Tree w = new Tree(dartShell(), SWT.NONE);
        DartTree impl = (DartTree) w.getImpl();
        impl.setHeaderVisible(headerVisible);
        for (int i = 0; i < columnCount; i++) new TreeColumn(w, SWT.NONE);
        int javaHeight = TreeSizes.getHeaderHeight(impl);
        CompletableFuture<Measure> result = flutter.measure(impl);
        Measure measure = assertCompletes(flutter, result);
        PointD header = measure.namedSizes != null ? measure.namedSizes.get("header") : null;
        if (javaHeight == 0) {
            assertThat(header == null || header.y() == 0).as("header not rendered when hidden").isTrue();
        } else {
            assertThat(header).as("namedSizes.header").isNotNull();
            assertThat(header.y()).as("header height").isEqualTo((double) javaHeight);
        }
    }

    static Stream<Arguments> cases() {
        return Stream.of(Arguments.of(false, 0), Arguments.of(true, 0), Arguments.of(true, 1), Arguments.of(true, 3));
    }

    @ParameterizedTest
    @MethodSource("fullSizeCases")
    void full_height_matches_flutter(boolean headerVisible, int columnCount, int itemCount) {
        Tree w = new Tree(dartShell(), SWT.NONE);
        DartTree impl = (DartTree) w.getImpl();
        impl.setHeaderVisible(headerVisible);
        for (int i = 0; i < columnCount; i++) new TreeColumn(w, SWT.NONE);
        for (int i = 0; i < itemCount; i++) new TreeItem(w, SWT.NONE);
        int javaHeight = TreeSizes.getPreferredHeight(impl);
        CompletableFuture<Measure> result = flutter.measureUnbounded(impl);
        Measure measure = assertCompletes(flutter, result);
        assertThat(measure.widget.y).as("widget height").isEqualTo(javaHeight);
    }

    static Stream<Arguments> fullSizeCases() {
        return Stream.of(Arguments.of(false, 0, 0), Arguments.of(true, 0, 0), Arguments.of(true, 1, 3), Arguments.of(true, 3, 3));
    }
}
