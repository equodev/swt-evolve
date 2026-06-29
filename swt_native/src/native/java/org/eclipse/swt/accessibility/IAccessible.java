package org.eclipse.swt.accessibility;

import java.net.*;
import java.util.*;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public interface IAccessible extends ImplAccessible {

    /**
     * Adds the listener to the collection of listeners who will
     * be notified when an accessible client asks for certain strings,
     * such as name, description, help, or keyboard shortcut. The
     * listener is notified by sending it one of the messages defined
     * in the <code>AccessibleListener</code> interface.
     *
     * @param listener the listener that should be notified when the receiver
     * is asked for a name, description, help, or keyboard shortcut string
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver's control has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver's control</li>
     * </ul>
     *
     * @see AccessibleListener
     * @see #removeAccessibleListener
     */
    void addAccessibleListener(AccessibleListener listener);

    /**
     * Adds the listener to the collection of listeners who will
     * be notified when an accessible client asks for custom control
     * specific information. The listener is notified by sending it
     * one of the messages defined in the <code>AccessibleControlListener</code>
     * interface.
     *
     * @param listener the listener that should be notified when the receiver
     * is asked for custom control specific information
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver's control has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver's control</li>
     * </ul>
     *
     * @see AccessibleControlListener
     * @see #removeAccessibleControlListener
     */
    void addAccessibleControlListener(AccessibleControlListener listener);

    /**
     * Adds the listener to the collection of listeners who will
     * be notified when an accessible client asks for custom text control
     * specific information. The listener is notified by sending it
     * one of the messages defined in the <code>AccessibleTextListener</code>
     * and <code>AccessibleTextExtendedListener</code> interfaces.
     *
     * @param listener the listener that should be notified when the receiver
     * is asked for custom text control specific information
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver's control has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver's control</li>
     * </ul>
     *
     * @see AccessibleTextListener
     * @see AccessibleTextExtendedListener
     * @see #removeAccessibleTextListener
     *
     * @since 3.0
     */
    void addAccessibleTextListener(AccessibleTextListener listener);

    /**
     * Adds the listener to the collection of listeners that will be
     * notified when an accessible client asks for any of the properties
     * defined in the <code>AccessibleActionListener</code> interface.
     *
     * @param listener the listener that should be notified when the receiver
     * is asked for <code>AccessibleActionListener</code> interface properties
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver's control has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver's control</li>
     * </ul>
     *
     * @see AccessibleActionListener
     * @see #removeAccessibleActionListener
     *
     * @since 3.6
     */
    void addAccessibleActionListener(AccessibleActionListener listener);

    /**
     * Adds the listener to the collection of listeners that will be
     * notified when an accessible client asks for any of the properties
     * defined in the <code>AccessibleEditableTextListener</code> interface.
     *
     * @param listener the listener that should be notified when the receiver
     * is asked for <code>AccessibleEditableTextListener</code> interface properties
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver's control has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver's control</li>
     * </ul>
     *
     * @see AccessibleEditableTextListener
     * @see #removeAccessibleEditableTextListener
     *
     * @since 3.7
     */
    void addAccessibleEditableTextListener(AccessibleEditableTextListener listener);

    /**
     * Adds the listener to the collection of listeners that will be
     * notified when an accessible client asks for any of the properties
     * defined in the <code>AccessibleHyperlinkListener</code> interface.
     *
     * @param listener the listener that should be notified when the receiver
     * is asked for <code>AccessibleHyperlinkListener</code> interface properties
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver's control has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver's control</li>
     * </ul>
     *
     * @see AccessibleHyperlinkListener
     * @see #removeAccessibleHyperlinkListener
     *
     * @since 3.6
     */
    void addAccessibleHyperlinkListener(AccessibleHyperlinkListener listener);

    /**
     * Adds the listener to the collection of listeners that will be
     * notified when an accessible client asks for any of the properties
     * defined in the <code>AccessibleTableListener</code> interface.
     *
     * @param listener the listener that should be notified when the receiver
     * is asked for <code>AccessibleTableListener</code> interface properties
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver's control has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver's control</li>
     * </ul>
     *
     * @see AccessibleTableListener
     * @see #removeAccessibleTableListener
     *
     * @since 3.6
     */
    void addAccessibleTableListener(AccessibleTableListener listener);

    /**
     * Adds the listener to the collection of listeners that will be
     * notified when an accessible client asks for any of the properties
     * defined in the <code>AccessibleTableCellListener</code> interface.
     *
     * @param listener the listener that should be notified when the receiver
     * is asked for <code>AccessibleTableCellListener</code> interface properties
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver's control has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver's control</li>
     * </ul>
     *
     * @see AccessibleTableCellListener
     * @see #removeAccessibleTableCellListener
     *
     * @since 3.6
     */
    void addAccessibleTableCellListener(AccessibleTableCellListener listener);

    /**
     * Adds the listener to the collection of listeners that will be
     * notified when an accessible client asks for any of the properties
     * defined in the <code>AccessibleValueListener</code> interface.
     *
     * @param listener the listener that should be notified when the receiver
     * is asked for <code>AccessibleValueListener</code> interface properties
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver's control has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver's control</li>
     * </ul>
     *
     * @see AccessibleValueListener
     * @see #removeAccessibleValueListener
     *
     * @since 3.6
     */
    void addAccessibleValueListener(AccessibleValueListener listener);

    /**
     * Adds the listener to the collection of listeners that will be
     * notified when an accessible client asks for any of the properties
     * defined in the <code>AccessibleAttributeListener</code> interface.
     *
     * @param listener the listener that should be notified when the receiver
     * is asked for <code>AccessibleAttributeListener</code> interface properties
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver's control has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver's control</li>
     * </ul>
     *
     * @see AccessibleAttributeListener
     * @see #removeAccessibleAttributeListener
     *
     * @since 3.6
     */
    void addAccessibleAttributeListener(AccessibleAttributeListener listener);

    /**
     * Adds a relation with the specified type and target
     * to the receiver's set of relations.
     *
     * @param type an <code>ACC</code> constant beginning with RELATION_* indicating the type of relation
     * @param target the accessible that is the target for this relation
     * @exception IllegalArgumentException ERROR_NULL_ARGUMENT - if the Accessible target is null
     * @since 3.6
     */
    void addRelation(int type, Accessible target);

    /**
     * Return YES if the UIElement doesn't show up to the outside world -
     * i.e. its parent should return the UIElement's children as its own -
     * cutting the UIElement out. E.g. NSControls are ignored when they are single-celled.
     * <p>
     * <b>IMPORTANT:</b> This field is <em>not</em> part of the SWT
     * public API. It is marked public only so that it can be shared
     * within the packages provided by SWT. It is not available on all
     * platforms and should never be accessed from application code.
     * </p>
     *
     * @noreference This method is not intended to be referenced by clients.
     */
    boolean internal_accessibilityIsIgnored(int childID);

    /**
     * Disposes of the operating system resources associated with
     * the receiver, and removes the receiver from its parent's
     * list of children.
     * <p>
     * This method should be called when an accessible that was created
     * with the public constructor <code>Accessible(Accessible parent)</code>
     * is no longer needed. You do not need to call this when the receiver's
     * control is disposed, because all <code>Accessible</code> instances
     * associated with a control are released when the control is disposed.
     * It is also not necessary to call this for instances of <code>Accessible</code>
     * that were retrieved with <code>Control.getAccessible()</code>.
     * </p>
     *
     * @since 3.6
     */
    void dispose();

    /**
     * Returns the control for this Accessible object.
     *
     * @return the receiver's control
     * @since 3.0
     */
    Control getControl();

    /**
     * Invokes platform specific functionality to dispose an accessible object.
     * <p>
     * <b>IMPORTANT:</b> This method is <em>not</em> part of the public
     * API for <code>Accessible</code>. It is marked public only so that it
     * can be shared within the packages provided by SWT. It is not
     * available on all platforms, and should never be called from
     * application code.
     * </p>
     *
     * @noreference This method is not intended to be referenced by clients.
     */
    void internal_dispose_Accessible();

    /**
     * Removes the listener from the collection of listeners who will
     * be notified when an accessible client asks for certain strings,
     * such as name, description, help, or keyboard shortcut.
     *
     * @param listener the listener that should no longer be notified when the receiver
     * is asked for a name, description, help, or keyboard shortcut string
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver's control has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver's control</li>
     * </ul>
     *
     * @see AccessibleListener
     * @see #addAccessibleListener
     */
    void removeAccessibleListener(AccessibleListener listener);

    /**
     * Removes the listener from the collection of listeners who will
     * be notified when an accessible client asks for custom control
     * specific information.
     *
     * @param listener the listener that should no longer be notified when the receiver
     * is asked for custom control specific information
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver's control has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver's control</li>
     * </ul>
     *
     * @see AccessibleControlListener
     * @see #addAccessibleControlListener
     */
    void removeAccessibleControlListener(AccessibleControlListener listener);

    /**
     * Removes the listener from the collection of listeners who will
     * be notified when an accessible client asks for custom text control
     * specific information.
     *
     * @param listener the listener that should no longer be notified when the receiver
     * is asked for custom text control specific information
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver's control has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver's control</li>
     * </ul>
     *
     * @see AccessibleTextListener
     * @see AccessibleTextExtendedListener
     * @see #addAccessibleTextListener
     *
     * @since 3.0
     */
    void removeAccessibleTextListener(AccessibleTextListener listener);

    /**
     * Removes the listener from the collection of listeners that will be
     * notified when an accessible client asks for any of the properties
     * defined in the <code>AccessibleActionListener</code> interface.
     *
     * @param listener the listener that should no longer be notified when the receiver
     * is asked for <code>AccessibleActionListener</code> interface properties
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver's control has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver's control</li>
     * </ul>
     *
     * @see AccessibleActionListener
     * @see #addAccessibleActionListener
     *
     * @since 3.6
     */
    void removeAccessibleActionListener(AccessibleActionListener listener);

    /**
     * Removes the listener from the collection of listeners that will be
     * notified when an accessible client asks for any of the properties
     * defined in the <code>AccessibleEditableTextListener</code> interface.
     *
     * @param listener the listener that should no longer be notified when the receiver
     * is asked for <code>AccessibleEditableTextListener</code> interface properties
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver's control has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver's control</li>
     * </ul>
     *
     * @see AccessibleEditableTextListener
     * @see #addAccessibleEditableTextListener
     *
     * @since 3.7
     */
    void removeAccessibleEditableTextListener(AccessibleEditableTextListener listener);

    /**
     * Removes the listener from the collection of listeners that will be
     * notified when an accessible client asks for any of the properties
     * defined in the <code>AccessibleHyperlinkListener</code> interface.
     *
     * @param listener the listener that should no longer be notified when the receiver
     * is asked for <code>AccessibleHyperlinkListener</code> interface properties
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver's control has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver's control</li>
     * </ul>
     *
     * @see AccessibleHyperlinkListener
     * @see #addAccessibleHyperlinkListener
     *
     * @since 3.6
     */
    void removeAccessibleHyperlinkListener(AccessibleHyperlinkListener listener);

    /**
     * Removes the listener from the collection of listeners that will be
     * notified when an accessible client asks for any of the properties
     * defined in the <code>AccessibleTableListener</code> interface.
     *
     * @param listener the listener that should no longer be notified when the receiver
     * is asked for <code>AccessibleTableListener</code> interface properties
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver's control has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver's control</li>
     * </ul>
     *
     * @see AccessibleTableListener
     * @see #addAccessibleTableListener
     *
     * @since 3.6
     */
    void removeAccessibleTableListener(AccessibleTableListener listener);

    /**
     * Removes the listener from the collection of listeners that will be
     * notified when an accessible client asks for any of the properties
     * defined in the <code>AccessibleTableCellListener</code> interface.
     *
     * @param listener the listener that should no longer be notified when the receiver
     * is asked for <code>AccessibleTableCellListener</code> interface properties
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver's control has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver's control</li>
     * </ul>
     *
     * @see AccessibleTableCellListener
     * @see #addAccessibleTableCellListener
     *
     * @since 3.6
     */
    void removeAccessibleTableCellListener(AccessibleTableCellListener listener);

    /**
     * Removes the listener from the collection of listeners that will be
     * notified when an accessible client asks for any of the properties
     * defined in the <code>AccessibleValueListener</code> interface.
     *
     * @param listener the listener that should no longer be notified when the receiver
     * is asked for <code>AccessibleValueListener</code> interface properties
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver's control has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver's control</li>
     * </ul>
     *
     * @see AccessibleValueListener
     * @see #addAccessibleValueListener
     *
     * @since 3.6
     */
    void removeAccessibleValueListener(AccessibleValueListener listener);

    /**
     * Removes the listener from the collection of listeners that will be
     * notified when an accessible client asks for any of the properties
     * defined in the <code>AccessibleAttributeListener</code> interface.
     *
     * @param listener the listener that should no longer be notified when the receiver
     * is asked for <code>AccessibleAttributeListener</code> interface properties
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver's control has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver's control</li>
     * </ul>
     *
     * @see AccessibleAttributeListener
     * @see #addAccessibleAttributeListener
     *
     * @since 3.6
     */
    void removeAccessibleAttributeListener(AccessibleAttributeListener listener);

    /**
     * Removes the relation with the specified type and target
     * from the receiver's set of relations.
     *
     * @param type an <code>ACC</code> constant beginning with RELATION_* indicating the type of relation
     * @param target the accessible that is the target for this relation
     * @exception IllegalArgumentException ERROR_NULL_ARGUMENT - if the Accessible target is null
     * @since 3.6
     */
    void removeRelation(int type, Accessible target);

    /**
     * Sends a message with event-specific data to accessible clients
     * indicating that something has changed within a custom control.
     *
     * @param event an <code>ACC</code> constant beginning with EVENT_* indicating the message to send
     * @param eventData an object containing event-specific data, or null if there is no event-specific data
     * (eventData is specified in the documentation for individual ACC.EVENT_* constants)
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver's control has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver's control</li>
     * </ul>
     *
     * @see ACC#EVENT_ACTION_CHANGED
     * @see ACC#EVENT_ATTRIBUTE_CHANGED
     * @see ACC#EVENT_DESCRIPTION_CHANGED
     * @see ACC#EVENT_DOCUMENT_LOAD_COMPLETE
     * @see ACC#EVENT_DOCUMENT_LOAD_STOPPED
     * @see ACC#EVENT_DOCUMENT_RELOAD
     * @see ACC#EVENT_HYPERLINK_ACTIVATED
     * @see ACC#EVENT_HYPERLINK_ANCHOR_COUNT_CHANGED
     * @see ACC#EVENT_HYPERLINK_END_INDEX_CHANGED
     * @see ACC#EVENT_HYPERLINK_SELECTED_LINK_CHANGED
     * @see ACC#EVENT_HYPERLINK_START_INDEX_CHANGED
     * @see ACC#EVENT_HYPERTEXT_LINK_COUNT_CHANGED
     * @see ACC#EVENT_HYPERTEXT_LINK_SELECTED
     * @see ACC#EVENT_LOCATION_CHANGED
     * @see ACC#EVENT_NAME_CHANGED
     * @see ACC#EVENT_PAGE_CHANGED
     * @see ACC#EVENT_SECTION_CHANGED
     * @see ACC#EVENT_SELECTION_CHANGED
     * @see ACC#EVENT_STATE_CHANGED
     * @see ACC#EVENT_TABLE_CAPTION_CHANGED
     * @see ACC#EVENT_TABLE_CHANGED
     * @see ACC#EVENT_TABLE_COLUMN_DESCRIPTION_CHANGED
     * @see ACC#EVENT_TABLE_COLUMN_HEADER_CHANGED
     * @see ACC#EVENT_TABLE_ROW_DESCRIPTION_CHANGED
     * @see ACC#EVENT_TABLE_ROW_HEADER_CHANGED
     * @see ACC#EVENT_TABLE_SUMMARY_CHANGED
     * @see ACC#EVENT_TEXT_ATTRIBUTE_CHANGED
     * @see ACC#EVENT_TEXT_CARET_MOVED
     * @see ACC#EVENT_TEXT_CHANGED
     * @see ACC#EVENT_TEXT_COLUMN_CHANGED
     * @see ACC#EVENT_TEXT_SELECTION_CHANGED
     * @see ACC#EVENT_VALUE_CHANGED
     *
     * @since 3.6
     */
    void sendEvent(int event, Object eventData);

    /**
     * Sends a message with event-specific data and a childID
     * to accessible clients, indicating that something has changed
     * within a custom control.
     *
     * NOTE: This API is intended for applications that are still using childIDs.
     * Moving forward, applications should use accessible objects instead of childIDs.
     *
     * @param event an <code>ACC</code> constant beginning with EVENT_* indicating the message to send
     * @param eventData an object containing event-specific data, or null if there is no event-specific data
     * (eventData is specified in the documentation for individual ACC.EVENT_* constants)
     * @param childID an identifier specifying a child of the control
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver's control has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver's control</li>
     * </ul>
     *
     * @see ACC#EVENT_DESCRIPTION_CHANGED
     * @see ACC#EVENT_LOCATION_CHANGED
     * @see ACC#EVENT_NAME_CHANGED
     * @see ACC#EVENT_SELECTION_CHANGED
     * @see ACC#EVENT_STATE_CHANGED
     * @see ACC#EVENT_TEXT_SELECTION_CHANGED
     * @see ACC#EVENT_VALUE_CHANGED
     *
     * @since 3.8
     */
    void sendEvent(int event, Object eventData, int childID);

    /**
     * Sends a message to accessible clients that the child selection
     * within a custom container control has changed.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver's control has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver's control</li>
     * </ul>
     *
     * @since 3.0
     */
    void selectionChanged();

    /**
     * Sends a message to accessible clients indicating that the focus
     * has changed within a custom control.
     *
     * @param childID an identifier specifying a child of the control
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver's control has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver's control</li>
     * </ul>
     */
    void setFocus(int childID);

    /**
     * Sends a message to accessible clients that the text
     * caret has moved within a custom control.
     *
     * @param index the new caret index within the control
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver's control has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver's control</li>
     * </ul>
     *
     * @since 3.0
     */
    void textCaretMoved(int index);

    /**
     * Sends a message to accessible clients that the text
     * within a custom control has changed.
     *
     * @param type the type of change, one of <code>ACC.TEXT_INSERT</code>
     * or <code>ACC.TEXT_DELETE</code>
     * @param startIndex the text index within the control where the insertion or deletion begins
     * @param length the non-negative length in characters of the insertion or deletion
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver's control has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver's control</li>
     * </ul>
     *
     * @see ACC#TEXT_INSERT
     * @see ACC#TEXT_DELETE
     *
     * @since 3.0
     */
    void textChanged(int type, int startIndex, int length);

    /**
     * Sends a message to accessible clients that the text
     * selection has changed within a custom control.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver's control has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver's control</li>
     * </ul>
     *
     * @since 3.0
     */
    void textSelectionChanged();

    /**
     * Adds relationship attributes if needed to the property list.
     * <p>
     * <b>IMPORTANT:</b> This method is <em>not</em> part of the SWT
     * public API. It is marked public only so that it can be shared
     * within the packages provided by SWT. It is not available on all
     * platforms and should never be accessed from application code.
     * </p>
     *
     * @noreference This method is not intended to be referenced by clients.
     */
    long internal_addRelationAttributes(long defaultAttributes);

    Accessible getApi();

    void setApi(Accessible api);
}
