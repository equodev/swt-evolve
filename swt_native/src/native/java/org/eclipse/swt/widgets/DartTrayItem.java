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

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import java.util.Objects;
import dev.equo.swt.*;

/**
 * Instances of this class represent icons that can be placed on the
 * system tray or task bar status area.
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>(none)</dd>
 * <dt><b>Events:</b></dt>
 * <dd>DefaultSelection, MenuDetect, Selection</dd>
 * </dl>
 * <p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 *
 * @see <a href="http://www.eclipse.org/swt/snippets/#tray">Tray, TrayItem snippets</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 *
 * @since 3.0
 * @noextend This class is not intended to be subclassed by clients.
 */
public class DartTrayItem extends DartItem implements ITrayItem {

    Tray parent;

    ToolTip toolTip;

    String toolTipText;

    boolean visible = true, highlight;

    Image highlightImage;

    /**
     * Constructs a new instance of this class given its parent
     * (which must be a <code>Tray</code>) and a style value
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
    public DartTrayItem(Tray parent, int style, TrayItem api) {
        super(parent, style, api);
        this.parent = parent;
        ((DartTray) parent.getImpl()).createItem(this.getApi(), parent.getItemCount());
        createWidget();
    }

    /**
     * Adds the listener to the collection of listeners who will
     * be notified when the platform-specific context menu trigger
     * has occurred, by sending it one of the messages defined in
     * the <code>MenuDetectListener</code> interface.
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
     * @see MenuDetectListener
     * @see #removeMenuDetectListener
     *
     * @since 3.3
     */
    public void addMenuDetectListener(MenuDetectListener listener) {
        addTypedListener(listener, SWT.MenuDetect);
    }

    /**
     * Adds the listener to the collection of listeners who will
     * be notified when the receiver is selected by the user, by sending
     * it one of the messages defined in the <code>SelectionListener</code>
     * interface.
     * <p>
     * <code>widgetSelected</code> is called when the receiver is selected
     * <code>widgetDefaultSelected</code> is called when the receiver is double-clicked
     * </p>
     *
     * @param listener the listener which should be notified when the receiver is selected by the user
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

    @Override
    void createHandle() {
    }

    @Override
    void deregister() {
        super.deregister();
    }

    @Override
    void destroyWidget() {
        ((DartTray) parent.getImpl()).destroyItem(this.getApi());
        releaseHandle();
    }

    /**
     * Returns the receiver's highlight image if it has one, or null
     * if it does not.
     *
     * @return the receiver's highlight image
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.8
     */
    public Image getHighlightImage() {
        checkWidget();
        return highlightImage;
    }

    Point getLocation() {
        return null;
    }

    /**
     * Returns the receiver's parent, which must be a <code>Tray</code>.
     *
     * @return the receiver's parent
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.2
     */
    public Tray getParent() {
        checkWidget();
        return parent;
    }

    /**
     * Returns the receiver's tool tip, or null if it has
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
    public ToolTip getToolTip() {
        checkWidget();
        return toolTip;
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

    /**
     * Returns <code>true</code> if the receiver is visible and
     * <code>false</code> otherwise.
     *
     * @return the receiver's visibility
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public boolean getVisible() {
        checkWidget();
        return visible;
    }

    @Override
    void register() {
        super.register();
    }

    @Override
    void releaseHandle() {
        super.releaseHandle();
        parent = null;
    }

    @Override
    void releaseWidget() {
        super.releaseWidget();
        toolTip = null;
        toolTipText = null;
        highlightImage = null;
    }

    /**
     * Removes the listener from the collection of listeners who will
     * be notified when the platform-specific context menu trigger has
     * occurred.
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
     * @see MenuDetectListener
     * @see #addMenuDetectListener
     *
     * @since 3.3
     */
    public void removeMenuDetectListener(MenuDetectListener listener) {
        checkWidget();
        if (listener == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (eventTable == null)
            return;
        eventTable.unhook(SWT.MenuDetect, listener);
    }

    /**
     * Removes the listener from the collection of listeners who will
     * be notified when the receiver is selected by the user.
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
     * Sets the receiver's image.
     *
     * @param image the new image
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the image has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    @Override
    public void setImage(Image image) {
        dirty();
        checkWidget();
        if (image != null && image.isDisposed())
            error(SWT.ERROR_INVALID_ARGUMENT);
        super.setImage(image);
        updateImage();
    }

    /**
     * Sets the receiver's highlight image.
     *
     * @param image the new highlight image
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the image has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.8
     */
    public void setHighlightImage(Image image) {
        image = GraphicsUtils.copyImage(getDisplay(), image);
        checkWidget();
        if (!java.util.Objects.equals(this.highlightImage, image)) {
            dirty();
        }
        if (image != null && image.isDisposed())
            error(SWT.ERROR_INVALID_ARGUMENT);
        highlightImage = image;
        updateImage();
    }

    /**
     * Sets the receiver's tool tip to the argument, which
     * may be null indicating that no tool tip should be shown.
     *
     * @param toolTip the new tool tip (or null)
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.2
     */
    public void setToolTip(ToolTip toolTip) {
        checkWidget();
        if (!java.util.Objects.equals(this.toolTip, toolTip)) {
            dirty();
        }
        ToolTip newTip = toolTip;
        this.toolTip = newTip;
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
        _setToolTipText(string);
    }

    void _setToolTipText(String string) {
        if (string != null) {
            char[] chars = new char[string.length()];
            string.getChars(0, chars.length, chars, 0);
        } else {
        }
    }

    /**
     * Makes the receiver visible if the argument is <code>true</code>,
     * and makes it invisible otherwise.
     *
     * @param visible the new visibility state
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setVisible(boolean visible) {
        checkWidget();
        if (!java.util.Objects.equals(this.visible, visible)) {
            dirty();
        }
        if (this.visible == visible)
            return;
        if (visible) {
            sendEvent(SWT.Show);
            if (isDisposed())
                return;
        }
        this.visible = visible;
        updateImage();
        if (!visible)
            sendEvent(SWT.Hide);
    }

    void showMenu(Menu menu) {
        ((DartDisplay) display.getImpl()).trayItemMenu = menu;
    }

    void showMenu() {
        _setToolTipText(null);
        Display display = this.display;
        ((DartDisplay) display.getImpl()).currentTrayItem = this.getApi();
        sendEvent(SWT.MenuDetect);
        if (!isDisposed())
            ((DartDisplay) display.getImpl()).runPopups();
        ((DartDisplay) display.getImpl()).currentTrayItem = null;
        if (isDisposed())
            return;
        _setToolTipText(toolTipText);
    }

    void displayMenu() {
        if (highlight) {
            ((DartDisplay) display.getImpl()).trayItemMenu = null;
            showMenu();
            if (((DartDisplay) display.getImpl()).trayItemMenu != null) {
                ((DartDisplay) display.getImpl()).trayItemMenu = null;
                highlight = false;
            }
        }
    }

    void updateImage() {
        Image image = this.image;
        if (highlight && highlightImage != null)
            image = highlightImage;
        if (image == null) {
        } else {
            if (visible) {
            }
        }
    }

    public Tray _parent() {
        return parent;
    }

    public ToolTip _toolTip() {
        return toolTip;
    }

    public String _toolTipText() {
        return toolTipText;
    }

    public boolean _visible() {
        return visible;
    }

    public boolean _highlight() {
        return highlight;
    }

    public Image _highlightImage() {
        return highlightImage;
    }

    protected void _hookEvents() {
        super._hookEvents();
        FlutterBridge.on(this, "MenuDetect", "MenuDetect", e -> {
            getDisplay().asyncExec(() -> {
                if (isDisposed())
                    return;
                sendEvent(SWT.MenuDetect, e);
            });
        });
        FlutterBridge.on(this, "Selection", "DefaultSelection", e -> {
            getDisplay().asyncExec(() -> {
                if (isDisposed())
                    return;
                sendEvent(SWT.DefaultSelection, e);
            });
        });
        FlutterBridge.on(this, "Selection", "Selection", e -> {
            getDisplay().asyncExec(() -> {
                if (isDisposed())
                    return;
                sendEvent(SWT.Selection, e);
            });
        });
    }

    public TrayItem getApi() {
        if (api == null)
            api = TrayItem.createApi(this);
        return (TrayItem) api;
    }

    public VTrayItem getValue() {
        if (value == null)
            value = new VTrayItem(this);
        return (VTrayItem) value;
    }
}
