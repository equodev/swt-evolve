/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2016 IBM Corporation and others.
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
 * A ControlEditor is a manager for a Control that appears above a composite and tracks with the
 * moving and resizing of that composite.  It can be used to display one control above
 * another control.  This could be used when editing a control that does not have editing
 * capabilities by using a text editor or for launching a dialog by placing a button
 * above a control.
 *
 * <p> Here is an example of using a ControlEditor:</p>
 *
 * <pre><code>
 * Canvas canvas = new Canvas(shell, SWT.BORDER);
 * canvas.setBounds(10, 10, 300, 300);
 * Color color = new Color(null, 255, 0, 0);
 * canvas.setBackground(color);
 * ControlEditor editor = new ControlEditor (canvas);
 * // The editor will be a button in the bottom right corner of the canvas.
 * // When selected, it will launch a Color dialog that will change the background
 * // of the canvas.
 * Button button = new Button(canvas, SWT.PUSH);
 * button.setText("Select Color...");
 * button.addSelectionListener (new SelectionAdapter() {
 * 	public void widgetSelected(SelectionEvent e) {
 * 		ColorDialog dialog = new ColorDialog(shell);
 * 		dialog.open();
 * 		RGB rgb = dialog.getRGB();
 * 		if (rgb != null) {
 * 			if (color != null) color.dispose();
 * 			color = new Color(null, rgb);
 * 			canvas.setBackground(color);
 * 		}
 *
 * 	}
 * });
 *
 * editor.horizontalAlignment = SWT.RIGHT;
 * editor.verticalAlignment = SWT.BOTTOM;
 * editor.grabHorizontal = false;
 * editor.grabVertical = false;
 * Point size = button.computeSize(SWT.DEFAULT, SWT.DEFAULT);
 * editor.minimumWidth = size.x;
 * editor.minimumHeight = size.y;
 * editor.setEditor (button);
 * </code></pre>
 *
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 */
public class ControlEditor {

    /**
     * Specifies how the editor should be aligned relative to the control.  Allowed values
     * are SWT.LEFT, SWT.RIGHT and SWT.CENTER.  The default value is SWT.CENTER.
     */
    public int horizontalAlignment = SWT.CENTER;

    /**
     * Specifies whether the editor should be sized to use the entire width of the control.
     * True means resize the editor to the same width as the cell.  False means do not adjust
     * the width of the editor.	The default value is false.
     */
    public boolean grabHorizontal = false;

    /**
     * Specifies the minimum width the editor can have.  This is used in association with
     * a true value of grabHorizontal.  If the cell becomes smaller than the minimumWidth, the
     * editor will not made smaller than the minimum width value.  The default value is 0.
     */
    public int minimumWidth = 0;

    /**
     * Specifies how the editor should be aligned relative to the control.  Allowed values
     * are SWT.TOP, SWT.BOTTOM and SWT.CENTER.  The default value is SWT.CENTER.
     */
    public int verticalAlignment = SWT.CENTER;

    /**
     * Specifies whether the editor should be sized to use the entire height of the control.
     * True means resize the editor to the same height as the underlying control.  False means do not adjust
     * the height of the editor.	The default value is false.
     */
    public boolean grabVertical = false;

    /**
     * Specifies the minimum height the editor can have.  This is used in association with
     * a true value of grabVertical.  If the control becomes smaller than the minimumHeight, the
     * editor will not made smaller than the minimum height value.  The default value is 0.
     */
    public int minimumHeight = 0;

    /**
     * Creates a ControlEditor for the specified Composite.
     *
     * @param parent the Composite above which this editor will be displayed
     */
    public ControlEditor(Composite parent) {
        this(new nat.org.eclipse.swt.custom.ControlEditor((nat.org.eclipse.swt.widgets.Composite) parent.getDelegate()));
    }

    /**
     * Removes all associations between the Editor and the underlying composite.  The
     * composite and the editor Control are <b>not</b> disposed.
     */
    public void dispose() {
        getDelegate().dispose();
    }

    /**
     * Returns the Control that is displayed above the composite being edited.
     *
     * @return the Control that is displayed above the composite being edited
     */
    public Control getEditor() {
        return getDelegate().getEditor().getApi();
    }

    /**
     * Lays out the control within the underlying composite.  This
     * method should be called after changing one or more fields to
     * force the Editor to resize.
     *
     * @since 2.1
     */
    public void layout() {
        getDelegate().layout();
        // this doesn't work because
        // resizing the column takes the focus away
    }

    /**
     * Specify the Control that is to be displayed.
     *
     * <p>Note: The Control provided as the editor <b>must</b> be created with its parent
     * being the Composite specified in the ControlEditor constructor.
     *
     * @param editor the Control that is displayed above the composite being edited
     */
    public void setEditor(Control editor) {
        getDelegate().setEditor(editor.getDelegate());
    }

    IControlEditor delegate;

    protected ControlEditor(IControlEditor delegate) {
        this.delegate = delegate;
        delegate.setApi(this);
    }

    public IControlEditor getDelegate() {
        return delegate;
    }
}
