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
import org.eclipse.swt.widgets.*;
import dev.equo.swt.*;

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
public class DartAccessible implements IIAccessible {

    static final int MAX_RELATION_TYPES = 15;

    static final int TABLE_MODEL_CHANGE_SIZE = 5;

    static final int TEXT_CHANGE_SIZE = 4;

    static final int SCROLL_RATE = 100;

    static final boolean DEBUG = false;

    //$NON-NLS-1$
    static final String PROPERTY_USEIA2 = "org.eclipse.swt.accessibility.UseIA2";

    static boolean UseIA2 = true;

    static int UniqueID = -0x10;

    int refCount = 0, enumIndex = 0;

    Runnable timer;

    List<AccessibleListener> accessibleListeners;

    List<AccessibleControlListener> accessibleControlListeners;

    List<AccessibleTextListener> accessibleTextListeners;

    List<AccessibleActionListener> accessibleActionListeners;

    List<AccessibleEditableTextListener> accessibleEditableTextListeners;

    List<AccessibleHyperlinkListener> accessibleHyperlinkListeners;

    List<AccessibleTableListener> accessibleTableListeners;

    List<AccessibleTableCellListener> accessibleTableCellListeners;

    List<AccessibleTextExtendedListener> accessibleTextExtendedListeners;

    List<AccessibleValueListener> accessibleValueListeners;

    List<AccessibleAttributeListener> accessibleAttributeListeners;

    Relation[] relations = new Relation[MAX_RELATION_TYPES];

    Object[] variants;

    Accessible parent;

    List<Accessible> children = new ArrayList<>();

    Control control;

    int uniqueID = -1;

    // type, rowStart, rowCount, columnStart, columnCount
    int[] tableChange;

    // type, start, end, text
    Object[] textDeleted;

    // type, start, end, text
    Object[] textInserted;

    ToolItem item;

    static {
        String property = System.getProperty(PROPERTY_USEIA2);
        if (property != null && property.equalsIgnoreCase("false")) {
            //$NON-NLS-1$
            UseIA2 = false;
        }
    }

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
    public DartAccessible(Accessible parent, Accessible api) {
        setApi(api);
        this.parent = checkNull(parent);
        this.control = parent.getImpl()._control();
        ((DartAccessible) parent.getImpl()).children.add(this.getApi());
        AddRef();
    }

    /**
     * @since 3.5
     * @deprecated
     */
    @Deprecated
    protected DartAccessible(Accessible api) {
        setApi(api);
    }

    DartAccessible(Control control, Accessible api) {
        setApi(api);
        this.control = control;
        long[] ppvObject = new long[1];
        /* The object needs to be checked, because if the CreateStdAccessibleObject()
		 * symbol is not found, the return value is S_OK.
		 */
        if (ppvObject[0] == 0)
            return;
        createIAccessible();
        AddRef();
    }

    DartAccessible(Accessible parent, long iaccessible_address, Accessible api) {
        this(parent, api);
    }

    static Accessible checkNull(Accessible parent) {
        if (parent == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        return parent;
    }

    void createIAccessible() {
    }

    void createIAccessibleApplication() {
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
    void createIAccessibleEditableText() {
    }

    void createIAccessibleHyperlink() {
    }

    void createIAccessibleHypertext() {
    }

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
    void createIAccessibleTable2() {
    }

    void createIAccessibleTableCell() {
    }

    void createIAccessibleValue() {
    }

    void createIEnumVARIANT() {
    }

    void createIServiceProvider() {
    }

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
        return new Accessible(control);
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
        checkWidget();
        if (listener == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        if (accessibleListeners == null)
            accessibleListeners = new ArrayList<>();
        accessibleListeners.add(listener);
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
        checkWidget();
        if (listener == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        if (accessibleControlListeners == null)
            accessibleControlListeners = new ArrayList<>();
        accessibleControlListeners.add(listener);
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
        checkWidget();
        if (listener == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        if (listener instanceof AccessibleTextExtendedListener) {
            if (accessibleTextExtendedListeners == null)
                accessibleTextExtendedListeners = new ArrayList<>();
            accessibleTextExtendedListeners.add((AccessibleTextExtendedListener) listener);
        } else {
            if (accessibleTextListeners == null)
                accessibleTextListeners = new ArrayList<>();
            accessibleTextListeners.add(listener);
        }
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
        checkWidget();
        if (listener == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        if (accessibleActionListeners == null)
            accessibleActionListeners = new ArrayList<>();
        accessibleActionListeners.add(listener);
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
        checkWidget();
        if (listener == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        if (accessibleEditableTextListeners == null)
            accessibleEditableTextListeners = new ArrayList<>();
        accessibleEditableTextListeners.add(listener);
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
        checkWidget();
        if (listener == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        if (accessibleHyperlinkListeners == null)
            accessibleHyperlinkListeners = new ArrayList<>();
        accessibleHyperlinkListeners.add(listener);
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
        checkWidget();
        if (listener == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        if (accessibleTableListeners == null)
            accessibleTableListeners = new ArrayList<>();
        accessibleTableListeners.add(listener);
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
        checkWidget();
        if (listener == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        if (accessibleTableCellListeners == null)
            accessibleTableCellListeners = new ArrayList<>();
        accessibleTableCellListeners.add(listener);
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
        checkWidget();
        if (listener == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        if (accessibleValueListeners == null)
            accessibleValueListeners = new ArrayList<>();
        accessibleValueListeners.add(listener);
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
        checkWidget();
        if (listener == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        if (accessibleAttributeListeners == null)
            accessibleAttributeListeners = new ArrayList<>();
        accessibleAttributeListeners.add(listener);
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
        checkWidget();
        if (target == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        if (relations[type] == null) {
            relations[type] = new Relation(this.getApi(), type);
        }
        relations[type].addTarget(target);
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
        if (parent == null)
            return;
        Release();
        ((DartAccessible) parent.getImpl()).children.remove(this.getApi());
        parent = null;
    }

    long getAddress() {
        return 0;
    }

    /**
     * Returns the control for this Accessible object.
     *
     * @return the receiver's control
     * @since 3.0
     */
    public Control getControl() {
        return control;
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
        Release();
        List<Accessible> list = new ArrayList<>(children);
        for (Accessible accChild : list) {
            accChild.dispose();
        }
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
        checkWidget();
        if (listener == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        if (accessibleListeners != null) {
            accessibleListeners.remove(listener);
            if (accessibleListeners.isEmpty())
                accessibleListeners = null;
        }
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
        checkWidget();
        if (listener == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        if (accessibleControlListeners != null) {
            accessibleControlListeners.remove(listener);
            if (accessibleControlListeners.isEmpty())
                accessibleControlListeners = null;
        }
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
        checkWidget();
        if (listener == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        if (listener instanceof AccessibleTextExtendedListener) {
            if (accessibleTextExtendedListeners != null) {
                accessibleTextExtendedListeners.remove(listener);
                if (accessibleTextExtendedListeners.isEmpty())
                    accessibleTextExtendedListeners = null;
            }
        } else {
            if (accessibleTextListeners != null) {
                accessibleTextListeners.remove(listener);
                if (accessibleTextListeners.isEmpty())
                    accessibleTextListeners = null;
            }
        }
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
        checkWidget();
        if (listener == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        if (accessibleActionListeners != null) {
            accessibleActionListeners.remove(listener);
            if (accessibleActionListeners.isEmpty())
                accessibleActionListeners = null;
        }
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
        checkWidget();
        if (listener == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        if (accessibleEditableTextListeners != null) {
            accessibleEditableTextListeners.remove(listener);
            if (accessibleEditableTextListeners.isEmpty())
                accessibleEditableTextListeners = null;
        }
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
        checkWidget();
        if (listener == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        if (accessibleHyperlinkListeners != null) {
            accessibleHyperlinkListeners.remove(listener);
            if (accessibleHyperlinkListeners.isEmpty())
                accessibleHyperlinkListeners = null;
        }
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
        checkWidget();
        if (listener == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        if (accessibleTableListeners != null) {
            accessibleTableListeners.remove(listener);
            if (accessibleTableListeners.isEmpty())
                accessibleTableListeners = null;
        }
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
        checkWidget();
        if (listener == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        if (accessibleTableCellListeners != null) {
            accessibleTableCellListeners.remove(listener);
            if (accessibleTableCellListeners.isEmpty())
                accessibleTableCellListeners = null;
        }
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
        checkWidget();
        if (listener == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        if (accessibleValueListeners != null) {
            accessibleValueListeners.remove(listener);
            if (accessibleValueListeners.isEmpty())
                accessibleValueListeners = null;
        }
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
        checkWidget();
        if (listener == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        if (accessibleAttributeListeners != null) {
            accessibleAttributeListeners.remove(listener);
            if (accessibleAttributeListeners.isEmpty())
                accessibleAttributeListeners = null;
        }
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
        checkWidget();
        if (target == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        Relation relation = relations[type];
        if (relation != null) {
            relation.removeTarget(target);
            if (!relation.hasTargets()) {
                relations[type].Release();
                relations[type] = null;
            }
        }
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
        checkWidget();
        if (!isATRunning())
            return;
        if (!UseIA2)
            return;
        if (DEBUG)
            print(this.getApi() + ".NotifyWinEvent " + getEventString(event) + " hwnd=" + control.handle + " childID=" + eventChildID());
        switch(event) {
            case ACC.EVENT_TABLE_CHANGED:
                {
                    if (!(eventData instanceof int[] && ((int[]) eventData).length == TABLE_MODEL_CHANGE_SIZE))
                        break;
                    tableChange = (int[]) eventData;
                    break;
                }
            case ACC.EVENT_TEXT_CHANGED:
                {
                    if (!(eventData instanceof Object[] && ((Object[]) eventData).length == TEXT_CHANGE_SIZE))
                        break;
                    Object[] data = (Object[]) eventData;
                    int type = ((Integer) data[0]).intValue();
                    switch(type) {
                        case ACC.DELETE:
                            textDeleted = (Object[]) eventData;
                            break;
                        case ACC.INSERT:
                            textInserted = (Object[]) eventData;
                            break;
                    }
                    break;
                }
            case ACC.EVENT_HYPERTEXT_LINK_SELECTED:
                {
                    if (!(eventData instanceof Integer))
                        break;
                    //			int index = ((Integer)eventData).intValue();
                    break;
                }
            case ACC.EVENT_VALUE_CHANGED:
                break;
            case ACC.EVENT_STATE_CHANGED:
                break;
            case ACC.EVENT_SELECTION_CHANGED:
                break;
            case ACC.EVENT_TEXT_SELECTION_CHANGED:
                break;
            case ACC.EVENT_LOCATION_CHANGED:
                break;
            case ACC.EVENT_NAME_CHANGED:
                break;
            case ACC.EVENT_DESCRIPTION_CHANGED:
                break;
            case ACC.EVENT_DOCUMENT_LOAD_COMPLETE:
                break;
            case ACC.EVENT_DOCUMENT_LOAD_STOPPED:
                break;
            case ACC.EVENT_DOCUMENT_RELOAD:
                break;
            case ACC.EVENT_PAGE_CHANGED:
                break;
            case ACC.EVENT_SECTION_CHANGED:
                break;
            case ACC.EVENT_ACTION_CHANGED:
                break;
            case ACC.EVENT_HYPERLINK_START_INDEX_CHANGED:
                break;
            case ACC.EVENT_HYPERLINK_END_INDEX_CHANGED:
                break;
            case ACC.EVENT_HYPERLINK_ANCHOR_COUNT_CHANGED:
                break;
            case ACC.EVENT_HYPERLINK_SELECTED_LINK_CHANGED:
                break;
            case ACC.EVENT_HYPERLINK_ACTIVATED:
                break;
            case ACC.EVENT_HYPERTEXT_LINK_COUNT_CHANGED:
                break;
            case ACC.EVENT_ATTRIBUTE_CHANGED:
                break;
            case ACC.EVENT_TABLE_CAPTION_CHANGED:
                break;
            case ACC.EVENT_TABLE_COLUMN_DESCRIPTION_CHANGED:
                break;
            case ACC.EVENT_TABLE_COLUMN_HEADER_CHANGED:
                break;
            case ACC.EVENT_TABLE_ROW_DESCRIPTION_CHANGED:
                break;
            case ACC.EVENT_TABLE_ROW_HEADER_CHANGED:
                break;
            case ACC.EVENT_TABLE_SUMMARY_CHANGED:
                break;
            case ACC.EVENT_TEXT_ATTRIBUTE_CHANGED:
                break;
            case ACC.EVENT_TEXT_CARET_MOVED:
                break;
            case ACC.EVENT_TEXT_COLUMN_CHANGED:
                break;
        }
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
        checkWidget();
        if (!isATRunning())
            return;
        if (!UseIA2)
            return;
        int osChildID = childID == ACC.CHILDID_SELF ? eventChildID() : childIDToOs(childID);
        if (DEBUG)
            print(this.getApi() + ".NotifyWinEvent " + getEventString(event) + " hwnd=" + control.handle + " childID=" + osChildID);
        switch(event) {
            case ACC.EVENT_STATE_CHANGED:
                break;
            case ACC.EVENT_NAME_CHANGED:
                break;
            case ACC.EVENT_VALUE_CHANGED:
                break;
            case ACC.EVENT_LOCATION_CHANGED:
                break;
            case ACC.EVENT_SELECTION_CHANGED:
                break;
            case ACC.EVENT_TEXT_SELECTION_CHANGED:
                break;
            case ACC.EVENT_DESCRIPTION_CHANGED:
                break;
        }
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
        checkWidget();
        if (!isATRunning())
            return;
        if (DEBUG)
            print(this.getApi() + ".NotifyWinEvent EVENT_OBJECT_SELECTIONWITHIN hwnd=" + control.handle + " childID=" + eventChildID());
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
        int newValue = childID;
        checkWidget();
        if (!isATRunning())
            return;
        int osChildID = childID == ACC.CHILDID_SELF ? eventChildID() : childIDToOs(childID);
        if (DEBUG)
            print(this.getApi() + ".NotifyWinEvent EVENT_OBJECT_FOCUS hwnd=" + control.handle + " childID=" + osChildID);
        this.focus = newValue;
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
        checkWidget();
        if (timer == null) {
            timer = new Runnable() {

                @Override
                public void run() {
                    if (!isATRunning())
                        return;
                    if (DEBUG)
                        print(this + ".NotifyWinEvent EVENT_OBJECT_LOCATIONCHANGE hwnd=" + control.handle + " childID=" + eventChildID());
                    if (!UseIA2)
                        return;
                    if (DEBUG)
                        print(this + ".NotifyWinEvent IA2_EVENT_TEXT_CARET_MOVED hwnd=" + control.handle + " childID=" + eventChildID());
                }
            };
        }
        control.getDisplay().timerExec(SCROLL_RATE, timer);
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
        checkWidget();
        if (!isATRunning())
            return;
        AccessibleTextEvent event = new AccessibleTextEvent(this.getApi());
        event.start = startIndex;
        event.end = startIndex + length;
        event.count = 0;
        event.type = ACC.TEXT_BOUNDARY_ALL;
        for (int i = 0; i < accessibleTextExtendedListenersSize(); i++) {
            AccessibleTextExtendedListener listener = accessibleTextExtendedListeners.get(i);
            listener.getText(event);
        }
        if (event.result != null) {
            Object[] eventData = new Object[] { Integer.valueOf(type), Integer.valueOf(startIndex), Integer.valueOf(startIndex + length), event.result };
            sendEvent(ACC.EVENT_TEXT_CHANGED, eventData);
            return;
        }
        if (DEBUG)
            print(this.getApi() + ".NotifyWinEvent EVENT_OBJECT_VALUECHANGE hwnd=" + control.handle + " childID=" + eventChildID());
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
        checkWidget();
        if (!isATRunning())
            return;
        if (DEBUG)
            print(this.getApi() + ".NotifyWinEvent EVENT_OBJECT_TEXTSELECTIONCHANGED hwnd=" + control.handle + " childID=" + eventChildID());
    }

    /* QueryInterface([in] iid, [out] ppvObject)
	 * Ownership of ppvObject transfers from callee to caller so reference count on ppvObject
	 * must be incremented before returning.  Caller is responsible for releasing ppvObject.
	 */
    int QueryInterface(long iid, long ppvObject) {
        return 0;
    }

    int accessibleListenersSize() {
        return accessibleListeners == null ? 0 : accessibleListeners.size();
    }

    int accessibleControlListenersSize() {
        return accessibleControlListeners == null ? 0 : accessibleControlListeners.size();
    }

    int accessibleValueListenersSize() {
        return accessibleValueListeners == null ? 0 : accessibleValueListeners.size();
    }

    int accessibleTextExtendedListenersSize() {
        return accessibleTextExtendedListeners == null ? 0 : accessibleTextExtendedListeners.size();
    }

    int accessibleTextListenersSize() {
        return accessibleTextListeners == null ? 0 : accessibleTextListeners.size();
    }

    int accessibleTableCellListenersSize() {
        return accessibleTableCellListeners == null ? 0 : accessibleTableCellListeners.size();
    }

    int accessibleTableListenersSize() {
        return accessibleTableListeners == null ? 0 : accessibleTableListeners.size();
    }

    int accessibleHyperlinkListenersSize() {
        return accessibleHyperlinkListeners == null ? 0 : accessibleHyperlinkListeners.size();
    }

    int accessibleEditableTextListenersSize() {
        return accessibleEditableTextListeners == null ? 0 : accessibleEditableTextListeners.size();
    }

    int accessibleAttributeListenersSize() {
        return accessibleAttributeListeners == null ? 0 : accessibleAttributeListeners.size();
    }

    int accessibleActionListenersSize() {
        return accessibleActionListeners == null ? 0 : accessibleActionListeners.size();
    }

    int AddRef() {
        refCount++;
        return refCount;
    }

    int Release() {
        refCount--;
        if (refCount == 0) {
            // The following lines are intentionally commented. We are not providing IAccessibleComponent at this time.
            //			if (objIAccessibleComponent != null)
            //				objIAccessibleComponent.dispose();
            //			objIAccessibleComponent = null;
            // The following lines are intentionally commented. We are not providing IAccessibleImage at this time.
            //			if (objIAccessibleImage != null)
            //				objIAccessibleImage.dispose();
            //			objIAccessibleImage = null;
            for (Relation relation : relations) {
                if (relation != null)
                    relation.Release();
            }
            // TODO: also remove all relations for which 'this' is a target??
        }
        return refCount;
    }

    /* QueryService([in] guidService, [in] riid, [out] ppvObject) */
    int QueryService(long guidService, long riid, long ppvObject) {
        return 0;
    }

    /* IAccessible::accDoDefaultAction([in] varChild) */
    int accDoDefaultAction(long varChild) {
        if (DEBUG)
            print(this.getApi() + ".IAccessible::accDoDefaultAction");
        if (accessibleActionListenersSize() > 0) {
        }
        return 0;
    }

    /* IAccessible::accHitTest([in] xLeft, [in] yTop, [out] pvarChild) */
    int accHitTest(int xLeft, int yTop, long pvarChild) {
        int osChild = ACC.CHILDID_NONE;
        long osChildObject = 0;
        AccessibleControlEvent event = new AccessibleControlEvent(this.getApi());
        event.childID = osChild == ACC.CHILDID_NONE ? ACC.CHILDID_NONE : osToChildID(osChild);
        // TODO: event.accessible = Accessible for osChildObject;
        event.x = xLeft;
        event.y = yTop;
        for (int i = 0; i < accessibleControlListenersSize(); i++) {
            AccessibleControlListener listener = accessibleControlListeners.get(i);
            listener.getChildAtPoint(event);
        }
        Accessible accessible = event.accessible;
        if (accessible != null) {
            ((DartAccessible) accessible.getImpl()).AddRef();
        }
        int childID = event.childID;
        if (childID == ACC.CHILDID_NONE) {
            if (osChildObject != 0) {
            }
        }
        return 0;
    }

    /* IAccessible::accLocation([out] pxLeft, [out] pyTop, [out] pcxWidth, [out] pcyHeight, [in] varChild) */
    int accLocation(long pxLeft, long pyTop, long pcxWidth, long pcyHeight, long varChild) {
        int osLeft = 0, osTop = 0, osWidth = 0, osHeight = 0;
        AccessibleControlEvent event = new AccessibleControlEvent(this.getApi());
        event.x = osLeft;
        event.y = osTop;
        event.width = osWidth;
        event.height = osHeight;
        for (int i = 0; i < accessibleControlListenersSize(); i++) {
            AccessibleControlListener listener = accessibleControlListeners.get(i);
            listener.getLocation(event);
        }
        return 0;
    }

    /* IAccessible::accNavigate([in] navDir, [in] varStart, [out] pvarEndUpAt) */
    int accNavigate(int navDir, long varStart, long pvarEndUpAt) {
        if (DEBUG)
            print(this.getApi() + ".IAccessible::accNavigate");
        return 0;
    }

    // TODO: Consider supporting this in future.
    /* IAccessible::accSelect([in] flagsSelect, [in] varChild) */
    int accSelect(int flagsSelect, long varChild) {
        return 0;
    }

    /* IAccessible::get_accChild([in] varChild, [out] ppdispChild)
	 * Ownership of ppdispChild transfers from callee to caller so reference count on ppdispChild
	 * must be incremented before returning.  The caller is responsible for releasing ppdispChild.
	 */
    int get_accChild(long varChild, long ppdispChild) {
        Accessible osAccessible = null;
        AccessibleControlEvent event = new AccessibleControlEvent(this.getApi());
        for (int i = 0; i < accessibleControlListenersSize(); i++) {
            AccessibleControlListener listener = accessibleControlListeners.get(i);
            listener.getChild(event);
        }
        Accessible accessible = event.accessible;
        if (accessible == null)
            accessible = osAccessible;
        if (accessible != null) {
            ((DartAccessible) accessible.getImpl()).AddRef();
        }
        return 0;
    }

    /* IAccessible::get_accChildCount([out] pcountChildren) */
    int get_accChildCount(long pcountChildren) {
        int osChildCount = 0;
        AccessibleControlEvent event = new AccessibleControlEvent(this.getApi());
        event.childID = ACC.CHILDID_SELF;
        event.detail = osChildCount;
        for (int i = 0; i < accessibleControlListenersSize(); i++) {
            AccessibleControlListener listener = accessibleControlListeners.get(i);
            listener.getChildCount(event);
        }
        return 0;
    }

    /* IAccessible::get_accDefaultAction([in] varChild, [out] pszDefaultAction) */
    int get_accDefaultAction(long varChild, long pszDefaultAction) {
        if (DEBUG)
            print(this.getApi() + ".IAccessible::get_accDefaultAction");
        String osDefaultAction = null;
        AccessibleControlEvent event = new AccessibleControlEvent(this.getApi());
        event.result = osDefaultAction;
        for (int i = 0; i < accessibleControlListenersSize(); i++) {
            AccessibleControlListener listener = accessibleControlListeners.get(i);
            listener.getDefaultAction(event);
        }
        setString(pszDefaultAction, event.result);
        return 0;
    }

    /* IAccessible::get_accDescription([in] varChild, [out] pszDescription) */
    int get_accDescription(long varChild, long pszDescription) {
        String osDescription = null;
        AccessibleEvent event = new AccessibleEvent(this.getApi());
        event.result = osDescription;
        // TEMPORARY CODE
        for (int i = 0; i < accessibleListenersSize(); i++) {
            AccessibleListener listener = accessibleListeners.get(i);
            listener.getDescription(event);
        }
        setString(pszDescription, event.result);
        return 0;
    }

    /* IAccessible::get_accFocus([out] pvarChild)
	 * Ownership of pvarChild transfers from callee to caller so reference count on pvarChild
	 * must be incremented before returning.  The caller is responsible for releasing pvarChild.
	 */
    int get_accFocus(long pvarChild) {
        int osChild = ACC.CHILDID_NONE;
        AccessibleControlEvent event = new AccessibleControlEvent(this.getApi());
        event.childID = osChild == ACC.CHILDID_NONE ? ACC.CHILDID_NONE : osToChildID(osChild);
        for (int i = 0; i < accessibleControlListenersSize(); i++) {
            AccessibleControlListener listener = accessibleControlListeners.get(i);
            listener.getFocus(event);
        }
        Accessible accessible = event.accessible;
        if (accessible != null) {
            ((DartAccessible) accessible.getImpl()).AddRef();
        }
        int childID = event.childID;
        if (childID == ACC.CHILDID_NONE) {
        }
        if (childID == ACC.CHILDID_SELF) {
            AddRef();
        }
        return 0;
    }

    /* IAccessible::get_accHelp([in] varChild, [out] pszHelp) */
    int get_accHelp(long varChild, long pszHelp) {
        if (DEBUG)
            print(this.getApi() + ".IAccessible::get_accHelp");
        String osHelp = null;
        AccessibleEvent event = new AccessibleEvent(this.getApi());
        event.result = osHelp;
        for (int i = 0; i < accessibleListenersSize(); i++) {
            AccessibleListener listener = accessibleListeners.get(i);
            listener.getHelp(event);
        }
        setString(pszHelp, event.result);
        return 0;
    }

    /* IAccessible::get_accHelpTopic([out] pszHelpFile, [in] varChild, [out] pidTopic) */
    int get_accHelpTopic(long pszHelpFile, long varChild, long pidTopic) {
        if (DEBUG)
            print(this.getApi() + ".IAccessible::get_accHelpTopic");
        return 0;
    }

    /* IAccessible::get_accKeyboardShortcut([in] varChild, [out] pszKeyboardShortcut) */
    int get_accKeyboardShortcut(long varChild, long pszKeyboardShortcut) {
        if (DEBUG)
            print(this.getApi() + ".IAccessible::get_accKeyboardShortcut");
        String osKeyboardShortcut = null;
        AccessibleEvent event = new AccessibleEvent(this.getApi());
        event.result = osKeyboardShortcut;
        for (int i = 0; i < accessibleListenersSize(); i++) {
            AccessibleListener listener = accessibleListeners.get(i);
            listener.getKeyboardShortcut(event);
        }
        setString(pszKeyboardShortcut, event.result);
        return 0;
    }

    /* IAccessible::get_accName([in] varChild, [out] pszName) */
    int get_accName(long varChild, long pszName) {
        String osName = null;
        AccessibleEvent event = new AccessibleEvent(this.getApi());
        event.result = osName;
        /*
		* Bug in Windows:  A Text with SWT.SEARCH style uses EM_SETCUEBANNER
		* to set the message text. This text should be used as the control's
		* accessible name, however it is not. The fix is to return the message
		* text here as the accName (unless there is a preceding label).
		*/
        if (control instanceof Text && (control.getStyle() & SWT.SEARCH) != 0 && osName == null) {
            event.result = ((Text) control).getMessage();
        }
        for (int i = 0; i < accessibleListenersSize(); i++) {
            AccessibleListener listener = accessibleListeners.get(i);
            listener.getName(event);
        }
        setString(pszName, event.result);
        return 0;
    }

    /* IAccessible::get_accParent([out] ppdispParent)
	 * Ownership of ppdispParent transfers from callee to caller so reference count on ppdispParent
	 * must be incremented before returning.  The caller is responsible for releasing ppdispParent.
	 */
    int get_accParent(long ppdispParent) {
        if (parent != null) {
            /* For lightweight accessibles, return the accessible's parent. */
            ((DartAccessible) parent.getImpl()).AddRef();
        }
        return 0;
    }

    /* IAccessible::get_accRole([in] varChild, [out] pvarRole) */
    int get_accRole(long varChild, long pvarRole) {
        AccessibleControlEvent event = new AccessibleControlEvent(this.getApi());
        // TEMPORARY CODE
        /* Currently our checkbox table and tree are emulated using state mask images,
		 * so we need to specify 'checkbox' role for the items. */
        if (control instanceof Tree || control instanceof Table) {
        }
        for (int i = 0; i < accessibleControlListenersSize(); i++) {
            AccessibleControlListener listener = accessibleControlListeners.get(i);
            listener.getRole(event);
        }
        return 0;
    }

    /* IAccessible::get_accSelection([out] pvarChildren)
	 * Ownership of pvarChildren transfers from callee to caller so reference count on pvarChildren
	 * must be incremented before returning.  The caller is responsible for releasing pvarChildren.
	 */
    int get_accSelection(long pvarChildren) {
        if (DEBUG)
            print(this.getApi() + ".IAccessible::get_accSelection");
        int osChild = ACC.CHILDID_NONE;
        AccessibleControlEvent event = new AccessibleControlEvent(this.getApi());
        event.childID = osChild;
        for (int i = 0; i < accessibleControlListenersSize(); i++) {
            AccessibleControlListener listener = accessibleControlListeners.get(i);
            listener.getSelection(event);
        }
        Accessible accessible = event.accessible;
        if (accessible != null) {
            ((DartAccessible) accessible.getImpl()).AddRef();
        }
        int childID = event.childID;
        if (childID == ACC.CHILDID_NONE) {
        }
        if (childID == ACC.CHILDID_MULTIPLE) {
            // TODO: return an enumeration for event.children (currently just returns enumeration from proxy)
            //AddRef();
        }
        if (childID == ACC.CHILDID_SELF) {
            AddRef();
        }
        return 0;
    }

    /* IAccessible::get_accState([in] varChild, [out] pvarState) */
    int get_accState(long varChild, long pvarState) {
        int osState = 0;
        boolean grayed = false;
        AccessibleControlEvent event = new AccessibleControlEvent(this.getApi());
        event.detail = osToState(osState);
        // TEMPORARY CODE
        for (int i = 0; i < accessibleControlListenersSize(); i++) {
            AccessibleControlListener listener = accessibleControlListeners.get(i);
            listener.getState(event);
        }
        int state = stateToOs(event.detail);
        if ((state & ACC.STATE_CHECKED) != 0 && grayed) {
        }
        return 0;
    }

    /* IAccessible::get_accValue([in] varChild, [out] pszValue) */
    int get_accValue(long varChild, long pszValue) {
        String osValue = null;
        AccessibleControlEvent event = new AccessibleControlEvent(this.getApi());
        event.result = osValue;
        /*
		* Bug in Windows:  A Text with SWT.SEARCH style uses EM_SETCUEBANNER
		* to set the message text. This text should be used as the control's
		* accessible value when the control does not have focus, however it
		* is not. The fix is to return the message text here as the accValue.
		*/
        if (control instanceof Text && (control.getStyle() & SWT.SEARCH) != 0 && !control.isFocusControl()) {
            event.result = ((Text) control).getMessage();
        }
        for (int i = 0; i < accessibleControlListenersSize(); i++) {
            AccessibleControlListener listener = accessibleControlListeners.get(i);
            listener.getValue(event);
        }
        // empty string is a valid value, so do not test for it
        setString(pszValue, event.result);
        return 0;
    }

    /* put_accName([in] varChild, [in] szName) */
    int put_accName(long varChild, long szName) {
        return 0;
    }

    /* put_accValue([in] varChild, [in] szValue) */
    int put_accValue(long varChild, long szValue) {
        return 0;
    }

    /* IEnumVARIANT methods: Next, Skip, Reset, Clone */
    /* Retrieve the next celt items in the enumeration sequence.
	 * If there are fewer than the requested number of elements left
	 * in the sequence, retrieve the remaining elements.
	 * The number of elements actually retrieved is returned in pceltFetched
	 * (unless the caller passed in NULL for that parameter).
	 */
    /* IEnumVARIANT::Next([in] celt, [out] rgvar, [in, out] pceltFetched)
	 * Ownership of rgvar transfers from callee to caller so reference count on rgvar
	 * must be incremented before returning.  The caller is responsible for releasing rgvar.
	 */
    int Next(int celt, long rgvar, long pceltFetched) {
        if (DEBUG)
            print(this.getApi() + ".IEnumVARIANT::Next");
        if (enumIndex == 0) {
            AccessibleControlEvent event = new AccessibleControlEvent(this.getApi());
            event.childID = ACC.CHILDID_SELF;
            for (int i = 0; i < accessibleControlListenersSize(); i++) {
                AccessibleControlListener listener = accessibleControlListeners.get(i);
                listener.getChildren(event);
            }
            variants = event.children;
        }
        Object[] nextItems = null;
        if (variants != null && celt >= 1) {
            int endIndex = enumIndex + celt - 1;
            if (endIndex > (variants.length - 1))
                endIndex = variants.length - 1;
            if (enumIndex <= endIndex) {
                nextItems = new Object[endIndex - enumIndex + 1];
                for (int i = 0; i < nextItems.length; i++) {
                    Object child = variants[enumIndex];
                    if (child instanceof Integer) {
                        nextItems[i] = Integer.valueOf(childIDToOs(((Integer) child).intValue()));
                    } else {
                        nextItems[i] = child;
                    }
                    enumIndex++;
                }
            }
        }
        if (nextItems != null) {
            for (int i = 0; i < nextItems.length; i++) {
                Object nextItem = nextItems[i];
                if (nextItem instanceof Integer) {
                } else {
                    Accessible accessible = (Accessible) nextItem;
                    ((DartAccessible) accessible.getImpl()).AddRef();
                }
            }
        } else {
        }
        return 0;
    }

    /* IEnumVARIANT::Skip([in] celt) over the specified number of elements in the enumeration sequence. */
    int Skip(int celt) {
        if (DEBUG)
            print(this.getApi() + ".IEnumVARIANT::Skip");
        enumIndex += celt;
        if (enumIndex > (variants.length - 1)) {
            enumIndex = variants.length - 1;
        }
        return 0;
    }

    /* IEnumVARIANT::Reset() the enumeration sequence to the beginning. */
    int Reset() {
        if (DEBUG)
            print(this.getApi() + ".IEnumVARIANT::Reset");
        enumIndex = 0;
        return 0;
    }

    /* IEnumVARIANT::Clone([out] ppEnum)
	 * Ownership of ppEnum transfers from callee to caller so reference count on ppEnum
	 * must be incremented before returning.  The caller is responsible for releasing ppEnum.
	 */
    int Clone(long ppEnum) {
        if (DEBUG)
            print(this.getApi() + ".IEnumVARIANT::Clone");
        AddRef();
        return 0;
    }

    /* IAccessible2::get_nRelations([out] pNRelations) */
    int get_nRelations(long pNRelations) {
        return 0;
    }

    /* IAccessible2::get_relation([in] relationIndex, [out] ppRelation) */
    int get_relation(int relationIndex, long ppRelation) {
        int i = -1;
        for (int type = 0; type < MAX_RELATION_TYPES; type++) {
            Relation relation = relations[type];
            if (relation != null)
                i++;
            if (i == relationIndex) {
                relation.AddRef();
            }
        }
        return 0;
    }

    /* IAccessible2::get_relations([in] maxRelations, [out] ppRelations, [out] pNRelations) */
    int get_relations(int maxRelations, long ppRelations, long pNRelations) {
        int count = 0;
        for (int type = 0; type < MAX_RELATION_TYPES; type++) {
            if (count == maxRelations)
                break;
            Relation relation = relations[type];
            if (relation != null) {
                relation.AddRef();
                count++;
            }
        }
        return 0;
    }

    /* IAccessible2::get_role([out] pRole) */
    int get_role(long pRole) {
        int role = getRole();
        if (role == 0)
            role = getDefaultRole();
        return 0;
    }

    /* IAccessible2::scrollTo([in] scrollType) */
    int scrollTo(int scrollType) {
        if (DEBUG)
            print(this.getApi() + ".IAccessible2::scrollTo");
        return 0;
    }

    /* IAccessible2::scrollToPoint([in] coordinateType, [in] x, [in] y) */
    int scrollToPoint(int coordinateType, int x, int y) {
        if (DEBUG)
            print(this.getApi() + ".IAccessible2::scrollToPoint");
        return 0;
    }

    /* IAccessible2::get_groupPosition([out] pGroupLevel, [out] pSimilarItemsInGroup, [out] pPositionInGroup) */
    int get_groupPosition(long pGroupLevel, long pSimilarItemsInGroup, long pPositionInGroup) {
        AccessibleAttributeEvent event = new AccessibleAttributeEvent(this.getApi());
        event.groupLevel = event.groupCount = event.groupIndex = -1;
        for (int i = 0; i < accessibleAttributeListenersSize(); i++) {
            AccessibleAttributeListener listener = accessibleAttributeListeners.get(i);
            listener.getAttributes(event);
        }
        int groupLevel = (event.groupLevel != -1) ? event.groupLevel : 0;
        int similarItemsInGroup = (event.groupCount != -1) ? event.groupCount : 0;
        int positionInGroup = (event.groupIndex != -1) ? event.groupIndex : 0;
        if (similarItemsInGroup == 0 && positionInGroup == 0) {
            /* Determine position and count for radio buttons. */
            if (control instanceof Button && ((control.getStyle() & SWT.RADIO) != 0)) {
                positionInGroup = 1;
                similarItemsInGroup = 1;
                for (Control child : control.getParent().getChildren()) {
                    if (child instanceof Button && ((child.getStyle() & SWT.RADIO) != 0)) {
                        if (child == control)
                            positionInGroup = similarItemsInGroup;
                        else
                            similarItemsInGroup++;
                    }
                }
            }
        }
        return 0;
    }

    /* IAccessible2::get_states([out] pStates) */
    int get_states(long pStates) {
        AccessibleControlEvent event = new AccessibleControlEvent(this.getApi());
        event.childID = ACC.CHILDID_SELF;
        for (int i = 0; i < accessibleControlListenersSize(); i++) {
            AccessibleControlListener listener = accessibleControlListeners.get(i);
            listener.getState(event);
        }
        /* If the role is text and there are TextExtendedListeners, then set IA2_STATE_EDITABLE.
		 * Note that IA2_STATE_EDITABLE is not the opposite of STATE_READONLY.
		 * Instead, it means: "has a caret, supports IAccessibleText, and is a text editing environment".
		 */
        if (getRole() == ACC.ROLE_TEXT && accessibleTextExtendedListenersSize() > 0) {
        }
        return 0;
    }

    /* IAccessible2::get_extendedRole([out] pbstrExtendedRole) */
    int get_extendedRole(long pbstrExtendedRole) {
        /* This feature is not supported. */
        setString(pbstrExtendedRole, null);
        return 0;
    }

    /* IAccessible2::get_localizedExtendedRole([out] pbstrLocalizedExtendedRole) */
    int get_localizedExtendedRole(long pbstrLocalizedExtendedRole) {
        /* This feature is not supported. */
        setString(pbstrLocalizedExtendedRole, null);
        return 0;
    }

    /* IAccessible2::get_nExtendedStates([out] pNExtendedStates) */
    int get_nExtendedStates(long pNExtendedStates) {
        return 0;
    }

    /* IAccessible2::get_extendedStates([in] maxExtendedStates, [out] ppbstrExtendedStates, [out] pNExtendedStates) */
    int get_extendedStates(int maxExtendedStates, long ppbstrExtendedStates, long pNExtendedStates) {
        /* This feature is not supported. */
        setString(ppbstrExtendedStates, null);
        return 0;
    }

    /* IAccessible2::get_localizedExtendedStates([in] maxLocalizedExtendedStates, [out] ppbstrLocalizedExtendedStates, [out] pNLocalizedExtendedStates) */
    int get_localizedExtendedStates(int maxLocalizedExtendedStates, long ppbstrLocalizedExtendedStates, long pNLocalizedExtendedStates) {
        /* This feature is not supported. */
        setString(ppbstrLocalizedExtendedStates, null);
        return 0;
    }

    /* IAccessible2::get_uniqueID([out] pUniqueID) */
    int get_uniqueID(long pUniqueID) {
        if (uniqueID == -1)
            uniqueID = UniqueID--;
        return 0;
    }

    /* IAccessible2::get_windowHandle([out] pWindowHandle) */
    int get_windowHandle(long pWindowHandle) {
        return 0;
    }

    /* IAccessible2::get_indexInParent([out] pIndexInParent) */
    int get_indexInParent(long pIndexInParent) {
        AccessibleControlEvent event = new AccessibleControlEvent(this.getApi());
        event.childID = ACC.CHILDID_CHILD_INDEX;
        event.detail = -1;
        for (int i = 0; i < accessibleControlListenersSize(); i++) {
            AccessibleControlListener listener = accessibleControlListeners.get(i);
            listener.getChild(event);
        }
        int indexInParent = event.detail;
        if (indexInParent == -1) {
            //			/* The application did not implement CHILDID_CHILD_INDEX,
            //			 * so determine the index by looping through the parent's
            //			 * children looking for this Accessible. This may be slow,
            //			 * so applications are strongly encouraged to implement
            //			 * getChild for CHILDID_CHILD_INDEX.
            //			 */
            //			// TODO: finish this. See also get_groupPosition
            // this won't work because VARIANT.sizeof isn't big enough on 64-bit machines.
            // just create an  long [] ppdispParent - it's not a variant anyhow...
            //			long ppdispParent = OS.GlobalAlloc (OS.GMEM_FIXED | OS.GMEM_ZEROINIT, VARIANT.sizeof);
            //			int code = get_accParent(ppdispParent);
            //			if (code == COM.S_OK) {
            //				VARIANT v = getVARIANT(ppdispParent);
            //				if (v.vt == COM.VT_DISPATCH) {
            //					IAccessible accParent = new IAccessible(v.lVal);
            //					long pcountChildren = OS.GlobalAlloc (OS.GMEM_FIXED | OS.GMEM_ZEROINIT, 4);
            //					code = accParent.get_accChildCount(pcountChildren);
            //					if (code == COM.S_OK) {
            //						int [] childCount = new int[1];
            //						OS.MoveMemory(childCount, pcountChildren, 4);
            //						int[] pcObtained = new int[1];
            //						long rgVarChildren = OS.GlobalAlloc (OS.GMEM_FIXED | OS.GMEM_ZEROINIT, VARIANT.sizeof * childCount[0]);
            //						System.out.println("Asking for AccessibleChildren");
            //						code = COM.AccessibleChildren(accParent.getAddress(), 0, childCount[0], rgVarChildren, pcObtained);
            //						if (code == COM.S_OK) {
            //							System.out.println("Got this far - now what?");
            //						} else {
            //							System.out.println("AccessibleChildren failed? code=" + code);
            //						}
            //						OS.GlobalFree(rgVarChildren);
            //					} else {
            //						System.out.println("get_accChildCount failed? code=" + code);
            //					}
            //					OS.GlobalFree (pcountChildren);
            //				} else {
            //					System.out.println("get_accParent did not return VT_DISPATCH? It returned: " + v.vt);
            //				}
            //				COM.VariantClear(ppdispParent);
            //				OS.GlobalFree (ppdispParent);
            //			} else {
            //				System.out.println("get_accParent failed? code=" + code);
            //			}
        }
        return 0;
    }

    /* IAccessible2::get_locale([out] pLocale) */
    int get_locale(long pLocale) {
        /* Return the default locale for the JVM. */
        Locale locale = Locale.getDefault();
        char[] data = (locale.getLanguage() + "\0").toCharArray();
        data = (locale.getCountry() + "\0").toCharArray();
        data = (locale.getVariant() + "\0").toCharArray();
        return 0;
    }

    /* IAccessible2::get_attributes([out] pbstrAttributes) */
    int get_attributes(long pbstrAttributes) {
        AccessibleAttributeEvent event = new AccessibleAttributeEvent(this.getApi());
        for (int i = 0; i < accessibleAttributeListenersSize(); i++) {
            AccessibleAttributeListener listener = accessibleAttributeListeners.get(i);
            listener.getAttributes(event);
        }
        String attributes = "";
        attributes += "margin-left:" + event.leftMargin + ";";
        attributes += "margin-top:" + event.topMargin + ";";
        attributes += "margin-right:" + event.rightMargin + ";";
        attributes += "margin-bottom:" + event.bottomMargin + ";";
        if (event.tabStops != null) {
            for (int tabStop : event.tabStops) {
                attributes += "tab-stop:position=" + tabStop + ";";
            }
        }
        if (event.justify)
            attributes += "text-align:justify;";
        attributes += "text-align:" + (event.alignment == SWT.LEFT ? "left" : event.alignment == SWT.RIGHT ? "right" : "center") + ";";
        attributes += "text-indent:" + event.indent + ";";
        if (event.attributes != null) {
            for (int i = 0; i + 1 < event.attributes.length; i += 2) {
                attributes += event.attributes[i] + ":" + event.attributes[i + 1] + ";";
            }
        }
        /* If the role is text, then specify the text model for JAWS. */
        if (getRole() == ACC.ROLE_TEXT) {
            attributes += "text-model:a1;";
        }
        setString(pbstrAttributes, attributes);
        return 0;
    }

    /* IAccessibleAction::get_nActions([out] pNActions) */
    int get_nActions(long pNActions) {
        AccessibleActionEvent event = new AccessibleActionEvent(this.getApi());
        for (int i = 0; i < accessibleActionListenersSize(); i++) {
            AccessibleActionListener listener = accessibleActionListeners.get(i);
            listener.getActionCount(event);
        }
        return 0;
    }

    /* IAccessibleAction::doAction([in] actionIndex) */
    int doAction(int actionIndex) {
        AccessibleActionEvent event = new AccessibleActionEvent(this.getApi());
        event.index = actionIndex;
        for (int i = 0; i < accessibleActionListenersSize(); i++) {
            AccessibleActionListener listener = accessibleActionListeners.get(i);
            listener.doAction(event);
        }
        return 0;
    }

    /* IAccessibleAction::get_description([in] actionIndex, [out] pbstrDescription) */
    int get_description(int actionIndex, long pbstrDescription) {
        AccessibleActionEvent event = new AccessibleActionEvent(this.getApi());
        event.index = actionIndex;
        for (int i = 0; i < accessibleActionListenersSize(); i++) {
            AccessibleActionListener listener = accessibleActionListeners.get(i);
            listener.getDescription(event);
        }
        setString(pbstrDescription, event.result);
        return 0;
    }

    /* IAccessibleAction::get_keyBinding([in] actionIndex, [in] nMaxBindings, [out] ppbstrKeyBindings, [out] pNBindings) */
    int get_keyBinding(int actionIndex, int nMaxBindings, long ppbstrKeyBindings, long pNBindings) {
        AccessibleActionEvent event = new AccessibleActionEvent(this.getApi());
        event.index = actionIndex;
        for (int i = 0; i < accessibleActionListenersSize(); i++) {
            AccessibleActionListener listener = accessibleActionListeners.get(i);
            listener.getKeyBinding(event);
        }
        String keyBindings = event.result;
        int length = 0;
        if (keyBindings != null)
            length = keyBindings.length();
        int i = 0, count = 0;
        while (i < length) {
            if (count == nMaxBindings)
                break;
            int j = keyBindings.indexOf(';', i);
            if (j == -1)
                j = length;
            String keyBinding = keyBindings.substring(i, j);
            if (keyBinding.length() > 0) {
                count++;
            }
            i = j + 1;
        }
        if (count == 0) {
            setString(ppbstrKeyBindings, null);
        }
        return 0;
    }

    /* IAccessibleAction::get_name([in] actionIndex, [out] pbstrName) */
    int get_name(int actionIndex, long pbstrName) {
        AccessibleActionEvent event = new AccessibleActionEvent(this.getApi());
        event.index = actionIndex;
        event.localized = false;
        for (int i = 0; i < accessibleActionListenersSize(); i++) {
            AccessibleActionListener listener = accessibleActionListeners.get(i);
            listener.getName(event);
        }
        if (event.result == null || event.result.length() == 0) {
            setString(pbstrName, null);
        }
        setString(pbstrName, event.result);
        return 0;
    }

    /* IAccessibleAction::get_localizedName([in] actionIndex, [out] pbstrLocalizedName) */
    int get_localizedName(int actionIndex, long pbstrLocalizedName) {
        AccessibleActionEvent event = new AccessibleActionEvent(this.getApi());
        event.index = actionIndex;
        event.localized = true;
        for (int i = 0; i < accessibleActionListenersSize(); i++) {
            AccessibleActionListener listener = accessibleActionListeners.get(i);
            listener.getName(event);
        }
        if (event.result == null || event.result.length() == 0) {
            setString(pbstrLocalizedName, null);
        }
        setString(pbstrLocalizedName, event.result);
        return 0;
    }

    /* IAccessibleApplication::get_appName([out] pbstrName) */
    int get_appName(long pbstrName) {
        String appName = SwtDisplay.getAppName();
        if (appName == null || appName.length() == 0) {
            setString(pbstrName, null);
        }
        setString(pbstrName, appName);
        return 0;
    }

    /* IAccessibleApplication::get_appVersion([out] pbstrVersion) */
    int get_appVersion(long pbstrVersion) {
        String appVersion = SwtDisplay.getAppVersion();
        if (appVersion == null || appVersion.length() == 0) {
            setString(pbstrVersion, null);
        }
        setString(pbstrVersion, appVersion);
        return 0;
    }

    /* IAccessibleApplication::get_toolkitName([out] pbstrName) */
    int get_toolkitName(long pbstrName) {
        String toolkitName = "SWT";
        setString(pbstrName, toolkitName);
        return 0;
    }

    /* IAccessibleApplication::get_toolkitVersion([out] pbstrVersion) */
    int get_toolkitVersion(long pbstrVersion) {
        //$NON-NLS-1$
        String toolkitVersion = "" + SWT.getVersion();
        setString(pbstrVersion, toolkitVersion);
        return 0;
    }

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
    /* IAccessibleEditableText::copyText([in] startOffset, [in] endOffset) */
    int copyText(int startOffset, int endOffset) {
        if (DEBUG)
            print(this.getApi() + ".IAccessibleEditableText::copyText, start=" + startOffset + ", end=" + endOffset);
        AccessibleEditableTextEvent event = new AccessibleEditableTextEvent(this.getApi());
        for (int i = 0; i < accessibleEditableTextListenersSize(); i++) {
            AccessibleEditableTextListener listener = accessibleEditableTextListeners.get(i);
            listener.copyText(event);
        }
        return 0;
    }

    /* IAccessibleEditableText::deleteText([in] startOffset, [in] endOffset) */
    int deleteText(int startOffset, int endOffset) {
        if (DEBUG)
            print(this.getApi() + ".IAccessibleEditableText::deleteText, start=" + startOffset + ", end=" + endOffset);
        AccessibleEditableTextEvent event = new AccessibleEditableTextEvent(this.getApi());
        event.string = "";
        for (int i = 0; i < accessibleEditableTextListenersSize(); i++) {
            AccessibleEditableTextListener listener = accessibleEditableTextListeners.get(i);
            listener.replaceText(event);
        }
        return 0;
    }

    /* IAccessibleEditableText::insertText([in] offset, [in] pbstrText) */
    int insertText(int offset, long pbstrText) {
        if (DEBUG)
            print(this.getApi() + ".IAccessibleEditableText::insertText, offset=" + offset + ", pbstrText=" + pbstrText);
        AccessibleEditableTextEvent event = new AccessibleEditableTextEvent(this.getApi());
        event.end = event.start;
        event.string = getString(pbstrText);
        for (int i = 0; i < accessibleEditableTextListenersSize(); i++) {
            AccessibleEditableTextListener listener = accessibleEditableTextListeners.get(i);
            listener.replaceText(event);
        }
        return 0;
    }

    /* IAccessibleEditableText::cutText([in] startOffset, [in] endOffset) */
    int cutText(int startOffset, int endOffset) {
        if (DEBUG)
            print(this.getApi() + ".IAccessibleEditableText::cutText, start=" + startOffset + ", end=" + endOffset);
        AccessibleEditableTextEvent event = new AccessibleEditableTextEvent(this.getApi());
        for (int i = 0; i < accessibleEditableTextListenersSize(); i++) {
            AccessibleEditableTextListener listener = accessibleEditableTextListeners.get(i);
            listener.cutText(event);
        }
        return 0;
    }

    /* IAccessibleEditableText::pasteText([in] offset) */
    int pasteText(int offset) {
        if (DEBUG)
            print(this.getApi() + ".IAccessibleEditableText::pasteText, offset=" + offset);
        AccessibleEditableTextEvent event = new AccessibleEditableTextEvent(this.getApi());
        event.end = event.start;
        for (int i = 0; i < accessibleEditableTextListenersSize(); i++) {
            AccessibleEditableTextListener listener = accessibleEditableTextListeners.get(i);
            listener.pasteText(event);
        }
        return 0;
    }

    /* IAccessibleEditableText::replaceText([in] startOffset, [in] endOffset, [in] pbstrText) */
    int replaceText(int startOffset, int endOffset, long pbstrText) {
        if (DEBUG)
            print(this.getApi() + ".IAccessibleEditableText::replaceText, start=" + startOffset + ", end=" + endOffset + ", pbstrText=" + pbstrText);
        AccessibleEditableTextEvent event = new AccessibleEditableTextEvent(this.getApi());
        event.string = getString(pbstrText);
        for (int i = 0; i < accessibleEditableTextListenersSize(); i++) {
            AccessibleEditableTextListener listener = accessibleEditableTextListeners.get(i);
            listener.replaceText(event);
        }
        return 0;
    }

    /* IAccessibleEditableText::setAttributes([in] startOffset, [in] endOffset, [in] pbstrAttributes) */
    int setAttributes(int startOffset, int endOffset, long pbstrAttributes) {
        if (DEBUG)
            print(this.getApi() + ".IAccessibleEditableText::setAttributes, start=" + startOffset + ", end=" + endOffset + ", pbstrAttributes=" + pbstrAttributes);
        AccessibleTextAttributeEvent event = new AccessibleTextAttributeEvent(this.getApi());
        String string = getString(pbstrAttributes);
        if (string != null && string.length() > 0) {
            TextStyle style = new TextStyle();
            FontData fontData = null;
            // used for default rise
            int points = 10;
            String[] attributes = new String[0];
            int begin = 0;
            int end = string.indexOf(';');
            while (end != -1 && end < string.length()) {
                String keyValue = string.substring(begin, end).trim();
                int colonIndex = keyValue.indexOf(':');
                if (colonIndex != -1 && colonIndex + 1 < keyValue.length()) {
                    String[] newAttributes = new String[attributes.length + 2];
                    System.arraycopy(attributes, 0, newAttributes, 0, attributes.length);
                    newAttributes[attributes.length] = keyValue.substring(0, colonIndex).trim();
                    newAttributes[attributes.length + 1] = keyValue.substring(colonIndex + 1).trim();
                    attributes = newAttributes;
                }
                begin = end + 1;
                end = string.indexOf(';', begin);
            }
            for (int i = 0; i + 1 < attributes.length; i += 2) {
                String key = attributes[i];
                String value = attributes[i + 1];
                if (key.equals("text-position")) {
                    if (value.equals("super"))
                        style.rise = points / 2;
                    else if (value.equals("sub"))
                        style.rise = -points / 2;
                } else if (key.equals("text-underline-type")) {
                    style.underline = true;
                    if (value.equals("double"))
                        style.underlineStyle = SWT.UNDERLINE_DOUBLE;
                    else if (value.equals("single")) {
                        if (style.underlineStyle != SWT.UNDERLINE_SQUIGGLE && style.underlineStyle != SWT.UNDERLINE_ERROR) {
                            style.underlineStyle = SWT.UNDERLINE_SINGLE;
                        }
                    }
                } else if (key.equals("text-underline-style") && value.equals("wave")) {
                    style.underline = true;
                    style.underlineStyle = SWT.UNDERLINE_SQUIGGLE;
                } else if (key.equals("invalid") && value.equals("true")) {
                    style.underline = true;
                    style.underlineStyle = SWT.UNDERLINE_ERROR;
                } else if (key.equals("text-line-through-type")) {
                    if (value.equals("single"))
                        style.strikeout = true;
                } else if (key.equals("font-family")) {
                    if (fontData == null)
                        fontData = new FontData();
                    fontData.setName(value);
                } else if (key.equals("font-size")) {
                    try {
                        String pts = value.endsWith("pt") ? value.substring(0, value.length() - 2) : value;
                        points = Integer.parseInt(pts);
                        if (fontData == null)
                            fontData = new FontData();
                        fontData.setHeight(points);
                        if (style.rise > 0)
                            style.rise = points / 2;
                        else if (style.rise < 0)
                            style.rise = -points / 2;
                    } catch (NumberFormatException ex) {
                    }
                } else if (key.equals("font-style")) {
                    if (value.equals("italic")) {
                        if (fontData == null)
                            fontData = new FontData();
                        fontData.setStyle(fontData.getStyle() | SWT.ITALIC);
                    }
                } else if (key.equals("font-weight")) {
                    if (value.equals("bold")) {
                        if (fontData == null)
                            fontData = new FontData();
                        fontData.setStyle(fontData.getStyle() | SWT.BOLD);
                    } else {
                        try {
                            int weight = Integer.parseInt(value);
                            if (fontData == null)
                                fontData = new FontData();
                            if (weight > 400)
                                fontData.setStyle(fontData.getStyle() | SWT.BOLD);
                        } catch (NumberFormatException ex) {
                        }
                    }
                } else if (key.equals("color")) {
                    style.foreground = colorFromString(value);
                } else if (key.equals("background-color")) {
                    style.background = colorFromString(value);
                }
            }
            if (attributes.length > 0) {
                event.attributes = attributes;
                if (fontData != null) {
                    style.font = new Font(control.getDisplay(), fontData);
                }
                if (!style.equals(new TextStyle()))
                    event.textStyle = style;
            }
            for (int i = 0; i < accessibleEditableTextListenersSize(); i++) {
                AccessibleEditableTextListener listener = accessibleEditableTextListeners.get(i);
                listener.setTextAttributes(event);
            }
            if (style.font != null) {
                style.font.dispose();
            }
            if (style.foreground != null) {
                style.foreground.dispose();
            }
            if (style.background != null) {
                style.background.dispose();
            }
        }
        return 0;
    }

    /* IAccessibleHyperlink::get_anchor([in] index, [out] pAnchor) */
    int get_anchor(int index, long pAnchor) {
        if (DEBUG)
            print(this.getApi() + ".IAccessibleHyperlink::get_anchor");
        AccessibleHyperlinkEvent event = new AccessibleHyperlinkEvent(this.getApi());
        event.index = index;
        for (int i = 0; i < accessibleHyperlinkListenersSize(); i++) {
            AccessibleHyperlinkListener listener = accessibleHyperlinkListeners.get(i);
            listener.getAnchor(event);
        }
        Accessible accessible = event.accessible;
        if (accessible != null) {
            ((DartAccessible) accessible.getImpl()).AddRef();
        }
        setStringVARIANT(pAnchor, event.result);
        return 0;
    }

    /* IAccessibleHyperlink::get_anchorTarget([in] index, [out] pAnchorTarget) */
    int get_anchorTarget(int index, long pAnchorTarget) {
        if (DEBUG)
            print(this.getApi() + ".IAccessibleHyperlink::get_anchorTarget");
        AccessibleHyperlinkEvent event = new AccessibleHyperlinkEvent(this.getApi());
        event.index = index;
        for (int i = 0; i < accessibleHyperlinkListenersSize(); i++) {
            AccessibleHyperlinkListener listener = accessibleHyperlinkListeners.get(i);
            listener.getAnchorTarget(event);
        }
        Accessible accessible = event.accessible;
        if (accessible != null) {
            ((DartAccessible) accessible.getImpl()).AddRef();
        }
        setStringVARIANT(pAnchorTarget, event.result);
        return 0;
    }

    /* IAccessibleHyperlink::get_startIndex([out] pIndex) */
    int get_startIndex(long pIndex) {
        if (DEBUG)
            print(this.getApi() + ".IAccessibleHyperlink::get_startIndex");
        AccessibleHyperlinkEvent event = new AccessibleHyperlinkEvent(this.getApi());
        for (int i = 0; i < accessibleHyperlinkListenersSize(); i++) {
            AccessibleHyperlinkListener listener = accessibleHyperlinkListeners.get(i);
            listener.getStartIndex(event);
        }
        return 0;
    }

    /* IAccessibleHyperlink::get_endIndex([out] pIndex) */
    int get_endIndex(long pIndex) {
        if (DEBUG)
            print(this.getApi() + ".IAccessibleHyperlink::get_endIndex");
        AccessibleHyperlinkEvent event = new AccessibleHyperlinkEvent(this.getApi());
        for (int i = 0; i < accessibleHyperlinkListenersSize(); i++) {
            AccessibleHyperlinkListener listener = accessibleHyperlinkListeners.get(i);
            listener.getEndIndex(event);
        }
        return 0;
    }

    /* IAccessibleHyperlink::get_valid([out] pValid) */
    int get_valid(long pValid) {
        return 0;
    }

    /* IAccessibleHypertext::get_nHyperlinks([out] pHyperlinkCount) */
    int get_nHyperlinks(long pHyperlinkCount) {
        if (DEBUG)
            print(this.getApi() + ".IAccessibleHypertext::get_nHyperlinks");
        AccessibleTextEvent event = new AccessibleTextEvent(this.getApi());
        for (int i = 0; i < accessibleTextExtendedListenersSize(); i++) {
            AccessibleTextExtendedListener listener = accessibleTextExtendedListeners.get(i);
            listener.getHyperlinkCount(event);
        }
        return 0;
    }

    /* IAccessibleHypertext::get_hyperlink([in] index, [out] ppHyperlink) */
    int get_hyperlink(int index, long ppHyperlink) {
        if (DEBUG)
            print(this.getApi() + ".IAccessibleHypertext::get_hyperlink");
        AccessibleTextEvent event = new AccessibleTextEvent(this.getApi());
        event.index = index;
        for (int i = 0; i < accessibleTextExtendedListenersSize(); i++) {
            AccessibleTextExtendedListener listener = accessibleTextExtendedListeners.get(i);
            listener.getHyperlink(event);
        }
        Accessible accessible = event.accessible;
        if (accessible == null) {
        }
        ((DartAccessible) accessible.getImpl()).AddRef();
        return 0;
    }

    /* IAccessibleHypertext::get_hyperlinkIndex([in] charIndex, [out] pHyperlinkIndex) */
    int get_hyperlinkIndex(int charIndex, long pHyperlinkIndex) {
        if (DEBUG)
            print(this.getApi() + ".IAccessibleHypertext::get_hyperlinkIndex");
        AccessibleTextEvent event = new AccessibleTextEvent(this.getApi());
        event.offset = charIndex;
        event.index = -1;
        for (int i = 0; i < accessibleTextExtendedListenersSize(); i++) {
            AccessibleTextExtendedListener listener = accessibleTextExtendedListeners.get(i);
            listener.getHyperlinkIndex(event);
        }
        return 0;
    }

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
    /* IAccessibleTable2::get_cellAt([in] row, [in] column, [out] ppCell) */
    int get_cellAt(int row, int column, long ppCell) {
        AccessibleTableEvent event = new AccessibleTableEvent(this.getApi());
        event.row = row;
        event.column = column;
        for (int i = 0; i < accessibleTableListenersSize(); i++) {
            AccessibleTableListener listener = accessibleTableListeners.get(i);
            listener.getCell(event);
        }
        Accessible accessible = event.accessible;
        if (DEBUG)
            print(this.getApi() + ".IAccessibleTable2::get_cellAt(row=" + row + ", column=" + column + ") returning " + accessible);
        ((DartAccessible) accessible.getImpl()).AddRef();
        return 0;
    }

    /* IAccessibleTable2::get_caption([out] ppAccessible) */
    int get_caption(long ppAccessible) {
        AccessibleTableEvent event = new AccessibleTableEvent(this.getApi());
        for (int i = 0; i < accessibleTableListenersSize(); i++) {
            AccessibleTableListener listener = accessibleTableListeners.get(i);
            listener.getCaption(event);
        }
        Accessible accessible = event.accessible;
        if (DEBUG)
            print(this.getApi() + ".IAccessibleTable2::get_caption() returning " + accessible);
        if (accessible == null) {
        }
        ((DartAccessible) accessible.getImpl()).AddRef();
        return 0;
    }

    /* IAccessibleTable2::get_columnDescription([in] column, [out] pbstrDescription) */
    int get_columnDescription(int column, long pbstrDescription) {
        AccessibleTableEvent event = new AccessibleTableEvent(this.getApi());
        event.column = column;
        for (int i = 0; i < accessibleTableListenersSize(); i++) {
            AccessibleTableListener listener = accessibleTableListeners.get(i);
            listener.getColumnDescription(event);
        }
        if (DEBUG)
            print(this.getApi() + ".IAccessibleTable2::get_columnDescription(column=" + column + ") returning " + event.result);
        setString(pbstrDescription, event.result);
        return 0;
    }

    /* IAccessibleTable2::get_nColumns([out] pColumnCount) */
    int get_nColumns(long pColumnCount) {
        AccessibleTableEvent event = new AccessibleTableEvent(this.getApi());
        for (int i = 0; i < accessibleTableListenersSize(); i++) {
            AccessibleTableListener listener = accessibleTableListeners.get(i);
            listener.getColumnCount(event);
        }
        if (DEBUG)
            print(this.getApi() + ".IAccessibleTable2::get_nColumns() returning " + event.count);
        return 0;
    }

    /* IAccessibleTable2::get_nRows([out] pRowCount) */
    int get_nRows(long pRowCount) {
        AccessibleTableEvent event = new AccessibleTableEvent(this.getApi());
        for (int i = 0; i < accessibleTableListenersSize(); i++) {
            AccessibleTableListener listener = accessibleTableListeners.get(i);
            listener.getRowCount(event);
        }
        if (DEBUG)
            print(this.getApi() + ".IAccessibleTable2::get_nRows() returning " + event.count);
        return 0;
    }

    /* IAccessibleTable2::get_nSelectedCells([out] pCellCount) */
    int get_nSelectedCells(long pCellCount) {
        AccessibleTableEvent event = new AccessibleTableEvent(this.getApi());
        for (int i = 0; i < accessibleTableListenersSize(); i++) {
            AccessibleTableListener listener = accessibleTableListeners.get(i);
            listener.getSelectedCellCount(event);
        }
        if (DEBUG)
            print(this.getApi() + ".IAccessibleTable2::get_nSelectedCells() returning " + event.count);
        return 0;
    }

    /* IAccessibleTable2::get_nSelectedColumns([out] pColumnCount) */
    int get_nSelectedColumns(long pColumnCount) {
        AccessibleTableEvent event = new AccessibleTableEvent(this.getApi());
        for (int i = 0; i < accessibleTableListenersSize(); i++) {
            AccessibleTableListener listener = accessibleTableListeners.get(i);
            listener.getSelectedColumnCount(event);
        }
        if (DEBUG)
            print(this.getApi() + ".IAccessibleTable2::get_nSelectedColumns() returning " + event.count);
        return 0;
    }

    /* IAccessibleTable2::get_nSelectedRows([out] pRowCount) */
    int get_nSelectedRows(long pRowCount) {
        AccessibleTableEvent event = new AccessibleTableEvent(this.getApi());
        for (int i = 0; i < accessibleTableListenersSize(); i++) {
            AccessibleTableListener listener = accessibleTableListeners.get(i);
            listener.getSelectedRowCount(event);
        }
        if (DEBUG)
            print(this.getApi() + ".IAccessibleTable2::get_nSelectedRows() returning " + event.count);
        return 0;
    }

    /* IAccessibleTable2::get_rowDescription([in] row, [out] pbstrDescription) */
    int get_rowDescription(int row, long pbstrDescription) {
        AccessibleTableEvent event = new AccessibleTableEvent(this.getApi());
        event.row = row;
        for (int i = 0; i < accessibleTableListenersSize(); i++) {
            AccessibleTableListener listener = accessibleTableListeners.get(i);
            listener.getRowDescription(event);
        }
        if (DEBUG)
            print(this.getApi() + ".IAccessibleTable2::get_rowDescription(row=" + row + ") returning " + event.result);
        setString(pbstrDescription, event.result);
        return 0;
    }

    /* IAccessibleTable2::get_selectedCells([out] ppCells, [out] pNSelectedCells) */
    int get_selectedCells(long ppCells, long pNSelectedCells) {
        AccessibleTableEvent event = new AccessibleTableEvent(this.getApi());
        for (int i = 0; i < accessibleTableListenersSize(); i++) {
            AccessibleTableListener listener = accessibleTableListeners.get(i);
            listener.getSelectedCells(event);
        }
        if (DEBUG)
            print(this.getApi() + ".IAccessibleTable2::get_selectedCells() returning " + (event.accessibles == null ? "null" : "accessibles[" + event.accessibles.length + "]"));
        if (event.accessibles == null || event.accessibles.length == 0) {
        }
        int length = event.accessibles.length;
        int count = 0;
        for (int i = 0; i < length; i++) {
            Accessible accessible = event.accessibles[i];
            if (accessible != null) {
                ((DartAccessible) accessible.getImpl()).AddRef();
                count++;
            }
        }
        return 0;
    }

    /* IAccessibleTable2::get_selectedColumns([out] ppSelectedColumns, [out] pNColumns) */
    int get_selectedColumns(long ppSelectedColumns, long pNColumns) {
        AccessibleTableEvent event = new AccessibleTableEvent(this.getApi());
        for (int i = 0; i < accessibleTableListenersSize(); i++) {
            AccessibleTableListener listener = accessibleTableListeners.get(i);
            listener.getSelectedColumns(event);
        }
        int count = event.selected == null ? 0 : event.selected.length;
        if (DEBUG)
            print(this.getApi() + ".IAccessibleTable2::get_selectedColumns() returning " + (count == 0 ? "null" : "selected[" + count + "]"));
        if (count == 0) {
        }
        return 0;
    }

    /* IAccessibleTable2::get_selectedRows([out] ppSelectedRows, [out] pNRows) */
    int get_selectedRows(long ppSelectedRows, long pNRows) {
        AccessibleTableEvent event = new AccessibleTableEvent(this.getApi());
        for (int i = 0; i < accessibleTableListenersSize(); i++) {
            AccessibleTableListener listener = accessibleTableListeners.get(i);
            listener.getSelectedRows(event);
        }
        int count = event.selected == null ? 0 : event.selected.length;
        if (DEBUG)
            print(this.getApi() + ".IAccessibleTable2::get_selectedRows() returning " + (count == 0 ? "null" : "selected[" + count + "]"));
        if (count == 0) {
        }
        return 0;
    }

    /* IAccessibleTable2::get_summary([out] ppAccessible) */
    int get_summary(long ppAccessible) {
        AccessibleTableEvent event = new AccessibleTableEvent(this.getApi());
        for (int i = 0; i < accessibleTableListenersSize(); i++) {
            AccessibleTableListener listener = accessibleTableListeners.get(i);
            listener.getSummary(event);
        }
        Accessible accessible = event.accessible;
        if (DEBUG)
            print(this.getApi() + ".IAccessibleTable2::get_summary() returning " + accessible);
        if (accessible == null) {
        }
        ((DartAccessible) accessible.getImpl()).AddRef();
        return 0;
    }

    /* IAccessibleTable2::get_isColumnSelected([in] column, [out] pIsSelected) */
    int get_isColumnSelected(int column, long pIsSelected) {
        AccessibleTableEvent event = new AccessibleTableEvent(this.getApi());
        event.column = column;
        for (int i = 0; i < accessibleTableListenersSize(); i++) {
            AccessibleTableListener listener = accessibleTableListeners.get(i);
            listener.isColumnSelected(event);
        }
        if (DEBUG)
            print(this.getApi() + ".IAccessibleTable2::get_isColumnSelected() returning " + event.isSelected);
        return 0;
    }

    /* IAccessibleTable2::get_isRowSelected([in] row, [out] pIsSelected) */
    int get_isRowSelected(int row, long pIsSelected) {
        AccessibleTableEvent event = new AccessibleTableEvent(this.getApi());
        event.row = row;
        for (int i = 0; i < accessibleTableListenersSize(); i++) {
            AccessibleTableListener listener = accessibleTableListeners.get(i);
            listener.isRowSelected(event);
        }
        if (DEBUG)
            print(this.getApi() + ".IAccessibleTable2::get_isRowSelected() returning " + event.isSelected);
        return 0;
    }

    /* IAccessibleTable2::selectRow([in] row) */
    int selectRow(int row) {
        AccessibleTableEvent event = new AccessibleTableEvent(this.getApi());
        event.row = row;
        for (int i = 0; i < accessibleTableListenersSize(); i++) {
            AccessibleTableListener listener = accessibleTableListeners.get(i);
            listener.setSelectedRow(event);
        }
        if (DEBUG)
            print(this.getApi() + ".IAccessibleTable2::selectRow() returning " + (event.result == null ? "E_INVALIDARG" : event.result));
        return 0;
    }

    /* IAccessibleTable2::selectColumn([in] column) */
    int selectColumn(int column) {
        AccessibleTableEvent event = new AccessibleTableEvent(this.getApi());
        event.column = column;
        for (int i = 0; i < accessibleTableListenersSize(); i++) {
            AccessibleTableListener listener = accessibleTableListeners.get(i);
            listener.setSelectedColumn(event);
        }
        if (DEBUG)
            print(this.getApi() + ".IAccessibleTable2::selectColumn() returning " + (event.result == null ? "E_INVALIDARG" : event.result));
        return 0;
    }

    /* IAccessibleTable2::unselectRow([in] row) */
    int unselectRow(int row) {
        AccessibleTableEvent event = new AccessibleTableEvent(this.getApi());
        event.row = row;
        for (int i = 0; i < accessibleTableListenersSize(); i++) {
            AccessibleTableListener listener = accessibleTableListeners.get(i);
            listener.deselectRow(event);
        }
        if (DEBUG)
            print(this.getApi() + ".IAccessibleTable2::unselectRow() returning " + (event.result == null ? "E_INVALIDARG" : event.result));
        return 0;
    }

    /* IAccessibleTable2::unselectColumn([in] column) */
    int unselectColumn(int column) {
        AccessibleTableEvent event = new AccessibleTableEvent(this.getApi());
        event.column = column;
        for (int i = 0; i < accessibleTableListenersSize(); i++) {
            AccessibleTableListener listener = accessibleTableListeners.get(i);
            listener.deselectColumn(event);
        }
        if (DEBUG)
            print(this.getApi() + ".IAccessibleTable2::unselectColumn() returning " + (event.result == null ? "E_INVALIDARG" : event.result));
        return 0;
    }

    /* IAccessibleTable2::get_modelChange([out] pModelChange) */
    int get_modelChange(long pModelChange) {
        if (DEBUG)
            print(this.getApi() + ".IAccessibleTable2::get_modelChange() returning " + (tableChange == null ? "null" : "tableChange=" + tableChange[0] + ", " + tableChange[1] + ", " + tableChange[2] + ", " + tableChange[3]));
        if (tableChange == null) {
        }
        return 0;
    }

    /* IAccessibleTableCell::get_columnExtent([out] pNColumnsSpanned) */
    int get_columnExtent(long pNColumnsSpanned) {
        AccessibleTableCellEvent event = new AccessibleTableCellEvent(this.getApi());
        for (int i = 0; i < accessibleTableCellListenersSize(); i++) {
            AccessibleTableCellListener listener = accessibleTableCellListeners.get(i);
            listener.getColumnSpan(event);
        }
        if (DEBUG)
            print(this.getApi() + ".IAccessibleTableCell::get_columnExtent() returning " + event.count);
        return 0;
    }

    /* IAccessibleTableCell::get_columnHeaderCells([out] ppCellAccessibles, [out] pNColumnHeaderCells) */
    int get_columnHeaderCells(long ppCellAccessibles, long pNColumnHeaderCells) {
        AccessibleTableCellEvent event = new AccessibleTableCellEvent(this.getApi());
        for (int i = 0; i < accessibleTableCellListenersSize(); i++) {
            AccessibleTableCellListener listener = accessibleTableCellListeners.get(i);
            listener.getColumnHeaders(event);
        }
        if (DEBUG)
            print(this.getApi() + ".IAccessibleTableCell::get_columnHeaderCells() returning " + (event.accessibles == null ? "null" : "accessibles[" + event.accessibles.length + "]"));
        if (event.accessibles == null || event.accessibles.length == 0) {
        }
        int length = event.accessibles.length;
        int count = 0;
        for (int i = 0; i < length; i++) {
            Accessible accessible = event.accessibles[i];
            if (accessible != null) {
                ((DartAccessible) accessible.getImpl()).AddRef();
                count++;
            }
        }
        return 0;
    }

    /* IAccessibleTableCell::get_columnIndex([out] pColumnIndex) */
    int get_columnIndex(long pColumnIndex) {
        AccessibleTableCellEvent event = new AccessibleTableCellEvent(this.getApi());
        for (int i = 0; i < accessibleTableCellListenersSize(); i++) {
            AccessibleTableCellListener listener = accessibleTableCellListeners.get(i);
            listener.getColumnIndex(event);
        }
        if (DEBUG)
            print(this.getApi() + ".IAccessibleTableCell::get_columnIndex() returning " + event.index);
        return 0;
    }

    /* IAccessibleTableCell::get_rowExtent([out] pNRowsSpanned) */
    int get_rowExtent(long pNRowsSpanned) {
        AccessibleTableCellEvent event = new AccessibleTableCellEvent(this.getApi());
        for (int i = 0; i < accessibleTableCellListenersSize(); i++) {
            AccessibleTableCellListener listener = accessibleTableCellListeners.get(i);
            listener.getRowSpan(event);
        }
        if (DEBUG)
            print(this.getApi() + ".IAccessibleTableCell::get_rowExtent() returning " + event.count);
        return 0;
    }

    /* IAccessibleTableCell::get_rowHeaderCells([out] ppCellAccessibles, [out] pNRowHeaderCells) */
    int get_rowHeaderCells(long ppCellAccessibles, long pNRowHeaderCells) {
        AccessibleTableCellEvent event = new AccessibleTableCellEvent(this.getApi());
        for (int i = 0; i < accessibleTableCellListenersSize(); i++) {
            AccessibleTableCellListener listener = accessibleTableCellListeners.get(i);
            listener.getRowHeaders(event);
        }
        if (DEBUG)
            print(this.getApi() + ".IAccessibleTableCell::get_rowHeaderCells() returning " + (event.accessibles == null ? "null" : "accessibles[" + event.accessibles.length + "]"));
        if (event.accessibles == null || event.accessibles.length == 0) {
        }
        int length = event.accessibles.length;
        int count = 0;
        for (int i = 0; i < length; i++) {
            Accessible accessible = event.accessibles[i];
            if (accessible != null) {
                ((DartAccessible) accessible.getImpl()).AddRef();
                count++;
            }
        }
        return 0;
    }

    /* IAccessibleTableCell::get_rowIndex([out] pRowIndex) */
    int get_rowIndex(long pRowIndex) {
        AccessibleTableCellEvent event = new AccessibleTableCellEvent(this.getApi());
        for (int i = 0; i < accessibleTableCellListenersSize(); i++) {
            AccessibleTableCellListener listener = accessibleTableCellListeners.get(i);
            listener.getRowIndex(event);
        }
        if (DEBUG)
            print(this.getApi() + ".IAccessibleTableCell::get_rowIndex() returning " + event.index);
        return 0;
    }

    /* IAccessibleTableCell::get_isSelected([out] pIsSelected) */
    int get_isSelected(long pIsSelected) {
        AccessibleTableCellEvent event = new AccessibleTableCellEvent(this.getApi());
        for (int i = 0; i < accessibleTableCellListenersSize(); i++) {
            AccessibleTableCellListener listener = accessibleTableCellListeners.get(i);
            listener.isSelected(event);
        }
        if (DEBUG)
            print(this.getApi() + ".IAccessibleTableCell::get_isSelected() returning " + event.isSelected);
        return 0;
    }

    /* IAccessibleTableCell::get_rowColumnExtents([out] pRow, [out] pColumn, [out] pRowExtents, [out] pColumnExtents, [out] pIsSelected) */
    int get_rowColumnExtents(long pRow, long pColumn, long pRowExtents, long pColumnExtents, long pIsSelected) {
        if (DEBUG)
            print(this.getApi() + ".IAccessibleTableCell::get_rowColumnExtents");
        //		AccessibleTableCellEvent event = new AccessibleTableCellEvent(this);
        //		for (int i = 0; i < accessibleTableCellListenersSize(); i++) {
        //			AccessibleTableCellListener listener = (AccessibleTableCellListener) accessibleTableCellListeners.get(i);
        //			listener.getRowColumnExtents(event);
        //		}
        //		COM.MoveMemory(pRow, new int [] { event.row }, 4);
        //		COM.MoveMemory(pColumn, new int [] { event.column }, 4);
        //		COM.MoveMemory(pRowExtents, new int [] { event.rowExtents }, 4);
        //		COM.MoveMemory(pColumnExtents, new int [] { event.columnExtents }, 4);
        //		return COM.S_OK;
        return 0;
    }

    /* IAccessibleTableCell::get_table([out] ppTable) */
    int get_table(long ppTable) {
        AccessibleTableCellEvent event = new AccessibleTableCellEvent(this.getApi());
        for (int i = 0; i < accessibleTableCellListenersSize(); i++) {
            AccessibleTableCellListener listener = accessibleTableCellListeners.get(i);
            listener.getTable(event);
        }
        Accessible accessible = event.accessible;
        if (DEBUG)
            print(this.getApi() + ".IAccessibleTableCell::get_table() returning " + accessible);
        if (accessible == null) {
        }
        ((DartAccessible) accessible.getImpl()).AddRef();
        return 0;
    }

    /* IAccessibleText::addSelection([in] startOffset, [in] endOffset) */
    int addSelection(int startOffset, int endOffset) {
        if (DEBUG)
            print(this.getApi() + ".IAccessibleText::addSelection(" + startOffset + ", " + endOffset + ")");
        AccessibleTextEvent event = new AccessibleTextEvent(this.getApi());
        for (int i = 0; i < accessibleTextExtendedListenersSize(); i++) {
            AccessibleTextExtendedListener listener = accessibleTextExtendedListeners.get(i);
            listener.addSelection(event);
        }
        return 0;
    }

    /* IAccessibleText::get_attributes([in] offset, [out] pStartOffset, [out] pEndOffset, [out] pbstrTextAttributes) */
    int get_attributes(int offset, long pStartOffset, long pEndOffset, long pbstrTextAttributes) {
        AccessibleTextAttributeEvent event = new AccessibleTextAttributeEvent(this.getApi());
        for (int i = 0; i < accessibleAttributeListenersSize(); i++) {
            AccessibleAttributeListener listener = accessibleAttributeListeners.get(i);
            listener.getTextAttributes(event);
        }
        String textAttributes = "";
        TextStyle style = event.textStyle;
        if (style != null) {
            if (style.rise != 0) {
                textAttributes += "text-position:";
                if (style.rise > 0)
                    textAttributes += "super";
                else
                    textAttributes += "sub";
            }
            if (style.underline) {
                textAttributes += "text-underline-type:";
                switch(style.underlineStyle) {
                    case SWT.UNDERLINE_SINGLE:
                        textAttributes += "single;";
                        break;
                    case SWT.UNDERLINE_DOUBLE:
                        textAttributes += "double;";
                        break;
                    case SWT.UNDERLINE_SQUIGGLE:
                        textAttributes += "single;text-underline-style:wave;";
                        break;
                    case SWT.UNDERLINE_ERROR:
                        textAttributes += "single;text-underline-style:wave;invalid:true;";
                        break;
                    default:
                        textAttributes += "none;";
                        break;
                }
                // style.underlineColor is not currently part of the IA2 spec. If provided, it would be "text-underline-color:rgb(n,n,n);"
            }
            if (style.strikeout) {
                textAttributes += "text-line-through-type:single;";
                // style.strikeoutColor is not currently part of the IA2 spec. If provided, it would be "text-line-through-color:rgb(n,n,n);"
            }
            Font font = style.font;
            if (font != null && !font.isDisposed()) {
                FontData fontData = font.getFontData()[0];
                textAttributes += "font-family:" + fontData.getName() + ";";
                textAttributes += "font-size:" + fontData.getHeight() + "pt;";
            }
            Color color = style.foreground;
            if (color != null && !color.isDisposed()) {
                textAttributes += "color:rgb(" + color.getRed() + "," + color.getGreen() + "," + color.getBlue() + ");";
            }
            color = style.background;
            if (color != null && !color.isDisposed()) {
                textAttributes += "background-color:rgb(" + color.getRed() + "," + color.getGreen() + "," + color.getBlue() + ");";
            }
        }
        if (event.attributes != null) {
            for (int i = 0; i + 1 < event.attributes.length; i += 2) {
                textAttributes += event.attributes[i] + ":" + event.attributes[i + 1] + ";";
            }
        }
        if (DEBUG)
            print(this.getApi() + ".IAccessibleText::get_attributes(" + offset + ") returning start = " + event.start + ", end = " + event.end + ", attributes = " + textAttributes);
        setString(pbstrTextAttributes, textAttributes);
        return 0;
    }

    /* IAccessibleText::get_caretOffset([out] pOffset) */
    int get_caretOffset(long pOffset) {
        return 0;
    }

    /* IAccessibleText::get_characterExtents([in] offset, [in] coordType, [out] pX, [out] pY, [out] pWidth, [out] pHeight) */
    int get_characterExtents(int offset, int coordType, long pX, long pY, long pWidth, long pHeight) {
        AccessibleTextEvent event = new AccessibleTextEvent(this.getApi());
        for (int i = 0; i < accessibleTextExtendedListenersSize(); i++) {
            AccessibleTextExtendedListener listener = accessibleTextExtendedListeners.get(i);
            listener.getTextBounds(event);
        }
        /* Note: event.rectangles is not used here, because IAccessibleText::get_characterExtents is just for one character. */
        if (DEBUG)
            print(this.getApi() + ".IAccessibleText::get_characterExtents(" + offset + ") returning " + event.x + ", " + event.y + ", " + event.width + ", " + event.height);
        return 0;
    }

    /* IAccessibleText::get_nSelections([out] pNSelections) */
    int get_nSelections(long pNSelections) {
        AccessibleTextEvent event = new AccessibleTextEvent(this.getApi());
        event.count = -1;
        for (int i = 0; i < accessibleTextExtendedListenersSize(); i++) {
            AccessibleTextExtendedListener listener = accessibleTextExtendedListeners.get(i);
            listener.getSelectionCount(event);
        }
        if (event.count == -1) {
            event.childID = ACC.CHILDID_SELF;
            event.offset = -1;
            event.length = 0;
            for (int i = 0; i < accessibleTextListenersSize(); i++) {
                AccessibleTextListener listener = accessibleTextListeners.get(i);
                listener.getSelectionRange(event);
            }
            event.count = event.offset != -1 && event.length > 0 ? 1 : 0;
        }
        if (DEBUG)
            print(this.getApi() + ".IAccessibleText::get_nSelections returning " + event.count);
        return 0;
    }

    /* IAccessibleText::get_offsetAtPoint([in] x, [in] y, [in] coordType, [out] pOffset) */
    int get_offsetAtPoint(int x, int y, int coordType, long pOffset) {
        AccessibleTextEvent event = new AccessibleTextEvent(this.getApi());
        event.x = x;
        event.y = y;
        event.offset = -1;
        for (int i = 0; i < accessibleTextExtendedListenersSize(); i++) {
            AccessibleTextExtendedListener listener = accessibleTextExtendedListeners.get(i);
            listener.getOffsetAtPoint(event);
        }
        return 0;
    }

    /* IAccessibleText::get_selection([in] selectionIndex, [out] pStartOffset, [out] pEndOffset) */
    int get_selection(int selectionIndex, long pStartOffset, long pEndOffset) {
        AccessibleTextEvent event = new AccessibleTextEvent(this.getApi());
        event.index = selectionIndex;
        event.start = -1;
        event.end = -1;
        for (int i = 0; i < accessibleTextExtendedListenersSize(); i++) {
            AccessibleTextExtendedListener listener = accessibleTextExtendedListeners.get(i);
            listener.getSelection(event);
        }
        if (event.start == -1 && selectionIndex == 0) {
            event.childID = ACC.CHILDID_SELF;
            event.offset = -1;
            event.length = 0;
            for (int i = 0; i < accessibleTextListenersSize(); i++) {
                AccessibleTextListener listener = accessibleTextListeners.get(i);
                listener.getSelectionRange(event);
            }
            event.start = event.offset;
            event.end = event.offset + event.length;
        }
        if (DEBUG)
            print(this.getApi() + ".IAccessibleText::get_selection(" + selectionIndex + ") returning " + event.start + ", " + event.end);
        return 0;
    }

    /* IAccessibleText::get_text([in] startOffset, [in] endOffset, [out] pbstrText) */
    int get_text(int startOffset, int endOffset, long pbstrText) {
        AccessibleTextEvent event = new AccessibleTextEvent(this.getApi());
        if (event.start > event.end) {
            /* IA2 spec says that indices can be exchanged. */
            int temp = event.start;
            event.start = event.end;
            event.end = temp;
        }
        event.count = 0;
        event.type = ACC.TEXT_BOUNDARY_ALL;
        for (int i = 0; i < accessibleTextExtendedListenersSize(); i++) {
            AccessibleTextExtendedListener listener = accessibleTextExtendedListeners.get(i);
            listener.getText(event);
        }
        if (event.result == null) {
            AccessibleControlEvent e = new AccessibleControlEvent(this.getApi());
            e.childID = ACC.CHILDID_SELF;
            for (int i = 0; i < accessibleControlListenersSize(); i++) {
                AccessibleControlListener listener = accessibleControlListeners.get(i);
                listener.getRole(e);
                listener.getValue(e);
            }
            // TODO: Consider passing the value through for other roles as well (i.e. combo, etc). Keep in sync with get_nCharacters.
            if (e.detail == ACC.ROLE_TEXT) {
                event.result = e.result;
            }
        }
        setString(pbstrText, event.result);
        return 0;
    }

    /* IAccessibleText::get_textBeforeOffset([in] offset, [in] boundaryType, [out] pStartOffset, [out] pEndOffset, [out] pbstrText) */
    int get_textBeforeOffset(int offset, int boundaryType, long pStartOffset, long pEndOffset, long pbstrText) {
        AccessibleTextEvent event = new AccessibleTextEvent(this.getApi());
        int charCount = getCharacterCount();
        event.end = event.start;
        event.count = -1;
        for (int i = 0; i < accessibleTextExtendedListenersSize(); i++) {
            AccessibleTextExtendedListener listener = accessibleTextExtendedListeners.get(i);
            listener.getText(event);
        }
        if (event.end < charCount) {
        }
        setString(pbstrText, event.result);
        return 0;
    }

    /* IAccessibleText::get_textAfterOffset([in] offset, [in] boundaryType, [out] pStartOffset, [out] pEndOffset, [out] pbstrText) */
    int get_textAfterOffset(int offset, int boundaryType, long pStartOffset, long pEndOffset, long pbstrText) {
        AccessibleTextEvent event = new AccessibleTextEvent(this.getApi());
        int charCount = getCharacterCount();
        event.end = event.start;
        event.count = 1;
        for (int i = 0; i < accessibleTextExtendedListenersSize(); i++) {
            AccessibleTextExtendedListener listener = accessibleTextExtendedListeners.get(i);
            listener.getText(event);
        }
        if (event.end < charCount) {
        }
        setString(pbstrText, event.result);
        return 0;
    }

    /* IAccessibleText::get_textAtOffset([in] offset, [in] boundaryType, [out] pStartOffset, [out] pEndOffset, [out] pbstrText) */
    int get_textAtOffset(int offset, int boundaryType, long pStartOffset, long pEndOffset, long pbstrText) {
        AccessibleTextEvent event = new AccessibleTextEvent(this.getApi());
        int charCount = getCharacterCount();
        event.end = event.start;
        event.count = 0;
        for (int i = 0; i < accessibleTextExtendedListenersSize(); i++) {
            AccessibleTextExtendedListener listener = accessibleTextExtendedListeners.get(i);
            listener.getText(event);
        }
        if (event.end < charCount) {
        }
        setString(pbstrText, event.result);
        return 0;
    }

    /* IAccessibleText::removeSelection([in] selectionIndex) */
    int removeSelection(int selectionIndex) {
        AccessibleTextEvent event = new AccessibleTextEvent(this.getApi());
        event.index = selectionIndex;
        for (int i = 0; i < accessibleTextExtendedListenersSize(); i++) {
            AccessibleTextExtendedListener listener = accessibleTextExtendedListeners.get(i);
            listener.removeSelection(event);
        }
        return 0;
    }

    /* IAccessibleText::setCaretOffset([in] offset) */
    int setCaretOffset(int offset) {
        AccessibleTextEvent event = new AccessibleTextEvent(this.getApi());
        for (int i = 0; i < accessibleTextExtendedListenersSize(); i++) {
            AccessibleTextExtendedListener listener = accessibleTextExtendedListeners.get(i);
            listener.setCaretOffset(event);
        }
        return 0;
    }

    /* IAccessibleText::setSelection([in] selectionIndex, [in] startOffset, [in] endOffset) */
    int setSelection(int selectionIndex, int startOffset, int endOffset) {
        AccessibleTextEvent event = new AccessibleTextEvent(this.getApi());
        event.index = selectionIndex;
        for (int i = 0; i < accessibleTextExtendedListenersSize(); i++) {
            AccessibleTextExtendedListener listener = accessibleTextExtendedListeners.get(i);
            listener.setSelection(event);
        }
        if (DEBUG)
            print(this.getApi() + ".IAccessibleText::setSelection(index=" + selectionIndex + ", start=" + event.start + ", end=" + event.end + ") returning " + (event.result.equals(ACC.OK) ? "OK" : "INVALIDARG"));
        return 0;
    }

    /* IAccessibleText::get_nCharacters([out] pNCharacters) */
    int get_nCharacters(long pNCharacters) {
        int count = getCharacterCount();
        if (DEBUG)
            print(this.getApi() + ".IAccessibleText::get_nCharacters returning " + count);
        return 0;
    }

    /* IAccessibleText::scrollSubstringTo([in] startIndex, [in] endIndex, [in] scrollType) */
    int scrollSubstringTo(int startIndex, int endIndex, int scrollType) {
        if (DEBUG)
            print(this.getApi() + ".IAccessibleText::scrollSubstringTo");
        AccessibleTextEvent event = new AccessibleTextEvent(this.getApi());
        event.start = startIndex;
        event.end = endIndex;
        for (int i = 0; i < accessibleTextExtendedListenersSize(); i++) {
            AccessibleTextExtendedListener listener = accessibleTextExtendedListeners.get(i);
            listener.scrollText(event);
        }
        return 0;
    }

    /* IAccessibleText::scrollSubstringToPoint([in] startIndex, [in] endIndex, [in] coordinateType, [in] x, [in] y) */
    int scrollSubstringToPoint(int startIndex, int endIndex, int coordinateType, int x, int y) {
        if (DEBUG)
            print(this.getApi() + ".IAccessibleText::scrollSubstringToPoint");
        AccessibleTextEvent event = new AccessibleTextEvent(this.getApi());
        event.start = startIndex;
        event.end = endIndex;
        event.type = ACC.SCROLL_TYPE_POINT;
        event.x = x;
        event.y = y;
        for (int i = 0; i < accessibleTextExtendedListenersSize(); i++) {
            AccessibleTextExtendedListener listener = accessibleTextExtendedListeners.get(i);
            listener.scrollText(event);
        }
        return 0;
    }

    /* IAccessibleText::get_newText([out] pNewText) */
    int get_newText(long pNewText) {
        if (DEBUG)
            print(this.getApi() + ".IAccessibleText::get_newText");
        String text = null;
        int start = 0;
        int end = 0;
        if (textInserted != null) {
            text = (String) textInserted[3];
            start = ((Integer) textInserted[1]).intValue();
            end = ((Integer) textInserted[2]).intValue();
        }
        setString(pNewText, text);
        return 0;
    }

    /* IAccessibleText::get_oldText([out] pOldText) */
    int get_oldText(long pOldText) {
        if (DEBUG)
            print(this.getApi() + ".IAccessibleText::get_oldText");
        String text = null;
        int start = 0;
        int end = 0;
        if (textDeleted != null) {
            text = (String) textDeleted[3];
            start = ((Integer) textDeleted[1]).intValue();
            end = ((Integer) textDeleted[2]).intValue();
        }
        setString(pOldText, text);
        return 0;
    }

    /* IAccessibleValue::get_currentValue([out] pCurrentValue) */
    int get_currentValue(long pCurrentValue) {
        AccessibleValueEvent event = new AccessibleValueEvent(this.getApi());
        for (int i = 0; i < accessibleValueListenersSize(); i++) {
            AccessibleValueListener listener = accessibleValueListeners.get(i);
            listener.getCurrentValue(event);
        }
        setNumberVARIANT(pCurrentValue, event.value);
        return 0;
    }

    /* IAccessibleValue::setCurrentValue([in] value) */
    int setCurrentValue(long value) {
        if (DEBUG)
            print(this.getApi() + ".IAccessibleValue::setCurrentValue");
        AccessibleValueEvent event = new AccessibleValueEvent(this.getApi());
        event.value = getNumberVARIANT(value);
        for (int i = 0; i < accessibleValueListenersSize(); i++) {
            AccessibleValueListener listener = accessibleValueListeners.get(i);
            listener.setCurrentValue(event);
        }
        return 0;
    }

    /* IAccessibleValue::get_maximumValue([out] pMaximumValue) */
    int get_maximumValue(long pMaximumValue) {
        AccessibleValueEvent event = new AccessibleValueEvent(this.getApi());
        for (int i = 0; i < accessibleValueListenersSize(); i++) {
            AccessibleValueListener listener = accessibleValueListeners.get(i);
            listener.getMaximumValue(event);
        }
        setNumberVARIANT(pMaximumValue, event.value);
        return 0;
    }

    /* IAccessibleValue::get_minimumValue([out] pMinimumValue) */
    int get_minimumValue(long pMinimumValue) {
        AccessibleValueEvent event = new AccessibleValueEvent(this.getApi());
        for (int i = 0; i < accessibleValueListenersSize(); i++) {
            AccessibleValueListener listener = accessibleValueListeners.get(i);
            listener.getMinimumValue(event);
        }
        setNumberVARIANT(pMinimumValue, event.value);
        return 0;
    }

    int eventChildID() {
        if (uniqueID == -1)
            uniqueID = UniqueID--;
        return uniqueID;
    }

    void checkUniqueID(int childID) {
        /* If the application is using child ids, check whether there's a corresponding
		 * accessible, and if so, use the child id as that accessible's unique id. */
        AccessibleControlEvent event = new AccessibleControlEvent(this.getApi());
        event.childID = childID;
        for (int l = 0; l < accessibleControlListenersSize(); l++) {
            AccessibleControlListener listener = accessibleControlListeners.get(l);
            listener.getChild(event);
        }
        Accessible accessible = event.accessible;
        if (accessible != null && accessible.getImpl()._uniqueID() == -1) {
            if (accessible.getImpl() instanceof DartAccessible) {
                ((DartAccessible) accessible.getImpl()).uniqueID = childID;
            }
            if (accessible.getImpl() instanceof SwtAccessible) {
                ((SwtAccessible) accessible.getImpl()).uniqueID = childID;
            }
        }
    }

    int childIDToOs(int childID) {
        /* ChildIDs are 1-based indices. */
        int osChildID = childID + 1;
        if (control instanceof Tree) {
        }
        checkUniqueID(osChildID);
        return osChildID;
    }

    int osToChildID(int osChildID) {
        /*
		* Feature of Windows:
		* Before Windows XP, tree item ids were 1-based indices.
		* Windows XP and later use the tree item handle for the
		* accessible child ID. For backward compatibility, we still
		* take 1-based childIDs for tree items prior to Windows XP.
		* All other childIDs are 1-based indices.
		*/
        if (!(control instanceof Tree))
            return osChildID - 1;
        return 0;
    }

    int stateToOs(int state) {
        int osState = 0;
        return osState;
    }

    int osToState(int osState) {
        int state = ACC.STATE_NORMAL;
        return state;
    }

    int roleToOs(int role) {
        switch(role) {
            case ACC.ROLE_CLIENT_AREA:
            case ACC.ROLE_WINDOW:
            case ACC.ROLE_MENUBAR:
            case ACC.ROLE_MENU:
            case ACC.ROLE_MENUITEM:
            case ACC.ROLE_SEPARATOR:
            case ACC.ROLE_TOOLTIP:
            case ACC.ROLE_SCROLLBAR:
            case ACC.ROLE_DIALOG:
            case ACC.ROLE_LABEL:
            case ACC.ROLE_PUSHBUTTON:
            case ACC.ROLE_CHECKBUTTON:
            case ACC.ROLE_RADIOBUTTON:
            case ACC.ROLE_SPLITBUTTON:
            case ACC.ROLE_COMBOBOX:
            case ACC.ROLE_TEXT:
            case ACC.ROLE_TOOLBAR:
            case ACC.ROLE_LIST:
            case ACC.ROLE_LISTITEM:
            case ACC.ROLE_TABLE:
            case ACC.ROLE_TABLECELL:
            case ACC.ROLE_TABLECOLUMNHEADER:
            case ACC.ROLE_TABLEROWHEADER:
            case ACC.ROLE_TREE:
            case ACC.ROLE_TREEITEM:
            case ACC.ROLE_TABFOLDER:
            case ACC.ROLE_TABITEM:
            case ACC.ROLE_PROGRESSBAR:
            case ACC.ROLE_SLIDER:
            case ACC.ROLE_LINK:
            case ACC.ROLE_ALERT:
            case ACC.ROLE_ANIMATION:
            case ACC.ROLE_COLUMN:
            case ACC.ROLE_DOCUMENT:
            case ACC.ROLE_GRAPHIC:
            case ACC.ROLE_GROUP:
            case ACC.ROLE_ROW:
            case ACC.ROLE_SPINBUTTON:
            case ACC.ROLE_STATUSBAR:
            case ACC.ROLE_CLOCK:
            case ACC.ROLE_CALENDAR:
            /* The rest are IA2 roles, so return the closest match. */
            case ACC.ROLE_CANVAS:
            case ACC.ROLE_CHECKMENUITEM:
            case ACC.ROLE_RADIOMENUITEM:
            case ACC.ROLE_DATETIME:
            case ACC.ROLE_FOOTER:
            case ACC.ROLE_FORM:
            case ACC.ROLE_HEADER:
            case ACC.ROLE_HEADING:
            case ACC.ROLE_PAGE:
            case ACC.ROLE_PARAGRAPH:
            case ACC.ROLE_SECTION:
        }
        return 0;
    }

    int osToRole(int osRole) {
        return ACC.ROLE_CLIENT_AREA;
    }

    /*
	 * Return a Color given a string of the form "rgb(n,n,n)".
	 */
    Color colorFromString(String rgbString) {
        try {
            int open = rgbString.indexOf('(');
            int comma1 = rgbString.indexOf(',');
            int comma2 = rgbString.indexOf(',', comma1 + 1);
            int close = rgbString.indexOf(')');
            int r = Integer.parseInt(rgbString.substring(open + 1, comma1));
            int g = Integer.parseInt(rgbString.substring(comma1 + 1, comma2));
            int b = Integer.parseInt(rgbString.substring(comma2 + 1, close));
            return new Color(control.getDisplay(), r, g, b);
        } catch (NumberFormatException ex) {
        }
        return null;
    }

    int getCaretOffset() {
        AccessibleTextEvent event = new AccessibleTextEvent(this.getApi());
        event.offset = -1;
        for (int i = 0; i < accessibleTextExtendedListenersSize(); i++) {
            AccessibleTextListener listener = accessibleTextExtendedListeners.get(i);
            listener.getCaretOffset(event);
        }
        if (event.offset == -1) {
            for (int i = 0; i < accessibleTextListenersSize(); i++) {
                event.childID = ACC.CHILDID_SELF;
                AccessibleTextListener listener = accessibleTextListeners.get(i);
                listener.getCaretOffset(event);
            }
        }
        return event.offset;
    }

    int getCharacterCount() {
        AccessibleTextEvent event = new AccessibleTextEvent(this.getApi());
        event.count = -1;
        for (int i = 0; i < accessibleTextExtendedListenersSize(); i++) {
            AccessibleTextExtendedListener listener = accessibleTextExtendedListeners.get(i);
            listener.getCharacterCount(event);
        }
        if (event.count == -1) {
            AccessibleControlEvent e = new AccessibleControlEvent(this.getApi());
            e.childID = ACC.CHILDID_SELF;
            for (int i = 0; i < accessibleControlListenersSize(); i++) {
                AccessibleControlListener listener = accessibleControlListeners.get(i);
                listener.getRole(e);
                listener.getValue(e);
            }
            // TODO: Consider passing the value through for other roles as well (i.e. combo, etc). Keep in sync with get_text.
            event.count = e.detail == ACC.ROLE_TEXT && e.result != null ? e.result.length() : 0;
        }
        return event.count;
    }

    int getRelationCount() {
        int count = 0;
        for (int type = 0; type < MAX_RELATION_TYPES; type++) {
            if (relations[type] != null)
                count++;
        }
        return count;
    }

    int getRole() {
        AccessibleControlEvent event = new AccessibleControlEvent(this.getApi());
        event.childID = ACC.CHILDID_SELF;
        for (int i = 0; i < accessibleControlListenersSize(); i++) {
            AccessibleControlListener listener = accessibleControlListeners.get(i);
            listener.getRole(event);
        }
        return event.detail;
    }

    int getDefaultRole() {
        return 0;
    }

    String getString(long psz) {
        return null;
    }

    Number getNumberVARIANT(long variant) {
        return null;
    }

    void setIntVARIANT(long variant, short vt, int lVal) {
    }

    void setPtrVARIANT(long variant, short vt, long lVal) {
    }

    void setNumberVARIANT(long variant, Number number) {
        if (number == null) {
        } else if (number instanceof Double) {
        } else if (number instanceof Float) {
        } else if (number instanceof Long) {
        } else {
        }
    }

    void setString(long psz, String string) {
        if (string != null) {
        }
    }

    void setStringVARIANT(long variant, String string) {
        if (string != null) {
        }
    }

    /* checkWidget was copied from Widget, and rewritten to work in this package */
    void checkWidget() {
        if (!isValidThread())
            SWT.error(SWT.ERROR_THREAD_INVALID_ACCESS);
        if (control.isDisposed())
            SWT.error(SWT.ERROR_WIDGET_DISPOSED);
    }

    boolean isATRunning() {
        /*
		 * Currently there is no accurate way to check if AT is running from 'refCount'.
		 * JAWS screen reader cannot be detected using 'refCount' approach,
		 * because 'refCount' continues to be 1 even when JAWS is running.
		 */
        // if (refCount <= 1) return false;
        return true;
    }

    /* isValidThread was copied from Widget, and rewritten to work in this package */
    boolean isValidThread() {
        return control.getDisplay().getThread() == Thread.currentThread();
    }

    // START DEBUG CODE
    static void print(String str) {
        if (DEBUG)
            System.out.println(str);
    }

    String getRoleString(int role) {
        return "Unknown role (" + role + ")";
    }

    String getStateString(int state) {
        if (state == 0)
            return " no state bits set";
        StringBuilder stateString = new StringBuilder();
        if (DEBUG) {
            if (stateString.length() == 0)
                stateString.append(" Unknown state[s] (" + Integer.toHexString(state) + ")");
        }
        return stateString.toString();
    }

    String getIA2StatesString(int ia2States) {
        if (ia2States == 0)
            return " no state bits set";
        StringBuilder stateString = new StringBuilder();
        if (DEBUG) {
            if (stateString.length() == 0)
                stateString.append(" Unknown IA2 state[s] (" + ia2States + ")");
        }
        return stateString.toString();
    }

    String getEventString(int event) {
        if (DEBUG)
            switch(event) {
                case ACC.EVENT_TABLE_CHANGED:
                    return "IA2_EVENT_TABLE_CHANGED";
                case ACC.EVENT_TEXT_CHANGED:
                    return "IA2_EVENT_TEXT_REMOVED or IA2_EVENT_TEXT_INSERTED";
                case ACC.EVENT_HYPERTEXT_LINK_SELECTED:
                    return "IA2_EVENT_HYPERTEXT_LINK_SELECTED";
                case ACC.EVENT_VALUE_CHANGED:
                    return "EVENT_OBJECT_VALUECHANGE";
                case ACC.EVENT_STATE_CHANGED:
                    return "EVENT_OBJECT_STATECHANGE";
                case ACC.EVENT_SELECTION_CHANGED:
                    return "EVENT_OBJECT_SELECTIONWITHIN";
                case ACC.EVENT_TEXT_SELECTION_CHANGED:
                    return "EVENT_OBJECT_TEXTSELECTIONCHANGED";
                case ACC.EVENT_LOCATION_CHANGED:
                    return "EVENT_OBJECT_LOCATIONCHANGE";
                case ACC.EVENT_NAME_CHANGED:
                    return "EVENT_OBJECT_NAMECHANGE";
                case ACC.EVENT_DESCRIPTION_CHANGED:
                    return "EVENT_OBJECT_DESCRIPTIONCHANGE";
                case ACC.EVENT_DOCUMENT_LOAD_COMPLETE:
                    return "IA2_EVENT_DOCUMENT_LOAD_COMPLETE";
                case ACC.EVENT_DOCUMENT_LOAD_STOPPED:
                    return "IA2_EVENT_DOCUMENT_LOAD_STOPPED";
                case ACC.EVENT_DOCUMENT_RELOAD:
                    return "IA2_EVENT_DOCUMENT_RELOAD";
                case ACC.EVENT_PAGE_CHANGED:
                    return "IA2_EVENT_PAGE_CHANGED";
                case ACC.EVENT_SECTION_CHANGED:
                    return "IA2_EVENT_SECTION_CHANGED";
                case ACC.EVENT_ACTION_CHANGED:
                    return "IA2_EVENT_ACTION_CHANGED";
                case ACC.EVENT_HYPERLINK_START_INDEX_CHANGED:
                    return "IA2_EVENT_HYPERLINK_START_INDEX_CHANGED";
                case ACC.EVENT_HYPERLINK_END_INDEX_CHANGED:
                    return "IA2_EVENT_HYPERLINK_END_INDEX_CHANGED";
                case ACC.EVENT_HYPERLINK_ANCHOR_COUNT_CHANGED:
                    return "IA2_EVENT_HYPERLINK_ANCHOR_COUNT_CHANGED";
                case ACC.EVENT_HYPERLINK_SELECTED_LINK_CHANGED:
                    return "IA2_EVENT_HYPERLINK_SELECTED_LINK_CHANGED";
                case ACC.EVENT_HYPERLINK_ACTIVATED:
                    return "IA2_EVENT_HYPERLINK_ACTIVATED";
                case ACC.EVENT_HYPERTEXT_LINK_COUNT_CHANGED:
                    return "IA2_EVENT_HYPERTEXT_LINK_COUNT_CHANGED";
                case ACC.EVENT_ATTRIBUTE_CHANGED:
                    return "IA2_EVENT_ATTRIBUTE_CHANGED";
                case ACC.EVENT_TABLE_CAPTION_CHANGED:
                    return "IA2_EVENT_TABLE_CAPTION_CHANGED";
                case ACC.EVENT_TABLE_COLUMN_DESCRIPTION_CHANGED:
                    return "IA2_EVENT_TABLE_COLUMN_DESCRIPTION_CHANGED";
                case ACC.EVENT_TABLE_COLUMN_HEADER_CHANGED:
                    return "IA2_EVENT_TABLE_COLUMN_HEADER_CHANGED";
                case ACC.EVENT_TABLE_ROW_DESCRIPTION_CHANGED:
                    return "IA2_EVENT_TABLE_ROW_DESCRIPTION_CHANGED";
                case ACC.EVENT_TABLE_ROW_HEADER_CHANGED:
                    return "IA2_EVENT_TABLE_ROW_HEADER_CHANGED";
                case ACC.EVENT_TABLE_SUMMARY_CHANGED:
                    return "IA2_EVENT_TABLE_SUMMARY_CHANGED";
                case ACC.EVENT_TEXT_ATTRIBUTE_CHANGED:
                    return "IA2_EVENT_TEXT_ATTRIBUTE_CHANGED";
                case ACC.EVENT_TEXT_CARET_MOVED:
                    return "IA2_EVENT_TEXT_CARET_MOVED";
                case ACC.EVENT_TEXT_COLUMN_CHANGED:
                    return "IA2_EVENT_TEXT_COLUMN_CHANGED";
            }
        return "Unknown event (" + event + ")";
    }

    private String hresult(int code) {
        return " HRESULT=" + code;
    }

    @Override
    public String toString() {
        String toString = super.toString();
        if (DEBUG) {
            int role = getRole();
            if (role == 0)
                role = getDefaultRole();
            return toString.substring(toString.lastIndexOf('.') + 1) + "(" + getRoleString(role) + ")";
        }
        return toString;
    }

    // END DEBUG CODE
    int focus;

    public int _refCount() {
        return refCount;
    }

    public int _enumIndex() {
        return enumIndex;
    }

    public Runnable _timer() {
        return timer;
    }

    public List<AccessibleListener> _accessibleListeners() {
        return accessibleListeners;
    }

    public List<AccessibleControlListener> _accessibleControlListeners() {
        return accessibleControlListeners;
    }

    public List<AccessibleTextListener> _accessibleTextListeners() {
        return accessibleTextListeners;
    }

    public List<AccessibleActionListener> _accessibleActionListeners() {
        return accessibleActionListeners;
    }

    public List<AccessibleEditableTextListener> _accessibleEditableTextListeners() {
        return accessibleEditableTextListeners;
    }

    public List<AccessibleHyperlinkListener> _accessibleHyperlinkListeners() {
        return accessibleHyperlinkListeners;
    }

    public List<AccessibleTableListener> _accessibleTableListeners() {
        return accessibleTableListeners;
    }

    public List<AccessibleTableCellListener> _accessibleTableCellListeners() {
        return accessibleTableCellListeners;
    }

    public List<AccessibleTextExtendedListener> _accessibleTextExtendedListeners() {
        return accessibleTextExtendedListeners;
    }

    public List<AccessibleValueListener> _accessibleValueListeners() {
        return accessibleValueListeners;
    }

    public List<AccessibleAttributeListener> _accessibleAttributeListeners() {
        return accessibleAttributeListeners;
    }

    public Relation[] _relations() {
        return relations;
    }

    public Object[] _variants() {
        return variants;
    }

    public Accessible _parent() {
        return parent;
    }

    public List<Accessible> _children() {
        return children;
    }

    public Control _control() {
        return control;
    }

    public int _uniqueID() {
        return uniqueID;
    }

    public int[] _tableChange() {
        return tableChange;
    }

    public Object[] _textDeleted() {
        return textDeleted;
    }

    public Object[] _textInserted() {
        return textInserted;
    }

    public ToolItem _item() {
        return item;
    }

    public int _focus() {
        return focus;
    }

    public Accessible getApi() {
        if (api == null)
            api = Accessible.createApi(this);
        return (Accessible) api;
    }

    protected Accessible api;

    public void setApi(Accessible api) {
        this.api = api;
        if (api != null)
            api.impl = this;
    }

    protected VAccessible value;

    public VAccessible getValue() {
        if (value == null)
            value = new VAccessible(this);
        return (VAccessible) value;
    }
}
