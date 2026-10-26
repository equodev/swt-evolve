package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.NoopBridge;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A lone Alt whose window lost the keyboard before its release (Alt+Tab away and back) does not
 * activate the menu bar.
 *
 * <pre>./gradlew :swt-evolve:swt_native:test --tests '*MenuBarLoneAltDeactivationFlutterTest'</pre>
 */
@Tag("flutter-it")
class MenuBarLoneAltDeactivationFlutterTest {

    private Display display;
    private TestWebBridge web;

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed())
            display.dispose();
        FlutterBridge.set(null);
    }

    @Test
    @DisplayName("the shell deactivated between Alt's press and release keeps the menu bar inactive")
    void deactivationEndsTheLoneAlt() {
        FlutterBridge.set(new NoopBridge());
        display = new Display();
        FlutterBridge.set(null);
        DartDisplay dd = (DartDisplay) display.getImpl();
        web = new TestWebBridge(dd);
        dd.setBridge(web);
        web.start(dd);
        Shell shell = new Shell(display);
        Menu bar = new Menu(shell, SWT.BAR);
        MenuItem file = new MenuItem(bar, SWT.CASCADE);
        file.setText("&File");
        file.setMenu(new Menu(shell, SWT.DROP_DOWN));
        shell.setMenuBar(bar);
        Text text = new Text(shell, SWT.SINGLE);
        shell.open();
        DartControl focus = (DartControl) text.getImpl();

        ControlHelper.routeKeyDown(focus, key(SWT.ALT));
        web.comm.fireContaining("/" + shell.hashCode() + "/Shell/Deactivate", new Event());
        while (display.readAndDispatch()) {
        }
        web.comm.sent.clear();
        ControlHelper.routeKeyUp(focus, key(SWT.ALT));

        assertThat(web.comm.sent).noneMatch(f -> f.event.endsWith("/activate"));
    }

    private static Event key(int keyCode) {
        Event key = new Event();
        key.keyCode = keyCode;
        return key;
    }
}
