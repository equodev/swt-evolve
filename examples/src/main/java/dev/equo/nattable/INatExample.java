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

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public interface INatExample {

    public static final String BASE_PACKAGE = "org.eclipse.nebula.widgets.nattable.examples";

    public static final String CLASSIC_BASE_PACKAGE = BASE_PACKAGE
            + ".examples";

    public static final String BASE_PATH = "/org/eclipse/nebula/widgets/nattable/examples";

    public static final String CLASSIC_BASE_PATH = BASE_PATH + "/examples";

    public static final String TUTORIAL_EXAMPLES_PREFIX = "Tutorial Examples/";

    public static final String CLASSIC_EXAMPLES_PREFIX = "Classic Examples/";

    public String getName();

    public String getShortDescription();

    public String getDescription();

    public Control createExampleControl(Composite parent);

    public void onStart();

    public void onStop();

}
