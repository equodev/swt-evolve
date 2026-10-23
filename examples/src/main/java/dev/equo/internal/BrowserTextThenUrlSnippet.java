package dev.equo.internal;

import com.sun.net.httpserver.HttpServer;
import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationAdapter;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.ProgressAdapter;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Shows a loading page with setText, then registers the ProgressListener whose completed() replaces
 * it with the real page through setUrl, the order an application uses to show a spinner while a page
 * loads. The left Browser's loading page is small, the right one's carries a ~2 MB inline image.
 *
 * Both Browsers must end on "Loaded page": the console prints "completed" for the loading page and
 * then for the page setUrl loads. A Browser that stays on its spinner never reported the loading page
 * as completed.
 */
public class BrowserTextThenUrlSnippet {

    public static void main(String[] args) throws Exception {
        Config.useEquo(Browser.class);
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/page", ex -> {
            byte[] body = "<html><body><h1>Loaded page</h1></body></html>".getBytes(StandardCharsets.UTF_8);
            ex.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
            ex.sendResponseHeaders(200, body.length);
            try (OutputStream out = ex.getResponseBody()) { out.write(body); }
        });
        server.start();
        String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/page";

        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("Browser: setText, then setUrl on completed");
        shell.setLayout(new FillLayout());
        loadAfterSpinner(new Browser(shell, SWT.NONE), "small", spinnerPage(0), url);
        loadAfterSpinner(new Browser(shell, SWT.NONE), "large", spinnerPage(2_000_000), url);
        shell.setSize(900, 400);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
        server.stop(0);
    }

    static void loadAfterSpinner(Browser browser, String name, String spinner, String url) {
        browser.addLocationListener(new LocationAdapter() {
            @Override
            public void changed(LocationEvent event) {
                System.out.println(name + ": location " + event.location);
            }
        });
        browser.setText(spinner);
        browser.addProgressListener(new ProgressAdapter() {
            @Override
            public void completed(ProgressEvent event) {
                System.out.println(name + ": completed, loading " + url);
                browser.removeProgressListener(this);
                browser.setUrl(url);
            }
        });
    }

    static String spinnerPage(int imageBytes) {
        char[] data = new char[imageBytes];
        Arrays.fill(data, 'A');
        String image = imageBytes == 0 ? "" : "<img src=\"data:image/gif;base64," + new String(data) + "\">";
        return "<html><body><p>Loading...</p>" + image + "</body></html>";
    }
}
