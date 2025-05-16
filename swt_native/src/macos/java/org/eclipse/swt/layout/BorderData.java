/**
 * ****************************************************************************
 *  Copyright (c) 2022 Christoph Läubrich and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Christoph Läubrich - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.swt.layout;

import static org.eclipse.swt.SWT.*;
import java.util.*;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

/**
 * Controls the several aspects of a {@link BorderLayout}.
 *
 * @since 3.119
 */
public final class BorderData {

    public int hHint = SWT.DEFAULT;

    public int wHint = SWT.DEFAULT;

    public int region = SWT.CENTER;

    /**
     * creates a {@link BorderData} with default options
     */
    public BorderData() {
        this(new SwtBorderData());
    }

    /**
     * creates a {@link BorderData} initialized with the given region, valid values
     * are {@link SWT#TOP}, {@link SWT#CENTER}, {@link SWT#LEFT}, {@link SWT#RIGHT},
     * {@link SWT#BOTTOM}
     *
     * @param region the region valid values are {@link SWT#TOP},
     *               {@link SWT#CENTER}, {@link SWT#LEFT}, {@link SWT#RIGHT},
     *               {@link SWT#BOTTOM}
     */
    public BorderData(int region) {
        this(new SwtBorderData(region));
    }

    /**
     * creates a {@link BorderData} initialized with the given region and width and
     * height hints
     *
     * @param region     the region valid values are {@link SWT#TOP},
     *                   {@link SWT#CENTER}, {@link SWT#LEFT}, {@link SWT#RIGHT},
     *                   {@link SWT#BOTTOM}
     * @param widthHint  the default hint for the width
     * @param heightHint he default hint for the height
     */
    public BorderData(int region, int widthHint, int heightHint) {
        this(new SwtBorderData(region, widthHint, heightHint));
    }

    public String toString() {
        return getImpl().toString();
    }

    IBorderData impl;

    protected BorderData(IBorderData impl) {
        this.impl = impl;
        impl.setApi(this);
    }

    public static BorderData createApi(IBorderData impl) {
        return new BorderData(impl);
    }

    public IBorderData getImpl() {
        return impl;
    }
}
