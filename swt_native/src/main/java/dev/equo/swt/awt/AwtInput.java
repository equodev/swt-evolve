package dev.equo.swt.awt;

import java.awt.Component;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import javax.swing.SwingUtilities;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * Translates SWT (Flutter-delivered) pointer/keyboard events into
 * {@code java.awt.event.*} and dispatches them into the embedded off-screen
 * Swing {@link Frame} on the EDT — the AWT/Swing mirror of the input forwarding
 * {@code FXCanvas} does for JavaFX.
 *
 * <p>Most events are dispatched to the frame itself; its lightweight dispatcher
 * routes each to the deepest Swing child at the point (frame-local coordinates
 * equal canvas-local coordinates because the frame is sized to the canvas). A
 * HEAVYWEIGHT descendant (e.g. a hosted app's {@code JApplet}) is routed
 * manually instead — see {@link #findHeavyweightAt}.</p>
 */
final class AwtInput {

    private AwtInput() {}

    /**
     * Subscribes this embedding's canvas to SWT input and forwards it to the off-screen frame.
     *
     * <p>Holds the per-embedding state the forwarding needs. {@code buttonsDown} carries the
     * pressed button into {@code MOUSE_DRAGGED}, which Evolve omits from a move's
     * {@code stateMask}; it is cleared on any button-up rather than by unsetting that button's
     * bit, because a release whose reported button doesn't exactly clear what its press set would
     * otherwise strand it non-zero and turn every later move into a drag.
     *
     * <p>{@code capturedTarget}/{@code capturedOffset} emulate a native peer's mouse capture:
     * once a press lands on a heavyweight, its drags and release keep going to that same
     * component at that same offset even if the pointer leaves its bounds. {@code repaintTimer}
     * pushes frames while that capture is held, covering the target's own on-screen response to
     * the drag.
     */
    static void attach(Canvas canvas, Frame frame, Container contentRoot, Runnable forceRepaint) {
        final int[] buttonsDown = {0};
        final Component[] capturedTarget = {null};
        final int[] capturedOffset = {0, 0};
        final javax.swing.Timer[] repaintTimer = {null};

        Listener l = new Listener() {
            @Override
            public void handleEvent(Event e) {
                switch (e.type) {
                    case SWT.MouseDown: {
                        canvas.forceFocus();
                        buttonsDown[0] |= awtButtonDownMask(e.button);
                        postMouse(frame, contentRoot, MouseEvent.MOUSE_PRESSED, e, buttonsDown[0], 1,
                                capturedTarget, capturedOffset, true, repaintTimer, forceRepaint);
                        break;
                    }
                    case SWT.MouseUp: {
                        // AWT expects the released button's down-mask still present on
                        // the RELEASED event's modifiers.
                        postMouse(frame, contentRoot, MouseEvent.MOUSE_RELEASED, e, buttonsDown[0], 1,
                                capturedTarget, capturedOffset, false, repaintTimer, forceRepaint);
                        buttonsDown[0] = 0;
                        break;
                    }
                    case SWT.MouseMove: {
                        int id = buttonsDown[0] != 0
                                ? MouseEvent.MOUSE_DRAGGED : MouseEvent.MOUSE_MOVED;
                        postMouse(frame, contentRoot, id, e, buttonsDown[0], 0,
                                capturedTarget, capturedOffset, false, repaintTimer, forceRepaint);
                        break;
                    }
                    case SWT.MouseEnter:
                        postMouse(frame, contentRoot, MouseEvent.MOUSE_ENTERED, e, buttonsDown[0], 0,
                                capturedTarget, capturedOffset, false, repaintTimer, forceRepaint);
                        break;
                    case SWT.MouseExit:
                        postMouse(frame, contentRoot, MouseEvent.MOUSE_EXITED, e, buttonsDown[0], 0,
                                capturedTarget, capturedOffset, false, repaintTimer, forceRepaint);
                        break;
                    case SWT.KeyDown:
                        postKey(frame, KeyEvent.KEY_PRESSED, e);
                        if (e.character != '\0' && e.character != SWT.CR) {
                            postKeyTyped(frame, e);
                        }
                        break;
                    case SWT.KeyUp:
                        postKey(frame, KeyEvent.KEY_RELEASED, e);
                        break;
                    case SWT.MouseWheel:
                        postWheel(frame, contentRoot, e, buttonsDown[0], forceRepaint);
                        break;
                }
            }
        };
        canvas.addListener(SWT.MouseDown, l);
        canvas.addListener(SWT.MouseUp, l);
        canvas.addListener(SWT.MouseMove, l);
        canvas.addListener(SWT.MouseEnter, l);
        canvas.addListener(SWT.MouseExit, l);
        canvas.addListener(SWT.KeyDown, l);
        canvas.addListener(SWT.KeyUp, l);
        canvas.addListener(SWT.MouseWheel, l);
    }

    /**
     * Forwards one SWT mouse event to the off-screen frame, routing it the way a native peer
     * would.
     *
     * <p>AWT's own {@code LightweightDispatcher} stops retargeting at a heavyweight child, since
     * that child's peer would normally receive real input directly; there is no peer here, so a
     * heavyweight hit is resolved and dispatched manually. That bypasses the peer layer, and three
     * of the services it provides have to be emulated with it: capture across a drag, the
     * {@code MOUSE_CLICKED} it synthesizes from a press/release pair, and retargeting down to the
     * deepest component under the cursor. The last one is applied to moves only — retargeting a
     * press deeper while its release stays captured at the boundary would split one gesture across
     * two components, and a captured drag deliberately keeps its original target.
     *
     * <p>A release also releases the capture, from inside the queue rather than from the SWT
     * listener: the press acquires it a round trip away through the AWT event queue, so a
     * synchronous clear can run first and strand it. It also schedules staggered repaints, which
     * cover both dispatch paths — a heavyweight only has the drag timer while captured, and a
     * lightweight target has no proactive repaint at all, so a toolbar button's click can reach
     * the view's model with nothing on screen reflecting it.
     */
    private static void postMouse(Frame frame, Container contentRoot, int id, Event e, int buttonsDown,
            int clickCount, Component[] capturedTarget, int[] capturedOffset, boolean isPress,
            javax.swing.Timer[] repaintTimer, Runnable forceRepaint) {
        final int x = e.x, y = e.y;
        final int button = awtButton(e.button, id);
        final int modifiers = swtToAwtModifiers(e.stateMask) | buttonsDown;
        final long when = System.currentTimeMillis();
        final boolean popup = e.button == 3;
        if (id == MouseEvent.MOUSE_PRESSED || id == MouseEvent.MOUSE_RELEASED) {
            EvolveSwingHost.ensureDispatcherInstalled();
        }
        EventQueue.invokeLater(() -> {
            Component target = capturedTarget[0];
            int lx, ly;
            if (target != null) {
                lx = x - capturedOffset[0];
                ly = y - capturedOffset[1];
            } else {
                int[] offset = new int[2];
                Component hit = findHeavyweightAt(contentRoot, x, y, offset);
                if (hit != null) {
                    target = hit;
                    lx = x - offset[0];
                    ly = y - offset[1];
                    if (!isPress) {
                        Component deep = SwingUtilities.getDeepestComponentAt(target, lx, ly);
                        if (deep != null && deep != target) {
                            java.awt.Point p = SwingUtilities.convertPoint(target, lx, ly, deep);
                            target = deep;
                            lx = p.x;
                            ly = p.y;
                        }
                    }
                    if (isPress) {
                        capturedTarget[0] = hit;
                        capturedOffset[0] = offset[0];
                        capturedOffset[1] = offset[1];
                        startRepaintTimer(repaintTimer, forceRepaint);
                    }
                } else {
                    lx = x;
                    ly = y;
                }
            }
            if (target != null) {
                target.dispatchEvent(new MouseEvent(target, id, when, modifiers, lx, ly,
                        clickCount, popup, button));
                if (id == MouseEvent.MOUSE_RELEASED) {
                    target.dispatchEvent(new MouseEvent(target, MouseEvent.MOUSE_CLICKED, when,
                            modifiers, lx, ly, clickCount, popup, button));
                }
            } else {
                frame.dispatchEvent(new MouseEvent(frame, id, when, modifiers, x, y,
                        clickCount, popup, button));
            }
            if (id == MouseEvent.MOUSE_RELEASED) {
                capturedTarget[0] = null;
                stopRepaintTimer(repaintTimer);
                EvolveSwingHost.scheduleStaggeredRepaints(forceRepaint, 100, 300, 700, 1500);
            }
        });
    }

    /**
     * Forwards one wheel notch. Wheel events are never captured or dragged, so unlike
     * {@link #postMouse} this always does a fresh {@link #findHeavyweightAt} hit-test rather than
     * reusing a held target.
     *
     * <p>SWT and AWT disagree on sign: SWT's {@code count} is positive scrolling away, AWT's
     * {@code getWheelRotation()} is negative. A heavyweight hit also gets staggered repaints — a
     * wheel-driven zoom updates the view's model immediately but, like a drag, posts no
     * {@code PaintEvent} of its own. A notch is discrete and infrequent, so retrying is cheap;
     * lightweight scrolling already repaints itself and is left alone.
     */
    private static void postWheel(Frame frame, Container contentRoot, Event e, int buttonsDown,
            Runnable forceRepaint) {
        final int x = e.x, y = e.y;
        final int modifiers = swtToAwtModifiers(e.stateMask) | buttonsDown;
        final long when = System.currentTimeMillis();
        final int wheelRotation = e.count > 0 ? -1 : 1;
        EventQueue.invokeLater(() -> {
            int[] offset = new int[2];
            Component hit = findHeavyweightAt(contentRoot, x, y, offset);
            Component target = hit != null ? hit : frame;
            int lx = hit != null ? x - offset[0] : x;
            int ly = hit != null ? y - offset[1] : y;
            target.dispatchEvent(new MouseWheelEvent(target, MouseEvent.MOUSE_WHEEL, when, modifiers,
                    lx, ly, 0, false, MouseWheelEvent.WHEEL_UNIT_SCROLL, 3, wheelRotation));
            if (hit != null) EvolveSwingHost.scheduleStaggeredRepaints(forceRepaint, 60, 200, 500);
        });
    }

    private static void startRepaintTimer(javax.swing.Timer[] repaintTimer, Runnable forceRepaint) {
        if (repaintTimer[0] != null) return;
        javax.swing.Timer t = new javax.swing.Timer(33, ev -> forceRepaint.run());
        t.setRepeats(true);
        repaintTimer[0] = t;
        t.start();
    }

    private static void stopRepaintTimer(javax.swing.Timer[] repaintTimer) {
        if (repaintTimer[0] == null) return;
        repaintTimer[0].stop();
        repaintTimer[0] = null;
    }

    /**
     * Finds the heavyweight descendant of {@code root} under (x,y), with its absolute offset from
     * {@code root}'s origin — or {@code null} if the point resolves to purely lightweight content,
     * which {@code frame.dispatchEvent()}'s own {@code LightweightDispatcher} already routes
     * correctly.
     *
     * <p>AWT's {@code LightweightDispatcher} deliberately stops retargeting a synthetic event at a
     * heavyweight child — normally that child's own native peer receives real input directly there
     * is no such peer here (the same reason {@code EvolveSwingHost.paintHeavyweightDescendants}
     * exists for output), so input needs the same manual routing the paint side already gets.</p>
     */
    private static Component findHeavyweightAt(Container root, int x, int y, int[] offsetOut) {
        return findHeavyweightAt(root, x, y, 0, 0, offsetOut);
    }

    private static Component findHeavyweightAt(Container root, int x, int y, int baseX, int baseY,
            int[] offsetOut) {
        for (Component c : root.getComponents()) {
            if (!c.isVisible()) continue;
            Rectangle b = c.getBounds();
            if (x < b.x || y < b.y || x >= b.x + b.width || y >= b.y + b.height) continue;
            int cx = baseX + b.x, cy = baseY + b.y;
            if (!c.isLightweight()) {
                offsetOut[0] = cx;
                offsetOut[1] = cy;
                return c;
            }
            if (c instanceof Container) {
                Component deeper = findHeavyweightAt((Container) c, x - b.x, y - b.y, cx, cy, offsetOut);
                if (deeper != null) return deeper;
            }
        }
        return null;
    }

    private static void postKey(Frame frame, int id, Event e) {
        final int modifiers = swtToAwtModifiers(e.stateMask);
        final int keyCode = swtToAwtKeyCode(e.keyCode, e.character);
        final char ch = e.character == '\0' ? KeyEvent.CHAR_UNDEFINED : e.character;
        final long when = System.currentTimeMillis();
        EventQueue.invokeLater(() -> {
            // Key events go to the focus owner (e.g. a JTextField), not the frame — the frame's
            // dispatch does not route them there, so text input would otherwise be dropped.
            Component target = focusTarget(frame);
            target.dispatchEvent(new KeyEvent(target, id, when, modifiers, keyCode, ch,
                    KeyEvent.KEY_LOCATION_STANDARD));
        });
    }

    private static void postKeyTyped(Frame frame, Event e) {
        final int modifiers = swtToAwtModifiers(e.stateMask);
        final char ch = e.character;
        final long when = System.currentTimeMillis();
        EventQueue.invokeLater(() -> {
            Component target = focusTarget(frame);
            // KEY_TYPED carries the actual character that text components insert; keyLocation
            // must be UNKNOWN for a typed event.
            target.dispatchEvent(new KeyEvent(target, KeyEvent.KEY_TYPED, when, modifiers,
                    KeyEvent.VK_UNDEFINED, ch, KeyEvent.KEY_LOCATION_UNKNOWN));
        });
    }

    /** The Swing component that should receive keyboard input, falling back to the frame. */
    private static Component focusTarget(Frame frame) {
        Component fo = frame.getFocusOwner();
        if (fo == null) fo = frame.getMostRecentFocusOwner();
        return fo != null ? fo : frame;
    }

    private static int awtButton(int swtButton, int id) {
        if (id == MouseEvent.MOUSE_MOVED || id == MouseEvent.MOUSE_DRAGGED
                || id == MouseEvent.MOUSE_ENTERED || id == MouseEvent.MOUSE_EXITED) {
            return MouseEvent.NOBUTTON;
        }
        switch (swtButton) {
            case 1: return MouseEvent.BUTTON1;
            case 2: return MouseEvent.BUTTON2;
            case 3: return MouseEvent.BUTTON3;
            default: return MouseEvent.NOBUTTON;
        }
    }

    /**
     * Sets both the modern extended ({@code *_DOWN_MASK}) and the deprecated legacy bits, as a
     * real AWT-generated event carries both. Host code written against the pre-1.4
     * {@code getModifiers()} API is still common — a middle-button-drag gate testing
     * {@code getModifiers() & BUTTON2_MASK} would otherwise read a silent zero.
     */
    @SuppressWarnings("deprecation")
    private static int awtButtonDownMask(int swtButton) {
        switch (swtButton) {
            case 1: return InputEvent.BUTTON1_DOWN_MASK | InputEvent.BUTTON1_MASK;
            case 2: return InputEvent.BUTTON2_DOWN_MASK | InputEvent.BUTTON2_MASK;
            case 3: return InputEvent.BUTTON3_DOWN_MASK | InputEvent.BUTTON3_MASK;
            default: return 0;
        }
    }

    @SuppressWarnings("deprecation")
    private static int swtToAwtModifiers(int stateMask) {
        int m = 0;
        if ((stateMask & SWT.SHIFT) != 0) m |= InputEvent.SHIFT_DOWN_MASK | InputEvent.SHIFT_MASK;
        if ((stateMask & SWT.CONTROL) != 0) m |= InputEvent.CTRL_DOWN_MASK | InputEvent.CTRL_MASK;
        if ((stateMask & SWT.ALT) != 0) m |= InputEvent.ALT_DOWN_MASK | InputEvent.ALT_MASK;
        if ((stateMask & SWT.COMMAND) != 0) m |= InputEvent.META_DOWN_MASK | InputEvent.META_MASK;
        return m;
    }

    /** Best-effort SWT keyCode → AWT VK. Text input rides the KEY_TYPED character. */
    private static int swtToAwtKeyCode(int swtKeyCode, char character) {
        switch (swtKeyCode) {
            case SWT.ARROW_UP: return KeyEvent.VK_UP;
            case SWT.ARROW_DOWN: return KeyEvent.VK_DOWN;
            case SWT.ARROW_LEFT: return KeyEvent.VK_LEFT;
            case SWT.ARROW_RIGHT: return KeyEvent.VK_RIGHT;
            case SWT.CR:
            case SWT.KEYPAD_CR: return KeyEvent.VK_ENTER;
            case SWT.BS: return KeyEvent.VK_BACK_SPACE;
            case SWT.DEL: return KeyEvent.VK_DELETE;
            case SWT.TAB: return KeyEvent.VK_TAB;
            case SWT.ESC: return KeyEvent.VK_ESCAPE;
            case SWT.HOME: return KeyEvent.VK_HOME;
            case SWT.END: return KeyEvent.VK_END;
            default:
                if (character >= 'a' && character <= 'z') return Character.toUpperCase(character);
                if (character >= 'A' && character <= 'Z') return character;
                if (character >= '0' && character <= '9') return character;
                return KeyEvent.VK_UNDEFINED;
        }
    }
}
