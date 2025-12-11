/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2023 IBM Corporation and others.
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
import org.eclipse.swt.widgets.Menu.*;
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

    long groupHandle, labelHandle, imageHandle;

    /**
     * Feature in Gtk: as of Gtk version 3.10 GtkImageMenuItem is deprecated,
     * meaning that MenuItems in SWT with images can no longer be GtkImageMenuItems
     * after Gtk3.10. The solution to this is to create a GtkMenuItem, add a GtkBox
     * as its child, and pack that box with a GtkLabel and GtkImage. This reproduces
     * the functionality of a GtkImageMenuItem and allows SWT to retain image support
     * for MenuItems.
     *
     * For more information see:
     * https://developer.gnome.org/gtk3/stable/GtkImageMenuItem.html#GtkImageMenuItem.description
     * Bug 470298
     */
    long boxHandle;

    int accelerator, userId;

    String toolTipText;

    Image defaultDisableImage;

    boolean enabled = true;

    /**
     * GTK4 only fields
     */
    long modelHandle, actionHandle, shortcutHandle;

    DartMenu.Section section;

    String actionName;

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
        int count = parent.getItemCount();
        if (!(0 <= index && index <= count)) {
            error(SWT.ERROR_INVALID_RANGE);
        }
        ((DartMenu) parent.getImpl()).createItem(this.getApi(), parent.getItemCount());
    }

    void addAccelerator(long accelGroup) {
        updateAccelerator(accelGroup, true);
    }

    void addAccelerators(long accelGroup) {
        addAccelerator(accelGroup);
        if (menu != null)
            ((DartMenu) menu.getImpl()).addAccelerators(accelGroup);
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

    static int checkStyle(int style) {
        return checkBits(style, SWT.PUSH, SWT.CHECK, SWT.RADIO, SWT.SEPARATOR, SWT.CASCADE, 0);
    }

    @Override
    public void checkSubclass() {
        if (!isValidSubclass())
            error(SWT.ERROR_INVALID_SUBCLASS);
    }

    @Override
    void createHandle(int index) {
    }

    void fixMenus(Decorations newParent) {
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

    long getAccelGroup() {
        Menu menu = parent;
        while (menu != null && ((DartMenu) menu.getImpl()).cascade != null) {
            menu = ((DartMenuItem) ((DartMenu) menu.getImpl()).cascade.getImpl()).parent;
        }
        if (menu == null)
            return 0;
        Decorations shell = ((DartMenu) menu.getImpl()).parent;
        if (shell == null)
            return 0;
        return ((SwtDecorations) shell.getImpl()).menuBar == menu ? ((SwtDecorations) shell.getImpl()).accelGroup : 0;
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
        return toolTipText;
    }

    @Override
    void hookEvents() {
        super.hookEvents();
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
    void releaseParent() {
        super.releaseParent();
        if (menu != null) {
            if (((DartMenu) menu.getImpl()).selectedItem == this.getApi())
                ((DartMenu) menu.getImpl()).selectedItem = null;
            menu.dispose();
            menu = null;
        }
    }

    @Override
    void releaseWidget() {
        super.releaseWidget();
        accelerator = 0;
        disposeDefaultDisabledImage();
    }

    @Override
    void destroyWidget() {
        {
            super.destroyWidget();
        }
    }

    void removeAccelerator(long accelGroup) {
        updateAccelerator(accelGroup, false);
    }

    void removeAccelerators(long accelGroup) {
        removeAccelerator(accelGroup);
        if (menu != null)
            ((DartMenu) menu.getImpl()).removeAccelerators(accelGroup);
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
        {
            long accelGroup = getAccelGroup();
            if (accelGroup != 0)
                removeAccelerator(accelGroup);
            this.accelerator = accelerator;
            if (accelGroup != 0)
                addAccelerator(accelGroup);
        }
    }

    void addShortcut(int accelerator) {
        if (accelerator == 0 || !getEnabled())
            return;
        if ((accelerator & SWT.COMMAND) != 0)
            return;
        int keyval = accelerator & SWT.KEY_MASK;
        int newKey = SwtDisplay.untranslateKey(keyval);
        if (newKey != 0) {
            keyval = newKey;
        } else {
            switch(keyval) {
                case '\r':
                    break;
                default:
            }
        }
        if (keyval != 0) {
            if (shortcutHandle == 0)
                error(SWT.ERROR_NO_HANDLES);
        }
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
        if (this.enabled == enabled)
            return;
        this.enabled = enabled;
        _setEnabledOrDisabledImage();
    }

    private void _setEnabledOrDisabledImage() {
        if (!enabled) {
            if (defaultDisableImage == null && image != null) {
                defaultDisableImage = new Image(getDisplay(), image, SWT.IMAGE_DISABLE);
            }
            _setImage(defaultDisableImage);
        }
        if (enabled && image != null)
            _setImage(image);
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
        disposeDefaultDisabledImage();
        super.setImage(image);
        _setEnabledOrDisabledImage();
    }

    private void _setImage(Image image) {
        if (image != null) {
        } else {
            if (imageHandle != 0) {
            }
        }
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
        dirty();
        checkWidget();
        /* Check to make sure the new menu is valid */
        if ((getApi().style & SWT.CASCADE) == 0) {
            error(SWT.ERROR_MENUITEM_NOT_CASCADE);
        }
        if (menu != null) {
            if ((menu.style & SWT.DROP_DOWN) == 0) {
                error(SWT.ERROR_MENU_NOT_DROP_DOWN);
            }
            if (((DartMenu) menu.getImpl()).parent != ((DartMenu) parent.getImpl()).parent) {
                error(SWT.ERROR_INVALID_PARENT);
            }
        }
        /* Assign the new menu */
        Menu oldMenu = this.menu;
        if (oldMenu == menu)
            return;
        if (oldMenu != null)
            ((DartMenu) oldMenu.getImpl()).cascade = null;
        this.menu = menu;
        if (menu != null) {
            ((DartMenu) menu.getImpl()).cascade = this.getApi();
        }
    }

    @Override
    void setOrientation(boolean create) {
        super.setOrientation(create);
        if ((parent.style & SWT.RIGHT_TO_LEFT) != 0 || !create) {
            if (menu != null)
                ((DartMenu) menu.getImpl())._setOrientation(parent.style & (SWT.RIGHT_TO_LEFT | SWT.LEFT_TO_RIGHT));
        }
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
        int index = string.indexOf('\t');
        if (index != -1) {
            string = string.substring(0, index);
        }
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
        if (toolTip != null && (toolTip.trim().length() == 0 || toolTip.equals(toolTipText)))
            return;
        toolTipText = toolTip;
        setToolTipText(getApi().handle, toolTip);
    }

    void updateAccelerator(long accelGroup, boolean add) {
        if (accelerator == 0 || !getEnabled())
            return;
        if ((accelerator & SWT.COMMAND) != 0)
            return;
        int keysym = accelerator & SWT.KEY_MASK;
        int newKey = SwtDisplay.untranslateKey(keysym);
        if (newKey != 0) {
            keysym = newKey;
        } else {
            switch(keysym) {
                case '\r':
                    break;
                default:
            }
        }
        /* When accel_key is zero, it causes GTK warnings */
        if (keysym != 0) {
            if (add) {
            } else {
            }
        }
    }

    private static class MaskKeysym {

        int mask = 0;

        int keysym = 0;
    }

    private MaskKeysym getMaskKeysym() {
        return null;
    }

    boolean updateAcceleratorText(boolean show) {
        if (accelerator != 0)
            return false;
        MaskKeysym maskKeysym = null;
        if (show) {
            maskKeysym = getMaskKeysym();
        }
        if (maskKeysym == null)
            return true;
        if (maskKeysym.keysym != 0) {
            if (show) {
            } else {
            }
        }
        return maskKeysym.keysym != 0;
    }

    @Override
    long dpiChanged(long object, long arg0) {
        super.dpiChanged(object, arg0);
        if (image != null) {
            setImage(image);
        }
        return 0;
    }

    private void disposeDefaultDisabledImage() {
        if (defaultDisableImage != null) {
            defaultDisableImage.dispose();
            defaultDisableImage = null;
        }
    }

    boolean selection;

    public Menu _parent() {
        return parent;
    }

    public Menu _menu() {
        return menu;
    }

    public long _groupHandle() {
        return groupHandle;
    }

    public long _labelHandle() {
        return labelHandle;
    }

    public long _imageHandle() {
        return imageHandle;
    }

    public long _boxHandle() {
        return boxHandle;
    }

    public int _accelerator() {
        return accelerator;
    }

    public int _userId() {
        return userId;
    }

    public String _toolTipText() {
        return toolTipText;
    }

    public Image _defaultDisableImage() {
        return defaultDisableImage;
    }

    public boolean _enabled() {
        return enabled;
    }

    public long _modelHandle() {
        return modelHandle;
    }

    public long _actionHandle() {
        return actionHandle;
    }

    public long _shortcutHandle() {
        return shortcutHandle;
    }

    public String _actionName() {
        return actionName;
    }

    public boolean _selection() {
        return selection;
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
