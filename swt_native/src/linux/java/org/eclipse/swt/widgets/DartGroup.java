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
package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.internal.*;
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

    long clientHandle, labelHandle;

    String text = "";

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
    long clientHandle() {
        return clientHandle;
    }

    @Override
    Point computeSizeInPixels(int wHint, int hHint, boolean changed) {
        return Sizes.compute(this);
    }

    @Override
    Rectangle computeTrimInPixels(int x, int y, int width, int height) {
        checkWidget();
        forceResize();
        return new Rectangle(x, y, width, height);
    }

    @Override
    Rectangle getClientAreaInPixels() {
        Rectangle clientRectangle = super.getClientAreaInPixels();
        /*
	* Bug 453827 Child position fix.
	* SWT's calls to gtk_widget_size_allocate and gtk_widget_set_allocation
	* causes GTK+ to move the clientHandle's SwtFixed down by the size of the label.
	* These calls can come up from 'shell' and group has no control over these calls.
	*
	* This is an undesired side-effect. Client handle's x & y positions should never
	* be incremented as this is an internal sub-container.
	*
	* Note: 0 by 0 was chosen as 1 by 1 shifts controls beyond their original pos.
	* The long term fix would be to not use widget_*_allocation from higher containers
	* like shell and to not use	gtkframe in non-group widgets (e.g used in label atm).
	*/
        clientRectangle.x = 0;
        clientRectangle.y = 0;
        return clientRectangle;
    }

    @Override
    void createHandle(int index) {
        // In GTK 3 font description is inherited from parent widget which is not how SWT has always worked,
    }

    @Override
    int applyThemeBackground() {
        return 1;
    }

    @Override
    void deregister() {
        super.deregister();
        ((SwtDisplay) display.getImpl()).removeWidget(clientHandle);
        ((SwtDisplay) display.getImpl()).removeWidget(labelHandle);
    }

    @Override
    void enableWidget(boolean enabled) {
    }

    @Override
    long eventHandle() {
        /*
	 * Bug 453827 - Group's events should be handled via it's internal
	 * fixed container (clientHandle) and not via it's top level.
	 * This makes it behave more like composite.
	 */
        return clientHandle;
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
    void hookEvents() {
        super.hookEvents();
        if (labelHandle != 0) {
        }
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
    public long parentingHandle() {
        /*
	 * Bug 453827 - Children should be attached to the internal fixed
	 * subcontainer (clienthandle) and not the top-level fixedHandle.
	 */
        return clientHandle;
    }

    @Override
    void register() {
        super.register();
        ((SwtDisplay) display.getImpl()).addWidget(clientHandle, this.getApi());
        ((SwtDisplay) display.getImpl()).addWidget(labelHandle, this.getApi());
    }

    @Override
    void releaseHandle() {
        super.releaseHandle();
        clientHandle = labelHandle = 0;
    }

    @Override
    void releaseWidget() {
        super.releaseWidget();
        text = null;
    }

    @Override
    void setFontDescription(long font) {
        super.setFontDescription(font);
        setFontDescription(labelHandle, font);
    }

    @Override
    void setOrientation(boolean create) {
        super.setOrientation(create);
        if ((getApi().style & SWT.RIGHT_TO_LEFT) != 0 || !create) {
        }
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
        if (string.length() != 0) {
        } else {
        }
    }

    @Override
    void showWidget() {
        super.showWidget();
    }

    @Override
    int setBounds(int x, int y, int width, int height, boolean move, boolean resize) {
        // Work around for bug 470129.
        // See also https://bugzilla.gnome.org/show_bug.cgi?id=754976 :
        // GtkFrame: Attempt to allocate size of width 1 (or a small number) fails
        //
        return super.setBounds(x, y, width, height, move, resize);
    }

    @Override
    long paintHandle() {
        long topHandle = topHandle();
        /* we draw all our children on the clientHandle*/
        long paintHandle = clientHandle;
        while (paintHandle != topHandle) {
        }
        return paintHandle;
    }

    @Override
    long paintWindow() {
        return 0;
    }

    @Override
    long paintSurface() {
        return 0;
    }

    public long _clientHandle() {
        return clientHandle;
    }

    public long _labelHandle() {
        return labelHandle;
    }

    public String _text() {
        return text;
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
