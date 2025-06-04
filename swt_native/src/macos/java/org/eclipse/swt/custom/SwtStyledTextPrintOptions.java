/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2008 IBM Corporation and others.
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
package org.eclipse.swt.custom;

/**
 * Use StyledTextPrintOptions to specify printing options for the
 * StyledText.print(Printer, StyledTextPrintOptions) API.
 * <p>
 * The following example prints a right aligned page number in the footer,
 * sets the job name to "Example" and prints line background colors but no other
 * formatting:
 * </p>
 * <pre>
 * StyledTextPrintOptions options = new StyledTextPrintOptions();
 * options.footer = "\t\t&lt;page&gt;";
 * options.jobName = "Example";
 * options.printLineBackground = true;
 *
 * Runnable runnable = styledText.print(new Printer(), options);
 * runnable.run();
 * </pre>
 *
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 *
 * @since 2.1
 */
public class SwtStyledTextPrintOptions implements IStyledTextPrintOptions {

    public StyledTextPrintOptions getApi() {
        if (api == null)
            api = StyledTextPrintOptions.createApi(this);
        return (StyledTextPrintOptions) api;
    }

    protected StyledTextPrintOptions api;

    public void setApi(StyledTextPrintOptions api) {
        this.api = api;
    }
}
