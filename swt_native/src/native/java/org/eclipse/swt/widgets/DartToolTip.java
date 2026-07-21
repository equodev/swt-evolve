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
import java.util.Objects;
import dev.equo.swt.*;

/**
 * Instances of this class represent popup windows that are used
 * to inform or warn the user.
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>BALLOON, ICON_ERROR, ICON_INFORMATION, ICON_WARNING</dd>
 * <dt><b>Events:</b></dt>
 * <dd>Selection</dd>
 * </dl>
 * <p>
 * Note: Only one of the styles ICON_ERROR, ICON_INFORMATION,
 * and ICON_WARNING may be specified.
 * </p><p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 *
 * @see <a href="https://eclipse.dev/eclipse/swt/snippets/#tooltips">Tool Tips snippets</a>
 * @see <a href="https://eclipse.dev/eclipse/swt/examples.html">SWT Example: ControlExample</a>
 * @see <a href="https://eclipse.dev/eclipse/swt/">Sample code and further information</a>
 *
 * @since 3.2
 * @noextend This class is not intended to be subclassed by clients.
 */
public class DartToolTip extends DartWidget implements IToolTip {

    Shell parent;

    TrayItem item;

    int x, y;

    int[] borderPolygon;

    boolean spikeAbove, autohide;

    Listener listener, parentListener;

    TextLayout layoutText, layoutMessage;

    Region region;

    Font boldFont;

    Runnable runnable;

    static final int BORDER = 5;

    static final int PADDING = 5;

    static final int INSET = 4;

    static final int TIP_HEIGHT = 20;

    static final int IMAGE_SIZE = 16;

    static final int DELAY = 10000;

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
     *    <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
     * </ul>
     *
     * @see SWT#BALLOON
     * @see SWT#ICON_ERROR
     * @see SWT#ICON_INFORMATION
     * @see SWT#ICON_WARNING
     * @see Widget#checkSubclass
     * @see Widget#getStyle
     */
    public DartToolTip(Shell parent, int style, ToolTip api) {
        super(parent, checkStyle(style), api);
        this.parent = parent;
        createWidget();
    }

    static int checkStyle(int style) {
        int mask = SWT.ICON_ERROR | SWT.ICON_INFORMATION | SWT.ICON_WARNING;
        if ((style & mask) == 0)
            return style;
        return checkBits(style, SWT.ICON_INFORMATION, SWT.ICON_WARNING, SWT.ICON_ERROR, 0, 0, 0);
    }

    /**
     * Adds the listener to the collection of listeners who will
     * be notified when the receiver is selected by the user, by sending
     * it one of the messages defined in the <code>SelectionListener</code>
     * interface.
     * <p>
     * <code>widgetSelected</code> is called when the receiver is selected.
     * <code>widgetDefaultSelected</code> is not called.
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

    /**
     * Returns <code>true</code> if the receiver is automatically
     * hidden by the platform, and <code>false</code> otherwise.
     *
     * @return the receiver's auto hide state
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public boolean getAutoHide() {
        checkWidget();
        return autohide;
    }

    Point getSize(int maxWidth) {
        int textWidth = 0, messageWidth = 0;
        if (layoutText != null) {
            layoutText.setWidth(-1);
            textWidth = layoutText.getBounds().width;
        }
        if (layoutMessage != null) {
            layoutMessage.setWidth(-1);
            messageWidth = layoutMessage.getBounds().width;
        }
        int messageTrim = 2 * INSET + 2 * BORDER + 2 * PADDING;
        boolean hasImage = layoutText != null && (getApi().style & SWT.BALLOON) != 0 && (getApi().style & (SWT.ICON_ERROR | SWT.ICON_INFORMATION | SWT.ICON_WARNING)) != 0;
        int textTrim = messageTrim + (hasImage ? IMAGE_SIZE : 0);
        int width = Math.min(maxWidth, Math.max(textWidth + textTrim, messageWidth + messageTrim));
        int textHeight = 0, messageHeight = 0;
        if (layoutText != null) {
            layoutText.setWidth(maxWidth - textTrim);
            textHeight = layoutText.getBounds().height;
        }
        if (layoutMessage != null) {
            layoutMessage.setWidth(maxWidth - messageTrim);
            messageHeight = layoutMessage.getBounds().height;
        }
        int height = 2 * BORDER + 2 * PADDING + messageHeight;
        if (layoutText != null)
            height += Math.max(IMAGE_SIZE, textHeight) + 2 * PADDING;
        return new Point(width, height);
    }

    /**
     * Returns the receiver's message, which will be an empty
     * string if it has never been set.
     *
     * @return the receiver's message
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public String getMessage() {
        checkWidget();
        return layoutMessage != null ? layoutMessage.getText() : "";
    }

    /**
     * Returns the receiver's parent, which must be a <code>Shell</code>.
     *
     * @return the receiver's parent
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public Shell getParent() {
        checkWidget();
        return parent;
    }

    /**
     * Returns the receiver's text, which will be an empty
     * string if it has never been set.
     *
     * @return the receiver's text
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public String getText() {
        checkWidget();
        return layoutText != null ? layoutText.getText() : "";
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
        return visible;
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

    void onDispose(Event event) {
        Control parent = getParent();
        parent.removeListener(SWT.Dispose, parentListener);
        removeListener(SWT.Dispose, listener);
        notifyListeners(SWT.Dispose, event);
        event.type = SWT.None;
        if (runnable != null) {
            Display display = getDisplay();
            display.timerExec(-1, runnable);
        }
        runnable = null;
        ;
        ;
        if (region != null)
            region.dispose();
        region = null;
        if (layoutText != null)
            layoutText.dispose();
        layoutText = null;
        if (layoutMessage != null)
            layoutMessage.dispose();
        layoutMessage = null;
        if (boldFont != null)
            boldFont.dispose();
        boldFont = null;
        borderPolygon = null;
    }

    void onMouseDown(Event event) {
        sendSelectionEvent(SWT.Selection, null, true);
        setVisible(false);
    }

    void onPaint(Event event) {
        int x = BORDER + PADDING;
        int y = BORDER + PADDING;
        if ((getApi().style & SWT.BALLOON) != 0) {
            if (spikeAbove)
                y += TIP_HEIGHT;
        } else {
        }
        if (layoutText != null) {
            x += INSET;
            y += 2 * PADDING + Math.max(IMAGE_SIZE, layoutText.getBounds().height);
        }
        if (layoutMessage != null) {
            x = BORDER + PADDING + INSET;
        }
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
     * Makes the receiver hide automatically when <code>true</code>,
     * and remain visible when <code>false</code>.
     *
     * @param autoHide the auto hide state
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see #getVisible
     * @see #setVisible
     */
    public void setAutoHide(boolean autoHide) {
        checkWidget();
        if (!java.util.Objects.equals(this.autohide, autoHide)) {
            dirty();
        }
        this.autohide = autoHide;
        //TODO - update when visible
    }

    /**
     * Sets the location of the receiver, which must be a tooltip,
     * to the point specified by the arguments which are relative
     * to the display.
     * <p>
     * Note that this is different from most widgets where the
     * location of the widget is relative to the parent.
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
        dirty();
        Point newValue = new Point(x, y);
        checkWidget();
        if (this.x == x && this.y == y)
            return;
        this.x = x;
        this.y = y;
        this.location = newValue;
        ;
        dirty();
        if (display != null) {
            ((DartDisplay) display.getImpl())._addActiveTooltip(this.getApi());
        }
    }

    /**
     * Sets the location of the receiver, which must be a tooltip,
     * to the point specified by the argument which is relative
     * to the display.
     * <p>
     * Note that this is different from most widgets where the
     * location of the widget is relative to the parent.
     * </p><p>
     * Note that the platform window manager ultimately has control
     * over the location of tooltips.
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
     */
    public void setLocation(Point location) {
        checkWidget();
        if (location == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        setLocation(location.x, location.y);
    }

    /**
     * Sets the receiver's message.
     *
     * @param string the new message
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the text is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setMessage(String string) {
        String newValue = string;
        if (!java.util.Objects.equals(this.message, newValue)) {
            dirty();
        }
        checkWidget();
        if (string == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (layoutMessage != null)
            layoutMessage.dispose();
        layoutMessage = null;
        if (string.length() != 0) {
            Display display = getDisplay();
            layoutMessage = new TextLayout(display);
            layoutMessage.setText(string);
        }
        this.message = newValue;
        ;
    }

    /**
     * Sets the receiver's text.
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
    public void setText(String string) {
        String newValue = string;
        if (!java.util.Objects.equals(this.text, newValue)) {
            dirty();
        }
        checkWidget();
        if (string == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (layoutText != null)
            layoutText.dispose();
        layoutText = null;
        if (boldFont != null)
            boldFont.dispose();
        boldFont = null;
        if (string.length() != 0) {
            Display display = getDisplay();
            layoutText = new TextLayout(display);
            layoutText.setText(string);
            Font font = display.getSystemFont();
            FontData data = font.getFontData()[0];
            boldFont = new Font(display, data.getName(), data.getHeight(), SWT.BOLD);
            TextStyle style = new TextStyle(boldFont, null, null);
            layoutText.setStyle(style, 0, string.length());
        }
        this.text = newValue;
        ;
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
        boolean newValue = visible;
        if (!java.util.Objects.equals(this.visible, newValue)) {
            dirty();
        }
        checkWidget();
        ;
        ;
        Display display = getDisplay();
        if (runnable != null)
            display.timerExec(-1, runnable);
        runnable = null;
        this.visible = newValue;
        if (autohide && visible) {
            runnable = () -> {
                if (!isDisposed())
                    setVisible(false);
            };
            display.timerExec(DELAY, runnable);
        }
        dirty();
        if (display != null) {
            ((DartDisplay) display.getImpl())._addActiveTooltip(this.getApi());
        }
    }

    Point location;

    String message;

    String text = "";

    boolean visible;

    public Shell _parent() {
        return parent;
    }

    public TrayItem _item() {
        return item;
    }

    public int _x() {
        return x;
    }

    public int _y() {
        return y;
    }

    public int[] _borderPolygon() {
        return borderPolygon;
    }

    public boolean _spikeAbove() {
        return spikeAbove;
    }

    public boolean _autohide() {
        return autohide;
    }

    public Listener _listener() {
        return listener;
    }

    public Listener _parentListener() {
        return parentListener;
    }

    public TextLayout _layoutText() {
        return layoutText;
    }

    public TextLayout _layoutMessage() {
        return layoutMessage;
    }

    public Region _region() {
        return region;
    }

    public Font _boldFont() {
        return boldFont;
    }

    public Runnable _runnable() {
        return runnable;
    }

    public Point _location() {
        return location;
    }

    public String _message() {
        return message;
    }

    public String _text() {
        return text;
    }

    public boolean _visible() {
        return visible;
    }

    void createWidget() {
        super.createWidget();
        text = "";
        message = "";
        x = y = -1;
        autohide = true;
    }

    @Override
    protected void releaseWidget() {
        if (display != null && !display.isDisposed()) {
            ((DartDisplay) display.getImpl())._removeActiveTooltip(this.getApi());
        }
        super.releaseWidget();
    }

    public FlutterBridge getBridge() {
        if (bridge != null)
            return bridge;
        Composite p = parent;
        while (p != null && !(p.getImpl() instanceof DartWidget)) p = p.getImpl()._parent();
        if (p != null)
            return ((DartWidget) p.getImpl()).getBridge();
        Display display = getDisplay();
        if (display != null && display.getImpl() instanceof DartDisplay dd)
            return dd.getDisplayBridge();
        return null;
    }

    protected void _hookEvents() {
        super._hookEvents();
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
                setVisible(false);
                sendEvent(SWT.Selection, e);
            });
        });
    }

    public ToolTip getApi() {
        if (api == null)
            api = ToolTip.createApi(this);
        return (ToolTip) api;
    }

    public VToolTip getValue() {
        if (value == null)
            value = new VToolTip(this);
        return (VToolTip) value;
    }
}
