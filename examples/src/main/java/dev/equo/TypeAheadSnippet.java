package dev.equo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;

/**
 * Letters typed in an open menu or drop-down list.
 *
 * <pre>
 * ./gradlew :examples:runWebExample -PmainClass=dev.equo.TypeAheadSnippet
 * </pre>
 *
 * <p>Right-click the log for the menu, or open a list, and type {@code t} several times. Windows:
 * the menu cycles Tango, Tiger, tom and {@code b} activates Bravo; the lists select the next item
 * starting with the letter at once. Linux: the menu and the lists ignore letters that are no
 * mnemonic. macOS: typed letters accumulate into a prefix. Every event is logged.
 */
public class TypeAheadSnippet {

    static final String[] ITEMS = {"Alpha", "Tango", "Bravo", "Tiger", "Charlie", "tom", "Delta"};

    private static List log;

    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("TypeAheadSnippet");
        shell.setLayout(new GridLayout(2, false));

        Combo combo = new Combo(shell, SWT.READ_ONLY);
        labeled(shell, "Combo READ_ONLY", combo);
        CCombo readOnly = new CCombo(shell, SWT.READ_ONLY | SWT.BORDER);
        labeled(shell, "CCombo READ_ONLY", readOnly);
        CCombo editable = new CCombo(shell, SWT.BORDER);
        labeled(shell, "CCombo editable", editable);

        log = new List(shell, SWT.BORDER | SWT.V_SCROLL);
        log.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

        Menu menu = new Menu(log);
        for (String text : new String[]{"Alpha", "Tango", "Bravo", "Tiger", "tom"}) {
            MenuItem item = new MenuItem(menu, SWT.PUSH);
            item.setText(text);
            item.addListener(SWT.Selection, e -> log("menu Selection " + text));
        }
        log.setMenu(menu);

        shell.setSize(420, 420);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
    }

    static void labeled(Shell shell, String name, Widget widget) {
        new Label(shell, SWT.NONE).setText(name);
        if (widget instanceof Combo) {
            Combo c = (Combo) widget;
            c.setItems(ITEMS);
            c.select(0);
            c.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        } else {
            CCombo c = (CCombo) widget;
            c.setItems(ITEMS);
            c.select(0);
            c.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        }
        widget.addListener(SWT.Modify, e -> log(name + " Modify"));
        widget.addListener(SWT.Selection, e -> log(name + " Selection " + text(widget)));
        widget.addListener(SWT.DefaultSelection, e -> log(name + " DefaultSelection " + text(widget)));
    }

    static String text(Widget widget) {
        return widget instanceof Combo ? ((Combo) widget).getText() : ((CCombo) widget).getText();
    }

    static void log(String line) {
        System.out.println(line);
        log.add(line);
        log.setTopIndex(log.getItemCount() - 1);
    }
}
