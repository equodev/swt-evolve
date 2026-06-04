package dev.equo.swt.bench.gen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Mocks;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.Widget;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Dev tool that generates {@code dev.equo.swt.bench.WorkbenchTree} from the captured
 * {@code resources/bench/workbench.json}. The bench's {@code WorkbenchTree} is committed as
 * <em>hardcoded</em> Java (no runtime JSON parse, no reflection); this is how that file is produced
 * when the capture is refreshed.
 *
 * <p>Strategy: parse the JSON, build the matching SWT widget tree <em>reflectively</em> in the test
 * mock display (so every setter we emit is one reflection already proved exists on that type), and
 * while building, record the equivalent Java — {@code new}/setter/link statements keyed by a {@code
 * Widget[] w} index — then emit a self-contained class. Constructions, then setters, then id-links,
 * each chunked into small methods to stay readable and under the JVM method-size limit.
 *
 * <p>It extends {@link SerializeTestBase} only for its mock-display setup ({@code Mocks} extension,
 * {@code Config.forceEquo}, {@code MockFlutterBridge}, accessible/cursor mocks) — the same context the
 * serialize tests build widgets in. Tagged {@code bench} so it never runs in the normal {@code test}
 * job. Run it on demand (writes to the source tree):
 *
 * <pre>{@code
 * ./gradlew :swt-evolve:swt_native:benchmark \
 *     --tests dev.equo.swt.bench.gen.WorkbenchTreeGenerator -DskipFlutterLib
 * }</pre>
 *
 * Override the output path with {@code -Dwb.out=<path>} (default
 * {@code src/test/java/dev/equo/swt/bench/WorkbenchTree.java}, relative to the swt_native project dir).
 */
@Tag("bench")
public class WorkbenchTreeGenerator extends SerializeTestBase {

    @Test
    void generate() throws Exception {
        Shell shell = Mocks.swtShell();
        String source = new Builder().buildAndEmit(shell);
        Path out = Paths.get(System.getProperty("wb.out",
                "src/test/java/dev/equo/swt/bench/WorkbenchTree.java"));
        Files.writeString(out, source);
        System.out.println("[WorkbenchTreeGenerator] wrote " + out.toAbsolutePath());
    }

    /** Reflective JSON→widget builder that records the equivalent hardcoded Java as it goes. */
    static final class Builder {

        /** Fields carrying structure/identity, handled explicitly rather than as reflective scalars. */
        private static final Set<String> STRUCTURAL = Set.of(
                "swt", "id", "style", "children", "items", "columns", "control", "topRight", "menu",
                "editors", "horizontalBar", "verticalBar", "sortColumn", "selection");
        private static final Object UNCONVERTIBLE = new Object();
        private static final JsonArray EMPTY = new JsonArray();
        private static final int CHUNK = 30;

        private final Map<Long, Widget> byId = new HashMap<>();
        private final IdentityHashMap<Object, Integer> idxOf = new IdentityHashMap<>();
        private final List<String> creations = new ArrayList<>();
        private final List<String> setters = new ArrayList<>();
        private final List<String> links = new ArrayList<>();
        private int n = 0;

        String buildAndEmit(Shell shell) {
            JsonObject root = parse();
            if (buildControl(root, shell) == null) throw new IllegalStateException("no root control");
            return emit();
        }

        private static JsonObject parse() {
            try (InputStream in = WorkbenchTreeGenerator.class.getResourceAsStream("/bench/workbench.json")) {
                if (in == null) throw new IllegalStateException("resource /bench/workbench.json not found");
                try (InputStreamReader r = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                    return JsonParser.parseReader(r).getAsJsonObject();
                }
            } catch (IllegalStateException e) {
                throw e;
            } catch (Exception e) {
                throw new IllegalStateException("failed reading bench/workbench.json", e);
            }
        }

        // ====== reflective build (mirrors the emitted tree) ======

        private Control buildControl(JsonObject node, Composite parent) {
            String type = node.get("swt").getAsString();
            int style = node.has("style") ? node.get("style").getAsInt() : SWT.NONE;
            Control c = createControl(type, parent, style);
            if (c == null) return null;
            register(node, c);
            recordCreate(c, parent, "Composite", style);
            populate(c, node);
            applyScalars(c, node);
            return c;
        }

        private static Control createControl(String type, Composite parent, int style) {
            try {
                switch (type) {
                    case "Composite":
                    case "MainComposite": return new Composite(parent, style);
                    case "Canvas":        return new Canvas(parent, style);
                    case "ToolBar":       return new ToolBar(parent, style);
                    case "Tree":          return new Tree(parent, style);
                    case "CTabFolder":    return new CTabFolder(parent, style);
                    case "Label":         return new Label(parent, style);
                    case "Link":          return new Link(parent, style);
                    case "Text":          return new Text(parent, style);
                    case "Browser":
                        try { return new Browser(parent, style); }
                        catch (Throwable t) { return new Composite(parent, SWT.NONE); }
                    default:
                        System.out.println("[gen] unknown widget type '" + type + "' -> Composite");
                        return new Composite(parent, style);
                }
            } catch (Throwable t) {
                try { return new Composite(parent, SWT.NONE); } catch (Throwable t2) { return null; }
            }
        }

        private void populate(Control c, JsonObject node) {
            if (c instanceof ToolBar tb) {
                for (JsonElement it : arr(node, "items")) {
                    JsonObject io = it.getAsJsonObject();
                    try { ToolItem item = new ToolItem(tb, styleOf(io)); register(io, item); recordCreate(item, tb, "ToolBar", styleOf(io)); applyScalars(item, io); }
                    catch (Throwable t) { /* skip */ }
                }
            }
            if (c instanceof Tree tr) {
                for (JsonElement col : arr(node, "columns")) {
                    JsonObject co = col.getAsJsonObject();
                    try { TreeColumn tc = new TreeColumn(tr, styleOf(co)); register(co, tc); recordCreate(tc, tr, "Tree", styleOf(co)); applyScalars(tc, co); }
                    catch (Throwable t) { /* skip */ }
                }
                if (node.has("menu") && node.get("menu").isJsonObject()) {
                    try { Menu m = new Menu(tr); recordCreate(m, tr, "Control", -1); applyScalars(m, node.getAsJsonObject("menu")); tr.setMenu(m); recordLink(tr, "setMenu", m, "Menu"); }
                    catch (Throwable t) { /* skip */ }
                }
            }
            if (c instanceof CTabFolder cf) {
                buildChildren(cf, node);
                for (JsonElement it : arr(node, "items")) {
                    JsonObject io = it.getAsJsonObject();
                    try {
                        CTabItem item = new CTabItem(cf, styleOf(io));
                        register(io, item);
                        recordCreate(item, cf, "CTabFolder", styleOf(io));
                        applyScalars(item, io);
                        Control ctl = linkedControl(io.get("control"));
                        if (ctl != null) { item.setControl(ctl); recordLink(item, "setControl", ctl, "Control"); }
                    } catch (Throwable t) { /* skip */ }
                }
                Control tr = linkedControl(node.get("topRight"));
                if (tr != null) { try { cf.setTopRight(tr); recordLink(cf, "setTopRight", tr, "Control"); } catch (Throwable t) { /* skip */ } }
                return;
            }
            if (c instanceof Composite comp) buildChildren(comp, node);
        }

        private void buildChildren(Composite parent, JsonObject node) {
            for (JsonElement ch : arr(node, "children")) {
                if (ch.isJsonObject() && ch.getAsJsonObject().has("swt")) {
                    try { buildControl(ch.getAsJsonObject(), parent); }
                    catch (Throwable t) { /* skip subtree */ }
                }
            }
        }

        private Control linkedControl(JsonElement ref) {
            if (ref == null || !ref.isJsonObject()) return null;
            JsonObject o = ref.getAsJsonObject();
            if (!o.has("id")) return null;
            Widget w = byId.get(o.get("id").getAsLong());
            return w instanceof Control ? (Control) w : null;
        }

        private void register(JsonObject node, Widget w) {
            if (node.has("id") && !node.get("id").isJsonNull()) byId.put(node.get("id").getAsLong(), w);
        }

        private void applyScalars(Object widget, JsonObject node) {
            for (Map.Entry<String, JsonElement> e : node.entrySet()) {
                String key = e.getKey();
                if (STRUCTURAL.contains(key)) continue;
                JsonElement v = e.getValue();
                if (v == null || v.isJsonNull()) continue;
                invokeSetter(widget, key, v);
            }
        }

        private void invokeSetter(Object widget, String key, JsonElement v) {
            String setter = "set" + Character.toUpperCase(key.charAt(0)) + key.substring(1);
            for (Method m : widget.getClass().getMethods()) {
                if (m.getParameterCount() != 1 || !m.getName().equals(setter)) continue;
                Object arg;
                try { arg = convert(v, m.getParameterTypes()[0]); }
                catch (Throwable t) { continue; }
                if (arg == UNCONVERTIBLE) continue;
                try {
                    m.invoke(widget, arg);
                    recordSetter(widget, m.getName(), exprOf(v, m.getParameterTypes()[0]));
                    return;
                } catch (Throwable t) { /* wrong overload / rejected — try next */ }
            }
        }

        private Object convert(JsonElement v, Class<?> t) {
            if (t == boolean.class || t == Boolean.class) return v.getAsBoolean();
            if (t == int.class || t == Integer.class) return v.getAsInt();
            if (t == long.class || t == Long.class) return v.getAsLong();
            if (t == String.class) return v.isJsonPrimitive() ? v.getAsString() : UNCONVERTIBLE;
            if (v.isJsonObject()) {
                JsonObject o = v.getAsJsonObject();
                if (t == Rectangle.class) return new Rectangle(i(o, "x"), i(o, "y"), i(o, "width"), i(o, "height"));
                if (t == Point.class) return new Point(i(o, "x"), i(o, "y"));
                if (t == Color.class) return new Color(i(o, "red"), i(o, "green"), i(o, "blue"), o.has("alpha") ? i(o, "alpha") : 255);
                if (t == Image.class) return image(o);
                if (t == Font.class) return font(o);
            }
            if (v.isJsonArray()) {
                JsonArray a = v.getAsJsonArray();
                if (t == String[].class) { String[] out = new String[a.size()]; for (int k = 0; k < a.size(); k++) out[k] = a.get(k).getAsString(); return out; }
                if (t == int[].class) { int[] out = new int[a.size()]; for (int k = 0; k < a.size(); k++) out[k] = a.get(k).getAsInt(); return out; }
            }
            return UNCONVERTIBLE;
        }

        private static Font font(JsonObject o) {
            JsonArray fds = o.has("fontData") && o.get("fontData").isJsonArray() ? o.getAsJsonArray("fontData") : null;
            if (fds == null || fds.isEmpty()) return null;
            JsonObject fd = fds.get(0).getAsJsonObject();
            String name = fd.has("name") ? fd.get("name").getAsString() : "Sans";
            int height = Math.max(1, fd.has("height") ? fd.get("height").getAsInt() : 10);
            int fstyle = fd.has("style") ? fd.get("style").getAsInt() : SWT.NORMAL;
            return new Font(Mocks.device(), name, height, fstyle);
        }

        private static Image image(JsonObject o) {
            JsonObject id = o.has("imageData") && o.get("imageData").isJsonObject() ? o.getAsJsonObject("imageData") : null;
            if (id == null) return null;
            int w = Math.max(1, i(id, "width"));
            int h = Math.max(1, i(id, "height"));
            int depth = id.has("depth") ? id.get("depth").getAsInt() : 24;
            PaletteData pal = depth == 16 ? new PaletteData(0x7C00, 0x3E0, 0x1F) : new PaletteData(0xFF0000, 0xFF00, 0xFF);
            if (depth != 16 && depth != 24 && depth != 32) depth = 24;
            ImageData data = new ImageData(w, h, depth, pal);
            if (id.has("alphaData") && !id.get("alphaData").isJsonNull()) data.alphaData = new byte[w * h];
            return new Image(Mocks.device(), data);
        }

        // ====== recording + emission ======

        private void recordCreate(Widget w, Object parent, String parentCast, int style) {
            int idx = n++;
            idxOf.put(w, idx);
            String type = w.getClass().getSimpleName();
            String parentExpr = parent instanceof Shell ? "shell" : "(" + parentCast + ") w[" + idxOf.get(parent) + "]";
            String ctor = "Menu".equals(type)
                    ? "new Menu(" + parentExpr + ")"
                    : "new " + type + "(" + parentExpr + ", " + style + ")";
            creations.add("        w[" + idx + "] = " + ctor + ";");
        }

        private void recordSetter(Object widget, String method, String expr) {
            if (expr == null) return;
            Integer idx = idxOf.get(widget);
            if (idx == null) return;
            setters.add("        ((" + widget.getClass().getSimpleName() + ") w[" + idx + "])." + method + "(" + expr + ");");
        }

        private void recordLink(Widget recv, String method, Widget target, String argCast) {
            Integer ri = idxOf.get(recv), ti = idxOf.get(target);
            if (ri == null || ti == null) return;
            links.add("        ((" + recv.getClass().getSimpleName() + ") w[" + ri + "])." + method
                    + "((" + argCast + ") w[" + ti + "]);");
        }

        private String exprOf(JsonElement v, Class<?> t) {
            if (t == boolean.class || t == Boolean.class) return v.getAsBoolean() ? "true" : "false";
            if (t == int.class || t == Integer.class) return Integer.toString(v.getAsInt());
            if (t == long.class || t == Long.class) return v.getAsLong() + "L";
            if (t == String.class) return v.isJsonPrimitive() ? q(v.getAsString()) : null;
            if (v.isJsonObject()) {
                JsonObject o = v.getAsJsonObject();
                if (t == Rectangle.class) return "new Rectangle(" + i(o, "x") + ", " + i(o, "y") + ", " + i(o, "width") + ", " + i(o, "height") + ")";
                if (t == Point.class) return "new Point(" + i(o, "x") + ", " + i(o, "y") + ")";
                if (t == Color.class) return "color(" + i(o, "red") + ", " + i(o, "green") + ", " + i(o, "blue") + ", " + (o.has("alpha") ? i(o, "alpha") : 255) + ")";
                if (t == Image.class) return imgExpr(o);
                if (t == Font.class) return fontExpr(o);
            }
            if (v.isJsonArray()) {
                JsonArray a = v.getAsJsonArray();
                if (t == String[].class) {
                    StringBuilder b = new StringBuilder("new String[]{");
                    for (int k = 0; k < a.size(); k++) { if (k > 0) b.append(", "); b.append(q(a.get(k).getAsString())); }
                    return b.append("}").toString();
                }
                if (t == int[].class) {
                    StringBuilder b = new StringBuilder("new int[]{");
                    for (int k = 0; k < a.size(); k++) { if (k > 0) b.append(", "); b.append(a.get(k).getAsInt()); }
                    return b.append("}").toString();
                }
            }
            return null;
        }

        private String imgExpr(JsonObject o) {
            JsonObject id = o.has("imageData") && o.get("imageData").isJsonObject() ? o.getAsJsonObject("imageData") : null;
            if (id == null) return null;
            int w = Math.max(1, i(id, "width"));
            int h = Math.max(1, i(id, "height"));
            int depth = id.has("depth") ? id.get("depth").getAsInt() : 24;
            boolean alpha = id.has("alphaData") && !id.get("alphaData").isJsonNull();
            String fn = o.has("filename") && !o.get("filename").isJsonNull() ? q(o.get("filename").getAsString()) : "null";
            return "img(" + w + ", " + h + ", " + depth + ", " + alpha + ", " + fn + ")";
        }

        private String fontExpr(JsonObject o) {
            JsonArray fds = o.has("fontData") && o.get("fontData").isJsonArray() ? o.getAsJsonArray("fontData") : null;
            if (fds == null || fds.isEmpty()) return null;
            JsonObject fd = fds.get(0).getAsJsonObject();
            String name = fd.has("name") ? fd.get("name").getAsString() : "Sans";
            int height = Math.max(1, fd.has("height") ? fd.get("height").getAsInt() : 10);
            int fstyle = fd.has("style") ? fd.get("style").getAsInt() : SWT.NORMAL;
            return "font(" + q(name) + ", " + height + ", " + fstyle + ")";
        }

        private static String q(String s) {
            return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t") + "\"";
        }

        private String emit() {
            StringBuilder b = new StringBuilder();
            b.append("package dev.equo.swt.bench;\n\n");
            for (String imp : new String[]{
                    "org.eclipse.swt.browser.Browser", "org.eclipse.swt.custom.CTabFolder",
                    "org.eclipse.swt.custom.CTabItem", "org.eclipse.swt.graphics.Color",
                    "org.eclipse.swt.graphics.DartImage", "org.eclipse.swt.graphics.Font",
                    "org.eclipse.swt.graphics.Image", "org.eclipse.swt.graphics.ImageData",
                    "org.eclipse.swt.graphics.PaletteData", "org.eclipse.swt.graphics.Point",
                    "org.eclipse.swt.graphics.Rectangle", "org.eclipse.swt.widgets.Canvas",
                    "org.eclipse.swt.widgets.Composite", "org.eclipse.swt.widgets.Control",
                    "org.eclipse.swt.widgets.DartControl", "org.eclipse.swt.widgets.Label",
                    "org.eclipse.swt.widgets.Link", "org.eclipse.swt.widgets.Menu",
                    "org.eclipse.swt.widgets.Mocks", "org.eclipse.swt.widgets.Shell",
                    "org.eclipse.swt.widgets.Text", "org.eclipse.swt.widgets.ToolBar",
                    "org.eclipse.swt.widgets.ToolItem", "org.eclipse.swt.widgets.Tree",
                    "org.eclipse.swt.widgets.TreeColumn", "org.eclipse.swt.widgets.Widget"}) {
                b.append("import ").append(imp).append(";\n");
            }
            b.append("\n/**\n");
            b.append(" * The Eclipse workbench bench shape as a hardcoded live SWT widget tree (").append(n).append(" widgets),\n");
            b.append(" * GENERATED from resources/bench/workbench.json by dev.equo.swt.bench.gen.WorkbenchTreeGenerator.\n");
            b.append(" * Built once at bench startup; re-serialized through the production Serializer each iteration like\n");
            b.append(" * every other shape. Do not edit by hand — re-run the generator after re-capturing the JSON.\n");
            b.append(" */\n");
            b.append("final class WorkbenchTree {\n\n");
            b.append("    static DartControl build(Shell shell) {\n");
            b.append("        Widget[] w = new Widget[").append(n).append("];\n");
            emitCalls(b, "c", creations.size(), true);
            emitCalls(b, "s", setters.size(), false);
            emitCalls(b, "k", links.size(), false);
            b.append("        return (DartControl) w[0].getImpl();\n");
            b.append("    }\n\n");
            emitMethods(b, "c", creations, true);
            emitMethods(b, "s", setters, false);
            emitMethods(b, "k", links, false);
            b.append(HELPERS);
            b.append("}\n");
            return b.toString();
        }

        private void emitCalls(StringBuilder b, String prefix, int total, boolean rootPhase) {
            int methods = (total + CHUNK - 1) / CHUNK;
            for (int m = 0; m < methods; m++) {
                boolean first = rootPhase && m == 0;
                b.append("        ").append(prefix).append(m).append(first ? "(shell, w);\n" : "(w);\n");
            }
        }

        private void emitMethods(StringBuilder b, String prefix, List<String> lines, boolean rootPhase) {
            int methods = (lines.size() + CHUNK - 1) / CHUNK;
            for (int m = 0; m < methods; m++) {
                boolean first = rootPhase && m == 0;
                b.append("    private static void ").append(prefix).append(m)
                 .append(first ? "(Shell shell, Widget[] w) {\n" : "(Widget[] w) {\n");
                for (int k = m * CHUNK; k < Math.min(lines.size(), (m + 1) * CHUNK); k++) b.append(lines.get(k)).append("\n");
                b.append("    }\n\n");
            }
        }

        private static JsonArray arr(JsonObject node, String field) {
            return node.has(field) && node.get(field).isJsonArray() ? node.getAsJsonArray(field) : EMPTY;
        }

        private static int styleOf(JsonObject node) {
            return node.has("style") ? node.get("style").getAsInt() : SWT.NONE;
        }

        private static int i(JsonObject o, String k) {
            return o.has(k) && !o.get(k).isJsonNull() ? o.get(k).getAsInt() : 0;
        }

        private static final String HELPERS =
                "    private static Color color(int r, int g, int b, int a) { return new Color(r, g, b, a); }\n\n" +
                "    private static Font font(String name, int height, int style) {\n" +
                "        return new Font(Mocks.device(), name, height, style);\n" +
                "    }\n\n" +
                "    private static Image img(int width, int height, int depth, boolean alpha, String filename) {\n" +
                "        PaletteData pal = depth == 16 ? new PaletteData(0x7C00, 0x3E0, 0x1F) : new PaletteData(0xFF0000, 0xFF00, 0xFF);\n" +
                "        if (depth != 16 && depth != 24 && depth != 32) depth = 24;\n" +
                "        ImageData data = new ImageData(width, height, depth, pal);\n" +
                "        if (alpha) data.alphaData = new byte[width * height];\n" +
                "        Image image = new Image(Mocks.device(), data);\n" +
                "        if (filename != null) ((DartImage) image.getImpl())._filename(filename);\n" +
                "        return image;\n" +
                "    }\n";
    }
}
