/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2012 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *      IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.internal.cocoa.*;

/**
 * Instances of this class allow the user to select a color
 * from a predefined set of available colors.
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>(none)</dd>
 * <dt><b>Events:</b></dt>
 * <dd>(none)</dd>
 * </dl>
 * <p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 *
 * @see <a href="http://www.eclipse.org/swt/examples.php">SWT Example: ControlExample, Dialog tab</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class ColorDialog extends Dialog {

    /**
     * Constructs a new instance of this class given only its parent.
     *
     * @param parent a composite control which will be the parent of the new instance
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
     *    <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
     * </ul>
     *
     * @see SWT
     * @see Widget#checkSubclass
     * @see Widget#getStyle
     */
    public ColorDialog(Shell parent) {
        this(new nat.org.eclipse.swt.widgets.ColorDialog((nat.org.eclipse.swt.widgets.Shell) parent.getDelegate()));
    }

    /**
     * Constructs a new instance of this class given its parent
     * and a style value describing its behavior and appearance.
     * <p>
     * The style value is either one of the style constants defined in
     * class <code>SWT</code> which is applicable to instances of this
     * class, or must be built by <em>bitwise OR</em>'ing together
     * (that is, using the <code>int</code> "|" operator) two or more
     * of those <code>SWT</code> style constants. The class description
     * lists the style constants that are applicable to the class.
     * Style bits are also inherited from superclasses.
     * </p>
     *
     * @param parent a composite control which will be the parent of the new instance (cannot be null)
     * @param style the style of control to construct
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
     *    <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
     * </ul>
     *
     * @see SWT
     * @see Widget#checkSubclass
     * @see Widget#getStyle
     */
    public ColorDialog(Shell parent, int style) {
        this(new nat.org.eclipse.swt.widgets.ColorDialog((nat.org.eclipse.swt.widgets.Shell) parent.getDelegate(), style));
    }

    /**
     * Returns the currently selected color in the receiver.
     *
     * @return the RGB value for the selected color, may be null
     *
     * @see PaletteData#getRGBs
     */
    public RGB getRGB() {
        return getDelegate().getRGB();
    }

    /**
     * Returns an array of <code>RGB</code>s which are the list of
     * custom colors selected by the user in the receiver, or null
     * if no custom colors were selected.
     *
     * @return the array of RGBs, which may be null
     *
     * @since 3.8
     */
    public RGB[] getRGBs() {
        return getDelegate().getRGBs();
    }

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
    public RGB open() {
        return getDelegate().open();
    }

    /**
     * Sets the receiver's selected color to be the argument.
     *
     * @param rgb the new RGB value for the selected color, may be
     *        null to let the platform select a default when
     *        open() is called
     * @see PaletteData#getRGBs
     */
    public void setRGB(RGB rgb) {
        getDelegate().setRGB(rgb);
    }

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
    public void setRGBs(RGB[] rgbs) {
        getDelegate().setRGBs(rgbs);
    }

    protected ColorDialog(IColorDialog delegate) {
        super(delegate);
    }

    protected IColorDialog getDelegate() {
        return (IColorDialog) super.getDelegate();
    }
}
