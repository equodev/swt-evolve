package dev.equo;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * The dialog flows an RCP application opens: a question, a wizard whose Browse opens a dialog that
 * reports an error, preferences whose Add opens a dialog that opens another, and a progress dialog.
 * Each dialog is the JFace class an Eclipse IDE uses, so each gets the presentation it gets there.
 */
public class DialogsSnippet {

    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("Dialogs");
        shell.setLayout(new GridLayout());
        Composite row = new Composite(shell, SWT.NONE);
        row.setLayout(new RowLayout());
        Label status = new Label(shell, SWT.NONE);
        status.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        status.setText("Open a dialog.");

        button(row, "Confirm Exit", () -> {
            MessageDialogWithToggle d = MessageDialogWithToggle.openOkCancelConfirm(shell, "Confirm Exit",
                    "Exit the application?", "Always exit without asking", false, null, null);
            status.setText("Confirm Exit: " + (d.getReturnCode() == IDialogConstants.OK_ID ? "OK" : "Cancel"));
        });
        button(row, "Save Changes", () -> {
            int answer = new MessageDialog(shell, "Save Resource", null, "'OrderService.java' has been modified. Save changes?",
                    MessageDialog.QUESTION_WITH_CANCEL, 0, "Save", "Don't Save", "Cancel").open();
            status.setText("Save Changes: " + answer);
        });
        button(row, "Message Box", () -> {
            MessageBox box = new MessageBox(shell, SWT.ICON_WARNING | SWT.YES | SWT.NO);
            box.setText("Delete");
            box.setMessage("Delete 'OrderService.java'?");
            status.setText("Message Box: " + (box.open() == SWT.YES ? "Yes" : "No"));
        });
        button(row, "New Project", () -> {
            int code = new WizardDialog(shell, new ProjectWizard()).open();
            status.setText("New Project: " + (code == 0 ? "Finish" : "Cancel"));
        });
        button(row, "Preferences", () -> {
            int code = preferences(shell).open();
            status.setText("Preferences: " + (code == 0 ? "OK" : "Cancel"));
        });
        button(row, "Long Operation", () -> {
            try {
                new ProgressMonitorDialog(shell).run(true, true, monitor -> {
                    monitor.beginTask("Building workspace", 50);
                    for (int i = 0; i < 50 && !monitor.isCanceled(); i++) {
                        monitor.subTask("Compiling OrderService" + i + ".java");
                        sleep(100);
                        monitor.worked(1);
                    }
                    monitor.done();
                });
                status.setText("Long Operation: done");
            } catch (InvocationTargetException | InterruptedException e) {
                status.setText("Long Operation: " + e.getClass().getSimpleName());
            }
        });
        button(row, "Plain Dialog", () -> {
            Shell dialog = new Shell(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
            dialog.setText("Rename Resource");
            dialog.setLayout(new GridLayout(2, false));
            new Label(dialog, SWT.NONE).setText("New name:");
            new Text(dialog, SWT.BORDER).setText("OrderService2.java");
            Button ok = new Button(dialog, SWT.PUSH);
            ok.setText("OK");
            ok.addListener(SWT.Selection, e -> dialog.close());
            dialog.setDefaultButton(ok);
            dialog.pack();
            dialog.open();
            while (!dialog.isDisposed()) {
                if (!display.readAndDispatch()) display.sleep();
            }
            status.setText("Plain Dialog: closed");
        });

        shell.setSize(900, 600);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
    }

    private static void button(Composite parent, String text, Runnable action) {
        Button b = new Button(parent, SWT.PUSH);
        b.setText(text);
        b.addListener(SWT.Selection, e -> action.run());
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /** A dialog that browses for a folder and reports an error when the one picked is not a JDK. */
    static class ChooseFolderDialog extends TitleAreaDialog {
        String chosen;
        private List list;

        ChooseFolderDialog(Shell parent) {
            super(parent);
        }

        @Override
        public void create() {
            super.create();
            setTitle("Choose Directory");
            setMessage("Select the home folder of a JDK.");
        }

        @Override
        protected void configureShell(Shell shell) {
            super.configureShell(shell);
            shell.setText("Choose Directory");
        }

        @Override
        protected Control createDialogArea(Composite parent) {
            Composite area = (Composite) super.createDialogArea(parent);
            list = new List(area, SWT.BORDER | SWT.SINGLE);
            list.setItems("/opt/jdk-17", "/opt/jdk-21", "/opt/jdk-23", "/opt/maven");
            list.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
            return area;
        }

        @Override
        protected void okPressed() {
            int i = list.getSelectionIndex();
            String picked = i < 0 ? "" : list.getItem(i);
            if (picked.endsWith("maven") || picked.isEmpty()) {
                MessageDialog.openError(getShell(), "Not a JDK", "'" + picked + "' has no bin/java.");
                return;
            }
            chosen = picked;
            super.okPressed();
        }
    }

    static class ProjectWizard extends Wizard {
        ProjectWizard() {
            setWindowTitle("New Java Project");
        }

        @Override
        public void addPages() {
            addPage(new WizardPage("project") {
                {
                    setTitle("Create a Java Project");
                    setDescription("Enter a project name.");
                }

                @Override
                public void createControl(Composite parent) {
                    Composite c = new Composite(parent, SWT.NONE);
                    c.setLayout(new GridLayout(3, false));
                    new Label(c, SWT.NONE).setText("Project name:");
                    Text name = new Text(c, SWT.BORDER);
                    name.setText("inventory");
                    name.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
                    new Label(c, SWT.NONE).setText("Location:");
                    Text location = new Text(c, SWT.BORDER);
                    location.setText("~/workspace/inventory");
                    location.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
                    Button browse = new Button(c, SWT.PUSH);
                    browse.setText("Browse...");
                    browse.addListener(SWT.Selection, e -> {
                        ChooseFolderDialog d = new ChooseFolderDialog(getShell());
                        if (d.open() == 0) location.setText(d.chosen);
                    });
                    setControl(c);
                }
            });
            addPage(new WizardPage("settings") {
                {
                    setTitle("Java Settings");
                    setDescription("Define the Java build settings.");
                }

                @Override
                public void createControl(Composite parent) {
                    Composite c = new Composite(parent, SWT.NONE);
                    c.setLayout(new GridLayout(2, false));
                    new Label(c, SWT.NONE).setText("Output folder:");
                    new Text(c, SWT.BORDER).setText("target/classes");
                    setControl(c);
                }
            });
        }

        @Override
        public boolean performFinish() {
            return true;
        }
    }

    static PreferenceDialog preferences(Shell parent) {
        PreferenceManager manager = new PreferenceManager();
        PreferenceStore store = new PreferenceStore();
        manager.addToRoot(new PreferenceNode("general", new FieldEditorPreferencePage("General", FieldEditorPreferencePage.GRID) {
            @Override
            protected void createFieldEditors() {
                addField(new BooleanFieldEditor("confirmExit", "Confirm exit when closing last window", getFieldEditorParent()));
            }
        }));
        PreferenceNode java = new PreferenceNode("java", new PreferencePage("Installed JREs") {
            @Override
            protected Control createContents(Composite parent) {
                Composite c = new Composite(parent, SWT.NONE);
                c.setLayout(new GridLayout(2, false));
                List jres = new List(c, SWT.BORDER);
                jres.setItems("JDK 21 (default)", "JDK 17");
                jres.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
                Button add = new Button(c, SWT.PUSH);
                add.setText("Add...");
                add.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
                add.addListener(SWT.Selection, e -> {
                    AddJreDialog d = new AddJreDialog(getShell());
                    if (d.open() == 0 && d.home != null) jres.add(d.home);
                });
                return c;
            }
        });
        manager.addToRoot(java);
        PreferenceDialog dialog = new PreferenceDialog(parent, manager);
        dialog.setPreferenceStore(store);
        return dialog;
    }

    static class AddJreDialog extends TitleAreaDialog {
        String home;
        private Text homeText;

        AddJreDialog(Shell parent) {
            super(parent);
        }

        @Override
        public void create() {
            super.create();
            setTitle("Add JRE");
            setMessage("Specify attributes for a JRE.");
        }

        @Override
        protected void configureShell(Shell shell) {
            super.configureShell(shell);
            shell.setText("Add JRE");
        }

        @Override
        protected Control createDialogArea(Composite parent) {
            Composite area = (Composite) super.createDialogArea(parent);
            Composite c = new Composite(area, SWT.NONE);
            c.setLayout(new GridLayout(3, false));
            c.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
            new Label(c, SWT.NONE).setText("JRE home:");
            homeText = new Text(c, SWT.BORDER);
            homeText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            Button directory = new Button(c, SWT.PUSH);
            directory.setText("Directory...");
            directory.addListener(SWT.Selection, e -> {
                ChooseFolderDialog d = new ChooseFolderDialog(getShell());
                if (d.open() == 0) homeText.setText(d.chosen);
            });
            return area;
        }

        @Override
        protected void okPressed() {
            home = homeText.getText();
            super.okPressed();
        }
    }
}
