package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public class SashSnippet {

    public static void main (String [] args) {
        Config.forceEquo();
        Display display = new Display ();
        Shell shell = new Shell (display);
        shell.setText("Snippet 54");
        shell.setSize(400, 300);

        final Composite container = new Composite(shell, SWT.NONE);
        //container.setBackground(display.getSystemColor(SWT.COLOR_RED));
        // Make the Composite a bit smaller than the Shell and center it
        Rectangle clientArea = shell.getClientArea();
        int offset = 40;
        int compositeWidth = clientArea.width - offset;
        int compositeHeight = clientArea.height - offset;
        int leftMargin = (clientArea.width - compositeWidth) / 2;
        int topMargin = (clientArea.height - compositeHeight) / 2;
        container.setBounds(clientArea.x + leftMargin, clientArea.y + topMargin, compositeWidth, compositeHeight);

        // Create the Sash as a child of the Composite
        //VERTICAL
        final Sash sash = new Sash(container, SWT.BORDER | SWT.VERTICAL);
        sash.setBounds((compositeWidth)/2, 0, 10, compositeHeight);
        //HORIZONTAL
        //final Sash sash = new Sash(container, SWT.BORDER | SWT.HORIZONTAL);
        //sash.setBounds(0, (compositeHeight)/2, compositeWidth, 10);

        sash.setBackground(display.getSystemColor(SWT.COLOR_BLUE));
        sash.addListener (SWT.Selection, e -> sash.setBounds (e.x, e.y, e.width, e.height));

        shell.open();
        sash.setFocus ();
        while (!shell.isDisposed ()) {
            if (!display.readAndDispatch ()) display.sleep ();
        }
        display.dispose ();
    }
}
