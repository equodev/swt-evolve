package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.ConfigFlags;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.NoopBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * With every menu closed, pressing a menu-bar item's accelerator while a control of its shell holds
 * the focus fires {@link SWT#Selection} on the item, and the control does not get the key.
 *
 * <pre>./gradlew :swt-evolve:swt_native:test --tests '*MenuAcceleratorFlutterTest'</pre>
 */
@Tag("flutter-it")
class MenuAcceleratorFlutterTest {

    private Display display;
    private KeyedWebBridge web;
    private Shell shell;
    private Menu bar;
    private Text text;
    private Menu fileMenu;
    private MenuItem newItem;
    private final List<String> events = new ArrayList<>();
    private final List<Event> selections = new ArrayList<>();

    @BeforeEach
    void open() {
        install();
        shell = new Shell(display);
        shell.setLayout(new FillLayout());
        bar = new Menu(shell, SWT.BAR);
        shell.setMenuBar(bar);
        fileMenu = cascade(bar, "&File");
        newItem = item(fileMenu, SWT.PUSH, "&New", SWT.MOD1 | 'N');
        item(cascade(bar, "&Edit"), SWT.PUSH, "&Copy Path", SWT.MOD1 | SWT.MOD2 | 'C');
        text = new Text(shell, SWT.BORDER | SWT.MULTI);
        text.addListener(SWT.KeyDown, e -> events.add("Text KeyDown " + (char) e.keyCode));
        shell.setSize(320, 200);
        shell.open();
        text.setFocus();
        drain();
        events.clear();
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed())
            display.dispose();
        FlutterBridge.set(null);
    }

    private Menu cascade(Menu parent, String title) {
        MenuItem cascade = new MenuItem(parent, SWT.CASCADE);
        cascade.setText(title);
        Menu menu = new Menu(shell, SWT.DROP_DOWN);
        cascade.setMenu(menu);
        return menu;
    }

    private MenuItem item(Menu menu, int style, String name, int accelerator) {
        MenuItem item = new MenuItem(menu, style);
        item.setText(name);
        item.setAccelerator(accelerator);
        item.addListener(SWT.Selection, e -> {
            events.add("Selection " + name);
            selections.add(e);
        });
        return item;
    }

    /** A key as the client posts it on the Display key channel: the lower-case key code and the modifiers held. */
    private void press(int stateMask, char key) {
        web.comm.fireContaining("Display/" + display.hashCode() + "/Key/KeyDown", key(stateMask, key));
        drain();
    }

    /** A key a focused editor forwards on its own channel instead. */
    private void pressInText(int stateMask, char key) {
        ControlHelper.sendFlutterKeyDown((DartWidget) text.getImpl(), key(stateMask, key));
        drain();
    }

    private static Event key(int stateMask, char key) {
        Event e = new Event();
        e.keyCode = key;
        e.character = key;
        e.stateMask = stateMask;
        e.time = 4242;
        return e;
    }

    @Test
    @DisplayName("MOD1+N fires Selection on New and the focused Text does not get the key")
    void mod1N() {
        press(SWT.MOD1, 'n');

        assertThat(events).containsExactly("Selection &New");
    }

    @Test
    @DisplayName("MOD1+Shift+C fires Selection on Copy Path, not on New")
    void mod1ShiftC() {
        press(SWT.MOD1 | SWT.SHIFT, 'c');

        assertThat(events).containsExactly("Selection &Copy Path");
    }

    @Test
    @DisplayName("the Selection carries the modifiers held and the key's time, as a native accelerator's does")
    void selectionCarriesTheKeyState() {
        press(SWT.MOD1 | SWT.SHIFT, 'c');

        assertThat(selections).hasSize(1);
        assertThat(selections.get(0).stateMask).isEqualTo(SWT.MOD1 | SWT.SHIFT);
        assertThat(selections.get(0).time).isEqualTo(4242);
    }

    @Test
    @DisplayName("a key a focused editor forwards itself takes the accelerator too")
    void editorForwardedKey() {
        pressInText(SWT.MOD1, 'n');

        assertThat(events).containsExactly("Selection &New");
    }

    @Test
    @DisplayName("the accelerator's key without its modifiers is an ordinary key")
    void plainKeyReachesTheText() {
        press(0, 'n');
        press(SWT.MOD1, 'c');

        assertThat(events).containsExactly("Text KeyDown n", "Text KeyDown c");
    }

    @Test
    @DisplayName("a disabled item does not take its accelerator")
    void disabledItem() {
        newItem.setEnabled(false);

        press(SWT.MOD1, 'n');

        assertThat(events).containsExactly("Text KeyDown n");
    }

    @Test
    @DisplayName("an item in a disabled menu does not take its accelerator")
    void itemInDisabledMenu() {
        fileMenu.setEnabled(false);

        press(SWT.MOD1, 'n');

        assertThat(events).containsExactly("Text KeyDown n");
    }

    @Test
    @DisplayName("an item under a disabled cascade does not take its accelerator")
    void itemUnderDisabledCascade() {
        fileMenu.getParentItem().setEnabled(false);

        press(SWT.MOD1, 'n');

        assertThat(events).containsExactly("Text KeyDown n");
    }

    @Test
    @DisplayName("a disabled menu bar takes no accelerator")
    void disabledBar() {
        bar.setEnabled(false);

        press(SWT.MOD1, 'n');

        assertThat(events).containsExactly("Text KeyDown n");
    }

    @Test
    @DisplayName("a check item flips, then tells its Selection listeners")
    void checkItemFlips() {
        MenuItem wrap = item(fileMenu, SWT.CHECK, "&Wrap", SWT.MOD1 | 'W');

        press(SWT.MOD1, 'w');

        assertThat(wrap.getSelection()).isTrue();
        assertThat(events).containsExactly("Selection &Wrap");
    }

    @Test
    @DisplayName("a radio item selects itself in its group; the deselected one is told first")
    void radioItemSelectsItsGroup() {
        Menu view = cascade(bar, "&View");
        MenuItem list = item(view, SWT.RADIO, "&List", SWT.MOD1 | '1');
        MenuItem details = item(view, SWT.RADIO, "&Details", SWT.MOD1 | '2');
        list.setSelection(true);

        press(SWT.MOD1, '2');

        assertThat(list.getSelection()).isFalse();
        assertThat(details.getSelection()).isTrue();
        assertThat(events).containsExactly("Selection &List", "Selection &Details");
    }

    @Test
    @DisplayName("a radio item in a NO_RADIO_GROUP menu toggles on its own")
    void radioItemWithoutGroup() {
        MenuItem cascade = new MenuItem(bar, SWT.CASCADE);
        Menu view = new Menu(shell, SWT.DROP_DOWN | SWT.NO_RADIO_GROUP);
        cascade.setMenu(view);
        MenuItem list = item(view, SWT.RADIO, "&List", SWT.MOD1 | '1');
        MenuItem details = item(view, SWT.RADIO, "&Details", SWT.MOD1 | '2');
        list.setSelection(true);

        press(SWT.MOD1, '2');

        assertThat(list.getSelection()).isTrue();
        assertThat(details.getSelection()).isTrue();
        assertThat(events).containsExactly("Selection &Details");

        press(SWT.MOD1, '2');

        assertThat(details.getSelection()).isFalse();
    }

    @Test
    @DisplayName("another shell's menu bar does not take the key")
    void otherShell() {
        Text field = openDialog();

        press(SWT.MOD1, 'n');

        assertThat(events).containsExactly("Field KeyDown n");
        assertThat(display.getFocusControl()).isSameAs(field);
    }

    @Test
    @DisplayName("on Cocoa a shell without a menu bar uses the application's")
    void cocoaShellWithoutBarUsesTheApplicationBar() {
        assumeTrue("cocoa".equals(SWT.getPlatform()));
        withSystemMenuBar(() -> {
            item(cascade(display.getMenuBar(), "&App"), SWT.PUSH, "&Settings", SWT.MOD1 | ',');
            openDialog();

            press(SWT.MOD1, ',');

            assertThat(events).containsExactly("Selection &Settings");
        });
    }

    private Text openDialog() {
        Shell dialog = new Shell(shell, SWT.DIALOG_TRIM);
        dialog.setLayout(new FillLayout());
        Text field = new Text(dialog, SWT.BORDER);
        field.addListener(SWT.KeyDown, e -> events.add("Field KeyDown " + (char) e.keyCode));
        dialog.open();
        field.setFocus();
        drain();
        events.clear();
        return field;
    }

    private static void withSystemMenuBar(Runnable body) {
        ConfigFlags saved = Config.getConfigFlags();
        String override = System.getProperty("dev.equo.swt.systemMenuBar");
        System.setProperty("dev.equo.swt.systemMenuBar", "true");
        Config.setConfigFlags(null);
        try {
            body.run();
        } finally {
            if (override == null)
                System.clearProperty("dev.equo.swt.systemMenuBar");
            else
                System.setProperty("dev.equo.swt.systemMenuBar", override);
            Config.setConfigFlags(saved);
        }
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

    /** Also wires the Display key channel, where the client posts every key no editor forwards itself. */
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
