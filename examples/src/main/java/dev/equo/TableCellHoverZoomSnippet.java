package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

/**
 * Spike: the hovered-ToolItem zoom, applied to Table cells. The content of the cell under the
 * pointer grows; the row keeps the height the size model laid the table out from.
 *
 * <pre>
 * ./gradlew :examples:runDeskExample -PmainClass=dev.equo.TableCellHoverZoomSnippet
 * </pre>
 *
 * <p>Two tables, because they answer different questions.
 *
 * <p><b>Top — plain cells.</b> Text, images, alignments. What to judge:
 * <ul>
 * <li>Does growing one cell read as feedback, or as the table wobbling? A toolbar icon is an island
 *     with space around it; a cell has a neighbour one pixel away in all four directions.</li>
 * <li>The grid lines are painted by the Table's own border, which Flutter draws <em>after</em> every
 *     cell — so a grown cell is crossed by the column divider and cannot cover it.</li>
 * <li>"Notes" is narrow on purpose: its text is already ellipsized, so zooming scales the ellipsis
 *     rather than revealing more text.</li>
 * <li>Right- and centre-aligned columns anchor the growth at their own edge.</li>
 * </ul>
 *
 * <p><b>Bottom — cells hosting real controls</b> through {@link TableEditor}: Combo, Text, a CHECK
 * Button, a PUSH Button and a Spinner. These are not painted by the cell at all — the editor puts a
 * live control on top of it — so the zoom should <em>not</em> reach them. That is the thing to
 * confirm: hovering a hosted control must leave it alone and keep it usable (type in the Text, open
 * the Combo, click the buttons), while the plain cells in the same row still zoom.
 */
public class TableCellHoverZoomSnippet {

    public static void main(String[] args) {
        Config.useEquo(Table.class);
        Config.useEquo(TableItem.class);
        Config.useEquo(TableColumn.class);
        Config.useEquo(Label.class);
        Config.useEquo(Combo.class);
        Config.useEquo(Button.class);
        Config.useEquo(Text.class);

        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("Table cell hover zoom");
        shell.setSize(860, 620);
        shell.setLayout(new GridLayout(1, false));

        Image green = square(display, display.getSystemColor(SWT.COLOR_DARK_GREEN));
        Image red = square(display, display.getSystemColor(SWT.COLOR_DARK_RED));

        label(shell, "Plain cells — hover anything. The content grows; row height and column "
                + "widths must not change.\nThe grid lines are drawn over every cell by the Table "
                + "border, so a grown cell is crossed by them.");
        buildPlainTable(shell, green, red);

        label(shell, "Cells hosting controls (TableEditor). Hovering these must NOT zoom them and "
                + "must leave them usable —\ntype in the Text, open the Combo, toggle the check, "
                + "click Run, change the Spinner. The 'Name' cells in the same rows still zoom.");
        buildEditorTable(shell, green, red);

        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }

    private static void buildPlainTable(Shell shell, Image green, Image red) {
        Table table = new Table(shell, SWT.BORDER | SWT.FULL_SELECTION);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
        data.heightHint = 190;
        table.setLayoutData(data);

        newColumn(table, "Status", 90, SWT.LEFT);
        newColumn(table, "Name", 200, SWT.LEFT);
        newColumn(table, "Count", 80, SWT.RIGHT);
        newColumn(table, "Centred", 100, SWT.CENTER);
        newColumn(table, "Notes", 120, SWT.LEFT);

        row(table, green, "Well A-12", "1420", "ok", "short");
        row(table, red, "Well B-07", "38", "check", "a much longer note that will be ellipsized");
        row(table, green, "Well C-31", "7", "ok", "another fairly long note here");
        row(table, green, "Reservoir model", "204", "ok", "fits");
        row(table, red, "Well D-02", "0", "fail", "short");
        for (int i = 1; i <= 8; i++) {
            row(table, i % 2 == 0 ? green : red, "Filler row " + i, String.valueOf(i * 13), "ok",
                    "note " + i);
        }
    }

    /**
     * Every cell kind an application actually puts in a table: a read-only Combo, an editable Text,
     * a CHECK Button, a PUSH Button and a Spinner, each hosted by its own {@link TableEditor}.
     */
    private static void buildEditorTable(Shell shell, Image green, Image red) {
        Table table = new Table(shell, SWT.BORDER | SWT.FULL_SELECTION);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
        data.heightHint = 220;
        table.setLayoutData(data);

        newColumn(table, "Status", 70, SWT.LEFT);
        newColumn(table, "Name", 160, SWT.LEFT);
        newColumn(table, "Combo", 130, SWT.LEFT);
        newColumn(table, "Text", 150, SWT.LEFT);
        newColumn(table, "Check", 70, SWT.CENTER);
        newColumn(table, "Action", 90, SWT.LEFT);
        newColumn(table, "Spinner", 90, SWT.LEFT);

        String[] phases = {"Drilling", "Completion", "Production", "Abandoned"};

        for (int i = 0; i < 6; i++) {
            TableItem item = new TableItem(table, SWT.NONE);
            item.setImage(0, i % 2 == 0 ? green : red);
            // Columns 2..6 are covered by an editor, so they carry no text of their own.
            item.setText(new String[] {"", "Well E-" + (10 + i), "", "", "", "", ""});

            Combo combo = new Combo(table, SWT.READ_ONLY);
            combo.setItems(phases);
            combo.select(i % phases.length);
            editor(table).setEditor(combo, item, 2);

            Text text = new Text(table, SWT.BORDER);
            text.setText("edit me " + i);
            editor(table).setEditor(text, item, 3);

            Button check = new Button(table, SWT.CHECK);
            check.setSelection(i % 3 == 0);
            editor(table).setEditor(check, item, 4);

            Button run = new Button(table, SWT.PUSH);
            run.setText("Run");
            editor(table).setEditor(run, item, 5);

            Spinner spinner = new Spinner(table, SWT.BORDER);
            spinner.setMinimum(0);
            spinner.setMaximum(100);
            spinner.setSelection(i * 7);
            editor(table).setEditor(spinner, item, 6);
        }
    }

    /** A TableEditor that fills its cell, which is what every hosted control here wants. */
    private static TableEditor editor(Table table) {
        TableEditor editor = new TableEditor(table);
        editor.grabHorizontal = true;
        editor.grabVertical = true;
        return editor;
    }

    private static void label(Shell shell, String text) {
        Label label = new Label(shell, SWT.WRAP);
        label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        label.setText(text);
    }

    private static void newColumn(Table table, String text, int width, int alignment) {
        TableColumn column = new TableColumn(table, alignment);
        column.setText(text);
        column.setWidth(width);
    }

    private static void row(Table table, Image image, String name, String count, String centred,
                            String note) {
        TableItem item = new TableItem(table, SWT.NONE);
        item.setImage(0, image);
        item.setText(new String[] {"", name, count, centred, note});
    }

    /** A flat 12x12 tile, so the zoom is judged on size alone and not on any artwork detail. */
    private static Image square(Display display, Color color) {
        Image image = new Image(display, 12, 12);
        GC gc = new GC(image);
        gc.setBackground(color);
        gc.fillRectangle(0, 0, 12, 12);
        gc.dispose();
        return image;
    }
}
