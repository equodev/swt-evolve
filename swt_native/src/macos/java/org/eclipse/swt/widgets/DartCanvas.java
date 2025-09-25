/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2019 IBM Corporation and others.
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
import org.eclipse.swt.accessibility.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.internal.graphics.*;
import dev.equo.swt.*;

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
public class DartCanvas extends DartComposite implements ICanvas {

    Caret caret;

    IME ime;

    DartCanvas(Canvas api) {
        super(api);
        /* Do nothing */
    }

    @Override
    public void sendFocusEvent(int type) {
        if (caret != null) {
            if (type == SWT.FocusIn) {
                ((DartCaret) caret.getImpl()).setFocus();
            } else {
                ((DartCaret) caret.getImpl()).killFocus();
            }
        }
        super.sendFocusEvent(type);
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
    public DartCanvas(Composite parent, int style, Canvas api) {
        super(parent, style, api);
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
        drawBackground(gc, x, y, width, height, 0, 0);
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
        checkWidget();
        return caret;
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
        checkWidget();
        return ime;
    }

    @Override
    boolean imeInComposition() {
        return ime != null && ((SwtIME) ime.getImpl()).isInlineEnabled() && ((SwtIME) ime.getImpl()).startOffset != -1;
    }

    @Override
    void releaseChildren(boolean destroy) {
        if (caret != null) {
            caret.getImpl().release(false);
            caret = null;
        }
        if (ime != null) {
            ime.getImpl().release(false);
            ime = null;
        }
        super.releaseChildren(destroy);
    }

    @Override
    void reskinChildren(int flags) {
        if (caret != null)
            caret.reskin(flags);
        if (ime != null)
            ime.reskin(flags);
        super.reskinChildren(flags);
    }

    @Override
    void releaseWidget() {
        super.releaseWidget();
    }

    @Override
    public void resetVisibleRegion() {
        super.resetVisibleRegion();
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
        checkWidget();
        if (width <= 0 || height <= 0)
            return;
        int deltaX = destX - x, deltaY = destY - y;
        if (deltaX == 0 && deltaY == 0)
            return;
        if (!isDrawing())
            return;
        boolean isFocus = caret != null && ((DartCaret) caret.getImpl()).isFocusCaret();
        if (isFocus)
            ((DartCaret) caret.getImpl()).killFocus();
        Rectangle clientRect = getClientArea();
        Rectangle sourceRect = new Rectangle(x, y, width, height);
        Control control = findBackgroundControl();
        boolean redraw = control != null && control.getImpl()._backgroundImage() != null;
        if (!redraw)
            redraw = hasRegion();
        if (!redraw)
            redraw = isObscured();
        if (!redraw && sourceRect.intersects(clientRect)) {
            ((SwtShell) getShell().getImpl()).setScrolling();
            redraw = !update(all);
        }
        if (redraw) {
        } else {
            boolean disjoint = (destX + width < x) || (x + width < destX) || (destY + height < y) || (y + height < destY);
            if (disjoint) {
            } else {
                if (deltaX != 0) {
                    int newX = destX - deltaX;
                    if (deltaX < 0)
                        newX = destX + width;
                }
                if (deltaY != 0) {
                    int newY = destY - deltaY;
                    if (deltaY < 0)
                        newY = destY + height;
                }
            }
        }
        if (all) {
            Control[] children = _getChildren();
            for (int i = 0; i < children.length; i++) {
                Control child = children[i];
                Rectangle rect = child.getBounds();
                if (Math.min(x + width, rect.x + rect.width) >= Math.max(x, rect.x) && Math.min(y + height, rect.y + rect.height) >= Math.max(y, rect.y)) {
                    child.setLocation(rect.x + deltaX, rect.y + deltaY);
                }
            }
        }
        if (isFocus)
            ((DartCaret) caret.getImpl()).setFocus();
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
        dirty();
        checkWidget();
        Caret newCaret = caret;
        Caret oldCaret = this.caret;
        this.caret = newCaret;
        if (hasFocus()) {
            if (oldCaret != null)
                ((DartCaret) oldCaret.getImpl()).killFocus();
            if (newCaret != null) {
                if (newCaret.isDisposed())
                    error(SWT.ERROR_INVALID_ARGUMENT);
                ((DartCaret) newCaret.getImpl()).setFocus();
            }
        }
    }

    @Override
    public void setFont(Font font) {
        dirty();
        checkWidget();
        if (caret != null)
            caret.setFont(font);
        super.setFont(font);
    }

    @Override
    void setOpenGLContext(Object value) {
        Shell shell = getShell();
        {
            ((SwtShell) shell.getImpl()).glContextCount--;
        }
        ((SwtShell) shell.getImpl()).updateOpaque();
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
        dirty();
        checkWidget();
        if (ime != null && ime.isDisposed())
            error(SWT.ERROR_INVALID_ARGUMENT);
        this.ime = ime;
    }

    public Caret _caret() {
        return caret;
    }

    public IME _ime() {
        return ime;
    }

    protected void _hookEvents() {
        super._hookEvents();
    }

    public Canvas getApi() {
        if (api == null)
            api = Canvas.createApi(this);
        return (Canvas) api;
    }

    public VCanvas getValue() {
        if (value == null)
            value = new VCanvas(this);
        return (VCanvas) value;
    }
}
