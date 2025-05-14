package org.eclipse.swt.widgets;

import java.util.*;
import java.util.concurrent.*;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;

public interface ISynchronizer {

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
    void asyncExec(Runnable runnable);

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
    void syncExec(Runnable runnable);

    Synchronizer getApi();

    void setApi(Synchronizer api);
}
