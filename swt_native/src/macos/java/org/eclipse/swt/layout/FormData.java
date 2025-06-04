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
 * Instances of this class are used to define the attachments
 * of a control in a <code>FormLayout</code>.
 * <p>
 * To set a <code>FormData</code> object into a control, you use the
 * <code>setLayoutData ()</code> method. To define attachments for the
 * <code>FormData</code>, set the fields directly, like this:
 * </p>
 * <pre>
 * 		FormData data = new FormData();
 * 		data.left = new FormAttachment(0,5);
 * 		data.right = new FormAttachment(100,-5);
 * 		button.setLayoutData(formData);
 * </pre>
 * <p>
 * <code>FormData</code> contains the <code>FormAttachments</code> for
 * each edge of the control that the <code>FormLayout</code> uses to
 * determine the size and position of the control. <code>FormData</code>
 * objects also allow you to set the width and height of controls within
 * a <code>FormLayout</code>.
 * </p>
 *
 * @see FormLayout
 * @see FormAttachment
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 *
 * @since 2.0
 */
public final class FormData {

    /**
     * width specifies the preferred width in points. This value
     * is the wHint passed into Control.computeSize(int, int, boolean)
     * to determine the preferred size of the control.
     *
     * The default value is SWT.DEFAULT.
     *
     * @see Control#computeSize(int, int, boolean)
     */
    public int width = SWT.DEFAULT;

    /**
     * height specifies the preferred height in points. This value
     * is the hHint passed into Control.computeSize(int, int, boolean)
     * to determine the preferred size of the control.
     *
     * The default value is SWT.DEFAULT.
     *
     * @see Control#computeSize(int, int, boolean)
     */
    public int height = SWT.DEFAULT;

    /**
     * left specifies the attachment of the left side of
     * the control.
     */
    public FormAttachment left;

    /**
     * right specifies the attachment of the right side of
     * the control.
     */
    public FormAttachment right;

    /**
     * top specifies the attachment of the top of the control.
     */
    public FormAttachment top;

    /**
     * bottom specifies the attachment of the bottom of the
     * control.
     */
    public FormAttachment bottom;

    /**
     * Constructs a new instance of FormData using
     * default values.
     */
    public FormData() {
        this((IFormData) null);
        setImpl(new SwtFormData(this));
    }

    /**
     * Constructs a new instance of FormData according to the parameters.
     * A value of SWT.DEFAULT indicates that no minimum width or
     * no minimum height is specified.
     *
     * @param width a minimum width for the control
     * @param height a minimum height for the control
     */
    public FormData(int width, int height) {
        this((IFormData) null);
        setImpl(new SwtFormData(width, height, this));
    }

    /**
     * Returns a string containing a concise, human-readable
     * description of the receiver.
     *
     * @return a string representation of the FormData object
     */
    public String toString() {
        return getImpl().toString();
    }

    protected IFormData impl;

    protected FormData(IFormData impl) {
        if (impl != null)
            impl.setApi(this);
    }

    static FormData createApi(IFormData impl) {
        return new FormData(impl);
    }

    public IFormData getImpl() {
        return impl;
    }

    protected FormData setImpl(IFormData impl) {
        this.impl = impl;
        return this;
    }
}
