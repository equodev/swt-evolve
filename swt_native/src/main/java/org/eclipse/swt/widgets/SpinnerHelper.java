package org.eclipse.swt.widgets;

import java.text.DecimalFormatSymbols;
import java.util.Locale;

import org.eclipse.swt.SWT;

public class SpinnerHelper {

    private SpinnerHelper() {
    }

    public static String getText(DartSpinner c) {
        c.checkWidget();
        return c.keptText != null ? c.keptText : format(c, c.selection);
    }

    /**
     * The decimal separator of the user's locale, which is what the text of a native spinner shows (the number
     * formatter's on Cocoa, the user locale's on Win32, the C library's on GTK). The user is the session's client.
     */
    public static String decimalSeparator(DartSpinner c) {
        Locale locale = c.textLocale();
        return String.valueOf(DecimalFormatSymbols.getInstance(locale == null ? Locale.ROOT : locale).getDecimalSeparator());
    }

    /** {@code value} as the spinner's text field shows it. */
    static String format(DartSpinner c, int value) {
        return format(value, c.digits, decimalSeparator(c));
    }

    /**
     * Renders a spinner value the way its text field shows it: {@code digits} decimal places, and the sign of a
     * negative value in front of them, as Win32 and GTK write it ("-0.05").
     */
    static String format(int value, int digits, String separator) {
        if (digits <= 0) {
            return String.valueOf(value);
        }
        String string = String.valueOf(Math.abs((long) value));
        int index = string.length() - digits;
        StringBuilder buffer = new StringBuilder(value < 0 ? "-" : "");
        if (index > 0) {
            buffer.append(string, 0, index);
            buffer.append(separator);
            buffer.append(string.substring(index));
        } else {
            buffer.append("0").append(separator);
            while (index++ < 0) buffer.append("0");
            buffer.append(string);
        }
        return buffer.toString();
    }

    /**
     * Up and Down step the value by the increment, Page Up and Page Down by the page increment, as
     * the platform's spinner does for a key no listener vetoed: the new text is offered as a Verify
     * over the old, then Modify and Selection. A WRAP spinner wraps past either end to the other.
     */
    public static void stepByKey(DartSpinner c, Event key) {
        if ((key.stateMask & SWT.MODIFIER_MASK) != 0)
            return;
        int delta;
        switch (key.keyCode) {
            case SWT.ARROW_UP:
                delta = c.increment;
                break;
            case SWT.ARROW_DOWN:
                delta = -c.increment;
                break;
            case SWT.PAGE_UP:
                delta = c.pageIncrement;
                break;
            case SWT.PAGE_DOWN:
                delta = -c.pageIncrement;
                break;
            default:
                return;
        }
        int value = c.selection + delta;
        if ((c.getApi().getStyle() & SWT.WRAP) != 0) {
            if (value > c.maximum)
                value = c.minimum;
            else if (value < c.minimum)
                value = c.maximum;
        }
        value = Math.max(c.minimum, Math.min(c.maximum, value));
        if (value == c.selection)
            return;
        Event verify = new Event();
        verify.text = format(c, value);
        verify.start = 0;
        verify.end = getText(c).length();
        c.sendEvent(SWT.Verify, verify);
        if (!verify.doit || c.isDisposed())
            return;
        applySelection(c, value, true, true, rewritten(c, value, verify));
    }

    /**
     * Sets the range, the steps and the digits, then the selection. Its Verify replaces the text the field shows,
     * which the old digits wrote.
     */
    static void setValues(DartSpinner c, int selection, int minimum, int maximum, int digits, int increment,
            int pageIncrement) {
        if (maximum < minimum || digits < 0 || increment < 1 || pageIncrement < 1)
            return;
        String shown = getText(c);
        if (c.minimum != minimum)
            c.getValue().markDirty(VSpinner.MINIMUM);
        if (c.maximum != maximum)
            c.getValue().markDirty(VSpinner.MAXIMUM);
        if (c.increment != increment)
            c.getValue().markDirty(VSpinner.INCREMENT);
        if (c.pageIncrement != pageIncrement)
            c.getValue().markDirty(VSpinner.PAGE_INCREMENT);
        if (c.digits != digits)
            c.getValue().markDirty(VSpinner.DIGITS);
        c.minimum = minimum;
        c.maximum = maximum;
        c.increment = increment;
        c.pageIncrement = pageIncrement;
        c.digits = digits;
        keepText(c, shown);
        c.setSelection(selection);
    }

    /**
     * Takes the new digits, then offers the text they write as a Verify over the one the field shows, as natively:
     * a refused text stays in the field with the new digits taken, a rewritten one is what the field shows.
     */
    static void setDigits(DartSpinner c, int value) {
        c.checkWidget();
        if (value < 0)
            c.error(SWT.ERROR_INVALID_ARGUMENT);
        if (value == c.digits)
            return;
        String shown = getText(c);
        c.getValue().markDirty(VSpinner.DIGITS);
        c.digits = value;
        keepText(c, shown);
        setSelection(c, c.selection, false, true, false);
    }

    /**
     * Moves the spinner to {@code value}. With {@code setText} the text follows, as natively: a Verify listener
     * hears the new text first and may refuse it, which keeps the old text and sends no Modify or Selection, or
     * rewrite it, which the field then shows; the position has moved already, as the native stepper's has.
     *
     * @return false when a Verify listener refused the text
     */
    static boolean setSelection(DartSpinner c, int value, boolean setPos, boolean setText, boolean notify) {
        if (c.selection != value) {
            c.getValue().markDirty(VSpinner.SELECTION);
        }
        if (setText && (c.hooks(SWT.Verify) || c.filters(SWT.Verify))) {
            String old = getText(c);
            Event verify = new Event();
            verify.text = format(c, value);
            verify.start = 0;
            verify.end = old.length();
            // The field still shows the old text while the listeners decide, as a native one does.
            keepText(c, old);
            c.selection = value;
            c.sendEvent(SWT.Verify, verify);
            if (c.isDisposed()) {
                return true;
            }
            if (!verify.doit) {
                return false;
            }
            applySelection(c, value, true, notify, rewritten(c, value, verify));
            return true;
        }
        applySelection(c, value, setText, notify, null);
        return true;
    }

    /** The text a Verify listener put in place of {@code value}'s own, or null when it left it as offered. */
    private static String rewritten(DartSpinner c, int value, Event verify) {
        return java.util.Objects.equals(verify.text, format(c, value)) ? null : verify.text;
    }

    private static void applySelection(DartSpinner c, int value, boolean setText, boolean notify, String shown) {
        if (c.selection != value) {
            c.getValue().markDirty(VSpinner.SELECTION);
        }
        c.selection = value;
        if (setText) {
            keepText(c, shown);
            c.sendEvent(SWT.Modify);
        }
        if (notify)
            c.sendSelectionEvent(SWT.Selection);
    }

    /** {@code text} stays in the field over the selection, or with null the field shows the selection again. */
    private static void keepText(DartSpinner c, String text) {
        if (text == null ? c.keptText != null : !text.equals(c.keptText)) {
            c.getValue().markDirty(VSpinner.KEPT_TEXT);
        }
        c.keptText = text;
    }

    /**
     * A value the user chose with the arrows of the client. The text follows it, so listeners hear Modify and then
     * Selection, in the order a native spinner sends them; a refused text sends neither.
     */
    static void selectedInFlutter(DartSpinner c, int value) {
        if (!setSelection(c, Math.max(c.minimum, Math.min(c.maximum, value)), true, true, false) || c.isDisposed()) {
            return;
        }
        c.sendSelectionEvent(SWT.Selection, null, true);
    }

    /**
     * A value committed with Enter in the client. The edits that typed it were already handled by
     * {@link #editedInFlutter}, so a value that stands only gets DefaultSelection; one that differs (the client
     * clamps what Java held out of range) is applied first and notified as Selection.
     */
    static void committedInFlutter(DartSpinner c, Event e) {
        int previous = c.selection;
        // An edit already moved the value and notified Modify, so an Enter on a value that stands only commits.
        if (e.index != previous) {
            c.setSelection(e.index);
            if (c.isDisposed()) {
                return;
            }
            if (c.selection != previous) {
                c.sendSelectionEvent(SWT.Selection, null, true);
                if (c.isDisposed()) {
                    return;
                }
            }
        }
        c.sendEvent(SWT.DefaultSelection, e);
    }

    /**
     * The value a text field stands for, read as Cocoa's {@code getSelectionText} reads it: the host's decimal
     * separator, scaled by the digits, and a value outside the range is no value. Null when there is none, which is
     * what a partial edit ("", "-", "5x") or an out-of-range one leaves the selection on.
     */
    static Integer parseText(String string, int digits, int minimum, int maximum, String separator) {
        try {
            int value;
            if (digits > 0) {
                int index = string.indexOf(separator);
                if (index != -1) {
                    int startIndex = string.startsWith("+") || string.startsWith("-") ? 1 : 0;
                    String wholePart = startIndex != index ? string.substring(startIndex, index) : "0";
                    StringBuilder decimalPart = new StringBuilder(string.substring(index + separator.length()));
                    if (decimalPart.length() > digits) {
                        decimalPart.setLength(digits);
                    }
                    while (decimalPart.length() < digits) {
                        decimalPart.append('0');
                    }
                    int wholeValue = Integer.parseInt(wholePart);
                    int decimalValue = Integer.parseInt(decimalPart.toString());
                    for (int i = 0; i < digits; i++) wholeValue *= 10;
                    value = wholeValue + decimalValue;
                    if (string.startsWith("-")) value = -value;
                } else {
                    value = Integer.parseInt(string);
                    for (int i = 0; i < digits; i++) value *= 10;
                }
            } else {
                value = Integer.parseInt(string);
            }
            if (minimum <= value && value <= maximum) {
                return value;
            }
        } catch (NumberFormatException ex) {
            // not a number: no value
        }
        return null;
    }

    /**
     * An edit of the text typed in the client, in the order a native spinner takes it: Verify is asked about the
     * characters being replaced when anyone listens, then a text that stands for a value in range moves the
     * selection and notifies Selection, then Modify, whatever the text was. The selection of a partial or
     * out-of-range text stays where it was.
     *
     * <p>The event carries the new text, the range of the previous text it replaced ({@code start}, {@code end}) and
     * the number of characters it put there ({@code count}). Selection and Modify are posted, to be delivered once the
     * edit is handled, as Cocoa and GTK deliver both and Win32 its Selection.
     */
    static void editedInFlutter(DartSpinner c, Event e) {
        if (e.text == null) {
            // A notice with no edit in it: there is nothing to ask or to read.
            c.sendEvent(SWT.Modify, e);
            return;
        }
        String text = e.text;
        if (c.hooks(SWT.Verify) || c.filters(SWT.Verify)) {
            int count = Math.max(0, Math.min(e.count, text.length() - e.start));
            String typed = text.substring(e.start, e.start + count);
            String verified = verifyText(c, typed, e.start, e.end);
            if (c.isDisposed()) {
                return;
            }
            boolean accepted = typed.equals(verified);
            // The client shows what was typed until told otherwise: a veto, or text a listener changed, sends it
            // back to the value Java holds.
            dev.equo.swt.FlutterBridge.send(c, "modify/verdict", dev.equo.swt.Java8.map("doit", accepted));
            if (verified == null) {
                return;
            }
            if (!accepted) {
                text = text.substring(0, e.start) + verified + text.substring(e.start + count);
            }
        }
        keepText(c, null);
        Integer value = parseText(text, c.digits, c.minimum, c.maximum, decimalSeparator(c));
        if (value != null && value != c.selection) {
            c.getValue().markDirty(VSpinner.SELECTION);
            c.selection = value;
            c.sendSelectionEvent(SWT.Selection, null, false);
        }
        c.postEvent(SWT.Modify);
    }

    /**
     * What a Verify listener answers to characters the user is putting into the field; null vetoes them. By default
     * it lets through digits, the separator of the host's locale in a spinner with digits, and a leading minus in one
     * that can go below zero, as Win32 and GTK do (Cocoa takes neither the minus nor another separator than a point).
     */
    private static String verifyText(DartSpinner c, String string, int start, int end) {
        Event event = new Event();
        event.text = string;
        event.start = start;
        event.end = end;
        String digitsOnly = string;
        int index = 0;
        if (c.digits > 0) {
            String separator = decimalSeparator(c);
            int point = digitsOnly.indexOf(separator);
            if (point != -1) {
                digitsOnly = digitsOnly.substring(0, point) + digitsOnly.substring(point + separator.length());
            }
        }
        if (c.minimum < 0 && digitsOnly.startsWith("-")) {
            index++;
        }
        while (index < digitsOnly.length()) {
            if (!Character.isDigit(digitsOnly.charAt(index))) {
                break;
            }
            index++;
        }
        event.doit = index == digitsOnly.length();
        c.sendEvent(SWT.Verify, event);
        if (!event.doit || c.isDisposed()) {
            return null;
        }
        return event.text;
    }

}
