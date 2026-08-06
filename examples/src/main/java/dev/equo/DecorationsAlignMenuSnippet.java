package dev.equo;

import dev.equo.swt.Config;
import dev.equo.swt.size.MenuSizes;
import org.eclipse.e4.ui.workbench.renderers.swt.TrimmedPartLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

/**
 * Exercises the {@code decorations_align} flag against a menu bar that may be empty.
 *
 * <p>The flag decides where the Shell's menu bar is rendered: as a horizontal strip above the main
 * toolbar ({@code hleft} / {@code hright}) or collapsed into a hamburger button inside the toolbar
 * row ({@code vleft} / {@code vright}). In every one of those four modes a menu bar with no items
 * must be invisible <em>and</em> weightless: no strip, no button, and no layout space reserved for
 * either — the toolbar must look exactly as it does on a Shell with no menu bar at all.
 *
 * <p>Run it once per alignment (the flag is read once, at startup):
 *
 * <pre>
 * ./gradlew :examples:runDeskExample -PmainClass=dev.equo.DecorationsAlignMenuSnippet --args=hleft
 * ./gradlew :examples:runDeskExample -PmainClass=dev.equo.DecorationsAlignMenuSnippet --args=hright
 * ./gradlew :examples:runDeskExample -PmainClass=dev.equo.DecorationsAlignMenuSnippet --args=vleft
 * ./gradlew :examples:runDeskExample -PmainClass=dev.equo.DecorationsAlignMenuSnippet --args=vright
 * </pre>
 *
 * <p>Use {@code runWebExample} instead to render in a browser. The "Fill / Clear menu bar" buttons
 * flip the menu between its two states; every flip prints the measured geometry next to what it
 * should be, so the run either says PASS on each line or shows exactly which number is off.
 *
 * <p>The main toolbar and the trims only exist under an e4-style trimmed Shell, which
 * {@link TrimmedPartLayout} stands in for here.
 */
public class DecorationsAlignMenuSnippet {

    private static final String ALIGN_PROPERTY = "swt.evolve.decorations_align";

    public static void main(String[] args) {
        final String align = parseAlign(args);
        System.setProperty(ALIGN_PROPERTY, align);
        boolean vertical = align.startsWith("v");
        boolean atStart = align.equals("vleft") || align.equals("hleft");

        Config.useEquo(Menu.class);
        Config.useEquo(MenuItem.class);
        Config.useEquo(ToolBar.class);
        Config.useEquo(ToolItem.class);
        Config.useEquo(Button.class);
        Config.useEquo(Label.class);

        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("decorations_align = " + align);
        shell.setSize(900, 520);

        TrimmedPartLayout trim = new TrimmedPartLayout();
        shell.setLayout(trim);

        // TOP trim -> DartMainToolbar. It needs a layout of its own, otherwise it hands the whole
        // layout pass back to its superclass and never applies the menu-button reserve.
        Composite mainToolbar = trim.getTrimComposite(shell, SWT.TOP);
        mainToolbar.setLayout(flushRowLayout());
        newToolBar(mainToolbar, "New", "Open", "Save");
        newToolBar(mainToolbar, "Undo", "Redo");

        Composite body = trim.getClientArea(shell);
        body.setLayout(new GridLayout(1, false));

        Menu menuBar = new Menu(shell, SWT.BAR);
        shell.setMenuBar(menuBar);

        Label status = new Label(body, SWT.WRAP);
        status.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        Composite buttons = new Composite(body, SWT.NONE);
        buttons.setLayout(new RowLayout(SWT.HORIZONTAL));

        Runnable report = () -> status.setText(check(shell, mainToolbar, menuBar, align, vertical, atStart));

        Button fill = new Button(buttons, SWT.PUSH);
        fill.setText("Fill menu bar");
        fill.addSelectionListener(widgetSelectedAdapter(e -> {
            setMenuItems(shell, menuBar, true);
            report.run();
        }));

        Button clear = new Button(buttons, SWT.PUSH);
        clear.setText("Clear menu bar");
        clear.addSelectionListener(widgetSelectedAdapter(e -> {
            setMenuItems(shell, menuBar, false);
            report.run();
        }));

        Label hint = new Label(body, SWT.WRAP);
        hint.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        hint.setText(vertical
                ? "Vertical alignment: with items the toolbar shows a hamburger button at the "
                        + (atStart ? "left" : "right") + " and the tool buttons shift over to make room.\n"
                        + "With an empty menu bar there must be no button at all and the tool buttons "
                        + "must sit flush against the " + (atStart ? "left" : "right") + " edge."
                : "Horizontal alignment: with items the menu strip sits above the toolbar row, "
                        + (atStart ? "starting at the left" : "ending at the right") + ".\n"
                        + "With an empty menu bar there must be no strip -- not even an empty one -- "
                        + "and the toolbar row must sit at the very top of the window.");

        // Walk both states up front so a single run already prints the whole matrix for this
        // alignment, then leave the window in the empty state -- the one the flag is really about.
        setMenuItems(shell, menuBar, true);
        report.run();
        setMenuItems(shell, menuBar, false);
        report.run();

        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }

    /** No margins, so the first tool button's x is exactly the width the menu button reserved. */
    private static RowLayout flushRowLayout() {
        RowLayout layout = new RowLayout(SWT.HORIZONTAL);
        layout.marginLeft = layout.marginRight = layout.marginTop = layout.marginBottom = 0;
        layout.marginWidth = layout.marginHeight = 0;
        layout.spacing = 0;
        return layout;
    }

    private static String parseAlign(String[] args) {
        String align = args.length > 0 ? args[0].trim().toLowerCase() : "vleft";
        if (align.equals("hleft") || align.equals("hright")
                || align.equals("vleft") || align.equals("vright")) {
            return align;
        }
        System.out.println("Unknown alignment '" + align + "', falling back to vleft.");
        return "vleft";
    }

    /**
     * Measures what the menu bar costs the main toolbar and compares it against what the alignment
     * says it should cost. An empty menu bar costs nothing in every alignment.
     */
    private static String check(Shell shell, Composite mainToolbar, Menu menuBar,
                               String align, boolean vertical, boolean atStart) {
        shell.layout(true, true);

        boolean hasItems = menuBar.getItemCount() > 0;
        boolean hasStrip = !vertical && hasItems;
        int expectedHeight = hasStrip
                ? MenuSizes.HEIGHT_HORIZONTAL_MENU
                : MenuSizes.HEIGHT_VERTICAL_MENU;
        int expectedIndent = vertical && hasItems && atStart ? MenuSizes.VERTICAL_MENU_BUTTON_WIDTH : 0;

        int actualHeight = mainToolbar.getSize().y;
        Control[] children = mainToolbar.getChildren();
        org.eclipse.swt.graphics.Rectangle first =
                children.length > 0 ? children[0].getBounds() : new org.eclipse.swt.graphics.Rectangle(0, 0, 0, 0);

        StringBuilder out = new StringBuilder();
        out.append("align=").append(align)
           .append("  menu items=").append(menuBar.getItemCount()).append('\n');
        // The toolbar row is centred in whatever is left below the menu strip. Without a strip that
        // band is the whole toolbar, so this also catches the row keeping the strip's offset after
        // the strip itself is gone -- which would squash the row instead of moving it up.
        int strip = hasStrip ? MenuSizes.MENU_BAR_HEIGHT : 0;
        int expectedY = strip + Math.max(0, (expectedHeight - strip - first.height) / 2);

        line(out, "main toolbar height", actualHeight, expectedHeight);
        line(out, "first tool button x", first.x, expectedIndent);
        line(out, "first tool button y", first.y, expectedY);
        System.out.println(out);
        return out.toString();
    }

    private static void line(StringBuilder out, String what, int actual, int expected) {
        out.append(actual == expected ? "PASS  " : "FAIL  ")
           .append(what).append(": ").append(actual)
           .append(" (expected ").append(expected).append(")\n");
    }

    private static void setMenuItems(Shell shell, Menu menuBar, boolean withItems) {
        for (MenuItem item : menuBar.getItems()) {
            item.dispose();
        }
        if (withItems) {
            addCascade(shell, menuBar, "File", "New", "Open", "Save");
            addCascade(shell, menuBar, "Edit", "Undo", "Redo");
            addCascade(shell, menuBar, "Help", "About");
        }
        // A BAR menu reaches Flutter inside its Shell's payload, so re-setting it is what marks the
        // Shell as changed; adding or removing items on its own leaves the Shell untouched.
        shell.setMenuBar(null);
        shell.setMenuBar(menuBar);
        shell.layout(true, true);
    }

    private static void addCascade(Shell shell, Menu menuBar, String text, String... entries) {
        MenuItem cascade = new MenuItem(menuBar, SWT.CASCADE);
        cascade.setText(text);
        Menu dropDown = new Menu(shell, SWT.DROP_DOWN);
        cascade.setMenu(dropDown);
        for (String entry : entries) {
            MenuItem item = new MenuItem(dropDown, SWT.PUSH);
            item.setText(entry);
            item.addSelectionListener(widgetSelectedAdapter(e -> System.out.println(text + " > " + entry)));
        }
    }

    private static void newToolBar(Composite parent, String... labels) {
        ToolBar bar = new ToolBar(parent, SWT.FLAT);
        for (String label : labels) {
            ToolItem item = new ToolItem(bar, SWT.PUSH);
            item.setText(label);
        }
        bar.pack();
    }
}
