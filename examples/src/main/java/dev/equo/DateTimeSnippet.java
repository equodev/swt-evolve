package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.*;

/**
 * Demonstrates all SWT DateTime widget modes and style combinations:
 *
 *  DATE  | MEDIUM      - default date picker (MM/DD/YYYY)
 *  DATE  | SHORT       - compact date (MM/YYYY)
 *  DATE  | LONG        - same as MEDIUM (SWT doesn't add DOW on all platforms)
 *  DATE  | DROP_DOWN   - date field with inline calendar dropdown
 *  TIME  | MEDIUM      - time picker (HH:MM:SS)
 *  TIME  | SHORT       - compact time (HH:MM)
 *  CALENDAR             - full month grid
 *  CALENDAR | CALENDAR_WEEKNUMBERS - calendar with ISO week numbers
 *
 *  Also exercises: setDate(), setTime(), getDay/Month/Year/Hours/Minutes/Seconds
 */
public class DateTimeSnippet {

    public static void main(String[] args) {
        Config.useEquo(DateTime.class);

        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("DateTime Snippet");
        shell.setLayout(new GridLayout(2, false));

        // ── Status label ────────────────────────────────────────────────
        Label statusLabel = new Label(shell, SWT.BORDER | SWT.WRAP);
        statusLabel.setText("Interact with any widget to see events here.");
        GridData statusGd = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
        statusGd.heightHint = 40;
        statusLabel.setLayoutData(statusGd);

        // ── DATE | MEDIUM ────────────────────────────────────────────────
        addSectionLabel(shell, "DATE | MEDIUM (default):");
        DateTime dateMedium = new DateTime(shell, SWT.DATE | SWT.MEDIUM | SWT.BORDER);
        dateMedium.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                statusLabel.setText("DATE MEDIUM → "
                        + pad(dateMedium.getMonth() + 1) + "/"
                        + pad(dateMedium.getDay()) + "/"
                        + dateMedium.getYear());
            }
        });

        // ── DATE | SHORT ─────────────────────────────────────────────────
        addSectionLabel(shell, "DATE | SHORT (MM/YYYY):");
        DateTime dateShort = new DateTime(shell, SWT.DATE | SWT.SHORT | SWT.BORDER);
        dateShort.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                statusLabel.setText("DATE SHORT → "
                        + pad(dateShort.getMonth() + 1) + "/"
                        + dateShort.getYear());
            }
        });

        // ── DATE | LONG ──────────────────────────────────────────────────
        addSectionLabel(shell, "DATE | LONG (MM/DD/YYYY):");
        DateTime dateLong = new DateTime(shell, SWT.DATE | SWT.LONG | SWT.BORDER);
        dateLong.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                statusLabel.setText("DATE LONG → "
                        + pad(dateLong.getMonth() + 1) + "/"
                        + pad(dateLong.getDay()) + "/"
                        + dateLong.getYear());
            }
        });

        // ── DATE | DROP_DOWN ─────────────────────────────────────────────
        addSectionLabel(shell, "DATE | DROP_DOWN (with calendar):");
        DateTime dateDropDown = new DateTime(shell, SWT.DATE | SWT.DROP_DOWN | SWT.BORDER);
        dateDropDown.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                statusLabel.setText("DATE DROP_DOWN → "
                        + pad(dateDropDown.getMonth() + 1) + "/"
                        + pad(dateDropDown.getDay()) + "/"
                        + dateDropDown.getYear());
            }
        });

        // ── TIME | MEDIUM ────────────────────────────────────────────────
        addSectionLabel(shell, "TIME | MEDIUM (HH:MM:SS):");
        DateTime timeMedium = new DateTime(shell, SWT.TIME | SWT.MEDIUM | SWT.BORDER);
        timeMedium.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                statusLabel.setText("TIME MEDIUM → "
                        + pad(timeMedium.getHours()) + ":"
                        + pad(timeMedium.getMinutes()) + ":"
                        + pad(timeMedium.getSeconds()));
            }
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                statusLabel.setText("TIME MEDIUM [Enter] → "
                        + pad(timeMedium.getHours()) + ":"
                        + pad(timeMedium.getMinutes()) + ":"
                        + pad(timeMedium.getSeconds()));
            }
        });

        // ── TIME | SHORT ─────────────────────────────────────────────────
        addSectionLabel(shell, "TIME | SHORT (HH:MM):");
        DateTime timeShort = new DateTime(shell, SWT.TIME | SWT.SHORT | SWT.BORDER);
        timeShort.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                statusLabel.setText("TIME SHORT → "
                        + pad(timeShort.getHours()) + ":"
                        + pad(timeShort.getMinutes()));
            }
        });

        // ── CALENDAR ─────────────────────────────────────────────────────
        addSectionLabel(shell, "CALENDAR:");
        DateTime calendar = new DateTime(shell, SWT.CALENDAR | SWT.BORDER);
        calendar.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                statusLabel.setText("CALENDAR → "
                        + pad(calendar.getMonth() + 1) + "/"
                        + pad(calendar.getDay()) + "/"
                        + calendar.getYear());
            }
        });

        // ── CALENDAR | CALENDAR_WEEKNUMBERS ──────────────────────────────
        addSectionLabel(shell, "CALENDAR | CALENDAR_WEEKNUMBERS:");
        DateTime calendarWk = new DateTime(shell, SWT.CALENDAR | SWT.CALENDAR_WEEKNUMBERS | SWT.BORDER);
        calendarWk.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                statusLabel.setText("CALENDAR WK# → "
                        + pad(calendarWk.getMonth() + 1) + "/"
                        + pad(calendarWk.getDay()) + "/"
                        + calendarWk.getYear());
            }
        });

        // ── Programmatic API buttons ─────────────────────────────────────
        Label apiLabel = new Label(shell, SWT.NONE);
        apiLabel.setText("Programmatic API:");
        apiLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));

        Composite buttonRow = new Composite(shell, SWT.NONE);
        buttonRow.setLayout(new RowLayout(SWT.HORIZONTAL));
        buttonRow.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));

        Button setDateBtn = new Button(buttonRow, SWT.PUSH);
        setDateBtn.setText("Set date → 2000-06-15");
        setDateBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                // setDate(year, month, day) — month is 0-indexed
                dateMedium.setDate(2000, 5, 15);
                dateShort.setDate(2000, 5, 15);
                dateLong.setDate(2000, 5, 15);
                dateDropDown.setDate(2000, 5, 15);
                calendar.setDate(2000, 5, 15);
                calendarWk.setDate(2000, 5, 15);
                statusLabel.setText("setDate(2000, 5, 15) called on all DATE/CALENDAR widgets");
            }
        });

        Button setTimeBtn = new Button(buttonRow, SWT.PUSH);
        setTimeBtn.setText("Set time → 14:30:00");
        setTimeBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                timeMedium.setTime(14, 30, 0);
                timeShort.setTime(14, 30, 0);
                statusLabel.setText("setTime(14, 30, 0) called on all TIME widgets");
            }
        });

        Button readBtn = new Button(buttonRow, SWT.PUSH);
        readBtn.setText("Read all values");
        readBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String date = pad(dateMedium.getMonth() + 1) + "/"
                        + pad(dateMedium.getDay()) + "/" + dateMedium.getYear();
                String time = pad(timeMedium.getHours()) + ":"
                        + pad(timeMedium.getMinutes()) + ":"
                        + pad(timeMedium.getSeconds());
                String cal = pad(calendar.getMonth() + 1) + "/"
                        + pad(calendar.getDay()) + "/" + calendar.getYear();
                statusLabel.setText("date=" + date + "  time=" + time + "  cal=" + cal);
            }
        });

        shell.setSize(600, 700);
        shell.open();

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
    }

    private static void addSectionLabel(Shell shell, String text) {
        Label label = new Label(shell, SWT.NONE);
        label.setText(text);
        label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
    }

    private static String pad(int value) {
        return value < 10 ? "0" + value : String.valueOf(value);
    }
}