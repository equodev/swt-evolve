package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.NoopBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/**
 * Shift+F10 and the Menu key on a focused control fire {@link SWT#MenuDetect} with
 * {@link SWT#MENU_KEYBOARD} and show the control's menu, below the selected item of a Tree.
 *
 * <pre>./gradlew :swt-evolve:swt_native:test --tests '*ContextMenuKeyboardFlutterTest'</pre>
 */
@Tag("flutter-it")
class ContextMenuKeyboardFlutterTest {

    private Display display;
    private KeyedWebBridge web;
    private Shell shell;
    private Tree tree;
    private Menu menu;
    private final List<Event> menuDetects = new ArrayList<>();

    @BeforeEach
    void open() {
        install();
        shell = new Shell(display);
        shell.setLayout(new FillLayout());
        tree = new Tree(shell, SWT.BORDER);
        for (String s : new String[] {"First", "Second", "Third"})
            new TreeItem(tree, SWT.NONE).setText(s);
        tree.setSelection(tree.getItem(1));
        tree.addListener(SWT.MenuDetect, e -> menuDetects.add(copy(e)));
        menu = new Menu(tree);
        new MenuItem(menu, SWT.PUSH).setText("&Open");
        tree.setMenu(menu);
        shell.setSize(300, 200);
        shell.open();
        tree.setFocus();
        drain();
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed())
            display.dispose();
        FlutterBridge.set(null);
    }

    @Test
    @DisplayName("Shift+F10 fires MenuDetect MENU_KEYBOARD and shows the menu")
    void shiftF10() {
        assumeFalse("cocoa".equals(SWT.getPlatform()), "Cocoa has no keyboard context-menu key");

        pressShiftF10();

        assertThat(menuDetects).singleElement().satisfies(e -> assertThat(e.detail).isEqualTo(SWT.MENU_KEYBOARD));
        assertThat(menu.getVisible()).isTrue();
    }

    @Test
    @DisplayName("A Shift+F10 KeyDown a listener vetoes opens nothing")
    void shiftF10Vetoed() {
        assumeFalse("cocoa".equals(SWT.getPlatform()), "Cocoa has no keyboard context-menu key");
        tree.addListener(SWT.KeyDown, e -> e.doit = false);

        pressShiftF10();

        assertThat(menuDetects).isEmpty();
        assertThat(menu.getVisible()).isFalse();
    }

    @Test
    @DisplayName("The Menu key fires MenuDetect MENU_KEYBOARD and shows the menu below the selected item")
    void menuKey() {
        pressMenuKey();

        Rectangle item = tree.getItem(1).getBounds();
        Point expected = tree.toDisplay(item.x + item.width / 2, item.y + item.height);
        assertThat(menuDetects).singleElement().satisfies(e -> {
            assertThat(e.detail).isEqualTo(SWT.MENU_KEYBOARD);
            assertThat(new Point(e.x, e.y)).isEqualTo(expected);
        });
        assertThat(menu.getVisible()).isTrue();
    }

    @Test
    @DisplayName("A MenuDetect listener that clears doit keeps the menu closed")
    void menuDetectVeto() {
        tree.addListener(SWT.MenuDetect, e -> e.doit = false);

        pressMenuKey();

        assertThat(menuDetects).hasSize(1);
        assertThat(menu.getVisible()).isFalse();
    }

    private void pressShiftF10() {
        Event key = new Event();
        key.keyCode = SWT.F10;
        key.stateMask = SWT.SHIFT;
        web.comm.fireContaining("Display/" + display.hashCode() + "/Key/KeyDown", key);
        drain();
    }

    private void pressMenuKey() {
        web.comm.fireContaining("Display/" + display.hashCode() + "/MenuKey", new Event());
        drain();
    }

    private static Event copy(Event e) {
        Event c = new Event();
        c.detail = e.detail;
        c.x = e.x;
        c.y = e.y;
        return c;
    }

    private void drain() {
        while (display.readAndDispatch()) {
        }
    }

    private void install() {
        FlutterBridge.set(new NoopBridge());
        display = new Display();
        FlutterBridge.set(null);
        DartDisplay dd = (DartDisplay) display.getImpl();
        web = new KeyedWebBridge(dd);
        dd.setBridge(web);
        web.start(dd);
    }

    private static final class KeyedWebBridge extends TestWebBridge {
        KeyedWebBridge(DartDisplay display) {
            super(display);
        }

        @Override
        protected void start(DartDisplay display) {
            super.start(display);
            registerDisplayKeyEvents(display);
        }
    }
}
