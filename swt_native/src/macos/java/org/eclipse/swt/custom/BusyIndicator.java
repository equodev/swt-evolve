/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2022 IBM Corporation and others.
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
 *      Chrsitoph Läubrich - add methods to work with {@link CompletableFuture}s
 * *****************************************************************************
 */
package org.eclipse.swt.custom;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

/**
 * Support for showing a Busy Cursor during a long running process.
 *
 * @see <a href="http://www.eclipse.org/swt/snippets/#busyindicator">BusyIndicator snippets</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 */
public class BusyIndicator {

    /**
     * Runs the given <code>Runnable</code> while providing
     * busy feedback using this busy indicator.
     *
     * @param display the display on which the busy feedback should be
     *        displayed.  If the display is null, the Display for the current
     *        thread will be used.  If there is no Display for the current thread,
     *        the runnable code will be executed and no busy feedback will be displayed.
     * @param runnable the runnable for which busy feedback is to be shown.
     *        Must not be null.
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the runnable is null</li>
     * </ul>
     */
    public static void showWhile(Display display, Runnable runnable) {
        nat.org.eclipse.swt.custom.BusyIndicator.showWhile((nat.org.eclipse.swt.widgets.Display) (display != null ? display.getDelegate() : null), runnable);
    }

    /**
     * If called from a {@link Display} thread, waits for the given
     * <code>Future</code> to complete and provides busy feedback using the busy
     * indicator. While waiting for completion, pending UI events are processed to
     * prevent UI freeze.
     *
     * If there is no {@link Display} for the current thread, the
     * {@link Future#get()} will be executed ignoring any {@link ExecutionException}
     * and no busy feedback will be displayed.
     *
     * @param future the {@link Future} for which busy feedback is to be shown.
     * @since 3.127
     * @implNote In some cases completion is detected by a regular timed wakeup of
     *           the {@link Display} thread,for minimal latency pass a
     *           {@link CompletableFuture} or trigger {@link Display#wake()} as an
     *           external event.
     */
    public static void showWhile(Future<?> future) {
        nat.org.eclipse.swt.custom.BusyIndicator.showWhile(future);
    }

    /**
     * If called from a {@link Display} thread use the given {@link SwtRunnable} to
     * produces a {@link CompletableFuture} providing busy feedback using the busy
     * indicator while execution is running. If called from a non {@link Display}
     * the execution is performed in place and the result returned as a
     * {@link CompletableFuture}. It is therefore safe to call this method from any
     * thread and the {@link SwtCallable} is always evaluated outside the UI. The
     * {@link ForkJoinPool#commonPool()} is used to execute the computation in case
     * this is called from a {@link Display} thread
     *
     * @param action the action that should be executed and produces a result of the
     *               {@link CompletableFuture}
     *
     * @since 3.123
     */
    public static <E extends Exception> CompletableFuture<?> execute(SwtRunnable<E> action) {
        return nat.org.eclipse.swt.custom.BusyIndicator.execute(action);
    }

    /**
     * If called from a {@link Display} thread use the given {@link SwtRunnable} to
     * produces a {@link CompletableFuture} providing busy feedback using the busy
     * indicator while execution is running. If called from a non {@link Display}
     * the execution is performed in place and the result returned as a
     * {@link CompletableFuture}. It is therefore safe to call this method from any
     * thread and the {@link SwtCallable} is always evaluated outside the UI.
     *
     * @param action   the action that should be executed and produces a result of
     *                 the {@link CompletableFuture}
     * @param executor the Executor to perform the computation in case this is
     *                 called from a {@link Display} thread, passing a
     *                 {@link Display} will throw an
     *                 {@link IllegalArgumentException} as this will lead to a
     *                 blocking UI and violates the contract of this method
     * @since 3.123
     */
    public static <E extends Exception> CompletableFuture<?> execute(SwtRunnable<E> action, Executor executor) {
        return nat.org.eclipse.swt.custom.BusyIndicator.execute(action, executor);
    }

    /**
     * If called from a {@link Display} thread use the given {@link SwtCallable} to
     * produces a {@link CompletableFuture} providing busy feedback using the busy
     * indicator while computation is running. If called from a non {@link Display}
     * the computation is performed in place and the result returned as a
     * {@link CompletableFuture}. It is therefore safe to call this method from any
     * thread and the {@link SwtCallable} is always evaluated outside the UI. The
     * {@link ForkJoinPool#commonPool()} is used to execute the computation in case
     * this is called from a {@link Display} thread
     *
     * @param action the action that should be executed and produces a result of the
     *               {@link CompletableFuture}
     *
     * @since 3.123
     */
    public static <V, E extends Exception> CompletableFuture<V> compute(SwtCallable<V, E> action) {
        return nat.org.eclipse.swt.custom.BusyIndicator.compute(action);
    }

    /**
     * If called from a {@link Display} thread use the given {@link SwtCallable} to
     * compute a {@link CompletableFuture} providing busy feedback using the busy
     * indicator while computation is running. If called from a non {@link Display}
     * the computation is performed in place and the result returned as a
     * {@link CompletableFuture}. It is therefore safe to call this method from any
     * thread and the {@link SwtCallable} is always evaluated outside the UI.
     *
     * @param action   the action that should be executed and produces a result of
     *                 the {@link CompletableFuture}
     * @param executor the Executor to perform the computation in case this is
     *                 called from a {@link Display} thread, passing a
     *                 {@link Display} will throw an
     *                 {@link IllegalArgumentException} as this will lead to a
     *                 blocking UI and violates the contract of this method
     *
     * @since 3.123
     */
    public static <V, E extends Exception> CompletableFuture<V> compute(SwtCallable<V, E> action, Executor executor) {
        return nat.org.eclipse.swt.custom.BusyIndicator.compute(action, executor);
    }

    public BusyIndicator() {
        this(new nat.org.eclipse.swt.custom.BusyIndicator());
    }

    IBusyIndicator delegate;

    protected BusyIndicator(IBusyIndicator delegate) {
        this.delegate = delegate;
        delegate.setApi(this);
    }

    public static BusyIndicator createApi(IBusyIndicator delegate) {
        return new BusyIndicator(delegate);
    }

    public IBusyIndicator getDelegate() {
        return delegate;
    }
}
