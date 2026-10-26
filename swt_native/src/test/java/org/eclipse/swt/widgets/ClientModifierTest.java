package org.eclipse.swt.widgets;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.swt.SWT;
import org.junit.jupiter.api.Test;

/** A Mac client's Cmd chord reaches the listeners as the platform's MOD1 chord. */
class ClientModifierTest {

    private static Event key(int stateMask) {
        Event e = new Event();
        e.keyCode = 's';
        e.character = 's';
        e.stateMask = stateMask;
        return e;
    }

    @Test
    void commandBecomesMod1() {
        Event e = key(SWT.COMMAND);

        ControlHelper.applyClientModifier(e);

        assertEquals(SWT.MOD1, e.stateMask);
    }

    @Test
    void otherModifiersAreKept() {
        Event e = key(SWT.COMMAND | SWT.SHIFT | SWT.ALT);

        ControlHelper.applyClientModifier(e);

        assertEquals(SWT.MOD1 | SWT.SHIFT | SWT.ALT, e.stateMask);
    }

    @Test
    void aKeyWithoutCommandIsUntouched() {
        Event e = key(SWT.CTRL | SWT.SHIFT);

        ControlHelper.applyClientModifier(e);

        assertEquals(SWT.CTRL | SWT.SHIFT, e.stateMask);
    }
}
