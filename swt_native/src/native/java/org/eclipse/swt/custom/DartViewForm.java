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
import java.util.Objects;
import dev.equo.swt.*;

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
 * @see <a href="https://eclipse.dev/eclipse/swt/">Sample code and further information</a>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class DartViewForm extends DartComposite implements IViewForm {

    // SWT widgets
    Control topLeft;

    Control topCenter;

    Control topRight;

    Control content;

    // Configuration and state info
    boolean separateTopCenter = false;

    boolean showBorder = false;

    int separator = -1;

    int borderTop = 0;

    int borderBottom = 0;

    int borderLeft = 0;

    int borderRight = 0;

    int highlight = 0;

    Point oldSize;

    Color selectionBackground;

    Listener listener;

    static final int OFFSCREEN = -200;

    static final int BORDER1_COLOR = SWT.COLOR_WIDGET_NORMAL_SHADOW;

    static final int SELECTION_BACKGROUND = SWT.COLOR_LIST_BACKGROUND;

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
    public DartViewForm(Composite parent, int style, ViewForm api) {
        super(parent, checkStyle(style), api);
        super.setLayout(new ViewFormLayout());
        setBorderVisible((style & SWT.BORDER) != 0);
        listener = e -> {
            switch(e.type) {
                case SWT.Dispose:
                    onDispose(e);
                    break;
                case SWT.Paint:
                    onPaint(e.gc);
                    break;
                case SWT.Resize:
                    onResize();
                    break;
            }
        };
        int[] events = new int[] { SWT.Dispose, SWT.Paint, SWT.Resize };
        for (int event : events) {
            addListener(event, listener);
        }
    }

    static int checkStyle(int style) {
        int mask = SWT.FLAT | SWT.LEFT_TO_RIGHT | SWT.RIGHT_TO_LEFT;
        return style & mask | SWT.NO_REDRAW_RESIZE;
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
        return Sizes.computeTrim(this, x, y, width, height);
    }

    @Override
    public Rectangle getClientArea() {
        return Sizes.getClientArea(this);
    }

    /**
     * Returns the content area.
     *
     * @return the control in the content area of the pane or null
     */
    public Control getContent() {
        /*
	 * This call is intentionally commented out, to allow this getter method to be
	 * called from a thread which is different from one that created the parent.
	 */
        //checkWidget();
        return content;
    }

    /**
     * Returns Control that appears in the top center of the pane.
     * Typically this is a toolbar.
     *
     * @return the control in the top center of the pane or null
     */
    public Control getTopCenter() {
        /*
	 * This call is intentionally commented out, to allow this getter method to be
	 * called from a thread which is different from one that created the widget.
	 */
        //checkWidget();
        return topCenter;
    }

    /**
     * Returns the Control that appears in the top left corner of the pane.
     * Typically this is a label such as CLabel.
     *
     * @return the control in the top left corner of the pane or null
     */
    public Control getTopLeft() {
        /*
	 * This call is intentionally commented out, to allow this getter method to be
	 * called from a thread which is different from one that created the widget.
	 */
        //checkWidget();
        return topLeft;
    }

    /**
     * Returns the control in the top right corner of the pane.
     * Typically this is a Close button or a composite with a Menu and Close button.
     *
     * @return the control in the top right corner of the pane or null
     */
    public Control getTopRight() {
        /*
	 * This call is intentionally commented out, to allow this getter method to be
	 * called from a thread which is different from one that created the widget.
	 */
        //checkWidget();
        return topRight;
    }

    void onDispose(Event event) {
        removeListener(SWT.Dispose, listener);
        notifyListeners(SWT.Dispose, event);
        event.type = SWT.None;
        topLeft = null;
        topCenter = null;
        topRight = null;
        content = null;
        oldSize = null;
        selectionBackground = null;
    }

    void onPaint(GC gc) {
        if (showBorder) {
            if (highlight > 0) {
            }
        }
        if (separator > -1) {
        }
    }

    void onResize() {
        Point size = getSize();
        if (oldSize == null || oldSize.x == 0 || oldSize.y == 0) {
            redraw();
        } else {
            int width = 0;
            if (oldSize.x < size.x) {
                width = size.x - oldSize.x + borderRight + highlight;
            } else if (oldSize.x > size.x) {
                width = borderRight + highlight;
            }
            redraw(size.x - width, 0, width, size.y, false);
            int height = 0;
            if (oldSize.y < size.y) {
                height = size.y - oldSize.y + borderBottom + highlight;
            }
            if (oldSize.y > size.y) {
                height = borderBottom + highlight;
            }
            redraw(0, size.y - height, size.x, height, false);
        }
        oldSize = size;
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
        checkWidget();
        if (!java.util.Objects.equals(this.content, content)) {
            dirty();
        }
        if (content != null && content.getParent() != this.getApi()) {
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        }
        if (this.content != null && !this.content.isDisposed()) {
            this.content.setBounds(OFFSCREEN, OFFSCREEN, 0, 0);
        }
        this.content = content;
        layout(false);
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
        checkWidget();
        return;
    }

    void setSelectionBackground(Color color) {
        checkWidget();
        if (selectionBackground == color)
            return;
        if (color == null)
            color = getDisplay().getSystemColor(SELECTION_BACKGROUND);
        selectionBackground = color;
        redraw();
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
        checkWidget();
        if (!java.util.Objects.equals(this.topCenter, topCenter)) {
            dirty();
        }
        if (topCenter != null && topCenter.getParent() != this.getApi()) {
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        }
        if (this.topCenter != null && !this.topCenter.isDisposed()) {
            Point size = this.topCenter.getSize();
            this.topCenter.setLocation(OFFSCREEN - size.x, OFFSCREEN - size.y);
        }
        this.topCenter = topCenter;
        layout(false);
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
        checkWidget();
        if (!java.util.Objects.equals(this.topLeft, c)) {
            dirty();
        }
        if (c != null && c.getParent() != this.getApi()) {
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        }
        if (this.topLeft != null && !this.topLeft.isDisposed()) {
            Point size = this.topLeft.getSize();
            this.topLeft.setLocation(OFFSCREEN - size.x, OFFSCREEN - size.y);
        }
        this.topLeft = c;
        layout(false);
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
        checkWidget();
        if (!java.util.Objects.equals(this.topRight, c)) {
            dirty();
        }
        if (c != null && c.getParent() != this.getApi()) {
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        }
        if (this.topRight != null && !this.topRight.isDisposed()) {
            Point size = this.topRight.getSize();
            this.topRight.setLocation(OFFSCREEN - size.x, OFFSCREEN - size.y);
        }
        this.topRight = c;
        layout(false);
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
        checkWidget();
        if (!java.util.Objects.equals(this.showBorder, show)) {
            dirty();
        }
        if (showBorder == show)
            return;
        showBorder = show;
        if (showBorder) {
            borderLeft = borderTop = borderRight = borderBottom = 1;
            if ((getStyle() & SWT.FLAT) == 0)
                highlight = 2;
        } else {
            borderBottom = borderTop = borderLeft = borderRight = 0;
            highlight = 0;
        }
        layout(false);
        redraw();
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
        checkWidget();
        if (!java.util.Objects.equals(this.separateTopCenter, show)) {
            dirty();
        }
        separateTopCenter = show;
        layout(false);
    }

    public Control _topLeft() {
        return topLeft;
    }

    public Control _topCenter() {
        return topCenter;
    }

    public Control _topRight() {
        return topRight;
    }

    public Control _content() {
        return content;
    }

    public boolean _separateTopCenter() {
        return separateTopCenter;
    }

    public boolean _showBorder() {
        return showBorder;
    }

    public int _separator() {
        return separator;
    }

    public int _borderTop() {
        return borderTop;
    }

    public int _borderBottom() {
        return borderBottom;
    }

    public int _borderLeft() {
        return borderLeft;
    }

    public int _borderRight() {
        return borderRight;
    }

    public int _highlight() {
        return highlight;
    }

    public Point _oldSize() {
        return oldSize;
    }

    public Color _selectionBackground() {
        return selectionBackground;
    }

    public Listener _listener() {
        return listener;
    }

    protected void _hookEvents() {
        super._hookEvents();
    }

    public ViewForm getApi() {
        if (api == null)
            api = ViewForm.createApi(this);
        return (ViewForm) api;
    }

    public VViewForm getValue() {
        if (value == null)
            value = new VViewForm(this);
        return (VViewForm) value;
    }
}
