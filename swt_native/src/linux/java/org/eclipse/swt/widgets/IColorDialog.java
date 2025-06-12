package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.internal.*;
import org.eclipse.swt.internal.gtk.*;
import org.eclipse.swt.internal.gtk3.*;
import org.eclipse.swt.internal.gtk4.*;

public interface IColorDialog extends IDialog {

    /**
     * Returns the currently selected color in the receiver.
     *
     * @return the RGB value for the selected color, may be null
     *
     * @see PaletteData#getRGBs
     */
    RGB getRGB();

    /**
     * Returns an array of <code>RGB</code>s which are the list of
     * custom colors selected by the user in the receiver, or null
     * if no custom colors were selected.
     *
     * @return the array of RGBs, which may be null
     *
     * @since 3.8
     */
    RGB[] getRGBs();

    /**
     * Makes the receiver visible and brings it to the front
     * of the display.
     *
     * @return the selected color, or null if the dialog was
     *         cancelled, no color was selected, or an error
     *         occurred
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    RGB open();

    /**
     * Sets the receiver's selected color to be the argument.
     *
     * @param rgb the new RGB value for the selected color, may be
     *        null to let the platform select a default when
     *        open() is called
     * @see PaletteData#getRGBs
     */
    void setRGB(RGB rgb);

    /**
     * Sets the receiver's list of custom colors to be the given array
     * of <code>RGB</code>s, which may be null to let the platform select
     * a default when open() is called.
     *
     * @param rgbs the array of RGBs, which may be null
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if an RGB in the rgbs array is null</li>
     * </ul>
     *
     * @since 3.8
     */
    void setRGBs(RGB[] rgbs);
}
