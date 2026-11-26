package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.function.IntFunction;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A Link or a Button created with BORDER has room for the outline drawn inside its bounds: its size is the
 * one without the border plus the border on each side, and its hints are its content's, as on Win32.
 */
@Tag("native-unit")
class BorderOutlineSizeTest {

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    private static IntFunction<Control> factory(String kind) {
        return switch (kind) {
            case "Link" -> style -> {
                Link link = new Link(DartMocks.dartShell(), style);
                link.setText("Visit the <a href=\"x\">Eclipse.org</a> project and the <a>SWT</a> homepage.");
                return link;
            };
            case "Button" -> style -> {
                Button button = new Button(DartMocks.dartShell(), SWT.PUSH | style);
                button.setText("Three");
                return button;
            };
            default -> throw new IllegalArgumentException(kind);
        };
    }

    private static final int BORDER = 2 * ControlHelper.BORDER_WIDTH;

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"Link", "Button"})
    @DisplayName("with BORDER, the preferred size is the border wider and taller on each side")
    void borderIsAddedAroundThePreferredSize(String kind) {
        Point plain = factory(kind).apply(SWT.NONE).computeSize(SWT.DEFAULT, SWT.DEFAULT);
        Point bordered = factory(kind).apply(SWT.BORDER).computeSize(SWT.DEFAULT, SWT.DEFAULT);

        assertThat(bordered).isEqualTo(new Point(plain.x + BORDER, plain.y + BORDER));
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"Link", "Button"})
    @DisplayName("with BORDER, the hints are the content's and the border goes around them")
    void hintsAreTheContent(String kind) {
        Point plain = factory(kind).apply(SWT.NONE).computeSize(120, 40);
        Point bordered = factory(kind).apply(SWT.BORDER).computeSize(120, 40);

        assertThat(bordered).isEqualTo(new Point(plain.x + BORDER, plain.y + BORDER));
    }
}
