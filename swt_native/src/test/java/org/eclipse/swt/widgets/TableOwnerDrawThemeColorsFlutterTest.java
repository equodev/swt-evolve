package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.ConfigFlags;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The owner-draw GC carries the cell's colours as the application set them, whatever the theme, as
 * native SWT hands them to the listeners: Evolve keeps the text legible where it paints it, on the
 * ground actually under each string.
 *
 * <p>The shape is an Eclipse 4 workbench whose light CSS sets a results table to black on white, run
 * in the dark theme.
 */
@Tag("flutter-it")
class TableOwnerDrawThemeColorsFlutterTest {

    private static final RGB BLACK = new RGB(0, 0, 0);
    private static final RGB WHITE = new RGB(255, 255, 255);

    private RecordingBridge bridge;
    private Display display;
    private ConfigFlags savedFlags;

    @BeforeAll
    static void useEquo() {
        Config.forceEquo();
    }

    @AfterAll
    static void reset() {
        Config.defaultToEclipse();
    }

    @BeforeEach
    void setUp() {
        savedFlags = Config.getConfigFlags();
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed()) {
            display.dispose();
        }
        FlutterBridge.set(null);
        Config.setConfigFlags(savedFlags);
    }

    @Test
    void aTablesCssColours_reachTheGcAsSet_underTheDarkTheme() {
        start("dark");
        Shell shell = new Shell(display);
        Table table = new Table(shell, SWT.NONE);
        table.setBounds(0, 0, 300, 200);
        cssColoured(table);
        TableItem row = new TableItem(table, SWT.NONE);
        AtomicReference<RGB> eraseBackground = new AtomicReference<>();
        AtomicReference<RGB> paintForeground = new AtomicReference<>();
        listen(table, eraseBackground, paintForeground);

        paint(table, row.hashCode(), "Table");
        assertThat(OwnerDrawOverlay.isListening(row)).as("the row is owner-drawn").isTrue();

        assertCellColours(eraseBackground, paintForeground, BLACK, WHITE);
    }

    @Test
    void aTreesCssColours_reachTheGcAsSet_underTheDarkTheme() {
        start("dark");
        Shell shell = new Shell(display);
        Tree tree = new Tree(shell, SWT.NONE);
        tree.setBounds(0, 0, 300, 200);
        cssColoured(tree);
        TreeItem row = new TreeItem(tree, SWT.NONE);
        AtomicReference<RGB> eraseBackground = new AtomicReference<>();
        AtomicReference<RGB> paintForeground = new AtomicReference<>();
        listen(tree, eraseBackground, paintForeground);

        paint(tree, row.hashCode(), "Tree");
        assertThat(OwnerDrawOverlay.isListening(row)).as("the row is owner-drawn").isTrue();

        assertCellColours(eraseBackground, paintForeground, BLACK, WHITE);
    }

    @Test
    void anAppsRowColours_reachTheGcAsSet_underTheDarkTheme() {
        start("dark");
        Shell shell = new Shell(display);
        Table table = new Table(shell, SWT.NONE);
        table.setBounds(0, 0, 300, 200);
        cssColoured(table);
        TableItem row = new TableItem(table, SWT.NONE);
        Color red = new Color(display, 255, 0, 0);
        Color lightBlue = new Color(display, 204, 229, 255);
        row.setForeground(red);
        row.setBackground(lightBlue);
        AtomicReference<RGB> eraseBackground = new AtomicReference<>();
        AtomicReference<RGB> paintForeground = new AtomicReference<>();
        listen(table, eraseBackground, paintForeground);

        paint(table, row.hashCode(), "Table");

        assertCellColours(eraseBackground, paintForeground, red.getRGB(), lightBlue.getRGB());
        red.dispose();
        lightBlue.dispose();
    }

    // ---- harness ----------------------------------------------------------------------------------

    private void start(String theme) {
        ConfigFlags flags = new ConfigFlags();
        flags.force_theme = theme;
        Config.setConfigFlags(flags);
        bridge = new RecordingBridge();
        FlutterBridge.set(bridge);
        display = new Display();
    }

    /** Coloured as an Eclipse 4 light CSS colours a results list: black text on white. */
    private void cssColoured(Control control) {
        control.setForeground(display.getSystemColor(SWT.COLOR_BLACK));
        control.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
    }

    /** Listeners that clear the cell, then draw its text in the GC's colours, recording them. */
    private static void listen(Control control, AtomicReference<RGB> eraseBackground,
            AtomicReference<RGB> paintForeground) {
        control.addListener(SWT.EraseItem, event -> {
            eraseBackground.set(event.gc.getBackground().getRGB());
            event.detail &= ~SWT.FOREGROUND;
        });
        control.addListener(SWT.PaintItem, event -> {
            paintForeground.set(event.gc.getForeground().getRGB());
            event.gc.drawString("Result", event.x + 2, event.y + 2, true);
        });
    }

    private void paint(Control parent, int rowId, String type) {
        Event e = new Event();
        e.segments = new int[] { rowId };
        bridge.comm.fireContaining(type + "/" + parent.hashCode() + "/PaintItem/PaintItem", e);
        while (display.readAndDispatch()) {
            // run the handler's asyncExec
        }
        Object item = FlutterBridge.findById(rowId);
        if (item instanceof DartTableItem) ((DartTableItem) item).getTexts();
        if (item instanceof DartTreeItem) ((DartTreeItem) item).getTexts();
    }

    private static void assertCellColours(AtomicReference<RGB> eraseBackground,
            AtomicReference<RGB> paintForeground, RGB foreground, RGB background) {
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(paintForeground.get()).as("the GC's foreground is the cell's own")
                    .isEqualTo(foreground);
            softly.assertThat(eraseBackground.get()).as("the GC's background is the cell's own")
                    .isEqualTo(background);
        });
    }
}
