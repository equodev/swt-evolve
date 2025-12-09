package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swt.widgets.Shell;

public class SashHorizontalSnippet {

    public static void main (String [] args) {
        Config.forceEquo();
        Display display = new Display ();
        Shell shell = new Shell (display);
        shell.setText("Snippet Sash Horizontal");
        shell.setSize(400, 300);

        final Composite container = new Composite(shell, SWT.NONE);

        // Make the Composite a bit smaller than the Shell and center it
        Rectangle clientArea = shell.getClientArea();
        int offset = 40;
        int compositeWidth = clientArea.width - offset;
        int compositeHeight = clientArea.height - offset;
        int leftMargin = (clientArea.width - compositeWidth) / 2;
        int topMargin = (clientArea.height - compositeHeight) / 2;
        container.setBounds(
                clientArea.x + leftMargin,
                clientArea.y + topMargin,
                compositeWidth,
                compositeHeight
        );

        // Create top composite (above the sash)
        final Composite topComposite = new Composite(container, SWT.BORDER);
        topComposite.setBounds(0, 0, compositeWidth, compositeHeight / 2 - 5);

        Button topButton = new Button(topComposite, SWT.PUSH);
        topButton.setText("Top Button");
        topButton.setBounds(10, 10, 100, 30);

        System.out.println("TOP COMPOSITE - Bounds: " + topComposite.getBounds());

        // Create the Sash as a child of the Composite
        final Sash sash = new Sash(container, SWT.BORDER | SWT.HORIZONTAL);
        sash.setBounds(0, compositeHeight / 2, compositeWidth, 10);
        Color sashColor = new Color(display, 100, 100, 100);
        sash.setBackground(sashColor);
        System.out.println("SASH - Bounds: " + sash.getBounds());
        System.out.println("SASH - Background: " + sash.getBackground());
        System.out.println("SASH - Style: " + sash.getStyle());

        // Create bottom composite (below the sash)
        final Composite bottomComposite = new Composite(container, SWT.BORDER);
        bottomComposite.setBounds(
                0,
                compositeHeight / 2 + 5,
                compositeWidth,
                compositeHeight / 2 - 5
        );

        Button bottomButton = new Button(bottomComposite, SWT.PUSH);
        bottomButton.setText("Bottom Button");
        bottomButton.setBounds(10, 10, 100, 30);

        System.out.println("BOTTOM COMPOSITE - Bounds: " + bottomComposite.getBounds());

        // Add listener to resize composites when sash moves
        sash.addListener(SWT.Selection, e -> {
            sash.setBounds(e.x, e.y, e.width, e.height);
            topComposite.setBounds(0, 0, compositeWidth, e.y);
            int bottomY = e.y + e.height;
            int bottomHeight = compositeHeight - bottomY;
            bottomComposite.setBounds(0, bottomY, compositeWidth, bottomHeight);
        });


        System.out.println("\nCONTAINER CHILDREN COUNT: " + container.getChildren().length);
        System.out.println("CONTAINER CHILDREN:");
        for (int i = 0; i < container.getChildren().length; i++) {
            System.out.println("  [" + i + "] " + container.getChildren()[i].getClass().getSimpleName());
        }

        shell.open();
        sash.setFocus();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
    }
}
