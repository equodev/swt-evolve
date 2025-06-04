/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2014 IBM Corporation and others.
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

import java.util.*;
import java.util.concurrent.*;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;

/**
 * Instances of this class provide synchronization support
 * for displays. A default instance is created automatically
 * for each display, and this instance is sufficient for almost
 * all applications.
 * <p>
 * <b>IMPORTANT:</b> Typical application code <em>never</em>
 * needs to deal with this class. It is provided only to
 * allow applications which require non-standard
 * synchronization behavior to plug in the support they
 * require. <em>Subclasses which override the methods in
 * this class must ensure that the superclass methods are
 * invoked in their implementations</em>
 * </p>
 *
 * @see Display#setSynchronizer
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 */
public class Synchronizer {

    /**
     * Constructs a new instance of this class.
     *
     * @param display the display to create the synchronizer on
     */
    public Synchronizer(Display display) {
        this((ISynchronizer) null);
        setImpl(new SwtSynchronizer(display));
    }

    /**
     * Causes the <code>run()</code> method of the runnable to
     * be invoked by the user-interface thread at the next
     * reasonable opportunity. The caller of this method continues
     * to run in parallel, and is not notified when the
     * runnable has completed.
     *
     * @param runnable code to run on the user-interface thread.
     *
     * @see #syncExec
     */
    protected void asyncExec(Runnable runnable) {
        getImpl().asyncExec(runnable);
    }

    /**
     * Causes the <code>run()</code> method of the runnable to
     * be invoked by the user-interface thread at the next
     * reasonable opportunity. The thread which calls this method
     * is suspended until the runnable completes.
     *
     * @param runnable code to run on the user-interface thread.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_FAILED_EXEC - if an exception occurred when executing the runnable</li>
     * </ul>
     *
     * @see #asyncExec
     */
    protected void syncExec(Runnable runnable) {
        getImpl().syncExec(runnable);
    }

    protected ISynchronizer impl;

    protected Synchronizer(ISynchronizer impl) {
        if (impl == null) {
            dev.equo.swt.Creation.creating.push(this);
        } else {
            this.impl = impl;
            impl.setApi(this);
        }
    }

    static Synchronizer createApi(ISynchronizer impl) {
        if (dev.equo.swt.Creation.creating.peek() instanceof Synchronizer inst) {
            inst.impl = impl;
            return inst;
        } else
            return new Synchronizer(impl);
    }

    public ISynchronizer getImpl() {
        return impl;
    }

    protected Synchronizer setImpl(ISynchronizer impl) {
        this.impl = impl;
        impl.setApi(this);
        dev.equo.swt.Creation.creating.pop();
        return this;
    }
}
