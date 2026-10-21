package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.ConfigFlags;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.size.MenuSizes;
import org.eclipse.e4.ui.workbench.renderers.swt.TrimmedPartLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.DPIUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The menu bar is drawn inside the main toolbar, so a Shell with no visible one draws the strip
 * itself, above its client area, as a native Shell does. The client area has to give that strip up,
 * or the Shell's layout puts its top row of controls underneath it, and whatever moves the menu
 * between the two re-sends the Shell and lays it out again.
 */
@Tag("native-unit")
class ShellMenuBarClientAreaNativeTest {

    private static final Rectangle SHELL_BOUNDS = new Rectangle(0, 0, 800, 600);
    private static final String OWN_MENU_STRIP = "ownMenuStrip";
    private static final String SYSTEM_MENU_BAR = "dev.equo.swt.systemMenuBar";

    private Shell shell;
    private DartShell dartShell;
    private VShell shellValue;
    private Menu menuBar;

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new dev.equo.swt.harness.RecordingBridge());
        shell = DartMocks.dartShell();
        dartShell = (DartShell) shell.getImpl();
        shellValue = mock(VShell.class);
        when(dartShell.getValue()).thenReturn(shellValue);
        when(dartShell.getApi()).thenReturn(shell);
        when(dartShell.getBounds()).thenReturn(SHELL_BOUNDS);
        when(shell.getChildren()).thenReturn(new Control[0]);
        menuBar = mock(Menu.class);
        when(menuBar.getItemCount()).thenReturn(2);
        when(shell.getMenuBar()).thenReturn(menuBar);
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    private Composite mainToolbar() {
        TrimmedPartLayout trim = new TrimmedPartLayout();
        when(shell.getLayout()).thenReturn(trim);
        Composite top = trim.getTrimComposite(shell, SWT.TOP);
        assertThat(top.getImpl()).isInstanceOf(DartMainToolbar.class);
        when(shell.getChildren()).thenReturn(new Control[] {top});
        clearInvocations(shellValue, dartShell);
        return top;
    }

    private void verifyMenuStripMoved() {
        verify(shellValue).markDirty(OWN_MENU_STRIP);
        verify(dartShell).sendEvent(SWT.Resize);
    }

    @Test
    void a_shell_with_no_main_toolbar_keeps_its_menu_strip_out_of_the_client_area() {
        assertThat(Sizes.getClientArea(dartShell))
                .isEqualTo(new Rectangle(0, 0, 800, 600 - MenuSizes.MENU_BAR_HEIGHT));
        assertThat(Sizes.computeTrim(dartShell, 0, 0, 800, 600 - MenuSizes.MENU_BAR_HEIGHT))
                .isEqualTo(new Rectangle(0, -MenuSizes.MENU_BAR_HEIGHT, 800, 600));
    }

    @Test
    void the_strip_keeps_the_monitor_scale_when_the_zoom_keeps_text_at_its_true_size() {
        int savedZoom = DPIUtil.getDeviceZoom();
        String savedMode = ConfigFlags.mode();
        String savedSystemMenuBar = System.getProperty(SYSTEM_MENU_BAR);
        try {
            // SWT laying out at 100 on a 150% monitor: text is painted 1.5 times larger than boxes.
            ConfigFlags.setMode(ConfigFlags.MODE_DESKTOP);
            // The strip drawn in the window: on a macOS host the desktop mode puts the menu bar in the
            // OS menu bar instead, and the Shell draws none.
            System.setProperty(SYSTEM_MENU_BAR, "false");
            DPIUtil.setDeviceZoom(100);
            Config.setClientDeviceZoom(150);

            assertThat(Sizes.menuStripHeight()).isEqualTo(42);
            assertThat(Sizes.getClientArea(dartShell).height).isEqualTo(600 - 42);
        } finally {
            Config.setClientDeviceZoom(100);
            DPIUtil.setDeviceZoom(savedZoom);
            if (savedSystemMenuBar == null) System.clearProperty(SYSTEM_MENU_BAR);
            else System.setProperty(SYSTEM_MENU_BAR, savedSystemMenuBar);
            if (savedMode == null) System.clearProperty(ConfigFlags.MODE_PROPERTY);
            else ConfigFlags.setMode(savedMode);
        }
    }

    @Test
    void a_visible_main_toolbar_hosts_the_menu_so_the_client_area_is_whole() {
        mainToolbar();

        assertThat(Sizes.getClientArea(dartShell)).isEqualTo(new Rectangle(0, 0, 800, 600));
    }

    @Test
    void a_hidden_main_toolbar_leaves_the_strip_to_the_shell() {
        mainToolbar().setVisible(false);

        assertThat(Sizes.getClientArea(dartShell).height)
                .isEqualTo(600 - MenuSizes.MENU_BAR_HEIGHT);
    }

    @Test
    void an_itemless_menu_bar_takes_no_strip() {
        when(menuBar.getItemCount()).thenReturn(0);

        assertThat(Sizes.getClientArea(dartShell)).isEqualTo(new Rectangle(0, 0, 800, 600));
    }

    @Test
    void a_bare_decorations_keeps_its_menu_strip_out_of_the_client_area_too() {
        Decorations decorations = mock(Decorations.class);
        DartDecorations impl = mock(DartDecorations.class);
        when(decorations.getImpl()).thenReturn(impl);
        when(impl.getApi()).thenReturn(decorations);
        when(impl.getBounds()).thenReturn(SHELL_BOUNDS);
        when(decorations.getChildren()).thenReturn(new Control[0]);
        when(decorations.getMenuBar()).thenReturn(menuBar);

        assertThat(Sizes.getClientArea(impl).height).isEqualTo(600 - MenuSizes.MENU_BAR_HEIGHT);
    }

    @Test
    void hiding_the_main_toolbar_re_sends_and_lays_out_the_shell_that_now_draws_the_menu() {
        Composite toolbar = mainToolbar();

        toolbar.setVisible(false);

        verifyMenuStripMoved();
    }

    @Test
    void showing_the_main_toolbar_again_takes_the_menu_back_from_the_shell() {
        Composite toolbar = mainToolbar();
        toolbar.setVisible(false);
        clearInvocations(shellValue, dartShell);

        toolbar.setVisible(true);

        verifyMenuStripMoved();
        assertThat(Sizes.getClientArea(dartShell)).isEqualTo(new Rectangle(0, 0, 800, 600));
    }

    @Test
    void hiding_the_main_toolbar_of_a_shell_with_no_menu_does_not_re_send_it() {
        when(shell.getMenuBar()).thenReturn(null);
        Composite toolbar = mainToolbar();

        toolbar.setVisible(false);

        verify(shellValue, never()).markDirty(OWN_MENU_STRIP);
        verify(dartShell, never()).sendEvent(SWT.Resize);
    }

    @Test
    void the_first_item_on_a_menu_bar_gives_the_shell_its_strip_and_the_last_one_out_takes_it_away() {
        Menu bar = new Menu(shell, SWT.BAR);
        when(shell.getMenuBar()).thenReturn(bar);
        dartShell.menuBar = bar;

        MenuItem file = new MenuItem(bar, SWT.CASCADE);
        verifyMenuStripMoved();

        clearInvocations(shellValue, dartShell);
        MenuItem help = new MenuItem(bar, SWT.CASCADE);
        verify(dartShell, never()).sendEvent(SWT.Resize);

        help.dispose();
        verify(dartShell, never()).sendEvent(SWT.Resize);
        file.dispose();
        verify(dartShell, times(1)).sendEvent(SWT.Resize);
        assertThat(Sizes.getClientArea(dartShell)).isEqualTo(new Rectangle(0, 0, 800, 600));
    }

    @Test
    void a_control_under_the_shells_own_strip_maps_to_the_display_below_it() {
        Composite body = new Composite(shell, SWT.NONE);
        body.setBounds(10, 20, 100, 50);

        assertThat(body.toDisplay(0, 0).y).isEqualTo(20 + MenuSizes.MENU_BAR_HEIGHT);

        when(shell.getMenuBar()).thenReturn(null);
        assertThat(body.toDisplay(0, 0).y).isEqualTo(20);
    }
}
