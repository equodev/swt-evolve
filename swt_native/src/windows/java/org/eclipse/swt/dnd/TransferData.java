/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2012 IBM Corporation and others.
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
package org.eclipse.swt.dnd;

import org.eclipse.swt.internal.ole.win32.*;

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
public class TransferData {

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
     * The formatetc structure is a generalized data transfer format, enhanced to
     * encompass a target device, the aspect, or view of the data, and
     * a storage medium.
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
    public FORMATETC formatetc;

    /**
     * The stgmedium structure is a generalized global memory handle used for
     * data transfer operations.
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
    public STGMEDIUM stgmedium;

    /**
     * The result field contains the result of converting a
     * java data type into a platform specific value.
     * (Warning: This field is platform dependent)
     * <p>
     * <b>IMPORTANT:</b> This field is <em>not</em> part of the SWT
     * public API. It is marked public only so that it can be shared
     * within the packages provided by SWT. It is not available on all
     * platforms and should never be accessed from application code.
     * </p>
     * <p>The value of result is 1 if the conversion was successful.
     * The value of result is 0 if the conversion failed.</p>
     *
     * @noreference This field is not intended to be referenced by clients.
     */
    public int result = COM.E_FAIL;

    /**
     * The pIDataObject is the address of an IDataObject OLE Interface which
     * provides access to the data associated with the transfer.
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
    public long pIDataObject;

    public TransferData() {
        this((ITransferData) null);
        setImpl(new SwtTransferData(this));
    }

    protected ITransferData impl;

    protected TransferData(ITransferData impl) {
        if (impl != null)
            impl.setApi(this);
    }

    static TransferData createApi(ITransferData impl) {
        return new TransferData(impl);
    }

    public ITransferData getImpl() {
        return impl;
    }

    protected TransferData setImpl(ITransferData impl) {
        this.impl = impl;
        return this;
    }
}
