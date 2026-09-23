package dev.equo.swt;

import org.eclipse.e4.ui.workbench.renderers.swt.TrimmedPartLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DartMocks;
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
}
