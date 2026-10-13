package dev.equo.swt.size;

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
import org.eclipse.swt.widgets.DartTreeItem;

import static dev.equo.swt.Styles.hasFlags;

/**
 * The text and the image a widget draws, which is what its size is measured from. The generated
 * {@code *Sizes} classes all ask here, so what each widget draws is decided in one place.
 */
public final class Drawn {

    private Drawn() {
    }

    /** Without the mnemonic marker. */
    public static String text(DartButton widget) {
        return Mnemonics.display(widget.getText());
    }

    /** Without the mnemonic marker, and only while the label shows its text rather than its image. */
    public static String text(DartLabel widget) {
        return Mnemonics.display(widget._displayedText());
    }

    /** Without the mnemonic marker; a CLabel never given a text draws none. */
    public static String text(DartCLabel widget) {
        String text = widget.getText();
        return text == null ? "" : Mnemonics.display(text);
    }

    /** Without its markup and without the mnemonic marker. */
    public static String text(DartLink widget) {
        String text = widget.getText();
        return text == null ? null : Mnemonics.display(text.replaceAll("<[^>]+>", ""));
    }

    /** As typed, or one '*' per character when it is a password. */
    public static String text(DartText widget) {
        String text = widget.getText();
        if (text != null && hasFlags(widget.getStyle(), SWT.PASSWORD))
            return dev.equo.swt.Java8.repeat("*", text.length());
        return text;
    }

    public static String text(DartCombo widget) {
        return widget.getText();
    }

    public static String text(DartCCombo widget) {
        return widget.getText();
    }

    public static String text(DartTableItem widget) {
        return widget.getText();
    }

    public static String text(DartTreeItem widget) {
        return widget.getText();
    }

    public static Image image(DartButton widget) {
        return widget.getImage();
    }

    /** Only while the label shows its image rather than its text. */
    public static Image image(DartLabel widget) {
        return widget._displayedImage();
    }

    public static Image image(DartCLabel widget) {
        return widget.getImage();
    }

    public static Image image(DartTableItem widget) {
        return widget.getImage();
    }

    public static Image image(DartTreeItem widget) {
        return widget.getImage();
    }
}
