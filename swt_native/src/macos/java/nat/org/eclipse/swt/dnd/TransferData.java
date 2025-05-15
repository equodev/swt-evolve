/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2010 IBM Corporation and others.
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
package nat.org.eclipse.swt.dnd;

import org.eclipse.swt.internal.cocoa.*;
import org.eclipse.swt.dnd.ITransferData;

/**
 * The <code>TransferData</code> class is a platform specific data structure for
 * describing the type and the contents of data being converted by a transfer agent.
 *
 * <p>As an application writer, you do not need to know the specifics of
 * TransferData.  TransferData instances are passed to a subclass of Transfer
 * and the Transfer object manages the platform specific issues.
 * You can ask a Transfer subclass if it can handle this data by calling
 * Transfer.isSupportedType(transferData).</p>
 *
 * <p>You should only need to become familiar with the fields in this class if you
 * are implementing a Transfer subclass and you are unable to subclass the
 * ByteArrayTransfer class.</p>
 *
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 */
public class TransferData implements ITransferData {

    /**
     * The type is a unique identifier of a system format or user defined format.
     * (Warning: This field is platform dependent)
     * <p>
     * <b>IMPORTANT:</b> This field is <em>not</em> part of the SWT
     * public API. It is marked public only so that it can be shared
     * within the packages provided by SWT. It is not available on all
     * platforms and should never be accessed from application code.
     * </p>
     *
     * @noreference This field is not intended to be referenced by clients.
     */
    public int type;

    /**
     * The data being transferred.
     * The data field may contain multiple values.
     * (Warning: This field is platform dependent)
     * <p>
     * <b>IMPORTANT:</b> This field is <em>not</em> part of the SWT
     * public API. It is marked public only so that it can be shared
     * within the packages provided by SWT. It is not available on all
     * platforms and should never be accessed from application code.
     * </p>
     *
     * @noreference This field is not intended to be referenced by clients.
     */
    public NSObject data;

    public org.eclipse.swt.dnd.TransferData getApi() {
        if (api == null)
            api = org.eclipse.swt.dnd.TransferData.createApi(this);
        return (org.eclipse.swt.dnd.TransferData) api;
    }

    protected org.eclipse.swt.dnd.TransferData api;

    public void setApi(org.eclipse.swt.dnd.TransferData api) {
        this.api = api;
    }

    public static TransferData safeDelegate(org.eclipse.swt.dnd.TransferData api) {
        return (api != null) ? (TransferData) api.getDelegate() : null;
    }
}
