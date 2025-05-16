package org.eclipse.swt.custom;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public interface IControlEditor {

    /**
     * Removes all associations between the Editor and the underlying composite.  The
     * composite and the editor Control are <b>not</b> disposed.
     */
    void dispose();

    /**
     * Returns the Control that is displayed above the composite being edited.
     *
     * @return the Control that is displayed above the composite being edited
     */
    Control getEditor();

    /**
     * Lays out the control within the underlying composite.  This
     * method should be called after changing one or more fields to
     * force the Editor to resize.
     *
     * @since 2.1
     */
    void layout();

    /**
     * Specify the Control that is to be displayed.
     *
     * <p>Note: The Control provided as the editor <b>must</b> be created with its parent
     * being the Composite specified in the ControlEditor constructor.
     *
     * @param editor the Control that is displayed above the composite being edited
     */
    void setEditor(Control editor);

    ControlEditor getApi();

    void setApi(ControlEditor api);
}
