/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package dev.equo.nattable;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.nattable.util.ObjectUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractNatExample implements INatExample {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractNatExample.class);

    private Text outputArea;

    @Override
    public String getName() {
        return getClass().getSimpleName().replaceAll("^_[0-9]*_", "").replace('_', ' ');
    }

    @Override
    public String getShortDescription() {
        String description = getDescription();
        return description.substring(0, description.indexOf('.') + 1);
    }

    @Override
    public String getDescription() {
        String description = getResourceAsString(getClass().getSimpleName() + ".txt");
        if (description != null) {
            return description;
        } else {
            return "";
        }
    }

    @Override
    public void onStart() {
    }

    @Override
    public void onStop() {
    }

    private String getResourceAsString(String resource) {
        try (InputStream inStream = getClass().getResourceAsStream(resource)) {
            if (inStream != null) {
                StringBuilder strBuf = new StringBuilder();
                int i = -1;
                while ((i = inStream.read()) != -1) {
                    strBuf.append((char) i);
                }

                return strBuf.toString();
            }
        } catch (IOException e) {
            LOG.error("Error on reading resource {}", resource, e);
        }

        return null;
    }

    /**
     *
     * @param parent
     *            The parent composite.
     * @return The Text area at the bottom.
     */
    public Text setupTextArea(Composite parent) {
        this.outputArea = new Text(parent, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
        this.outputArea.setEditable(false);

        GridDataFactory.fillDefaults().grab(true, false).hint(0, 100).align(SWT.FILL, SWT.BEGINNING).applyTo(this.outputArea);
        return this.outputArea;
    }

    public void log(String msg) {
        if (ObjectUtils.isNotNull(this.outputArea)) {
            this.outputArea.append(msg + "\n");
            System.out.println(msg);
        }
    }
}
