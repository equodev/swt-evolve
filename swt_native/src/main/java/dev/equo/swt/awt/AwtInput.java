package dev.equo.swt.awt;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

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
 * <p>Events are dispatched to the frame itself; its lightweight dispatcher routes
 * each to the deepest Swing child at the point (frame-local coordinates equal
 * canvas-local coordinates because the frame is sized to the canvas).</p>
 */
final class AwtInput {

    private AwtInput() {}

    static void attach(Canvas canvas, Frame frame) {
        // Bitmask of AWT BUTTONx_DOWN masks currently held — kept so MOUSE_DRAGGED
        // carries the pressed button, which Evolve omits from a move's stateMask.
        final int[] buttonsDown = {0};

        Listener l = new Listener() {
            @Override
            public void handleEvent(Event e) {
                switch (e.type) {
                    case SWT.MouseDown: {
                        canvas.forceFocus();
                        buttonsDown[0] |= awtButtonDownMask(e.button);
                        post(frame, MouseEvent.MOUSE_PRESSED, e, buttonsDown[0], 1);
                        break;
                    }
                    case SWT.MouseUp: {
                        int mask = awtButtonDownMask(e.button);
                        // AWT expects the released button's down-mask still present on
                        // the RELEASED event's modifiers.
                        post(frame, MouseEvent.MOUSE_RELEASED, e, buttonsDown[0], 1);
                        buttonsDown[0] &= ~mask;
                        break;
                    }
                    case SWT.MouseMove: {
                        int id = buttonsDown[0] != 0
                                ? MouseEvent.MOUSE_DRAGGED : MouseEvent.MOUSE_MOVED;
                        post(frame, id, e, buttonsDown[0], 0);
                        break;
                    }
                    case SWT.MouseEnter:
                        post(frame, MouseEvent.MOUSE_ENTERED, e, buttonsDown[0], 0);
                        break;
                    case SWT.MouseExit:
                        post(frame, MouseEvent.MOUSE_EXITED, e, buttonsDown[0], 0);
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
    }

    private static void post(Frame frame, int id, Event e, int buttonsDown, int clickCount) {
        final int x = e.x, y = e.y;
        final int button = awtButton(e.button, id);
        final int modifiers = swtToAwtModifiers(e.stateMask) | buttonsDown;
        final long when = System.currentTimeMillis();
        final boolean popup = e.button == 3;
        EventQueue.invokeLater(() -> {
            Component c = frame;
            frame.dispatchEvent(new MouseEvent(c, id, when, modifiers, x, y,
                    clickCount, popup, button));
        });
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

    private static int awtButtonDownMask(int swtButton) {
        switch (swtButton) {
            case 1: return InputEvent.BUTTON1_DOWN_MASK;
            case 2: return InputEvent.BUTTON2_DOWN_MASK;
            case 3: return InputEvent.BUTTON3_DOWN_MASK;
            default: return 0;
        }
    }

    private static int swtToAwtModifiers(int stateMask) {
        int m = 0;
        if ((stateMask & SWT.SHIFT) != 0) m |= InputEvent.SHIFT_DOWN_MASK;
        if ((stateMask & SWT.CONTROL) != 0) m |= InputEvent.CTRL_DOWN_MASK;
        if ((stateMask & SWT.ALT) != 0) m |= InputEvent.ALT_DOWN_MASK;
        if ((stateMask & SWT.COMMAND) != 0) m |= InputEvent.META_DOWN_MASK;
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
