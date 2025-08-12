/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2018 IBM Corporation and others.
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
import org.eclipse.swt.internal.*;
import org.eclipse.swt.internal.cairo.*;
import org.eclipse.swt.internal.gtk.*;
import dev.equo.swt.Config;

/**
 * Instances of this class provide a surface for drawing
 * arbitrary graphics.
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>(none)</dd>
 * <dt><b>Events:</b></dt>
 * <dd>(none)</dd>
 * </dl>
 * <p>
 * This class may be subclassed by custom control implementors
 * who are building controls that are <em>not</em> constructed
 * from aggregates of other controls. That is, they are either
 * painted using SWT graphics calls or are handled by native
 * methods.
 * </p>
 *
 * @see Composite
 * @see <a href="http://www.eclipse.org/swt/snippets/#canvas">Canvas snippets</a>
 * @see <a href="http://www.eclipse.org/swt/examples.php">SWT Example: ControlExample</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 */
public class Canvas extends Composite {

    Canvas() {
        this((ICanvas) null);
        setImpl(Config.isEquo(Canvas.class) ? new DartCanvas(this) : new SwtCanvas(this));
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
     * </ul>
     *
     * @see SWT
     * @see Widget#getStyle
     */
    public Canvas(Composite parent, int style) {
        this((ICanvas) null);
        setImpl(Config.isEquo(Canvas.class, parent) ? new DartCanvas(parent, style, this) : new SwtCanvas(parent, style, this));
    }

    /**
     * Fills the interior of the rectangle specified by the arguments,
     * with the receiver's background.
     *
     * @param gc the gc where the rectangle is to be filled
     * @param x the x coordinate of the rectangle to be filled
     * @param y the y coordinate of the rectangle to be filled
     * @param width the width of the rectangle to be filled
     * @param height the height of the rectangle to be filled
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the gc is null</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the gc has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.2
     */
    public void drawBackground(GC gc, int x, int y, int width, int height) {
        getImpl().drawBackground(gc, x, y, width, height);
    }

    /**
     * Returns the caret.
     * <p>
     * The caret for the control is automatically hidden
     * and shown when the control is painted or resized,
     * when focus is gained or lost and when an the control
     * is scrolled.  To avoid drawing on top of the caret,
     * the programmer must hide and show the caret when
     * drawing in the window any other time.
     * </p>
     *
     * @return the caret for the receiver, may be null
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public Caret getCaret() {
        return getImpl().getCaret();
    }

    /**
     * Returns the IME.
     *
     * @return the IME
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.4
     */
    public IME getIME() {
        return getImpl().getIME();
    }

    /**
     * Scrolls a rectangular area of the receiver by first copying
     * the source area to the destination and then causing the area
     * of the source which is not covered by the destination to
     * be repainted. Children that intersect the rectangle are
     * optionally moved during the operation. In addition, all outstanding
     * paint events are flushed before the source area is copied to
     * ensure that the contents of the canvas are drawn correctly.
     *
     * @param destX the x coordinate of the destination
     * @param destY the y coordinate of the destination
     * @param x the x coordinate of the source
     * @param y the y coordinate of the source
     * @param width the width of the area
     * @param height the height of the area
     * @param all <code>true</code>if children should be scrolled, and <code>false</code> otherwise
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void scroll(int destX, int destY, int x, int y, int width, int height, boolean all) {
        getImpl().scroll(destX, destY, x, y, width, height, all);
    }

    /**
     * Sets the receiver's caret.
     * <p>
     * The caret for the control is automatically hidden
     * and shown when the control is painted or resized,
     * when focus is gained or lost and when an the control
     * is scrolled.  To avoid drawing on top of the caret,
     * the programmer must hide and show the caret when
     * drawing in the window any other time.
     * </p>
     * @param caret the new caret for the receiver, may be null
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the caret has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setCaret(Caret caret) {
        getImpl().setCaret(caret);
    }

    public void setFont(Font font) {
        getImpl().setFont(font);
    }

    /**
     * Sets the receiver's IME.
     *
     * @param ime the new IME for the receiver, may be null
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the IME has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.4
     */
    public void setIME(IME ime) {
        getImpl().setIME(ime);
    }

    protected Canvas(ICanvas impl) {
        super(impl);
    }

    static Canvas createApi(ICanvas impl) {
        return new Canvas(impl);
    }

    public ICanvas getImpl() {
        return (ICanvas) super.getImpl();
    }
}
