package dev.equo.jface;

import dev.equo.swt.Config;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.LineNumberRulerColumn;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

/**
 * Run with:
 *   ./gradlew :examples:runExample -PmainClass=dev.equo.SourceViewerRulerSnippet
 *
 * 1. Click inside the editor, press Enter to add lines.
 *    Do the line numbers update immediately?
 * 2. Click on the grey area below the editor to trigger focus-out.
 *    Do the line numbers update now?
 *
 * Console: [modify] fires on every keystroke, [ruler paint] fires when
 * Flutter actually repaints the ruler.
 */
public class SourceViewerRulerSnippet {

    public static void main(String[] args) throws ClassNotFoundException {
        Config.useEquo(StyledText.class);
        Config.useEquo(Class.forName("org.eclipse.swt.custom.StyledTextRenderer"));
        Config.useEquo(Menu.class);
        Config.useEquo(MenuItem.class);

        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("SourceViewer Ruler Snippet");
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
                "Line 5"
        );
        viewer.getTextWidget().setForeground(new Color(0,0,0));
        viewer.getTextWidget().setBackground(display.getSystemColor(SWT.COLOR_WHITE));
        viewer.setDocument(document);

        // Dynamic context menu — items populated in Show listener (simulates menuAboutToShow)
        Menu menu = new Menu(shell, SWT.POP_UP);
        menu.addListener(SWT.Show, e -> {
            for (MenuItem item : menu.getItems()) {
                item.dispose();
            }
            MenuItem cut = new MenuItem(menu, SWT.PUSH);
            cut.setText("Cut");
            cut.addListener(SWT.Selection, ev -> System.out.println("Cut clicked"));

            MenuItem copy = new MenuItem(menu, SWT.PUSH);
            copy.setText("Copy");
            copy.addListener(SWT.Selection, ev -> System.out.println("Copy clicked"));

            MenuItem paste = new MenuItem(menu, SWT.PUSH);
            paste.setText("Paste");
            paste.addListener(SWT.Selection, ev -> System.out.println("Paste clicked"));

            new MenuItem(menu, SWT.SEPARATOR);

            MenuItem selectAll = new MenuItem(menu, SWT.PUSH);
            selectAll.setText("Select All");
            selectAll.addListener(SWT.Selection, ev -> viewer.getTextWidget().selectAll());
        });
        viewer.getTextWidget().setMenu(menu);

        ruler.getControl().addPaintListener(e ->
                System.out.println("[ruler paint] lines=" + document.getNumberOfLines()));

        viewer.getTextWidget().addModifyListener(e ->
                System.out.println("[modify] lines=" + document.getNumberOfLines()));

        viewer.getTextWidget().addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { System.out.println("[focus] gained"); }
            @Override public void focusLost(FocusEvent e)   { System.out.println("[focus] lost"); }
        });

        // Clickable area below the editor — clicking here moves focus away from StyledText
        Label clickArea = new Label(shell, SWT.BORDER);
        clickArea.setText("← click here to trigger focus-out");
        clickArea.setBackground(display.getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
        clickArea.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        display.dispose();
    }
}