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
package nat.org.eclipse.swt.graphics;

import org.eclipse.swt.graphics.IDeviceData;

public class DeviceData implements IDeviceData {

    /*
	* Debug fields - may not be honoured
	* on some SWT platforms.
	*/
    public boolean debug;

    public boolean tracking;

    public Error[] errors;

    public Object[] objects;

    public org.eclipse.swt.graphics.DeviceData getApi() {
        if (api == null)
            api = org.eclipse.swt.graphics.DeviceData.createApi(this);
        return (org.eclipse.swt.graphics.DeviceData) api;
    }

    protected org.eclipse.swt.graphics.DeviceData api;

    public void setApi(org.eclipse.swt.graphics.DeviceData api) {
        this.api = api;
    }

    public static DeviceData safeDelegate(org.eclipse.swt.graphics.DeviceData api) {
        return (api != null) ? (DeviceData) api.getDelegate() : null;
    }
}
