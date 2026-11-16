package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * A Browser hidden with setVisible(false) shows nothing: the area below the buttons is the
 * composite's background. Shown again, it shows the same page, not a reloaded one: the count, the
 * typed text and the time the page was loaded are what they were before it was hidden.
 *
 * <p>Each toggle prints the page's state, read with {@code evaluate}:
 * {@code visible=<v> count=<n> text=<typed> loadedAt=<ms>}. A reloaded page prints count=0, an
 * empty text and a new loadedAt.
 */
public class BrowserHideSnippet {

    private static final String PAGE = "<html><body style='margin:0;background:#c62828;color:white;"
            + "font:18px sans-serif'><div style='padding:16px'>"
            + "<p>Loaded at <span id='t'></span></p>"
            + "<p><button id='inc' onclick=\"var n=document.getElementById('n');"
            + "n.textContent=+n.textContent+1\">Count</button> <span id='n'>0</span></p>"
            + "<p><input id='text' placeholder='Type here'></p>"
            + "</div><script>document.getElementById('t').textContent=Date.now();</script>"
            + "</body></html>";

    public static void main(String[] args) {
        Config.useEquo(Browser.class);
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("Browser setVisible");
        shell.setLayout(new GridLayout(1, false));

        Composite buttons = new Composite(shell, SWT.NONE);
        buttons.setLayout(new FillLayout());
        Button toggle = new Button(buttons, SWT.PUSH);
        toggle.setText("Hide Browser");
        Button count = new Button(buttons, SWT.PUSH);
        count.setText("Count in page");

        Composite holder = new Composite(shell, SWT.NONE);
        holder.setLayout(new FillLayout());
        GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
        data.widthHint = 400;
        data.heightHint = 250;
        holder.setLayoutData(data);

        Browser browser = new Browser(holder, SWT.NONE);
        browser.setText(PAGE);

        count.addListener(SWT.Selection, e -> {
            browser.execute("document.getElementById('inc').click();");
            printState(browser);
        });
        toggle.addListener(SWT.Selection, e -> {
            boolean show = !browser.getVisible();
            browser.setVisible(show);
            toggle.setText(show ? "Hide Browser" : "Show Browser");
            printState(browser);
        });

        shell.pack();
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
    }

    private static void printState(Browser browser) {
        Object state = browser.evaluate("return 'count=' + document.getElementById('n').textContent"
                + " + ' text=' + document.getElementById('text').value"
                + " + ' loadedAt=' + document.getElementById('t').textContent;");
        System.out.println("visible=" + browser.getVisible() + " " + state);
    }
}
