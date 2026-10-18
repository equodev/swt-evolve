package org.eclipse.swt.custom;

import dev.equo.swt.harness.UserInput;
import dev.equo.swt.harness.UserInput.Key;
import dev.equo.swt.harness.WebDisplayHarness;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A live web client driving a StyledText (the subject) with real browser input, beside a twin that
 * gets the same keys as Java-posted events: the twin's resulting state is what SWT specifies.
 */
final class StyledTextFlutterStage {

    static final int WIDTH = 380, HEIGHT = 260;

    final WebDisplayHarness flutter = new WebDisplayHarness();
    Display display;
    Shell shell;
    /** A focusable control outside the editors, for focus-leaving scenarios. */
    Text outside;
    StyledText subject;
    StyledText twin;
    final EventLog subjectLog = new EventLog();
    final EventLog twinLog = new EventLog();

    // ---------------- lifecycle ----------------

    void boot() {
        display = flutter.boot();
        shell = new Shell(display);
        shell.setBounds(0, 0, 2 * WIDTH + 30, HEIGHT + 80);
        outside = new Text(shell, SWT.SINGLE);
        outside.setBounds(0, HEIGHT + 20, 200, 24);
        shell.open();
        fresh(SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL, "");
    }

    void shutdown() {
        flutter.teardown();
    }

    /** Replaces both editors with new ones of {@code style} holding {@code text}. */
    void fresh(int style, String text) {
        if (subject != null) {
            // Failures here belong to the previous test's leftovers, already reported.
            ignoringLeftovers(this::clickOutside);
            subject.dispose();
            twin.dispose();
            ignoringLeftovers(this::settle);
        }
        subjectLog.clear();
        twinLog.clear();
        subject = new StyledText(shell, style);
        subject.setBounds(0, 0, WIDTH, HEIGHT);
        subject.setText(text);
        twin = new StyledText(shell, style);
        twin.setBounds(WIDTH + 20, 0, WIDTH, HEIGHT);
        twin.setText(text);
        subjectLog.attach(subject);
        twinLog.attach(twin);
        settle();
    }

    private static void ignoringLeftovers(Runnable step) {
        for (int attempt = 0; attempt < 3; attempt++) {
            try {
                step.run();
                return;
            } catch (RuntimeException leftover) {
                // retried: each attempt runs more of what was queued
            }
        }
    }

    /** Drains and flushes until a round brings no new work from the client. */
    void settle() {
        int quietRounds = 0;
        for (int round = 0; round < 40 && quietRounds < 3; round++) {
            long before = flutter.dispatched();
            flutter.drain();
            flutter.flush();
            flutter.drain();
            quietRounds = flutter.dispatched() == before ? quietRounds + 1 : 0;
        }
    }

    UserInput input() {
        return flutter.input();
    }

    // ---------------- input aimed at the subject ----------------

    void press(Key key, int modifiers) {
        input().press(key, modifiers);
        settle();
    }

    void press(Key key) {
        press(key, 0);
    }

    void type(String text) {
        input().type(text);
        settle();
    }

    /** The page position of a point in {@code widget}'s coordinates. */
    double[] page(StyledText widget, int x, int y) {
        double[] rect = flutter.renderedRect(widget);
        return new double[] { rect[0] + x, rect[1] + y };
    }

    /** The widget point a click lands on to put the caret at {@code offset}: its leading edge, mid-line. */
    Point pointAt(StyledText widget, int offset) {
        Point p = widget.getLocationAtOffset(offset);
        // Just inside the leading edge, and never past the client area: the last offset of a
        // right-aligned line sits exactly on the right edge, where a click would miss the widget.
        Rectangle area = widget.getClientArea();
        return new Point(Math.min(p.x + 1, area.width - 1), p.y + widget.getLineHeight(offset) / 2);
    }

    void clickAt(Point widgetPoint, int count, int modifiers) {
        double[] at = page(subject, widgetPoint.x, widgetPoint.y);
        if (lastClickPoint == null || Math.hypot(at[0] - lastClickPoint[0], at[1] - lastClickPoint[1]) <= CLICK_SLOP)
            separateFromLastClick();
        input().click(at[0], at[1], count, modifiers);
        settle();
        lastClickAt = System.currentTimeMillis();
        lastClicked = subject;
        lastClickPoint = at;
    }

    /**
     * When the client chains two presses into a multi-click: on the same control, within
     * kDoubleTapSlop of each other and kDoubleTapTimeout apart (control_evolve.dart registerPointerDown).
     */
    private static final double CLICK_SLOP = 100;
    private static final long CLICK_CHAIN_MS = 300;

    private long lastClickAt;
    private StyledText lastClicked;
    private double[] lastClickPoint;

    /** Waits until a press on the subject can no longer chain with the last one. */
    void separateFromLastClick() {
        if (lastClicked != subject) return;
        long wait = lastClickAt + CLICK_CHAIN_MS + 50 - System.currentTimeMillis();
        if (wait <= 0) return;
        try {
            Thread.sleep(wait);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    void clickAtOffset(int offset) {
        // The position API answers from the last pushed geometry, so push the current one first.
        settle();
        clickAt(pointAt(subject, offset), 1, 0);
    }

    /** Gives the subject keyboard focus the way a user does, leaving the caret at {@code offset}. */
    void focusAt(int offset) {
        clickAtOffset(offset);
    }

    void clickOutside() {
        double[] rect = flutter.renderedRect(outside);
        input().click(rect[0] + 5, rect[1] + rect[3] / 2);
        settle();
    }

    // ---------------- the twin oracle ----------------

    /** Puts the twin in the subject's state, ready to receive the same input. */
    void mirrorSubjectIntoTwin() {
        twin.setWordWrap(subject.getWordWrap());
        twin.setText(subject.getText());
        Point selection = subject.getSelection();
        if (selection.x != selection.y) {
            // setSelection leaves the caret at the end the subject's caret is on.
            if (subject.getCaretOffset() == selection.x) twin.setSelection(selection.y, selection.x);
            else twin.setSelection(selection.x, selection.y);
        } else {
            twin.setCaretOffset(subject.getCaretOffset());
        }
        twin.setTopPixel(subject.getTopPixel());
        twin.setHorizontalPixel(subject.getHorizontalPixel());
        ((DartStyledText) twin.getImpl()).columnX = ((DartStyledText) subject.getImpl()).columnX;
        settle();
        twinLog.clear();
        twinTraversed = false;
    }

    /** Whether a key given to the twin since {@link #mirrorSubjectIntoTwin} moved focus away from it. */
    boolean twinTraversed;

    /** Delivers {@code key} to the twin as SWT itself would receive it from the platform. */
    void pressOnTwin(Key key, int modifiers) {
        // A keyboard reports each modifier as a key of its own: pressed with the modifiers already
        // held in its state mask, released with its own bit included.
        int[] order = { SWT.CTRL, SWT.ALT, SWT.COMMAND, SWT.SHIFT };
        int held = 0;
        for (int modifier : order) {
            if ((modifiers & modifier) == 0) continue;
            twin.notifyListeners(SWT.KeyDown, modifierEvent(modifier, held));
            held |= modifier;
        }
        // As on cocoa: a traversal key is offered as a Traverse (doit false for a key-listening
        // Canvas) and the KeyDown is delivered only when no traversal happened.
        int traversal = traversalOf(key, modifiers);
        Event offered = keyEvent(key, modifiers);
        offered.doit = false;
        boolean traversed = traversal != SWT.TRAVERSE_NONE && twin.traverse(traversal, offered);
        twinTraversed |= traversed;
        if (traversed) {
            subject.setFocus();
        } else {
            twin.notifyListeners(SWT.KeyDown, keyEvent(key, modifiers));
        }
        twin.notifyListeners(SWT.KeyUp, keyEvent(key, modifiers));
        for (int i = order.length - 1; i >= 0; i--) {
            if ((modifiers & order[i]) == 0) continue;
            twin.notifyListeners(SWT.KeyUp, modifierEvent(order[i], held));
            held &= ~order[i];
        }
        settle();
    }

    /** The traversal a platform offers for a key: {@code SwtControl.translateTraversal} on cocoa. */
    private static int traversalOf(Key key, int modifiers) {
        switch (key.toString()) {
            case "Escape": return SWT.TRAVERSE_ESCAPE;
            case "Enter": return SWT.TRAVERSE_RETURN;
            case "Tab": return (modifiers & SWT.SHIFT) != 0 ? SWT.TRAVERSE_TAB_PREVIOUS : SWT.TRAVERSE_TAB_NEXT;
            case "ArrowDown":
            case "ArrowRight": return SWT.TRAVERSE_ARROW_NEXT;
            case "ArrowUp":
            case "ArrowLeft": return SWT.TRAVERSE_ARROW_PREVIOUS;
            case "PageDown": return (modifiers & SWT.CTRL) != 0 ? SWT.TRAVERSE_PAGE_NEXT : SWT.TRAVERSE_NONE;
            case "PageUp": return (modifiers & SWT.CTRL) != 0 ? SWT.TRAVERSE_PAGE_PREVIOUS : SWT.TRAVERSE_NONE;
            default: return SWT.TRAVERSE_NONE;
        }
    }

    private static Event modifierEvent(int modifier, int stateMask) {
        Event e = new Event();
        e.keyCode = modifier;
        e.stateMask = stateMask;
        e.doit = true;
        return e;
    }

    /** A key and modifiers the widget binds to {@code action} on this platform. */
    record Binding(Key key, int modifiers) { }

    Binding bindingFor(StyledText widget, int action) {
        Key[] keys = { Key.LEFT, Key.RIGHT, Key.UP, Key.DOWN, Key.HOME, Key.END, Key.PAGE_UP, Key.PAGE_DOWN,
                Key.BACKSPACE, Key.DELETE, Key.INSERT };
        int[] swtKeys = { SWT.ARROW_LEFT, SWT.ARROW_RIGHT, SWT.ARROW_UP, SWT.ARROW_DOWN, SWT.HOME, SWT.END,
                SWT.PAGE_UP, SWT.PAGE_DOWN, SWT.BS, SWT.DEL, SWT.INSERT };
        int[] modifiers = { 0, SWT.MOD1, SWT.MOD2, SWT.MOD3, SWT.MOD1 | SWT.MOD2, SWT.MOD2 | SWT.MOD3 };
        for (int m : modifiers) {
            for (int i = 0; i < keys.length; i++) {
                if (widget.getKeyBinding(swtKeys[i] | m) == action) return new Binding(keys[i], m);
            }
        }
        throw new IllegalStateException("no key bound to action " + action);
    }

    void pressAction(int action) {
        Binding binding = bindingFor(subject, action);
        press(binding.key(), binding.modifiers());
    }

    static Event keyEvent(Key key, int modifiers) {
        Event e = new Event();
        e.stateMask = modifiers;
        e.doit = true;
        switch (key.toString()) {
            case "Enter": e.keyCode = SWT.CR; e.character = SWT.CR; break;
            case "Tab": e.keyCode = SWT.TAB; e.character = SWT.TAB; break;
            case "Backspace": e.keyCode = SWT.BS; e.character = SWT.BS; break;
            case "Delete": e.keyCode = SWT.DEL; e.character = SWT.DEL; break;
            case "Escape": e.keyCode = SWT.ESC; e.character = SWT.ESC; break;
            case "Insert": e.keyCode = SWT.INSERT; break;
            case "Home": e.keyCode = SWT.HOME; break;
            case "End": e.keyCode = SWT.END; break;
            case "PageUp": e.keyCode = SWT.PAGE_UP; break;
            case "PageDown": e.keyCode = SWT.PAGE_DOWN; break;
            case "ArrowLeft": e.keyCode = SWT.ARROW_LEFT; break;
            case "ArrowRight": e.keyCode = SWT.ARROW_RIGHT; break;
            case "ArrowUp": e.keyCode = SWT.ARROW_UP; break;
            case "ArrowDown": e.keyCode = SWT.ARROW_DOWN; break;
            case "F10": e.keyCode = SWT.F10; break;
            default: {
                char c = key.toString().charAt(0);
                // keyCode is the character the key gives without Shift (SwtWidget.calculateKeycode
                // on cocoa), so Shift+9 is '9' with character '('.
                e.keyCode = unshifted(c);
                boolean control = (modifiers & SWT.CTRL) != 0 && !"cocoa".equals(SWT.getPlatform());
                e.character = control && Character.isLetter(c)
                        ? (char) (Character.toUpperCase(c) - 64)
                        : c;
            }
        }
        return e;
    }

    private static char unshifted(char c) {
        switch (c) {
            case '(': return '9';
            case ')': return '0';
            case '{': return '[';
            case '}': return ']';
            case '"': return '\'';
            default: return Character.toLowerCase(c);
        }
    }

    // ---------------- what the client paints ----------------

    Map<String, Object> facts(StyledText widget) {
        return flutter.renderedFacts(widget);
    }

    @SuppressWarnings("unchecked")
    List<String> renderedLines(StyledText widget) {
        Object lines = facts(widget).get("lines");
        return lines == null ? List.of() : (List<String>) lines;
    }

    /** The document offset of each caret the client shows. */
    @SuppressWarnings("unchecked")
    List<Integer> renderedCarets(StyledText widget) {
        List<Integer> offsets = new ArrayList<>();
        for (Map<String, Object> caret : (List<Map<String, Object>>) facts(widget).getOrDefault("carets", List.of()))
            offsets.add(offsetOf(widget, caret));
        return offsets;
    }

    /** The painted caret's rect in widget coordinates, {x, y, width, height}, or null. */
    @SuppressWarnings("unchecked")
    double[] renderedCaretRect(StyledText widget) {
        List<Map<String, Object>> carets = (List<Map<String, Object>>) facts(widget).getOrDefault("carets", List.of());
        if (carets.isEmpty() || !carets.get(0).containsKey("rect")) return null;
        Map<String, Object> r = (Map<String, Object>) carets.get(0).get("rect");
        return new double[] { num(r.get("x")), num(r.get("y")), num(r.get("width")), num(r.get("height")) };
    }

    /** Each highlighted range the client shows, as {start, end} document offsets. */
    @SuppressWarnings("unchecked")
    List<Point> renderedSelections(StyledText widget) {
        List<Point> ranges = new ArrayList<>();
        for (Map<String, Object> s : (List<Map<String, Object>>) facts(widget).getOrDefault("selections", List.of()))
            ranges.add(new Point(offsetOf(widget, (Map<String, Object>) s.get("start")),
                    offsetOf(widget, (Map<String, Object>) s.get("end"))));
        return ranges;
    }

    /** The highlight rectangles the client painted for the selection, in widget coordinates. */
    @SuppressWarnings("unchecked")
    List<double[]> renderedSelectionRects(StyledText widget) {
        List<double[]> rects = new ArrayList<>();
        for (Map<String, Object> r : (List<Map<String, Object>>) facts(widget).getOrDefault("selectionRects", List.of()))
            rects.add(new double[] { num(r.get("x")), num(r.get("y")), num(r.get("width")), num(r.get("height")) });
        return rects;
    }

    @SuppressWarnings("unchecked")
    double[] renderedScroll(StyledText widget) {
        Map<String, Object> scroll = (Map<String, Object>) facts(widget).get("scroll");
        return new double[] { num(scroll.get("x")), num(scroll.get("y")) };
    }

    /** The logical lines the client actually painted, as {first, last}; empty when it painted none. */
    @SuppressWarnings("unchecked")
    int[] paintedLines(StyledText widget) {
        List<Object> painted = (List<Object>) facts(widget).get("paintedLines");
        if (painted == null || painted.size() < 2) return new int[0];
        return new int[] { (int) num(painted.get(0)), (int) num(painted.get(1)) };
    }

    /** Where the client painted each visible line, by line index, in widget coordinates. */
    @SuppressWarnings("unchecked")
    Map<Integer, Double> paintedRowY(StyledText widget) {
        List<Map<String, Object>> rows = (List<Map<String, Object>>) facts(widget).get("paintedRowY");
        Map<Integer, Double> byLine = new java.util.LinkedHashMap<>();
        if (rows == null) return byLine;
        for (Map<String, Object> row : rows) byLine.put((int) num(row.get("line")), num(row.get("y")));
        return byLine;
    }

    /** The style runs the client paints on {@code line}. */
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> renderedRuns(StyledText widget, int line) {
        List<List<Map<String, Object>>> runs = (List<List<Map<String, Object>>>) facts(widget).get("runs");
        return runs == null || line >= runs.size() ? List.of() : runs.get(line);
    }

    /** The painted run covering {@code offset}, or an empty map. */
    Map<String, Object> renderedRunAt(StyledText widget, int offset) {
        int line = widget.getLineAtOffset(offset);
        int column = offset - widget.getOffsetAtLine(line);
        for (Map<String, Object> run : renderedRuns(widget, line)) {
            if (num(run.get("start")) <= column && column < num(run.get("end"))) return run;
        }
        return Map.of();
    }

    private static int offsetOf(StyledText widget, Map<String, Object> position) {
        int line = (int) num(position.get("line"));
        int column = (int) num(position.get("column"));
        if (line >= widget.getLineCount()) return -1;
        return widget.getOffsetAtLine(line) + column;
    }

    static double num(Object o) {
        return o == null ? Double.NaN : ((Number) o).doubleValue();
    }

    // ---------------- clipboard ----------------

    void setClipboardText(String text) {
        Clipboard clipboard = new Clipboard(display);
        clipboard.setContents(new Object[] { text }, new Transfer[] { TextTransfer.getInstance() });
        clipboard.dispose();
        settle();
    }

    String clipboardText() {
        Clipboard clipboard = new Clipboard(display);
        try {
            return (String) clipboard.getContents(TextTransfer.getInstance());
        } finally {
            clipboard.dispose();
        }
    }

    /** Pushes pending state without waiting for input; for tests that change the widget from Java. */
    void pushState() {
        settle();
    }

    // ---------------- listener events ----------------

    /** Every event SWT delivers to a StyledText's listeners, in order, with the fields that matter. */
    static final class EventLog {
        private static final int[] TYPES = { SWT.KeyDown, SWT.KeyUp, SWT.Verify, SWT.Modify, SWT.Selection,
                SWT.MouseDown, SWT.MouseUp, SWT.MouseDoubleClick, SWT.MouseWheel, SWT.Traverse, SWT.FocusIn,
                SWT.FocusOut, SWT.MenuDetect, SWT.ImeComposition, ST.VerifyKey, ST.CaretMoved, ST.ExtendedModify };

        final List<Event> events = new ArrayList<>();
        final List<String> lines = new ArrayList<>();

        void attach(StyledText widget) {
            Listener recorder = e -> record(widget, e);
            for (int type : TYPES) widget.addListener(type, recorder);
            // SWT delivers a composition to the widget's IME, not to the widget itself
            // (SwtIME.setMarkedText_selectedRange / insertText send on the IME).
            if (widget.getIME() != null) widget.getIME().addListener(SWT.ImeComposition, recorder);
        }

        void clear() {
            events.clear();
            lines.clear();
        }

        private void record(StyledText widget, Event e) {
            Event copy = new Event();
            copy.type = e.type;
            copy.x = e.x;
            copy.y = e.y;
            copy.start = e.start;
            copy.end = e.end;
            copy.text = e.text;
            copy.count = e.count;
            copy.button = e.button;
            copy.detail = e.detail;
            copy.keyCode = e.keyCode;
            copy.character = e.character;
            copy.stateMask = e.stateMask;
            copy.doit = e.doit;
            events.add(copy);
            lines.add(describe(widget, e));
        }

        private static String describe(StyledText widget, Event e) {
            switch (e.type) {
                case SWT.KeyDown: return "KeyDown(keyCode=" + e.keyCode + ", character=" + (int) e.character + ", stateMask=" + e.stateMask + ")";
                case SWT.KeyUp: return "KeyUp(keyCode=" + e.keyCode + ")";
                case ST.VerifyKey: return "VerifyKey(keyCode=" + e.keyCode + ")";
                case SWT.Verify: return "Verify(" + e.start + ", " + e.end + ", " + quote(e.text) + ")";
                case SWT.Modify: return "Modify(caret=" + widget.getCaretOffset() + ")";
                case ST.ExtendedModify: return "ExtendedModify(" + e.start + ", " + e.end + ", " + quote(e.text) + ")";
                case ST.CaretMoved: return "CaretMoved(" + e.end + ")";
                case SWT.Selection: return "Selection(" + e.x + ", " + e.y + ")";
                case SWT.MouseDown: return "MouseDown(button=" + e.button + ", count=" + e.count + ")";
                case SWT.MouseUp: return "MouseUp(button=" + e.button + ", count=" + e.count + ")";
                case SWT.MouseDoubleClick: return "MouseDoubleClick(" + e.x + ", " + e.y + ")";
                case SWT.MouseWheel: return "MouseWheel(count=" + e.count + ")";
                case SWT.Traverse: return "Traverse(detail=" + e.detail + ", doit=" + e.doit + ")";
                case SWT.ImeComposition: return "ImeComposition(detail=" + e.detail + ", " + quote(e.text) + ")";
                case SWT.FocusIn: return "FocusIn";
                case SWT.FocusOut: return "FocusOut";
                case SWT.MenuDetect: return "MenuDetect(detail=" + e.detail + ")";
                default: return "type " + e.type;
            }
        }

        private static String quote(String s) {
            return s == null ? "null" : "'" + s.replace("\r", "\\r").replace("\n", "\\n").replace("\t", "\\t") + "'";
        }

        List<Event> ofType(int type) {
            List<Event> result = new ArrayList<>();
            for (Event e : events) if (e.type == type) result.add(e);
            return result;
        }

        int count(int type) {
            return ofType(type).size();
        }

        /** The descriptions of the editing and caret events, which a keystroke must reproduce exactly. */
        List<String> editingSequence() {
            List<String> result = new ArrayList<>();
            for (int i = 0; i < events.size(); i++) {
                int type = events.get(i).type;
                if (type == SWT.KeyDown || type == SWT.KeyUp || type == ST.VerifyKey || type == SWT.Verify
                        || type == SWT.Modify || type == ST.ExtendedModify || type == ST.CaretMoved
                        || type == SWT.Selection || type == SWT.Traverse) {
                    result.add(lines.get(i));
                }
            }
            return result;
        }
    }
}
