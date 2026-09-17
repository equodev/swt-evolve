package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.e4.ui.workbench.renderers.swt.TrimmedPartLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

/**
 * Exercises the two things SWT cannot express: a tooltip with more in it than one line of text, and
 * a transient notification that does not stop the user.
 *
 * <p>Neither needs anything from the application. This snippet sets two system properties and builds
 * an ordinary toolbar — every hover and every click below goes through unmodified SWT calls.
 *
 * <p>Both are off unless asked for, and both are confined to the main toolbar — the e4 top trim,
 * which {@link TrimmedPartLayout} stands in for here. The second toolbar, in the window body,
 * carries the same items on purpose: it must keep the plain one-line tooltip and raise nothing on
 * click, which is what proves the scoping.
 *
 * <pre>
 * ./gradlew :examples:runDeskExample -PmainClass=dev.equo.RichToolTipToastSnippet
 * ./gradlew :examples:runDeskExample -PmainClass=dev.equo.RichToolTipToastSnippet --args=off
 * </pre>
 *
 * <p>What to look at:
 * <ul>
 * <li>Hovering an item in the top row opens a card with the item's icon, a title, a body and a
 *     shortcut chip. "New" and "Open" carry a shortcut in their tooltip the way e4 writes them;
 *     "Sync" carries a sentence as well; "Tools" carries a bare label and gets the sample body,
 *     which is what an application that says nothing more looks like.
 * <li>The same items in the body row show the plain strip.
 * <li>Clicking any item in the top row raises a notification card in the bottom-right: it slides in,
 *     stacks under any already there, and removes itself. Clicking in the body row raises nothing.
 * <li>Run with {@code off} and every one of those is gone, with no other change to the window.
 * </ul>
 */
public class RichToolTipToastSnippet {

    public static void main(String[] args) {
        applyFlags(args);

        Config.useEquo(ToolBar.class);
        Config.useEquo(ToolItem.class);
        Config.useEquo(Label.class);

        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("custom tooltip + notification popup " + (enabled() ? "on" : "off"));
        shell.setSize(900, 460);

        TrimmedPartLayout trim = new TrimmedPartLayout();
        shell.setLayout(trim);

        // TOP trim -> DartMainToolbar, the only place either feature is active.
        Composite mainToolbar = trim.getTrimComposite(shell, SWT.TOP);
        mainToolbar.setLayout(new RowLayout(SWT.HORIZONTAL));
        newToolBar(mainToolbar, display);

        Composite body = trim.getClientArea(shell);
        body.setLayout(new GridLayout(1, false));

        Label hint = new Label(body, SWT.WRAP);
        hint.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        hint.setText("Top row = main toolbar: hover for the card, click for the notification.\n"
                + "Row below = a plain ToolBar in the body: same items, plain tooltips, no notification.");

        ToolBar barInBody = newToolBar(body, display);
        barInBody.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        shell.layout(true, true);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }

    /** {@code [off]} — set before the first {@code Config} read, which caches the flags. */
    private static void applyFlags(String[] args) {
        boolean off = args.length > 0 && args[0].trim().equalsIgnoreCase("off");
        System.setProperty("swt.evolve.custom_tooltip", Boolean.toString(!off));
        System.setProperty("swt.evolve.notification_popup", Boolean.toString(!off));
    }

    private static boolean enabled() {
        return Boolean.getBoolean("swt.evolve.custom_tooltip");
    }

    private static ToolBar newToolBar(Composite parent, Display display) {
        ToolBar bar = new ToolBar(parent, SWT.FLAT | SWT.HORIZONTAL);

        push(bar, display, "New", "New (Ctrl+N)", display.getSystemColor(SWT.COLOR_DARK_BLUE));
        push(bar, display, "Open", "Open a project (Ctrl+O)",
                display.getSystemColor(SWT.COLOR_DARK_GREEN));

        ToolItem disabled = push(bar, display, "Save", "Save (Ctrl+S)",
                display.getSystemColor(SWT.COLOR_DARK_RED));
        disabled.setEnabled(false);

        new ToolItem(bar, SWT.SEPARATOR);

        push(bar, display, "Sync", "Sync with server (Ctrl+Shift+S)",
                display.getSystemColor(SWT.COLOR_DARK_MAGENTA));

        // A bare one-word tooltip, which is all some applications give a toolbar item.
        ToolItem bare = new ToolItem(bar, SWT.PUSH);
        bare.setToolTipText("Tools");
        bare.setImage(square(display, display.getSystemColor(SWT.COLOR_DARK_CYAN)));

        return bar;
    }

    private static ToolItem push(ToolBar bar, Display display, String text, String tooltip, Color color) {
        ToolItem item = new ToolItem(bar, SWT.PUSH);
        item.setText(text);
        item.setToolTipText(tooltip);
        item.setImage(square(display, color));
        return item;
    }

    /** A flat 16x16 tile: the snippet is about the tooltip and the notification, not the artwork. */
    private static Image square(Display display, Color color) {
        Image image = new Image(display, 16, 16);
        GC gc = new GC(image);
        gc.setBackground(color);
        gc.fillRectangle(0, 0, 16, 16);
        gc.dispose();
        return image;
    }
}
