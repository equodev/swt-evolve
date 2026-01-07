/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2022 IBM Corporation and others.
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
 *      Conrad Groth - Bug 401015 - [CSS] Add support for styling hyperlinks in Links
 * *****************************************************************************
 */
package org.eclipse.swt.widgets;

import java.util.*;
import org.eclipse.swt.*;
import org.eclipse.swt.accessibility.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.internal.*;
import dev.equo.swt.*;

/**
 * Instances of this class represent a selectable
 * user interface object that displays a text with
 * links.
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>(none)</dd>
 * <dt><b>Events:</b></dt>
 * <dd>Selection</dd>
 * </dl>
 * <p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 *
 * @see <a href="http://www.eclipse.org/swt/snippets/#link">Link snippets</a>
 * @see <a href="http://www.eclipse.org/swt/examples.php">SWT Example: ControlExample</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 *
 * @since 3.1
 * @noextend This class is not intended to be subclassed by clients.
 */
public class DartLink extends DartControl implements ILink {

    String text;

    int linkForeground = -1;

    String[] ids;

    char[] mnemonics;

    int nextFocusItem = -1;

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
     * @see Widget#checkSubclass
     * @see Widget#getStyle
     */
    public DartLink(Composite parent, int style, Link api) {
        super(parent, style, api);
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
        return Sizes.computeSize(this, wHint, hHint, changed);
    }

    @Override
    void createHandle() {
    }

    @Override
    void createWidget() {
        super.createWidget();
        text = "";
        ids = new String[0];
        mnemonics = new char[0];
        /*
	 * Accessible tool like JAWS by default only reads the hypertext link text and
	 * not the non-link text. To make JAWS read the full text we need to tweak the
	 * default behavior and explicitly return the full link text as below.
	 */
        this.getAccessible().addAccessibleListener(new AccessibleAdapter() {

            @Override
            public void getName(AccessibleEvent e) {
                e.result = text;
            }
        });
    }

    @Override
    void enableWidget(boolean enabled) {
        super.enableWidget(enabled);
    }

    int getFocusItem() {
        return -1;
    }

    /**
     * Returns the link foreground color.
     *
     * @return the receiver's link foreground color.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @since 3.105
     */
    public Color getLinkForeground() {
        checkWidget();
        return this._linkForeground;
    }

    @Override
    String getNameText() {
        return getText();
    }

    /**
     * Returns the receiver's text, which will be an empty
     * string if it has never been set.
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
        return text;
    }

    @Override
    boolean mnemonicHit(char key) {
        char uckey = Character.toUpperCase(key);
        for (int i = 0; i < mnemonics.length; i++) {
            if (uckey == mnemonics[i]) {
                nextFocusItem = i;
                return setFocus() && setFocusItem(i);
            }
        }
        return false;
    }

    @Override
    boolean mnemonicMatch(char key) {
        char uckey = Character.toUpperCase(key);
        for (char mnemonic : mnemonics) {
            if (uckey == mnemonic) {
                return true;
            }
        }
        return false;
    }

    void parse(String string) {
        int length = string.length();
        // The shortest link length is 7 characters (<a></a>).
        ids = new String[length / 7];
        mnemonics = new char[length / 7];
        int index = 0, state = 0, linkIndex = 0;
        int linkStart = 0, linkEnd = 0, refStart = 0, refEnd = 0;
        char mnemonic = 0;
        while (index < length) {
            char c = Character.toLowerCase(string.charAt(index));
            switch(state) {
                case 0:
                    if (c == '<') {
                        state++;
                    } else if (c == '&') {
                        state = 16;
                    }
                    break;
                case 1:
                    if (c == 'a')
                        state++;
                    break;
                case 2:
                    switch(c) {
                        case 'h':
                            state = 7;
                            break;
                        case '>':
                            linkStart = index + 1;
                            state++;
                            break;
                        default:
                            if (Character.isWhitespace(c))
                                break;
                            else
                                state = 13;
                    }
                    break;
                case 3:
                    if (c == '<') {
                        linkEnd = index;
                        state++;
                    }
                    break;
                case 4:
                    state = c == '/' ? state + 1 : 3;
                    break;
                case 5:
                    state = c == 'a' ? state + 1 : 3;
                    break;
                case 6:
                    if (c == '>') {
                        if (refStart == 0) {
                            refStart = linkStart;
                            refEnd = linkEnd;
                        }
                        ids[linkIndex] = string.substring(refStart, refEnd);
                        if (mnemonic != 0) {
                            mnemonics[linkIndex] = mnemonic;
                        }
                        linkIndex++;
                        linkStart = linkEnd = refStart = refEnd = mnemonic = 0;
                        state = 0;
                    } else {
                        state = 3;
                    }
                    break;
                case 7:
                    state = c == 'r' ? state + 1 : 0;
                    break;
                case 8:
                    state = c == 'e' ? state + 1 : 0;
                    break;
                case 9:
                    state = c == 'f' ? state + 1 : 0;
                    break;
                case 10:
                    state = c == '=' ? state + 1 : 0;
                    break;
                case 11:
                    if (c == '"') {
                        state++;
                        refStart = index + 1;
                    } else {
                        state = 0;
                    }
                    break;
                case 12:
                    if (c == '"') {
                        refEnd = index;
                        state = 2;
                    }
                    break;
                case 13:
                    if (Character.isWhitespace(c)) {
                        state = 0;
                    } else if (c == '=') {
                        state++;
                    }
                    break;
                case 14:
                    state = c == '"' ? state + 1 : 0;
                    break;
                case 15:
                    if (c == '"')
                        state = 2;
                    break;
                case 16:
                    if (c == '<') {
                        state = 1;
                    } else {
                        state = 0;
                        if (c != '&')
                            mnemonic = Character.toUpperCase(c);
                    }
                    break;
                default:
                    state = 0;
                    break;
            }
            index++;
        }
        ids = Arrays.copyOf(ids, linkIndex);
        mnemonics = Arrays.copyOf(mnemonics, linkIndex);
    }

    @Override
    void releaseWidget() {
        super.releaseWidget();
        ids = null;
        mnemonics = null;
        text = null;
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

    boolean setFocusItem(int index) {
        if (index > 0) {
        }
        int activeIndex = getFocusItem();
        if (activeIndex == index)
            return true;
        if (activeIndex >= 0) {
        }
        if (index > 0) {
        }
        return false;
    }

    /**
     * Sets the link foreground color to the color specified
     * by the argument, or to the default system color for the link
     * if the argument is null.
     * <p>
     * Note: This operation is a hint and may be overridden by the platform.
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
     * @since 3.105
     */
    public void setLinkForeground(Color color) {
        Color newValue = color;
        if (!java.util.Objects.equals(this._linkForeground, newValue)) {
            dirty();
        }
        checkWidget();
        int pixel = -1;
        if (color != null) {
            if (color.isDisposed())
                error(SWT.ERROR_INVALID_ARGUMENT);
            pixel = color.handle;
        }
        if (pixel == linkForeground)
            return;
        linkForeground = pixel;
        this._linkForeground = newValue;
    }

    /**
     * Sets the receiver's text.
     * <p>
     * The string can contain both regular text and hyperlinks.  A hyperlink
     * is delimited by an anchor tag, &lt;a&gt; and &lt;/a&gt;.  Within an
     * anchor, a single HREF attribute is supported.  When a hyperlink is
     * selected, the text field of the selection event contains either the
     * text of the hyperlink or the value of its HREF, if one was specified.
     * In the rare case of identical hyperlinks within the same string, the
     * HREF attribute can be used to distinguish between them.  The string may
     * include the mnemonic character and line delimiters. The only delimiter
     * the HREF attribute supports is the quotation mark ("). Text containing
     * angle-bracket characters &lt; or &gt; may be escaped using \\, however
     * this operation is a hint and varies from platform to platform.
     * </p>
     * <p>
     * Mnemonics are indicated by an '&amp;' that causes the next
     * character to be the mnemonic. The receiver can have a
     * mnemonic in the text preceding each link. When the user presses a
     * key sequence that matches the mnemonic, focus is assigned
     * to the link that follows the text. Mnemonics in links and in
     * the trailing text are ignored. On most platforms,
     * the mnemonic appears underlined but may be emphasised in a
     * platform specific manner.  The mnemonic indicator character
     * '&amp;' can be escaped by doubling it in the string, causing
     * a single '&amp;' to be displayed.
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
        if (string.equals(text))
            return;
        text = string;
        if ((getApi().state & HAS_AUTO_DIRECTION) != 0) {
            updateTextDirection(AUTO_TEXT_DIRECTION);
        }
        parse(string);
    }

    @Override
    int resolveTextDirection() {
        return 0;
    }

    @Override
    boolean updateTextDirection(int textDirection) {
        if (super.updateTextDirection(textDirection)) {
            int flags = SWT.RIGHT_TO_LEFT | SWT.LEFT_TO_RIGHT;
            getApi().style &= ~SWT.MIRRORED;
            getApi().style &= ~flags;
            getApi().style |= textDirection & flags;
            updateOrientation();
            checkMirrored();
            return true;
        }
        return false;
    }

    @Override
    int widgetStyle() {
        return 0;
    }

    Color _linkForeground;

    public String _text() {
        return text;
    }

    public int _linkForeground() {
        return linkForeground;
    }

    public String[] _ids() {
        return ids;
    }

    public char[] _mnemonics() {
        return mnemonics;
    }

    public int _nextFocusItem() {
        return nextFocusItem;
    }

    public Color __linkForeground() {
        return _linkForeground;
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

    public Link getApi() {
        if (api == null)
            api = Link.createApi(this);
        return (Link) api;
    }

    public VLink getValue() {
        if (value == null)
            value = new VLink(this);
        return (VLink) value;
    }
}
