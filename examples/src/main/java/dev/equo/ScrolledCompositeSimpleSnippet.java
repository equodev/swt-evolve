package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.*;

/**
 * Simple demonstration of SWT ScrolledComposite widget.
 * Creates a ScrolledComposite with a large button that triggers horizontal and vertical scrollbars.
 */

public class ScrolledCompositeSimpleSnippet {
    public static void main(String[] args) {
        //Config.forceEquo();
        Config.useEquo(ScrolledComposite.class);
        Config.useEquo(Button.class);

        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("ScrolledComposite Example");
        shell.setLayout(new FillLayout());
        shell.setSize(300, 300);

        // Create ScrolledComposite with ONLY one instance for simplicity
        ScrolledComposite sc = new ScrolledComposite(shell, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);

        // Create a large button (400x400) that should trigger scrollbars
        Button button = new Button(sc, SWT.PUSH);
        button.setText("Big Button 400x400");
        button.setSize(400, 400);

        // Set content
        sc.setContent(button);

        // Set minimum size (button size) and enable expansion
        sc.setMinSize(400, 400);
        sc.setExpandHorizontal(true);
        sc.setExpandVertical(true);

        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }
}