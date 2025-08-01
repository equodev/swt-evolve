/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2011 IBM Corporation and others.
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
package org.eclipse.swt.events;

import org.eclipse.swt.widgets.Event;

/**
 * Instances of this class are sent as a result of
 * widgets handling keyboard events
 *
 * @see VerifyListener
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 */
public final class VerifyEvent extends KeyEvent {

    /**
     * the range of text being modified.
     * Setting these fields has no effect.
     */
    public int start, end;

    /**
     * the new text that will be inserted.
     * Setting this field will change the text that is about to
     * be inserted or deleted.
     */
    public String text;

    static final long serialVersionUID = 3257003246269577014L;

    /**
     * Constructs a new instance of this class based on the
     * information in the given untyped event.
     *
     * @param e the untyped event containing the information
     */
    public VerifyEvent(Event e) {
        super(e);
        this.start = e.start;
        this.end = e.end;
        this.text = e.text;
    }

    /**
     * Returns a string containing a concise, human-readable
     * description of the receiver.
     *
     * @return a string representation of the event
     */
    @Override
    public String toString() {
        String string = super.toString();
        return // remove trailing '}'
        string.substring(0, string.length() - 1) + " start=" + start + " end=" + end + " text=" + text + "}";
    }
}
