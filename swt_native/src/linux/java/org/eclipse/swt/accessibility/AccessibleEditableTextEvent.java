/**
 * ****************************************************************************
 *  Copyright (c) 2010, 2017 IBM Corporation and others.
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

/**
 * Instances of this class are sent as a result of accessibility clients
 * sending AccessibleEditableText messages to an accessible object.
 *
 * @see AccessibleEditableTextListener
 * @see AccessibleEditableTextAdapter
 *
 * @since 3.7
 */
public class AccessibleEditableTextEvent extends EventObject {

    /**
     * [in] 0-based start offset of the character range to perform
     * the operation on
     *
     * @see AccessibleEditableTextListener#copyText
     * @see AccessibleEditableTextListener#cutText
     * @see AccessibleEditableTextListener#pasteText
     * @see AccessibleEditableTextListener#replaceText
     */
    public int start;

    /**
     * [in] 0-based ending offset of the character range to perform
     * the operation on
     *
     * @see AccessibleEditableTextListener#copyText
     * @see AccessibleEditableTextListener#cutText
     * @see AccessibleEditableTextListener#replaceText
     */
    public int end;

    /**
     * [in] a string that will replace the specified character range
     *
     * @see AccessibleEditableTextListener#replaceText
     */
    public String string;

    /**
     * [out] Set this field to {@link ACC#OK} if the operation
     * was completed successfully, and <code>null</code> otherwise.
     *
     * @see AccessibleEditableTextListener#copyText
     * @see AccessibleEditableTextListener#cutText
     * @see AccessibleEditableTextListener#pasteText
     * @see AccessibleEditableTextListener#replaceText
     */
    public String result;

    static final long serialVersionUID = -5045447704486894646L;

    /**
     * Constructs a new instance of this class.
     *
     * @param source the object that fired the event
     */
    public AccessibleEditableTextEvent(Object source) {
        super(source);
    }

    /**
     * Returns a string containing a concise, human-readable
     * description of the receiver.
     *
     * @return a string representation of the event
     */
    @Override
    public String toString() {
        return //$NON-NLS-1$
        "AccessibleEditableTextEvent {" + "start=" + //$NON-NLS-1$
        start + " end=" + //$NON-NLS-1$
        end + " string=" + //$NON-NLS-1$
        string + " result=" + //$NON-NLS-1$
        result + //$NON-NLS-1$
        "}";
    }
}
