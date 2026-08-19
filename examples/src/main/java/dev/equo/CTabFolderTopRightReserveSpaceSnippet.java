package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

/**
 * With several tabs open and a narrow window, the topRight toolbar floats on top of
 * the tab row and can hide tabs underneath it with no way to reach them. Default
 * behaviour, unchanged. Pass -Dswt.evolve.ctabfolder_topright_auto_hide=false to
 * compare against the fix (toolbar always shown, reserving its own space instead of
 * floating).
 */
public class CTabFolderTopRightReserveSpaceSnippet {

    public static void main(String[] args) {
        Config.forceEquo();
        // Forced on for both runs of this snippet: makes the topRight toolbar
        // render without needing a live mouse hover even in the default (hover-only)
        // mode. ctabfolder_topright_auto_hide is the actual variable under
        // comparison -- pass -Dswt.evolve.ctabfolder_topright_auto_hide=false to
        // see the fix.
        System.setProperty("swt.evolve.ctabfolder_visible_controls", "true");
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("CTabFolder TopRight Reserve-Space Snippet -- auto_hide="
                + Boolean.parseBoolean(System.getProperty("swt.evolve.ctabfolder_topright_auto_hide", "true")));
        shell.setLayout(new GridLayout());
        final Point pinnedSize = new Point(300, 250);
        shell.setSize(pinnedSize);
        // The desktop harness's DPI/monitor negotiation drifts the shell size upward
        // over the first couple seconds if left alone -- pin it back on every resize
        // so the narrow-window scenario this snippet exists to show actually holds.
        shell.addControlListener(new ControlAdapter() {
            @Override
            public void controlResized(ControlEvent e) {
                if (!shell.getSize().equals(pinnedSize)) {
                    shell.setSize(pinnedSize);
                }
            }
        });

        CTabFolder tabFolder = new CTabFolder(shell, SWT.BORDER);
        tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Composite toolbarComposite = new Composite(tabFolder, SWT.NONE);
        FillLayout toolbarLayout = new FillLayout();
        toolbarLayout.marginHeight = 2;
        toolbarLayout.marginWidth = 4;
        toolbarComposite.setLayout(toolbarLayout);

        ToolBar toolBar = new ToolBar(toolbarComposite, SWT.FLAT);
        ToolItem itemA = new ToolItem(toolBar, SWT.PUSH);
        itemA.setText("Refresh");
        ToolItem itemB = new ToolItem(toolBar, SWT.PUSH);
        itemB.setText("Settings");

        tabFolder.setTopRight(toolbarComposite);

        for (int i = 1; i <= 8; i++) {
            CTabItem tab = new CTabItem(tabFolder, SWT.NONE);
            tab.setText("Tab " + i);
            Composite body = new Composite(tabFolder, SWT.NONE);
            body.setLayout(new FillLayout());
            new Label(body, SWT.NONE).setText("Content of tab " + i);
            tab.setControl(body);
        }

        // Selects the last tab -- the one most likely to end up hidden underneath
        // the toolbar overlay when the tabs don't all fit.
        tabFolder.setSelection(tabFolder.getItemCount() - 1);

        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }
}
