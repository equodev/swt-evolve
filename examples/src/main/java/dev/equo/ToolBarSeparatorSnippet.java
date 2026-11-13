package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

/**
 * A horizontal and a vertical {@code ToolBar} whose item groups are split by {@code SWT.SEPARATOR}
 * items.
 *
 * <pre>
 * ./gradlew :examples:runWebExample -PmainClass=dev.equo.ToolBarSeparatorSnippet
 * </pre>
 *
 * <p>What to look at: each separator draws a thin line between its neighbours, as native SWT does.
 * With {@code -Dswt.evolve.bigger_icon_size=true} it is a gap with no line.
 */
public class ToolBarSeparatorSnippet {
    public static void main(String[] args) {
        Config.useEquo(ToolBar.class);
        Config.useEquo(ToolItem.class);
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("ToolBar separators");
        shell.setLayout(new GridLayout(1, false));

        ToolBar horizontal = new ToolBar(shell, SWT.HORIZONTAL | SWT.FLAT);
        fill(horizontal);
        horizontal.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));

        ToolBar vertical = new ToolBar(shell, SWT.VERTICAL | SWT.FLAT);
        fill(vertical);
        vertical.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));

        shell.setSize(360, 260);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
    }

    private static void fill(ToolBar bar) {
        item(bar, "Cut");
        item(bar, "Copy");
        new ToolItem(bar, SWT.SEPARATOR);
        item(bar, "Paste");
        new ToolItem(bar, SWT.SEPARATOR);
        item(bar, "Undo");
    }

    private static void item(ToolBar bar, String text) {
        ToolItem item = new ToolItem(bar, SWT.PUSH);
        item.setText(text);
    }
}
