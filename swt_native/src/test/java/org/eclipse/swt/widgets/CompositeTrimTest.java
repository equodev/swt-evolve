package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A plain Composite or Canvas frames its content the way upstream's scroll view does: a BORDER takes the outline
 * Flutter draws on every side, and each bar takes its thickness on its own side. The trim makes room for every bar
 * the style asks for; the client area gives up room only for the bars that are set visible, whether or not the
 * window is on screen.
 */
@ExtendWith(Mocks.class)
class CompositeTrimTest {

    private static final int BAR = Sizes.SCROLL_BAR_SIZE;
    private static final int LINE = ControlHelper.BORDER_WIDTH;
    private static final int HEADER = 24;

    @BeforeAll
    static void useEquo() {
        Config.forceEquo();
    }

    @AfterAll
    static void reset() {
        Config.defaultToEclipse();
    }

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    private static Composite sized(Composite composite) {
        composite.setSize(100, 50);
        return composite;
    }

    /** A layout that always asks for the same size. */
    private static Layout fixedLayout(int width, int height) {
        return new Layout() {
            @Override
            protected Point computeSize(Composite c, int wHint, int hHint, boolean flushCache) {
                return new Point(width, height);
            }

            @Override
            protected void layout(Composite c, boolean flushCache) {
            }
        };
    }

    /** A trim that reserves a header strip above the content, as a subclass that paints one does. */
    private static Rectangle withHeader(Rectangle trim) {
        trim.y -= HEADER;
        trim.height += HEADER;
        return trim;
    }

    @Test
    @DisplayName("a plain composite or canvas frames its content; a widget built on one keeps its own trim")
    void onlyPlainCompositesAndCanvasesFrameTheirContent() {
        Shell shell = Mocks.shell();
        assertThat(CompositeHelper.framesItsContent((DartScrollable) new Composite(shell, SWT.NONE).getImpl())).isTrue();
        assertThat(CompositeHelper.framesItsContent((DartScrollable) new Canvas(shell, SWT.NONE).getImpl())).isTrue();
        assertThat(CompositeHelper.framesItsContent((DartScrollable) new Composite(shell, SWT.NONE) {}.getImpl())).isTrue();
        assertThat(CompositeHelper.framesItsContent((DartScrollable) new StyledText(shell, SWT.BORDER).getImpl())).isFalse();
        assertThat(CompositeHelper.framesItsContent((DartScrollable) new ScrolledComposite(shell, SWT.BORDER).getImpl())).isFalse();
    }

    @Test
    @DisplayName("a border takes its line on every side, at the origin")
    void aBorderTakesItsLineOnEverySide() {
        Shell shell = Mocks.shell();
        Composite composite = sized(new Composite(shell, SWT.BORDER));
        assertThat(composite.getClientArea()).isEqualTo(new Rectangle(0, 0, 100 - 2 * LINE, 50 - 2 * LINE));
        assertThat(composite.computeTrim(5, 6, 10, 20)).isEqualTo(new Rectangle(5 - LINE, 6 - LINE, 10 + 2 * LINE, 20 + 2 * LINE));
        Canvas canvas = new Canvas(shell, SWT.BORDER);
        canvas.setSize(100, 50);
        assertThat(canvas.getClientArea()).isEqualTo(new Rectangle(0, 0, 100 - 2 * LINE, 50 - 2 * LINE));
    }

    @Test
    @DisplayName("each bar takes its thickness on its own side, and the trim counts a hidden bar too")
    void eachBarTakesItsOwnSide() {
        Shell shell = Mocks.shell();
        Composite both = sized(new Composite(shell, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER));
        assertThat(both.getClientArea()).isEqualTo(new Rectangle(0, 0, 100 - 2 * LINE - BAR, 50 - 2 * LINE - BAR));
        assertThat(both.computeTrim(0, 0, 10, 10)).isEqualTo(new Rectangle(-LINE, -LINE, 10 + 2 * LINE + BAR, 10 + 2 * LINE + BAR));
        both.getVerticalBar().setVisible(false);
        assertThat(both.getClientArea()).isEqualTo(new Rectangle(0, 0, 100 - 2 * LINE, 50 - 2 * LINE - BAR));
        assertThat(both.computeTrim(0, 0, 10, 10)).isEqualTo(new Rectangle(-LINE, -LINE, 10 + 2 * LINE + BAR, 10 + 2 * LINE + BAR));
        Composite horizontal = sized(new Composite(shell, SWT.H_SCROLL));
        assertThat(horizontal.getClientArea()).isEqualTo(new Rectangle(0, 0, 100, 50 - BAR));
        assertThat(horizontal.computeTrim(0, 0, 10, 10)).isEqualTo(new Rectangle(0, 0, 10, 10 + BAR));
    }

    @Test
    @DisplayName("a client area smaller than the frame is empty, never negative")
    void aTinyCompositeHasAnEmptyClientArea() {
        Composite composite = new Composite(Mocks.shell(), SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        composite.setSize(5, 5);
        assertThat(composite.getClientArea()).isEqualTo(new Rectangle(0, 0, 0, 0));
    }

    @Test
    @DisplayName("the preferred size of a scrolled composite leaves its layout the room it asked for")
    void thePreferredSizeLeavesTheLayoutItsRoom() {
        Composite composite = new Composite(Mocks.shell(), SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        composite.setLayout(fixedLayout(100, 50));
        composite.setSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        assertThat(composite.getClientArea()).isEqualTo(new Rectangle(0, 0, 100, 50));
    }

    @Test
    @DisplayName("the preferred size of a subclass that reserves a header in computeTrim includes the header")
    void thePreferredSizeGoesThroughAnOverriddenTrim() {
        Composite composite = new Composite(Mocks.shell(), SWT.NONE) {
            @Override
            public Rectangle computeTrim(int x, int y, int width, int height) {
                return withHeader(super.computeTrim(x, y, width, height));
            }
        };
        composite.setLayout(fixedLayout(100, 50));
        assertThat(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT)).isEqualTo(new Point(100, 50 + HEADER));
    }

    @Test
    @DisplayName("without a layout, the preferred size measures the children from a subclass's own client area")
    void thePreferredSizeGoesThroughAnOverriddenClientArea() {
        Composite composite = new Composite(Mocks.shell(), SWT.NONE) {
            @Override
            public Rectangle getClientArea() {
                Rectangle area = super.getClientArea();
                area.y += HEADER;
                area.height = Math.max(0, area.height - HEADER);
                return area;
            }
        };
        Composite child = new Composite(composite, SWT.NONE);
        child.setBounds(10, HEADER + 5, 30, 20);
        // Native Composite.minimumSize: each child's far edge, less the client area's origin.
        assertThat(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT)).isEqualTo(new Point(10 + 30, 5 + 20));
    }

    @Test
    @DisplayName("a styled text or tab folder subclass that reserves a header in computeTrim is that much taller")
    void subclassableScrollablesGoThroughAnOverriddenTrim() {
        Shell shell = Mocks.shell();
        StyledText plainText = new StyledText(shell, SWT.NONE);
        StyledText headedText = new StyledText(shell, SWT.NONE) {
            @Override
            public Rectangle computeTrim(int x, int y, int width, int height) {
                return withHeader(super.computeTrim(x, y, width, height));
            }
        };
        assertThat(headedText.computeSize(SWT.DEFAULT, SWT.DEFAULT).y)
                .isEqualTo(plainText.computeSize(SWT.DEFAULT, SWT.DEFAULT).y + HEADER);

        CTabFolder plainFolder = new CTabFolder(shell, SWT.NONE);
        CTabFolder headedFolder = new CTabFolder(shell, SWT.NONE) {
            @Override
            public Rectangle computeTrim(int x, int y, int width, int height) {
                return withHeader(super.computeTrim(x, y, width, height));
            }
        };
        assertThat(headedFolder.computeSize(SWT.DEFAULT, SWT.DEFAULT).y)
                .isEqualTo(plainFolder.computeSize(SWT.DEFAULT, SWT.DEFAULT).y + HEADER);
    }
}
