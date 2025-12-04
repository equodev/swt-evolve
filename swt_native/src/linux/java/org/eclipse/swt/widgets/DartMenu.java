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

import java.util.*;
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.internal.*;
import dev.equo.swt.*;

/**
 * Instances of this class are user interface objects that contain
 * menu items.
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>BAR, DROP_DOWN, POP_UP, NO_RADIO_GROUP</dd>
 * <dd>LEFT_TO_RIGHT, RIGHT_TO_LEFT</dd>
 * <dt><b>Events:</b></dt>
 * <dd>Help, Hide, Show </dd>
 * </dl>
 * <p>
 * Note: Only one of BAR, DROP_DOWN and POP_UP may be specified.
 * Only one of LEFT_TO_RIGHT or RIGHT_TO_LEFT may be specified.
 * </p><p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 *
 * @see <a href="http://www.eclipse.org/swt/snippets/#menu">Menu snippets</a>
 * @see <a href="http://www.eclipse.org/swt/examples.php">SWT Example: ControlExample</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class DartMenu extends DartWidget implements IMenu {

    int x, y;

    boolean hasLocation;

    MenuItem cascade, selectedItem;

    Decorations parent;

    int poppedUpCount;

    long menuHandle;

    /**
     * GTK4 only fields
     */
    long modelHandle, actionGroup, shortcutController;

    public class Section {

        LinkedList<MenuItem> sectionItems;

        private MenuItem separator;

        private long sectionHandle;

        public Section() {
            this.sectionItems = new LinkedList<>();
        }

        public Section(MenuItem separator) {
            this();
            this.separator = separator;
        }

        public Section(long sectionHandle) {
            this();
            this.sectionHandle = sectionHandle;
        }

        public long getSectionHandle() {
            return sectionHandle != 0 ? sectionHandle : ((DartMenuItem) separator.getImpl()).modelHandle;
        }

        public int getItemPosition(MenuItem item) {
            return sectionItems.indexOf(item);
        }

        public int getSectionSize() {
            return sectionItems.size();
        }

        public int getSectionPosition() {
            return items.indexOf(separator);
        }
    }

    LinkedList<Section> sections;

    LinkedList<MenuItem> items;

    /**
     * Constructs a new instance of this class given its parent,
     * and sets the style for the instance so that the instance
     * will be a popup menu on the given parent's shell.
     * <p>
     * After constructing a menu, it can be set into its parent
     * using <code>parent.setMenu(menu)</code>.  In this case, the parent may
     * be any control in the same widget tree as the parent.
     * </p>
     *
     * @param parent a control which will be the parent of the new instance (cannot be null)
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
     *    <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
     * </ul>
     *
     * @see SWT#POP_UP
     * @see Widget#checkSubclass
     * @see Widget#getStyle
     */
    public DartMenu(Control parent, Menu api) {
        this(checkNull(parent).getImpl().menuShell(), SWT.POP_UP, api);
    }

    /**
     * Constructs a new instance of this class given its parent
     * (which must be a <code>Decorations</code>) and a style value
     * describing its behavior and appearance.
     * <p>
     * The style value is either one of the style constants defined in
     * class <code>SWT</code> which is applicable to instances of this
     * class, or must be built by <em>bitwise OR</em>'ing together
     * (that is, using the <code>int</code> "|" operator) two or more
     * of those <code>SWT</code> style constants. The class description
     * lists the style constants that are applicable to the class.
     * Style bits are also inherited from superclasses.
     * </p><p>
     * After constructing a menu or menuBar, it can be set into its parent
     * using <code>parent.setMenu(menu)</code> or <code>parent.setMenuBar(menuBar)</code>.
     * </p>
     *
     * @param parent a decorations control which will be the parent of the new instance (cannot be null)
     * @param style the style of menu to construct
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
     *    <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
     * </ul>
     *
     * @see SWT#BAR
     * @see SWT#DROP_DOWN
     * @see SWT#POP_UP
     * @see SWT#NO_RADIO_GROUP
     * @see SWT#LEFT_TO_RIGHT
     * @see SWT#RIGHT_TO_LEFT
     * @see Widget#checkSubclass
     * @see Widget#getStyle
     */
    public DartMenu(Decorations parent, int style, Menu api) {
        super(parent, checkStyle(style), api);
        this.parent = parent;
        createWidget(0);
    }

    /**
     * Constructs a new instance of this class given its parent
     * (which must be a <code>Menu</code>) and sets the style
     * for the instance so that the instance will be a drop-down
     * menu on the given parent's parent.
     * <p>
     * After constructing a drop-down menu, it can be set into its parentMenu
     * using <code>parentMenu.setMenu(menu)</code>.
     * </p>
     *
     * @param parentMenu a menu which will be the parent of the new instance (cannot be null)
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
     *    <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
     * </ul>
     *
     * @see SWT#DROP_DOWN
     * @see Widget#checkSubclass
     * @see Widget#getStyle
     */
    public DartMenu(Menu parentMenu, Menu api) {
        this(checkNull(parentMenu).getImpl()._parent(), SWT.DROP_DOWN, api);
    }

    /**
     * Constructs a new instance of this class given its parent
     * (which must be a <code>MenuItem</code>) and sets the style
     * for the instance so that the instance will be a drop-down
     * menu on the given parent's parent menu.
     * <p>
     * After constructing a drop-down menu, it can be set into its parentItem
     * using <code>parentItem.setMenu(menu)</code>.
     * </p>
     *
     * @param parentItem a menu item which will be the parent of the new instance (cannot be null)
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
     *    <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
     * </ul>
     *
     * @see SWT#DROP_DOWN
     * @see Widget#checkSubclass
     * @see Widget#getStyle
     */
    public DartMenu(MenuItem parentItem, Menu api) {
        this(((DartMenuItem) checkNull(parentItem).getImpl()).parent, api);
    }

    static Control checkNull(Control control) {
        if (control == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        return control;
    }

    static Menu checkNull(Menu menu) {
        if (menu == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        return menu;
    }

    static MenuItem checkNull(MenuItem item) {
        if (item == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        return item;
    }

    static int checkStyle(int style) {
        return checkBits(style, SWT.POP_UP, SWT.BAR, SWT.DROP_DOWN, 0, 0, 0);
    }

    /**
     * Bug 532074: setLocation method is limited on Wayland since Wayland
     * has no global coordinates and we cannot use gdk_popup_at_rect if GdkWindow of
     * getShell is not mapped. In this case, we can only pop the menu at the pointer.
     *
     * This happens for example when Problems view is a fast view on Eclipse start,
     * the drop down menu takes PartRenderingEngine's limbo shell
     * (see PartRenderingEngine#safeCreateGui) which is off the screen.
     *
     * @return true iff the location of menu is set and can be used successfully
     */
    boolean ableToSetLocation() {
        return hasLocation;
    }

    public void _setVisible(boolean visible) {
        if (visible) {
            /*
		 * Feature in GTK. When a menu with no items is shown, GTK shows a
		 * weird small rectangle. For comparison, Windows API prevents showing
		 * empty menus on its own. At the same time, Eclipse creates menu items
		 * lazily in `SWT.Show`, so we can't know if menu is empty before
		 * `SWT.Show` is sent.
		 * Known solutions all have drawbacks:
		 * 1) Send `SWT.Show` here, instead of sending it when GtkMenu
		 *    receives 'show' event (see gtk_show()). This allows us to test if
		 *    menu has items before asking GTK to show it. The drawback is that
		 *    when GTK fails to show menu (see Bug 564595), application still
		 *    receives fake `SWT.Show` without a matching `SWT.Hide`.
		 * 2) Send SWT.Show` from gtk_show(). This solves drawback of (1).
		 *    The new drawback is that empty menu will now show, because it's
		 *    hard to stop menu from showing at this point.
		 * 3) Rework Eclipse (and possibly other SWT users) to stop depending
		 *    on `SWT.Show` and use some other event for lazy init. This will
		 *    allow to know if menu is empty in advance. This sounds like the
		 *    best solution, because it solves core problem: empty menus
		 *    shouldn't try to show. The drawback is that it will be a breaking
		 *    change.
		 * Solution (1) has been there since 2002-05-29.
		 */
            sendEvent(SWT.Show);
            if (getItemCount() != 0) {
                /*
			* Feature in GTK. ON_TOP shells will send out
			* SWT.Deactivate whenever a context menu is shown.
			* The fix is to prevent the menu from taking focus
			* when it is being shown in an ON_TOP shell.
			*/
                if ((parent.getImpl()._getShell().style & SWT.ON_TOP) != 0) {
                }
                long eventPtr = 0;
                if (ableToSetLocation()) {
                } else {
                    if (eventPtr == 0) {
                    }
                    adjustParentWindowWayland(eventPtr);
                    verifyMenuPosition(getItemCount());
                }
                poppedUpCount = getItemCount();
            } else {
                sendEvent(SWT.Hide);
            }
        } else {
        }
    }

    void addAccelerators(long accelGroup) {
        MenuItem[] items = getItems();
        for (int i = 0; i < items.length; i++) {
            MenuItem item = items[i];
            ((DartMenuItem) item.getImpl()).addAccelerators(accelGroup);
        }
    }

    /**
     * Adds the listener to the collection of listeners who will
     * be notified when menus are hidden or shown, by sending it
     * one of the messages defined in the <code>MenuListener</code>
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
     * @see MenuListener
     * @see #removeMenuListener
     */
    public void addMenuListener(MenuListener listener) {
        addTypedListener(listener, SWT.Hide, SWT.Show);
    }

    /**
     * Adds the listener to the collection of listeners who will
     * be notified when help events are generated for the control,
     * by sending it one of the messages defined in the
     * <code>HelpListener</code> interface.
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

    @Override
    void createHandle(int index) {
    }

    @Override
    void createWidget(int index) {
        checkOrientation(parent);
        super.createWidget(index);
        ((SwtDecorations) parent.getImpl()).addMenu(this.getApi());
    }

    void fixMenus(Decorations newParent) {
        if (isDisposed()) {
            return;
        }
        MenuItem[] items = getItems();
        for (int i = 0; i < items.length; i++) {
            ((DartMenuItem) items[i].getImpl()).fixMenus(newParent);
        }
        ((SwtDecorations) parent.getImpl()).removeMenu(this.getApi());
        ((SwtDecorations) newParent.getImpl()).addMenu(this.getApi());
        this.parent = newParent;
    }

    /**
     * Returns the default menu item or null if none has
     * been previously set.
     *
     * @return the default menu item.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public MenuItem getDefaultItem() {
        checkWidget();
        return null;
    }

    /**
     * Returns <code>true</code> if the receiver is enabled, and
     * <code>false</code> otherwise. A disabled menu is typically
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
     * Returns the item at the given, zero-relative index in the
     * receiver. Throws an exception if the index is out of range.
     *
     * @param index the index of the item to return
     * @return the item at the given index
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_RANGE - if the index is not between 0 and the number of elements in the list minus 1 (inclusive)</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public MenuItem getItem(int index) {
        checkWidget();
        return this._items[index];
    }

    /**
     * Returns the number of items contained in the receiver.
     *
     * @return the number of items
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public int getItemCount() {
        checkWidget();
        return this._items != null ? this._items.length : 0;
    }

    /**
     * Returns a (possibly empty) array of <code>MenuItem</code>s which
     * are the items in the receiver.
     * <p>
     * Note: This is not the actual structure used by the receiver
     * to maintain its list of items, so modifying the array will
     * not affect the receiver.
     * </p>
     *
     * @return the items in the receiver
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public MenuItem[] getItems() {
        checkWidget();
        if (_items == null)
            return new MenuItem[0];
        return _items;
    }

    @Override
    String getNameText() {
        String result = "";
        MenuItem[] items = getItems();
        int length = items.length;
        if (length > 0) {
            for (int i = 0; i < length - 1; i++) {
                result = result + ((DartMenuItem) items[i].getImpl()).getNameText() + ", ";
            }
            result = result + ((DartMenuItem) items[length - 1].getImpl()).getNameText();
        }
        return result;
    }

    /**
     * Returns the orientation of the receiver, which will be one of the
     * constants <code>SWT.LEFT_TO_RIGHT</code> or <code>SWT.RIGHT_TO_LEFT</code>.
     *
     * @return the orientation style
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.7
     */
    public int getOrientation() {
        checkWidget();
        return getApi().style & (SWT.LEFT_TO_RIGHT | SWT.RIGHT_TO_LEFT);
    }

    /**
     * Returns the receiver's parent, which must be a <code>Decorations</code>.
     *
     * @return the receiver's parent
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public Decorations getParent() {
        checkWidget();
        return parent;
    }

    /**
     * Returns the receiver's parent item, which must be a
     * <code>MenuItem</code> or null when the receiver is a
     * root.
     *
     * @return the receiver's parent item
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public MenuItem getParentItem() {
        checkWidget();
        return cascade;
    }

    /**
     * Returns the receiver's parent item, which must be a
     * <code>Menu</code> or null when the receiver is a
     * root.
     *
     * @return the receiver's parent item
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public Menu getParentMenu() {
        checkWidget();
        if (cascade == null)
            return null;
        return cascade.getParent();
    }

    /**
     * Returns the receiver's shell. For all controls other than
     * shells, this simply returns the control's nearest ancestor
     * shell. Shells return themselves, even if they are children
     * of other shells. Returns null if receiver or its ancestor
     * is the application menubar.
     *
     * @return the receiver's shell or null
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see #getParent
     */
    public Shell getShell() {
        checkWidget();
        return parent.getShell();
    }

    /**
     * Returns <code>true</code> if the receiver is visible, and
     * <code>false</code> otherwise.
     * <p>
     * If one of the receiver's ancestors is not visible or some
     * other condition makes the receiver not visible, this method
     * may still indicate that it is considered visible even though
     * it may not actually be showing.
     * </p>
     *
     * @return the receiver's visibility state
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public boolean getVisible() {
        checkWidget();
        if ((getApi().style & SWT.POP_UP) != 0) {
            Menu[] popups = ((SwtDisplay) display.getImpl()).popups;
            if (popups != null) {
                for (int i = 0; i < popups.length; i++) {
                    if (popups[i] == this.getApi())
                        return true;
                }
            }
        }
        return this.visible;
    }

    @Override
    void hookEvents() {
        super.hookEvents();
    }

    /**
     * Searches the receiver's list starting at the first item
     * (index 0) until an item is found that is equal to the
     * argument, and returns the index of that item. If no item
     * is found, returns -1.
     *
     * @param item the search item
     * @return the index of the item
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the item is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public int indexOf(MenuItem item) {
        checkWidget();
        if (item == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        MenuItem[] items = getItems();
        for (int i = 0; i < items.length; i++) {
            if (items[i] == item)
                return i;
        }
        return -1;
    }

    /**
     * Returns <code>true</code> if the receiver is enabled and all
     * of the receiver's ancestors are enabled, and <code>false</code>
     * otherwise. A disabled menu is typically not selectable from the
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
        checkWidget();
        Menu parentMenu = getParentMenu();
        if (parentMenu == null) {
            return getEnabled() && parent.isEnabled();
        }
        return getEnabled() && parentMenu.isEnabled();
    }

    /**
     * Returns <code>true</code> if the receiver is visible and all
     * of the receiver's ancestors are visible and <code>false</code>
     * otherwise.
     *
     * @return the receiver's visibility state
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see #getVisible
     */
    public boolean isVisible() {
        checkWidget();
        return getVisible();
    }

    @Override
    void releaseChildren(boolean destroy) {
        MenuItem[] items = getItems();
        for (int i = 0; i < items.length; i++) {
            MenuItem item = items[i];
            if (item != null && !item.isDisposed()) {
                item.getImpl().release(false);
            }
        }
        super.releaseChildren(destroy);
    }

    @Override
    void releaseParent() {
        super.releaseParent();
        if (cascade != null)
            cascade.setMenu(null);
        if ((getApi().style & SWT.BAR) != 0 && this.getApi() == ((SwtDecorations) parent.getImpl()).menuBar) {
            parent.setMenuBar(null);
        } else {
            if ((getApi().style & SWT.POP_UP) != 0) {
                ((SwtDisplay) display.getImpl()).removePopup(this.getApi());
            }
        }
    }

    @Override
    void releaseWidget() {
        super.releaseWidget();
        if (parent != null)
            ((SwtDecorations) parent.getImpl()).removeMenu(this.getApi());
        parent = null;
        cascade = null;
    }

    /**
     * Overridden to fix memory leak on GTK3, see bug 573983
     * {@inheritDoc}
     */
    @Override
    void destroyWidget() {
        super.destroyWidget();
        if (menuHandle != 0) {
            menuHandle = 0;
        }
    }

    /**
     * Removes the listener from the collection of listeners who will
     * be notified when the menu events are generated for the control.
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
     * @see MenuListener
     * @see #addMenuListener
     */
    public void removeMenuListener(MenuListener listener) {
        checkWidget();
        if (listener == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (eventTable == null)
            return;
        eventTable.unhook(SWT.Hide, listener);
        eventTable.unhook(SWT.Show, listener);
    }

    void removeAccelerators(long accelGroup) {
        MenuItem[] items = getItems();
        for (int i = 0; i < items.length; i++) {
            MenuItem item = items[i];
            ((DartMenuItem) item.getImpl()).removeAccelerators(accelGroup);
        }
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

    @Override
    void reskinChildren(int flags) {
        MenuItem[] items = getItems();
        for (int i = 0; i < items.length; i++) {
            MenuItem item = items[i];
            item.reskin(flags);
        }
        super.reskinChildren(flags);
    }

    boolean sendHelpEvent(long helpType) {
        if (selectedItem != null && !selectedItem.isDisposed()) {
            if (((DartWidget) selectedItem.getImpl()).hooks(SWT.Help)) {
                ((DartWidget) selectedItem.getImpl()).postEvent(SWT.Help);
                return true;
            }
        }
        if (hooks(SWT.Help)) {
            postEvent(SWT.Help);
            return true;
        }
        return ((DartControl) parent.getImpl()).sendHelpEvent(helpType);
    }

    /**
     * Sets the default menu item to the argument or removes
     * the default emphasis when the argument is <code>null</code>.
     *
     * @param item the default menu item or null
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the menu item has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setDefaultItem(MenuItem item) {
        dirty();
        checkWidget();
        this.defaultItem = item;
    }

    /**
     * Enables the receiver if the argument is <code>true</code>,
     * and disables it otherwise. A disabled menu is typically
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
        this.enabled = enabled;
    }

    /**
     * Sets the location of the receiver, which must be a popup,
     * to the point specified by the arguments which are relative
     * to the display.
     * <p>
     * Note that this is different from most widgets where the
     * location of the widget is relative to the parent.
     * </p><p>
     * Also note that the actual location of the menu is dependent
     * on platform specific behavior. For example: on Linux with
     * Wayland this operation is a hint due to lack of global
     * coordinates.
     * </p>
     *
     * @param x the new x coordinate for the receiver
     * @param y the new y coordinate for the receiver
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setLocation(int x, int y) {
        checkWidget();
        setLocation(new Point(x, y));
    }

    void setLocationInPixels(int x, int y) {
        checkWidget();
        if ((getApi().style & (SWT.BAR | SWT.DROP_DOWN)) != 0)
            return;
        this.x = x;
        this.y = y;
        hasLocation = true;
    }

    /**
     * Sets the location of the receiver, which must be a popup,
     * to the point specified by the argument which is relative
     * to the display.
     * <p>
     * Note that this is different from most widgets where the
     * location of the widget is relative to the parent.
     * </p><p>
     * Note that the platform window manager ultimately has control
     * over the location of popup menus.
     * </p>
     *
     * @param location the new location for the receiver
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the point is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 2.1
     */
    public void setLocation(Point location) {
        dirty();
        checkWidget();
        setLocationInPixels(DPIUtil.autoScaleUp(location));
        this.location = location;
    }

    void setLocationInPixels(Point location) {
        checkWidget();
        if (location == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        setLocationInPixels(location.x, location.y);
    }

    /**
     * Sets the orientation of the receiver, which must be one
     * of the constants <code>SWT.LEFT_TO_RIGHT</code> or <code>SWT.RIGHT_TO_LEFT</code>.
     *
     * @param orientation new orientation style
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.7
     */
    public void setOrientation(int orientation) {
        checkWidget();
        if ((getApi().style & (SWT.BAR | SWT.DROP_DOWN)) != 0)
            return;
        _setOrientation(orientation);
    }

    void _setOrientation(int orientation) {
        dirty();
        int flags = SWT.RIGHT_TO_LEFT | SWT.LEFT_TO_RIGHT;
        if ((orientation & flags) == 0 || (orientation & flags) == flags)
            return;
        getApi().style &= ~flags;
        getApi().style |= orientation & flags;
        setOrientation(false);
    }

    @Override
    void setOrientation(boolean create) {
        dirty();
        if ((getApi().style & SWT.RIGHT_TO_LEFT) != 0 || !create) {
            MenuItem[] items = getItems();
            for (int i = 0; i < items.length; i++) {
                ((DartMenuItem) items[i].getImpl()).setOrientation(create);
            }
        }
    }

    /**
     * Lack of absolute coordinates make Wayland event windows inaccurate.
     * Currently the best approach is to the use the GdkWindow of the mouse
     * pointer. See bug 530059 and 532074.<p>
     *
     * @param eventPtr a pointer to the GdkEvent
     */
    void adjustParentWindowWayland(long eventPtr) {
        return;
    }

    /**
     * Feature in GTK3 on X11: context menus in SWT are populated
     * dynamically, sometimes asynchronously outside of SWT
     * (i.e. in Platform UI). This means that items are added and
     * removed just before the menu is shown. This method of
     * changing the menu content can sometimes cause sizing issues
     * internally in GTK, specifically with the height of the
     * toplevel GdkWindow. <p>
     *
     * The fix is to cache the number of items popped up previously,
     * and if the number of items in the current menu (to be popped up)
     * is different, then:<ul>
     *     <li>get the preferred height of the menu</li>
     *     <li>set the toplevel GdkWindow to that height</li></ul>
     *
     * @param itemCount the current number of items in the menu, just
     * before it's about to be shown/popped-up
     */
    void verifyMenuPosition(int itemCount) {
    }

    /**
     * Marks the receiver as visible if the argument is <code>true</code>,
     * and marks it invisible otherwise.
     * <p>
     * If one of the receiver's ancestors is not visible or some
     * other condition makes the receiver not visible, marking
     * it visible may not actually cause it to be displayed.
     * </p>
     *
     * @param visible the new visibility state
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setVisible(boolean visible) {
        dirty();
        checkWidget();
        if ((getApi().style & (SWT.BAR | SWT.DROP_DOWN)) != 0)
            return;
        if (visible) {
            ((SwtDisplay) display.getImpl()).addPopup(this.getApi());
        } else {
            ((SwtDisplay) display.getImpl()).removePopup(this.getApi());
            _setVisible(false);
        }
        this.visible = visible;
    }

    MenuItem defaultItem;

    boolean enabled = true;

    MenuItem[] _items = new MenuItem[0];

    Point location;

    Menu parentMenu;

    boolean visible = false;

    public int _x() {
        return x;
    }

    public int _y() {
        return y;
    }

    public boolean _hasLocation() {
        return hasLocation;
    }

    public MenuItem _cascade() {
        return cascade;
    }

    public MenuItem _selectedItem() {
        return selectedItem;
    }

    public Decorations _parent() {
        return parent;
    }

    public int _poppedUpCount() {
        return poppedUpCount;
    }

    public long _menuHandle() {
        return menuHandle;
    }

    public long _modelHandle() {
        return modelHandle;
    }

    public long _actionGroup() {
        return actionGroup;
    }

    public long _shortcutController() {
        return shortcutController;
    }

    public MenuItem _defaultItem() {
        return defaultItem;
    }

    public boolean _enabled() {
        return enabled;
    }

    public MenuItem[] _items() {
        return _items;
    }

    public Point _location() {
        return location;
    }

    public Menu _parentMenu() {
        return parentMenu;
    }

    public boolean _visible() {
        return visible;
    }

    Control ownerControl;

    public Control findOwnerControl() {
        if (ownerControl != null) {
            return ownerControl;
        }
        if (cascade != null) {
            Menu parentMenu = ((DartMenuItem) cascade.getImpl()).parent;
            if (parentMenu != null) {
                return ((DartMenu) parentMenu.getImpl()).findOwnerControl();
            }
        }
        return null;
    }

    @Override
    protected void dirty() {
        if (ownerControl != null && ownerControl.getImpl() instanceof DartControl) {
            ((DartControl) ownerControl.getImpl()).dirty();
        } else {
            super.dirty();
        }
    }

    void createItem(MenuItem item, int index) {
        if (_items == null)
            _items = new MenuItem[0];
        MenuItem[] newItems = new MenuItem[_items.length + 1];
        System.arraycopy(_items, 0, newItems, 0, index);
        newItems[index] = item;
        System.arraycopy(_items, index, newItems, index + 1, _items.length - index);
        _items = newItems;
        ((DartWidget) item.getImpl()).createWidget(index);
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
        FlutterBridge.on(this, "Help", "Help", e -> {
            getDisplay().asyncExec(() -> {
                sendEvent(SWT.Help, e);
            });
        });
        FlutterBridge.on(this, "Menu", "Hide", e -> {
            getDisplay().asyncExec(() -> {
                sendEvent(SWT.Hide, e);
            });
        });
        FlutterBridge.on(this, "Menu", "Show", e -> {
            getDisplay().asyncExec(() -> {
                sendEvent(SWT.Show, e);
            });
        });
    }

    public Menu getApi() {
        if (api == null)
            api = Menu.createApi(this);
        return (Menu) api;
    }

    public VMenu getValue() {
        if (value == null)
            value = new VMenu(this);
        return (VMenu) value;
    }
}
