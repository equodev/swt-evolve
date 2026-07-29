package dev.equo.jface;

import dev.equo.swt.Config;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.LineNumberRulerColumn;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

/**
 * Repro for issue #861: right-clicking the line numbers of an editor must open
 * the ruler context menu (Add Bookmark..., etc.).
 *
 * Mirrors AbstractTextEditor.createPartControl: the menu is set on
 * ruler.getControl() (a composite Canvas whose area is covered by the
 * line-number child canvas), items are (re)populated on SWT.Show like a
 * MenuManager with removeAllWhenShown(true).
 *
 * Run web:
 *   ./gradlew :swt-evolve:examples:runWebExample -PmainClass=dev.equo.jface.RulerContextMenuSnippet
 */
public class RulerContextMenuSnippet {

    public static void main(String[] args) throws ClassNotFoundException {
        Config.useEquo(StyledText.class);
        Config.useEquo(Class.forName("org.eclipse.swt.custom.StyledTextRenderer"));
        Config.useEquo(Menu.class);
        Config.useEquo(MenuItem.class);

        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("Ruler Context Menu Snippet (#861)");
        shell.setLayout(new GridLayout(1, false));
        shell.setSize(700, 520);

        CompositeRuler ruler = new CompositeRuler();
        LineNumberRulerColumn lineNumbers = new LineNumberRulerColumn();
        lineNumbers.setForeground(display.getSystemColor(SWT.COLOR_DARK_GREEN));
        ruler.addDecorator(0, lineNumbers);

        SourceViewer viewer = new SourceViewer(
                shell, ruler, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
        viewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Document document = new Document(
                "Line 1\n" +
                "Line 2\n" +
                "Line 3\n" +
                "Line 4\n" +
                "Line 5");
        viewer.getTextWidget().setForeground(new Color(0, 0, 0));
        viewer.getTextWidget().setBackground(display.getSystemColor(SWT.COLOR_WHITE));
        viewer.setDocument(document);

        // The ruler context menu, exactly like AbstractTextEditor wires #RulerContext:
        // set on ruler.getControl() and repopulated on every SWT.Show.
        Control rulerControl = ruler.getControl();
        Menu rulerMenu = new Menu(rulerControl);
        rulerMenu.addListener(SWT.Show, e -> {
            for (MenuItem item : rulerMenu.getItems()) {
                item.dispose();
            }
            MenuItem bookmark = new MenuItem(rulerMenu, SWT.PUSH);
            bookmark.setText("Add Bookmark...");
            bookmark.addListener(SWT.Selection, ev -> System.out.println("[861] Add Bookmark clicked"));

            MenuItem task = new MenuItem(rulerMenu, SWT.PUSH);
            task.setText("Add Task...");
            task.addListener(SWT.Selection, ev -> System.out.println("[861] Add Task clicked"));

            new MenuItem(rulerMenu, SWT.SEPARATOR);

            MenuItem showLines = new MenuItem(rulerMenu, SWT.CHECK);
            showLines.setText("Show Line Numbers");
            showLines.setSelection(true);
            showLines.addListener(SWT.Selection, ev -> System.out.println("[861] Show Line Numbers clicked"));
        });
        rulerControl.setMenu(rulerMenu);
        System.out.println("[861] ruler control = " + rulerControl.getClass().getName()
                + " children=" + ((Composite) rulerControl).getChildren().length);

        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        display.dispose();
    }
}
