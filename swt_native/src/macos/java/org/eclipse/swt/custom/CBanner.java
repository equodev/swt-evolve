/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2021 IBM Corporation and others.
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
package org.eclipse.swt.custom;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

/**
 * Instances of this class implement a Composite that lays out its
 * children and allows programmatic control of the layout. It draws
 * a separator between the left and right children which can be dragged
 * to resize the right control.
 * CBanner is used in the workbench to layout the toolbar area and
 * perspective switching toolbar.
 * <p>
 * Note that although this class is a subclass of <code>Composite</code>,
 * it does not make sense to set a layout on it.
 * </p>
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>NONE</dd>
 * <dt><b>Events:</b></dt>
 * <dd>(None)</dd>
 * </dl>
 * <p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 *
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 *
 * @since 3.0
 * @noextend This class is not intended to be subclassed by clients.
 */
public class CBanner extends Composite {

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
     * @param parent a widget which will be the parent of the new instance (cannot be null)
     * @param style the style of widget to construct
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
     * </ul>
     */
    public CBanner(Composite parent, int style) {
        this(new nat.org.eclipse.swt.custom.CBanner((nat.org.eclipse.swt.widgets.Composite) (parent != null ? parent.getDelegate() : null), style));
    }

    /*
* This class was not intended to be subclassed but this restriction
* cannot be enforced without breaking backward compatibility.
*/
    //protected void checkSubclass () {
    //	String name = getClass ().getName ();
    //	int index = name.lastIndexOf ('.');
    //	if (!name.substring (0, index + 1).equals ("org.eclipse.swt.custom.")) {
    //		SWT.error (SWT.ERROR_INVALID_SUBCLASS);
    //	}
    //}
    /**
     * Returns the Control that appears on the bottom side of the banner.
     *
     * @return the control that appears on the bottom side of the banner or null
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.0
     */
    public Control getBottom() {
        IControl ret = getDelegate().getBottom();
        return ret != null ? ret.getApi() : null;
    }

    public Rectangle getClientArea() {
        IRectangle ret = getDelegate().getClientArea();
        return ret != null ? ret.getApi() : null;
    }

    /**
     * Returns the Control that appears on the left side of the banner.
     *
     * @return the control that appears on the left side of the banner or null
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.0
     */
    public Control getLeft() {
        IControl ret = getDelegate().getLeft();
        return ret != null ? ret.getApi() : null;
    }

    /**
     * Returns the Control that appears on the right side of the banner.
     *
     * @return the control that appears on the right side of the banner or null
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.0
     */
    public Control getRight() {
        IControl ret = getDelegate().getRight();
        return ret != null ? ret.getApi() : null;
    }

    /**
     * Returns the minimum size of the control that appears on the right of the banner.
     *
     * @return the minimum size of the control that appears on the right of the banner
     *
     * @since 3.1
     */
    public Point getRightMinimumSize() {
        IPoint ret = getDelegate().getRightMinimumSize();
        return ret != null ? ret.getApi() : null;
    }

    /**
     * Returns the width of the control that appears on the right of the banner.
     *
     * @return the width of the control that appears on the right of the banner
     *
     * @since 3.0
     */
    public int getRightWidth() {
        return getDelegate().getRightWidth();
    }

    /**
     * Returns <code>true</code> if the CBanner is rendered
     * with a simple, traditional shape.
     *
     * @return <code>true</code> if the CBanner is rendered with a simple shape
     *
     * @since 3.0
     */
    public boolean getSimple() {
        return getDelegate().getSimple();
    }

    /**
     * Set the control that appears on the bottom side of the banner.
     * The bottom control is optional.  Setting the bottom control to null will remove it from
     * the banner - however, the creator of the control must dispose of the control.
     *
     * @param control the control to be displayed on the bottom or null
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the bottom control was not created as a child of the receiver</li>
     * </ul>
     *
     * @since 3.0
     */
    public void setBottom(Control control) {
        getDelegate().setBottom((control != null ? control.getDelegate() : null));
    }

    /**
     * Sets the layout which is associated with the receiver to be
     * the argument which may be null.
     * <p>
     * Note: No Layout can be set on this Control because it already
     * manages the size and position of its children.
     * </p>
     *
     * @param layout the receiver's new layout or null
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setLayout(Layout layout) {
        getDelegate().setLayout((layout != null ? layout.getDelegate() : null));
    }

    /**
     * Set the control that appears on the left side of the banner.
     * The left control is optional.  Setting the left control to null will remove it from
     * the banner - however, the creator of the control must dispose of the control.
     *
     * @param control the control to be displayed on the left or null
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the left control was not created as a child of the receiver</li>
     * </ul>
     *
     * @since 3.0
     */
    public void setLeft(Control control) {
        getDelegate().setLeft((control != null ? control.getDelegate() : null));
    }

    /**
     * Set the control that appears on the right side of the banner.
     * The right control is optional.  Setting the right control to null will remove it from
     * the banner - however, the creator of the control must dispose of the control.
     *
     * @param control the control to be displayed on the right or null
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the right control was not created as a child of the receiver</li>
     * </ul>
     *
     * @since 3.0
     */
    public void setRight(Control control) {
        getDelegate().setRight((control != null ? control.getDelegate() : null));
    }

    /**
     * Set the minimum height of the control that appears on the right side of the banner.
     *
     * @param size the minimum size of the control on the right
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the size is null or the values of size are less than SWT.DEFAULT</li>
     * </ul>
     *
     * @since 3.1
     */
    public void setRightMinimumSize(Point size) {
        getDelegate().setRightMinimumSize((size != null ? size.getDelegate() : null));
    }

    /**
     * Set the width of the control that appears on the right side of the banner.
     *
     * @param width the width of the control on the right
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *    <li>ERROR_INVALID_ARGUMENT - if width is less than SWT.DEFAULT</li>
     * </ul>
     *
     * @since 3.0
     */
    public void setRightWidth(int width) {
        getDelegate().setRightWidth(width);
    }

    /**
     * Sets the shape that the CBanner will use to render itself.
     *
     * @param simple <code>true</code> if the CBanner should render itself in a simple, traditional style
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.0
     */
    public void setSimple(boolean simple) {
        getDelegate().setSimple(simple);
    }

    protected CBanner(ICBanner delegate) {
        super(delegate);
    }

    public static CBanner createApi(ICBanner delegate) {
        return new CBanner(delegate);
    }

    public ICBanner getDelegate() {
        return (ICBanner) super.getDelegate();
    }
}
