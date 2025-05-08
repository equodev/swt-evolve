package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;

public interface IFontDialog extends IDialog {

    /**
     * Returns <code>true</code> if the dialog's effects selection controls
     * are visible, and <code>false</code> otherwise.
     * <p>
     * If the platform's font dialog does not have any effects selection controls,
     * then this method always returns false.
     * </p>
     *
     * @return <code>true</code> if the dialog's effects selection controls
     * are visible and <code>false</code> otherwise
     *
     * @since 3.8
     */
    boolean getEffectsVisible();

    /**
     * Returns a FontData object describing the font that was
     * selected in the dialog, or null if none is available.
     *
     * @return the FontData for the selected font, or null
     * @deprecated use #getFontList ()
     */
    FontData getFontData();

    /**
     * Returns a FontData set describing the font that was
     * selected in the dialog, or null if none is available.
     *
     * @return the FontData for the selected font, or null
     * @since 2.1.1
     */
    FontData[] getFontList();

    /**
     * Returns an RGB describing the color that was selected
     * in the dialog, or null if none is available.
     *
     * @return the RGB value for the selected color, or null
     *
     * @see PaletteData#getRGBs
     *
     * @since 2.1
     */
    RGB getRGB();

    /**
     * Makes the dialog visible and brings it to the front
     * of the display.
     *
     * @return a FontData object describing the font that was selected,
     *         or null if the dialog was cancelled or an error occurred
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the dialog has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the dialog</li>
     * </ul>
     */
    FontData open();

    /**
     * Sets the effects selection controls in the dialog visible if the
     * argument is <code>true</code>, and invisible otherwise.
     * <p>
     * By default the effects selection controls are displayed if the
     * platform font dialog supports effects selection.
     * </p>
     *
     * @param visible whether or not the dialog will show the effects selection controls
     *
     * @since 3.8
     */
    void setEffectsVisible(boolean visible);

    /**
     * Sets a FontData object describing the font to be
     * selected by default in the dialog, or null to let
     * the platform choose one.
     *
     * @param fontData the FontData to use initially, or null
     * @deprecated use #setFontList (FontData [])
     */
    void setFontData(FontData fontData);

    /**
     * Sets the set of FontData objects describing the font to
     * be selected by default in the dialog, or null to let
     * the platform choose one.
     *
     * @param fontData the set of FontData objects to use initially, or null
     *        to let the platform select a default when open() is called
     *
     * @see Font#getFontData
     *
     * @since 2.1.1
     */
    void setFontList(FontData[] fontData);

    /**
     * Sets the RGB describing the color to be selected by default
     * in the dialog, or null to let the platform choose one.
     *
     * @param rgb the RGB value to use initially, or null to let
     *        the platform select a default when open() is called
     *
     * @see PaletteData#getRGBs
     *
     * @since 2.1
     */
    void setRGB(RGB rgb);

    FontDialog getApi();

    void setApi(FontDialog api);
}
