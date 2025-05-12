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

import java.util.*;
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.internal.cocoa.*;

/**
 * Instances of this class represent a selectable user interface object
 * that represents a button in a tool bar.
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>PUSH, CHECK, RADIO, SEPARATOR, DROP_DOWN</dd>
 * <dt><b>Events:</b></dt>
 * <dd>Selection</dd>
 * </dl>
 * <p>
 * Note: Only one of the styles CHECK, PUSH, RADIO, SEPARATOR and DROP_DOWN
 * may be specified.
 * </p><p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 *
 * @see <a href="http://www.eclipse.org/swt/snippets/#toolbar">ToolBar, ToolItem snippets</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class ToolItem extends Item {

    /**
     * Constructs a new instance of this class given its parent
     * (which must be a <code>ToolBar</code>) and a style value
     * describing its behavior and appearance. The item is added
     * to the end of the items maintained by its parent.
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
     * @see SWT#PUSH
     * @see SWT#CHECK
     * @see SWT#RADIO
     * @see SWT#SEPARATOR
     * @see SWT#DROP_DOWN
     * @see Widget#checkSubclass
     * @see Widget#getStyle
     */
    public ToolItem(ToolBar parent, int style) {
        this(new nat.org.eclipse.swt.widgets.ToolItem((nat.org.eclipse.swt.widgets.ToolBar) parent.getDelegate(), style));
    }

    /**
     * Constructs a new instance of this class given its parent
     * (which must be a <code>ToolBar</code>), a style value
     * describing its behavior and appearance, and the index
     * at which to place it in the items maintained by its parent.
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
     * @param index the zero-relative index to store the receiver in its parent
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
     *    <li>ERROR_INVALID_RANGE - if the index is not between 0 and the number of elements in the parent (inclusive)</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
     *    <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
     * </ul>
     *
     * @see SWT#PUSH
     * @see SWT#CHECK
     * @see SWT#RADIO
     * @see SWT#SEPARATOR
     * @see SWT#DROP_DOWN
     * @see Widget#checkSubclass
     * @see Widget#getStyle
     */
    public ToolItem(ToolBar parent, int style, int index) {
        this(new nat.org.eclipse.swt.widgets.ToolItem((nat.org.eclipse.swt.widgets.ToolBar) parent.getDelegate(), style, index));
    }

    /**
     * Adds the listener to the collection of listeners who will
     * be notified when the control is selected by the user, by sending
     * it one of the messages defined in the <code>SelectionListener</code>
     * interface.
     * <p>
     * When <code>widgetSelected</code> is called when the mouse is over the arrow portion of a drop-down tool,
     * the event object detail field contains the value <code>SWT.ARROW</code>.
     * <code>widgetDefaultSelected</code> is not called.
     * </p>
     * <p>
     * When the <code>SWT.RADIO</code> style bit is set, the <code>widgetSelected</code> method is
     * also called when the receiver loses selection because another item in the same radio group
     * was selected by the user. During <code>widgetSelected</code> the application can use
     * <code>getSelection()</code> to determine the current selected state of the receiver.
     * </p>
     *
     * @param listener the listener which should be notified when the control is selected by the user,
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see SelectionListener
     * @see #removeSelectionListener
     * @see SelectionEvent
     */
    public void addSelectionListener(SelectionListener listener) {
        getDelegate().addSelectionListener(listener);
    }

    /**
     * Returns the receiver's background color.
     * <p>
     * Note: This operation is a hint and may be overridden by the platform.
     * For example, on some versions of Windows the background of a TabFolder,
     * is a gradient rather than a solid color.
     * </p>
     * @return the background color
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.120
     */
    public Color getBackground() {
        return getDelegate().getBackground();
    }

    /**
     * Returns a rectangle describing the receiver's size and location
     * relative to its parent.
     *
     * @return the receiver's bounding rectangle
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public Rectangle getBounds() {
        return getDelegate().getBounds();
    }

    /**
     * Returns the control that is used to fill the bounds of
     * the item when the item is a <code>SEPARATOR</code>.
     *
     * @return the control
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public Control getControl() {
        return getDelegate().getControl().getApi();
    }

    /**
     * Returns the receiver's disabled image if it has one, or null
     * if it does not.
     * <p>
     * The disabled image is displayed when the receiver is disabled.
     * </p>
     *
     * @return the receiver's disabled image
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public Image getDisabledImage() {
        return getDelegate().getDisabledImage();
    }

    /**
     * Returns <code>true</code> if the receiver is enabled, and
     * <code>false</code> otherwise. A disabled control is typically
     * not selectable from the user interface and draws with an
     * inactive or "grayed" look.
     *
     * @return the receiver's enabled state
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see #isEnabled
     */
    public boolean getEnabled() {
        return getDelegate().getEnabled();
    }

    /**
     * Returns the foreground color that the receiver will use to draw.
     *
     * @return the receiver's foreground color
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.120
     */
    public Color getForeground() {
        return getDelegate().getForeground();
    }

    /**
     * Returns the receiver's hot image if it has one, or null
     * if it does not.
     * <p>
     * The hot image is displayed when the mouse enters the receiver.
     * </p>
     *
     * @return the receiver's hot image
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public Image getHotImage() {
        return getDelegate().getHotImage();
    }

    /**
     * Returns the receiver's enabled image if it has one, or null
     * if it does not.
     *
     * @return the receiver's enabled image
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    @Override
    public Image getImage() {
        return getDelegate().getImage();
    }

    /**
     * Returns the receiver's parent, which must be a <code>ToolBar</code>.
     *
     * @return the receiver's parent
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public ToolBar getParent() {
        return getDelegate().getParent().getApi();
    }

    /**
     * Returns <code>true</code> if the receiver is selected,
     * and false otherwise.
     * <p>
     * When the receiver is of type <code>CHECK</code> or <code>RADIO</code>,
     * it is selected when it is checked (which some platforms draw as a
     * pushed in button). If the receiver is of any other type, this method
     * returns false.
     * </p>
     *
     * @return the selection state
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public boolean getSelection() {
        return getDelegate().getSelection();
    }

    /**
     * Returns the receiver's tool tip text, or null if it has not been set.
     *
     * @return the receiver's tool tip text
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public String getToolTipText() {
        return getDelegate().getToolTipText();
    }

    /**
     * Gets the width of the receiver.
     *
     * @return the width
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public int getWidth() {
        return getDelegate().getWidth();
    }

    /**
     * Returns <code>true</code> if the receiver is enabled and all
     * of the receiver's ancestors are enabled, and <code>false</code>
     * otherwise. A disabled control is typically not selectable from the
     * user interface and draws with an inactive or "grayed" look.
     *
     * @return the receiver's enabled state
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see #getEnabled
     */
    public boolean isEnabled() {
        return getDelegate().isEnabled();
    }

    /**
     * Removes the listener from the collection of listeners who will
     * be notified when the control is selected by the user.
     *
     * @param listener the listener which should no longer be notified
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see SelectionListener
     * @see #addSelectionListener
     */
    public void removeSelectionListener(SelectionListener listener) {
        getDelegate().removeSelectionListener(listener);
    }

    /**
     * Sets the receiver's background color to the color specified
     * by the argument, or to the default system color for the control
     * if the argument is null.
     * <p>
     * Note: This operation is a hint and may be overridden by the platform.
     * </p>
     * @param color the new color (or null)
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the argument has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.120
     */
    public void setBackground(Color color) {
        getDelegate().setBackground(color);
    }

    /**
     * Sets the control that is used to fill the bounds of
     * the item when the item is a <code>SEPARATOR</code>.
     *
     * @param control the new control
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the control has been disposed</li>
     *    <li>ERROR_INVALID_PARENT - if the control is not in the same widget tree</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setControl(Control control) {
        getDelegate().setControl(control.getDelegate());
    }

    /**
     * Enables the receiver if the argument is <code>true</code>,
     * and disables it otherwise.
     * <p>
     * A disabled control is typically
     * not selectable from the user interface and draws with an
     * inactive or "grayed" look.
     * </p>
     *
     * @param enabled the new enabled state
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setEnabled(boolean enabled) {
        getDelegate().setEnabled(enabled);
    }

    /**
     * Sets the receiver's disabled image to the argument, which may be
     * null indicating that no disabled image should be displayed.
     * <p>
     * The disabled image is displayed when the receiver is disabled.
     * </p>
     *
     * @param image the disabled image to display on the receiver (may be null)
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the image has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setDisabledImage(Image image) {
        getDelegate().setDisabledImage(image);
    }

    /**
     * Sets the receiver's foreground color to the color specified
     * by the argument, or to the default system color for the control
     * if the argument is null.
     * <p>
     * Note: This operation is a hint and may be overridden by the platform.
     * </p>
     * @param color the new color (or null)
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the argument has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.120
     */
    public void setForeground(Color color) {
        getDelegate().setForeground(color);
    }

    /**
     * Sets the receiver's hot image to the argument, which may be
     * null indicating that no hot image should be displayed.
     * <p>
     * The hot image is displayed when the mouse enters the receiver.
     * </p>
     *
     * @param image the hot image to display on the receiver (may be null)
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the image has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setHotImage(Image image) {
        getDelegate().setHotImage(image);
    }

    @Override
    public void setImage(Image image) {
        getDelegate().setImage(image);
    }

    /**
     * Sets the selection state of the receiver.
     * <p>
     * When the receiver is of type <code>CHECK</code> or <code>RADIO</code>,
     * it is selected when it is checked (which some platforms draw as a
     * pushed in button).
     * </p>
     *
     * @param selected the new selection state
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setSelection(boolean selected) {
        getDelegate().setSelection(selected);
    }

    /**
     * Sets the receiver's text. The string may include
     * the mnemonic character.
     * <p>
     * Mnemonics are indicated by an '&amp;' that causes the next
     * character to be the mnemonic.  When the user presses a
     * key sequence that matches the mnemonic, a selection
     * event occurs. On most platforms, the mnemonic appears
     * underlined but may be emphasised in a platform specific
     * manner.  The mnemonic indicator character '&amp;' can be
     * escaped by doubling it in the string, causing a single
     * '&amp;' to be displayed.
     * </p><p>
     * Note: If control characters like '\n', '\t' etc. are used
     * in the string, then the behavior is platform dependent.
     * </p>
     *
     * @param string the new text
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the text is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    @Override
    public void setText(String string) {
        getDelegate().setText(string);
    }

    /**
     * Sets the receiver's tool tip text to the argument, which
     * may be null indicating that the default tool tip for the
     * control will be shown. For a control that has a default
     * tool tip, such as the Tree control on Windows, setting
     * the tool tip text to an empty string replaces the default,
     * causing no tool tip text to be shown.
     * <p>
     * The mnemonic indicator (character '&amp;') is not displayed in a tool tip.
     * To display a single '&amp;' in the tool tip, the character '&amp;' can be
     * escaped by doubling it in the string.
     * </p>
     * <p>
     * NOTE: This operation is a hint and behavior is platform specific, on Windows
     * for CJK-style mnemonics of the form " (&amp;C)" at the end of the tooltip text
     * are not shown in tooltip.
     * </p>
     *
     * @param string the new tool tip text (or null)
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setToolTipText(String string) {
        getDelegate().setToolTipText(string);
    }

    /**
     * Sets the width of the receiver, for <code>SEPARATOR</code> ToolItems.
     *
     * @param width the new width. If the new value is <code>SWT.DEFAULT</code>,
     * the width is a fixed-width area whose amount is determined by the platform.
     * If the new value is 0 a vertical or horizontal line will be drawn, depending
     * on the setting of the corresponding style bit (<code>SWT.VERTICAL</code> or
     * <code>SWT.HORIZONTAL</code>). If the new value is <code>SWT.SEPARATOR_FILL</code>
     * a variable-width space is inserted that acts as a spring between the two adjoining
     * items which will push them out to the extent of the containing ToolBar.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setWidth(int width) {
        getDelegate().setWidth(width);
    }

    protected ToolItem(IToolItem delegate) {
        super(delegate);
    }

    public IToolItem getDelegate() {
        return (IToolItem) super.getDelegate();
    }
}
