package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import javax.swing.text.TableView;

/**
 * Comprehensive snippet to test Font support across multiple SWT widgets
 */
public class FontWidgetsSnippet {
    public static void main(String[] args) {
        Config.forceEquo();
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("Font Test - Multiple Widgets");
        shell.setSize(700, 1000);
        GridLayout layout = new GridLayout(2, false);
        layout.makeColumnsEqualWidth = true;
        shell.setLayout(layout);

        // Create various fonts
        Font arial14 = new Font(display, "Arial", 14, SWT.NORMAL);
        Font arial16Bold = new Font(display, "Arial", 16, SWT.BOLD);
        Font courier12 = new Font(display, "Courier New", 12, SWT.NORMAL);
        Font courier14Bold = new Font(display, "Courier New", 14, SWT.BOLD);
        Font times14Italic = new Font(display, "Times New Roman", 14, SWT.ITALIC);
        Font times16BoldItalic = new Font(display, "Times New Roman", 16, SWT.BOLD | SWT.ITALIC);
        Font helvetica13 = new Font(display, "Helvetica", 13, SWT.NORMAL);
        Font verdana12Bold = new Font(display, "Verdana", 12, SWT.BOLD);

        // 1. Label widgets
        createLabel(shell, "Label with Arial 14", arial14);
        createLabel(shell, "Label with Arial 16 Bold", arial16Bold);

        // 2. Button widgets
        createButton(shell, "Button with Courier 12", courier12);
        createButton(shell, "Button with Courier 14 Bold", courier14Bold);

        // 3. Text widgets
        createText(shell, "Text input with Times 14 Italic", times14Italic);
        createText(shell, "Text input with Times 16 Bold+Italic", times16BoldItalic);

        // 4. Combo widgets
        //createCombo(shell, "Combo with Helvetica 13", helvetica13);
        //createCombo(shell, "Combo with Verdana 12 Bold", verdana12Bold);

        // 5. List widget
        createList(shell, "List with Arial 14", arial14);
        createList(shell, "List with Courier 12", courier12);

        // 6. Group widgets
        createGroup(shell, "Group with Arial 16 Bold", arial16Bold, times14Italic);
        createGroup(shell, "Group with Verdana 12 Bold", verdana12Bold, helvetica13);

        // 7. Link widget
        createLink(shell, "Link with Times 14 Italic", times14Italic);
        createLink(shell, "Link with Arial 16 Bold", arial16Bold);

        // 8. Table widget
        createTable(shell, "Table with Arial 14", arial14);
        createTable(shell, "Table with Courier 12", courier12);

        // 9. Tree widget
        createTree(shell, "Tree with Helvetica 13", helvetica13);
        createTree(shell, "Tree with Times 14 Italic", times14Italic);

        // 10. StyledText widget
//        createStyledText(shell, "StyledText with Arial 14", arial14);
//        createStyledText(shell, "StyledText with Verdana 12 Bold", verdana12Bold);

        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }

        // Dispose fonts
        arial14.dispose();
        arial16Bold.dispose();
        courier12.dispose();
        courier14Bold.dispose();
        times14Italic.dispose();
        times16BoldItalic.dispose();
        helvetica13.dispose();
        verdana12Bold.dispose();

        display.dispose();
    }

    private static void createLabel(Shell parent, String text, Font font) {
        Label label = new Label(parent, SWT.NONE);
        label.setText(text);
        label.setFont(font);
        label.setBackground(new Color(255, 255, 254));
        label.setForeground(new Color(1, 2, 3));
        GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        label.setLayoutData(gd);
    }

    private static void createButton(Shell parent, String text, Font font) {
        Button button = new Button(parent, SWT.PUSH);
        button.setText(text);
        button.setFont(font);
//        button.setBackground(new Color(255, 255, 254));
//        button.setForeground(new Color(1, 2, 3));
        GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        button.setLayoutData(gd);
    }

    private static void createText(Shell parent, String defaultText, Font font) {
        Text text = new Text(parent, SWT.BORDER);
        text.setText(defaultText);
        text.setMessage("Message: " + defaultText);
        text.setFont(font);
        text.setBackground(new Color(255, 255, 254));
        text.setForeground(new Color(1, 2, 3));
        GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        text.setLayoutData(gd);
    }

    private static void createCombo(Shell parent, String text, Font font) {
        Combo combo = new Combo(parent, SWT.DROP_DOWN);
        combo.add("Option 1 - " + text);
        combo.add("Option 2 - " + text);
        combo.add("Option 3 - " + text);
        combo.select(0);
        combo.setFont(font);
        GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        combo.setLayoutData(gd);
    }

    private static void createList(Shell parent, String prefix, Font font) {
        List list = new List(parent, SWT.BORDER | SWT.V_SCROLL);
        String[] items = {prefix + " - Item 1", prefix + " - Item 2", prefix + " - Item 3"};
        list.setItems(items);
        list.setFont(font);
        list.setBackground(new Color(255, 255, 254));
        list.setForeground(new Color(1, 2, 3));
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
        gd.heightHint = 60;
        list.setLayoutData(gd);
    }

    private static void createGroup(Shell parent, String title, Font titleFont, Font contentFont) {
        Group group = new Group(parent, SWT.NONE);
        group.setText(title);
        group.setFont(titleFont);
        group.setLayout(new GridLayout(1, false));
        group.setForeground(new Color(255, 255, 254));
        group.setBackground(new Color(1, 2, 3));
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
        gd.heightHint = 80;
        group.setLayoutData(gd);
        Label label = new Label(group, SWT.NONE);
        label.setText("Content inside group");
        label.setFont(contentFont);
        label.setForeground(new Color(255, 255, 254));
        label.setBackground(new Color(1, 2, 3));
    }

    private static void createLink(Shell parent, String text, Font font) {
        Link link = new Link(parent, SWT.NONE);
        link.setText("<a>" + text + "</a>");
        link.setFont(font);
        GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        link.setLayoutData(gd);
        link.setBackground(new Color(255, 255, 254));
        link.setLinkForeground(new Color(255, 0, 254));
    }

    private static void createTable(Shell parent, String prefix, Font font) {
        Table table = new Table(parent, SWT.BORDER | SWT.FULL_SELECTION);
        table.setHeaderVisible(true);
        table.setFont(font);
        table.setForeground(new Color(255, 255, 254));

        TableColumn col1 = new TableColumn(table, SWT.LEFT);
        col1.setText("Column 1");
        col1.setWidth(150);

        TableColumn col2 = new TableColumn(table, SWT.LEFT);
        col2.setText("Column 2");
        col2.setWidth(150);

        TableItem item1 = new TableItem(table, SWT.NONE);
        item1.setText(new String[]{prefix + " Row 1 Col 1", "Data 1"});
        TableItem item2 = new TableItem(table, SWT.NONE);
        item2.setText(new String[]{prefix + " Row 2 Col 1", "Data 2"});

        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
        gd.heightHint = 80;
        table.setLayoutData(gd);
    }

    private static void createTree(Shell parent, String prefix, Font font) {
        Tree tree = new Tree(parent, SWT.BORDER);
        tree.setFont(font);

        TreeItem item1 = new TreeItem(tree, SWT.NONE);
        item1.setText(prefix + " - Parent 1");

        TreeItem child1 = new TreeItem(item1, SWT.NONE);
        child1.setText(prefix + " - Child 1.1");

        TreeItem child2 = new TreeItem(item1, SWT.NONE);
        child2.setText(prefix + " - Child 1.2");

        TreeItem item2 = new TreeItem(tree, SWT.NONE);
        item2.setText(prefix + " - Parent 2");

        item1.setExpanded(true);

        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
        gd.heightHint = 80;
        tree.setLayoutData(gd);
    }

    private static void createStyledText(Shell parent, String text, Font font) {
        org.eclipse.swt.custom.StyledText styledText =
            new org.eclipse.swt.custom.StyledText(parent, SWT.BORDER | SWT.WRAP);
        styledText.setTopIndex(0);
        styledText.setText(text + "\nThis is a multi-line\nStyledText widget");
        styledText.setFont(font);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.heightHint = 60;
        styledText.setLayoutData(gd);
    }
}