package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingComm;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * What showing and hiding a control's context menu sends once the client holds it: the menu's
 * visibility and the Display's list of popups, not the menu, its items and the Display again.
 */
@Tag("flutter-it")
class ContextMenuWireBudgetTest {

    /** Bytes one show and hide of a menu the client already holds may take. */
    private static final int CYCLE_BUDGET = 80;

    private Display display;
    private RecordingWebBridge web;
    private Table table;
    private Menu menu;

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed()) display.dispose();
        FlutterBridge.set(null);
    }

    @Test
    void aShowAndHideSendsTheVisibilityAndThePopupList() {
        contextMenu();
        show();
        hide();
        web.comm.sent.clear();

        show();
        hide();

        assertThat(channels()).as("nothing but the popup list and the menu's change")
                .containsOnly(popupsChannel(), menuChannel(), menuChannel() + "/shown");
        assertThat(records(menuChannel())).allSatisfy(state -> assertThat(state).containsKey("_d"));
        assertThat(bytes()).isLessThanOrEqualTo(CYCLE_BUDGET);
    }

    @Test
    void anItemChangedWhileTheMenuIsHiddenReachesTheClientAsAChange() {
        contextMenu();
        show();
        hide();
        web.comm.sent.clear();

        MenuItem first = menu.getItem(0);
        first.setText("Changed");
        first.setEnabled(false);
        flush();
        show();
        hide();

        assertThat(records("MenuItem/" + first.hashCode())).singleElement().satisfies(state -> {
            assertThat(state).containsEntry("text", "Changed").containsEntry("enabled", false).containsKey("_d");
        });
        assertThat(channels()).doesNotContain(displayChannel());
        assertThat(bytes()).isLessThanOrEqualTo(CYCLE_BUDGET + 30);
    }

    @Test
    void whatAShowListenerChangesIsSentBeforeTheMenuIsShown() {
        contextMenu();
        show();
        hide();
        MenuItem first = menu.getItem(0);
        menu.addListener(SWT.Show, e -> first.setText("Filled on show"));
        web.comm.sent.clear();

        show();

        List<String> channels = channels();
        assertThat(channels).contains("MenuItem/" + first.hashCode());
        assertThat(channels.indexOf("MenuItem/" + first.hashCode())).isLessThan(channels.indexOf(menuChannel() + "/shown"));
    }

    @Test
    void aPopupNoControlHoldsIsDescribedAgainWhenShownAfterBeingHidden() {
        web = RecordingWebBridge.install();
        display = web.display;
        Shell shell = new Shell(display);
        shell.setSize(400, 300);
        shell.open();
        menu = new Menu(shell, SWT.POP_UP);
        new MenuItem(menu, SWT.PUSH).setText("Only");
        flush();
        show();
        web.comm.sent.clear();

        hide();
        assertThat(records(menuChannel())).as("nothing is sent to a client that let go of it").isEmpty();

        show();
        assertThat(records(menuChannel())).as("the client let go of it when it was hidden")
                .anySatisfy(state -> assertThat(state).doesNotContainKey("_d").containsKey("items"));
    }

    /** The problems view's shape: a table whose menu has twelve actions, a separator and a cascade. */
    private void contextMenu() {
        web = RecordingWebBridge.install();
        display = web.display;
        Shell shell = new Shell(display);
        shell.setSize(400, 300);
        table = new Table(shell, SWT.NONE);
        table.setBounds(0, 0, 300, 200);
        menu = new Menu(table);
        for (int m = 0; m < 12; m++) new MenuItem(menu, SWT.PUSH).setText("Action " + m + "\tCtrl+" + (char) ('A' + m));
        new MenuItem(menu, SWT.SEPARATOR);
        MenuItem cascade = new MenuItem(menu, SWT.CASCADE);
        cascade.setText("Quick Fix");
        Menu sub = new Menu(cascade);
        for (int m = 0; m < 6; m++) new MenuItem(sub, SWT.PUSH).setText("Fix " + m);
        cascade.setMenu(sub);
        table.setMenu(menu);
        shell.open();
        flush();
    }

    /** Java shows the menu, and the client answers with its Show. */
    private void show() {
        menu.setLocation(new Point(60, 40));
        menu.setVisible(true);
        flush();
        web.comm.fireContaining(menuChannel() + "/Menu/Show", new Event());
        flush();
    }

    /** Java hides the menu, and the client answers with its Hide. */
    private void hide() {
        menu.setVisible(false);
        flush();
        web.comm.fireContaining(menuChannel() + "/Menu/Hide", new Event());
        flush();
    }

    private String menuChannel() {
        return "Menu/" + menu.hashCode();
    }

    private String displayChannel() {
        return "Display/" + display.hashCode();
    }

    private String popupsChannel() {
        return displayChannel() + "/Popups";
    }

    private List<String> channels() {
        return web.comm.sent.stream().map(f -> f.event).toList();
    }

    private List<Map<String, Object>> records(String channel) {
        return web.comm.sent.stream().filter(f -> f.event.equals(channel) && f.value != null)
                .map(RecordingComm.Frame::state).toList();
    }

    private int bytes() {
        return web.comm.sent.stream().mapToInt(f -> f.size).sum();
    }

    private void flush() {
        FlutterBridge.update();
        while (display.readAndDispatch()) {
            // the client's notices run in asyncExec
        }
        FlutterBridge.update();
    }
}
