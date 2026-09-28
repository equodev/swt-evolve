package dev.equo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * A {@link Browser} showing a fixed page, for comparing what {@code swt.autoScale} does to embedded
 * web content. Native SWT does not carry the flag inside a browser: the widget's box grows, the page
 * gets a wider viewport, and its type stays the size it was.
 *
 * <pre>
 * ./gradlew :swt-evolve:examples:runDeskExample -PmainClass=dev.equo.BrowserZoomSnippet
 * </pre>
 */
public class BrowserZoomSnippet {

    private static final String PAGE = "<html><body style='margin:8px;font-family:Segoe UI,sans-serif'>"
            + "<h2 style='margin:0 0 8px 0'>Announcements</h2>"
            + "<p style='font-size:14px;margin:0 0 6px 0'>This release has 11 defects and enhancements"
            + " focused on stability, visualization, security and large-dataset workflows.</p>"
            + "<ul style='font-size:14px;margin:0'>"
            + "<li>Mapping and frameworks</li><li>Well tie</li><li>Data analysis</li></ul>"
            + "</body></html>";

    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("Browser zoom");
        shell.setLayout(new FillLayout());

        Browser browser = new Browser(shell, SWT.NONE);
        browser.setText(PAGE);

        shell.setSize(600, 320);
        shell.open();
        display.timerExec(3000, () -> {
            if (shell.isDisposed()) return;
            System.out.println("[browser] bounds=" + browser.getBounds()
                    + " deviceZoom=" + org.eclipse.swt.internal.DPIUtil.getDeviceZoom());
        });
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
    }
}
