/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2020 IBM Corporation and others.
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
 *      Christoph Läubrich - Bug 513185
 * *****************************************************************************
 */
package org.eclipse.swt.layout;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

/**
 * <code>FillLayout</code> is the simplest layout class. It lays out
 * controls in a single row or column, forcing them to be the same size.
 * <p>
 * Initially, the controls will all be as tall as the tallest control,
 * and as wide as the widest. <code>FillLayout</code> does not wrap,
 * but you can specify margins and spacing. You might use it to
 * lay out buttons in a task bar or tool bar, or to stack checkboxes
 * in a <code>Group</code>. <code>FillLayout</code> can also be used
 * when a <code>Composite</code> only has one child. For example,
 * if a <code>Shell</code> has a single <code>Group</code> child,
 * <code>FillLayout</code> will cause the <code>Group</code> to
 * completely fill the <code>Shell</code> (if margins are 0).
 * </p>
 * <p>
 * Example code: first a <code>FillLayout</code> is created and
 * its type field is set, and then the layout is set into the
 * <code>Composite</code>. Note that in a <code>FillLayout</code>,
 * children are always the same size, and they fill all available space.
 * </p>
 * <pre>
 * 		FillLayout fillLayout = new FillLayout();
 * 		fillLayout.type = SWT.VERTICAL;
 * 		shell.setLayout(fillLayout);
 * </pre>
 *
 * @see <a href="http://www.eclipse.org/swt/examples.php">SWT Example: LayoutExample</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 */
public final class FillLayout extends Layout {

    /**
     * type specifies how controls will be positioned
     * within the layout.
     *
     * The default value is HORIZONTAL.
     *
     * Possible values are: <ul>
     *    <li>HORIZONTAL: Position the controls horizontally from left to right</li>
     *    <li>VERTICAL: Position the controls vertically from top to bottom</li>
     * </ul>
     */
    public int type = SWT.HORIZONTAL;

    /**
     * marginWidth specifies the number of points of horizontal margin
     * that will be placed along the left and right edges of the layout.
     *
     * The default value is 0.
     *
     * @since 3.0
     */
    public int marginWidth = 0;

    /**
     * marginHeight specifies the number of points of vertical margin
     * that will be placed along the top and bottom edges of the layout.
     *
     * The default value is 0.
     *
     * @since 3.0
     */
    public int marginHeight = 0;

    /**
     * spacing specifies the number of points between the edge of one cell
     * and the edge of its neighbouring cell.
     *
     * The default value is 0.
     *
     * @since 3.0
     */
    public int spacing = 0;

    /**
     * Constructs a new instance of this class.
     */
    public FillLayout() {
        this((IFillLayout) null);
        setImpl(new SwtFillLayout());
    }

    /**
     * Constructs a new instance of this class given the type.
     *
     * @param type the type of fill layout
     *
     * @since 2.0
     */
    public FillLayout(int type) {
        this((IFillLayout) null);
        setImpl(new SwtFillLayout(type));
    }

    protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
        return getImpl().computeSize(composite, wHint, hHint, flushCache);
    }

    protected boolean flushCache(Control control) {
        return getImpl().flushCache(control);
    }

    protected void layout(Composite composite, boolean flushCache) {
        getImpl().layout(composite, flushCache);
    }

    /**
     * Returns a string containing a concise, human-readable
     * description of the receiver.
     *
     * @return a string representation of the layout
     */
    public String toString() {
        return getImpl().toString();
    }

    protected FillLayout(IFillLayout impl) {
        super(impl);
    }

    static FillLayout createApi(IFillLayout impl) {
        if (dev.equo.swt.Creation.creating.peek() instanceof FillLayout inst) {
            inst.impl = impl;
            return inst;
        } else
            return new FillLayout(impl);
    }

    public IFillLayout getImpl() {
        return (IFillLayout) super.getImpl();
    }
}
