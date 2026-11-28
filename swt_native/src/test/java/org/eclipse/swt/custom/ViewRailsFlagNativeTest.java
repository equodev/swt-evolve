package org.eclipse.swt.custom;

import dev.equo.swt.Config;
import dev.equo.swt.ConfigFlags;
import dev.equo.swt.FlutterBridge;
import org.eclipse.swt.widgets.DartDisplay;
import org.eclipse.swt.widgets.DartMocks;
import org.eclipse.swt.widgets.DartWidget;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

/**
 * The view rails are a feature flag, {@code swt.evolve.view_rails}, on by default only in the Eclipse
 * IDE (its {@code eclipse.product}); the property overrides that either way. Off, a
 * changed view stack starts no rails work for its session: no record reaches the client, and the
 * workbench keeps its tab strips.
 */
@Tag("native-unit")
class ViewRailsFlagNativeTest {

    private static final String PROPERTY = "swt.evolve.view_rails";
    private static final String PRODUCT = "eclipse.product";
    private static final String SIZE_PROPERTY = "swt.evolve.view_rails_size";
    private static final String SESSION_KEY = ViewRails.class.getName();

    private ConfigFlags savedFlags;
    private String savedProperty;
    private String savedProduct;
    private Display display;
    private DartWidget changed;

    @BeforeEach
    void setUp() {
        savedFlags = Config.setConfigFlags(null);
        savedProperty = System.getProperty(PROPERTY);
        savedProduct = System.getProperty(PRODUCT);
        System.clearProperty(PRODUCT);
        FlutterBridge.set(new dev.equo.swt.harness.RecordingBridge());
        Shell shell = DartMocks.dartShell();
        display = shell.getDisplay();
        DartDisplay displayImpl = (DartDisplay) display.getImpl();
        doReturn(Thread.currentThread()).when(displayImpl).getThread();
        changed = (DartWidget) shell.getImpl();
        doReturn(shell).when(changed).getApi();
        display.setData(SESSION_KEY, null);
    }

    @AfterEach
    void tearDown() {
        if (savedProperty == null) System.clearProperty(PROPERTY);
        else System.setProperty(PROPERTY, savedProperty);
        if (savedProduct == null) System.clearProperty(PRODUCT);
        else System.setProperty(PRODUCT, savedProduct);
        System.clearProperty(SIZE_PROPERTY);
        Config.setConfigFlags(savedFlags);
        display.setData(SESSION_KEY, null);
        FlutterBridge.set(null);
    }

    @Test
    void the_rails_are_off_by_default_outside_the_eclipse_ide() {
        System.clearProperty(PROPERTY);
        System.setProperty(PRODUCT, "org.example.rcp.product");

        assertThat(Config.getConfigFlags().view_rails).isFalse();
    }

    @Test
    void the_rails_are_on_by_default_in_the_eclipse_ide() {
        System.clearProperty(PROPERTY);
        System.setProperty(PRODUCT, "org.eclipse.epp.package.java.product");

        assertThat(Config.getConfigFlags().view_rails).isTrue();
    }

    @Test
    void setting_the_property_to_true_turns_them_on_in_any_product() {
        System.setProperty(PROPERTY, "true");

        assertThat(Config.getConfigFlags().view_rails).isTrue();
    }

    @Test
    void every_eclipse_ide_product_counts_and_nothing_else() {
        assertThat(Config.isEclipseIde("org.eclipse.epp.package.rcp.product")).isTrue();
        assertThat(Config.isEclipseIde("org.eclipse.sdk.ide")).isTrue();
        assertThat(Config.isEclipseIde("org.eclipse.platform.ide")).isTrue();
        assertThat(Config.isEclipseIde("org.eclipse.e4.ide.application")).isFalse();
        assertThat(Config.isEclipseIde(null)).isFalse();
    }

    @Test
    void setting_the_property_to_false_turns_them_off_in_the_eclipse_ide() {
        System.setProperty(PRODUCT, "org.eclipse.epp.package.java.product");
        System.setProperty(PROPERTY, "false");

        assertThat(Config.getConfigFlags().view_rails).isFalse();
    }

    @Test
    void with_the_rails_on_a_changed_view_stack_starts_its_sessions_rails() {
        System.setProperty(PROPERTY, "true");

        ViewRails.touched(changed);

        assertThat(display.getData(SESSION_KEY)).isInstanceOf(ViewRails.class);
    }

    @Test
    void with_the_rails_off_a_changed_view_stack_starts_nothing() {
        System.setProperty(PROPERTY, "false");

        ViewRails.touched(changed);

        assertThat(display.getData(SESSION_KEY)).isNull();
    }

    @Test
    void the_rails_start_medium_unless_a_size_is_set() {
        System.clearProperty(SIZE_PROPERTY);
        assertThat(Config.getConfigFlags().view_rails_size).isEqualTo("medium");

        System.setProperty(SIZE_PROPERTY, "large");
        Config.setConfigFlags(null);
        assertThat(Config.getConfigFlags().view_rails_size).isEqualTo("large");
    }

    @Test
    void a_rail_is_as_wide_as_its_size_with_icons_to_draw_and_takes_no_room_without() {
        System.setProperty(PROPERTY, "true");
        ViewRails.touched(changed);
        ViewRails rails = (ViewRails) display.getData(SESSION_KEY);
        org.eclipse.swt.widgets.Composite left = trim(), right = trim(), other = trim();
        rails.current = new ViewRails.Record(java.util.List.of(org.mockito.Mockito.mock(CTabFolder.class)),
                java.util.List.of((long) (ViewRails.RIGHT_TOP | ViewRails.MINIMIZED)), java.util.List.of(left, right),
                java.util.List.of((long) org.eclipse.swt.SWT.LEFT, (long) org.eclipse.swt.SWT.RIGHT), java.util.List.of(),
                ViewRails.LARGE);

        assertThat(ViewRails.railWidth(sideBar(right))).isEqualTo(dev.equo.swt.size.ViewRailsSizes.width(ViewRails.LARGE));
        assertThat(ViewRails.railWidth(sideBar(left))).isZero();
        assertThat(ViewRails.railWidth(sideBar(other))).as("a side bar that draws no rail").isEqualTo(-1);

        System.setProperty(PROPERTY, "false");
        Config.setConfigFlags(null);
        assertThat(ViewRails.railWidth(sideBar(right))).as("with the rails off").isEqualTo(-1);
    }

    @Test
    void a_press_on_the_view_being_worked_in_minimizes_its_stack_and_on_any_other_does_not() {
        System.setProperty(PROPERTY, "true");
        ViewRails.touched(changed);
        ViewRails rails = (ViewRails) display.getData(SESSION_KEY);
        java.util.List<String> minimized = new java.util.ArrayList<>();
        DartCTabFolder folder = folder(minimized, 1, true);

        rails.press(folder, 1);
        assertThat(minimized).containsExactly("minimize");

        minimized.clear();
        rails.press(folder(minimized, 1, false), 1);
        rails.press(folder(minimized, 0, true), 1);
        assertThat(minimized).as("a view on screen but not active, and a view not on screen").isEmpty();
    }

    /** A folder with view {@code selected} on screen, holding the active part or not. */
    private static DartCTabFolder folder(java.util.List<String> calls, int selected, boolean active) {
        CTabFolder api = org.mockito.Mockito.mock(CTabFolder.class);
        api.folderListeners = new CTabFolder2Listener[] {new CTabFolder2Adapter() {
            @Override
            public void minimize(CTabFolderEvent event) {
                calls.add("minimize");
            }
        }};
        doReturn(selected).when(api).getSelectionIndex();
        DartCTabFolder folder = org.mockito.Mockito.mock(DartCTabFolder.class);
        doReturn(api).when(folder).getApi();
        doReturn(active).when(folder)._highlight();
        doReturn(2).when(folder).getItemCount();
        return folder;
    }

    @Test
    void header_controls_that_do_not_fit_a_listed_view_run_past_its_inner_edge() {
        System.setProperty(PROPERTY, "true");
        ViewRails.touched(changed);
        ViewRails rails = (ViewRails) display.getData(SESSION_KEY);
        CTabFolder right = org.mockito.Mockito.mock(CTabFolder.class), left = org.mockito.Mockito.mock(CTabFolder.class);
        rails.current = new ViewRails.Record(java.util.List.of(right, left),
                java.util.List.of((long) ViewRails.RIGHT_TOP, (long) ViewRails.LEFT_TOP), java.util.List.of(),
                java.util.List.of(), java.util.List.of(), ViewRails.MEDIUM);

        org.eclipse.swt.graphics.Rectangle[] wide = ViewRails.headerControlBounds(header(right, 100, 150, 40), headerRects(), new boolean[2]);
        assertThat(wide[0]).as("the toolbar, from the left of a view on the right rail")
                .isEqualTo(new org.eclipse.swt.graphics.Rectangle(-90, 2, 150, 20));
        assertThat(wide[1].x).as("minimize and maximize, after it").isEqualTo(60);

        wide = ViewRails.headerControlBounds(header(left, 100, 150, 40), headerRects(), new boolean[2]);
        assertThat(wide[0].x).as("from the left edge of a view on the left rail").isZero();
        assertThat(wide[1].x).isEqualTo(150);

        assertThat(ViewRails.headerControlBounds(header(right, 300, 150, 40), headerRects(), new boolean[2]))
                .as("controls that fit stay where the folder put them").isNull();
    }

    /** The toolbar squeezed to 50 and minimize-maximize at 40, as a 100-wide folder packs them. */
    private static org.eclipse.swt.graphics.Rectangle[] headerRects() {
        return new org.eclipse.swt.graphics.Rectangle[] {
            new org.eclipse.swt.graphics.Rectangle(10, 2, 50, 20), new org.eclipse.swt.graphics.Rectangle(60, 2, 40, 20)};
    }

    private DartCTabFolder header(CTabFolder api, int width, int toolbar, int buttons) {
        doReturn(display).when(api).getDisplay();
        doReturn(new org.eclipse.swt.graphics.Point(width, 200)).when(api).getSize();
        DartCTabFolder folder = org.mockito.Mockito.mock(DartCTabFolder.class);
        doReturn(api).when(folder).getApi();
        folder.controls = new org.eclipse.swt.widgets.Control[] {control(toolbar), control(buttons)};
        return folder;
    }

    private static org.eclipse.swt.widgets.Control control(int preferredWidth) {
        org.eclipse.swt.widgets.Control control = org.mockito.Mockito.mock(org.eclipse.swt.widgets.Control.class);
        doReturn(true).when(control).getVisible();
        doReturn(new org.eclipse.swt.graphics.Point(preferredWidth, 20)).when(control)
                .computeSize(org.eclipse.swt.SWT.DEFAULT, org.eclipse.swt.SWT.DEFAULT);
        return control;
    }

    private org.eclipse.swt.widgets.Composite trim() {
        org.eclipse.swt.widgets.Composite trim = org.mockito.Mockito.mock(org.eclipse.swt.widgets.Composite.class);
        doReturn(display).when(trim).getDisplay();
        return trim;
    }

    private static org.eclipse.swt.widgets.DartSideBar sideBar(org.eclipse.swt.widgets.Composite api) {
        org.eclipse.swt.widgets.DartSideBar sideBar = org.mockito.Mockito.mock(org.eclipse.swt.widgets.DartSideBar.class);
        doReturn(api).when(sideBar).getApi();
        return sideBar;
    }
}
