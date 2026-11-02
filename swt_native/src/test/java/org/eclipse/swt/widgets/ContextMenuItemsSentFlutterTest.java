package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.WireValues;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A context menu is filled when it is about to show, so the items it gains then have to reach the
 * client in the menu's own record: the control it belongs to only names it, and naming the same menu
 * again carries none of what changed in it.
 */
@Tag("flutter-it")
class ContextMenuItemsSentFlutterTest {

    private Display display;

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed())
            display.dispose();
        FlutterBridge.set(null);
    }

    @Test
    void itemsAddedToAControlsMenuAreSentInTheMenusRecord() {
        RecordingWebBridge web = RecordingWebBridge.install();
        display = web.display;
        Shell shell = new Shell(display);
        shell.setSize(400, 300);
        Tree tree = new Tree(shell, SWT.NONE);
        Menu menu = new Menu(tree);
        tree.setMenu(menu);
        shell.open();
        FlutterBridge.update();
        web.comm.sent.clear();

        MenuItem refresh = new MenuItem(menu, SWT.PUSH);
        refresh.setText("Refresh");
        FlutterBridge.update();

        String channel = "Menu/" + menu.hashCode();
        assertThat(web.comm.sent.stream().filter(f -> f.event.equals(channel)).map(f -> f.value))
                .as("the menu's own record carries the item it gained")
                .anyMatch(value -> WireValues.mentions(value, "items"));
    }
}
