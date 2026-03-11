package dev.equo.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.*;

import java.io.File;

class CrashDialog {

    enum CrashType { RUNTIME, NATIVE, ABNORMAL_EXIT }

    private final Display display;
    private final File crashLog;
    private final File eclipseLog;
    private final CrashType crashType;

    CrashDialog(Display display, File crashLog, File eclipseLog) {
        this(display, crashLog, eclipseLog, CrashType.RUNTIME);
    }

    CrashDialog(Display display, File crashLog, File eclipseLog, CrashType crashType) {
        this.display = display;
        this.crashLog = crashLog;
        this.eclipseLog = eclipseLog;
        this.crashType = crashType;
    }

    void open() {
        Shell shell = new Shell(display, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        shell.setText("Crash Report");

        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 20;
        layout.marginHeight = 20;
        shell.setLayout(layout);

        // Title label (bold, +2pt)
        Label titleLabel = new Label(shell, SWT.NONE);
        switch (crashType) {
            case NATIVE:
                titleLabel.setText("The application crashed during the last session");
                break;
            case ABNORMAL_EXIT:
                titleLabel.setText("The application didn't shut down cleanly during the last session");
                break;
            default:
                titleLabel.setText("Oops, something went wrong!");
                break;
        }
        FontData[] fd = titleLabel.getFont().getFontData();
        for (FontData f : fd) {
            f.setHeight(f.getHeight() + 2);
            f.setStyle(SWT.BOLD);
        }
        Font titleFont = new Font(display, fd);
        titleLabel.setFont(titleFont);

        // Description label
        Label descLabel = new Label(shell, SWT.WRAP);
        switch (crashType) {
            case NATIVE:
                descLabel.setText("It looks like the application suffered a native crash. Could you describe what you were doing before it happened? Even a short hint helps us investigate.");
                break;
            case ABNORMAL_EXIT:
                descLabel.setText("This could mean it crashed or was terminated unexpectedly. Could you describe what you were doing before it happened? Even a short hint helps us investigate.");
                break;
            default:
                descLabel.setText("Could you briefly describe what you were doing? Even a short hint helps us track down and fix the problem faster.");
                break;
        }
        descLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        // Text area for user description
        Text descriptionText = new Text(shell, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
        GridData descGD = new GridData(SWT.FILL, SWT.FILL, true, true);
        descGD.heightHint = 120;
        descriptionText.setLayoutData(descGD);

        // Email label
        Label emailLabel = new Label(shell, SWT.NONE);
        emailLabel.setText("Email (optional):");

        // Email text
        Text emailText = new Text(shell, SWT.SINGLE | SWT.BORDER);
        emailText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        // Email note
        Label emailNote = new Label(shell, SWT.WRAP);
        emailNote.setText("We'll only use this to follow up on the bug or share a workaround with you.");
        emailNote.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        // Link to log file
        Link logLink = new Link(shell, SWT.WRAP);
        File logToOpen = crashLog != null ? crashLog : eclipseLog;
        if (logToOpen != null) {
            String logLabel = crashLog != null ? "crash log" : "application log";
            logLink.setText("An <a>" + logLabel + "</a> will be attached to the report. You can also send it directly to support@equo.dev.");
        } else {
            logLink.setText("You can send details directly to support@equo.dev.");
        }
        logLink.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        logLink.addListener(SWT.Selection, e -> {
            if (logToOpen != null) {
                Program.launch(logToOpen.getAbsolutePath());
            }
        });

        // Button bar
        Composite buttonBar = new Composite(shell, SWT.NONE);
        GridLayout buttonLayout = new GridLayout(2, true);
        buttonLayout.marginWidth = 0;
        buttonBar.setLayout(buttonLayout);
        buttonBar.setLayoutData(new GridData(SWT.END, SWT.CENTER, true, false));

        Button sendButton = new Button(buttonBar, SWT.PUSH);
        sendButton.setText("Send Report && Close");
        sendButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        shell.setDefaultButton(sendButton);

        Button exitButton = new Button(buttonBar, SWT.PUSH);
        exitButton.setText("Close Without Sending");
        exitButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        sendButton.addListener(SWT.Selection, e -> {
            shell.setCursor(display.getSystemCursor(SWT.CURSOR_WAIT));
            sendButton.setEnabled(false);
            exitButton.setEnabled(false);

            boolean success = CrashReporter.sendReport(
                    descriptionText.getText(),
                    emailText.getText().isBlank() ? null : emailText.getText().trim(),
                    crashLog,
                    eclipseLog
            );

            shell.setCursor(null);

            if (success) {
                MessageBox mb = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK);
                mb.setText("Report Sent");
                mb.setMessage("Thank you for helping us improve! Your report has been sent.");
                mb.open();
            } else {
                MessageBox mb = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
                mb.setText("Send Failed");
                String msg = "We couldn't send the report right now.";
                if (logToOpen != null) {
                    msg += "\n\nYou can send the log manually:\n" + logToOpen.getAbsolutePath();
                }
                msg += "\n\nContact us at support@equo.dev";
                mb.setMessage(msg);
                mb.open();
            }
            shell.dispose();
        });

        exitButton.addListener(SWT.Selection, e -> shell.dispose());

        shell.addListener(SWT.Dispose, e -> titleFont.dispose());

        // Size and center
        shell.setMinimumSize(500, SWT.DEFAULT);
        shell.setSize(500, shell.computeSize(500, SWT.DEFAULT).y);
        Rectangle screenBounds = display.getPrimaryMonitor().getBounds();
        Rectangle shellBounds = shell.getBounds();
        shell.setLocation(
                screenBounds.x + (screenBounds.width - shellBounds.width) / 2,
                screenBounds.y + (screenBounds.height - shellBounds.height) / 2
        );

        shell.open();

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
    }
}
