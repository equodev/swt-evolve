/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2017 IBM Corporation and others.
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

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.internal.*;
import org.eclipse.swt.widgets.Display.*;
import java.util.Objects;
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

    Point[] offsets;

    String[] ids;

    int[] mnemonics;

    double[] linkForeground;

    int focusIndex;

    boolean ignoreNextMouseUp;

    APPEARANCE lastAppAppearance;

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
    public Point computeSize(int wHint, int hHint, boolean changed) {
        return Sizes.computeSize(this, wHint, hHint, changed);
    }

    @Override
    void createHandle() {
    }

    @Override
    void createWidget() {
        super.createWidget();
        text = "";
        offsets = new Point[0];
        ids = new String[0];
        mnemonics = new int[0];
        focusIndex = -1;
    }

    @Override
    void deregister() {
        super.deregister();
    }

    @Override
    void enableWidget(boolean enabled) {
        super.enableWidget(enabled);
        setLinkColor(enabled);
    }

    @Override
    public Cursor findCursor() {
        Cursor cursor = super.findCursor();
        if (cursor != null)
            return cursor;
        return null;
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

    String parse(String string) {
        int length = string.length();
        offsets = new Point[length / 4];
        ids = new String[length / 4];
        mnemonics = new int[length / 4 + 1];
        StringBuilder result = new StringBuilder();
        char[] buffer = new char[length];
        string.getChars(0, string.length(), buffer, 0);
        int index = 0, state = 0, linkIndex = 0;
        int start = 0, tagStart = 0, linkStart = 0, endtagStart = 0, refStart = 0;
        while (index < length) {
            char c = Character.toLowerCase(buffer[index]);
            switch(state) {
                case 0:
                    if (c == '<') {
                        tagStart = index;
                        state++;
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
                        endtagStart = index;
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
                        mnemonics[linkIndex] = parseMnemonics(buffer, start, tagStart, result);
                        int offset = result.length();
                        parseMnemonics(buffer, linkStart, endtagStart, result);
                        offsets[linkIndex] = new Point(offset, result.length() - 1);
                        if (ids[linkIndex] == null) {
                            ids[linkIndex] = new String(buffer, linkStart, endtagStart - linkStart);
                        }
                        linkIndex++;
                        start = tagStart = linkStart = endtagStart = refStart = index + 1;
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
                        ids[linkIndex] = new String(buffer, refStart, index - refStart);
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
                default:
                    state = 0;
                    break;
            }
            index++;
        }
        if (start < length) {
            int tmp = parseMnemonics(buffer, start, tagStart, result);
            int mnemonic = parseMnemonics(buffer, Math.max(tagStart, linkStart), length, result);
            if (mnemonic == -1)
                mnemonic = tmp;
            mnemonics[linkIndex] = mnemonic;
        } else {
            mnemonics[linkIndex] = -1;
        }
        if (offsets.length != linkIndex) {
            Point[] newOffsets = new Point[linkIndex];
            System.arraycopy(offsets, 0, newOffsets, 0, linkIndex);
            offsets = newOffsets;
            String[] newIDs = new String[linkIndex];
            System.arraycopy(ids, 0, newIDs, 0, linkIndex);
            ids = newIDs;
            int[] newMnemonics = new int[linkIndex + 1];
            System.arraycopy(mnemonics, 0, newMnemonics, 0, linkIndex + 1);
            mnemonics = newMnemonics;
        }
        return result.toString();
    }

    int parseMnemonics(char[] buffer, int start, int end, StringBuilder result) {
        int mnemonic = -1, index = start;
        while (index < end) {
            if (buffer[index] == '&') {
                if (index + 1 < end && buffer[index + 1] == '&') {
                    result.append(buffer[index]);
                    index++;
                } else {
                    mnemonic = result.length();
                }
            } else {
                result.append(buffer[index]);
            }
            index++;
        }
        return mnemonic;
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
    void releaseWidget() {
        super.releaseWidget();
        offsets = null;
        ids = null;
        mnemonics = null;
        text = null;
        linkForeground = null;
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
    public void sendFocusEvent(int type) {
        super.sendFocusEvent(type);
    }

    @Override
    boolean sendKeyEvent(int type, Event event) {
        boolean result = super.sendKeyEvent(type, event);
        if (!result)
            return result;
        if (focusIndex == -1)
            return result;
        if (type != SWT.KeyDown)
            return result;
        int keyCode = event.keyCode;
        switch(keyCode) {
            case SWT.CR:
            case SWT.KEYPAD_CR:
            case 32:
                /* Space */
                Event event1 = new Event();
                event1.text = ids[focusIndex];
                sendEvent(SWT.Selection, event1);
                break;
            case SWT.TAB:
                int modifierFlags = event.stateMask;
                boolean next = (modifierFlags & SWT.SHIFT) == 0;
                if (next) {
                    if (focusIndex < offsets.length - 1) {
                        focusIndex++;
                        redraw();
                        return false;
                    }
                } else {
                    if (focusIndex > 0) {
                        focusIndex--;
                        redraw();
                        return false;
                    }
                }
                break;
        }
        return result;
    }

    @Override
    void setForeground(double[] color) {
        if (!getEnabled())
            return;
    }

    void setLinkColor(boolean enabled) {
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
        if (color != null) {
            if (color.isDisposed())
                error(SWT.ERROR_INVALID_ARGUMENT);
        }
        double[] linkForeground = color != null ? color.handle : null;
        //if (equals(linkForeground, this.linkForeground))    return;
        ;
        this.linkForeground = linkForeground;
        if (getEnabled()) {
            setLinkColor(true);
        }
        this._linkForeground = newValue;
    }

    @Override
    void setOrientation() {
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
        focusIndex = offsets.length > 0 ? 0 : -1;
        for (int i = 0; i < offsets.length; i++) {
        }
    }

    @Override
    void setZOrder() {
        super.setZOrder();
    }

    @Override
    int traversalCode(int key, Object theEvent) {
        if (offsets.length == 0)
            return 0;
        int bits = super.traversalCode(key, theEvent);
        if (key == 48 && /* Tab */
        theEvent != null) {
        }
        return bits;
    }

    @Override
    public void updateCursorRects(boolean enabled) {
        super.updateCursorRects(enabled);
    }

    void updateThemeColors() {
        /*
	 * On macOS 10.14 and 10.15, when application sets Dark appearance, NSTextView
	 * does not change the text color. In case of the link, this means that text
	 * outside <a></a> will be black-on-dark. Fix this by setting the text color
	 * explicitly. It seems that this is no longer needed on macOS 11.0. Note that
	 * there is 'setUsesAdaptiveColorMappingForDarkAppearance:' which causes
	 * NSTextView to adapt its colors, but it will also remap any colors used in
	 * .setBackground(), which makes it difficult to use. I wasn't able to find an
	 * event that colors changed, 'drawRect' seems to be the best option.
	 */
        // Avoid infinite loop of redraws
        if (lastAppAppearance == ((SwtDisplay) display.getImpl()).appAppearance)
            return;
        lastAppAppearance = ((SwtDisplay) display.getImpl()).appAppearance;
        // Only default colors are affected
        if (foreground != null)
            return;
    }

    Color _linkForeground;

    public String _text() {
        return text;
    }

    public Point[] _offsets() {
        return offsets;
    }

    public String[] _ids() {
        return ids;
    }

    public int[] _mnemonics() {
        return mnemonics;
    }

    public double[] _linkForeground() {
        return linkForeground;
    }

    public int _focusIndex() {
        return focusIndex;
    }

    public boolean _ignoreNextMouseUp() {
        return ignoreNextMouseUp;
    }

    public APPEARANCE _lastAppAppearance() {
        return lastAppAppearance;
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
