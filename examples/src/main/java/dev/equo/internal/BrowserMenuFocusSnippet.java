package dev.equo.internal;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * An application menu drawn in a Browser and driven through {@code evaluate}: the page is loaded
 * into a view created on first use, and the application focuses the menu's element from
 * {@code ProgressListener.completed}, on focus-in and on shell activation. Each button runs one
 * load/evaluate ordering; the log shows what every {@code evaluate} returned, or its exception.
 * <p>
 * Run with {@code -Dequo=false} for the native baseline.
 */
public class BrowserMenuFocusSnippet {

    static final String MENU_HTML = "<!doctype html><html><head><title>Menu</title></head><body>"
            + "<button id='menu'>File</button> <button>Edit</button>"
            + "<div id='late'></div>"
            + "<script>"
            + "document.addEventListener('wheel', function () {}, {passive: true});"
            + "var ua = navigator.userAgent;"
            + "document.getElementById('late').innerHTML = \"<button id='dyn'>Dynamic</button>\";"
            + "</script></body></html>";

    /** The shape of the application's script: look an element up, focus it, report it. */
    static final String FOCUS_SCRIPT =
            "var m = document.getElementById('menu'); m.focus(); return m.id + '@' + location.href;";

    static Browser browser;
    static Text log;
    static Composite menuView;

    public static void main(String[] args) {
        if (Boolean.parseBoolean(System.getProperty("equo", "true"))) Config.forceEquo();
        else Config.forceEclipse();

        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("Browser menu focus");
        shell.setLayout(new GridLayout(1, false));

        Composite actions = new Composite(shell, SWT.NONE);
        actions.setLayout(new RowLayout());
        actions.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        CTabFolder views = new CTabFolder(shell, SWT.BORDER);
        views.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        CTabItem home = new CTabItem(views, SWT.NONE);
        home.setText("Home");
        Label homeContent = new Label(views, SWT.NONE);
        homeContent.setText("Open the menu view to create its Browser.");
        home.setControl(homeContent);
        CTabItem menuTab = new CTabItem(views, SWT.NONE);
        menuTab.setText("Menu");
        menuView = new Composite(views, SWT.NONE);
        menuView.setLayout(new FillLayout());
        menuTab.setControl(menuView);
        views.setSelection(home);
        views.addListener(SWT.Selection, e -> {
            if (views.getSelection() == menuTab) ensureBrowser();
        });

        log = new Text(shell, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.READ_ONLY);
        GridData logData = new GridData(SWT.FILL, SWT.BOTTOM, true, false);
        logData.heightHint = 180;
        log.setLayoutData(logData);

        button(actions, "Open menu view", () -> {
            views.setSelection(menuTab);
            ensureBrowser();
        });
        button(actions, "setText again", () -> ensureBrowser().setText(MENU_HTML));
        button(actions, "setText + evaluate now", () -> {
            ensureBrowser().setText(MENU_HTML);
            evaluate("right after setText");
        });
        button(actions, "evaluate now", () -> evaluate("button"));
        button(actions, "setUrl(file:)", () -> ensureBrowser().setUrl(menuFile().toURI().toString()));
        button(actions, "hide/show view", () -> {
            ensureBrowser().setVisible(false);
            browser.setVisible(true);
            evaluate("after hide/show");
        });
        button(actions, "clear log", () -> log.setText(""));

        shell.addListener(SWT.Activate, e -> {
            if (browser != null) evaluate("shell activated");
        });

        shell.setSize(900, 650);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
    }

    static Browser ensureBrowser() {
        if (browser != null) return browser;
        browser = new Browser(menuView, SWT.NONE);
        browser.addProgressListener(ProgressListener.completedAdapter(e -> evaluate("completed")));
        browser.addListener(SWT.FocusIn, e -> evaluate("focus in"));
        browser.setText(MENU_HTML);
        menuView.layout(true, true);
        append("Browser created, setText issued");
        return browser;
    }

    static void evaluate(String trigger) {
        try {
            append(trigger + " -> " + browser.evaluate(FOCUS_SCRIPT));
        } catch (RuntimeException ex) {
            append(trigger + " -> FAILED " + ex.getMessage());
        }
    }

    static File menuFile() {
        try {
            File f = File.createTempFile("menu", ".html");
            f.deleteOnExit();
            Files.write(f.toPath(), MENU_HTML.getBytes(StandardCharsets.UTF_8));
            return f;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    static void button(Composite parent, String text, Runnable action) {
        Button b = new Button(parent, SWT.PUSH);
        b.setText(text);
        b.addListener(SWT.Selection, e -> action.run());
    }

    static void append(String line) {
        String stamped = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS")) + "  " + line;
        System.out.println(stamped);
        if (!log.isDisposed()) log.append(stamped + "\n");
    }
}
