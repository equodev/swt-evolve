/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2021 IBM Corporation and others.
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
 *      Lars Vogel <Lars.Vogel@vogella.com> - Bug 455263
 * *****************************************************************************
 */
package org.eclipse.swt;

import org.eclipse.swt.internal.*;
import org.eclipse.swt.widgets.*;

/**
 * This class provides access to a small number of SWT system-wide
 * methods, and in addition defines the public constants provided
 * by SWT.
 * <p>
 * By defining constants like UP and DOWN in a single class, SWT
 * can share common names and concepts at the same time minimizing
 * the number of classes, names and constants for the application
 * programmer.
 * </p><p>
 * Note that some of the constants provided by this class represent
 * optional, appearance related aspects of widgets which are available
 * either only on some window systems, or for a differing set of
 * widgets on each window system. These constants are marked
 * as <em>HINT</em>s. The set of widgets which support a particular
 * <em>HINT</em> may change from release to release, although we typically
 * will not withdraw support for a <em>HINT</em> once it is made available.
 * </p>
 *
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 */
/* NOTE:
 *   Good javadoc coding style is to put the values of static final
 *   constants in the comments. This reinforces the fact that
 *   consumers are allowed to rely on the value (and they must
 *   since the values are compiled inline in their code). We
 *   can <em>not</em> change the values of these constants between
 *   releases.
 */
public class SWT {

    /* Widget Event Constants */
    /**
     * The null event type (value is 0).
     *
     * @since 3.0
     */
    public static final int None = 0;

    /**
     * The key down event type (value is 1).
     *
     * <p>
     * Note:
     * </p>
     * <ul>
     * <li>Event.keyCode: the constant of the pressed key, even if it is just a
     * modifier (Shift, Ctrl, ...)</li>
     * <li>Event.character: the character that the key press created (or 0 if
     * none)</li>
     * <li>Event.keyLocation: either SWT.NONE, SWT.LEFT/.RIGHT (e.g. for Shift key)
     * or SWT.KEYPAD</li>
     * <li>Event.stateMask: an or-combined bit mask of the modifiers
     * (SWT.BUTTON_MASK, SWT.SHIFT, SWT.CTRL, SWT.CMD, SWT.ALT, SWT.MOD1, SWT.MOD2,
     * SWT.MOD3)</li>
     * </ul>
     *
     * @see org.eclipse.swt.widgets.Widget#addListener
     * @see org.eclipse.swt.widgets.Display#addFilter
     * @see org.eclipse.swt.widgets.Event
     *
     * @see org.eclipse.swt.widgets.Control#addKeyListener
     * @see org.eclipse.swt.widgets.Tracker#addKeyListener
     * @see org.eclipse.swt.events.KeyListener#keyPressed
     * @see org.eclipse.swt.events.KeyEvent
     */
    public static final int KeyDown = 1;

    /**
     * The key up event type (value is 2).
     *
     * @see org.eclipse.swt.widgets.Widget#addListener
     * @see org.eclipse.swt.widgets.Display#addFilter
     * @see org.eclipse.swt.widgets.Event
     *
     * @see org.eclipse.swt.widgets.Control#addKeyListener
     * @see org.eclipse.swt.widgets.Tracker#addKeyListener
     * @see org.eclipse.swt.events.KeyListener#keyReleased
     * @see org.eclipse.swt.events.KeyEvent
     */
    public static final int KeyUp = 2;

    /**
     * The mouse down event type (value is 3).
     *
     * <p>
     * Note:
     * </p>
     * <ul>
     * <li>Event.button: either 1, 2 or 3 for the mouse button that was pressed</li>
     * <li>Event.x, Event.y: the cursor position relative to the control
     * (event.widget)</li>
     * <li>Event.stateMask: an or-combined bit mask of the modifiers
     * (SWT.BUTTON_MASK)</li>
     * </ul>
     *
     * @see org.eclipse.swt.widgets.Widget#addListener
     * @see org.eclipse.swt.widgets.Display#addFilter
     * @see org.eclipse.swt.widgets.Event
     *
     * @see org.eclipse.swt.widgets.Control#addMouseListener
     * @see org.eclipse.swt.events.MouseListener#mouseDown
     * @see org.eclipse.swt.events.MouseEvent
     */
    public static final int MouseDown = 3;

    /**
     * The mouse up event type (value is 4).
     *
     * @see org.eclipse.swt.widgets.Widget#addListener
     * @see org.eclipse.swt.widgets.Display#addFilter
     * @see org.eclipse.swt.widgets.Event
     *
     * @see org.eclipse.swt.widgets.Control#addMouseListener
     * @see org.eclipse.swt.events.MouseListener#mouseUp
     * @see org.eclipse.swt.events.MouseEvent
     */
    public static final int MouseUp = 4;

    /**
     * The mouse move event type (value is 5).
     *
     * @see org.eclipse.swt.widgets.Widget#addListener
     * @see org.eclipse.swt.widgets.Display#addFilter
     * @see org.eclipse.swt.widgets.Event
     *
     * @see org.eclipse.swt.widgets.Control#addMouseMoveListener
     * @see org.eclipse.swt.events.MouseMoveListener#mouseMove
     * @see org.eclipse.swt.events.MouseEvent
     */
    public static final int MouseMove = 5;

    /**
     * The mouse enter event type (value is 6).
     *
     * <p>
     * Note: This event is received when the user enters a control's bounds with the
     * mouse cursor.
     * </p>
     *
     * @see org.eclipse.swt.widgets.Widget#addListener
     * @see org.eclipse.swt.widgets.Display#addFilter
     * @see org.eclipse.swt.widgets.Event
     *
     * @see org.eclipse.swt.widgets.Control#addMouseTrackListener
     * @see org.eclipse.swt.events.MouseTrackListener#mouseEnter
     * @see org.eclipse.swt.events.MouseEvent
     */
    public static final int MouseEnter = 6;

    /**
     * The mouse exit event type (value is 7).
     *
     * <p>
     * Note: This event is received when the user exits a control's bounds with the
     * mouse cursor. This is not 100% reliable and if you want to have it 100%
     * reliable, use control.setCapture(true).
     * </p>
     *
     * @see org.eclipse.swt.widgets.Control#setCapture
     * @see org.eclipse.swt.widgets.Widget#addListener
     * @see org.eclipse.swt.widgets.Display#addFilter
     * @see org.eclipse.swt.widgets.Event
     *
     * @see org.eclipse.swt.widgets.Control#addMouseTrackListener
     * @see org.eclipse.swt.events.MouseTrackListener#mouseExit
     * @see org.eclipse.swt.events.MouseEvent
     */
    public static final int MouseExit = 7;

    /**
     * The mouse double click event type (value is 8).
     *
     * @see org.eclipse.swt.widgets.Widget#addListener
     * @see org.eclipse.swt.widgets.Display#addFilter
     * @see org.eclipse.swt.widgets.Event
     *
     * @see org.eclipse.swt.widgets.Control#addMouseListener
     * @see org.eclipse.swt.events.MouseListener#mouseDoubleClick
     * @see org.eclipse.swt.events.MouseEvent
     */
    public static final int MouseDoubleClick = 8;

    /**
     * The paint event type (value is 9).
     *
     * <p>
     * Note:
     * </p>
     * <ul>
     * <li>Event.gc: the graphic context used for drawing.</li>
     * <li>Event.x, Event.y, Event.width, Event.height: the area that needs to be
     * painted</li>
     * </ul>
     *
     * @see org.eclipse.swt.widgets.Widget#addListener
     * @see org.eclipse.swt.widgets.Display#addFilter
     * @see org.eclipse.swt.widgets.Event
     *
     * @see org.eclipse.swt.widgets.Control#addPaintListener
     * @see org.eclipse.swt.events.PaintListener#paintControl
     * @see org.eclipse.swt.events.PaintEvent
     */
    public static final int Paint = 9;

    /**
     * The move event type (value is 10).
     *
     * @see org.eclipse.swt.widgets.Widget#addListener
     * @see org.eclipse.swt.widgets.Display#addFilter
     * @see org.eclipse.swt.widgets.Event
     *
     * @see org.eclipse.swt.widgets.Control#addControlListener
     * @see org.eclipse.swt.widgets.TableColumn#addControlListener
     * @see org.eclipse.swt.widgets.Tracker#addControlListener
     * @see org.eclipse.swt.widgets.TreeColumn#addControlListener
     * @see org.eclipse.swt.events.ControlListener#controlMoved
     * @see org.eclipse.swt.events.ControlEvent
     */
    public static final int Move = 10;

    /**
     * The resize event type (value is 11).
     *
     * @see org.eclipse.swt.widgets.Widget#addListener
     * @see org.eclipse.swt.widgets.Display#addFilter
     * @see org.eclipse.swt.widgets.Event
     *
     * @see org.eclipse.swt.widgets.Control#addControlListener
     * @see org.eclipse.swt.widgets.TableColumn#addControlListener
     * @see org.eclipse.swt.widgets.Tracker#addControlListener
     * @see org.eclipse.swt.widgets.TreeColumn#addControlListener
     * @see org.eclipse.swt.events.ControlListener#controlResized
     * @see org.eclipse.swt.events.ControlEvent
     */
    public static final int Resize = 11;

    /**
     * The dispose event type (value is 12).<br>
     * <br>
     * Note: This event is sent to indicate the beginning of the
     * disposing process. For a {@link Composite} this event is
     * sent before it is sent to its children, before any child
     * has been disposed.
     * {@link Display} still runs {@link Display#readAndDispatch}
     * after sending this event. If you want to dispose any resources,
     * this might cause problems. Use {@link Display#disposeExec}
     * instead.
     *
     * @see org.eclipse.swt.widgets.Widget#addListener
     * @see org.eclipse.swt.widgets.Display#addListener
     * @see org.eclipse.swt.widgets.Display#addFilter
     * @see org.eclipse.swt.widgets.Display#disposeExec
     * @see org.eclipse.swt.widgets.Event
     *
     * @see org.eclipse.swt.widgets.Widget#addDisposeListener
     * @see org.eclipse.swt.events.DisposeListener#widgetDisposed
     * @see org.eclipse.swt.events.DisposeEvent
     */
    public static final int Dispose = 12;

    /**
     * The selection event type (value is 13).
     *
     * @see org.eclipse.swt.widgets.Widget#addListener
     * @see org.eclipse.swt.widgets.Display#addFilter
     * @see org.eclipse.swt.widgets.Event
     *
     * @see org.eclipse.swt.widgets.Button#addSelectionListener
     * @see org.eclipse.swt.widgets.Combo#addSelectionListener
     * @see org.eclipse.swt.widgets.CoolItem#addSelectionListener
     * @see org.eclipse.swt.widgets.Link#addSelectionListener
     * @see org.eclipse.swt.widgets.List#addSelectionListener
     * @see org.eclipse.swt.widgets.MenuItem#addSelectionListener
     * @see org.eclipse.swt.widgets.Sash#addSelectionListener
     * @see org.eclipse.swt.widgets.Scale#addSelectionListener
     * @see org.eclipse.swt.widgets.ScrollBar#addSelectionListener
     * @see org.eclipse.swt.widgets.Slider#addSelectionListener
     * @see org.eclipse.swt.widgets.TabFolder#addSelectionListener
     * @see org.eclipse.swt.widgets.Table#addSelectionListener
     * @see org.eclipse.swt.widgets.TableColumn#addSelectionListener
     * @see org.eclipse.swt.widgets.ToolItem#addSelectionListener
     * @see org.eclipse.swt.widgets.TrayItem#addSelectionListener
     * @see org.eclipse.swt.widgets.Tree#addSelectionListener
     * @see org.eclipse.swt.widgets.TreeColumn#addSelectionListener
     * @see org.eclipse.swt.events.SelectionListener#widgetSelected
     * @see org.eclipse.swt.events.SelectionEvent
     */
    public static final int Selection = 13;

    /**
     * The default selection event type (value is 14).
     *
     * @see org.eclipse.swt.widgets.Widget#addListener
     * @see org.eclipse.swt.widgets.Display#addFilter
     * @see org.eclipse.swt.widgets.Event
     *
     * @see org.eclipse.swt.widgets.Combo#addSelectionListener
     * @see org.eclipse.swt.widgets.List#addSelectionListener
     * @see org.eclipse.swt.widgets.Spinner#addSelectionListener
     * @see org.eclipse.swt.widgets.Table#addSelectionListener
     * @see org.eclipse.swt.widgets.Text#addSelectionListener
     * @see org.eclipse.swt.widgets.TrayItem#addSelectionListener
     * @see org.eclipse.swt.widgets.Tree#addSelectionListener
     * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected
     * @see org.eclipse.swt.events.SelectionEvent
     */
    public static final int DefaultSelection = 14;

    /**
     * The focus in event type (value is 15).
     *
     * @see org.eclipse.swt.widgets.Widget#addListener
     * @see org.eclipse.swt.widgets.Display#addFilter
     * @see org.eclipse.swt.widgets.Event
     *
     * @see org.eclipse.swt.widgets.Control#addFocusListener
     * @see org.eclipse.swt.events.FocusListener#focusGained
     * @see org.eclipse.swt.events.FocusEvent
     */
    public static final int FocusIn = 15;

    /**
     * The focus out event type (value is 16).
     *
     * @see org.eclipse.swt.widgets.Widget#addListener
     * @see org.eclipse.swt.widgets.Display#addFilter
     * @see org.eclipse.swt.widgets.Event
     *
     * @see org.eclipse.swt.widgets.Control#addFocusListener
     * @see org.eclipse.swt.events.FocusListener#focusLost
     * @see org.eclipse.swt.events.FocusEvent
     */
    public static final int FocusOut = 16;

    /**
     * The expand event type (value is 17).
     *
     * <p>
     * Note:
     * </p>
     * <ul>
     * <li>Event.item: the TreeItem which gets expanded</li>
     * </ul>
     *
     * @see org.eclipse.swt.widgets.Widget#addListener
     * @see org.eclipse.swt.widgets.Display#addFilter
     * @see org.eclipse.swt.widgets.Event
     *
     * @see org.eclipse.swt.widgets.Tree#addTreeListener
     * @see org.eclipse.swt.events.TreeListener#treeExpanded
     * @see org.eclipse.swt.events.TreeEvent
     */
    public static final int Expand = 17;

    /**
     * The collapse event type (value is 18).
     *
     * <p>
     * Note:
     * </p>
     * <ul>
     * <li>Event.item: the TreeItem which gets collapsed</li>
     * </ul>
     *
     * @see org.eclipse.swt.widgets.Widget#addListener
     * @see org.eclipse.swt.widgets.Display#addFilter
     * @see org.eclipse.swt.widgets.Event
     *
     * @see org.eclipse.swt.widgets.Tree#addTreeListener
     * @see org.eclipse.swt.events.TreeListener#treeCollapsed
     * @see org.eclipse.swt.events.TreeEvent
     */
    public static final int Collapse = 18;

    /**
     * The iconify event type (value is 19).
     *
     * @see org.eclipse.swt.widgets.Widget#addListener
     * @see org.eclipse.swt.widgets.Display#addFilter
     * @see org.eclipse.swt.widgets.Event
     *
     * @see org.eclipse.swt.widgets.Shell#addShellListener
     * @see org.eclipse.swt.events.ShellListener#shellIconified
     * @see org.eclipse.swt.events.ShellEvent
     */
    public static final int Iconify = 19;

    /**
     * The de-iconify event type (value is 20).
     *
     * @see org.eclipse.swt.widgets.Widget#addListener
     * @see org.eclipse.swt.widgets.Display#addFilter
     * @see org.eclipse.swt.widgets.Event
     *
     * @see org.eclipse.swt.widgets.Shell#addShellListener
     * @see org.eclipse.swt.events.ShellListener#shellDeiconified
     * @see org.eclipse.swt.events.ShellEvent
     */
    public static final int Deiconify = 20;

    /**
     * The close event type (value is 21).
     *
     * @see org.eclipse.swt.widgets.Widget#addListener
     * @see org.eclipse.swt.widgets.Display#addListener
     * @see org.eclipse.swt.widgets.Display#addFilter
     * @see org.eclipse.swt.widgets.Event
     *
     * @see org.eclipse.swt.widgets.Shell#addShellListener
     * @see org.eclipse.swt.events.ShellListener#shellClosed
     * @see org.eclipse.swt.events.ShellEvent
     */
    public static final int Close = 21;

    /**
     * The show event type (value is 22).
     *
     * @see org.eclipse.swt.widgets.Widget#addListener
     * @see org.eclipse.swt.widgets.Display#addFilter
     * @see org.eclipse.swt.widgets.Event
     *
     * @see org.eclipse.swt.widgets.Menu#addMenuListener
     * @see org.eclipse.swt.events.MenuListener#menuShown
     * @see org.eclipse.swt.events.MenuEvent
     */
    public static final int Show = 22;

    /**
     * The hide event type (value is 23).
     *
     * @see org.eclipse.swt.widgets.Widget#addListener
     * @see org.eclipse.swt.widgets.Display#addFilter
     * @see org.eclipse.swt.widgets.Event
     *
     * @see org.eclipse.swt.widgets.Menu#addMenuListener
     * @see org.eclipse.swt.events.MenuListener#menuHidden
     * @see org.eclipse.swt.events.MenuEvent
     */
    public static final int Hide = 23;

    /**
     * The modify event type (value is 24).
     *
     * @see org.eclipse.swt.widgets.Widget#addListener
     * @see org.eclipse.swt.widgets.Display#addFilter
     * @see org.eclipse.swt.widgets.Event
     *
     * @see org.eclipse.swt.widgets.Combo#addModifyListener
     * @see org.eclipse.swt.widgets.Spinner#addModifyListener
     * @see org.eclipse.swt.widgets.Text#addModifyListener
     * @see org.eclipse.swt.events.ModifyListener#modifyText
     * @see org.eclipse.swt.events.ModifyEvent
     */
    public static final int Modify = 24;

    /**
     * The verify event type (value is 25).
     *
     * <p>
     * Note:This event is sent before the actual change happens and the listener is
     * able to prevent the change by setting event.doit to false.
     * </p>
     *
     * <ul>
     * <li>Event.text: the new text</li>
     * <li>Event.start: the start position where the new text is
     * inserted/replaced</li>
     * <li>Event.end: the end position where the new text is inserted/replaced</li>
     * <li>Event.character/.keyCode/.stateMask: if the change is caused by a key
     * event, these fields are taken from the key event</li>
     * </ul>
     *
     * @see org.eclipse.swt.widgets.Widget#addListener
     * @see org.eclipse.swt.widgets.Display#addFilter
     * @see org.eclipse.swt.widgets.Event
     *
     * @see org.eclipse.swt.custom.CCombo#addVerifyListener
     * @see org.eclipse.swt.widgets.Combo#addVerifyListener
     * @see org.eclipse.swt.custom.StyledText#addVerifyListener
     * @see org.eclipse.swt.widgets.Text#addVerifyListener
     * @see org.eclipse.swt.events.VerifyListener#verifyText
     * @see org.eclipse.swt.events.VerifyEvent
     */
    public static final int Verify = 25;

    /**
     * The activate event type (value is 26).
     *
     * @see org.eclipse.swt.widgets.Widget#addListener
     * @see org.eclipse.swt.widgets.Display#addFilter
     * @see org.eclipse.swt.widgets.Event
     *
     * @see org.eclipse.swt.widgets.Shell#addShellListener
     * @see org.eclipse.swt.events.ShellListener#shellActivated
     * @see org.eclipse.swt.events.ShellEvent
     */
    public static final int Activate = 26;

    /**
     * The deactivate event type (value is 27).
     *
     * @see org.eclipse.swt.widgets.Widget#addListener
     * @see org.eclipse.swt.widgets.Display#addFilter
     * @see org.eclipse.swt.widgets.Event
     *
     * @see org.eclipse.swt.widgets.Shell#addShellListener
     * @see org.eclipse.swt.events.ShellListener#shellDeactivated
     * @see org.eclipse.swt.events.ShellEvent
     */
    public static final int Deactivate = 27;

    /**
     * The help event type (value is 28).
     *
     * @see org.eclipse.swt.widgets.Widget#addListener
     * @see org.eclipse.swt.widgets.Display#addFilter
     * @see org.eclipse.swt.widgets.Event
     *
     * @see org.eclipse.swt.widgets.Control#addHelpListener
     * @see org.eclipse.swt.widgets.Menu#addHelpListener
     * @see org.eclipse.swt.widgets.MenuItem#addHelpListener
     * @see org.eclipse.swt.events.HelpListener#helpRequested
     * @see org.eclipse.swt.events.HelpEvent
     */
    public static final int Help = 28;

    /**
     * The drag detect event type (value is 29).
     *
     * @see org.eclipse.swt.widgets.Widget#addListener
     * @see org.eclipse.swt.widgets.Display#addFilter
     * @see org.eclipse.swt.widgets.Event
     *
     * @see org.eclipse.swt.widgets.Control#addDragDetectListener
     * @see org.eclipse.swt.events.DragDetectListener#dragDetected
     * @see org.eclipse.swt.events.DragDetectEvent
     * @see org.eclipse.swt.dnd.DragSource
     */
    public static final int DragDetect = 29;

    /**
     * The arm event type (value is 30).
     *
     * @see org.eclipse.swt.widgets.Widget#addListener
     * @see org.eclipse.swt.widgets.Display#addFilter
     * @see org.eclipse.swt.widgets.Event
     *
     * @see org.eclipse.swt.widgets.MenuItem#addArmListener
     * @see org.eclipse.swt.events.ArmListener#widgetArmed
     * @see org.eclipse.swt.events.ArmEvent
     */
    public static final int Arm = 30;

    /**
     * The traverse event type (value is 31).
     *
     * <p>
     * Note: Event.detail: one of the SWT.TRAVERSE_* constants
     * </p>
     * <ul>
     * <li>Event.stateMask: the or-combined bit masks of pressed modifiers, e.g.
     * SWT.MOD1</li>
     * <li>Event.doit: set to false if no default behavior should happen</li>
     * </ul>
     *
     * @see org.eclipse.swt.widgets.Widget#addListener
     * @see org.eclipse.swt.widgets.Display#addFilter
     * @see org.eclipse.swt.widgets.Event
     *
     * @see org.eclipse.swt.widgets.Control#addTraverseListener
     * @see org.eclipse.swt.events.TraverseListener#keyTraversed
     * @see org.eclipse.swt.events.TraverseEvent
     */
    public static final int Traverse = 31;

    /**
     * The mouse hover event type (value is 32).
     *
     * <p>
     * Note: This event is sent if the cursor stays over the same position for a
     * short time, e.g. to show a tool-tip
     * </p>
     *
     * @see org.eclipse.swt.widgets.Widget#addListener
     * @see org.eclipse.swt.widgets.Display#addFilter
     * @see org.eclipse.swt.widgets.Event
     *
     * @see org.eclipse.swt.widgets.Control#addMouseTrackListener
     * @see org.eclipse.swt.events.MouseTrackListener#mouseHover
     * @see org.eclipse.swt.events.MouseEvent
     */
    public static final int MouseHover = 32;

    /**
     * The hardware key down event type (value is 33).
     *
     * @see org.eclipse.swt.widgets.Widget#addListener
     * @see org.eclipse.swt.widgets.Display#addFilter
     * @see org.eclipse.swt.widgets.Event
     */
    public static final int HardKeyDown = 33;

    /**
     * The hardware key up event type (value is 34).
     *
     * @see org.eclipse.swt.widgets.Widget#addListener
     * @see org.eclipse.swt.widgets.Display#addFilter
     * @see org.eclipse.swt.widgets.Event
     */
    public static final int HardKeyUp = 34;

    /**
     * The menu detect event type (value is 35).
     *
     * <p>
     * Note:
     * </p>
     * <ul>
     * <li>Event.detail: either SWT.MENU_MOUSE or SWT.MENU_KEYBOARD</li>
     * <li>Event.x, Event.y: cursor position in display coordinates</li>
     * </ul>
     *
     * @see org.eclipse.swt.widgets.Widget#addListener
     * @see org.eclipse.swt.widgets.Display#addFilter
     * @see org.eclipse.swt.widgets.Event
     *
     * @see org.eclipse.swt.widgets.Control#addMenuDetectListener
     * @see org.eclipse.swt.widgets.TrayItem#addMenuDetectListener
     * @see org.eclipse.swt.events.MenuDetectListener#menuDetected
     * @see org.eclipse.swt.events.MenuDetectEvent
     *
     * @since 3.0
     */
    public static final int MenuDetect = 35;

    /**
     * The set data event type (value is 36).
     *
     * @see org.eclipse.swt.widgets.Widget#addListener
     * @see org.eclipse.swt.widgets.Display#addFilter
     * @see org.eclipse.swt.widgets.Event
     *
     * @see org.eclipse.swt.widgets.Table
     * @see org.eclipse.swt.widgets.Tree
     *
     * @since 3.0
     */
    public static final int SetData = 36;

    /**
     * The mouse vertical wheel event type (value is 37).
     *
     * @see org.eclipse.swt.widgets.Control#addMouseWheelListener
     * @see org.eclipse.swt.widgets.Display#addFilter
     * @see org.eclipse.swt.widgets.Event
     *
     * @since 3.6
     */
    public static final int MouseVerticalWheel = 37;

    /**
     * The mouse horizontal wheel event type (value is 38).
     *
     * @see org.eclipse.swt.widgets.Widget#addListener
     * @see org.eclipse.swt.widgets.Display#addFilter
     * @see org.eclipse.swt.widgets.Event
     *
     * @since 3.6
     */
    public static final int MouseHorizontalWheel = 38;

    /**
     * The mouse wheel event type (value is 37).
     * This is a synonym for {@link #MouseVerticalWheel} (value is 37).
     * Newer applications should use {@link #MouseVerticalWheel} instead
     * of {@link #MouseWheel} to make code more understandable.
     *
     * @see org.eclipse.swt.widgets.Control#addMouseWheelListener
     * @see org.eclipse.swt.widgets.Display#addFilter
     * @see org.eclipse.swt.widgets.Event
     *
     * @since 3.1
     */
    public static final int MouseWheel = MouseVerticalWheel;

    /**
     * The settings changed event type (value is 39).
     * <p>
     * The settings changed event is sent when an operating system
     * property, such as a system font or color, has been changed.
     * The event occurs after the property has been changed, but
     * before any widget is redrawn.  Applications that cache operating
     * system properties can use this event to update their caches.
     * A specific property change can be detected by querying the
     * new value of a property and comparing it with the equivalent
     * cached value.  The operating system automatically redraws and
     * lays out all widgets after this event is sent.
     * </p>
     *
     * @see org.eclipse.swt.widgets.Display#addListener
     * @see org.eclipse.swt.widgets.Event
     *
     * @since 3.2
     */
    public static final int Settings = 39;

    /**
     * The erase item event type (value is 40).
     *
     * <p>
     * Note:
     * </p>
     * <ul>
     * <li>Event.detail: an or-combined bit mask of the current state, e.g.
     * SWT.SELECTED, SWT.HOT</li>
     * <li>Event.gc: the graphics context to draw or modify colors</li>
     * <li>Event.x, Event.y, Event.width, Event.height: the cell's bounds</li>
     * <li>Event.item: the TreeItem or TableItem</li>
     * <li>Event.index: the column of the cell</li>
     * </ul>
     *
     * @see org.eclipse.swt.widgets.Widget#addListener
     * @see org.eclipse.swt.widgets.Display#addFilter
     * @see org.eclipse.swt.widgets.Event
     *
     * @since 3.2
     */
    public static final int EraseItem = 40;

    /**
     * The measure item event type (value is 41).
     *
     * <p>
     * Note:
     * </p>
     * <ul>
     * <li>Event.detail: an or-combined bit mask of the current state, e.g.
     * SWT.SELECTED, SWT.HOT</li>
     * <li>Event.gc: the graphics context to draw or modify colors</li>
     * <li>Event.x, Event.y, Event.width, Event.height: the cell's bounds, used as
     * input and output</li>
     * <li>Event.item: the TreeItem or TableItem</li>
     * <li>Event.index: the column of the cell</li>
     * </ul>
     *
     * @see org.eclipse.swt.widgets.Widget#addListener
     * @see org.eclipse.swt.widgets.Display#addFilter
     * @see org.eclipse.swt.widgets.Event
     *
     * @since 3.2
     */
    public static final int MeasureItem = 41;

    /**
     * The paint item event type (value is 42).
     *
     * <p>
     * Note:
     * </p>
     * <ul>
     * <li>Event.detail: an or-combined bit mask of the current state, e.g.
     * SWT.SELECTED, SWT.HOT</li>
     * <li>Event.gc: the graphics context to draw or modify colors</li>
     * <li>Event.x, Event.y, Event.width, Event.height: the cell's bounds</li>
     * <li>Event.item: the TreeItem or TableItem</li>
     * <li>Event.index: the column of the cell</li>
     * </ul>
     *
     * @see org.eclipse.swt.widgets.Widget#addListener
     * @see org.eclipse.swt.widgets.Display#addFilter
     * @see org.eclipse.swt.widgets.Event
     *
     * @since 3.2
     */
    public static final int PaintItem = 42;

    /**
     * The IME composition event type (value is 43).
     * <p>
     * The IME composition event is sent to allow
     * custom text editors to implement in-line
     * editing of international text.
     * </p>
     *
     * The detail field indicates the action to be taken:
     * <ul>
     * <li>{@link SWT#COMPOSITION_CHANGED}</li>
     * <li>{@link SWT#COMPOSITION_OFFSET}</li>
     * <li>{@link SWT#COMPOSITION_SELECTION}</li>
     * </ul>
     *
     * @see org.eclipse.swt.widgets.Widget#addListener
     * @see org.eclipse.swt.widgets.Display#addFilter
     * @see org.eclipse.swt.widgets.Event
     *
     * @since 3.4
     */
    public static final int ImeComposition = 43;

    /**
     * The orientation change event type (value is 44).
     * <p>
     * On some platforms the orientation of text widgets
     * can be changed by keyboard shortcut.
     * The application can use the <code>doit</code> field
     * of the event to stop the change from happening.
     * </p>
     *
     * @see org.eclipse.swt.widgets.Widget#addListener
     * @see org.eclipse.swt.widgets.Display#addFilter
     * @see org.eclipse.swt.widgets.Event
     *
     * @since 3.6
     */
    public static final int OrientationChange = 44;

    /**
     * The skin event type (value is 45).
     *
     * <p>
     * The skin event is sent by the display when a widget needs to
     * be skinned.
     * </p>
     *
     * @see org.eclipse.swt.widgets.Widget#addListener
     * @see org.eclipse.swt.widgets.Display#addFilter
     * @see org.eclipse.swt.widgets.Event
     * @see org.eclipse.swt.widgets.Widget#reskin(int)
     *
     * @since 3.6
     */
    public static final int Skin = 45;

    /**
     * The open document event type (value is 46).
     *
     * <p>
     * This event is sent when SWT receives notification that a document
     * should be opened.
     * </p>
     *
     * @see org.eclipse.swt.widgets.Display#addListener
     * @see org.eclipse.swt.widgets.Event
     *
     * @since 3.6
     */
    public static final int OpenDocument = 46;

    /**
     * The touch event type (value is 47).
     *
     * <p>
     * This event is sent when a touch has been performed
     * on a touch-based input source.
     * </p>
     *
     * @see org.eclipse.swt.widgets.Display#addListener
     * @see org.eclipse.swt.widgets.Event
     *
     * @since 3.7
     */
    public static final int Touch = 47;

    /**
     * The gesture event type (value is 48).
     *
     * <p>
     * This event is sent when a gesture has been performed.
     * </p>
     *
     * @see org.eclipse.swt.widgets.Display#addListener
     * @see org.eclipse.swt.widgets.Event
     * @see SWT#GESTURE_MAGNIFY
     * @see SWT#GESTURE_PAN
     * @see SWT#GESTURE_ROTATE
     * @see SWT#GESTURE_SWIPE
     *
     * @since 3.7
     */
    public static final int Gesture = 48;

    /**
     * The segments event type (value is 49).
     *
     * <p>
     * This event is sent when text content has been changed.
     * </p>
     *
     * @see org.eclipse.swt.widgets.Widget#addListener
     * @see org.eclipse.swt.widgets.Display#addFilter
     * @see org.eclipse.swt.widgets.Event
     *
     * @see org.eclipse.swt.widgets.Text#addSegmentListener
     * @see org.eclipse.swt.events.SegmentEvent
     *
     * @since 3.8
     */
    public static final int Segments = 49;

    /**
     * The PreEvent event type (value is 50).
     *
     * <p>
     * This event is sent before an event other than {@link #PreExternalEventDispatch} or
     * {@link #PostExternalEventDispatch} is dispatched.
     * </p>
     * <p>
     * The detail field of the event contains the type of the following event.
     * </p>
     *
     * @since 3.103
     */
    public static final int PreEvent = 50;

    /**
     * The PostEvent event type (value is 51).
     *
     * <p>
     * This event is sent after an event other than {@link #PreExternalEventDispatch} or
     * {@link #PostExternalEventDispatch} is dispatched.
     * </p>
     * <p>
     * The detail field of the event contains the type of the prior event.
     * </p>
     *
     * @since 3.103
     */
    public static final int PostEvent = 51;

    /**
     * The PreExternalEventDispatch event type (value is 52).
     *
     * <p>
     * This event is sent before calling a blocking method that does its own event dispatch outside
     * of the SWT code.
     * </p>
     *
     * @since 3.104
     */
    public static final int PreExternalEventDispatch = 52;

    /**
     * The PostExternalEventDispatch event type (value is 53).
     *
     * <p>
     * This event is sent after calling a blocking method that does its own event dispatch outside
     * of the SWT code.
     * </p>
     *
     * @since 3.104
     */
    public static final int PostExternalEventDispatch = 53;

    /**
     * @deprecated The same as PreExternalEventDispatch (value is 52).
     * @since 3.103
     */
    @Deprecated
    public static final int Sleep = PreExternalEventDispatch;

    /**
     * @deprecated The same as PostExternalEventDispatch (value is 53).
     * @since 3.103
     */
    @Deprecated
    public static final int Wakeup = PostExternalEventDispatch;

    /**
     * The open URL event type (value is 54).
     *
     * <p>
     * This event is sent when SWT receives notification that a URL
     * should be opened.
     * </p>
     *
     * @see org.eclipse.swt.widgets.Display#addListener
     * @see org.eclipse.swt.widgets.Event
     *
     * @since 3.107
     */
    public static final int OpenUrl = 54;

    /**
     * The SWT zoom change event type (value is 55).
     *
     * <p>
     * This event is sent on <code>Shell</code> when the SWT zoom has changed. The SWT
     * zoom changes when the operating system DPI or scale factor changes dynamically.
     * </p>
     * <p>
     * Note that this is a <em>HINT</em> and is not sent on platforms that do not
     * support dynamic DPI changes. This event is currently sent on Windows 10 and GTK
     * only.
     * </p>
     *
     * @see org.eclipse.swt.widgets.Widget#addListener
     * @see org.eclipse.swt.widgets.Display#addFilter
     * @see org.eclipse.swt.widgets.Event
     *
     * @since 3.108
     */
    public static final int ZoomChanged = 55;

    /**
     * The SWT emptiness change event type (value is 56).
     *
     * <p>
     * This event is sent on <code>Tree</code> when the first <code>TreeItem</code> was
     * added to it (with the <code>detail</code> field set to 0) or the last
     * <code>TreeItem</code> was removed from it (with the <code>detail</code> field
     * set to 1).
     * </p>
     *
     * @since 3.118
     */
    public static final int EmptinessChanged = 56;

    /* Event Details */
    /**
     * The IME composition event detail that indicates
     * a change in the IME composition. The text field
     * of the event is the new composition text.
     * The start and end indicate the offsets where the
     * composition text should be inserted.
     * The styles and ranges are stored in the IME
     * object (value is 1).
     *
     * @see SWT#ImeComposition
     *
     * @since 3.4
     */
    public static final int COMPOSITION_CHANGED = 1;

    /**
     * The IME composition event detail that indicates
     * that the IME needs the offset for a given location.
     * The x and y fields of the event are used by the
     * application to determine the offset.
     *
     * The index field of the event should be set to the
     * text offset at that location. The count field should
     * be set to indicate whether the location is closer to
     * the leading edge (0) or the trailing edge (1) (value is 2).
     *
     * @see SWT#ImeComposition
     * @see org.eclipse.swt.graphics.TextLayout#getOffset(int, int, int[])
     *
     * @since 3.4
     */
    public static final int COMPOSITION_OFFSET = 2;

    /**
     * The IME composition event detail that indicates
     * that IME needs the selected text and its start
     * and end offsets (value is 3).
     *
     * @see SWT#ImeComposition
     *
     * @since 3.4
     */
    public static final int COMPOSITION_SELECTION = 3;

    /**
     * Indicates that a user-interface component is being dragged,
     * for example dragging the thumb of a scroll bar (value is 1).
     */
    public static final int DRAG = 1;

    /**
     * Event detail field that indicates a user-interface component
     * state is selected (value is 1&lt;&lt;1).
     *
     * @since 3.2
     */
    public static final int SELECTED = 1 << 1;

    /**
     * Event detail field that indicates a user-interface component
     * state is focused (value is 1&lt;&lt;2).
     *
     * @since 3.2
     */
    public static final int FOCUSED = 1 << 2;

    /**
     * Event detail field that indicates a user-interface component
     * draws the background (value is 1&lt;&lt;3).
     *
     * @since 3.2
     */
    public static final int BACKGROUND = 1 << 3;

    /**
     * Event detail field that indicates a user-interface component
     * draws the foreground (value is 1&lt;&lt;4).
     *
     * @since 3.2
     */
    public static final int FOREGROUND = 1 << 4;

    /**
     * Event detail field that indicates a user-interface component
     * state is hot (value is 1&lt;&lt;5).
     *
     * @since 3.3
     */
    public static final int HOT = 1 << 5;

    /* This code is intentionally commented */
    //public static final int PRESSED = 1 << 3;
    //public static final int ACTIVE = 1 << 4;
    //public static final int DISABLED = 1 << 5;
    //public static final int HOT = 1 << 6;
    //public static final int DEFAULTED = 1 << 7;
    /**
     * Traversal event detail field value indicating that no
     * traversal action should be taken
     * (value is 0).
     */
    public static final int TRAVERSE_NONE = 0;

    /**
     * Traversal event detail field value indicating that the
     * key which designates that a dialog should be cancelled was
     * pressed; typically, this is the ESC key
     * (value is 1&lt;&lt;1).
     */
    public static final int TRAVERSE_ESCAPE = 1 << 1;

    /**
     * Traversal event detail field value indicating that the
     * key which activates the default button in a dialog was
     * pressed; typically, this is the ENTER key
     * (value is 1&lt;&lt;2).
     */
    public static final int TRAVERSE_RETURN = 1 << 2;

    /**
     * Traversal event detail field value indicating that the
     * key which designates that focus should be given to the
     * previous tab group was pressed; typically, this is the
     * SHIFT-TAB key sequence
     * (value is 1&lt;&lt;3).
     */
    public static final int TRAVERSE_TAB_PREVIOUS = 1 << 3;

    /**
     * Traversal event detail field value indicating that the
     * key which designates that focus should be given to the
     * next tab group was pressed; typically, this is the
     * TAB key
     * (value is 1&lt;&lt;4).
     */
    public static final int TRAVERSE_TAB_NEXT = 1 << 4;

    /**
     * Traversal event detail field value indicating that the
     * key which designates that focus should be given to the
     * previous tab item was pressed; typically, this is either
     * the LEFT-ARROW or UP-ARROW keys
     * (value is 1&lt;&lt;5).
     */
    public static final int TRAVERSE_ARROW_PREVIOUS = 1 << 5;

    /**
     * Traversal event detail field value indicating that the
     * key which designates that focus should be given to the
     * previous tab item was pressed; typically, this is either
     * the RIGHT-ARROW or DOWN-ARROW keys
     * (value is 1&lt;&lt;6).
     */
    public static final int TRAVERSE_ARROW_NEXT = 1 << 6;

    /**
     * Traversal event detail field value indicating that a
     * mnemonic key sequence was pressed
     * (value is 1&lt;&lt;7).
     */
    public static final int TRAVERSE_MNEMONIC = 1 << 7;

    /**
     * Traversal event detail field value indicating that the
     * key which designates that the previous page of a multi-page
     * window should be shown was pressed; typically, this
     * is the CTRL-PAGEUP key sequence
     * (value is 1&lt;&lt;8).
     */
    public static final int TRAVERSE_PAGE_PREVIOUS = 1 << 8;

    /**
     * Traversal event detail field value indicating that the
     * key which designates that the next page of a multi-page
     * window should be shown was pressed; typically, this
     * is the CTRL-PAGEDOWN key sequence
     * (value is 1&lt;&lt;9).
     */
    public static final int TRAVERSE_PAGE_NEXT = 1 << 9;

    /**
     * Gesture event detail field value indicating that a continuous
     * gesture is about to begin.
     *
     * @since 3.7
     */
    public static final int GESTURE_BEGIN = 1 << 1;

    /**
     * Gesture event detail field value indicating that a continuous
     * gesture has ended.
     *
     * @since 3.7
     */
    public static final int GESTURE_END = 1 << 2;

    /**
     * Gesture event detail field value indicating that a
     * rotation gesture has happened. Only the rotation field
     * of the event is valid.
     *
     * @since 3.7
     */
    public static final int GESTURE_ROTATE = 1 << 3;

    /**
     * Gesture event detail field value indicating that a
     * swipe gesture has happened.
     *
     * @since 3.7
     */
    public static final int GESTURE_SWIPE = 1 << 4;

    /**
     * Gesture event detail field value indicating that a
     * magnification gesture has happened.
     *
     * @since 3.7
     */
    public static final int GESTURE_MAGNIFY = 1 << 5;

    /**
     * Gesture event detail field value indicating that a
     * panning (two-finger scroll) gesture has happened.
     *
     * @since 3.7
     */
    public static final int GESTURE_PAN = 1 << 6;

    /**
     * A constant indicating that a finger touched the device.
     *
     * @see org.eclipse.swt.widgets.Touch#state
     *
     * @since 3.7
     */
    public static final int TOUCHSTATE_DOWN = 1 << 0;

    /**
     * A constant indicating that a finger moved on the device.
     *
     * @see org.eclipse.swt.widgets.Touch#state
     *
     * @since 3.7
     */
    public static final int TOUCHSTATE_MOVE = 1 << 1;

    /**
     * A constant indicating that a finger was lifted from the device.
     *
     * @see org.eclipse.swt.widgets.Touch#state
     *
     * @since 3.7
     */
    public static final int TOUCHSTATE_UP = 1 << 2;

    /**
     * MenuDetect event detail value indicating that a context menu
     * was requested by a mouse or other pointing device (value is 0).
     *
     * @since 3.8
     */
    public static final int MENU_MOUSE = 0;

    /**
     * MenuDetect event detail value indicating that a context menu
     * was requested by a keyboard or other focus-based device (value is 1).
     *
     * @since 3.8
     */
    public static final int MENU_KEYBOARD = 1;

    /**
     * A constant indicating that widgets have changed.
     * (value is 1&lt;&lt;1).
     *
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>Composite</code> layout</li>
     * </ul>
     *
     * @see org.eclipse.swt.widgets.Composite#layout(org.eclipse.swt.widgets.Control[], int)
     *
     * @since 3.6
     */
    public static final int CHANGED = 1 << 1;

    /**
     * A constant indicating that a given operation should be deferred.
     * (value is 1&lt;&lt;2).
     *
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>Composite</code> layout</li>
     * </ul>
     *
     * @see org.eclipse.swt.widgets.Composite#layout(org.eclipse.swt.widgets.Control[], int)
     *
     * @since 3.6
     */
    public static final int DEFER = 1 << 2;

    /**
     * A constant known to be zero (0), typically used in operations
     * which take bit flags to indicate that "no bits are set".
     */
    public static final int NONE = 0;

    /**
     * A constant known to be zero (0), used in operations which
     * take pointers to indicate a null argument.
     */
    public static final int NULL = 0;

    /**
     * Indicates that a default should be used (value is -1).
     */
    public static final int DEFAULT = -1;

    /**
     * Indicates that a property is off (value is 0).
     *
     * @since 3.1
     */
    public static final int OFF = 0;

    /**
     * Indicates that a property is on (value is 1).
     *
     * @since 3.1
     */
    public static final int ON = 1;

    /**
     * Indicates low quality (value is 1).
     *
     * @since 3.1
     */
    public static final int LOW = 1;

    /**
     * Indicates high quality (value is 2).
     *
     * @since 3.1
     */
    public static final int HIGH = 2;

    /**
     * Style constant for menu bar behavior (value is 1&lt;&lt;1).
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>Menu</code></li>
     * </ul>
     */
    public static final int BAR = 1 << 1;

    /**
     * Style constant for drop down menu/list behavior (value is 1&lt;&lt;2).
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>Menu</code></li>
     * <li><code>ToolItem</code></li>
     * <li><code>CoolItem</code></li>
     * <li><code>Combo</code></li>
     * <li><code>DateTime</code></li>
     * </ul>
     */
    public static final int DROP_DOWN = 1 << 2;

    /**
     * Style constant for pop up menu behavior (value is 1&lt;&lt;3).
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>Menu</code></li>
     * </ul>
     */
    public static final int POP_UP = 1 << 3;

    /**
     * Style constant for line separator behavior (value is 1&lt;&lt;1).
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>Label</code></li>
     * <li><code>MenuItem</code></li>
     * <li><code>ToolItem</code></li>
     * </ul>
     */
    public static final int SEPARATOR = 1 << 1;

    /**
     * Constant representing a flexible space separator in a ToolBar.
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>ToolItem.setWidth()</code></li>
     * </ul>
     *
     * @since 3.7
     */
    public static final int SEPARATOR_FILL = -2;

    /**
     * Style constant for toggle button behavior (value is 1&lt;&lt;1).
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>Button</code></li>
     * </ul>
     */
    public static final int TOGGLE = 1 << 1;

    /**
     * Style constant for arrow button behavior (value is 1&lt;&lt;2).
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>Button</code></li>
     * </ul>
     */
    public static final int ARROW = 1 << 2;

    /**
     * Style constant for push button behavior (value is 1&lt;&lt;3).
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>Button</code></li>
     * <li><code>MenuItem</code></li>
     * <li><code>ToolItem</code></li>
     * </ul>
     */
    public static final int PUSH = 1 << 3;

    /**
     * Style constant for radio button behavior (value is 1&lt;&lt;4).
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>Button</code></li>
     * <li><code>MenuItem</code></li>
     * <li><code>ToolItem</code></li>
     * </ul>
     */
    public static final int RADIO = 1 << 4;

    /**
     * Style constant for check box behavior (value is 1&lt;&lt;5).
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>Button</code></li>
     * <li><code>MenuItem</code></li>
     * <li><code>ToolItem</code></li>
     * <li><code>Table</code></li>
     * <li><code>Tree</code></li>
     * </ul>
     */
    public static final int CHECK = 1 << 5;

    /**
     * Style constant for cascade behavior (value is 1&lt;&lt;6).
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>MenuItem</code></li>
     * </ul>
     */
    public static final int CASCADE = 1 << 6;

    /**
     * Style constant for multi-selection behavior in lists
     * and multiple line support on text fields (value is 1&lt;&lt;1).
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>Text</code></li>
     * <li><code>List</code></li>
     * <li><code>Table</code></li>
     * <li><code>Tree</code></li>
     * <li><code>FileDialog</code></li>
     * </ul>
     */
    public static final int MULTI = 1 << 1;

    /**
     * Style constant for single selection behavior in lists
     * and single line support on text fields (value is 1&lt;&lt;2).
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>Text</code></li>
     * <li><code>List</code></li>
     * <li><code>Table</code></li>
     * <li><code>Tree</code></li>
     * </ul>
     */
    public static final int SINGLE = 1 << 2;

    /**
     * Style constant for read-only behavior (value is 1&lt;&lt;3).
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>Combo</code></li>
     * <li><code>Text</code></li>
     * </ul>
     */
    public static final int READ_ONLY = 1 << 3;

    /**
     * Style constant for automatic line wrap behavior (value is 1&lt;&lt;6).
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>Button</code></li>
     * <li><code>Label</code></li>
     * <li><code>Text</code></li>
     * <li><code>ToolBar</code></li>
     * <li><code>Spinner</code></li>
     * </ul>
     */
    public static final int WRAP = 1 << 6;

    /**
     * Style constant for search behavior (value is 1&lt;&lt;7).
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>Text</code></li>
     * </ul>
     *
     * @since 3.3
     */
    public static final int SEARCH = 1 << 7;

    /**
     * Style constant for simple (not drop down) behavior (value is 1&lt;&lt;6).
     * <br>Note that this is a <em>HINT</em>.
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>Combo</code></li>
     * </ul>
     */
    public static final int SIMPLE = 1 << 6;

    /**
     * Style constant for password behavior (value is 1&lt;&lt;22).
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>Text</code></li>
     * </ul>
     *
     * @since 3.0
     */
    public static final int PASSWORD = 1 << 22;

    /**
     * Style constant for shadow in behavior (value is 1&lt;&lt;2).
     * <br>Note that this is a <em>HINT</em>.
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>Label</code></li>
     * <li><code>Group</code></li>
     * </ul>
     */
    public static final int SHADOW_IN = 1 << 2;

    /**
     * Style constant for shadow out behavior (value is 1&lt;&lt;3).
     * <br>Note that this is a <em>HINT</em>.
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>Label</code></li>
     * <li><code>Group</code></li>
     * <li><code>ToolBar</code></li>
     * </ul>
     */
    public static final int SHADOW_OUT = 1 << 3;

    /**
     * Style constant for shadow etched in behavior (value is 1&lt;&lt;4).
     * <br>Note that this is a <em>HINT</em>. It is currently ignored on all platforms.
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>Group</code></li>
     * </ul>
     */
    public static final int SHADOW_ETCHED_IN = 1 << 4;

    /**
     * Style constant for shadow etched out behavior (value is 1&lt;&lt;6).
     * <br>Note that this is a <em>HINT</em>. It is currently ignored on all platforms.
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>Group</code></li>
     * </ul>
     */
    public static final int SHADOW_ETCHED_OUT = 1 << 6;

    /**
     * Style constant for no shadow behavior (value is 1&lt;&lt;5).
     * <br>Note that this is a <em>HINT</em>.
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>Label</code></li>
     * <li><code>Group</code></li>
     * </ul>
     */
    public static final int SHADOW_NONE = 1 << 5;

    /**
     * Style constant for progress bar behavior (value is 1&lt;&lt;1).
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>ProgressBar</code></li>
     * </ul>
     */
    public static final int INDETERMINATE = 1 << 1;

    /**
     * Style constant for tool window behavior (value is 1&lt;&lt;2).
     * <p>
     * A tool window is a window intended to be used as a floating toolbar.
     * It typically has a title bar that is shorter than a normal title bar,
     * and the window title is typically drawn using a smaller font.
     * <br>Note that this is a <em>HINT</em>.
     * </p><p><b>Used By:</b></p>
     * <ul>
     * <li><code>Decorations</code> and subclasses</li>
     * </ul>
     */
    public static final int TOOL = 1 << 2;

    /**
     * Style constant to ensure no trimmings are used (value is 1&lt;&lt;3).
     * <br>Note that this overrides all other trim styles.
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>Decorations</code> and subclasses</li>
     * </ul>
     */
    public static final int NO_TRIM = 1 << 3;

    /**
     * Style constant for resize box trim (value is 1&lt;&lt;4).
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>Decorations</code> and subclasses</li>
     * <li><code>Tracker</code></li>
     * </ul>
     */
    public static final int RESIZE = 1 << 4;

    /**
     * Style constant for title area trim (value is 1&lt;&lt;5).
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>Decorations</code> and subclasses</li>
     * </ul>
     */
    public static final int TITLE = 1 << 5;

    /**
     * Style constant for close box trim (value is 1&lt;&lt;6,
     * since we do not distinguish between CLOSE style and MENU style).
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>Decorations</code> and subclasses</li>
     * </ul>
     */
    public static final int CLOSE = 1 << 6;

    /**
     * Style constant for shell menu trim (value is 1&lt;&lt;6,
     * since we do not distinguish between CLOSE style and MENU style).
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>Decorations</code> and subclasses</li>
     * </ul>
     */
    public static final int MENU = CLOSE;

    /**
     * Style constant for minimize box trim (value is 1&lt;&lt;7).
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>Decorations</code> and subclasses</li>
     * </ul>
     */
    public static final int MIN = 1 << 7;

    /**
     * Style constant for maximize box trim (value is 1&lt;&lt;10).
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>Decorations</code> and subclasses</li>
     * </ul>
     */
    public static final int MAX = 1 << 10;

    /**
     * Style constant for the no move behavior (value is 1&lt;&lt;23).
     * Creates the title trim when no other trim style is specified.
     * Doesn't create the title trim when NO_TRIM is specified.
     * <p>Note that this is a <em>HINT</em>.
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>Shell</code></li>
     * </ul>
     * @since 3.105
     */
    public static final int NO_MOVE = 1 << 23;

    /**
     * Style constant for horizontal scrollbar behavior (value is 1&lt;&lt;8).
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>Scrollable</code> and subclasses</li>
     * </ul>
     */
    public static final int H_SCROLL = 1 << 8;

    /**
     * Style constant for vertical scrollbar behavior (value is 1&lt;&lt;9).
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>Scrollable</code> and subclasses</li>
     * </ul>
     */
    public static final int V_SCROLL = 1 << 9;

    /**
     * Style constant for no scrollbar behavior (value is 1&lt;&lt;4).
     * <p>
     * When neither H_SCROLL or V_SCROLL are specified, controls
     * are free to create the default scroll bars for the control.
     * Using NO_SCROLL overrides the default and forces the control
     * to have no scroll bars.
     *
     * <b>Used By:</b></p>
     * <ul>
     * <li><code>Tree</code></li>
     * <li><code>Table</code></li>
     * </ul>
     *
     * @since 3.4
     */
    public static final int NO_SCROLL = 1 << 4;

    /**
     * Style constant for bordered behavior (value is 1&lt;&lt;11).
     * <br>Note that this is a <em>HINT</em>.
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>Control</code> and subclasses</li>
     * </ul>
     */
    public static final int BORDER = 1 << 11;

    /**
     * Style constant indicating that the window manager should clip
     * a widget's children with respect to its viewable area. (value is 1&lt;&lt;12).
     * <br>Note that this is a <em>HINT</em>.
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>Control</code> and subclasses</li>
     * </ul>
     */
    public static final int CLIP_CHILDREN = 1 << 12;

    /**
     * Style constant indicating that the window manager should clip
     * a widget's siblings with respect to its viewable area. (value is 1&lt;&lt;13).
     * <br>Note that this is a <em>HINT</em>.
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>Control</code> and subclasses</li>
     * </ul>
     */
    public static final int CLIP_SIBLINGS = 1 << 13;

    /**
     * Style constant for always on top behavior (value is 1&lt;&lt;14).
     * <br>Note that this is a <em>HINT</em>.
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>Shell</code> and subclasses</li>
     * </ul>
     */
    public static final int ON_TOP = 1 << 14;

    /**
     * Style constant for sheet window behavior (value is 1&lt;&lt;28).
     * <p>
     * A sheet window is a window intended to be used as a temporary modal
     * dialog that is attached to a parent window. It is typically used to
     * prompt the user before proceeding. The window trim, positioning and
     * general look of a sheet window is platform specific. For example,
     * on the Macintosh, at the time this documentation was written, the
     * window title is not visible.
     * <br>Note that this is a <em>HINT</em>.
     * </p><p><b>Used By:</b></p>
     * <ul>
     * <li><code>Dialog</code> and subclasses</li>
     * <li><code>Shell</code> and subclasses</li>
     * </ul>
     *
     * @since 3.5
     */
    public static final int SHEET = 1 << 28;

    /**
     * Trim style convenience constant for the most common top level shell appearance
     * (value is CLOSE|TITLE|MIN|MAX|RESIZE).
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>Shell</code></li>
     * </ul>
     */
    public static final int SHELL_TRIM = CLOSE | TITLE | MIN | MAX | RESIZE;

    /**
     * Trim style convenience constant for the most common dialog shell appearance
     * (value is CLOSE|TITLE|BORDER).
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>Shell</code></li>
     * </ul>
     */
    public static final int DIALOG_TRIM = TITLE | CLOSE | BORDER;

    /**
     * Style constant for modeless behavior (value is 0).
     * <br>Note that this is a <em>HINT</em>.
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>Dialog</code></li>
     * <li><code>Shell</code></li>
     * </ul>
     */
    public static final int MODELESS = 0;

    /**
     * Style constant for primary modal behavior (value is 1&lt;&lt;15).
     * <br>Note that this is a <em>HINT</em>.
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>Dialog</code></li>
     * <li><code>Shell</code></li>
     * </ul>
     */
    public static final int PRIMARY_MODAL = 1 << 15;

    /**
     * Style constant for application modal behavior (value is 1&lt;&lt;16).
     * <br>Note that this is a <em>HINT</em>.
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>Dialog</code></li>
     * <li><code>Shell</code></li>
     * </ul>
     */
    public static final int APPLICATION_MODAL = 1 << 16;

    /**
     * Style constant for system modal behavior (value is 1&lt;&lt;17).
     * <br>Note that this is a <em>HINT</em>.
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>Dialog</code></li>
     * <li><code>Shell</code></li>
     * </ul>
     */
    public static final int SYSTEM_MODAL = 1 << 17;

    /**
     * Style constant for selection hiding behavior when the widget loses focus (value is 1&lt;&lt;15).
     * <br>Note that this is a <em>HINT</em>.
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>Table</code></li>
     * </ul>
     */
    public static final int HIDE_SELECTION = 1 << 15;

    /**
     * Style constant for full row selection behavior and
     * selection constant indicating that a full line should be
     * drawn. (value is 1&lt;&lt;16).
     * <br>Note that for some widgets this is a <em>HINT</em>.
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>Table</code></li>
     * <li><code>Tree</code></li>
     * <li><code>StyledText</code></li>
     * <li><code>TextLayout</code></li>
     * </ul>
     */
    public static final int FULL_SELECTION = 1 << 16;

    /**
     * Style constant for flat appearance. (value is 1&lt;&lt;23).
     * <br>Note that this is a <em>HINT</em>.
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>Button</code></li>
     * <li><code>ToolBar</code></li>
     * </ul>
     */
    public static final int FLAT = 1 << 23;

    /**
     * Style constant for smooth appearance. (value is 1&lt;&lt;16).
     * <br>Note that this is a <em>HINT</em>.
     * <p><b>Used By:</b></p><ul>
     * <li><code>ProgressBar</code></li>
     * <li><code>Sash</code></li>
     * </ul>
     */
    public static final int SMOOTH = 1 << 16;

    /**
     * Style constant for no background behavior (value is 1&lt;&lt;18).
     * <p>
     * By default, before a widget paints, the client area is filled with the current background.
     * When this style is specified, the background is not filled, and the application is responsible
     * for filling every pixel of the client area.
     * This style might be used as an alternative to "double-buffering" in order to reduce flicker.
     * This style does not mean "transparent" - widgets that are obscured will not draw through.
     * </p><p><b>Used By:</b></p>
     * <ul>
     * <li><code>Composite</code></li>
     * </ul>
     */
    public static final int NO_BACKGROUND = 1 << 18;

    /**
     * Style constant for no focus from the mouse behavior (value is 1&lt;&lt;19).
     * <p>
     * Normally, when the user clicks on a control, focus is assigned to that
     * control, providing the control has no children.  Some controls, such as
     * tool bars and sashes, don't normally take focus when the mouse is clicked
     * or accept focus when assigned from within the program.  This style allows
     * Composites to implement "no focus" mouse behavior.
     *
     * <br>Note that this is a <em>HINT</em>.
     * </p>
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>Composite</code></li>
     * </ul>
     */
    public static final int NO_FOCUS = 1 << 19;

    /**
     * Style constant for no redraw on resize behavior (value is 1&lt;&lt;20).
     * <p>
     * This style stops the entire client area from being invalidated when the size
     * of the Canvas changes. Specifically, when the size of the Canvas gets smaller,
     * the SWT.Paint event is not sent. When it gets bigger, an SWT.Paint event is
     * sent with a GC clipped to only the new areas to be painted. Without this
     * style, the entire client area will be repainted.
     *
     * <br>Note that this is a <em>HINT</em>.
     * </p><p><b>Used By:</b></p>
     * <ul>
     * <li><code>Composite</code></li>
     * </ul>
     */
    public static final int NO_REDRAW_RESIZE = 1 << 20;

    /**
     * Style constant for no paint event merging behavior (value is 1&lt;&lt;21).
     *
     * <br>Note that this is a <em>HINT</em>.
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>Composite</code></li>
     * </ul>
     */
    public static final int NO_MERGE_PAINTS = 1 << 21;

    /**
     * Style constant for preventing child radio group behavior (value is 1&lt;&lt;22).
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>Composite</code></li>
     * <li><code>Menu</code></li>
     * </ul>
     */
    public static final int NO_RADIO_GROUP = 1 << 22;

    /**
     * Style constant for left to right orientation (value is 1&lt;&lt;25).
     * <p>
     * When orientation is not explicitly specified, orientation is
     * inherited.  This means that children will be assigned the
     * orientation of their parent.  To override this behavior and
     * force an orientation for a child, explicitly set the orientation
     * of the child when that child is created.
     * <br>Note that this is a <em>HINT</em>.
     * </p>
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>Control</code></li>
     * <li><code>Menu</code></li>
     * <li><code>GC</code></li>
     * </ul>
     *
     * @since 2.1.2
     */
    public static final int LEFT_TO_RIGHT = 1 << 25;

    /**
     * Style constant for right to left orientation (value is 1&lt;&lt;26).
     * <p>
     * When orientation is not explicitly specified, orientation is
     * inherited.  This means that children will be assigned the
     * orientation of their parent.  To override this behavior and
     * force an orientation for a child, explicitly set the orientation
     * of the child when that child is created.
     * <br>Note that this is a <em>HINT</em>.
     * </p>
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>Control</code></li>
     * <li><code>Menu</code></li>
     * <li><code>GC</code></li>
     * </ul>
     *
     * @since 2.1.2
     */
    public static final int RIGHT_TO_LEFT = 1 << 26;

    /**
     * Style constant to indicate coordinate mirroring (value is 1&lt;&lt;27).
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>Control</code></li>
     * <li><code>Menu</code></li>
     * </ul>
     *
     * @since 2.1.2
     */
    public static final int MIRRORED = 1 << 27;

    /**
     * Style constant to allow embedding (value is 1&lt;&lt;24).
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>Composite</code></li>
     * </ul>
     *
     * @since 3.0
     */
    public static final int EMBEDDED = 1 << 24;

    /**
     * Style constant to allow virtual data (value is 1&lt;&lt;28).
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>Table</code></li>
     * <li><code>Tree</code></li>
     * </ul>
     *
     * @since 3.0
     */
    public static final int VIRTUAL = 1 << 28;

    /**
     * Style constant to indicate double buffering (value is 1&lt;&lt;29).
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>Control</code></li>
     * </ul>
     *
     * @since 3.1
     */
    public static final int DOUBLE_BUFFERED = 1 << 29;

    /**
     * Style constant for transparent behavior (value is 1&lt;&lt;30).
     * <p>
     * By default, before a widget paints, the client area is filled with the current background.
     * When this style is specified, the background is not filled and widgets that are obscured
     * will draw through.
     * </p><p><b>Used By:</b></p>
     * <ul>
     * <li><code>Composite</code></li>
     * </ul>
     *
     * @since 3.4
     *
     * WARNING: THIS API IS UNDER CONSTRUCTION AND SHOULD NOT BE USED
     */
    public static final int TRANSPARENT = 1 << 30;

    /**
     * Style constant to indicate base text direction (value is 1&lt;&lt;31).
     * <p>
     * When the bit is set, text direction mismatches the widget orientation.
     * <br>Note that this is a <em>HINT</em>.
     * </p>
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>Control</code></li>
     * </ul>
     *
     * @see org.eclipse.swt.widgets.Control#setTextDirection(int)
     * @see org.eclipse.swt.widgets.Control#getTextDirection()
     *
     * @since 3.102
     */
    public static final int FLIP_TEXT_DIRECTION = 1 << 31;

    /**
     * A bit mask to indicate Bidi "auto" text direction.
     * <p>
     * When the bit is set, text direction is derived from the direction of the
     * first strong Bidi character.
     * </p>
     * <br>Note that this is a <em>HINT</em> and it works on Windows only.
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>Control</code></li>
     * <li><code>TextLayout</code></li>
     * </ul>
     *
     * @see org.eclipse.swt.widgets.Control#setTextDirection(int)
     * @see org.eclipse.swt.graphics.TextLayout#setTextDirection(int)
     *
     * @since 3.105
     */
    public static final int AUTO_TEXT_DIRECTION = SWT.LEFT_TO_RIGHT | SWT.RIGHT_TO_LEFT;

    /**
     * Style constant for align up behavior (value is 1&lt;&lt;7,
     * since align UP and align TOP are considered the same).
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>Button</code> with <code>ARROW</code> style</li>
     * <li><code>Tracker</code></li>
     * <li><code>Table</code></li>
     * <li><code>Tree</code></li>
     * </ul>
     */
    public static final int UP = 1 << 7;

    /**
     * Style constant to indicate single underline (value is 0).
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>TextStyle</code></li>
     * </ul>
     *
     * @since 3.4
     */
    public static final int UNDERLINE_SINGLE = 0;

    /**
     * Style constant to indicate double underline (value is 1).
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>TextStyle</code></li>
     * </ul>
     *
     * @since 3.4
     */
    public static final int UNDERLINE_DOUBLE = 1;

    /**
     * Style constant to indicate error underline (value is 2).
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>TextStyle</code></li>
     * </ul>
     *
     * @since 3.4
     */
    public static final int UNDERLINE_ERROR = 2;

    /**
     * Style constant to indicate squiggle underline (value is 3).
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>TextStyle</code></li>
     * </ul>
     *
     * @since 3.4
     */
    public static final int UNDERLINE_SQUIGGLE = 3;

    /**
     * Style constant to indicate link underline (value is 0).
     * <p>
     * If the text color or the underline color are not set in the range
     * the usage of <code>UNDERLINE_LINK</code> will change these colors
     * to the preferred link color of the platform.<br>
     * Note that clients that use this style, such as <code>StyledText</code>,
     * will include code to track the mouse and change the cursor to the hand
     * cursor when mouse is over the link.
     * </p>
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>TextStyle</code></li>
     * </ul>
     *
     * @since 3.5
     */
    public static final int UNDERLINE_LINK = 4;

    /**
     * Style constant to indicate solid border (value is 1).
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>TextStyle</code></li>
     * </ul>
     *
     * @since 3.4
     */
    public static final int BORDER_SOLID = 1;

    /**
     * Style constant to indicate dashed border (value is 2).
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>TextStyle</code></li>
     * </ul>
     *
     * @since 3.4
     */
    public static final int BORDER_DASH = 2;

    /**
     * Style constant to indicate dotted border (value is 4).
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>TextStyle</code></li>
     * </ul>
     *
     * @since 3.4
     */
    public static final int BORDER_DOT = 4;

    /**
     * Style constant for align top behavior (value is 1&lt;&lt;7,
     * since align UP and align TOP are considered the same).
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>FormAttachment</code> in a <code>FormLayout</code></li>
     * <li><code>BoderData</code> in a <code>BoderLayout</code></li>
     * </ul>
     */
    public static final int TOP = UP;

    /**
     * Style constant for align down behavior (value is 1&lt;&lt;10,
     * since align DOWN and align BOTTOM are considered the same).
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>Button</code> with <code>ARROW</code> style</li>
     * <li><code>Tracker</code></li>
     * <li><code>Table</code></li>
     * <li><code>Tree</code></li>
     * </ul>
     */
    public static final int DOWN = 1 << 10;

    /**
     * Style constant for align bottom behavior (value is 1&lt;&lt;10,
     * since align DOWN and align BOTTOM are considered the same).
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>FormAttachment</code> in a <code>FormLayout</code></li>
     * <li><code>TabFolder</code></li>
     * <li><code>BoderData</code> in a <code>BoderLayout</code></li>
     * </ul>
     */
    public static final int BOTTOM = DOWN;

    /**
     * Style constant for leading alignment (value is 1&lt;&lt;14).
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>Button</code></li>
     * <li><code>Label</code></li>
     * <li><code>Text</code></li>
     * <li><code>TableColumn</code></li>
     * <li><code>TreeColumn</code></li>
     * <li><code>Tracker</code></li>
     * <li><code>FormAttachment</code> in a <code>FormLayout</code></li>
     * </ul>
     *
     * @since 2.1.2
     */
    public static final int LEAD = 1 << 14;

    /**
     * Style constant for align left behavior (value is 1&lt;&lt;14).
     * This is a synonym for {@link #LEAD} (value is 1&lt;&lt;14).  Newer
     * applications should use {@link #LEAD} instead of {@link #LEFT} to make code more
     * understandable on right-to-left platforms.
     * <p>
     * This constant can also be used to representing the left keyboard
     * location during a key event.
     * </p>
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>BoderData</code> in a <code>BoderLayout</code></li>
     * </ul>
     */
    public static final int LEFT = LEAD;

    /**
     * Style constant for trailing alignment (value is 1&lt;&lt;17).
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>Button</code></li>
     * <li><code>Label</code></li>
     * <li><code>Text</code></li>
     * <li><code>TableColumn</code></li>
     * <li><code>TreeColumn</code></li>
     * <li><code>Tracker</code></li>
     * <li><code>FormAttachment</code> in a <code>FormLayout</code></li>
     * </ul>
     *
     * @since 2.1.2
     */
    public static final int TRAIL = 1 << 17;

    /**
     * Style constant for align right behavior (value is 1&lt;&lt;17).
     * This is a synonym for {@link #TRAIL} (value is 1&lt;&lt;17).  Newer
     * applications should use {@link #TRAIL} instead of {@link #RIGHT} to make code more
     * understandable on right-to-left platforms.
     * <p>
     * This constant can also be used to representing the right keyboard
     * location during a key event.
     * </p>
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>BoderData</code> in a <code>BoderLayout</code></li>
     * </ul>
     */
    public static final int RIGHT = TRAIL;

    /**
     * Style constant for align center behavior (value is 1&lt;&lt;24).
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>Button</code></li>
     * <li><code>Label</code></li>
     * <li><code>TableColumn</code></li>
     * <li><code>FormAttachment</code> in a <code>FormLayout</code></li>
     * <li><code>BoderData</code> in a <code>BoderLayout</code></li>
     * </ul>
     */
    public static final int CENTER = 1 << 24;

    /**
     * Style constant for horizontal alignment or orientation behavior (value is 1&lt;&lt;8).
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>Label</code></li>
     * <li><code>ProgressBar</code></li>
     * <li><code>Sash</code></li>
     * <li><code>Scale</code></li>
     * <li><code>ScrollBar</code></li>
     * <li><code>Slider</code></li>
     * <li><code>ToolBar</code></li>
     * <li><code>FillLayout</code> type</li>
     * <li><code>RowLayout</code> type</li>
     * </ul>
     */
    public static final int HORIZONTAL = 1 << 8;

    /**
     * Style constant for vertical alignment or orientation behavior (value is 1&lt;&lt;9).
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>Label</code></li>
     * <li><code>ProgressBar</code></li>
     * <li><code>Sash</code></li>
     * <li><code>Scale</code></li>
     * <li><code>ScrollBar</code></li>
     * <li><code>Slider</code></li>
     * <li><code>ToolBar</code></li>
     * <li><code>CoolBar</code></li>
     * <li><code>FillLayout</code> type</li>
     * <li><code>RowLayout</code> type</li>
     * </ul>
     */
    public static final int VERTICAL = 1 << 9;

    /**
     * Style constant for date display (value is 1&lt;&lt;5).
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>DateTime</code></li>
     * </ul>
     *
     * @since 3.3
     */
    public static final int DATE = 1 << 5;

    /**
     * Style constant for time display (value is 1&lt;&lt;7).
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>DateTime</code></li>
     * </ul>
     *
     * @since 3.3
     */
    public static final int TIME = 1 << 7;

    /**
     * Style constant for calendar display (value is 1&lt;&lt;10).
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>DateTime</code></li>
     * </ul>
     *
     * @since 3.3
     */
    public static final int CALENDAR = 1 << 10;

    /**
     * Style constant for displaying week numbers in the calendar.
     * <br>Note that this is a <em>HINT</em> and is supported on Windows &amp; GTK platforms only.
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>DateTime</code></li>
     * </ul>
     *
     * @since 3.108
     */
    public static final int CALENDAR_WEEKNUMBERS = 1 << 14;

    /**
     * Style constant for short date/time format (value is 1&lt;&lt;15).
     * <p>
     * A short date displays the month and year.
     * A short time displays hours and minutes.
     * <br>Note that this is a <em>HINT</em>.
     * </p>
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>DateTime</code></li>
     * </ul>
     *
     * @since 3.3
     */
    public static final int SHORT = 1 << 15;

    /**
     * Style constant for medium date/time format (value is 1&lt;&lt;16).
     * <p>
     * A medium date displays the day, month and year.
     * A medium time displays hours, minutes, and seconds.
     * <br>Note that this is a <em>HINT</em>.
     * </p>
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>DateTime</code></li>
     * </ul>
     *
     * @since 3.3
     */
    public static final int MEDIUM = 1 << 16;

    /**
     * Style constant for long date/time format (value is 1&lt;&lt;28).
     * <p>
     * A long date displays the day, month and year.
     * A long time displays hours, minutes, and seconds.
     * The day and month names may be displayed.
     * <br>Note that this is a <em>HINT</em>.
     * </p>
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>DateTime</code></li>
     * </ul>
     *
     * @since 3.3
     */
    public static final int LONG = 1 << 28;

    /**
     * Style constant specifying that a Browser should use a Mozilla GRE
     * for rendering its content (value is 1&lt;&lt;15).
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>Browser</code></li>
     * </ul>
     *
     * @since 3.3
     * @deprecated This style is deprecated and will be removed in the future.
     *             XULRunner as a browser renderer is no longer supported. Use
     *             <code>SWT.WEBKIT</code> or <code>SWT.NONE</code> instead.
     */
    @Deprecated
    public static final int MOZILLA = 1 << 15;

    /**
     * Style constant specifying that a Browser should use WebKit
     * for rendering its content (value is 1&lt;&lt;16).
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>Browser</code></li>
     * </ul>
     *
     * @since 3.7
     */
    public static final int WEBKIT = 1 << 16;

    /**
     * Style constant specifying that a Browser should use Chromium Embedded Framework
     * for rendering its content (value is 1&lt;&lt;17).
     * <p><b>Note:</b> No longer supported and ignored.</p>
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>Browser</code></li>
     * </ul>
     *
     * @since 3.115
     * @deprecated Support for Chromium was limited to ancient and full of CVEs version of Chromium.
     * See <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=572010">bug report</a> for details
     */
    @Deprecated(forRemoval = true, since = "2024-03")
    public static final int CHROMIUM = 1 << 17;

    /**
     * Style constant specifying that a Browser should use Edge (WebView2)
     * for rendering its content (value is 1&lt;&lt;18).
     * <p>NOTE: Edge integration is experimental, it isn't a drop-in replacement
     * for Internet Explorer.</p>
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>Browser</code></li>
     * </ul>
     *
     * @since 3.116
     */
    public static final int EDGE = 1 << 18;

    /**
     * Style constant for balloon behavior (value is 1&lt;&lt;12).
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>ToolTip</code></li>
     * </ul>
     *
     * @since 3.2
     */
    public static final int BALLOON = 1 << 12;

    /**
     * Style constant for alignment or orientation behavior (value is 1).
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>GridLayout</code> type</li>
     * </ul>
     */
    public static final int BEGINNING = 1;

    /**
     * Style constant for alignment or orientation behavior (value is 4).
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>GridLayout</code> type</li>
     * </ul>
     */
    public static final int FILL = 4;

    /**
     * Input Method Editor style constant for double byte
     * input behavior (value is 1&lt;&lt;1).
     */
    public static final int DBCS = 1 << 1;

    /**
     * Input Method Editor style constant for alpha
     * input behavior (value is 1&lt;&lt;2).
     */
    public static final int ALPHA = 1 << 2;

    /**
     * Input Method Editor style constant for native
     * input behavior (value is 1&lt;&lt;3).
     */
    public static final int NATIVE = 1 << 3;

    /**
     * Input Method Editor style constant for phonetic
     * input behavior (value is 1&lt;&lt;4).
     */
    public static final int PHONETIC = 1 << 4;

    /**
     * Input Method Editor style constant for romanicized
     * input behavior (value is 1&lt;&lt;5).
     */
    public static final int ROMAN = 1 << 5;

    /**
     * ASCII character convenience constant for the backspace character
     * (value is the <code>char</code> '\b').
     */
    public static final char BS = '\b';

    /**
     * ASCII character convenience constant for the carriage return character
     * (value is the <code>char</code> '\r').
     */
    public static final char CR = '\r';

    /**
     * ASCII character convenience constant for the delete character
     * (value is the <code>char</code> with value 127).
     */
    public static final char DEL = 0x7F;

    /**
     * ASCII character convenience constant for the escape character
     * (value is the <code>char</code> with value 27).
     */
    public static final char ESC = 0x1B;

    /**
     * ASCII character convenience constant for the line feed character
     * (value is the <code>char</code> '\n').
     */
    public static final char LF = '\n';

    /**
     * ASCII character convenience constant for the tab character
     * (value is the <code>char</code> '\t').
     *
     * @since 2.1
     */
    public static final char TAB = '\t';

    /**
     * ASCII character convenience constant for the space character
     * (value is the <code>char</code> ' ').
     *
     * @since 3.7
     */
    public static final char SPACE = ' ';

    /**
     * Keyboard and/or mouse event mask indicating that the ALT_GR key
     * was pushed on the keyboard when the event was generated
     * (value is 1 &lt;&lt; 15).
     *
     * @since 3.108
     */
    public static final int ALT_GR = 1 << 15;

    /**
     * keyboard and/or mouse event mask indicating that the ALT key
     * was pushed on the keyboard when the event was generated
     * (value is 1&lt;&lt;16).
     */
    public static final int ALT = 1 << 16;

    /**
     * Keyboard and/or mouse event mask indicating that the SHIFT key
     * was pushed on the keyboard when the event was generated
     * (value is 1&lt;&lt;17).
     */
    public static final int SHIFT = 1 << 17;

    /**
     * Keyboard and/or mouse event mask indicating that the CTRL key
     * was pushed on the keyboard when the event was generated
     * (value is 1&lt;&lt;18).
     */
    public static final int CTRL = 1 << 18;

    /**
     * Keyboard and/or mouse event mask indicating that the CTRL key
     * was pushed on the keyboard when the event was generated. This
     * is a synonym for CTRL (value is 1&lt;&lt;18).
     */
    public static final int CONTROL = CTRL;

    /**
     * Keyboard and/or mouse event mask indicating that the COMMAND key
     * was pushed on the keyboard when the event was generated
     * (value is 1&lt;&lt;22).
     *
     * @since 2.1
     */
    public static final int COMMAND = 1 << 22;

    /**
     * Keyboard and/or mouse event mask indicating all possible
     * keyboard modifiers.
     *
     * To allow for the future, this mask  is intended to be used in
     * place of code that references  each individual keyboard mask.
     *  For example, the following expression will determine whether
     * any modifier is pressed and will continue to work as new modifier
     * masks are added.
     *
     * <code>(stateMask &amp; SWT.MODIFIER_MASK) != 0</code>.
     *
     * @since 2.1
     */
    public static final int MODIFIER_MASK;

    /**
     * Keyboard and/or mouse event mask indicating that mouse button one (usually 'left')
     * was pushed when the event was generated. (value is 1&lt;&lt;19).
     */
    public static final int BUTTON1 = 1 << 19;

    /**
     * Keyboard and/or mouse event mask indicating that mouse button two (usually 'middle')
     * was pushed when the event was generated. (value is 1&lt;&lt;20).
     */
    public static final int BUTTON2 = 1 << 20;

    /**
     * Keyboard and/or mouse event mask indicating that mouse button three (usually 'right')
     * was pushed when the event was generated. (value is 1&lt;&lt;21).
     */
    public static final int BUTTON3 = 1 << 21;

    /**
     * Keyboard and/or mouse event mask indicating that mouse button four
     * was pushed when the event was generated. (value is 1&lt;&lt;23).
     *
     * @since 3.1
     */
    public static final int BUTTON4 = 1 << 23;

    /**
     * Keyboard and/or mouse event mask indicating that mouse button five
     * was pushed when the event was generated. (value is 1&lt;&lt;25).
     *
     * @since 3.1
     */
    public static final int BUTTON5 = 1 << 25;

    /**
     * Keyboard and/or mouse event mask indicating all possible
     * mouse buttons.
     *
     * To allow for the future, this mask  is intended to be used
     * in place of code that references each individual button mask.
     * For example, the following expression will determine whether
     * any button is pressed and will continue to work as new button
     * masks are added.
     *
     * <code>(stateMask &amp; SWT.BUTTON_MASK) != 0</code>.
     *
     * @since 2.1
     */
    public static final int BUTTON_MASK;

    /**
     * Keyboard and/or mouse event mask indicating that the MOD1 key
     * was pushed on the keyboard when the event was generated.
     *
     * This is the primary keyboard modifier for the platform.
     * <p>
     * {@link #CTRL} on most platforms ({@link #COMMAND} on the Mac).
     * </p>
     *
     * @since 2.1
     */
    public static final int MOD1;

    /**
     * Keyboard and/or mouse event mask indicating that the MOD2 key
     * was pushed on the keyboard when the event was generated.
     *
     * This is the secondary keyboard modifier for the platform.
     * <p>
     * {@link #SHIFT} on most platforms.
     * </p>
     *
     * @since 2.1
     */
    public static final int MOD2;

    /**
     * Keyboard and/or mouse event mask indicating that the MOD3 key
     * was pushed on the keyboard when the event was generated.
     * <p>
     * {@link #ALT} on most platforms.
     * </p>
     *
     * @since 2.1
     */
    public static final int MOD3;

    /**
     * Keyboard and/or mouse event mask indicating that the MOD4 key
     * was pushed on the keyboard when the event was generated.
     * <p>
     * Undefined on most platforms ({@link #CTRL} on the Mac).
     * </p>
     *
     * @since 2.1
     */
    public static final int MOD4;

    /**
     * Constants to indicate line scrolling (value is 1).
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>Control</code></li>
     * </ul>
     *
     * @since 3.1
     */
    public static final int SCROLL_LINE = 1;

    /**
     * Constants to indicate page scrolling (value is 2).
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>Control</code></li>
     * </ul>
     *
     * @since 3.1
     */
    public static final int SCROLL_PAGE = 2;

    /**
     * Accelerator constant used to differentiate a key code from a
     * unicode character.
     *
     * If this bit is set, then the key stroke
     * portion of an accelerator represents a key code.  If this bit
     * is not set, then the key stroke portion of an accelerator is
     * a unicode character.
     *
     * The following expression is false:
     *
     * <code>((SWT.MOD1 | SWT.MOD2 | 'T') &amp; SWT.KEYCODE_BIT) != 0</code>.
     *
     * The following expression is true:
     *
     * <code>((SWT.MOD3 | SWT.F2) &amp; SWT.KEYCODE_BIT) != 0</code>.
     *
     * (value is (1&lt;&lt;24))
     *
     * @since 2.1
     */
    public static final int KEYCODE_BIT = (1 << 24);

    /**
     * Accelerator constant used to extract the key stroke portion of
     * an accelerator.
     *
     * The key stroke may be a key code or a unicode
     * value.  If the key stroke is a key code <code>KEYCODE_BIT</code>
     * will be set.
     *
     * @since 2.1
     */
    public static final int KEY_MASK = KEYCODE_BIT + 0xFFFF;

    /**
     * Keyboard event constant representing the UP ARROW key
     * (value is (1&lt;&lt;24)+1).
     */
    public static final int ARROW_UP = KEYCODE_BIT + 1;

    /**
     * Keyboard event constant representing the DOWN ARROW key
     * (value is (1&lt;&lt;24)+2).
     */
    public static final int ARROW_DOWN = KEYCODE_BIT + 2;

    /**
     * Keyboard event constant representing the LEFT ARROW key
     * (value is (1&lt;&lt;24)+3).
     */
    public static final int ARROW_LEFT = KEYCODE_BIT + 3;

    /**
     * Keyboard event constant representing the RIGHT ARROW key
     * (value is (1&lt;&lt;24)+4).
     */
    public static final int ARROW_RIGHT = KEYCODE_BIT + 4;

    /**
     * Keyboard event constant representing the PAGE UP key
     * (value is (1&lt;&lt;24)+5).
     */
    public static final int PAGE_UP = KEYCODE_BIT + 5;

    /**
     * Keyboard event constant representing the PAGE DOWN key
     * (value is (1&lt;&lt;24)+6).
     */
    public static final int PAGE_DOWN = KEYCODE_BIT + 6;

    /**
     * Keyboard event constant representing the HOME key
     * (value is (1&lt;&lt;24)+7).
     */
    public static final int HOME = KEYCODE_BIT + 7;

    /**
     * Keyboard event constant representing the END key
     * (value is (1&lt;&lt;24)+8).
     * <p>
     * Additional this constant is used by GridLayout for alignments.
     * </p>
     */
    public static final int END = KEYCODE_BIT + 8;

    /**
     * Keyboard event constant representing the INSERT key
     * (value is (1&lt;&lt;24)+9).
     */
    public static final int INSERT = KEYCODE_BIT + 9;

    /**
     * Keyboard event constant representing the F1 key
     * (value is (1&lt;&lt;24)+10).
     */
    public static final int F1 = KEYCODE_BIT + 10;

    /**
     * Keyboard event constant representing the F2 key
     * (value is (1&lt;&lt;24)+11).
     */
    public static final int F2 = KEYCODE_BIT + 11;

    /**
     * Keyboard event constant representing the F3 key
     * (value is (1&lt;&lt;24)+12).
     */
    public static final int F3 = KEYCODE_BIT + 12;

    /**
     * Keyboard event constant representing the F4 key
     * (value is (1&lt;&lt;24)+13).
     */
    public static final int F4 = KEYCODE_BIT + 13;

    /**
     * Keyboard event constant representing the F5 key
     * (value is (1&lt;&lt;24)+14).
     */
    public static final int F5 = KEYCODE_BIT + 14;

    /**
     * Keyboard event constant representing the F6 key
     * (value is (1&lt;&lt;24)+15).
     */
    public static final int F6 = KEYCODE_BIT + 15;

    /**
     * Keyboard event constant representing the F7 key
     * (value is (1&lt;&lt;24)+16).
     */
    public static final int F7 = KEYCODE_BIT + 16;

    /**
     * Keyboard event constant representing the F8 key
     * (value is (1&lt;&lt;24)+17).
     */
    public static final int F8 = KEYCODE_BIT + 17;

    /**
     * Keyboard event constant representing the F9 key
     * (value is (1&lt;&lt;24)+18).
     */
    public static final int F9 = KEYCODE_BIT + 18;

    /**
     * Keyboard event constant representing the F10 key
     * (value is (1&lt;&lt;24)+19).
     */
    public static final int F10 = KEYCODE_BIT + 19;

    /**
     * Keyboard event constant representing the F11 key
     * (value is (1&lt;&lt;24)+20).
     */
    public static final int F11 = KEYCODE_BIT + 20;

    /**
     * Keyboard event constant representing the F12 key
     * (value is (1&lt;&lt;24)+21).
     */
    public static final int F12 = KEYCODE_BIT + 21;

    /**
     * Keyboard event constant representing the F13 key
     * (value is (1&lt;&lt;24)+22).
     *
     * @since 3.0
     */
    public static final int F13 = KEYCODE_BIT + 22;

    /**
     * Keyboard event constant representing the F14 key
     * (value is (1&lt;&lt;24)+23).
     *
     * @since 3.0
     */
    public static final int F14 = KEYCODE_BIT + 23;

    /**
     * Keyboard event constant representing the F15 key
     * (value is (1&lt;&lt;24)+24).
     *
     * @since 3.0
     */
    public static final int F15 = KEYCODE_BIT + 24;

    /**
     * Keyboard event constant representing the F16 key
     * (value is (1&lt;&lt;25)+25).
     *
     * @since 3.6
     */
    public static final int F16 = KEYCODE_BIT + 25;

    /**
     * Keyboard event constant representing the F17 key
     * (value is (1&lt;&lt;26)+26).
     *
     * @since 3.6
     */
    public static final int F17 = KEYCODE_BIT + 26;

    /**
     * Keyboard event constant representing the F18 key
     * (value is (1&lt;&lt;27)+27).
     *
     * @since 3.6
     */
    public static final int F18 = KEYCODE_BIT + 27;

    /**
     * Keyboard event constant representing the F19 key
     * (value is (1&lt;&lt;28)+28).
     *
     * @since 3.6
     */
    public static final int F19 = KEYCODE_BIT + 28;

    /**
     * Keyboard event constant representing the F20 key
     * (value is (1&lt;&lt;29)+29).
     *
     * @since 3.6
     */
    public static final int F20 = KEYCODE_BIT + 29;

    /**
     * Keyboard event constant representing the keypad location.
     * (value is 1&lt;&lt;1).
     *
     * @since 3.6
     */
    public static final int KEYPAD = 1 << 1;

    /**
     * Keyboard event constant representing the numeric key
     * pad multiply key (value is (1&lt;&lt;24)+42).
     *
     * @since 3.0
     */
    public static final int KEYPAD_MULTIPLY = KEYCODE_BIT + 42;

    /**
     * Keyboard event constant representing the numeric key
     * pad add key (value is (1&lt;&lt;24)+43).
     *
     * @since 3.0
     */
    public static final int KEYPAD_ADD = KEYCODE_BIT + 43;

    /**
     * Keyboard event constant representing the numeric key
     * pad subtract key (value is (1&lt;&lt;24)+45).
     *
     * @since 3.0
     */
    public static final int KEYPAD_SUBTRACT = KEYCODE_BIT + 45;

    /**
     * Keyboard event constant representing the numeric key
     * pad decimal key (value is (1&lt;&lt;24)+46).
     *
     * @since 3.0
     */
    public static final int KEYPAD_DECIMAL = KEYCODE_BIT + 46;

    /**
     * Keyboard event constant representing the numeric key
     * pad divide key (value is (1&lt;&lt;24)+47).
     *
     * @since 3.0
     */
    public static final int KEYPAD_DIVIDE = KEYCODE_BIT + 47;

    /**
     * Keyboard event constant representing the numeric key
     * pad zero key (value is (1&lt;&lt;24)+48).
     *
     * @since 3.0
     */
    public static final int KEYPAD_0 = KEYCODE_BIT + 48;

    /**
     * Keyboard event constant representing the numeric key
     * pad one key (value is (1&lt;&lt;24)+49).
     *
     * @since 3.0
     */
    public static final int KEYPAD_1 = KEYCODE_BIT + 49;

    /**
     * Keyboard event constant representing the numeric key
     * pad two key (value is (1&lt;&lt;24)+50).
     *
     * @since 3.0
     */
    public static final int KEYPAD_2 = KEYCODE_BIT + 50;

    /**
     * Keyboard event constant representing the numeric key
     * pad three key (value is (1&lt;&lt;24)+51).
     *
     * @since 3.0
     */
    public static final int KEYPAD_3 = KEYCODE_BIT + 51;

    /**
     * Keyboard event constant representing the numeric key
     * pad four key (value is (1&lt;&lt;24)+52).
     *
     * @since 3.0
     */
    public static final int KEYPAD_4 = KEYCODE_BIT + 52;

    /**
     * Keyboard event constant representing the numeric key
     * pad five key (value is (1&lt;&lt;24)+53).
     *
     * @since 3.0
     */
    public static final int KEYPAD_5 = KEYCODE_BIT + 53;

    /**
     * Keyboard event constant representing the numeric key
     * pad six key (value is (1&lt;&lt;24)+54).
     *
     * @since 3.0
     */
    public static final int KEYPAD_6 = KEYCODE_BIT + 54;

    /**
     * Keyboard event constant representing the numeric key
     * pad seven key (value is (1&lt;&lt;24)+55).
     *
     * @since 3.0
     */
    public static final int KEYPAD_7 = KEYCODE_BIT + 55;

    /**
     * Keyboard event constant representing the numeric key
     * pad eight key (value is (1&lt;&lt;24)+56).
     *
     * @since 3.0
     */
    public static final int KEYPAD_8 = KEYCODE_BIT + 56;

    /**
     * Keyboard event constant representing the numeric key
     * pad nine key (value is (1&lt;&lt;24)+57).
     *
     * @since 3.0
     */
    public static final int KEYPAD_9 = KEYCODE_BIT + 57;

    /**
     * Keyboard event constant representing the numeric key
     * pad equal key (value is (1&lt;&lt;24)+61).
     *
     * @since 3.0
     */
    public static final int KEYPAD_EQUAL = KEYCODE_BIT + 61;

    /**
     * Keyboard event constant representing the numeric key
     * pad enter key (value is (1&lt;&lt;24)+80).
     *
     * @since 3.0
     */
    public static final int KEYPAD_CR = KEYCODE_BIT + 80;

    /**
     * Keyboard event constant representing the help
     * key (value is (1&lt;&lt;24)+81).
     *
     * NOTE: The HELP key maps to the key labeled "help",
     * not "F1". If your keyboard does not have a HELP key,
     * you will never see this key press.  To listen for
     * help on a control, use SWT.Help.
     *
     * @since 3.0
     *
     * @see SWT#Help
     */
    public static final int HELP = KEYCODE_BIT + 81;

    /**
     * Keyboard event constant representing the caps
     * lock key (value is (1&lt;&lt;24)+82).
     *
     * @since 3.0
     */
    public static final int CAPS_LOCK = KEYCODE_BIT + 82;

    /**
     * Keyboard event constant representing the num
     * lock key (value is (1&lt;&lt;24)+83).
     *
     * @since 3.0
     */
    public static final int NUM_LOCK = KEYCODE_BIT + 83;

    /**
     * Keyboard event constant representing the scroll
     * lock key (value is (1&lt;&lt;24)+84).
     *
     * @since 3.0
     */
    public static final int SCROLL_LOCK = KEYCODE_BIT + 84;

    /**
     * Keyboard event constant representing the pause
     * key (value is (1&lt;&lt;24)+85).
     *
     * @since 3.0
     */
    public static final int PAUSE = KEYCODE_BIT + 85;

    /**
     * Keyboard event constant representing the break
     * key (value is (1&lt;&lt;24)+86).
     *
     * @since 3.0
     */
    public static final int BREAK = KEYCODE_BIT + 86;

    /**
     * Keyboard event constant representing the print screen
     * key (value is (1&lt;&lt;24)+87).
     *
     * @since 3.0
     */
    public static final int PRINT_SCREEN = KEYCODE_BIT + 87;

    /**
     * The <code>MessageBox</code> style constant for error icon
     * behavior (value is 1).
     */
    public static final int ICON_ERROR = 1;

    /**
     * The <code>MessageBox</code> style constant for information icon
     * behavior (value is 1&lt;&lt;1).
     */
    public static final int ICON_INFORMATION = 1 << 1;

    /**
     * The <code>MessageBox</code> style constant for question icon
     * behavior (value is 1&lt;&lt;2).
     */
    public static final int ICON_QUESTION = 1 << 2;

    /**
     * The <code>MessageBox</code> style constant for warning icon
     * behavior (value is 1&lt;&lt;3).
     */
    public static final int ICON_WARNING = 1 << 3;

    /**
     * The <code>MessageBox</code> style constant for "working" icon
     * behavior (value is 1&lt;&lt;4).
     */
    public static final int ICON_WORKING = 1 << 4;

    /**
     * The style constant for "search" icon. This style constant is
     * used with <code>Text</code> in combination with <code>SWT.SEARCH
     * </code> (value is 1&lt;&lt;9).
     *
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>Text</code></li>
     * </ul>
     *
     * @see #SEARCH
     * @see #ICON_CANCEL
     *
     * @since 3.5
     */
    public static final int ICON_SEARCH = 1 << 9;

    /**
     * The style constant for "cancel" icon. This style constant is
     * used with <code>Text</code> in combination with <code>SWT.SEARCH
     * </code> (value is 1&lt;&lt;8).
     *
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>Text</code></li>
     * </ul>
     *
     * @see #SEARCH
     * @see #ICON_SEARCH
     *
     * @since 3.5
     */
    public static final int ICON_CANCEL = 1 << 8;

    /**
     * The <code>MessageBox</code> style constant for an OK button;
     * valid combinations are OK, OK|CANCEL
     * (value is 1&lt;&lt;5).
     */
    public static final int OK = 1 << 5;

    /**
     * The <code>MessageBox</code> style constant for YES button;
     * valid combinations are YES|NO, YES|NO|CANCEL
     * (value is 1&lt;&lt;6).
     */
    public static final int YES = 1 << 6;

    /**
     * The <code>MessageBox</code> style constant for NO button;
     * valid combinations are YES|NO, YES|NO|CANCEL
     * (value is 1&lt;&lt;7).
     */
    public static final int NO = 1 << 7;

    /**
     * The <code>MessageBox</code> style constant for a CANCEL button;
     * valid combinations are OK|CANCEL, YES|NO|CANCEL, RETRY|CANCEL
     * (value is 1&lt;&lt;8).
     *
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>MessageBox</code></li>
     * </ul>
     */
    public static final int CANCEL = 1 << 8;

    /**
     * The <code>MessageBox</code> style constant for an ABORT button;
     * the only valid combination is ABORT|RETRY|IGNORE
     * (value is 1&lt;&lt;9).
     */
    public static final int ABORT = 1 << 9;

    /**
     * The <code>MessageBox</code> style constant for a RETRY button;
     *  valid combinations are ABORT|RETRY|IGNORE, RETRY|CANCEL
     * (value is 1&lt;&lt;10).
     */
    public static final int RETRY = 1 << 10;

    /**
     * The <code>MessageBox</code> style constant for an IGNORE button;
     * the only valid combination is ABORT|RETRY|IGNORE
     * (value is 1&lt;&lt;11).
     */
    public static final int IGNORE = 1 << 11;

    /**
     * The <code>FileDialog</code> style constant for open file dialog behavior
     * (value is 1&lt;&lt;12).
     */
    public static final int OPEN = 1 << 12;

    /**
     * The <code>FileDialog</code> style constant for save file dialog behavior
     * (value is 1&lt;&lt;13).
     */
    public static final int SAVE = 1 << 13;

    /**
     * The <code>Composite</code> constant to indicate that
     * an attribute (such as background) is not inherited
     * by the children (value is 0).
     *
     * @since 3.2
     */
    public static final int INHERIT_NONE = 0;

    /**
     * The <code>Composite</code> constant to indicate that
     * an attribute (such as background) is inherited by
     * children who choose this value as their "default"
     * (value is 1).  For example, a label child will
     * typically choose to inherit the background color
     * of a composite while a list or table will not.
     *
     * @since 3.2
     */
    public static final int INHERIT_DEFAULT = 1;

    /**
     * The <code>Composite</code> constant to indicate that
     * an attribute (such as background) is inherited by
     * all children.
     *
     * @since 3.2
     */
    public static final int INHERIT_FORCE = 2;

    /**
     * Default color white (value is 1).
     */
    public static final int COLOR_WHITE = 1;

    /**
     * Default color black (value is 2).
     */
    public static final int COLOR_BLACK = 2;

    /**
     * Default color red (value is 3).
     */
    public static final int COLOR_RED = 3;

    /**
     * Default color dark red (value is 4).
     */
    public static final int COLOR_DARK_RED = 4;

    /**
     * Default color green (value is 5).
     */
    public static final int COLOR_GREEN = 5;

    /**
     * Default color dark green (value is 6).
     */
    public static final int COLOR_DARK_GREEN = 6;

    /**
     * Default color yellow (value is 7).
     */
    public static final int COLOR_YELLOW = 7;

    /**
     * Default color dark yellow (value is 8).
     */
    public static final int COLOR_DARK_YELLOW = 8;

    /**
     * Default color blue (value is 9).
     */
    public static final int COLOR_BLUE = 9;

    /**
     * Default color dark blue (value is 10).
     */
    public static final int COLOR_DARK_BLUE = 10;

    /**
     * Default color magenta (value is 11).
     */
    public static final int COLOR_MAGENTA = 11;

    /**
     * Default color dark magenta (value is 12).
     */
    public static final int COLOR_DARK_MAGENTA = 12;

    /**
     * Default color cyan (value is 13).
     */
    public static final int COLOR_CYAN = 13;

    /**
     * Default color dark cyan (value is 14).
     */
    public static final int COLOR_DARK_CYAN = 14;

    /**
     * Default color gray (value is 15).
     */
    public static final int COLOR_GRAY = 15;

    /**
     * Default color dark gray (value is 16).
     */
    public static final int COLOR_DARK_GRAY = 16;

    /*
	 * System Colors
	 *
	 * Dealing with system colors is an area where there are
	 * many platform differences.  On some platforms, system
	 * colors can change dynamically while the program is
	 * running.  On other platforms, system colors can be
	 * changed for all instances of a particular widget.
	 * Therefore, the only truly portable method to obtain
	 * a widget color query is to query the color from an
	 * instance of the widget.
	 *
	 *	It is expected that the list of supported colors
	 * will grow over time.
	 */
    /**
     * System color used to paint dark shadow areas (value is 17).
     */
    public static final int COLOR_WIDGET_DARK_SHADOW = 17;

    /**
     * System color used to paint normal shadow areas (value is 18).
     */
    public static final int COLOR_WIDGET_NORMAL_SHADOW = 18;

    /**
     * System color used to paint light shadow areas (value is 19).
     */
    public static final int COLOR_WIDGET_LIGHT_SHADOW = 19;

    /**
     * System color used to paint highlight shadow areas (value is 20).
     */
    public static final int COLOR_WIDGET_HIGHLIGHT_SHADOW = 20;

    /**
     * System color used to paint foreground areas (value is 21).
     */
    public static final int COLOR_WIDGET_FOREGROUND = 21;

    /**
     * System color used to paint background areas (value is 22).
     */
    public static final int COLOR_WIDGET_BACKGROUND = 22;

    /**
     * System color used to paint border areas (value is 23).
     */
    public static final int COLOR_WIDGET_BORDER = 23;

    /**
     * System color used to paint list foreground areas (value is 24).
     */
    public static final int COLOR_LIST_FOREGROUND = 24;

    /**
     * System color used to paint list background areas (value is 25).
     */
    public static final int COLOR_LIST_BACKGROUND = 25;

    /**
     * System color used to paint list selection background areas (value is 26).
     */
    public static final int COLOR_LIST_SELECTION = 26;

    /**
     * System color used to paint list selected text (value is 27).
     */
    public static final int COLOR_LIST_SELECTION_TEXT = 27;

    /**
     * System color used to paint tooltip text (value is 28).
     */
    public static final int COLOR_INFO_FOREGROUND = 28;

    /**
     * System color used to paint tooltip background areas (value is 29).
     */
    public static final int COLOR_INFO_BACKGROUND = 29;

    /**
     * System color used to paint title text (value is 30).
     */
    public static final int COLOR_TITLE_FOREGROUND = 30;

    /**
     * System color used to paint title background areas (value is 31).
     */
    public static final int COLOR_TITLE_BACKGROUND = 31;

    /**
     * System color used to paint title background gradient (value is 32).
     */
    public static final int COLOR_TITLE_BACKGROUND_GRADIENT = 32;

    /**
     * System color used to paint inactive title text (value is 33).
     */
    public static final int COLOR_TITLE_INACTIVE_FOREGROUND = 33;

    /**
     * System color used to paint inactive title background areas (value is 34).
     */
    public static final int COLOR_TITLE_INACTIVE_BACKGROUND = 34;

    /**
     * System color used to paint inactive title background gradient (value is 35).
     */
    public static final int COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT = 35;

    /**
     * System color used to paint link text (value is 36).
     *
     * @since 3.102
     */
    public static final int COLOR_LINK_FOREGROUND = 36;

    /**
     * System color used to paint with alpha 0 (value is 37).
     * <p>
     * This pseudo-color can be used to set a transparent background on SWT
     * controls. <br>
     * Note that this is a <em>HINT</em> and may be overridden by the platform.
     * For example:
     * <ul>
     * <li>{@link org.eclipse.swt.widgets.Combo Combo},
     * {@link org.eclipse.swt.widgets.List List} and
     * {@link org.eclipse.swt.widgets.Tree Tree} support transparent background
     * on GTK3 and Windows only.</li>
     * <li>{@link org.eclipse.swt.widgets.Text Text} supports transparent
     * background on Windows only whereas {@link org.eclipse.swt.widgets.Table
     * Table} supports transparent background on GTK3 only.</li>
     * </ul>
     *
     * @since 3.104
     */
    public static final int COLOR_TRANSPARENT = 37;

    /**
     * System color used to paint disabled text background areas (value is 38).
     *
     * @since 3.112
     */
    public static final int COLOR_TEXT_DISABLED_BACKGROUND = 38;

    /**
     * System color used to paint disabled foreground areas (value is 39).
     *
     * @since 3.112
     */
    public static final int COLOR_WIDGET_DISABLED_FOREGROUND = 39;

    /**
     * Draw constant indicating whether the drawing operation
     * should fill the background (value is 1&lt;&lt;0).
     */
    public static final int DRAW_TRANSPARENT = 1 << 0;

    /**
     * Draw constant indicating whether the string drawing operation
     * should handle line-delimiters (value is 1&lt;&lt;1).
     */
    public static final int DRAW_DELIMITER = 1 << 1;

    /**
     * Draw constant indicating whether the string drawing operation
     * should expand TAB characters (value is 1&lt;&lt;2).
     */
    public static final int DRAW_TAB = 1 << 2;

    /**
     * Draw constant indicating whether the string drawing operation
     * should handle mnemonics (value is 1&lt;&lt;3).
     */
    public static final int DRAW_MNEMONIC = 1 << 3;

    /**
     * Selection constant indicating that a line delimiter should be
     * drawn (value is 1&lt;&lt;17).
     *
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>TextLayout</code></li>
     * </ul>
     *
     * @see #FULL_SELECTION
     * @see #LAST_LINE_SELECTION
     *
     * @since 3.3
     */
    public static final int DELIMITER_SELECTION = 1 << 17;

    /**
     * Selection constant indicating that the last line is selected
     * to the end and should be drawn using either a line delimiter
     * or full line selection (value is 1&lt;&lt;20).
     *
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>TextLayout</code></li>
     * </ul>
     *
     * @see #DELIMITER_SELECTION
     * @see #FULL_SELECTION
     *
     * @since 3.3
     */
    public static final int LAST_LINE_SELECTION = 1 << 20;

    /**
     * SWT error constant indicating that no error number was specified
     * (value is 1).
     */
    public static final int ERROR_UNSPECIFIED = 1;

    /**
     * SWT error constant indicating that no more handles for an
     * operating system resource are available
     * (value is 2).
     */
    public static final int ERROR_NO_HANDLES = 2;

    /**
     * SWT error constant indicating that no more callback resources are available
     * (value is 3).
     */
    public static final int ERROR_NO_MORE_CALLBACKS = 3;

    /**
     * SWT error constant indicating that a null argument was passed in
     * (value is 4).
     */
    public static final int ERROR_NULL_ARGUMENT = 4;

    /**
     * SWT error constant indicating that an invalid argument was passed in
     * (value is 5).
     */
    public static final int ERROR_INVALID_ARGUMENT = 5;

    /**
     * SWT error constant indicating that a value was found to be
     * outside the allowable range
     * (value is 6).
     */
    public static final int ERROR_INVALID_RANGE = 6;

    /**
     * SWT error constant indicating that a value which can not be
     * zero was found to be
     * (value is 7).
     */
    public static final int ERROR_CANNOT_BE_ZERO = 7;

    /**
     * SWT error constant indicating that the underlying operating
     * system was unable to provide the value of an item
     * (value is 8).
     */
    public static final int ERROR_CANNOT_GET_ITEM = 8;

    /**
     * SWT error constant indicating that the underlying operating
     * system was unable to provide the selection
     * (value is 9).
     */
    public static final int ERROR_CANNOT_GET_SELECTION = 9;

    /**
     * SWT error constant indicating that the matrix is not invertible
     * (value is 10).
     *
     * @since 3.1
     */
    public static final int ERROR_CANNOT_INVERT_MATRIX = 10;

    /**
     * SWT error constant indicating that the underlying operating
     * system was unable to provide the height of an item
     * (value is 11).
     */
    public static final int ERROR_CANNOT_GET_ITEM_HEIGHT = 11;

    /**
     * SWT error constant indicating that the underlying operating
     * system was unable to provide the text of a widget
     * (value is 12).
     */
    public static final int ERROR_CANNOT_GET_TEXT = 12;

    /**
     * SWT error constant indicating that the underlying operating
     * system was unable to set the text of a widget
     * (value is 13).
     */
    public static final int ERROR_CANNOT_SET_TEXT = 13;

    /**
     * SWT error constant indicating that the underlying operating
     * system was unable to add an item
     * (value is 14).
     */
    public static final int ERROR_ITEM_NOT_ADDED = 14;

    /**
     * SWT error constant indicating that the underlying operating
     * system was unable to remove an item
     * (value is 15).
     */
    public static final int ERROR_ITEM_NOT_REMOVED = 15;

    /**
     * SWT error constant indicating that the graphics library
     * is not available
     * (value is 16).
     */
    public static final int ERROR_NO_GRAPHICS_LIBRARY = 16;

    /**
     * SWT error constant indicating that a particular feature has
     * not been implemented on this platform
     * (value is 20).
     */
    public static final int ERROR_NOT_IMPLEMENTED = 20;

    /**
     * SWT error constant indicating that a menu which needed
     * to have the drop down style had some other style instead
     * (value is 21).
     */
    public static final int ERROR_MENU_NOT_DROP_DOWN = 21;

    /**
     * SWT error constant indicating that an attempt was made to
     * invoke an SWT operation which can only be executed by the
     * user-interface thread from some other thread
     * (value is 22).
     */
    public static final int ERROR_THREAD_INVALID_ACCESS = 22;

    /**
     * SWT error constant indicating that an attempt was made to
     * invoke an SWT operation using a widget which had already
     * been disposed
     * (value is 24).
     */
    public static final int ERROR_WIDGET_DISPOSED = 24;

    /**
     * SWT error constant indicating that a menu item which needed
     * to have the cascade style had some other style instead
     * (value is 27).
     */
    public static final int ERROR_MENUITEM_NOT_CASCADE = 27;

    /**
     * SWT error constant indicating that the underlying operating
     * system was unable to set the selection of a widget
     * (value is 28).
     */
    public static final int ERROR_CANNOT_SET_SELECTION = 28;

    /**
     * SWT error constant indicating that the underlying operating
     * system was unable to set the menu
     * (value is 29).
     */
    public static final int ERROR_CANNOT_SET_MENU = 29;

    /**
     * SWT error constant indicating that the underlying operating
     * system was unable to set the enabled state
     * (value is 30).
     */
    public static final int ERROR_CANNOT_SET_ENABLED = 30;

    /**
     * SWT error constant indicating that the underlying operating
     * system was unable to provide enabled/disabled state information
     * (value is 31).
     */
    public static final int ERROR_CANNOT_GET_ENABLED = 31;

    /**
     * SWT error constant indicating that a provided widget can
     * not be used as a parent in the current operation
     * (value is 32).
     */
    public static final int ERROR_INVALID_PARENT = 32;

    /**
     * SWT error constant indicating that a menu which needed
     * to have the menu bar style had some other style instead
     * (value is 33).
     */
    public static final int ERROR_MENU_NOT_BAR = 33;

    /**
     * SWT error constant indicating that the underlying operating
     * system was unable to provide count information
     * (value is 36).
     */
    public static final int ERROR_CANNOT_GET_COUNT = 36;

    /**
     * SWT error constant indicating that a menu which needed
     * to have the pop up menu style had some other style instead
     * (value is 37).
     */
    public static final int ERROR_MENU_NOT_POP_UP = 37;

    /**
     * SWT error constant indicating that a graphics operation
     * was attempted with an image of an unsupported depth
     * (value is 38).
     */
    public static final int ERROR_UNSUPPORTED_DEPTH = 38;

    /**
     * SWT error constant indicating that an input/output operation
     * failed during the execution of an SWT operation
     * (value is 39).
     */
    public static final int ERROR_IO = 39;

    /**
     * SWT error constant indicating that a graphics operation
     * was attempted with an image having an invalid format
     * (value is 40).
     */
    public static final int ERROR_INVALID_IMAGE = 40;

    /**
     * SWT error constant indicating that a graphics operation
     * was attempted with an image having a valid but unsupported
     * format
     * (value is 42).
     */
    public static final int ERROR_UNSUPPORTED_FORMAT = 42;

    /**
     * SWT error constant indicating that an attempt was made
     * to subclass an SWT widget class without implementing the
     * <code>checkSubclass()</code> method
     * (value is 43).
     *
     * For additional information see the comment in
     * <code>Widget.checkSubclass()</code>.
     *
     * @see org.eclipse.swt.widgets.Widget#checkSubclass
     */
    public static final int ERROR_INVALID_SUBCLASS = 43;

    /**
     * SWT error constant indicating that an attempt was made to
     * invoke an SWT operation using a graphics object which had
     * already been disposed
     * (value is 44).
     */
    public static final int ERROR_GRAPHIC_DISPOSED = 44;

    /**
     * SWT error constant indicating that an attempt was made to
     * invoke an SWT operation using a device which had already
     * been disposed
     * (value is 45).
     */
    public static final int ERROR_DEVICE_DISPOSED = 45;

    /**
     * SWT error constant indicating that an exception happened
     * when executing a runnable
     * (value is 46).
     */
    public static final int ERROR_FAILED_EXEC = 46;

    /**
     * SWT error constant indicating that an unsatisfied link
     * error occurred while attempting to load a library
     * (value is 47).
     *
     * @since 3.1
     */
    public static final int ERROR_FAILED_LOAD_LIBRARY = 47;

    /**
     * SWT error constant indicating that a font is not valid
     * (value is 48).
     *
     * @since 3.1
     */
    public static final int ERROR_INVALID_FONT = 48;

    /**
     * SWT error constant indicating that an attempt was made to
     * use an BrowserFunction object which had already been disposed
     * (value is 49).
     *
     * @since 3.5
     */
    public static final int ERROR_FUNCTION_DISPOSED = 49;

    /**
     * SWT error constant indicating that an exception happened
     * when evaluating a javascript expression
     * (value is 50).
     *
     * @since 3.5
     */
    public static final int ERROR_FAILED_EVALUATE = 50;

    /**
     * SWT error constant indicating that an invalid value was returned
     * (value is 51).
     *
     * @since 3.5
     */
    public static final int ERROR_INVALID_RETURN_VALUE = 51;

    /**
     * Constant indicating that an image or operation is of type bitmap  (value is 0).
     */
    public static final int BITMAP = 0;

    /**
     * Constant indicating that an image or operation is of type icon  (value is 1).
     */
    public static final int ICON = 1;

    /**
     * The <code>Image</code> constructor argument indicating that
     * the new image should be a copy of the image provided as
     * an argument  (value is 0).
     */
    public static final int IMAGE_COPY = 0;

    /**
     * The <code>Image</code> constructor argument indicating that
     * the new image should have the appearance of a "disabled"
     * (using the platform's rules for how this should look)
     * copy of the image provided as an argument  (value is 1).
     */
    public static final int IMAGE_DISABLE = 1;

    /**
     * The <code>Image</code> constructor argument indicating that
     * the new image should have the appearance of a "gray scaled"
     * copy of the image provided as an argument  (value is 2).
     */
    public static final int IMAGE_GRAY = 2;

    /**
     * Constant to indicate an error state (value is 1).
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>ProgressBar</code></li>
     * </ul>
     *
     * @since 3.4
     */
    public static final int ERROR = 1;

    /**
     * Constant to a indicate a paused state (value is 4).
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>ProgressBar</code></li>
     * </ul>
     *
     * @since 3.4
     */
    public static final int PAUSED = 1 << 2;

    /**
     * The font style constant indicating a normal weight, non-italic font
     * (value is 0). This constant is also used with <code>ProgressBar</code>
     * to indicate a normal state.
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>ProgressBar</code></li>
     * </ul>
     */
    public static final int NORMAL = 0;

    /**
     * The font style constant indicating a bold weight font
     * (value is 1&lt;&lt;0).
     */
    public static final int BOLD = 1 << 0;

    /**
     * The font style constant indicating an italic font
     * (value is 1&lt;&lt;1).
     */
    public static final int ITALIC = 1 << 1;

    /**
     * System arrow cursor  (value is 0).
     */
    public static final int CURSOR_ARROW = 0;

    /**
     * System wait cursor  (value is 1).
     */
    public static final int CURSOR_WAIT = 1;

    /**
     * System cross hair cursor  (value is 2).
     */
    public static final int CURSOR_CROSS = 2;

    /**
     * System app startup cursor  (value is 3).
     */
    public static final int CURSOR_APPSTARTING = 3;

    /**
     * System help cursor  (value is 4).
     */
    public static final int CURSOR_HELP = 4;

    /**
     * System resize all directions cursor (value is 5).
     */
    public static final int CURSOR_SIZEALL = 5;

    /**
     * System resize north-east-south-west cursor  (value is 6).
     */
    public static final int CURSOR_SIZENESW = 6;

    /**
     * System resize north-south cursor  (value is 7).
     */
    public static final int CURSOR_SIZENS = 7;

    /**
     * System resize north-west-south-east cursor  (value is 8).
     */
    public static final int CURSOR_SIZENWSE = 8;

    /**
     * System resize west-east cursor  (value is 9).
     */
    public static final int CURSOR_SIZEWE = 9;

    /**
     * System resize north cursor  (value is 10).
     */
    public static final int CURSOR_SIZEN = 10;

    /**
     * System resize south cursor  (value is 11).
     */
    public static final int CURSOR_SIZES = 11;

    /**
     * System resize east cursor  (value is 12).
     */
    public static final int CURSOR_SIZEE = 12;

    /**
     * System resize west cursor  (value is 13).
     */
    public static final int CURSOR_SIZEW = 13;

    /**
     * System resize north-east cursor (value is 14).
     */
    public static final int CURSOR_SIZENE = 14;

    /**
     * System resize south-east cursor (value is 15).
     */
    public static final int CURSOR_SIZESE = 15;

    /**
     * System resize south-west cursor (value is 16).
     */
    public static final int CURSOR_SIZESW = 16;

    /**
     * System resize north-west cursor (value is 17).
     */
    public static final int CURSOR_SIZENW = 17;

    /**
     * System up arrow cursor  (value is 18).
     */
    public static final int CURSOR_UPARROW = 18;

    /**
     * System i-beam cursor (value is 19).
     */
    public static final int CURSOR_IBEAM = 19;

    /**
     * System "not allowed" cursor (value is 20).
     */
    public static final int CURSOR_NO = 20;

    /**
     * System hand cursor (value is 21).
     */
    public static final int CURSOR_HAND = 21;

    /**
     * Line drawing style for flat end caps (value is 1).
     *
     * @see org.eclipse.swt.graphics.GC#setLineCap(int)
     * @see org.eclipse.swt.graphics.GC#getLineCap()
     *
     * @since 3.1
     */
    public static final int CAP_FLAT = 1;

    /**
     * Line drawing style for rounded end caps (value is 2).
     *
     * @see org.eclipse.swt.graphics.GC#setLineCap(int)
     * @see org.eclipse.swt.graphics.GC#getLineCap()
     *
     * @since 3.1
     */
    public static final int CAP_ROUND = 2;

    /**
     * Line drawing style for square end caps (value is 3).
     *
     * @see org.eclipse.swt.graphics.GC#setLineCap(int)
     * @see org.eclipse.swt.graphics.GC#getLineCap()
     *
     * @since 3.1
     */
    public static final int CAP_SQUARE = 3;

    /**
     * Line drawing style for miter joins (value is 1).
     *
     * @see org.eclipse.swt.graphics.GC#setLineJoin(int)
     * @see org.eclipse.swt.graphics.GC#getLineJoin()
     *
     * @since 3.1
     */
    public static final int JOIN_MITER = 1;

    /**
     * Line drawing  style for rounded joins (value is 2).
     *
     * @see org.eclipse.swt.graphics.GC#setLineJoin(int)
     * @see org.eclipse.swt.graphics.GC#getLineJoin()
     *
     * @since 3.1
     */
    public static final int JOIN_ROUND = 2;

    /**
     * Line drawing style for bevel joins (value is 3).
     *
     * @see org.eclipse.swt.graphics.GC#setLineJoin(int)
     * @see org.eclipse.swt.graphics.GC#getLineJoin()
     *
     * @since 3.1
     */
    public static final int JOIN_BEVEL = 3;

    /**
     * Line drawing style for solid lines  (value is 1).
     */
    public static final int LINE_SOLID = 1;

    /**
     * Line drawing style for dashed lines (value is 2).
     */
    public static final int LINE_DASH = 2;

    /**
     * Line drawing style for dotted lines (value is 3).
     */
    public static final int LINE_DOT = 3;

    /**
     * Line drawing style for alternating dash-dot lines (value is 4).
     */
    public static final int LINE_DASHDOT = 4;

    /**
     * Line drawing style for dash-dot-dot lines (value is 5).
     */
    public static final int LINE_DASHDOTDOT = 5;

    /**
     * Line drawing style for custom dashed lines (value is 6).
     *
     * @see org.eclipse.swt.graphics.GC#setLineDash(int[])
     * @see org.eclipse.swt.graphics.GC#getLineDash()
     *
     * @since 3.1
     */
    public static final int LINE_CUSTOM = 6;

    /**
     * Path constant that represents a "move to" operation (value is 1).
     *
     * @since 3.1
     */
    public static final int PATH_MOVE_TO = 1;

    /**
     * Path constant that represents a "line to" operation (value is 2).
     *
     * @since 3.1
     */
    public static final int PATH_LINE_TO = 2;

    /**
     * Path constant that represents a "quadratic curve to" operation (value is 3).
     *
     * @since 3.1
     */
    public static final int PATH_QUAD_TO = 3;

    /**
     * Path constant that represents a "cubic curve to" operation (value is 4).
     *
     * @since 3.1
     */
    public static final int PATH_CUBIC_TO = 4;

    /**
     * Path constant that represents a "close" operation (value is 5).
     *
     * @since 3.1
     */
    public static final int PATH_CLOSE = 5;

    /**
     * Even odd rule for filling operations (value is 1).
     *
     * @since 3.1
     */
    public static final int FILL_EVEN_ODD = 1;

    /**
     * Winding rule for filling operations (value is 2).
     *
     * @since 3.1
     */
    public static final int FILL_WINDING = 2;

    /**
     * Image format constant indicating an unknown image type (value is -1).
     */
    public static final int IMAGE_UNDEFINED = -1;

    /**
     * Image format constant indicating a Windows BMP format image (value is 0).
     */
    public static final int IMAGE_BMP = 0;

    /**
     * Image format constant indicating a run-length encoded
     * Windows BMP format image (value is 1).
     */
    public static final int IMAGE_BMP_RLE = 1;

    /**
     * Image format constant indicating a GIF format image (value is 2).
     */
    public static final int IMAGE_GIF = 2;

    /**
     * Image format constant indicating a ICO format image (value is 3).
     */
    public static final int IMAGE_ICO = 3;

    /**
     * Image format constant indicating a JPEG format image (value is 4).
     */
    public static final int IMAGE_JPEG = 4;

    /**
     * Image format constant indicating a PNG format image (value is 5).
     */
    public static final int IMAGE_PNG = 5;

    /**
     * Image format constant indicating a TIFF format image (value is 6).
     */
    public static final int IMAGE_TIFF = 6;

    /**
     * Image format constant indicating an OS/2 BMP format image (value is 7).
     */
    public static final int IMAGE_OS2_BMP = 7;

    /**
     * Image format constant indicating a SVG format image (value is 8).
     * <br>Note that this is a <em>HINT</em> and is currently only supported on GTK.
     *
     * @since 3.113
     */
    public static final int IMAGE_SVG = 8;

    /**
     * GIF image disposal method constants indicating that the
     * disposal method is unspecified (value is 0).
     */
    public static final int DM_UNSPECIFIED = 0x0;

    /**
     * GIF image disposal method constants indicating that the
     * disposal method is to do nothing; that is, to leave the
     * previous image in place (value is 1).
     */
    public static final int DM_FILL_NONE = 0x1;

    /**
     * GIF image disposal method constants indicating that the
     * the previous images should be covered with the background
     * color before displaying the next image (value is 2).
     */
    public static final int DM_FILL_BACKGROUND = 0x2;

    /**
     * GIF image disposal method constants indicating that the
     * disposal method is to restore the previous picture
     * (value is 3).
     */
    public static final int DM_FILL_PREVIOUS = 0x3;

    /**
     * Image transparency constant indicating that the image
     * contains no transparency information (value is 0).
     */
    public static final int TRANSPARENCY_NONE = 0x0;

    /**
     * Image transparency constant indicating that the image
     * contains alpha transparency information (value is 1&lt;&lt;0).
     */
    public static final int TRANSPARENCY_ALPHA = 1 << 0;

    /**
     * Image transparency constant indicating that the image
     * contains a transparency mask (value is 1&lt;&lt;1).
     */
    public static final int TRANSPARENCY_MASK = 1 << 1;

    /**
     * Image transparency constant indicating that the image
     * contains a transparent pixel (value is 1&lt;&lt;2).
     */
    public static final int TRANSPARENCY_PIXEL = 1 << 2;

    /**
     * The character movement type (value is 1&lt;&lt;0).
     * This constant is used to move a text offset over a character.
     *
     * @see org.eclipse.swt.graphics.TextLayout#getNextOffset(int, int)
     * @see org.eclipse.swt.graphics.TextLayout#getPreviousOffset(int, int)
     *
     * @since 3.0
     */
    public static final int MOVEMENT_CHAR = 1 << 0;

    /**
     * The cluster movement type (value is 1&lt;&lt;1).
     * This constant is used to move a text offset over a cluster.
     * A cluster groups one or more characters. A cluster is
     * undivisible, this means that a caret offset can not be placed in the
     * middle of a cluster.
     *
     * @see org.eclipse.swt.graphics.TextLayout#getNextOffset(int, int)
     * @see org.eclipse.swt.graphics.TextLayout#getPreviousOffset(int, int)
     *
     * @since 3.0
     */
    public static final int MOVEMENT_CLUSTER = 1 << 1;

    /**
     * The word movement type (value is 1&lt;&lt;2).
     * This constant is used to move a text offset over a word.
     * The behavior of this constant depends on the platform and on the
     * direction of the movement. For example, on Windows the stop is
     * always at the start of the word. On GTK and Mac the stop is at the end
     * of the word if the direction is next and at the start of the word if the
     * direction is previous.
     *
     * @see org.eclipse.swt.graphics.TextLayout#getNextOffset(int, int)
     * @see org.eclipse.swt.graphics.TextLayout#getPreviousOffset(int, int)
     *
     * @since 3.0
     */
    public static final int MOVEMENT_WORD = 1 << 2;

    /**
     * The word end movement type (value is 1&lt;&lt;3).
     * This constant is used to move a text offset to the next or previous
     * word end. The behavior of this constant does not depend on the platform.
     *
     * @see org.eclipse.swt.graphics.TextLayout#getNextOffset(int, int)
     * @see org.eclipse.swt.graphics.TextLayout#getPreviousOffset(int, int)
     *
     * @since 3.3
     */
    public static final int MOVEMENT_WORD_END = 1 << 3;

    /**
     * The word start movement type (value is 1&lt;&lt;4).
     * This constant is used to move a text offset to the next or previous
     * word start. The behavior of this constant does not depend on the platform.
     *
     * @see org.eclipse.swt.graphics.TextLayout#getNextOffset(int, int)
     * @see org.eclipse.swt.graphics.TextLayout#getPreviousOffset(int, int)
     *
     * @since 3.3
     */
    public static final int MOVEMENT_WORD_START = 1 << 4;

    /**
     * A constant indicating that a given operation should be performed on
     * all widgets (value is 1&lt;&lt;0).
     *
     * <p><b>Used By:</b></p>
     * <ul>
     * <li><code>Composite</code> layout</li>
     * </ul>
     *
     * @see org.eclipse.swt.widgets.Composite#layout(org.eclipse.swt.widgets.Control[], int)
     *
     * @since 3.6
     */
    public static final int ALL = 1 << 0;

    /**
     * ID for the About menu item (value is -1).
     *
     * @see org.eclipse.swt.widgets.MenuItem#setID(int)
     * @see org.eclipse.swt.widgets.MenuItem#getID()
     *
     * @since 3.7
     */
    public static final int ID_ABOUT = -1;

    /**
     * ID for the Preferences menu item (value is -2).
     *
     * @see org.eclipse.swt.widgets.MenuItem#setID(int)
     * @see org.eclipse.swt.widgets.MenuItem#getID()
     *
     * @since 3.7
     */
    public static final int ID_PREFERENCES = -2;

    /**
     * ID for the Hide menu item (value is -3).
     *
     * @see org.eclipse.swt.widgets.MenuItem#setID(int)
     * @see org.eclipse.swt.widgets.MenuItem#getID()
     *
     * @since 3.7
     */
    public static final int ID_HIDE = -3;

    /**
     * ID for the Hide Others menu item (value is -4).
     *
     * @see org.eclipse.swt.widgets.MenuItem#setID(int)
     * @see org.eclipse.swt.widgets.MenuItem#getID()
     *
     * @since 3.7
     */
    public static final int ID_HIDE_OTHERS = -4;

    /**
     * ID for the Show All menu item (value is -5).
     *
     * @see org.eclipse.swt.widgets.MenuItem#setID(int)
     * @see org.eclipse.swt.widgets.MenuItem#getID()
     *
     * @since 3.7
     */
    public static final int ID_SHOW_ALL = -5;

    /**
     * ID for the Quit menu item (value is -6).
     *
     * @see org.eclipse.swt.widgets.MenuItem#setID(int)
     * @see org.eclipse.swt.widgets.MenuItem#getID()
     *
     * @since 3.7
     */
    public static final int ID_QUIT = -6;

    /**
     * Key name for setting and getting the skin class of a widget.
     * <p>
     * Note: SWT currently doesn't read or process this property. The only
     * effect of setting this property is to trigger a call to
     * {@link Widget#reskin(int) Widget#reskin(SWT.ALL)}.
     * </p>
     *
     * @see org.eclipse.swt.widgets.Widget#getData(String)
     * @see org.eclipse.swt.widgets.Widget#setData(String, Object)
     *
     * @since 3.6
     */
    //$NON-NLS-1$
    public static final String SKIN_CLASS = "org.eclipse.swt.skin.class";

    /**
     * Key name for setting and getting the skin id of a widget.
     * <p>
     * Note: SWT currently doesn't read or process this property. The only
     * effect of setting this property is to trigger a call to
     * {@link Widget#reskin(int) Widget#reskin(SWT.ALL)}.
     * </p>
     *
     * @see org.eclipse.swt.widgets.Widget#getData(String)
     * @see org.eclipse.swt.widgets.Widget#setData(String, Object)
     *
     * @since 3.6
     */
    //$NON-NLS-1$
    public static final String SKIN_ID = "org.eclipse.swt.skin.id";

    /**
     * The <code>Scrollable</code> constant to indicate that
     * the receiver is using overlay scrollbars. (value is 1)
     *
     * @since 3.8
     */
    public static final int SCROLLBAR_OVERLAY = 1 << 1;

    /**
     * Returns a boolean indicating whether this SWT implementation can
     * be loaded.  Examples of criteria that may be used to determine this
     * include the OS and architecture of the JRE that is being used.
     *
     * @return <code>true</code> if this SWT implementation can be loaded
     * and <code>false</code> otherwise
     *
     * @since 3.8
     */
    public static boolean isLoadable() {
        return Platform.isLoadable();
    }

    /**
     * Answers a concise, human readable description of the error code.
     *
     * @param code the SWT error code.
     * @return a description of the error code.
     *
     * @see SWT
     */
    static String findErrorText(int code) {
        switch(code) {
            //$NON-NLS-1$
            case ERROR_UNSPECIFIED:
                return "Unspecified error";
            //$NON-NLS-1$
            case ERROR_NO_HANDLES:
                return "No more handles";
            //$NON-NLS-1$
            case ERROR_NO_MORE_CALLBACKS:
                return "No more callbacks";
            //$NON-NLS-1$
            case ERROR_NULL_ARGUMENT:
                return "Argument cannot be null";
            //$NON-NLS-1$
            case ERROR_INVALID_ARGUMENT:
                return "Argument not valid";
            //$NON-NLS-1$
            case ERROR_INVALID_RETURN_VALUE:
                return "Return value not valid";
            //$NON-NLS-1$
            case ERROR_INVALID_RANGE:
                return "Index out of bounds";
            //$NON-NLS-1$
            case ERROR_CANNOT_BE_ZERO:
                return "Argument cannot be zero";
            //$NON-NLS-1$
            case ERROR_CANNOT_GET_ITEM:
                return "Cannot get item";
            //$NON-NLS-1$
            case ERROR_CANNOT_GET_SELECTION:
                return "Cannot get selection";
            //$NON-NLS-1$
            case ERROR_CANNOT_GET_ITEM_HEIGHT:
                return "Cannot get item height";
            //$NON-NLS-1$
            case ERROR_CANNOT_GET_TEXT:
                return "Cannot get text";
            //$NON-NLS-1$
            case ERROR_CANNOT_SET_TEXT:
                return "Cannot set text";
            //$NON-NLS-1$
            case ERROR_ITEM_NOT_ADDED:
                return "Item not added";
            //$NON-NLS-1$
            case ERROR_ITEM_NOT_REMOVED:
                return "Item not removed";
            //$NON-NLS-1$
            case ERROR_NOT_IMPLEMENTED:
                return "Not implemented";
            //$NON-NLS-1$
            case ERROR_MENU_NOT_DROP_DOWN:
                return "Menu must be a drop down";
            //$NON-NLS-1$
            case ERROR_THREAD_INVALID_ACCESS:
                return "Invalid thread access";
            //$NON-NLS-1$
            case ERROR_WIDGET_DISPOSED:
                return "Widget is disposed";
            //$NON-NLS-1$
            case ERROR_MENUITEM_NOT_CASCADE:
                return "Menu item is not a CASCADE";
            //$NON-NLS-1$
            case ERROR_CANNOT_SET_SELECTION:
                return "Cannot set selection";
            //$NON-NLS-1$
            case ERROR_CANNOT_SET_MENU:
                return "Cannot set menu";
            //$NON-NLS-1$
            case ERROR_CANNOT_SET_ENABLED:
                return "Cannot set the enabled state";
            //$NON-NLS-1$
            case ERROR_CANNOT_GET_ENABLED:
                return "Cannot get the enabled state";
            //$NON-NLS-1$
            case ERROR_INVALID_PARENT:
                return "Widget has the wrong parent";
            //$NON-NLS-1$
            case ERROR_MENU_NOT_BAR:
                return "Menu is not a BAR";
            //$NON-NLS-1$
            case ERROR_CANNOT_GET_COUNT:
                return "Cannot get count";
            //$NON-NLS-1$
            case ERROR_MENU_NOT_POP_UP:
                return "Menu is not a POP_UP";
            //$NON-NLS-1$
            case ERROR_UNSUPPORTED_DEPTH:
                return "Unsupported color depth";
            //$NON-NLS-1$
            case ERROR_IO:
                return "i/o error";
            //$NON-NLS-1$
            case ERROR_INVALID_IMAGE:
                return "Invalid image";
            //$NON-NLS-1$
            case ERROR_UNSUPPORTED_FORMAT:
                return "Unsupported or unrecognized format";
            //$NON-NLS-1$
            case ERROR_INVALID_SUBCLASS:
                return "Subclassing not allowed";
            //$NON-NLS-1$
            case ERROR_GRAPHIC_DISPOSED:
                return "Graphic is disposed";
            //$NON-NLS-1$
            case ERROR_DEVICE_DISPOSED:
                return "Device is disposed";
            //$NON-NLS-1$
            case ERROR_FUNCTION_DISPOSED:
                return "BrowserFunction is disposed";
            //$NON-NLS-1$
            case ERROR_FAILED_EXEC:
                return "Failed to execute runnable";
            //$NON-NLS-1$
            case ERROR_FAILED_EVALUATE:
                return "Failed to evaluate javascript expression";
            //$NON-NLS-1$
            case ERROR_FAILED_LOAD_LIBRARY:
                return "Unable to load library";
            //$NON-NLS-1$
            case ERROR_CANNOT_INVERT_MATRIX:
                return "Cannot invert matrix";
            //$NON-NLS-1$
            case ERROR_NO_GRAPHICS_LIBRARY:
                return "Unable to load graphics library";
            //$NON-NLS-1$
            case ERROR_INVALID_FONT:
                return "Font not valid";
        }
        //$NON-NLS-1$
        return "Unknown error";
    }

    /**
     * Returns the NLS'ed message for the given argument.
     *
     * @param key the key to look up
     * @return the message for the given key
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the key is null</li>
     * </ul>
     */
    public static String getMessage(String key) {
        return Compatibility.getMessage(key);
    }

    /**
     * Returns the NLS'ed message for the given arguments.
     *
     * @param key the key to look up
     * @param args the parameters to insert into the message
     * @return the message for the given parameterized key
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the key or args are null</li>
     * </ul>
     *
     * @since 3.8
     */
    public static String getMessage(String key, Object[] args) {
        return Compatibility.getMessage(key, args);
    }

    /**
     * Returns the SWT platform name.
     * Examples: "win32", "gtk", "cocoa"
     *
     * @return the SWT platform name
     */
    public static String getPlatform() {
        return Platform.PLATFORM;
    }

    /**
     * Returns the SWT version number as an integer.
     * Example: "SWT051" == 51
     *
     * @return the SWT version number
     */
    public static int getVersion() {
        return Library.SWT_VERSION;
    }

    /**
     * Throws an appropriate exception based on the passed in error code.
     *
     * @param code the SWT error code
     */
    public static void error(int code) {
        error(code, null);
    }

    /**
     * Throws an appropriate exception based on the passed in error code.
     * The <code>throwable</code> argument should be either null, or the
     * throwable which caused SWT to throw an exception.
     * <p>
     * In SWT, errors are reported by throwing one of three exceptions:
     * </p>
     * <dl>
     * <dd>java.lang.IllegalArgumentException</dd>
     * <dt>thrown whenever one of the API methods is invoked with an illegal argument</dt>
     * <dd>org.eclipse.swt.SWTException (extends java.lang.RuntimeException)</dd>
     * <dt>thrown whenever a recoverable error happens internally in SWT</dt>
     * <dd>org.eclipse.swt.SWTError (extends java.lang.Error)</dd>
     * <dt>thrown whenever a <b>non-recoverable</b> error happens internally in SWT</dt>
     * </dl>
     * This method provides the logic which maps between error codes
     * and one of the above exceptions.
     *
     * @param code the SWT error code.
     * @param throwable the exception which caused the error to occur.
     *
     * @see SWTError
     * @see SWTException
     * @see IllegalArgumentException
     */
    public static void error(int code, Throwable throwable) {
        error(code, throwable, null);
    }

    /**
     * Throws an appropriate exception based on the passed in error code.
     * The <code>throwable</code> argument should be either null, or the
     * throwable which caused SWT to throw an exception.
     * <p>
     * In SWT, errors are reported by throwing one of three exceptions:
     * </p>
     * <dl>
     * <dd>java.lang.IllegalArgumentException</dd>
     * <dt>thrown whenever one of the API methods is invoked with an illegal argument</dt>
     * <dd>org.eclipse.swt.SWTException (extends java.lang.RuntimeException)</dd>
     * <dt>thrown whenever a recoverable error happens internally in SWT</dt>
     * <dd>org.eclipse.swt.SWTError (extends java.lang.Error)</dd>
     * <dt>thrown whenever a <b>non-recoverable</b> error happens internally in SWT</dt>
     * </dl>
     * This method provides the logic which maps between error codes
     * and one of the above exceptions.
     *
     * @param code the SWT error code.
     * @param throwable the exception which caused the error to occur.
     * @param detail more information about error.
     *
     * @see SWTError
     * @see SWTException
     * @see IllegalArgumentException
     *
     * @since 3.0
     */
    public static void error(int code, Throwable throwable, String detail) {
        /*
	* This code prevents the creation of "chains" of SWTErrors and
	* SWTExceptions which in turn contain other SWTErrors and
	* SWTExceptions as their throwable. This can occur when low level
	* code throws an exception past a point where a higher layer is
	* being "safe" and catching all exceptions. (Note that, this is
	* _a_bad_thing_ which we always try to avoid.)
	*
	* On the theory that the low level code is closest to the
	* original problem, we simply re-throw the original exception here.
	*
	* NOTE: Exceptions thrown in syncExec and asyncExec must be
	* wrapped.
	*/
        if (code != SWT.ERROR_FAILED_EXEC) {
            if (throwable instanceof SWTError)
                throw (SWTError) throwable;
            if (throwable instanceof SWTException)
                throw (SWTException) throwable;
        }
        String message = findErrorText(code);
        if (detail != null)
            message += detail;
        switch(code) {
            /* Illegal Arguments (non-fatal) */
            case ERROR_NULL_ARGUMENT:
            case ERROR_CANNOT_BE_ZERO:
            case ERROR_INVALID_ARGUMENT:
            case ERROR_MENU_NOT_BAR:
            case ERROR_MENU_NOT_DROP_DOWN:
            case ERROR_MENU_NOT_POP_UP:
            case ERROR_MENUITEM_NOT_CASCADE:
            case ERROR_INVALID_PARENT:
            case ERROR_INVALID_RANGE:
                {
                    throw new IllegalArgumentException(message);
                }
            /* SWT Exceptions (non-fatal) */
            case ERROR_INVALID_SUBCLASS:
            case ERROR_THREAD_INVALID_ACCESS:
            case ERROR_WIDGET_DISPOSED:
            case ERROR_GRAPHIC_DISPOSED:
            case ERROR_DEVICE_DISPOSED:
            case ERROR_FUNCTION_DISPOSED:
            case ERROR_INVALID_IMAGE:
            case ERROR_UNSUPPORTED_DEPTH:
            case ERROR_UNSUPPORTED_FORMAT:
            case ERROR_FAILED_EXEC:
            case ERROR_FAILED_EVALUATE:
            case ERROR_CANNOT_INVERT_MATRIX:
            case ERROR_NO_GRAPHICS_LIBRARY:
            case ERROR_INVALID_RETURN_VALUE:
            case ERROR_IO:
                {
                    SWTException exception = new SWTException(code, message);
                    exception.throwable = throwable;
                    throw exception;
                }
            /* Operation System Errors (fatal, may occur only on some platforms) */
            case ERROR_CANNOT_GET_COUNT:
            case ERROR_CANNOT_GET_ENABLED:
            case ERROR_CANNOT_GET_ITEM:
            case ERROR_CANNOT_GET_ITEM_HEIGHT:
            case ERROR_CANNOT_GET_SELECTION:
            case ERROR_CANNOT_GET_TEXT:
            case ERROR_CANNOT_SET_ENABLED:
            case ERROR_CANNOT_SET_MENU:
            case ERROR_CANNOT_SET_SELECTION:
            case ERROR_CANNOT_SET_TEXT:
            case ERROR_ITEM_NOT_ADDED:
            case ERROR_ITEM_NOT_REMOVED:
            //FALL THROUGH
            /* SWT Errors (fatal, may occur only on some platforms) */
            case ERROR_FAILED_LOAD_LIBRARY:
            case ERROR_NO_MORE_CALLBACKS:
            case ERROR_NOT_IMPLEMENTED:
            case ERROR_UNSPECIFIED:
                {
                    SWTError error = new SWTError(code, message);
                    error.throwable = throwable;
                    throw error;
                }
            case ERROR_NO_HANDLES:
                SWTError error = new SWTError(code, message);
                error.throwable = throwable;
                throw error;
        }
        /* Unknown/Undefined Error */
        SWTError error = new SWTError(code, message);
        error.throwable = throwable;
        throw error;
    }

    static {
        /*
	* These values represent bit masks that may need to
	* expand in the future.  Therefore they are not initialized
	* in the declaration to stop the compiler from inlining.
	*/
        BUTTON_MASK = BUTTON1 | BUTTON2 | BUTTON3 | BUTTON4 | BUTTON5;
        MODIFIER_MASK = ALT | SHIFT | CTRL | COMMAND | ALT_GR;
        /*
	* These values can be different on different platforms.
	* Therefore they are not initialized in the declaration
	* to stop the compiler from inlining.
	*/
        String platform = getPlatform();
        if ("cocoa".equals(platform)) {
            //$NON-NLS-1$
            MOD1 = COMMAND;
            MOD2 = SHIFT;
            MOD3 = ALT;
            MOD4 = CONTROL;
        } else {
            MOD1 = CONTROL;
            MOD2 = SHIFT;
            MOD3 = ALT;
            MOD4 = 0;
        }
    }
}
