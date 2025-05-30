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
public final class Touch {

    /**
     * The unique identity of the touch. Use this value to track changes to a touch
     * during the touch's life. Two touches may have the same identity even if they
     * come from different sources.
     */
    public long id;

    /**
     * The object representing the input source that generated the touch.
     */
    public TouchSource source;

    /**
     * The state of this touch at the time it was generated. If this field is 0
     * then the finger is still touching the device but has not moved
     * since the last <code>TouchEvent</code> was generated.
     *
     * @see org.eclipse.swt.SWT#TOUCHSTATE_DOWN
     * @see org.eclipse.swt.SWT#TOUCHSTATE_MOVE
     * @see org.eclipse.swt.SWT#TOUCHSTATE_UP
     */
    public int state;

    /**
     * A flag indicating that the touch is the first touch from a previous
     * state of no touch points. Once designated as such, the touch remains
     * the primary touch until all fingers are removed from the device.
     */
    public boolean primary;

    /**
     * The x location of the touch in TouchSource coordinates.
     */
    public int x;

    /**
     * The y location of the touch in TouchSource coordinates.
     */
    public int y;

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
    Touch(long identity, TouchSource source, int state, boolean primary, int x, int y) {
        this((ITouch) null);
        setImpl(new SwtTouch(identity, source, state, primary, x, y));
    }

    /**
     * Returns a string containing a concise, human-readable
     * description of the receiver.
     *
     * @return a string representation of the event
     */
    public String toString() {
        return getImpl().toString();
    }

    protected ITouch impl;

    protected Touch(ITouch impl) {
        if (impl == null) {
            dev.equo.swt.Creation.creating.push(this);
        } else {
            this.impl = impl;
            impl.setApi(this);
        }
    }

    static Touch createApi(ITouch impl) {
        if (dev.equo.swt.Creation.creating.peek() instanceof Touch inst) {
            inst.impl = impl;
            return inst;
        } else
            return new Touch(impl);
    }

    public ITouch getImpl() {
        return impl;
    }

    protected Touch setImpl(ITouch impl) {
        this.impl = impl;
        impl.setApi(this);
        dev.equo.swt.Creation.creating.pop();
        return this;
    }
}
