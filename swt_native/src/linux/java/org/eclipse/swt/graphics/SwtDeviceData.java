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
package org.eclipse.swt.graphics;

public class SwtDeviceData implements IDeviceData {

    public DeviceData getApi() {
        if (api == null)
            api = DeviceData.createApi(this);
        return (DeviceData) api;
    }

    protected DeviceData api;

    public void setApi(DeviceData api) {
        this.api = api;
        if (api != null)
            api.impl = this;
    }

    public SwtDeviceData(DeviceData api) {
        setApi(api);
    }
}
