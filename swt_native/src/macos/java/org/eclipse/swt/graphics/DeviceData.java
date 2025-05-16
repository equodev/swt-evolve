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
package org.eclipse.swt.graphics;

public class DeviceData {

    /*
	* Debug fields - may not be honoured
	* on some SWT platforms.
	*/
    public boolean debug;

    public boolean tracking;

    public Error[] errors;

    public Object[] objects;

    public DeviceData() {
        this(new SwtDeviceData());
    }

    IDeviceData impl;

    protected DeviceData(IDeviceData impl) {
        this.impl = impl;
        impl.setApi(this);
    }

    public static DeviceData createApi(IDeviceData impl) {
        return new DeviceData(impl);
    }

    public IDeviceData getImpl() {
        return impl;
    }
}
