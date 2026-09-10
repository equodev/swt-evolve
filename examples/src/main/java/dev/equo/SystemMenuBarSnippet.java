package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

/**
 * Exercises the Shell menu bar on the surface that has an OS-owned one.
 *
 * <pre>
 * ./gradlew :examples:runDeskExample -PmainClass=dev.equo.SystemMenuBarSnippet
 * </pre>
 *
 * <p>On macOS every menu below belongs in the system menu bar at the top of the screen, next to the
 * application menu, and nothing of it is drawn inside the window. Everything the bar has to survive
 * is here: accelerators, check and radio items, separators, a nested submenu, an item renamed and
 * disabled while the bar is installed, a whole top-level menu added after {@code open()}, and a
 * drop-down filled from its own {@code SWT.Show} listener. Every selection, show and hide is logged
 * to the list in the window, so a click in the system bar either shows up there or the round trip
 * back into SWT is broken.
 *
 * <p>Run it with {@code runWebExample} to see the other half of the same switch: in a browser tab
 * the system bar belongs to the browser, so the identical menu bar is drawn inside the window.
 */
public class SystemMenuBarSnippet {

    private static List log;

    public static void main(String[] args) {
        Config.useEquo(Menu.class);
        Config.useEquo(MenuItem.class);
        Config.useEquo(Button.class);
        Config.useEquo(Label.class);
        Config.useEquo(List.class);

        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("System menu bar");
        shell.setSize(760, 460);
        shell.setLayout(new GridLayout(1, false));

        Label hint = new Label(shell, SWT.WRAP);
        hint.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        hint.setText("system_menu_bar = " + Config.getConfigFlags().system_menu_bar
                + "\nWhen true the menus below are in the macOS bar at the top of the screen and"
                + " nowhere inside this window. Every click on one lands in the list.");

        log = new List(shell, SWT.BORDER | SWT.V_SCROLL);
        log.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Menu bar = new Menu(shell, SWT.BAR);
        shell.setMenuBar(bar);

        Menu file = cascade(shell, bar, "&File");
        push(file, "&New\tCmd+N", SWT.COMMAND | 'N');
        push(file, "&Open...\tCmd+O", SWT.COMMAND | 'O');
        new MenuItem(file, SWT.SEPARATOR);
        MenuItem renamed = push(file, "Rename me", 0);
        MenuItem disabled = push(file, "Disable me", 0);
        new MenuItem(file, SWT.SEPARATOR);
        Menu recent = cascade(shell, file, "Open &Recent");
        push(recent, "one.txt", 0);
        push(recent, "two.txt", 0);

        Menu edit = cascade(shell, bar, "&Edit");
        check(edit, "Word wrap", true);
        new MenuItem(edit, SWT.SEPARATOR);
        radio(edit, "Spaces", true);
        radio(edit, "Tabs", false);

        // Filled only when it opens, the way an application contributes to a menu lazily. If the
        // mirror copied the tree once instead of subscribing to it, this menu would stay empty.
        Menu lazy = cascade(shell, bar, "&Lazy");
        lazy.addListener(SWT.Show, e -> {
            record("Show: Lazy");
            for (MenuItem item : lazy.getItems()) item.dispose();
            push(lazy, "Filled at " + System.currentTimeMillis() % 100000, 0);
        });
        lazy.addListener(SWT.Hide, e -> record("Hide: Lazy"));

        Button rename = new Button(shell, SWT.PUSH);
        rename.setText("Rename / disable File items");
        rename.addSelectionListener(widgetSelectedAdapter(e -> {
            renamed.setText("Renamed at " + System.currentTimeMillis() % 100000);
            disabled.setEnabled(!disabled.getEnabled());
            record("File items updated; disabled=" + !disabled.getEnabled());
        }));

        Button add = new Button(shell, SWT.PUSH);
        add.setText("Add a top-level Help menu");
        add.addSelectionListener(widgetSelectedAdapter(e -> {
            Menu help = cascade(shell, bar, "&Help");
            push(help, "About", 0);
            record("Help menu added");
        }));

        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        display.dispose();
    }

    private static Menu cascade(Shell shell, Menu parent, String text) {
        MenuItem item = new MenuItem(parent, SWT.CASCADE);
        item.setText(text);
        Menu menu = new Menu(shell, SWT.DROP_DOWN);
        item.setMenu(menu);
        return menu;
    }

    private static MenuItem push(Menu menu, String text, int accelerator) {
        MenuItem item = new MenuItem(menu, SWT.PUSH);
        item.setText(text);
        if (accelerator != 0)
            item.setAccelerator(accelerator);
        item.addSelectionListener(widgetSelectedAdapter(e -> record("Selection: " + item.getText())));
        return item;
    }

    private static void check(Menu menu, String text, boolean selected) {
        MenuItem item = new MenuItem(menu, SWT.CHECK);
        item.setText(text);
        item.setSelection(selected);
        item.addSelectionListener(widgetSelectedAdapter(
                e -> record("Selection: " + item.getText() + " checked=" + item.getSelection())));
    }

    private static void radio(Menu menu, String text, boolean selected) {
        MenuItem item = new MenuItem(menu, SWT.RADIO);
        item.setText(text);
        item.setSelection(selected);
        item.addSelectionListener(widgetSelectedAdapter(
                e -> record("Selection: " + item.getText() + " on=" + item.getSelection())));
    }

    private static void record(String message) {
        System.out.println(message);
        if (log != null && !log.isDisposed()) {
            log.add(message);
            log.setTopIndex(log.getItemCount() - 1);
        }
    }
}
