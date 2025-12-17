/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2012 IBM Corporation and others.
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
import java.util.Objects;
import dev.equo.swt.*;

/**
 * Instances of this class provide an etched border
 * with an optional title.
 * <p>
 * Shadow styles are hints and may not be honoured
 * by the platform.  To create a group with the
 * default shadow style for the platform, do not
 * specify a shadow style.
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>SHADOW_ETCHED_IN, SHADOW_ETCHED_OUT, SHADOW_IN, SHADOW_OUT, SHADOW_NONE</dd>
 * <dt><b>Events:</b></dt>
 * <dd>(none)</dd>
 * </dl>
 * <p>
 * Note: Only one of the above styles may be specified.
 * </p><p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 *
 * @see <a href="http://www.eclipse.org/swt/examples.php">SWT Example: ControlExample</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class DartGroup extends DartComposite implements IGroup {

    String text = "";

    boolean ignoreResize;

    int hMargin, vMargin;

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
     * @see SWT#SHADOW_ETCHED_IN
     * @see SWT#SHADOW_ETCHED_OUT
     * @see SWT#SHADOW_IN
     * @see SWT#SHADOW_OUT
     * @see SWT#SHADOW_NONE
     * @see Widget#checkSubclass
     * @see Widget#getStyle
     */
    public DartGroup(Composite parent, int style, Group api) {
        super(parent, checkStyle(style), api);
    }

    static int checkStyle(int style) {
        style |= SWT.NO_FOCUS;
        /*
	* Even though it is legal to create this widget
	* with scroll bars, they serve no useful purpose
	* because they do not automatically scroll the
	* widget's client area.  The fix is to clear
	* the SWT style.
	*/
        return style & ~(SWT.H_SCROLL | SWT.V_SCROLL);
    }

    @Override
    public void checkSubclass() {
        if (!isValidSubclass())
            error(SWT.ERROR_INVALID_SUBCLASS);
    }

    @Override
    public Rectangle computeTrim(int x, int y, int width, int height) {
        checkWidget();
        ignoreResize = true;
        ignoreResize = false;
        return super.computeTrim(x, y, width, height);
    }

    @Override
    void createHandle() {
    }

    @Override
    void deregister() {
        super.deregister();
    }

    @Override
    public Rectangle getClientArea() {
        checkWidget();
        return getBounds();
    }

    @Override
    String getNameText() {
        return getText();
    }

    /**
     * Returns the receiver's text, which is the string that the
     * is used as the <em>title</em>. If the text has not previously
     * been set, returns an empty string.
     *
     * @return the text
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public String getText() {
        checkWidget();
        return text;
    }

    @Override
    public boolean isTransparent() {
        return true;
    }

    @Override
    public float getThemeAlpha() {
        return (background != null ? 1 : 0.25f) * parent.getImpl().getThemeAlpha();
    }

    @Override
    void register() {
        super.register();
    }

    @Override
    void releaseHandle() {
        super.releaseHandle();
    }

    @Override
    void resized() {
        if (!ignoreResize)
            super.resized();
    }

    @Override
    void setForeground(double[] color) {
        if (color == null) {
        } else {
        }
    }

    @Override
    void setOrientation() {
    }

    /**
     * Sets the receiver's text, which is the string that will
     * be displayed as the receiver's <em>title</em>, to the argument,
     * which may not be null. The string may include the mnemonic character.
     * <p>
     * Mnemonics are indicated by an '&amp;' that causes the next
     * character to be the mnemonic.  When the user presses a
     * key sequence that matches the mnemonic, focus is assigned
     * to the first child of the group. On most platforms, the
     * mnemonic appears underlined but may be emphasised in a
     * platform specific manner.  The mnemonic indicator character
     * '&amp;' can be escaped by doubling it in the string, causing
     * a single '&amp;' to be displayed.
     * </p><p>
     * Note: If control characters like '\n', '\t' etc. are used
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
        checkWidget();
        if (!java.util.Objects.equals(this.text, string)) {
            dirty();
        }
        if (string == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        text = string;
        char[] buffer = new char[text.length()];
        text.getChars(0, buffer.length, buffer, 0);
    }

    public String _text() {
        return text;
    }

    public boolean _ignoreResize() {
        return ignoreResize;
    }

    public int _hMargin() {
        return hMargin;
    }

    public int _vMargin() {
        return vMargin;
    }

    @Override
    public Point computeSize(int wHint, int hHint, boolean changed) {
        checkWidget();
        return Sizes.compute(this);
    }

    protected void _hookEvents() {
        super._hookEvents();
    }

    public Group getApi() {
        if (api == null)
            api = Group.createApi(this);
        return (Group) api;
    }

    public VGroup getValue() {
        if (value == null)
            value = new VGroup(this);
        return (VGroup) value;
    }
}
