package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class NestedCompositeExample {

    public static void main(String[] args) {
        Config.forceEquo();
//        Config.forceEclipse();
        System.setProperty("dev.equo.swt.Composite", "equo");
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("SWT Nested Composite Example");
        shell.setSize(300, 200);
        shell.setLayout(new FillLayout());

        // Composite padre
        Composite parentComposite = new Composite(shell, SWT.BORDER);
        parentComposite.setLayout(new RowLayout(SWT.VERTICAL));
        parentComposite.setBackground(display.getSystemColor(SWT.COLOR_WHITE));

        // Botón 1 (en el composite padre)
        Button button1 = new Button(parentComposite, SWT.PUSH);
        button1.setText("Button1");
        button1.addListener(SWT.Selection, e -> {
            System.out.println("button1");
        });

        // Composite hijo dentro del padre
        Composite childComposite = new Composite(parentComposite, SWT.BORDER);
        childComposite.setLayout(new RowLayout());

        // Botón 2 (dentro del composite hijo)
        Button button2 = new Button(childComposite, SWT.PUSH);
        button2.setText("Button2");
        button2.addListener(SWT.Selection, e -> {
            System.out.println("button2");
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