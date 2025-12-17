/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2025 IBM Corporation and others.
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
import java.util.Objects;
import dev.equo.swt.*;

/**
 * Instances of this class represent a selectable user interface object
 * corresponding to a tab for a page in a tab folder.
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
 * @see <a href="http://www.eclipse.org/swt/snippets/#tabfolder">TabFolder, TabItem snippets</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class DartTabItem extends DartItem implements ITabItem {

    long labelHandle, imageHandle, pageHandle;

    long cssProvider;

    Control control;

    TabFolder parent;

    String toolTipText;

    /**
     * Constructs a new instance of this class given its parent
     * (which must be a <code>TabFolder</code>) and a style value
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
     * @see SWT
     * @see Widget#checkSubclass
     * @see Widget#getStyle
     */
    public DartTabItem(TabFolder parent, int style, TabItem api) {
        super(parent, style, api);
        this.parent = parent;
        createWidget(parent.getItemCount());
    }

    /**
     * Constructs a new instance of this class given its parent
     * (which must be a <code>TabFolder</code>), a style value
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
     * @see SWT
     * @see Widget#checkSubclass
     * @see Widget#getStyle
     */
    public DartTabItem(TabFolder parent, int style, int index, TabItem api) {
        super(parent, style, api);
        this.parent = parent;
        createWidget(index);
    }

    @Override
    public void checkSubclass() {
        if (!isValidSubclass())
            error(SWT.ERROR_INVALID_SUBCLASS);
    }

    @Override
    void createWidget(int index) {
        ((DartTabFolder) parent.getImpl()).createItem(this.getApi(), index);
        setOrientation(true);
        hookEvents();
        register();
        text = "";
    }

    @Override
    void deregister() {
        super.deregister();
        if (labelHandle != 0)
            ((SwtDisplay) display.getImpl()).removeWidget(labelHandle);
    }

    @Override
    void destroyWidget() {
        ((DartTabFolder) parent.getImpl()).destroyItem(this.getApi());
        releaseHandle();
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
     *
     * @since 3.4
     */
    public Rectangle getBounds() {
        checkWidget();
        return null;
    }

    /**
     * Returns the control that is used to fill the client area of
     * the tab folder when the user selects the tab item.  If no
     * control has been set, return <code>null</code>.
     *
     * @return the control
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public Control getControl() {
        checkWidget();
        return control;
    }

    /**
     * Returns the receiver's parent, which must be a <code>TabFolder</code>.
     *
     * @return the receiver's parent
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public TabFolder getParent() {
        checkWidget();
        return parent;
    }

    /**
     * Returns the receiver's tool tip text, or null if it has
     * not been set.
     *
     * @return the receiver's tool tip text
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public String getToolTipText() {
        checkWidget();
        return toolTipText;
    }

    @Override
    void hookEvents() {
        super.hookEvents();
    }

    @Override
    void register() {
        super.register();
        if (labelHandle != 0)
            ((SwtDisplay) display.getImpl()).addWidget(labelHandle, this.getApi());
    }

    @Override
    public void release(boolean destroy) {
        //Since controls are now nested under the tabItem,
        //tabItem is responsible for it's release.
        if (control != null && !control.isDisposed()) {
        }
        super.release(destroy);
    }

    @Override
    void releaseHandle() {
        super.releaseHandle();
        pageHandle = labelHandle = imageHandle = 0;
        cssProvider = 0;
        parent = null;
    }

    @Override
    void releaseParent() {
        super.releaseParent();
        int index = parent.indexOf(this.getApi());
        if (index == parent.getSelectionIndex()) {
            if (control != null)
                control.setVisible(false);
        }
    }

    /**
     * Sets the control that is used to fill the client area of
     * the tab folder when the user selects the tab item.
     *
     * @param control the new control (or null)
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
        checkWidget();
        if (!java.util.Objects.equals(this.control, control)) {
            dirty();
        }
        if (control != null) {
            if (control.isDisposed())
                error(SWT.ERROR_INVALID_ARGUMENT);
            if (((DartControl) control.getImpl()).parent != parent)
                error(SWT.ERROR_INVALID_PARENT);
        }
    }

    void setFontDescription(long font) {
        setFontDescription(labelHandle, font);
    }

    @Override
    public void setImage(Image image) {
        dirty();
        checkWidget();
        super.setImage(image);
        if (image != null) {
        } else {
        }
    }

    @Override
    void setOrientation(boolean create) {
        if ((parent.style & SWT.RIGHT_TO_LEFT) != 0 || !create) {
        }
    }

    /**
     * Sets the receiver's text.  The string may include
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
        dirty();
        checkWidget();
        if (string == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        super.setText(string);
        if (string.length() != 0) {
        } else {
        }
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
        checkWidget();
        if (!java.util.Objects.equals(this.toolTipText, string)) {
            dirty();
        }
        toolTipText = string;
        setToolTipText(getApi().handle, string);
    }

    @Override
    long dpiChanged(long object, long arg0) {
        super.dpiChanged(object, arg0);
        if (image != null) {
            setImage(image);
        }
        return 0;
    }

    public long _labelHandle() {
        return labelHandle;
    }

    public long _imageHandle() {
        return imageHandle;
    }

    public long _pageHandle() {
        return pageHandle;
    }

    public long _cssProvider() {
        return cssProvider;
    }

    public Control _control() {
        return control;
    }

    public TabFolder _parent() {
        return parent;
    }

    public String _toolTipText() {
        return toolTipText;
    }

    public FlutterBridge getBridge() {
        if (bridge != null)
            return bridge;
        Composite p = parent;
        while (p != null && !(p.getImpl() instanceof DartWidget)) p = p.getImpl()._parent();
        return p != null ? ((DartWidget) p.getImpl()).getBridge() : null;
    }

    protected void _hookEvents() {
        super._hookEvents();
    }

    public TabItem getApi() {
        if (api == null)
            api = TabItem.createApi(this);
        return (TabItem) api;
    }

    public VTabItem getValue() {
        if (value == null)
            value = new VTabItem(this);
        return (VTabItem) value;
    }
}
