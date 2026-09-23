package dev.equo.swt;

import org.eclipse.e4.ui.workbench.renderers.swt.TrimmedPartLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DartMainToolbar;
import org.eclipse.swt.widgets.DartMocks;
import org.eclipse.swt.widgets.DartSideBar;
import org.eclipse.swt.widgets.DartStatusBar;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * The implementation a workbench trim composite gets is decided while it is constructed, before e4
 * assigns the matching TrimmedPartLayout field, so its side has to come from the e4 model bound to
 * the Shell. The stand-ins below reproduce what that model exposes and how e4 renders a bar: its
 * renderer is assigned right before its composite is created, its widget bound right after.
 */
@Tag("native-unit")
class TrimSideClassificationNativeTest {

    private Shell shell;
    private TrimmedPartLayout trim;

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new dev.equo.swt.harness.RecordingBridge());
        shell = DartMocks.dartShell();
        trim = new TrimmedPartLayout();
        when(shell.getLayout()).thenReturn(trim);
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    @Test
    void rails_of_a_window_that_does_not_render_its_bottom_trim_are_side_bars() {
        TrimmedWindow window = boundWindow(
                bar(SideValue.TOP), bar(SideValue.BOTTOM).hidden(), bar(SideValue.LEFT), bar(SideValue.RIGHT));

        render(window);

        assertThat(trim.top.getImpl()).isInstanceOf(DartMainToolbar.class);
        assertThat(trim.bottom).isNull();
        assertThat(trim.left.getImpl()).isInstanceOf(DartSideBar.class);
        assertThat(trim.right.getImpl()).isInstanceOf(DartSideBar.class);
    }

    @Test
    void every_rendered_trim_is_built_for_its_own_side() {
        TrimmedWindow window = boundWindow(
                bar(SideValue.TOP), bar(SideValue.BOTTOM), bar(SideValue.LEFT), bar(SideValue.RIGHT));

        render(window);

        assertSidesBuiltAsTheirOwnImplementations();
    }

    @Test
    void a_bottom_trim_rendered_before_the_top_one_is_a_status_bar() {
        TrimmedWindow window = boundWindow(
                bar(SideValue.BOTTOM), bar(SideValue.TOP), bar(SideValue.LEFT), bar(SideValue.RIGHT));

        render(window);

        assertSidesBuiltAsTheirOwnImplementations();
    }

    @Test
    void a_bottom_trim_rendered_later_is_a_status_bar() {
        TrimBar bottom = bar(SideValue.BOTTOM).hidden();
        TrimmedWindow window = boundWindow(bar(SideValue.TOP), bottom, bar(SideValue.LEFT), bar(SideValue.RIGHT));
        render(window);

        bottom.toBeRendered = true;
        render(window);

        assertSidesBuiltAsTheirOwnImplementations();
    }

    @Test
    void a_bottom_trim_added_while_the_side_trims_are_pending_is_a_status_bar() {
        TrimBar top = bar(SideValue.TOP);
        TrimBar right = bar(SideValue.RIGHT);
        TrimBar left = bar(SideValue.LEFT);
        TrimmedWindow window = boundWindow(top, right, left);
        render(top);

        // Added to the window while it renders: e4 renders it at once, ahead of the side trims.
        TrimBar bottom = bar(SideValue.BOTTOM);
        window.trimBars.add(bottom);
        render(bottom);
        render(window);

        assertSidesBuiltAsTheirOwnImplementations();
    }

    @Test
    void without_an_e4_model_the_side_is_still_classified_by_elimination() {
        for (SideValue side : SideValue.values()) {
            trim.getTrimComposite(shell, side.swt);
        }

        assertSidesBuiltAsTheirOwnImplementations();
    }

    private void assertSidesBuiltAsTheirOwnImplementations() {
        assertThat(trim.top.getImpl()).isInstanceOf(DartMainToolbar.class);
        assertThat(trim.bottom.getImpl()).isInstanceOf(DartStatusBar.class);
        assertThat(trim.left.getImpl()).isInstanceOf(DartSideBar.class);
        assertThat(trim.right.getImpl()).isInstanceOf(DartSideBar.class);
    }

    private TrimmedWindow boundWindow(TrimBar... bars) {
        TrimmedWindow window = new TrimmedWindow();
        window.trimBars.addAll(List.of(bars));
        when(shell.getData("modelElement")).thenReturn(window);
        return window;
    }

    /** What e4's window renderer does with the trim bars: render each pending one in list order. */
    private void render(TrimmedWindow window) {
        for (TrimBar bar : window.getTrimBars()) {
            if (bar.isToBeRendered() && bar.getWidget() == null) render(bar);
        }
    }

    /** What e4 does to render one bar: renderer first, then the composite, then the widget binding. */
    private void render(TrimBar bar) {
        bar.renderer = new Object();
        Composite composite = trim.getTrimComposite(shell, bar.getSide().swt);
        bar.widget = composite;
    }

    private static TrimBar bar(SideValue side) {
        return new TrimBar(side);
    }

    /** Stand-in for {@code org.eclipse.e4.ui.model.application.ui.SideValue}. */
    public enum SideValue {
        TOP(SWT.TOP), BOTTOM(SWT.BOTTOM), LEFT(SWT.LEFT), RIGHT(SWT.RIGHT);

        final int swt;

        SideValue(int swt) {
            this.swt = swt;
        }
    }

    /** Stand-in for the model's {@code MTrimmedWindow}. */
    public static class TrimmedWindow {
        final List<TrimBar> trimBars = new ArrayList<>();

        public List<TrimBar> getTrimBars() {
            return trimBars;
        }
    }

    /** Stand-in for the model's {@code MTrimBar}. */
    public static class TrimBar {
        final SideValue side;
        boolean toBeRendered = true;
        Object renderer;
        Object widget;

        TrimBar(SideValue side) {
            this.side = side;
        }

        TrimBar hidden() {
            toBeRendered = false;
            return this;
        }

        public SideValue getSide() {
            return side;
        }

        public boolean isToBeRendered() {
            return toBeRendered;
        }

        public Object getRenderer() {
            return renderer;
        }

        public Object getWidget() {
            return widget;
        }
    }
}
