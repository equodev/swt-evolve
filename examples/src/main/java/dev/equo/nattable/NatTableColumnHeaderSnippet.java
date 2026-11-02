package dev.equo.nattable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultColumnHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultCornerDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultRowHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultColumnHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultGridLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultRowHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.IStyle;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * A NatTable grid with the default column header, opened from a button the way a data view opens
 * once the application is up: a fixed-height header row that NatTable scales by
 * {@code Display.getDPI()}, painted with the header font. Run it above 100 % display scale to compare
 * the header row against the text drawn into it; the label under the button gives both heights.
 */
public class NatTableColumnHeaderSnippet {

    private static final String[] PROPERTIES = { "id", "name", "birthDate" };

    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("NatTable column header");
        shell.setLayout(new GridLayout());

        Button open = new Button(shell, SWT.PUSH);
        open.setText("Add data");
        Label heights = new Label(shell, SWT.NONE);
        heights.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        open.addListener(SWT.Selection, e -> {
            open.setEnabled(false);
            NatTable natTable = createTable(shell);
            natTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
            heights.setText("Header row " + natTable.getRowHeightByPosition(0)
                    + " px, header text " + headerTextHeight(natTable) + " px");
            shell.layout(true, true);
        });

        shell.setSize(600, 340);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
    }

    private static NatTable createTable(Shell shell) {
        IDataProvider body = new ListDataProvider<>(people(),
                new ReflectiveColumnPropertyAccessor<SimplePerson>(PROPERTIES));
        DefaultColumnHeaderDataProvider columnHeader = new DefaultColumnHeaderDataProvider(PROPERTIES);
        DefaultRowHeaderDataProvider rowHeader = new DefaultRowHeaderDataProvider(body);
        DefaultGridLayer grid = new DefaultGridLayer(new DataLayer(body),
                new DefaultColumnHeaderDataLayer(columnHeader),
                new DefaultRowHeaderDataLayer(rowHeader),
                new DataLayer(new DefaultCornerDataProvider(columnHeader, rowHeader)));

        NatTable natTable = new NatTable(shell, grid, false);
        natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
        natTable.configure();
        natTable.setBackground(GUIHelper.COLOR_WHITE);
        return natTable;
    }

    /** The height of a header label in the font the column header paints with. */
    private static int headerTextHeight(NatTable natTable) {
        IStyle style = natTable.getConfigRegistry().getConfigAttribute(CellConfigAttributes.CELL_STYLE,
                DisplayMode.NORMAL, GridRegion.COLUMN_HEADER);
        Font font = style.getAttributeValue(CellStyleAttributes.FONT);
        GC gc = new GC(natTable);
        try {
            gc.setFont(font);
            return gc.textExtent(PROPERTIES[1]).y;
        } finally {
            gc.dispose();
        }
    }

    private static List<SimplePerson> people() {
        String[] names = { "Mickey Mouse", "Batman", "Bender", "Cartman", "Dogbert", "Homer" };
        List<SimplePerson> people = new ArrayList<>();
        for (int i = 0; i < names.length; i++) {
            people.add(new SimplePerson(100 + 10 * i, names[i], new Date(1000000L * (i + 1))));
        }
        return people;
    }
}
