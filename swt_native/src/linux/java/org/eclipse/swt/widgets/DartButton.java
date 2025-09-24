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

    long boxHandle, labelHandle, imageHandle, arrowHandle, groupHandle;

    boolean selected, grayed;

    /**
     * True iff this toggle button requires special theme handling. See bug 546552.
     */
    boolean toggleButtonTheming;

    Image image;

    String text;

    Image defaultDisableImage;

    boolean enabled = true;

    static final int INNER_BORDER = 1;

    static final int DEFAULT_BORDER = 1;

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
    }

    static int checkStyle(int style) {
        style = checkBits(style, SWT.PUSH, SWT.ARROW, SWT.CHECK, SWT.RADIO, SWT.TOGGLE, 0);
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

    @Override
    Point computeSizeInPixels(int wHint, int hHint, boolean changed) {
        return Sizes.compute(this);
    }

    @Override
    void createHandle(int index) {
        // In GTK 3 font description is inherited from parent widget which is not how SWT has always worked,
    }

    @Override
    void createWidget(int index) {
        super.createWidget(index);
        text = "";
    }

    @Override
    void checkBackground() {
        /*
	 * Feature in GTK: some toggle style (check, radio, and toggle) buttons
	 * have inverted colors, meaning the background color when checked is
	 * a dark color (like blue or green), and the checkmark/indicator is
	 * white. To complicate matters, this background area is an image, and
	 * overriding this with a color causes the checkmark to be invisible.
	 * The new (GTK3 >= 3.24.11) Adwaita theme is affected, as well as the
	 * default Yaru theme on Ubuntu.
	 *
	 * Part of the fix is to not inherit the parents background. See bug 553657.
	 */
        if (toggleButtonTheming && (getApi().style & (SWT.CHECK | SWT.RADIO)) != 0) {
            getApi().state &= ~PARENT_BACKGROUND;
        } else {
            super.checkBackground();
        }
    }

    @Override
    void deregister() {
        super.deregister();
        if (boxHandle != 0)
            ((SwtDisplay) display.getImpl()).removeWidget(boxHandle);
        if (labelHandle != 0)
            ((SwtDisplay) display.getImpl()).removeWidget(labelHandle);
        if (imageHandle != 0)
            ((SwtDisplay) display.getImpl()).removeWidget(imageHandle);
        if (arrowHandle != 0)
            ((SwtDisplay) display.getImpl()).removeWidget(arrowHandle);
    }

    @Override
    long fontHandle() {
        if (labelHandle != 0)
            return labelHandle;
        return super.fontHandle();
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
        return this.selection;
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

    @Override
    boolean mustBeVisibleOnInitBounds() {
        // Bug 540298: Button needs to be visible in order to gain focus.
        return true;
    }

    @Override
    void hookEvents() {
        super.hookEvents();
        if (labelHandle != 0) {
        }
    }

    @Override
    boolean isDescribedByLabel() {
        return false;
    }

    @Override
    boolean mnemonicHit(char key) {
        if (labelHandle == 0)
            return false;
        boolean result = super.mnemonicHit(labelHandle, key);
        if (result)
            setFocus();
        return result;
    }

    @Override
    boolean mnemonicMatch(char key) {
        if (labelHandle == 0)
            return false;
        return mnemonicMatch(labelHandle, key);
    }

    @Override
    void register() {
        super.register();
        if (boxHandle != 0)
            ((SwtDisplay) display.getImpl()).addWidget(boxHandle, this.getApi());
        if (labelHandle != 0)
            ((SwtDisplay) display.getImpl()).addWidget(labelHandle, this.getApi());
        if (imageHandle != 0)
            ((SwtDisplay) display.getImpl()).addWidget(imageHandle, this.getApi());
        if (arrowHandle != 0)
            ((SwtDisplay) display.getImpl()).addWidget(arrowHandle, this.getApi());
    }

    @Override
    void releaseHandle() {
        super.releaseHandle();
        boxHandle = imageHandle = labelHandle = arrowHandle = 0;
    }

    @Override
    void releaseWidget() {
        super.releaseWidget();
        groupHandle = 0;
        image = null;
        text = null;
        disposeDefaultDisabledImage();
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
    void resizeHandle(int width, int height) {
        if ((getApi().style & (SWT.CHECK | SWT.RADIO)) != 0 && (getApi().style & SWT.WRAP) == 0) {
        } else {
            super.resizeHandle(width, height);
        }
    }

    void selectRadio() {
        /*
	* This code is intentionally commented.  When two groups
	* of radio buttons with the same parent are separated by
	* another control, the correct behavior should be that
	* the two groups act independently.  This is consistent
	* with radio tool and menu items.  The commented code
	* implements this behavior.
	*/
        //	int index = 0;
        //	Control [] children = parent._getChildren ();
        //	while (index < children.length && children [index] != this) index++;
        //	int i = index - 1;
        //	while (i >= 0 && children [i].setRadioSelection (false)) --i;
        //	int j = index + 1;
        //	while (j < children.length && children [j].setRadioSelection (false)) j++;
        //	setSelection (true);
        Control[] children = parent.getImpl()._getChildren();
        for (int i = 0; i < children.length; i++) {
            Control child = children[i];
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
        checkWidget();
        _setAlignment(alignment);
    }

    void _setAlignment(int alignment) {
        dirty();
        if ((getApi().style & SWT.ARROW) != 0) {
            if ((getApi().style & (SWT.UP | SWT.DOWN | SWT.LEFT | SWT.RIGHT)) == 0)
                return;
            getApi().style &= ~(SWT.UP | SWT.DOWN | SWT.LEFT | SWT.RIGHT);
            getApi().style |= alignment & (SWT.UP | SWT.DOWN | SWT.LEFT | SWT.RIGHT);
            switch(alignment) {
                case SWT.UP:
                    break;
                case SWT.DOWN:
                    break;
                case SWT.LEFT:
                    break;
                case SWT.RIGHT:
                    break;
            }
            return;
        }
        if ((alignment & (SWT.LEFT | SWT.RIGHT | SWT.CENTER)) == 0)
            return;
        getApi().style &= ~(SWT.LEFT | SWT.RIGHT | SWT.CENTER);
        getApi().style |= alignment & (SWT.LEFT | SWT.RIGHT | SWT.CENTER);
        if ((alignment & SWT.LEFT) != 0) {
            return;
        }
        if ((alignment & SWT.CENTER) != 0) {
            return;
        }
        if ((alignment & SWT.RIGHT) != 0) {
            return;
        }
    }

    @Override
    int setBounds(int x, int y, int width, int height, boolean move, boolean resize) {
        int result = super.setBounds(x, y, width, height, move, resize);
        return result;
    }

    @Override
    void setFontDescription(long fontDesc) {
        // Don't set the font if we have no text set
        if ((text != null && text.isEmpty()) || text == null) {
            return;
        } else {
            super.setFontDescription(fontDesc);
            {
                /*
			 * GTK3 Workaround for bug which causes incorrect size
			 * calculation when the button (radio/check) is set active
			 * before setting font description.
			 */
                boolean selected = getSelection();
                if (selected)
                    setSelection(!selected);
                if (labelHandle != 0)
                    setFontDescription(labelHandle, fontDesc);
                setSelection(selected);
            }
        }
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
        if ((getApi().style & SWT.ARROW) != 0)
            return;
        disposeDefaultDisabledImage();
        if (!enabled && defaultDisableImage != image && defaultDisableImage != null) {
            return;
        }
        this.image = image;
        _setImage(image);
    }

    private void _setImage(Image image) {
        checkWidget();
        if ((getApi().style & SWT.ARROW) != 0)
            return;
        if (image != null) {
            if (image.isDisposed())
                error(SWT.ERROR_INVALID_ARGUMENT);
        } else {
        }
        updateWidgetsVisibility();
        _setAlignment(getApi().style);
    }

    @Override
    void setOrientation(boolean create) {
        super.setOrientation(create);
        if ((getApi().style & SWT.RIGHT_TO_LEFT) != 0 || !create) {
            if (arrowHandle != 0) {
                if ((getApi().style & (SWT.LEFT | SWT.RIGHT)) != 0) {
                }
            }
        }
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
        if ((getApi().style & SWT.RADIO) != 0) {
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
        updateWidgetsVisibility();
        _setAlignment(getApi().style);
        // Now that text has been added, set the font. This ensures
        // the Button's size is correctly calculated/updated.
        setFontDescription(font == null ? defaultFont().handle : font.handle);
    }

    private void updateWidgetsVisibility() {
        if (text.length() == 0 && image == null) {
        } else {
        }
    }

    @Override
    void showWidget() {
        super.showWidget();
    }

    @Override
    int traversalCode(int key, Object event) {
        int code = super.traversalCode(key, event);
        if ((getApi().style & SWT.ARROW) != 0)
            code &= ~(SWT.TRAVERSE_TAB_NEXT | SWT.TRAVERSE_TAB_PREVIOUS);
        if ((getApi().style & SWT.RADIO) != 0)
            code |= SWT.TRAVERSE_ARROW_NEXT | SWT.TRAVERSE_ARROW_PREVIOUS;
        return code;
    }

    @Override
    long dpiChanged(long object, long arg0) {
        super.dpiChanged(object, arg0);
        if (image != null) {
            setImage(image);
        }
        return 0;
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
    @Override
    public void setEnabled(boolean enabled) {
        dirty();
        checkWidget();
        if (this.enabled == enabled)
            return;
        this.enabled = enabled;
        super.setEnabled(enabled);
        if (!enabled) {
            if (defaultDisableImage == null && image != null) {
                defaultDisableImage = new Image(getDisplay(), image, SWT.IMAGE_DISABLE);
            }
            _setImage(defaultDisableImage);
        }
        if (enabled && image != null)
            _setImage(image);
    }

    private void disposeDefaultDisabledImage() {
        if (defaultDisableImage != null) {
            defaultDisableImage.dispose();
            defaultDisableImage = null;
        }
    }

    boolean selection;

    public long _boxHandle() {
        return boxHandle;
    }

    public long _labelHandle() {
        return labelHandle;
    }

    public long _imageHandle() {
        return imageHandle;
    }

    public long _arrowHandle() {
        return arrowHandle;
    }

    public long _groupHandle() {
        return groupHandle;
    }

    public boolean _selected() {
        return selected;
    }

    public boolean _grayed() {
        return grayed;
    }

    public boolean _toggleButtonTheming() {
        return toggleButtonTheming;
    }

    public Image _image() {
        return image;
    }

    public String _text() {
        return text;
    }

    public Image _defaultDisableImage() {
        return defaultDisableImage;
    }

    public boolean _enabled() {
        return enabled;
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
