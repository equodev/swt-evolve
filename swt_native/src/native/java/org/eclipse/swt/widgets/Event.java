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
 * *****************************************************************************
 */
package org.eclipse.swt.widgets;

import org.eclipse.swt.graphics.*;

/**
 * Instances of this class provide a description of a particular
 * event which occurred within SWT. The SWT <em>untyped listener</em>
 * API uses these instances for all event dispatching.
 * <p>
 * Note: For a given event, only the fields which are appropriate
 * will be filled in. The contents of the fields which are not used
 * by the event are unspecified.
 * </p>
 *
 * @see Listener
 * @see org.eclipse.swt.events.TypedEvent
 * @see <a href="https://eclipse.dev/eclipse/swt/examples.html">SWT Example: ControlExample, Listeners</a>
 * @see <a href="https://eclipse.dev/eclipse/swt/">Sample code and further information</a>
 */
public class Event implements dev.equo.swt.WireObject {

    /**
     * the display where the event occurred
     *
     * @since 2.0
     */
    public Display display;

    /**
     * the widget that issued the event
     */
    public Widget widget;

    /**
     * the type of event, as defined by the event type constants
     * in class <code>SWT</code>
     *
     * @see org.eclipse.swt.SWT
     */
    public int type;

    /**
     * the event specific detail field, as defined by the detail constants
     * in class <code>SWT</code>
     *
     * @see org.eclipse.swt.SWT
     */
    public int detail;

    /**
     * the item that the event occurred in (can be null)
     */
    public Widget item;

    /**
     * the index of the item where the event occurred
     *
     * @since 3.2
     */
    public int index;

    /**
     * the graphics context to use when painting
     * that is configured to use the colors, font and
     * damaged region of the control.  It is valid
     * only during the paint and must not be disposed
     */
    public GC gc;

    /**
     * depending on the event type, the x offset of the bounding
     * rectangle of the region that requires painting or the
     * widget-relative, x coordinate of the pointer at the
     * time the mouse button was pressed or released
     */
    public int x;

    /**
     * depending on the event type, the y offset of the bounding
     * rectangle of the  region that requires painting or the
     * widget-relative, y coordinate of the pointer at the
     * time the mouse button was pressed or released
     */
    public int y;

    /**
     * the width of the bounding rectangle of the
     * region that requires painting
     */
    public int width;

    /**
     * the height of the bounding rectangle of the
     * region that requires painting
     */
    public int height;

    /**
     * depending on the event type, the number of following
     * paint events that are pending which may always be zero
     * on some platforms, or the number of lines or pages to
     * scroll using the mouse wheel, or the number of times the
     * mouse has been clicked
     */
    public int count;

    /**
     * the time that the event occurred.
     *
     * NOTE: This field is an unsigned integer and should
     * be AND'ed with 0xFFFFFFFFL so that it can be treated
     * as a signed long.
     */
    public int time;

    /**
     * the button that was pressed or released; 1 for the
     * first button, 2 for the second button, and 3 for the
     * third button, etc.
     */
    public int button;

    /**
     * depending on the event, the character represented by the key
     * that was typed.  This is the final character that results
     * after all modifiers have been applied.  For example, when the
     * user types Ctrl+A, the character value is 0x01 (ASCII SOH).
     * It is important that applications do not attempt to modify the
     * character value based on a stateMask (such as SWT.CTRL) or the
     * resulting character will not be correct.
     */
    public char character;

    /**
     * character that is good for keyboard shortcut comparison. Unlike
     * {@link #character}, this tries to ignore modifier keys and deal
     * with non-latin keyboard layouts.
     * Examples:<br>
     * <table>
     *  <tr><th>Layout</th><th>US key label</th><th>character</th><th>keyCode</th></tr>
     *  <tr><td>Windows English US    </td><td>C      </td><td>'c' </td> <td>'c'</td></tr>
     *  <tr><td>Windows English US    </td><td>Shift+C</td><td>'C' </td> <td>'c'</td></tr>
     *  <tr><td>Windows English US    </td><td>Ctrl+C </td><td>0x03</td><td>'c'</td></tr>
     *  <tr><td>Windows English Dvorak</td><td>I      </td><td>'c' </td><td>'c'</td></tr>
     *  <tr><td>Windows Bulgarian     </td><td>Ъ      </td><td>'ъ' </td><td>'c'</td></tr>
     *  <tr><td>Windows French        </td><td>2      </td><td>'é' </td><td>'2'</td></tr>
     *  <tr><td>Windows French        </td><td>Shift+2</td><td>'2' </td><td>'2'</td></tr>
     * </table><br>
     *
     * How it is done differs per platform, and on many platforms,
     * SWT resorts to various magic. To understand the problem,
     * consider the following questions about the well known
     * Ctrl+C shortcut:
     * <ul>
     *  <li>Which key invokes Ctrl+C in English? Well, that's easy, key
     *   C does it.</li>
     *  <li>Dvorak is basically an English layout, but with keys
     *   shuffled around. Where English has C, Dvorak has J. Where
     *   English has I, Dvorak has C. Which key invokes Ctrl+C in
     *   English-Dvorak? Well, both answers are kind of correct.
     *   There's a heated debate on the internet which answer is better:
     *   some argue that they like English shortcuts they learned
     *   before they decided to try Dvorak. Others argue that key
     *   labels shall match invoked shortcuts. The usually preferred
     *   answer is that if a key types C in Dvorak, then it shall
     *   invoke Ctrl+C. That is, the key that is labeled I in English.</li>
     *  <li>Which key invokes Ctrl+C in non-latin keyboard layouts?
     *   That's where it gets hard. Consider Hebrew, Cyrillic,
     *   Japanese. These layouts simply don't have latin C anywhere!</li>
     * </ul>
     * Approaching all 3 questions at once is non-trivial, and each OS
     * does it in a different way. In very simple words:
     * <ul>
     *  <li>Windows keyboard layouts have an additional invisible
     *   mapping of keys to keyboard shortcuts, completely independent
     *   from characters typed by these keys. Layouts also have the
     *   regular mapping of keys to produced characters. Both are
     *   edited in keyboard layout editors.</li>
     *  <li>macOS keyboard layouts have hidden Latin sub-layouts and
     *   switch to them when a modifier such as Cmd is pressed. This
     *   is easy to see in builtin on-screen keyboard. It's a lot more
     *   complicated with modifiers other than Cmd and Ctrl.</li>
     *  <li>Linux is worst. It lacks the information to map shortcuts
     *   on non-latin keyboards and resorts to inspecting other
     *   installed layouts to find something latin.</li>
     * </ul>
     *
     * For detailed information, see {@link Widget#setKeyState} per
     * platform.
     *
     * @see org.eclipse.swt.SWT
     */
    /*
	 * For debugging, see
	 * <ul>
	 *  <li>Unit tests Test_org_eclipse_swt_events_KeyEvent</li>
	 *  <li>Files with names containing 'Issue0351_EventKeyCode'</li>
	 * </ul>
	 */
    public int keyCode;

    /**
     * depending on the event, the location of key specified by the
     * keyCode or character. The possible values for this field are
     * <code>SWT.LEFT</code>, <code>SWT.RIGHT</code>, <code>SWT.KEYPAD</code>,
     * or <code>SWT.NONE</code> representing the main keyboard area.
     * <p>
     * The location field can be used to differentiate key events that have
     * the same key code and character but are generated by different keys
     * in the keyboard. For example, a key down event with the key code equals
     * to SWT.SHIFT can be generated by the left and the right shift keys in the
     * keyboard. The location field can only be used to determine the location
     * of the key code or character in the current event. It does not
     * include information about the location of modifiers in state
     * mask.
     * </p>
     *
     * @see org.eclipse.swt.SWT#LEFT
     * @see org.eclipse.swt.SWT#RIGHT
     * @see org.eclipse.swt.SWT#KEYPAD
     *
     * @since 3.6
     */
    public int keyLocation;

    /**
     * depending on the event, the state of the keyboard modifier
     * keys and mouse masks at the time the event was generated.
     *
     * @see org.eclipse.swt.SWT#MODIFIER_MASK
     * @see org.eclipse.swt.SWT#BUTTON_MASK
     */
    public int stateMask;

    /**
     * depending on the event, the range of text being modified.
     * Setting these fields only has effect during ImeComposition
     * events.
     */
    public int start, end;

    /**
     * depending on the event, the new text that will be inserted.
     * Setting this field will change the text that is about to
     * be inserted or deleted.
     */
    public String text;

    /**
     * Bidi segment offsets
     * @since 3.8
     */
    public int[] segments;

    /**
     * Characters to be applied on the segment boundaries
     * @since 3.8
     */
    public char[] segmentsChars;

    /**
     * depending on the event, a flag indicating whether the operation
     * should be allowed.  Setting this field to false will cancel the
     * operation.
     */
    public boolean doit = true;

    /**
     * a field for application use
     */
    public Object data;

    /**
     * An array of the touch states for the current touch event.
     *
     * @since 3.7
     */
    public Touch[] touches;

    /**
     * If nonzero, a positive value indicates a swipe to the right,
     * and a negative value indicates a swipe to the left.
     *
     * @since 3.7
     */
    public int xDirection;

    /**
     * If nonzero, a positive value indicates a swipe in the up direction,
     * and a negative value indicates a swipe in the down direction.
     *
     * @since 3.7
     */
    public int yDirection;

    /**
     * The change in magnification. This value should be added to the current
     * scaling of an item to get the new scale factor.
     *
     * @since 3.7
     */
    public double magnification;

    /**
     * The number of degrees rotated on the track pad.
     *
     * @since 3.7
     */
    public double rotation;

    /**
     * Gets the bounds.
     *
     * @return a rectangle that is the bounds.
     */
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    Point getLocation() {
        return new Point(x, y);
    }

    /**
     * Sets the bounds.
     *
     * @param rect the new rectangle
     */
    public void setBounds(Rectangle rect) {
        this.x = rect.x;
        this.y = rect.y;
        this.width = rect.width;
        this.height = rect.height;
    }

    void setLocation(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Returns a string containing a concise, human-readable
     * description of the receiver.
     *
     * @return a string representation of the event
     */
    @Override
    public String toString() {
        //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
        return "Event {type=" + type + " " + widget + " time=" + time + " data=" + data + " x=" + x + " y=" + y + " width=" + width + " height=" + height + " detail=" + detail + "}";
    }

    public int feedback;

    public int operations;

    public int currentDataTypeId;

    public Image dragImage;

    public long itemId;

    @Override
    public int wireTypeId() {
        return 34;
    }

    @Override
    public void writeWireFields(dev.equo.swt.WidgetWire w) {
        w.key(213);
        w.ref(widget);
        w.key(212);
        w.write((int) type);
        w.key(190);
        w.write((int) detail);
        w.key(198);
        w.ref(item);
        w.key(197);
        w.write((int) index);
        w.key(195);
        w.ref(gc);
        w.key(215);
        w.write((int) x);
        w.key(217);
        w.write((int) y);
        w.key(214);
        w.write((int) width);
        w.key(196);
        w.write((int) height);
        w.key(188);
        w.write((int) count);
        w.key(210);
        w.write((int) time);
        w.key(186);
        w.write((int) button);
        w.key(187);
        w.write(character);
        w.key(200);
        w.write((int) keyCode);
        w.key(201);
        w.write((int) keyLocation);
        w.key(208);
        w.write((int) stateMask);
        w.key(207);
        w.write((int) start);
        w.key(193);
        w.write((int) end);
        w.key(209);
        w.write(text);
        w.key(205);
        w.write(segments);
        w.key(206);
        w.write(segmentsChars);
        w.key(191);
        w.write(doit);
        w.key(211);
        {
            Touch[] a_ = touches;
            if (a_ == null)
                w.listSize(-1);
            else {
                w.listSize(a_.length);
                for (Touch e_ : a_) w.object(e_);
            }
        }
        w.key(216);
        w.write((int) xDirection);
        w.key(218);
        w.write((int) yDirection);
        w.key(202);
        w.write(magnification);
        w.key(204);
        w.write(rotation);
        w.key(194);
        w.write((int) feedback);
        w.key(203);
        w.write((int) operations);
        w.key(189);
        w.write((int) currentDataTypeId);
        w.key(192);
        w.image(dragImage);
        w.key(199);
        w.write(itemId);
    }

    @Override
    public boolean readWire(int key, dev.equo.swt.WireReader r) {
        switch(key) {
            case 212:
                type = r.int32();
                return true;
            case 190:
                detail = r.int32();
                return true;
            case 197:
                index = r.int32();
                return true;
            case 215:
                x = r.int32();
                return true;
            case 217:
                y = r.int32();
                return true;
            case 214:
                width = r.int32();
                return true;
            case 196:
                height = r.int32();
                return true;
            case 188:
                count = r.int32();
                return true;
            case 210:
                time = r.int32();
                return true;
            case 186:
                button = r.int32();
                return true;
            case 187:
                character = r.char16();
                return true;
            case 200:
                keyCode = r.int32();
                return true;
            case 201:
                keyLocation = r.int32();
                return true;
            case 208:
                stateMask = r.int32();
                return true;
            case 207:
                start = r.int32();
                return true;
            case 193:
                end = r.int32();
                return true;
            case 209:
                text = r.string();
                return true;
            case 205:
                segments = r.ints();
                return true;
            case 206:
                segmentsChars = r.chars();
                return true;
            case 191:
                doit = r.bool();
                return true;
            case 216:
                xDirection = r.int32();
                return true;
            case 218:
                yDirection = r.int32();
                return true;
            case 202:
                magnification = r.float64();
                return true;
            case 204:
                rotation = r.float64();
                return true;
            case 194:
                feedback = r.int32();
                return true;
            case 203:
                operations = r.int32();
                return true;
            case 189:
                currentDataTypeId = r.int32();
                return true;
            case 199:
                itemId = r.int64();
                return true;
        }
        return false;
    }
}
