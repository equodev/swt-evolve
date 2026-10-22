package dev.equo.swt;

import org.eclipse.e4.ui.workbench.renderers.swt.TrimmedPartLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DartMainToolbar;
import org.eclipse.swt.widgets.DartMocks;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * e4 creates the four trim bars of the workbench Shell lazily, so the side a trim occupies is not
 * knowable while its Composite is being constructed; Evolve classifies it by elimination over the
 * layout fields already assigned, and a host that never builds a bottom trim (no status line) makes
 * every later trim look like the bottom one. The trim implementation therefore has to size itself
 * from the side it really landed on, read at computeSize time.
 *
 * <p>The size at stake is a width: e4's TrimmedPartLayout takes {@code computeSize().x} off the
 * client area for each vertical trim, so a bar that claims a main-toolbar width on the left and on
 * the right leaves the workbench body with none.
 */
@Tag("native-unit")
class TrimBarSideSizingNativeTest {

    /** Wide enough that two bars claiming a toolbar width would still overflow it. */
    private static final int SHELL_WIDTH = 2100;
    private static final int BODY_HEIGHT = 1155;

    private Shell shell;
    private TrimmedPartLayout trim;

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new dev.equo.swt.harness.RecordingBridge());
        shell = DartMocks.dartShell();
        trim = new TrimmedPartLayout();
        when(shell.getLayout()).thenReturn(trim);
        // The main toolbar is always the first trim e4 asks for; everything below is what comes next.
        trim.getTrimComposite(shell, SWT.TOP);
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    private static int trimWidth(Composite trimBar) {
        return trimBar.computeSize(SWT.DEFAULT, BODY_HEIGHT, true).x;
    }

    @Test
    void empty_vertical_trims_leave_the_whole_body_width() {
        Composite left = trim.getTrimComposite(shell, SWT.LEFT);
        Composite right = trim.getTrimComposite(shell, SWT.RIGHT);

        assertThat(trimWidth(left)).isZero();
        assertThat(trimWidth(right)).isZero();
        assertThat(SHELL_WIDTH - trimWidth(left) - trimWidth(right)).isEqualTo(SHELL_WIDTH);
    }

    @Test
    void a_populated_vertical_trim_reserves_a_rail_not_a_bar() {
        Composite left = trim.getTrimComposite(shell, SWT.LEFT);
        new Composite(left, SWT.NONE);

        assertThat(trimWidth(left)).isEqualTo(30);
    }

    @Test
    void the_bottom_trim_still_sizes_as_a_status_bar() {
        Composite bottom = trim.getTrimComposite(shell, SWT.BOTTOM);

        assertThat(bottom.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).y).isEqualTo(36);
    }

    /**
     * e4's TrimBarLayout caches the lines it wraps the trim into and only rebuilds them when
     * computeSize is asked to flush; TrimmedPartLayout asks exactly that of the bottom trim on every
     * pass. A status bar that answers without consulting its layout leaves the lines of its first,
     * wider pass in place, and every contribution past the new width is laid out off-screen.
     */
    @Test
    void the_bottom_trim_lets_its_layout_rebuild_for_the_new_width() {
        Composite bottom = trim.getTrimComposite(shell, SWT.BOTTOM);
        RecordingLayout layout = new RecordingLayout();
        bottom.setLayout(layout);

        bottom.computeSize(1920, SWT.DEFAULT, true);
        Point size = bottom.computeSize(1400, SWT.DEFAULT, true);

        assertThat(layout.lastWHint).isEqualTo(1400);
        assertThat(layout.lastFlush).isTrue();
        assertThat(size.y).isEqualTo(36);
    }

    /**
     * The first trim is built as the main toolbar, so on a Shell whose first trim is the bottom one
     * that class sizes the status bar, and owes its layout the same flush.
     */
    @Test
    void a_main_toolbar_on_the_bottom_trim_lets_its_layout_rebuild_too() {
        Shell bottomFirst = DartMocks.dartShell();
        TrimmedPartLayout bottomFirstTrim = new TrimmedPartLayout();
        when(bottomFirst.getLayout()).thenReturn(bottomFirstTrim);
        Composite bottom = bottomFirstTrim.getTrimComposite(bottomFirst, SWT.BOTTOM);
        assertThat(bottom.getImpl()).isInstanceOf(DartMainToolbar.class);
        RecordingLayout layout = new RecordingLayout();
        bottom.setLayout(layout);

        Point size = bottom.computeSize(1400, SWT.DEFAULT, true);

        assertThat(layout.lastWHint).isEqualTo(1400);
        assertThat(layout.lastFlush).isTrue();
        assertThat(size.y).isEqualTo(36);
    }

    private static final class RecordingLayout extends Layout {
        int lastWHint = Integer.MIN_VALUE;
        boolean lastFlush;

        @Override
        protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
            lastWHint = wHint;
            lastFlush = flushCache;
            return new Point(wHint, 30);
        }

        @Override
        protected void layout(Composite composite, boolean flushCache) {
        }
    }
}
