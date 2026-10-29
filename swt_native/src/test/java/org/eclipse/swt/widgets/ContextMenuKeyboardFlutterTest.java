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

/**
 * Shift+F10 and the Menu key on a focused control fire {@link SWT#MenuDetect} with
 * {@link SWT#MENU_KEYBOARD} and show the control's menu: below the selected item of a Tree or a
 * Table, at the pointer otherwise.
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
        menu = menuOf(tree);
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
        press(SWT.F10, SWT.SHIFT);

        assertThat(menuDetects).singleElement().satisfies(e -> assertThat(e.detail).isEqualTo(SWT.MENU_KEYBOARD));
        assertThat(menu.getVisible()).isTrue();
    }

    @Test
    @DisplayName("F10 alone or with another modifier added to Shift is not a context-menu request")
    void otherF10Chords() {
        press(SWT.F10, 0);
        press(SWT.F10, SWT.SHIFT | SWT.CTRL);

        assertThat(menuDetects).isEmpty();
        assertThat(menu.getVisible()).isFalse();
    }

    @Test
    @DisplayName("A Shift+F10 KeyDown a listener vetoes opens nothing")
    void shiftF10Vetoed() {
        tree.addListener(SWT.KeyDown, e -> e.doit = false);

        press(SWT.F10, SWT.SHIFT);

        assertThat(menuDetects).isEmpty();
        assertThat(menu.getVisible()).isFalse();
    }

    @Test
    @DisplayName("The Menu key fires MenuDetect MENU_KEYBOARD and shows the menu below the selected item")
    void menuKey() {
        pressMenuKey();

        Point expected = below(tree, tree.getItem(1).getBounds());
        assertThat(menuDetects).singleElement().satisfies(e -> {
            assertThat(e.detail).isEqualTo(SWT.MENU_KEYBOARD);
            assertThat(new Point(e.x, e.y)).isEqualTo(expected);
        });
        assertThat(menu.getVisible()).isTrue();
        assertThat(location(menu)).isEqualTo(expected);
    }

    @Test
    @DisplayName("A Tree with nothing selected opens its menu at the pointer")
    void treeWithoutSelection() {
        tree.deselectAll();

        pressMenuKey();

        assertOpenedAtPointer(menu);
    }

    @Test
    @DisplayName("A Table opens its menu below its selected row")
    void tableBelowSelectedRow() {
        Table table = new Table(shell, SWT.BORDER);
        for (String s : new String[] {"First", "Second", "Third"})
            new TableItem(table, SWT.NONE).setText(s);
        table.setSelection(2);
        Menu tableMenu = focusWithMenu(table);

        pressMenuKey();

        Point expected = below(table, table.getItem(2).getBounds());
        assertThat(menuDetects).singleElement().satisfies(e -> assertThat(new Point(e.x, e.y)).isEqualTo(expected));
        assertThat(tableMenu.getVisible()).isTrue();
        assertThat(location(tableMenu)).isEqualTo(expected);
    }

    @Test
    @DisplayName("A Table with nothing selected opens its menu at the pointer")
    void tableWithoutSelection() {
        Table table = new Table(shell, SWT.BORDER);
        new TableItem(table, SWT.NONE).setText("First");
        Menu tableMenu = focusWithMenu(table);

        pressMenuKey();

        assertOpenedAtPointer(tableMenu);
    }

    @Test
    @DisplayName("A control without items opens its menu at the pointer")
    void controlWithoutItems() {
        Button button = new Button(shell, SWT.PUSH);
        Menu buttonMenu = focusWithMenu(button);

        press(SWT.F10, SWT.SHIFT);

        assertOpenedAtPointer(buttonMenu);
    }

    @Test
    @DisplayName("A MenuDetect listener that moves the request opens the menu where it moved it")
    void listenerMovesTheMenu() {
        Button button = new Button(shell, SWT.PUSH);
        Menu buttonMenu = focusWithMenu(button);
        button.addListener(SWT.MenuDetect, e -> {
            e.x = 42;
            e.y = 24;
        });

        pressMenuKey();

        assertThat(buttonMenu.getVisible()).isTrue();
        assertThat(location(buttonMenu)).isEqualTo(new Point(42, 24));
    }

    @Test
    @DisplayName("A control without a menu still gets MenuDetect, so its listener can build one")
    void controlWithoutMenu() {
        tree.setMenu(null);

        pressMenuKey();

        assertThat(menuDetects).singleElement().satisfies(e -> assertThat(e.detail).isEqualTo(SWT.MENU_KEYBOARD));
        assertThat(menu.getVisible()).isFalse();
    }

    @Test
    @DisplayName("A menu disposed while still set on the control does not open")
    void disposedMenu() {
        menu.dispose();

        pressMenuKey();

        assertThat(menuDetects).hasSize(1);
        assertThat(tree.getMenu() == null || tree.getMenu().isDisposed()).isTrue();
    }

    @Test
    @DisplayName("A MenuDetect listener can attach the menu it builds, and that menu opens")
    void listenerAttachesTheMenu() {
        tree.setMenu(null);
        Menu built = new Menu(tree);
        new MenuItem(built, SWT.PUSH).setText("&Built");
        tree.addListener(SWT.MenuDetect, e -> tree.setMenu(built));

        pressMenuKey();

        assertThat(built.getVisible()).isTrue();
    }

    @Test
    @DisplayName("A MenuDetect listener that disposes the control ends the request")
    void listenerDisposesTheControl() {
        tree.addListener(SWT.MenuDetect, e -> tree.dispose());

        pressMenuKey();

        assertThat(tree.isDisposed()).isTrue();
        assertThat(menu.isDisposed() || !menu.getVisible()).isTrue();
    }

    @Test
    @DisplayName("A MenuDetect listener that clears doit keeps the menu closed")
    void menuDetectVeto() {
        tree.addListener(SWT.MenuDetect, e -> e.doit = false);

        pressMenuKey();

        assertThat(menuDetects).hasSize(1);
        assertThat(menu.getVisible()).isFalse();
    }

    private Menu focusWithMenu(Control control) {
        control.addListener(SWT.MenuDetect, e -> menuDetects.add(copy(e)));
        Menu controlMenu = menuOf(control);
        shell.layout();
        control.setFocus();
        drain();
        return controlMenu;
    }

    private static Menu menuOf(Control control) {
        Menu controlMenu = new Menu(control);
        new MenuItem(controlMenu, SWT.PUSH).setText("&Open");
        control.setMenu(controlMenu);
        return controlMenu;
    }

    /** Opened where Menu.setVisible puts a menu given no location: the pointer. */
    private void assertOpenedAtPointer(Menu opened) {
        Point pointer = display.getCursorLocation();
        assertThat(menuDetects).singleElement().satisfies(e -> {
            assertThat(e.detail).isEqualTo(SWT.MENU_KEYBOARD);
            assertThat(new Point(e.x, e.y)).isEqualTo(pointer);
        });
        assertThat(opened.getVisible()).isTrue();
        assertThat(((DartMenu) opened.getImpl())._hasLocation()).isFalse();
    }

    private static Point below(Control control, Rectangle item) {
        return control.toDisplay(item.x + item.width / 2, item.y + item.height);
    }

    private static Point location(Menu menu) {
        return ((DartMenu) menu.getImpl())._location();
    }

    private void press(int keyCode, int stateMask) {
        Event key = new Event();
        key.keyCode = keyCode;
        key.stateMask = stateMask;
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
