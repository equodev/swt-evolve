package dev.equo.nattable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.BiFunction;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
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
import org.eclipse.nebula.widgets.nattable.hover.HoverLayer;
import org.eclipse.nebula.widgets.nattable.hover.config.BodyHoverStylingBindings;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.grid.cell.AlternatingRowConfigLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.SelectionStyleLabels;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * The same hover + selection NatTable three times: with the application's colors, with Canvas theming
 * turned on ({@code disable_swt_canvas_colors_<Class>}), and themed with its hover and selection
 * colors declared ({@code canvas_fill_roles_<Class>}). Hover a row and click cells in each to compare
 * how the highlight colors survive theming.
 */
public class NatTableHoverThemingSnippet {

    /** Opts back out of the NatTable-wide flag, so the tables run side by side in one Display. */
    public static class AppColorsNatTable extends NatTable {
        public AppColorsNatTable(Composite parent, DefaultGridLayer layer) {
            super(parent, layer, false);
        }
    }

    /** Themed like any NatTable, with the fill roles declared for this class alone. */
    public static class DeclaredRolesNatTable extends NatTable {
        public DeclaredRolesNatTable(Composite parent, DefaultGridLayer layer) {
            super(parent, layer, false);
        }
    }

    public static void main(String[] args) {
        System.setProperty("swt.evolve.disable_swt_canvas_colors_" + NatTable.class.getName(), "true");
        System.setProperty("swt.evolve.disable_swt_canvas_colors_" + AppColorsNatTable.class.getName(), "false");
        System.setProperty("swt.evolve.canvas_fill_roles_" + DeclaredRolesNatTable.class.getName(),
                "F6E6D6:hover,DBC7B4:selection");

        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("NatTable hover/selection under Canvas theming");
        shell.setLayout(new GridLayout(3, true));

        label(shell, "Application colors");
        label(shell, "Themed");
        label(shell, "Themed, roles declared");
        table(shell, AppColorsNatTable::new);
        table(shell, (parent, grid) -> new NatTable(parent, grid, false));
        table(shell, DeclaredRolesNatTable::new);

        shell.setSize(1400, 450);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
    }

    private static void label(Composite parent, String text) {
        Label label = new Label(parent, SWT.NONE);
        label.setText(text);
    }

    private static void table(Composite parent, BiFunction<Composite, DefaultGridLayer, NatTable> create) {
        String[] properties = { "id", "name", "birthDate" };
        IDataProvider body = new ListDataProvider<>(people(),
                new ReflectiveColumnPropertyAccessor<SimplePerson>(properties));

        DefaultColumnHeaderDataProvider columnHeader = new DefaultColumnHeaderDataProvider(properties);
        DefaultRowHeaderDataProvider rowHeader = new DefaultRowHeaderDataProvider(body);
        HoverLayer hover = new HoverLayer(new DataLayer(body), false);
        DefaultGridLayer grid = new DefaultGridLayer(hover,
                new DefaultColumnHeaderDataLayer(columnHeader),
                new DefaultRowHeaderDataLayer(rowHeader),
                new DataLayer(new DefaultCornerDataProvider(columnHeader, rowHeader)));

        NatTable natTable = create.apply(parent, grid);
        natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
        natTable.addConfiguration(new BodyHoverStylingBindings(hover));
        natTable.addConfiguration(new HoverStyle());
        natTable.configure();
        natTable.setBackground(GUIHelper.COLOR_WHITE);
        natTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    }

    /**
     * A table styled for a light background: white with warm banding, hover and selection, each a
     * little darker than the white cells, and warm headers.
     */
    static class HoverStyle extends AbstractRegistryConfiguration {
        @Override
        public void configureRegistry(IConfigRegistry registry) {
            background(registry, DisplayMode.NORMAL, AlternatingRowConfigLabelAccumulator.ODD_ROW_CONFIG_TYPE,
                    GUIHelper.COLOR_WHITE);
            background(registry, DisplayMode.NORMAL, AlternatingRowConfigLabelAccumulator.EVEN_ROW_CONFIG_TYPE,
                    GUIHelper.getColor(252, 246, 237));
            background(registry, DisplayMode.HOVER, GridRegion.BODY, GUIHelper.getColor(246, 230, 214));
            background(registry, DisplayMode.SELECT, GridRegion.BODY, GUIHelper.getColor(219, 199, 180));
            background(registry, DisplayMode.SELECT, SelectionStyleLabels.SELECTION_ANCHOR_STYLE,
                    GUIHelper.getColor(219, 199, 180));
            background(registry, DisplayMode.NORMAL, GridRegion.COLUMN_HEADER, GUIHelper.getColor(154, 132, 120));
            background(registry, DisplayMode.NORMAL, GridRegion.CORNER, GUIHelper.getColor(154, 132, 120));
            background(registry, DisplayMode.NORMAL, GridRegion.ROW_HEADER, GUIHelper.getColor(234, 230, 227));
            background(registry, DisplayMode.SELECT, GridRegion.COLUMN_HEADER, GUIHelper.getColor(219, 199, 180));
            background(registry, DisplayMode.SELECT, GridRegion.ROW_HEADER, GUIHelper.getColor(219, 199, 180));
            registry.registerConfigAttribute(CellConfigAttributes.GRID_LINE_COLOR, GUIHelper.getColor(194, 180, 154));
        }

        private static void background(IConfigRegistry registry, DisplayMode mode, String label, Color color) {
            Style style = new Style();
            style.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, color);
            registry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, style, mode, label);
        }
    }

    private static List<SimplePerson> people() {
        String[] names = { "Mickey Mouse", "Batman", "Bender", "Cartman", "Dogbert", "Homer", "Lisa",
                "Marge", "Bart", "Maggie" };
        List<SimplePerson> people = new ArrayList<>();
        for (int i = 0; i < names.length; i++) {
            people.add(new SimplePerson(100 + 10 * i, names[i], new Date(1000000L * (i + 1))));
        }
        return people;
    }
}
