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
        this((IDeviceData) null);
        setImpl(new SwtDeviceData());
    }

    protected IDeviceData impl;

    protected DeviceData(IDeviceData impl) {
        if (impl == null) {
            dev.equo.swt.Creation.creating.push(this);
        } else {
            this.impl = impl;
            impl.setApi(this);
        }
    }

    static DeviceData createApi(IDeviceData impl) {
        if (dev.equo.swt.Creation.creating.peek() instanceof DeviceData inst) {
            inst.impl = impl;
            return inst;
        } else
            return new DeviceData(impl);
    }

    public IDeviceData getImpl() {
        return impl;
    }

    protected DeviceData setImpl(IDeviceData impl) {
        this.impl = impl;
        impl.setApi(this);
        dev.equo.swt.Creation.creating.pop();
        return this;
    }
}
