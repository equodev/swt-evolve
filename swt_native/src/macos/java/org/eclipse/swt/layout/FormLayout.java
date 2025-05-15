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
package org.eclipse.swt.layout;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

/**
 *  Instances of this class control the position and size of the
 *  children of a composite control by using <code>FormAttachments</code>
 *  to optionally configure the left, top, right and bottom edges of
 *  each child.
 *  <p>
 *  The following example code creates a <code>FormLayout</code> and then sets
 *  it into a <code>Shell</code>:
 *  </p>
 *  <pre>
 *  		Display display = new Display ();
 * 		Shell shell = new Shell(display);
 * 		FormLayout layout = new FormLayout();
 * 		layout.marginWidth = 3;
 * 		layout.marginHeight = 3;
 * 		shell.setLayout(layout);
 *  </pre>
 *  <p>
 *  To use a <code>FormLayout</code>, create a <code>FormData</code> with
 *  <code>FormAttachment</code> for each child of <code>Composite</code>.
 *  The following example code attaches <code>button1</code> to the top
 *  and left edge of the composite and <code>button2</code> to the right
 *  edge of <code>button1</code> and the top and right edges of the
 *  composite:
 *  </p>
 *  <pre>
 * 		FormData data1 = new FormData();
 * 		data1.left = new FormAttachment(0, 0);
 * 		data1.top = new FormAttachment(0, 0);
 * 		button1.setLayoutData(data1);
 * 		FormData data2 = new FormData();
 * 		data2.left = new FormAttachment(button1);
 * 		data2.top = new FormAttachment(0, 0);
 * 		data2.right = new FormAttachment(100, 0);
 * 		button2.setLayoutData(data2);
 *  </pre>
 *  <p>
 *  Each side of a child control can be attached to a position in the parent
 *  composite, or to other controls within the <code>Composite</code> by
 *  creating instances of <code>FormAttachment</code> and setting them into
 *  the top, bottom, left, and right fields of the child's <code>FormData</code>.
 *  </p>
 *  <p>
 *  If a side is not given an attachment, it is defined as not being attached
 *  to anything, causing the child to remain at its preferred size.  If a child
 *  is given no attachment on either the left or the right or top or bottom, it is
 *  automatically attached to the left and top of the composite respectively.
 *  The following code positions <code>button1</code> and <code>button2</code>
 *  but relies on default attachments:
 *  </p>
 *  <pre>
 * 		FormData data2 = new FormData();
 * 		data2.left = new FormAttachment(button1);
 * 		data2.right = new FormAttachment(100, 0);
 * 		button2.setLayoutData(data2);
 *  </pre>
 *  <p>
 *  IMPORTANT: Do not define circular attachments.  For example, do not attach
 *  the right edge of <code>button1</code> to the left edge of <code>button2</code>
 *  and then attach the left edge of <code>button2</code> to the right edge of
 *  <code>button1</code>.  This will over constrain the layout, causing undefined
 *  behavior.  The algorithm will terminate, but the results are undefined.
 *  </p>
 *
 *  @see FormData
 *  @see FormAttachment
 *  @see <a href="http://www.eclipse.org/swt/snippets/#formlayout">FormLayout snippets</a>
 *  @see <a href="http://www.eclipse.org/swt/examples.php">SWT Example: LayoutExample</a>
 *  @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 *
 *  @since 2.0
 */
public final class FormLayout extends Layout {

    /**
     * marginWidth specifies the number of points of horizontal margin
     * that will be placed along the left and right edges of the layout.
     *
     * The default value is 0.
     */
    public int marginWidth = 0;

    /**
     * marginHeight specifies the number of points of vertical margin
     * that will be placed along the top and bottom edges of the layout.
     *
     * The default value is 0.
     */
    public int marginHeight = 0;

    /**
     * marginLeft specifies the number of points of horizontal margin
     * that will be placed along the left edge of the layout.
     *
     * The default value is 0.
     *
     * @since 3.1
     */
    public int marginLeft = 0;

    /**
     * marginTop specifies the number of points of vertical margin
     * that will be placed along the top edge of the layout.
     *
     * The default value is 0.
     *
     * @since 3.1
     */
    public int marginTop = 0;

    /**
     * marginRight specifies the number of points of horizontal margin
     * that will be placed along the right edge of the layout.
     *
     * The default value is 0.
     *
     * @since 3.1
     */
    public int marginRight = 0;

    /**
     * marginBottom specifies the number of points of vertical margin
     * that will be placed along the bottom edge of the layout.
     *
     * The default value is 0.
     *
     * @since 3.1
     */
    public int marginBottom = 0;

    /**
     * spacing specifies the number of points between the edge of one control
     * and the edge of its neighbouring control.
     *
     * The default value is 0.
     *
     * @since 3.0
     */
    public int spacing = 0;

    /**
     * Constructs a new instance of this class.
     */
    public FormLayout() {
        this(new nat.org.eclipse.swt.layout.FormLayout());
    }

    protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
        IPoint ret = getDelegate().computeSize((composite != null ? composite.getDelegate() : null), wHint, hHint, flushCache);
        return ret != null ? ret.getApi() : null;
    }

    protected boolean flushCache(Control control) {
        return getDelegate().flushCache((control != null ? control.getDelegate() : null));
    }

    protected void layout(Composite composite, boolean flushCache) {
        getDelegate().layout((composite != null ? composite.getDelegate() : null), flushCache);
    }

    /**
     * Returns a string containing a concise, human-readable
     * description of the receiver.
     *
     * @return a string representation of the layout
     */
    public String toString() {
        return getDelegate().toString();
    }

    protected FormLayout(IFormLayout delegate) {
        super(delegate);
    }

    public static FormLayout createApi(IFormLayout delegate) {
        return new FormLayout(delegate);
    }

    public IFormLayout getDelegate() {
        return (IFormLayout) super.getDelegate();
    }
}
