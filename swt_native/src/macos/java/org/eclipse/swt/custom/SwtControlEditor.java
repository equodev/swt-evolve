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
public class SwtControlEditor implements IControlEditor {

    Composite parent;

    Control editor;

    private boolean hadFocus;

    private Listener controlListener;

    private Listener scrollbarListener;

    private final static int[] EVENTS = { SWT.KeyDown, SWT.KeyUp, SWT.MouseDown, SWT.MouseUp, SWT.Resize };

    /**
     * Creates a ControlEditor for the specified Composite.
     *
     * @param parent the Composite above which this editor will be displayed
     */
    public SwtControlEditor(Composite parent) {
        this.parent = parent;
        controlListener = e -> layout();
        for (int event : EVENTS) {
            parent.addListener(event, controlListener);
        }
        scrollbarListener = this::scroll;
        ScrollBar hBar = parent.getHorizontalBar();
        if (hBar != null)
            hBar.addListener(SWT.Selection, scrollbarListener);
        ScrollBar vBar = parent.getVerticalBar();
        if (vBar != null)
            vBar.addListener(SWT.Selection, scrollbarListener);
    }

    Rectangle computeBounds() {
        Rectangle clientArea = parent.getClientArea();
        Rectangle editorRect = new Rectangle(clientArea.x, clientArea.y, getApi().minimumWidth, getApi().minimumHeight);
        if (getApi().grabHorizontal)
            editorRect.width = Math.max(clientArea.width, getApi().minimumWidth);
        if (getApi().grabVertical)
            editorRect.height = Math.max(clientArea.height, getApi().minimumHeight);
        switch(getApi().horizontalAlignment) {
            case SWT.RIGHT:
                editorRect.x += clientArea.width - editorRect.width;
                break;
            case SWT.LEFT:
                // do nothing - clientArea.x is the right answer
                break;
            default:
                // default is CENTER
                editorRect.x += (clientArea.width - editorRect.width) / 2;
        }
        switch(getApi().verticalAlignment) {
            case SWT.BOTTOM:
                editorRect.y += clientArea.height - editorRect.height;
                break;
            case SWT.TOP:
                // do nothing - clientArea.y is the right answer
                break;
            default:
                // default is CENTER
                editorRect.y += (clientArea.height - editorRect.height) / 2;
        }
        return editorRect;
    }

    /**
     * Removes all associations between the Editor and the underlying composite.  The
     * composite and the editor Control are <b>not</b> disposed.
     */
    public void dispose() {
        if (parent != null && !parent.isDisposed()) {
            for (int event : EVENTS) {
                parent.removeListener(event, controlListener);
            }
            ScrollBar hBar = parent.getHorizontalBar();
            if (hBar != null)
                hBar.removeListener(SWT.Selection, scrollbarListener);
            ScrollBar vBar = parent.getVerticalBar();
            if (vBar != null)
                vBar.removeListener(SWT.Selection, scrollbarListener);
        }
        parent = null;
        editor = null;
        hadFocus = false;
        controlListener = null;
        scrollbarListener = null;
    }

    /**
     * Returns the Control that is displayed above the composite being edited.
     *
     * @return the Control that is displayed above the composite being edited
     */
    public Control getEditor() {
        return editor;
    }

    /**
     * Lays out the control within the underlying composite.  This
     * method should be called after changing one or more fields to
     * force the Editor to resize.
     *
     * @since 2.1
     */
    public void layout() {
        if (editor == null || editor.isDisposed())
            return;
        if (editor.getVisible()) {
            hadFocus = editor.isFocusControl();
        }
        // this doesn't work because
        // resizing the column takes the focus away
        // before we get here
        editor.setBounds(computeBounds());
        if (hadFocus) {
            if (editor == null || editor.isDisposed())
                return;
            editor.setFocus();
        }
    }

    void scroll(Event e) {
        if (editor == null || editor.isDisposed())
            return;
        layout();
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
        if (editor == null) {
            // this is the case where the caller is setting the editor to be blank
            // set all the values accordingly
            this.editor = null;
            return;
        }
        this.editor = editor;
        layout();
        if (this.editor == null || this.editor.isDisposed())
            return;
        editor.setVisible(true);
    }

    public ControlEditor getApi() {
        if (api == null)
            api = ControlEditor.createApi(this);
        return (ControlEditor) api;
    }

    protected ControlEditor api;

    public void setApi(ControlEditor api) {
        this.api = api;
    }
}
