package dev.equo.swt.size;

import java.util.regex.Pattern;

import dev.equo.swt.Java8;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.DartCCombo;
import org.eclipse.swt.custom.DartCLabel;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.DartButton;
import org.eclipse.swt.widgets.DartCombo;
import org.eclipse.swt.widgets.DartLabel;
import org.eclipse.swt.widgets.DartLink;
import org.eclipse.swt.widgets.DartTableItem;
import org.eclipse.swt.widgets.DartText;
import org.eclipse.swt.widgets.DartToolItem;
import org.eclipse.swt.widgets.DartTreeItem;

import static dev.equo.swt.Styles.hasFlags;

/**
 * The text and the image a widget draws, which is what its size is measured from: the size tables
 * {@code measure.dart} emits ask here.
 */
final class Drawn {

    private static final Pattern LINK_MARKUP = Pattern.compile("<[^>]+>");

    private Drawn() {
    }

    /** Without the mnemonic marker. */
    static String text(DartButton widget) {
        return Mnemonics.display(widget.getText());
    }

    /** Without the mnemonic marker, and only while the label shows its text rather than its image. */
    static String text(DartLabel widget) {
        return Mnemonics.display(widget._displayedText());
    }

    /** Without the mnemonic marker; a CLabel never given a text draws none. */
    static String text(DartCLabel widget) {
        String text = widget.getText();
        return text == null ? "" : Mnemonics.display(text);
    }

    /** Without its markup and without the mnemonic marker. */
    static String text(DartLink widget) {
        String text = widget.getText();
        return text == null ? null : Mnemonics.display(LINK_MARKUP.matcher(text).replaceAll(""));
    }

    /** As typed, or its echo character once per character when it hides what is typed. */
    static String text(DartText widget) {
        String text = widget.getText();
        char echo = widget.getEchoChar();
        // The embedded GTK and Win32 implementations report no echo character for a password.
        if (echo == '\0' && hasFlags(widget.getStyle(), SWT.PASSWORD))
            echo = '*';
        return text == null || echo == '\0' ? text : Java8.repeat(String.valueOf(echo), text.length());
    }

    static String text(DartCombo widget) {
        return widget.getText();
    }

    static String text(DartCCombo widget) {
        return widget.getText();
    }

    static String text(DartTableItem widget) {
        return widget.getText();
    }

    static String text(DartToolItem widget) {
        return widget.getText();
    }

    static String text(DartTreeItem widget) {
        return widget.getText();
    }

    static Image image(DartButton widget) {
        return widget.getImage();
    }

    /** Only while the label shows its image rather than its text. */
    static Image image(DartLabel widget) {
        return widget._displayedImage();
    }

    static Image image(DartCLabel widget) {
        return widget.getImage();
    }

    static Image image(DartTableItem widget) {
        return widget.getImage();
    }

    static Image image(DartToolItem widget) {
        return widget.getImage();
    }

    static Image image(DartTreeItem widget) {
        return widget.getImage();
    }
}
