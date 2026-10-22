package org.eclipse.swt.widgets;

/**
 * AppKit enum values the macOS menu classes need, held here rather than read off
 * {@code org.eclipse.swt.internal.cocoa.OS}.
 *
 * Upstream renames these constants across the SWT releases Evolve supports, and not in step:
 * {@code NSCommandKeyMask}, {@code NSShiftKeyMask} and {@code NSControlKeyMask} became
 * {@code NSEventModifierFlag*}, and {@code NSOnState} / {@code NSOffState} became
 * {@code NSControlStateValueOn} / {@code NSControlStateValueOff}, while {@code NSAlternateKeyMask}
 * kept its name. No single spelling resolves against every supported version's {@code OS}, and
 * these files are shared by all of them. The numbers are AppKit ABI constants, so pinning them here
 * is stable in a way that any upstream name is not.
 */
final class AppKitValues {

    /** NSEventModifierFlagShift. */
    static final int SHIFT_KEY_MASK = 1 << 17;

    /** NSEventModifierFlagControl. */
    static final int CONTROL_KEY_MASK = 1 << 18;

    /** NSEventModifierFlagOption. */
    static final int ALTERNATE_KEY_MASK = 1 << 19;

    /** NSEventModifierFlagCommand. */
    static final int COMMAND_KEY_MASK = 1 << 20;

    /** NSControlStateValueOn. */
    static final int CONTROL_STATE_ON = 1;

    /** NSControlStateValueOff. */
    static final int CONTROL_STATE_OFF = 0;

    private AppKitValues() {
    }
}
