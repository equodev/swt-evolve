package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import org.eclipse.swt.SWT;


/**
 * Delivers the client's composition (text, caret, commit) to a Canvas's {@link IME} as
 * {@link SWT#ImeComposition} events, the way the platform's text-input client does.
 */
public final class ImeHelper {

    private ImeHelper() {
    }

    public static void hookComposition(DartCanvas canvas) {
        FlutterBridge.onPayload(canvas, dev.equo.swt.WireTable.CHANNEL_IME_COMPOSITION, payload -> {
            if (payload == null)
                return;
            canvas.getDisplay().asyncExec(() -> apply(canvas, payload));
        });
    }

    static void apply(DartCanvas canvas, byte[] payload) {
        if (canvas.isDisposed())
            return;
        IME ime = canvas.getApi().getIME();
        if (ime == null || ime.isDisposed() || !(ime.getImpl() instanceof DartIME))
            return;
        // The text, the caret within it, and whether it is committed.
        dev.equo.swt.WireReader r = new dev.equo.swt.WireReader(payload, 0, payload.length);
        String text = r.string();
        int caret = r.int32();
        boolean commit = r.bool();
        if (text == null)
            return;
        DartIME impl = (DartIME) ime.getImpl();
        if (commit) {
            if (commit(impl, text))
                insertCommitted(canvas, text);
        } else {
            compose(impl, text, caret);
        }
    }

    /** The composition changed to {@code text}, with the caret {@code caret} characters into it. */
    static void compose(DartIME ime, String text, int caret) {
        if (!ime.isInlineEnabled())
            return;
        ime.resetStyles();
        ime.caretOffset = ime.commitCount = 0;
        int end = ime.startOffset + ime.text.length();
        if (ime.startOffset == -1) {
            Event event = new Event();
            event.detail = SWT.COMPOSITION_SELECTION;
            ime.sendEvent(SWT.ImeComposition, event);
            if (ime.isDisposed())
                return;
            ime.startOffset = event.start;
            end = event.end;
        }
        ime.caretOffset = Math.max(0, Math.min(caret, text.length()));
        Event event = new Event();
        event.detail = SWT.COMPOSITION_CHANGED;
        event.start = ime.startOffset;
        event.end = end;
        event.text = ime.text = text;
        ime.sendEvent(SWT.ImeComposition, event);
        if (ime.isDisposed())
            return;
        if (text.isEmpty()) {
            ime.startOffset = -1;
            ime.caretOffset = 0;
        }
    }

    /**
     * Answers whether the committed text is still to be inserted: the event takes the composed text
     * back out of the document, and the platform then types it (see {@link #insertCommitted}).
     */
    static boolean commit(DartIME ime, String text) {
        if (!ime.isInlineEnabled())
            return false;
        if (ime.startOffset == -1) {
            compose(ime, text, text.length());
            if (ime.isDisposed() || ime.startOffset == -1)
                return false;
        }
        int end = ime.startOffset + ime.text.length();
        ime.resetStyles();
        ime.caretOffset = ime.commitCount = text.length();
        Event event = new Event();
        event.detail = SWT.COMPOSITION_CHANGED;
        event.start = ime.startOffset;
        event.end = end;
        event.text = ime.text = text;
        ime.sendEvent(SWT.ImeComposition, event);
        if (ime.isDisposed())
            return false;
        ime.text = "";
        ime.caretOffset = ime.commitCount = 0;
        ime.startOffset = -1;
        return event.doit;
    }

    /** Typed as key events, as the platform does after a commit, so Verify and Modify listeners see it. */
    private static void insertCommitted(DartCanvas canvas, String text) {
        for (int i = 0; i < text.length() && !canvas.isDisposed(); i++) {
            Event event = new Event();
            event.type = SWT.KeyDown;
            event.character = text.charAt(i);
            canvas.getApi().notifyListeners(SWT.KeyDown, event);
        }
    }
}
