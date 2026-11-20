package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.ProgressAdapter;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * A Browser shows a {@code setText} document larger than 2 MB, and reports {@code completed} for it.
 * The pattern is a loading page with a large inline image, replaced from {@code completed} by the
 * real content: the window first shows "Loading" in blue, then "Loaded after completed" in green.
 *
 * <p>Prints {@code completed: <url>} for each completed load.
 */
public class BrowserLargeTextSnippet {

    private static final int DOCUMENT_CHARS = 2_300_000;

    public static void main(String[] args) {
        Config.useEquo(Browser.class);
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("Browser setText over 2 MB");
        shell.setLayout(new FillLayout());

        Browser browser = new Browser(shell, SWT.NONE);
        browser.addProgressListener(new ProgressAdapter() {
            @Override
            public void completed(ProgressEvent event) {
                System.out.println("completed: " + browser.getUrl());
                browser.removeProgressListener(this);
                browser.setText("<html><body style='margin:0;background:#2e7d32;color:white;"
                        + "font:24px sans-serif'><h1 style='padding:16px'>Loaded after completed</h1>"
                        + "</body></html>");
            }
        });
        browser.setText(largeDocument());

        shell.setSize(600, 400);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
    }

    private static String largeDocument() {
        StringBuilder html = new StringBuilder("<html><body style='margin:0;background:#1565c0;"
                + "color:white;font:24px sans-serif'><h1 style='padding:16px'>Loading</h1><!--");
        while (html.length() < DOCUMENT_CHARS) html.append("0123456789abcdef");
        return html.append("--></body></html>").toString();
    }
}
