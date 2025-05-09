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
package nat.org.eclipse.swt.widgets;

import org.eclipse.swt.widgets.IRunnableLock;

/**
 * Instances of this class are used to ensure that an
 * application cannot interfere with the locking mechanism
 * used to implement asynchronous and synchronous communication
 * between widgets and background threads.
 */
class RunnableLock implements IRunnableLock {

    Runnable runnable;

    Thread thread;

    Throwable throwable;

    RunnableLock(Runnable runnable) {
        this.runnable = runnable;
    }

    boolean done() {
        return runnable == null || throwable != null;
    }

    void run(Display display) {
        if (runnable != null) {
            try {
                runnable.run();
            } catch (RuntimeException exception) {
                display.getRuntimeExceptionHandler().accept(exception);
            } catch (Error error) {
                display.getErrorHandler().accept(error);
            }
        }
        runnable = null;
    }

    public org.eclipse.swt.widgets.RunnableLock getApi() {
        return (org.eclipse.swt.widgets.RunnableLock) api;
    }

    org.eclipse.swt.widgets.RunnableLock api;

    public void setApi(org.eclipse.swt.widgets.RunnableLock api) {
        this.api = api;
    }
}
