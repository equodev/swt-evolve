/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2018 IBM Corporation and others.
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
 *      Anton Leherbauer (Wind River Systems) - Bug 439419
 *      Angelo Zerr <angelo.zerr@gmail.com> - Customize different line spacing of StyledText - Bug 522020
 * *****************************************************************************
 */
package org.eclipse.swt.custom;

import java.util.*;
import java.util.List;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import dev.equo.swt.Config;

/**
 * A StyledTextRenderer renders the content of a StyledText widget.
 * This class can be used to render to the display or to a printer.
 */
class StyledTextRenderer {

    StyledTextRenderer(Device device, StyledText styledText) {
        this((IStyledTextRenderer) null);
        setImpl(Config.isEquo(StyledTextRenderer.class) ? new DartStyledTextRenderer(device, styledText, this) : new SwtStyledTextRenderer(device, styledText, this));
    }

    /**
     * See {@link TextLayout#setFixedLineMetrics}
     *
     * @since 3.125
     */
    public void setFixedLineMetrics(FontMetrics metrics) {
        getImpl().setFixedLineMetrics(metrics);
    }

    public boolean hasVerticalIndent() {
        return getImpl().hasVerticalIndent();
    }

    protected IStyledTextRenderer impl;

    protected StyledTextRenderer(IStyledTextRenderer impl) {
        if (impl != null)
            impl.setApi(this);
    }

    static StyledTextRenderer createApi(IStyledTextRenderer impl) {
        return new StyledTextRenderer(impl);
    }

    public IStyledTextRenderer getImpl() {
        return impl;
    }

    protected StyledTextRenderer setImpl(IStyledTextRenderer impl) {
        this.impl = impl;
        return this;
    }
}
