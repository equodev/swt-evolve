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
package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.internal.*;
import dev.equo.swt.*;

/**
 * Instances of this class represent a selectable user interface object
 * that issues notification when pressed and released.
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>CHECK, CASCADE, PUSH, RADIO, SEPARATOR</dd>
 * <dt><b>Events:</b></dt>
 * <dd>Arm, Help, Selection</dd>
 * </dl>
 * <p>
 * Note: Only one of the styles CHECK, CASCADE, PUSH, RADIO and SEPARATOR
 * may be specified.
 * </p><p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 *
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class DartMenuItem extends DartItem implements IMenuItem {

    Menu parent, menu;

    long hBitmap;

    int id, accelerator, userId;

    ToolTip itemToolTip;

    /* Image margin. */
    final static int MARGIN_WIDTH = 1;

    final static int MARGIN_HEIGHT = 1;

    private final static int LEFT_TEXT_MARGIN = 7;

    private final static int IMAGE_TEXT_GAP = 3;

    // There is a weird behavior in the Windows API with menus in OWENERDRAW mode that the returned
    // value in wmMeasureChild is increased by a fixed value (in points) when wmDrawChild is called
    // This static is used to mitigate this increase
    private final static int WINDOWS_OVERHEAD = 6;

    /**
     * Constructs a new instance of this class given its parent
     * (which must be a <code>Menu</code>) and a style value
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
     * @param parent a menu control which will be the parent of the new instance (cannot be null)
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
     * @see SWT#CHECK
     * @see SWT#CASCADE
     * @see SWT#PUSH
     * @see SWT#RADIO
     * @see SWT#SEPARATOR
     * @see Widget#checkSubclass
     * @see Widget#getStyle
     */
    public DartMenuItem(Menu parent, int style, MenuItem api) {
        super(parent, checkStyle(style), api);
        this.parent = parent;
        ((DartMenu) parent.getImpl()).createItem(this.getApi(), parent.getItemCount());
    }

    /**
     * Constructs a new instance of this class given its parent
     * (which must be a <code>Menu</code>), a style value
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
     * @param parent a menu control which will be the parent of the new instance (cannot be null)
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
     * @see SWT#CHECK
     * @see SWT#CASCADE
     * @see SWT#PUSH
     * @see SWT#RADIO
     * @see SWT#SEPARATOR
     * @see Widget#checkSubclass
     * @see Widget#getStyle
     */
    public DartMenuItem(Menu parent, int style, int index, MenuItem api) {
        super(parent, checkStyle(style), api);
        this.parent = parent;
        ((DartMenu) parent.getImpl()).createItem(this.getApi(), index);
    }

    /**
     * Adds the listener to the collection of listeners who will
     * be notified when the arm events are generated for the control, by sending
     * it one of the messages defined in the <code>ArmListener</code>
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
     * @see ArmListener
     * @see #removeArmListener
     */
    public void addArmListener(ArmListener listener) {
        addTypedListener(listener, SWT.Arm);
    }

    /**
     * Adds the listener to the collection of listeners who will
     * be notified when the help events are generated for the control, by sending
     * it one of the messages defined in the <code>HelpListener</code>
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
     * @see HelpListener
     * @see #removeHelpListener
     */
    public void addHelpListener(HelpListener listener) {
        addTypedListener(listener, SWT.Help);
    }

    /**
     * Adds the listener to the collection of listeners who will
     * be notified when the menu item is selected by the user, by sending
     * it one of the messages defined in the <code>SelectionListener</code>
     * interface.
     * <p>
     * When <code>widgetSelected</code> is called, the stateMask field of the event object is valid.
     * <code>widgetDefaultSelected</code> is not called.
     * </p>
     * <p>
     * When the <code>SWT.RADIO</code> style bit is set, the <code>widgetSelected</code> method is
     * also called when the receiver loses selection because another item in the same radio group
     * was selected by the user. During <code>widgetSelected</code> the application can use
     * <code>getSelection()</code> to determine the current selected state of the receiver.
     * </p>
     *
     * @param listener the listener which should be notified when the menu item is selected by the user
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

    @Override
    public void checkSubclass() {
        if (!isValidSubclass())
            error(SWT.ERROR_INVALID_SUBCLASS);
    }

    static int checkStyle(int style) {
        return checkBits(style, SWT.PUSH, SWT.CHECK, SWT.RADIO, SWT.SEPARATOR, SWT.CASCADE, 0);
    }

    @Override
    void destroyWidget() {
        ((DartMenu) parent.getImpl()).destroyItem(this.getApi());
        releaseHandle();
    }

    void fixMenus(Decorations newParent) {
        this.getApi().nativeZoom = newParent.nativeZoom;
        if (menu != null && !menu.isDisposed() && !newParent.isDisposed())
            ((DartMenu) menu.getImpl()).fixMenus(newParent);
    }

    /**
     * Returns the widget accelerator.  An accelerator is the bit-wise
     * OR of zero or more modifier masks and a key. Examples:
     * <code>SWT.CONTROL | SWT.SHIFT | 'T', SWT.ALT | SWT.F2</code>.
     * The default value is zero, indicating that the menu item does
     * not have an accelerator.
     *
     * @return the accelerator or 0
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public int getAccelerator() {
        checkWidget();
        return accelerator;
    }

    /**
     * Returns a rectangle describing the receiver's size and location
     * relative to its parent (or its display if its parent is null).
     *
     * @return the receiver's bounding rectangle
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.1
     */
    /*public*/
    Rectangle getBounds() {
        checkWidget();
        int index = parent.indexOf(this.getApi());
        if (index == -1)
            return new Rectangle(0, 0, 0, 0);
        if ((parent.style & SWT.BAR) != 0) {
            Decorations shell = ((DartMenu) parent.getImpl()).parent;
            if (((SwtDecorations) shell.getImpl()).menuBar != parent) {
                return new Rectangle(0, 0, 0, 0);
            }
        } else {
        }
        return null;
    }

    /**
     * Returns <code>true</code> if the receiver is enabled, and
     * <code>false</code> otherwise. A disabled menu item is typically
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
        checkWidget();
        /*
	* Feature in Windows.  For some reason, when the menu item
	* is a separator, GetMenuItemInfo() always indicates that
	* the item is not enabled.  The fix is to track the enabled
	* state for separators.
	*/
        if ((getApi().style & SWT.SEPARATOR) != 0) {
            return (getApi().state & DISABLED) == 0;
        }
        return this.enabled;
    }

    /**
     * Gets the identifier associated with the receiver.
     *
     * @return the receiver's identifier
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.7
     */
    public int getID() {
        checkWidget();
        return userId;
    }

    /**
     * Returns the receiver's cascade menu if it has one or null
     * if it does not. Only <code>CASCADE</code> menu items can have
     * a pull down menu. The sequence of key strokes, button presses
     * and/or button releases that are used to request a pull down
     * menu is platform specific.
     *
     * @return the receiver's menu
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    @Override
    public Menu getMenu() {
        checkWidget();
        return menu;
    }

    @Override
    String getNameText() {
        if ((getApi().style & SWT.SEPARATOR) != 0)
            return "|";
        return super.getNameText();
    }

    /**
     * Returns the receiver's parent, which must be a <code>Menu</code>.
     *
     * @return the receiver's parent
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public Menu getParent() {
        checkWidget();
        return parent;
    }

    /**
     * Returns <code>true</code> if the receiver is selected,
     * and false otherwise.
     * <p>
     * When the receiver is of type <code>CHECK</code> or <code>RADIO</code>,
     * it is selected when it is checked.
     *
     * @return the selection state
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public boolean getSelection() {
        checkWidget();
        if ((getApi().style & (SWT.CHECK | SWT.RADIO)) == 0)
            return false;
        return this.selection;
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
     *
     * @since 3.104
     */
    public String getToolTipText() {
        checkWidget();
        return (itemToolTip == null || itemToolTip.isDisposed()) ? null : itemToolTip.getMessage();
    }

    void hideToolTip() {
        if (itemToolTip == null || itemToolTip.isDisposed())
            return;
        itemToolTip.setVisible(false);
    }

    /**
     * Returns <code>true</code> if the receiver is enabled and all
     * of the receiver's ancestors are enabled, and <code>false</code>
     * otherwise. A disabled menu item is typically not selectable from the
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
        return getEnabled() && parent.isEnabled();
    }

    @Override
    void releaseChildren(boolean destroy) {
        if (menu != null) {
            menu.getImpl().release(false);
            menu = null;
        }
        super.releaseChildren(destroy);
    }

    @Override
    void releaseHandle() {
        super.releaseHandle();
        parent = null;
        id = -1;
    }

    @Override
    void releaseParent() {
        super.releaseParent();
        if (menu != null)
            menu.dispose();
        menu = null;
    }

    @Override
    void releaseWidget() {
        super.releaseWidget();
        hBitmap = 0;
        if (accelerator != 0) {
            ((DartMenu) parent.getImpl()).destroyAccelerators();
        }
        accelerator = 0;
        if (itemToolTip != null && !itemToolTip.isDisposed()) {
            itemToolTip.setVisible(false);
            itemToolTip.dispose();
            itemToolTip = null;
        }
        ((SwtDisplay) display.getImpl()).removeMenuItem(this.getApi());
    }

    /**
     * Removes the listener from the collection of listeners who will
     * be notified when the arm events are generated for the control.
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
     * @see ArmListener
     * @see #addArmListener
     */
    public void removeArmListener(ArmListener listener) {
        checkWidget();
        if (listener == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (eventTable == null)
            return;
        eventTable.unhook(SWT.Arm, listener);
    }

    /**
     * Removes the listener from the collection of listeners who will
     * be notified when the help events are generated for the control.
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
     * @see HelpListener
     * @see #addHelpListener
     */
    public void removeHelpListener(HelpListener listener) {
        checkWidget();
        if (listener == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (eventTable == null)
            return;
        eventTable.unhook(SWT.Help, listener);
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

    @Override
    void reskinChildren(int flags) {
        if (menu != null) {
            menu.reskin(flags);
        }
        super.reskinChildren(flags);
    }

    void selectRadio() {
        int index = 0;
        MenuItem[] items = parent.getItems();
        while (index < items.length && items[index] != this.getApi()) index++;
        int i = index - 1;
        while (i >= 0 && ((DartMenuItem) items[i].getImpl()).setRadioSelection(false)) --i;
        int j = index + 1;
        while (j < items.length && ((DartMenuItem) items[j].getImpl()).setRadioSelection(false)) j++;
        setSelection(true);
    }

    /**
     * Sets the widget accelerator.  An accelerator is the bit-wise
     * OR of zero or more modifier masks and a key. Examples:
     * <code>SWT.MOD1 | SWT.MOD2 | 'T', SWT.MOD3 | SWT.F2</code>.
     * <code>SWT.CONTROL | SWT.SHIFT | 'T', SWT.ALT | SWT.F2</code>.
     * The default value is zero, indicating that the menu item does
     * not have an accelerator.
     *
     * @param accelerator an integer that is the bit-wise OR of masks and a key
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setAccelerator(int accelerator) {
        dirty();
        checkWidget();
        if (this.accelerator == accelerator)
            return;
        this.accelerator = accelerator;
        ((DartMenu) parent.getImpl()).destroyAccelerators();
    }

    /**
     * Enables the receiver if the argument is <code>true</code>,
     * and disables it otherwise. A disabled menu item is typically
     * not selectable from the user interface and draws with an
     * inactive or "grayed" look.
     *
     * @param enabled the new enabled state
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setEnabled(boolean enabled) {
        dirty();
        checkWidget();
        /*
	* Feature in Windows.  For some reason, when the menu item
	* is a separator, GetMenuItemInfo() always indicates that
	* the item is not enabled.  The fix is to track the enabled
	* state for separators.
	*/
        if ((getApi().style & SWT.SEPARATOR) != 0) {
            if (enabled) {
                getApi().state &= ~DISABLED;
            } else {
                getApi().state |= DISABLED;
            }
        }
        if (enabled) {
        } else {
        }
        ((DartMenu) parent.getImpl()).destroyAccelerators();
        ((DartMenu) parent.getImpl()).redraw();
        this.enabled = enabled;
    }

    /**
     * Sets the identifier associated with the receiver to the argument.
     *
     * @param id the new identifier. This must be a non-negative value. System-defined identifiers are negative values.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *    <li>ERROR_INVALID_ARGUMENT - if called with an negative-valued argument.</li>
     * </ul>
     *
     * @since 3.7
     */
    public void setID(int id) {
        dirty();
        checkWidget();
        if (id < 0)
            error(SWT.ERROR_INVALID_ARGUMENT);
        userId = id;
    }

    /**
     * Sets the receiver's image to the argument, which may be
     * null indicating that no image should be displayed.
     * <p>
     * Note: This operation is a <em>HINT</em> and is not supported on
     * platforms that do not have this concept (for example, Windows NT).
     * Some platforms (such as GTK3) support images alongside check boxes.
     * </p>
     *
     * @param image the image to display on the receiver (may be null)
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    @Override
    public void setImage(Image image) {
        dirty();
        checkWidget();
        if (this.image == image)
            return;
        if ((getApi().style & SWT.SEPARATOR) != 0)
            return;
        super.setImage(image);
        if (((DartMenu) parent.getImpl()).needsMenuCallback()) {
        } else {
        }
        ((DartMenu) parent.getImpl()).redraw();
    }

    /**
     * Sets the receiver's pull down menu to the argument.
     * Only <code>CASCADE</code> menu items can have a
     * pull down menu. The sequence of key strokes, button presses
     * and/or button releases that are used to request a pull down
     * menu is platform specific.
     * <p>
     * Note: Disposing of a menu item that has a pull down menu
     * will dispose of the menu.  To avoid this behavior, set the
     * menu to null before the menu item is disposed.
     * </p>
     *
     * @param menu the new pull down menu
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_MENU_NOT_DROP_DOWN - if the menu is not a drop down menu</li>
     *    <li>ERROR_MENUITEM_NOT_CASCADE - if the menu item is not a <code>CASCADE</code></li>
     *    <li>ERROR_INVALID_ARGUMENT - if the menu has been disposed</li>
     *    <li>ERROR_INVALID_PARENT - if the menu is not in the same widget tree</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setMenu(Menu menu) {
        checkWidget();
        /* Check to make sure the new menu is valid */
        if ((getApi().style & SWT.CASCADE) == 0) {
            error(SWT.ERROR_MENUITEM_NOT_CASCADE);
        }
        if (menu != null) {
            if (menu.isDisposed())
                error(SWT.ERROR_INVALID_ARGUMENT);
            if ((menu.style & SWT.DROP_DOWN) == 0) {
                error(SWT.ERROR_MENU_NOT_DROP_DOWN);
            }
            if (((DartMenu) menu.getImpl()).parent != ((DartMenu) parent.getImpl()).parent) {
                error(SWT.ERROR_INVALID_PARENT);
            }
        }
        setMenu(menu, false);
    }

    void setMenu(Menu menu, boolean dispose) {
        dirty();
        /* Assign the new menu */
        Menu oldMenu = this.menu;
        if (oldMenu == menu)
            return;
        if (oldMenu != null)
            ((DartMenu) oldMenu.getImpl()).cascade = null;
        this.menu = menu;
        int cch = 128;
        if (menu != null) {
            ((DartMenu) menu.getImpl()).cascade = this.getApi();
        }
        if (dispose || oldMenu == null) {
        } else {
        }
        ((DartMenu) parent.getImpl()).destroyAccelerators();
    }

    boolean setRadioSelection(boolean value) {
        if ((getApi().style & SWT.RADIO) == 0)
            return false;
        if (getSelection() != value) {
            setSelection(value);
            sendSelectionEvent(SWT.Selection);
        }
        return true;
    }

    void setOrientation(int orientation) {
        if (menu != null)
            ((DartMenu) menu.getImpl())._setOrientation(orientation);
    }

    /**
     * Sets the selection state of the receiver.
     * <p>
     * When the receiver is of type <code>CHECK</code> or <code>RADIO</code>,
     * it is selected when it is checked.
     *
     * @param selected the new selection state
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setSelection(boolean selected) {
        dirty();
        checkWidget();
        if ((getApi().style & (SWT.CHECK | SWT.RADIO)) == 0)
            return;
        ((DartMenu) parent.getImpl()).redraw();
        this.selection = selected;
        Control ownerCtrl = ((DartMenu) parent.getImpl()).findOwnerControl();
        if (ownerCtrl != null && ownerCtrl.getImpl() instanceof DartControl) {
            ((DartControl) ownerCtrl.getImpl()).dirty();
        }
    }

    /**
     * Sets the receiver's text. The string may include
     * the mnemonic character and accelerator text.
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
     * <p>
     * Accelerator text is indicated by the '\t' character.
     * On platforms that support accelerator text, the text
     * that follows the '\t' character is displayed to the user,
     * typically indicating the key stroke that will cause
     * the item to become selected.  On most platforms, the
     * accelerator text appears right aligned in the menu.
     * Setting the accelerator text does not install the
     * accelerator key sequence. The accelerator key sequence
     * is installed using #setAccelerator.
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
     *
     * @see #setAccelerator
     */
    @Override
    public void setText(String string) {
        dirty();
        checkWidget();
        if (string == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if ((getApi().style & SWT.SEPARATOR) != 0)
            return;
        if (text.equals(string))
            return;
        super.setText(string);
        ((DartMenu) parent.getImpl()).redraw();
    }

    /**
     * Sets the receiver's tool tip text to the argument, which
     * may be null indicating that the default tool tip for the
     * control will be shown. For a menu item that has a default
     * tool tip, setting
     * the tool tip text to an empty string replaces the default,
     * causing no tool tip text to be shown.
     * <p>
     * The mnemonic indicator (character '&amp;') is not displayed in a tool tip.
     * To display a single '&amp;' in the tool tip, the character '&amp;' can be
     * escaped by doubling it in the string.
     * </p>
     * <p>
     * NOTE: Tooltips are currently not shown for top-level menu items in the
     * {@link Shell#setMenuBar(Menu) shell menubar} on Windows, Mac, and Ubuntu Unity desktop.
     * </p>
     * <p>
     * NOTE: This operation is a hint and behavior is platform specific, on Windows
     * for CJK-style mnemonics of the form " (&amp;C)" at the end of the tooltip text
     * are not shown in tooltip.
     * </p>
     * @param toolTip the new tool tip text (or null)
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.104
     */
    public void setToolTipText(String toolTip) {
        dirty();
        checkWidget();
        if (toolTip == null && itemToolTip != null) {
            if (!itemToolTip.isDisposed()) {
                itemToolTip.setVisible(false);
                itemToolTip.dispose();
            }
            itemToolTip = null;
        }
        if (toolTip == null || toolTip.trim().length() == 0 || (itemToolTip != null && !itemToolTip.isDisposed() && toolTip.equals(itemToolTip.getMessage())))
            return;
        if (itemToolTip != null)
            itemToolTip.dispose();
        ;
        itemToolTip.setMessage(toolTip);
        itemToolTip.setVisible(false);
        this.toolTipText = toolTip;
    }

    void showTooltip(int x, int y) {
        if (itemToolTip == null || itemToolTip.isDisposed())
            return;
        ((DartToolTip) itemToolTip.getImpl()).setLocationInPixels(x, y);
        itemToolTip.setVisible(true);
    }

    int widgetStyle() {
        Decorations shell = ((DartMenu) parent.getImpl()).parent;
        if ((shell.style & SWT.MIRRORED) != 0) {
            if ((parent.style & SWT.LEFT_TO_RIGHT) != 0) {
            }
        } else {
            if ((parent.style & SWT.RIGHT_TO_LEFT) != 0) {
            }
        }
        return 0;
    }

    @Override
    GC createNewGC(long hDC, GCData data) {
        return super.createNewGC(hDC, data);
    }

    private int getMonitorZoom() {
        return ((SwtMonitor) getMenu().getShell().getMonitor().getImpl()).zoom;
    }

    private int getMenuZoom() {
        if (getDisplay().isRescalingAtRuntime()) {
            return super.getZoom();
        } else {
            return DPIUtil.getZoomForAutoscaleProperty(getMonitorZoom());
        }
    }

    private Point calculateRenderedTextSize() {
        GC gc = new GC(this.getMenu().getShell());
        String textWithoutMnemonicCharacter = getText().replace("&", "");
        Point points = gc.textExtent(textWithoutMnemonicCharacter);
        gc.dispose();
        if (!getDisplay().isRescalingAtRuntime()) {
            int primaryMonitorZoom = this.getDisplay().getDeviceZoom();
            int adjustedPrimaryMonitorZoom = DPIUtil.getZoomForAutoscaleProperty(primaryMonitorZoom);
            if (primaryMonitorZoom != adjustedPrimaryMonitorZoom) {
                // Windows will use a font matching the native primary monitor zoom for calculating the size in pixels,
                // GC will use the native primary monitor zoom to scale down from pixels to points in this scenario
                // Therefore we need to make sure adjust the points as if it would have been scaled down by the
                // native primary monitor zoom.
                // Example:
                // Primary monitor on 150% with int200: native zoom 150%, adjusted zoom 100%
                // Pixel height of font in this example is 15px
                // GC calculated height of 15px, scales down with adjusted zoom of 100% and returns 15pt -> should be 10pt
                // this calculation is corrected by the following line
                // This is the only place, where the GC needs to use the native zoom to do that, therefore it is fixed only here
                points = DPIUtil.scaleDown(DPIUtil.scaleUp(points, adjustedPrimaryMonitorZoom), primaryMonitorZoom);
            }
        }
        return points;
    }

    private static void handleDPIChange(Widget widget, int newZoom, float scalingFactor) {
        if (!(widget instanceof MenuItem menuItem)) {
            return;
        }
        // Refresh the image
        Image menuItemImage = menuItem.getImage();
        if (menuItemImage != null) {
            Image currentImage = menuItemImage;
            ((DartMenuItem) menuItem.getImpl()).image = null;
            menuItem.setImage(currentImage);
        }
        // Refresh the sub menu
        Menu subMenu = menuItem.getMenu();
        if (subMenu != null) {
        }
    }

    boolean enabled = true;

    boolean selection;

    String toolTipText;

    public Menu _parent() {
        return parent;
    }

    public Menu _menu() {
        return menu;
    }

    public long _hBitmap() {
        return hBitmap;
    }

    public int _id() {
        return id;
    }

    public int _accelerator() {
        return accelerator;
    }

    public int _userId() {
        return userId;
    }

    public ToolTip _itemToolTip() {
        return itemToolTip;
    }

    public boolean _enabled() {
        return enabled;
    }

    public boolean _selection() {
        return selection;
    }

    public String _toolTipText() {
        return toolTipText;
    }

    public FlutterBridge getBridge() {
        if (bridge != null)
            return bridge;
        Composite p = parent.getParent();
        while (p != null && !(p.getImpl() instanceof DartWidget)) p = p.getImpl()._parent();
        return p != null ? ((DartWidget) p.getImpl()).getBridge() : null;
    }

    protected void _hookEvents() {
        super._hookEvents();
        FlutterBridge.on(this, "Arm", "Arm", e -> {
            getDisplay().asyncExec(() -> {
                sendEvent(SWT.Arm, e);
            });
        });
        FlutterBridge.on(this, "Help", "Help", e -> {
            getDisplay().asyncExec(() -> {
                sendEvent(SWT.Help, e);
            });
        });
        FlutterBridge.on(this, "Selection", "DefaultSelection", e -> {
            getDisplay().asyncExec(() -> {
                sendEvent(SWT.DefaultSelection, e);
            });
        });
        FlutterBridge.on(this, "Selection", "Selection", e -> {
            getDisplay().asyncExec(() -> {
                if ((getApi().style & SWT.CHECK) != 0) {
                    setSelection(!getSelection());
                } else if ((getApi().style & SWT.RADIO) != 0) {
                    selectRadio();
                }
                sendEvent(SWT.Selection, e);
            });
        });
    }

    public MenuItem getApi() {
        if (api == null)
            api = MenuItem.createApi(this);
        return (MenuItem) api;
    }

    public VMenuItem getValue() {
        if (value == null)
            value = new VMenuItem(this);
        return (VMenuItem) value;
    }
}
