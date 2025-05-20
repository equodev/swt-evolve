/**
 * ****************************************************************************
 *  Copyright (c) 2010, 2011 IBM Corporation and others.
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

/**
 * Instances of this class are created in response to a
 * touch-based input device being touched. They are found
 * in the <code>touches</code> field of an Event or TouchEvent.
 *
 * @see org.eclipse.swt.events.TouchEvent
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 *
 * @since 3.7
 */
public final class SwtTouch implements ITouch {

    /**
     * Constructs a new touch state from the given inputs.
     *
     * @param identity Identity of the touch
     * @param source Object representing the device that generated the touch
     * @param state One of the state constants representing the state of this touch
     * @param primary Whether or not the touch is the primary touch
     * @param x X location of the touch in screen coordinates
     * @param y Y location of the touch in screen coordinates
     */
    SwtTouch(long identity, TouchSource source, int state, boolean primary, int x, int y) {
        this.getApi().id = identity;
        this.getApi().source = source;
        this.getApi().state = state;
        this.getApi().primary = primary;
        this.getApi().x = x;
        this.getApi().y = y;
    }

    /**
     * Returns a string containing a concise, human-readable
     * description of the receiver.
     *
     * @return a string representation of the event
     */
    @Override
    public String toString() {
        return "Touch {id=" + getApi().id + " source=" + getApi().source + " state=" + getApi().state + " primary=" + getApi().primary + " x=" + getApi().x + " y=" + getApi().y + "}";
    }

    public Touch getApi() {
        if (api == null)
            api = Touch.createApi(this);
        return (Touch) api;
    }

    protected Touch api;

    public void setApi(Touch api) {
        this.api = api;
    }
}
