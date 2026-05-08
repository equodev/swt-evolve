package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

public class CursorHoverExample {

    public static void main(String[] args) {
        Config.forceEclipse();
        Config.defaultToEclipse();
        Display display = new Display();
        Shell shell = new Shell(display);

        shell.setText("SWT Hover Example");
        shell.setSize(600, 250);
        shell.setLayout(new GridLayout());

        Composite container = new Composite(shell, SWT.NONE);
        container.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        container.setLayout(new GridLayout(2, false));

        // Simula un item clickeable
        Composite item = new Composite(container, SWT.BORDER);
        item.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        item.setLayout(new GridLayout(2, false));

        // Cursor tipo "mano"
        Cursor handCursor = display.getSystemCursor(SWT.CURSOR_HAND);
        item.setCursor(handCursor);

        Label icon = new Label(item, SWT.NONE);
        icon.setText("📁");
        //icon.setCursor(handCursor);

        Label text = new Label(item, SWT.NONE);
        text.setText("Proyecto de ejemplo");
        //text.setCursor(handCursor);

        item.addMouseTrackListener(new org.eclipse.swt.events.MouseTrackAdapter() {
            @Override
            public void mouseEnter(org.eclipse.swt.events.MouseEvent e) {
                item.setBackground(display.getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
                icon.setBackground(display.getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
                text.setBackground(display.getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
            }

            @Override
            public void mouseExit(org.eclipse.swt.events.MouseEvent e) {
                item.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
                icon.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
                text.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
            }
        });

        // Composite con CTabFolder
        Composite tabContainer = new Composite(container, SWT.NONE);
        tabContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        tabContainer.setLayout(new GridLayout());

        CTabFolder tabFolder = new CTabFolder(tabContainer, SWT.BORDER);
        tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Image tabImage = display.getSystemImage(SWT.ICON_INFORMATION);
        CTabItem tab1 = new CTabItem(tabFolder, SWT.NONE);
        tab1.setText("tab1");
        tab1.setImage(tabImage);

        tabFolder.setSelection(0);

        shell.open();

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }

        display.dispose();
    }
}