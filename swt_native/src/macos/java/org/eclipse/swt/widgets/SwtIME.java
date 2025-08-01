/**
 * ****************************************************************************
 *  Copyright (c) 2007, 2017 IBM Corporation and others.
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
import org.eclipse.swt.internal.cocoa.*;

/**
 * Instances of this class represent input method editors.
 * These are typically in-line pre-edit text areas that allow
 * the user to compose characters from Far Eastern languages
 * such as Japanese, Chinese or Korean.
 *
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>(none)</dd>
 * <dt><b>Events:</b></dt>
 * <dd>ImeComposition</dd>
 * </dl>
 * <p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 *
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 *
 * @since 3.4
 * @noextend This class is not intended to be subclassed by clients.
 */
public class SwtIME extends SwtWidget implements IIME {

    Canvas parent;

    int caretOffset;

    int startOffset;

    int commitCount;

    String text;

    int[] ranges;

    TextStyle[] styles;

    static final int UNDERLINE_THICK = 1 << 16;

    /**
     * Prevents uninitialized instances from being created outside the package.
     */
    SwtIME(IME api) {
        super(api);
    }

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
     * @param parent a canvas control which will be the parent of the new instance (cannot be null)
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
    public SwtIME(Canvas parent, int style, IME api) {
        super(parent, style, api);
        this.parent = parent;
        createWidget();
    }

    @Override
    long attributedSubstringFromRange(long id, long sel, long rangePtr) {
        Event event = new Event();
        event.detail = SWT.COMPOSITION_SELECTION;
        sendEvent(SWT.ImeComposition, event);
        NSRange range = new NSRange();
        OS.memmove(range, rangePtr, NSRange.sizeof);
        int start = (int) range.location;
        int end = (int) (range.location + range.length);
        if (event.start <= start && start <= event.end && event.start <= end && end <= event.end) {
            NSString str = (NSString) new NSString().alloc();
            str = str.initWithString(event.text.substring(start - event.start, end - event.start));
            NSAttributedString attriStr = ((NSAttributedString) new NSAttributedString().alloc()).initWithString(str, null);
            str.release();
            attriStr.autorelease();
            return attriStr.id;
        }
        return 0;
    }

    @Override
    long characterIndexForPoint(long id, long sel, long point) {
        if (!isInlineEnabled())
            return OS.NSNotFound();
        NSPoint pt = new NSPoint();
        OS.memmove(pt, point, NSPoint.sizeof);
        if (parent == null || parent.getImpl() instanceof SwtCanvas) {
            NSView view = parent.view;
            pt = view.window().convertScreenToBase(pt);
            pt = view.convertPoint_fromView_(pt, null);
        }
        Event event = new Event();
        event.detail = SWT.COMPOSITION_OFFSET;
        event.x = (int) pt.x;
        event.y = (int) pt.y;
        sendEvent(SWT.ImeComposition, event);
        int offset = event.index + event.count;
        return offset != -1 ? offset : OS.NSNotFound();
    }

    @Override
    void createWidget() {
        text = "";
        startOffset = -1;
        if (parent.getIME() == null) {
            parent.setIME(this.getApi());
        }
    }

    @Override
    NSRect firstRectForCharacterRange(long id, long sel, long range) {
        NSRect rect = new NSRect();
        Caret caret = parent.getImpl()._caret();
        if (caret != null) {
            if (parent == null || parent.getImpl() instanceof SwtCanvas) {
                NSView view = parent.view;
                NSPoint pt = new NSPoint();
                pt.x = caret.getImpl()._x();
                pt.y = caret.getImpl()._y() + caret.getImpl()._height();
                pt = view.convertPoint_toView_(pt, null);
                pt = view.window().convertBaseToScreen(pt);
                rect.x = pt.x;
                rect.y = pt.y;
            }
            rect.width = caret.getImpl()._width();
            rect.height = caret.getImpl()._height();
        }
        return rect;
    }

    /**
     * Returns the offset of the caret from the start of the document.
     * -1 means that there is currently no active composition.
     * The caret is within the current composition.
     *
     * @return the caret offset
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public int getCaretOffset() {
        checkWidget();
        return startOffset + caretOffset;
    }

    /**
     * Returns the commit count of the composition.  This is the
     * number of characters that have been composed.  When the
     * commit count is equal to the length of the composition
     * text, then the in-line edit operation is complete.
     *
     * @return the commit count
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see IME#getText
     */
    public int getCommitCount() {
        checkWidget();
        return commitCount;
    }

    /**
     * Returns the offset of the composition from the start of the document.
     * This is the start offset of the composition within the document and
     * in not changed by the input method editor itself during the in-line edit
     * session.
     *
     * @return the offset of the composition
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public int getCompositionOffset() {
        checkWidget();
        return startOffset;
    }

    /**
     * Returns the ranges for the style that should be applied during the
     * in-line edit session.
     * <p>
     * The ranges array contains start and end pairs.  Each pair refers to
     * the corresponding style in the styles array.  For example, the pair
     * that starts at ranges[n] and ends at ranges[n+1] uses the style
     * at styles[n/2] returned by <code>getStyles()</code>.
     * </p>
     * @return the ranges for the styles
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see IME#getStyles
     */
    public int[] getRanges() {
        checkWidget();
        if (ranges == null)
            return new int[0];
        int[] result = new int[ranges.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = ranges[i] + startOffset;
        }
        return result;
    }

    /**
     * Returns the styles for the ranges.
     * <p>
     * The ranges array contains start and end pairs.  Each pair refers to
     * the corresponding style in the styles array.  For example, the pair
     * that starts at ranges[n] and ends at ranges[n+1] uses the style
     * at styles[n/2].
     * </p>
     *
     * @return the ranges for the styles
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see IME#getRanges
     */
    public TextStyle[] getStyles() {
        checkWidget();
        if (styles == null)
            return new TextStyle[0];
        TextStyle[] result = new TextStyle[styles.length];
        System.arraycopy(styles, 0, result, 0, styles.length);
        return result;
    }

    TextStyle getStyle(NSDictionary attribs) {
        NSArray keys = attribs.allKeys();
        long count = keys.count();
        TextStyle style = new TextStyle();
        for (int j = 0; j < count; j++) {
            NSString key = new NSString(keys.objectAtIndex(j));
            if (key.isEqual(OS.NSBackgroundColorAttributeName)) {
                NSColor color = new NSColor(attribs.objectForKey(key));
                style.background = SwtColor.cocoa_new(display, ((SwtDisplay) display.getImpl()).getNSColorRGB(color));
            } else if (key.isEqual(OS.NSForegroundColorAttributeName)) {
                NSColor color = new NSColor(attribs.objectForKey(key));
                style.foreground = SwtColor.cocoa_new(display, ((SwtDisplay) display.getImpl()).getNSColorRGB(color));
            } else if (key.isEqual(OS.NSUnderlineColorAttributeName)) {
                NSColor color = new NSColor(attribs.objectForKey(key));
                style.underlineColor = SwtColor.cocoa_new(display, ((SwtDisplay) display.getImpl()).getNSColorRGB(color));
            } else if (key.isEqual(OS.NSUnderlineStyleAttributeName)) {
                NSNumber value = new NSNumber(attribs.objectForKey(key));
                switch(value.intValue()) {
                    case OS.NSUnderlineStyleSingle:
                        style.underlineStyle = SWT.UNDERLINE_SINGLE;
                        break;
                    case OS.NSUnderlineStyleDouble:
                        style.underlineStyle = SWT.UNDERLINE_DOUBLE;
                        break;
                    case OS.NSUnderlineStyleThick:
                        style.underlineStyle = UNDERLINE_THICK;
                        break;
                }
                style.underline = value.intValue() != OS.NSUnderlineStyleNone;
            } else if (key.isEqual(OS.NSStrikethroughColorAttributeName)) {
                NSColor color = new NSColor(attribs.objectForKey(key));
                style.strikeoutColor = SwtColor.cocoa_new(display, ((SwtDisplay) display.getImpl()).getNSColorRGB(color));
            } else if (key.isEqual(OS.NSStrikethroughStyleAttributeName)) {
                NSNumber value = new NSNumber(attribs.objectForKey(key));
                style.strikeout = value.intValue() != OS.NSUnderlineStyleNone;
            } else if (key.isEqual(OS.NSFontAttributeName)) {
                NSFont font = new NSFont(attribs.objectForKey(key));
                font.retain();
                style.font = SwtFont.cocoa_new(display, font);
            }
        }
        return style;
    }

    /**
     * Returns the composition text.
     * <p>
     * The text for an IME is the characters in the widget that
     * are in the current composition. When the commit count is
     * equal to the length of the composition text, then the
     * in-line edit operation is complete.
     * </p>
     *
     * @return the widget text
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

    /**
     * Returns <code>true</code> if the caret should be wide, and
     * <code>false</code> otherwise.  In some languages, for example
     * Korean, the caret is typically widened to the width of the
     * current character in the in-line edit session.
     *
     * @return the wide caret state
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public boolean getWideCaret() {
        return false;
    }

    @Override
    boolean hasMarkedText(long id, long sel) {
        return text.length() != 0;
    }

    @Override
    boolean insertText(long id, long sel, long string) {
        if (startOffset == -1)
            return true;
        NSString str = new NSString(string);
        if (str.isKindOfClass(OS.class_NSAttributedString)) {
            str = new NSAttributedString(string).string();
        }
        int length = (int) str.length();
        int end = startOffset + text.length();
        resetStyles();
        caretOffset = commitCount = length;
        Event event = new Event();
        event.detail = SWT.COMPOSITION_CHANGED;
        event.start = startOffset;
        event.end = end;
        event.text = text = str.getString();
        sendEvent(SWT.ImeComposition, event);
        text = "";
        caretOffset = commitCount = 0;
        startOffset = -1;
        return event.doit;
    }

    boolean isInlineEnabled() {
        return hooks(SWT.ImeComposition);
    }

    @Override
    NSRange markedRange(long id, long sel) {
        NSRange range = new NSRange();
        if (startOffset != -1) {
            range.location = startOffset;
            range.length = text.length();
        } else {
            range.location = OS.NSNotFound();
        }
        return range;
    }

    void resetStyles() {
        if (styles != null) {
            for (int i = 0; i < styles.length; i++) {
                TextStyle style = styles[i];
                Font font = style.font;
                if (font != null)
                    font.handle.release();
            }
        }
        styles = null;
        ranges = null;
    }

    @Override
    void releaseParent() {
        super.releaseParent();
        if (this.getApi() == parent.getIME())
            parent.setIME(null);
    }

    @Override
    void releaseWidget() {
        super.releaseWidget();
        parent = null;
        text = null;
        resetStyles();
    }

    @Override
    NSRange selectedRange(long id, long sel) {
        Event event = new Event();
        event.detail = SWT.COMPOSITION_SELECTION;
        sendEvent(SWT.ImeComposition, event);
        NSRange range = new NSRange();
        range.location = event.start;
        range.length = event.text.length();
        return range;
    }

    /**
     * Sets the offset of the composition from the start of the document.
     * This is the start offset of the composition within the document and
     * in not changed by the input method editor itself during the in-line edit
     * session but may need to be changed by clients of the IME.  For example,
     * if during an in-line edit operation, a text editor inserts characters
     * above the IME, then the IME must be informed that the composition
     * offset has changed.
     *
     * @param offset the offset of the composition
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setCompositionOffset(int offset) {
        checkWidget();
        if (offset < 0)
            return;
        if (startOffset != -1) {
            startOffset = offset;
        }
    }

    @Override
    boolean setMarkedText_selectedRange(long id, long sel, long string, long selRange) {
        if (!isInlineEnabled())
            return true;
        resetStyles();
        caretOffset = commitCount = 0;
        int end = startOffset + text.length();
        if (startOffset == -1) {
            Event event = new Event();
            event.detail = SWT.COMPOSITION_SELECTION;
            sendEvent(SWT.ImeComposition, event);
            startOffset = event.start;
            end = event.end;
        }
        NSString str = new NSString(string);
        if (str.isKindOfClass(OS.class_NSAttributedString)) {
            NSAttributedString attribStr = new NSAttributedString(string);
            str = attribStr.string();
            int length = (int) str.length();
            styles = new TextStyle[length];
            ranges = new int[length * 2];
            NSRange rangeLimit = new NSRange(), effectiveRange = new NSRange();
            rangeLimit.length = length;
            int rangeCount = 0;
            long ptr = C.malloc(NSRange.sizeof);
            for (int i = 0; i < length; ) {
                NSDictionary attribs = attribStr.attributesAtIndex(i, ptr, rangeLimit);
                OS.memmove(effectiveRange, ptr, NSRange.sizeof);
                i = (int) (effectiveRange.location + effectiveRange.length);
                ranges[rangeCount * 2] = (int) effectiveRange.location;
                ranges[rangeCount * 2 + 1] = (int) (effectiveRange.location + effectiveRange.length - 1);
                styles[rangeCount++] = getStyle(attribs);
            }
            C.free(ptr);
            if (rangeCount != styles.length) {
                TextStyle[] newStyles = new TextStyle[rangeCount];
                System.arraycopy(styles, 0, newStyles, 0, newStyles.length);
                styles = newStyles;
                int[] newRanges = new int[rangeCount * 2];
                System.arraycopy(ranges, 0, newRanges, 0, newRanges.length);
                ranges = newRanges;
            }
        }
        int length = (int) str.length();
        if (ranges == null && length > 0) {
            styles = new TextStyle[] { getStyle(((SwtDisplay) display.getImpl()).markedAttributes) };
            ranges = new int[] { 0, length - 1 };
        }
        NSRange range = new NSRange();
        OS.memmove(range, selRange, NSRange.sizeof);
        /*
	 * Bug 427882: There is a macOS bug where it sends
	 * 'setMarkedText:selectedRange:' with 'markedText' that is incorrectly
	 * too short, which results in 'selectedRange.location' being outside it.
	 * If caret's position is already at the end, this will result in trying
	 * to set caret outside the text. The workaround is to correct 'location'
	 * so that it's always within 'markedText'.
	 */
        range.location = Math.min(range.location, length);
        caretOffset = (int) range.location;
        Event event = new Event();
        event.detail = SWT.COMPOSITION_CHANGED;
        event.start = startOffset;
        event.end = end;
        event.text = text = str.getString();
        sendEvent(SWT.ImeComposition, event);
        if (isDisposed())
            return false;
        if (text.length() == 0) {
            Shell s = parent.getShell();
            ((SwtShell) s.getImpl()).keyInputHappened = true;
            startOffset = -1;
            resetStyles();
        }
        return true;
    }

    @Override
    long validAttributesForMarkedText(long id, long sel) {
        NSMutableArray attribs = NSMutableArray.arrayWithCapacity(6);
        attribs.addObject(OS.NSForegroundColorAttributeName);
        attribs.addObject(OS.NSBackgroundColorAttributeName);
        attribs.addObject(OS.NSUnderlineStyleAttributeName);
        attribs.addObject(OS.NSUnderlineColorAttributeName);
        attribs.addObject(OS.NSStrikethroughStyleAttributeName);
        attribs.addObject(OS.NSStrikethroughColorAttributeName);
        return attribs.id;
    }

    public IME getApi() {
        if (api == null)
            api = IME.createApi(this);
        return (IME) api;
    }
}
