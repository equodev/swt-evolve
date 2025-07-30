package org.eclipse.swt.custom;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.internal.TabitemSwtSizingConstants;
import org.eclipse.swt.values.CTabItemValue;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FlutterItem;
import org.eclipse.swt.widgets.FlutterSwt;
import org.eclipse.swt.widgets.IControl;
import org.eclipse.swt.widgets.SWTComposite;
import org.eclipse.swt.widgets.SWTControl;
import org.eclipse.swt.widgets.Widget;

import java.util.ArrayList;

/**
 * Instances of this class represent a selectable user interface object
 * that represent a page in a notebook widget.
 *
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>SWT.CLOSE</dd>
 * <dt><b>Events:</b></dt>
 * <dd>(none)</dd>
 * </dl>
 * <p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 *
 * @see <a href="http://www.eclipse.org/swt/snippets/#ctabfolder">CTabFolder, CTabItem snippets</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class FlutterCTabItem extends FlutterItem implements ICTabItem {

    FlutterCTabFolder parent;

    /**
     * Constructs a new instance of this class given its parent
     * (which must be a <code>CTabFolder</code>) and a style value
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
     * @param parent a CTabFolder which will be the parent of the new instance (cannot be null)
     * @param style the style of control to construct
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
     * </ul>
     *
     * @see SWT
     * @see Widget#getStyle()
     */
    public FlutterCTabItem(FlutterCTabFolder parent, int style) {
        this(parent, style, parent.getItemCount());
    }

    /**
     * Constructs a new instance of this class given its parent
     * (which must be a <code>CTabFolder</code>), a style value
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
     * @param parent a CTabFolder which will be the parent of the new instance (cannot be null)
     * @param style the style of control to construct
     * @param index the zero-relative index to store the receiver in its parent
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
     *    <li>ERROR_INVALID_RANGE - if the index is not between 0 and the number of elements in the parent (inclusive)</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
     * </ul>
     *
     * @see SWT
     * @see Widget#getStyle()
     */
    public FlutterCTabItem(FlutterCTabFolder parent, int style, int index) {
        super(parent, style);
        this.parent = parent;
//        showClose = (style & SWT.CLOSE) != 0;
//        parent.createItem(this, index);
    }

    /**
     * Returns a rectangle describing the receiver's size and location
     * relative to its parent.
     *
     * @return the receiver's bounding column rectangle
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public Rectangle getBounds() {
        return null;
        /*
	 * This call is intentionally commented out, to allow this getter method to be
	 * called from a thread which is different from one that created the widget.
	 */
    }

    /**
     * Gets the control that is displayed in the content area of the tab item.
     *
     * @return the control
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public IControl getControl() {
        return control;
    }

    /**
     * Get the image displayed in the tab if the tab is disabled.
     *
     * @return the disabled image or null
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @deprecated the disabled image is not used
     */
    @Deprecated
    public Image getDisabledImage() {
        return null;
    }

    /**
     * Returns the foreground color that the receiver will use to paint textual information.
     *
     * @return the receiver's foreground color
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @since 3.114
     */
    public Color getForeground() {
        return null;
    }

    /**
     * Returns the selection foreground color that the receiver will use to paint textual information.
     *
     * @return the receiver's selection foreground color
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @since 3.114
     */
    public Color getSelectionForeground() {
        return null;
    }

    /**
     * Returns the font that the receiver will use to paint textual information.
     *
     * @return the receiver's font
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     *  @since 3.0
     */
    public Font getFont() {
        return null;
    }

    /**
     * Returns the receiver's parent, which must be a <code>CTabFolder</code>.
     *
     * @return the receiver's parent
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public ICTabFolder getParent() {
        /*
	 * This call is intentionally commented out, to allow this getter method to be
	 * called from a thread which is different from one that created the widget.
	 */
        //checkWidget();
        return parent;
    }

    /**
     * Returns <code>true</code> to indicate that the receiver's close button should be shown.
     * Otherwise return <code>false</code>. The initial value is defined by the style (SWT.CLOSE)
     * that was used to create the receiver.
     *
     * @return <code>true</code> if the close button should be shown
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.4
     */
    public boolean getShowClose() {
        return builder().getShowClose().orElse(false);
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
        return builder().getToolTipText().orElse(null);
    }

    /**
     * Returns <code>true</code> if the item will be rendered in the visible area of the CTabFolder. Returns false otherwise.
     *
     *  @return <code>true</code> if the item will be rendered in the visible area of the CTabFolder. Returns false otherwise.
     *
     *  @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.0
     */
    public boolean isShowing() {
        return false;
    }

    private IControl control;
    /**
     * Sets the control that is used to fill the client area of
     * the tab folder when the user selects the tab item.
     *
     * @param control the new control (or null)
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
        // Not Generated
        checkWidget();
        if (control != null) {
            if (control.isDisposed())
                SWT.error(SWT.ERROR_INVALID_ARGUMENT);
            if (control.getParent() != parent.childComposite)
                SWT.error(SWT.ERROR_INVALID_PARENT);
        }
        if (this.control != null && !this.control.isDisposed()) {
            this.control.setVisible(false);
        }
        this.control = control;
        if (this.control != null) {
            int index = parent.indexOf(this);
            if (index == parent.getSelectionIndex()) {
                parent.setSelection(index);
            } else {
                int selectedIndex = parent.getSelectionIndex();
                IControl selectedControl = null;
                if (selectedIndex != -1) {
                    selectedControl = ((FlutterCTabItem) (parent.getItem(selectedIndex))).control;
                }
                if (this.control != selectedControl) {
                    this.control.setVisible(false);
                }
                parent.setSelection(index);
            }
        }
    }

    /**
     * Sets the image that is displayed if the tab item is disabled.
     * Null will clear the image.
     *
     * @param image the image to be displayed when the item is disabled or null
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @deprecated This image is not used
     */
    @Deprecated
    public void setDisabledImage(Image image) {
    }

    /**
     * Sets the font that the receiver will use to paint textual information
     * for this item to the font specified by the argument, or to the default font
     * for that kind of control if the argument is null.
     *
     * @param font the new font (or null)
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the argument has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.0
     */
    public void setFont(Font font) {
    }

    /**
     * Sets the foreground color that the receiver will use to paint textual information
     * for this item to the color specified by the argument, or to the default foreground color
     * for that kind of control if the argument is null.
     *
     * @param color the new color (or null)
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the argument has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @since 3.114
     */
    public void setForeground(Color color) {
    }

    /**
     * Sets the selection foreground color that the receiver will use to paint textual information
     * for this item to the color specified by the argument, or to the default selection foreground color
     * for that kind of control if the argument is null.
     *
     * @param color the new color (or null)
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the argument has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @since 3.114
     */
    public void setSelectionForeground(Color color) {
    }

    @Override
    public void setImage(Image image) {
    }

    /**
     * Sets to <code>true</code> to indicate that the receiver's close button should be shown.
     * If the parent (CTabFolder) was created with SWT.CLOSE style, changing this value has
     * no effect.
     *
     * @param close the new state of the close button
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.4
     */
    public void setShowClose(boolean close) {
        builder().setShowClose(close);
        FlutterSwt.dirty(this);
    }

    /**
     * Sets the text to display on the tab.
     * A carriage return '\n' allows to display multi line text.
     *
     * @param string the tab name
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    @Override
    public void setText(String string) {
        // Not Generated
        super.setText(string);
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
     *
     * @param string the new tool tip text (or null)
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setToolTipText(String string) {
        if (string != null) {
            builder().setToolTipText(string);
            FlutterSwt.dirty(this);
        }
    }

    // --- Generated computeSize method for Ctabitem ---
    // IMPORTANT: Review and adjust the parameter initializations (TODO sections)
    // and SWT style checks below to match the specific behavior of Ctabitem.
    @Override
    public Point computeSize() {
        checkWidget();
        int width = 0;
        int height = 0;

        // --- Parameter initializations---
        boolean hasImage = getImage() != null;
        boolean hasText = getText() != null && !getText().isEmpty();
        double textLength = hasText ? getText().length() : 0.0;

        // --- Style-based calculations ---
        width = (int) (TabitemSwtSizingConstants.RIGHT_PADDING + (hasImage ? (TabitemSwtSizingConstants.ICON_SIZE + TabitemSwtSizingConstants.ICON_RIGHT_PADDING) : 0.0) + (hasText ? (textLength * TabitemSwtSizingConstants.AVERAGE_CHAR_WIDTH) : 0.0));
        height = (int) (Math.max(TabitemSwtSizingConstants.TEXT_HEIGHT + TabitemSwtSizingConstants.TEXT_BOTTOM_PADDING, hasImage ? (TabitemSwtSizingConstants.ICON_SIZE + TabitemSwtSizingConstants.ICON_BOTTOM_PADDING) : 0.0));

        return new Point(width, height);
    }

    public CTabItemValue.Builder builder() {
        if (builder == null)
            builder = CTabItemValue.builder().setId(hashCode()).setStyle(style);
        return (CTabItemValue.Builder) builder;
    }
}
