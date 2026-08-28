package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.e4.ui.workbench.renderers.swt.TrimmedPartLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

/**
 * A menu bar popup drawn over an SWT {@link Browser}.
 *
 * <p>On web the Browser is a real DOM {@code <iframe>} while every other control is painted on
 * Flutter's canvas, so a popup that overlaps the iframe rectangle has no DOM element of its own
 * there and the iframe takes the click. The band above the Browser is what makes the failure
 * legible in a single popup: the entries that fall above the iframe's top edge still work, and
 * everything below it is dead.
 *
 * <pre>
 * ./gradlew :examples:runWebExample -PmainClass=dev.equo.MenuOverBrowserSnippet
 * </pre>
 *
 * <p>Every entry prints on selection, so the console says whether a click landed at all. Runs with
 * {@code decorations_align=vleft}, the hamburger menu the reported app uses; pass {@code hleft} to
 * exercise the horizontal menu bar strip instead.
 */
public class MenuOverBrowserSnippet {

    private static final String ALIGN_PROPERTY = "swt.evolve.decorations_align";

    /** Tall enough that the popup's lower entries are guaranteed to reach past the iframe's top edge. */
    private static final int BAND_HEIGHT = 90;

    public static void main(String[] args) {
        System.setProperty(ALIGN_PROPERTY, args.length > 0 ? args[0].trim().toLowerCase() : "vleft");

        Config.useEquo(Menu.class);
        Config.useEquo(MenuItem.class);
        Config.useEquo(ToolBar.class);
        Config.useEquo(ToolItem.class);
        Config.useEquo(Label.class);

        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("Menu over Browser");
        shell.setSize(900, 620);

        TrimmedPartLayout trim = new TrimmedPartLayout();
        shell.setLayout(trim);

        Composite mainToolbar = trim.getTrimComposite(shell, SWT.TOP);
        mainToolbar.setLayout(new RowLayout(SWT.HORIZONTAL));
        ToolBar bar = new ToolBar(mainToolbar, SWT.FLAT);
        for (String label : new String[] {"New", "Open", "Save"}) {
            new ToolItem(bar, SWT.PUSH).setText(label);
        }
        bar.pack();

        Composite body = trim.getClientArea(shell);
        body.setLayout(new GridLayout(1, false));

        Label band = new Label(body, SWT.WRAP);
        band.setText("Canvas band -- popup entries that stay inside it keep working.");
        GridData bandData = new GridData(SWT.FILL, SWT.TOP, true, false);
        bandData.heightHint = BAND_HEIGHT;
        band.setLayoutData(bandData);

        Composite browserHost = new Composite(body, SWT.NONE);
        browserHost.setLayout(new FillLayout());
        browserHost.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        Browser browser = new Browser(browserHost, SWT.NONE);
        browser.setText("<html><head><title>Under the menu</title></head>"
                + "<body style='margin:0;background:#dfe7f2;font-family:sans-serif'>"
                + "<h2 style='padding:16px'>Browser iframe</h2></body></html>");

        Menu menuBar = new Menu(shell, SWT.BAR);
        // Enough cascades that the popup is taller than the band and so crosses the iframe's edge.
        for (String name : new String[] {"File", "Edit", "Navigate", "Search", "Project", "Run", "Window", "Help"}) {
            MenuItem cascade = new MenuItem(menuBar, SWT.CASCADE);
            cascade.setText(name);
            Menu dropDown = new Menu(shell, SWT.DROP_DOWN);
            cascade.setMenu(dropDown);
            for (String entry : new String[] {"First", "Second", "Third"}) {
                MenuItem item = new MenuItem(dropDown, SWT.PUSH);
                item.setText(entry);
                item.addSelectionListener(widgetSelectedAdapter(e -> System.out.println("SELECTED " + name + " > " + entry)));
            }
            cascade.addListener(SWT.Arm, e -> System.out.println("ARMED " + name));
        }
        shell.setMenuBar(menuBar);

        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }
}
