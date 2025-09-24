/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2019 IBM Corporation and others.
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
 *      Conrad Groth - Bug 23837 [FEEP] Button, do not respect foreground and background color on Windows
 * *****************************************************************************
 */
package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.internal.*;
import dev.equo.swt.*;

/**
 * Instances of this class represent a selectable user interface object that
 * issues notification when pressed and released.
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>ARROW, CHECK, PUSH, RADIO, TOGGLE, FLAT, WRAP</dd>
 * <dd>UP, DOWN, LEFT, RIGHT, CENTER</dd>
 * <dt><b>Events:</b></dt>
 * <dd>Selection</dd>
 * </dl>
 * <p>
 * Note: Only one of the styles ARROW, CHECK, PUSH, RADIO, and TOGGLE
 * may be specified.
 * </p><p>
 * Note: Only one of the styles LEFT, RIGHT, and CENTER may be specified.
 * </p><p>
 * Note: Only one of the styles UP, DOWN, LEFT, and RIGHT may be specified
 * when the ARROW style is specified.
 * </p><p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 *
 * @see <a href="http://www.eclipse.org/swt/snippets/#button">Button snippets</a>
 * @see <a href="http://www.eclipse.org/swt/examples.php">SWT Example: ControlExample</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class DartButton extends DartControl implements IButton {

    String text = "", message = "";

    Image image, disabledImage;

    boolean ignoreMouse, grayed, useDarkModeExplorerTheme;

    static final int MARGIN = 4;

    private int checkWidth, checkHeight;

    static final int ICON_WIDTH = 128, ICON_HEIGHT = 128;

    static boolean /*final*/
    COMMAND_LINK = false;

    static final char[] STRING_WITH_ZERO_CHAR = new char[] { '0' };

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
     * @see SWT#ARROW
     * @see SWT#CHECK
     * @see SWT#PUSH
     * @see SWT#RADIO
     * @see SWT#TOGGLE
     * @see SWT#FLAT
     * @see SWT#UP
     * @see SWT#DOWN
     * @see SWT#LEFT
     * @see SWT#RIGHT
     * @see SWT#CENTER
     * @see Widget#checkSubclass
     * @see Widget#getStyle
     */
    public DartButton(Composite parent, int style, Button api) {
        super(parent, checkStyle(style), api);
        refreshCheckSize(this.getApi().nativeZoom);
    }

    /**
     * Refresh the checkWidth and checkHeight Size whenever there is a dpi change or
     * when button is created. Factor is calculated by identifying the zoom change
     * e.g. 100 -> 200 should be scaled down to half (currentZoom / primaryZoom)
     *
     * @param currentZoom
     */
    private void refreshCheckSize(int currentZoom) {
    }

    void _setImage(Image image) {
        if ((getApi().style & SWT.COMMAND) != 0)
            return;
        if (image != null) {
            if (text.length() == 0) {
            } else {
            }
        } else {
        }
    }

    void _setText(String text) {
        /*
	* Bug in Windows.  When a Button control is right-to-left and
	* is disabled, the first pixel of the text is clipped.  The fix
	* is to append a space to the text.
	*/
        if ((getApi().style & SWT.RIGHT_TO_LEFT) != 0) {
        }
        if ((getApi().state & HAS_AUTO_DIRECTION) != 0) {
            updateTextDirection(AUTO_TEXT_DIRECTION);
        }
    }

    /**
     * Adds the listener to the collection of listeners who will
     * be notified when the control is selected by the user, by sending
     * it one of the messages defined in the <code>SelectionListener</code>
     * interface.
     * <p>
     * <code>widgetSelected</code> is called when the control is selected by the user.
     * <code>widgetDefaultSelected</code> is not called.
     * </p>
     * <p>
     * When the <code>SWT.RADIO</code> style bit is set, the <code>widgetSelected</code> method is
     * also called when the receiver loses selection because another item in the same radio group
     * was selected by the user. During <code>widgetSelected</code> the application can use
     * <code>getSelection()</code> to determine the current selected state of the receiver.
     * </p>
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
     * @see SelectionListener
     * @see #removeSelectionListener
     * @see SelectionEvent
     */
    public void addSelectionListener(SelectionListener listener) {
        addTypedListener(listener, SWT.Selection, SWT.DefaultSelection);
    }

    static int checkStyle(int style) {
        style = checkBits(style, SWT.PUSH, SWT.ARROW, SWT.CHECK, SWT.RADIO, SWT.TOGGLE, COMMAND_LINK ? SWT.COMMAND : 0);
        if ((style & (SWT.PUSH | SWT.TOGGLE)) != 0) {
            return checkBits(style, SWT.CENTER, SWT.LEFT, SWT.RIGHT, 0, 0, 0);
        }
        if ((style & (SWT.CHECK | SWT.RADIO)) != 0) {
            return checkBits(style, SWT.LEFT, SWT.RIGHT, SWT.CENTER, 0, 0, 0);
        }
        if ((style & SWT.ARROW) != 0) {
            style |= SWT.NO_FOCUS;
            return checkBits(style, SWT.UP, SWT.DOWN, SWT.LEFT, SWT.RIGHT, 0, 0);
        }
        return style;
    }

    void click() {
        /*
	* Feature in Windows.  BM_CLICK sends a fake WM_LBUTTONDOWN and
	* WM_LBUTTONUP in order to click the button.  This causes the
	* application to get unexpected mouse events.  The fix is to
	* ignore mouse events when they are caused by BM_CLICK.
	*/
        ignoreMouse = true;
        ignoreMouse = false;
    }

    // TODO: this method ignores the style LEFT, CENTER or RIGHT
    int computeLeftMargin() {
        if ((getApi().style & (SWT.PUSH | SWT.TOGGLE)) == 0)
            return MARGIN;
        int margin = 0;
        if (image != null && text.length() != 0) {
            Rectangle bounds = DPIUtil.scaleBounds(image.getBounds(), this.getZoom(), 100);
            margin += bounds.width + MARGIN * 2;
            if ((getApi().style & SWT.LEFT) != 0) {
                margin = MARGIN;
            } else if ((getApi().style & SWT.RIGHT) != 0) {
            } else {
            }
        }
        return margin;
    }

    @Override
    Point computeSizeInPixels(int wHint, int hHint, boolean changed) {
        return Sizes.compute(this);
    }

    @Override
    void createHandle() {
    }

    private boolean customBackgroundDrawing() {
        return background != -1 && !isRadioOrCheck();
    }

    private boolean customDrawing() {
        return customBackgroundDrawing() || customForegroundDrawing();
    }

    private boolean customForegroundDrawing() {
        return false;
    }

    @Override
    int defaultBackground() {
        if ((getApi().style & (SWT.PUSH | SWT.TOGGLE)) != 0) {
        }
        return super.defaultBackground();
    }

    @Override
    int defaultForeground() {
        return 0;
    }

    @Override
    void enableWidget(boolean enabled) {
        super.enableWidget(enabled);
        /*
	* Bug in Windows.  When a Button control is right-to-left and
	* is disabled, the first pixel of the text is clipped.  The fix
	* is to append a space to the text.
	*/
        if ((getApi().style & SWT.RIGHT_TO_LEFT) != 0) {
        }
        /*
	* Bug in Windows.  When a button has the style BS_CHECKBOX
	* or BS_RADIOBUTTON, is checked, and is displaying both an
	* image and some text, when BCM_SETIMAGELIST is used to
	* assign an image list for each of the button states, the
	* button does not draw properly.  When the user drags the
	* mouse in and out of the button, it draws using a blank
	* image.  The fix is to set the complete image list only
	* when the button is disabled.
	*/
        updateImageList();
    }

    /**
     * Returns a value which describes the position of the
     * text or image in the receiver. The value will be one of
     * <code>LEFT</code>, <code>RIGHT</code> or <code>CENTER</code>
     * unless the receiver is an <code>ARROW</code> button, in
     * which case, the alignment will indicate the direction of
     * the arrow (one of <code>LEFT</code>, <code>RIGHT</code>,
     * <code>UP</code> or <code>DOWN</code>).
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
        if ((getApi().style & SWT.ARROW) != 0) {
            if ((getApi().style & SWT.UP) != 0)
                return SWT.UP;
            if ((getApi().style & SWT.DOWN) != 0)
                return SWT.DOWN;
            if ((getApi().style & SWT.LEFT) != 0)
                return SWT.LEFT;
            if ((getApi().style & SWT.RIGHT) != 0)
                return SWT.RIGHT;
            return SWT.UP;
        }
        if ((getApi().style & SWT.LEFT) != 0)
            return SWT.LEFT;
        if ((getApi().style & SWT.CENTER) != 0)
            return SWT.CENTER;
        if ((getApi().style & SWT.RIGHT) != 0)
            return SWT.RIGHT;
        return SWT.LEFT;
    }

    boolean getDefault() {
        if ((getApi().style & SWT.PUSH) == 0)
            return false;
        return false;
    }

    /**
     * Returns <code>true</code> if the receiver is grayed,
     * and false otherwise. When the widget does not have
     * the <code>CHECK</code> style, return false.
     *
     * @return the grayed state of the checkbox
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.4
     */
    public boolean getGrayed() {
        checkWidget();
        if ((getApi().style & SWT.CHECK) == 0)
            return false;
        return grayed;
    }

    /**
     * Returns the receiver's image if it has one, or null
     * if it does not.
     *
     * @return the receiver's image
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public Image getImage() {
        checkWidget();
        return image;
    }

    /**
     * Returns the widget message. When the widget is created
     * with the style <code>SWT.COMMAND</code>, the message text
     * is displayed to provide further information for the user.
     *
     * @return the widget message
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.3
     */
    /*public*/
    String getMessage() {
        checkWidget();
        return message;
    }

    @Override
    String getNameText() {
        return getText();
    }

    /**
     * Returns <code>true</code> if the receiver is selected,
     * and false otherwise.
     * <p>
     * When the receiver is of type <code>CHECK</code> or <code>RADIO</code>,
     * it is selected when it is checked. When it is of type <code>TOGGLE</code>,
     * it is selected when it is pushed in. If the receiver is of any other type,
     * this method returns false.
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
        if ((getApi().style & (SWT.CHECK | SWT.RADIO | SWT.TOGGLE)) == 0)
            return false;
        return isChecked();
    }

    /**
     * Returns the receiver's text, which will be an empty
     * string if it has never been set or if the receiver is
     * an <code>ARROW</code> button.
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
        if ((getApi().style & SWT.ARROW) != 0)
            return "";
        return text;
    }

    private boolean isChecked() {
        return false;
    }

    private boolean isRadioOrCheck() {
        return (getApi().style & (SWT.RADIO | SWT.CHECK)) != 0;
    }

    @Override
    public boolean isTabItem() {
        if ((getApi().style & SWT.PUSH) != 0)
            return isTabGroup();
        return super.isTabItem();
    }

    @Override
    boolean mnemonicHit(char ch) {
        /*
	 * Feature in Windows. When a radio button gets focus, it selects the button in
	 * WM_SETFOCUS. Workaround is to never set focus to an unselected radio button.
	 * Therefore, don't try to set focus on radio buttons, click will set focus.
	 */
        if ((getApi().style & SWT.RADIO) == 0 && !setFocus())
            return false;
        click();
        return true;
    }

    @Override
    boolean mnemonicMatch(char key) {
        char mnemonic = findMnemonic(getText());
        if (mnemonic == '\0')
            return false;
        return Character.toUpperCase(key) == Character.toUpperCase(mnemonic);
    }

    @Override
    void releaseWidget() {
        super.releaseWidget();
        if (disabledImage != null)
            disabledImage.dispose();
        disabledImage = null;
        text = null;
        image = null;
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
    int resolveTextDirection() {
        return 0;
    }

    void selectRadio() {
        for (Control child : parent.getImpl()._getChildren()) {
            if (this.getApi() != child)
                ((DartControl) child.getImpl()).setRadioSelection(false);
        }
        setSelection(true);
    }

    /**
     * Controls how text, images and arrows will be displayed
     * in the receiver. The argument should be one of
     * <code>LEFT</code>, <code>RIGHT</code> or <code>CENTER</code>
     * unless the receiver is an <code>ARROW</code> button, in
     * which case, the argument indicates the direction of
     * the arrow (one of <code>LEFT</code>, <code>RIGHT</code>,
     * <code>UP</code> or <code>DOWN</code>).
     *
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
        if ((getApi().style & SWT.ARROW) != 0) {
            if ((getApi().style & (SWT.UP | SWT.DOWN | SWT.LEFT | SWT.RIGHT)) == 0)
                return;
            getApi().style &= ~(SWT.UP | SWT.DOWN | SWT.LEFT | SWT.RIGHT);
            getApi().style |= alignment & (SWT.UP | SWT.DOWN | SWT.LEFT | SWT.RIGHT);
            return;
        }
        if ((alignment & (SWT.LEFT | SWT.RIGHT | SWT.CENTER)) == 0)
            return;
        getApi().style &= ~(SWT.LEFT | SWT.RIGHT | SWT.CENTER);
        getApi().style |= alignment & (SWT.LEFT | SWT.RIGHT | SWT.CENTER);
    }

    /**
     * Sets the button's background color to the color specified
     * by the argument, or to the default system color for the control
     * if the argument is null.
     * <p>
     * Note: This is custom paint operation and only affects {@link SWT#PUSH} and {@link SWT#TOGGLE} buttons. If the native button
     * has a 3D look an feel (e.g. Windows 7), this method will cause the button to look FLAT irrespective of the state of the
     * {@link SWT#FLAT} style.
     * For {@link SWT#CHECK} and {@link SWT#RADIO} buttons, this method delegates to {@link Control#setBackground(Color)}.
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
     */
    @Override
    public void setBackground(Color color) {
        // This method only exists in order to provide custom documentation
        super.setBackground(color);
    }

    public void setDefault(boolean value) {
        if ((getApi().style & SWT.PUSH) == 0)
            return;
        if (value) {
        } else {
        }
    }

    @Override
    public boolean setFocus() {
        checkWidget();
        /*
	* Feature in Windows.  When a radio button gets focus,
	* it selects the button in WM_SETFOCUS.  The fix is to
	* not assign focus to an unselected radio button.
	*/
        if ((getApi().style & SWT.RADIO) != 0 && !isChecked())
            return false;
        return super.setFocus();
    }

    /**
     * Sets the receiver's image to the argument, which may be
     * <code>null</code> indicating that no image should be displayed.
     * <p>
     * Note that a Button can display an image and text simultaneously.
     * </p>
     * @param image the image to display on the receiver (may be <code>null</code>)
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the image has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setImage(Image image) {
        dirty();
        checkWidget();
        if (image != null && image.isDisposed())
            error(SWT.ERROR_INVALID_ARGUMENT);
        if ((getApi().style & SWT.ARROW) != 0)
            return;
        this.image = image;
        _setImage(image);
    }

    /**
     * Sets the grayed state of the receiver.  This state change
     * only applies if the control was created with the SWT.CHECK
     * style.
     *
     * @param grayed the new grayed state
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.4
     */
    public void setGrayed(boolean grayed) {
        dirty();
        checkWidget();
        if ((getApi().style & SWT.CHECK) == 0)
            return;
        this.grayed = grayed;
        if (grayed) {
        } else {
        }
    }

    /**
     * Sets the widget message. When the widget is created
     * with the style <code>SWT.COMMAND</code>, the message text
     * is displayed to provide further information for the user.
     *
     * @param message the new message
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the string is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.3
     */
    /*public*/
    void setMessage(String message) {
        checkWidget();
        if (message == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        this.message = message;
        if ((getApi().style & SWT.COMMAND) != 0) {
            int length = message.length();
            char[] chars = new char[length + 1];
            message.getChars(0, length, chars, 0);
        }
    }

    @Override
    public boolean setRadioFocus(boolean tabbing) {
        if ((getApi().style & SWT.RADIO) == 0 || !getSelection())
            return false;
        return tabbing ? setTabItemFocus() : setFocus();
    }

    @Override
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
     * Sets the selection state of the receiver, if it is of type <code>CHECK</code>,
     * <code>RADIO</code>, or <code>TOGGLE</code>.
     *
     * <p>
     * When the receiver is of type <code>CHECK</code> or <code>RADIO</code>,
     * it is selected when it is checked. When it is of type <code>TOGGLE</code>,
     * it is selected when it is pushed in.
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
        if ((getApi().style & (SWT.CHECK | SWT.RADIO | SWT.TOGGLE)) == 0)
            return;
        if ((getApi().style & SWT.CHECK) != 0) {
        }
        this.selection = selected;
    }

    /**
     * Sets the receiver's text.
     * <p>
     * This method sets the button label.  The label may include
     * the mnemonic character but must not contain line delimiters.
     * </p>
     * <p>
     * Mnemonics are indicated by an '&amp;' that causes the next
     * character to be the mnemonic.  When the user presses a
     * key sequence that matches the mnemonic, a selection
     * event occurs. On most platforms, the mnemonic appears
     * underlined but may be emphasized in a platform specific
     * manner.  The mnemonic indicator character '&amp;' can be
     * escaped by doubling it in the string, causing a single
     * '&amp;' to be displayed.
     * </p><p>
     * Note that a Button can display an image and text simultaneously
     * on Windows (starting with XP), GTK+ and OSX.  On other platforms,
     * a Button that has an image and text set into it will display the
     * image or text that was set most recently.
     * </p><p>
     * Also note, if control characters like '\n', '\t' etc. are used
     * in the string, then the behavior is platform dependent.
     * </p>
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
        dirty();
        checkWidget();
        if (string == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if ((getApi().style & SWT.ARROW) != 0)
            return;
        text = string;
        _setText(string);
    }

    @Override
    boolean updateTextDirection(int textDirection) {
        if (super.updateTextDirection(textDirection)) {
            // TODO: Keep for now, to follow up
            //		int flags = SWT.RIGHT_TO_LEFT | SWT.LEFT_TO_RIGHT;
            //		style &= ~SWT.MIRRORED;
            //		style &= ~flags;
            //		style |= textDirection & flags;
            //		updateOrientation ();
            //		checkMirrored ();
            return true;
        }
        return false;
    }

    void updateImageList() {
    }

    @Override
    void updateOrientation() {
        super.updateOrientation();
        updateImageList();
    }

    void updateSelection(int flags) {
    }

    @Override
    int widgetStyle() {
        return 0;
    }

    private int getCheckboxTextOffset(long hdc) {
        int result = 0;
        {
            result += DPIUtil.scaleUp(13, getApi().nativeZoom);
        }
        return result;
    }

    static int getThemeStateId(int style, boolean pressed, boolean enabled) {
        int direction = style & (SWT.UP | SWT.DOWN | SWT.LEFT | SWT.RIGHT);
        /*
	 * Feature in Windows.  DrawThemeBackground() does not mirror the drawing.
	 * The fix is switch left to right and right to left.
	 */
        if ((style & SWT.MIRRORED) != 0) {
            if (direction == SWT.LEFT) {
                direction = SWT.RIGHT;
            } else if (direction == SWT.RIGHT) {
                direction = SWT.LEFT;
            }
        }
        /*
	 * On Win11, scrollbars no longer show arrows by default.
	 * Arrows only show up when hot/disabled/pushed.
	 * The workaround is to use hot image in place of default.
	 */
        boolean hot = false;
        if (hot) {
            switch(direction) {
                case SWT.UP:
                case SWT.DOWN:
                case SWT.LEFT:
                case SWT.RIGHT:
            }
        }
        if (pressed) {
            switch(direction) {
                case SWT.UP:
                case SWT.DOWN:
                case SWT.LEFT:
                case SWT.RIGHT:
            }
        }
        if (!enabled) {
            switch(direction) {
                case SWT.UP:
                case SWT.DOWN:
                case SWT.LEFT:
                case SWT.RIGHT:
            }
        }
        switch(direction) {
            case SWT.UP:
            case SWT.DOWN:
            case SWT.LEFT:
            case SWT.RIGHT:
        }
        return 0;
    }

    private static void handleDPIChange(Widget widget, int newZoom, float scalingFactor) {
        if (!(widget instanceof Button button)) {
            return;
        }
        //Refresh the CheckSize
        ((DartButton) button.getImpl()).refreshCheckSize(newZoom);
        // Refresh the image
        if (((DartButton) button.getImpl()).image != null) {
            ((DartButton) button.getImpl())._setImage(((DartButton) button.getImpl()).image);
            ((DartButton) button.getImpl()).updateImageList();
        }
    }

    boolean selection;

    public String _text() {
        return text;
    }

    public String _message() {
        return message;
    }

    public Image _image() {
        return image;
    }

    public Image _disabledImage() {
        return disabledImage;
    }

    public boolean _ignoreMouse() {
        return ignoreMouse;
    }

    public boolean _grayed() {
        return grayed;
    }

    public boolean _useDarkModeExplorerTheme() {
        return useDarkModeExplorerTheme;
    }

    public boolean _selection() {
        return selection;
    }

    protected void _hookEvents() {
        super._hookEvents();
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

    public Button getApi() {
        if (api == null)
            api = Button.createApi(this);
        return (Button) api;
    }

    public VButton getValue() {
        if (value == null)
            value = new VButton(this);
        return (VButton) value;
    }
}
