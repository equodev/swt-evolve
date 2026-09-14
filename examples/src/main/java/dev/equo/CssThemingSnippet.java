package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Spinner;

/**
 * An application that paints itself the way the e4 CSS engine does: the engine only ever reaches
 * SWT through {@link Control#setBackground}, {@link Control#setForeground} and
 * {@link Control#setFont}, so calling them directly is the same input the engine produces from a
 * stylesheet. Toggle {@code Config.useEquo} / {@code Config.useEclipse} below to compare.
 */
public class CssThemingSnippet {

    // Two stylesheets, the way a product ships a light and a dark theme. Which one applies
    // follows -Dswt.evolve.force_theme, the same switch that picks the base Evolve renders the
    // rest of the chrome with, so each palette lands on the base it was designed for. A serif
    // face, so the font axis is visible next to the theme's sans-serif.
    private static Color pageBackground;
    private static Color pageForeground;
    private static Color accentBackground;
    private static Color accentForeground;
    private static Font themeFont;

    public static void main(String[] args) {
        Config.forceEquo();
        Display display = new Display();

        boolean light = "light".equals(System.getProperty("swt.evolve.force_theme"));
        if (light) {
            pageBackground = new Color(display, 0xFA, 0xF6, 0xEE);
            pageForeground = new Color(display, 0x2E, 0x2A, 0x24);
            accentBackground = new Color(display, 0x1F, 0x6F, 0x8B);
            accentForeground = new Color(display, 0xFF, 0xFF, 0xFF);
        } else {
            pageBackground = new Color(display, 0x1E, 0x24, 0x30);
            pageForeground = new Color(display, 0xE8, 0xE2, 0xD2);
            accentBackground = new Color(display, 0x3F, 0xA7, 0xA0);
            accentForeground = new Color(display, 0x0E, 0x1A, 0x1A);
        }
        themeFont = new Font(display, "Georgia", 13, SWT.NORMAL);

        Shell shell = new Shell(display);
        shell.setText("CssThemingSnippet");
        shell.setLayout(new GridLayout(1, false));

        Composite page = new Composite(shell, SWT.NONE);
        page.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        page.setLayout(new GridLayout(2, false));

        Label label = new Label(page, SWT.NONE);
        label.setText("Label styled by the application");

        Button button = new Button(page, SWT.PUSH);
        button.setText("Button");

        Text text = new Text(page, SWT.BORDER);
        text.setText("Text");
        text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        Label spacer = new Label(page, SWT.NONE);
        spacer.setText("");

        Tree tree = new Tree(page, SWT.BORDER);
        tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        for (int i = 0; i < 3; i++) {
            TreeItem root = new TreeItem(tree, SWT.NONE);
            root.setText("Tree node " + i);
            new TreeItem(root, SWT.NONE).setText("child " + i);
            root.setExpanded(true);
        }

        Table table = new Table(page, SWT.BORDER | SWT.FULL_SELECTION);
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        for (int i = 0; i < 3; i++) {
            new TableItem(table, SWT.NONE).setText("Table row " + i);
        }

        Group group = new Group(page, SWT.NONE);
        group.setText("Group");
        group.setLayout(new GridLayout(1, false));
        new CLabel(group, SWT.NONE).setText("CLabel");
        new Link(group, SWT.NONE).setText("a <a>Link</a> here");

        Composite widgets = new Composite(page, SWT.NONE);
        widgets.setLayout(new GridLayout(2, false));
        Combo combo = new Combo(widgets, SWT.READ_ONLY);
        combo.setItems("one", "two");
        combo.select(0);
        new Spinner(widgets, SWT.NONE).setSelection(3);
        new DateTime(widgets, SWT.DATE);
        new Slider(widgets, SWT.HORIZONTAL).setSelection(40);
        ProgressBar bar = new ProgressBar(widgets, SWT.HORIZONTAL);
        bar.setSelection(40);
        new Scale(widgets, SWT.HORIZONTAL).setSelection(40);

        List list = new List(page, SWT.BORDER | SWT.V_SCROLL);
        list.setItems("List item 0", "List item 1", "List item 2");
        list.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        StyledText styled = new StyledText(page, SWT.BORDER | SWT.MULTI);
        styled.setText("StyledText run underline\nselected");
        styled.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        // The setters the CSS engine drives beyond Control's three: the selection highlight, the
        // margins, and a run's own background and underline colour.
        styled.setSelectionBackground(accentBackground);
        styled.setSelectionForeground(accentForeground);
        styled.setMargins(20, 4, 4, 4);
        styled.setMarginColor(accentBackground);
        StyleRange run = new StyleRange(11, 3, accentForeground, accentBackground);
        StyleRange underline = new StyleRange();
        underline.start = 15;
        underline.length = 9;
        underline.underline = true;
        underline.underlineColor = accentBackground;
        styled.setStyleRanges(new StyleRange[] { run, underline });
        styled.setSelection(25, 33);

        ToolBar toolBar = new ToolBar(page, SWT.HORIZONTAL);
        GridData toolData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        toolData.horizontalSpan = 2;
        toolBar.setLayoutData(toolData);
        for (String name : new String[] { "ToolItem A", "ToolItem B" }) {
            new ToolItem(toolBar, SWT.PUSH).setText(name);
        }

        TabFolder tabs = new TabFolder(page, SWT.NONE);
        GridData tabsData = new GridData(SWT.FILL, SWT.FILL, true, true);
        tabsData.horizontalSpan = 2;
        tabs.setLayoutData(tabsData);
        TabItem tabItem = new TabItem(tabs, SWT.NONE);
        tabItem.setText("TabItem");
        Label tabContent = new Label(tabs, SWT.NONE);
        tabContent.setText("TabFolder body");
        tabItem.setControl(tabContent);

        CTabFolder folder = new CTabFolder(page, SWT.BORDER);
        GridData folderData = new GridData(SWT.FILL, SWT.FILL, true, true);
        folderData.horizontalSpan = 2;
        folder.setLayoutData(folderData);
        CTabItem tab = new CTabItem(folder, SWT.NONE);
        tab.setText("Tab");
        Label tabBody = new Label(folder, SWT.NONE);
        tabBody.setText("Tab body");
        tab.setControl(tabBody);
        folder.setSelection(tab);

        // What the CSS engine does once the tree exists.
        applyStyles(shell);

        shell.setSize(640, 520);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        display.dispose();
    }

    private static void applyStyles(Control control) {
        boolean accent = control instanceof Button || control instanceof CTabFolder;
        control.setBackground(accent ? accentBackground : pageBackground);
        control.setForeground(accent ? accentForeground : pageForeground);
        control.setFont(themeFont);
        if (control instanceof Composite) {
            for (Control child : ((Composite) control).getChildren()) {
                applyStyles(child);
            }
        }
    }
}
