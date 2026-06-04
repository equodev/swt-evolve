package dev.equo.swt.bench;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.MockFlutterBridge;
import dev.equo.swt.Serializer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.DartImage;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.VImage;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DartControl;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Mocks;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;

import java.io.IOException;
import java.util.List;

/**
 * Bench payloads built from real SWT widgets. Each shape is a plausible production-style
 * widget tree whose serialized size falls naturally in a chosen rough size class — no padding,
 * no synthetic bulk. All wire bytes are valid production JSON produced by the real
 * {@link Serializer} path ({@code writeWithId} envelope, real DSL-JSON converters).
 *
 * <p>Three shapes — {@link #SMALL} (~250–400B single Button), {@link #MEDIUM} (~3–6KB labelled
 * form), {@link #LARGE} (~50–80KB workbench-style pane tree). Constructed in a static
 * initializer under a mocked Shell + {@link MockFlutterBridge}, then frozen.
 */
public final class BenchPayloads {

    public static final Shape SMALL;
    public static final Shape MEDIUM;
    public static final Shape LARGE;
    /** A Button carrying a real {@link Image} — the serialized VButton holds a nested VImage
     *  whose pixel data ships base64-encoded inside the JSON (the cost we want to baseline). */
    public static final Shape IMAGE_SMALL;
    public static final Shape IMAGE_LARGE;
    /** A Button carrying an SVG image — VImage.svgContent is plain text markup (no base64,
     *  no raster pixel data), so it serializes as a JSON string. */
    public static final Shape SVG;
    /** A real Eclipse workbench widget tree (CTabFolder editor stacks, Trees with columns, ToolBars,
     *  …) as live SWT widgets built by {@link WorkbenchTree} — hardcoded Java generated once from the
     *  captured {@code resources/bench/workbench.json}, so it re-serializes through the production Java
     *  path each iteration like every other shape, not a fixed byte replay. */
    public static final Shape WORKBENCH;

    /** A bench shape: a live widget tree re-serialized from its {@code DartControl} impl on each call. */
    public static final class Shape {
        public final String name;
        public final DartControl impl;
        public final int size;

        private Shape(String name, DartControl impl) {
            this.name = name;
            this.impl = impl;
            this.size = encode(impl).length;
        }

        /** Live shape — re-serialized fresh on each call (measures DSL-JSON encode cost). */
        static Shape live(String name, DartControl impl) { return new Shape(name, impl); }

        /** J→D payload — re-encoded from the live widget tree on every call. */
        public byte[] serialize() { return encode(impl); }

        @Override public String toString() { return name; }
    }

    private static final Serializer serializer = new Serializer();

    private BenchPayloads() {}

    static {
        FlutterBridge.set(new MockFlutterBridge());
        Config.forceEquo();
        Shell shell = Mocks.swtShell();
        SMALL  = Shape.live("small",  implOf(buildSmall(shell)));
        MEDIUM = Shape.live("medium", implOf(buildMedium(shell)));
        LARGE  = Shape.live("large",  implOf(buildLarge(shell)));
        IMAGE_SMALL = Shape.live("img_small", implOf(buildImageButton(shell, 48, 48)));
        IMAGE_LARGE = Shape.live("img_large", implOf(buildImageButton(shell, 256, 256)));
        SVG = Shape.live("svg", implOf(buildSvgButton(shell)));
        WORKBENCH = Shape.live("workbench", WorkbenchTree.build(shell));
        for (Shape s : List.of(SMALL, MEDIUM, LARGE, IMAGE_SMALL, IMAGE_LARGE, SVG, WORKBENCH)) {
            System.out.println("[BenchPayloads] " + s.name + " = " + s.size + " bytes");
        }
    }

    // ====== Tree builders — real SWT widget construction ======

    /** Small: a single Button — smallest meaningful real-V* shape, ~250–400B. */
    private static Widget buildSmall(Shell shell) {
        Button b = new Button(shell, SWT.PUSH);
        b.setBounds(new Rectangle(0, 0, 100, 28));
        b.setText("OK");
        return b;
    }

    /** Medium: a small dialog form — 4 labelled input rows + a 3-button action row, ~4–5KB. */
    private static Widget buildMedium(Shell shell) {
        Composite root = new Composite(shell, SWT.NONE);
        root.setBounds(new Rectangle(0, 0, 480, 200));
        for (int i = 0; i < 4; i++) {
            Label l = new Label(root, SWT.LEFT);
            l.setBounds(new Rectangle(10, i * 32, 160, 22));
            l.setText("Preference field " + i);
            Button b = new Button(root, SWT.PUSH);
            b.setBounds(new Rectangle(180, i * 32, 280, 28));
            b.setText("Edit preference value " + i);
        }
        Composite actions = new Composite(root, SWT.NONE);
        actions.setBounds(new Rectangle(10, 160, 400, 40));
        for (String label : new String[]{"Apply", "Cancel", "Reset"}) {
            Button b = new Button(actions, SWT.PUSH);
            b.setText(label);
        }
        return root;
    }

    /** Large: workbench-style editor — 5 panes × 12 (label,button) rows + 5 action rows, ~50–70KB. */
    private static Widget buildLarge(Shell shell) {
        Composite root = new Composite(shell, SWT.NONE);
        root.setBounds(new Rectangle(0, 0, 800, 1200));
        for (int p = 0; p < 5; p++) {
            Composite pane = new Composite(root, SWT.NONE);
            pane.setBounds(new Rectangle(p * 160, 0, 160, 1200));
            for (int k = 0; k < 12; k++) {
                Label l = new Label(pane, SWT.LEFT);
                l.setBounds(new Rectangle(0, k * 60, 160, 22));
                l.setText("Pane " + p + " row " + k + " label");
                Button b = new Button(pane, SWT.PUSH);
                b.setBounds(new Rectangle(0, k * 60 + 24, 160, 30));
                b.setText("Pane " + p + " action row " + k);
            }
            Composite actions = new Composite(pane, SWT.NONE);
            actions.setBounds(new Rectangle(0, 1000, 160, 40));
            for (String label : new String[]{"Open", "Refresh", "Close pane"}) {
                Button b = new Button(actions, SWT.PUSH);
                b.setText(label);
            }
        }
        return root;
    }

    /**
     * A Button with a real image whose pixels are a non-trivial pattern (so the encoded PNG —
     * and thus its base64 wire form — has realistic size). This is the production J→D image
     * path: an icon-bearing widget whose VButton nests a VImage carrying the base64 pixel data.
     */
    private static Widget buildImageButton(Shell shell, int w, int h) {
        ImageData data = new ImageData(w, h, 24, new PaletteData(0xFF0000, 0x00FF00, 0x0000FF));
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                data.setPixel(x, y, ((x * 13 & 0xFF) << 16) | ((y * 17 & 0xFF) << 8) | ((x + y) & 0xFF));
            }
        }
        Image img = new Image(Mocks.device(), data);
        Button b = new Button(shell, SWT.PUSH);
        b.setBounds(new Rectangle(0, 0, w, h));
        b.setImage(img);
        return b;
    }

    /**
     * A Button whose image is an SVG. SVG crosses the bridge as plain text in
     * {@code VImage.svgContent} — a JSON string, no base64 and no raster pixel data — so this
     * shape isolates the cost of shipping vector markup vs raster images.
     */
    private static Widget buildSvgButton(Shell shell) {
        Image img = new Image(Mocks.device(), 1, 1);
        VImage v = ((DartImage) img.getImpl()).getValue();
        v.setImageData(null);      // pure vector: drop the 1×1 raster placeholder
        v.setSvgContent(SAMPLE_SVG);
        Button b = new Button(shell, SWT.PUSH);
        b.setBounds(new Rectangle(0, 0, 24, 24));
        b.setImage(img);
        return b;
    }

    /** A representative real-world icon SVG (~1 KB of markup). */
    private static final String SAMPLE_SVG =
            "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"24\" height=\"24\" viewBox=\"0 0 24 24\">"
          + "<defs><linearGradient id=\"g\" x1=\"0\" y1=\"0\" x2=\"1\" y2=\"1\">"
          + "<stop offset=\"0%\" stop-color=\"#4f8cff\"/><stop offset=\"100%\" stop-color=\"#1b4fd0\"/>"
          + "</linearGradient></defs>"
          + "<path fill=\"url(#g)\" d=\"M19.14 12.94c.04-.31.06-.62.06-.94s-.02-.63-.07-.94l2.03-1.58a"
          + ".49.49 0 0 0 .12-.62l-1.92-3.32a.49.49 0 0 0-.59-.22l-2.39.96c-.5-.38-1.03-.7-1.62-.94l-.36-2.54"
          + "a.49.49 0 0 0-.48-.41h-3.84a.49.49 0 0 0-.48.41l-.36 2.54c-.59.24-1.13.57-1.62.94l-2.39-.96a"
          + ".49.49 0 0 0-.59.22L2.74 8.87a.49.49 0 0 0 .12.62l2.03 1.58c-.05.31-.07.63-.07.94s.02.63.07.94"
          + "l-2.03 1.58a.49.49 0 0 0-.12.62l1.92 3.32c.13.22.39.31.59.22l2.39-.96c.5.38 1.03.7 1.62.94l.36 "
          + "2.54c.04.24.24.41.48.41h3.84c.24 0 .44-.17.48-.41l.36-2.54c.59-.24 1.13-.56 1.62-.94l2.39.96c.2"
          + ".08.46 0 .59-.22l1.92-3.32a.49.49 0 0 0-.12-.62l-2.03-1.58zM12 15.6A3.6 3.6 0 1 1 12 8.4a3.6 "
          + "3.6 0 0 1 0 7.2z\"/></svg>";

    private static DartControl implOf(Widget w) {
        return (DartControl) w.getImpl();
    }

    private static byte[] encode(DartControl impl) {
        try {
            return serializer.to(impl);
        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize bench impl", e);
        }
    }
}
