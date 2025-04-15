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

import java.util.WeakHashMap;

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
        this(new SWTTouch(identity, (SWTTouchSource) source.delegate, state, primary, x, y));
    }

    /**
     * Returns a string containing a concise, human-readable
     * description of the receiver.
     *
     * @return a string representation of the event
     */
    @Override
    public String toString() {
        return ((ITouch) this.delegate).toString();
    }

    public ITouch delegate;

    protected static <T extends Touch, I extends ITouch> T[] ofArray(I[] items, Class<T> clazz, java.util.function.Function<I, T> factory) {
        @SuppressWarnings("unchecked")
        T[] target = (T[]) java.lang.reflect.Array.newInstance(clazz, items.length);
        for (int i = 0; i < target.length; ++i) target[i] = factory.apply(items[i]);
        return target;
    }

    @SuppressWarnings("unchecked")
    protected static <T extends Touch, I extends ITouch> I[] fromArray(T[] items) {
        if (items.length == 0)
            return (I[]) java.lang.reflect.Array.newInstance(ITouch.class, 0);
        Class<I> targetClazz = null;
        for (T item : items) outer: {
            for (Class<?> i : item.getClass().getInterfaces()) {
                if (ITouch.class.isAssignableFrom(i)) {
                    targetClazz = (Class<I>) i;
                    break outer;
                }
            }
        }
        if (targetClazz == null)
            return (I[]) java.lang.reflect.Array.newInstance(ITouch.class, 0);
        I[] target = (I[]) java.lang.reflect.Array.newInstance(targetClazz, items.length);
        for (int i = 0; i < target.length; ++i) target[i] = (I) items[i].delegate;
        return target;
    }

    protected static final WeakHashMap<ITouch, Touch> INSTANCES = new WeakHashMap<ITouch, Touch>();

    protected Touch(ITouch delegate) {
        this.delegate = delegate;
        INSTANCES.put(delegate, this);
    }

    public static Touch getInstance(ITouch delegate) {
        if (delegate == null) {
            return null;
        }
        Touch ref = (Touch) INSTANCES.get(delegate);
        if (ref == null) {
            ref = new Touch(delegate);
        }
        return ref;
    }
}
