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
package org.eclipse.swt.widgets;

import java.util.WeakHashMap;

/**
 * Instances of this class are used to ensure that an
 * application cannot interfere with the locking mechanism
 * used to implement asynchronous and synchronous communication
 * between widgets and background threads.
 */
class RunnableLock {

    RunnableLock(Runnable runnable) {
        this(new SWTRunnableLock(runnable));
    }

    public IRunnableLock delegate;

    protected static <T extends RunnableLock, I extends IRunnableLock> T[] ofArray(I[] items, Class<T> clazz, java.util.function.Function<I, T> factory) {
        @SuppressWarnings("unchecked")
        T[] target = (T[]) java.lang.reflect.Array.newInstance(clazz, items.length);
        for (int i = 0; i < target.length; ++i) target[i] = factory.apply(items[i]);
        return target;
    }

    @SuppressWarnings("unchecked")
    protected static <T extends RunnableLock, I extends IRunnableLock> I[] fromArray(T[] items) {
        if (items.length == 0)
            return (I[]) java.lang.reflect.Array.newInstance(IRunnableLock.class, 0);
        Class<I> targetClazz = null;
        for (T item : items) outer: {
            for (Class<?> i : item.getClass().getInterfaces()) {
                if (IRunnableLock.class.isAssignableFrom(i)) {
                    targetClazz = (Class<I>) i;
                    break outer;
                }
            }
        }
        if (targetClazz == null)
            return (I[]) java.lang.reflect.Array.newInstance(IRunnableLock.class, 0);
        I[] target = (I[]) java.lang.reflect.Array.newInstance(targetClazz, items.length);
        for (int i = 0; i < target.length; ++i) target[i] = (I) items[i].delegate;
        return target;
    }

    protected static final WeakHashMap<IRunnableLock, RunnableLock> INSTANCES = new WeakHashMap<IRunnableLock, RunnableLock>();

    protected RunnableLock(IRunnableLock delegate) {
        this.delegate = delegate;
        INSTANCES.put(delegate, this);
    }

    public static RunnableLock getInstance(IRunnableLock delegate) {
        if (delegate == null) {
            return null;
        }
        RunnableLock ref = (RunnableLock) INSTANCES.get(delegate);
        if (ref == null) {
            ref = new RunnableLock(delegate);
        }
        return ref;
    }
}
