package dev.equo.swt.size;

/**
 * The text a widget draws for a string in which '&' marks a mnemonic, as SWT draws it: every single '&' is
 * dropped, the one at the end too, and "&&" draws one '&'. All three platforms drop every single '&', not
 * only the first; Win32 and GTK drop a trailing one as well (Cocoa keeps it).
 */
public final class Mnemonics {

    private Mnemonics() {
    }

    /** The drawn text: one pass, and the string itself when it holds no '&'. */
    public static String display(String text) {
        if (text == null)
            return null;
        int length = text.length();
        char[] drawn = null;
        int j = 0;
        for (int i = 0; i < length; i++) {
            char c = text.charAt(i);
            if (c == '&') {
                if (drawn == null) {
                    // Each '&' drops a character, so the drawn text is at least one shorter.
                    drawn = new char[length - 1];
                    text.getChars(0, i, drawn, 0);
                    j = i;
                }
                if (i + 1 < length && text.charAt(i + 1) == '&') {
                    drawn[j++] = '&';
                    i++;
                }
            } else if (drawn != null) {
                drawn[j++] = c;
            }
        }
        return drawn == null ? text : new String(drawn, 0, j);
    }
}
