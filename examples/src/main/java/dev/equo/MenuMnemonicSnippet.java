package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.e4.ui.workbench.renderers.swt.TrimmedPartLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

/**
 * Mnemonic letters typed in an open menu.
 *
 * <pre>
 * ./gradlew :examples:runWebExample -PmainClass=dev.equo.MenuMnemonicSnippet
 * </pre>
 *
 * <p>Open the File menu (Alt+F, or a click), or right-click the list, and type a letter. On Windows
 * and Linux the item whose underlined letter it is gets selected and the menu closes: {@code n} for
 * New, {@code t} opens Recent and {@code f} then picks First. {@code s} is shared by Save and
 * Search, so it moves between them and Enter picks one. On macOS menus have no mnemonics; typing
 * jumps to the item whose title starts with the letters typed. Every selection is logged to the
 * list.
 *
 * <p>Toggle {@code Config.useEclipse(...)} for the native behaviour on the same OS.
 */
public class MenuMnemonicSnippet {

    private static List log;

    public static void main(String[] args) {
        Config.useEquo(Menu.class);
        Config.useEquo(MenuItem.class);
        Config.useEquo(Label.class);
        Config.useEquo(List.class);
        Config.useEquo(ToolBar.class);
        Config.useEquo(ToolItem.class);

        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("Menu mnemonics");
        shell.setSize(560, 400);
        TrimmedPartLayout trim = new TrimmedPartLayout();
        shell.setLayout(trim);

        // In a browser the menu bar is drawn in the main toolbar, the shell's TOP trim.
        Composite mainToolbar = trim.getTrimComposite(shell, SWT.TOP);
        mainToolbar.setLayout(new RowLayout(SWT.HORIZONTAL));
        new ToolItem(new ToolBar(mainToolbar, SWT.FLAT), SWT.PUSH).setText("Tool");

        Composite body = trim.getClientArea(shell);
        body.setLayout(new GridLayout(1, false));

        Label hint = new Label(body, SWT.WRAP);
        hint.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        hint.setText("Open File, or right-click this text or the list, and type an item's mnemonic letter.");

        log = new List(body, SWT.BORDER | SWT.V_SCROLL);
        log.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Menu bar = new Menu(shell, SWT.BAR);
        shell.setMenuBar(bar);
        MenuItem file = new MenuItem(bar, SWT.CASCADE);
        file.setText("&File");
        Menu fileMenu = new Menu(shell, SWT.DROP_DOWN);
        file.setMenu(fileMenu);
        fill(fileMenu);

        Menu context = new Menu(log);
        fill(context);
        log.setMenu(context);
        hint.setMenu(context);

        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
    }

    private static void fill(Menu menu) {
        item(menu, SWT.PUSH, "&New\tCtrl+N", "New");
        item(menu, SWT.PUSH, "&Open...", "Open");
        item(menu, SWT.PUSH, "&Save", "Save");
        item(menu, SWT.CHECK, "&Search", "Search");
        item(menu, SWT.PUSH, "&Print", "Print").setEnabled(false);
        MenuItem recent = item(menu, SWT.CASCADE, "Recen&t", "Recent");
        Menu recentMenu = new Menu(menu.getShell(), SWT.DROP_DOWN);
        recent.setMenu(recentMenu);
        item(recentMenu, SWT.PUSH, "&First", "First");
        item(recentMenu, SWT.PUSH, "Se&cond", "Second");
    }

    private static MenuItem item(Menu menu, int style, String text, String label) {
        MenuItem item = new MenuItem(menu, style);
        item.setText(text);
        if (style != SWT.CASCADE) {
            item.addListener(SWT.Selection, e -> {
                System.out.println("Selected: " + label);
                log.add("Selected: " + label);
            });
        }
        return item;
    }
}
