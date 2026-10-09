package dev.equo.swt.size;

/** The text a widget draws for a string in which '&' marks a mnemonic. */
public final class Mnemonics {

    private Mnemonics() {
    }

    /** Drops the '&' that marks a mnemonic and turns "&&" into a literal '&'. */
    public static String display(String text) {
        if (text == null || text.indexOf('&') < 0)
            return text;
        StringBuilder drawn = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c != '&') {
                drawn.append(c);
            } else if (i + 1 < text.length() && text.charAt(i + 1) == '&') {
                drawn.append('&');
                i++;
            }
        }
        return drawn.toString();
    }
}
