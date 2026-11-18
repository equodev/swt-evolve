package org.eclipse.swt.widgets;

import dev.equo.swt.FontMetricsUtil;
import dev.equo.swt.SerializeTestBase;
import dev.equo.swt.size.TableItemTheme;
import dev.equo.swt.size.TableSizes;
import dev.equo.swt.size.TreeItemTheme;
import dev.equo.swt.size.TreeSizes;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.GCHelper;
import org.eclipse.swt.graphics.VGC;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.function.IntSupplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.swt.widgets.Mocks.shell;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

/**
 * An application that sets {@code org.eclipse.swt.internal.cocoa.useNativeItemHeight=false} asks for
 * rows one text line tall, and an owner-drawing one paints its row from the event's top edge on that
 * assumption. The default row keeps the theme's height.
 */
@Tag("native-unit")
class CompactItemHeightNativeTest extends SerializeTestBase {

    private static int compact(IntSupplier itemHeight) {
        System.setProperty(DisplayHelper.USE_NATIVE_ITEM_HEIGHT, "false");
        try {
            return itemHeight.getAsInt();
        } finally {
            System.clearProperty(DisplayHelper.USE_NATIVE_ITEM_HEIGHT);
        }
    }

    /** One text line plus the compact row padding measured on the Dart Table. */
    private static int compactTableRow() {
        return (int) Math.ceil(FontMetricsUtil.getFontSize("Ag", TableItemTheme.get().textStyle()).y()
                + TableSizes.COMPACT_ROW_PADDING_VERTICAL);
    }

    /** One text line plus the row padding measured on the Dart Tree, which a compact row keeps. */
    private static int compactTreeRow() {
        return (int) Math.ceil(FontMetricsUtil.getFontSize("Ag", TreeItemTheme.get().textStyle()).y()
                + TreeSizes.ROW_PADDING_VERTICAL);
    }

    @Test
    @DisplayName("a Table keeps the theme's row height by default")
    void tableDefaultRow() {
        Table table = new Table(shell(), SWT.NONE);
        new TableItem(table, SWT.NONE).setText("Sample");

        assertThat(table.getItemHeight()).isEqualTo(24);
    }

    @Test
    @DisplayName("a Table's row is one text line tall when native item heights are off")
    void tableCompactRow() {
        Shell shell = shell();
        Table table = new Table(shell, SWT.NONE);
        int empty = compact(table::getItemHeight);
        new TableItem(table, SWT.NONE).setText("Sample");
        int filled = compact(table::getItemHeight);

        assertThat(filled).isLessThan(24).isEqualTo(compactTableRow()).isEqualTo(empty);
    }

    /** Font heights a PaintItem listener reads: the GC's own font, then fonts it sets. */
    private record PaintFonts(int own, int system, int systemExtent, int built, int otherHeight) {}

    private static PaintFonts paintFonts(Control owner, Item item) {
        Display display = owner.getDisplay();
        FontData face = display.getSystemFont().getFontData()[0];
        // As an application may build it: a new font from the system font's name and height.
        Font built = new Font(display, face.getName(), face.getHeight(), SWT.NORMAL);
        Font other = new Font(display, face.getName(), 9, SWT.NORMAL);
        int[] seen = new int[5];
        owner.addListener(SWT.PaintItem, e -> {
            seen[0] = e.gc.getFontMetrics().getHeight();
            e.gc.setFont(display.getSystemFont());
            seen[1] = e.gc.getFontMetrics().getHeight();
            seen[2] = e.gc.textExtent("Ag").y;
            e.gc.setFont(built);
            seen[3] = e.gc.stringExtent("Ag").y;
            e.gc.setFont(other);
            seen[4] = e.gc.getFontMetrics().getHeight();
        });
        // The mock display swallows every event; its real dispatch only needs the listener table.
        DartDisplay impl = (DartDisplay) display.getImpl();
        doCallRealMethod().when(impl).sendEvent(any(EventTable.class), any(Event.class));
        try {
            OwnerDrawOverlay.paint(owner, item, 1);
        } finally {
            doNothing().when(impl).sendEvent(any(EventTable.class), any(Event.class));
        }
        return new PaintFonts(seen[0], seen[1], seen[2], seen[3], seen[4]);
    }

    private static int canvasFontHeight(Shell shell, Font font) {
        return onCanvas(shell, font, gc -> gc.getFontMetrics().getHeight());
    }

    private static int canvasExtent(Shell shell, Font font) {
        return onCanvas(shell, font, gc -> gc.textExtent("Ag").y);
    }

    private static int onCanvas(Shell shell, Font font, java.util.function.ToIntFunction<GC> read) {
        GC gc = new GC(new Canvas(shell, SWT.NONE));
        try {
            gc.setFont(font);
            return read.applyAsInt(gc);
        } finally {
            gc.dispose();
        }
    }

    @Test
    @DisplayName("a compact Table's PaintItem GC draws and measures the system font at the native height")
    void compactPaintItemSystemFont() {
        Shell shell = shell();
        Display display = shell.getDisplay();
        Table table = new Table(shell, SWT.NONE);
        TableItem item = new TableItem(table, SWT.NONE);
        item.setText("Show log");
        FontData face = display.getSystemFont().getFontData()[0];
        Font nativeSystem = new Font(display, face.getName(), 13, SWT.NORMAL);
        int nativeHeight = canvasFontHeight(shell, nativeSystem);
        int themeHeight = canvasFontHeight(shell, display.getSystemFont());
        int otherHeight = canvasFontHeight(shell, new Font(display, face.getName(), 9, SWT.NORMAL));

        Table regular = new Table(shell, SWT.NONE);
        int themeRowFont = paintFonts(regular, new TableItem(regular, SWT.NONE)).own();
        PaintFonts[] fonts = new PaintFonts[1];
        int itemHeight = compact(() -> {
            fonts[0] = paintFonts(table, item);
            return table.getItemHeight();
        });

        assertThat(nativeHeight).as("the test display's system font is not 13 pt").isNotEqualTo(themeHeight);
        assertThat(fonts[0].own()).as("the row's theme font, as without the flag").isEqualTo(themeRowFont);
        int nativeLine = GCHelper.NATIVE_SYSTEM_FONT_ASCENT + GCHelper.NATIVE_SYSTEM_FONT_DESCENT;
        assertThat(fonts[0].system()).as("gc.setFont(display.getSystemFont())").isEqualTo(nativeLine);
        assertThat(fonts[0].systemExtent()).as("its textExtent").isEqualTo(nativeLine);
        assertThat(fonts[0].built()).as("a font built like the system font").isEqualTo(nativeLine);
        assertThat(fonts[0].otherHeight()).as("a font at another height").isEqualTo(otherHeight);
        assertThat(itemHeight).as("the row fits the native system font").isEqualTo(18);
        assertThat(display.getSystemFont().getFontData()[0].getHeight()).isEqualTo(face.getHeight());
        assertThat(canvasFontHeight(shell, display.getSystemFont())).as("outside the event").isEqualTo(themeHeight);
    }

    /** What a GC reports for its font, and the y and flags {@code drawText(TEXT, 10, 5, flags)} sends. */
    private record LineBox(int ascent, int descent, int leading, int height, int extent, int drawnY, int drawnFlags) {}

    private static final int DRAW_Y = 5;

    private static LineBox lineBox(GC gc, Font font, int flags) {
        gc.setFont(font);
        FontMetrics m = gc.getFontMetrics();
        int extent = gc.textExtent("Show log").y;
        gc.drawText("Show log", 10, DRAW_Y, flags);
        VGC.VGCDrawTextStringintintint sent = VGC.VGCDrawTextStringintintint.reused();
        return new LineBox(m.getAscent(), m.getDescent(), m.getLeading(), m.getHeight(), extent, sent.y, sent.flags);
    }

    private static LineBox paintLineBox(Table table, TableItem item, java.util.function.Function<Display, Font> font, int flags) {
        Display display = table.getDisplay();
        LineBox[] seen = new LineBox[1];
        table.addListener(SWT.PaintItem, e -> seen[0] = lineBox(e.gc, font.apply(display), flags));
        DartDisplay impl = (DartDisplay) display.getImpl();
        doCallRealMethod().when(impl).sendEvent(any(EventTable.class), any(Event.class));
        try {
            OwnerDrawOverlay.paint(table, item, 1);
        } finally {
            doNothing().when(impl).sendEvent(any(EventTable.class), any(Event.class));
        }
        return seen[0];
    }

    private static LineBox canvasLineBox(Shell shell, Font font, int flags) {
        GC gc = new GC(new Canvas(shell, SWT.NONE));
        try {
            return lineBox(gc, font, flags);
        } finally {
            gc.dispose();
        }
    }

    @Test
    @DisplayName("a compact Table's PaintItem GC draws the system font in native's line box")
    void compactPaintItemNativeLineBox() {
        Shell shell = shell();
        Display display = shell.getDisplay();
        Table table = new Table(shell, SWT.NONE);
        TableItem item = new TableItem(table, SWT.NONE);
        FontData face = display.getSystemFont().getFontData()[0];
        Font evolve13 = new Font(display, face.getName(), GCHelper.NATIVE_SYSTEM_FONT_HEIGHT, SWT.NORMAL);
        LineBox outside = canvasLineBox(shell, evolve13, SWT.DRAW_TRANSPARENT);

        LineBox transparent = compactPaint(shell, table, item, SWT.DRAW_TRANSPARENT);
        LineBox opaque = compactPaint(shell, table, item, 0);

        int nativeLine = GCHelper.NATIVE_SYSTEM_FONT_ASCENT + GCHelper.NATIVE_SYSTEM_FONT_DESCENT;
        assertThat(outside.ascent()).as("the test font's own ascent differs from native's").isNotEqualTo(GCHelper.NATIVE_SYSTEM_FONT_ASCENT);
        assertThat(transparent.ascent()).isEqualTo(GCHelper.NATIVE_SYSTEM_FONT_ASCENT);
        assertThat(transparent.descent()).isEqualTo(GCHelper.NATIVE_SYSTEM_FONT_DESCENT);
        assertThat(transparent.leading()).isZero();
        assertThat(transparent.height()).isEqualTo(nativeLine);
        assertThat(transparent.extent()).isEqualTo(nativeLine);
        assertThat(transparent.drawnY() + outside.ascent()).as("the drawn baseline")
                .isEqualTo(DRAW_Y + GCHelper.NATIVE_SYSTEM_FONT_ASCENT);
        assertThat(opaque.drawnY()).isEqualTo(transparent.drawnY());
        assertThat(opaque.drawnFlags() & SWT.DRAW_TRANSPARENT).as("its background is filled over native's box").isNotZero();
        assertThat(canvasLineBox(shell, evolve13, 0)).as("outside the event").isEqualTo(new LineBox(outside.ascent(),
                outside.descent(), outside.leading(), outside.height(), outside.extent(), DRAW_Y, 0));
    }

    private static LineBox compactPaint(Shell shell, Table table, TableItem item, int flags) {
        LineBox[] box = new LineBox[1];
        compact(() -> {
            box[0] = paintLineBox(table, item, Display::getSystemFont, flags);
            return 0;
        });
        return box[0];
    }

    /** Runs {@code paint} on a compact Table row's PaintItem GC. */
    private static void compactPaintGc(Table table, java.util.function.Consumer<GC> paint) {
        TableItem item = new TableItem(table, SWT.NONE);
        Listener listener = e -> paint.accept(e.gc);
        table.addListener(SWT.PaintItem, listener);
        try (Mocks.Delivery ignored = Mocks.deliverEvents(table.getDisplay())) {
            compact(() -> {
                OwnerDrawOverlay.paint(table, item, 1);
                return 0;
            });
        } finally {
            table.removeListener(SWT.PaintItem, listener);
        }
    }

    private static int nativeLine() {
        return GCHelper.NATIVE_SYSTEM_FONT_ASCENT + GCHelper.NATIVE_SYSTEM_FONT_DESCENT;
    }

    @Test
    @DisplayName("a compact Table's PaintItem GC set to no font takes the system font at the native height")
    void compactPaintItemNoFont() {
        Shell shell = shell();
        Table table = new Table(shell, SWT.NONE);
        FontData face = shell.getDisplay().getSystemFont().getFontData()[0];
        FontData[] font = new FontData[1];
        int[] height = new int[1];

        compactPaintGc(table, gc -> {
            gc.setFont(null);
            font[0] = gc.getFont().getFontData()[0];
            height[0] = gc.getFontMetrics().getHeight();
        });

        assertThat(font[0].getName()).isEqualTo(face.getName());
        assertThat(font[0].getHeight()).isEqualTo(GCHelper.NATIVE_SYSTEM_FONT_HEIGHT);
        assertThat(height[0]).isEqualTo(nativeLine());
    }

    @Test
    @DisplayName("a compact Table's PaintItem GC keeps a bold system font bold at the native height")
    void compactPaintItemBoldSystemFont() {
        Shell shell = shell();
        Display display = shell.getDisplay();
        Table table = new Table(shell, SWT.NONE);
        FontData face = display.getSystemFont().getFontData()[0];
        Font bold = new Font(display, face.getName(), face.getHeight(), SWT.BOLD);
        FontData[] font = new FontData[1];

        compactPaintGc(table, gc -> {
            gc.setFont(bold);
            font[0] = gc.getFont().getFontData()[0];
        });

        assertThat(font[0].getStyle()).isEqualTo(SWT.BOLD);
        assertThat(font[0].getHeight()).isEqualTo(GCHelper.NATIVE_SYSTEM_FONT_HEIGHT);
    }

    @Test
    @DisplayName("a compact Table's PaintItem GC keeps another family at the system font's height as it is")
    void compactPaintItemOtherFamilySameHeight() {
        Shell shell = shell();
        Display display = shell.getDisplay();
        Table table = new Table(shell, SWT.NONE);
        FontData face = display.getSystemFont().getFontData()[0];
        String family = "Courier New".equalsIgnoreCase(face.getName()) ? "Times New Roman" : "Courier New";
        Font other = new Font(display, family, face.getHeight(), SWT.NORMAL);
        Font[] font = new Font[1];

        compactPaintGc(table, gc -> {
            gc.setFont(other);
            font[0] = gc.getFont();
        });

        assertThat(font[0]).isSameAs(other);
    }

    @Test
    @DisplayName("a compact Table's PaintItem GC refuses a disposed font")
    void compactPaintItemDisposedFont() {
        Shell shell = shell();
        Table table = new Table(shell, SWT.NONE);
        Font disposed = new Font(shell.getDisplay(), shell.getDisplay().getSystemFont().getFontData()[0]);
        disposed.dispose();
        int[] code = {-1};

        compactPaintGc(table, gc -> {
            try {
                gc.setFont(disposed);
            } catch (IllegalArgumentException e) {
                code[0] = SWT.ERROR_INVALID_ARGUMENT;
            }
        });

        assertThat(code[0]).isEqualTo(SWT.ERROR_INVALID_ARGUMENT);
    }

    @Test
    @DisplayName("a compact row font a PaintItem listener disposed is made again for the next row")
    void compactPaintItemFontDisposedByListener() {
        Shell shell = shell();
        Font system = shell.getDisplay().getSystemFont();
        Table table = new Table(shell, SWT.NONE);
        Font[] first = new Font[1];
        Font[] second = new Font[1];
        int[] height = new int[1];

        compactPaintGc(table, gc -> {
            gc.setFont(system);
            first[0] = gc.getFont();
            first[0].dispose();
        });
        compactPaintGc(table, gc -> {
            gc.setFont(system);
            second[0] = gc.getFont();
            height[0] = gc.getFontMetrics().getHeight();
        });

        assertThat(second[0]).isNotSameAs(first[0]);
        assertThat(second[0].isDisposed()).isFalse();
        assertThat(height[0]).isEqualTo(nativeLine());
    }

    @Test
    @DisplayName("the compact row fonts are disposed with their display")
    void compactRowFontsDisposedWithDisplay() {
        Shell shell = shell();
        Display display = shell.getDisplay();
        FontData face = display.getSystemFont().getFontData()[0];
        Font bold = new Font(display, face.getName(), face.getHeight(), SWT.BOLD);
        Table table = new Table(shell, SWT.NONE);
        Font[] made = new Font[2];

        compactPaintGc(table, gc -> {
            gc.setFont(display.getSystemFont());
            made[0] = gc.getFont();
            gc.setFont(bold);
            made[1] = gc.getFont();
        });
        org.mockito.ArgumentCaptor<Runnable> onDispose = org.mockito.ArgumentCaptor.forClass(Runnable.class);
        org.mockito.Mockito.verify(display, org.mockito.Mockito.atLeastOnce()).disposeExec(onDispose.capture());
        onDispose.getAllValues().forEach(Runnable::run);

        assertThat(made[0].isDisposed()).isTrue();
        assertThat(made[1].isDisposed()).isTrue();
    }

    @Test
    @DisplayName("a Table's PaintItem GC draws the system font in its own line box by default")
    void defaultPaintItemLineBox() {
        Shell shell = shell();
        Table table = new Table(shell, SWT.NONE);
        TableItem item = new TableItem(table, SWT.NONE);
        Font system = shell.getDisplay().getSystemFont();

        LineBox painted = paintLineBox(table, item, Display::getSystemFont, SWT.DRAW_TRANSPARENT);

        assertThat(painted).isEqualTo(canvasLineBox(shell, system, SWT.DRAW_TRANSPARENT));
        assertThat(painted.drawnY()).isEqualTo(DRAW_Y);
    }

    @Test
    @DisplayName("a Table's PaintItem GC keeps the theme's system font by default")
    void defaultPaintItemSystemFont() {
        Shell shell = shell();
        Table table = new Table(shell, SWT.NONE);
        TableItem item = new TableItem(table, SWT.NONE);
        item.setText("Show log");
        int themeHeight = canvasFontHeight(shell, shell.getDisplay().getSystemFont());

        PaintFonts fonts = paintFonts(table, item);

        assertThat(fonts.system()).isEqualTo(themeHeight);
        assertThat(fonts.systemExtent()).isEqualTo(canvasExtent(shell, shell.getDisplay().getSystemFont()));
        assertThat(table.getItemHeight()).isEqualTo(24);
    }

    @Test
    @DisplayName("a Tree's row is one text line tall when native item heights are off")
    void treeCompactRow() {
        Shell shell = shell();
        Tree tree = new Tree(shell, SWT.NONE);
        int regular = tree.getItemHeight();
        new TreeItem(tree, SWT.NONE).setText("Sample");
        int compact = compact(tree::getItemHeight);

        assertThat(compact).isLessThan(regular).isEqualTo(compactTreeRow());
    }

    @Test
    @DisplayName("a compact Tree's PaintItem GC draws and measures the system font at the native height")
    void treeCompactPaintItemSystemFont() {
        Shell shell = shell();
        Tree tree = new Tree(shell, SWT.NONE);
        TreeItem item = new TreeItem(tree, SWT.NONE);
        item.setText("Show log");
        Tree regular = new Tree(shell, SWT.NONE);
        int themeRowFont = paintFonts(regular, new TreeItem(regular, SWT.NONE)).own();
        int themeHeight = canvasFontHeight(shell, shell.getDisplay().getSystemFont());
        PaintFonts[] fonts = new PaintFonts[1];
        compact(() -> {
            fonts[0] = paintFonts(tree, item);
            return tree.getItemHeight();
        });

        int nativeLine = GCHelper.NATIVE_SYSTEM_FONT_ASCENT + GCHelper.NATIVE_SYSTEM_FONT_DESCENT;
        assertThat(fonts[0].own()).as("the row's theme font, as without the flag").isEqualTo(themeRowFont);
        assertThat(fonts[0].system()).as("gc.setFont(display.getSystemFont())").isEqualTo(nativeLine);
        assertThat(fonts[0].systemExtent()).as("its textExtent").isEqualTo(nativeLine);
        assertThat(fonts[0].built()).as("a font built like the system font").isEqualTo(nativeLine);
        assertThat(canvasFontHeight(shell, shell.getDisplay().getSystemFont())).as("outside the event").isEqualTo(themeHeight);
    }
}
