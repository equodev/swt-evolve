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
 * *****************************************************************************
 */
package org.eclipse.swt.custom;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

/**
 * This Layout stacks all the controls one on top of the other and resizes all controls
 * to have the same size and location.
 * The control specified in topControl is visible and all other controls are not visible.
 * Users must set the topControl value to flip between the visible items and then call
 * layout() on the composite which has the StackLayout.
 *
 * <p> Here is an example which places ten buttons in a stack layout and
 * flips between them:
 *
 * <pre><code>
 * 	public static void main(String[] args) {
 * 		Display display = new Display();
 * 		Shell shell = new Shell(display);
 * 		shell.setLayout(new GridLayout());
 *
 * 		final Composite parent = new Composite(shell, SWT.NONE);
 * 		parent.setLayoutData(new GridData(GridData.FILL_BOTH));
 * 		final StackLayout layout = new StackLayout();
 * 		parent.setLayout(layout);
 * 		final Button[] bArray = new Button[10];
 * 		for (int i = 0; i &lt; 10; i++) {
 * 			bArray[i] = new Button(parent, SWT.PUSH);
 * 			bArray[i].setText("Button "+i);
 * 		}
 * 		layout.topControl = bArray[0];
 *
 * 		Button b = new Button(shell, SWT.PUSH);
 * 		b.setText("Show Next Button");
 * 		final int[] index = new int[1];
 * 		b.addListener(SWT.Selection, new Listener(){
 * 			public void handleEvent(Event e) {
 * 				index[0] = (index[0] + 1) % 10;
 * 				layout.topControl = bArray[index[0]];
 * 				parent.layout();
 * 			}
 * 		});
 *
 * 		shell.open();
 * 		while (shell != null &amp;&amp; !shell.isDisposed()) {
 * 			if (!display.readAndDispatch())
 * 				display.sleep();
 * 		}
 * 	}
 * </code></pre>
 *
 * @see <a href="http://www.eclipse.org/swt/snippets/#stacklayout">StackLayout snippets</a>
 * @see <a href="http://www.eclipse.org/swt/examples.php">SWT Example: LayoutExample</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 */
public class StackLayout extends Layout {

    /**
     * marginWidth specifies the number of points of horizontal margin
     * that will be placed along the left and right edges of the layout.
     *
     * The default value is 0.
     */
    public int marginWidth = 0;

    /**
     * marginHeight specifies the number of points of vertical margin
     * that will be placed along the top and bottom edges of the layout.
     *
     * The default value is 0.
     */
    public int marginHeight = 0;

    /**
     * topControl the Control that is displayed at the top of the stack.
     * All other controls that are children of the parent composite will not be visible.
     */
    public Control topControl;

    /**
     * Returns a string containing a concise, human-readable
     * description of the receiver.
     *
     * @return a string representation of the layout
     */
    public String toString() {
        return getDelegate().toString();
    }

    public StackLayout() {
        this(new nat.org.eclipse.swt.custom.StackLayout());
    }

    protected StackLayout(IStackLayout delegate) {
        super(delegate);
    }

    public IStackLayout getDelegate() {
        return (IStackLayout) super.getDelegate();
    }
}
