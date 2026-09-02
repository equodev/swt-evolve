package org.eclipse.swt.widgets;

import dev.equo.swt.harness.WidgetFlutterHarness;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * A popup {@code Menu} reaches the client as an entry in {@code Display}'s popup set, and that entry
 * is what makes the menu exist in the rendered tree. Natively the platform owns a shown menu — it
 * stays up on its own once shown, and taking it down is what makes it stop being visible — so both
 * halves of that contract have to be honoured explicitly here:
 *
 * <ul>
 * <li>showing must not consume the set, or the menu is torn down the moment it opens;
 * <li>the client's notice that it was hidden must reach the same state a platform dismissal does,
 *     or the menu keeps advertising itself as visible and is shown straight back.
 * </ul>
 *
 * <pre>./gradlew :swt-evolve:swt_native:nativeTest</pre>
 */
@Tag("flutter-it")
class PopupMenuVisibilityFlutterTest {

    private WidgetFlutterHarness flutter;
    private Display display;
    private Shell shell;

    @BeforeEach
    void setUp() {
        flutter = new WidgetFlutterHarness();
        flutter.init(); // wire the harness as the global bridge BEFORE creating widgets
        display = new Display();
        shell = new Shell(display);
        shell.setLayout(new FillLayout());
        shell.setSize(400, 300);
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed()) display.dispose();
        if (flutter != null) flutter.teardown();
    }

    /** The shape a preference page builds: an ARROW button showing a popup of special keys. */
    private Menu specialKeysPopup() {
        Button arrow = new Button(shell, SWT.ARROW | SWT.LEFT);
        Menu menu = new Menu(arrow);
        for (String key : new String[] {"⌫", "Tab", "⌦", "⇧Tab"}) {
            new MenuItem(menu, SWT.PUSH).setText(key);
        }
        return menu;
    }

    @Test
    void showingThePopupDoesNotTakeItBackDown() {
        Menu menu = specialKeysPopup();
        DartDisplay impl = (DartDisplay) display.getImpl();

        menu.setVisible(true);
        assertThat(impl.popups).isNotNull().contains(menu);

        // The event loop shows every queued popup — one turn of exactly what happens next.
        impl.runPopups();

        assertThat(((DartMenu) menu.getImpl())._visible())
                .as("nothing asked the menu to hide")
                .isTrue();
        assertThat(impl.popups)
                .as("a popup that is up must stay in the set that keeps it rendered")
                .isNotNull()
                .contains(menu);
    }

    @Test
    void hidingItFromTheClientStopsItBeingAdvertisedAsVisible() {
        Menu menu = specialKeysPopup();
        DartDisplay impl = (DartDisplay) display.getImpl();

        menu.setVisible(true);
        impl.runPopups();
        assertThat(impl.popups).contains(menu);

        // The client hid the popup: an item was picked, or a click landed outside it.
        MenuHelper.onClientHid((DartMenu) menu.getImpl(), new Event());

        assertThat(((DartMenu) menu.getImpl())._visible())
                .as("a dismissed popup must stop reporting itself visible, or it is shown again")
                .isFalse();
        assertThat(impl.popups)
                .as("and it must leave the set that keeps it rendered")
                .doesNotContain(menu);
    }
}
