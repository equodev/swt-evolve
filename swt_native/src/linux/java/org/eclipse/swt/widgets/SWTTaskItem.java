/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2009 IBM Corporation and others.
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
import org.eclipse.swt.graphics.*;

/**
 * Instances of this class represent a task item.
 *
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>(none)</dd>
 * <dt><b>Events:</b></dt>
 * <dd>(none)</dd>
 * </dl>
 *
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 *
 * @since 3.6
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class SWTTaskItem extends SWTItem implements ITaskItem {

    SWTTaskBar parent;

    SWTShell shell;

    int progress, progressState = SWT.DEFAULT;

    Image overlayImage;

    String overlayText = "";

    SWTMenu menu;

    static final int PROGRESS_MAX = 100;

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
    SWTTaskItem(SWTTaskBar parent, int style) {
        super(parent, style);
        this.parent = parent;
        parent.createItem(this, -1);
    }

    @Override
    protected void checkSubclass() {
        if (!isValidSubclass())
            error(SWT.ERROR_INVALID_SUBCLASS);
    }

    @Override
    void destroyWidget() {
        parent.destroyItem(this);
        releaseHandle();
    }

    /**
     * Returns the receiver's pop up menu if it has one, or null
     * if it does not.
     *
     * @return the receiver's menu
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public IMenu getMenu() {
        checkWidget();
        return menu;
    }

    /**
     * Returns the receiver's overlay image if it has one, or null
     * if it does not.
     *
     * @return the receiver's overlay image
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public Image getOverlayImage() {
        checkWidget();
        return overlayImage;
    }

    /**
     * Returns the receiver's overlay text, which will be an empty
     * string if it has never been set.
     *
     * @return the receiver's overlay text
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public String getOverlayText() {
        checkWidget();
        return overlayText;
    }

    /**
     * Returns the receiver's parent, which must be a <code>TaskBar</code>.
     *
     * @return the receiver's parent
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public ITaskBar getParent() {
        checkWidget();
        return parent;
    }

    /**
     * Returns the receiver's progress.
     *
     * @return the receiver's progress
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public int getProgress() {
        checkWidget();
        return progress;
    }

    /**
     * Returns the receiver's progress state.
     *
     * @return the receiver's progress state
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public int getProgressState() {
        checkWidget();
        return progressState;
    }

    @Override
    void releaseHandle() {
        super.releaseHandle();
        parent = null;
    }

    @Override
    void releaseWidget() {
        super.releaseWidget();
        overlayImage = null;
        overlayText = null;
    }

    /**
     * Sets the receiver's pop up menu to the argument. The way the menu is
     * shown is platform specific.
     *
     * <p>
     * This feature might not be available for the receiver on all
     * platforms. The application code can check if it is supported
     * by calling the respective get method. When the feature is not
     * available, the get method will always return the NULL.</p>
     *
     * <p>
     * For better cross platform support, the application code should
     * set this feature on the <code>TaskItem</code> for application.<br>
     * On Windows, this feature will only work on RCP applications.</p>
     *
     * <p>
     * The menu should be fully created before this method is called.
     * Dynamic changes to the menu after the method is called will not be reflected
     * in the native menu.</p>
     *
     * @param menu the new pop up menu
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_MENU_NOT_POP_UP - the menu is not a pop up menu</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the menu has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setMenu(IMenu menu_) {
        SWTMenu menu = (SWTMenu) menu_;
        checkWidget();
        if (menu != null) {
            if (menu.isDisposed())
                SWT.error(SWT.ERROR_INVALID_ARGUMENT);
            if ((menu.style & SWT.POP_UP) == 0) {
                error(SWT.ERROR_MENU_NOT_POP_UP);
            }
        }
        this.menu = menu;
    }

    /**
     * Sets the receiver's overlay image, which may be null
     * indicating that no image should be displayed. The bounds
     * for the overlay image is determined by the platform and in
     * general it should be a small image.
     *
     * <p>
     * This feature might not be available for the receiver on all
     * platforms. The application code can check if it is supported
     * by calling the respective get method. When the feature is not
     * available, the get method will always return the NULL.</p>
     *
     * <p>
     * For better cross platform support, the application code should
     * first try to set this feature on the <code>TaskItem</code> for the
     * main shell then on the <code>TaskItem</code> for the application.</p>
     *
     * @param overlayImage the new overlay image (may be null)
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the overlayImage has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setOverlayImage(Image overlayImage) {
        checkWidget();
        if (overlayImage != null && overlayImage.isDisposed())
            error(SWT.ERROR_INVALID_ARGUMENT);
        this.overlayImage = overlayImage;
    }

    /**
     * Sets the receiver's overlay text. The space available to display the
     * overlay text is platform dependent and in general it should be no longer
     * than a few characters.
     *
     * <p>
     * This feature might not be available for the receiver on all
     * platforms. The application code can check if it is supported
     * by calling the respective get method. When the feature is not
     * available, the get method will always return an empty string.</p>
     *
     * <p>
     * For better cross platform support, the application code should
     * first try to set this feature on the <code>TaskItem</code> for the
     * main shell then on the <code>TaskItem</code> for the application.</p>
     *
     * @param overlayText the new overlay text
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the overlayText is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setOverlayText(String overlayText) {
        checkWidget();
        if (overlayText == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        this.overlayText = overlayText;
    }

    /**
     * Sets the receiver's progress state, the state can be one of
     * the following:
     * <ul>
     * <li>{@link SWT#DEFAULT}</li>
     * <li>{@link SWT#NORMAL}</li>
     * <li>{@link SWT#PAUSED}</li>
     * <li>{@link SWT#ERROR}</li>
     * <li>{@link SWT#INDETERMINATE}</li>
     * </ul>
     *
     * The percentage of progress shown by the states <code>SWT#NORMAL</code>, <code>SWT#PAUSED</code>,
     * <code>SWT#ERROR</code> is set with <code>setProgress()</code>. <br>
     * The state <code>SWT#DEFAULT</code> indicates that no progress should be shown.
     *
     * <p>
     * This feature might not be available for the receiver on all
     * platforms. The application code can check if it is supported
     * by calling the respective get method. When the feature is not
     * available, the get method will always return <code>SWT#DEFAULT</code>.</p>
     *
     * <p>
     * For better cross platform support, the application code should
     * first try to set this feature on the <code>TaskItem</code> for the
     * main shell then on the <code>TaskItem</code> for the application.</p>
     *
     * @param progressState the new progress state
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * #see {@link #setProgress(int)}
     */
    public void setProgressState(int progressState) {
        checkWidget();
        if (this.progressState == progressState)
            return;
        this.progressState = progressState;
    }

    /**
     * Sets the receiver's progress, the progress represents a percentage and
     * should be in range from 0 to 100. The progress is only shown when the progress
     * state is different than <code>SWT#DEFAULT</code>.
     *
     * <p>
     * This feature might not be available for the receiver on all
     * platforms. The application code can check if it is supported
     * by calling the respective get method. When the feature is not
     * available, the get method will always return zero.</p>
     *
     * <p>
     * For better cross platform support, the application code should
     * first try to set this feature on the <code>TaskItem</code> for the
     * main shell then on the <code>TaskItem</code> for the application.</p>
     *
     * @param progress the new progress
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * #see {@link #setProgressState(int)}
     */
    public void setProgress(int progress) {
        checkWidget();
        progress = Math.max(0, Math.min(progress, PROGRESS_MAX));
        if (this.progress == progress)
            return;
        this.progress = progress;
    }
}
