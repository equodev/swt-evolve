package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

public class CompositeButtonSnippet {
    public static void main(String[] args) {
        Config.useEquo(Button.class);
        Config.useEquo(Label.class);

        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("Shell with Composite and Checkbox Example");
        shell.setSize(400, 300);
        
        // Configure shell layout
        shell.setLayout(new FillLayout());
        
        // Set shell background color
        shell.setBackground(display.getSystemColor(SWT.COLOR_RED));
        
        // Create a composite inside the shell
        Composite composite = new Composite(shell, SWT.BORDER);
        composite.setLayout(new GridLayout(1, false));
        
        // Set composite background color
        composite.setBackground(display.getSystemColor(SWT.COLOR_MAGENTA));
        composite.setBackgroundMode(SWT.INHERIT_FORCE);

        // Add an explanatory label
        Label label = new Label(composite, SWT.NONE);
        label.setText("This is a Label inside a Composite inside a Shell:");
        label.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
        
        // Create a checkbox inside the composite
        Button checkbox = new Button(composite, SWT.CHECK);
        checkbox.setText("Check this option!");
        checkbox.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
        
        // Set checkbox background color explicitly
        checkbox.setBackground(display.getSystemColor(SWT.COLOR_YELLOW));
        
        // Print background colors of all components
        Color shellColor = shell.getBackground();
        Color compositeColor = composite.getBackground();
        Color checkboxColor = checkbox.getBackground();
        
        System.out.println("Shell background color: " + shellColor);
        System.out.println("Composite background color: " + compositeColor);
        System.out.println("Checkbox background color: " + checkboxColor);

        // Add listener to checkbox
        checkbox.addSelectionListener(widgetSelectedAdapter(e -> {
            boolean isSelected = checkbox.getSelection();
            System.out.println("Checkbox inside the Composite was " + (isSelected ? "checked" : "unchecked") + "!");
            MessageBox messageBox = new MessageBox(shell, SWT.ICON_INFORMATION);
            messageBox.setText("Information");
            messageBox.setMessage("Checkbox " + (isSelected ? "checked" : "unchecked") + " successfully!");
            messageBox.open();
        }));
        
        // Open the shell
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }
}