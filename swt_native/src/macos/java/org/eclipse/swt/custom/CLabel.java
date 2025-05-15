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
package org.eclipse.swt.custom;

import org.eclipse.swt.*;
import org.eclipse.swt.accessibility.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import dev.equo.swt.Convert;

/**
 *  A Label which supports aligned text and/or an image and different border styles.
 *  <p>
 *  If there is not enough space a CLabel uses the following strategy to fit the
 *  information into the available space:
 *  <pre>
 *  		ignores the indent in left align mode
 *  		ignores the image and the gap
 *  		shortens the text by replacing the center portion of the label with an ellipsis
 *  		shortens the text by removing the center portion of the label
 *  </pre>
 *  <dl>
 *  <dt><b>Styles:</b>
 *  <dd>LEFT, RIGHT, CENTER, SHADOW_IN, SHADOW_OUT, SHADOW_NONE</dd>
 *  <dt><b>Events:</b>
 *  <dd>(NONE)</dd>
 *  </dl>
 *
 * <p>
 *  This class may be subclassed for the purpose of overriding the default string
 *  shortening algorithm that is implemented in method <code>shortenText()</code>.
 *  </p>
 *
 *  @see <a href="http://www.eclipse.org/swt/examples.php">SWT Example: CustomControlExample</a>
 *  @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 *  @see CLabel#shortenText(GC, String, int)
 */
public class CLabel extends Canvas {

    /**
     * a string inserted in the middle of text that has been shortened
     */
    // The tooltip is used for two purposes - the application can set
    // a tooltip or the tooltip can be used to display the full text when the
    // the text has been truncated due to the label being too short.
    // The appToolTip stores the tooltip set by the application.  Control.tooltiptext
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
     * @param parent a widget which will be the parent of the new instance (cannot be null)
     * @param style the style of widget to construct
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
     * </ul>
     *
     * @see SWT#LEFT
     * @see SWT#RIGHT
     * @see SWT#CENTER
     * @see SWT#SHADOW_IN
     * @see SWT#SHADOW_OUT
     * @see SWT#SHADOW_NONE
     * @see #getStyle()
     */
    public CLabel(Composite parent, int style) {
        this(new nat.org.eclipse.swt.custom.CLabel((nat.org.eclipse.swt.widgets.Composite) (parent != null ? parent.getDelegate() : null), style));
    }

    public Point computeSize(int wHint, int hHint, boolean changed) {
        IPoint ret = getDelegate().computeSize(wHint, hHint, changed);
        return ret != null ? ret.getApi() : null;
    }

    /**
     * Returns the horizontal alignment.
     * The alignment style (LEFT, CENTER or RIGHT) is returned.
     *
     * @return SWT.LEFT, SWT.RIGHT or SWT.CENTER
     */
    public int getAlignment() {
        return getDelegate().getAlignment();
    }

    /**
     * Return the CLabel's bottom margin.
     *
     * @return the bottom margin of the label
     *
     * @since 3.6
     */
    public int getBottomMargin() {
        return getDelegate().getBottomMargin();
    }

    /**
     * Return the CLabel's image or <code>null</code>.
     *
     * @return the image of the label or null
     */
    public Image getImage() {
        IImage ret = getDelegate().getImage();
        return ret != null ? ret.getApi() : null;
    }

    /**
     * Return the CLabel's left margin.
     *
     * @return the left margin of the label
     *
     * @since 3.6
     */
    public int getLeftMargin() {
        return getDelegate().getLeftMargin();
    }

    /**
     * Return the CLabel's right margin.
     *
     * @return the right margin of the label
     *
     * @since 3.6
     */
    public int getRightMargin() {
        return getDelegate().getRightMargin();
    }

    public int getStyle() {
        return getDelegate().getStyle();
    }

    /**
     * Return the Label's text.
     *
     * @return the text of the label or null
     */
    public String getText() {
        return getDelegate().getText();
    }

    public String getToolTipText() {
        return getDelegate().getToolTipText();
    }

    /**
     * Return the CLabel's top margin.
     *
     * @return the top margin of the label
     *
     * @since 3.6
     */
    public int getTopMargin() {
        return getDelegate().getTopMargin();
    }

    /**
     * Set the horizontal alignment of the CLabel.
     * Use the values LEFT, CENTER and RIGHT to align image and text within the available space.
     *
     * @param align the alignment style of LEFT, RIGHT or CENTER
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the value of align is not one of SWT.LEFT, SWT.RIGHT or SWT.CENTER</li>
     * </ul>
     */
    public void setAlignment(int align) {
        getDelegate().setAlignment(align);
    }

    public void setBackground(Color color) {
        getDelegate().setBackground((color != null ? color.getDelegate() : null));
    }

    /**
     *  Specify a gradient of colours to be drawn in the background of the CLabel.
     *  <p>For example, to draw a gradient that varies from dark blue to blue and then to
     *  white and stays white for the right half of the label, use the following call
     *  to setBackground:</p>
     *  <pre>
     * 	clabel.setBackground(new Color[]{display.getSystemColor(SWT.COLOR_DARK_BLUE),
     * 		                           display.getSystemColor(SWT.COLOR_BLUE),
     * 		                           display.getSystemColor(SWT.COLOR_WHITE),
     * 		                           display.getSystemColor(SWT.COLOR_WHITE)},
     * 		               new int[] {25, 50, 100});
     *  </pre>
     *
     *  @param colors an array of Color that specifies the colors to appear in the gradient
     *                in order of appearance from left to right;  The value <code>null</code>
     *                clears the background gradient; the value <code>null</code> can be used
     *                inside the array of Color to specify the background color.
     *  @param percents an array of integers between 0 and 100 specifying the percent of the width
     *                  of the widget at which the color should change; the size of the percents
     *                  array must be one less than the size of the colors array.
     *
     *  @exception SWTException <ul>
     *     <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *     <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *     <li>ERROR_INVALID_ARGUMENT - if the values of colors and percents are not consistent</li>
     *  </ul>
     */
    public void setBackground(Color[] colors, int[] percents) {
        getDelegate().setBackground(Convert.array(colors, Color::getDelegate, IColor[]::new), percents);
    }

    /**
     *  Specify a gradient of colours to be drawn in the background of the CLabel.
     *  <p>For example, to draw a gradient that varies from dark blue to white in the vertical,
     *  direction use the following call
     *  to setBackground:</p>
     *  <pre>
     * 	clabel.setBackground(new Color[]{display.getSystemColor(SWT.COLOR_DARK_BLUE),
     * 		                           display.getSystemColor(SWT.COLOR_WHITE)},
     * 		                 new int[] {100}, true);
     *  </pre>
     *
     *  @param colors an array of Color that specifies the colors to appear in the gradient
     *                in order of appearance from left/top to right/bottom;  The value <code>null</code>
     *                clears the background gradient; the value <code>null</code> can be used
     *                inside the array of Color to specify the background color.
     *  @param percents an array of integers between 0 and 100 specifying the percent of the width/height
     *                  of the widget at which the color should change; the size of the percents
     *                  array must be one less than the size of the colors array.
     *  @param vertical indicate the direction of the gradient.  True is vertical and false is horizontal.
     *
     *  @exception SWTException <ul>
     *     <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *     <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *     <li>ERROR_INVALID_ARGUMENT - if the values of colors and percents are not consistent</li>
     *  </ul>
     *
     *  @since 3.0
     */
    public void setBackground(Color[] colors, int[] percents, boolean vertical) {
        getDelegate().setBackground(Convert.array(colors, Color::getDelegate, IColor[]::new), percents, vertical);
    }

    /**
     * Set the image to be drawn in the background of the label.
     *
     * @param image the image to be drawn in the background
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setBackground(Image image) {
        getDelegate().setBackground((image != null ? image.getDelegate() : null));
    }

    /**
     * Set the label's bottom margin, in points.
     *
     * @param bottomMargin the bottom margin of the label, which must be equal to or greater than zero
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.6
     */
    public void setBottomMargin(int bottomMargin) {
        getDelegate().setBottomMargin(bottomMargin);
    }

    public void setFont(Font font) {
        getDelegate().setFont((font != null ? font.getDelegate() : null));
    }

    /**
     * Set the label's Image.
     * The value <code>null</code> clears it.
     *
     * @param image the image to be displayed in the label or null
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setImage(Image image) {
        getDelegate().setImage((image != null ? image.getDelegate() : null));
    }

    /**
     * Set the label's horizontal left margin, in points.
     *
     * @param leftMargin the left margin of the label, which must be equal to or greater than zero
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.6
     */
    public void setLeftMargin(int leftMargin) {
        getDelegate().setLeftMargin(leftMargin);
    }

    /**
     * Set the label's margins, in points.
     *
     * @param leftMargin the left margin.
     * @param topMargin the top margin.
     * @param rightMargin the right margin.
     * @param bottomMargin the bottom margin.
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.6
     */
    public void setMargins(int leftMargin, int topMargin, int rightMargin, int bottomMargin) {
        getDelegate().setMargins(leftMargin, topMargin, rightMargin, bottomMargin);
    }

    /**
     * Set the label's right margin, in points.
     *
     * @param rightMargin the right margin of the label, which must be equal to or greater than zero
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.6
     */
    public void setRightMargin(int rightMargin) {
        getDelegate().setRightMargin(rightMargin);
    }

    /**
     * Set the label's text.
     * The value <code>null</code> clears it.
     * <p>
     * Mnemonics are indicated by an '&amp;' that causes the next
     * character to be the mnemonic.  When the user presses a
     * key sequence that matches the mnemonic, focus is assigned
     * to the control that follows the label. On most platforms,
     * the mnemonic appears underlined but may be emphasised in a
     * platform specific manner.  The mnemonic indicator character
     * '&amp;' can be escaped by doubling it in the string, causing
     * a single '&amp;' to be displayed.
     * </p><p>
     * Note: If control characters like '\n', '\t' etc. are used
     * in the string, then the behavior is platform dependent.
     * </p>
     *
     * @param text the text to be displayed in the label or null
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setText(String text) {
        getDelegate().setText(text);
    }

    public void setToolTipText(String string) {
        getDelegate().setToolTipText(string);
    }

    /**
     * Set the label's top margin, in points.
     *
     * @param topMargin the top margin of the label, which must be equal to or greater than zero
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.6
     */
    public void setTopMargin(int topMargin) {
        getDelegate().setTopMargin(topMargin);
    }

    /**
     * Shorten the given text <code>t</code> so that its length doesn't exceed
     * the given width. The default implementation replaces characters in the
     * center of the original string with an ellipsis ("...").
     * Override if you need a different strategy.
     *
     * @param gc the gc to use for text measurement
     * @param t the text to shorten
     * @param width the width to shorten the text to, in points
     * @return the shortened text
     */
    protected String shortenText(GC gc, String t, int width) {
        return getDelegate().shortenText((gc != null ? gc.getDelegate() : null), t, width);
    }

    protected CLabel(ICLabel delegate) {
        super(delegate);
    }

    public static CLabel createApi(ICLabel delegate) {
        return new CLabel(delegate);
    }

    public ICLabel getDelegate() {
        return (ICLabel) super.getDelegate();
    }
}
