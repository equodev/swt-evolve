package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * A bordered CTabFolder with several tabs, drawn the way an Eclipse part stack configures it.
 *
 * <p>Native SWT draws one outline: it runs around the whole folder, the tab strip included, climbs
 * the selected tab's sides and top, and goes on along the strip's bottom edge on both sides of it.
 * The selected tab is open towards the page below, so tab, strip and frame read as one shape.
 *
 * <p>Evolve draws that outline only with {@code -Dswt.use_swt_border=true}, which this snippet sets;
 * without it a bordered folder keeps the theme's flat frame.
 */
public class CTabFolderTabOutlineSnippet {

    public static void main(String[] args) {
        System.setProperty("swt.use_swt_border", "true");
        Config.forceEquo();
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("CTabFolder tab outline");
        shell.setLayout(new FillLayout());

        CTabFolder folder = new CTabFolder(shell, SWT.BORDER | SWT.CLOSE);
        folder.setSelectionBackground(display.getSystemColor(SWT.COLOR_LIST_BACKGROUND));
        folder.setTabHeight(24);
        for (String name : new String[] {"View A", "View B", "View C"}) {
            CTabItem item = new CTabItem(folder, SWT.NONE);
            item.setText(name);
            Label page = new Label(folder, SWT.NONE);
            page.setText(name + " content");
            page.setBackground(display.getSystemColor(SWT.COLOR_LIST_BACKGROUND));
            item.setControl(page);
        }
        folder.setSelection(0);

        shell.setSize(360, 160);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
    }
}
