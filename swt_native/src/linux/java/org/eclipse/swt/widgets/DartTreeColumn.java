/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2016 IBM Corporation and others.
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
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.internal.*;
import dev.equo.swt.*;

/**
 * Instances of this class represent a column in a tree widget.
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>LEFT, RIGHT, CENTER</dd>
 * <dt><b>Events:</b></dt>
 * <dd> Move, Resize, Selection</dd>
 * </dl>
 * <p>
 * Note: Only one of the styles LEFT, RIGHT and CENTER may be specified.
 * </p><p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 *
 * @see <a href="http://www.eclipse.org/swt/snippets/#tree">Tree, TreeItem, TreeColumn snippets</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 *
 * @since 3.1
 * @noextend This class is not intended to be subclassed by clients.
 */
public class DartTreeColumn extends DartItem implements ITreeColumn {

    long headerButtonCSSProvider = 0;

    long labelHandle, imageHandle, buttonHandle;

    Tree parent;

    int modelIndex, lastTime, lastX, lastWidth;

    boolean customDraw;

    String toolTipText;

    /**
     * Constructs a new instance of this class given its parent
     * (which must be a <code>Tree</code>) and a style value
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
     * @see SWT#LEFT
     * @see SWT#RIGHT
     * @see SWT#CENTER
     * @see Widget#checkSubclass
     * @see Widget#getStyle
     */
    public DartTreeColumn(Tree parent, int style, TreeColumn api) {
        super(parent, checkStyle(style), api);
        this.parent = parent;
        createWidget(parent.getColumnCount());
    }

    /**
     * Constructs a new instance of this class given its parent
     * (which must be a <code>Tree</code>), a style value
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
     * <p>
     * Note that due to a restriction on some platforms, the first column
     * is always left aligned.
     * </p>
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
     * @see SWT#LEFT
     * @see SWT#RIGHT
     * @see SWT#CENTER
     * @see Widget#checkSubclass
     * @see Widget#getStyle
     */
    public DartTreeColumn(Tree parent, int style, int index, TreeColumn api) {
        super(parent, checkStyle(style), api);
        this.parent = parent;
        createWidget(index);
    }

    /**
     * Adds the listener to the collection of listeners who will
     * be notified when the control is moved or resized, by sending
     * it one of the messages defined in the <code>ControlListener</code>
     * interface.
     *
     * @param listener the listener which should be notified
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see ControlListener
     * @see #removeControlListener
     */
    public void addControlListener(ControlListener listener) {
        addTypedListener(listener, SWT.Resize, SWT.Move);
    }

    /**
     * Adds the listener to the collection of listeners who will
     * be notified when the control is selected by the user, by sending
     * it one of the messages defined in the <code>SelectionListener</code>
     * interface.
     * <p>
     * <code>widgetSelected</code> is called when the column header is selected.
     * <code>widgetDefaultSelected</code> is not called.
     * </p>
     *
     * @param listener the listener which should be notified when the control is selected by the user
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
        addTypedListener(listener, SWT.Selection, SWT.DefaultSelection);
    }

    static int checkStyle(int style) {
        return checkBits(style, SWT.LEFT, SWT.CENTER, SWT.RIGHT, 0, 0, 0);
    }

    @Override
    public void checkSubclass() {
        if (!isValidSubclass())
            error(SWT.ERROR_INVALID_SUBCLASS);
    }

    @Override
    void createWidget(int index) {
        ((DartTree) parent.getImpl()).createItem(this.getApi(), index);
        setOrientation(true);
        hookEvents();
        register();
        text = "";
    }

    @Override
    void deregister() {
        super.deregister();
        ((SwtDisplay) display.getImpl()).removeWidget(getApi().handle);
        if (buttonHandle != 0)
            ((SwtDisplay) display.getImpl()).removeWidget(buttonHandle);
        if (labelHandle != 0)
            ((SwtDisplay) display.getImpl()).removeWidget(labelHandle);
    }

    @Override
    void destroyWidget() {
        ((DartTree) parent.getImpl()).destroyItem(this.getApi());
        releaseHandle();
    }

    /**
     * Returns a value which describes the position of the
     * text or image in the receiver. The value will be one of
     * <code>LEFT</code>, <code>RIGHT</code> or <code>CENTER</code>.
     *
     * @return the alignment
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public int getAlignment() {
        checkWidget();
        if ((getApi().style & SWT.LEFT) != 0)
            return SWT.LEFT;
        if ((getApi().style & SWT.CENTER) != 0)
            return SWT.CENTER;
        if ((getApi().style & SWT.RIGHT) != 0)
            return SWT.RIGHT;
        return SWT.LEFT;
    }

    /**
     * Gets the moveable attribute. A column that is
     * not moveable cannot be reordered by the user
     * by dragging the header but may be reordered
     * by the programmer.
     *
     * @return the moveable attribute
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see Tree#getColumnOrder()
     * @see Tree#setColumnOrder(int[])
     * @see TreeColumn#setMoveable(boolean)
     * @see SWT#Move
     *
     * @since 3.2
     */
    public boolean getMoveable() {
        checkWidget();
        return this.moveable;
    }

    /**
     * Returns the receiver's parent, which must be a <code>Tree</code>.
     *
     * @return the receiver's parent
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public Tree getParent() {
        checkWidget();
        return parent;
    }

    /**
     * Gets the resizable attribute. A column that is
     * not resizable cannot be dragged by the user but
     * may be resized by the programmer.
     *
     * @return the resizable attribute
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public boolean getResizable() {
        checkWidget();
        return this.resizable;
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
     *
     * @since 3.2
     */
    public String getToolTipText() {
        checkWidget();
        return toolTipText;
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
        checkWidget();
        return DPIUtil.autoScaleDown(getWidthInPixels());
    }

    int getWidthInPixels() {
        checkWidget();
        return this.lastWidth;
    }

    @Override
    void hookEvents() {
        super.hookEvents();
        if (buttonHandle != 0) {
        }
    }

    /**
     * Causes the receiver to be resized to its preferred size.
     * For a composite, this involves computing the preferred size
     * from its layout, if there is one.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void pack() {
        checkWidget();
        int width = 0;
        if (buttonHandle != 0) {
        }
        if ((parent.style & SWT.VIRTUAL) != 0) {
            for (int i = 0; i < ((DartTree) parent.getImpl()).items.length; i++) {
                TreeItem item = ((DartTree) parent.getImpl()).items[i];
                if (item != null && ((DartTreeItem) item.getImpl()).cached) {
                    width = Math.max(width, ((DartTree) parent.getImpl()).calculateWidth(getApi().handle, item.handle, true));
                }
            }
        } else {
        }
        setWidthInPixels(width);
    }

    @Override
    void register() {
        super.register();
        ((SwtDisplay) display.getImpl()).addWidget(getApi().handle, this.getApi());
        if (buttonHandle != 0)
            ((SwtDisplay) display.getImpl()).addWidget(buttonHandle, this.getApi());
        if (labelHandle != 0)
            ((SwtDisplay) display.getImpl()).addWidget(labelHandle, this.getApi());
    }

    @Override
    void releaseHandle() {
        super.releaseHandle();
        getApi().handle = buttonHandle = labelHandle = imageHandle = 0;
        modelIndex = -1;
        parent = null;
    }

    @Override
    void releaseParent() {
        super.releaseParent();
        if (((DartTree) parent.getImpl()).sortColumn == this.getApi()) {
            ((DartTree) parent.getImpl()).sortColumn = null;
        }
    }

    /**
     * Removes the listener from the collection of listeners who will
     * be notified when the control is moved or resized.
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
     * @see ControlListener
     * @see #addControlListener
     */
    public void removeControlListener(ControlListener listener) {
        checkWidget();
        if (listener == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (eventTable == null)
            return;
        eventTable.unhook(SWT.Move, listener);
        eventTable.unhook(SWT.Resize, listener);
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
        checkWidget();
        if (listener == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (eventTable == null)
            return;
        eventTable.unhook(SWT.Selection, listener);
        eventTable.unhook(SWT.DefaultSelection, listener);
    }

    /**
     * Controls how text and images will be displayed in the receiver.
     * The argument should be one of <code>LEFT</code>, <code>RIGHT</code>
     * or <code>CENTER</code>.
     * <p>
     * Note that due to a restriction on some platforms, the first column
     * is always left aligned.
     * </p>
     * @param alignment the new alignment
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setAlignment(int alignment) {
        dirty();
        checkWidget();
        if ((alignment & (SWT.LEFT | SWT.RIGHT | SWT.CENTER)) == 0)
            return;
        int index = parent.indexOf(this.getApi());
        if (index == -1 || index == 0)
            return;
        getApi().style &= ~(SWT.LEFT | SWT.RIGHT | SWT.CENTER);
        getApi().style |= alignment & (SWT.LEFT | SWT.RIGHT | SWT.CENTER);
        ((DartTree) parent.getImpl()).createRenderers(getApi().handle, modelIndex, index == 0, getApi().style);
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

    /**
     * Sets the moveable attribute.  A column that is
     * moveable can be reordered by the user by dragging
     * the header. A column that is not moveable cannot be
     * dragged by the user but may be reordered
     * by the programmer.
     *
     * @param moveable the moveable attribute
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see Tree#setColumnOrder(int[])
     * @see Tree#getColumnOrder()
     * @see TreeColumn#getMoveable()
     * @see SWT#Move
     *
     * @since 3.2
     */
    public void setMoveable(boolean moveable) {
        dirty();
        checkWidget();
        this.moveable = moveable;
    }

    @Override
    void setOrientation(boolean create) {
        if ((parent.style & SWT.RIGHT_TO_LEFT) != 0 || !create) {
            if (buttonHandle != 0) {
            }
        }
    }

    /**
     * Sets the resizable attribute.  A column that is
     * not resizable cannot be dragged by the user but
     * may be resized by the programmer.
     *
     * @param resizable the resize attribute
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setResizable(boolean resizable) {
        dirty();
        checkWidget();
        this.resizable = resizable;
    }

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
     *
     * @since 3.2
     */
    public void setToolTipText(String string) {
        dirty();
        checkWidget();
        toolTipText = string;
        setToolTipText(buttonHandle, string);
    }

    /**
     * Sets the width of the receiver.
     *
     * @param width the new width
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setWidth(int width) {
        checkWidget();
        setWidthInPixels(DPIUtil.autoScaleUp(width));
    }

    void setWidthInPixels(int width) {
        dirty();
        checkWidget();
        if (width < 0)
            return;
        if (width == lastWidth)
            return;
        if (width > 0) {
        }
        lastWidth = width;
        /*
	 * Bug in GTK. When the column is made visible the event window of column
	 * header is raised above the gripper window of the previous column. In
	 * some cases, this can cause the previous column to be not resizable by
	 * the mouse. The fix is to find the event window and lower it to bottom to
	 * the z-order stack.
	 */
        if (width != 0) {
            if (buttonHandle != 0) {
            }
        }
        sendEvent(SWT.Resize);
    }

    void setHeaderCSS(String css) {
        if (headerButtonCSSProvider == 0) {
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (headerButtonCSSProvider != 0) {
            headerButtonCSSProvider = 0;
        }
    }

    @Override
    long dpiChanged(long object, long arg0) {
        super.dpiChanged(object, arg0);
        if (image != null) {
            setImage(image);
        }
        return 0;
    }

    boolean moveable;

    boolean resizable;

    public long _headerButtonCSSProvider() {
        return headerButtonCSSProvider;
    }

    public long _labelHandle() {
        return labelHandle;
    }

    public long _imageHandle() {
        return imageHandle;
    }

    public long _buttonHandle() {
        return buttonHandle;
    }

    public Tree _parent() {
        return parent;
    }

    public int _modelIndex() {
        return modelIndex;
    }

    public int _lastTime() {
        return lastTime;
    }

    public int _lastX() {
        return lastX;
    }

    public int _lastWidth() {
        return lastWidth;
    }

    public boolean _customDraw() {
        return customDraw;
    }

    public String _toolTipText() {
        return toolTipText;
    }

    public boolean _moveable() {
        return moveable;
    }

    public boolean _resizable() {
        return resizable;
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
        FlutterBridge.on(this, "Control", "Move", e -> {
            getDisplay().asyncExec(() -> {
                sendEvent(SWT.Move, e);
            });
        });
        FlutterBridge.on(this, "Control", "Resize", e -> {
            getDisplay().asyncExec(() -> {
                sendEvent(SWT.Resize, e);
            });
        });
        FlutterBridge.on(this, "Selection", "DefaultSelection", e -> {
            getDisplay().asyncExec(() -> {
                sendEvent(SWT.DefaultSelection, e);
            });
        });
        FlutterBridge.on(this, "Selection", "Selection", e -> {
            getDisplay().asyncExec(() -> {
                sendEvent(SWT.Selection, e);
            });
        });
    }

    public TreeColumn getApi() {
        if (api == null)
            api = TreeColumn.createApi(this);
        return (TreeColumn) api;
    }

    public VTreeColumn getValue() {
        if (value == null)
            value = new VTreeColumn(this);
        return (VTreeColumn) value;
    }
}
