package org.eclipse.swt.custom;

import org.eclipse.swt.*;
import org.eclipse.swt.accessibility.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public interface ICLabel extends ICanvas {

    IPoint computeSize(int wHint, int hHint, boolean changed);

    /**
     * Returns the horizontal alignment.
     * The alignment style (LEFT, CENTER or RIGHT) is returned.
     *
     * @return SWT.LEFT, SWT.RIGHT or SWT.CENTER
     */
    int getAlignment();

    /**
     * Return the CLabel's bottom margin.
     *
     * @return the bottom margin of the label
     *
     * @since 3.6
     */
    int getBottomMargin();

    /**
     * Return the CLabel's image or <code>null</code>.
     *
     * @return the image of the label or null
     */
    IImage getImage();

    /**
     * Return the CLabel's left margin.
     *
     * @return the left margin of the label
     *
     * @since 3.6
     */
    int getLeftMargin();

    /**
     * Return the CLabel's right margin.
     *
     * @return the right margin of the label
     *
     * @since 3.6
     */
    int getRightMargin();

    int getStyle();

    /**
     * Return the Label's text.
     *
     * @return the text of the label or null
     */
    String getText();

    String getToolTipText();

    /**
     * Return the CLabel's top margin.
     *
     * @return the top margin of the label
     *
     * @since 3.6
     */
    int getTopMargin();

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
    void setAlignment(int align);

    void setBackground(IColor color);

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
    void setBackground(IColor[] colors, int[] percents);

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
    void setBackground(IColor[] colors, int[] percents, boolean vertical);

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
    void setBackground(IImage image);

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
    void setBottomMargin(int bottomMargin);

    void setFont(IFont font);

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
    void setImage(IImage image);

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
    void setLeftMargin(int leftMargin);

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
    void setMargins(int leftMargin, int topMargin, int rightMargin, int bottomMargin);

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
    void setRightMargin(int rightMargin);

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
    void setText(String text);

    void setToolTipText(String string);

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
    void setTopMargin(int topMargin);

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
    String shortenText(IGC gc, String t, int width);

    CLabel getApi();
}
