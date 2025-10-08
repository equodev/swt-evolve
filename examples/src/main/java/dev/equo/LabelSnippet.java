/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package dev.equo;

/*
 * Label example snippet: create a label (with an image)
 *
 * For a list of all SWT example snippets see
 * http://www.eclipse.org/swt/snippets/
 */

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class LabelSnippet {

public static void main (String[] args) {
	Display display = new Display();
	Image image = new Image(display, LabelSnippet.class.getClassLoader().getResourceAsStream("swt-evolve.png"));
	Shell shell = new Shell (display);
	shell.setText("Label Snippet");
	shell.setSize(400, 200);
	Label label = new Label (shell, SWT.BORDER);
	//label.setLocation (10, 10);
	label.setText ("Label with Image");
	label.setImage (image);
	label.setSize(200, 100);
	shell.open ();
	while (!shell.isDisposed ()) {
		if (!display.readAndDispatch ()) display.sleep ();
	}
	image.dispose ();
	display.dispose ();
}

}
