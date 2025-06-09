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
import java.util.stream.Stream;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.internal.*;
import org.eclipse.swt.internal.gtk.*;
import org.eclipse.swt.internal.gtk3.*;
import org.eclipse.swt.internal.gtk4.*;

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
public class Menu extends Widget {

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
    public Menu(Control parent) {
        this((IMenu) null);
        if (parent.delegate instanceof SWTControl) {
            delegate = new SWTMenu((SWTControl) parent.delegate);
        } else {
            delegate = new IMenu() {
                @Override
                public void addTypedListener(EventListener listener, int... eventTypes) {
                }

                @Override
                public void checkWidget() {
                }

                @Override
                public long getHandle() {
                    return 0;
                }

                @Override
                public long topHandle() {
                    return 0;
                }

                @Override
                public void addListener(int eventType, Listener listener) {
                }

                @Override
                public void addDisposeListener(DisposeListener listener) {
                }

                @Override
                public void dispose() {
                }

                @Override
                public Object getData() {
                    return null;
                }

                @Override
                public Object getData(String key) {
                    return null;
                }

                @Override
                public IDisplay getDisplay() {
                    return null;
                }

                @Override
                public Listener[] getListeners(int eventType) {
                    return null;
                }

                @Override
                public <L extends EventListener> Stream<L> getTypedListeners(int eventType, Class<L> listenerType) {
                    return null;
                }

                @Override
                public int getStyle() {
                    return 0;
                }

                @Override
                public boolean isAutoDirection() {
                    return false;
                }

                @Override
                public boolean isDisposed() {
                    return false;
                }

                @Override
                public boolean isListening(int eventType) {
                    return false;
                }

                @Override
                public void notifyListeners(int eventType, Event event) {
                }

                @Override
                public void removeListener(int eventType, Listener listener) {
                }

                @Override
                public void reskin(int flags) {
                }

                @Override
                public void removeDisposeListener(DisposeListener listener) {
                }

                @Override
                public void setData(Object data) {
                }

                @Override
                public void setData(String key, Object value) {
                }

                @Override
                public void addMenuListener(MenuListener listener) {
                }

                @Override
                public void addHelpListener(HelpListener listener) {
                }

                @Override
                public IMenuItem getDefaultItem() {
                    return null;
                }

                @Override
                public boolean getEnabled() {
                    return false;
                }

                @Override
                public IMenuItem getItem(int index) {
                    return null;
                }

                @Override
                public int getItemCount() {
                    return 0;
                }

                @Override
                public IMenuItem[] getItems() {
                    return null;
                }

                @Override
                public int getOrientation() {
                    return 0;
                }

                @Override
                public IDecorations getParent() {
                    return null;
                }

                @Override
                public IMenuItem getParentItem() {
                    return null;
                }

                @Override
                public IMenu getParentMenu() {
                    return null;
                }

                @Override
                public IShell getShell() {
                    return null;
                }

                @Override
                public boolean getVisible() {
                    return false;
                }

                @Override
                public int indexOf(IMenuItem item) {
                    return 0;
                }

                @Override
                public boolean isEnabled() {
                    return false;
                }

                @Override
                public boolean isVisible() {
                    return false;
                }

                @Override
                public void removeMenuListener(MenuListener listener) {
                }

                @Override
                public void removeHelpListener(HelpListener listener) {
                }

                @Override
                public void setDefaultItem(IMenuItem item) {
                }

                @Override
                public void setEnabled(boolean enabled) {
                }

                @Override
                public void setLocation(int x, int y) {
                }

                @Override
                public void setLocation(Point location) {
                }

                @Override
                public void setOrientation(int orientation) {
                }

                @Override
                public void setVisible(boolean visible) {
                }
            };
        }
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
    public Menu(Decorations parent, int style) {
        this(new SWTMenu((SWTDecorations) parent.delegate, style));
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
    public Menu(Menu parentMenu) {
        this((IMenu) null);
        if (parentMenu.delegate instanceof SWTMenu) {
            delegate = new SWTMenu((SWTMenu) parentMenu.delegate);
        } else {
            delegate = new IMenu() {

                @Override
                public void addTypedListener(EventListener listener, int... eventTypes) {
                }

                @Override
                public void checkWidget() {
                }

                @Override
                public long getHandle() {
                    return 0;
                }

                @Override
                public long topHandle() {
                    return 0;
                }

                @Override
                public void addListener(int eventType, Listener listener) {
                }

                @Override
                public void addDisposeListener(DisposeListener listener) {
                }

                @Override
                public void dispose() {
                }

                @Override
                public Object getData() {
                    return null;
                }

                @Override
                public Object getData(String key) {
                    return null;
                }

                @Override
                public IDisplay getDisplay() {
                    return null;
                }

                @Override
                public Listener[] getListeners(int eventType) {
                    return null;
                }

                @Override
                public <L extends EventListener> Stream<L> getTypedListeners(int eventType, Class<L> listenerType) {
                    return null;
                }

                @Override
                public int getStyle() {
                    return 0;
                }

                @Override
                public boolean isAutoDirection() {
                    return false;
                }

                @Override
                public boolean isDisposed() {
                    return false;
                }

                @Override
                public boolean isListening(int eventType) {
                    return false;
                }

                @Override
                public void notifyListeners(int eventType, Event event) {
                }

                @Override
                public void removeListener(int eventType, Listener listener) {
                }

                @Override
                public void reskin(int flags) {
                }

                @Override
                public void removeDisposeListener(DisposeListener listener) {
                }

                @Override
                public void setData(Object data) {
                }

                @Override
                public void setData(String key, Object value) {
                }

                @Override
                public void addMenuListener(MenuListener listener) {
                }

                @Override
                public void addHelpListener(HelpListener listener) {
                }

                @Override
                public IMenuItem getDefaultItem() {
                    return null;
                }

                @Override
                public boolean getEnabled() {
                    return false;
                }

                @Override
                public IMenuItem getItem(int index) {
                    return null;
                }

                @Override
                public int getItemCount() {
                    return 0;
                }

                @Override
                public IMenuItem[] getItems() {
                    return null;
                }

                @Override
                public int getOrientation() {
                    return 0;
                }

                @Override
                public IDecorations getParent() {
                    return null;
                }

                @Override
                public IMenuItem getParentItem() {
                    return null;
                }

                @Override
                public IMenu getParentMenu() {
                    return null;
                }

                @Override
                public IShell getShell() {
                    return null;
                }

                @Override
                public boolean getVisible() {
                    return false;
                }

                @Override
                public int indexOf(IMenuItem item) {
                    return 0;
                }

                @Override
                public boolean isEnabled() {
                    return false;
                }

                @Override
                public boolean isVisible() {
                    return false;
                }

                @Override
                public void removeMenuListener(MenuListener listener) {
                }

                @Override
                public void removeHelpListener(HelpListener listener) {
                }

                @Override
                public void setDefaultItem(IMenuItem item) {
                }

                @Override
                public void setEnabled(boolean enabled) {
                }

                @Override
                public void setLocation(int x, int y) {
                }

                @Override
                public void setLocation(Point location) {
                }

                @Override
                public void setOrientation(int orientation) {
                }

                @Override
                public void setVisible(boolean visible) {
                }
            };
        }
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
    public Menu(MenuItem parentItem) {
        this(new SWTMenu((SWTMenuItem) parentItem.delegate));
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
        ((IMenu) this.delegate).addMenuListener(listener);
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
        ((IMenu) this.delegate).addHelpListener(listener);
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
        return MenuItem.getInstance(((IMenu) this.delegate).getDefaultItem());
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
        return ((IMenu) this.delegate).getEnabled();
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
        return MenuItem.getInstance(((IMenu) this.delegate).getItem(index));
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
        return ((IMenu) this.delegate).getItemCount();
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
        return MenuItem.ofArray(((IMenu) this.delegate).getItems(), MenuItem.class);
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
        return ((IMenu) this.delegate).getOrientation();
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
        return Decorations.getInstance(((IMenu) this.delegate).getParent());
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
        return MenuItem.getInstance(((IMenu) this.delegate).getParentItem());
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
        return Menu.getInstance(((IMenu) this.delegate).getParentMenu());
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
        return Shell.getInstance(((IMenu) this.delegate).getShell());
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
        return ((IMenu) this.delegate).getVisible();
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
        return ((IMenu) this.delegate).indexOf((IMenuItem) item.delegate);
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
        return ((IMenu) this.delegate).isEnabled();
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
        return ((IMenu) this.delegate).isVisible();
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
        ((IMenu) this.delegate).removeMenuListener(listener);
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
        ((IMenu) this.delegate).removeHelpListener(listener);
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
        ((IMenu) this.delegate).setDefaultItem((IMenuItem) item.delegate);
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
        ((IMenu) this.delegate).setEnabled(enabled);
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
        ((IMenu) this.delegate).setLocation(x, y);
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
        ((IMenu) this.delegate).setLocation(location);
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
        ((IMenu) this.delegate).setOrientation(orientation);
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
        ((IMenu) this.delegate).setVisible(visible);
    }

    protected Menu(IMenu delegate) {
        super(delegate);
        this.delegate = delegate;
        INSTANCES.put(delegate, this);
    }

    public static Menu getInstance(IMenu delegate) {
        if (delegate == null) {
            return null;
        }
        Menu ref = (Menu) INSTANCES.get(delegate);
        if (ref == null) {
            ref = new Menu(delegate);
        }
        return ref;
    }
}
