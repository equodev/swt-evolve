package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.ToolitemSwtSizingConstants;
import org.eclipse.swt.values.ToolItemValue;

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
public class FlutterToolItem extends FlutterItem implements IToolItem {

    FlutterToolBar parent;

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
    public FlutterToolItem(IToolBar parent, int style) {
        super(parent, checkStyle(style));
        this.parent = (FlutterToolBar) parent;
        createWidget(parent.getItemCount());
        if (parent instanceof FlutterControl flutterParent) {
            flutterParent.currentSize = null;
        }
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
    public FlutterToolItem(IToolBar parent, int style, int index) {
        super(parent, checkStyle(style));
        this.parent = (FlutterToolBar) parent;
        int count = parent.getItemCount();
        if (!(0 <= index && index <= count)) {
            error(SWT.ERROR_INVALID_RANGE);
        }
        createWidget(index);
        if (parent instanceof FlutterControl flutterParent) {
            flutterParent.currentSize = null;
        }
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
        checkWidget();
        if (listener == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        TypedListener typedListener = new TypedListener(listener);
        addListener(SWT.Selection, typedListener);
        addListener(SWT.DefaultSelection, typedListener);
    }

    static int checkStyle(int style) {
        return checkBits(style, SWT.PUSH, SWT.CHECK, SWT.RADIO, SWT.SEPARATOR, SWT.DROP_DOWN, 0);
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
        return null;
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
        return null;
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
    public IControl getControl() {
        return parent;
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
        return null;
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
        return builder().getEnabled().orElse(false);
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
        return null;
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
        return null;
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
        return null;
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
    public IToolBar getParent() {
        checkWidget();
        if (parent == null)
            error(SWT.ERROR_WIDGET_DISPOSED);
        return parent;
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
        return builder().getSelection().orElse(false);
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
        return builder().getToolTipText().orElse(null);
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
        return builder().getWidth().orElse(0);
    }

    @Override
    protected void hookEvents() {
        super.hookEvents();
        String ev = FlutterSwt.getEvent(this);
        FlutterSwt.CLIENT.getComm().on(ev + "/Selection/Selection", p -> {
            System.out.println(ev + "/Selection/Selection event");
            display.asyncExec(() -> {
                sendEvent(SWT.Selection);
            });
        });
        FlutterSwt.CLIENT.getComm().on(ev + "/Selection/DefaultSelection", p -> {
            System.out.println(ev + "/Selection/DefaultSelection event");
            display.asyncExec(() -> {
                sendEvent(SWT.DefaultSelection);
            });
        });
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
        return false;
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
    public void setControl(IControl control) {
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
        builder().setEnabled(enabled);
        FlutterSwt.dirty(this);
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
    }

    @Override
    public void setImage(Image image) {
        if (image != null) {
            ImageData data = image.getImageData();
            String imageFile = data.getFilename();
            if (imageFile == null) {
                if (data.alphaData != null && data.data.length == data.alphaData.length * 4) {
                    for (int i = 0; i < data.alphaData.length; ++i) {
                        data.data[i * 4] = data.alphaData[i];
                    }
                }
//                builder().setImageData(data.data);
            } else
            if (imageFile != null) {
                builder().setImage(imageFile);
            }
            FlutterSwt.dirty(this);
        }
    }

    boolean setRadioSelection(boolean value) {
        // Not Generated
        if ((style & SWT.RADIO) == 0)
            return false;
        if (getSelection() != value) {
            setSelection(value);
            //sendSelectionEvent(SWT.Selection);
        }
        return true;
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
        builder().setSelection(selected);
        FlutterSwt.dirty(this);
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
        // Not Generated
        builder().setText(string);
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
        builder().setToolTipText(string);
        FlutterSwt.dirty(this);
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
        builder().setWidth(width);
        FlutterSwt.dirty(this);
    }

    @Override
    public Point computeSize() {
        checkWidget();
        int width = 0;
        int height = 0;

        // --- Parameter initializations
        boolean hasText = getText() != null && !getText().isEmpty();
        double textLength = hasText ? getText().length() : 0.0;
        boolean hasImage = getImage() != null;

        // --- Style-based calculations ---
        // Note for Push: Standard push button with optional text and image. For icon-only buttons, a circular container with padding is used.
        if ((SWT.PUSH != 0 && (style & SWT.PUSH) == SWT.PUSH)) {
            width = (int) (Math.max(ToolitemSwtSizingConstants.PUSH_BUTTON_MIN_WIDTH, (hasText ? (textLength * ToolitemSwtSizingConstants.AVERAGE_CHAR_WIDTH) : 0.0) + (hasImage ? (ToolitemSwtSizingConstants.SMALL_ICON_SIZE + (hasText ? ToolitemSwtSizingConstants.ICON_SPACING : 0.0)) : 0.0) + (2 * ToolitemSwtSizingConstants.HORIZONTAL_PADDING)));
            height = (int) (ToolitemSwtSizingConstants.PUSH_BUTTON_HEIGHT);
        }
//        // Note for PushIconOnly: Icon-only push button uses a circular container with the icon centered.
//        else if ((SWT.PUSHICONONLY != 0 && (style & SWT.PUSHICONONLY) == SWT.PUSHICONONLY)) {
//            width = (int) (ToolitemSwtSizingConstants.ICON_BUTTON_SIZE);
//            height = (int) (ToolitemSwtSizingConstants.ICON_BUTTON_SIZE);
//        }
        // Note for Check: Checkbox with optional text label. The checkbox itself has a fixed size.
        else if ((SWT.CHECK != 0 && (style & SWT.CHECK) == SWT.CHECK)) {
            width = (int) (ToolitemSwtSizingConstants.CHECK_BOX_SIZE + (hasText ? (ToolitemSwtSizingConstants.CHECK_BOX_TEXT_SPACING + (textLength * ToolitemSwtSizingConstants.AVERAGE_CHAR_WIDTH)) : 0.0) + (2 * ToolitemSwtSizingConstants.CHECK_BOX_HORIZONTAL_PADDING));
            height = (int) (Math.max(ToolitemSwtSizingConstants.CHECK_BOX_SIZE, ToolitemSwtSizingConstants.DEFAULT_FONT_SIZE) + (2 * ToolitemSwtSizingConstants.CHECK_BOX_VERTICAL_PADDING));
        }
        // Note for Radio: Radio button with optional text label. The radio button itself has a fixed size.
        else if ((SWT.RADIO != 0 && (style & SWT.RADIO) == SWT.RADIO)) {
            width = (int) (ToolitemSwtSizingConstants.RADIO_BUTTON_SIZE + (hasText ? (ToolitemSwtSizingConstants.RADIO_BUTTON_TEXT_SPACING + (textLength * ToolitemSwtSizingConstants.AVERAGE_CHAR_WIDTH)) : 0.0) + (2 * ToolitemSwtSizingConstants.RADIO_BUTTON_HORIZONTAL_PADDING));
            height = (int) (Math.max(ToolitemSwtSizingConstants.RADIO_BUTTON_SIZE, ToolitemSwtSizingConstants.DEFAULT_FONT_SIZE) + (2 * ToolitemSwtSizingConstants.RADIO_BUTTON_VERTICAL_PADDING));
        }
        // Note for DropDown: Dropdown button with text and a dropdown arrow icon. Has a fixed height.
        else if ((SWT.DROP_DOWN != 0 && (style & SWT.DROP_DOWN) == SWT.DROP_DOWN)) {
            width = (int) (Math.max(ToolitemSwtSizingConstants.DROPDOWN_BUTTON_MIN_WIDTH, (textLength * ToolitemSwtSizingConstants.AVERAGE_CHAR_WIDTH) + ToolitemSwtSizingConstants.DROPDOWN_ARROW_SIZE + ToolitemSwtSizingConstants.DROPDOWN_ARROW_SPACING + (2 * ToolitemSwtSizingConstants.HORIZONTAL_PADDING)));
            height = (int) (ToolitemSwtSizingConstants.DROPDOWN_BUTTON_HEIGHT);
        }
        // Note for Separator: Simple separator with fixed dimensions and customizable thickness.
        else if ((SWT.SEPARATOR != 0 && (style & SWT.SEPARATOR) == SWT.SEPARATOR)) {
            width = (int) (ToolitemSwtSizingConstants.SEPARATOR_WIDTH);
            height = (int) (ToolitemSwtSizingConstants.SEPARATOR_HEIGHT);
        }
        else {
            width = (int) (textLength * ToolitemSwtSizingConstants.AVERAGE_CHAR_WIDTH);
            height = (int) (ToolitemSwtSizingConstants.DEFAULT_FONT_SIZE * 1.2);
        }

        // --- Apply hints and border ---
        if (width == 0) {
            width = SWT.DEFAULT;
        }
        if (height == 0) {
            width = SWT.DEFAULT; // TODO: Define appropriate DEFAULT_HEIGHT for Toolitem if needed
        }

        int borderWidth = (int) ToolitemSwtSizingConstants.BORDER_RADIUS;
        width += borderWidth * 2;
        height += borderWidth * 2;

        return new Point(width, height);
    }

    public ToolItemValue.Builder builder() {
        if (builder == null)
            builder = ToolItemValue.builder().setId(hashCode()).setStyle(style);
        return (ToolItemValue.Builder) builder;
    }
}
