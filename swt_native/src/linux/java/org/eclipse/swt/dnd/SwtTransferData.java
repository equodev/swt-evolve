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
public class SwtTransferData implements ITransferData {

    public TransferData getApi() {
        if (api == null)
            api = TransferData.createApi(this);
        return (TransferData) api;
    }

    protected TransferData api;

    public void setApi(TransferData api) {
        this.api = api;
        if (api != null)
            api.impl = this;
    }

    public SwtTransferData(TransferData api) {
        setApi(api);
    }
}
