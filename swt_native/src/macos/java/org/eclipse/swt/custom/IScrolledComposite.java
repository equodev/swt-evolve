package org.eclipse.swt.custom;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public interface IScrolledComposite extends IComposite {

    /**
     * Returns the Always Show Scrollbars flag.  True if the scrollbars are
     * always shown even if they are not required.  False if the scrollbars are only
     * visible when some part of the composite needs to be scrolled to be seen.
     * The H_SCROLL and V_SCROLL style bits are also required to enable scrollbars in the
     * horizontal and vertical directions.
     *
     * @return the Always Show Scrollbars flag value
     */
    boolean getAlwaysShowScrollBars();

    /**
     * Returns <code>true</code> if the content control
     * will be expanded to fill available horizontal space.
     *
     * @return the receiver's horizontal expansion state
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.2
     */
    boolean getExpandHorizontal();

    /**
     * Returns <code>true</code> if the content control
     * will be expanded to fill available vertical space.
     *
     * @return the receiver's vertical expansion state
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.2
     */
    boolean getExpandVertical();

    /**
     * Returns the minimum width of the content control.
     *
     * @return the minimum width
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.2
     */
    int getMinWidth();

    /**
     * Returns the minimum height of the content control.
     *
     * @return the minimum height
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.2
     */
    int getMinHeight();

    /**
     * Get the content that is being scrolled.
     *
     * @return the control displayed in the content area
     */
    Control getContent();

    /**
     * Returns <code>true</code> if the receiver automatically scrolls to a focused child control
     * to make it visible. Otherwise, returns <code>false</code>.
     *
     * @return a boolean indicating whether focused child controls are automatically scrolled into the viewport
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.4
     */
    boolean getShowFocusedControl();

    /**
     * Return the point in the content that currently appears in the top left
     * corner of the scrolled composite.
     *
     * @return the point in the content that currently appears in the top left
     * corner of the scrolled composite.  If no content has been set, this returns
     * (0, 0).
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 2.0
     */
    Point getOrigin();

    /**
     * Scrolls the content so that the specified point in the content is in the top
     * left corner.  If no content has been set, nothing will occur.
     *
     * Negative values will be ignored.  Values greater than the maximum scroll
     * distance will result in scrolling to the end of the scrollbar.
     *
     * @param origin the point on the content to appear in the top left corner
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *    <li>ERROR_INVALID_ARGUMENT - value of origin is outside of content
     * </ul>
     * @since 2.0
     */
    void setOrigin(Point origin);

    /**
     * Scrolls the content so that the specified point in the content is in the top
     * left corner.  If no content has been set, nothing will occur.
     *
     * Negative values will be ignored.  Values greater than the maximum scroll
     * distance will result in scrolling to the end of the scrollbar.
     *
     * @param x the x coordinate of the content to appear in the top left corner
     *
     * @param y the y coordinate of the content to appear in the top left corner
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 2.0
     */
    void setOrigin(int x, int y);

    /**
     * Set the Always Show Scrollbars flag.  True if the scrollbars are
     * always shown even if they are not required.  False if the scrollbars are only
     * visible when some part of the composite needs to be scrolled to be seen.
     * The H_SCROLL and V_SCROLL style bits are also required to enable scrollbars in the
     * horizontal and vertical directions.
     *
     * @param show true to show the scrollbars even when not required, false to show scrollbars only when required
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    void setAlwaysShowScrollBars(boolean show);

    /**
     * Set the content that will be scrolled.
     *
     * @param content the control to be displayed in the content area
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    void setContent(Control content);

    /**
     * Configure the ScrolledComposite to resize the content object to be as wide as the
     * ScrolledComposite when the width of the ScrolledComposite is greater than the
     * minimum width specified in setMinWidth.  If the ScrolledComposite is less than the
     * minimum width, the content will not be resized and instead the horizontal scroll bar will be
     * used to view the entire width.
     * If expand is false, this behaviour is turned off.  By default, this behaviour is turned off.
     *
     * @param expand true to expand the content control to fill available horizontal space
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    void setExpandHorizontal(boolean expand);

    /**
     * Configure the ScrolledComposite to resize the content object to be as tall as the
     * ScrolledComposite when the height of the ScrolledComposite is greater than the
     * minimum height specified in setMinHeight.  If the ScrolledComposite is less than the
     * minimum height, the content will not be resized and instead the vertical scroll bar will be
     * used to view the entire height.
     * If expand is false, this behaviour is turned off.  By default, this behaviour is turned off.
     *
     * @param expand true to expand the content control to fill available vertical space
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    void setExpandVertical(boolean expand);

    /**
     * Sets the layout which is associated with the receiver to be
     * the argument which may be null.
     * <p>
     * Note: No Layout can be set on this Control because it already
     * manages the size and position of its children.
     * </p>
     *
     * @param layout the receiver's new layout or null
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    void setLayout(Layout layout);

    /**
     * Specify the minimum height at which the ScrolledComposite will begin scrolling the
     * content with the vertical scroll bar.  This value is only relevant if
     * setExpandVertical(true) has been set.
     *
     * @param height the minimum height or 0 for default height
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    void setMinHeight(int height);

    /**
     * Specify the minimum width and height at which the ScrolledComposite will begin scrolling the
     * content with the horizontal scroll bar.  This value is only relevant if
     * setExpandHorizontal(true) and setExpandVertical(true) have been set.
     *
     * @param size the minimum size or null for the default size
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    void setMinSize(Point size);

    /**
     * Specify the minimum width and height at which the ScrolledComposite will begin scrolling the
     * content with the horizontal scroll bar.  This value is only relevant if
     * setExpandHorizontal(true) and setExpandVertical(true) have been set.
     *
     * @param width the minimum width or 0 for default width
     * @param height the minimum height or 0 for default height
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    void setMinSize(int width, int height);

    /**
     * Specify the minimum width at which the ScrolledComposite will begin scrolling the
     * content with the horizontal scroll bar.  This value is only relevant if
     * setExpandHorizontal(true) has been set.
     *
     * @param width the minimum width or 0 for default width
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    void setMinWidth(int width);

    /**
     * Configure the receiver to automatically scroll to a focused child control
     * to make it visible.
     *
     * If show is <code>false</code>, show a focused control is off.
     * By default, show a focused control is off.
     *
     * @param show <code>true</code> to show a focused control.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.4
     */
    void setShowFocusedControl(boolean show);

    /**
     * Scrolls the content of the receiver so that the control is visible.
     *
     * @param control the control to be shown
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the control is null</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the control has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.4
     */
    void showControl(Control control);

    ScrolledComposite getApi();
}
