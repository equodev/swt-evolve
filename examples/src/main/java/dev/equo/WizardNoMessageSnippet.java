package dev.equo;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/** A wizard whose page has a title but no description or message. */
public class WizardNoMessageSnippet {

    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        Wizard wizard = new Wizard() {
            @Override
            public void addPages() {
                addPage(new WizardPage("page") {
                    {
                        setTitle("Page Without Message");
                    }

                    @Override
                    public void createControl(Composite parent) {
                        Composite container = new Composite(parent, SWT.NONE);
                        container.setLayout(new FillLayout());
                        new Label(container, SWT.NONE).setText("Content");
                        setControl(container);
                    }
                });
            }

            @Override
            public boolean performFinish() {
                return true;
            }
        };
        wizard.setWindowTitle("Wizard");
        new WizardDialog(shell, wizard).open();
        display.dispose();
    }
}
