/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2025 IBM Corporation and others.
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
import org.eclipse.swt.accessibility.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.internal.*;
import java.util.Objects;
import dev.equo.swt.*;

/**
 * Instances of this class represent a non-selectable
 * user interface object that displays a string or image.
 * When SEPARATOR is specified, displays a single
 * vertical or horizontal line.
 * <p>
 * Shadow styles are hints and may not be honored
 * by the platform.  To create a separator label
 * with the default shadow style for the platform,
 * do not specify a shadow style.
 * </p>
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>SEPARATOR, HORIZONTAL, VERTICAL</dd>
 * <dd>SHADOW_IN, SHADOW_OUT, SHADOW_NONE</dd>
 * <dd>CENTER, LEFT, RIGHT, WRAP</dd>
 * <dt><b>Events:</b></dt>
 * <dd>(none)</dd>
 * </dl>
 * <p>
 * Note: Only one of SHADOW_IN, SHADOW_OUT and SHADOW_NONE may be specified.
 * SHADOW_NONE is a HINT. Only one of HORIZONTAL and VERTICAL may be specified.
 * Only one of CENTER, LEFT and RIGHT may be specified.
 * </p><p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 *
 * @see <a href="http://www.eclipse.org/swt/snippets/#label">Label snippets</a>
 * @see <a href="http://www.eclipse.org/swt/examples.php">SWT Example: ControlExample</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class DartLabel extends DartControl implements ILabel {

    long frameHandle, labelHandle, imageHandle, boxHandle;

    Image image;

    String text;

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
     * @see SWT#SEPARATOR
     * @see SWT#HORIZONTAL
     * @see SWT#VERTICAL
     * @see SWT#SHADOW_IN
     * @see SWT#SHADOW_OUT
     * @see SWT#SHADOW_NONE
     * @see SWT#CENTER
     * @see SWT#LEFT
     * @see SWT#RIGHT
     * @see SWT#WRAP
     * @see Widget#checkSubclass
     * @see Widget#getStyle
     */
    public DartLabel(Composite parent, int style, Label api) {
        super(parent, checkStyle(style), api);
    }

    static int checkStyle(int style) {
        style |= SWT.NO_FOCUS;
        if ((style & SWT.SEPARATOR) != 0) {
            style = checkBits(style, SWT.VERTICAL, SWT.HORIZONTAL, 0, 0, 0, 0);
            return checkBits(style, SWT.SHADOW_OUT, SWT.SHADOW_IN, SWT.SHADOW_NONE, 0, 0, 0);
        }
        return checkBits(style, SWT.LEFT, SWT.CENTER, SWT.RIGHT, 0, 0, 0);
    }

    @Override
    public void addRelation(Control control) {
        if (!control.getImpl().isDescribedByLabel())
            return;
        if (labelHandle == 0)
            return;
        ((DartControl) control.getImpl())._getAccessible().addRelation(ACC.RELATION_LABELLED_BY, _getAccessible());
        if (control.getImpl() instanceof DartControl) {
            ((DartControl) control.getImpl()).labelRelation = this.getApi();
        }
        if (control.getImpl() instanceof SwtControl) {
            ((SwtControl) control.getImpl()).labelRelation = this.getApi();
        }
    }

    @Override
    Point computeNativeSize(long h, int wHint, int hHint, boolean changed) {
        int width = wHint, height = hHint;
        {
            return super.computeNativeSize(h, wHint, hHint, changed);
        }
    }

    @Override
    Point computeSizeInPixels(int wHint, int hHint, boolean changed) {
        return Sizes.computeSize(this, wHint, hHint, changed);
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
    void deregister() {
        super.deregister();
        if (frameHandle != 0)
            ((SwtDisplay) display.getImpl()).removeWidget(frameHandle);
        if (labelHandle != 0)
            ((SwtDisplay) display.getImpl()).removeWidget(labelHandle);
        if (imageHandle != 0)
            ((SwtDisplay) display.getImpl()).removeWidget(imageHandle);
        if (boxHandle != 0)
            ((SwtDisplay) display.getImpl()).removeWidget(boxHandle);
    }

    @Override
    long eventHandle() {
        return fixedHandle;
    }

    @Override
    long cssHandle() {
        if ((getApi().style & SWT.SEPARATOR) == 0) {
            return labelHandle;
        }
        return getApi().handle;
    }

    /**
     * Returns a value which describes the position of the
     * text or image in the receiver. The value will be one of
     * <code>LEFT</code>, <code>RIGHT</code> or <code>CENTER</code>
     * unless the receiver is a <code>SEPARATOR</code> label, in
     * which case, <code>NONE</code> is returned.
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
        if ((getApi().style & SWT.SEPARATOR) != 0)
            return 0;
        if ((getApi().style & SWT.LEFT) != 0)
            return SWT.LEFT;
        if ((getApi().style & SWT.CENTER) != 0)
            return SWT.CENTER;
        if ((getApi().style & SWT.RIGHT) != 0)
            return SWT.RIGHT;
        return SWT.LEFT;
    }

    @Override
    int getBorderWidthInPixels() {
        checkWidget();
        if (frameHandle != 0) {
            return getThickness(frameHandle).x;
        }
        return 0;
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
     * Returns the receiver's text, which will be an empty
     * string if it has never been set or if the receiver is
     * a <code>SEPARATOR</code> label.
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
        if ((getApi().style & SWT.SEPARATOR) != 0)
            return "";
        return text;
    }

    @Override
    void hookEvents() {
        super.hookEvents();
        if (labelHandle != 0) {
        }
    }

    @Override
    public boolean isDescribedByLabel() {
        return false;
    }

    @Override
    boolean mnemonicHit(char key) {
        if (labelHandle == 0)
            return false;
        boolean result = super.mnemonicHit(labelHandle, key);
        if (result) {
            Control control = this.getApi();
            while (control.getImpl()._parent() != null) {
                Control[] children = control.getImpl()._parent().getImpl()._getChildren();
                int index = 0;
                while (index < children.length) {
                    if (children[index] == control)
                        break;
                    index++;
                }
                index++;
                if (index < children.length) {
                    if (children[index].setFocus())
                        return result;
                }
                control = control.getImpl()._parent();
            }
        }
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
        if (frameHandle != 0)
            ((SwtDisplay) display.getImpl()).addWidget(frameHandle, this.getApi());
        if (labelHandle != 0)
            ((SwtDisplay) display.getImpl()).addWidget(labelHandle, this.getApi());
        if (imageHandle != 0)
            ((SwtDisplay) display.getImpl()).addWidget(imageHandle, this.getApi());
    }

    @Override
    void releaseHandle() {
        super.releaseHandle();
        frameHandle = imageHandle = labelHandle = boxHandle = 0;
    }

    @Override
    void releaseWidget() {
        super.releaseWidget();
        image = null;
        text = null;
    }

    @Override
    void resizeHandle(int width, int height) {
    }

    /**
     * Controls how text and images will be displayed in the receiver.
     * The argument should be one of <code>LEFT</code>, <code>RIGHT</code>
     * or <code>CENTER</code>.  If the receiver is a <code>SEPARATOR</code>
     * label, the argument is ignored and the alignment is not changed.
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
        if ((getApi().style & SWT.SEPARATOR) != 0)
            return;
        if ((alignment & (SWT.LEFT | SWT.RIGHT | SWT.CENTER)) == 0)
            return;
        getApi().style &= ~(SWT.LEFT | SWT.RIGHT | SWT.CENTER);
        getApi().style |= alignment & (SWT.LEFT | SWT.RIGHT | SWT.CENTER);
        setAlignment();
    }

    void setAlignment() {
        if ((getApi().style & SWT.LEFT) != 0) {
            return;
        }
        if ((getApi().style & SWT.CENTER) != 0) {
            return;
        }
        if ((getApi().style & SWT.RIGHT) != 0) {
            return;
        }
    }

    @Override
    int setBounds(int x, int y, int width, int height, boolean move, boolean resize) {
        /*
	* Bug in GTK.  For some reason, when the label is
	* wrappable and its container is resized, it does not
	* cause the label to be wrapped.  The fix is to
	* determine the size that will wrap the label
	* and expilictly set that size to force the label
	* to wrap.
	*
	* This part of the fix causes the label to be
	* resized to the preferred size but it still
	* won't draw properly.
	*/
        boolean fixWrap = resize && labelHandle != 0 && (getApi().style & SWT.WRAP) != 0;
        int result = super.setBounds(x, y, width, height, move, resize);
        /*
	* Bug in GTK.  For some reason, when the label is
	* wrappable and its container is resized, it does not
	* cause the label to be wrapped.  The fix is to
	* determine the size that will wrap the label
	* and expilictly set that size to force the label
	* to wrap.
	*
	* This part of the fix forces the label to be
	* resized so that it will draw wrapped.
	*/
        if (fixWrap) {
        }
        return result;
    }

    @Override
    void setFontDescription(long font) {
        super.setFontDescription(font);
        if (labelHandle != 0)
            setFontDescription(labelHandle, font);
        if (labelHandle != 0) {
            // Bug 445801: Work around for computeSize not returning a different value after
            // changing font, see https://bugzilla.gnome.org/show_bug.cgi?id=753116
        }
    }

    @Override
    void setOrientation(boolean create) {
        super.setOrientation(create);
        if ((getApi().style & SWT.RIGHT_TO_LEFT) != 0 || !create) {
        }
    }

    /**
     * Sets the receiver's image to the argument, which may be
     * null indicating that no image should be displayed.
     *
     * @param image the image to display on the receiver (may be null)
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
        checkWidget();
        if (!java.util.Objects.equals(this.image, image)) {
            dirty();
        }
        if (image != null && image.isDisposed()) {
            error(SWT.ERROR_INVALID_ARGUMENT);
        }
        if ((getApi().style & SWT.SEPARATOR) != 0)
            return;
        this.image = GraphicsUtils.copyImage(display, image);
        if (image != null) {
        } else {
        }
    }

    /**
     * Sets the receiver's text.
     * <p>
     * This method sets the widget label.  The label may include
     * the mnemonic character and line delimiters.
     * </p>
     * <p>
     * Mnemonics are indicated by an '&amp;' that causes the next
     * character to be the mnemonic.  When the user presses a
     * key sequence that matches the mnemonic, focus is assigned
     * to the control that follows the label. On most platforms,
     * the mnemonic appears underlined but may be emphasised in a
     * platform specific manner.  The mnemonic indicator character
     * '&amp;' can be escaped by doubling it in the string, causing
     * a single '&amp;' to be displayed.
     * </p>
     * <p>
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
    public void setText(String string) {
        checkWidget();
        if (!java.util.Objects.equals(this.text, string)) {
            dirty();
        }
        if (string == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if ((getApi().style & SWT.SEPARATOR) != 0)
            return;
        text = string;
    }

    @Override
    void setWidgetBackground() {
    }

    @Override
    void showWidget() {
        super.showWidget();
    }

    @Override
    long dpiChanged(long object, long arg0) {
        super.dpiChanged(object, arg0);
        if (image != null) {
            setImage(image);
        }
        return 0;
    }

    public long _frameHandle() {
        return frameHandle;
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

    public Image _image() {
        return image;
    }

    public String _text() {
        return text;
    }

    protected void _hookEvents() {
        super._hookEvents();
    }

    public Label getApi() {
        if (api == null)
            api = Label.createApi(this);
        return (Label) api;
    }

    public VLabel getValue() {
        if (value == null)
            value = new VLabel(this);
        return (VLabel) value;
    }
}
