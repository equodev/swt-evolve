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
package org.eclipse.swt.custom;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import java.util.WeakHashMap;

/**
 * Instances of this class implement a Composite that positions and sizes
 * children and allows programmatic control of layout and border parameters.
 * ViewForm is used in the workbench to lay out a view's label/menu/toolbar
 * local bar.
 * <p>
 * Note that although this class is a subclass of <code>Composite</code>,
 * it does not make sense to set a layout on it.
 * </p>
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>BORDER, FLAT</dd>
 * <dt><b>Events:</b></dt>
 * <dd>(None)</dd>
 * </dl>
 * <p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 *
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class ViewForm extends Composite {

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
     *
     * @see SWT#BORDER
     * @see SWT#FLAT
     * @see #getStyle()
     */
    public ViewForm(Composite parent, int style) {
        this(new SWTViewForm((SWTComposite) parent.delegate, style));
    }

    //protected void checkSubclass () {
    //	String name = getClass().getName ();
    //	String validName = ViewForm.class.getName();
    //	if (!validName.equals(name)) {
    //		SWT.error (SWT.ERROR_INVALID_SUBCLASS);
    //	}
    //}
    @Override
    public Rectangle computeTrim(int x, int y, int width, int height) {
        return ((IViewForm) this.delegate).computeTrim(x, y, width, height);
    }

    @Override
    public Rectangle getClientArea() {
        return ((IViewForm) this.delegate).getClientArea();
    }

    /**
     * Returns the content area.
     *
     * @return the control in the content area of the pane or null
     */
    public Control getContent() {
        return Control.getInstance(((IViewForm) this.delegate).getContent());
    }

    /**
     * Returns Control that appears in the top center of the pane.
     * Typically this is a toolbar.
     *
     * @return the control in the top center of the pane or null
     */
    public Control getTopCenter() {
        return Control.getInstance(((IViewForm) this.delegate).getTopCenter());
    }

    /**
     * Returns the Control that appears in the top left corner of the pane.
     * Typically this is a label such as CLabel.
     *
     * @return the control in the top left corner of the pane or null
     */
    public Control getTopLeft() {
        return Control.getInstance(((IViewForm) this.delegate).getTopLeft());
    }

    /**
     * Returns the control in the top right corner of the pane.
     * Typically this is a Close button or a composite with a Menu and Close button.
     *
     * @return the control in the top right corner of the pane or null
     */
    public Control getTopRight() {
        return Control.getInstance(((IViewForm) this.delegate).getTopRight());
    }

    /**
     * Sets the content.
     * Setting the content to null will remove it from
     * the pane - however, the creator of the content must dispose of the content.
     *
     * @param content the control to be displayed in the content area or null
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the control is not a child of this ViewForm</li>
     * </ul>
     */
    public void setContent(Control content) {
        ((IViewForm) this.delegate).setContent((IControl) content.delegate);
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
    @Override
    public void setLayout(Layout layout) {
        ((IViewForm) this.delegate).setLayout(layout);
    }

    /**
     * Set the control that appears in the top center of the pane.
     * Typically this is a toolbar.
     * The topCenter is optional.  Setting the topCenter to null will remove it from
     * the pane - however, the creator of the topCenter must dispose of the topCenter.
     *
     * @param topCenter the control to be displayed in the top center or null
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the control is not a child of this ViewForm</li>
     * </ul>
     */
    public void setTopCenter(Control topCenter) {
        ((IViewForm) this.delegate).setTopCenter((IControl) topCenter.delegate);
    }

    /**
     * Set the control that appears in the top left corner of the pane.
     * Typically this is a label such as CLabel.
     * The topLeft is optional.  Setting the top left control to null will remove it from
     * the pane - however, the creator of the control must dispose of the control.
     *
     * @param c the control to be displayed in the top left corner or null
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the control is not a child of this ViewForm</li>
     * </ul>
     */
    public void setTopLeft(Control c) {
        ((IViewForm) this.delegate).setTopLeft((IControl) c.delegate);
    }

    /**
     * Set the control that appears in the top right corner of the pane.
     * Typically this is a Close button or a composite with a Menu and Close button.
     * The topRight is optional.  Setting the top right control to null will remove it from
     * the pane - however, the creator of the control must dispose of the control.
     *
     * @param c the control to be displayed in the top right corner or null
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the control is not a child of this ViewForm</li>
     * </ul>
     */
    public void setTopRight(Control c) {
        ((IViewForm) this.delegate).setTopRight((IControl) c.delegate);
    }

    /**
     * Specify whether the border should be displayed or not.
     *
     * @param show true if the border should be displayed
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setBorderVisible(boolean show) {
        ((IViewForm) this.delegate).setBorderVisible(show);
    }

    /**
     * If true, the topCenter will always appear on a separate line by itself, otherwise the
     * topCenter will appear in the top row if there is room and will be moved to the second row if
     * required.
     *
     * @param show true if the topCenter will always appear on a separate line by itself
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setTopCenterSeparate(boolean show) {
        ((IViewForm) this.delegate).setTopCenterSeparate(show);
    }

    protected ViewForm(IViewForm delegate) {
        super(delegate);
        this.delegate = delegate;
        INSTANCES.put(delegate, this);
    }

    public static ViewForm getInstance(IViewForm delegate) {
        if (delegate == null) {
            return null;
        }
        ViewForm ref = (ViewForm) INSTANCES.get(delegate);
        if (ref == null) {
            ref = new ViewForm(delegate);
        }
        return ref;
    }
}
