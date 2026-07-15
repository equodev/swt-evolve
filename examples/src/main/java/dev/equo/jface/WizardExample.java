package dev.equo.jface;

import dev.equo.swt.CrashReporter;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class WizardExample {

    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("SWT Wizard Example");
        shell.setSize(400, 200);
        shell.setLayout(new FillLayout());

        Button openWizardButton = new Button(shell, SWT.PUSH);
        openWizardButton.setText("Open Wizard");

        openWizardButton.addListener(SWT.Selection, e -> {
            MyWizard wizard = new MyWizard();
            WizardDialog dialog = new WizardDialog(shell, wizard);
            dialog.open();
        });

        Button crashButton = new Button(shell, SWT.PUSH);
        crashButton.setText("Test Crash Dialog");
        crashButton.addListener(SWT.Selection, e ->
                CrashReporter.handleCrash(new RuntimeException("test crash")));

        shell.open();

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }

        display.dispose();
    }

    private static class MyWizard extends Wizard {

        public MyWizard() {
            setWindowTitle("My Wizard");
        }

        @Override
        public void addPages() {
            addPage(new MyWizardPage());
        }

        @Override
        public boolean performFinish() {
            System.out.println("Finish pressed");
            return true;
        }
    }

    private static class MyWizardPage extends WizardPage {

        protected MyWizardPage() {
            super("page1");
            setTitle("Example Wizard");
            setDescription("This is a simple wizard page.");
        }

        @Override
        public void createControl(Composite parent) {
            Composite container = new Composite(parent, SWT.NONE);
            container.setLayout(new FillLayout());

            new Label(container, SWT.NONE).setText("Hello");

            setControl(container);
        }
    }
}
