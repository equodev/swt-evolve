package dev.equo.swt.harness;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * Real user input (keys, mouse, wheel, IME, clipboard) delivered to the web client through Chrome.
 * Positions are page (CSS) pixels; modifiers are SWT masks, pressed as keys before the modified key.
 */
public final class UserInput {

    // CDP modifier bits.
    private static final int CDP_ALT = 1, CDP_CTRL = 2, CDP_META = 4, CDP_SHIFT = 8;
    // SWT modifier masks (org.eclipse.swt.SWT values, repeated so this compiles without SWT).
    private static final int SWT_ALT = 1 << 16, SWT_SHIFT = 1 << 17, SWT_CTRL = 1 << 18, SWT_COMMAND = 1 << 22;

    /** A physical key: DOM {@code key}, DOM {@code code}, Windows virtual key code, produced text. */
    public static final class Key {
        final String key, code, text;
        final int keyCode;

        private Key(String key, String code, int keyCode, String text) {
            this.key = key;
            this.code = code;
            this.keyCode = keyCode;
            this.text = text;
        }

        public static final Key ENTER = new Key("Enter", "Enter", 13, "\r");
        public static final Key TAB = new Key("Tab", "Tab", 9, null);
        public static final Key BACKSPACE = new Key("Backspace", "Backspace", 8, null);
        public static final Key DELETE = new Key("Delete", "Delete", 46, null);
        public static final Key ESCAPE = new Key("Escape", "Escape", 27, null);
        public static final Key INSERT = new Key("Insert", "Insert", 45, null);
        public static final Key HOME = new Key("Home", "Home", 36, null);
        public static final Key END = new Key("End", "End", 35, null);
        public static final Key PAGE_UP = new Key("PageUp", "PageUp", 33, null);
        public static final Key PAGE_DOWN = new Key("PageDown", "PageDown", 34, null);
        public static final Key LEFT = new Key("ArrowLeft", "ArrowLeft", 37, null);
        public static final Key UP = new Key("ArrowUp", "ArrowUp", 38, null);
        public static final Key RIGHT = new Key("ArrowRight", "ArrowRight", 39, null);
        public static final Key DOWN = new Key("ArrowDown", "ArrowDown", 40, null);
        public static final Key F10 = new Key("F10", "F10", 121, null);
        static final Key SHIFT = new Key("Shift", "ShiftLeft", 16, null);
        static final Key CONTROL = new Key("Control", "ControlLeft", 17, null);
        static final Key ALT = new Key("Alt", "AltLeft", 18, null);
        static final Key META = new Key("Meta", "MetaLeft", 91, null);

        /** The US-layout key producing {@code c} (letters, digits, space and common punctuation). */
        public static Key of(char c) {
            char lower = Character.toLowerCase(c);
            if (lower >= 'a' && lower <= 'z')
                return new Key(String.valueOf(c), "Key" + Character.toUpperCase(lower), Character.toUpperCase(lower), String.valueOf(c));
            if (c >= '0' && c <= '9')
                return new Key(String.valueOf(c), "Digit" + c, c, String.valueOf(c));
            switch (c) {
                case ' ': return new Key(" ", "Space", 32, " ");
                case '(': return new Key("(", "Digit9", '9', "(");
                case ')': return new Key(")", "Digit0", '0', ")");
                case '.': return new Key(".", "Period", 190, ".");
                case ',': return new Key(",", "Comma", 188, ",");
                case ';': return new Key(";", "Semicolon", 186, ";");
                case '=': return new Key("=", "Equal", 187, "=");
                case '-': return new Key("-", "Minus", 189, "-");
                case '{': return new Key("{", "BracketLeft", 219, "{");
                case '}': return new Key("}", "BracketRight", 221, "}");
                case '"': return new Key("\"", "Quote", 222, "\"");
                default: throw new IllegalArgumentException("No key mapping for '" + c + "'");
            }
        }

        boolean needsShift() {
            return text != null && text.length() == 1
                    && (Character.isUpperCase(text.charAt(0)) || "(){}\"".indexOf(text.charAt(0)) >= 0);
        }

        @Override
        public String toString() {
            return key;
        }
    }

    private final DevTools devTools;
    private int buttons;

    public UserInput(DevTools devTools) {
        this.devTools = devTools;
        // A headless page is never the focused window; without this, Flutter's text input and the
        // async clipboard refuse to work.
        devTools.send("Page.bringToFront", null);
        JsonObject focus = new JsonObject();
        focus.addProperty("enabled", true);
        devTools.send("Emulation.setFocusEmulationEnabled", focus);
        JsonObject grant = new JsonObject();
        grant.add("permissions", DevTools.array("clipboardReadWrite", "clipboardSanitizedWrite"));
        devTools.send("Browser.grantPermissions", grant);
    }

    // ---------------- keyboard ----------------

    /** Presses and releases {@code key} with the SWT modifier mask {@code swtModifiers} held. */
    public void press(Key key, int swtModifiers) {
        int cdp = 0;
        for (Key m : modifierKeys(swtModifiers)) {
            cdp |= cdpBit(m);
            keyEvent("rawKeyDown", m, cdp, null);
        }
        String text = key.text;
        if ((cdp & (CDP_CTRL | CDP_META | CDP_ALT)) != 0) text = null;
        keyEvent(text == null ? "rawKeyDown" : "keyDown", key, cdp, text);
        keyEvent("keyUp", key, cdp, null);
        Key[] held = modifierKeys(swtModifiers);
        for (int i = held.length - 1; i >= 0; i--) {
            cdp &= ~cdpBit(held[i]);
            keyEvent("keyUp", held[i], cdp, null);
        }
    }

    public void press(Key key) {
        press(key, 0);
    }

    /** Types {@code text} one key at a time; {@code '\n'} presses Enter. */
    public void type(String text) {
        for (char c : text.toCharArray()) {
            if (c == '\n') {
                press(Key.ENTER);
                continue;
            }
            Key k = Key.of(c);
            press(k, k.needsShift() ? SWT_SHIFT : 0);
        }
    }

    private void keyEvent(String type, Key key, int cdpModifiers, String text) {
        JsonObject p = new JsonObject();
        p.addProperty("type", type);
        p.addProperty("key", key.key);
        p.addProperty("code", key.code);
        // Only the Windows code: nativeVirtualKeyCode is platform-specific, and on macOS a Windows
        // code names another key (72, 'H', is Volume Up there), which headless Chrome then repeats.
        p.addProperty("windowsVirtualKeyCode", key.keyCode);
        p.addProperty("modifiers", cdpModifiers);
        if (text != null) {
            p.addProperty("text", text);
            p.addProperty("unmodifiedText", text);
        }
        devTools.send("Input.dispatchKeyEvent", p);
    }

    private static Key[] modifierKeys(int swt) {
        java.util.List<Key> keys = new java.util.ArrayList<>();
        if ((swt & SWT_CTRL) != 0) keys.add(Key.CONTROL);
        if ((swt & SWT_ALT) != 0) keys.add(Key.ALT);
        if ((swt & SWT_COMMAND) != 0) keys.add(Key.META);
        if ((swt & SWT_SHIFT) != 0) keys.add(Key.SHIFT);
        return keys.toArray(new Key[0]);
    }

    private static int cdpBit(Key modifier) {
        if (modifier == Key.CONTROL) return CDP_CTRL;
        if (modifier == Key.ALT) return CDP_ALT;
        if (modifier == Key.META) return CDP_META;
        return CDP_SHIFT;
    }

    private static int cdpModifiers(int swt) {
        int cdp = 0;
        for (Key m : modifierKeys(swt)) cdp |= cdpBit(m);
        return cdp;
    }

    // ---------------- mouse ----------------

    /** A primary-button click; {@code count} 2 or 3 makes it the second or third of a sequence. */
    public void click(double x, double y, int count, int swtModifiers) {
        for (int i = 1; i <= count; i++) {
            mouse("mousePressed", x, y, "left", i, swtModifiers);
            mouse("mouseReleased", x, y, "left", i, swtModifiers);
        }
    }

    public void click(double x, double y) {
        click(x, y, 1, 0);
    }

    /** Presses the primary button at the start, moves in {@code steps} to the end, releases. */
    public void drag(double fromX, double fromY, double toX, double toY, int steps) {
        mouse("mouseMoved", fromX, fromY, "none", 0, 0);
        mouse("mousePressed", fromX, fromY, "left", 1, 0);
        for (int i = 1; i <= steps; i++) {
            double t = (double) i / steps;
            mouse("mouseMoved", fromX + (toX - fromX) * t, fromY + (toY - fromY) * t, "left", 0, 0);
        }
        mouse("mouseReleased", toX, toY, "left", 1, 0);
    }

    public void secondaryClick(double x, double y) {
        mouse("mousePressed", x, y, "right", 1, 0);
        mouse("mouseReleased", x, y, "right", 1, 0);
    }

    /** One wheel event; positive {@code deltaY} scrolls down, positive {@code deltaX} right. */
    public void wheel(double x, double y, double deltaX, double deltaY, int swtModifiers) {
        mouse("mouseMoved", x, y, "none", 0, 0);
        JsonObject p = mouseParams("mouseWheel", x, y, "none", 0, swtModifiers);
        p.addProperty("deltaX", deltaX);
        p.addProperty("deltaY", deltaY);
        devTools.send("Input.dispatchMouseEvent", p);
    }

    private void mouse(String type, double x, double y, String button, int clickCount, int swtModifiers) {
        if ("mousePressed".equals(type)) buttons |= buttonBit(button);
        if ("mouseReleased".equals(type)) buttons &= ~buttonBit(button);
        devTools.send("Input.dispatchMouseEvent", mouseParams(type, x, y, button, clickCount, swtModifiers));
    }

    private JsonObject mouseParams(String type, double x, double y, String button, int clickCount, int swtModifiers) {
        JsonObject p = new JsonObject();
        p.addProperty("type", type);
        p.addProperty("x", x);
        p.addProperty("y", y);
        p.addProperty("button", button);
        p.addProperty("buttons", buttons);
        p.addProperty("clickCount", clickCount);
        p.addProperty("modifiers", cdpModifiers(swtModifiers));
        return p;
    }

    private static int buttonBit(String button) {
        switch (button) {
            case "left": return 1;
            case "right": return 2;
            case "middle": return 4;
            default: return 0;
        }
    }

    // ---------------- IME ----------------

    /** Shows {@code text} as the uncommitted composition, as an input method does while typing. */
    public void compose(String text) {
        JsonObject p = new JsonObject();
        p.addProperty("text", text);
        p.addProperty("selectionStart", text.length());
        p.addProperty("selectionEnd", text.length());
        devTools.send("Input.imeSetComposition", p);
    }

    /** Commits {@code text}, ending any composition in progress. */
    public void commit(String text) {
        JsonObject p = new JsonObject();
        p.add("text", new JsonPrimitive(text));
        devTools.send("Input.insertText", p);
    }

    /** Whether the page is visible, focused and still producing animation frames — for failure reports. */
    public String pageState() {
        return String.valueOf(devTools.evaluate("Promise.race(["
                + "new Promise(r => requestAnimationFrame(() => r('frames running'))),"
                + "new Promise(r => setTimeout(() => r('no animation frame in 2s'), 2000))"
                + "]).then(f => Promise.race(["
                + "new Promise(r => window.evolveTest.waitForFrame(() => r(f + ', flutter frames running'))),"
                + "new Promise(r => setTimeout(() => r(f + ', no flutter frame in 2s'), 2000))]))"
                + ".then(f => f + ', visibility=' + document.visibilityState + ', focus=' + document.hasFocus()"
                + " + ', active=' + (document.activeElement && document.activeElement.tagName))"));
    }

    // ---------------- clipboard ----------------

    /** Puts {@code text} on the system clipboard, as copying in another application does. */
    public void writeSystemClipboard(String text) {
        devTools.evaluate("navigator.clipboard.writeText(" + new JsonPrimitive(text) + ").then(() => true)");
    }

    public String readSystemClipboard() {
        return devTools.evaluate("navigator.clipboard.readText()").getAsString();
    }
}
