package dev.equo.swing;

import dev.equo.swt.Config;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Window;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * Two embedded Swing toolbars whose button opens a JPopupMenu right under it. The top one sits in a
 * strip only as tall as the toolbar, so the menu does not fit inside the embedded frame; the bottom
 * one has room for it. Both menus must open under their button, drawn inside the SWT window, and
 * picking an item prints it. The console reports, for each menu, the window that hosts it and its
 * screen location against the button's.
 */
public class SwingToolbarPopupSnippet {

    public static void main(String[] args) {
        Config.forceEquo();
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("Swing toolbar pull-down");
        shell.setLayout(new GridLayout(1, false));

        new Label(shell, SWT.NONE).setText("Toolbar in a strip as tall as the toolbar:");
        Composite strip = new Composite(shell, SWT.EMBEDDED);
        GridData stripData = new GridData(SWT.FILL, SWT.TOP, true, false);
        stripData.heightHint = 40;
        strip.setLayoutData(stripData);
        toolbar(SWT_AWT.new_Frame(strip), "strip");

        new Label(shell, SWT.NONE).setText("Toolbar in a tall panel:");
        Composite tall = new Composite(shell, SWT.EMBEDDED);
        tall.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        toolbar(SWT_AWT.new_Frame(tall), "tall");

        shell.setSize(700, 500);
        shell.open();
        while (!shell.isDisposed())
            if (!display.readAndDispatch()) display.sleep();
        display.dispose();
    }

    private static void toolbar(Frame frame, String name) {
        JPanel panel = new JPanel(new BorderLayout());
        JToolBar bar = new JToolBar();
        bar.setFloatable(false);
        JButton add = new JButton("+ " + name);
        JPopupMenu menu = new JPopupMenu();
        for (String item : new String[] {"Open...", "Save...", "Close..."}) {
            menu.add(item).addActionListener(e -> System.out.println("[" + name + "] picked " + item));
        }
        add.addActionListener(e -> {
            menu.show(add, 0, add.getHeight());
            new Timer(300, t -> {
                ((Timer) t.getSource()).stop();
                report(name, add, menu);
            }).start();
        });
        bar.add(add);
        panel.add(bar, BorderLayout.NORTH);
        frame.add(panel);
        frame.validate();
    }

    private static void report(String name, Component button, JPopupMenu menu) {
        Window host = SwingUtilities.getWindowAncestor(menu);
        System.out.println("[" + name + "] button on screen " + button.getLocationOnScreen()
                + " | menu on screen " + (menu.isShowing() ? menu.getLocationOnScreen() : "not showing")
                + " | menu host " + (host == null ? "none" : host.getClass().getName() + " " + host.getBounds()));
    }
}
