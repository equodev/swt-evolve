package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.*;

import java.io.InputStream;

public class DropDownToolbarExample {

    public static void main(String[] args) {
        Config.forceEquo();
        Config.useEclipse(Menu.class);
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("Toolbar DropDown Example");
        shell.setSize(400, 200);
        shell.setLayout(new FillLayout());

        Composite composite = new Composite(shell, SWT.NONE);
        composite.setLayout(new FillLayout(SWT.HORIZONTAL));

        ToolBar toolBar = new ToolBar(composite, SWT.FLAT | SWT.RIGHT);

        ToolItem dropDownItem = new ToolItem(toolBar, SWT.DROP_DOWN);
        //dropDownItem.setText("AA");
        InputStream image = DropDownToolbarExample.class.getResourceAsStream("/synced.png");
        dropDownItem.setImage(new Image(display, image));

        Menu dropDownMenu = new Menu(shell, SWT.POP_UP);

        MenuItem optionA = new MenuItem(dropDownMenu, SWT.PUSH);
        optionA.setText("Op A");
        optionA.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                System.out.println("Op A");
            }
        });

        MenuItem optionB = new MenuItem(dropDownMenu, SWT.PUSH);
        optionB.setText("Op B");
        optionB.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                System.out.println("Op B ");
            }
        });

        dropDownItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (e.detail == SWT.ARROW) {
                    ToolItem item = (ToolItem) e.widget;
                    ToolBar bar = item.getParent();
                    Rectangle rect = item.getBounds();
                    Point pt = bar.toDisplay(new Point(rect.x, rect.y + rect.height));
                    dropDownMenu.setLocation(pt);
                    dropDownMenu.setVisible(true);
                } else {
                    System.out.println("hello");
                }
            }
        });

        shell.open();

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }
}

