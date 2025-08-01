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
 * *****************************************************************************
 */
package org.eclipse.swt.accessibility;

import java.util.*;
import java.util.List;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.internal.*;
import org.eclipse.swt.internal.ole.win32.*;
import org.eclipse.swt.internal.win32.*;
import org.eclipse.swt.ole.win32.*;
import org.eclipse.swt.widgets.*;

/**
 * Instances of this class provide a bridge between application
 * code and assistive technology clients. Many platforms provide
 * default accessible behavior for most widgets, and this class
 * allows that default behavior to be overridden. Applications
 * can get the default Accessible object for a control by sending
 * it <code>getAccessible</code>, and then add an accessible listener
 * to override simple items like the name and help string, or they
 * can add an accessible control listener to override complex items.
 * As a rule of thumb, an application would only want to use the
 * accessible control listener to implement accessibility for a
 * custom control.
 *
 * @see Control#getAccessible
 * @see AccessibleListener
 * @see AccessibleEvent
 * @see AccessibleControlListener
 * @see AccessibleControlEvent
 * @see <a href="http://www.eclipse.org/swt/snippets/#accessibility">Accessibility snippets</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 *
 * @since 2.0
 */
public class Accessible {

    /**
     * Constructs a new instance of this class given its parent.
     *
     * @param parent the Accessible parent, which must not be null
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
     * </ul>
     *
     * @see #dispose
     * @see Control#getAccessible
     *
     * @since 3.6
     */
    public Accessible(Accessible parent) {
        this((IIAccessible) null);
        setImpl(new SwtAccessible(parent, this));
    }

    /**
     * @since 3.5
     * @deprecated
     */
    @Deprecated
    protected Accessible() {
        this((IIAccessible) null);
        setImpl(new SwtAccessible(this));
    }

    Accessible(Control control) {
        this((IIAccessible) null);
        setImpl(new SwtAccessible(control, this));
    }

    Accessible(Accessible parent, long iaccessible_address) {
        this((IIAccessible) null);
        setImpl(new SwtAccessible(parent, iaccessible_address, this));
    }

    // This method is intentionally commented. We are not providing IAccessibleComponent at this time.
    //	void createIAccessibleComponent() {
    //		objIAccessibleComponent = new COMObject(new int[] {2,0,0,2,1,1}) {
    //			public long method0(long[] args) {return QueryInterface(args[0], args[1]);}
    //			public long method1(long[] args) {return AddRef();}
    //			public long method2(long[] args) {return Release();}
    //			public long method3(long[] args) {return get_locationInParent(args[0], args[1]);}
    //			public long method4(long[] args) {return get_foreground(args[0]);}
    //			public long method5(long[] args) {return get_background(args[0]);}
    //		};
    //	}
    // This method is intentionally commented. We are not providing IAccessibleImage at this time.
    //	void createIAccessibleImage() {
    //		objIAccessibleImage = new COMObject(new int[] {2,0,0,1,3,2}) {
    //			public long method0(long[] args) {return QueryInterface(args[0], args[1]);}
    //			public long method1(long[] args) {return AddRef();}
    //			public long method2(long[] args) {return Release();}
    //			public long method3(long[] args) {return get_description(args[0]);}
    //			public long method4(long[] args) {return get_imagePosition((int)args[0], args[1], args[2]);}
    //			public long method5(long[] args) {return get_imageSize(args[0], args[1]);}
    //		};
    //	}
    /**
     * Invokes platform specific functionality to allocate a new accessible object.
     * <p>
     * <b>IMPORTANT:</b> This method is <em>not</em> part of the public
     * API for <code>Accessible</code>. It is marked public only so that it
     * can be shared within the packages provided by SWT. It is not
     * available on all platforms, and should never be called from
     * application code.
     * </p>
     *
     * @param control the control to get the accessible object for
     * @return the platform specific accessible object
     *
     * @noreference This method is not intended to be referenced by clients.
     */
    public static Accessible internal_new_Accessible(Control control) {
        return SwtAccessible.internal_new_Accessible(control);
    }

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
    public void addAccessibleListener(AccessibleListener listener) {
        getImpl().addAccessibleListener(listener);
    }

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
    public void addAccessibleControlListener(AccessibleControlListener listener) {
        getImpl().addAccessibleControlListener(listener);
    }

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
    public void addAccessibleTextListener(AccessibleTextListener listener) {
        getImpl().addAccessibleTextListener(listener);
    }

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
    public void addAccessibleActionListener(AccessibleActionListener listener) {
        getImpl().addAccessibleActionListener(listener);
    }

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
    public void addAccessibleEditableTextListener(AccessibleEditableTextListener listener) {
        getImpl().addAccessibleEditableTextListener(listener);
    }

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
    public void addAccessibleHyperlinkListener(AccessibleHyperlinkListener listener) {
        getImpl().addAccessibleHyperlinkListener(listener);
    }

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
    public void addAccessibleTableListener(AccessibleTableListener listener) {
        getImpl().addAccessibleTableListener(listener);
    }

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
    public void addAccessibleTableCellListener(AccessibleTableCellListener listener) {
        getImpl().addAccessibleTableCellListener(listener);
    }

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
    public void addAccessibleValueListener(AccessibleValueListener listener) {
        getImpl().addAccessibleValueListener(listener);
    }

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
    public void addAccessibleAttributeListener(AccessibleAttributeListener listener) {
        getImpl().addAccessibleAttributeListener(listener);
    }

    /**
     * Adds a relation with the specified type and target
     * to the receiver's set of relations.
     *
     * @param type an <code>ACC</code> constant beginning with RELATION_* indicating the type of relation
     * @param target the accessible that is the target for this relation
     * @exception IllegalArgumentException ERROR_NULL_ARGUMENT - if the Accessible target is null
     * @since 3.6
     */
    public void addRelation(int type, Accessible target) {
        getImpl().addRelation(type, target);
    }

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
    public void dispose() {
        getImpl().dispose();
    }

    /**
     * Returns the control for this Accessible object.
     *
     * @return the receiver's control
     * @since 3.0
     */
    public Control getControl() {
        return getImpl().getControl();
    }

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
    public void internal_dispose_Accessible() {
        getImpl().internal_dispose_Accessible();
    }

    /**
     * Invokes platform specific functionality to handle a window message.
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
    public long internal_WM_GETOBJECT(long wParam, long lParam) {
        return getImpl().internal_WM_GETOBJECT(wParam, lParam);
    }

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
    public void removeAccessibleListener(AccessibleListener listener) {
        getImpl().removeAccessibleListener(listener);
    }

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
    public void removeAccessibleControlListener(AccessibleControlListener listener) {
        getImpl().removeAccessibleControlListener(listener);
    }

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
    public void removeAccessibleTextListener(AccessibleTextListener listener) {
        getImpl().removeAccessibleTextListener(listener);
    }

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
    public void removeAccessibleActionListener(AccessibleActionListener listener) {
        getImpl().removeAccessibleActionListener(listener);
    }

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
    public void removeAccessibleEditableTextListener(AccessibleEditableTextListener listener) {
        getImpl().removeAccessibleEditableTextListener(listener);
    }

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
    public void removeAccessibleHyperlinkListener(AccessibleHyperlinkListener listener) {
        getImpl().removeAccessibleHyperlinkListener(listener);
    }

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
    public void removeAccessibleTableListener(AccessibleTableListener listener) {
        getImpl().removeAccessibleTableListener(listener);
    }

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
    public void removeAccessibleTableCellListener(AccessibleTableCellListener listener) {
        getImpl().removeAccessibleTableCellListener(listener);
    }

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
    public void removeAccessibleValueListener(AccessibleValueListener listener) {
        getImpl().removeAccessibleValueListener(listener);
    }

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
    public void removeAccessibleAttributeListener(AccessibleAttributeListener listener) {
        getImpl().removeAccessibleAttributeListener(listener);
    }

    /**
     * Removes the relation with the specified type and target
     * from the receiver's set of relations.
     *
     * @param type an <code>ACC</code> constant beginning with RELATION_* indicating the type of relation
     * @param target the accessible that is the target for this relation
     * @exception IllegalArgumentException ERROR_NULL_ARGUMENT - if the Accessible target is null
     * @since 3.6
     */
    public void removeRelation(int type, Accessible target) {
        getImpl().removeRelation(type, target);
    }

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
    public void sendEvent(int event, Object eventData) {
        getImpl().sendEvent(event, eventData);
    }

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
    public void sendEvent(int event, Object eventData, int childID) {
        getImpl().sendEvent(event, eventData, childID);
    }

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
    public void selectionChanged() {
        getImpl().selectionChanged();
    }

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
    public void setFocus(int childID) {
        getImpl().setFocus(childID);
    }

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
    public void textCaretMoved(int index) {
        getImpl().textCaretMoved(index);
    }

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
    public void textChanged(int type, int startIndex, int length) {
        getImpl().textChanged(type, startIndex, length);
    }

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
    public void textSelectionChanged() {
        getImpl().textSelectionChanged();
    }

    // TODO: Consider supporting this in future.
    /* IEnumVARIANT methods: Next, Skip, Reset, Clone */
    /* Retrieve the next celt items in the enumeration sequence.
	 * If there are fewer than the requested number of elements left
	 * in the sequence, retrieve the remaining elements.
	 * The number of elements actually retrieved is returned in pceltFetched
	 * (unless the caller passed in NULL for that parameter).
	 */
    // The following 3 method are intentionally commented. We are not providing IAccessibleComponent at this time.
    //	/* IAccessibleComponent::get_locationInParent([out] pX, [out] pY) */
    //	int get_locationInParent(long pX, long pY) {
    //		if (DEBUG) print(this + ".IAccessibleComponent::get_locationInParent");
    //		// TO DO: support transparently (hard for lightweight parents - screen vs. parent coords)
    //		AccessibleControlEvent event = new AccessibleControlEvent(this);
    //		for (int i = 0; i < accessibleControlListenersSize(); i++) {
    //			AccessibleControlListener listener = (AccessibleControlListener) accessibleControlListeners.get(i);
    //			listener.getLocation (event);
    //		}
    //		COM.MoveMemory(pX, new int [] { event.x }, 4);
    //		COM.MoveMemory(pY, new int [] { event.y }, 4);
    //		return COM.S_OK;
    //	}
    //
    //	/* IAccessibleComponent::get_foreground([out] pForeground) */
    //	int get_foreground(long pForeground) {
    //		Color color = control.getForeground();
    //		if (DEBUG) print(this + ".IAccessibleComponent::get_foreground returning " + color.handle);
    //		COM.MoveMemory(pForeground, new int [] { color.handle }, 4);
    //		return COM.S_OK;
    //	}
    //
    //	/* IAccessibleComponent::get_background([out] pBackground) */
    //	int get_background(long pBackground) {
    //		Color color = control.getBackground();
    //		if (DEBUG) print(this + ".IAccessibleComponent::get_background returning " + color.handle);
    //		COM.MoveMemory(pBackground, new int [] { color.handle }, 4);
    //		return COM.S_OK;
    //	}
    // The following 3 method are intentionally commented. We are not providing IAccessibleImage at this time.
    //	/* IAccessibleImage::get_description([out] pbstrDescription) */
    //	int get_description(long pbstrDescription) {
    //		if (DEBUG) print(this + ".IAccessibleImage::get_description");
    //		// TO DO: Does it make sense to just reuse description?
    //		AccessibleEvent event = new AccessibleEvent(this);
    //		event.childID = ACC.CHILDID_SELF;
    //		for (int i = 0; i < accessibleListenersSize(); i++) {
    //			AccessibleListener listener = (AccessibleListener) accessibleListeners.get(i);
    //			listener.getDescription(event);
    //		}
    //		setString(pbstrDescription, event.result);
    //		if (event.result == null) return COM.S_FALSE;
    //		return COM.S_OK;
    //	}
    //
    //	/* IAccessibleImage::get_imagePosition([in] coordinateType, [out] pX, [out] pY) */
    //	int get_imagePosition(int coordinateType, long pX, long pY) {
    //		if (DEBUG) print(this + ".IAccessibleImage::get_imagePosition");
    //		// TO DO: does it make sense to just reuse getLocation?
    //		AccessibleControlEvent event = new AccessibleControlEvent(this);
    //		event.childID = ACC.CHILDID_SELF;
    //		for (int i = 0; i < accessibleControlListenersSize(); i++) {
    //			AccessibleControlListener listener = (AccessibleControlListener) accessibleControlListeners.get(i);
    //			listener.getLocation(event);
    //		}
    //		COM.MoveMemory(pX, new int [] { event.x }, 4);
    //		COM.MoveMemory(pY, new int [] { event.y }, 4);
    //		return COM.S_OK;
    //	}
    //
    //	/* IAccessibleImage::get_imageSize([out] pHeight, [out] pWidth) */
    //	int get_imageSize(long pHeight, long pWidth) {
    //		if (DEBUG) print(this + ".IAccessibleImage::get_imageSize");
    //		// TO DO: does it make sense to just reuse getLocation?
    //		AccessibleControlEvent event = new AccessibleControlEvent(this);
    //		for (int i = 0; i < accessibleControlListenersSize(); i++) {
    //			AccessibleControlListener listener = (AccessibleControlListener) accessibleControlListeners.get(i);
    //			listener.getLocation(event);
    //		}
    //		COM.MoveMemory(pHeight, new int [] { event.height }, 4);
    //		COM.MoveMemory(pWidth, new int [] { event.width }, 4);
    //		return COM.S_OK;
    //	}
    public String toString() {
        return getImpl().toString();
    }

    // END DEBUG CODE
    protected IIAccessible impl;

    protected Accessible(IIAccessible impl) {
        if (impl != null)
            impl.setApi(this);
    }

    static Accessible createApi(IIAccessible impl) {
        return new Accessible(impl);
    }

    public IIAccessible getImpl() {
        return impl;
    }

    protected Accessible setImpl(IIAccessible impl) {
        this.impl = impl;
        return this;
    }
}
